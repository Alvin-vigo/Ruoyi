# -*- coding: utf-8 -*-
"""
ruoyi-ai / Prompt 工程

医学场景对"幻觉"零容忍，所有 prompt 统一加两条硬约束：
1) 只能依据给定信息推断，不得编造检验数值与既往史；
2) 输出必须是结构化文本，便于前端直接渲染与落库。
"""
from typing import List

from schemas import MedicalRecordReq

SYSTEM_RECORD = (
    "你是一名三甲医院内科主治医师，负责撰写规范的门诊电子病历。"
    "必须遵守：1) 仅依据提供的患者信息与主诉进行推断，不得虚构检验结果、既往史、过敏史；"
    "2) 使用中文书面语，术语规范；3) 输出严格按要求的标题分段，不要输出多余解释与客套话。"
)

SYSTEM_RAG = (
    "你是医院智能导诊助手。必须严格依据【参考资料】回答问题。"
    "禁止编造参考资料中不存在的诊断、用药与剂量。"
    "若参考资料不足以回答，必须明确说明并建议线下就医。"
)


def build_record_prompt(req: MedicalRecordReq) -> str:
    """模块①：由患者信息+主诉生成病历初稿"""
    history_lines = []
    if req.allergy_history:
        history_lines.append(f"过敏史：{req.allergy_history}")
    if req.past_history:
        history_lines.append(f"既往病史：{req.past_history}")
    history_block = "\n".join(history_lines) if history_lines else "（无特殊过敏史与既往病史记录）"

    return f"""请根据以下门诊信息撰写电子病历。

【患者信息】
姓名：{req.patient_name}
年龄：{req.age}岁
性别：{req.gender}
主诉：{req.chief_complaint}
{history_block}

【输出格式】严格按以下四段输出，标题原样保留：
## 现病史
（围绕主诉展开，描述症状性质、持续时间、伴随症状、加重缓解因素）

## 体格检查
（记录关键查体发现，无异常则写明"心肺腹查体未见明显异常"）

## 初步诊断
（给出1-3个可能的诊断，按可能性降序，用顿号分隔）

## 医嘱建议
（必要的检查项目与生活干预建议，逐条列出）

【重要约束】只能依据上方提供的患者信息与主诉推断，不得虚构检验结果、查体发现与既往史；生成内容须由接诊医生核对确认后方可作为正式病历。"""


def build_rag_prompt(question: str, context: str) -> str:
    """模块④：基于检索到的知识库原文回答"""
    return f"""【参考资料】
{context}

【患者提问】
{question}

【要求】
1. 优先使用参考资料中的表述回答，不得引入资料外的诊疗结论；
2. 回答控制在200字以内，分点陈述，语言通俗，避免使用过多专业术语；
3. 若参考资料不足以支撑结论，直接说明"建议前往医院就诊"，不要猜测；
4. 回答末尾另起一行输出"参考来源："，列出你实际用到的资料编号或关键词。"""


def join_context(documents: List[str]) -> str:
    """把检索结果拼成带编号的上下文，便于大模型在回答里给出可追溯来源"""
    return "\n\n".join(f"[{i + 1}] {d}" for i, d in enumerate(documents))
