package hello.jdbc.repository;

import hello.jdbc.domain.Member;

public interface MemberRepository {

    Member save(Member member);

    Member findById(String memberId);

    void updateById(String memberId, int money);

    void deleteById(String memberId);

}
