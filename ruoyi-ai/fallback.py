# -*- coding: utf-8 -*-
"""
ruoyi-ai / 降级兜底（结构化模板病历）

大模型不可用时（Ollama 未启动 / 尚未 pull 模型 / 生成超时），接口不能返回 500。
这里产出一份**规则驱动的结构化模板病历草稿**，按主诉关键词匹配 5 套预设模板之一：
  1) 呼吸道感染  2) 消化道不适  3) 高血压  4) 糖尿病  5) 普通外伤
匹配不到则回退到通用模板。每套模板固定输出四段结构：
  现病史 / 体格检查 / 初步诊断 / 医嘱建议
并自动填充患者姓名、年龄、性别，以及从患者档案带入的过敏史、既往病史。

注意：这只是占位草稿，绝不是"AI 生成病历"的成果，答辩时不要拿它充当实验数据。
"""
from typing import Dict, List, Optional

from schemas import MedicalRecordReq

# 5 套预设模板：关键词 -> （体格检查要点, 初步诊断提示, 医嘱建议要点）
_TEMPLATES: List[Dict] = [
    {
        "key": "呼吸道感染",
        "keywords": ["发热", "发烧", "咳嗽", "咳痰", "咽痛", "流涕", "鼻塞", "感冒", "气管", "哮喘", "呼吸"],
        "exam": "咽轻度充血，扁桃体无肿大；双肺呼吸音粗，未闻及明显干湿啰音；心率、心律正常。",
        "diagnosis": "急性上呼吸道感染（待查，需结合血象与胸片排除下呼吸道感染）",
        "advice": "1) 注意休息、多饮水；2) 对症退热、止咳；3) 查血常规、C反应蛋白，必要时胸片；4) 症状加重或持续发热及时复诊。",
    },
    {
        "key": "消化道不适",
        "keywords": ["腹痛", "腹泻", "恶心", "呕吐", "腹胀", "食欲不振", "反酸", "便血", "肠胃", "胃肠"],
        "exam": "腹平软，无明显压痛及反跳痛，肠鸣音正常；肝脾肋下未触及。",
        "diagnosis": "急性胃肠炎（待查，需结合便常规、腹部超声排除急腹症）",
        "advice": "1) 清淡易消化饮食，避免油腻生冷；2) 补液、纠正电解质紊乱；3) 粪便常规+隐血，必要时腹部超声；4) 持续腹痛或便血立即就诊。",
    },
    {
        "key": "高血压",
        "keywords": ["血压", "头晕", "头痛", "高血压", "眩晕", "心悸"],
        "exam": "神清，血压偏高（需现场复测）；心率正常，心界无扩大；双下肢无水肿。",
        "diagnosis": "原发性高血压（待分级，需非同日三次复核）",
        "advice": "1) 低盐低脂饮食、规律运动、控制体重；2) 每日监测血压并记录；3) 完善血脂、血糖、肾功能、心电图；4) 必要时启动或调整降压方案。",
    },
    {
        "key": "糖尿病",
        "keywords": ["血糖", "多饮", "多尿", "消瘦", "糖尿病", "口渴", "乏力"],
        "exam": "神清，体型偏瘦/正常；皮肤无感染灶；足底感觉待查；心率正常。",
        "diagnosis": "糖尿病（待分型，需结合空腹血糖、糖化血红蛋白与 OGTT）",
        "advice": "1) 糖尿病饮食、规律运动；2) 监测空腹及餐后血糖；3) 完善糖化血红蛋白、OGTT、尿微量白蛋白；4) 内分泌科随诊，必要时降糖治疗。",
    },
    {
        "key": "普通外伤",
        "keywords": ["外伤", "扭伤", "擦伤", "骨折", "跌打", "伤口", "崴", "碰伤", "撞", "挫伤"],
        "exam": "局部肿胀、压痛，活动受限；肢体远端血运、感觉正常；无畸形（骨折待排）。",
        "diagnosis": "软组织挫伤 / 扭伤（待查，需 X 线排除骨折）",
        "advice": "1) 受伤 24-48h 内冷敷、制动、抬高患肢；2) 外用活血化瘀药物；3) 患处 X 线排除骨折；4) 疼痛加重或活动受限及时复诊。",
    },
]


def _match_template(chief_complaint: str) -> Optional[Dict]:
    cc = chief_complaint or ""
    for tpl in _TEMPLATES:
        if any(k in cc for k in tpl["keywords"]):
            return tpl
    return None


def _history_block(req: MedicalRecordReq) -> str:
    parts = []
    if req.allergy_history:
        parts.append(f"过敏史：{req.allergy_history}。")
    if req.past_history:
        parts.append(f"既往病史：{req.past_history}。")
    return "".join(parts)


def build_fallback_record(req: MedicalRecordReq, reason: str) -> Dict[str, object]:
    """生成结构化模板病历草稿（大模型不可用时的降级输出）"""
    tpl = _match_template(req.chief_complaint)
    if tpl:
        exam = tpl["exam"]
        diagnosis = tpl["diagnosis"]
        advice = tpl["advice"]
        template_name = tpl["key"]
    else:
        exam = "生命体征平稳，心、肺、腹查体未见明显异常（具体体征需医师面诊补充）。"
        diagnosis = "待查，需进一步采集病史与辅助检查"
        advice = "1) 根据初步判断选择相应实验室与影像学检查；2) 注意休息、清淡饮食；3) 症状不缓解及时复诊。"
        template_name = "通用"

    history = _history_block(req)
    if history:
        present_illness = (
            f"患者{req.gender}，{req.age}岁，因「{req.chief_complaint}」就诊。"
            f"{history}起病时间与加重缓解因素待进一步问诊补充。"
        )
    else:
        present_illness = (
            f"患者{req.gender}，{req.age}岁，因「{req.chief_complaint}」就诊。"
            f"症状性质与持续时间待补充。"
        )

    sections = {
        "present_illness": present_illness,
        "physical_exam": exam,
        "preliminary_diagnosis": diagnosis,
        "advice": advice,
    }

    content = (
        "## 现病史\n{present}\n\n"
        "## 体格检查\n{exam}\n\n"
        "## 初步诊断\n{diag}\n\n"
        "## 医嘱建议\n{advice}\n\n"
        "## 医嘱备注\n{notes}"
    ).format(
        present=sections["present_illness"],
        exam=sections["physical_exam"],
        diag=sections["preliminary_diagnosis"],
        advice=sections["advice"],
        notes=(
            "本条为系统生成的病历骨架草稿（匹配模板：" + template_name + "），尚未由大模型润色。"
            "请接诊医生根据实际问诊与查体结果补充修改后保存；生成内容须经医生确认方可作为正式病历。"
        ),
    )

    return {
        "content": content,
        "sections": sections,
        "template": template_name,
        "degraded": True,
        "engine": "template",
        "note": f"大模型不可用（{reason}），当前输出为结构化模板草稿（{template_name}），非 AI 生成内容。",
    }
