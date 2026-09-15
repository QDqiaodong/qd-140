-- 存量库升级：为训练课次表增加课次状态字段（init.sql 仅在全新数据库时执行）
-- 支架停用保存成功后，该支架上上课日还没到（含今天）的有效课次会被标为 0（因支架停用失效），
-- 重新启用支架不会自动恢复；已过上课日的课保持 1，留着当时的记录。
-- MySQL 8.0.29+ 支持 ADD COLUMN IF NOT EXISTS；低版本请手动执行去掉 IF NOT EXISTS 的语句。
SET NAMES utf8mb4;

ALTER TABLE training_session
    ADD COLUMN IF NOT EXISTS status INT DEFAULT 1 COMMENT '课次状态：1-有效，0-因支架停用失效（须场务重排，不随支架重新启用自动恢复）' AFTER expected_person_count;
