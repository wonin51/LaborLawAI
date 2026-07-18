from pathlib import Path
root=Path(r'G:\AI-law-creater\.codex\tmp\requirements_text')
keywords=['EOS','维修','工单','企业维修','维修文档','RAG','知识库','后端','Spring','MyBatis','Elasticsearch','向量','AI']
print('file\t'+'\t'.join(keywords))
for p in sorted(root.glob('*.txt')):
    text=p.read_text(encoding='utf-8', errors='ignore').lower()
    counts=[str(text.count(k.lower())) for k in keywords]
    print(p.name+'\t'+'\t'.join(counts))
