<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="患者姓名" prop="patientName">
        <el-input
          v-model="queryParams.patientName"
          placeholder="请输入患者姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="患者性别" prop="gender">
        <el-select v-model="queryParams.gender" placeholder="请选择患者性别" clearable>
          <el-option label="男" value="0" />
          <el-option label="女" value="1" />
          <el-option label="未知" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input
          v-model="queryParams.idCard"
          placeholder="请输入身份证号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="患者状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择患者状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="住院" value="1" />
          <el-option label="出院" value="2" />
          <el-option label="死亡" value="3" />
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
          v-hasPermi="['medical:patient:add']"
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
          v-hasPermi="['medical:patient:edit']"
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
          v-hasPermi="['medical:patient:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['medical:patient:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="patientList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="患者ID" align="center" prop="patientId" />
      <el-table-column label="患者姓名" align="center" prop="patientName" />
      <el-table-column label="患者性别" align="center" prop="gender">
        <template slot-scope="scope">
          <span>{{ formatGender(scope.row.gender) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="出生日期" align="center" prop="birthDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.birthDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="身份证号" align="center" prop="idCard">
        <template slot-scope="scope">{{ maskId(scope.row.idCard) }}</template>
      </el-table-column>
      <el-table-column label="联系电话" align="center" prop="phone">
        <template slot-scope="scope">{{ maskPhone(scope.row.phone) }}</template>
      </el-table-column>
      <el-table-column label="血型" align="center" prop="bloodType" />
      <el-table-column label="患者状态" align="center" prop="status">
        <template slot-scope="scope">
          <span>{{ formatStatus(scope.row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['medical:patient:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['medical:patient:remove']"
          >删除</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-tickets"
            @click="handleHistory(scope.row)"
            v-hasPermi="['medical:diagnosis:query']"
          >诊疗历史</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-data-analysis"
            @click="handleRisk(scope.row)"
          >风险预测</el-button>
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

    <!-- 添加或修改患者信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="患者姓名" prop="patientName">
          <el-input v-model="form.patientName" placeholder="请输入患者姓名" />
        </el-form-item>
        <el-form-item label="患者性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio label="0">男</el-radio>
            <el-radio label="1">女</el-radio>
            <el-radio label="2">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker clearable
            v-model="form.birthDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择出生日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="住址" prop="address">
          <el-input v-model="form.address" type="textarea" placeholder="请输入住址" />
        </el-form-item>
        <el-form-item label="血型" prop="bloodType">
          <el-select v-model="form.bloodType" placeholder="请选择血型">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="O" value="O" />
            <el-option label="AB" value="AB" />
          </el-select>
        </el-form-item>
        <el-form-item label="过敏史" prop="allergyHistory">
          <el-input v-model="form.allergyHistory" type="textarea" placeholder="请输入过敏史" />
        </el-form-item>
        <el-form-item label="患者状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择患者状态">
            <el-option label="正常" value="0" />
            <el-option label="住院" value="1" />
            <el-option label="出院" value="2" />
            <el-option label="死亡" value="3" />
          </el-select>
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

    <!-- 患者诊疗历史对话框 -->
    <el-dialog :title="'诊疗历史 - ' + historyPatientName" :visible.sync="historyOpen" width="900px" append-to-body>
      <el-table v-loading="historyLoading" :data="historyList" border>
        <el-table-column label="就诊日期" align="center" prop="visitDate" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.visitDate, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="科室" align="center" prop="department" width="100" />
        <el-table-column label="主治医生" align="center" prop="doctorName" width="100" />
        <el-table-column label="主诉" align="center" prop="chiefComplaint" :show-overflow-tooltip="true" />
        <el-table-column label="诊断结果" align="center" prop="diagnosisResult" :show-overflow-tooltip="true" />
        <el-table-column label="诊疗费用" align="center" prop="cost" width="100" />
        <el-table-column label="状态" align="center" prop="status" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '2' ? 'success' : (scope.row.status === '1' ? 'warning' : 'info')">{{ { '0':'待就诊', '1':'就诊中', '2':'已完成' }[scope.row.status] || scope.row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="historyOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPatient, getPatient, delPatient, addPatient, updatePatient } from "@/api/medical/patient";
import { listDiagnosisByPatientId } from "@/api/medical/diagnosis";
import { listFollowupByPatientId } from "@/api/medical/followup";

export default {
  name: "MedicalPatient",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      historyOpen: false,
      historyList: [],
      historyLoading: false,
      historyPatientName: '',
      followupOpen: false,
      followupList: [],
      followupLoading: false,
      followupPatientName: '',
      total: 0,
      patientList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        patientName: null,
        gender: null,
        idCard: null,
        status: null
      },
      form: {},
      rules: {
        patientName: [
          { required: true, message: "患者姓名不能为空", trigger: "blur" }
        ],
        idCard: [
          { required: true, message: "身份证号不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    formatGender(gender) {
      const map = { '0': '男', '1': '女', '2': '未知' };
      return map[gender] || gender;
    },
    formatStatus(status) {
      const map = { '0': '正常', '1': '住院', '2': '出院', '3': '死亡' };
      return map[status] || status;
    },
    /** 身份证号列表掩码：保留前 4 后 4，中间星号（详情/编辑页完整显示） */
    maskId(val) {
      if (!val || val.length < 8) return val;
      return val.substring(0, 4) + '**********' + val.substring(val.length - 4);
    },
    /** 手机号列表掩码：138****8001 */
    maskPhone(val) {
      if (!val || val.length < 7) return val;
      return val.substring(0, 3) + '****' + val.substring(val.length - 4);
    },
    /** 优化①联动点：从患者档案一键进入疾病风险预测，并带入患者信息 */
    handleRisk(row) {
      this.$router.push({
        path: '/medical/ai/risk',
        query: {
          patientId: row.patientId,
          patientName: row.patientName,
          age: this.calcAge(row.birthDate),
          gender: row.gender === '1' ? '男' : (row.gender === '2' ? '女' : '')
        }
      });
    },
    /** 由出生日期计算年龄（周岁） */
    calcAge(birthDate) {
      if (!birthDate) return undefined;
      const b = new Date(birthDate.replace(/-/g, '/'));
      const n = new Date();
      let age = n.getFullYear() - b.getFullYear();
      const m = n.getMonth() - b.getMonth();
      if (m < 0 || (m === 0 && n.getDate() < b.getDate())) age--;
      return age >= 0 ? age : undefined;
    },
    /** 打开患者诊疗历史对话框 */
    handleHistory(row) {
      this.historyOpen = true;
      this.historyPatientName = row.patientName;
      this.getHistory(row.patientId);
    },
    /** 查询患者诊疗历史 */
    getHistory(patientId) {
      this.historyLoading = true;
      listDiagnosisByPatientId(patientId).then(response => {
        this.historyList = response;
        this.historyLoading = false;
      });
    },
    /** 打开患者随访记录对话框 */
    handleFollowup(row) {
      this.followupOpen = true;
      this.followupPatientName = row.patientName;
      this.getFollowupList(row.patientId);
    },
    /** 查询患者随访记录 */
    getFollowupList(patientId) {
      this.followupLoading = true;
      listFollowupByPatientId(patientId).then(response => {
        this.followupList = response;
        this.followupLoading = false;
      });
    },
    getList() {
      this.loading = true;
      listPatient(this.queryParams).then(response => {
        this.patientList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        patientId: null,
        patientName: null,
        gender: null,
        birthDate: null,
        idCard: null,
        phone: null,
        address: null,
        bloodType: null,
        allergyHistory: null,
        status: null,
        remark: null
      };
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.patientId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加患者信息";
    },
    handleUpdate(row) {
      this.reset();
      const patientId = row.patientId || this.ids;
      getPatient(patientId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改患者信息";
      });
    },
    submitForm() {
      if (!this.$refs.form) return
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.patientId != null) {
            updatePatient(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addPatient(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const patientIds = row.patientId || this.ids;
      this.$modal.confirm('是否确认删除患者信息编号为"' + patientIds + '"的数据项？').then(() => {
        return delPatient(patientIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('/medical/patient/export', {
        ...this.queryParams
      }, `patient_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
