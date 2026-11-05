CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    role        VARCHAR(20),
    kakao_id    BIGINT UNIQUE,
    created_at  DATETIME(6)  NOT NULL
);

CREATE TABLE auth_tokens (
    token       VARCHAR(64)  PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    expires_at  DATETIME(6)  NOT NULL,
    CONSTRAINT fk_auth_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ① 사장님 가게 정보. 금액은 만 원 단위
CREATE TABLE shops (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id           BIGINT       NOT NULL UNIQUE,
    name               VARCHAR(100) NOT NULL,
    category           VARCHAR(30)  NOT NULL,
    main_menu          VARCHAR(50),
    district           VARCHAR(20)  NOT NULL,
    address            VARCHAR(200),
    opened_year        INT,
    monthly_sales      INT,
    asking_price       INT          NOT NULL,
    handover_items     VARCHAR(200),
    retire_date        DATE         NOT NULL,
    apprentice_months  INT          NOT NULL,
    work_hours         VARCHAR(50),
    early_morning      BOOLEAN      NOT NULL,
    message            VARCHAR(500),
    created_at         DATETIME(6)  NOT NULL,
    updated_at         DATETIME(6)  NOT NULL,
    CONSTRAINT fk_shops_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

-- ① 청년 정보. 금액은 만 원 단위
CREATE TABLE youth_profiles (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id               BIGINT       NOT NULL UNIQUE,
    age                   INT,
    experience            VARCHAR(20)  NOT NULL,
    experience_detail     VARCHAR(100),
    funds_ready           INT          NOT NULL,
    loan_planned          BOOLEAN      NOT NULL,
    loan_amount           INT          NOT NULL,
    preferred_categories  VARCHAR(200),
    preferred_districts   VARCHAR(200),
    early_morning_ok      BOOLEAN      NOT NULL,
    intro                 VARCHAR(500),
    created_at            DATETIME(6)  NOT NULL,
    updated_at            DATETIME(6)  NOT NULL,
    CONSTRAINT fk_youth_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ③ 제안
CREATE TABLE offers (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id       BIGINT       NOT NULL,
    youth_id      BIGINT       NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    price         INT          NOT NULL,
    message       VARCHAR(500),
    match_score   INT          NOT NULL,
    expires_at    DATETIME(6)  NOT NULL,
    responded_at  DATETIME(6),
    created_at    DATETIME(6)  NOT NULL,
    CONSTRAINT fk_offers_shop FOREIGN KEY (shop_id) REFERENCES shops (id),
    CONSTRAINT fk_offers_youth FOREIGN KEY (youth_id) REFERENCES users (id)
);

CREATE TABLE successions (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    offer_id           BIGINT       NOT NULL UNIQUE,
    shop_id            BIGINT       NOT NULL,
    owner_id           BIGINT       NOT NULL,
    youth_id           BIGINT       NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    price              INT          NOT NULL,
    accepted_date      DATE         NOT NULL,
    retire_date        DATE         NOT NULL,
    apprentice_months  INT          NOT NULL,
    started_at         DATETIME(6)  NOT NULL,
    completed_at       DATETIME(6),
    CONSTRAINT fk_successions_offer FOREIGN KEY (offer_id) REFERENCES offers (id),
    CONSTRAINT fk_successions_shop FOREIGN KEY (shop_id) REFERENCES shops (id),
    CONSTRAINT fk_successions_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT fk_successions_youth FOREIGN KEY (youth_id) REFERENCES users (id)
);

-- ④ 가일정
CREATE TABLE schedule_items (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    succession_id  BIGINT       NOT NULL,
    seq            INT          NOT NULL,
    code           VARCHAR(30)  NOT NULL,
    title          VARCHAR(100) NOT NULL,
    note           VARCHAR(200),
    start_date     DATE,
    end_date       DATE         NOT NULL,
    CONSTRAINT fk_schedule_items_succession FOREIGN KEY (succession_id) REFERENCES successions (id)
);

CREATE TABLE schedule_change_requests (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    succession_id    BIGINT       NOT NULL,
    requested_by     VARCHAR(20)  NOT NULL,
    new_retire_date  DATE         NOT NULL,
    reason           VARCHAR(300),
    status           VARCHAR(20)  NOT NULL,
    created_at       DATETIME(6)  NOT NULL,
    decided_at       DATETIME(6),
    CONSTRAINT fk_schedule_change_requests_succession FOREIGN KEY (succession_id) REFERENCES successions (id)
);

-- ⑤ 체크리스트
CREATE TABLE checklist_items (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    succession_id       BIGINT       NOT NULL,
    seq                 INT          NOT NULL,
    code                VARCHAR(30)  NOT NULL,
    title               VARCHAR(100) NOT NULL,
    description         VARCHAR(300),
    owner_confirmed_at  DATETIME(6),
    owner_note          VARCHAR(300),
    youth_confirmed_at  DATETIME(6),
    youth_note          VARCHAR(300),
    CONSTRAINT fk_checklist_items_succession FOREIGN KEY (succession_id) REFERENCES successions (id)
);

-- ⑥ AI 승계 계획
CREATE TABLE plans (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    succession_id    BIGINT       NOT NULL UNIQUE,
    version          INT          NOT NULL,
    generated_by     VARCHAR(20)  NOT NULL,
    owner_agreed_at  DATETIME(6),
    youth_agreed_at  DATETIME(6),
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    CONSTRAINT fk_plans_succession FOREIGN KEY (succession_id) REFERENCES successions (id)
);

CREATE TABLE plan_sections (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id    BIGINT       NOT NULL,
    seq        INT          NOT NULL,
    code       VARCHAR(30)  NOT NULL,
    title      VARCHAR(100) NOT NULL,
    content    TEXT         NOT NULL,
    edited_by  VARCHAR(20),
    edited_at  DATETIME(6),
    CONSTRAINT fk_plan_sections_plan FOREIGN KEY (plan_id) REFERENCES plans (id)
);

-- ⑦ 수습 로드맵
CREATE TABLE roadmap_stages (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    succession_id  BIGINT       NOT NULL,
    seq            INT          NOT NULL,
    title          VARCHAR(100) NOT NULL,
    description    VARCHAR(300),
    start_date     DATE         NOT NULL,
    end_date       DATE         NOT NULL,
    CONSTRAINT fk_roadmap_stages_succession FOREIGN KEY (succession_id) REFERENCES successions (id)
);

CREATE TABLE roadmap_tasks (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    stage_id      BIGINT       NOT NULL,
    seq           INT          NOT NULL,
    title         VARCHAR(100) NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    memo          VARCHAR(300),
    feedback      VARCHAR(300),
    requested_at  DATETIME(6),
    decided_at    DATETIME(6),
    CONSTRAINT fk_roadmap_tasks_stage FOREIGN KEY (stage_id) REFERENCES roadmap_stages (id)
);

-- ⑧ 납입 관리. 금액은 만 원 단위
CREATE TABLE payment_installments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    succession_id  BIGINT       NOT NULL,
    seq            INT          NOT NULL,
    label          VARCHAR(50)  NOT NULL,
    amount         INT          NOT NULL,
    due_date       DATE         NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    paid_date      DATE,
    memo           VARCHAR(300),
    reported_at    DATETIME(6),
    confirmed_at   DATETIME(6),
    CONSTRAINT fk_payment_installments_succession FOREIGN KEY (succession_id) REFERENCES successions (id)
);
