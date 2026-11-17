package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 业务 ID 分配器实体
 *
 * <p>用途：为「需要复用已删除 ID」的业务提供串行化的 ID 分配点。
 * 以 biz_key 为主键的单行记录，作为 SELECT ... FOR UPDATE 的行锁载体。
 *
 * <p>注意：last_id 仅用于审计与排障，**不参与下一次分配计算**；
 * 分配值恒为「当前表中最大 ID + 1」，以保证删除后可复用被删 ID 的语义（AC-08）。
 *
 * @author ruoyi
 */
public class MedicalIdAllocator implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务标识，如 diagnosis */
    private String bizKey;

    /** 最近一次分配出的 ID（仅审计用） */
    private Long lastId;

    /** 更新时间 */
    private Date updateTime;

    public String getBizKey() {
        return bizKey;
    }

    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }

    public Long getLastId() {
        return lastId;
    }

    public void setLastId(Long lastId) {
        this.lastId = lastId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
