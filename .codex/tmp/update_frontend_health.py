from pathlib import Path
p = Path(r'G:\AI-law-creater\frontend\src\App.vue')
text = p.read_text(encoding='utf-8')
text = text.replace("""    if (data?.status === 'ok') {
      healthStatus.value = 'ok'
      healthText.value = '后端服务已连接'
      return
    }
""", """    const status = String(data?.status || '').toLowerCase()
    if (status === 'ok' || status === 'up') {
      healthStatus.value = 'ok'
      healthText.value = '后端服务已连接'
      return
    }
""")
p.write_text(text, encoding='utf-8')
print('updated', p)
