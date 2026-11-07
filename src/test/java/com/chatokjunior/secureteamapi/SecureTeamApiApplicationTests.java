package com.chatokjunior.secureteamapi;

import com.chatokjunior.secureteamapi.config.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfig.class)
class SecureTeamApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
