package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *  JDBCTemplate 적용
 */
@Slf4j
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

    private final JdbcTemplate template;

    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        // JdbcTemplate 사용 위해서는 DataSource 가 전달되어야 한다.
        this.template = new JdbcTemplate(dataSource);
    }


    @Override
    public Item save(Item item) {
        String sqlStr = "insert into item(item_name, price, quantity) values (?, ?, ?)";

        // JdbcTemplate 에서 generate Key 값 가져오는 방법
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            // 자동 증가 키의 경우 이렇게 설정해야 한다.
            PreparedStatement ps = connection.prepareStatement(sqlStr, new String[]{"id"});

            ps.setString(1, item.getItemName());
            ps.setInt(2, item.getPrice());
            ps.setInt(3, item.getQuantity());
            return ps;
        }, keyHolder);
        // getKey 의 반환타입은 Number ( 숫자 관련 오브젝트 )
        long key = keyHolder.getKey().longValue();
        item.setId(key);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sqlStr = "update item set item_name = ?, price = ?, quantity = ? where id = ?";
        
        // Create, Update, Delete 는 그냥 update 사용
        template.update(sqlStr,
                updateParam.getItemName(),
                updateParam.getPrice(),
                updateParam.getQuantity(),
                itemId);
    }

    // 단건 조회는 queryForObject()
    // 여러건 조회는 query()
    @Override
    public Optional<Item> findById(Long memberId) {
        String sqlStr = "select id, item_name, price, quantity from item where id = ?";

        // queryForObject 의 경우 결과가 null 이면 EmptyResultDataAccessException 예외 발생
        try {
            Item item = template.queryForObject(sqlStr, itemRowWrapper(), memberId);
            return Optional.of(item);   // .of() 는 해당 오브젝트를 Optional 로 감싸주는 것! ( 반환타입을 잘 보자 )
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();    // Optional 에서 null 은 이거임
        }
    }

    // RowMapper 쓰는 법 기억해 두자!
    // ResultSet 을 오브젝트로 바꿔주는 부분!
    private RowMapper<Item> itemRowWrapper() {
        return ((rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        });
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();  // null 값으로 체크하기위해 래퍼클래스 사용

        String sqlStr = "select id, item_name, price, quantity from item";
    
        // JdbcTemplate 으로 동적쿼리 작성하는 법
        // itemName, maxPrice 는 동적으로 전달되는 값이 다르므로 이렇게 조건 걸어 주어야만 함!
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sqlStr += " where";
        }

        boolean andFlag = false;
        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            // like 는 이 방법으로 연결해줘야 함
            sqlStr += " item_name like concat('%', ?, '%')";
            param.add(itemName);
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                sqlStr += " and";
            }
            sqlStr += " price <= ?";
            param.add(maxPrice);
        }
        log.info("최종 SQL 쿼리문 = {}", sqlStr);
        
        // 여러항목 조회할때는 .query() 사용
        // JdbcTemplate 이 RowMapper 에서 커서 이동하여 루프 돌리는 부분을 알아서 해줌 !
        return template.query(sqlStr, itemRowWrapper(), param.toArray());
    }
}
