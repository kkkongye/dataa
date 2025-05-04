package cn.hdu.liu.obj;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PropagationControl {
    @JsonProperty("operations")
    private Map<String, Integer> operations = new HashMap<>();

    // 定义所有可用的操作类型
    public static final String[] AVAILABLE_OPERATIONS = {
            "modify",    // 可修改
            "delegate", // 可委托
            "share",    // 可共享
            "read",     // 可读
            "destroy"   // 可销毁
    };

    // 初始化所有操作为禁止状态
    public PropagationControl() {
        for (String operation : AVAILABLE_OPERATIONS) {
            operations.put(operation, 0);
        }
    }

    // 管理员选择操作权限
    public void selectOperation(String operation, boolean allowed) {
        if (!operations.containsKey(operation)) {
            throw new IllegalArgumentException("未知的操作类型: " + operation);
        }
        operations.put(operation, allowed ? 1 : 0);
    }

    // 检查操作是否被允许
    public boolean isOperationAllowed(String operation) {
        return operations.getOrDefault(operation, 0) == 1;
    }

    // 兼容性方法
    public boolean isCanModify() {
        return isOperationAllowed("modify");
    }

    public boolean isCanRead() {
        return isOperationAllowed("read");
    }

    public boolean isCanShare() {
        return isOperationAllowed("share");
    }

    public boolean isCanDelegate() {
        return isOperationAllowed("delegate");
    }

    public boolean isCanDestroy() {
        return isOperationAllowed("destroy");
    }

    // 兼容性设置方法
    public void setCanModify(boolean allowed) {
        selectOperation("modify", allowed);
    }

    public void setCanRead(boolean allowed) {
        selectOperation("read", allowed);
    }

    public void setCanShare(boolean allowed) {
        selectOperation("share", allowed);
    }

    public void setCanDelegate(boolean allowed) {
        selectOperation("delegate", allowed);
    }

    public void setCanDestroy(boolean allowed) {
        selectOperation("destroy", allowed);
    }

    // 获取所有已选择的操作
    public Map<String, Boolean> getSelectedOperations() {
        Map<String, Boolean> selected = new HashMap<>();
        for (Map.Entry<String, Integer> entry : operations.entrySet()) {
            selected.put(entry.getKey(), entry.getValue() == 1);
        }
        return selected;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Ψ=(");
        boolean first = true;
        for (String operation : AVAILABLE_OPERATIONS) {
            if (!first) {
                sb.append(",");
            }
            sb.append(operation).append("=").append(operations.get(operation));
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    // 获取可读的中文描述
    @JsonIgnore
    public String getDescription() {
        StringBuilder sb = new StringBuilder("传播控制操作集合：\n");
        for (String operation : AVAILABLE_OPERATIONS) {
            String operationName = getOperationName(operation);
            String status = operations.get(operation) == 1 ? "允许" : "禁止";
            sb.append(operationName).append(": ").append(status).append("\n");
        }
        return sb.toString();
    }

    private String getOperationName(String operation) {
        switch (operation.toLowerCase()) {
            case "modify": return "可修改";
            case "delegate": return "可委托";
            case "share": return "可共享";
            case "read": return "可读";
            case "destroy": return "可销毁";
            default: return operation;
        }
    }
}