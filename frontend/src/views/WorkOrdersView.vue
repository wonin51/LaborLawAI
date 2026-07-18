<template>
  <section class="page-template consult-page">
    <div class="hero-copy centered consult-hero">
      <p class="eyebrow">LEGAL CONSULTATION</p>
      <h2>把劳动合同问题说清楚</h2>
      <p>根据现行法律法规、司法解释和人社政策，为你整理判断依据、处理步骤和证据清单。</p>
    </div>

    <el-card class="ask-card" shadow="never">
      <el-input
        type="textarea"
        :rows="8"
        placeholder="例如：入职两个月还没有签劳动合同，我可以要求公司赔偿吗？"
        disabled
      />
      <div class="ask-toolbar">
        <div class="toolbar-links">
          <span><el-icon><Clipboard /></el-icon> 粘贴合同条款</span>
          <span><el-icon><Upload /></el-icon> 上传示例文本</span>
        </div>
        <el-button class="ink-button" disabled>
          <el-icon><Promotion /></el-icon> 发送咨询
        </el-button>
      </div>
      <p class="safe-hint"><el-icon><Warning /></el-icon> 请勿输入身份证号、银行卡号等敏感信息。</p>
    </el-card>

    <section class="faq-section">
      <h3>高频问题</h3>
      <div class="faq-grid">
        <div class="faq-card">
          <span>加班费</span>
          <strong>周末总是被叫去加班，没给加班费怎么办？</strong>
          <el-icon><Right /></el-icon>
        </div>
        <div class="faq-card">
          <span>未签合同</span>
          <strong>工作半年了公司一直不跟我签合同合法吗？</strong>
          <el-icon><Right /></el-icon>
        </div>
        <div class="faq-card">
          <span>试用期补偿</span>
          <strong>试用期最后一天被辞退，有 N+1 补偿吗？</strong>
          <el-icon><Right /></el-icon>
        </div>
        <div class="faq-card">
          <span>拖欠工资</span>
          <strong>公司以效益不好为由拖欠工资两个月，如何维权？</strong>
          <el-icon><Right /></el-icon>
        </div>
      </div>
    </section>

    <section class="process-section">
      <h3>你将获得什么</h3>
      <div class="process-line">
        <div class="process-step"><span>01</span><strong>结论摘要</strong></div>
        <div class="process-step"><span>02</span><strong>法律依据</strong></div>
        <div class="process-step"><span>03</span><strong>维权流程</strong></div>
        <div class="process-step"><span>04</span><strong>证据清单</strong></div>
        <div class="process-step"><span>05</span><strong>注意事项</strong></div>
      </div>
    </section>
  </section>
</template>
