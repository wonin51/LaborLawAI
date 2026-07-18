<template>
  <section class="page-template history-page">
    <div class="history-heading-row">
      <div class="hero-copy compact">
        <p class="eyebrow">CONSULTATION HISTORY</p>
        <h2>咨询历史</h2>
        <p>查看、继续提问或导出您过去的劳动法咨询记录。</p>
      </div>
      <div class="history-tools">
        <el-input placeholder="搜索问题或关键词..." disabled>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button disabled>全部类型</el-button>
        <el-button disabled>近30天</el-button>
      </div>
    </div>

    <div class="history-list">
      <article class="history-card">
        <el-tag effect="plain" type="success">薪酬福利</el-tag>
        <h3>关于试用期工资低于当地最低工资标准的维权</h3>
        <p><strong>结论摘要：</strong>试用期工资不得低于本单位相同岗位最低档工资的 80% 或劳动合同约定工资的 80%，且不得低于用人单位所在地最低工资标准。</p>
        <div class="history-meta">
          <span><el-icon><Clock /></el-icon> 2026-07-18 14:30 更新</span>
          <span><el-icon><ScaleToOriginal /></el-icon> 引用法规：3项</span>
          <el-icon class="bookmark"><Bookmark /></el-icon>
        </div>
      </article>

      <article class="history-card">
        <el-tag effect="plain" type="info">解除辞退</el-tag>
        <h3>公司未提前通知单方面解除劳动合同的赔偿计算</h3>
        <p><strong>结论摘要：</strong>若认定为违法解除，可主张经济赔偿金；若合法但未提前 30 天通知，可主张代通知金加经济补偿金。</p>
        <div class="history-meta">
          <span><el-icon><Clock /></el-icon> 2026-07-16 09:15 更新</span>
          <span><el-icon><ScaleToOriginal /></el-icon> 引用法规：5项</span>
          <el-icon class="bookmark"><Bookmark /></el-icon>
        </div>
      </article>
    </div>
  </section>
</template>
