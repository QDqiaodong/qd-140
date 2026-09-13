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
