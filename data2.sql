CREATE DATABASE IF NOT EXISTS login_database 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;
    
USE login_database;
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    PASSWORD VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users
ADD COLUMN ROLE VARCHAR(255);





CREATE DATABASE IF NOT EXISTS provider_database 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE provider_database;

DROP TABLE data_objects;

CREATE TABLE data_objects (
    id VARCHAR(36) PRIMARY KEY COMMENT '唯一标识符',
    numeric_id BIGINT AUTO_INCREMENT COMMENT '数据库自增长ID',
    file_path VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    db_grade DOUBLE,
    table_grade DOUBLE,
    row_grades VARCHAR(1000),
    column_grades VARCHAR(1000),
    total_category_value VARCHAR(255),
    total_grade_value VARCHAR(255),
    industry_category VARCHAR(255),
    processing_time_category VARCHAR(255),
    data_source_category VARCHAR(255),
    UNIQUE KEY (numeric_id)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;



ALTER TABLE data_objects 
ADD COLUMN data_content TEXT,
ADD COLUMN metadata_json TEXT,
ADD COLUMN location_info_json TEXT,
ADD COLUMN constraint_set_json TEXT,
ADD COLUMN propagation_control_json TEXT,
ADD COLUMN audit_info_json TEXT;

INSERT INTO data_objects (
    id, 
    data_content, 
    metadata_json, 
    location_info_json, 
    constraint_set_json, 
    propagation_control_json, 
    audit_info_json, 
    file_path,
    created_at, 
    updated_at
) VALUES (
'123e4567-e89b-12d3-a456-426614174000',
    '{"entity":"税务仿真数据","status":"待生成分类分级值","feedback":"","metadata":null,"dataItems":[
        {"姓名":"陈建国","性别":"男","年龄":65,"身份证号":"330102196001011234","居住地址":"浙江省杭州市上城区清波街道1号","家庭纳税额":0,"免税信息":"养老金免税","收入状态评估":1,"重要程度":"重要"},
        {"姓名":"林美华","性别":"女","年龄":42,"身份证号":"330103198305125678","居住地址":"浙江省杭州市西湖区灵隐街道2号","家庭纳税额":3200,"免税信息":"子女教育扣除1000元/月","收入状态评估":3,"重要程度":"一般"},
        {"姓名":"王守仁","性别":"男","年龄":78,"身份证号":"330104194709239012","居住地址":"浙江省杭州市江干区采荷街道3号","家庭纳税额":0,"免税信息":"养老金免税","收入状态评估":1,"重要程度":"重要"},
        {"姓名":"赵丽娟","性别":"女","年龄":53,"身份证号":"330105197203152345","居住地址":"浙江省杭州市拱墅区米市巷街道4号","家庭纳税额":850,"免税信息":"继续教育扣除400元/月","收入状态评估":2,"重要程度":"核心"},
        {"姓名":"钱伟长","性别":"男","年龄":31,"身份证号":"330106199407286789","居住地址":"浙江省杭州市滨江区西兴街道5号","家庭纳税额":5600,"免税信息":"房贷利息扣除1000元/月","收入状态评估":4,"重要程度":"一般"},
        {"姓名":"孙玉梅","性别":"女","年龄":82,"身份证号":"330107194311100123","居住地址":"浙江省杭州市萧山区城厢街道6号","家庭纳税额":0,"免税信息":"养老金免税","收入状态评估":1,"重要程度":"重要"},
        {"姓名":"周志远","性别":"男","年龄":47,"身份证号":"330108197802224567","居住地址":"浙江省杭州市余杭区临平街道7号","家庭纳税额":4100,"免税信息":"赡养老人扣除2000元/月","收入状态评估":3,"重要程度":"一般"},
        {"姓名":"吴秀芳","性别":"女","年龄":29,"身份证号":"330109199610058901","居住地址":"浙江省杭州市富阳区富春街道8号","家庭纳税额":1200,"免税信息":"租房扣除1500元/月","收入状态评估":2,"重要程度":"一般"},
        {"姓名":"郑浩然","性别":"男","年龄":58,"身份证号":"330110196704172345","居住地址":"浙江省杭州市临安区锦城街道9号","家庭纳税额":2800,"免税信息":"大病医疗扣除80000元/年","收入状态评估":3,"重要程度":"核心"},
        {"姓名":"王桂兰","性别":"女","年龄":61,"身份证号":"330111196408296789","居住地址":"浙江省杭州市桐庐县桐君街道10号","家庭纳税额":0,"免税信息":"养老金免税","收入状态评估":1,"重要程度":"重要"},
        {"姓名":"陈嘉庚","性别":"男","年龄":35,"身份证号":"330112199001130123","居住地址":"浙江省杭州市淳安县千岛湖镇11号","家庭纳税额":4800,"免税信息":"子女教育扣除1000元/月","收入状态评估":4,"重要程度":"一般"},
        {"姓名":"李淑兰","性别":"女","年龄":73,"身份证号":"330113195206064567","居住地址":"浙江省杭州市建德市新安江街道12号","家庭纳税额":0,"免税信息":"养老金免税","收入状态评估":1,"重要程度":"重要"},
        {"姓名":"张伟明","性别":"男","年龄":44,"身份证号":"330114198111198901","居住地址":"浙江省杭州市西湖风景名胜区13号","家庭纳税额":3900,"免税信息":"赡养老人扣除2000元/月","收入状态评估":3,"重要程度":"一般"},
        {"姓名":"刘桂英","性别":"女","年龄":26,"身份证号":"330115199903032345","居住地址":"浙江省杭州市上城区南星街道14号","家庭纳税额":6200,"免税信息":"房贷利息扣除1000元/月","收入状态评估":4,"重要程度":"一般"},
        {"姓名":"黄志强","性别":"男","年龄":55,"身份证号":"330116197007156789","居住地址":"浙江省杭州市西湖区西溪街道15号","家庭纳税额":3400,"免税信息":"继续教育扣除400元/月","收入状态评估":3,"重要程度":"核心"},
        {"姓名":"林秀英","性别":"女","年龄":68,"身份证号":"330117195712280123","居住地址":"浙江省杭州市江干区凯旋街道16号","家庭纳税额":0,"免税信息":"养老金免税","收入状态评估":1,"重要程度":"重要"},
        {"姓名":"何光明","性别":"男","年龄":39,"身份证号":"330118198605114567","居住地址":"浙江省杭州市拱墅区和睦街道17号","家庭纳税额":4500,"免税信息":"子女教育扣除1000元/月","收入状态评估":3,"重要程度":"一般"},
        {"姓名":"郭秀云","性别":"女","年龄":49,"身份证号":"330119197609248901","居住地址":"浙江省杭州市滨江区长河街道18号","家庭纳税额":2300,"免税信息":"赡养老人扣除2000元/月","收入状态评估":2,"重要程度":"核心"},
        {"姓名":"马建国","性别":"男","年龄":24,"身份证号":"330120199610152345","居住地址":"浙江省杭州市萧山区北干街道19号","家庭纳税额":5900,"免税信息":"租房扣除1500元/月","收入状态评估":4,"重要程度":"一般"},
        {"姓名":"罗玉华","性别":"女","年龄":76,"身份证号":"330121194904276789","居住地址":"浙江省杭州市余杭区南苑街道20号","家庭纳税额":0,"免税信息":"养老金免税","收入状态评估":1,"重要程度":"重要"},
        {"姓名":"梁启东","性别":"男","年龄":33,"身份证号":"330122199208090123","居住地址":"浙江省杭州市富阳区春江街道21号","家庭纳税额":4300,"免税信息":"房贷利息扣除1000元/月","收入状态评估":3,"重要程度":"一般"},
        {"姓名":"宋春兰","性别":"女","年龄":51,"身份证号":"330123197401224567","居住地址":"浙江省杭州市临安区青山湖街道22号","家庭纳税额":2700,"免税信息":"赡养老人扣除2000元/月","收入状态评估":2,"重要程度":"核心"},
        {"姓名":"唐国强","性别":"男","年龄":63,"身份证号":"330124196206148901","居住地址":"浙江省杭州市桐庐县旧县街道23号","家庭纳税额":0,"免税信息":"养老金免税","收入状态评估":1,"重要程度":"核心"},
        {"姓名":"许红梅","性别":"女","年龄":37,"身份证号":"330125198811272345","居住地址":"浙江省杭州市淳安县文昌镇24号","家庭纳税额":4000,"免税信息":"子女教育扣除1000元/月","收入状态评估":3,"重要程度":"一般"},
        {"姓名":"邓建军","性别":"男","年龄":71,"身份证号":"330126195403106789","居住地址":"浙江省杭州市建德市梅城镇25号","家庭纳税额":0,"免税信息":"养老金免税","收入状态评估":1,"重要程度":"重要"}
    ],"data":{}}',
    '{"dataName":"税务局仿真信息表","sourceUnit":"税务局","contactPerson":"刘科长","contactPhone":"13700137000","resourceSummary":"居民税务档案","fieldClassification":"公共服务数据","headers":["姓名","性别","年龄","身份证号","居住地址","家庭纳税额","免税信息","收入状态评估","重要程度"]}',
    '{"databaseName": "税务信息库","tableName": "TaxBureau","selectFields": "Name,Gender,Age,IDCard,ResidentialAddress,FamilyTaxAmount,TaxExemptionInfo,IncomeStatusAssessment,Importance"}',
    '{"constraints":[{"formatConstraint":"json","accessConstraint":"全部允许","pathConstraint":"点对点","regionConstraint":"内网","shareConstraint":"允许共享"}]}',
    '{"operations":{"read":1,"share":1}}',
    '{"auditUser":"inventory_audit","auditTime":"2023-12-05 14:20:00"}',

    'D:\\datasystem\\test2',
    NOW(),
    NOW()
);



CREATE DATABASE IF NOT EXISTS application_database 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;
    
USE application_database;

-- 数字对象展示表（存储所有数字对象的展示信息）
CREATE TABLE digital_object_display (
    object_id VARCHAR(255) PRIMARY KEY COMMENT '数字对象ID（关联data_objects.id）',
    entity VARCHAR(255) NOT NULL COMMENT '数据实体名称（来自data_content）',
    constraint_control VARCHAR(255) COMMENT '约束条件传输控制操作（来自constraint_set）',
    STATUS VARCHAR(50) COMMENT '状态（来自data_content.status）',
    source_agreed BOOLEAN DEFAULT FALSE COMMENT '数源方同意状态',
    governance_agreed BOOLEAN DEFAULT FALSE COMMENT '治理方同意状态'
) COMMENT '数字对象展示表';

-- 申请记录表
CREATE TABLE application_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    object_id VARCHAR(255) NOT NULL COMMENT '数字对象ID',
    applicant VARCHAR(255) NOT NULL COMMENT '申请人用户名',
    entity VARCHAR(255) NOT NULL COMMENT '数据实体名称',
    source_agreed BOOLEAN DEFAULT FALSE COMMENT '数源方同意状态',
    governance_agreed BOOLEAN DEFAULT FALSE COMMENT '治理方同意状态',
    apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    FOREIGN KEY (object_id) REFERENCES digital_object_display(object_id)
) COMMENT '申请记录表';

ALTER TABLE application_record MODIFY id VARCHAR(36) PRIMARY KEY;


CREATE DATABASE IF NOT EXISTS governance_database 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;
    
USE governance_database;
CREATE TABLE governance_objects (
    id VARCHAR(36) PRIMARY KEY COMMENT '唯一标识符',
    file_path VARCHAR(255) NOT NULL COMMENT '文件存储路径',
    created_at TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL COMMENT '更新时间',
    db_grade DOUBLE COMMENT '数据库分级值',
    table_grade DOUBLE COMMENT '表分级值',
    row_grades TEXT COMMENT '行分级值JSON',
    column_grades TEXT COMMENT '列分级值JSON',
    total_category_value VARCHAR(50) COMMENT '总分类值',
    total_grade_value VARCHAR(50) COMMENT '总分级值',
    industry_category VARCHAR(50) COMMENT '行业领域分类',
    processing_time_category VARCHAR(50) COMMENT '处理时效分类',
    data_source_category VARCHAR(50) COMMENT '数据来源分类'
);


CREATE DATABASE IF NOT EXISTS consumer_database 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE consumer_database;

CREATE TABLE data_objects (
    id VARCHAR(36) PRIMARY KEY COMMENT '唯一标识符',
    numeric_id BIGINT AUTO_INCREMENT COMMENT '数据库自增长ID',
    file_path VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    db_grade DOUBLE,
    table_grade DOUBLE,
    row_grades VARCHAR(1000),
    column_grades VARCHAR(1000),
    total_category_value VARCHAR(255),
    total_grade_value VARCHAR(255),
    industry_category VARCHAR(255),
    processing_time_category VARCHAR(255),
    data_source_category VARCHAR(255),
    UNIQUE KEY (numeric_id)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

ALTER TABLE data_objects 
ADD COLUMN data_content TEXT,
ADD COLUMN metadata_json TEXT,
ADD COLUMN location_info_json TEXT,
ADD COLUMN constraint_set_json TEXT,
ADD COLUMN propagation_control_json TEXT,
ADD COLUMN audit_info_json TEXT;


SHOW TABLES LIKE 'data_objects';

