package cn.hdu.liu.obj;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Data
@Setter
public class Constraint {
    // 地域性约束的可选值
    public static final String REGION_INTRANET = "内网";
    public static final String REGION_INTERNET = "外网";

    // 访问约束的可选值
    public static final String ACCESS_ALL_ALLOWED = "全部允许";
    public static final String ACCESS_ADMIN_ONLY = "只允许管理方获取";

    // 路径约束的可选值
    public static final String PATH_POINT_TO_POINT = "点对点";
    public static final String PATH_BROADCAST = "广播";

    // 共享约束的可选值
    public static final String SHARE_ALLOWED = "允许共享";
    public static final String SHARE_FORBIDDEN = "不允许共享";

    // 格式约束的可选值
    public static final String FORMAT_XLSX = "xlsx";
    public static final String FORMAT_JPG = "jpg";

    private String formatConstraint;      // 格式约束，如 "xlsx"
    private String accessConstraint;      // 访问权限，如 "全部允许"
    private String pathConstraint;        // 传输路径约束，如 "点对点"
    private String regionConstraint;      // 地域性约束，如 "内网"
    private String shareConstraint;       // 共享约束，如 "允许共享"


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constraint that = (Constraint) o;
        return Objects.equals(formatConstraint, that.formatConstraint) &&
                Objects.equals(accessConstraint, that.accessConstraint) &&
                Objects.equals(pathConstraint, that.pathConstraint) &&
                Objects.equals(regionConstraint, that.regionConstraint) &&
                Objects.equals(shareConstraint, that.shareConstraint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formatConstraint, accessConstraint, pathConstraint, regionConstraint, shareConstraint);
    }
    // 手动添加setter和getter
    public String getFormatConstraint() {
        return formatConstraint;
    }

    public void setFormatConstraint(String formatConstraint) {
        this.formatConstraint = formatConstraint;
    }

    public String getAccessConstraint() {
        return accessConstraint;
    }

    public void setAccessConstraint(String accessConstraint) {
        this.accessConstraint = accessConstraint;
    }

    public String getPathConstraint() {
        return pathConstraint;
    }

    public void setPathConstraint(String pathConstraint) {
        this.pathConstraint = pathConstraint;
    }

    public String getRegionConstraint() {
        return regionConstraint;
    }

    public void setRegionConstraint(String regionConstraint) {
        this.regionConstraint = regionConstraint;
    }

    public String getShareConstraint() {
        return shareConstraint;
    }

    public void setShareConstraint(String shareConstraint) {
        this.shareConstraint = shareConstraint;
    }
    // 获取约束数组格式
    @JsonIgnore
    public List<String> getConstraintArray() {
        return Arrays.asList(
                "格式约束:" + formatConstraint,
                "访问权限:" + accessConstraint,
                "传输路径约束:" + pathConstraint,
                "地域性约束:" + regionConstraint,
                "共享约束:" + shareConstraint
        );
    }

    // 获取所有可用的地域性约束选项
    @JsonIgnore
    public List<String> getAvailableRegionConstraints() {
        return Arrays.asList(REGION_INTRANET, REGION_INTERNET);
    }

    // 获取所有可用的访问约束选项
    @JsonIgnore
    public List<String> getAvailableAccessConstraints() {
        return Arrays.asList(ACCESS_ALL_ALLOWED, ACCESS_ADMIN_ONLY);
    }

    // 获取所有可用的路径约束选项
    @JsonIgnore
    public List<String> getAvailablePathConstraints() {
        return Arrays.asList(PATH_POINT_TO_POINT, PATH_BROADCAST);
    }

    // 获取所有可用的共享约束选项
    @JsonIgnore
    public List<String> getAvailableShareConstraints() {
        return Arrays.asList(SHARE_ALLOWED, SHARE_FORBIDDEN);
    }

    // 获取所有可用的格式约束选项
    @JsonIgnore
    public List<String> getAvailableFormatConstraints() {
        return Arrays.asList(FORMAT_XLSX, FORMAT_JPG);
    }

    // 从约束数组解析
    public void parseFromArray(List<String> constraints) {
        for (String constraint : constraints) {
            String[] parts = constraint.split(":");
            if (parts.length != 2) continue;

            String key = parts[0].trim();
            String value = parts[1].trim();

            switch (key) {
                case "格式约束":
                    setFormatConstraint(value);
                    break;
                case "访问权限":
                    setAccessConstraint(value);
                    break;
                case "传输路径约束":
                    setPathConstraint(value);
                    break;
                case "地域性约束":
                    setRegionConstraint(value);
                    break;
                case "共享约束":
                    setShareConstraint(value);
                    break;
            }
        }
    }

    @Override
    public String toString() {
        return String.format("θ={%s,%s,%s,%s,%s}",
                formatConstraint,
                accessConstraint,
                pathConstraint,
                regionConstraint,
                shareConstraint
        );
    }

    // 获取可读的中文描述
    @JsonIgnore
    public String getDescription() {
        StringBuilder sb = new StringBuilder("约束条件：\n");
        sb.append("格式约束: ").append(formatConstraint).append("\n");
        sb.append("访问权限: ").append(accessConstraint).append("\n");
        sb.append("传输路径约束: ").append(pathConstraint).append("\n");
        sb.append("地域性约束: ").append(regionConstraint).append("\n");
        sb.append("共享约束: ").append(shareConstraint).append("\n");
        return sb.toString();
    }
}