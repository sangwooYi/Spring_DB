package hello.itemservice.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity // JPA 가 사용하는 객체라는 뜻 ( 엔티티 )
@Table(name = "item")   // 객체명이랑 테이블명이 같으면 생략 가능
public class Item {
    // PK 값,  AutoIncrement
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사실 스네이크케이스 - 카멜케이스 변환은 자동으로 해줌
    // 따라서 이 경우는 @Column 생략해도 됨!
    @Column(name = "item_name", length = 10)
    private String itemName;
    private Integer price;
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
