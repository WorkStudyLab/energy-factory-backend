# CloudFront + Route 53 커스텀 도메인 설정

## 개요
가비아에서 구매한 도메인(energy-factory.kr)을 AWS CloudFront + S3 정적 웹사이트에 연결하는 과정

## 목표
- 기존: https://d1o0ytu060swr1.cloudfront.net (CloudFront 기본 도메인)
- 변경: https://energy-factory.kr 및 https://www.energy-factory.kr

## 아키텍처
```
사용자
  ↓
energy-factory.kr (Route 53 A 레코드)
  ↓
CloudFront Distribution (SSL 인증서 적용)
  ↓
S3 Bucket (정적 웹사이트)
```

---

## 1단계: 도메인 구매 (가비아)

### 구매 정보
- **도메인**: energy-factory.kr
- **레지스트라**: 가비아(Gabia)
- **구매일**: 2025년

### 주의사항
- 도메인 구매 시 개인정보 보호 서비스 선택 권장
- 도메인 자동 갱신 설정 확인

---

## 2단계: Route 53 Hosted Zone 생성

### 2-1. Hosted Zone 생성
```
AWS Console → Route 53 → Hosted zones → Create hosted zone

설정:
- Domain name: energy-factory.kr
- Type: Public hosted zone
```

### 2-2. 네임서버 확인
생성되면 자동으로 4개의 네임서버(NS) 레코드가 생성됩니다:

예시:
```
ns-123.awsdns-12.com
ns-456.awsdns-45.net
ns-789.awsdns-78.org
ns-012.awsdns-01.co.uk
```

**중요**: 이 4개의 네임서버를 복사해둡니다 (다음 단계에서 가비아에 등록)

---

## 3단계: SSL 인증서 발급 (AWS Certificate Manager)

### 3-1. 리전 변경 **⚠️ 중요**
```
AWS Console 상단 리전 선택 → US East (N. Virginia) us-east-1로 변경
```

**왜 us-east-1인가?**
- CloudFront는 글로벌 서비스로, SSL 인증서는 반드시 us-east-1 리전에 있어야 함
- 다른 리전에서 발급하면 CloudFront에서 사용 불가

### 3-2. 인증서 요청
```
AWS Console → Certificate Manager (us-east-1) → Request certificate

설정:
- Certificate type: Request a public certificate
- Fully qualified domain name:
  * energy-factory.kr
  * www.energy-factory.kr (Add another name to this certificate)
- Validation method: DNS validation (권장)
- Key algorithm: RSA 2048
```

### 3-3. DNS 검증 레코드 추가
인증서 요청 후 "Create records in Route 53" 버튼 클릭
- Route 53 Hosted Zone에 자동으로 CNAME 레코드 추가됨
- 검증 완료까지 5분~30분 소요

**수동 추가 시**:
ACM에서 제공하는 CNAME 레코드를 Route 53에 직접 추가:
```
Name: _abc123.energy-factory.kr
Type: CNAME
Value: _xyz456.acm-validations.aws.
```

### 3-4. 인증서 상태 확인
```
Status: Issued (발급 완료)
```

---

## 4단계: 가비아 네임서버 변경

### 4-1. 가비아 관리 콘솔 접속
```
가비아 → My가비아 → 도메인 관리 → energy-factory.kr 선택
```

### 4-2. 네임서버 변경
```
도메인 정보 변경 → 네임서버 설정 → 다른 네임서버 사용

기존 가비아 네임서버 삭제 후 Route 53 네임서버 4개 등록:
- ns-123.awsdns-12.com
- ns-456.awsdns-45.net
- ns-789.awsdns-78.org
- ns-012.awsdns-01.co.uk
```

### 4-3. 전파 대기
- DNS 전파 시간: 10분~48시간 (보통 1~2시간 내 완료)
- 확인 명령어:
```bash
# 네임서버 확인
dig NS energy-factory.kr

# 또는
nslookup -type=NS energy-factory.kr
```

**⚠️ 중요한 타이밍 이슈**:
- 네임서버를 변경하기 전에 ACM 인증서를 발급받으면, 가비아 DNS에서 CNAME 레코드를 찾지 못해 검증 실패
- **올바른 순서**: Route 53 생성 → 네임서버 변경 → ACM 인증서 요청
- 또는: ACM 인증서 요청 후 네임서버 변경 → DNS 전파 후 자동 검증 완료

---

## 5단계: CloudFront Distribution 설정

### 5-1. Alternate Domain Names (CNAME) 추가
```
AWS Console → CloudFront → Distributions → 기존 Distribution 선택 → Edit

Settings:
- Alternate domain names (CNAMEs):
  * energy-factory.kr
  * www.energy-factory.kr

- Custom SSL certificate:
  * 앞서 발급받은 ACM 인증서 선택
  * (예: energy-factory.kr (abc12345-xxxx-xxxx-xxxx-xxxxxxxxxxxx))
```

### 5-2. 변경 사항 저장
- "Save changes" 클릭
- CloudFront 배포 상태: Deploying → Enabled (5~10분 소요)

---

## 6단계: Route 53 A 레코드 추가

### 6-1. A 레코드 생성 (Root 도메인)
```
Route 53 → Hosted zones → energy-factory.kr → Create record

설정:
- Record name: (비워둠, root 도메인)
- Record type: A - Routes traffic to an IPv4 address
- Alias: Yes
- Route traffic to:
  * Alias to CloudFront distribution
  * Distribution 선택: d1o0ytu060swr1.cloudfront.net
- Routing policy: Simple routing
```

### 6-2. A 레코드 생성 (www 서브도메인)
```
Route 53 → Create record

설정:
- Record name: www
- Record type: A
- Alias: Yes
- Route traffic to:
  * Alias to CloudFront distribution
  * Distribution 선택: d1o0ytu060swr1.cloudfront.net
```

### 6-3. (선택) AAAA 레코드 추가 (IPv6 지원)
IPv6 지원을 원하면 동일한 방식으로 AAAA 레코드도 생성:
```
- Record type: AAAA
- 나머지 설정은 A 레코드와 동일
```

---

## 7단계: SecurityConfig CORS 설정 업데이트

### 7-1. Spring Boot CORS 설정 수정
`src/main/java/com/energyfactory/energy_factory/config/SecurityConfig.java`

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
            "https://energy-factory.kr",  // 커스텀 도메인
            "https://www.energy-factory.kr",  // www 포함
            "https://d1o0ytu060swr1.cloudfront.net",  // CloudFront 도메인 (유지)
            "http://localhost:3000",
            "http://localhost:5173"
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### 7-2. application-prod.yml 업데이트
```yaml
app:
  oauth2:
    redirect-url: ${FRONTEND_URL:https://energy-factory.kr}
  cors:
    allowed-origins: ${ALLOWED_ORIGINS:https://energy-factory.kr,https://www.energy-factory.kr,https://d1o0ytu060swr1.cloudfront.net}
```

### 7-3. EC2 환경 변수 설정
```bash
FRONTEND_URL=https://energy-factory.kr
ALLOWED_ORIGINS=https://energy-factory.kr,https://www.energy-factory.kr,https://d1o0ytu060swr1.cloudfront.net
```

---

## 8단계: 테스트 및 검증

### 8-1. DNS 전파 확인
```bash
# A 레코드 확인
dig A energy-factory.kr
dig A www.energy-factory.kr

# 예상 결과: CloudFront IP 주소 반환
```

### 8-2. SSL 인증서 확인
```bash
# 브라우저에서 접속
https://energy-factory.kr
https://www.energy-factory.kr

# 또는 curl로 확인
curl -I https://energy-factory.kr
```

**확인 사항**:
- ✅ SSL 인증서 유효 (자물쇠 아이콘)
- ✅ 인증서 발급자: Amazon
- ✅ 도메인 이름 일치
- ✅ 페이지 정상 로드

### 8-3. CloudFront 동작 확인
응답 헤더에서 CloudFront 확인:
```bash
curl -I https://energy-factory.kr

# 예상 헤더
x-cache: Hit from cloudfront
via: 1.1 xxxxx.cloudfront.net (CloudFront)
```

### 8-4. CORS 테스트
프론트엔드에서 API 호출 시 CORS 에러가 발생하지 않는지 확인

---

## 최종 구성도

```
┌─────────────────────┐
│  energy-factory.kr  │
│ www.energy-factory  │
└──────────┬──────────┘
           │ (Route 53 A Record)
           ↓
┌─────────────────────────────────┐
│  CloudFront Distribution        │
│  - SSL: ACM Certificate         │
│  - Origin: S3 Bucket            │
│  - HTTPS Only                   │
└──────────┬──────────────────────┘
           │
           ↓
┌─────────────────────────────────┐
│  S3 Bucket                      │
│  - Static Website Hosting       │
│  - React Build Files            │
└─────────────────────────────────┘
```

---

## 비용 정보

### AWS 서비스 비용
| 서비스 | 비용 |
|--------|------|
| Route 53 Hosted Zone | $0.50/월 |
| Route 53 쿼리 (100만 건) | $0.40 |
| ACM SSL 인증서 | 무료 (퍼블릭 인증서) |
| CloudFront 데이터 전송 | 첫 1TB $0.085/GB |
| S3 스토리지 | $0.023/GB/월 |

### 도메인 비용
- **energy-factory.kr**: 연간 약 15,000원 (가비아 기준)

---

## 트러블슈팅

### 문제 1: ACM 인증서가 "Pending validation" 상태
**원인**: DNS 레코드가 Route 53에 없거나, 네임서버가 아직 가비아로 설정되어 있음

**해결**:
1. Route 53에 CNAME 검증 레코드가 있는지 확인
2. 가비아 네임서버를 Route 53으로 변경했는지 확인
3. DNS 전파 대기 (최대 48시간)

### 문제 2: 도메인 접속 시 "DNS_PROBE_FINISHED_NXDOMAIN" 에러
**원인**: DNS 전파가 완료되지 않았거나 A 레코드 설정 오류

**해결**:
1. `dig A energy-factory.kr`로 A 레코드 확인
2. Route 53 Hosted Zone의 A 레코드가 CloudFront를 가리키는지 확인
3. 시크릿 모드나 다른 네트워크에서 테스트 (DNS 캐시 문제 가능성)

### 문제 3: HTTPS 접속 시 인증서 경고
**원인**: CloudFront에 ACM 인증서가 제대로 연결되지 않음

**해결**:
1. CloudFront 설정에서 "Custom SSL certificate" 확인
2. 인증서가 us-east-1 리전에 있는지 확인
3. 인증서 도메인이 energy-factory.kr과 www.energy-factory.kr를 포함하는지 확인

### 문제 4: www는 되는데 root 도메인은 안 됨 (또는 반대)
**원인**: Route 53 A 레코드 중 하나가 누락됨

**해결**:
1. Route 53에서 두 개의 A 레코드 확인:
   - energy-factory.kr (root)
   - www.energy-factory.kr
2. 둘 다 CloudFront Distribution을 가리키는지 확인

### 문제 5: CORS 에러 발생
**원인**: SecurityConfig에 새 도메인이 추가되지 않음

**해결**:
1. `SecurityConfig.java`의 `setAllowedOrigins`에 도메인 추가
2. 애플리케이션 재배포
3. 브라우저 캐시 삭제 후 재시도

---

## 참고 자료

### AWS 공식 문서
- [Route 53 시작하기](https://docs.aws.amazon.com/route53/latest/developerguide/Welcome.html)
- [CloudFront에 SSL 인증서 사용](https://docs.aws.amazon.com/acm/latest/userguide/cloudfront-certs.html)
- [ACM 인증서 검증](https://docs.aws.amazon.com/acm/latest/userguide/dns-validation.html)

### DNS 전파 확인 도구
- https://dnschecker.org
- https://www.whatsmydns.net

### SSL 인증서 확인
- https://www.ssllabs.com/ssltest/

---

## 체크리스트

설정 완료 확인용 체크리스트:

- [ ] 가비아에서 도메인 구매 완료
- [ ] Route 53 Hosted Zone 생성
- [ ] Route 53 네임서버 4개 확인
- [ ] 가비아에서 네임서버를 Route 53으로 변경
- [ ] ACM 인증서 발급 (us-east-1 리전)
- [ ] ACM 인증서 상태 "Issued" 확인
- [ ] CloudFront에 커스텀 도메인 추가
- [ ] CloudFront에 ACM 인증서 연결
- [ ] Route 53에 A 레코드 생성 (root)
- [ ] Route 53에 A 레코드 생성 (www)
- [ ] SecurityConfig CORS 설정 업데이트
- [ ] EC2 환경 변수 업데이트
- [ ] https://energy-factory.kr 접속 테스트
- [ ] https://www.energy-factory.kr 접속 테스트
- [ ] SSL 인증서 유효성 확인
- [ ] 프론트엔드에서 API 호출 CORS 테스트

---

## 완료 일자
- 도메인 구매: 2025년
- Route 53 설정: 2025년
- SSL 인증서 발급: 2025년
- CloudFront 연결: 2025년
- 최종 테스트 완료: 2025년
