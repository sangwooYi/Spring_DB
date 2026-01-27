package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;

/**
 *  SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {

    private final DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator();
    }

    @Override
    public Member save(Member member) {
        // 실제 sql 문을 사용, 이후에 PreparedStatement 에서
        // parameter 매핑을 직접 해 줘야 함!
        String sqlStr = "insert into member(member_id, money) values (?, ?)";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = this.getConnection();
            preparedStatement = connection.prepareStatement(sqlStr);
            // sql 문에서 parameter 를 수동으로 매핑 해줘야한다!
            preparedStatement.setString(1, member.getMemberId());
            preparedStatement.setInt(2, member.getMoney());
            
            // 해당 SQL 구문 실행 ( Insert, Update, DELETE 하는 경우에 executeUpdate() 사용 )
            preparedStatement.executeUpdate();
            return member;
        } catch (SQLException e) {
            // task 에는 그냥 메서드명을 대입해주면 된다. ( 그냥 스택트레이스 에서 표기되는 문구에 영향 주는 부분 )
            // 따라서 그냥 뭐하다 에러난건지 알기 위해 호출한 메서드명을 적어주면 됨!
            DataAccessException errResult = exTranslator.translate("save", sqlStr, e);
            throw errResult;

        } finally {
            // 반드시 외부 resource 를 close 처리 해줘야 한다!
            this.close(connection, preparedStatement, null);
        }
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

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Member member = null;
        try {
            connection = this.getConnection();
            preparedStatement = connection.prepareStatement(sqlStr);
            preparedStatement.setString(1, memberId);

            // ResultSet 이라는 Set 형태에 결과를 담아준다.
            // 조회일 때는 executeQuery 메서드 사용
            resultSet = preparedStatement.executeQuery();

            // 존재할 때 ( PK 값으로 인한 단건조회이므로 if 사용, 아니면 while 사용해야 함 )
            if (resultSet.next()) { // .next() 는 다음 Set 값이 없을 때 까지 계속 커서를 이동함
                member = new Member();
                // ResultSet에서 컬럼 값 가져오는 방법
                member.setMemberId(resultSet.getString("member_id"));
                member.setMoney(resultSet.getInt("money"));
            } else { // 없을 때
                log.error("Member Not Found member_Id = " + memberId);
            }
        } catch (SQLException e) {
            log.error("SQL Exception", e);
            throw exTranslator.translate("findById", sqlStr, e);
        } finally {
            this.close(connection, preparedStatement, resultSet);
        }
        return member;
    }

    @Override
    public void updateById(String memberId, int money)  {
        String sqlStr = "update member set money = ? where member_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = this.getConnection();
            preparedStatement = connection.prepareStatement(sqlStr);
            // sql 문에서 parameter 를 수동으로 매핑 해줘야한다! parameter 순서 주의!
            preparedStatement.setInt(1, money);
            preparedStatement.setString(2, memberId);

            // 해당 SQL 구문 실행 ( Insert, Update, DELETE 하는 경우에 executeUpdate() 사용 )
            // executeUpdate는 실행 후, 영향받은 row 갯수를 반환함.
            int resultSize = preparedStatement.executeUpdate();
            log.info("resultSize = {}", resultSize);
        } catch (SQLException e) {
            log.error("SQLException !", e);
            throw exTranslator.translate("updateById", sqlStr, e);
        } finally {
            // 반드시 외부 resource 를 close 처리 해줘야 한다!
            this.close(connection, preparedStatement, null);
        }
    }
    @Override
    public void deleteById(String memberId) {

        String sqlStr = "delete from member where member_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = this.getConnection();
            preparedStatement = connection.prepareStatement(sqlStr);
            // sql 문에서 parameter 를 수동으로 매핑 해줘야한다! parameter 순서 주의!
            preparedStatement.setString(1, memberId);

            // 해당 SQL 구문 실행 ( Insert, Update, DELETE 하는 경우에 executeUpdate() 사용 )
            // executeUpdate는 실행 후, 영향받은 row 갯수를 반환함.
            int resultSize = preparedStatement.executeUpdate();
            log.info("resultSize = {}", resultSize);
        } catch (SQLException e) {
            log.error("SQLException !", e);
            throw exTranslator.translate("deleteById", sqlStr, e);
        } finally {
            // 반드시 외부 resource 를 close 처리 해줘야 한다!
            this.close(connection, preparedStatement, null);
        }

    }

    // DataSourceUtils 사용
    private Connection getConnection() {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        log.info("get Connection = {}, class = {}", connection, connection.getClass());
        return connection;
    }

    // DataSourceUtils 사용
    private void close(Connection connection, Statement statement, ResultSet resultSet) {

        JdbcUtils.closeStatement(statement);
        JdbcUtils.closeResultSet(resultSet);

        // 트랜잭션 동기화를 사용하려면 이렇게!
        DataSourceUtils.releaseConnection(connection, dataSource);

    }

}
