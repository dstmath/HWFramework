package huawei.android.widget.plume.model;

import android.util.ArrayMap;
import java.util.Map;

public class LayoutData extends PlumeData {
    private static final int DEFAULT_CAPACITY = 10;
    private Map<Integer, WidgetData> mWidgetMap = new ArrayMap(10);

    public LayoutData(int targetId) {
        super(targetId);
    }

    public void addWidget(int widgetId, WidgetData widgetData) {
        this.mWidgetMap.put(Integer.valueOf(widgetId), widgetData);
    }

    public Map<Integer, WidgetData> getWidgetMap() {
        return this.mWidgetMap;
    }

    public WidgetData getWidget(int widgetId) {
        if (this.mWidgetMap.containsKey(Integer.valueOf(widgetId))) {
            return this.mWidgetMap.get(Integer.valueOf(widgetId));
        }
        return null;
    }

    public boolean hasWidget(int widgetId) {
        if (!this.mWidgetMap.containsKey(Integer.valueOf(widgetId)) || this.mWidgetMap.get(Integer.valueOf(widgetId)) == null) {
            return false;
        }
        return true;
    }
}
