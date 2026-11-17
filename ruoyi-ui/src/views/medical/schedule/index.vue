<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="科室" prop="dept">
        <el-input v-model="queryParams.dept" placeholder="请输入科室" clearable size="small" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="医生" prop="doctorName">
        <el-input v-model="queryParams.doctorName" placeholder="请输入医生" clearable size="small" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="时段" prop="period">
        <el-select v-model="queryParams.period" placeholder="全部" clearable size="small" style="width:120px">
          <el-option label="上午" value="0" />
          <el-option label="下午" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable size="small" style="width:120px">
          <el-option label="出诊" value="0" />
          <el-option label="停诊" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增排班</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="scheduleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="科室" align="center" prop="dept" width="100" />
      <el-table-column label="出诊医生" align="center" prop="doctorName" width="120" />
      <el-table-column label="排班日期" align="center" prop="scheduleDate" width="130">
        <template slot-scope="scope">{{ parseTime(scope.row.scheduleDate, '{y}-{m}-{d}') }}</template>
      </el-table-column>
      <el-table-column label="时段" align="center" prop="period" width="80">
        <template slot-scope="scope">{{ scope.row.period === '1' ? '下午' : '上午' }}</template>
      </el-table-column>
      <el-table-column label="号源(已用/总数)" align="center" width="150">
        <template slot-scope="scope">
          <el-tag :type="scope.row.usedNum >= scope.row.totalNum ? 'danger' : 'success'" size="mini">
            {{ scope.row.usedNum }} / {{ scope.row.totalNum }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '1' ? 'info' : 'success'" size="mini">
            {{ scope.row.status === '1' ? '停诊' : '出诊' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="140">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="科室" prop="dept">
          <el-input v-model="form.dept" placeholder="如 内科" />
        </el-form-item>
        <el-form-item label="出诊医生" prop="doctorName">
          <el-input v-model="form.doctorName" placeholder="请输入医生姓名" />
        </el-form-item>
        <el-form-item label="排班日期" prop="scheduleDate">
          <el-date-picker v-model="form.scheduleDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="时段" prop="period">
          <el-radio-group v-model="form.period">
            <el-radio label="0">上午</el-radio>
            <el-radio label="1">下午</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="号源总数" prop="totalNum">
          <el-input-number v-model="form.totalNum" :min="1" :max="999" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">出诊</el-radio>
            <el-radio label="1">停诊</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listSchedule, addSchedule, updateSchedule, delSchedule } from '@/api/medical/schedule'

export default {
  name: 'MedicalSchedule',
  data() {
    return {
      loading: false,
      // 排班只能选今天及以后的日期
      dateOptions: {
        disabledDate(time) {
          const d = new Date(time); d.setHours(0, 0, 0, 0)
          const t = new Date(); t.setHours(0, 0, 0, 0)
          return d.getTime() < t.getTime()
        }
      },
      showSearch: true,
      scheduleList: [],
      total: 0,
      single: true,
      multiple: true,
      ids: [],
      title: '',
      open: false,
      queryParams: { pageNum: 1, pageSize: 10, dept: undefined, doctorName: undefined, period: undefined, status: undefined },
      form: {},
      rules: {
        dept: [{ required: true, message: '请输入科室', trigger: 'blur' }],
        doctorName: [{ required: true, message: '请输入医生姓名', trigger: 'blur' }],
        scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }],
        totalNum: [{ required: true, message: '请输入号源总数', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listSchedule(this.queryParams).then(res => {
        this.scheduleList = res.rows
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
      this.ids = selection.map(i => i.scheduleId)
      this.single = selection.length !== 1
      this.multiple = selection.length === 0
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增排班'
    },
    handleUpdate(row) {
      this.reset()
      this.form = JSON.parse(JSON.stringify(row))
      this.open = true
      this.title = '修改排班'
    },
    reset() {
      this.form = { dept: '', doctorName: '', scheduleDate: '', period: '0', totalNum: 30, status: '0', remark: '' }
    },
    cancel() {
      this.open = false
      this.reset()
    },
    submitForm() {
      if (!this.$refs.form) return
      this.$refs.form.validate(valid => {
        if (!valid) return
        const op = this.form.scheduleId ? updateSchedule(this.form) : addSchedule(this.form)
        op.then(() => {
          this.$message.success('保存成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const ids = row.scheduleId ? [row.scheduleId] : this.ids
      this.$confirm('确认删除选中的排班？已挂号记录不受影响。', '提示', { type: 'warning' })
        .then(() => delSchedule(ids)).then(() => {
          this.getList()
          this.$message.success('删除成功')
        }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
</style>
