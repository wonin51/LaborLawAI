from pathlib import Path
import subprocess, time, urllib.request
root = Path(r'G:\AI-law-creater\backend')
log_dir = Path(r'G:\AI-law-creater\.codex\tmp')
log_dir.mkdir(parents=True, exist_ok=True)
log = log_dir / 'db-ping-app-2.log'
err = log_dir / 'db-ping-app-2.err.log'
cmd = [
    'cmd.exe', '/c', 'mvn', 'spring-boot:run',
    '-Dspring-boot.run.arguments=--server.port=18080 --spring.ai.openai.api-key=dummy-key-for-db-ping-test'
]
proc = subprocess.Popen(cmd, cwd=str(root), stdout=log.open('w', encoding='utf-8'), stderr=err.open('w', encoding='utf-8'))
try:
    health = None
    for _ in range(45):
        time.sleep(1)
        try:
            with urllib.request.urlopen('http://127.0.0.1:18080/api/health', timeout=2) as r:
                health = r.read().decode('utf-8')
                break
        except Exception:
            if proc.poll() is not None:
                break
    print('PROCESS_EXIT=', proc.poll())
    print('HEALTH=', health)
    try:
        with urllib.request.urlopen('http://127.0.0.1:18080/api/db/ping', timeout=10) as r:
            print('DB_PING=', r.read().decode('utf-8'))
    except Exception as e:
        print('DB_PING_ERROR=', repr(e))
finally:
    if proc.poll() is None:
        proc.terminate()
        try:
            proc.wait(timeout=10)
        except subprocess.TimeoutExpired:
            proc.kill()
print('LOG_TAIL=')
if log.exists():
    lines = log.read_text(encoding='utf-8', errors='ignore').splitlines()
    print('\n'.join(lines[-50:]))
print('ERR_TAIL=')
if err.exists():
    lines = err.read_text(encoding='utf-8', errors='ignore').splitlines()
    print('\n'.join(lines[-20:]))
