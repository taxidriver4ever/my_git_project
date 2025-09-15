package org.example.tiktokproject.controller;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/sts")
public class StsTokenController {

    @Value("${aliyun.sts.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.sts.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.sts.roleArn}")
    private String roleArn;

    @Value("${aliyun.sts.roleSessionName:tiktok-app}")
    private String roleSessionName;

    @Value("${aliyun.sts.region:cn-hangzhou}") // STS使用杭州区域（固定）
    private String stsRegion;

    @Value("${aliyun.oss.region:cn-guangzhou}") // OSS区域（广州）
    private String ossRegion;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    @Value("${aliyun.oss.endpoint:oss-cn-guangzhou.aliyuncs.com}")
    private String endpoint;

    @GetMapping("/token")
    public ResponseEntity<STSTokenResponse> getSTSToken() {
        try {
            // 配置STS客户端 - 使用STS区域（杭州）
            DefaultProfile profile = DefaultProfile.getProfile(stsRegion, accessKeyId, accessKeySecret);
            IAcsClient client = new DefaultAcsClient(profile);

            // 构建请求
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setSysRegionId(stsRegion); // 设置STS区域，不是OSS区域
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setDurationSeconds(3600L);

            // 设置权限策略（允许操作OSS）
            String policy = String.format(
                    "{\"Version\":\"1\",\"Statement\":[" +
                            "{\"Effect\":\"Allow\",\"Action\":[" +
                            "\"oss:PutObject\"," +
                            "\"oss:GetObject\"," +
                            "\"oss:DeleteObject\"," +
                            "\"oss:ListObjects\"" +
                            "],\"Resource\":[" +
                            "\"acs:oss:*:*:%s\"," +
                            "\"acs:oss:*:*:%s/*\"" +
                            "]}]}",
                    bucket, bucket
            );
            request.setPolicy(policy);

            // 获取临时凭证
            AssumeRoleResponse response = client.getAcsResponse(request);
            AssumeRoleResponse.Credentials credentials = response.getCredentials();

            // 构建返回结果
            STSTokenResponse tokenResponse = new STSTokenResponse();
            tokenResponse.setAccessKeyId(credentials.getAccessKeyId());
            tokenResponse.setAccessKeySecret(credentials.getAccessKeySecret());
            tokenResponse.setStsToken(credentials.getSecurityToken());
            tokenResponse.setExpiration(credentials.getExpiration());
            tokenResponse.setRegion(ossRegion); // 返回OSS区域给前端
            tokenResponse.setBucket(bucket);
            tokenResponse.setEndpoint(endpoint);

            return ResponseEntity.ok(tokenResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Data
    public static class STSTokenResponse {
        private String accessKeyId;
        private String accessKeySecret;
        private String stsToken;
        private String expiration;
        private String region;    // OSS区域
        private String bucket;
        private String endpoint;  // OSS端点
    }
}