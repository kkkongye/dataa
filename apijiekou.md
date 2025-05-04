### 1. 数源方接口 (SourceController)

基础路径: /api

| HTTP方法 | 接口路径 | 功能描述 |

|---------|---------|---------|

| DELETE | /{id} | 根据ID删除数字对象 |ok

| GET | /objects/{id} | 根据ID查询数字对象 |ok

| POST | /objects | 新增数字对象 |ok

| PUT | /objects/{id} | 根据ID更新数字对象 |ok

| GET | /objects/list | 查询所有数字对象 |ok

| POST | /encrypt | 加密数据（模拟实现） |no

| POST | /objects/submit | 将加密数据发送到治理方 |no

### 2. 治理方接口 (GovernanceController)

基础路径: /governance

| HTTP方法 | 接口路径 | 功能描述 |

|---------|---------|---------|

| POST | /receive | 接收数据源发送的加密数据 |

| POST | /decrypt | 解密数据（模拟实现） |ok

| GET | /inspect/{id} | 检查特定ID的数据元组 |

| POST | /encrypt | 加密数据（模拟实现） |

| POST | /send-data | 发送数据到其他系统 |

### 3. 使用方接口 (ConsumerController)

基础路径: /consumer

| HTTP方法 | 接口路径 | 功能描述 |

|---------|---------|---------|

| GET | /{id} | 验证属性 |

| POST | /receive | 接收数据 |

| POST | /decrypt | 解密数据（模拟实现） |ok

### 4. 文件上传接口 (UploadController)

基础路径: /

| HTTP方法 | 接口路径 | 功能描述 |

|---------|---------|---------|

| POST | /api/objects/excel | 上传Excel文件 |ok
