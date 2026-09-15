<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="logo">
        <span>🚣</span>
        <span>赛艇支架系统</span>
      </div>
      <el-menu :default-active="activeMenu" class="menu" @select="handleMenuSelect">
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>统计看板</span>
        </el-menu-item>
        <el-menu-item index="/bracket">
          <el-icon><Box /></el-icon>
          <span>支架管理</span>
        </el-menu-item>
        <el-menu-item index="/group">
          <el-icon><UserFilled /></el-icon>
          <span>组别管理</span>
        </el-menu-item>
        <el-menu-item index="/binding">
          <el-icon><Link /></el-icon>
          <span>绑定管理</span>
        </el-menu-item>
        <el-menu-item index="/schedule">
          <el-icon><Calendar /></el-icon>
          <span>训练排课</span>
        </el-menu-item>
        <el-menu-item index="/logs">
          <el-icon><Document /></el-icon>
          <span>变更日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">{{ pageTitle }}</div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataLine, Box, UserFilled, Link, Document, Calendar } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const pageTitles: Record<string, string> = {
  '/dashboard': '竞速距离统计看板',
  '/bracket': '水上停靠支架管理',
  '/group': '赛艇训练组别管理',
  '/binding': '支架绑定关系管理',
  '/schedule': '训练课次排课管理',
  '/logs': '绑定变更日志'
}

const pageTitle = computed(() => pageTitles[route.path] || '赛艇训练基地水上停靠支架绑定系统')

const handleMenuSelect = (index: string) => {
  router.push(index)
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background-color: #1a365d;
  color: white;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  font-size: 18px;
  font-weight: bold;
  gap: 8px;
}

.menu {
  border-right: none;
}

.header {
  background-color: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  padding: 0 20px;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
  color: #1a365d;
  line-height: 60px;
}

.main {
  padding: 20px;
  background-color: #f5f7fa;
}
</style>