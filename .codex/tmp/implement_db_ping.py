from pathlib import Path
import shutil

root = Path(r'G:\AI-law-creater\backend')
main_base = root / 'src/main/java/com/laborlaw/ragkbdemo'
test_base = root / 'src/test/java/com/laborlaw/ragkbdemo'

# Clean target package and old package to avoid duplicate SpringBootApplication classes.
for path in [main_base, test_base, root / 'src/main/java/com/ailaw', root / 'src/test/java/com/ailaw']:
    if path.exists():
        shutil.rmtree(path)

for pkg in ['controller', 'service', 'mapper', 'entity', 'dto', 'vo', 'config']:
    (main_base / pkg).mkdir(parents=True, exist_ok=True)
(test_base / 'controller').mkdir(parents=True, exist_ok=True)
(root / 'src/main/resources/mapper').mkdir(parents=True, exist_ok=True)

(main_base / 'RagKbDemoApplication.java').write_text('''package com.laborlaw.ragkbdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.laborlaw.ragkbdemo.mapper")
@SpringBootApplication
public class RagKbDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagKbDemoApplication.class, args);
    }
}
''', encoding='utf-8')

(main_base / 'vo/ApiResponse.java').write_text('''package com.laborlaw.ragkbdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;

    private String message;

    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}
''', encoding='utf-8')

(main_base / 'vo/HealthVO.java').write_text('''package com.laborlaw.ragkbdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthVO {

    private String status;
}
''', encoding='utf-8')

(main_base / 'vo/DbPingVO.java').write_text('''package com.laborlaw.ragkbdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbPingVO {

    private String timestamp;

    private String status;
}
''', encoding='utf-8')

(main_base / 'controller/HealthController.java').write_text('''package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.vo.HealthVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthVO health() {
        return new HealthVO("ok");
    }
}
''', encoding='utf-8')

(main_base / 'service/DbPingService.java').write_text('''package com.laborlaw.ragkbdemo.service;

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
''', encoding='utf-8')

(main_base / 'controller/DbPingController.java').write_text('''package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.service.DbPingService;
import com.laborlaw.ragkbdemo.vo.ApiResponse;
import com.laborlaw.ragkbdemo.vo.DbPingVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/db")
public class DbPingController {

    private final DbPingService dbPingService;

    public DbPingController(DbPingService dbPingService) {
        this.dbPingService = dbPingService;
    }

    @GetMapping("/ping")
    public ApiResponse<DbPingVO> ping() {
        try {
            return ApiResponse.success(dbPingService.ping());
        } catch (Exception ex) {
            return ApiResponse.error(500, ex.getMessage(), new DbPingVO(null, "error"));
        }
    }
}
''', encoding='utf-8')

# package-info files
infos = {
    'controller': 'REST controllers.',
    'service': 'Business services.',
    'mapper': 'MyBatis-Plus mapper interfaces.',
    'entity': 'MyBatis-Plus entity classes.',
    'dto': 'Request DTO classes.',
    'vo': 'Response view objects.',
    'config': 'Spring configuration classes.'
}
for pkg, desc in infos.items():
    (main_base / pkg / 'package-info.java').write_text(f'''/**\n * {desc}\n */\npackage com.laborlaw.ragkbdemo.{pkg};\n''', encoding='utf-8')

# Tests in new package
(test_base / 'controller/HealthControllerTest.java').write_text('''package com.laborlaw.ragkbdemo.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/health returns ok status")
    void healthReturnsOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value("ok"));
    }
}
''', encoding='utf-8')

(test_base / 'controller/DbPingControllerTest.java').write_text('''package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.service.DbPingService;
import com.laborlaw.ragkbdemo.vo.DbPingVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DbPingController.class)
class DbPingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DbPingService dbPingService;

    @Test
    @DisplayName("GET /api/db/ping returns database timestamp when connection is available")
    void pingReturnsDatabaseTimestamp() throws Exception {
        when(dbPingService.ping()).thenReturn(new DbPingVO("2026-07-18 16:00:00", "ok"));

        mockMvc.perform(get("/api/db/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.timestamp").value("2026-07-18 16:00:00"))
                .andExpect(jsonPath("$.data.status").value("ok"));
    }

    @Test
    @DisplayName("GET /api/db/ping returns clear error message when connection fails")
    void pingReturnsClearErrorWhenDatabaseUnavailable() throws Exception {
        when(dbPingService.ping()).thenThrow(new IllegalStateException("Database ping failed: connection timeout"));

        mockMvc.perform(get("/api/db/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Database ping failed: connection timeout"))
                .andExpect(jsonPath("$.data.status").value("error"));
    }
}
''', encoding='utf-8')

# application.yml
(root / 'src/main/resources/application.yml').write_text('''spring:
  application:
    name: rag-kb-demo
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:legal_assistant}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}

server:
  port: 8080

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.laborlaw.ragkbdemo.entity
  configuration:
    map-underscore-to-camel-case: true

rag-kb-demo:
  scenario: labor-contract-legal-assistant
''', encoding='utf-8')

# Ensure Mockito subclass workaround remains
mockito_dir = root / 'src/test/resources/mockito-extensions'
mockito_dir.mkdir(parents=True, exist_ok=True)
(mockito_dir / 'org.mockito.plugins.MockMaker').write_text('mock-maker-subclass\n', encoding='utf-8')

print('IMPLEMENTED_DB_PING_AND_PACKAGE_MIGRATION')
for p in sorted((root / 'src/main/java/com/laborlaw/ragkbdemo').rglob('*.java')):
    print(p)
