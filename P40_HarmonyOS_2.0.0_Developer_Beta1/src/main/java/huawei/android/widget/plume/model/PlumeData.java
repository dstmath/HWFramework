package huawei.android.widget.plume.model;

import android.util.ArrayMap;
import java.util.Map;

public class PlumeData {
    private static final int DEFAULT_CAPACITY = 10;
    private Map<String, String> mAttributeMap = new ArrayMap(10);
    private int mTargetId;

    public PlumeData(int targetId) {
        this.mTargetId = targetId;
    }

    public int getTargetId() {
        return this.mTargetId;
    }

    public void addAttribute(String name, String value) {
        this.mAttributeMap.put(name, value);
    }

    public Map<String, String> getAttributeMap() {
        return this.mAttributeMap;
    }
}
