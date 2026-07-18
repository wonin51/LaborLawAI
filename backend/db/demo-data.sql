-- ============================================================
-- 法律文档示例数据 (demo-data.sql)
-- 基于 backend/db/init.sql 表结构补充
-- 适用数据库: MySQL 8.0+
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 说明：以下数据已在 init.sql 中初始化，无需重复插入
-- ============================================================
-- 【sys_role】角色数据已存在：ADMIN、REVIEWER
-- 【qa_topic】问题分类数据已存在，包含：
--    - OVERTIME    (加班工资)
--    - PROBATION   (试用期)
--    - CONTRACT_SIGN (合同签订/未签合同)
-- 【ai_prompt】提示词数据已存在：LABOR_LAW_QA
-- 【ai_prompt_version】提示词版本数据已存在：v1.0
-- 【sys_account】管理员账号已存在：admin
-- 【sys_account_role】admin 与 ADMIN 角色关联已存在
-- ============================================================


-- ============================================================
-- 1. 法律文档示例数据 (kb_document / kb_document_version)
-- ============================================================
-- 共插入 10 条知识文档，覆盖 LAW、JUDICIAL_INTERPRETATION、
-- POLICY、FAQ、SAMPLE_CONTRACT 五种类型
-- 所有文档 status 统一为 DRAFT（待后续索引）
-- ============================================================


-- --------------------------------------------------
-- 文档 1：法律 — 《中华人民共和国劳动合同法》
-- --------------------------------------------------
-- 【正文摘要】
-- 第十条 建立劳动关系，应当订立书面劳动合同。已建立劳动关系，未同时订立书面劳动合同的，
--        应当自用工之日起一个月内订立书面劳动合同。
-- 第三十六条 用人单位与劳动者协商一致，可以解除劳动合同。
-- 第三十九条 劳动者有下列情形之一的，用人单位可以解除劳动合同：
--        （一）在试用期间被证明不符合录用条件的；
--        （二）严重违反用人单位的规章制度的；
--        （三）严重失职，营私舞弊，给用人单位造成重大损害的。
-- 第四十六条 有下列情形之一的，用人单位应当向劳动者支付经济补偿：
--        （二）用人单位依照本法第三十六条规定向劳动者提出解除劳动合同并与劳动者协商一致解除劳动合同的。
-- 第四十七条 经济补偿按劳动者在本单位工作的年限，每满一年支付一个月工资的标准向劳动者支付。
--        六个月以上不满一年的，按一年计算；不满六个月的，向劳动者支付半个月工资的经济补偿。
-- 【标签】劳动合同,订立,履行,变更,解除,经济补偿

INSERT INTO `kb_document` (
    `public_id`, `title`, `document_type`, `issuing_authority`,
    `canonical_source_url`, `jurisdiction_code`, `scope_text`,
    `authority_level`, `current_version_id`, `status`, `lock_version`
) VALUES (
    '01HZY8QX3K7V2MBR5CJ9W4D6N0',
    '中华人民共和国劳动合同法',
    'LAW',
    '全国人民代表大会常务委员会',
    'https://www.gov.cn/flfg/2007-06/29/content_693995.htm',
    'CN',
    '劳动合同,订立,履行,变更,解除,经济补偿',
    10,
    NULL,
    'DRAFT',
    0
);
SET @doc1_id = LAST_INSERT_ID();

INSERT INTO `kb_document_version` (
    `document_id`, `version_no`, `document_number`, `source_url`,
    `published_date`, `effective_date`, `expiry_date`,
    `validity_status`, `review_status`, `content_sha256`, `language_code`
) VALUES (
    @doc1_id, 'v1.0', '主席令第六十五号',
    'https://www.gov.cn/flfg/2007-06/29/content_693995.htm',
    '2007-06-29', '2008-01-01', NULL,
    'DRAFT', 'PENDING',
    SHA2('中华人民共和国劳动合同法_2012修正_v1.0', 256),
    'zh-CN'
);
SET @doc1_version_id = LAST_INSERT_ID();
UPDATE `kb_document` SET `current_version_id` = @doc1_version_id WHERE `id` = @doc1_id;


-- --------------------------------------------------
-- 文档 2：法律 — 《中华人民共和国劳动法》
-- --------------------------------------------------
-- 【正文摘要】
-- 第三十六条 国家实行劳动者每日工作时间不超过八小时、平均每周工作时间不超过四十四小时的工时制度。
-- 第四十四条 有下列情形之一的，用人单位应当按照下列标准支付高于劳动者正常工作时间工资的工资报酬：
--        （一）安排劳动者延长工作时间的，支付不低于工资的百分之一百五十的工资报酬；
--        （二）休息日安排劳动者工作又不能安排补休的，支付不低于工资的百分之二百的工资报酬；
--        （三）法定休假日安排劳动者工作的，支付不低于工资的百分之三百的工资报酬。
-- 第五十条 工资应当以货币形式按月支付给劳动者本人。不得克扣或者无故拖欠劳动者的工资。
-- 第七十九条 劳动争议发生后，当事人可以向本单位劳动争议调解委员会申请调解；
--        调解不成，可以向劳动争议仲裁委员会申请仲裁；对仲裁裁决不服的，可以向人民法院提起诉讼。
-- 【标签】工作时间,休息休假,工资支付,加班工资,劳动争议

INSERT INTO `kb_document` (
    `public_id`, `title`, `document_type`, `issuing_authority`,
    `canonical_source_url`, `jurisdiction_code`, `scope_text`,
    `authority_level`, `current_version_id`, `status`, `lock_version`
) VALUES (
    '01HZY8QX3K7V2MBR5CJ9W4D6N1',
    '中华人民共和国劳动法',
    'LAW',
    '全国人民代表大会常务委员会',
    'https://www.gov.cn/flfg/1994-07/05/content_1488.htm',
    'CN',
    '工作时间,休息休假,工资支付,加班工资,劳动争议',
    10,
    NULL,
    'DRAFT',
    0
);
SET @doc2_id = LAST_INSERT_ID();

INSERT INTO `kb_document_version` (
    `document_id`, `version_no`, `document_number`, `source_url`,
    `published_date`, `effective_date`, `expiry_date`,
    `validity_status`, `review_status`, `content_sha256`, `language_code`
) VALUES (
    @doc2_id, 'v1.0', '主席令第二十八号',
    'https://www.gov.cn/flfg/1994-07/05/content_1488.htm',
    '1994-07-05', '1995-01-01', NULL,
    'DRAFT', 'PENDING',
    SHA2('中华人民共和国劳动法_2018修正_v1.0', 256),
    'zh-CN'
);
SET @doc2_version_id = LAST_INSERT_ID();
UPDATE `kb_document` SET `current_version_id` = @doc2_version_id WHERE `id` = @doc2_id;


-- --------------------------------------------------
-- 文档 3：法律 — 《中华人民共和国劳动争议调解仲裁法》
-- --------------------------------------------------
-- 【正文摘要】
-- 第五条 发生劳动争议，当事人不愿协商、协商不成或者达成和解协议后不履行的，
--        可以向调解组织申请调解；不愿调解、调解不成或者达成调解协议后不履行的，
--        可以向劳动争议仲裁委员会申请仲裁；对仲裁裁决不服的，除本法另有规定的外，可以向人民法院提起诉讼。
-- 第二十七条 劳动争议申请仲裁的时效期间为一年。仲裁时效期间从当事人知道或者应当知道其权利被侵害之日起计算。
--        劳动关系存续期间因拖欠劳动报酬发生争议的，劳动者申请仲裁不受本条第一款规定的仲裁时效期间的限制；
--        但是，劳动关系终止的，应当自劳动关系终止之日起一年内提出。
-- 第四十三条 仲裁庭裁决劳动争议案件，应当自劳动争议仲裁委员会受理仲裁申请之日起四十五日内结束。
--        案情复杂需要延期的，经劳动争议仲裁委员会主任批准，可以延期并书面通知当事人，但是延长期限不得超过十五日。
-- 【标签】劳动争议,调解,仲裁,时效,诉讼

INSERT INTO `kb_document` (
    `public_id`, `title`, `document_type`, `issuing_authority`,
    `canonical_source_url`, `jurisdiction_code`, `scope_text`,
    `authority_level`, `current_version_id`, `status`, `lock_version`
) VALUES (
    '01HZY8QX3K7V2MBR5CJ9W4D6N2',
    '中华人民共和国劳动争议调解仲裁法',
    'LAW',
    '全国人民代表大会常务委员会',
    'https://www.gov.cn/flfg/2007-12/29/content_847373.htm',
    'CN',
    '劳动争议,调解,仲裁,时效,诉讼',
    10,
    NULL,
    'DRAFT',
    0
);
SET @doc3_id = LAST_INSERT_ID();

INSERT INTO `kb_document_version` (
    `document_id`, `version_no`, `document_number`, `source_url`,
    `published_date`, `effective_date`, `expiry_date`,
    `validity_status`, `review_status`, `content_sha256`, `language_code`
) VALUES (
    @doc3_id, 'v1.0', '主席令第八十号',
    'https://www.gov.cn/flfg/2007-12/29/content_847373.htm',
    '2007-12-29', '2008-05-01', NULL,
    'DRAFT', 'PENDING',
    SHA2('中华人民共和国劳动争议调解仲裁法_v1.0', 256),
    'zh-CN'
);
SET @doc3_version_id = LAST_INSERT_ID();
UPDATE `kb_document` SET `current_version_id` = @doc3_version_id WHERE `id` = @doc3_id;


-- --------------------------------------------------
-- 文档 4：司法解释 — 《最高人民法院关于审理劳动争议案件适用法律问题的解释（一）》
-- --------------------------------------------------
-- 【正文摘要】
-- 第一条 劳动者与用人单位之间发生的下列纠纷，属于劳动争议，当事人不服劳动争议仲裁机构作出的裁决，
--        依法提起诉讼的，人民法院应予受理：
--        （一）劳动者与用人单位在履行劳动合同过程中发生的纠纷；
--        （二）劳动者与用人单位之间没有订立书面劳动合同，但已形成劳动关系后发生的纠纷。
-- 第三十二条 用人单位与其招用的已经依法享受养老保险待遇或者领取退休金的人员发生用工争议而提起诉讼的，
--        人民法院应当按劳务关系处理。
-- 第四十四条 因用人单位作出的开除、除名、辞退、解除劳动合同、减少劳动报酬、计算劳动者工作年限等决定
--        而发生的劳动争议，用人单位负举证责任。
-- 【标签】劳动争议,司法解释,受案范围,举证责任,劳务关系

INSERT INTO `kb_document` (
    `public_id`, `title`, `document_type`, `issuing_authority`,
    `canonical_source_url`, `jurisdiction_code`, `scope_text`,
    `authority_level`, `current_version_id`, `status`, `lock_version`
) VALUES (
    '01HZY8QX3K7V2MBR5CJ9W4D6N3',
    '最高人民法院关于审理劳动争议案件适用法律问题的解释（一）',
    'JUDICIAL_INTERPRETATION',
    '最高人民法院',
    'https://www.court.gov.cn/fabu-xiangqing-314821.html',
    'CN',
    '劳动争议,司法解释,受案范围,举证责任,劳务关系',
    20,
    NULL,
    'DRAFT',
    0
);
SET @doc4_id = LAST_INSERT_ID();

INSERT INTO `kb_document_version` (
    `document_id`, `version_no`, `document_number`, `source_url`,
    `published_date`, `effective_date`, `expiry_date`,
    `validity_status`, `review_status`, `content_sha256`, `language_code`
) VALUES (
    @doc4_id, 'v1.0', '法释〔2020〕26号',
    'https://www.court.gov.cn/fabu-xiangqing-314821.html',
    '2020-12-29', '2021-01-01', NULL,
    'DRAFT', 'PENDING',
    SHA2('劳动争议司法解释一_2020_v1.0', 256),
    'zh-CN'
);
SET @doc4_version_id = LAST_INSERT_ID();
UPDATE `kb_document` SET `current_version_id` = @doc4_version_id WHERE `id` = @doc4_id;


-- --------------------------------------------------
-- 文档 5：司法解释 — 《最高人民法院关于审理劳动争议案件适用法律问题的解释（二）》
-- --------------------------------------------------
-- 【正文摘要】
-- 第三条 劳动者主张加班费的，应当就加班事实的存在承担举证责任。
--        但劳动者有证据证明用人单位掌握加班事实存在的证据，用人单位不提供的，由用人单位承担不利后果。
-- 第九条 劳动者与用人单位就解除或者终止劳动合同办理相关手续、支付工资报酬、加班费、经济补偿或者赔偿金等
--        达成的协议，不违反法律、行政法规的强制性规定，且不存在欺诈、胁迫或者乘人之危情形的，应当认定有效。
--        前款协议存在重大误解或者显失公平情形，当事人请求撤销的，人民法院应予支持。
-- 【标签】加班费,举证责任,解除协议,经济补偿,显失公平

INSERT INTO `kb_document` (
    `public_id`, `title`, `document_type`, `issuing_authority`,
    `canonical_source_url`, `jurisdiction_code`, `scope_text`,
    `authority_level`, `current_version_id`, `status`, `lock_version`
) VALUES (
    '01HZY8QX3K7V2MBR5CJ9W4D6N4',
    '最高人民法院关于审理劳动争议案件适用法律问题的解释（二）',
    'JUDICIAL_INTERPRETATION',
    '最高人民法院',
    'https://www.court.gov.cn/zixun/xiangqing/440672.html',
    'CN',
    '加班费,举证责任,解除协议,经济补偿,显失公平',
    20,
    NULL,
    'DRAFT',
    0
);
SET @doc5_id = LAST_INSERT_ID();

INSERT INTO `kb_document_version` (
    `document_id`, `version_no`, `document_number`, `source_url`,
    `published_date`, `effective_date`, `expiry_date`,
    `validity_status`, `review_status`, `content_sha256`, `language_code`
) VALUES (
    @doc5_id, 'v1.0', '法释〔2023〕XX号',
    'https://www.court.gov.cn/zixun/xiangqing/440672.html',
    '2023-12-12', '2024-01-01', NULL,
    'DRAFT', 'PENDING',
    SHA2('劳动争议司法解释二_2024_v1.0', 256),
    'zh-CN'
);
SET @doc5_version_id = LAST_INSERT_ID();
UPDATE `kb_document` SET `current_version_id` = @doc5_version_id WHERE `id` = @doc5_id;


-- --------------------------------------------------
-- 文档 6：人社政策 — 《关于贯彻执行〈中华人民共和国劳动法〉若干问题的意见》
-- --------------------------------------------------
-- 【正文摘要】
-- 第17条 用人单位与劳动者之间形成了事实劳动关系，而用人单位故意拖延不订立劳动合同，
--        劳动行政部门应予以纠正。用人单位因此给劳动者造成损害的，应按劳动部《违反〈劳动法〉有关劳动合同规定的赔偿办法》的规定进行赔偿。
-- 第53条 劳动法中的"工资"一般包括计时工资、计件工资、奖金、津贴和补贴、
--        延长工作时间的工资报酬以及特殊情况下支付的工资等。
-- 第58条 企业下岗人员、工伤职工依据国务院有关规定，领取的基本生活费、伤残抚恤金等，
--        视为提供了正常劳动，用人单位应当支付工资。
-- 【标签】事实劳动关系,工资范围,劳动合同,赔偿

INSERT INTO `kb_document` (
    `public_id`, `title`, `document_type`, `issuing_authority`,
    `canonical_source_url`, `jurisdiction_code`, `scope_text`,
    `authority_level`, `current_version_id`, `status`, `lock_version`
) VALUES (
    '01HZY8QX3K7V2MBR5CJ9W4D6N5',
    '关于贯彻执行〈中华人民共和国劳动法〉若干问题的意见',
    'POLICY',
    '劳动部（现人力资源和社会保障部）',
    'https://www.mohrss.gov.cn/',
    'CN',
    '事实劳动关系,工资范围,劳动合同,赔偿',
    30,
    NULL,
    'DRAFT',
    0
);
SET @doc6_id = LAST_INSERT_ID();

INSERT INTO `kb_document_version` (
    `document_id`, `version_no`, `document_number`, `source_url`,
    `published_date`, `effective_date`, `expiry_date`,
    `validity_status`, `review_status`, `content_sha256`, `language_code`
) VALUES (
    @doc6_id, 'v1.0', '劳部发〔1995〕309号',
    'https://www.mohrss.gov.cn/',
    '1995-08-04', '1995-08-04', NULL,
    'DRAFT', 'PENDING',
    SHA2('贯彻执行劳动法若干问题的意见_v1.0', 256),
    'zh-CN'
);
SET @doc6_version_id = LAST_INSERT_ID();
UPDATE `kb_document` SET `current_version_id` = @doc6_version_id WHERE `id` = @doc6_id;


-- --------------------------------------------------
-- 文档 7：人社政策 — 《企业职工带薪年休假实施办法》
-- --------------------------------------------------
-- 【正文摘要】
-- 第三条 职工连续工作满12个月以上的，享受带薪年休假。
--        年休假天数根据职工累计工作时间确定：
--        职工累计工作已满1年不满10年的，年休假5天；
--        已满10年不满20年的，年休假10天；
--        已满20年的，年休假15天。
-- 第五条 单位确因工作需要不能安排职工休年休假的，经职工本人同意，可以不安排职工休年休假。
--        对职工应休未休的年休假天数，单位应当按照该职工日工资收入的300%支付年休假工资报酬，
--        其中包含用人单位支付职工正常工作期间的工资收入。
-- 第十条 用人单位与职工解除或者终止劳动合同时，当年度未安排职工休满应休年休假天数的，
--        应当按照职工当年已工作时间折算应休未休年休假天数并支付未休年休假工资报酬，
--        但折算后不足1整天的部分不支付未休年休假工资报酬。
-- 【标签】年休假,休假天数,工资报酬,解除劳动合同

INSERT INTO `kb_document` (
    `public_id`, `title`, `document_type`, `issuing_authority`,
    `canonical_source_url`, `jurisdiction_code`, `scope_text`,
    `authority_level`, `current_version_id`, `status`, `lock_version`
) VALUES (
    '01HZY8QX3K7V2MBR5CJ9W4D6N6',
    '企业职工带薪年休假实施办法',
    'POLICY',
    '人力资源和社会保障部',
    'https://www.gov.cn/zhengce/zhengceku/2008-09/28/content_1162644.htm',
    'CN',
    '年休假,休假天数,工资报酬,解除劳动合同',
    30,
    NULL,
    'DRAFT',
    0
);
SET @doc7_id = LAST_INSERT_ID();

INSERT INTO `kb_document_version` (
    `document_id`, `version_no`, `document_number`, `source_url`,
    `published_date`, `effective_date`, `expiry_date`,
    `validity_status`, `review_status`, `content_sha256`, `language_code`
) VALUES (
    @doc7_id, 'v1.0', '人力资源和社会保障部令第1号',
    'https://www.gov.cn/zhengce/zhengceku/2008-09/28/content_1162644.htm',
    '2008-09-18', '2008-09-18', NULL,
    'DRAFT', 'PENDING',
    SHA2('企业职工带薪年休假实施办法_v1.0', 256),
    'zh-CN'
);
SET @doc7_version_id = LAST_INSERT_ID();
UPDATE `kb_document` SET `current_version_id` = @doc7_version_id WHERE `id` = @doc7_id;


-- --------------------------------------------------
-- 文档 8：FAQ — 劳动合同常见问题解答（人社政策问答）
-- --------------------------------------------------
-- 【正文摘要】
-- 【问】试用期最长可以约定多久？
-- 【答】劳动合同期限三个月以上不满一年的，试用期不得超过一个月；
--        劳动合同期限一年以上不满三年的，试用期不得超过二个月；
--        三年以上固定期限和无固定期限的劳动合同，试用期不得超过六个月。
--        同一用人单位与同一劳动者只能约定一次试用期。
-- 【问】用人单位未与劳动者签订书面劳动合同有什么法律后果？
-- 【答】用人单位自用工之日起超过一个月不满一年未与劳动者订立书面劳动合同的，
--        应当向劳动者每月支付二倍的工资。
--        用人单位自用工之日起满一年不与劳动者订立书面劳动合同的，
--        视为用人单位与劳动者已订立无固定期限劳动合同。
-- 【问】劳动者在什么情况下需要支付违约金？
-- 【答】根据《劳动合同法》规定，只有在两种情形下用人单位可以与劳动者约定由劳动者承担违约金：
--        一是用人单位为劳动者提供专项培训费用并约定服务期，劳动者违反服务期约定的；
--        二是劳动者违反竞业限制约定的。除此之外，用人单位不得与劳动者约定由劳动者承担违约金。
-- 【标签】劳动合同,试用期,未签合同,违约金,双倍工资

INSERT INTO `kb_document` (
    `public_id`, `title`, `document_type`, `issuing_authority`,
    `canonical_source_url`, `jurisdiction_code`, `scope_text`,
    `authority_level`, `current_version_id`, `status`, `lock_version`
) VALUES (
    '01HZY8QX3K7V2MBR5CJ9W4D6N7',
    '劳动合同常见问题解答（人社政策问答）',
    'FAQ',
    '人力资源和社会保障部',
    'https://www.mohrss.gov.cn/',
    'CN',
    '劳动合同,试用期,未签合同,违约金,双倍工资',
    40,
    NULL,
    'DRAFT',
    0
);
SET @doc8_id = LAST_INSERT_ID();

INSERT INTO `kb_document_version` (
    `document_id`, `version_no`, `document_number`, `source_url`,
    `published_date`, `effective_date`, `expiry_date`,
    `validity_status`, `review_status`, `content_sha256`, `language_code`
) VALUES (
    @doc8_id, 'v1.0', '人社厅函〔2021〕XXX号',
    'https://www.mohrss.gov.cn/',
    '2021-06-15', '2021-06-15', NULL,
    'DRAFT', 'PENDING',
    SHA2('劳动合同常见问题解答_FAQ_v1.0', 256),
    'zh-CN'
);
SET @doc8_version_id = LAST_INSERT_ID();
UPDATE `kb_document` SET `current_version_id` = @doc8_version_id WHERE `id` = @doc8_id;


-- --------------------------------------------------
-- 文档 9：示例合同 — 劳动合同示范文本（通用版）
-- --------------------------------------------------
-- 【正文摘要】
-- 第一条 劳动合同期限
--        甲乙双方选择以下第___种形式确定本合同期限：
--        （一）固定期限：自____年__月__日起至____年__月__日止；
--        （二）无固定期限：自____年__月__日起；
--        （三）以完成一定工作任务为期限：自____年__月__日起至________工作任务完成时止。
-- 第二条 工作内容和工作地点
--        甲方根据工作需要，安排乙方在________岗位工作，工作地点为________。
-- 第三条 工作时间和休息休假
--        甲方实行每日工作时间不超过8小时、平均每周工作时间不超过40小时的工时制度。
--        甲方保证乙方每周至少休息一日。
-- 第四条 劳动报酬
--        甲方每月__日前以货币形式支付乙方工资，月工资为____元或按________执行。
-- 第五条 社会保险和福利待遇
--        甲乙双方按照国家和地方有关规定参加社会保险，缴纳社会保险费。
-- 【标签】劳动合同,示范文本,合同期限,工作内容,社会保险

INSERT INTO `kb_document` (
    `public_id`, `title`, `document_type`, `issuing_authority`,
    `canonical_source_url`, `jurisdiction_code`, `scope_text`,
    `authority_level`, `current_version_id`, `status`, `lock_version`
) VALUES (
    '01HZY8QX3K7V2MBR5CJ9W4D6N8',
    '劳动合同示范文本（通用版）',
    'SAMPLE_CONTRACT',
    '人力资源和社会保障部',
    'https://www.mohrss.gov.cn/',
    'CN',
    '劳动合同,示范文本,合同期限,工作内容,社会保险',
    50,
    NULL,
    'DRAFT',
    0
);
SET @doc9_id = LAST_INSERT_ID();

INSERT INTO `kb_document_version` (
    `document_id`, `version_no`, `document_number`, `source_url`,
    `published_date`, `effective_date`, `expiry_date`,
    `validity_status`, `review_status`, `content_sha256`, `language_code`
) VALUES (
    @doc9_id, 'v1.0', '人社厅发〔2020〕XX号',
    'https://www.mohrss.gov.cn/',
    '2020-03-01', '2020-03-01', NULL,
    'DRAFT', 'PENDING',
    SHA2('劳动合同示范文本_通用版_v1.0', 256),
    'zh-CN'
);
SET @doc9_version_id = LAST_INSERT_ID();
UPDATE `kb_document` SET `current_version_id` = @doc9_version_id WHERE `id` = @doc9_id;


-- --------------------------------------------------
-- 文档 10：示例合同 — 解除劳动合同协议书示范文本
-- --------------------------------------------------
-- 【正文摘要】
-- 第一条 甲乙双方经协商一致，同意于____年__月__日解除劳动合同。
-- 第二条 甲方同意向乙方支付经济补偿金共计人民币____元（大写：________元整），
--        支付时间为本协议签订后__个工作日内。
-- 第三条 乙方应于本协议签订后__个工作日内完成工作交接，交接内容包括但不限于：
--        工作资料、办公设备、门禁卡、钥匙等。
-- 第四条 甲乙双方确认，除本协议约定事项外，双方不存在其他任何劳动争议。
--        乙方不得就劳动关系存续期间的任何事项向甲方主张权利。
-- 第五条 乙方承诺对在甲方工作期间知悉的商业秘密和与知识产权相关的保密事项予以保密，不得向第三方披露。
-- 【标签】劳动合同,解除,经济补偿,工作交接,保密义务

INSERT INTO `kb_document` (
    `public_id`, `title`, `document_type`, `issuing_authority`,
    `canonical_source_url`, `jurisdiction_code`, `scope_text`,
    `authority_level`, `current_version_id`, `status`, `lock_version`
) VALUES (
    '01HZY8QX3K7V2MBR5CJ9W4D6N9',
    '解除劳动合同协议书示范文本',
    'SAMPLE_CONTRACT',
    '北京市人力资源和社会保障局',
    'https://rsj.beijing.gov.cn/',
    'CN',
    '劳动合同,解除,经济补偿,工作交接,保密义务',
    50,
    NULL,
    'DRAFT',
    0
);
SET @doc10_id = LAST_INSERT_ID();

INSERT INTO `kb_document_version` (
    `document_id`, `version_no`, `document_number`, `source_url`,
    `published_date`, `effective_date`, `expiry_date`,
    `validity_status`, `review_status`, `content_sha256`, `language_code`
) VALUES (
    @doc10_id, 'v1.0', '京人社发〔2019〕XX号',
    'https://rsj.beijing.gov.cn/',
    '2019-01-01', '2019-01-01', NULL,
    'DRAFT', 'PENDING',
    SHA2('解除劳动合同协议书示范文本_v1.0', 256),
    'zh-CN'
);
SET @doc10_version_id = LAST_INSERT_ID();
UPDATE `kb_document` SET `current_version_id` = @doc10_version_id WHERE `id` = @doc10_id;


SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 脚本执行完成
-- ============================================================
-- 本次共插入 10 条 kb_document 记录，覆盖 5 种文档类型：
--   - LAW: 3 条（劳动合同法、劳动法、劳动争议调解仲裁法）
--   - JUDICIAL_INTERPRETATION: 2 条（司法解释一、司法解释二）
--   - POLICY: 2 条（劳动法贯彻意见、年休假实施办法）
--   - FAQ: 1 条（劳动合同常见问题解答）
--   - SAMPLE_CONTRACT: 2 条（劳动合同示范文本、解除协议示范文本）
-- 所有文档 status 均为 DRAFT，待后续索引入库。
-- ============================================================
