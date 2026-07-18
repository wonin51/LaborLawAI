<template>
  <section class="page-template review-page">
    <div class="hero-copy compact">
      <p class="eyebrow">CONTRACT REVIEW</p>
      <h2>检查劳动合同条款中的常见风险</h2>
      <p>识别常见明显风险，不替代完整的律师合同审查。</p>
    </div>

    <div class="review-grid">
      <el-card class="paper-panel doc-input" shadow="never">
        <template #header>
          <div class="panel-title split-title">
            <span>输入条款文本</span>
            <el-button link type="primary" disabled>
              <el-icon><Upload /></el-icon> 上传文本
            </el-button>
          </div>
        </template>
        <el-input
          type="textarea"
          :rows="18"
          placeholder="在此粘贴您的劳动合同条款..."
          disabled
        />
        <div class="review-footer">
          <el-button text disabled>清空</el-button>
          <el-button class="ink-button" disabled>开始检查</el-button>
        </div>
      </el-card>

      <el-card class="paper-panel doc-result" shadow="never">
        <template #header>
          <div class="panel-title">
            <el-icon><DocumentChecked /></el-icon>
            <span>审查结果</span>
          </div>
        </template>

        <div class="risk-card danger-line">
          <div class="risk-title">
            <h3>竞业限制补偿约定不明确</h3>
            <el-tag type="danger" effect="plain">高风险</el-tag>
          </div>
          <p class="case-label">原始片段</p>
          <div class="quote-box">“乙方离职后两年内不得从事同类业务，甲方视情况给予一定补偿。”</div>
          <div class="risk-columns">
            <div><span>风险类型</span><strong>约定不明 / 显失公平</strong></div>
            <div><span>原因</span><strong>未明确具体补偿金额和发放方式。</strong></div>
          </div>
          <p class="case-label">修改建议</p>
          <div class="quote-box strong-box">建议补充明确的月度补偿标准，通常不低于离职前十二个月平均工资的 30%。</div>
        </div>

        <div class="risk-card calm-line">
          <div class="risk-title">
            <h3>工作地点表述宽泛</h3>
            <el-tag type="info" effect="plain">低风险</el-tag>
          </div>
          <p class="case-label">原始片段</p>
          <div class="quote-box">“工作地点：全国范围内根据公司业务需要调配。”</div>
          <p class="case-label">修改建议</p>
          <div class="quote-box">虽属常见，但建议尽可能细化核心工作常驻地，避免未来跨省调岗引发争议。</div>
        </div>
      </el-card>
    </div>
  </section>
</template>
