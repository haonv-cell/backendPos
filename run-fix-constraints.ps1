# Script to fix database constraints
# Run this to allow both uppercase and lowercase values for role and provider

Write-Host ""
Write-Host "Fixing database constraints..." -ForegroundColor Cyan
Write-Host ""

# PostgreSQL connection details
$env:PGPASSWORD = "postgres"
$dbHost = "localhost"
$dbPort = "5432"
$dbName = "pos"
$dbUser = "postgres"

# Try to find psql in common locations
$psqlPaths = @(
    "C:\Program Files\PostgreSQL\16\bin\psql.exe",
    "C:\Program Files\PostgreSQL\15\bin\psql.exe",
    "C:\Program Files\PostgreSQL\14\bin\psql.exe",
    "C:\Program Files\PostgreSQL\13\bin\psql.exe",
    "C:\Program Files (x86)\PostgreSQL\16\bin\psql.exe",
    "C:\Program Files (x86)\PostgreSQL\15\bin\psql.exe"
)

$psqlPath = $null
foreach ($path in $psqlPaths) {
    if (Test-Path $path) {
        $psqlPath = $path
        break
    }
}

if ($psqlPath) {
    Write-Host "Found psql at: $psqlPath" -ForegroundColor Green
    Write-Host ""

    # Execute SQL
    & $psqlPath -h $dbHost -p $dbPort -U $dbUser -d $dbName -f "fix-constraints.sql"

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Constraints fixed successfully!" -ForegroundColor Green
        Write-Host "You can now restart the application and test again." -ForegroundColor Yellow
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "Failed to execute SQL" -ForegroundColor Red
        Write-Host ""
    }
} else {
    Write-Host "Could not find psql.exe automatically." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please run the SQL manually:" -ForegroundColor Cyan
    Write-Host "1. Open pgAdmin or your PostgreSQL client"
    Write-Host "2. Connect to database 'pos'"
    Write-Host "3. Open and execute file: fix-constraints.sql"
    Write-Host ""
}

# Clear password from environment
Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue

