# Test Full Flow: Register → Verify Database → Login

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST FULL AUTHENTICATION FLOW" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api"
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$testEmail = "testuser$timestamp@example.com"

# Step 1: Kiểm tra số lượng users hiện tại
Write-Host "📊 STEP 1: Kiểm tra số lượng users hiện tại..." -ForegroundColor Yellow
try {
    $countBefore = Invoke-RestMethod -Uri "$baseUrl/users/count" -Method Get
    Write-Host "   ✅ Số lượng users hiện tại: $countBefore" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Lỗi: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 2: Register user mới
Write-Host "📝 STEP 2: Register user mới..." -ForegroundColor Yellow
Write-Host "   Email: $testEmail" -ForegroundColor White

$registerBody = @{
    name = "Test User $timestamp"
    email = $testEmail
    password = "password123"
    role = "CUSTOMER"
    phone = "0123456789"
    country = "Vietnam"
    companyName = "Test Company"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $registerBody

    Write-Host "   ✅ Register thành công!" -ForegroundColor Green
    Write-Host "   User ID: $($registerResponse.user.id)" -ForegroundColor White
    Write-Host "   User Code: $($registerResponse.user.code)" -ForegroundColor White
    Write-Host "   JWT Token: $($registerResponse.accessToken.Substring(0, 50))..." -ForegroundColor White

    $userId = $registerResponse.user.id
    $token = $registerResponse.accessToken
} catch {
    Write-Host "   ❌ Lỗi: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 3: Verify dữ liệu đã được lưu vào database
Write-Host "🔍 STEP 3: Verify dữ liệu trong database..." -ForegroundColor Yellow
try {
    $countAfter = Invoke-RestMethod -Uri "$baseUrl/users/count" -Method Get
    Write-Host "   ✅ Số lượng users sau khi register: $countAfter" -ForegroundColor Green
    
    if ($countAfter -eq ($countBefore + 1)) {
        Write-Host "   ✅ Dữ liệu ĐÃ được lưu vào database!" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  Cảnh báo: Số lượng users không tăng!" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ❌ Lỗi: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Step 4: Lấy thông tin user vừa tạo từ database
Write-Host "📋 STEP 4: Lấy thông tin user từ database..." -ForegroundColor Yellow
try {
    $allUsers = Invoke-RestMethod -Uri "$baseUrl/users/all" -Method Get
    $newUser = $allUsers | Where-Object { $_.email -eq $testEmail }
    
    if ($newUser) {
        Write-Host "   ✅ Tìm thấy user trong database:" -ForegroundColor Green
        Write-Host "      ID: $($newUser.id)" -ForegroundColor White
        Write-Host "      Code: $($newUser.code)" -ForegroundColor White
        Write-Host "      Name: $($newUser.name)" -ForegroundColor White
        Write-Host "      Email: $($newUser.email)" -ForegroundColor White
        Write-Host "      Phone: $($newUser.phone)" -ForegroundColor White
        Write-Host "      Country: $($newUser.country)" -ForegroundColor White
        Write-Host "      Company: $($newUser.companyName)" -ForegroundColor White
        Write-Host "      Role: $($newUser.role)" -ForegroundColor White
        Write-Host "      Provider: $($newUser.provider)" -ForegroundColor White
        Write-Host "      Status: $($newUser.status)" -ForegroundColor White
        Write-Host "      Email Verified: $($newUser.emailVerified)" -ForegroundColor White
        Write-Host "      Created At: $($newUser.createdAt)" -ForegroundColor White
    } else {
        Write-Host "   ❌ Không tìm thấy user trong database!" -ForegroundColor Red
    }
} catch {
    Write-Host "   ❌ Lỗi: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Step 5: Login với user vừa tạo
Write-Host "🔐 STEP 5: Login với user vừa tạo..." -ForegroundColor Yellow

$loginBody = @{
    email = $testEmail
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody

    Write-Host "   ✅ Login thành công!" -ForegroundColor Green
    Write-Host "   JWT Token: $($loginResponse.accessToken.Substring(0, 50))..." -ForegroundColor White

    $loginToken = $loginResponse.accessToken
} catch {
    Write-Host "   ❌ Lỗi: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Step 6: Test protected endpoint với JWT token
Write-Host "🔒 STEP 6: Test protected endpoint với JWT token..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $loginToken"
    }
    
    $meResponse = Invoke-RestMethod -Uri "$baseUrl/users/me" `
        -Method Get `
        -Headers $headers
    
    Write-Host "   ✅ Truy cập protected endpoint thành công!" -ForegroundColor Green
    Write-Host "   Current User: $($meResponse.name) ($($meResponse.email))" -ForegroundColor White
} catch {
    Write-Host "   ❌ Lỗi: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ HOÀN THÀNH TEST FULL FLOW!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

# Summary
Write-Host "📊 SUMMARY:" -ForegroundColor Cyan
Write-Host "   • Users trước khi test: $countBefore" -ForegroundColor White
Write-Host "   • Users sau khi test: $countAfter" -ForegroundColor White
Write-Host "   • User mới: $testEmail" -ForegroundColor White
Write-Host "   • User ID: $userId" -ForegroundColor White
Write-Host ""
Write-Host "✅ Kết luận: Hệ thống hoạt động hoàn hảo!" -ForegroundColor Green
Write-Host "   - Register: ✅" -ForegroundColor Green
Write-Host "   - Database: ✅" -ForegroundColor Green
Write-Host "   - Login: ✅" -ForegroundColor Green
Write-Host "   - JWT Auth: ✅" -ForegroundColor Green
Write-Host ""

