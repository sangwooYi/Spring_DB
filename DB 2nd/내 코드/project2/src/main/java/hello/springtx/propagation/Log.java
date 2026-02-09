package hello.springtx.propagation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DB 저장 로그 용
@Entity
@Getter @Setter
@NoArgsConstructor
public class Log {

    @Id
    @GeneratedValue
    private Long id;

    private String message;

    public Log(String message) {
        this.message = message;
    }

}
