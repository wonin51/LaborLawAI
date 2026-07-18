from pathlib import Path
import shutil

root = Path(r'G:\AI-law-creater').resolve()
backend = (root / 'backend').resolve()
if root not in backend.parents and backend != root:
    raise SystemExit(f'Refuse to operate outside root: {backend}')
if backend.exists():
    shutil.rmtree(backend)

packages = ['controller', 'service', 'mapper', 'entity', 'dto', 'vo', 'config']
for pkg in packages:
    (backend / 'src' / 'main' / 'java' / 'com' / 'ailaw' / 'ragkbdemo' / pkg).mkdir(parents=True, exist_ok=True)
(backend / 'src' / 'main' / 'resources').mkdir(parents=True, exist_ok=True)
(backend / 'src' / 'test' / 'java' / 'com' / 'ailaw' / 'ragkbdemo' / 'controller').mkdir(parents=True, exist_ok=True)

(backend / 'pom.xml').write_text('''<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.3</version>
        <relativePath/>
    </parent>

    <groupId>com.ailaw</groupId>
    <artifactId>rag-kb-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>rag-kb-demo</name>
    <description>EOS repair work order and enterprise maintenance document RAG knowledge base demo backend</description>

    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0</spring-ai.version>
        <mybatis-plus.version>3.5.12</mybatis-plus.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-openai</artifactId>
        </dependency>

        <dependency>
            <groupId>co.elastic.clients</groupId>
            <artifactId>elasticsearch-java</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
''', encoding='utf-8')

(backend / 'src/main/java/com/ailaw/ragkbdemo/RagKbDemoApplication.java').write_text('''package com.ailaw.ragkbdemo;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        MybatisPlusAutoConfiguration.class
})
public class RagKbDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagKbDemoApplication.class, args);
    }
}
''', encoding='utf-8')

(backend / 'src/main/resources/application.yml').write_text('''spring:
  application:
    name: rag-kb-demo

server:
  port: 8080

rag-kb-demo:
  scenario: eos-repair-work-order-and-enterprise-maintenance-documents
''', encoding='utf-8')

(backend / 'src/test/java/com/ailaw/ragkbdemo/controller/HealthControllerTest.java').write_text('''package com.ailaw.ragkbdemo.controller;

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

for pkg in ['service', 'mapper', 'entity', 'dto', 'config']:
    (backend / 'src/main/java/com/ailaw/ragkbdemo' / pkg / 'package-info.java').write_text(f'''/**
 * Reserved {pkg} package for EOS repair work order and enterprise maintenance document RAG features.
 */
package com.ailaw.ragkbdemo.{pkg};
''', encoding='utf-8')

print('CREATED_SKELETON', backend)
for path in sorted(backend.rglob('*')):
    if path.is_file():
        print(path)
