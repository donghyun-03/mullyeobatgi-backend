-- 와이어프레임 반영

-- 청년 등록 화면에서 새벽 근무 여부를 묻지 않을 수 있다. 비어 있으면 '아직 모름'
ALTER TABLE youth_profiles MODIFY COLUMN early_morning_ok BOOLEAN NULL;

-- ⑥ 계획을 고칠 때 바뀌기 전 내용을 남겨 "주 6일 → 주 5일"처럼 보여 준다
ALTER TABLE plan_sections ADD COLUMN previous_content TEXT;

-- ⑤ 체크리스트 서류(대출 승인서 등). 무료 서버는 디스크가 임시라 DB에 저장한다
CREATE TABLE checklist_attachments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id        BIGINT       NOT NULL,
    uploaded_by    VARCHAR(20)  NOT NULL,
    file_name      VARCHAR(200) NOT NULL,
    content_type   VARCHAR(100) NOT NULL,
    size_bytes     BIGINT       NOT NULL,
    data           LONGBLOB     NOT NULL,
    created_at     DATETIME(6)  NOT NULL,
    CONSTRAINT fk_checklist_attachments_item FOREIGN KEY (item_id) REFERENCES checklist_items (id)
);
