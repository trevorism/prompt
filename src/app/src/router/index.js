import {createRouter, createWebHistory} from 'vue-router'
import Splash from "../views/Splash.vue";
import AskQuestion from "../views/AskQuestion.vue";


const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'splash',
            component: Splash
        },
        {
            path: '/ask',
            name: 'ask',
            component: AskQuestion
        }
    ]
})

export default router
