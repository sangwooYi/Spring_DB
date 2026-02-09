package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    /**
     * 트랜잭션 각각 사용 Ver
     */
    public void joinV1(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("MemberRepository save 호출 시작");
        memberRepository.save(member);
        log.info("MemberRepository save 호출 종료");

        log.info("LogRepository save 호출 시작");
        logRepository.save(logMessage);
        log.info("LogRepository save 호출 종료");
    }

    /**
     * 트랜잭션 각각 사용 Ver
     * + 로그 예외 발생은 try-catch로 잡는 버전
     */
    public void joinV2(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("MemberRepository save 호출 시작");
        memberRepository.save(member);
        log.info("MemberRepository save 호출 종료");

        log.info("LogRepository save 호출 시작");
        try {
            logRepository.save(logMessage);
        } catch (MyLogException e) {
            log.debug("로그 저장에 실패함", e);
            log.debug("정상 흐름으로 진행 Ver2");
        }
        log.info("LogRepository save 호출 종료");
    }

    /**
     * 서비스 트랜잭션 적용 Ver
     * + 로그 예외 발생시 throw
     */
    @Transactional
    public void joinV3(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("MemberRepository save 호출 시작");
        memberRepository.save(member);
        log.info("MemberRepository save 호출 종료");

        log.info("LogRepository save 호출 시작");
        logRepository.save(logMessage);
        log.info("LogRepository save 호출 종료");
    }

    /**
     * 트랜잭션 사용 Ver
     * + 로그 예외 발생은 try-catch로 잡는 버전
     */
    @Transactional
    public void joinV4(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("MemberRepository save 호출 시작");
        memberRepository.save(member);
        log.info("MemberRepository save 호출 종료");

        log.info("LogRepository save 호출 시작");
        try {
            logRepository.save(logMessage);
        } catch (MyLogException e) {
            log.debug("로그 저장에 실패함", e);
            log.debug("정상 흐름으로 진행 Ver2");
        }
        log.info("LogRepository save 호출 종료");
    }

    /**
     * 트랜잭션 전체 적용 + 전파 옵션 REQUIRES_NEW 사용으로 물리트랜잭션 분리 Ver
     * + 로그 예외 발생은 try-catch로 잡는 버전 
     * + 로그 Repository 의 트랜잭션 전파 옵션 REQUIRES_NEW 적용 ( 물리 트랜잭션을 분리 ) 
     */
    @Transactional
    public void joinV5(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("MemberRepository save 호출 시작");
        memberRepository.save(member);
        log.info("MemberRepository save 호출 종료");

        log.info("LogRepository save 호출 시작");
        try {
            // 로그 Repository 의 트랜잭션 전파 옵션 REQUIRES_NEW 적용 ( 물리 트랜잭션을 분리 )
            logRepository.saveTxRequiresNew(logMessage);
        } catch (MyLogException e) {
            log.debug("로그 저장에 실패함", e);
            log.debug("정상 흐름으로 진행 Ver2");
        }
        log.info("LogRepository save 호출 종료");
    }
}
