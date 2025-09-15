<template>
  <div class="tiktok-container" ref="container"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove"
    @touchend="handleTouchEnd"
    @wheel="handleWheel">
    
    <div class="videos-wrapper" :style="{ transform: `translateY(-${currentIndex * 100}vh)` }">
      <div v-for="(video, index) in videos" :key="video.id" class="video-item">
        <video 
          :width="VideoWidth"
          :height="VideoHeight" 
          :src="video.url"
          muted
          playsinline
          ref="videoPlayers"
          @loadeddata="onVideoLoaded"
          @canplay="onVideoCanPlay"
        ></video>
        <div class="video-info">
          <div class="video-title">{{ video.title }}</div>
          <div class="video-author">
            <div class="author-avatar"></div>
            @{{ video.author }}
          </div>
        </div>
      </div>
    </div>
    
    <div class="controls">
      <div class="control-btn">
        <div class="control-icon">❤️</div>
        <div>{{ currentVideo.likes }}</div>
      </div>
      <div class="control-btn">
        <div class="control-icon">💬</div>
        <div>{{ currentVideo.comments }}</div>
      </div>
      <div class="control-btn">
        <div class="control-icon">↗️</div>
        <div>分享</div>
      </div>
    </div>
    
    <div class="loading-indicator" v-if="isLoading"></div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, nextTick } from 'vue'

// 视频尺寸
const VideoWidth = ref(1300);
const VideoHeight = ref(700);

// 视频数据
const videos = ref([
  {
    id: 1,
    url: 'https://pluer.oss-cn-guangzhou.aliyuncs.com/uploads/2023-05-06%20235244.mp4',
    title: '美丽风景视频，大自然的奇迹',
    author: '旅行爱好者',
    likes: 2456,
    comments: 134
  },
  {
    id: 2,
    url: 'https://pluer.oss-cn-guangzhou.aliyuncs.com/uploads/C0054.MP4',
    title: '城市夜景延时摄影，灯火辉煌',
    author: '城市探索者',
    likes: 3821,
    comments: 287
  },
  {
    id: 3,
    url: 'https://pluer.oss-cn-guangzhou.aliyuncs.com/uploads/C0055.MP4',
    title: '可爱猫咪的日常，萌化你的心',
    author: '宠物日记',
    likes: 12567,
    comments: 842
  }
]);

const currentIndex = ref(0);
const isSwiping = ref(false);
const startY = ref(0);
const currentY = ref(0);
const isLoading = ref(true);
const container = ref<HTMLElement | null>(null);
const videoPlayers = ref<HTMLVideoElement[]>([]);
const hasSwiped = ref(false); // 标记是否已经滑动过一次

// 当前视频
const currentVideo = computed(() => videos.value[currentIndex.value]);

// 处理触摸开始事件
const handleTouchStart = (e: TouchEvent) => {
  isSwiping.value = true;
  startY.value = e.touches[0].clientY;
  hasSwiped.value = false; // 重置滑动标记
};

// 处理触摸移动事件
const handleTouchMove = (e: TouchEvent) => {
  if (!isSwiping.value || hasSwiped.value) return;
  
  currentY.value = e.touches[0].clientY;
  const diff = currentY.value - startY.value;
  
  // 限制最大滑动距离
  if (Math.abs(diff) > 80) {
    isSwiping.value = false;
    hasSwiped.value = true; // 标记已经滑动过一次
    handleSwipe(diff > 0 ? 'down' : 'up');
    
    // 添加一个短暂延迟后重置滑动标记，允许再次滑动
    setTimeout(() => {
      hasSwiped.value = false;
    }, 300);
  }
};

// 处理触摸结束事件
const handleTouchEnd = () => {
  isSwiping.value = false;
  // 触摸结束后立即重置滑动标记，允许再次滑动
  hasSwiped.value = false;
};

// 处理滚轮事件
const handleWheel = (e: WheelEvent) => {
  if (hasSwiped.value) return; // 如果已经滑动过，不再处理
  
  if (e.deltaY > 50) {
    hasSwiped.value = true; // 标记已经滑动过一次
    handleSwipe('up');
    
    // 添加一个短暂延迟后重置滑动标记，允许再次滑动
    setTimeout(() => {
      hasSwiped.value = false;
    }, 300);
  } else if (e.deltaY < -50) {
    hasSwiped.value = true; // 标记已经滑动过一次
    handleSwipe('down');
    
    // 添加一个短暂延迟后重置滑动标记，允许再次滑动
    setTimeout(() => {
      hasSwiped.value = false;
    }, 300);
  }
};

// 处理滑动
const handleSwipe = (direction: string) => {
  if (direction === 'up') {
    // 向上滑动，播放下一个视频
    if (currentIndex.value < videos.value.length - 1) {
      pauseCurrentVideo();
      currentIndex.value++;
      playCurrentVideo();
    }
  } else if (direction === 'down') {
    // 向下滑动，播放上一个视频
    if (currentIndex.value > 0) {
      pauseCurrentVideo();
      currentIndex.value--;
      playCurrentVideo();
    }
  }
};

// 播放当前视频
const playCurrentVideo = () => {
  nextTick(() => {
    if (videoPlayers.value[currentIndex.value]) {
      videoPlayers.value[currentIndex.value].play().catch(e => {
        console.log("Autoplay prevented:", e);
      });
    }
  });
};

// 暂停当前视频
const pauseCurrentVideo = () => {
  if (videoPlayers.value[currentIndex.value]) {
    videoPlayers.value[currentIndex.value].pause();
  }
};

// 视频加载完成
const onVideoLoaded = () => {
  console.log("Video loaded");
};

// 视频可以播放
const onVideoCanPlay = () => {
  isLoading.value = false;
  // 自动播放当前视频
  playCurrentVideo();
};

onMounted(() => {
  // 设置容器高度
  if (container.value) {
    container.value.style.height = `${window.innerHeight}px`;
  }
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (container.value) {
      container.value.style.height = `${window.innerHeight}px`;
    }
  });
});
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.tiktok-container {
  position: relative;
  width: 100%;
  overflow: hidden;
  background: #000;
}

.videos-wrapper {
  position: relative;
  width: 100%;
  transition: transform 0.3s ease-out;
}

.video-item {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #000;
}

.video-item video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-info {
  position: absolute;
  bottom: 80px;
  left: 16px;
  right: 16px;
  z-index: 10;
}

.video-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0,0,0,0.5);
}

.video-author {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #fff;
  opacity: 0.9;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 10px;
  background: linear-gradient(45deg, #ff0050, #ffd900);
}

.loading-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 20;
  width: 50px;
  height: 50px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

.controls {
  position: absolute;
  right: 16px;
  bottom: 80px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  z-index: 15;
}

.control-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #fff;
  font-size: 12px;
}

.control-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}
</style>