package hello.itemservice.repository.mybatis;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

// @Mapper 를 붙인 클래스가 매퍼가 되어, 마이바티스 매핑 XML 을 호출해 줌
@Mapper
public interface ItemMapper {

    // parameter 가 2개 이상이면 @Param 으로 지정 해줘야함
    // 1개는 그냥 생략 가능 (당연히 써줘도 됨)
    void save(Item item);

    void update(@Param("id") Long id, @Param("updateParam")ItemUpdateDto updateDto);

    Optional<Item> findById(Long id);

    List<Item> findAll(ItemSearchCond itemSearchCond);

}
