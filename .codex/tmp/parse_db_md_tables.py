from pathlib import Path
import re
p = Path(r'G:\AI-law-creater\需求文档\2026-07-18-labor-law-database-design (1).md')
text = p.read_text(encoding='utf-8-sig')
# print headings containing backticked table names
for m in re.finditer(r'^(#{3,5})\s+.*?`([^`]+)`.*$', text, re.M):
    print(m.group(1), m.group(2), '::', m.group(0))
print('\n--- all backticked likely table names ---')
seen=[]
for name in re.findall(r'`([a-z][a-z0-9_]+)`', text):
    if name not in seen:
        seen.append(name)
for i,n in enumerate(seen,1):
    print(i,n)
