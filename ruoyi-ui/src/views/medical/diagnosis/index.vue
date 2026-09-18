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
      <el-form-item label="科室" prop="department">
        <el-input
          v-model="queryParams.department"
          placeholder="请输入科室"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="医生" prop="doctorName">
        <el-input
          v-model="queryParams.doctorName"
          placeholder="请输入医生"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="就诊日期">
        <el-date-picker
          v-model="dateRange"
          size="small"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="诊疗状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择诊疗状态" clearable>
          <el-option label="待就诊" value="0" />
          <el-option label="就诊中" value="1" />
          <el-option label="已完成" value="2" />
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
          v-hasPermi="['medical:diagnosis:add']"
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
          v-hasPermi="['medical:diagnosis:edit']"
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
          v-hasPermi="['medical:diagnosis:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['medical:diagnosis:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="diagnosisList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="诊疗ID" align="center" prop="diagnosisId" />
      <el-table-column label="患者ID" align="center" prop="patientId" />
      <el-table-column label="患者姓名" align="center" prop="patientName" />
      <el-table-column label="就诊日期" align="center" prop="visitDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.visitDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="科室" align="center" prop="department" />
      <el-table-column label="主治医生" align="center" prop="doctorName" />
      <el-table-column label="诊断结果" align="center" prop="diagnosisResult" />
      <el-table-column label="诊疗费用" align="center" prop="cost" />
      <el-table-column label="诊疗状态" align="center" prop="status">
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
            v-hasPermi="['medical:diagnosis:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['medical:diagnosis:remove']"
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

    <!-- 添加或修改诊疗信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="患者ID" prop="patientId">
          <el-input v-model.number="form.patientId" placeholder="请输入患者ID" />
        </el-form-item>
        <el-form-item label="就诊日期" prop="visitDate">
          <el-date-picker clearable
            v-model="form.visitDate"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="请选择就诊日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="科室" prop="department">
          <el-input v-model="form.department" placeholder="请输入科室" />
        </el-form-item>
        <el-form-item label="主治医生" prop="doctorName">
          <el-input v-model="form.doctorName" placeholder="请输入主治医生" />
        </el-form-item>
        <el-form-item label="主诉" prop="chiefComplaint">
          <el-input v-model="form.chiefComplaint" type="textarea" placeholder="请输入主诉" />
        </el-form-item>
        <el-form-item>
          <el-button size="small" type="warning" icon="el-icon-magic-stick"
            :loading="aiGenerating" :disabled="!form.patientId"
            @click="handleAiGenerate">AI 辅助写病历</el-button>
          <span class="ai-tip" v-if="!form.patientId">（先填写患者ID以关联档案）</span>
        </el-form-item>
        <el-form-item label="现病史" prop="presentIllness">
          <el-input v-model="form.presentIllness" type="textarea" placeholder="可由「AI 辅助写病历」自动生成" />
        </el-form-item>
        <el-form-item label="诊断结果" prop="diagnosisResult">
          <el-input v-model="form.diagnosisResult" type="textarea" placeholder="请输入诊断结果" />
        </el-form-item>
        <el-form-item label="治疗方案" prop="treatmentPlan">
          <el-input v-model="form.treatmentPlan" type="textarea" placeholder="请输入治疗方案" />
        </el-form-item>
        <el-form-item label="诊疗费用" prop="cost">
          <el-input v-model="form.cost" placeholder="请输入诊疗费用" />
        </el-form-item>
        <el-form-item label="诊疗状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择诊疗状态">
            <el-option label="待就诊" value="0" />
            <el-option label="就诊中" value="1" />
            <el-option label="已完成" value="2" />
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
  </div>
</template>

<script>
import { listDiagnosis, getDiagnosis, delDiagnosis, addDiagnosis, updateDiagnosis } from "@/api/medical/diagnosis";
import { generateRecord } from "@/api/medical/ai";
import { getPatient } from "@/api/medical/patient";

export default {
  name: "MedicalDiagnosis",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      diagnosisList: [],
      title: "",
      open: false,
      aiGenerating: false,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        patientName: null,
        department: null,
        doctorName: null,
        status: null
      },
      form: {},
      rules: {
        patientId: [
          { required: true, message: "患者ID不能为空", trigger: "blur" }
        ],
        visitDate: [
          { required: true, message: "就诊日期不能为空", trigger: "blur" }
        ],
        department: [
          { required: true, message: "科室不能为空", trigger: "blur" }
        ],
        doctorName: [
          { required: true, message: "主治医生不能为空", trigger: "blur" }
        ],
        diagnosisResult: [
          { required: true, message: "诊断结果不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    formatStatus(status) {
      const map = { '0': '待就诊', '1': '就诊中', '2': '已完成' };
      return map[status] || status;
    },
    getList() {
      this.loading = true;
      const params = this.addDateRange(this.queryParams, this.dateRange, 'visitDate');
      listDiagnosis(params).then(response => {
        this.diagnosisList = response.rows;
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
        diagnosisId: null,
        patientId: null,
        visitDate: null,
        department: null,
        doctorName: null,
        chiefComplaint: null,
        presentIllness: null,
        diagnosisResult: null,
        treatmentPlan: null,
        cost: null,
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
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.diagnosisId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加诊疗信息";
    },
    handleUpdate(row) {
      this.reset();
      const diagnosisId = row.diagnosisId || this.ids;
      getDiagnosis(diagnosisId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改诊疗信息";
      });
    },
    /** 联动点①：接诊页嵌入 AI 生成病历，并回填现病史/诊断结果（V1.0 由医生确认后采纳） */
    handleAiGenerate() {
      if (!this.form.patientId) {
        this.$modal.msgWarning("请先填写患者ID");
        return;
      }
      this.aiGenerating = true;
      const fill = (profile) => {
        const payload = {
          patientId: this.form.patientId,
          patientName: profile ? profile.patientName : (this.form.patientName || ''),
          age: profile ? this.calcAge(profile.birthDate) : 0,
          gender: profile ? (profile.gender === '1' ? '女' : '男') : '男',
          chiefComplaint: this.form.chiefComplaint || '',
          allergyHistory: profile ? (profile.allergyHistory || '') : '',
          pastHistory: profile ? (profile.pastHistory || '') : ''
        };
        generateRecord(payload).then(res => {
          if (res && res.content) {
            this.form.presentIllness = this.parseSection(res.content, '现病史') || this.form.presentIllness;
            this.form.diagnosisResult = this.parseSection(res.content, '初步诊断') || this.form.diagnosisResult;
            this.$modal.msgSuccess(res.degraded ? '已生成模板草稿（大模型不可用）' : 'AI 病历已生成并回填');
          }
          this.aiGenerating = false;
        }).catch(() => { this.aiGenerating = false; });
      };
      getPatient(this.form.patientId).then(r => fill(r)).catch(() => fill(null));
    },
    parseSection(content, title) {
      if (!content) return '';
      const re = new RegExp('##\\s*' + title + '\\s*\\n([\\s\\S]*?)(?=\\n##\\s*|$)');
      const m = content.match(re);
      return m ? m[1].trim() : '';
    },
    calcAge(birthDate) {
      if (!birthDate) return 0;
      const b = new Date(birthDate);
      if (isNaN(b.getTime())) return 0;
      const now = new Date();
      let age = now.getFullYear() - b.getFullYear();
      const m = now.getMonth() - b.getMonth();
      if (m < 0 || (m === 0 && now.getDate() < b.getDate())) age--;
      return age;
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.diagnosisId != null) {
            updateDiagnosis(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addDiagnosis(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const diagnosisIds = row.diagnosisId || this.ids;
      this.$modal.confirm('是否确认删除诊疗信息编号为"' + diagnosisIds + '"的数据项？').then(() => {
        return delDiagnosis(diagnosisIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      const params = this.addDateRange(this.queryParams, this.dateRange, 'visitDate');
      this.download('medical/diagnosis/export', {
        ...params
      }, `diagnosis_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
