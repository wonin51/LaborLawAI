from pathlib import Path
p = Path(r'G:\AI-law-creater\.codex\tmp\CreateLegalAssistantDb.java')
p.write_text('''import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateLegalAssistantDb {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
        try (Connection connection = DriverManager.getConnection(url, "root", "12181168");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS legal_assistant DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci");
            System.out.println("CREATE_DATABASE_OK");
        }
    }
}
''', encoding='utf-8')
print('rewrote', p)
