from pathlib import Path
p = Path(r'G:\AI-law-creater\frontend\src\styles.css')
text = p.read_text(encoding='utf-8')
old = '''.rail-menu .el-menu-item {
  height: 62px;
  margin: 0 0 10px;
  padding-left: 18px !important;
  border-radius: 5px;
  color: #26323d;
  border-left: 4px solid transparent;
}

.rail-menu .el-menu-item .el-icon { font-size: 24px; }
'''
new = '''.rail-menu .el-menu-item {
  height: 72px;
  margin: 0 0 10px;
  padding: 0 16px 0 18px !important;
  display: flex;
  align-items: center;
  gap: 12px;
  border-radius: 5px;
  color: #26323d;
  border-left: 4px solid transparent;
  line-height: 1.2;
  overflow: hidden;
}

.rail-menu .el-menu-item .el-icon {
  flex: 0 0 24px;
  margin-right: 0;
  font-size: 24px;
}
'''
if old not in text:
    raise SystemExit('menu item block not found')
text = text.replace(old, new)
old2 = '''.menu-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-left: 10px;
}

.menu-copy strong { font-size: 17px; font-weight: 700; }
.menu-copy span { font-size: 12px; color: #6b7280; }
'''
new2 = '''.menu-copy {
  min-width: 0;
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  margin-left: 0;
  line-height: 1.2;
  overflow: hidden;
}

.menu-copy strong {
  display: block;
  font-size: 17px;
  font-weight: 700;
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.menu-copy span {
  display: block;
  font-size: 12px;
  line-height: 1.35;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
'''
if old2 not in text:
    raise SystemExit('menu copy block not found')
text = text.replace(old2, new2)
p.write_text(text, encoding='utf-8')
print('UPDATED', p)
