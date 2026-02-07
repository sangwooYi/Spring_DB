package hello.springtx.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue // 디폴트는 strategy = GenerationType.AUTO
    private Long id;

    private String userName;
    private String payStatus;
}
