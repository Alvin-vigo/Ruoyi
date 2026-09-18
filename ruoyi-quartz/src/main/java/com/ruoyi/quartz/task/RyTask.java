package com.ruoyi.quartz.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 定时任务调度测试（示例任务）
 *
 * @author ruoyi
 */
@Component("ryTask")
public class RyTask {
    private static final Logger log = LoggerFactory.getLogger(RyTask.class);

    /**
     * 无参数方法
     */
    public void ryNoParams() {
        log.info("ryTask.ryNoParams 方法执行，无参数");
    }

    /**
     * 带参方法（参数以字符串传入）
     *
     * @param params 调用目标字符串中的参数
     */
    public void ryParams(String params) {
        log.info("ryTask.ryParams 方法执行，参数：{}", params);
    }
}
