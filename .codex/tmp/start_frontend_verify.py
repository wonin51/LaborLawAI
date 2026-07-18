from pathlib import Path
import subprocess
import time
import urllib.request
import json
import re

root = Path(r'G:\AI-law-creater')
tmp = root / '.codex' / 'tmp'
tmp.mkdir(parents=True, exist_ok=True)
backend_log = tmp / 'backend-run.log'
backend_err = tmp / 'backend-run.err.log'
frontend_log = tmp / 'frontend-vite.log'
frontend_err = tmp / 'frontend-vite.err.log'

def get_json(url, timeout=3):
    with urllib.request.urlopen(url, timeout=timeout) as resp:
        body = resp.read().decode('utf-8')
        return resp.status, body

def backend_ok():
    try:
        status, body = get_json('http://127.0.0.1:8080/api/health', 2)
        return status == 200 and json.loads(body).get('status') == 'ok', body
    except Exception as e:
        return False, str(e)

ok, backend_msg = backend_ok()
if not ok:
    jar = root / 'backend' / 'target' / 'rag-kb-demo-0.0.1-SNAPSHOT.jar'
    if not jar.exists():
        raise SystemExit(f'Backend jar not found: {jar}')
    bo = backend_log.open('w', encoding='utf-8')
    be = backend_err.open('w', encoding='utf-8')
    subprocess.Popen(['java', '-jar', str(jar)], cwd=str(root / 'backend'), stdout=bo, stderr=be)
    for _ in range(20):
        time.sleep(1)
        ok, backend_msg = backend_ok()
        if ok:
            break

print('--- backend health ---')
print(backend_msg)

for p in [frontend_log, frontend_err]:
    try: p.unlink()
    except FileNotFoundError: pass
fo = frontend_log.open('w', encoding='utf-8')
fe = frontend_err.open('w', encoding='utf-8')
subprocess.Popen(['cmd.exe', '/c', 'npm.cmd', 'run', 'dev'], cwd=str(root / 'frontend'), stdout=fo, stderr=fe)
time.sleep(8)
stdout = frontend_log.read_text(encoding='utf-8', errors='ignore') if frontend_log.exists() else ''
stderr = frontend_err.read_text(encoding='utf-8', errors='ignore') if frontend_err.exists() else ''
print('--- frontend stdout ---')
print(stdout)
print('--- frontend stderr ---')
print(stderr)

url = 'http://127.0.0.1:5173/'
for text in [stdout, stderr]:
    m = re.search(r'Local:\s+(http://[^\s]+)', text)
    if m:
        url = m.group(1)
        break
print('--- frontend url ---')
print(url)
print('--- frontend page status ---')
try:
    status, body = get_json(url, 5)
    print(f'HTTP {status}, {len(body)} bytes')
except Exception as e:
    print(str(e))
print('--- frontend proxy health ---')
try:
    base = url.rstrip('/')
    status, body = get_json(base + '/api/health', 5)
    print(f'HTTP {status}, {body}')
except Exception as e:
    print(str(e))
