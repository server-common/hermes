# Hermes - 메일 전송 시스템

Spring Boot 3.5.4, Java 21, Gradle을 사용한 메일 전송 시스템입니다.
Hermes는 그리스 신화의 전령의 신으로, 빠르고 안정적인 메시지 전달을 상징합니다.

## 기술 스택

- **Java 21**
- **Spring Boot 3.5.4**
- **Gradle 9.1.0**
- **Lombok** - 보일러플레이트 코드 제거
- **PostgreSQL** - 메일 로그 저장
- **Redis** - 메일 큐잉 및 캐싱
- **Spring Mail** - 메일 전송

## 주요 기능

- 비동기 메일 전송
- **대량 메일 발송** - 최대 1,000명까지 한 번에 발송 가능
- **배치 처리** - 큐 처리 성능 최적화를 위한 배치 단위 처리
- Redis를 이용한 메일 큐잉
- PostgreSQL을 이용한 메일 로그 관리
- **메일 템플릿 시스템** - 변수 치환 지원
- **개인화 메일** - 수신자별 개별 변수 치환
- **시스템 설정 관리** - 전송 제한, 재시도 설정 등
- **Redis 캐시 시스템** - 설정값 및 템플릿 캐싱으로 성능 최적화
- **자동 캐시 워밍업** - 애플리케이션 시작 시 자주 사용되는 데이터 미리 로드
- RESTful API
- 메일 전송 상태 추적
- 일일 전송 제한 기능

## API 엔드포인트

### 메일 전송
```http
POST /mail/send
Content-Type: application/json

{
  "to": "recipient@example.com",
  "subject": "테스트 메일",
  "content": "메일 내용",
  "isHtml": true
}
```

### 템플릿 메일 전송
```http
POST /mail/send/template
Content-Type: application/json

{
  "to": "recipient@example.com",
  "templateName": "welcome",
  "variables": {
    "name": "홍길동",
    "company": "Hermes Corp"
  }
}
```

### 대량 메일 전송
```http
POST /mail/send/bulk
Content-Type: application/json

{
  "recipients": [
    {
      "to": "user1@example.com",
      "name": "홍길동"
    },
    {
      "to": "user2@example.com", 
      "name": "김철수"
    }
  ],
  "subject": "{{name}}님께 드리는 공지사항",
  "content": "<h1>안녕하세요 {{name}}님!</h1><p>중요한 공지사항을 전달드립니다.</p>",
  "isHtml": true
}
```

### 대량 템플릿 메일 전송
```http
POST /mail/send/bulk/template
Content-Type: application/json

{
  "recipients": [
    {
      "to": "user1@example.com",
      "variables": {
        "name": "홍길동",
        "company": "ABC Corp",
        "position": "개발자"
      }
    },
    {
      "to": "user2@example.com",
      "variables": {
        "name": "김철수", 
        "company": "XYZ Corp",
        "position": "디자이너"
      }
    }
  ],
  "templateName": "welcome"
}

# 응답 예시
{
  "batchId": "BULK_A1B2C3D4",
  "totalCount": 2,
  "successCount": 2,
  "failedCount": 0,
  "results": [
    {
      "to": "user1@example.com",
      "success": true,
      "mailLogId": 123,
      "errorMessage": null
    },
    {
      "to": "user2@example.com", 
      "success": true,
      "mailLogId": 124,
      "errorMessage": null
    }
  ],
  "requestedAt": "2025-10-08T10:30:00"
}
```

### 메일 템플릿 관리
```http
# 템플릿 생성
POST /mail/template
{
  "name": "welcome",
  "subject": "{{company}}에 오신 것을 환영합니다, {{name}}님!",
  "content": "<h1>환영합니다!</h1><p>{{name}}님, {{company}}에 오신 것을 환영합니다.</p>",
  "isHtml": true
}

# 템플릿 조회
GET /mail/template?page=0&size=20&sortBy=createdAt&sortDir=desc
GET /mail/template/{id}
GET /mail/template/name/{name}

# 템플릿 검색
GET /mail/template/search?keyword=welcome&page=0&size=20
```

### 메일 설정 관리
```http
# 설정 생성
POST /mail/setting
{
  "settingKey": "daily_limit",
  "settingValue": "5000",
  "description": "일일 메일 전송 제한"
}

# 설정 조회 (캐시 적용)
GET /mail/setting?page=0&size=20&sortBy=settingKey&sortDir=asc
GET /mail/setting/key/{key}
GET /mail/setting/key/{key}/value
GET /mail/setting/all # 모든 설정 조회 (페이징 없음)

# 설정 값 변경 (캐시 자동 삭제)
PUT /mail/setting/key/{key}
{
  "value": "10000"
}
```

### 캐시 관리
```http
# 모든 캐시 삭제
DELETE /cache/all

# 메일 설정 캐시만 삭제
DELETE /cache/mail-settings

# 특정 캐시 키 삭제
DELETE /cache/{cacheName}/{key}

# 캐시 워밍업
POST /cache/warmup

# 캐시 강제 갱신
POST /cache/refresh

# 캐시 상태 체크
GET /cache/health

# 캐시 통계 조회
GET /cache/stats
```

### 메일 큐 시스템
```http
# 큐 상태 조회
GET /mail/queue/status

# 응답 예시
{
  "pendingCount": 15,     # 대기 중인 메일 수
  "processingCount": 3,   # 처리 중인 메일 수
  "retryCount": 2         # 재시도 대기 중인 메일 수
}
```

### 대량 발송 상태 조회
```http
# 배치 상태 조회
GET /mail/bulk/status/{batchId}

# 응답 예시
{
  "id": 1,
  "batchId": "BULK_A1B2C3D4",
  "totalCount": 1000,
  "successCount": 995,
  "failedCount": 5,
  "successRate": 99.5,
  "status": "COMPLETED",
  "templateName": "welcome",
  "createdAt": "2025-10-08T10:30:00",
  "completedAt": "2025-10-08T10:35:00",
  "processingTimeSeconds": 300
}
```

### 통계
```http
# 메일 통계 확인
GET /health/stats

# 응답 예시
{
  "successRate": 100.0,                         # 성공률
  "totalMails": 14,                             # 전체 메일 수
  "pendingMails": 0,                            # 대기 중인 메일 수
  "failedMails": 0,                             # 송신 실패 수
  "sentMails": 14,                              # 송신 메일 수
  "timestamp": "2025-10-07T07:44:17.719608"     # 조회 일자
}
```

## Redis 큐 시스템 동작 원리

### 큐 시스템의 역할
`redisTemplate.opsForList().rightPush(MAIL_QUEUE_KEY, savedMailLog.getId())` 코드는 **메일 전송 큐잉 시스템**의 핵심입니다:

#### 1. 비동기 처리
- 메일 전송 요청을 즉시 응답하고, 실제 전송은 백그라운드에서 처리
- 사용자는 메일 전송 완료를 기다리지 않아도 됨

#### 2. 안정성 보장
- Redis List를 FIFO 큐로 사용하여 메일 전송 순서 보장
- 서버 재시작 시에도 큐에 있는 메일들이 유실되지 않음

#### 3. 확장성
- 여러 서버에서 동일한 Redis 큐를 공유하여 부하 분산 가능
- 메일 전송량이 많아져도 큐를 통해 안정적으로 처리

#### 4. 재시도 메커니즘
- 전송 실패 시 자동으로 재시도 큐에 추가
- 설정 가능한 재시도 횟수와 지연 시간

### 큐 시스템 구조
```
메일 요청 → Redis 큐 → 백그라운드 워커 → 실제 전송
                ↓
            재시도 큐 (실패 시)
```

### 메일 로그 조회
```http
# 페이징 조회 (간편한 방식)
GET /mail/logs?page=0&size=20&sortBy=createdAt&sortDir=desc

# 특정 메일 로그 조회
GET /mail/logs/{id}

# 상태별 메일 로그 조회
GET /mail/logs/status/SENT?page=0&size=20&sortBy=createdAt&sortDir=desc
```

## 메일 상태

- `PENDING`: 전송 대기
- `SENT`: 전송 완료
- `FAILED`: 전송 실패

## 데이터베이스 스키마

### hermes 스키마 구조

- **mail_log**: 메일 전송 로그
- **mail_template**: 메일 템플릿
- **mail_setting**: 시스템 설정

### 주요 테이블

#### mail_log
- `id`: 메일 로그 고유 ID
- `recipient`: 수신자 이메일
- `subject`: 메일 제목
- `content`: 메일 내용
- `status`: 전송 상태 (PENDING, SENT, FAILED)
- `sent_at`: 전송 완료 시간
- `created_at`: 생성 시간
- `error_message`: 오류 메시지

#### mail_template
- `id`: 템플릿 고유 ID
- `name`: 템플릿 이름 (고유)
- `subject`: 메일 제목 템플릿
- `content`: 메일 내용 템플릿
- `is_html`: HTML 여부
- `created_at`, `updated_at`: 생성/수정 시간

#### mail_setting
- `id`: 설정 고유 ID
- `setting_key`: 설정 키 (고유)
- `setting_value`: 설정 값
- `description`: 설정 설명
- `created_at`, `updated_at`: 생성/수정 시간

### 인덱스
- 수신자별 조회 최적화
- 상태별 조회 최적화
- 날짜별 조회 최적화

## 설정

`src/main/resources/application.yml`에서 다음 설정을 수정할 수 있습니다:

- 데이터베이스 연결 정보 (hermes 스키마 사용)
- Redis 연결 정보
- SMTP 서버 설정
- JPA DDL 자동 생성 비활성화 (`ddl-auto: none`)
- 로깅 레벨
#
# 캐시 워밍업 시스템

### 자동 워밍업
- **애플리케이션 시작 시**: 자주 사용되는 설정값과 템플릿을 미리 캐시에 로드
- **스케줄 기반**: 매일 새벽 3시에 캐시 자동 갱신
- **설정 가능**: `application.yml`에서 워밍업 대상 설정 가능

### 워밍업 대상
- **메일 설정**: daily_limit, max_retry_count, retry_delay_minutes 등
- **메일 템플릿**: welcome, password_reset, verification, notification, reminder
- **전체 설정 목록**: 관리자 화면에서 사용

### 기본 템플릿
시스템에는 다음과 같은 기본 템플릿이 포함되어 있습니다:

1. **welcome** - 환영 메일 (회원가입 완료)
2. **password_reset** - 비밀번호 재설정
3. **verification** - 이메일 주소 인증
4. **notification** - 시스템 알림
5. **reminder** - 일정 리마인더

각 템플릿은 변수 치환을 지원하며, HTML 형식으로 제작되어 있습니다.

### 캐시 유지보수
- **매시간 상태 체크**: 캐시 정상 동작 여부 확인
- **주간 리포트**: 캐시 사용 통계 로깅
- **수동 갱신**: API를 통한 즉시 캐시 갱신 가능

### 설정 예시
```yaml
hermes:
  cache:
    warmup:
      enabled: true
      delay-seconds: 2
      frequent-settings:
        - daily_limit
        - max_retry_count
        - batch_size
      frequent-templates:
        - welcome
        - notification
```

## 대량 발송 시스템

### 주요 특징
- **최대 1,000명** 동시 발송 지원
- **배치 ID** 추적으로 발송 상태 관리
- **개별 결과** 제공 (성공/실패 상세 정보)
- **개인화 지원** - 수신자별 변수 치환
- **일일 제한** 자동 체크
- **실패 처리** - 개별 실패 시에도 나머지 발송 계속

### 성능 최적화
- **배치 처리**: `batch_size` 설정으로 큐 처리 성능 조절
- **비동기 처리**: 대량 발송 요청 즉시 응답
- **메모리 효율**: 스트림 처리로 메모리 사용량 최적화

### 권장 설정값
```http
POST /mail/setting
{
  "settingKey": "batch_size",
  "settingValue": "20",
  "description": "큐 처리 시 배치 크기 (기본: 10)"
}
```

### 사용 예시

#### 1. 단순 대량 발송
```json
{
  "recipients": [
    {"to": "user1@example.com", "name": "홍길동"},
    {"to": "user2@example.com", "name": "김철수"}
  ],
  "subject": "{{name}}님께 드리는 안내",
  "content": "안녕하세요 {{name}}님, 중요한 공지사항입니다.",
  "isHtml": false
}
```

#### 2. 복잡한 템플릿 대량 발송
```json
{
  "recipients": [
    {
      "to": "user1@example.com",
      "variables": {
        "name": "홍길동",
        "company": "ABC Corp",
        "expiry_date": "2025-12-31",
        "discount": "20%"
      }
    }
  ],
  "templateName": "promotion"
}
```
