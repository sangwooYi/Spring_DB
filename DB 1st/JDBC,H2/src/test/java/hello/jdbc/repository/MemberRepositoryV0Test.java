package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

@Slf4j
public class MemberRepositoryV0Test {

    MemberRepositoryV0 repositoryV0 = new MemberRepositoryV0();
    int ord = 1;

    @Test
    void testV1() {

        String memberId = "member";
        String chckMemberId = "";
        while(true) {
            chckMemberId = memberId + ord;

            boolean isExist = false;
            try {
                isExist = repositoryV0.isExistById(chckMemberId);
            } catch (SQLException e) {
                log.error("SQL Exception", e);
            }
            if (isExist) {
                ord++;
            } else {
                memberId = chckMemberId;
                break;
            }
        }

        Member member = new Member(memberId, 120000);

        try {
            repositoryV0.save(member);

            Member findMember = repositoryV0.findById(member.getMemberId());
            log.info("find Member = {}", findMember);
            
            // 일반적으로 객체비교는 isEqualTo 사용할 것
            Assertions.assertThat(findMember).isEqualTo(member);    // 이건 같은 값인지 여부 체크
            
            // 따라서 isSameAs 는 실제 인스턴스 체크 or primitive 데이터에 대해서만 사용할 것 
//            Assertions.assertThat(findMember).isSameAs(member);    // 이건 같은 인스턴스 여부를  체크 (주소값 체크)

        } catch (SQLException e) {
            log.error("SQL Exception", e);
        }
    }

    @Test
    void updateTest() {

        try {
            repositoryV0.updateById("member1", 200000);
        } catch (SQLException e) {
            log.error("SQL Exception", e);
        }

    }

    @Test
    void deleteTest() {
        try {
            repositoryV0.deleteById("member1");

            Member member = repositoryV0.findById("member1");
            Assertions.assertThat(member).isNull();

            // 만약 강의 처럼 Exception 던지는 형태면 아래처럼 쓰면 됨. ( isInstanceOf 안에 예상되는 예외 클래스 넣어주면 됨 0
            //Assertions.assertThatThrownBy(() -> repositoryV0.findById("member1")).isInstanceOf(NoSuchElementException.class);

        } catch (SQLException e) {
            log.error("SQL Exception", e);
        }
    }

}
