package com.danditon.mullyeobatgi.checklist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChecklistAttachmentRepository extends JpaRepository<ChecklistAttachment, Long> {

	@Query("""
			select new com.danditon.mullyeobatgi.checklist.ChecklistAttachmentInfo(
				a.id, a.item.id, a.uploadedBy, a.fileName, a.contentType, a.sizeBytes, a.createdAt)
			from ChecklistAttachment a
			where a.item.succession.id = :successionId
			order by a.createdAt""")
	List<ChecklistAttachmentInfo> findInfoBySuccessionId(Long successionId);

}
