from pathlib import Path
p = Path(r'G:\AI-law-creater\backend\src\main\resources\application-local.yml')
p.write_text('''spring:
  datasource:
    url: jdbc:mysql://localhost:3306/legal_assistant?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 12181168
  ai:
    openai:
      api-key: dummy-key-for-local-test
''', encoding='utf-8')
print(p)
