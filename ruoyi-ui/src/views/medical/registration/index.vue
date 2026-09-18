<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="挂号号" prop="regNo">
        <el-input v-model="queryParams.regNo" placeholder="GH..." clearable size="small" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="患者" prop="patientName">
        <el-input v-model="queryParams.patientName" placeholder="患者姓名" clearable size="small" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="科室" prop="dept">
        <el-input v-model="queryParams.dept" placeholder="科室" clearable size="small" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="regStatus">
        <el-select v-model="queryParams.regStatus" placeholder="全部" clearable size="small" style="width:120px">
          <el-option label="待就诊" value="0" />
          <el-option label="已就诊" value="1" />
          <el-option label="已退号" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">挂号</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="regList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="挂号号" align="center" prop="regNo" width="140" />
      <el-table-column label="患者" align="center" prop="patientName" width="100" />
      <el-table-column label="科室" align="center" prop="dept" width="90" />
      <el-table-column label="接诊医生" align="center" prop="doctorName" width="100" />
      <el-table-column label="类型" align="center" prop="regType" width="80">
        <template slot-scope="scope">
          <el-tag size="mini">{{ regTypeText(scope.row.regType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="挂号费" align="center" prop="regFee" width="90">
        <template slot-scope="scope">{{ money(scope.row.regFee) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="regStatus" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.regStatus === '2' ? 'info' : (scope.row.regStatus === '1' ? 'success' : 'warning')" size="mini">
            {{ regStatusText(scope.row.regStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="挂号时间" align="center" prop="regTime" width="160" />
      <el-table-column label="操作" align="center" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-video-play" :disabled="scope.row.regStatus !== '0'" @click="handleStartVisit(scope.row)">开始就诊</el-button>
          <el-button size="mini" type="text" icon="el-icon-refresh-left" :disabled="scope.row.regStatus === '2'" @click="handleRefund(scope.row)">退号</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="560px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="患者" prop="patientId">
          <el-select v-model="form.patientId" filterable placeholder="请选择患者" style="width:100%" @change="onPatientChange">
            <el-option v-for="p in patientOptions" :key="p.patientId" :label="patientLabel(p)" :value="p.patientId" />
          </el-select>
        </el-form-item>
        <el-form-item label="号源排班" prop="scheduleId">
          <el-select v-model="form.scheduleId" filterable placeholder="请选择号源" style="width:100%" @change="onScheduleChange">
            <el-option
              v-for="s in availableSchedules"
              :key="s.scheduleId"
              :label="s.dept + ' · ' + s.doctorName + ' · ' + (s.period === '1' ? '下午' : '上午') + ' · ' + s.scheduleDate + ' (' + s.usedNum + '/' + s.totalNum + ')'"
              :value="s.scheduleId"
              :disabled="s.usedNum >= s.totalNum || s.status === '1'" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室" prop="dept">
          <el-input v-model="form.dept" placeholder="随号源自动带出" />
        </el-form-item>
        <el-form-item label="接诊医生" prop="doctorName">
          <el-input v-model="form.doctorName" placeholder="随号源自动带出" />
        </el-form-item>
        <el-form-item label="挂号类型" prop="regType">
          <el-select v-model="form.regType" placeholder="请选择" style="width:100%">
            <el-option label="普通门诊" value="0" />
            <el-option label="专家门诊" value="1" />
            <el-option label="急诊" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="挂号费" prop="regFee">
          <el-input-number v-model="form.regFee" :min="0" :precision="2" :step="1" style="width:100%" />
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
import { listRegistration, addRegistration, delRegistration, refundRegistration, startVisit } from '@/api/medical/registration'
import { listPatient } from '@/api/medical/patient'
import { listSchedule } from '@/api/medical/schedule'

export default {
  name: 'MedicalRegistration',
  data() {
    return {
      loading: false,
      showSearch: true,
      regList: [],
      total: 0,
      single: true,
      multiple: true,
      ids: [],
      title: '',
      open: false,
      patientOptions: [],
      scheduleOptions: [],
      queryParams: { pageNum: 1, pageSize: 10, regNo: undefined, patientName: undefined, dept: undefined, regStatus: undefined },
      form: {},
      rules: {
        patientId: [{ required: true, message: '请选择患者', trigger: 'change' }],
        scheduleId: [{ required: true, message: '请选择号源排班', trigger: 'change' }],
        regType: [{ required: true, message: '请选择挂号类型', trigger: 'change' }]
      }
    }
  },
  computed: {
    availableSchedules() {
      return this.scheduleOptions
    }
  },
  created() {
    this.getList()
    this.loadPatients()
    this.loadSchedules()
  },
  methods: {
    /** 患者下拉展示文案：性别代码转文字，年龄由出生日期推算 */
    patientLabel(p) {
      const gender = p.gender === '0' ? '男' : (p.gender === '1' ? '女' : (p.gender || ''))
      let age = p.age
      if ((age === undefined || age === null || age === '') && p.birthDate) {
        const b = new Date(p.birthDate)
        if (!isNaN(b.getTime())) {
          const now = new Date()
          age = now.getFullYear() - b.getFullYear()
          if (now.getMonth() < b.getMonth() || (now.getMonth() === b.getMonth() && now.getDate() < b.getDate())) {
            age = age - 1
          }
        }
      }
      return p.patientName + '(' + gender + '/' + (age !== undefined && age !== null ? age + '岁' : '年龄未知') + ')'
    },
    money(v) {
      if (v == null) return '0.00'
      return Number(v).toFixed(2)
    },
    regTypeText(t) {
      return t === '1' ? '专家门诊' : (t === '2' ? '急诊' : '普通门诊')
    },
    regStatusText(s) {
      return s === '1' ? '已就诊' : (s === '2' ? '已退号' : '待就诊')
    },
    getList() {
      this.loading = true
      listRegistration(this.queryParams).then(res => {
        this.regList = res.rows
        this.total = res.total
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    loadPatients() {
      listPatient({ pageNum: 1, pageSize: 9999 }).then(res => {
        this.patientOptions = res.rows || []
      }).catch(() => {})
    },
    loadSchedules() {
      listSchedule({ pageNum: 1, pageSize: 9999 }).then(res => {
        this.scheduleOptions = res.rows || []
      }).catch(() => {})
    },
    onPatientChange(val) {
      const p = this.patientOptions.find(i => i.patientId === val)
      if (p) this.form.patientName = p.patientName
    },
    onScheduleChange(val) {
      const s = this.scheduleOptions.find(i => i.scheduleId === val)
      if (s) {
        this.form.dept = s.dept
        this.form.doctorName = s.doctorName
      }
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
      this.ids = selection.map(i => i.registrationId)
      this.single = selection.length !== 1
      this.multiple = selection.length === 0
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '挂号'
    },
    reset() {
      this.form = { patientId: undefined, patientName: '', scheduleId: undefined, dept: '', doctorName: '', regType: '0', regFee: 0, remark: '' }
    },
    cancel() {
      this.open = false
      this.reset()
    },
    submitForm() {
      if (!this.$refs.form) return
      this.$refs.form.validate(valid => {
        if (!valid) return
        addRegistration(this.form).then(() => {
          this.$message.success('挂号成功，号源已占用')
          this.open = false
          this.getList()
          this.loadSchedules()
        }).catch(() => {})
      })
    },
    handleStartVisit(row) {
      this.$confirm('将按本次挂号信息（患者/科室/医生）生成一条诊疗草稿，可在「诊疗管理」中继续完善。', '开始就诊', { type: 'info' }).then(() => {
        startVisit(row.registrationId).then(res => {
          const diagnosisId = res && res.data
          this.$message.success('已生成诊疗草稿（ID ' + diagnosisId + '），请到「诊疗管理」补全诊断结果')
          this.getList()
        }).catch(() => {})
      }).catch(() => {})
    },
    handleRefund(row) {
      this.$confirm('确认退号？将释放对应号源。', '提示', { type: 'warning' }).then(() => {
        refundRegistration(row.registrationId).then(() => {
          this.$message.success('退号成功，号源已释放')
          this.getList()
          this.loadSchedules()
        }).catch(() => {})
      }).catch(() => {})
    },
    handleDelete(row) {
      const ids = row.registrationId ? [row.registrationId] : this.ids
      this.$confirm('确认删除选中的挂号记录？', '提示', { type: 'warning' })
        .then(() => delRegistration(ids)).then(() => {
          this.getList()
          this.loadSchedules()
          this.$message.success('删除成功')
        }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
</style>
