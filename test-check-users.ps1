# Test script để kiểm tra users trong database

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "KIỂM TRA DỮ LIỆU USERS TRONG DATABASE" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1. Đếm số lượng users
Write-Host "1. Đếm số lượng users..." -ForegroundColor Yellow
try {
    $count = Invoke-RestMethod -Uri "http://localhost:8080/api/users/count" -Method Get
    Write-Host "   ✅ Tổng số users: $count" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Lỗi: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 2. Lấy tất cả users
Write-Host "2. Lấy danh sách tất cả users..." -ForegroundColor Yellow
try {
    $users = Invoke-RestMethod -Uri "http://localhost:8080/api/users/all" -Method Get
    
    if ($users.Count -eq 0) {
        Write-Host "   ⚠️  Không có user nào trong database!" -ForegroundColor Yellow
    } else {
        Write-Host "   ✅ Tìm thấy $($users.Count) user(s):" -ForegroundColor Green
        Write-Host ""
        
        foreach ($user in $users) {
            Write-Host "   📌 User #$($user.id)" -ForegroundColor Cyan
            Write-Host "      Code: $($user.code)" -ForegroundColor White
            Write-Host "      Name: $($user.name)" -ForegroundColor White
            Write-Host "      Email: $($user.email)" -ForegroundColor White
            Write-Host "      Role: $($user.role)" -ForegroundColor White
            Write-Host "      Provider: $($user.provider)" -ForegroundColor White
            Write-Host "      Status: $($user.status)" -ForegroundColor White
            Write-Host "      Created: $($user.createdAt)" -ForegroundColor White
            Write-Host ""
        }
    }
} catch {
    Write-Host "   ❌ Lỗi: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "========================================`n" -ForegroundColor Cyan

