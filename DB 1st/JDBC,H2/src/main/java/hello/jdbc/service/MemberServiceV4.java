package hello.jdbc.service;


import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 *   예외 누수 문제 해결
 *   MemberRepository 추상회된 인터페이스에 의존
 *   ( 의존성 느슨하게 ! )
 */

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV4 {

    private final MemberRepository memberRepository;

    // @Transactional 는 이 어노테이션이 걸린 메서드가 호출될 때 트랜젝션을 적용해준다.
    // 만약 클래스에다가 걸면 해당 클래스의 모든 public 메서드에 @Transactional 이 적용 됨!
    @Transactional
    public void accountTransfer(String fromId, String toId, int money) {
        this.bizLogic(fromId, toId, money);
    }

    // 레이어가 다른 경우는 이렇게 분리하는게 유지보수에 유리하다!
    private void bizLogic(String fromId, String toId, int money) {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.updateById(fromId, fromMember.getMoney()-money);
        this.validation(toMember);
        memberRepository.updateById(toId, toMember.getMoney()+money);
    }

    private void validation(Member member) {
        if (member.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
