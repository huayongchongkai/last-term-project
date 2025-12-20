@echo off
chcp 65001 > nul
echo ========================================
echo  智能垃圾分类 - 图像识别服务启动脚本
echo ========================================
echo.

REM 设置环境变量
set ALIBABA_ACCESS_KEY=test-access-key-id
set ALIBABA_ACCESS_SECRET=test-access-key-secret

echo 1. 清理项目...
call mvn clean

echo.
echo 2. 编译项目...
call mvn compile

echo.
echo 3. 启动服务...
echo ========================================
echo  服务启动中...
echo  控制台地址: http://localhost:8080
echo  API文档: http://localhost:8080/swagger-ui.html
echo  图片识别API: POST /api/image/recognize
echo ========================================
call mvn spring-boot:run

pause