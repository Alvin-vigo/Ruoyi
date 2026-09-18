<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入消息标题"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择类型" clearable size="small">
          <el-option label="系统" value="0" />
          <el-option label="低库存预警" value="1" />
          <el-option label="随访逾期提醒" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="readStatus">
        <el-select v-model="queryParams.readStatus" placeholder="请选择阅读状态" clearable size="small">
          <el-option label="未读" value="0" />
          <el-option label="已读" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-check"
          size="mini"
          :disabled="multiple"
          @click="handleRead"
          v-hasPermi="['medical:message:edit']"
        >标记已读</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-check"
          size="mini"
          @click="handleReadAll"
          v-hasPermi="['medical:message:edit']"
        >全部已读</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['medical:message:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <span class="unread-tip">未读：<b style="color:#f56c6c">{{ unreadTotal }}</b></span>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="messageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="消息ID" align="center" prop="messageId" width="90" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="内容" align="center" prop="content" :show-overflow-tooltip="true" />
      <el-table-column label="类型" align="center" prop="type" width="110">
        <template slot-scope="scope">
          <el-tag :type="scope.row.type === '2' ? 'danger' : (scope.row.type === '1' ? 'warning' : 'info')">
            {{ scope.row.type === '2' ? '随访逾期' : (scope.row.type === '1' ? '低库存预警' : '系统') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="接收人" align="center" prop="receiver" width="110" />
      <el-table-column label="状态" align="center" prop="readStatus" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.readStatus === '0' ? 'danger' : 'success'">
            {{ scope.row.readStatus === '0' ? '未读' : '已读' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="140">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleRead(scope.row)"
            v-hasPermi="['medical:message:query']"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['medical:message:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listMessage, getMessage, readMessage, readAllMessage, unreadCount, delMessage } from '@/api/medical/message'

export default {
  name: 'Message',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      unreadTotal: 0,
      messageList: [],
      currentReceiver: this.$store.state.user.name,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        type: undefined,
        readStatus: undefined,
        receiver: this.$store.state.user.name
      }
    }
  },
  created() {
    this.getList()
    this.getUnread()
  },
  methods: {
    getList() {
      this.loading = true
      listMessage(this.queryParams).then(response => {
        this.messageList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getUnread() {
      // 响应拦截器已解包 res.data，此处 response 即为未读数本身（数字）
      unreadCount(this.currentReceiver).then(count => {
        this.unreadTotal = count || 0
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.messageId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleRead(row) {
      const messageIds = (row && row.messageId) || this.ids
      if (!messageIds || (Array.isArray(messageIds) && messageIds.length === 0)) {
        return
      }
      const list = Array.isArray(messageIds) ? messageIds : [messageIds]
      const needRead = list.filter(id => {
        const item = this.messageList.find(m => m.messageId === id)
        return item && item.readStatus === '0'
      })
      if (needRead.length === 0) {
        this.getList()
        this.getUnread()
        return
      }
      Promise.all(needRead.map(id => readMessage(id))).then(() => {
        this.$modal.msgSuccess('操作成功')
        this.getList()
        this.getUnread()
      })
    },
    handleReadAll() {
      readAllMessage(this.currentReceiver).then(() => {
        this.$modal.msgSuccess('已全部标记为已读')
        this.getList()
        this.getUnread()
      })
    },
    handleDelete(row) {
      const messageIds = (row && row.messageId) || this.ids
      if (!messageIds || (Array.isArray(messageIds) && messageIds.length === 0)) {
        return
      }
      this.$modal.confirm('是否确认删除选中的消息？').then(() => {
        return delMessage(messageIds)
      }).then(() => {
        this.getList()
        this.getUnread()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.unread-tip {
  display: inline-block;
  line-height: 28px;
  margin-left: 12px;
  font-size: 13px;
}
</style>
