package huawei.android.widget.plume.model;

public class WidgetData extends PlumeData {
    public WidgetData(int targetId) {
        super(targetId);
    }

    public boolean hasAttribute(String attrName) {
        if (getAttributeMap().containsKey(attrName)) {
            return true;
        }
        return false;
    }

    public String removeAttribute(String attrName) {
        if (hasAttribute(attrName)) {
            return getAttributeMap().remove(attrName);
        }
        return null;
    }
}
