<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="药品名称" prop="drugName">
        <el-input
          v-model="queryParams.drugName"
          placeholder="请输入药品名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="药品分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择药品分类" clearable>
          <el-option label="西药" value="0" />
          <el-option label="中药" value="1" />
          <el-option label="中成药" value="2" />
          <el-option label="生物制品" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="生产厂家" prop="manufacturer">
        <el-input
          v-model="queryParams.manufacturer"
          placeholder="请输入生产厂家"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
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
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['medical:drug:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['medical:drug:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['medical:drug:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['medical:drug:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-warning"
          size="mini"
          @click="handleLowStock"
          v-hasPermi="['medical:drug:warn']"
        >低库存预警</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-time"
          size="mini"
          @click="handleNearExpiry"
          v-hasPermi="['medical:drug:expiry']"
        >近效期预警</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="drugList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="药品ID" align="center" prop="drugId" width="70" />
      <el-table-column label="药品名称" align="center" prop="drugName" :show-overflow-tooltip="true" />
      <el-table-column label="药品编码" align="center" prop="drugCode" :show-overflow-tooltip="true" />
      <el-table-column label="药品分类" align="center" prop="category">
        <template slot-scope="scope">
          <span>{{ formatCategory(scope.row.category) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="规格" align="center" prop="spec" :show-overflow-tooltip="true" />
      <el-table-column label="生产厂家" align="center" prop="manufacturer" :show-overflow-tooltip="true" />
      <el-table-column label="单位" align="center" prop="unit" width="60" />
      <el-table-column label="进价(元)" align="center" prop="purchasePrice" width="90" />
      <el-table-column label="售价(元)" align="center" prop="salePrice" width="90" />
      <el-table-column label="库存" align="center" prop="stock" width="70">
        <template slot-scope="scope">
          <span :style="{ color: scope.row.stock <= lowStockThreshold ? '#F56C6C' : 'inherit', fontWeight: scope.row.stock <= lowStockThreshold ? 'bold' : 'normal' }">{{ scope.row.stock }}</span>
        </template>
      </el-table-column>
      <el-table-column label="有效期至" align="center" prop="expiryDate" width="110">
        <template slot-scope="scope">
          <span :style="{ color: scope.row.expiryDate && daysLeft(scope.row.expiryDate) <= 90 ? '#E6A23C' : 'inherit' }">{{ parseTime(scope.row.expiryDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="70">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ formatStatus(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['medical:drug:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['medical:drug:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改药品信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="560px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="药品名称" prop="drugName">
          <el-input v-model="form.drugName" placeholder="请输入药品名称" />
        </el-form-item>
        <el-form-item label="药品编码" prop="drugCode">
          <el-input v-model="form.drugCode" placeholder="请输入药品编码" />
        </el-form-item>
        <el-form-item label="药品分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择药品分类">
            <el-option label="西药" value="0" />
            <el-option label="中药" value="1" />
            <el-option label="中成药" value="2" />
            <el-option label="生物制品" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格" prop="spec">
          <el-input v-model="form.spec" placeholder="如：0.25g*24粒/盒" />
        </el-form-item>
        <el-form-item label="生产厂家" prop="manufacturer">
          <el-input v-model="form.manufacturer" placeholder="请输入生产厂家" />
        </el-form-item>
        <el-form-item label="包装单位" prop="unit">
          <el-select v-model="form.unit" placeholder="请选择包装单位">
            <el-option label="盒" value="盒" />
            <el-option label="瓶" value="瓶" />
            <el-option label="支" value="支" />
            <el-option label="袋" value="袋" />
            <el-option label="片" value="片" />
          </el-select>
        </el-form-item>
        <el-form-item label="进价(元)" prop="purchasePrice">
          <el-input-number v-model="form.purchasePrice" :precision="2" :min="0" controls-position="right" placeholder="请输入进价" style="width: 100%" />
        </el-form-item>
        <el-form-item label="售价(元)" prop="salePrice">
          <el-input-number v-model="form.salePrice" :precision="2" :min="0" controls-position="right" placeholder="请输入售价" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :step="1" controls-position="right" placeholder="请输入库存数量" style="width: 100%" />
        </el-form-item>
        <el-form-item label="生产日期" prop="productionDate">
          <el-date-picker clearable
            v-model="form.productionDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择生产日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="有效期至" prop="expiryDate">
          <el-date-picker clearable
            v-model="form.expiryDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择有效期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 低库存预警对话框 -->
    <el-dialog title="低库存预警" :visible.sync="lowStockOpen" width="600px" append-to-body>
      <el-alert
        :closable="false"
        type="warning"
        :title="'库存数量 ≤ ' + lowStockThreshold + ' 的药品（共 ' + lowStockList.length + ' 项）'"
        style="margin-bottom:12px"
      />
      <el-table v-loading="lowStockLoading" :data="lowStockList" border>
        <el-table-column label="药品名称" align="center" prop="drugName" :show-overflow-tooltip="true" />
        <el-table-column label="分类" align="center" prop="category" width="100">
          <template slot-scope="scope"><span>{{ formatCategory(scope.row.category) }}</span></template>
        </el-table-column>
        <el-table-column label="当前库存" align="center" prop="stock" width="100">
          <template slot-scope="scope">
            <span :style="{ color: '#F56C6C', fontWeight: 'bold' }">{{ scope.row.stock }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="lowStockOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 近效期预警对话框 -->
    <el-dialog title="近效期预警" :visible.sync="nearExpiryOpen" width="660px" append-to-body>
      <el-alert
        :closable="false"
        type="warning"
        :title="'有效期至距今天数 ≤ ' + nearExpiryThreshold + ' 天的药品（共 ' + nearExpiryList.length + ' 项）'"
        style="margin-bottom:12px"
      />
      <el-table v-loading="nearExpiryLoading" :data="nearExpiryList" border>
        <el-table-column label="药品名称" align="center" prop="drugName" :show-overflow-tooltip="true" />
        <el-table-column label="分类" align="center" prop="category" width="100">
          <template slot-scope="scope"><span>{{ formatCategory(scope.row.category) }}</span></template>
        </el-table-column>
        <el-table-column label="有效期至" align="center" prop="expiryDate" width="120">
          <template slot-scope="scope"><span>{{ parseTime(scope.row.expiryDate, '{y}-{m}-{d}') }}</span></template>
        </el-table-column>
        <el-table-column label="剩余天数" align="center" width="130">
          <template slot-scope="scope">
            <span :style="{ color: daysLeft(scope.row.expiryDate) <= 0 ? '#F56C6C' : '#E6A23C', fontWeight: 'bold' }">
              {{ daysLeft(scope.row.expiryDate) }} 天{{ daysLeft(scope.row.expiryDate) <= 0 ? '（已过期）' : '' }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="nearExpiryOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDrug, getDrug, delDrug, addDrug, updateDrug, listDrugLowStock, listDrugNearExpiry } from "@/api/medical/drug";

export default {
  name: "MedicalDrug",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      drugList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        drugName: null,
        category: null,
        manufacturer: null,
        status: null
      },
      form: {},
      rules: {
        drugName: [
          { required: true, message: "药品名称不能为空", trigger: "blur" }
        ],
        purchasePrice: [
          { required: true, message: "进价不能为空", trigger: "blur" }
        ],
        salePrice: [
          { required: true, message: "售价不能为空", trigger: "blur" }
        ]
      },
      // 低库存预警
      lowStockThreshold: 10,
      lowStockOpen: false,
      lowStockLoading: false,
      lowStockList: [],
      // 近效期预警
      nearExpiryThreshold: 90,
      nearExpiryOpen: false,
      nearExpiryLoading: false,
      nearExpiryList: []
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 字典化药品分类 */
    formatCategory(category) {
      const map = { '0': '西药', '1': '中药', '2': '中成药', '3': '生物制品' };
      return map[category] || category;
    },
    /** 字典化状态 */
    formatStatus(status) {
      const map = { '0': '正常', '1': '停用' };
      return map[status] || status;
    },
    /** 计算距有效期剩余天数（已过期返回负数） */
    daysLeft(expiryDate) {
      if (!expiryDate) return '-';
      const raw = String(expiryDate);
      // 兼容 "2025-08-31" 与 ISO "2025-08-31T16:00:00.000+00:00" 两种返回格式
      const exp = raw.indexOf('T') > -1 ? new Date(raw) : new Date(raw.replace(/-/g, '/'));
      if (isNaN(exp.getTime())) return '-';
      return Math.ceil((exp - new Date()) / (1000 * 60 * 60 * 24));
    },
    /** 打开低库存预警对话框并加载数据 */
    handleLowStock() {
      this.lowStockOpen = true;
      this.getLowStockList();
    },
    /** 查询低库存药品列表 */
    getLowStockList() {
      this.lowStockLoading = true;
      listDrugLowStock({ threshold: this.lowStockThreshold }).then(response => {
        this.lowStockList = response.data || [];
        this.lowStockLoading = false;
      });
    },
    /** 打开近效期预警对话框并加载数据 */
    handleNearExpiry() {
      this.nearExpiryOpen = true;
      this.getNearExpiryList();
    },
    /** 查询近效期药品列表 */
    getNearExpiryList() {
      this.nearExpiryLoading = true;
      listDrugNearExpiry({ warnDays: this.nearExpiryThreshold }).then(response => {
        this.nearExpiryList = response.data || [];
        this.nearExpiryLoading = false;
      });
    },
    /** 查询药品信息列表 */
    getList() {
      this.loading = true;
      listDrug(this.queryParams).then(response => {
        this.drugList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        drugId: null,
        drugName: null,
        drugCode: null,
        category: "0",
        spec: null,
        manufacturer: null,
        unit: "盒",
        purchasePrice: null,
        salePrice: null,
        stock: 0,
        productionDate: null,
        expiryDate: null,
        status: "0",
        remark: null
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.drugId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加药品信息";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const drugId = row.drugId || this.ids;
      getDrug(drugId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改药品信息";
      });
    },
    /** 提交按钮 */
    submitForm() {
      if (!this.$refs.form) return
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.drugId != null) {
            updateDrug(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addDrug(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const drugIds = row.drugId || this.ids;
      this.$modal.confirm('是否确认删除药品信息编号为"' + drugIds + '"的数据项？').then(() => {
        return delDrug(drugIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('/medical/drug/export', {
        ...this.queryParams
      }, `drug_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
