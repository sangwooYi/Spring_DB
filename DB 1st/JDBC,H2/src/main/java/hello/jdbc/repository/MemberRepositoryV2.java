package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;

/**
 *  JDBC - DataSource 사용, JdbcUtil 사용
 *  트랜잭션 적용 ( 커넥션을 parameter로 전달함으로써
 *  동일 트랜젝션인 경우 동일 커넥션 유지 )
 */
@Slf4j
public class MemberRepositoryV2 {

    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
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
            log.error("SQLException !", e);
            throw e;
        } finally {
            // 반드시 외부 resource 를 close 처리 해줘야 한다!
            this.closeResourceFin(connection, preparedStatement, null);
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

    public Member findById(String memberId) throws SQLException{

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
            throw e;
        } finally {
            this.closeResourceFin(connection, preparedStatement, resultSet);
        }
        return member;
    }

    // 오버로딩으로 커넥션 전달하는 findById 정의
    public Member findById(String memberId, Connection connection) throws SQLException{

        String sqlStr = "select * from member where member_id = ?";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Member member = null;
        try {
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
            throw e;
        } finally {
            this.closeResourceWithoutCon(preparedStatement, resultSet);
        }
        return member;
    }

    public void updateById(String memberId, int money) throws SQLException {
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
            throw e;
        } finally {
            // 반드시 외부 resource 를 close 처리 해줘야 한다!
            this.closeResourceFin(connection, preparedStatement, null);
        }
    }

    public void updateById(String memberId, int money, Connection connection) throws SQLException {
        String sqlStr = "update member set money = ? where member_id = ?";

        PreparedStatement preparedStatement = null;

        try {
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
            throw e;
        } finally {
            // 반드시 외부 resource 를 close 처리 해줘야 한다!
            this.closeResourceWithoutCon(preparedStatement, null);
        }
    }

    public void deleteById(String memberId) throws SQLException {

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
            throw e;
        } finally {
            // 반드시 외부 resource 를 close 처리 해줘야 한다!
            this.closeResourceFin(connection, preparedStatement, null);
        }

    }

    private Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();

        log.info("get Connection = {}, class = {}", connection, connection.getClass());
        return connection;
    }

    // JdbcUtils 사용
    // try-catch 각각 감싸주는 이유 => 트랜잭션 범위 제한!
    // 반드시 자원 해제 해주어야 함!
    private void closeResourceWithoutCon(Statement statement, ResultSet resultSet) {

        // 알아서 Null 체크 및 다른 Exception 까지 대응해준 Util 클래스!
        // 여기서는 커넥션은 해제하면 안된다! 주의!
        JdbcUtils.closeStatement(statement);
        JdbcUtils.closeResultSet(resultSet);

    }

    private void closeResourceFin(Connection connection, Statement statement, ResultSet resultSet) {

        // 알아서 Null 체크 및 다른 Exception 까지 대응해준 Util 클래스!
        JdbcUtils.closeConnection(connection);
        JdbcUtils.closeStatement(statement);
        JdbcUtils.closeResultSet(resultSet);

    }

}
