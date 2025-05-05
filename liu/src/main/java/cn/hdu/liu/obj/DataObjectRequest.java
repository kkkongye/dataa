package cn.hdu.liu.obj;

public class DataObjectRequest {
    private ConstraintSet constraintSet;         // 约束条件
    private PropagationControl propagationControl;   // 传播控制
    private LocationInfo locationInfo;// 定位信息
    private Metadata metadata;

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }




    public ConstraintSet getConstraintSet() {
        return constraintSet;
    }


    public void setConstraintSet(ConstraintSet constraintSet) {
        this.constraintSet = constraintSet;
    }


    public PropagationControl getPropagationControl() {
        return propagationControl;
    }


    public void setPropagationControl(PropagationControl propagationControl) {
        this.propagationControl = propagationControl;
    }


    public LocationInfo getLocationInfo() {
        return locationInfo;
    }


    public void setLocationInfo(LocationInfo locationInfo) {
        this.locationInfo = locationInfo;
    }
}