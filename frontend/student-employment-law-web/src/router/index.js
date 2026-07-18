import { createRouter, createWebHistory } from 'vue-router'
import ContractReview from '../views/ContractReview.vue'
import LegalKnowledge from '../views/LegalKnowledge.vue'
import AiConsultation from '../views/AiConsultation.vue'

const routes = [
  { path: '/', redirect: '/contract-review' },
  { path: '/contract-review', name: 'contractReview', component: ContractReview, meta: { title: '劳务合同审查' } },
  { path: '/legal-knowledge', name: 'legalKnowledge', component: LegalKnowledge, meta: { title: '就业法律知识' } },
  { path: '/ai-consultation', name: 'aiConsultation', component: AiConsultation, meta: { title: 'AI 法律问答' } }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
