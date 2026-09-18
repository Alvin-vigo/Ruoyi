package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.AllergyChecker;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ruoyi.system.domain.MedicalAuditLog;
import com.ruoyi.system.domain.MedicalDispensing;
import com.ruoyi.system.domain.MedicalDrug;
import com.ruoyi.system.domain.MedicalPatient;
import com.ruoyi.system.domain.MedicalPrescription;
import com.ruoyi.system.domain.MedicalPrescriptionItem;
import com.ruoyi.system.domain.MedicalPriceItem;
import com.ruoyi.system.mapper.MedicalAuditLogMapper;
import com.ruoyi.system.mapper.MedicalDispensingMapper;
import com.ruoyi.system.mapper.MedicalDrugMapper;
import com.ruoyi.system.mapper.MedicalPrescriptionItemMapper;
import com.ruoyi.system.mapper.MedicalPrescriptionMapper;
import com.ruoyi.system.mapper.MedicalPriceItemMapper;
import com.ruoyi.system.mapper.MedicalScheduleMapper;
import com.ruoyi.system.service.IMedicalPatientService;
import com.ruoyi.system.service.IMedicalPrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处方信息Service业务层处理（含明细、发药、库存扣减）
 *
 * @author ruoyi
 */
@Service
public class MedicalPrescriptionServiceImpl implements IMedicalPrescriptionService {

    @Autowired
    private MedicalPrescriptionMapper prescriptionMapper;

    @Autowired
    private MedicalPrescriptionItemMapper itemMapper;

    @Autowired
    private MedicalDispensingMapper dispensingMapper;

    @Autowired
    private MedicalDrugMapper drugMapper;

    @Autowired
    private MedicalScheduleMapper scheduleMapper;

    @Autowired
    private IMedicalPatientService patientService;

    @Autowired
    private MedicalPriceItemMapper priceItemMapper;

    @Autowired
    private MedicalAuditLogMapper auditLogMapper;

    /**
     * 发药防重复提交锁：key=处方ID，value=最近一次发药开始时间戳。
     * 同一处方 5 秒内重复调用直接拒绝，规避前端连点 / 并发请求导致的重复扣库存。
     * 数据侧 dispensedQuantity 已防止"超发"，此处是接口层并发安全兜底。
     */
    private final Map<String, Long> dispensingLocks = new ConcurrentHashMap<>();

    /**
     * 查询处方信息
     *
     * @param prescriptionId 处方ID
     * @return 处方信息
     */
    @Override
    public MedicalPrescription selectMedicalPrescriptionById(Long prescriptionId) {
        return prescriptionMapper.selectMedicalPrescriptionById(prescriptionId);
    }

    /**
     * 查询处方信息列表
     *
     * @param medicalPrescription 处方信息
     * @return 处方信息集合
     */
    @Override
    public List<MedicalPrescription> selectMedicalPrescriptionList(MedicalPrescription medicalPrescription) {
        return prescriptionMapper.selectMedicalPrescriptionList(medicalPrescription);
    }

    /**
     * 新增处方信息（含明细与过敏校验，事务）
     *
     * @param pres 处方
     * @param items 处方明细
     * @return 结果
     */
    @Override
    @Transactional
    public int addPrescription(MedicalPrescription pres, List<MedicalPrescriptionItem> items) {
        if (pres.getPatientId() != null) {
            MedicalPatient patient = patientService.selectMedicalPatientById(pres.getPatientId());
            if (patient != null) {
                if (pres.getPatientAllergy() == null) {
                    pres.setPatientAllergy(patient.getAllergyHistory());
                }
                // 患者姓名一并补全：列表与审计日志都要显示，前端只传 patientId
                if (pres.getPatientName() == null || pres.getPatientName().isEmpty()) {
                    pres.setPatientName(patient.getPatientName());
                }
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String datePart = sdf.format(new Date());
        String randomPart = String.format("%04d", new Random().nextInt(9000) + 1000);
        pres.setPrescriptionNo("CF" + datePart + randomPart);
        pres.setPrescriptionStatus("0");

        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (MedicalPrescriptionItem item : items) {
                // P1-4 处方按价目表自动计价：以药品名称匹配启用中的药品价目，覆盖手工单价
                MedicalPriceItem priceItem = priceItemMapper.selectPriceByDrugName(item.getDrugName());
                if (priceItem != null && priceItem.getPrice() != null) {
                    item.setPrice(priceItem.getPrice());
                }
                int qty = item.getQuantity() == null ? 0 : item.getQuantity();
                BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
                total = total.add(BigDecimal.valueOf(qty).multiply(price));
            }
        }
        pres.setTotalAmount(total);

        int rows = prescriptionMapper.insertMedicalPrescription(pres);

        if (items != null) {
            for (MedicalPrescriptionItem item : items) {
                item.setPrescriptionId(pres.getPrescriptionId());
                item.setAllergyWarning(AllergyChecker.check(pres.getPatientAllergy(), item.getDrugName(), null));
                itemMapper.insertMedicalPrescriptionItem(item);
            }
        }
        // P1-6 关键操作审计：开方留痕
        audit("处方", "开方", pres.getPrescriptionId(), pres.getPrescriptionNo(),
                "患者：" + (pres.getPatientName() == null ? "" : pres.getPatientName()) + "，明细 " + (items == null ? 0 : items.size()) + " 项");
        return rows;
    }

    /**
     * 修改处方信息
     *
     * @param medicalPrescription 处方信息
     * @return 结果
     */
    @Override
    public int updateMedicalPrescription(MedicalPrescription medicalPrescription) {
        return prescriptionMapper.updateMedicalPrescription(medicalPrescription);
    }

    /**
     * 删除处方信息
     *
     * @param prescriptionId 处方ID
     * @return 结果
     */
    @Override
    public int deleteMedicalPrescriptionById(Long prescriptionId) {
        return prescriptionMapper.deleteMedicalPrescriptionById(prescriptionId);
    }

    /**
     * 批量删除处方信息
     *
     * @param prescriptionIds 需要删除的处方ID数组
     * @return 结果
     */
    @Override
    public int deleteMedicalPrescriptionByIds(Long[] prescriptionIds) {
        return prescriptionMapper.deleteMedicalPrescriptionByIds(prescriptionIds);
    }

    /**
     * 查询处方下的明细列表
     *
     * @param prescriptionId 处方ID
     * @return 明细集合
     */
    @Override
    public List<MedicalPrescriptionItem> selectItems(Long prescriptionId) {
        return itemMapper.selectByPrescriptionId(prescriptionId);
    }

    /**
     * 发药（含库存扣减与明细更新，事务）
     *
     * @param prescriptionId 处方ID
     * @param dispenseItems 本次发药明细（含 itemId 与 quantity）
     * @param operator 操作人
     */
    @Override
    @Transactional
    public void dispense(Long prescriptionId, List<MedicalPrescriptionItem> dispenseItems, String operator) {
        if (dispenseItems == null || dispenseItems.isEmpty()) {
            return;
        }
        // 接口层防重复提交：同一处方 5 秒内重复发药直接拒绝
        String lockKey = String.valueOf(prescriptionId);
        long now = System.currentTimeMillis();
        Long last = dispensingLocks.get(lockKey);
        if (last != null && now - last < 5000) {
            throw new ServiceException("操作过于频繁，请勿重复点击发药，稍后重试");
        }
        dispensingLocks.put(lockKey, now);
        for (MedicalPrescriptionItem dispenseItem : dispenseItems) {
            Long itemId = dispenseItem.getItemId();
            int qty = dispenseItem.getQuantity() == null ? 0 : dispenseItem.getQuantity();
            MedicalPrescriptionItem dbItem = itemMapper.selectMedicalPrescriptionItemById(itemId);
            if (dbItem == null) {
                throw new ServiceException("处方明细不存在：itemId=" + itemId);
            }
            int ordered = dbItem.getQuantity() == null ? 0 : dbItem.getQuantity();
            int dispensed = dbItem.getDispensedQuantity() == null ? 0 : dbItem.getDispensedQuantity();
            if (qty > ordered - dispensed) {
                throw new ServiceException("发药数量超过开方数量");
            }
            // P0-1 发药效期校验：过期药品禁止发药（医疗安全红线）
            MedicalDrug drug = drugMapper.selectMedicalDrugById(dbItem.getDrugId());
            if (drug != null && drug.getExpiryDate() != null && drug.getExpiryDate().before(new Date())) {
                throw new ServiceException("药品「" + dbItem.getDrugName() + "」已过期（有效期至 "
                        + new SimpleDateFormat("yyyy-MM-dd").format(drug.getExpiryDate()) + "），禁止发药");
            }
            int stockRows = drugMapper.decrementStock(dbItem.getDrugId(), qty);
            if (stockRows == 0) {
                throw new ServiceException("药品" + dbItem.getDrugName() + "库存不足");
            }
            dbItem.setDispensedQuantity(dispensed + qty);
            itemMapper.updateDispensed(dbItem);

            MedicalDispensing dispensing = new MedicalDispensing();
            dispensing.setPrescriptionId(prescriptionId);
            dispensing.setItemId(itemId);
            dispensing.setDrugId(dbItem.getDrugId());
            dispensing.setDrugName(dbItem.getDrugName());
            dispensing.setQuantity(qty);
            dispensing.setOperator(operator);
            dispensing.setDispensingTime(new Date());
            dispensingMapper.insertMedicalDispensing(dispensing);
        }

        List<MedicalPrescriptionItem> allItems = itemMapper.selectByPrescriptionId(prescriptionId);
        boolean allDone = true;
        boolean anyDispensed = false;
        for (MedicalPrescriptionItem it : allItems) {
            int ordered = it.getQuantity() == null ? 0 : it.getQuantity();
            int dispensed = it.getDispensedQuantity() == null ? 0 : it.getDispensedQuantity();
            if (dispensed > 0) {
                anyDispensed = true;
            }
            if (dispensed < ordered) {
                allDone = false;
            }
        }
        String status;
        if (allDone) {
            status = "2";
        } else if (anyDispensed) {
            status = "1";
        } else {
            status = "0";
        }
        prescriptionMapper.updatePrescriptionStatus(prescriptionId, status);
        // P1-6 关键操作审计：发药留痕（带上处方号，便于审计页按单号检索）
        audit("发药", "发药", prescriptionId, prescriptionNo(prescriptionId),
                "操作人：" + operator + "，本次发药 " + dispenseItems.size() + " 项");
    }

    /**
     * 退药（逆向流程）
     * 原子回滚库存、置对应发药记录为已退、回退处方明细已发数量、处方状态恢复待发药；
     * 已退的记录不会被重复回滚，保证幂等。
     *
     * @param prescriptionId 处方ID
     * @param operator 操作人
     */
    @Override
    @Transactional
    public void returnDispensing(Long prescriptionId, String operator) {
        List<MedicalDispensing> records = dispensingMapper.selectByPrescriptionId(prescriptionId);
        List<MedicalDispensing> active = new ArrayList<>();
        for (MedicalDispensing d : records) {
            if (d.getStatus() == null || "0".equals(d.getStatus())) {
                active.add(d);
            }
        }
        if (active.isEmpty()) {
            throw new ServiceException("该处方无已发药记录，无法退药");
        }
        for (MedicalDispensing d : active) {
            // 原子回加库存
            int rows = drugMapper.incrementStock(d.getDrugId(), d.getQuantity());
            if (rows == 0) {
                throw new ServiceException("药品「" + d.getDrugName() + "」库存回滚失败");
            }
            // 置发药记录为已退
            d.setStatus("1");
            dispensingMapper.updateMedicalDispensing(d);
            // 回退处方明细已发数量
            MedicalPrescriptionItem item = itemMapper.selectMedicalPrescriptionItemById(d.getItemId());
            if (item != null) {
                int dispensed = item.getDispensedQuantity() == null ? 0 : item.getDispensedQuantity();
                int back = d.getQuantity() == null ? 0 : d.getQuantity();
                item.setDispensedQuantity(Math.max(0, dispensed - back));
                itemMapper.updateDispensed(item);
            }
        }
        prescriptionMapper.updatePrescriptionStatus(prescriptionId, "0");
        // P1-6 关键操作审计：退药留痕
        audit("退药", "退药", prescriptionId, prescriptionNo(prescriptionId),
                "操作人：" + (operator == null ? currentUser() : operator) + "，回退发药记录 " + active.size() + " 条，库存已回滚");
    }

    /**
     * 作废处方
     *
     * @param prescriptionId 处方ID
     */
    @Override
    @Transactional
    public void cancel(Long prescriptionId) {
        prescriptionMapper.updatePrescriptionStatus(prescriptionId, "3");
    }

    /**
     * 写入操作审计日志。审计失败不影响主业务流程。
     */
    private void audit(String module, String action, Long bizId, String bizNo, String detail) {
        try {
            MedicalAuditLog log = new MedicalAuditLog();
            String user = "system";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                user = auth.getName();
            }
            log.setModule(module);
            log.setAction(action);
            log.setBizId(bizId);
            log.setBizNo(bizNo);
            log.setOperator(user);
            log.setDetail(detail);
            log.setCreateBy(user);
            auditLogMapper.insertMedicalAuditLog(log);
        } catch (Exception e) {
            // 审计不影响主流程
        }
    }

    /** 取当前登录用户名，取不到时给个兜底值 */
    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isEmpty()) {
            return auth.getName();
        }
        return "system";
    }

    /** 查处方单号，用于审计留痕 */
    private String prescriptionNo(Long prescriptionId) {
        try {
            MedicalPrescription pres = prescriptionMapper.selectMedicalPrescriptionById(prescriptionId);
            return pres == null ? null : pres.getPrescriptionNo();
        } catch (Exception e) {
            return null;
        }
    }
}
