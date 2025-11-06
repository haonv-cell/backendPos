# Test Register API with different email
$body = @{
    name = "John Doe"
    email = "john@example.com"
    password = "password123"
    role = "CUSTOMER"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $body -ContentType "application/json"
    
    Write-Host "`n✅ Register Success!" -ForegroundColor Green
    Write-Host "`nResponse:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10
    
    Write-Host "`n✅ Access Token:" -ForegroundColor Yellow
    Write-Host $response.accessToken
    
} catch {
    Write-Host "`n❌ Register Failed!" -ForegroundColor Red
    Write-Host $_.Exception.Message
}

