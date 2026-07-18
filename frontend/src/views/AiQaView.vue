<template>
  <section class="page-template ai-page">
    <div class="hero-copy">
      <p class="eyebrow">RAG ASSISTANT</p>
      <h2>围绕劳动合同法规与案例提问</h2>
      <p>后续将接入法规政策知识库、合同条款片段和咨询历史，返回可追溯的依据和处理建议。</p>
    </div>

    <div class="two-column-grid">
      <el-card class="paper-panel input-panel" shadow="never">
        <template #header>
          <div class="panel-title">
            <el-icon><ChatDotRound /></el-icon>
            <span>输入咨询问题</span>
          </div>
        </template>
        <el-input
          type="textarea"
          :rows="13"
          placeholder="例如：公司没有提前三十天通知就解除劳动合同，我能要求哪些补偿？"
          disabled
        />
        <div class="panel-actions">
          <span class="hint-line"><el-icon><InfoFilled /></el-icon> 当前阶段仅展示 UI，不提交业务数据</span>
          <el-button class="ink-button" disabled>生成答复</el-button>
        </div>
      </el-card>

      <el-card class="paper-panel result-panel" shadow="never">
        <template #header>
          <div class="panel-title">
            <el-icon><DataAnalysis /></el-icon>
            <span>答复结构预览</span>
          </div>
        </template>
        <div class="case-card high">
          <div class="case-topline">
            <h3>解除劳动合同补偿</h3>
            <el-tag type="warning" effect="plain">需结合事实</el-tag>
          </div>
          <p class="case-label">结论摘要</p>
          <div class="quote-box">可能涉及经济补偿、违法解除赔偿或代通知金，需结合解除理由、通知方式和工作年限判断。</div>
          <div class="case-meta">
            <div><span>依据类型</span><strong>法律法规</strong></div>
            <div><span>引用状态</span><strong>待接入知识库</strong></div>
          </div>
        </div>

        <div class="case-card muted">
          <div class="case-topline">
            <h3>建议准备证据</h3>
            <el-tag type="success" effect="plain">清单项</el-tag>
          </div>
          <p>劳动合同、解除通知、工资流水、社保记录、沟通记录、考勤记录等。</p>
        </div>
      </el-card>
    </div>
  </section>
</template>
