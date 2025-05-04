package cn.hdu.liu.obj;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 审计控制信息集合类
 * Ω=(ω1,ω2,...,ωk,…, ωn)
 * 每个审计记录ωk包含：操作主体、操作客体、操作类型、操作时间、区块哈希值
 */
@Data
public class AuditInfo {
    private List<AuditRecord> auditRecords = new ArrayList<>();

    @Data
    public static class AuditRecord {
        private String subject;          // 操作主体（如：用户ID、角色）
        private String object;           // 操作客体（如：数据ID、资源）
        private String operationType;    // 操作类型
        private String timestamp;        // 操作时间
        private String blockHash;        // 区块哈希值
        private String previousHash;     // 前一个记录的哈希值（用于链式追踪）

        /**
         * 设置操作主体
         * @param subject 操作主体
         */
        public void setSubject(String subject) {
            this.subject = subject;
        }

        /**
         * 获取操作主体
         * @return 操作主体
         */
        public String getSubject() {
            return this.subject;
        }

        /**
         * 设置操作客体
         * @param object 操作客体
         */
        public void setObject(String object) {
            this.object = object;
        }

        /**
         * 获取操作客体
         * @return 操作客体
         */
        public String getObject() {
            return this.object;
        }

        /**
         * 设置操作类型
         * @param operationType 操作类型
         */
        public void setOperationType(String operationType) {
            this.operationType = operationType;
        }

        /**
         * 获取操作类型
         * @return 操作类型
         */
        public String getOperationType() {
            return this.operationType;
        }

        /**
         * 设置操作时间
         * @param timestamp 操作时间
         */
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * 获取操作时间
         * @return 操作时间
         */
        public String getTimestamp() {
            return this.timestamp;
        }

        /**
         * 设置区块哈希值
         * @param blockHash 区块哈希值
         */
        public void setBlockHash(String blockHash) {
            this.blockHash = blockHash;
        }

        /**
         * 获取区块哈希值
         * @return 区块哈希值
         */
        public String getBlockHash() {
            return this.blockHash;
        }

        /**
         * 设置前一个记录的哈希值
         * @param previousHash 前一个记录的哈希值
         */
        public void setPreviousHash(String previousHash) {
            this.previousHash = previousHash;
        }

        /**
         * 获取前一个记录的哈希值
         * @return 前一个记录的哈希值
         */
        public String getPreviousHash() {
            return this.previousHash;
        }

        @Override
        public String toString() {
            return String.format(
                    "{\"主体\":\"%s\",\"客体\":\"%s\",\"操作\":\"%s\",\"时间\":\"%s\",\"区块哈希\":\"%s\",\"前向哈希\":\"%s\"}",
                    subject, object, operationType, timestamp, blockHash, previousHash
            );
        }
    }

    /**
     * 添加审计记录
     */
    public void addAuditRecord(String subject, String object, String operationType,
                               String timestamp, String blockHash) {
        AuditRecord record = new AuditRecord();
        record.setSubject(subject);
        record.setObject(object);
        record.setOperationType(operationType);
        record.setTimestamp(timestamp);
        record.setBlockHash(blockHash);

        // 设置前向哈希，用于链式追踪
        if (!auditRecords.isEmpty()) {
            record.setPreviousHash(auditRecords.get(auditRecords.size() - 1).getBlockHash());
        } else {
            record.setPreviousHash("genesis"); // 创世记录
        }

        auditRecords.add(record);
    }

    /**
     * 根据区块哈希值查找审计记录
     */
    public AuditRecord findByBlockHash(String blockHash) {
        return auditRecords.stream()
                .filter(record -> record.getBlockHash().equals(blockHash))
                .findFirst()
                .orElse(null);
    }

    /**
     * 追踪溯源：从指定记录开始，向前追踪所有相关记录
     */
    public List<AuditRecord> trace(String startBlockHash) {
        List<AuditRecord> trace = new ArrayList<>();
        AuditRecord current = findByBlockHash(startBlockHash);

        while (current != null && !current.getPreviousHash().equals("genesis")) {
            trace.add(current);
            current = findByBlockHash(current.getPreviousHash());
        }

        if (current != null) {
            trace.add(current); // 添加创世记录
        }

        return trace;
    }

    /**
     * 解析审计记录字符串
     * 格式：{用户A,数据1,查看,2025-03-31 23:30:00,0x1234...}
     */
    public void parseAuditRecord(String recordStr) {
        try {
            String content = recordStr.substring(1, recordStr.length() - 1);
            String[] parts = content.split(",");

            if (parts.length >= 5) {
                addAuditRecord(
                        parts[0].trim(),     // 主体
                        parts[1].trim(),     // 客体
                        parts[2].trim(),     // 操作类型
                        parts[3].trim(),     // 时间戳
                        parts[4].trim()      // 区块哈希
                );
            }
        } catch (Exception e) {
            // 解析失败时添加一个默认记录
            addAuditRecord(
                    "system",
                    "unknown",
                    "error",
                    LocalDateTime.now().toString(),
                    "0x0000"
            );
        }
    }

    @Override
    public String toString() {
        if (auditRecords.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{\"审计记录\":[");
        for (int i = 0; i < auditRecords.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(auditRecords.get(i).toString());
        }
        sb.append("]}");
        return sb.toString();
    }
    public List<AuditRecord> getAuditRecords() {
        return auditRecords;
    }

    public void setAuditRecords(List<AuditRecord> auditRecords) {
        this.auditRecords = auditRecords;
    }

}