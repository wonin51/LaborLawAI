from pathlib import Path
import subprocess, time, urllib.request, json
root = Path(r'G:\AI-law-creater\backend')
log_dir = Path(r'G:\AI-law-creater\.codex\tmp')
log_dir.mkdir(parents=True, exist_ok=True)
log = log_dir / 'db-ping-app.log'
err = log_dir / 'db-ping-app.err.log'
cmd = ['java', '-jar', str(root / 'target/rag-kb-demo-0.0.1-SNAPSHOT.jar'), '--server.port=18080']
proc = subprocess.Popen(cmd, cwd=str(root), stdout=log.open('w', encoding='utf-8'), stderr=err.open('w', encoding='utf-8'))
try:
    health = None
    for _ in range(35):
        time.sleep(1)
        try:
            with urllib.request.urlopen('http://127.0.0.1:18080/api/health', timeout=2) as r:
                health = r.read().decode('utf-8')
                break
        except Exception:
            pass
    print('HEALTH=', health)
    try:
        with urllib.request.urlopen('http://127.0.0.1:18080/api/db/ping', timeout=10) as r:
            body = r.read().decode('utf-8')
            print('DB_PING=', body)
    except Exception as e:
        print('DB_PING_ERROR=', repr(e))
finally:
    proc.terminate()
    try:
        proc.wait(timeout=10)
    except subprocess.TimeoutExpired:
        proc.kill()
print('LOG_TAIL=')
if log.exists():
    lines = log.read_text(encoding='utf-8', errors='ignore').splitlines()
    print('\n'.join(lines[-20:]))
print('ERR_TAIL=')
if err.exists():
    lines = err.read_text(encoding='utf-8', errors='ignore').splitlines()
    print('\n'.join(lines[-20:]))
