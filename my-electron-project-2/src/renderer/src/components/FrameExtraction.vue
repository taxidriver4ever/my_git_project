<template>
    <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh;">
        <div style="color: white;">作品上传</div>
        
        <el-upload
            class="upload-demo"
            drag
            :auto-upload="false"
            :on-change="handleFileChange"
            :show-file-list="false"
            accept="video/*"
        >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
                Drop video file here or <em>click to upload</em>
            </div>
            <template #tip>
                <div class="el-upload__tip">
                    mp4/mov/avi files
                </div>
            </template>
        </el-upload>

        <div v-if="selectedFile" class="file-info">
            <div class="file-name">已选择: {{ selectedFile.name }}</div>
            <div class="file-size">文件大小: {{ formatFileSize(selectedFile.size) }}</div>
            <div v-if="isExtracting" style="color: #409EFF; margin: 10px 0;">
                正在加载中... {{ Math.round(extractProgress) }}%
            </div>
            <el-button 
                type="danger" 
                size="small" 
                @click="removeSelectedFile"
                class="delete-btn"
                :icon="Delete"
            >
                删除
            </el-button>
        </div>
        
        <br />
        
        <el-button 
            type="primary" 
            @click="SendToGetSTS" 
            :disabled="isLoading || selectedFile === null || isExtracting"
            :loading="isLoading"
            style="margin-bottom: 100px;"
        >
            {{ isLoading ? '上传中...' : '上传视频和帧图片' }}
        </el-button>
    </div>
</template>

<script setup>
import { ElButton, ElUpload, ElIcon, ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';
import { ref } from 'vue';
import OSS from 'ali-oss';
import { UploadFilled, Delete } from '@element-plus/icons-vue'

const stsToken = ref("");
const region = ref("oss-cn-guangzhou");
const bucket = ref("pluer");
const accessKeyId = ref("");
const accessKeySecret = ref("");
const selectedFile = ref(null);
const isLoading = ref(false);
const isExtracting = ref(false);
const extractProgress = ref(0);
const frames = ref([]);

// 格式化文件大小
const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// 清理资源
const cleanupResources = () => {
    // 清理blob URLs
    frames.value.forEach(frame => {
        URL.revokeObjectURL(frame.url);
    });
    selectedFile.value = null;
    frames.value = [];
    isExtracting.value = false;
    extractProgress.value = 0;
};

// 删除选中的文件
const removeSelectedFile = () => {
    ElMessageBox.confirm(
        '确定要删除选中的视频文件吗？',
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(() => {
        cleanupResources();
        ElMessage.success('已删除选中的视频文件');
    }).catch(() => {
        // 用户取消删除
    });
};

// 处理文件选择变化事件
const handleFileChange = async (file, fileList) => {
    cleanupResources();
    
    if (file) {
        if (!file.raw.type.startsWith('video/')) {
            ElMessage.warning('请选择视频文件');
            return;
        }
        
        selectedFile.value = file.raw;
        // 开始抽取帧
        await extractVideoFrames(selectedFile.value);
    }
};

// 抽取视频帧
const extractVideoFrames = async (videoFile) => {
    isExtracting.value = true;
    extractProgress.value = 0;
    frames.value = [];
    
    return new Promise((resolve) => {
        const videoElement = document.createElement('video');
        videoElement.muted = true;
        videoElement.playsInline = true;
        videoElement.crossOrigin = 'anonymous';
        
        const objectUrl = URL.createObjectURL(videoFile);
        videoElement.src = objectUrl;
        
        videoElement.addEventListener('loadedmetadata', () => {
            const duration = videoElement.duration;
            const frameCount = 5;
            const interval = duration / (frameCount + 1);
            
            const canvas = document.createElement('canvas');
            const context = canvas.getContext('2d');
            let framesCaptured = 0;
            
            const captureFrame = (time) => {
                return new Promise((frameResolve) => {
                    videoElement.currentTime = time;
                    
                    videoElement.onseeked = () => {
                        canvas.width = videoElement.videoWidth;
                        canvas.height = videoElement.videoHeight;
                        context.drawImage(videoElement, 0, 0, canvas.width, canvas.height);
                        
                        canvas.toBlob((blob) => {
                            const frameUrl = URL.createObjectURL(blob);
                            frames.value.push({
                                url: frameUrl,
                                time: time.toFixed(1),
                                blob: blob
                            });
                            
                            framesCaptured++;
                            extractProgress.value = (framesCaptured / frameCount) * 100;
                            frameResolve();
                        }, 'image/jpeg', 0.8);
                    };
                });
            };
            
            // 按时间点捕获帧
            const captureFrames = async () => {
                for (let i = 1; i <= frameCount; i++) {
                    const time = i * interval;
                    await captureFrame(time);
                }
                
                isExtracting.value = false;
                URL.revokeObjectURL(objectUrl);
                ElMessage.success(`成功抽取 ${frameCount} 帧`);
                resolve();
            };
            
            captureFrames();
        });
        
        videoElement.load();
    });
};

async function SendToGetSTS() {
    if (!selectedFile.value) {
        ElMessage.warning('请先选择视频文件');
        return;
    }
    
    isLoading.value = true;
    
    try {
        const response = await axios({
            url: 'http://localhost:8080/sts/token',
            method: 'get',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        stsToken.value = response.data.stsToken;
        accessKeyId.value = response.data.accessKeyId;
        accessKeySecret.value = response.data.accessKeySecret;
        bucket.value = response.data.bucket;
        region.value = response.data.region;
        
        await uploadFileToOSS();
        
    } catch (error) {
        console.error('Error fetching STS token:', error);
        ElMessage.error('获取STS token失败');
        isLoading.value = false;
    }
}

async function uploadFileToOSS() {
    try {
        const client = new OSS({
            region: region.value,
            accessKeyId: accessKeyId.value,
            accessKeySecret: accessKeySecret.value,
            stsToken: stsToken.value,
            bucket: bucket.value,
            endpoint: 'https://oss-cn-guangzhou.aliyuncs.com',
        });
        
        const timestamp = Date.now();
        
        // 上传视频文件
        const videoFileName = `uploads/${timestamp}_${selectedFile.value.name}`;
        const videoResult = await client.put(videoFileName, selectedFile.value);
        
        // 上传帧图片
        let framesUploaded = 0;
        if (frames.value.length > 0) {
            for (let i = 0; i < frames.value.length; i++) {
                const frame = frames.value[i];
                const frameFileName = `uploads/frames/${timestamp}_frame_${i + 1}.jpg`;
                
                try {
                    await client.put(frameFileName, frame.blob);
                    framesUploaded++;
                } catch (error) {
                    console.error(`Error uploading frame ${i + 1}:`, error);
                }
            }
        }
        
        ElMessage.success(`上传成功! 视频和 ${framesUploaded} 张帧图片已上传`);
        cleanupResources();
        
    } catch (error) {
        console.error('Error uploading file:', error);
        ElMessage.error('上传失败');
    } finally {
        isLoading.value = false;
    }
}
</script>

<style scoped>
.upload-demo {
    margin-top: 20px;
}

:deep(.el-upload) {
    width: 660px;
}

:deep(.el-upload-dragger) {
    width: 660px;
    height: 380px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}

.file-info {
    margin-top: 16px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;
    border: 1px solid #e4e7ed;
    text-align: center;
    min-width: 300px;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.file-name {
    font-weight: 600;
    color: #303133;
    word-break: break-all;
}

.file-size {
    font-size: 12px;
    color: #909399;
    font-style: italic;
}

.delete-btn {
    margin-top: 8px;
}
</style>