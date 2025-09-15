package org.example.tiktokproject.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import org.example.tiktokproject.mapper.VideoMapper;
import org.example.tiktokproject.pojo.*;
import org.example.tiktokproject.repository.UserESRepository;
import org.example.tiktokproject.repository.VideoRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@RabbitListener(queues = "SendPhoto.direct.queue")
public class SendPhotoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SendPhotoConsumer.class);
    private static final int LOCK_WAIT_TIME = 3;
    private static final int LOCK_LEASE_TIME = 30;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VideoRepository videoRepository;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private UserESRepository userESRepository;

    @Resource
    private RedissonClient redisson;

    @RabbitHandler
    public void receiveMessage(PhotoAndVideo photoAndVideo,
                               Channel channel,
                               org.springframework.amqp.core.Message amqpMessage) {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        String videoUrl = photoAndVideo.getVideo();
        String messageId = amqpMessage.getMessageProperties().getMessageId();

        try {
            // 使用消息ID或组合键作为锁key，确保同一消息不会被并发处理
            String lockKey = "message_process_lock:" + (messageId != null ? messageId : videoUrl + System.currentTimeMillis());
            RLock lock = redisson.getLock(lockKey);

            boolean lockAcquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);

            if (!lockAcquired) {
                logger.warn("Failed to acquire lock for message: {}, will retry", messageId);
                channel.basicNack(deliveryTag, false, true); // 重试
                return;
            }

            try {
                // 处理视频业务逻辑
                processVideo(photoAndVideo, channel, amqpMessage);

            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

        }catch (Exception e) {
            logger.error("Error processing message: {}", messageId, e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception nackEx) {
                logger.error("Failed to NACK message: {}", nackEx.getMessage());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void processVideo(PhotoAndVideo photoAndVideo,
                              Channel channel,
                              org.springframework.amqp.core.Message amqpMessage) throws Exception {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();

        // 1. 创建识别请求
        List<RecognitionRequest> recognitionRequests = Arrays.asList(
                createRecognitionRequest(photoAndVideo.getPhotoA()),
                createRecognitionRequest(photoAndVideo.getPhotoB()),
                createRecognitionRequest(photoAndVideo.getPhotoC()),
                createRecognitionRequest(photoAndVideo.getPhotoD()),
                createRecognitionRequest(photoAndVideo.getPhotoE())
        );

        // 2. 发送识别请求到Python服务
        DetectionResponse detectionResponse = sendRecognitionRequest(recognitionRequests);

        // 3. 处理响应并确认消息
        if (detectionResponse != null) {
            // 4. 保存视频数据到数据库（允许重复URL）
            saveVideoData(photoAndVideo, detectionResponse);

            // 5. 确认消息（在处理完成后）
            channel.basicAck(deliveryTag, false);
            logger.info("Successfully processed and saved video: {}", photoAndVideo.getVideo());
        } else {
            throw new RuntimeException("Failed to get valid detection response");
        }
    }

    private DetectionResponse sendRecognitionRequest(List<RecognitionRequest> requests) throws Exception {
        String requestBody = objectMapper.writeValueAsString(requests);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8000/api/BatchVideoRecognition",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            logger.debug("Successfully received response from Python service");
            return objectMapper.readValue(response.getBody(), DetectionResponse.class);
        } else {
            throw new RuntimeException("Python service returned status: " + response.getStatusCode());
        }
    }

    private void saveVideoData(PhotoAndVideo photoAndVideo, DetectionResponse detectionResponse) throws Exception {
        // 1. 创建Video对象
        Video video = createVideo(photoAndVideo, detectionResponse);

        // 2. 创建MySQLVideo对象
        MySQLVideo mySQLVideo = convertToMySQLVideo(video);

        // 3. 从ES查询作者信息
        String author = determineAuthorFromDescription(video.getDescription());
        mySQLVideo.setAuthor(author);
        video.setAuthor(author);

        // 4. 保存到数据库（允许重复URL）
        videoMapper.insertVideo(mySQLVideo);
        videoRepository.save(video);

        logger.info("Saved video to database - URL: {}, Title: {}, Author: {}",
                video.getUrl(), video.getTitle(), video.getAuthor());
    }

    private Video createVideo(PhotoAndVideo photoAndVideo, DetectionResponse detectionResponse) {
        ConcurrentHashMap<String, Integer> objectMap = new ConcurrentHashMap<>();
        StringBuilder descriptionBuilder = new StringBuilder();

        // 安全地处理识别结果
        if (detectionResponse.getResults() != null) {
            for (int i = 0; i < Math.min(5, detectionResponse.getResults().size()); i++) {
                var result = detectionResponse.getResults().get(i);
                if (result.getPredictions() != null && !result.getPredictions().isEmpty()) {
                    var prediction = result.getPredictions().getFirst();
                    if (prediction.getObject_zh() != null) {
                        String objectZh = prediction.getObject_zh();
                        if (!objectMap.containsKey(objectZh)) {
                            objectMap.put(objectZh, 1);
                            descriptionBuilder.append(objectZh).append(" ");
                        }
                    }
                }
            }
        }

        Video video = new Video();
        video.setUrl(photoAndVideo.getVideo());
        video.setTitle(photoAndVideo.getTitle());
        video.setAuthor(photoAndVideo.getAuthor());
        video.setDescription(descriptionBuilder.toString().trim());
        video.setCoverUrl(photoAndVideo.getPhotoA());

        return video;
    }

    private String determineAuthorFromDescription(String description) {
        try {
            List<ESUser> users = userESRepository.findByUserName(description);
            if (users != null && !users.isEmpty() && users.getFirst() != null) {
                return users.getFirst().getUserName();
            }
        } catch (Exception e) {
            logger.warn("Error querying ES for author: {}", e.getMessage());
        }
        // 如果ES查询失败，使用原始作者或默认值
        return "Unknown Author";
    }

    private MySQLVideo convertToMySQLVideo(Video video) {
        MySQLVideo mySQLVideo = new MySQLVideo();
        mySQLVideo.setTitle(video.getTitle());
        mySQLVideo.setDescription(video.getDescription());
        mySQLVideo.setUrl(video.getUrl());
        mySQLVideo.setPublished(video.getPublished());
        mySQLVideo.setAuthor(video.getAuthor());
        mySQLVideo.setCoverUrl(video.getCoverUrl());
        return mySQLVideo;
    }

    private RecognitionRequest createRecognitionRequest(String imageUrl) {
        RecognitionRequest request = new RecognitionRequest();
        request.setImage_url(imageUrl);
        request.setTop_k("1");
        request.setTimeout("1");
        return request;
    }
}