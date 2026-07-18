from pathlib import Path
import re
p = Path(r'G:\AI-law-creater\需求文档\2026-07-18-labor-law-database-design (1).md')
text = p.read_text(encoding='utf-8-sig')
# Extract 5.1 to 5.5 only; print table sections compact
matches=list(re.finditer(r'^####\s+5\.\d+\.\d+\s+`([^`]+)`\s*$', text, re.M))
for idx,m in enumerate(matches):
    name=m.group(1)
    start=m.start()
    end=matches[idx+1].start() if idx+1 < len(matches) else len(text)
    section=text[start:end]
    # trim after next major section 6 if in last
    section=section.split('\n## 6.',1)[0]
    print('\n'+'='*80)
    print(name)
    print('='*80)
    # print up to constraints before long details
    print(section[:2500])
