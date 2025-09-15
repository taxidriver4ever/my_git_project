import torch
import clip
from PIL import Image
import requests
from io import BytesIO
from typing import List, Dict, Any, Optional
import threading
import time
from dataclasses import dataclass
import logging
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import uvicorn
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# 全局识别器实例
recognizer = None

# 英文到中文的映射字典 - 视频类别版本
EN_TO_ZH_MAP = {
# ==================== 基础视频类型 ====================
    "movie": "电影",
    "film": "影片",
    "video": "视频",
    "clip": "片段",
    "animation": "动画",
    "cartoon": "卡通",
    "anime": "动漫",
    "documentary": "纪录片",
    "short film": "短片",
    "feature film": "长片",
    "music video": "音乐视频",
    "MV": "音乐MV",
    "vlog": "视频博客",
    "tutorial": "教程视频",
    "how-to": "教学视频",
    "educational": "教育视频",

    # ==================== 视频内容和主题 ====================
    "action": "动作",
    "adventure": "冒险",
    "comedy": "喜剧",
    "drama": "剧情",
    "horror": "恐怖",
    "thriller": "惊悚",
    "sci-fi": "科幻",
    "fantasy": "奇幻",
    "romance": "爱情",
    "romantic comedy": "浪漫喜剧",
    "family": "家庭",
    "children": "儿童",
    "kids": "少儿",
    "teen": "青少年",
    "adult": "成人",

    # ==================== 视频风格和制作 ====================
    "live action": "真人",
    "CGI": "电脑特效",
    "3D animation": "3D动画",
    "2D animation": "2D动画",
    "stop motion": "定格动画",
    "claymation": "粘土动画",
    "motion graphics": "动态图形",
    "visual effects": "视觉特效",
    "VFX": "视觉特效",

    # ==================== 视频场景和环境 ====================
    "outdoor": "户外",
    "indoor": "室内",
    "nature": "自然",
    "wildlife": "野生动物",
    "landscape": "风景",
    "cityscape": "城市景观",
    "urban": "都市",
    "rural": "乡村",
    "beach": "海滩",
    "ocean": "海洋",
    "mountain": "山脉",
    "forest": "森林",
    "underwater": "水下",
    "space": "太空",
    "sky": "天空",

    # ==================== 视频人物和角色 ====================
    "human": "人类",
    "character": "角色",
    "actor": "演员",
    "actress": "女演员",
    "celebrity": "名人",
    "influencer": "网红",
    "host": "主持人",
    "interview": "采访",
    "performance": "表演",
    "dance": "舞蹈",
    "singing": "歌唱",
    "music performance": "音乐表演",

    # ==================== 视频活动和事件 ====================
    "sports": "运动",
    "game": "游戏",
    "competition": "比赛",
    "contest": "竞赛",
    "event": "事件",
    "ceremony": "典礼",
    "wedding": "婚礼",
    "birthday": "生日",
    "party": "派对",
    "concert": "音乐会",
    "festival": "节日",
    "celebration": "庆祝",

    # ==================== 视频情感和氛围 ====================
    "happy": "快乐",
    "sad": "悲伤",
    "exciting": "刺激",
    "relaxing": "放松",
    "funny": "有趣",
    "serious": "严肃",
    "emotional": "情感丰富",
    "inspiring": "鼓舞人心",
    "dramatic": "戏剧性",
    "suspenseful": "悬疑",
    "mysterious": "神秘",
    "epic": "史诗般",

    # ==================== 电影类型 ====================
    "action movie": "动作电影",
    "adventure movie": "冒险电影",
    "comedy movie": "喜剧电影",
    "drama movie": "剧情电影",
    "horror movie": "恐怖电影",
    "thriller movie": "惊悚电影",
    "sci-fi movie": "科幻电影",
    "fantasy movie": "奇幻电影",
    "romance movie": "爱情电影",
    "crime movie": "犯罪电影",
    "mystery movie": "悬疑电影",
    "film noir": "黑色电影",
    "war movie": "战争电影",
    "historical movie": "历史电影",
    "biographical movie": "传记电影",
    "musical movie": "音乐电影",
    "documentary movie": "纪录电影",
    "independent film": "独立电影",

    # ==================== 电视剧类型 ====================
    "TV series": "电视剧",
    "TV drama": "电视剧情",
    "sitcom": "情景喜剧",
    "soap opera": "肥皂剧",
    "reality show": "真人秀",
    "talk show": "脱口秀",
    "variety show": "综艺节目",
    "game show": "游戏节目",
    "talent show": "才艺秀",
    "news program": "新闻节目",
    "documentary series": "纪录片系列",
    "miniseries": "迷你剧",
    "web series": "网络剧",

    # ==================== 动画类型 ====================
    "clay animation": "粘土动画",
    "computer animation": "电脑动画",
    "traditional animation": "传统动画",
    "adult animation": "成人动画",
    "children animation": "儿童动画",

    # ==================== 短视频类型 ====================
    "short video": "短视频",
    "tutorial video": "教程视频",
    "how-to video": "教学视频",
    "educational video": "教育视频",
    "cooking video": "烹饪视频",
    "beauty tutorial": "美妆教程",
    "tech review": "科技评测",
    "gaming video": "游戏视频",
    "travel vlog": "旅行博客",
    "fitness video": "健身视频",
    "dance video": "舞蹈视频",
    "comedy sketch": "喜剧小品",
    "prank video": "恶搞视频",
    "challenge video": "挑战视频",
    "reaction video": "反应视频",
    "unboxing video": "开箱视频",
    "ASMR video": "ASMR视频",
    "time-lapse video": "延时视频",
    "slow motion video": "慢动作视频",

    # ==================== 直播类型 ====================
    "live streaming": "直播",
    "gaming live stream": "游戏直播",
    "music live stream": "音乐直播",
    "talk live stream": "谈话直播",
    "shopping live stream": "购物直播",
    "sports live stream": "体育直播",

    # ==================== 人物相关 ====================
    "celebrity interview": "名人访谈",
    "celebrity news": "名人新闻",
    "fan meeting": "粉丝见面会",
    "red carpet": "红毯活动",
    "awards ceremony": "颁奖典礼",
    "press conference": "新闻发布会",
    "behind the scenes": "幕后花絮",

    # ==================== 生活相关 ====================
    "daily life": "日常生活",
    "family life": "家庭生活",
    "couple life": "情侣生活",
    "single life": "单身生活",
    "student life": "学生生活",
    "work life": "工作生活",
    "travel experience": "旅行经历",
    "food experience": "美食体验",
    "shopping experience": "购物体验",
    "home decoration": "家居装饰",
    "pet care": "宠物护理",
    "gardening": "园艺",
    "DIY project": "DIY项目",

    # ==================== 教育学习 ====================
    "online course": "在线课程",
    "language learning": "语言学习",
    "coding tutorial": "编程教程",
    "programming tutorial": "编程教学",
    "software tutorial": "软件教程",
    "academic lecture": "学术讲座",
    "science education": "科学教育",
    "history documentary": "历史纪录片",
    "cultural documentary": "文化纪录片",
    "nature documentary": "自然纪录片",
    "wildlife documentary": "野生动物纪录片",

    # ==================== 健康健身 ====================
    "workout routine": "锻炼计划",
    "yoga practice": "瑜伽练习",
    "meditation guide": "冥想指导",
    "healthy cooking": "健康烹饪",
    "diet plan": "饮食计划",
    "mental health": "心理健康",
    "medical advice": "医疗建议",
    "fitness challenge": "健身挑战",

    # ==================== 科技数码 ====================
    "smartphone review": "智能手机评测",
    "laptop review": "笔记本电脑评测",
    "camera review": "相机评测",
    "gadget unboxing": " gadget开箱",
    "app review": "应用评测",
    "AI technology": "人工智能技术",
    "robotics": "机器人技术",
    "electric vehicle": "电动汽车",
    "smart home": "智能家居",

    # ==================== 游戏电竞 ====================
    "gameplay video": "游戏实况",
    "game review": "游戏评测",
    "eSports competition": "电子竞技比赛",
    "game tutorial": "游戏教程",
    "mobile gaming": "手机游戏",
    "PC gaming": "电脑游戏",
    "console gaming": "主机游戏",
    "game walkthrough": "游戏攻略",
    "game highlights": "游戏精彩时刻",
    "game montage": "游戏集锦",

    # ==================== 音乐艺术 ====================
    "pop music": "流行音乐",
    "rock music": "摇滚音乐",
    "hip hop music": "嘻哈音乐",
    "electronic music": "电子音乐",
    "classical music": "古典音乐",
    "jazz music": "爵士音乐",
    "folk music": "民谣音乐",
    "music festival": "音乐节",
    "dance performance": "舞蹈表演",
    "theater performance": "戏剧表演",
    "art exhibition": "艺术展览",
    "painting tutorial": "绘画教程",
    "photography tutorial": "摄影教程",

    # ==================== 体育运动 ====================
    "basketball game": "篮球比赛",
    "football game": "足球比赛",
    "soccer game": "英式足球比赛",
    "tennis match": "网球比赛",
    "volleyball game": "排球比赛",
    "swimming competition": "游泳比赛",
    "athletics competition": "田径比赛",
    "extreme sports": "极限运动",
    "winter sports": "冬季运动",
    "water sports": "水上运动",
    "martial arts": "武术",
    "fitness competition": "健身比赛",
    "sports highlights": "体育精彩时刻",
    "sports analysis": "体育分析",

    # ==================== 汽车交通 ====================
    "car review": "汽车评测",
    "motorcycle review": "摩托车评测",
    "car racing": "赛车",
    "car modification": "汽车改装",
    "driving tutorial": "驾驶教程",
    "travel by car": "汽车旅行",
    "public transportation": "公共交通",
    "aviation video": "航空视频",
    "train journey": "火车旅行",
    "ship cruise": "邮轮旅行",

    # ==================== 时尚美妆 ====================
    "fashion show": "时装秀",
    "outfit ideas": "穿搭灵感",
    "makeup tutorial": "化妆教程",
    "hairstyle tutorial": "发型教程",
    "skincare routine": "护肤流程",
    "fashion review": "时尚评测",
    "shopping haul": "购物分享",
    "beauty review": "美妆评测",

    # ==================== 商业财经 ====================
    "business news": "商业新闻",
    "stock market": "股票市场",
    "investment advice": "投资建议",
    "entrepreneurship": "创业",
    "marketing strategy": "营销策略",
    "career advice": "职业建议",
    "financial education": "金融教育",

    # ==================== 新闻时事 ====================
    "breaking news": "突发新闻",
    "political news": "政治新闻",
    "social news": "社会新闻",
    "international news": "国际新闻",
    "local news": "本地新闻",
    "weather forecast": "天气预报",
    "traffic news": "交通新闻",

    # ==================== 娱乐八卦 ====================
    "entertainment news": "娱乐新闻",
    "celebrity gossip": "名人八卦",
    "movie news": "电影新闻",
    "music news": "音乐新闻",
    "TV show news": "电视剧新闻",
    "premiere event": "首映活动",

    # ==================== 社会文化 ====================
    "cultural festival": "文化节",
    "traditional ceremony": "传统仪式",
    "religious event": "宗教活动",
    "social issue": "社会问题",
    "charity event": "慈善活动",
    "community activity": "社区活动",

    # ==================== 旅游户外 ====================
    "travel guide": "旅行指南",
    "hotel review": "酒店评测",
    "restaurant review": "餐厅评测",
    "tourist attraction": "旅游景点",
    "adventure travel": "冒险旅行",
    "budget travel": "经济旅行",
    "luxury travel": "豪华旅行",
    "camping video": "露营视频",
    "hiking adventure": "徒步冒险",
    "mountain climbing": "登山",
    "scuba diving": "潜水",
    "skiing video": "滑雪视频",

    # ==================== 美食烹饪 ====================
    "cooking tutorial": "烹饪教程",
    "recipe video": "食谱视频",
    "food review": "美食评测",
    "street food": "街头美食",
    "fine dining": "高级餐饮",
    "baking tutorial": "烘焙教程",
    "cocktail making": "鸡尾酒制作",
    "food challenge": "美食挑战",
    "food travel": "美食旅行",
    "food competition": "美食比赛",

    # ==================== 母婴亲子 ====================
    "pregnancy advice": "孕期建议",
    "baby care": "婴儿护理",
    "parenting tips": "育儿技巧",
    "children education": "儿童教育",
    "family activity": "家庭活动",
    "toy review": "玩具评测",
    "children entertainment": "儿童娱乐",

    # ==================== 幽默搞笑 ====================
    "comedy skit": "喜剧短剧",
    "funny moments": "搞笑时刻",
    "blooper reel": "花絮集锦",
    "stand-up comedy": "单口喜剧",
    "impression video": "模仿视频",
    "parody video": "恶搞视频",

    # ==================== 情感关系 ====================
    "relationship advice": "关系建议",
    "dating tips": "约会技巧",
    "marriage counseling": "婚姻咨询",
    "friendship video": "友谊视频",
    "self-improvement": "自我提升",

    # ==================== 视频制作风格 ====================
    "cinematic video": "电影感视频",
    "documentary style": "纪录片风格",
    "vlog style": "博客风格",
    "interview style": "采访风格",
    "tutorial style": "教程风格",
    "review style": "评测风格",
    "news report style": "新闻报道风格",
    "live broadcast style": "直播风格",

    # ==================== 视频情感氛围 ====================
    "emotional story": "情感故事",
    "inspiring video": "鼓舞人心视频",
    "heartwarming moment": "温馨时刻",
    "romantic moment": "浪漫时刻",
    "suspenseful scene": "悬疑场景",
    "dramatic scene": "戏剧性场景",

    # ==================== 视频质量特征 ====================
    "high quality video": "高质量视频",
    "professional production": "专业制作",
    "amateur video": "业余视频",
    "user generated content": "用户生成内容",
    "studio production": "工作室制作",
    "independent production": "独立制作",

    # ==================== 特殊场景 ====================
    "graduation ceremony": "毕业典礼",
    "anniversary celebration": "周年庆典",
    "conference recording": "会议记录",
    "product launch": "产品发布",
    "exhibition tour": "展览参观",
    "museum tour": "博物馆参观",
    "city tour": "城市游览",
    "nature scenery": "自然风景",
    "urban landscape": "城市景观",
    "underwater world": "水下世界",
    "space exploration": "太空探索",
    "time-lapse nature": "自然延时",

    # ==================== 受众群体 ====================
    "for children": "儿童向",
    "for teenagers": "青少年向",
    "for adults": "成人向",
    "for seniors": "老年人向",
    "for students": "学生向",
    "for professionals": "专业人士向",
    "for families": "家庭向",
    "for couples": "情侣向",
    "for gamers": "游戏玩家向",
    "for sports fans": "体育迷向",
    "for music lovers": "音乐爱好者向",
    "for movie fans": "电影爱好者向",
    "for travelers": "旅行者向",
    "for foodies": "美食家向",
    "for tech enthusiasts": "科技爱好者向",
    "for fashion lovers": "时尚爱好者向",

    # ==================== 时效性内容 ====================
    "trending topic": "热门话题",
    "viral video": "病毒视频",
    "seasonal content": "季节性内容",
    "holiday special": "节日特辑",
    "current event": "时事",
    "live coverage": "现场报道",
    "real-time update": "实时更新",

    # ==================== 默认映射 ====================
    "unknown": "未知视频类型",
    "video": "视频内容"
}


def translate_to_chinese(english_word: str) -> str:
    """
    将英文单词翻译成中文

    Args:
        english_word: 英文单词

    Returns:
        对应的中文翻译，如果找不到则返回英文原词
    """
    return EN_TO_ZH_MAP.get(english_word.lower(), english_word)


@dataclass
class RecognitionResult:
    """识别结果数据类"""
    object: str
    object_zh: str  # 新增中文字段
    confidence: float
    score: float


class RecognitionRequest(BaseModel):
    """识别请求模型"""
    image_url: str
    top_k: Optional[int] = 10
    timeout: Optional[int] = 10
    language: Optional[str] = "both"  # 新增语言选项：en, zh, both


class RecognitionResponse(BaseModel):
    """识别响应模型"""
    success: bool
    predictions: Optional[List[Dict[str, Any]]] = None
    image_size: Optional[tuple] = None
    error: Optional[str] = None
    timing: Optional[Dict[str, float]] = None
    request_id: Optional[str] = None


class ThreadSafeCLIPRecognizer:
    """
    线程安全的CLIP图像识别器
    每个线程拥有独立的模型实例，避免竞争条件
    """

    def __init__(self, model_name: str = "ViT-B/32", categories: Optional[List[str]] = None):
        """
        初始化识别器

        Args:
            model_name: CLIP模型名称
            categories: 自定义类别列表，如果为None则使用默认的视频类别
        """
        self.model_name = model_name
        self.categories = categories or self._get_video_categories()

        # 线程局部存储，每个线程有独立的资源
        self.thread_local = threading.local()

        logger.info(f"初始化视频识别器，加载了 {len(self.categories)} 个视频类别")
        logger.info(f"模型: {model_name}")

    def _get_video_categories(self) -> List[str]:
        """返回视频相关的类别列表"""
        return [
            # 视频类型和格式
            "movie", "film", "video", "clip", "animation", "cartoon", "anime",
            "documentary", "short film", "feature film", "music video", "MV", "vlog",
            "tutorial", "how-to", "educational",

            # 视频内容和主题
            "action", "adventure", "comedy", "drama", "horror", "thriller",
            "sci-fi", "fantasy", "romance", "romantic comedy", "family",
            "children", "kids", "teen", "adult",

            # 视频风格和制作
            "live action", "CGI", "3D animation", "2D animation", "stop motion",
            "claymation", "motion graphics", "visual effects", "VFX",

            # 视频场景和环境
            "outdoor", "indoor", "nature", "wildlife", "landscape", "cityscape",
            "urban", "rural", "beach", "ocean", "mountain", "forest",
            "underwater", "space", "sky",

            # 视频人物和角色
            "human", "character", "actor", "actress", "celebrity", "influencer",
            "host", "interview", "performance", "dance", "singing", "music performance",

            # 视频活动和事件
            "sports", "game", "competition", "contest", "event", "ceremony",
            "wedding", "birthday", "party", "concert", "festival", "celebration",

            # 视频技术和质量
            # "HD", "4K", "8K", "UHD", "slow motion", "time lapse", "hyperlapse",
            # "drone footage", "aerial view", "360 video", "VR", "AR",
            # "stabilized", "shaky",

            # 视频情感和氛围
            "happy", "sad", "exciting", "relaxing", "funny", "serious",
            "emotional", "inspiring", "dramatic", "suspenseful", "mysterious", "epic",

            # 视频制作元素
            # "close-up", "wide shot", "panning", "zooming", "tracking shot",
            # "crane shot", "POV", "montage", "transition", "special effects"
            # ==================== 视频内容类型 ====================
            # 电影类型
            "action movie", "adventure movie", "comedy movie", "drama movie",
            "horror movie", "thriller movie", "sci-fi movie", "fantasy movie",
            "romance movie", "romantic comedy", "crime movie", "mystery movie",
            "film noir", "war movie", "historical movie", "biographical movie",
            "musical movie", "documentary movie", "independent film",

            # 电视剧类型
            "TV series", "TV drama", "sitcom", "soap opera", "reality show",
            "talk show", "variety show", "game show", "talent show", "news program",
            "documentary series", "miniseries", "web series",

            # 动画类型
            "anime", "cartoon", "3D animation", "2D animation", "stop motion",
            "clay animation", "computer animation", "traditional animation",
            "adult animation", "children animation",

            # 短视频类型
            "short video", "vlog", "tutorial video", "how-to video", "educational video",
            "cooking video", "beauty tutorial", "tech review", "gaming video",
            "travel vlog", "fitness video", "music video", "dance video",
            "comedy sketch", "prank video", "challenge video", "reaction video",
            "unboxing video", "ASMR video", "time-lapse video", "slow motion video",

            # 直播类型
            "live streaming", "gaming live stream", "music live stream",
            "talk live stream", "shopping live stream", "sports live stream",

            # ==================== 视频主题内容 ====================
            # 人物相关
            "celebrity interview", "celebrity news", "fan meeting", "red carpet",
            "awards ceremony", "press conference", "behind the scenes",

            # 生活相关
            "daily life", "family life", "couple life", "single life", "student life",
            "work life", "travel experience", "food experience", "shopping experience",
            "home decoration", "pet care", "gardening", "DIY project",

            # 教育学习
            "online course", "language learning", "programming tutorial",
            "academic lecture", "science education", "history documentary",
            "cultural documentary", "nature documentary", "wildlife documentary",

            # 健康健身
            "workout routine", "yoga practice", "meditation guide", "healthy cooking",
            "diet plan", "mental health", "medical advice", "fitness challenge",

            # 科技数码
            "tech review", "smartphone review", "laptop review", "camera review",
            "gadget unboxing", "software tutorial", "app review", "coding tutorial",
            "AI technology", "robotics", "electric vehicle", "smart home",

            # 游戏电竞
            "gameplay video", "game review", "eSports competition", "game tutorial",
            "mobile gaming", "PC gaming", "console gaming", "game walkthrough",
            "game highlights", "game montage",

            # 音乐艺术
            "pop music", "rock music", "hip hop music", "electronic music",
            "classical music", "jazz music", "folk music", "music performance",
            "music festival", "dance performance", "theater performance",
            "art exhibition", "painting tutorial", "photography tutorial",

            # 体育运动
            "basketball game", "football game", "soccer game", "tennis match",
            "volleyball game", "swimming competition", "athletics competition",
            "extrem sports", "winter sports", "water sports", "martial arts",
            "fitness competition", "sports highlights", "sports analysis",

            # 汽车交通
            "car review", "motorcycle review", "car racing", "car modification",
            "driving tutorial", "travel by car", "public transportation",
            "aviation video", "train journey", "ship cruise",

            # 时尚美妆
            "fashion show", "outfit ideas", "makeup tutorial", "hairstyle tutorial",
            "skincare routine", "fashion review", "shopping haul", "beauty review",

            # 商业财经
            "business news", "stock market", "investment advice", "entrepreneurship",
            "marketing strategy", "career advice", "financial education",

            # 新闻时事
            "breaking news", "political news", "social news", "international news",
            "local news", "weather forecast", "traffic news",

            # 娱乐八卦
            "entertainment news", "celebrity gossip", "movie news", "music news",
            "TV show news", "award show", "premiere event",

            # 社会文化
            "cultural festival", "traditional ceremony", "religious event",
            "social issue", "charity event", "community activity",

            # 旅游户外
            "travel guide", "hotel review", "restaurant review", "tourist attraction",
            "adventure travel", "budget travel", "luxury travel", "camping video",
            "hiking adventure", "mountain climbing", "scuba diving", "skiing video",

            # 美食烹饪
            "cooking tutorial", "recipe video", "food review", "street food",
            "fine dining", "baking tutorial", "cocktail making", "food challenge",
            "food travel", "food competition",

            # 母婴亲子
            "pregnancy advice", "baby care", "parenting tips", "children education",
            "family activity", "toy review", "children entertainment",

            # 幽默搞笑
            "comedy skit", "prank video", "funny moments", "blooper reel",
            "stand-up comedy", "impression video", "parody video",

            # 情感关系
            "relationship advice", "dating tips", "marriage counseling",
            "friendship video", "family relationship", "self-improvement",

            # ==================== 视频制作风格 ====================
            "cinematic video", "documentary style", "vlog style", "interview style",
            "tutorial style", "review style", "news report style", "live broadcast style",

            # ==================== 视频情感氛围 ====================
            "emotional story", "inspiring video", "heartwarming moment",
            "exciting moment", "relaxing video", "funny content", "sad story",
            "romantic moment", "suspenseful scene", "epic moment", "dramatic scene",

            # ==================== 视频质量特征 ====================
            "high quality video", "professional production", "amateur video",
            "user generated content", "studio production", "independent production",

            # ==================== 特殊场景 ====================
            "wedding video", "birthday party", "graduation ceremony", "anniversary celebration",
            "festival celebration", "concert recording", "sports event", "conference recording",
            "product launch", "exhibition tour", "museum tour", "city tour",
            "nature scenery", "urban landscape", "underwater world", "space exploration",
            "wildlife footage", "pet video", "time-lapse nature", "drone footage",

            # ==================== 受众群体 ====================
            "for children", "for teenagers", "for adults", "for seniors",
            "for students", "for professionals", "for families", "for couples",
            "for gamers", "for sports fans", "for music lovers", "for movie fans",
            "for travelers", "for foodies", "for tech enthusiasts", "for fashion lovers",

            # ==================== 时效性内容 ====================
            "trending topic", "viral video", "seasonal content", "holiday special",
            "current event", "breaking news", "live coverage", "real-time update"
        ]

    def get_category_info(self) -> Dict[str, Any]:
        """获取类别信息"""
        return {
            "total_categories": len(self.categories),
            "categories": self.categories,
            "chinese_categories": [translate_to_chinese(cat) for cat in self.categories],
            "model": self.model_name,
            "category_type": "video_categories"
        }

    def _initialize_thread_resources(self):
        """为当前线程初始化模型和文本特征"""
        if not hasattr(self.thread_local, 'initialized'):
            try:
                # 设置设备
                self.thread_local.device = "cuda" if torch.cuda.is_available() else "cpu"
                logger.info(f"线程 {threading.get_ident()} 使用设备: {self.thread_local.device}")

                # 加载模型
                start_time = time.time()
                self.thread_local.model, self.thread_local.preprocess = clip.load(
                    self.model_name, device=self.thread_local.device
                )
                model_load_time = time.time() - start_time

                # 预计算文本特征
                start_time = time.time()
                self.thread_local.text_features = self._precompute_text_features()
                text_feature_time = time.time() - start_time

                self.thread_local.initialized = True

                logger.info(f"线程 {threading.get_ident()} 初始化完成 - "
                            f"模型加载: {model_load_time:.2f}s, "
                            f"文本特征: {text_feature_time:.2f}s")

            except Exception as e:
                logger.error(f"线程 {threading.get_ident()} 初始化失败: {str(e)}")
                raise

    def _precompute_text_features(self) -> torch.Tensor:
        """为当前线程预计算文本特征"""
        text_descriptions = [f"a video of {category}" for category in self.categories]
        text_inputs = torch.cat([
            clip.tokenize(desc) for desc in text_descriptions
        ]).to(self.thread_local.device)

        with torch.no_grad():
            text_features = self.thread_local.model.encode_text(text_inputs)
            text_features /= text_features.norm(dim=-1, keepdim=True)

        return text_features

    def download_image_to_memory(self, image_url: str, timeout: int = 10) -> Optional[Image.Image]:
        """下载图像到内存（不保存到文件）"""
        try:
            response = requests.get(image_url, timeout=timeout, stream=True)
            response.raise_for_status()

            # 直接读取到内存
            image_data = BytesIO(response.content)
            image = Image.open(image_data)

            # 确保图像格式正确
            if image.mode != 'RGB':
                image = image.convert('RGB')

            return image

        except requests.exceptions.Timeout:
            logger.warning(f"下载超时: {image_url}")
            return None
        except requests.exceptions.RequestException as e:
            logger.warning(f"下载失败: {image_url}, 错误: {str(e)}")
            return None
        except Exception as e:
            logger.error(f"处理图像失败: {image_url}, 错误: {str(e)}")
            return None

    def recognize_from_url(self, image_url: str, top_k: int = 10, timeout: int = 10, language: str = "both") -> Dict[
        str, Any]:
        """
        从URL识别视频类别（线程安全），完全在内存中处理

        Args:
            image_url: 图像URL（视频缩略图或关键帧）
            top_k: 返回前K个最可能的结果
            timeout: 请求超时时间(秒)
            language: 返回语言选项 (en, zh, both)

        Returns:
            包含识别结果的字典
        """
        start_time = time.time()
        thread_id = threading.get_ident()

        try:
            # 1. 下载图像到内存
            download_start = time.time()
            image = self.download_image_to_memory(image_url, timeout)
            if image is None:
                return {
                    "success": False,
                    "error": "下载或处理图像失败",
                    "url": image_url,
                    "thread_id": thread_id
                }
            download_time = time.time() - download_start

            # 2. 初始化当前线程资源
            init_start = time.time()
            self._initialize_thread_resources()
            init_time = time.time() - init_start

            # 3. 处理图像（在内存中）
            process_start = time.time()
            # 图像已经在 download_image_to_memory 中处理好了
            process_time = time.time() - process_start

            # 4. 推理识别
            inference_start = time.time()
            image_input = self.thread_local.preprocess(image).unsqueeze(0).to(self.thread_local.device)

            with torch.no_grad():
                image_features = self.thread_local.model.encode_image(image_input)
                image_features /= image_features.norm(dim=-1, keepdim=True)

                similarity = (image_features @ self.thread_local.text_features.T) * 100
                values, indices = similarity[0].topk(min(top_k, len(self.categories)))

            inference_time = time.time() - inference_start

            # 5. 组织结果
            results = []
            for value, index in zip(values, indices):
                object_en = self.categories[index]
                object_zh = translate_to_chinese(object_en)

                results.append(RecognitionResult(
                    object=object_en,
                    object_zh=object_zh,  # 修复：这里应该使用翻译后的中文
                    confidence=float(value.item()),
                    score=float(value.item())
                ))

            total_time = time.time() - start_time

            logger.info(f"线程 {thread_id} 视频类别识别完成 - "
                        f"总计: {total_time:.2f}s, "
                        f"下载: {download_time:.2f}s, "
                        f"初始化: {init_time:.2f}s, "
                        f"处理: {process_time:.2f}s, "
                        f"推理: {inference_time:.2f}s")

            # 根据语言选项格式化结果
            predictions = []
            for r in results:
                if language == "zh":
                    prediction = {
                        "object": r.object_zh,  # 使用中文结果
                        "confidence": f"{r.confidence:.4f}",
                        "score": r.score
                    }
                elif language == "en":
                    prediction = {
                        "object": r.object,  # 使用英文结果
                        "confidence": f"{r.confidence:.4f}",
                        "score": r.score
                    }
                else:  # both
                    prediction = {
                        "object": r.object,  # 英文
                        "object_zh": r.object_zh,  # 中文
                        "confidence": f"{r.confidence:.4f}",
                        "score": r.score
                    }
                predictions.append(prediction)

            return {
                "success": True,
                "predictions": predictions,
                "image_size": image.size,
                "url": image_url,
                "thread_id": thread_id,
                "timing": {
                    "total": round(total_time, 3),
                    "download": round(download_time, 3),
                    "init": round(init_time, 3),
                    "process": round(process_time, 3),
                    "inference": round(inference_time, 3)
                }
            }

        except Exception as e:
            error_time = time.time() - start_time
            logger.error(f"线程 {thread_id} 识别失败: {str(e)}, 耗时: {error_time:.2f}s")
            return {
                "success": False,
                "error": str(e),
                "url": image_url,
                "thread_id": thread_id,
                "timing": round(error_time, 3)
            }


@asynccontextmanager
async def lifespan(app: FastAPI):
    """生命周期事件处理器"""
    # Startup 逻辑
    global recognizer
    logger.info("正在初始化视频类别识别器...")
    recognizer = ThreadSafeCLIPRecognizer()
    logger.info("视频类别识别器初始化完成")

    yield  # 应用运行期间

    # Shutdown 逻辑
    logger.info("正在关闭应用...")
    # 清理资源


# 创建FastAPI应用，传入lifespan处理器
app = FastAPI(
    title="视频类别识别API",
    description="基于CLIP模型的视频类别识别服务，通过分析视频缩略图识别视频类型和内容",
    version="2.0.0",
    lifespan=lifespan
)

# 添加CORS中间件
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境应该限制域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
async def root():
    """根端点，返回服务信息"""
    return {
        "message": "视频类别识别服务（基于CLIP模型）",
        "version": "2.0.0",
        "endpoints": {
            "识别视频类别": "POST /api/VideoRecognition",
            "批量识别": "POST /api/BatchVideoRecognition",
            "健康检查": "GET /health",
            "类别信息": "GET /api/categories"
        },
        "features": {
            "no_disk_cache": True,
            "in_memory_processing": True,
            "auto_cleanup": True,
            "multi_language": True,
            "video_categories": True
        }
    }


@app.get("/health")
async def health_check():
    """健康检查端点"""
    return {
        "status": "healthy",
        "gpu_available": torch.cuda.is_available(),
        "device": "cuda" if torch.cuda.is_available() else "cpu",
        "memory_only": True,
        "no_disk_usage": True,
        "service_type": "video_category_recognition"
    }


@app.get("/api/categories")
async def get_categories():
    """获取支持的视频类别列表"""
    if recognizer is None:
        raise HTTPException(status_code=503, detail="识别器未初始化")

    return recognizer.get_category_info()


@app.post("/api/VideoRecognition", response_model=RecognitionResponse)
async def video_recognition(request: RecognitionRequest):
    """
    视频类别识别接口 - 通过视频缩略图识别视频类型

    - **image_url**: 视频缩略图或关键帧的URL地址
    - **top_k**: 返回前K个最可能的结果（可选，默认10）
    - **timeout**: 请求超时时间（可选，默认10秒）
    - **language**: 返回语言 (en, zh, both)（可选，默认both）
    """
    if recognizer is None:
        raise HTTPException(status_code=503, detail="识别器未初始化")

    # 验证URL格式
    if not request.image_url.startswith(('http://', 'https://')):
        raise HTTPException(status_code=400, detail="无效的URL格式")

    # 验证top_k范围
    if request.top_k < 1 or request.top_k > 50:
        raise HTTPException(status_code=400, detail="top_k必须在1-50之间")

    # 验证timeout范围
    if request.timeout < 1 or request.timeout > 60:
        raise HTTPException(status_code=400, detail="timeout必须在1-60之间")

    # 验证language选项
    if request.language not in ["en", "zh", "both"]:
        raise HTTPException(status_code=400, detail="language必须是 en, zh 或 both")

    logger.info(f"收到视频识别请求: {request.image_url}, top_k={request.top_k}, language={request.language}")

    # 执行识别（完全在内存中）
    result = recognizer.recognize_from_url(
        image_url=request.image_url,
        top_k=request.top_k,
        timeout=request.timeout,
        language=request.language
    )

    # 转换结果为响应模型
    response_data = {
        "success": result["success"],
        "request_id": f"req_{int(time.time())}_{threading.get_ident()}"
    }

    if result["success"]:
        response_data["predictions"] = result["predictions"]
        response_data["image_size"] = result["image_size"]
        response_data["timing"] = result.get("timing", {})
    else:
        response_data["error"] = result.get("error", "未知错误")

    return response_data


@app.post("/api/BatchVideoRecognition")
async def batch_video_recognition(requests: List[RecognitionRequest]):
    """
    批量视频类别识别接口 - 完全内存处理
    """
    if recognizer is None:
        raise HTTPException(status_code=503, detail="识别器未初始化")

    if len(requests) > 20:
        raise HTTPException(status_code=400, detail="单次最多处理20个请求")

    results = []
    for request in requests:
        result = recognizer.recognize_from_url(
            image_url=request.image_url,
            top_k=request.top_k,
            timeout=request.timeout,
            language=request.language
        )
        results.append(result)

    return {
        "success": True,
        "results": results,
        "total_count": len(results),
        "success_count": sum(1 for r in results if r["success"]),
        "processing_mode": "in_memory_no_disk"
    }


if __name__ == "__main__":
    # 启动FastAPI服务
    uvicorn.run(
        app,
        host="0.0.0.0",  # 允许外部访问
        port=8000,
        reload=False,  # 生产环境设为False
        workers=1,  # 由于GPU内存限制，建议使用1个worker
        log_level="info"
    )