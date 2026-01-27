package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;

/**
 *  JdbcTemplate 사용
 *  // 알아서 connection, state 설정, 쿼리실행, 자원해제까지 전부 해준다.
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository {

    private final JdbcTemplate template;

    public MemberRepositoryV5(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Member save(Member member) {
        // 실제 sql 문을 사용, 이후에 PreparedStatement 에서
        // parameter 매핑을 직접 해 줘야 함!
        String sqlStr = "insert into member(member_id, money) values (?, ?)";
        template.update(sqlStr, member.getMemberId(), member.getMoney());
        return member;
    }

    public boolean isExistById(String memberId) throws SQLException {

        Member member = this.findById(memberId);

        boolean isExist = true;
        if (member == null) {
            isExist = false;
        }
        return isExist;
    }
    @Override
    public Member findById(String memberId) {

        String sqlStr = "select * from member where member_id = ?";

        // 1건 조회는 queryForObject , 여러건 조회는 queryForList
        Member member = template.queryForObject(sqlStr, this.memberRowMapper(), memberId);
        return member;
    }

    // RowMapper 사용 방법!
    private RowMapper<Member> memberRowMapper(){
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }

    @Override
    public void updateById(String memberId, int money)  {
        String sqlStr = "update member set money = ? where member_id = ?";
        template.update(sqlStr, money, memberId);
    }
    @Override
    public void deleteById(String memberId) {
        String sqlStr = "delete from member where member_id = ?";
        template.update(sqlStr, memberId);
    }
}
