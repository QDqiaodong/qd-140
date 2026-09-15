SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS docking_bracket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bracket_code VARCHAR(50) NOT NULL UNIQUE COMMENT '支架编号',
    load_capacity DECIMAL(10,2) NOT NULL COMMENT '承重(kg)',
    min_distance INT NOT NULL COMMENT '适配最小竞速距离(m)',
    max_distance INT NOT NULL COMMENT '适配最大竞速距离(m)',
    status INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_distance_range (min_distance, max_distance),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='停靠支架表';

CREATE TABLE IF NOT EXISTS rowing_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL COMMENT '组别名称',
    group_code VARCHAR(50) NOT NULL UNIQUE COMMENT '组别编码',
    racing_distance INT NOT NULL COMMENT '常规竞速距离(m)',
    description VARCHAR(255) COMMENT '描述',
    plan_file_name VARCHAR(255) COMMENT '训练计划附件原始文件名',
    plan_file_path VARCHAR(500) COMMENT '训练计划附件存储路径',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_racing_distance (racing_distance)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='赛艇组别表';

CREATE TABLE IF NOT EXISTS bracket_binding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bracket_id BIGINT NOT NULL COMMENT '支架ID',
    group_id BIGINT NOT NULL COMMENT '组别ID',
    binding_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    status INT DEFAULT 1 COMMENT '状态：1-生效，0-失效',
    UNIQUE KEY uk_bracket_group (bracket_id, group_id),
    INDEX idx_bracket_id (bracket_id),
    INDEX idx_group_id (group_id),
    INDEX idx_status (status),
    CONSTRAINT fk_binding_bracket FOREIGN KEY (bracket_id) REFERENCES docking_bracket(id),
    CONSTRAINT fk_binding_group FOREIGN KEY (group_id) REFERENCES rowing_group(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支架绑定关系表';

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

CREATE TABLE IF NOT EXISTS binding_change_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bracket_id BIGINT NOT NULL COMMENT '支架ID',
    bracket_code VARCHAR(50) COMMENT '支架编号',
    group_id BIGINT COMMENT '组别ID',
    group_name VARCHAR(100) COMMENT '组别名称',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型：BIND-绑定，UNBIND-解绑，UPDATE-更新',
    previous_distance INT COMMENT '变更前竞速距离(m)',
    new_distance INT COMMENT '变更后竞速距离(m)',
    change_reason VARCHAR(255) COMMENT '变更原因',
    operator VARCHAR(50) COMMENT '操作人',
    changed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    INDEX idx_bracket_id (bracket_id),
    INDEX idx_group_id (group_id),
    INDEX idx_change_type (change_type),
    INDEX idx_changed_at (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绑定变更记录表';

INSERT INTO docking_bracket (bracket_code, load_capacity, min_distance, max_distance, status, remark) VALUES
('BK-001', 150.00, 500, 2000, 1, '单人艇停靠支架'),
('BK-002', 200.00, 500, 2000, 1, '双人艇停靠支架'),
('BK-003', 300.00, 1000, 5000, 1, '四人艇停靠支架'),
('BK-004', 400.00, 1000, 5000, 1, '八人艇停靠支架'),
('BK-005', 120.00, 500, 1500, 1, '轻量级单人艇支架'),
('BK-006', 180.00, 500, 1500, 1, '轻量级双人艇支架'),
('BK-007', 280.00, 2000, 5000, 1, '重量级四人艇支架'),
('BK-008', 380.00, 2000, 5000, 1, '重量级八人艇支架'),
('BK-009', 160.00, 500, 2000, 1, '女子单人艇支架'),
('BK-010', 220.00, 1000, 3000, 1, '混合双人艇支架');

INSERT INTO rowing_group (group_name, group_code, racing_distance, description) VALUES
('男子单人双桨', 'M1x', 2000, '男子单人双桨组'),
('女子单人双桨', 'W1x', 2000, '女子单人双桨组'),
('男子双人双桨', 'M2x', 2000, '男子双人双桨组'),
('女子双人双桨', 'W2x', 2000, '女子双人双桨组'),
('男子四人双桨', 'M4x', 2000, '男子四人双桨组'),
('女子四人双桨', 'W4x', 2000, '女子四人双桨组'),
('男子八人单桨', 'M8+', 2000, '男子八人单桨组'),
('女子八人单桨', 'W8+', 2000, '女子八人单桨组'),
('男子轻量级单人双桨', 'LM1x', 1500, '男子轻量级单人双桨组'),
('女子轻量级双人双桨', 'LW2x', 1500, '女子轻量级双人双桨组'),
('男子1000米组', 'M-1000', 1000, '男子1000米竞速组'),
('女子500米组', 'W-500', 500, '女子500米竞速组');

INSERT INTO bracket_binding (bracket_id, group_id, binding_time, status) VALUES
(1, 1, NOW(), 1),
(2, 3, NOW(), 1),
(3, 5, NOW(), 1),
(4, 7, NOW(), 1),
(5, 9, NOW(), 1),
(6, 10, NOW(), 1),
(7, 5, NOW(), 1),
(8, 7, NOW(), 1),
(9, 2, NOW(), 1),
(10, 4, NOW(), 1);

INSERT INTO binding_change_log (bracket_id, bracket_code, group_id, group_name, change_type, previous_distance, new_distance, change_reason, operator) VALUES
(1, 'BK-001', 1, '男子单人双桨', 'BIND', NULL, 2000, '初始绑定', 'admin'),
(2, 'BK-002', 3, '男子双人双桨', 'BIND', NULL, 2000, '初始绑定', 'admin'),
(3, 'BK-003', 5, '男子四人双桨', 'BIND', NULL, 2000, '初始绑定', 'admin'),
(4, 'BK-004', 7, '男子八人单桨', 'BIND', NULL, 2000, '初始绑定', 'admin');
