-- 存量库升级：为赛艇组别表增加训练计划附件字段（init.sql 仅在全新数据库时执行）
-- MySQL 8.0.29+ 支持 ADD COLUMN IF NOT EXISTS；低版本请手动执行去掉 IF NOT EXISTS 的语句。
SET NAMES utf8mb4;

ALTER TABLE rowing_group
    ADD COLUMN IF NOT EXISTS plan_file_name VARCHAR(255) NULL COMMENT '训练计划附件原始文件名' AFTER description,
    ADD COLUMN IF NOT EXISTS plan_file_path VARCHAR(500) NULL COMMENT '训练计划附件存储路径' AFTER plan_file_name;
