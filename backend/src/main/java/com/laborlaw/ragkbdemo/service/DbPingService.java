package com.laborlaw.ragkbdemo.service;

import com.laborlaw.ragkbdemo.vo.DbPingVO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DbPingService {

    private final JdbcTemplate jdbcTemplate;

    public DbPingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DbPingVO ping() {
        try {
            String timestamp = jdbcTemplate.queryForObject(
                    "SELECT DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')",
                    String.class
            );
            return new DbPingVO(timestamp, "ok");
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Database ping failed: " + rootMessage(ex), ex);
        }
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? throwable.getMessage() : current.getMessage();
    }
}
