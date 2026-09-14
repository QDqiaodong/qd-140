# qd-140 赛艇训练基地水上停靠支架绑定系统

## 项目简介

赛艇训练基地水上停靠支架、训练组别与绑定关系管理系统。项目包含 Vue/Vite 前端、Spring Boot 后端、MySQL 与 Redis，已按依赖层缓存和固定端口交付链路规范整理。

## 访问地址

- 前端地址: [http://localhost:8142](http://localhost:8142)
- 127.0.0.1 地址: [http://127.0.0.1:8142](http://127.0.0.1:8142)
- 后端 API: http://localhost:8150/api

## 端口

- 前端: 8142
- 后端: 8150
- MySQL: 3366
- Redis: 6439

## 编译与启动

```bash
cd backend
mvn compile -q

cd ../frontend
npm ci
npm run build

cd ..
docker compose up -d --build
```

Docker Compose 端口均绑定到 `127.0.0.1`，镜像基础地址通过 `.env` 中的 `DOCKER_REGISTRY` 统一控制。

## 功能说明

训练组别建档时可上传训练计划附件（场务操作）：

- 允许类型：`pdf`、`doc`、`docx`、`xls`、`xlsx`、`ppt`、`pptx`、`txt`，单个文件不超过 20MB；
- 附件类型不在允许范围内时，前后端都会提示“请更换文件”，且不会执行保存组别；
- 已有附件时再次上传会替换旧附件（服务端同步删除旧文件）；
- 附件上传通过后才允许保存/建档组别，附件保存在后端 `uploads/plans` 目录，
  容器部署时通过 `plan_files` 数据卷持久化。

## 存量数据库升级

`init.sql` 只在 MySQL 数据卷首次初始化时执行。已运行过的旧库需手工执行一次：

```bash
docker exec -i qd-140-mysql mysql -uroot -prowing2024 rowing_base \
  < backend/sql/migration_plan_attachment.sql
```
