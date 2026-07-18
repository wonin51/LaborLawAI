from pathlib import Path
p = Path(r'G:\AI-law-creater\README.md')
text = p.read_text(encoding='utf-8')
if '## 项目简介' not in text:
    text = text.replace('# EOS 维修 RAG 知识库实训项目\n\n', '# EOS 维修 RAG 知识库实训项目\n\n## 项目简介\n\n', 1)
p.write_text(text, encoding='utf-8')
print('updated', p)
