from pathlib import Path
import re
p = Path(r'G:\AI-law-creater\backend\db\init.sql')
text = p.read_text(encoding='utf-8-sig')
tables = ['eos_repair_order', 'kb_document', 'kb_chunk', 'kb_qa_log', 'kb_qa_reference']
required_global = ['DROP TABLE IF EXISTS', 'ENGINE=InnoDB', 'DEFAULT CHARSET=utf8mb4']
missing = []
for item in required_global:
    if item not in text:
        missing.append(item)
for table in tables:
    if f'DROP TABLE IF EXISTS {table};' not in text:
        missing.append(f'drop:{table}')
    if f'CREATE TABLE {table}' not in text:
        missing.append(f'create:{table}')
    block_match = re.search(rf'CREATE TABLE {table} \((.*?)\) ENGINE=InnoDB', text, re.S)
    if not block_match:
        missing.append(f'block:{table}')
        continue
    block = block_match.group(1)
    for col in ['id BIGINT UNSIGNED', 'created_at DATETIME(3)', 'updated_at DATETIME(3)']:
        if col not in block:
            missing.append(f'{table}:{col}')
    if 'group_code VARCHAR(32)' not in block:
        missing.append(f'{table}:group_code')

field_checks = {
    'eos_repair_order': ['order_no VARCHAR(64)', 'equipment_name VARCHAR(128)', 'fault_title VARCHAR(200)', 'fault_desc TEXT', 'fault_reason TEXT', 'solution TEXT', 'result_desc TEXT', 'repair_person VARCHAR(64)', 'repair_time DATETIME(3)'],
    'kb_document': ['title VARCHAR(200)', 'source_type VARCHAR(32)', 'file_name VARCHAR(255)', 'file_url VARCHAR(500)', 'status VARCHAR(32)', 'remark VARCHAR(500)'],
    'kb_chunk': ['document_id BIGINT UNSIGNED', 'source_type VARCHAR(32)', 'source_id BIGINT UNSIGNED', 'chunk_index INT', 'content TEXT', 'content_hash VARCHAR(64)', 'es_doc_id VARCHAR(128)', 'status VARCHAR(32)'],
    'kb_qa_log': ['question TEXT', 'answer TEXT', 'model_name VARCHAR(100)'],
    'kb_qa_reference': ['qa_log_id BIGINT UNSIGNED', 'chunk_id BIGINT UNSIGNED', 'score DECIMAL(10, 6)', 'content_snapshot TEXT'],
}
for table, fields in field_checks.items():
    block = re.search(rf'CREATE TABLE {table} \((.*?)\) ENGINE=InnoDB', text, re.S).group(1)
    for field in fields:
        if field not in block:
            missing.append(f'{table}:{field}')

print('SQL:', p)
print('SIZE:', p.stat().st_size)
print('TABLE_COUNT:', len(re.findall(r'CREATE TABLE ', text)))
print('MISSING:', missing)
if missing:
    raise SystemExit(1)
print('VERIFY=OK')
