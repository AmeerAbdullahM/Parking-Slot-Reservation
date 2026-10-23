@echo off
echo ========================================================
echo   ParkPulse Smart Parking Slot Reservation System
echo   Starting Backend & Frontend Development Servers...
echo ========================================================

echo [1/2] Launching Spring Boot Backend on http://localhost:8080 ...
start "ParkPulse Backend" cmd /k "cd backend && mvnw.cmd spring-boot:run"

timeout /t 5 /nobreak >nul

echo [2/2] Launching React Vite Frontend on http://localhost:5173 ...
start "ParkPulse Frontend" cmd /k "cd frontend && npm.cmd run dev"

echo.
echo ========================================================
echo   Both services initiated!
echo   Frontend: http://localhost:5173
echo   Backend:  http://localhost:8080
echo ========================================================
pause
