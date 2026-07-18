<template>
  <section class="page-grid">
    <el-card class="hero-card ai-card" shadow="never">
      <p class="eyebrow">入口三</p>
      <h3>AI 法律问答</h3>
      <p>面向就业签约过程中的疑问，后续可结合知识库检索与大模型生成回答，提供更贴近大学生就业场景的解释。</p>
      <div class="chat-preview">
        <div class="bubble user">兼职合同写“离职扣一个月工资”，合法吗？</div>
        <div class="bubble assistant">建议重点核查工资支付、违约责任和解除条款，后续将提供引用依据。</div>
      </div>
      <el-button type="warning" disabled>后续接入 AI 问答</el-button>
    </el-card>

    <el-card class="info-card" shadow="never">
      <h4>基础提示</h4>
      <p>本系统仅作为学习与辅助参考，不替代律师正式法律意见。</p>
    </el-card>
  </section>
</template>
