package com.danditon.mullyeobatgi.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JosaTest {

	@Test
	void 받침에_맞춰_조사를_붙인다() {
		assertThat(Josa.eulReul("레시피")).isEqualTo("레시피를");
		assertThat(Josa.eulReul("국밥")).isEqualTo("국밥을");
		assertThat(Josa.iGa("카페")).isEqualTo("카페가");
		assertThat(Josa.iGa("치킨")).isEqualTo("치킨이");
		assertThat(Josa.waGwa("칼국수")).isEqualTo("칼국수와");
		assertThat(Josa.waGwa("국밥")).isEqualTo("국밥과");
		assertThat(Josa.ieyo("중구")).isEqualTo("중구예요");
		assertThat(Josa.ieyo("수성구, 중구")).isEqualTo("수성구, 중구예요");
	}

}
