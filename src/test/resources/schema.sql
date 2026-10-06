-- 테스트용 DB(work_log_test)의 테이블을 만드는 스크립트.
-- 테스트가 시작될 때마다 전부 지우고 다시 만들어서, 항상 깨끗한 상태에서 시작한다.
-- 기획서 ERD(10개 테이블)와 같은 제약(유니크, FK, NOT NULL)을 일부러 그대로 둔다 —
-- 실제 DB와 같은 제약이 있어야 "중복 키" 같은 문제를 테스트가 잡을 수 있다.

-- 자식 테이블부터 지운다(FK 때문에 순서가 중요)
DROP TABLE IF EXISTS task_log;
DROP TABLE IF EXISTS daily_log;
DROP TABLE IF EXISTS task_work_system;
DROP TABLE IF EXISTS task_tag;
DROP TABLE IF EXISTS task_result;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS work_system;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS member;

CREATE TABLE member (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE project (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES member(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    archived_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE tag (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES member(id),
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (member_id, name)
);

CREATE TABLE work_system (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES member(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (member_id, name)
);

CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES member(id),
    project_id BIGINT NOT NULL REFERENCES project(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    due_date DATE,
    completed_at TIMESTAMP,
    archived_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_task_member_status_due ON task (member_id, status, due_date);
CREATE INDEX idx_task_project ON task (project_id);

CREATE TABLE task_result (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES task(id),
    metric_name VARCHAR(100) NOT NULL,
    before_value VARCHAR(200),
    after_value VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE task_tag (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES task(id),
    tag_id BIGINT NOT NULL REFERENCES tag(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (task_id, tag_id)
);
CREATE INDEX idx_task_tag_tag ON task_tag (tag_id);

CREATE TABLE task_work_system (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES task(id),
    work_system_id BIGINT NOT NULL REFERENCES work_system(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (task_id, work_system_id)
);
CREATE INDEX idx_task_work_system_ws ON task_work_system (work_system_id);

CREATE TABLE daily_log (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES member(id),
    log_date DATE NOT NULL,
    summary TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (member_id, log_date)
);

CREATE TABLE task_log (
    id BIGSERIAL PRIMARY KEY,
    daily_log_id BIGINT NOT NULL REFERENCES daily_log(id),
    task_id BIGINT NOT NULL REFERENCES task(id),
    content TEXT NOT NULL,
    spent_minutes INTEGER CHECK (spent_minutes >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_task_log_daily ON task_log (daily_log_id);
CREATE INDEX idx_task_log_task ON task_log (task_id);
