DROP TABLE IF EXISTS data_objects;

CREATE TABLE data_objects (
    id VARCHAR(36) PRIMARY KEY,
    numeric_id BIGINT AUTO_INCREMENT UNIQUE,
    data_content TEXT,
    metadata_json TEXT,
    location_info_json TEXT,
    constraint_set_json TEXT,
    propagation_control_json TEXT,
    audit_info_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


USE data_management;
INSERT INTO data_objects (
    id, data_content, metadata_json, location_info_json, 
    constraint_set_json, propagation_control_json, audit_info_json, 
    created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440001',
    '{"entity":"销售订单","status":"已合格","feedback":"数据完整","metadata":null,"dataItems":[{"订单号":"ORD-20231201","客户":"张三","金额":"5000"},{"订单号":"ORD-20231202","客户":"李四","金额":"3000"}],"data":{}}',
    '{"dataName":"2023年12月订单表","sourceUnit":"销售部","contactPerson":"王经理","contactPhone":"13800138000","resourceSummary":"月度销售订单汇总","fieldClassification":"业务数据","headers":["订单号","客户","金额"]}',
    '{"locations":[{"sheet": "生产表","startRow": "2","endRow": "100","startColumn": "B","endColumn": "E"}]}',
    '{"constraints":[{"formatConstraint":"xlsx","accessConstraint":"全部允许","pathConstraint":"点对点","regionConstraint":"内网","shareConstraint":"允许共享"}]}',
    '{"operations":{"read":1,"modify":1}}',
    '{"auditUser":"audit_admin","auditTime":"2023-12-05 09:30:00"}',
    NOW(),
    NOW()
);

INSERT INTO data_objects (
    id, data_content, metadata_json, location_info_json, 
    constraint_set_json, propagation_control_json, audit_info_json, 
    created_at, updated_at
) VALUES (
    '6ba7b810-9dad-11d1-80b4-00c04fd430c8',
    '{"entity":"用户日志","status":"待检验","feedback":"","metadata":null,"dataItems":[{"用户ID":"U1001","操作":"登录","时间":"2023-12-05 08:15"},{"用户ID":"U1002","操作":"退出","时间":"2023-12-05 09:45"}],"data":{}}',
    '{"dataName":"用户操作日志","sourceUnit":"IT部","contactPerson":"李技术员","contactPhone":"13900139000","resourceSummary":"用户登录/退出记录","fieldClassification":"日志数据","headers":["用户ID","操作","时间"]}',
    '{"locations":[{"sheet": "财务表","startRow": "1","endRow": "20","startColumn": "A","endColumn": "E"},{"sheet": "财务表","startRow": "25","endRow": "45","startColumn": "F","endColumn": "K"}]}',
    '{"constraints":[{"formatConstraint":"csv","accessConstraint":"只允许管理方获取","pathConstraint":"广播","regionConstraint":"外网","shareConstraint":"不允许共享"}]}',
    '{"operations":{"read":1}}',
    '{"auditUser":"sys_admin","auditTime":"2023-12-05 10:00:00"}',
    NOW(),
    NOW()
);

INSERT INTO data_objects (
    id, data_content, metadata_json, location_info_json, 
    constraint_set_json, propagation_control_json, audit_info_json, 
    created_at, updated_at
) VALUES (
    '123e4567-e89b-12d3-a456-426614174000',
    '{"entity":"库存管理","status":"已合格","feedback":"库存同步完成","metadata":null,"dataItems":[{"产品ID":"P1001","名称":"手机","库存量":"200"},{"产品ID":"P1002","名称":"耳机","库存量":"500"}],"data":{}}',
    '{"dataName":"产品库存表","sourceUnit":"仓储部","contactPerson":"张管理员","contactPhone":"13600136000","resourceSummary":"实时库存状态","fieldClassification":"运营数据","headers":["产品ID","名称","库存量"]}',
    '{"locations":[{"sheet": "日志表","startRow": "1","endRow": "1000","startColumn": "A","endColumn": "Z"}]}',
    '{"constraints":[{"formatConstraint":"json","accessConstraint":"全部允许","pathConstraint":"点对点","regionConstraint":"内网","shareConstraint":"允许共享"}]}',
    '{"operations":{"read":1,"share":1}}',
    '{"auditUser":"inventory_audit","auditTime":"2023-12-05 14:20:00"}',
    NOW(),
    NOW()
);

INSERT INTO data_objects (
    id, data_content, metadata_json, location_info_json, 
    constraint_set_json, propagation_control_json, audit_info_json, 
    created_at, updated_at
) VALUES (
    'd9428888-8f9b-4c4e-8c4a-123456789004',
    '{"entity":"客户反馈","status":"不合格","feedback":"数据格式错误","metadata":null,"dataItems":[{"反馈ID":"FB-001","内容":"页面加载慢","评分":"3"},{"反馈ID":"FB-002","内容":"功能缺失","评分":"2"}],"data":{}}',
    '{"dataName":"客户反馈记录","sourceUnit":"客服部","contactPerson":"赵客服","contactPhone":"13500135000","resourceSummary":"客户投诉与建议","fieldClassification":"用户反馈","headers":["反馈ID","内容","评分"]}',
    '{"locations":[{"sheet": "生产表","startRow": "2","endRow": "50","startColumn": "A","endColumn": "D"}]}',
    '{"constraints":[{"formatConstraint":"txt","accessConstraint":"只允许管理方获取","pathConstraint":"点对点","regionConstraint":"内网","shareConstraint":"不允许共享"}]}',
    '{"operations":{"read":1,"modify":1}}',
    '{"auditUser":"feedback_admin","auditTime":"2023-12-05 16:45:00"}',
    NOW(),
    NOW()
);

INSERT INTO data_objects (
    id, data_content, metadata_json, location_info_json, 
    constraint_set_json, propagation_control_json, audit_info_json, 
    created_at, updated_at
) VALUES (
    'a1b2c3d4-5678-90ef-1234-567890000005',
    '{"entity":"财务交易","status":"已合格","feedback":"","metadata":null,"dataItems":[{"交易ID":"TX-20231205-001","金额":"10000","类型":"收入"},{"交易ID":"TX-20231205-002","金额":"5000","类型":"支出"}],"data":{}}',
    '{"dataName":"财务日报表","sourceUnit":"财务部","contactPerson":"陈会计","contactPhone":"13700137000","resourceSummary":"每日资金流水","fieldClassification":"财务数据","headers":["交易ID","金额","类型"]}',
    '{"locations":[{"sheet": "生产表","startRow": "2","endRow": "50","startColumn": "A","endColumn": "D"}]}',
    '{"constraints":[{"formatConstraint":"pdf","accessConstraint":"只允许管理方获取","pathConstraint":"广播","regionConstraint":"外网","shareConstraint":"不允许共享"}]}',
    '{"operations":{"read":1}}',
    '{"auditUser":"finance_auditor","auditTime":"2023-12-05 17:30:00"}',
    NOW(),
    NOW()
);
