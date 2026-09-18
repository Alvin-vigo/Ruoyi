<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="70px">
      <el-form-item label="标题">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="queryParams.category" placeholder="请选择分类" clearable>
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
      </el-form-item>
      <el-form-item label="同步状态">
        <el-select v-model="queryParams.synced" placeholder="请选择" clearable>
          <el-option label="未同步" value="0" />
          <el-option label="已同步" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-sort" size="mini" :loading="syncing" @click="handleSync">
          同步到向量库
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-refresh" size="mini" @click="getList">刷新</el-button>
      </el-col>
    </el-row>

    <el-alert
      class="mb8"
      type="info"
      :closable="false"
      show-icon
      title="说明：本表是知识的可编辑源头；点击「同步到向量库」后内容才会写入 Chroma 参与检索。修改或新增后需重新同步。"
    />

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="编号" align="center" prop="knowledgeId" width="70" />
      <el-table-column label="标题" prop="title" width="160" show-overflow-tooltip />
      <el-table-column label="分类" align="center" prop="category" width="100" />
      <el-table-column label="内容" prop="content" min-width="260" show-overflow-tooltip />
      <el-table-column label="同步" align="center" width="90">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.synced === '1' ? 'success' : 'warning'">
            {{ scope.row.synced === '1' ? '已同步' : '未同步' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" width="140">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题，如：糖尿病典型症状" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" style="width:100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入知识正文，内容越具体越利于检索命中" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="可选" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listKnowledge, addKnowledge, updateKnowledge, delKnowledge, syncKnowledge, unwrapAi } from '@/api/medical/ai'

export default {
  name: 'MedicalAiKnowledge',
  data() {
    return {
      loading: false,
      syncing: false,
      ids: [],
      single: true,
      multiple: true,
      list: [],
      total: 0,
      open: false,
      title: '',
      categories: ['常见病', '用药指导', '检查检验', '保健养生', '就诊流程'],
      queryParams: { pageNum: 1, pageSize: 10, title: null, category: null, synced: null },
      form: {},
      rules: {
        title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
        content: [{ required: true, message: '内容不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listKnowledge(this.queryParams).then(raw => {
        const res = unwrapAi(raw)
        this.list = res.rows
        this.total = res.total
        this.loading = false
      }).catch(() => { this.loading = false })
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
      this.ids = selection.map(item => item.knowledgeId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加知识条目'
    },
    handleUpdate(row) {
      this.reset()
      const id = row.knowledgeId || this.ids[0]
      const target = this.list.find(i => i.knowledgeId === id)
      this.form = Object.assign({}, target)
      this.open = true
      this.title = '修改知识条目'
    },
    submitForm() {
      // el-dialog 懒渲染：首帧 $refs.form 可能尚未挂载，这里做防御避免点击报错
      if (!this.$refs.form) {
        return
      }
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (this.form.knowledgeId) {
          updateKnowledge(this.form).then(() => {
            this.$message.success('修改成功，请记得重新同步')
            this.open = false
            this.getList()
          })
        } else {
          addKnowledge(this.form).then(() => {
            this.$message.success('新增成功，请执行同步')
            this.open = false
            this.getList()
          })
        }
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = { knowledgeId: undefined, title: undefined, content: undefined, category: '常见病', remark: undefined }
      this.resetForm('form')
    },
    handleDelete(row) {
      const ids = row.knowledgeId ? [row.knowledgeId] : this.ids
      this.$confirm('确认删除选中的 ' + ids.length + ' 条知识？', '提示', { type: 'warning' }).then(() => {
        return delKnowledge(ids.join(','))
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    handleSync() {
      this.syncing = true
      syncKnowledge().then(msg => {
        this.syncing = false
        this.$message.success(msg || '同步完成')
        this.getList()
      }).catch(() => { this.syncing = false })
    }
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
</style>
