-- 站内信（消息中心）表
-- 用于承载定时任务产生的业务提醒：低库存预警(type=1)、随访逾期提醒(type=2) 等

DROP TABLE IF EXISTS medical_message;
create table medical_message (
  message_id    BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  title         VARCHAR(100) NOT NULL DEFAULT '' COMMENT '消息标题',
  content       VARCHAR(500) NOT NULL DEFAULT '' COMMENT '消息内容',
  type          CHAR(1) NOT NULL DEFAULT '0' COMMENT '消息类型（0系统 1低库存预警 2随访逾期提醒）',
  receiver      VARCHAR(64) NOT NULL DEFAULT 'admin' COMMENT '接收人（系统用户名）',
  read_status   CHAR(1) NOT NULL DEFAULT '0' COMMENT '阅读状态（0未读 1已读）',
  related_id    BIGINT DEFAULT NULL COMMENT '关联业务ID（随访ID/药品ID等）',
  create_time   DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (message_id),
  KEY idx_receiver_read (receiver, read_status),
  KEY idx_type_related (type, related_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment = '站内信消息表';

commit;
