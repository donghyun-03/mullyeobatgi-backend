package com.danditon.mullyeobatgi.succession;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TimelinePlannerTest {

	@Test
	void 은퇴_희망일에서_거꾸로_계산한다() {
		Timeline t = TimelinePlanner.plan(LocalDate.of(2026, 11, 6), LocalDate.of(2027, 8, 31), 6);

		assertThat(t.handoverStart()).isEqualTo(LocalDate.of(2027, 7, 1));
		assertThat(t.apprenticeEnd()).isEqualTo(LocalDate.of(2027, 6, 30));
		assertThat(t.apprenticeStart()).isEqualTo(LocalDate.of(2027, 1, 1));
		assertThat(t.planDue()).isEqualTo(LocalDate.of(2026, 12, 20));
		assertThat(t.checklistDue()).isEqualTo(LocalDate.of(2026, 12, 15));
		assertThat(t.warnings()).isEmpty();
	}

	@Test
	void 수습을_세_단계로_나눈다() {
		Timeline t = TimelinePlanner.plan(LocalDate.of(2026, 11, 6), LocalDate.of(2027, 8, 31), 6);

		assertThat(t.stages()).extracting(Timeline.Period::start).containsExactly(
				LocalDate.of(2027, 1, 1), LocalDate.of(2027, 3, 1), LocalDate.of(2027, 5, 1));
		assertThat(t.stages()).extracting(Timeline.Period::end).containsExactly(
				LocalDate.of(2027, 2, 28), LocalDate.of(2027, 4, 30), LocalDate.of(2027, 6, 30));
	}

	@Test
	void 남은_기간이_짧으면_경고한다() {
		Timeline t = TimelinePlanner.plan(LocalDate.of(2026, 11, 6), LocalDate.of(2027, 4, 30), 6);

		assertThat(t.warnings()).isNotEmpty();
	}

	@Test
	void 가장_빠른_은퇴_희망일이면_체크리스트에_일주일이_남는다() {
		LocalDate today = LocalDate.of(2026, 11, 6);
		LocalDate earliest = TimelinePlanner.earliestRetireDate(today, 6);

		assertThat(TimelinePlanner.plan(today, earliest, 6).warnings()).isEmpty();
	}

}
