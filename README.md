# downbit视频后端服务

基于 Spring Boot 构建的视频分享和推荐平台。

## 系统要求

运行本系统前,请确保已安装以下环境:

### 必需软件或服务

- JDK 21
- MySQL 8.0
- Redis
- MongoDB
- 腾讯云 COS 对象存储

## 环境配置指南

### 1. 腾讯云 COS 配置

1. 注册腾讯云账号
2. 创建 COS 存储桶
3. 在 `application.yml` 中更新 COS 配置:
4. 详情查看 [腾讯云 COS 文档](https://cloud.tencent.com/document/product/436/7751)

```yaml
downbit:
  cos:
    bucket-name: 你的存储桶名称
    region: 存储桶所在地域
    secret-id: 你的secret_id
    secret-key: 你的secret_key
```

### 2. 其它配置

1. 在 `application.yml` 中将${}替换为自己的配置
