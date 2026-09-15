-- 存量库升级：新增训练课次（排课）表（init.sql 仅在全新数据库时执行）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS training_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_date DATE NOT NULL COMMENT '训练日期',
    bracket_id BIGINT NOT NULL COMMENT '停靠支架ID',
    group_id BIGINT NULL COMMENT '训练组别ID（可空）',
    expected_person_count INT NOT NULL COMMENT '预计上艇人数（必填，不得超过支架当前承重）',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_session_date (session_date),
    INDEX idx_bracket_id (bracket_id),
    INDEX idx_group_id (group_id),
    CONSTRAINT fk_session_bracket FOREIGN KEY (bracket_id) REFERENCES docking_bracket(id),
    CONSTRAINT fk_session_group FOREIGN KEY (group_id) REFERENCES rowing_group(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练课次（排课）表';
