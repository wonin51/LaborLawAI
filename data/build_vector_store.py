# build_vector_store.py (适配你的 JSON 结构：id, title, content, full_text)
import json
import os

from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma

# 设置国内镜像加速（如果下载模型慢）
os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"

# 1. 加载 JSON 数据
with open("LaborContractLaw.json", "r", encoding="utf-8") as f:
    law_data = json.load(f)

print(f"📄 共加载 {len(law_data)} 条记录")

# 2. 构造文本块：用 content 作为主体，title 作为条款名称
texts = []
metadatas = []
for item in law_data:
    # 用 title + content 拼成完整文本（或直接用 full_text）
    full_text = item.get("full_text") or f"{item.get('title', '')} {item.get('content', '')}"
    texts.append(full_text)
    metadatas.append({
        "id": item.get("id", ""),
        "title": item.get("title", ""),
        "source": item.get("title", "")
    })

# 3. 二次切分（长文本按句号切分）
splitter = RecursiveCharacterTextSplitter(
    chunk_size=500,
    chunk_overlap=50,
    separators=["。", "；", "，", "\n"]
)

final_texts = []
final_metadatas = []
for text, meta in zip(texts, metadatas):
    chunks = splitter.split_text(text)
    for chunk in chunks:
        final_texts.append(chunk)
        final_metadatas.append(meta)  # 每个子块继承原条款的元数据

print(f"✂️ 切分为 {len(final_texts)} 个文本块")

# 4. 使用免费本地嵌入模型（支持中文）
print("🔄 正在加载中文嵌入模型（首次运行需下载约400MB，请耐心等待）...")
embeddings = HuggingFaceEmbeddings(
    model_name="sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
)

# 5. 存入向量库
vector_store = Chroma.from_texts(
    final_texts,
    embeddings,
    metadatas=final_metadatas,
    persist_directory="./vector_store"
)
vector_store.persist()

print(f"✅ 已存入 {len(final_texts)} 个文本块，保存在 ./vector_store")

# 6. 测试查询
query = "试用期最长多久？"
results = vector_store.similarity_search(query, k=3)
print("\n🔍 测试查询结果：")
for i, doc in enumerate(results):
    title = doc.metadata.get("title", "未知条款")
    print(f"{i+1}. {title}：{doc.page_content[:50]}...")