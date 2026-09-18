# -*- coding: utf-8 -*-
"""
ruoyi-ai / 请求与响应数据结构（Pydantic v2 兼容）
"""
from typing import List, Optional

from pydantic import BaseModel, Field


# ---------------- 模块① 智能病历 ----------------
class MedicalRecordReq(BaseModel):
    patient_name: str = Field(..., description="患者姓名")
    age: int = Field(..., ge=0, le=120, description="年龄")
    gender: str = Field(..., description="性别：男/女")
    chief_complaint: str = Field(..., min_length=1, description="主诉")
    patient_id: Optional[int] = Field(None, description="关联 medical_patient.patient_id，可空")
    allergy_history: Optional[str] = Field("", description="过敏史（从患者档案带入，用于生成时规避风险）")
    past_history: Optional[str] = Field("", description="既往病史（从患者档案带入，个性化生成）")


# ---------------- 模块③ 胸片：删除物理图片 ----------------
class DeleteUploadReq(BaseModel):
    filename: str = Field(..., description="待删除的上传文件名（仅文件名，不含路径，防目录穿越）")


# ---------------- 模块② 风险预测 ----------------
class RiskReq(BaseModel):
    features: List[float] = Field(
        ..., description="特征数组，顺序为 [年龄, 收缩压, 舒张压, BMI, 血糖]"
    )
    patient_id: Optional[int] = Field(None, description="可空")
    disease_name: Optional[str] = Field("糖尿病", description="预测病种，默认糖尿病")


# ---------------- 模块④ RAG 导诊 ----------------
class RagQueryReq(BaseModel):
    question: str = Field(..., min_length=1, description="患者提问")
    top_k: Optional[int] = Field(None, ge=1, le=10, description="检索条数，默认取配置值")
    session_id: Optional[str] = Field(None, description="多轮对话会话ID，留空则单轮无记忆")


class RagAddDocReq(BaseModel):
    docs: List[str] = Field(..., min_length=1, description="待入库的医学知识文本列表")
