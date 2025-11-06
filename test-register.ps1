# Test Register API
$body = @{
    name = "Test User"
    email = "test@example.com"
    password = "password123"
    role = "CUSTOMER"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $body -ContentType "application/json"

Write-Host "Register Response:" -ForegroundColor Green
$response | ConvertTo-Json -Depth 10

