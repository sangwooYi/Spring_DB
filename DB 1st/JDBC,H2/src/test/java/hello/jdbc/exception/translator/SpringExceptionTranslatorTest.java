package hello.jdbc.exception.translator;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class SpringExceptionTranslatorTest {

    DataSource dataSource;
    @BeforeEach
    void init() {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void sqlExceptionErrorCode() {
        // 의도적으로 문법 에러
        String sql = "select bad grammer";
        try {
            Connection con = dataSource.getConnection();
            PreparedStatement st = con.prepareStatement(sql);
            // executeQuery 는 단순 조회같은 결과 받아오는 쿼리
            // Create, Update 등은 executeUpdate 사용
            st.executeQuery();
        } catch (SQLException e) {
            log.info("errorCode = {}", e.getErrorCode(), e);
            Assertions.assertThat(e.getErrorCode()).isEqualTo(42122);
        }
    }

    @Test
    void exceptionTranslator() {
        String sql = "select bad grammer";

        try {
            Connection con = dataSource.getConnection();
            PreparedStatement pre = con.prepareStatement(sql);
            pre.executeQuery();
        } catch (SQLException e) {
            SQLErrorCodeSQLExceptionTranslator translator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
            DataAccessException dbException = translator.translate("select", sql, e);
            log.info("dbException", dbException);
            Assertions.assertThat(dbException.getClass()).isEqualTo(BadSqlGrammarException.class);
        }
    }
}
