from pathlib import Path
p = Path(r'G:\AI-law-creater\frontend\src\styles.css')
text = p.read_text(encoding='utf-8')
append = '''

/* Stitch UI labor assistant alignment additions */
.brand-block.with-seal {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 18px;
  align-items: start;
}

.seal-mark {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: 7px;
  color: #ffffff;
  background: var(--ink-panel);
  box-shadow: 0 8px 18px rgba(23, 50, 77, 0.16);
}

.seal-mark .el-icon { font-size: 22px; }

.policy-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 16px;
  border-radius: 5px;
  color: #00504d;
  background: #e0f7f5;
  font-weight: 600;
}

.consult-hero {
  padding-top: 4px;
}

.history-heading-row {
  display: grid;
  grid-template-columns: 1fr minmax(420px, 0.85fr);
  gap: 32px;
  align-items: end;
  margin-bottom: 28px;
}

.history-tools {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 110px 100px;
  gap: 14px;
  align-items: center;
  margin-bottom: 46px;
}

.history-list {
  display: grid;
  gap: 18px;
}

.history-card {
  position: relative;
  padding: 28px 30px;
  border: 1px solid #dfe5eb;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(23, 50, 77, 0.04);
}

.history-card h3 {
  margin: 14px 34px 16px 0;
  font-family: 'Noto Serif SC', serif;
  font-size: 26px;
  line-height: 1.35;
  color: var(--ink);
}

.history-card p {
  margin: 0 0 24px;
  padding-bottom: 22px;
  border-bottom: 1px solid #e5e7eb;
  color: #26323d;
  line-height: 1.75;
}

.history-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  color: #26323d;
  font-size: 14px;
}

.history-meta span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.history-meta .bookmark {
  margin-left: auto;
  font-size: 24px;
  color: #26323d;
}

.saved-grid,
.help-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 22px;
}

.help-grid { grid-template-columns: repeat(2, 1fr); }

.saved-card {
  min-height: 220px;
  padding: 28px;
  border: 1px solid #dfe5eb;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(23, 50, 77, 0.04);
}

.saved-card span {
  display: inline-flex;
  padding: 6px 10px;
  border-radius: 5px;
  color: #00504d;
  background: #e0f7f5;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}

.saved-card h3 {
  margin: 22px 0 12px;
  font-family: 'Noto Serif SC', serif;
  font-size: 22px;
  line-height: 1.4;
  color: var(--ink);
}

.saved-card p {
  margin: 0;
  line-height: 1.75;
  color: #26323d;
}

.brief-list {
  margin: 0;
  padding: 26px 32px 30px 50px;
  line-height: 1.9;
  color: #26323d;
}

.brief-list li + li { margin-top: 10px; }

.strong-box {
  font-weight: 700;
}

@media (max-width: 1200px) {
  .history-heading-row,
  .history-tools,
  .saved-grid,
  .help-grid {
    grid-template-columns: 1fr;
  }

  .history-tools { margin-bottom: 10px; }
}
'''
if '/* Stitch UI labor assistant alignment additions */' not in text:
    text += append
p.write_text(text, encoding='utf-8')
print('CSS_APPENDED', p, p.stat().st_size)
