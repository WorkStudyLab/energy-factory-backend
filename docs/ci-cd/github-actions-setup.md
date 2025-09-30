# GitHub Actions CI/CD 설정 가이드

## 개요
GitHub Actions를 사용하여 main 브랜치에 push할 때 자동으로 EC2에 배포되도록 설정

## 전체 프로세스 흐름
```
Developer → git push origin main → GitHub Actions → Build & Test → Deploy to EC2 → Restart Service
```

## 설정 단계

### 1. EC2에서 SSH 키 생성 🔄 진행 중

#### 키 생성
```bash
ssh-keygen -t rsa -b 4096 -C "github-actions" -f ~/.ssh/github_actions_key -N ""
```

#### 결과
- **Private Key**: `/home/ubuntu/.ssh/github_actions_key`
- **Public Key**: `/home/ubuntu/.ssh/github_actions_key.pub`

#### 진행 상황
- ✅ 키 생성 완료
- ⏳ public key를 authorized_keys에 추가 필요
- ⏳ GitHub Secrets 설정 필요

### 2. GitHub Secrets 설정 ⏳ 대기

GitHub 저장소 → Settings → Secrets and variables → Actions에서 설정:

| Secret Name | 설명 | 값 |
|-------------|------|-----|
| `EC2_HOST` | EC2 Public IP | `[EC2 Public IP]` |
| `EC2_SSH_KEY` | SSH Private Key | `[/home/ubuntu/.ssh/github_actions_key 내용]` |
| `EC2_USER` | SSH 사용자명 | `ubuntu` |

### 3. Workflow 파일 생성 ⏳ 대기

**파일 위치**: `.github/workflows/deploy.yml`

```yaml
name: Deploy to EC2

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
      
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Build with Gradle
      run: ./gradlew build
      
    - name: Run tests
      run: ./gradlew test

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
      
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Build with Gradle
      run: ./gradlew build
      
    - name: Deploy to EC2
      uses: appleboy/ssh-action@v0.1.5
      with:
        host: ${{ secrets.EC2_HOST }}
        username: ${{ secrets.EC2_USER }}
        key: ${{ secrets.EC2_SSH_KEY }}
        script: |
          # 프로젝트 디렉토리로 이동
          cd /home/ubuntu/energy_factory
          
          # Git 업데이트
          git pull origin main
          
          # 빌드
          chmod +x gradlew
          ./gradlew build
          
          # 새 JAR 파일 복사
          cp build/libs/energy-factory-0.0.1-SNAPSHOT.jar ./
          
          # 서비스 재시작
          sudo systemctl restart energy-factory
          
          # 상태 확인
          sudo systemctl status energy-factory
```

## 필요한 EC2 설정

### Git 저장소 설정
```bash
# EC2에 프로젝트 클론 (아직 안되어 있다면)
cd /home/ubuntu
git clone https://github.com/your-username/energy-factory-backend.git energy_factory

# 또는 기존 디렉토리를 Git 저장소로 초기화
cd /home/ubuntu/energy_factory
git init
git remote add origin https://github.com/your-username/energy-factory-backend.git
git fetch
git reset --hard origin/main
```

### Gradle 실행 권한
```bash
chmod +x /home/ubuntu/energy_factory/gradlew
```

## 테스트 시나리오

### 1. 로컬에서 코드 변경
```bash
# 간단한 변경사항 추가
echo "// CI/CD test" >> src/main/java/com/energyfactory/energy_factory/EnergyFactoryApplication.java

git add .
git commit -m "test: CI/CD pipeline test"
git push origin main
```

### 2. GitHub Actions 확인
- GitHub 저장소 → Actions 탭에서 실행 상태 확인
- 빌드 및 배포 로그 확인

### 3. EC2에서 배포 확인
```bash
# 서비스 상태 확인
sudo systemctl status energy-factory

# 로그 확인
sudo journalctl -u energy-factory -f
```

## 주의사항

1. **보안**: SSH Private Key는 절대 코드에 포함하지 말 것
2. **권한**: EC2에서 sudo 권한이 필요한 명령어들 확인
3. **포트**: 보안 그룹에서 8080 포트 열려있는지 확인
4. **Git**: EC2에서 Git 인증 설정 필요할 수 있음

## 문제 해결

### 일반적인 오류
1. **SSH 연결 실패**: 키 형식, EC2 보안 그룹 확인
2. **빌드 실패**: Java 버전, Gradle 권한 확인
3. **서비스 재시작 실패**: systemd 서비스 설정 확인