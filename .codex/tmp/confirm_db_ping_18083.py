from pathlib import Path
import subprocess, time, urllib.request
root = Path(r'G:\AI-law-creater\backend')
log_dir = Path(r'G:\AI-law-creater\.codex\tmp')
log_dir.mkdir(parents=True, exist_ok=True)
out = log_dir / 'db-ping-confirm-18083.out.log'
err = log_dir / 'db-ping-confirm-18083.err.log'
mvn = r'D:\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin\mvn.cmd'
proc = subprocess.Popen([mvn, 'spring-boot:run', '-Dspring-boot.run.profiles=local', '-Dspring-boot.run.arguments=--server.port=18083'], cwd=str(root), stdout=out.open('w', encoding='utf-8'), stderr=err.open('w', encoding='utf-8'))
try:
    health = None
    for _ in range(60):
        time.sleep(1)
        if proc.poll() is not None:
            break
        try:
            with urllib.request.urlopen('http://127.0.0.1:18083/api/health', timeout=2) as r:
                health = r.read().decode('utf-8')
                break
        except Exception:
            pass
    print('PROCESS_EXIT=', proc.poll())
    print('HEALTH=', health)
    try:
        with urllib.request.urlopen('http://127.0.0.1:18083/api/db/ping', timeout=10) as r:
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
