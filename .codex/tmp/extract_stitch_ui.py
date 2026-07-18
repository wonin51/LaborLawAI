from pathlib import Path
import zipfile, shutil
root=Path(r'G:\AI-law-creater')
zip_path=root/'需求文档'/'stitch_ui.zip'
dest=root/'.codex'/'tmp'/'stitch_ui_template'
if dest.exists(): shutil.rmtree(dest)
dest.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(zip_path) as z:
    z.extractall(dest)
print('EXTRACTED', dest)
for p in sorted(dest.rglob('*')):
    if p.is_file():
        print(p, p.suffix, p.stat().st_size)
