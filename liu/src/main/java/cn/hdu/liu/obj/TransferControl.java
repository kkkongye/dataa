package cn.hdu.liu.obj;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TransferControl {
    private List<String> controls = new ArrayList<>();

    public void addControl(String control) {
        if (!controls.contains(control)) {
            controls.add(control);
        }
    }

    public void removeControl(String control) {
        controls.remove(control);
    }

    public boolean hasControl(String control) {
        return controls.contains(control);
    }

    @Override
    public String toString() {
        return controls.toString();
    }
}