package com.teddy.legal.service;

import com.teddy.legal.dto.LegalDocumentCreateRequest;
import com.teddy.legal.dto.LegalDocumentQueryRequest;
import com.teddy.legal.entity.LegalDocumentEntity;
import com.teddy.legal.vo.LegalDocumentDetailVO;
import com.teddy.legal.vo.LegalDocumentSummaryVO;
import com.teddy.legal.vo.PageResult;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class LegalDocumentServiceImpl implements LegalDocumentService {

    private final AtomicLong idSequence = new AtomicLong(0);
    private final CopyOnWriteArrayList<LegalDocumentEntity> store = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        if (!store.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        store.add(new LegalDocumentEntity(
                nextId(),
                "中华人民共和国劳动合同法",
                "法律法规",
                "全国",
                LocalDate.of(2012, 12, 28),
                "现行有效",
                "https://www.gov.cn/flfg/2012-12/28/content_2305769.htm",
                "规定劳动合同订立、履行、解除、终止及相关责任。",
                "劳动合同法是劳动争议咨询中的核心依据之一，尤其涉及未签书面劳动合同、试用期、经济补偿、违法解除等问题。",
                now.minusDays(6),
                now.minusDays(1)
        ));
        store.add(new LegalDocumentEntity(
                nextId(),
                "劳动争议调解仲裁法",
                "司法解释",
                "全国",
                LocalDate.of(2007, 12, 29),
                "现行有效",
                "https://www.gov.cn/ziliao/flfg/2007-12/29/content_848789.htm",
                "明确劳动争议协商、调解、仲裁的程序和时效要求。",
                "在劳动争议场景里，仲裁时效、管辖、举证和程序节点都需要优先确认。",
                now.minusDays(5),
                now.minusHours(12)
        ));
        store.add(new LegalDocumentEntity(
                nextId(),
                "未签劳动合同常见问答",
                "项目知识",
                "项目资料",
                LocalDate.of(2026, 7, 18),
                "已启用",
                "https://internal.example/faq/no-contract",
                "面向实习和求职场景的咨询摘要，梳理未签合同、工资、社保和证据准备。",
                "用于项目演示的知识库样例，后续会替换为真实入库文档和分片内容。",
                now.minusDays(2),
                now
        ));
        idSequence.set(store.stream().mapToLong(LegalDocumentEntity::id).max().orElse(0));
    }

    @Override
    public PageResult<LegalDocumentSummaryVO> page(LegalDocumentQueryRequest request) {
        int pageNo = request.pageNo() == null || request.pageNo() < 1 ? 1 : request.pageNo();
        int pageSize = request.pageSize() == null || request.pageSize() < 1 ? 10 : request.pageSize();

        List<LegalDocumentSummaryVO> filtered = store.stream()
                .filter(item -> matches(item.title(), request.title()))
                .filter(item -> matches(item.sourceType(), request.sourceType()))
                .filter(item -> matches(item.jurisdiction(), request.jurisdiction()))
                .filter(item -> matches(item.effectiveStatus(), request.effectiveStatus()))
                .sorted(Comparator.comparing(LegalDocumentEntity::updatedAt).reversed())
                .map(this::toSummary)
                .collect(Collectors.toList());

        int fromIndex = Math.min((pageNo - 1) * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        return new PageResult<>(filtered.subList(fromIndex, toIndex), filtered.size(), pageNo, pageSize);
    }

    @Override
    public LegalDocumentDetailVO getById(Long id) {
        LegalDocumentEntity entity = store.stream()
                .filter(item -> Objects.equals(item.id(), id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("文档不存在"));
        return toDetail(entity);
    }

    @Override
    public LegalDocumentDetailVO create(LegalDocumentCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LegalDocumentEntity entity = new LegalDocumentEntity(
                nextId(),
                request.title(),
                request.sourceType(),
                request.jurisdiction(),
                request.publishDate(),
                request.effectiveStatus() == null || request.effectiveStatus().isBlank() ? "草稿" : request.effectiveStatus(),
                request.sourceUrl(),
                request.summary(),
                request.content(),
                now,
                now
        );
        store.add(0, entity);
        return toDetail(entity);
    }

    private long nextId() {
        return idSequence.incrementAndGet();
    }

    private boolean matches(String value, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        if (value == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private LegalDocumentSummaryVO toSummary(LegalDocumentEntity entity) {
        return new LegalDocumentSummaryVO(
                entity.id(),
                entity.title(),
                entity.sourceType(),
                entity.jurisdiction(),
                entity.publishDate(),
                entity.effectiveStatus(),
                entity.sourceUrl(),
                entity.summary(),
                entity.updatedAt()
        );
    }

    private LegalDocumentDetailVO toDetail(LegalDocumentEntity entity) {
        return new LegalDocumentDetailVO(
                entity.id(),
                entity.title(),
                entity.sourceType(),
                entity.jurisdiction(),
                entity.publishDate(),
                entity.effectiveStatus(),
                entity.sourceUrl(),
                entity.summary(),
                entity.content(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }
}
