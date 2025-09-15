// src/renderer/router/index.js (Vue版本)
import { createRouter, createWebHashHistory } from 'vue-router'
import FrameExtraction from '../components/FrameExtraction.vue'
import Home from '../components/Home.vue'
import Recommend from '../components/Recommend.vue'

const routes = [
    {
        path: '/',
        component: Home,
        children: [
            {
                path: 'frame-extraction',
                component: FrameExtraction
            },
            {
                path: 'recommend',
                component: Recommend
            }
        ]
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

export default router