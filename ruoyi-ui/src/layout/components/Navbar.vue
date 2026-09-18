<template>
  <div class="navbar">
    <hamburger :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
    <breadcrumb class="breadcrumb-container" />

    <div class="right-menu">
      <!-- 站内信红点 -->
      <el-popover
        placement="bottom-end"
        width="340"
        trigger="click"
        popper-class="message-popover"
        @show="onPopoverShow"
      >
        <div class="message-panel">
          <div class="message-panel__header">
            <span>消息中心</span>
            <el-button
              type="text"
              size="mini"
              :disabled="messageUnreadCount === 0"
              @click="handleReadAll"
            >全部已读</el-button>
          </div>
          <el-scrollbar class="message-panel__body">
            <div v-if="messageLatest.length === 0" class="message-empty">
              暂无消息
            </div>
            <ul v-else class="message-list">
              <li
                v-for="item in messageLatest"
                :key="item.messageId"
                class="message-item"
                :class="{ 'is-unread': item.readStatus === '0' }"
                @click="goMessageCenter"
              >
                <span class="dot" v-if="item.readStatus === '0'"></span>
                <span class="title">{{ item.title }}</span>
                <el-tag
                  size="mini"
                  :type="typeTagType(item.type)"
                  class="type-tag"
                >{{ typeLabel(item.type) }}</el-tag>
                <span class="time">{{ item.createTime }}</span>
              </li>
            </ul>
          </el-scrollbar>
          <div class="message-panel__footer">
            <el-button type="text" size="mini" @click="goMessageCenter">查看全部消息</el-button>
          </div>
        </div>
        <el-badge
          slot="reference"
          :value="messageUnreadCount"
          :max="99"
          :hidden="messageUnreadCount === 0"
          class="message-bell"
        >
          <el-tooltip content="消息中心" placement="bottom">
            <i class="el-icon-bell"></i>
          </el-tooltip>
        </el-badge>
      </el-popover>

      <el-dropdown class="avatar-container" trigger="click">
        <div class="avatar-wrapper">
          <span class="user-name">{{ name }}</span>
          <i class="el-icon-caret-bottom" />
        </div>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item @click.native="logout">
            <span>退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'
import { readAllMessage } from '@/api/medical/message'

export default {
  name: 'Navbar',
  components: { Breadcrumb, Hamburger },
  data() {
    return {
      pollTimer: null,
      POLL_INTERVAL: 60000
    }
  },
  computed: {
    ...mapGetters(['sidebar', 'name', 'messageUnreadCount', 'messageLatest'])
  },
  created() {
    this.refresh()
    this.startPolling()
    document.addEventListener('visibilitychange', this.onVisibilityChange)
  },
  destroyed() {
    this.stopPolling()
    document.removeEventListener('visibilitychange', this.onVisibilityChange)
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('toggleSidebar')
    },
    refresh() {
      const receiver = this.name || 'admin'
      this.$store.dispatch('message/fetchMessageOverview', receiver).catch(() => {})
    },
    startPolling() {
      this.stopPolling()
      this.pollTimer = setInterval(() => this.refresh(), this.POLL_INTERVAL)
    },
    stopPolling() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
        this.pollTimer = null
      }
    },
    onVisibilityChange() {
      if (document.hidden) {
        this.stopPolling()
      } else {
        this.refresh()
        this.startPolling()
      }
    },
    onPopoverShow() {
      this.refresh()
    },
    typeLabel(type) {
      const map = { '0': '系统', '1': '低库存预警', '2': '随访逾期' }
      return map[type] || '系统'
    },
    typeTagType(type) {
      if (type === '2') return 'danger'
      if (type === '1') return 'warning'
      return 'info'
    },
    handleReadAll() {
      readAllMessage(this.name || 'admin').then(() => {
        this.$modal.msgSuccess('已全部标记为已读')
        this.refresh()
      })
    },
    goMessageCenter() {
      this.$router.push('/medical/message')
    },
    logout() {
      this.$store.dispatch('Logout').then(() => {
        this.$router.push('/login')
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;

  .hamburger-container {
    height: 100%;
    float: left;
    padding: 0 15px;
    cursor: pointer;
  }
  .breadcrumb-container {
    float: left;
  }
  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;
    margin-right: 20px;
    display: flex;
    align-items: center;

    .message-bell {
      margin-right: 18px;
      cursor: pointer;
      font-size: 18px;
      color: #5a5e66;
      outline: none;
    }
    .avatar-wrapper {
      cursor: pointer;
      .user-name {
        font-size: 14px;
        margin-right: 5px;
        color: #303133;
      }
    }
  }
}

.message-panel {
  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
  }
  &__body {
    max-height: 280px;
  }
  &__footer {
    text-align: center;
    border-top: 1px solid #ebeef5;
    padding-top: 6px;
  }
  .message-empty {
    text-align: center;
    color: #909399;
    font-size: 13px;
    padding: 24px 0;
  }
  .message-list {
    list-style: none;
    margin: 0;
    padding: 0;
    .message-item {
      display: flex;
      align-items: center;
      padding: 8px 4px;
      border-bottom: 1px solid #f5f5f5;
      cursor: pointer;
      font-size: 13px;
      &:hover {
        background: #f5f7fa;
      }
      .dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: #f56c6c;
        margin-right: 6px;
        flex: none;
      }
      .title {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .type-tag {
        margin: 0 6px;
        flex: none;
      }
      .time {
        color: #909399;
        flex: none;
      }
    }
  }
}
</style>
