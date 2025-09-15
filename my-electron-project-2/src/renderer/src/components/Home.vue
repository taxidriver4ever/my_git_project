<template>
    <div style="background-color: rgb(22, 24, 35);height: 100vh;">
        <ElContainer>
            <ElHeader style="margin: 0;padding: 0; display: flex; flex-direction: row;">
                <div style="display: flex; height: 50px; margin-left: 15px;">
                    <ElImage src="/src/images/Logo.png" style="width: 50px;"></ElImage>
                </div>
                <div style="display: flex; font-size: 18px;margin-top: 15px; font-weight: 700;color: white;">
                    抖阳
                </div>
                <div style="display: flex; margin: auto;">
                    <div class="search-wrapper">
                        <ElInput class="search-input" placeholder="搜索你感兴趣的内容">
                        </ElInput>
                        <ElButton class="search-button">
                            <ElIcon>
                                <Search />
                            </ElIcon>
                            搜索
                        </ElButton>
                    </div>
                </div>
                <div style="display: flex; margin-left: auto; align-items: center; margin-right: 20px;">
                    <ElButton type="text" @click="ToFrameExtraction">投稿</ElButton>
                </div>
            </ElHeader>
            <ElContainer style="padding: 0;margin: 0;">
                <ElAside style="height: 100vh;width: 17vh;display: flex; flex-direction: column;">
                    <div style="margin-top: 15px; display: flex; justify-content: center;">
                        <ElButton type="primary" class="tool-button">
                            <ElIcon size="20" style="margin-right: 10px;">
                                <Pointer />
                            </ElIcon>精选
                        </ElButton>
                    </div>
                    <div style="display: flex; justify-content: center;" @click="ClickToRecommend">
                        <ElButton type="primary" class="tool-button">
                            <ElIcon size="20" style="margin-right: 10px;">
                                <StarFilled />
                            </ElIcon>推荐
                        </ElButton>
                    </div>
                    <div style="display: flex; justify-content: center;">
                        <ElButton type="primary" class="tool-button">
                            <ElIcon size="20" style="margin-right: 10px;">
                                <Histogram />
                            </ElIcon>AI抖阳
                        </ElButton>
                    </div>
                    <ElDivider style="padding: 0;margin: 12px; width: 80%; border-color: rgb(32, 34, 45);"></ElDivider>
                    <div style="display: flex; justify-content: center;">
                        <ElButton type="primary" class="tool-button">
                            <ElIcon size="20" style="margin-right: 10px;">
                                <Avatar />
                            </ElIcon>关注
                        </ElButton>
                    </div>
                    <div style="display: flex; justify-content: center;">
                        <ElButton type="primary" class="tool-button">
                            <ElIcon size="20" style="margin-right: 10px;">
                                <Promotion />
                            </ElIcon>朋友
                        </ElButton>
                    </div>
                    <div style="display: flex; justify-content: center;">
                        <ElButton type="primary" class="tool-button">
                            <ElIcon size="20" style="margin-right: 10px;">
                                <User />
                            </ElIcon>我的
                        </ElButton>
                    </div>
                    <ElDivider style="padding: 0;margin: 12px; width: 80%; border-color: rgb(32, 34, 45);"></ElDivider>
                    <div style="display: flex; justify-content: center;">
                        <ElButton type="primary" class="tool-button">
                            <ElIcon size="20" style="margin-right: 10px;">
                                <DataBoard />
                            </ElIcon>AI开学季
                        </ElButton>
                    </div>
                    <div style="display: flex; justify-content: center;">
                        <ElButton type="primary" class="tool-button">
                            <ElIcon size="20" style="margin-right: 10px;">
                                <VideoPlay />
                            </ElIcon>直播
                        </ElButton>
                    </div>
                    <div style="display: flex; justify-content: center;">
                        <ElButton type="primary" class="tool-button">
                            <ElIcon size="20" style="margin-right: 10px;">
                                <Monitor />
                            </ElIcon>放映厅
                        </ElButton>
                    </div>
                    <div style="display: flex; justify-content: center;">
                        <ElButton type="primary" class="tool-button">
                            <ElIcon size="20" style="margin-right: 10px;">
                                <VideoCamera />
                            </ElIcon>短剧
                        </ElButton>
                    </div>
                </ElAside>
                <ElMain style="display: flex;align-items: center; justify-content: center; height: 100vh; margin: 0; padding: 0;">
                    <RouterView></RouterView>
                </ElMain>
            </ElContainer>
        </ElContainer>
    </div>
</template>

<script setup>
import { ElAside, ElButton, ElContainer, ElDivider, ElHeader, ElIcon, ElImage, ElInput, ElMain } from 'element-plus';
import { Avatar, Back, DataBoard, Histogram, Lock, Monitor, Pointer, Promotion, Search, StarFilled, User, VideoCamera, VideoPlay } from '@element-plus/icons-vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router'; // 导入 useRouter

const router = useRouter();
function ClickToRecommend() {
    router.push("/recommend");    
}
function ToFrameExtraction() {
    router.push("/frame-extraction")
}
</script>

<style scoped>
.tool-button {
    width: 120px;
    height: 40px;
    background-color: rgb(22, 24, 35);
    color: rgb(201, 201, 201);
    border: none;
    font-size: 16px;
    border-radius: 10px;
    display: flex;
    justify-content: flex-start;
}

.tool-button:hover {
    background-color: rgba(201, 201, 201, 0.1);
    color: white
}

.search-input {
    margin-left: 200px;
    color: white;
    height: 42px;
    width: 370px;
    border-radius: 0 !important;
    --el-input-border-radius: 10px 0 0 10px;
    /* 通过CSS变量设置圆角 */
    --el-input-bg-color: rgb(57, 58, 68);
    --el-border-color: rgb(57, 58, 68);
}

.search-wrapper:hover {

    .search-button {
        font-size: 16px;
        font-weight: 600;
        border: 0;
        background-color: white;
        color: black;
    }

    .search-input {
        color: white;
        height: 42px;
        width: 370px;
        border-radius: 0 !important;
        --el-input-border-radius: 10px 0 0 10px;
        --el-input-bg-color: rgb(19, 19, 23);
        --el-border-color: white;
        --el-input-font-size: 16px;
        --el-input-text-color: #ffffff;
        border-right: 0;
    }

}

.search-input :deep(.el-input__wrapper.is-focus) {
    color: white;
    box-shadow: 0 0 0 2px white inset !important;
    --el-input-bg-color: rgb(19, 19, 23);
    --el-input-font-size: 16px;
    --el-input-text-color: #ffffff;
}

.search-input :deep(.el-input__wrapper:hover) {
    color: white;
    box-shadow: 0 0 0 2px white inset !important;
    --el-input-bg-color: rgb(19, 19, 23);
    --el-input-font-size: 16px;
    --el-input-text-color: #ffffff;
}


.search-button {
    /* margin-right: 80px; */
    width: 80px;
    font-size: 16px;
    font-weight: 600;
    height: 42px;
    border-radius: 0 10px 10px 0;
    border-color: rgb(22, 24, 35);
    border: 0;
    color: white;
    background-color: rgb(57, 58, 68);
}

.el-input__wrapper {
    border-radius: 0px;
    /* 默认值 */
}

.search-button:hover {
    font-size: 16px;
    font-weight: 600;
    border: 0;
    background-color: white;
    color: rgb(50, 50, 50);
}

/* 使用 :focus-within 实现联动效果 */
.search-wrapper:focus-within {
  .search-input {
    --el-input-bg-color: rgb(19, 19, 23);
    --el-border-color: white;
    border-right: 0;
  }

  .search-button {
    background-color: white !important;
    color: black !important;
  }
}

/* 确保 hover 和 focus 状态一致 */
.search-wrapper:hover,
.search-wrapper:focus-within {
  .search-input {
    --el-input-bg-color: rgb(19, 19, 23);
    --el-border-color: white;
    border-right: 0;
  }

  .search-button {
    background-color: white !important;
    color: black !important;
  }

  .search-button:hover {
    background-color: white !important;
    color: rgb(91, 91, 91) !important;
  }
}
</style>