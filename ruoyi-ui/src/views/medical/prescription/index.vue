<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="患者" prop="patientName">
        <el-input v-model="queryParams.patientName" placeholder="患者姓名" clearable size="small" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="开方医生" prop="doctorName">
        <el-input v-model="queryParams.doctorName" placeholder="医生姓名" clearable size="small" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="prescriptionStatus">
        <el-select v-model="queryParams.prescriptionStatus" placeholder="全部" clearable size="small" style="width:120px">
          <el-option label="待发药" value="0" />
          <el-option label="部分发药" value="1" />
          <el-option label="已发药" value="2" />
          <el-option label="已作废" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">开方</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="presList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="处方号" align="center" prop="prescriptionNo" width="150" />
      <el-table-column label="患者" align="center" prop="patientName" width="100" />
      <el-table-column label="开方医生" align="center" prop="doctorName" width="100" />
      <el-table-column label="科室" align="center" prop="dept" width="90" />
      <el-table-column label="诊断" align="center" prop="diagnosis" min-width="160" show-overflow-tooltip />
      <el-table-column label="金额(元)" align="center" prop="totalAmount" width="100">
        <template slot-scope="scope">{{ money(scope.row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="prescriptionStatus" width="90">
        <template slot-scope="scope">
          <el-tag :type="presStatusType(scope.row.prescriptionStatus)" size="mini">{{ presStatusText(scope.row.prescriptionStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开方时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)">明细</el-button>
          <el-button size="mini" type="text" icon="el-icon-sell" :disabled="scope.row.prescriptionStatus === '2' || scope.row.prescriptionStatus === '3'" @click="handleDispense(scope.row)">发药</el-button>
          <el-button size="mini" type="text" icon="el-icon-close" :disabled="scope.row.prescriptionStatus === '3'" @click="handleCancel(scope.row)">作废</el-button>
          <el-button size="mini" type="text" icon="el-icon-refresh-left" :disabled="scope.row.prescriptionStatus === '0' || scope.row.prescriptionStatus === '3'" @click="handleReturn(scope.row)">退药</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 开方对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="患者" prop="patientId">
              <el-select v-model="form.patientId" filterable placeholder="请选择患者" style="width:100%" @change="onPatientChange">
                <el-option v-for="p in patientOptions" :key="p.patientId" :label="patientLabel(p)" :value="p.patientId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开方医生" prop="doctorName">
              <el-input v-model="form.doctorName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科室" prop="dept">
              <el-input v-model="form.dept" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="诊断" prop="diagnosis">
              <el-input v-model="form.diagnosis" placeholder="请输入诊断" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">处方明细</el-divider>
        <el-button type="success" plain icon="el-icon-plus" size="mini" @click="addItemRow">添加药品</el-button>
        <el-table :data="form.items" style="margin-top:10px">
          <el-table-column label="药品" min-width="220">
            <template slot-scope="scope">
              <el-select v-model="scope.row.drugId" filterable placeholder="选择药品" style="width:100%" @change="(val) => onDrugChange(val, scope.$index)">
                <el-option v-for="d in drugOptions" :key="d.drugId" :label="d.drugName + '(' + d.spec + ')'" :value="d.drugId" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="单价" align="center" width="90">
            <template slot-scope="scope">{{ money(scope.row.price) }}</template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.quantity" :min="1" :max="999" size="mini" style="width:100%" />
            </template>
          </el-table-column>
          <el-table-column label="用法用量" min-width="160">
            <template slot-scope="scope">
              <el-input v-model="scope.row.dosage" size="mini" placeholder="如 口服 一次2粒 一日3次" />
            </template>
          </el-table-column>
          <el-table-column label="小计" align="center" width="90">
            <template slot-scope="scope">{{ money(subtotal(scope.row)) }}</template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="60">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-delete" @click="removeItemRow(scope.$index)" />
            </template>
          </el-table-column>
        </el-table>
        <div style="text-align:right;margin-top:8px;color:#909399;font-size:12px">单价按价目表自动带出，无需手工填写</div>
        <div style="text-align:right;margin-top:4px">合计：<b>{{ money(formTotal) }}</b> 元</div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 发药对话框 -->
    <el-dialog title="发药" :visible.sync="dispenseOpen" width="680px" append-to-body>
      <el-alert type="info" :closable="false" style="margin-bottom:12px" title="系统将按本次发药数量扣减药品库存，并校验用药安全。" />
      <el-form :model="dispenseForm" label-width="90px">
        <el-form-item label="处方号">{{ dispenseForm.prescriptionNo }}</el-form-item>
        <el-form-item label="发药人" prop="operator">
          <el-input v-model="dispenseForm.operator" placeholder="请输入发药人姓名" style="width:240px" />
        </el-form-item>
      </el-form>
      <el-table :data="dispenseForm.items">
        <el-table-column label="药品" align="center" prop="drugName" min-width="160" />
        <el-table-column label="开方数量" align="center" prop="quantity" width="90" />
        <el-table-column label="已发数量" align="center" prop="dispensedQuantity" width="90" />
        <el-table-column label="本次发药" width="140">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.disQty" :min="0" :max="scope.row.maxQty" size="mini" style="width:100%" />
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitDispense">确 定</el-button>
        <el-button @click="dispenseOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 明细对话框 -->
    <el-dialog title="处方明细" :visible.sync="detailOpen" width="680px" append-to-body>
      <el-descriptions :column="2" border size="mini">
        <el-descriptions-item label="处方号">{{ detail.prescriptionNo }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
        <el-descriptions-item label="开方医生">{{ detail.doctorName }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ detail.dept }}</el-descriptions-item>
        <el-descriptions-item label="诊断" :span="2">{{ detail.diagnosis }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailItems" style="margin-top:12px">
        <el-table-column label="药品" align="center" prop="drugName" min-width="160" />
        <el-table-column label="规格" align="center" prop="spec" min-width="120" />
        <el-table-column label="数量" align="center" prop="quantity" width="70" />
        <el-table-column label="单价" align="center" width="90">
          <template slot-scope="scope">{{ money(scope.row.price) }}</template>
        </el-table-column>
        <el-table-column label="用法用量" align="center" prop="dosage" min-width="160" />
        <el-table-column label="用药警示" min-width="200">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.allergyWarning" type="danger" size="mini">{{ scope.row.allergyWarning }}</el-tag>
            <span v-else type="info">—</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { listPrescription, addPrescription, delPrescription, dispensePrescription, cancelPrescription, returnPrescription, listPrescriptionItems } from '@/api/medical/prescription'
import { listPatient } from '@/api/medical/patient'
import { listDrug } from '@/api/medical/drug'

export default {
  name: 'MedicalPrescription',
  data() {
    return {
      loading: false,
      showSearch: true,
      presList: [],
      total: 0,
      single: true,
      multiple: true,
      ids: [],
      title: '',
      open: false,
      dispenseOpen: false,
      detailOpen: false,
      patientOptions: [],
      drugOptions: [],
      queryParams: { pageNum: 1, pageSize: 10, patientName: undefined, doctorName: undefined, prescriptionStatus: undefined },
      form: {},
      dispenseForm: { prescriptionId: undefined, prescriptionNo: '', operator: '', items: [] },
      detail: {},
      detailItems: [],
      rules: {
        patientId: [{ required: true, message: '请选择患者', trigger: 'change' }],
        doctorName: [{ required: true, message: '请输入开方医生', trigger: 'blur' }],
        dept: [{ required: true, message: '请输入科室', trigger: 'blur' }]
      }
    }
  },
  computed: {
    formTotal() {
      if (!this.form.items) return 0
      return this.form.items.reduce((s, it) => s + (Number(it.quantity) || 0) * (Number(it.price) || 0), 0)
    }
  },
  created() {
    this.getList()
    this.loadPatients()
    this.loadDrugs()
  },
  methods: {
    money(v) {
      if (v == null) return '0.00'
      return Number(v).toFixed(2)
    },
    /** 患者下拉展示文案：性别代码转文字，年龄由出生日期推算 */
    patientLabel(p) {
      if (!p) return ''
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
    subtotal(row) {
      return (Number(row.quantity) || 0) * (Number(row.price) || 0)
    },
    presStatusText(s) {
      return s === '1' ? '部分发药' : s === '2' ? '已发药' : s === '3' ? '已作废' : '待发药'
    },
    presStatusType(s) {
      return s === '1' ? 'primary' : s === '2' ? 'success' : s === '3' ? 'info' : 'warning'
    },
    getList() {
      this.loading = true
      listPrescription(this.queryParams).then(res => {
        this.presList = res.rows
        this.total = res.total
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    loadPatients() {
      listPatient({ pageNum: 1, pageSize: 9999 }).then(res => {
        this.patientOptions = res.rows || []
      }).catch(() => {})
    },
    loadDrugs() {
      listDrug({ pageNum: 1, pageSize: 9999 }).then(res => {
        this.drugOptions = res.rows || []
      }).catch(() => {})
    },
    onPatientChange(val) {
      const p = this.patientOptions.find(i => i.patientId === val)
      if (p) this.form.patientName = p.patientName
    },
    onDrugChange(val, idx) {
      const d = this.drugOptions.find(i => i.drugId === val)
      if (d) {
        this.form.items[idx].drugName = d.drugName
        this.form.items[idx].spec = d.spec
        this.form.items[idx].price = d.salePrice
      }
    },
    addItemRow() {
      this.form.items.push({ drugId: undefined, drugName: '', spec: '', quantity: 1, price: 0, dosage: '' })
    },
    removeItemRow(idx) {
      this.form.items.splice(idx, 1)
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
      this.ids = selection.map(i => i.prescriptionId)
      this.single = selection.length !== 1
      this.multiple = selection.length === 0
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '开方'
    },
    reset() {
      this.form = { patientId: undefined, patientName: '', doctorName: '', dept: '', diagnosis: '', items: [] }
    },
    cancel() {
      this.open = false
      this.reset()
    },
    submitForm() {
      if (!this.$refs.form) return
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (!this.form.items || this.form.items.length === 0) {
          this.$message.warning('请至少添加一种药品')
          return
        }
        if (this.form.items.some(it => !it.drugId || !it.quantity || it.quantity < 1)) {
          this.$message.warning('请完整填写每种药品的数量')
          return
        }
        const payload = {
          patientId: this.form.patientId,
          patientName: this.form.patientName,
          doctorName: this.form.doctorName,
          dept: this.form.dept,
          diagnosis: this.form.diagnosis,
          items: this.form.items.map(it => ({
            drugId: it.drugId,
            drugName: it.drugName,
            spec: it.spec,
            quantity: it.quantity,
            price: it.price,
            dosage: it.dosage
          }))
        }
        addPrescription(payload).then(() => {
          this.$message.success('开方成功')
          this.open = false
          this.getList()
          this.loadDrugs()
        }).catch(() => {})
      })
    },
    handleDispense(row) {
      listPrescriptionItems(row.prescriptionId).then(res => {
        const items = res.data || res.rows || []
        this.dispenseForm = {
          prescriptionId: row.prescriptionId,
          prescriptionNo: row.prescriptionNo,
          operator: '',
          items: items.map(it => {
            const ordered = Number(it.quantity) || 0
            const dispensed = Number(it.dispensedQuantity) || 0
            const remain = ordered - dispensed
            return {
              itemId: it.itemId,
              drugName: it.drugName,
              quantity: ordered,
              dispensedQuantity: dispensed,
              maxQty: remain,
              disQty: remain
            }
          })
        }
        this.dispenseOpen = true
      }).catch(() => {})
    },
    submitDispense() {
      if (!this.dispenseForm.operator) {
        this.$message.warning('请输入发药人')
        return
      }
      const items = this.dispenseForm.items
        .filter(it => it.disQty > 0)
        .map(it => ({ itemId: it.itemId, quantity: it.disQty }))
      if (items.length === 0) {
        this.$message.warning('请至少填写一种药品的发药数量')
        return
      }
      dispensePrescription({
        prescriptionId: this.dispenseForm.prescriptionId,
        items: items,
        operator: this.dispenseForm.operator
      }).then(() => {
        this.$message.success('发药成功，库存已扣减')
        this.dispenseOpen = false
        this.getList()
        this.loadDrugs()
      }).catch(() => {})
    },
    handleDetail(row) {
      listPrescriptionItems(row.prescriptionId).then(res => {
        this.detail = row
        this.detailItems = res.data || res.rows || []
        this.detailOpen = true
      }).catch(() => {})
    },
    handleCancel(row) {
      this.$confirm('确认作废该处方？作废后不可再发药。', '提示', { type: 'warning' }).then(() => {
        cancelPrescription(row.prescriptionId).then(() => {
          this.$message.success('处方已作废')
          this.getList()
        }).catch(() => {})
      }).catch(() => {})
    },
    handleReturn(row) {
      this.$confirm('确认退药？系统将回滚已发药品的库存，并标记发药记录为已退。', '提示', { type: 'warning' }).then(() => {
        returnPrescription(row.prescriptionId).then(() => {
          this.$message.success('退药成功，库存已回滚')
          this.getList()
          this.loadDrugs()
        }).catch(() => {})
      }).catch(() => {})
    },
    handleDelete(row) {
      const ids = row.prescriptionId ? [row.prescriptionId] : this.ids
      this.$confirm('确认删除选中的处方？', '提示', { type: 'warning' })
        .then(() => delPrescription(ids)).then(() => {
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
