<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="类别" prop="itemType">
        <el-select v-model="queryParams.itemType" placeholder="请选择类别" clearable size="small" style="width:140px">
          <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称" prop="itemName">
        <el-input v-model="queryParams.itemName" placeholder="请输入项目名称" clearable size="small" @keyup.enter.native="handleQuery" />
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
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="priceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="类别" align="center" prop="itemType" width="110" />
      <el-table-column label="项目名称" align="center" prop="itemName" min-width="160" show-overflow-tooltip />
      <el-table-column label="规格/说明" align="center" prop="spec" min-width="160" show-overflow-tooltip />
      <el-table-column label="单位" align="center" prop="unit" width="70" />
      <el-table-column label="价格(元)" align="center" prop="price" width="100">
        <template slot-scope="scope">{{ money(scope.row.price) }}</template>
      </el-table-column>
      <el-table-column label="医保" align="center" prop="insuranceFlag" width="70">
        <template slot-scope="scope">
          <el-tag :type="scope.row.insuranceFlag === '1' ? 'success' : 'info'" size="mini">
            {{ scope.row.insuranceFlag === '1' ? '医保' : '自费' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="140">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="类别" prop="itemType">
          <el-select v-model="form.itemType" placeholder="请选择" style="width:100%">
            <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称" prop="itemName">
          <el-input v-model="form.itemName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="规格说明" prop="spec">
          <el-input v-model="form.spec" placeholder="如 0.25g*24粒/盒" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="次/日/盒/瓶" />
        </el-form-item>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="医保" prop="insuranceFlag">
          <el-radio-group v-model="form.insuranceFlag">
            <el-radio label="1">医保</el-radio>
            <el-radio label="0">自费</el-radio>
          </el-radio-group>
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
import { listPrice, addPrice, updatePrice, delPrice } from '@/api/medical/price'

export default {
  name: 'MedicalPrice',
  data() {
    return {
      loading: false,
      showSearch: true,
      priceList: [],
      total: 0,
      single: true,
      multiple: true,
      ids: [],
      title: '',
      open: false,
      typeOptions: ['挂号费', '诊查费', '检查费', '化验费', '治疗费', '手术费', '护理费', '床位费', '药品', '其它'],
      queryParams: { pageNum: 1, pageSize: 10, itemType: undefined, itemName: undefined },
      form: {},
      rules: {
        itemType: [{ required: true, message: '请选择类别', trigger: 'change' }],
        itemName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
        price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    money(v) {
      if (v == null) return '0.00'
      return Number(v).toFixed(2)
    },
    getList() {
      this.loading = true
      listPrice(this.queryParams).then(res => {
        this.priceList = res.rows
        this.total = res.total
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    // 注意：方法名不要叫 resetForm（会被全局同名方法遮蔽导致递归）
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(i => i.itemId)
      this.single = selection.length !== 1
      this.multiple = selection.length === 0
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增价目'
    },
    handleUpdate(row) {
      this.reset()
      this.form = JSON.parse(JSON.stringify(row))
      this.open = true
      this.title = '修改价目'
    },
    reset() {
      this.form = { itemType: '挂号费', itemName: '', spec: '', unit: '次', price: 0, insuranceFlag: '1' }
    },
    cancel() {
      this.open = false
      this.reset()
    },
    submitForm() {
      if (!this.$refs.form) return
      this.$refs.form.validate(valid => {
        if (!valid) return
        const op = this.form.itemId ? updatePrice(this.form) : addPrice(this.form)
        op.then(() => {
          this.$message.success('保存成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const ids = row.itemId ? [row.itemId] : this.ids
      this.$confirm('确认删除选中的价目？', '提示', { type: 'warning' }).then(() => delPrice(ids)).then(() => {
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
