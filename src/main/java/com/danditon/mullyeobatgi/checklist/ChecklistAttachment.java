package com.danditon.mullyeobatgi.checklist;

import com.danditon.mullyeobatgi.user.Role;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ⑤ 체크리스트 항목에 올린 서류(대출 승인서, 임대차 계약서 등).
 * 무료 서버는 디스크가 임시라 파일을 DB에 저장한다. 목록은 data 없이 조회한다.
 */
@Entity
@Table(name = "checklist_attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistAttachment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "item_id")
	private ChecklistItem item;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role uploadedBy;

	@Column(nullable = false, length = 200)
	private String fileName;

	@Column(nullable = false, length = 100)
	private String contentType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(nullable = false, columnDefinition = "LONGBLOB")
	private byte[] data;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	public ChecklistAttachment(ChecklistItem item, Role uploadedBy, String fileName, String contentType,
			byte[] data, LocalDateTime now) {
		this.item = item;
		this.uploadedBy = uploadedBy;
		this.fileName = fileName;
		this.contentType = contentType;
		this.sizeBytes = data.length;
		this.data = data;
		this.createdAt = now;
	}

}
