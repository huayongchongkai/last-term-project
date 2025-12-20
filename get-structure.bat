@echo off
chcp 65001 >nul
echo 正在生成项目结构...

echo # 项目结构 > structure.txt
echo. >> structure.txt
echo ## Java文件 >> structure.txt
dir /s /b *.java 2>nul >> structure.txt
echo. >> structure.txt
echo ## 配置文件 >> structure.txt
dir /s /b *.yml *.yaml *.properties 2>nul >> structure.txt
echo. >> structure.txt
echo ## 依赖文件 >> structure.txt
dir /s /b pom.xml build.gradle 2>nul >> structure.txt

echo ✅ 完成！查看 structure.txt
pause