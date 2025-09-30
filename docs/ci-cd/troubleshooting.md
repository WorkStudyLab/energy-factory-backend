# CI/CD 문제 해결 가이드

**최종 업데이트**: 2025-09-30 14:30 UTC

## 현재 주요 문제 🔴

### 테스트 환경 MySQL 연결 실패

#### 문제 상황
- **발생 위치**: GitHub Actions 테스트 단계
- **에러 메시지**:
```
> Task :test FAILED
EnergyFactoryApplicationTests > contextLoads() FAILED
    java.lang.IllegalStateException
    Caused by: org.hibernate.exception.JDBCConnectionException
    Caused by: com.mysql.cj.jdbc.exceptions.CommunicationsException
    Caused by: java.net.ConnectException
```

#### 근본 원인
- GitHub Actions 환경에는 MySQL 데이터베이스가 설치되어 있지 않음
- Spring Boot 테스트가 애플리케이션 컨텍스트 로드 시 데이터베이스 연결을 시도
- 연결 실패로 인해 전체 CI/CD 파이프라인이 중단됨

## 해결 방안 옵션

### 옵션 1: 테스트 건너뛰기 (Quick Fix) ⚡

#### 장점
- 즉시 CI/CD 파이프라인 동작 확인 가능
- 구현이 가장 간단함
- 현재 workflow 최소 수정

#### 단점
- 테스트 없는 배포로 인한 품질 위험
- 버그 조기 발견 불가능
- 장기적으로 권장되지 않는 방식

#### 구현 방법
```yaml
# .github/workflows/deploy.yml 수정
- name: Build with Gradle
  run: ./gradlew build -x test  # 테스트 제외
```

#### 적용 시나리오
- 급한 배포가 필요한 경우
- 테스트 환경 구축 전 임시 해결책으로 사용

---

### 옵션 2: H2 인메모리 데이터베이스 사용 (추천) ⭐

#### 장점
- 완전한 테스트 환경 구축
- 데이터베이스 로직 테스트 가능
- 외부 의존성 없음
- 테스트 속도 빠름

#### 단점
- 추가 설정 파일 필요
- MySQL과 H2 간 SQL 문법 차이 고려 필요

#### 구현 방법

##### 1. 테스트용 application.yml 생성
**파일**: `src/test/resources/application-test.yml`
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    
  h2:
    console:
      enabled: true
```

##### 2. 테스트 profile 적용
GitHub Actions workflow에서:
```yaml
- name: Run tests
  run: ./gradlew test -Dspring.profiles.active=test
  env:
    SPRING_PROFILES_ACTIVE: test
```

##### 3. H2 의존성 추가 확인
`build.gradle`에서 확인:
```gradle
testImplementation 'com.h2database:h2'
```

#### 적용 시나리오
- 일반적인 CI/CD 환경에서 권장
- 데이터베이스 로직이 중요한 애플리케이션

---

### 옵션 3: GitHub Actions MySQL 서비스 추가 🔧

#### 장점
- 실제 운영 환경과 동일한 데이터베이스 사용
- MySQL 특화 기능 테스트 가능
- 가장 현실적인 테스트 환경

#### 단점
- 설정 복잡도 높음
- 테스트 실행 시간 증가
- 외부 서비스 의존성

#### 구현 방법

##### workflow 파일에 MySQL 서비스 추가
```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: test
          MYSQL_DATABASE: energy_factory_test
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3
    
    steps:
    - name: Wait for MySQL
      run: |
        until mysqladmin ping -h "127.0.0.1" --silent; do
          echo 'waiting for mysqld to be connectable...'
          sleep 2
        done
    
    - name: Run tests
      run: ./gradlew test
      env:
        DB_HOST: 127.0.0.1
        DB_PORT: 3306
        DB_NAME: energy_factory_test
        DB_USER: root
        DB_PASSWORD: test
```

##### 테스트용 데이터베이스 설정 추가
`application-test.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/energy_factory_test?allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: test
```

#### 적용 시나리오
- MySQL 특화 기능을 많이 사용하는 경우
- 완벽한 통합 테스트가 필요한 경우

## 권장 해결 순서

### 1단계: 즉시 해결 (옵션 1)
```bash
# 임시로 테스트 건너뛰기
./gradlew build -x test
```

### 2단계: 중기 해결 (옵션 2 - 추천)
1. H2 테스트 환경 구축
2. 테스트 프로파일 설정
3. CI/CD 재실행

### 3단계: 장기 해결 (옵션 3)
1. MySQL 서비스 CI/CD 통합
2. 완전한 통합 테스트 환경 구축

## 구현 체크리스트

### 옵션 1 구현
- [ ] workflow 파일에서 `-x test` 추가
- [ ] 배포 테스트 실행
- [ ] 성공 확인

### 옵션 2 구현 (추천)
- [ ] `src/test/resources/application-test.yml` 생성
- [ ] H2 의존성 확인
- [ ] workflow 파일에 test profile 추가
- [ ] 로컬에서 H2 테스트 실행
- [ ] CI/CD 재실행

### 옵션 3 구현
- [ ] workflow에 MySQL 서비스 추가
- [ ] 테스트용 DB 설정 추가
- [ ] 헬스체크 로직 추가
- [ ] 환경변수 설정
- [ ] CI/CD 재실행

## 추가 고려사항

### 테스트 데이터 관리
- 테스트용 더미 데이터 생성 전략
- 테스트 간 데이터 격리 방법
- 테스트 성능 최적화

### 환경별 설정 분리
```
application.yml          # 기본 설정
application-dev.yml       # 개발 환경
application-test.yml      # 테스트 환경  
application-prod.yml      # 운영 환경
```

### 보안 고려사항
- 테스트 환경 credential 관리
- GitHub Secrets 사용 범위
- 민감정보 노출 방지

## 참고 링크

- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [GitHub Actions MySQL Service](https://docs.github.com/en/actions/using-containerized-services/creating-postgresql-service-containers)
- [H2 Database 설정](https://www.h2database.com/html/tutorial.html)

## 다음 단계

1. **팀 결정**: 3가지 옵션 중 선택
2. **구현**: 선택한 옵션에 따른 설정 변경
3. **테스트**: CI/CD 파이프라인 재실행
4. **검증**: 배포 성공 확인
5. **문서화**: 선택한 방안 및 결과 기록