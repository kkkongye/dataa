DROP DATABASE IF EXISTS login_database ;
CREATE DATABASE IF NOT EXISTS login_database 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;
    
USE login_database;

DROP TABLE users;


CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    PASSWORD VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users
ADD COLUMN ROLE VARCHAR(255);
ALTER TABLE users ADD COLUMN superior VARCHAR(255);




CREATE DATABASE IF NOT EXISTS provider_database 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE provider_database;

DROP TABLE data_objects;
ALTER TABLE data_objects ADD COLUMN audit_report TEXT COMMENT '审查报告内容';
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
ALTER TABLE governance_objects ADD COLUMN audit_report TEXT COMMENT '审查报告内容';
DROP TABLE governance_objects;
SHOW TABLES LIKE 'governance_objects';

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

ALTER TABLE governance_objects 
ADD COLUMN data_content TEXT,
ADD COLUMN metadata_json TEXT,
ADD COLUMN location_info_json TEXT,
ADD COLUMN constraint_set_json TEXT,
ADD COLUMN propagation_control_json TEXT,
ADD COLUMN audit_info_json TEXT,
ADD COLUMN numeric_id BIGINT COMMENT '数据库自增长ID',
ADD COLUMN audit_report TEXT COMMENT '审查报告内容';

DESC governance_objects;
SELECT DATABASE();
CREATE DATABASE IF NOT EXISTS consumer_database 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE consumer_database;

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
ALTER TABLE data_objects ADD COLUMN data_content TEXT;


DROP DATABASE IF EXISTS consumer_database;
CREATE DATABASE consumer_database CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE consumer_database;
DROP TABLE IF EXISTS data_objects;

CREATE TABLE consumer_objects (
    id VARCHAR(255) PRIMARY KEY COMMENT '对象唯一标识',
    entity VARCHAR(255) COMMENT '数据实体名称',
    STATUS VARCHAR(50) COMMENT '状态(待校验/已合格/不合格)',
    propagation_control_json TEXT COMMENT '传播控制集合的JSON'
) ;


CREATE TABLE data_objects (
    id VARCHAR(255) PRIMARY KEY,
    numeric_id INT,
    file_path TEXT,
    data_content TEXT,
    metadata_json TEXT,
    location_info_json TEXT,
    constraint_set_json TEXT,
    propagation_control_json TEXT,
    audit_info_json TEXT,
    db_grade INT,
    table_grade INT,
    row_grades TEXT,
    column_grades TEXT,
    total_category_value DOUBLE,
    total_grade_value DOUBLE,
    industry_category VARCHAR(255),
    processing_time_category VARCHAR(255),
    data_source_category VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME
);


USE application_database;
DROP TABLE IF EXISTS application_record;

CREATE TABLE application_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请记录ID',
    object_ids TEXT NOT NULL COMMENT '申请的数据对象ID列表(逗号分隔)',
    applicant VARCHAR(255) NOT NULL COMMENT '使用方申请人username',
    source_agreed BOOLEAN DEFAULT NULL COMMENT '数源方同意状态',
    source_operator VARCHAR(255) DEFAULT NULL COMMENT '数源方审批操作人',
    governance_agreed1 BOOLEAN DEFAULT NULL COMMENT '治理方同意状态1',
    governance_agreed2 BOOLEAN DEFAULT NULL COMMENT '治理方同意状态2',
    governance_operator VARCHAR(255) DEFAULT NULL COMMENT '治理方审批操作人',
    apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    INDEX idx_applicant (applicant),
    INDEX idx_apply_time (apply_time)
) COMMENT '数据对象申请记录表';


ALTER TABLE application_record
MODIFY COLUMN source_agreed BOOLEAN DEFAULT FALSE,
MODIFY COLUMN source_operator VARCHAR(255),
MODIFY COLUMN governance_agreed1 BOOLEAN DEFAULT FALSE,
MODIFY COLUMN governance_operator VARCHAR(255),
MODIFY COLUMN governance_agreed2 BOOLEAN DEFAULT FALSE;

ALTER TABLE application_record 
MODIFY COLUMN apply_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);