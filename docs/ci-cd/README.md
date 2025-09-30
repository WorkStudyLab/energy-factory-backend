# CI/CD 구축 가이드

## 프로젝트 개요
- **프로젝트**: Energy Factory Backend
- **기술 스택**: Spring Boot, MySQL, AWS EC2, AWS RDS
- **CI/CD 도구**: GitHub Actions
- **배포 환경**: AWS EC2 (Ubuntu)

## 현재 상황 (2025-09-30)

### 기존 배포 방식
- **수동 배포**: EC2에 SSH 접속 후 수동으로 JAR 실행
- **프로세스 관리**: 수동 kill/start
- **로그 관리**: 수동 확인

### 목표 배포 방식
- **자동 배포**: GitHub push → 자동 빌드 → 자동 배포
- **프로세스 관리**: systemd 서비스
- **로그 관리**: systemd 로그 + 애플리케이션 로그

## 구축 단계

### 1단계: EC2 환경 설정 ✅ 완료
- **작업**: systemd 서비스 설정
- **결과**: 애플리케이션이 systemd 서비스로 관리됨
- **파일**: `/etc/systemd/system/energy-factory.service`

### 2단계: SSH 키 설정 🔄 진행 중
- **작업**: GitHub Actions용 SSH 키 생성
- **진행**: 키 생성 완료, authorized_keys 설정 필요

### 3단계: GitHub Actions 설정 ⏳ 대기
- **작업**: Workflow 파일 생성
- **필요**: GitHub Secrets 설정

### 4단계: 테스트 및 검증 ⏳ 대기
- **작업**: CI/CD 파이프라인 테스트
- **검증**: 자동 배포 동작 확인

## 관련 파일 구조
```
docs/
├── ci-cd/
│   ├── README.md                 # 이 파일
│   ├── systemd-setup.md         # systemd 설정 가이드
│   ├── github-actions-setup.md  # GitHub Actions 설정 가이드
│   └── troubleshooting.md       # 문제 해결 가이드
```