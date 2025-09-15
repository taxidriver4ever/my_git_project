package org.example.tiktokproject.service.imp;

import jakarta.annotation.Resource;
import org.example.tiktokproject.pojo.Video;
import org.example.tiktokproject.repository.VideoRepository;
import org.example.tiktokproject.service.VideoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class VideoServiceImp implements VideoService {

    @Resource
    private VideoRepository videoRepository;

    @Override
    public List<Video> getVideos() {
        String[] split = {"电影", "影片", "视频", "片段", "动画",
                "卡通", "动漫", "纪录片", "短片", "长片", "音乐视频",
                "音乐 MV", "视频博客", "教程视频", "教学视频", "教育视频", "动作", "冒险", "喜剧", "剧情", "恐怖", "惊悚",
                "科幻", "奇幻", "爱情", "浪漫喜剧", "家庭", "儿童", "少儿", "青少年", "成人", "真人", "电脑特效", "3D 动画",
                "2D 动画", "定格动画", "粘土动画", "动态图形", "视觉特效", "视觉特效", "户外", "室内", "自然", "野生动物", "风景",
                "城市景观", "都市", "乡村", "海滩", "海洋", "山脉", "森林", "水下", "太空", "天空", "人类", "角色", "演员", "女演员",
                "名人", "网红", "主持人", "采访", "表演", "舞蹈", "歌唱", "音乐表演", "运动", "游戏", "比赛", "竞赛", "事件", "典礼",
                "婚礼", "生日", "派对", "音乐会", "节日", "庆祝", "快乐", "悲伤", "刺激", "放松", "有趣", "严肃", "情感丰富", "鼓舞人心",
                "戏剧性", "悬疑", "神秘", "史诗般", "动作电影", "冒险电影", "喜剧电影", "剧情电影", "恐怖电影", "惊悚电影", "科幻电影",
                "奇幻电影", "爱情电影", "犯罪电影", "悬疑电影", "黑色电影", "战争电影", "历史电影", "传记电影", "音乐电影", "纪录电影",
                "独立电影", "电视剧", "电视剧情", "情景喜剧", "肥皂剧", "真人秀", "脱口秀", "综艺节目", "游戏节目", "才艺秀", "新闻节目",
                "纪录片系列", "迷你剧", "网络剧", "粘土动画", "电脑动画", "传统动画", "成人动画", "儿童动画", "短视频", "教程视频", "教学视频",
                "教育视频", "烹饪视频", "美妆教程", "科技评测", "游戏视频", "旅行博客", "健身视频", "舞蹈视频", "喜剧小品", "恶搞视频", "挑战视频",
                "反应视频", "开箱视频", "ASMR 视频", "延时视频", "慢动作视频", "直播", "游戏直播", "音乐直播", "谈话直播", "购物直播", "体育直播",
                "名人访谈", "名人新闻", "粉丝见面会", "红毯活动", "颁奖典礼", "新闻发布会", "幕后花絮", "日常生活", "家庭生活", "情侣生活", "单身生活",
                "学生生活", "工作生活", "旅行经历", "美食体验", "购物体验", "家居装饰", "宠物护理", "园艺", "DIY 项目", "在线课程", "语言学习", "编程教程", "编程教学", "软件教程", "学术讲座", "科学教育", "历史纪录片", "文化纪录片", "自然纪录片", "野生动物纪录片", "锻炼计划", "瑜伽练习", "冥想指导", "健康烹饪", "饮食计划", "心理健康", "医疗建议", "健身挑战", "智能手机评测", "笔记本电脑评测", "相机评测", "gadget 开箱", "应用评测", "人工智能技术", "机器人技术", "电动汽车", "智能家居", "游戏实况",
                "游戏评测", "电子竞技比赛", "游戏教程", "手机游戏", "电脑游戏", "主机游戏", "游戏攻略", "游戏精彩时刻", "游戏集锦", "流行音乐", "摇滚音乐", "嘻哈音乐", "电子音乐", "古典音乐", "爵士音乐", "民谣音乐", "音乐节", "舞蹈表演", "戏剧表演", "艺术展览", "绘画教程", "摄影教程", "篮球比赛", "足球比赛", "英式足球比赛", "网球比赛", "排球比赛", "游泳比赛", "田径比赛", "极限运动",
                "冬季运动", "水上运动", "武术", "健身比赛", "体育精彩时刻", "体育分析", "汽车评测", "摩托车评测", "赛车", "汽车改装", "驾驶教程", "汽车旅行", "公共交通", "航空视频", "火车旅行", "邮轮旅行", "时装秀", "穿搭灵感", "化妆教程", "发型教程", "护肤流程", "时尚评测", "购物分享", "美妆评测", "商业新闻", "股票市场", "投资建议", "创业", "营销策略", "职业建议", "金融教育", "突发新闻", "政治新闻", "社会新闻", "国际新闻", "本地新闻", "天气预报",
                "交通新闻", "娱乐新闻", "名人八卦", "电影新闻", "音乐新闻", "电视剧新闻", "首映活动", "文化节", "传统仪式", "宗教活动", "社会问题", "慈善活动", "社区活动", "旅行指南", "酒店评测", "餐厅评测", "旅游景点", "冒险旅行", "经济旅行", "豪华旅行", "露营视频", "徒步冒险", "登山", "潜水", "滑雪视频", "烹饪教程", "食谱视频", "美食评测", "街头美食", "高级餐饮", "烘焙教程", "鸡尾酒制作", "美食挑战", "美食旅行", "美食比赛", "孕期建议", "婴儿护理", "育儿技巧", "儿童教育", "家庭活动", "玩具评测",
                "儿童娱乐", "喜剧短剧", "搞笑时刻", "花絮集锦", "单口喜剧", "模仿视频", "恶搞视频", "关系建议", "约会技巧", "婚姻咨询", "友谊视频", "自我提升", "电影感视频", "纪录片风格", "博客风格", "采访风格", "教程风格", "评测风格", "新闻报道风格", "直播风格", "情感故事", "鼓舞人心视频", "温馨时刻", "浪漫时刻", "悬疑场景", "戏剧性场景", "高质量视频", "专业制作", "业余视频", "用户生成内容", "工作室制作", "独立制作", "毕业典礼", "周年庆典", "会议记录", "产品发布", "展览参观", "博物馆参观", "城市游览",
                "自然风景", "城市景观", "水下世界", "太空探索", "自然延时", "儿童向", "青少年向", "成人向", "老年人向", "学生向", "专业人士向", "家庭向", "情侣向", "游戏玩家向", "体育迷向", "音乐爱好者向", "电影爱好者向", "旅行者向", "美食家向", "科技爱好者向", "时尚爱好者向", "热门话题", "病毒视频", "季节性内容", "节日特辑",
                "时事", "现场报道", "实时更新"};
        Random random = new Random();
        List<Video> byDescriptionContaining = videoRepository.findByDescriptionContaining(
                split[random.nextInt(split.length)] + " " + split[random.nextInt(split.length)] + " " + split[random.nextInt(split.length)] + " " + split[random.nextInt(split.length)] + " " + split[random.nextInt(split.length)] + " " + split[random.nextInt(split.length)] + " " + split[random.nextInt(split.length)] + " " + split[random.nextInt(split.length)] + " " + split[random.nextInt(split.length)] + " " + split[random.nextInt(split.length)] + " "
        );
        CopyOnWriteArrayList<Video> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        Random random2 = new Random();
        int j = random2.nextInt(byDescriptionContaining.size());
        int j1 = random2.nextInt(byDescriptionContaining.size());
        int j2 = random2.nextInt(byDescriptionContaining.size());
        copyOnWriteArrayList.add(byDescriptionContaining.get(j));
        copyOnWriteArrayList.add(byDescriptionContaining.get(j1));
        copyOnWriteArrayList.add(byDescriptionContaining.get(j2));
        return copyOnWriteArrayList;
    }
}
