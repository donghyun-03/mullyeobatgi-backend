package com.danditon.mullyeobatgi.roadmap;

import com.danditon.mullyeobatgi.common.Josa;
import com.danditon.mullyeobatgi.succession.Succession;
import com.danditon.mullyeobatgi.succession.Timeline;

import java.util.List;

/**
 * ⑦ 기본 수습 로드맵 3단계. 업종과 상관없이 쓸 수 있도록 대표 메뉴 이름만 넣는다.
 */
public final class RoadmapFactory {

	static final String[] TITLES = {"주방 익히기", "혼자 만들기", "가게 운영"};

	private RoadmapFactory() {
	}

	public static List<RoadmapStage> create(Succession succession, Timeline timeline) {
		String menu = succession.getShop().getMainMenu() == null || succession.getShop().getMainMenu().isBlank()
				? "대표 메뉴" : succession.getShop().getMainMenu();
		List<Timeline.Period> periods = timeline.stages();
		return List.of(
				new RoadmapStage(succession, 1, TITLES[0], "재료 손질, 주방 동선, 거래처와 단골 익히기",
						periods.get(0).start(), periods.get(0).end(),
						List.of("재료 손질과 주방 동선 익히기", "거래처에 인사드리기", "단골 손님 얼굴과 이름 익히기")),
				new RoadmapStage(succession, 2, TITLES[1], Josa.waGwa(menu) + " 밑재료를 혼자 만들기",
						periods.get(1).start(), periods.get(1).end(),
						List.of(menu + " 혼자 만들기", "밑재료 혼자 준비하기", "점심 장사 혼자 보기")),
				new RoadmapStage(succession, 3, TITLES[2], "장부, 거래처 주문, 하루 운영 맡기",
						periods.get(2).start(), periods.get(2).end(),
						List.of("사장님과 장부·정산 같이 보기", "거래처 주문 혼자 하기", "하루 운영 혼자 맡기")));
	}

}
