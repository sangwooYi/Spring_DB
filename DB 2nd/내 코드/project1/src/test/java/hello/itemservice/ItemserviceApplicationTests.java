package hello.itemservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Test 쪽에 application.properties 가 있으면 그게 우선,
// 만약 없다면 본인이 속한 app 의 SpringBootApplication 위치를 찾아, 그 app 의 application.properties 를 사용함
@SpringBootTest
class ItemserviceApplicationTests {

	@Test
	void contextLoads() {
	}

}
