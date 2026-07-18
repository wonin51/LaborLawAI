from pathlib import Path
import re
p=Path(r'G:\AI-law-creater\需求文档\2026-07-18-labor-law-database-design (1).md')
text=p.read_text(encoding='utf-8')
# headings with backtick table names
for m in re.finditer(r'^####\s+\d+(?:\.\d+)*\s+`([^`]+)`\s*\n(.*?)(?=^####\s+\d|^###\s+\d|^##\s+\d|\Z)', text, re.S|re.M):
    name=m.group(1)
    block=m.group(2)
    desc=block.strip().splitlines()[0] if block.strip() else ''
    # count markdown rows starting | excluding separator
    rows=[]
    for line in block.splitlines():
        if line.startswith('|') and not re.match(r'^\|\s*-', line):
            rows.append(line)
    print('\nTABLE', name)
    print('DESC', desc)
    print('ROWS', max(0,len(rows)-1))
    # print fields
    for line in rows[:30]:
        print(line)
