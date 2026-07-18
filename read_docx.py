from docx import Document
import sys

doc = Document(r"D:\2026-shixi\劳动合同法律助手需求分析文档_新增反馈入库需求.docx")
for para in doc.paragraphs:
    print(para.text)
