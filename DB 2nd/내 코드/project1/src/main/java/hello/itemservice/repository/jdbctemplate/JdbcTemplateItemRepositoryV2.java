package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *  NamedParameterJdbcTemplate 적용
 *  
 *  파라미터 매핑 방법
 *  SqlParameterSource
 *   - BeanPropertySqlParameterSource -> DTO 만으로 매핑이 전부 해결 될 때
 *   - MapSqlParameterSource -> DTO 만으로는 매핑이 해결 안될 때 or 명시
 *  Map -> DTO 만으로는 매핑이 해결 안될 때 or 명시
 *
 *  RowMapper 는 아래 클래스로 훨씬 편하게 사용 가능
 *  BeanPropertyRowMapper
 */
@Slf4j
public class JdbcTemplateItemRepositoryV2 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;

    public JdbcTemplateItemRepositoryV2(DataSource dataSource) {
        // JdbcTemplate 사용 위해서는 DataSource 가 전달되어야 한다.
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }


    @Override
    public Item save(Item item) {
        // ? 대신에 파라미터 명을 정의해서 매칭하는 방식으로 바뀜  ( 모호함 제거 )
        String sqlStr = "insert into item(item_name, price, quantity) values (:itemName, :price, :quantity)";

        // 방법1 BeanPropertySqlParameterSource 사용
        // DTO 를 Object 로 넘겨서 이를 이용해서 parameter 랑 매칭함
        // Item DTO 와 필드명이 동일해야함 ( 따라서 위에 SQL 에서 카멜케이스로 씀 )
        // 자바 프로퍼티 규약을 따름 따라서 getItemName 면 -> itemName 이 필드명이어야 함
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);

        // JdbcTemplate 에서 generate Key 값 가져오는 방법
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sqlStr, param, keyHolder);

        // getKey 의 반환타입은 Number ( 숫자 관련 오브젝트 )
        long key = keyHolder.getKey().longValue();
        item.setId(key);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sqlStr = "update item set item_name = :itemName, price = :price, quantity = :quantity where id = :id";

        // 방법2 MapSqlParameterSource 사용 ( ItemUpdateDto 변수명과 일치해야 함 )
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", itemId)
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity());

        // Create, Update, Delete 는 그냥 update 사용
        template.update(sqlStr, param);
    }

    // 단건 조회는 queryForObject()
    // 여러건 조회는 query()
    @Override
    public Optional<Item> findById(Long memberId) {
        String sqlStr = "select id, item_name, price, quantity from item where id = :id";

        // queryForObject 의 경우 결과가 null 이면 EmptyResultDataAccessException 예외 발생
        try {
            // 방법2 MapSqlParameterSource 사용
//            SqlParameterSource param = new MapSqlParameterSource()
//                    .addValue("id", memberId);
            // 방법3 자바 Map 사용  ( Map.of(K, V) 는 초기화로 K, V 데이터를 가진 Map을 생성해 줌 )
            // Map.of(K1, V1, K2, V2, K3, V3 .. ) 이런식으로 여러쌍을 넣어서 초기화도 가능
            Map<String, Object> param = Map.of("id", memberId);
            Item item = template.queryForObject(sqlStr, param, itemRowWrapper());
            return Optional.of(item);   // .of() 는 해당 오브젝트를 Optional 로 감싸주는 것! ( 반환타입을 잘 보자 )
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();    // Optional 에서 null 은 이거임
        }
    }

    // BeanPropertyRowMapper 사용
    // 넘겨준 class 의 필드명을 이용해서 알아서 ResultSet 을 Object 로 변환해줌
    // 스네이크 케이스 - 카멜케이스 변환 지원 ( ex item_name <-> itemName 매핑 지원 )
    private RowMapper<Item> itemRowWrapper() {
        return BeanPropertyRowMapper.newInstance(Item.class);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();  // null 값으로 체크하기위해 래퍼클래스 사용

        // ItemSearchCond 와 밑에 매핑되는 변수명이 동일해야 한다.
        // DTO를 통채로 넘기기만 하면 되니 편한건 BeanPropertySqlParameterSource
        // 그냥 해당 DTO 하나로 끝나는 경우면 이 경우가 훨씬 편함
        SqlParameterSource param = new BeanPropertySqlParameterSource(cond);

        // MapSqlParameterSource 는 명시적으로 해당 화면에서 매핑되는 변수명 체크가 가능한 장점 존재.
        // 별도로 여기저기서 값을 조립해야하거나, 명시적으로 필드 표기해주고 싶으면 이 방법 권장
//        SqlParameterSource param2 = new MapSqlParameterSource()
//                .addValue("itemName", itemName)
//                .addValue("maxPrice", maxPrice);

        String sqlStr = "select id, item_name, price, quantity from item";
    
        // JdbcTemplate 으로 동적쿼리 작성하는 법
        // itemName, maxPrice 는 동적으로 전달되는 값이 다르므로 이렇게 조건 걸어 주어야만 함!
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sqlStr += " where";
        }

        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            // like 는 이 방법으로 연결해줘야 함
            sqlStr += " item_name like concat('%', :itemName, '%')";
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                sqlStr += " and";
            }
            sqlStr += " price <= :maxPrice";
        }
        log.info("최종 SQL 쿼리문 = {}", sqlStr);
        
        // 여러항목 조회할때는 .query() 사용
        // JdbcTemplate 이 RowMapper 에서 커서 이동하여 루프 돌리는 부분을 알아서 해줌 !
        return template.query(sqlStr, param, itemRowWrapper());
    }
}
