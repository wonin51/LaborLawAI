from pathlib import Path
p = Path(r'G:\AI-law-creater\README.md')
text = p.read_text(encoding='utf-8')
checks = [
    '项目简介',
    '后端启动方式',
    '前端启动方式',
    '环境版本要求',
    'GET /api/health',
    '8080 端口被占用',
    'Node 版本不符合要求',
    'Maven 依赖下载失败',
    '前端无法访问后端接口',
    'G:\\AI-law-creater\\backend',
    'G:\\AI-law-creater\\frontend',
    'npm install',
    'npm run dev',
    'mvn spring-boot:run',
]
missing = [item for item in checks if item not in text]
print('README:', p)
print('SIZE:', p.stat().st_size)
print('MISSING:', missing)
print('FIRST_LINES:')
print('\n'.join(text.splitlines()[:20]))
if missing:
    raise SystemExit(1)
