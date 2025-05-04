package cn.hdu.liu.obj;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ConstraintSet {
    @JsonProperty("constraints")
    private List<Constraint> constraints = new ArrayList<>();

    // 添加约束（移除选中逻辑）
    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);
    }

    // 获取约束数组格式（使用全部约束）
    @JsonIgnore
    public List<String> getConstraintArray() {
        return constraints.stream()
                .flatMap(c -> c.getConstraintArray().stream())
                .collect(Collectors.toList());
    }

    // 设置约束数组
    public void setConstraintArray(List<String> constraintValues) {
        Constraint constraint = new Constraint();
        constraint.parseFromArray(constraintValues);
        addConstraint(constraint);
    }

    @Override
    public String toString() {
        return constraints.toString();
    }

    // 获取可读的中文描述（使用全部约束）
    @JsonIgnore
    public String getDescription() {
        if (!constraints.isEmpty()) {
            return constraints.stream()
                    .map(Constraint::getDescription)
                    .collect(Collectors.joining("\n\n"));
        }
        return "约束条件集合：空";
    }

    // Getter for constraints（保持不变）
    public List<Constraint> getConstraints() {
        return new ArrayList<>(constraints);
    }

    // Setter for constraints（简化逻辑）
    public void setConstraints(List<Constraint> constraints) {
        this.constraints = new ArrayList<>(constraints);
    }
}