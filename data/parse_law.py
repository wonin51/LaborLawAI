import re
import json

# ========== 1. 将你上面的文本粘贴到下面的变量中 ==========

try:
    with open("law.txt", "r", encoding="utf-8") as f:
        raw_text = f.read()
except FileNotFoundError:
    print("❌ 请将docx内容复制到 law.txt 文件中，并放在脚本同目录下")
    exit(1)
    
# ========== 2. 正则提取所有“第X条” ==========
# 匹配模式：第(零一二三四五六七八九十百千万)+条，后面跟内容，直到遇到下一条或文件结束
pattern = r'(第[零一二三四五六七八九十百千万]+条)\s*(.*?)(?=\n第[零一二三四五六七八九十百千万]+条|$)'

matches = re.findall(pattern, raw_text, re.DOTALL)

# ========== 3. 清洗并组装成JSON ==========
articles = []
for idx, (title, content) in enumerate(matches, 1):
    # 去掉多余的换行和空格
    content = re.sub(r'\s+', ' ', content).strip()
    articles.append({
        "id": f"第{idx}条",
        "title": title,
        "content": content,
        "full_text": f"{title} {content}"
    })

# ========== 4. 保存为JSON文件 ==========
with open("law.json", "w", encoding="utf-8") as f:
    json.dump(articles, f, ensure_ascii=False, indent=2)

print(f"✅ 共提取 {len(articles)} 条条款")
print(f"✅ 已保存到 law.json")
print(f"✅ 示例：{articles[0]['title']} -> {articles[0]['content'][:50]}...")