from pathlib import Path
import re
text=Path(r'G:\AI-law-creater\需求文档\2026-07-18-labor-law-database-design (1).md').read_text(encoding='utf-8')
for table in ['sys_account_role','sys_role_permission','qa_answer','kb_document','kb_document_version','kb_chunk_ref','qa_answer_citation','ai_generation_run']:
    m=re.search(rf'####\s+[^\n]*`{table}`(.*?)(?=^####\s+|^###\s+|^##\s+|\Z)', text, re.S|re.M)
    print('\n---', table, '---')
    if m:
        print(m.group(0)[:3000])
