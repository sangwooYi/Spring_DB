package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
// JPA Param 반드시 이거 써야함 mybatis 거 호출 안하게 주의!
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataJpaItemRepository extends JpaRepository<Item, Long> {

    // 단순 CRUD 의 경우 이미 정의되어있어 별도로 정의할 필요도 없음!

    // 동적 쿼리가 안되기에 아래처럼 케이스별로 구현을 해야한다. ( 따라서 QueryDsl 이 반드시 필요! )
    // like 검색은 %a% 면 Containing , %a 면 StartingWith 이런식 ! Spring Data JPA 문서 찾아볼 것!
    List<Item> findByItemNameContaining(String itemName);

    List<Item> findByPriceLessThanEqual(Integer price);

    // 아래 두 메서드는 같은 역할을 한다. 당연히 아래처럼 쿼리메서드에 두개 이상 조건이 달리는경우는
    // 쿼리메서드 쓰지말자. 변수명이 유지보수하기 너무 힘들어짐.
    
    // 쿼리메서드
    List<Item> findByItemNameContainingAndPriceLessThanEqual(String itemName, Integer price);

    // @Query 사용 '%a%' 는 jpql 로 concat('%', :itemName, '%') 이런식으로 써주면 된다.
    @Query("select i from Item i where i.itemName like concat('%', :itemName, '%') and i.price <= :price")
    List<Item> findItems(@Param("itemName") String itemName, @Param("price") Integer price);

}
