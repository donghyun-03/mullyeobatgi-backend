package com.danditon.mullyeobatgi.demo;

import com.danditon.mullyeobatgi.shop.Shop;
import com.danditon.mullyeobatgi.shop.ShopRepository;
import com.danditon.mullyeobatgi.shop.ShopRequest;
import com.danditon.mullyeobatgi.user.Role;
import com.danditon.mullyeobatgi.user.User;
import com.danditon.mullyeobatgi.user.UserRepository;
import com.danditon.mullyeobatgi.youth.Experience;
import com.danditon.mullyeobatgi.youth.YouthProfile;
import com.danditon.mullyeobatgi.youth.YouthProfileRepository;
import com.danditon.mullyeobatgi.youth.YouthProfileRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 시연용 예시 데이터. DB가 비어 있을 때만 넣는다. 모든 가게와 사람은 가상의 예시다.
 * 개발용 로그인에서 같은 이름과 역할로 들어가면 이 사용자로 로그인된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.seed", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

	private final UserRepository users;
	private final ShopRepository shops;
	private final YouthProfileRepository youthProfiles;
	private final Clock clock;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (users.count() > 0) {
			return;
		}
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDate today = LocalDate.now(clock);

		User owner = users.save(new User("김순자", Role.OWNER, null, now));
		shops.save(new Shop(owner, new ShopRequest("남문 손칼국수", "한식", "칼국수", "중구", "대구 중구 남산동", 1987, 2100,
				4800, "권리금 · 주방 설비 · 레시피", today.plusDays(300), 6, "새벽 5시~오후 3시", true,
				"국물 맛만 지켜 주면 다 넘겨줄게요. 단골들 얼굴부터 익혀요."), now));

		youth("이도윤", 29, Experience.ONE_TO_3Y, "한식 주방 2년", 3000, true, 1800, List.of("한식", "면 요리"),
				List.of("중구", "남구"), true, "면 요리를 제대로 배우고 싶어요", now);
		youth("박서연", 33, Experience.OVER_3Y, "분식집 운영 4년", 5000, false, 0, List.of("한식", "분식"),
				List.of("수성구"), true, "제 가게를 단단하게 이어 가고 싶어요", now);
		youth("최민재", 26, Experience.UNDER_1Y, "카페 아르바이트", 1500, true, 2000, List.of("카페", "한식"),
				List.of("중구"), false, null, now);
		log.info("예시 데이터를 넣었어요: 사장님 1명(김순자), 청년 3명(이도윤, 박서연, 최민재)");
	}

	private void youth(String name, int age, Experience experience, String detail, int funds, boolean loan,
			int loanAmount, List<String> categories, List<String> districts, boolean earlyOk, String intro,
			LocalDateTime now) {
		User user = users.save(new User(name, Role.YOUTH, null, now));
		youthProfiles.save(new YouthProfile(user, new YouthProfileRequest(age, experience, detail, funds, loan,
				loanAmount, categories, districts, earlyOk, intro), now));
	}

}
