# 업무 관리 & 커리어 기록 서비스 기획서

작성일: 2026-09-21

## 개요

업무를 관리하면서 기록을 쌓고, 나중에 이력서와 면접에서 꺼내 쓰는 개인용 웹 서비스를 만든다.

- **용도**: 직접 쓰는 업무 관리·기록·업무일지 도구이자 이직용 포트폴리오
- **기술 방향**: Java/Spring Boot 백엔드(JPA, JWT, PostgreSQL)와 React 화면
- **비용**: 이미 설치된 PostgreSQL과 무료 도구만 사용

## 문제 정의

시간이 지나면 내가 했던 일이 기억나지 않아서, 이력서를 쓸 때마다 처음부터 기억을 더듬어야 한다.

특히 기억이 나지 않아 아쉬웠던 것은 세 가지다.

- 어떤 기술을 썼는지
- 문제를 어떻게 풀었는지
- 결과와 개선 수치

이 세 가지는 이력서 문장의 재료(상황·과제, 행동, 결과)와 그대로 이어진다. 그래서 업무를 하는 동안 바로 이 항목들을 남기는 것이 서비스의 핵심이다.

## 사용 시나리오

퇴근 후 집에서 하루를 돌아보며 일일 기록과 업무별 진행 메모를 함께 남긴다.

1. 그날 날짜의 일일 기록을 열고 하루 회고를 한두 줄 적는다.
2. 오늘 손댄 업무를 골라 진행 메모(겪은 문제, 해결 방법)와 그날 쓴 소요 시간을 남긴다. 목록에 없는 업무는 그 자리에서 새로 등록한다.
3. 업무를 끝내면 상태를 완료로 바꾸고 성과(지표명, 개선 전, 개선 후)를 적는다.

같은 메모를 날짜별로 보면 "그날 무엇을 했는지", 업무별로 보면 "이 업무를 어떤 과정으로 풀었는지"가 된다. 이력서에는 업무별 흐름이 필요하다.

기록은 회사 밖에서 쓰므로, 회사 문서나 코드는 붙여 넣지 않고 기억 속의 기술·문제·해결·수치만 본인 말로 적는다.

## 기능 범위

첫 버전(MVP)은 로그인, 업무 관리, 기록, 조회까지이고, 알림과 내보내기는 그 뒤에 붙인다.

| 기능 | 내용 | 단계 |
| --- | --- | --- |
| 회원가입/로그인 | JWT 인증, 내 데이터만 조회·수정 | MVP |
| 프로젝트·업무 관리 | 상태(예정/진행/완료), 마감일, 우선순위, 보관 처리 | MVP |
| 기술 태그 | 업무에 사용한 기술을 태그로 분류 | MVP |
| 업무 시스템 | 업무를 시스템별로 분류하고, 한 업무에 여러 시스템 지정 | MVP |
| 성과 항목 | 지표명, 개선 전, 개선 후를 문자열로 입력 | MVP |
| 일일 기록·진행 메모 | 하루 회고와 업무별 문제·해결 메모, 소요 시간 | MVP |
| 조회 | 날짜별, 업무별, 상태별, 시스템별 조회와 페이징 | MVP |
| 보관 항목 복구 | 보관 처리한 업무·프로젝트 되살리기 | 이후 |
| 알림 | 저녁 기록 알림, 마감일 알림(스케줄러, 슬랙/디스코드 웹훅) | 이후 |
| 이력서용 내보내기 | 선택한 업무와 성과를 마크다운으로 출력 | 이후 |
| GitHub 연동 | 커밋·PR을 기록 후보로 제안 | 이후 |
| Docker·CI | 앱 컨테이너화, GitHub Actions 자동 빌드·테스트 | 이후 |

## 데이터 구조

엔티티는 10개이고, 업무(Task)를 중심으로 프로젝트·태그·성과·진행 메모가 연결된다. 아래는 초안이며 ERD 단계에서 확정한다.

| 엔티티 | 주요 필드 | 관계 |
| --- | --- | --- |
| Member | 이메일(중복 불가), 비밀번호, 닉네임 | 모든 데이터의 주인 |
| Project | 이름, 설명, 시작일, 종료일 | Member 1:N Project |
| Task | 제목, 설명, 상태, 우선순위, 마감일, 완료일 | Project 1:N Task |
| TaskResult | 지표명, 개선 전, 개선 후(모두 문자열) | Task 1:N TaskResult |
| Tag | 기술 이름(회원별 중복 불가) | Member 1:N Tag |
| TaskTag | 업무와 태그를 잇는 중간 엔티티 | Task N:M Tag를 1:N, N:1로 풀어서 연결 |
| DailyLog | 날짜, 하루 회고(회원당 하루 하나) | Member 1:N DailyLog |
| TaskLog | 진행 메모(겪은 문제, 해결 방법), 소요 시간(분) | DailyLog 1:N TaskLog, Task 1:N TaskLog |
| WorkSystem | 업무 시스템 이름(회원별 중복 불가), 설명 | Member 1:N WorkSystem |
| TaskWorkSystem | 업무와 업무 시스템을 잇는 중간 엔티티 | Task N:M WorkSystem을 1:N, N:1로 풀어서 연결 |

**설계 결정**

- **다대다는 중간 엔티티로 푼다.** 업무와 태그(TaskTag), 업무와 업무 시스템(TaskWorkSystem)의 관계를 @ManyToMany로 바로 잇지 않고 중간 엔티티로 분리한다.
- **TaskLog가 일일 기록과 업무를 잇는다.** 같은 메모를 날짜별로도, 업무별로도 조회할 수 있다.
- **삭제는 보관 처리한다.** 업무를 삭제해도 진행 메모, 성과, 태그 연결은 남고 목록에서만 숨긴다. 프로젝트를 보관하면 하위 업무도 목록에서 함께 숨긴다.
- **성과는 문자열 항목형이다.** 수치가 아닌 성과(예: 수동 배포에서 자동 배포로)도 담을 수 있다. 개선율 자동 계산은 하지 않는다.

## ERD

테이블은 10개이고, 모든 테이블은 `id`(BIGINT, 자동 증가, 기본키)와 `created_at`, `updated_at`(TIMESTAMP, NOT NULL, JPA Auditing으로 자동 기록)을 공통으로 가진다. 아래 표는 그 외 컬럼과 제약 조건이다.

**공통 규칙**

- 테이블·컬럼 이름은 snake_case로 한다. 회원 테이블은 PostgreSQL 예약어 `user`를 피해 `member`로 하고, 업무 시스템은 자바 기본 클래스 `java.lang.System`과 이름이 겹치지 않도록 엔티티를 `WorkSystem`(테이블 `work_system`)으로 한다.
- 상태와 우선순위는 문자열로 저장한다. `status`는 TODO, IN_PROGRESS, DONE이고 `priority`는 HIGH, MEDIUM, LOW다.
- 보관 처리는 `archived_at`으로 한다. 비어 있으면 활성이고 값이 있으면 보관한 시각이다. 프로젝트와 업무에 적용한다.
- 연관관계는 모두 지연 로딩(LAZY)으로 하고, 다대일 단방향을 기본으로 한다.
- `task.member_id`는 프로젝트의 주인과 일치하는지 서비스에서 검증한다.

| 테이블 | 컬럼 | 타입 | 제약·설명 |
| --- | --- | --- | --- |
| `member` | `email` | VARCHAR(100) | NOT NULL, UNIQUE |
| `member` | `password` | VARCHAR(255) | NOT NULL, 암호화한 값만 저장 |
| `member` | `nickname` | VARCHAR(30) | NOT NULL |
| `project` | `member_id` | BIGINT | NOT NULL, FK → member |
| `project` | `name` | VARCHAR(100) | NOT NULL |
| `project` | `description` | TEXT | NULL 허용 |
| `project` | `start_date`, `end_date` | DATE | NULL 허용 |
| `project` | `archived_at` | TIMESTAMP | NULL이면 활성 |
| `task` | `member_id` | BIGINT | NOT NULL, FK → member |
| `task` | `project_id` | BIGINT | NOT NULL, FK → project |
| `task` | `title` | VARCHAR(200) | NOT NULL |
| `task` | `description` | TEXT | NULL 허용 |
| `task` | `status` | VARCHAR(20) | NOT NULL, 기본값 TODO |
| `task` | `priority` | VARCHAR(10) | NOT NULL, 기본값 MEDIUM |
| `task` | `due_date` | DATE | NULL 허용 |
| `task` | `completed_at` | TIMESTAMP | NULL 허용, 완료로 바꿀 때 기록 |
| `task` | `archived_at` | TIMESTAMP | NULL이면 활성 |
| `task_result` | `task_id` | BIGINT | NOT NULL, FK → task |
| `task_result` | `metric_name` | VARCHAR(100) | NOT NULL, 지표명 |
| `task_result` | `before_value` | VARCHAR(200) | NULL 허용, 개선 전 |
| `task_result` | `after_value` | VARCHAR(200) | NOT NULL, 개선 후 |
| `tag` | `member_id` | BIGINT | NOT NULL, FK → member |
| `tag` | `name` | VARCHAR(50) | NOT NULL, (member_id, name) UNIQUE |
| `task_tag` | `task_id` | BIGINT | NOT NULL, FK → task |
| `task_tag` | `tag_id` | BIGINT | NOT NULL, FK → tag, (task_id, tag_id) UNIQUE |
| `work_system` | `member_id` | BIGINT | NOT NULL, FK → member |
| `work_system` | `name` | VARCHAR(100) | NOT NULL, (member_id, name) UNIQUE |
| `work_system` | `description` | TEXT | NULL 허용 |
| `task_work_system` | `task_id` | BIGINT | NOT NULL, FK → task |
| `task_work_system` | `work_system_id` | BIGINT | NOT NULL, FK → work_system, (task_id, work_system_id) UNIQUE |
| `daily_log` | `member_id` | BIGINT | NOT NULL, FK → member |
| `daily_log` | `log_date` | DATE | NOT NULL, (member_id, log_date) UNIQUE |
| `daily_log` | `summary` | TEXT | NULL 허용, 하루 회고 |
| `task_log` | `daily_log_id` | BIGINT | NOT NULL, FK → daily_log |
| `task_log` | `task_id` | BIGINT | NOT NULL, FK → task |
| `task_log` | `content` | TEXT | NOT NULL, 겪은 문제와 해결 방법 |
| `task_log` | `spent_minutes` | INTEGER | NULL 허용, 0 이상, 소요 시간(분) |

`task_log`에는 유니크 제약을 걸지 않는다. 같은 날 같은 업무에 진행 메모를 여러 개 남길 수 있고, 소요 시간은 일지 화면에서 업무별로 합산한다.

**인덱스**

- `task (member_id, status, due_date)`: 내 업무를 상태와 마감일로 조회할 때 쓴다.
- `task (project_id)`: 프로젝트별 업무 조회에 쓴다.
- `task_log (daily_log_id)`, `task_log (task_id)`: 날짜별, 업무별 진행 메모 조회에 쓴다.
- `task_tag (tag_id)`, `task_work_system (work_system_id)`: 태그별, 시스템별 업무 조회에 쓴다.
- 유니크 제약이 걸린 컬럼은 인덱스가 자동으로 생기므로 따로 만들지 않는다.

## 진행 방식과 일정

백엔드와 React를 기능 단위로 함께 만들고, 가장 어려운 인증부터 시작해 약 2~3주를 예상한다. 하루 4~5시간 이상 쓸 수 있다는 전제이며, 태그·성과·보관 처리가 더해져 처음 계획보다 길어졌다.

| 순서 | 작업 | 기간 |
| --- | --- | --- |
| 0 | 프로젝트 세팅(Spring, React), DB 생성, 엔티티·ERD 설계 | 1일 |
| 1 | 회원가입/로그인(JWT)과 로그인 화면, CORS, 토큰 저장 방식 | 2~3일 |
| 2 | 프로젝트·업무·태그·성과·시스템 CRUD와 화면 | 4~5일 |
| 3 | 일일 기록과 업무별 진행 메모, 화면 | 2~3일 |
| 4 | 날짜별·업무별·상태별·시스템별 조회, 페이징, N+1 개선과 화면 | 2일 |
| 5 | 테스트 코드, Swagger 정리, README | 1~2일 |

**인증을 먼저 하는 이유**: 모든 기능이 "내 데이터만 조회"에 의존하므로, 나중에 붙이면 전부 수정해야 한다.

**진행 규칙**

- 각 기능은 API를 먼저 완성하고 Swagger로 검증한 뒤 화면을 붙인다.
- 에러가 나면 브라우저 개발자도구의 Network 탭에서 요청과 응답부터 확인해 프론트와 백엔드 문제를 가른다.
- 화면은 기능 위주로만 만들고 디자인은 마지막에 다듬는다.
- 백엔드 핵심(엔티티, 서비스, Security 설정)은 직접 작성하고 리뷰를 받는다. React 화면은 초안을 더 많이 받아도 된다.
- 시간이 밀리면 태그와 성과 화면을 뒤로 미룰 수 있도록 순서를 유연하게 둔다.

## API 목록

API는 7개 리소스, 32개 엔드포인트로 구성하고, 인증은 Access Token 하나로 처리한다.

**공통 규칙**

- 경로는 자원 중심으로 잡는다. 조회는 GET, 등록은 POST, 수정은 PATCH, 삭제는 DELETE이며, 업무·프로젝트의 DELETE는 보관 처리로 동작한다.
- 로그인 후 받은 Access Token을 `Authorization: Bearer` 헤더로 보낸다. 가입과 로그인을 제외한 모든 API가 대상이다. 토큰이 만료되면 다시 로그인하고, Refresh Token은 첫 버전 이후에 붙인다.
- 업무를 등록·수정할 때 `projectId`, `tagIds`, `systemIds`를 함께 받는다. TaskTag와 TaskWorkSystem은 서버 내부에서만 다룬다.
- 일일 기록은 날짜(`2026-09-21` 형식)로 접근한다. 회원당 하루에 하나라서 날짜가 곧 식별자다.
- 목록 조회는 `page`, `size`, `sort`를 받고, 업무 목록은 상태·우선순위·프로젝트·시스템·태그·마감일 범위·키워드 필터를 조합할 수 있다.
- 에러 응답은 `code`와 `message`를 담는 하나의 형식으로 통일한다. 검증 실패는 400, 미인증은 401, 다른 회원의 데이터 접근은 403, 없는 데이터는 404, 중복은 409로 응답한다.

| 구분 | 메서드·경로 | 설명 |
| --- | --- | --- |
| 인증 | POST /api/auth/signup | 회원가입 |
| 인증 | POST /api/auth/login | 로그인, Access Token 발급 |
| 인증 | GET /api/members/me | 내 정보 조회 |
| 프로젝트 | GET /api/projects | 프로젝트 목록(보관 제외) |
| 프로젝트 | POST /api/projects | 프로젝트 등록 |
| 프로젝트 | GET /api/projects/{id} | 프로젝트 상세 |
| 프로젝트 | PATCH /api/projects/{id} | 프로젝트 수정 |
| 프로젝트 | DELETE /api/projects/{id} | 보관 처리(하위 업무도 목록에서 숨김) |
| 업무 시스템 | GET /api/systems | 시스템 목록 |
| 업무 시스템 | POST /api/systems | 시스템 등록 |
| 업무 시스템 | PATCH /api/systems/{id} | 시스템 수정 |
| 업무 시스템 | DELETE /api/systems/{id} | 시스템 삭제 |
| 태그 | GET /api/tags | 태그 목록 |
| 태그 | POST /api/tags | 태그 등록 |
| 태그 | PATCH /api/tags/{id} | 태그 수정 |
| 태그 | DELETE /api/tags/{id} | 태그 삭제 |
| 업무 | GET /api/tasks | 업무 목록(필터, 페이징, 정렬) |
| 업무 | POST /api/tasks | 업무 등록(프로젝트, 태그, 시스템 포함) |
| 업무 | GET /api/tasks/{id} | 업무 상세(태그, 시스템, 성과 항목, 진행 메모 포함) |
| 업무 | PATCH /api/tasks/{id} | 업무 수정 |
| 업무 | PATCH /api/tasks/{id}/status | 상태 변경(완료로 바꾸면 완료일 기록) |
| 업무 | DELETE /api/tasks/{id} | 보관 처리 |
| 성과 항목 | POST /api/tasks/{taskId}/results | 성과 항목 추가(지표명, 개선 전, 개선 후) |
| 성과 항목 | PATCH /api/tasks/{taskId}/results/{resultId} | 성과 항목 수정 |
| 성과 항목 | DELETE /api/tasks/{taskId}/results/{resultId} | 성과 항목 삭제 |
| 일일 기록 | GET /api/daily-logs | 기간별 일일 기록 목록(from, to, 페이징) |
| 일일 기록 | GET /api/daily-logs/{date} | 하루 일지 조회(회고와 그날의 진행 메모) |
| 일일 기록 | PUT /api/daily-logs/{date} | 하루 회고 저장(없으면 생성, 있으면 수정) |
| 진행 메모 | POST /api/daily-logs/{date}/task-logs | 진행 메모 등록(업무, 내용, 소요 시간) |
| 진행 메모 | PATCH /api/task-logs/{id} | 진행 메모 수정 |
| 진행 메모 | DELETE /api/task-logs/{id} | 진행 메모 삭제 |
| 진행 메모 | GET /api/tasks/{taskId}/task-logs | 업무별 진행 메모 조회(시간순) |

**아직 정하지 않은 것**

- 태그·시스템·성과 항목·진행 메모를 삭제할 때의 처리(완전 삭제 또는 보관, 이미 연결된 업무가 있을 때의 동작)
- 조건 조합 조회의 구현 방식(QueryDSL, Specification, JPQL 중 선택)
- 토큰 저장 위치(localStorage 또는 httpOnly 쿠키)와 만료 시간

## 주의사항과 다음 단계

회사 기밀은 기록하지 않고, 다음 단계로 ERD 확정과 API 세부 설계를 진행한다.

**주의사항**

- 고객 정보, 내부 시스템 주소, 비공개 수치, 회사 문서와 코드는 적지 않는다. 기술, 문제, 해결, 수치를 본인 말로 요약하는 수준으로만 쓴다.
- 데이터는 로컬 PostgreSQL에만 두고 공개 배포하지 않는다. 포트폴리오 시연에는 가상 데이터를 쓰고, 업무 시스템 이름도 가상 이름이나 일반화한 이름으로 바꾼다.
- 회사에서 만들거나 쓰는 시스템의 코드·화면·양식은 이 프로젝트에 가져오지 않는다.

**다음 단계**

1. API 세부 설계: 위 목록의 요청·응답 필드와 에러 코드
2. ERD 확정: 위 엔티티 초안의 필드 타입과 제약 조건
3. 프로젝트 세팅과 인증 기능부터 구현 시작

---

## 진행 상황 (이 섹션은 작업하며 계속 갱신)

### 진행 방식
- 백엔드 핵심(엔티티, 서비스, Security 설정)은 사용자가 직접 코드를 치고, Claude는 소스와 설명을 먼저 보여준 뒤 사용자가 "다 썼어"라고 하면 파일을 읽고 `./mvnw -q compile`로 컴파일을 확인해 리뷰한다. (사용자가 직접 "너가 수정해줘"라고 요청하면 그때는 Claude가 Edit/Write로 직접 고친다.)
- 수정·삭제·추가(새 파일·의존성·설정 포함)는 먼저 무엇을 어떻게 바꿀지 말하고, 사용자가 하라고 해야 실행한다. 읽기·조회는 바로 해도 된다.
- 앱은 STS(Spring Tools for Eclipse)에서 실행한다. `DB_PASSWORD`, `JWT_SECRET`은 STS 실행 설정(Run Configurations → Environment)에 있고 시스템 환경변수에는 없다. 콘솔 로그는 STS 안에만 보이므로, 동작 확인은 8080 포트 프로세스 시작 시각과 클래스 컴파일 시각을 비교해 재시작 여부를 먼저 확인한 뒤 curl로 요청을 보내는 방식으로 한다.
- Lombok은 쓰지 않는다. DTO는 Java record로 만든다.
- 패키지는 도메인별로 구성한다(`common`, `config`, `member`, `auth`, `security`, 하위 `dto`, `common/exception`).
- `member` 테이블은 사용자가 SQL로 직접 만들었고, `application.yml`의 `ddl-auto`는 `validate`다(엔티티와 테이블이 맞는지 검사만 하고 자동으로 만들지 않음).
- 이 프로젝트는 `spring-boot-starter-webmvc`를 쓰는 Spring Boot 4 구성이라 **Jackson(`ObjectMapper`)이 기본 포함되지 않는다.** 필터 단계 등 Jackson이 필요해 보이는 곳에서는 라이브러리를 추가하기보다 간단한 문자열 조립으로 대체했다(예: `JwtAuthenticationEntryPoint`).
- GitHub 저장소: https://github.com/jh-st97/work-log (Public). STS(EGit)에서 Team → Commit, Team → Push to Upstream(원격 `origin`으로 고정 저장됨)으로 관리한다. 원격 연결 후 최초 1회는 "Push Branch main" 화면에서 URI·계정·토큰을 입력해 `origin`으로 저장해 둬야 다음부터 재입력 없이 push된다.
- **저장소 이름을 `work-log`에서 `work-log-backend`로 바꿀 예정(2026-09-22 결정, 아직 실행 안 함).** 바꾸면 이 문서의 주소와 로컬 STS의 원격 URL도 같이 고쳐야 한다.
- 프론트엔드 프로젝트가 별도로 생겼다: `C:\workspace\side-projects\work-log\work-log-frontend` (React + TypeScript + Vite). 자세한 내용은 그 프로젝트의 CLAUDE.md 참고. GitHub: https://github.com/jh-st97/work-log-frontend

### 완료 (2026-09-21~22, 기획서 1단계 대부분)
- `BaseEntity`(id, createdAt, updatedAt + JPA Auditing), `Member` 엔티티, `MemberRepository`(existsByEmail, findByEmail)
- `SecurityConfig`: signup·login만 permitAll, 나머지는 401, STATELESS, csrf 비활성, BCrypt `PasswordEncoder` Bean
- 회원가입 API `POST /api/auth/signup`: `SignupRequest`(record, 검증), 이메일 중복 시 409, 응답 `SignupResponse`(id, email, nickname만, 비밀번호 제외)
- 공통 에러 응답: `ErrorCode`(enum), `BusinessException`, `ErrorResponse`(code, message), `GlobalExceptionHandler`(`@RestControllerAdvice`)
- JWT 발급·검증: `JwtTokenProvider`(`jjwt` 0.12.6, HS256, `jwt.secret`은 `JWT_SECRET` 환경변수)
- 로그인 API `POST /api/auth/login`: `LoginRequest`/`LoginResponse`(record), 이메일 없음과 비밀번호 틀림을 구분 없이 401 `LOGIN_FAILED`로 응답(보안 관례)
- `JwtAuthenticationFilter`(`OncePerRequestFilter` 상속, `Authorization: Bearer` 헤더 해석 후 `SecurityContextHolder`에 회원 번호 등록), `SecurityConfig`에 `addFilterBefore`로 등록
- `GET /api/members/me`: `@AuthenticationPrincipal Long memberId`로 인증된 회원 조회, 없으면 404 `MEMBER_NOT_FOUND`
- `JwtAuthenticationEntryPoint`: 미인증(401) 응답도 `{code, message}` JSON 형식으로 통일 (Jackson 없이 문자열 직접 조립)
- 실제 요청으로 회원가입(201/409/400), 로그인(200/401), 인증 필터(토큰 없음·가짜 토큰 401, 유효한 토큰 200) 전부 확인 완료
- DB에 시험용 회원 `test@example.com`(id 1, 비밀번호 `test-password-1234`)이 남아 있음
- GitHub에 push 완료 (커밋: Initial commit, JWT 로그인 인증 구현 ×2)

### 1단계 완료
CORS까지 포함해 기획서 1단계(회원가입, 로그인, JWT 인증, 에러 응답 통일, CORS)가 전부 끝났다. `SecurityConfig`에 `.cors(cors -> cors.configurationSource(corsConfigurationSource()))` 추가, `CorsConfigurationSource` Bean에서 `http://localhost:5173`(Vite 기본 포트) 허용, 메서드는 `GET/POST/PATCH/PUT/DELETE`(PATCH 포함 필수), 헤더는 `*`. curl로는 브라우저 CORS 차단을 재현할 수 없어 preflight(OPTIONS + Origin 헤더)를 흉내 내 확인했다. 실제 검증은 React가 생긴 뒤에.

### 완료 (2026-09-22, 기획서 2단계 진행 중)
- `Project` 엔티티(`@ManyToOne Member`, LAZY), `ProjectRepository`(findByMemberIdAndArchivedAtIsNull, findByIdAndMemberId), `ProjectRequest`/`ProjectResponse`(record)
- `ProjectService`/`ProjectController`: 목록(보관 제외)/등록/상세/수정(더티 체킹, save 호출 안 함)/보관 처리(DELETE가 실제로는 archive) — `findMyProject`로 소유권 확인 공통화(내 것 아니면 403)
- `Tag` 엔티티(보관 개념 없음, `rename()`만 있음), `TagRepository`(findByMemberId, findByIdAndMemberId, existsByMemberIdAndName, existsByMemberIdAndNameAndIdNot), `TagRequest`/`TagResponse`
- `TagService`/`TagController`: 목록/등록(이름 중복 409)/수정(자기 자신 제외 중복 확인)/삭제(진짜 DELETE, 보관 아님) — `findByIdAndMemberId`로 소유권까지 한 번에 확인(TAG_ACCESS_DENIED는 정의만 해두고 미사용)
- `ErrorCode`에 `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED`, `TAG_NOT_FOUND`, `TAG_ACCESS_DENIED`, `TAG_DUPLICATED` 추가
- `WorkSystem` 엔티티(Tag와 동일 패턴 + description 필드), `WorkSystemRepository`, `WorkSystemRequest`/`WorkSystemResponse`, `WorkSystemService`/`WorkSystemController` — `GET/POST /api/systems`, `PATCH/DELETE /api/systems/{id}`
- `ErrorCode`에 `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED`, `TAG_NOT_FOUND`, `TAG_ACCESS_DENIED`, `TAG_DUPLICATED`, `WORK_SYSTEM_NOT_FOUND`, `WORK_SYSTEM_ACCESS_DENIED`, `WORK_SYSTEM_DUPLICATED` 추가
- 프로젝트·태그·업무 시스템 CRUD 전부 실제 요청으로 검증 완료(등록/중복/수정/삭제/404/403/400)
- `project`, `tag`, `work_system` 테이블은 사용자가 SQL로 직접 생성(`UNIQUE` 제약은 별도 인덱스 생성 안 함 — 기획서 규칙)
- **[중요, 테스트 방법 주의] curl로 한글이 포함된 body를 Windows에서 명령줄 인자(`-d '...'`)로 직접 넘기면 인코딩이 깨져서 요청이 손상되고, 그 결과로 서버가 401(`UNAUTHORIZED`)을 반환한다.** 처음엔 DevTools 재시작이나 원인 불명의 인증 버그로 오해했지만, 실제로는 **테스트 스크립트 문제였고 앱 코드는 처음부터 정상**이었다. 영어(ASCII)만 담긴 body는 항상 성공했고, 한글 body만 실패했다. 확인 방법: JSON body를 UTF-8 파일로 먼저 써 두고(Write 도구 사용) `curl --data-binary "@파일경로"`로 보낸다. **한글이 포함된 요청을 curl로 테스트할 때는 항상 이 파일 방식을 쓴다.**
- DB에 시험용 데이터가 남아 있음: `member` id 1(test@example.com), `work_system`에 `Payment System`(id 1), `test123`(id 2) 등. 태그·프로젝트도 시험 데이터가 조금 섞여 있을 수 있어 다음 세션에서 필요하면 정리.

### 다음 단계 (기획서 2단계, 남은 것)
1. **Task(업무)** — Project, Tag, WorkSystem이 모두 준비되어 이제 만들 수 있음. `TaskTag`, `TaskWorkSystem` 중간 엔티티 필요. `task.member_id`가 프로젝트 주인과 일치하는지 서비스에서 검증해야 함(기획서 규칙). 상태 변경(`PATCH /api/tasks/{id}/status`)은 완료로 바꾸면 `completed_at` 기록.
2. **TaskResult(성과 항목)** — Task 하위.
3. **DailyLog, TaskLog(일일 기록·진행 메모)** — 기획서 3단계.

### 정한 것 (기획서에는 없던, 대화 중 결정)
- 토큰 저장 위치: **localStorage**
- 토큰 만료 시간: **2시간** (처음엔 24시간으로 정했다가 너무 길다는 판단에 2시간으로 변경)
- 회원가입 비밀번호 검증: 8자 이상 72자 이하 (BCrypt 72바이트 한계 고려)
- 에러 응답의 `code` 값은 `ErrorCode` enum 이름을 그대로 사용
- JWT 서명 방식: HS256, `io.jsonwebtoken`(jjwt) 라이브러리 0.12.6
- CORS 허용 origin: `http://localhost:5173`(React를 다른 포트로 만들면 그때 수정)
- 엔티티 수정 메서드 이름은 의미에 맞게: `Project.update(...)`(여러 필드), `Tag.rename(...)`(단일 필드), `Project.archive()`(보관 처리 전용)

### 아직 안 정한 것
- 이메일 대소문자 구분 여부
- 태그·시스템·성과 항목·진행 메모 삭제 시 처리 방식 (Task/TaskTag가 아직 없어서 "이미 연결된 업무가 있을 때" 케이스는 아직 안 생김)
- 조건 조합 조회 구현 방식(QueryDSL vs Specification vs JPQL)
