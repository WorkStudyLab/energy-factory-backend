# systemd 서비스 설정 가이드

## 개요
Spring Boot 애플리케이션을 systemd 서비스로 등록하여 자동 관리되도록 설정

## 기존 상태
```bash
# 수동 실행 중인 프로세스 확인
ps aux | grep java
# 결과: java -jar energy-factory-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## 설정 과정

### 1. 기존 프로세스 종료
```bash
sudo kill 3910  # 기존 PID
```

### 2. systemd 서비스 파일 생성
**파일 위치**: `/etc/systemd/system/energy-factory.service`

```ini
[Unit]
Description=Energy Factory Spring Boot Application
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/energy_factory
ExecStart=/usr/bin/java -jar energy-factory-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
Restart=always
RestartSec=10
StandardOutput=append:/home/ubuntu/energy_factory/app.log
StandardError=append:/home/ubuntu/energy_factory/app.log

[Install]
WantedBy=multi-user.target
```

### 3. 서비스 등록 및 시작
```bash
# 설정 파일 다시 로드
sudo systemctl daemon-reload

# 부팅 시 자동 시작 설정
sudo systemctl enable energy-factory

# 서비스 시작
sudo systemctl start energy-factory

# 상태 확인
sudo systemctl status energy-factory
```

## 결과 확인

### 서비스 상태
```
● energy-factory.service - Energy Factory Spring Boot Application
     Loaded: loaded (/etc/systemd/system/energy-factory.service; enabled; preset: enabled)
     Active: active (running) since Tue 2025-09-30 14:04:38 UTC; 3s ago
   Main PID: 41607 (java)
      Tasks: 15 (limit: 1121)
     Memory: 94.0M (peak: 94.3M)
        CPU: 1.961s
```

### 포트 확인
```bash
ss -tlnp | grep :8080
# 결과: LISTEN 0 100 *:8080 *:* users:(("java",pid=41607,fd=19))
```

## 주요 명령어

### 서비스 제어
```bash
# 시작
sudo systemctl start energy-factory

# 중지
sudo systemctl stop energy-factory

# 재시작
sudo systemctl restart energy-factory

# 상태 확인
sudo systemctl status energy-factory

# 로그 확인
sudo journalctl -u energy-factory -f
```

### 파일 위치
- **서비스 파일**: `/etc/systemd/system/energy-factory.service`
- **애플리케이션**: `/home/ubuntu/energy_factory/energy-factory-0.0.1-SNAPSHOT.jar`
- **로그 파일**: `/home/ubuntu/energy_factory/app.log`

## 장점
1. **자동 재시작**: 애플리케이션 크래시 시 자동 재시작
2. **부팅 시 자동 시작**: 서버 재부팅 시 자동으로 애플리케이션 시작
3. **로그 관리**: systemd와 애플리케이션 로그 통합 관리
4. **프로세스 관리**: 표준화된 서비스 관리 방식