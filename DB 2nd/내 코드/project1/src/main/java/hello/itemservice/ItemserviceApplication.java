package hello.itemservice;

import hello.itemservice.config.*;
import hello.itemservice.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

// @Import 는 Config 파일을 어떤 걸 적용할지는 정하는 부분!
//@Import(MemoryConfig.class)
@Import(V2Config.class)
// @ComponentScan 은 디폴트로 이 어노테이션이 설정된 클래스 하위레벨 전체를 스캔함
// scanBasePackages 를 통해 스캔범위를 지정 가능! ( 이번 예제는 컨트롤러 외에는 전부 수동 @Bean 등록을 진행 해서 이걸 적용 )
@SpringBootApplication(scanBasePackages = "hello.itemservice.web")
@Slf4j
public class ItemserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemserviceApplication.class, args);
	}

    @Bean
    @Profile("local")   // 이건 application.properties 의 spring.profiles.active 으로 설정
    public TestDataInit testDataInit(ItemRepository itemRepository) {
        return new TestDataInit(itemRepository);
    }

    // Test 코드에 대해 DataSource 직접 등록
    // 참고로 별도로 DB 정보를 설정 안하면 스프링은 자동으로 H2 임베디드 DB 의 Datasource 를 만들어준다!
//    @Bean
//    @Profile("test")
//    public DataSource dataSource() {
//        log.info("테스트 데이터 메모리 초기화");
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName("org.h2.Driver");     // H2 DB 지정
//        dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=1"); // mem 은 memory mode 라는 의미
//        dataSource.setUsername("sa");
//        dataSource.setPassword("");
//
//        return dataSource;
//    }
}
