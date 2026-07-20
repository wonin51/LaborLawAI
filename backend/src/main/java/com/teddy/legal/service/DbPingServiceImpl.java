package com.teddy.legal.service;

import com.teddy.legal.vo.DbPingVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DbPingServiceImpl implements DbPingService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private DataSource dataSource;

    @Override
    public DbPingVO ping() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT NOW()")) {
            String timestamp = LocalDateTime.now().format(FORMATTER);
            if (resultSet.next()) {
                timestamp = resultSet.getTimestamp(1).toLocalDateTime().format(FORMATTER);
                return new DbPingVO(timestamp, "ok");
            }
            return new DbPingVO(timestamp, "ok");
        } catch (Exception ex) {
            throw new IllegalStateException("Database ping failed: " + ex.getMessage(), ex);
        }
    }
}
