from pathlib import Path
p = Path(r'G:\AI-law-creater\backend\pom.xml')
text = p.read_text(encoding='utf-8-sig')
text = text.replace('<groupId>com.ailaw</groupId>', '<groupId>com.laborlaw</groupId>', 1)
text = text.replace('EOS repair work order and enterprise maintenance document RAG knowledge base demo backend', 'Labor contract legal assistant RAG knowledge base demo backend')
p.write_text(text, encoding='utf-8')
print('updated', p)
