from pathlib import Path
p = Path(r'G:\AI-law-creater\backend\src\main\resources\application.yml')
text = p.read_text(encoding='utf-8')
old = '''  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:legal_assistant}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
'''
new = '''  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/legal_assistant?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: ${DB_PASSWORD:}
'''
if old not in text:
    raise SystemExit('expected datasource block not found')
p.write_text(text.replace(old, new), encoding='utf-8')
print('UPDATED', p)
