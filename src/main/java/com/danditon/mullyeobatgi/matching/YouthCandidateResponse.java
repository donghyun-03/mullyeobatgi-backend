package com.danditon.mullyeobatgi.matching;

import com.danditon.mullyeobatgi.offer.OfferStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "사장님이 보는 청년 후보")
public record YouthCandidateResponse(
		@Schema(description = "청년 사용자 id. 제안 보낼 때 youthId 로 써요", example = "2")
		Long youthId,
		@Schema(example = "이도윤")
		String name,
		@Schema(example = "29")
		Integer age,
		@Schema(example = "경력 1~3년")
		String experienceLabel,
		@Schema(example = "한식 주방 2년")
		String experienceDetail,
		@Schema(description = "준비된 자금 (만 원)", example = "3000")
		int fundsReady,
		@Schema(description = "적합도 (100점 만점)", example = "95")
		int score,
		List<MatchScore.Part> breakdown,
		List<MatchScore.Reason> reasons,
		@Schema(description = "AI 한마디. AI 키가 있을 때 상위 3명에게만, 없으면 null")
		String aiComment,
		@Schema(description = "이 청년에게 보낸 가장 최근 제안 id. 없으면 null")
		Long offerId,
		@Schema(description = "그 제안의 상태. 없으면 null")
		OfferStatus offerStatus) {
}
