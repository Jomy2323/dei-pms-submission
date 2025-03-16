import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import PeopleView from '@/views/people/PeopleView.vue'
import StatisticsView from '@/views/statistics/StatisticsView.vue'

import StaffView from '@/views/StaffView.vue'
import StudentView from '@/views/StudentView.vue'
import CoordinatorView from '@/views/CoordinatorView.vue'
import TeacherView from '@/views/TeacherView.vue'
import SCView from '../views/SCView.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/people',
      name: 'people',
      component: PeopleView
    },
    {
      path: '/statistics',
      name: 'statistics',
      component: StatisticsView
    },
    {
      path: '/student',
      name: 'StudentView',
      component: StudentView
    },
    {
      path: '/staff',
      name: 'StaffView',
      component: StaffView
    },
    {
      path: '/coordinator',
      name: 'CoordinatorView',
      component: CoordinatorView
    },
    {
      path: '/teacher',
      name: 'TeacherView',
      component: TeacherView
    },
    {
      path: '/sc',
      name: 'SCView',
      component: SCView
    },

  ]
})

export default router
