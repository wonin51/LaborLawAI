from pathlib import Path
p = Path(r'G:\AI-law-creater\backend\src\main\resources\application.yml')
text = p.read_text(encoding='utf-8')
needle = '''  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/legal_assistant?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: ${DB_PASSWORD:}
'''
replacement = '''  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/legal_assistant?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: ${DB_PASSWORD:}
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:}
      base-url: ${OPENAI_BASE_URL:https://api.openai.com}
'''
if needle not in text:
    raise SystemExit('expected datasource block not found')
p.write_text(text.replace(needle, replacement), encoding='utf-8')
print('UPDATED', p)
