package com.ruoyi.common.utils;

/**
 * 过敏校验工具类
 * 通过最朴素的 String.contains 实现过敏原与药品关键词的匹配，不依赖正则。
 *
 * @author ruoyi
 */
public class AllergyChecker {

    /**
     * 规则：过敏原关键词 -> 命中药品关键词（逗号分隔）
     * 青霉素: 青霉素/阿莫西林/氨苄/头孢
     * 磺胺:   磺胺
     * 头孢:   头孢
     * 阿司匹林: 阿司匹林
     */
    private static final String[][] RULES = {
            {"青霉素", "青霉素,阿莫西林,氨苄,头孢"},
            {"磺胺", "磺胺"},
            {"头孢", "头孢"},
            {"阿司匹林", "阿司匹林"}
    };

    /**
     * 过敏校验
     *
     * @param patientAllergy 患者过敏史文本
     * @param drugName 药品名称
     * @param drugRemark 药品备注（可空）
     * @return 命中时返回提示语，未命中返回 null
     */
    public static String check(String patientAllergy, String drugName, String drugRemark) {
        if (patientAllergy == null || patientAllergy.isEmpty()) {
            return null;
        }
        String drug = (drugName == null ? "" : drugName) + (drugRemark == null ? "" : drugRemark);
        if (drug.isEmpty()) {
            return null;
        }
        for (String[] rule : RULES) {
            String allergen = rule[0];
            if (patientAllergy.contains(allergen)) {
                String[] keywords = rule[1].split(",");
                for (String keyword : keywords) {
                    if (drug.contains(keyword)) {
                        return "患者既往对" + allergen + "过敏，开具" + (drugName == null ? "" : drugName) + "请谨慎";
                    }
                }
            }
        }
        return null;
    }
}
