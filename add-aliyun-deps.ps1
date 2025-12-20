# PowerShell脚本：添加阿里云依赖到pom.xml

param(
    [string] = "pom.xml"
)

Write-Host "正在更新pom.xml依赖..." -ForegroundColor Cyan

# 备份原文件
Copy-Item  ".backup.20251217133600" -Force
Write-Host "✅ 已备份原文件" -ForegroundColor Green

# 读取pom.xml内容
 = Get-Content  -Raw

# 要添加的依赖
\ = @'
    <!-- ===================== -->
    <!-- 阿里云图像识别依赖 -->
    <!-- ===================== -->
    <!-- 阿里云SDK核心 -->
    <dependency>
        <groupId>com.aliyun</groupId>
        <artifactId>aliyun-java-sdk-core</artifactId>
        <version>4.6.3</version>
    </dependency>
    
    <!-- 阿里云内容安全/图像识别SDK -->
    <dependency>
        <groupId>com.aliyun</groupId>
        <artifactId>aliyun-java-sdk-green</artifactId>
        <version>3.6.6</version>
    </dependency>
    
    <!-- 新版视觉智能平台SDK -->
    <dependency>
        <groupId>com.aliyun</groupId>
        <artifactId>green20220302</artifactId>
        <version>1.0.2</version>
    </dependency>
    
    <!-- JSON处理 -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
        <version>2.0.34</version>
    </dependency>
    
    <!-- 文件处理工具 -->
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.11.0</version>
    </dependency>
'@

# 查找<dependencies>标签位置
if (\ -match '<dependencies>') {
    # 在<dependencies>后面添加
    \ = \ -replace '<dependencies>', "<dependencies>
$dependenciesToAdd"
    Set-Content -Path \ -Value \ -Encoding UTF8
    Write-Host "✅ 成功添加阿里云依赖" -ForegroundColor Green
} else {
    Write-Host "❌ 未找到<dependencies>标签" -ForegroundColor Red
}

# 验证添加结果
Write-Host "
验证依赖添加结果：" -ForegroundColor Cyan
foreach (\ in @("aliyun-java-sdk-core", "fastjson")) {
    if ((Get-Content \ -Raw) -match \) {
        Write-Host "  ✓ \ 添加成功" -ForegroundColor Green
    } else {
        Write-Host "  ✗ \ 添加失败" -ForegroundColor Red
    }
}

Write-Host "
🎯 下一步操作：" -ForegroundColor Yellow
Write-Host "1. 手动检查pom.xml，确保依赖位置正确"
Write-Host "2. 运行: mvn clean compile"
Write-Host "3. 运行: mvn spring-boot:run"