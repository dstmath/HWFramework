package huawei.android.widget.plume.initializer;

import android.content.Context;
import android.util.Log;
import huawei.android.widget.plume.model.LayoutData;
import huawei.android.widget.plume.model.PlumeData;
import huawei.android.widget.plume.model.WidgetData;
import huawei.android.widget.plume.util.ParseUtil;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

public class LayoutInitializer extends BaseInitializer {
    private static final boolean DEBUG = false;
    private static final String ID = "id";
    private static final String LAYOUT = "layout";
    private static final int LENGTH_OF_PREFIX = 5;
    private static final int LENGTH_OF_SUFFIX = 5;
    private static final String TAG = LayoutInitializer.class.getSimpleName();
    private static final String USE_ATOM_ABILITY = "use_HwMagicLayout";
    private String mFileName;
    private Map<Integer, Set<Integer>> mPreData;

    public LayoutInitializer(Context context, Map<Integer, PlumeData> data, Map<Integer, Set<Integer>> preData, String fileName) {
        super(context, data);
        this.mPreData = preData;
        this.mFileName = fileName;
    }

    @Override // huawei.android.widget.plume.initializer.BaseInitializer
    public int initialize() {
        if (this.mContext == null || this.mData == null || this.mPreData == null || this.mFileName == null) {
            return -1;
        }
        return parsePlume(this.mFileName, ParseUtil.createJsonObject(ParseUtil.readJson(this.mContext, this.mFileName)));
    }

    /* access modifiers changed from: protected */
    @Override // huawei.android.widget.plume.initializer.BaseInitializer
    public int parsePlume(String fileName, JSONObject jsonObject) {
        if (fileName == null || jsonObject == null) {
            return -1;
        }
        int layoutId = this.mContext.getResources().getIdentifier(fileName.substring(0, fileName.length() - 5), LAYOUT, this.mContext.getPackageName());
        if (layoutId == 0) {
            String str = TAG;
            Log.e(str, "Plume: Get layout id failed! File name: " + fileName);
            return -1;
        }
        Set<Integer> widgetSet = new HashSet<>();
        LayoutData layoutData = new LayoutData(layoutId);
        parseLayout(jsonObject, layoutData, widgetSet);
        if (this.mData.putIfAbsent(Integer.valueOf(layoutId), layoutData) != null) {
            Log.i(TAG, "Plume: layout data is already parsed!");
        }
        if (widgetSet.isEmpty() || this.mPreData.putIfAbsent(Integer.valueOf(layoutId), widgetSet) == null) {
            return 1;
        }
        Log.i(TAG, "Plume: layout predata is already parsed!");
        return 1;
    }

    private void parseLayout(JSONObject layoutObject, LayoutData layoutData, Set<Integer> widgetSet) {
        Iterator<String> widgetIterator = layoutObject.keys();
        while (widgetIterator.hasNext()) {
            String widgetKey = widgetIterator.next();
            if (widgetKey != null) {
                if (!widgetKey.startsWith("R.id") || widgetKey.length() <= 5) {
                    String str = TAG;
                    Log.e(str, "Plume: Incorrect widget id format: " + widgetKey);
                } else {
                    int widgetId = this.mContext.getResources().getIdentifier(widgetKey.substring(5), ID, this.mContext.getPackageName());
                    if (widgetId == 0) {
                        String str2 = TAG;
                        Log.e(str2, "Plume: Get widget id failed: " + widgetKey);
                    } else {
                        parseWidget(widgetId, layoutObject.optJSONObject(widgetKey), layoutData, widgetSet);
                    }
                }
            }
        }
    }

    private void parseWidget(int widgetId, JSONObject jsonObject, LayoutData layoutData, Set<Integer> widgetSet) {
        WidgetData widgetData;
        String value;
        if (widgetId != 0 && jsonObject != null) {
            if (layoutData.hasWidget(widgetId)) {
                widgetData = layoutData.getWidget(widgetId);
                ParseUtil.parseAttrs(jsonObject, widgetData);
            } else {
                widgetData = new WidgetData(widgetId);
                ParseUtil.parseAttrs(jsonObject, widgetData);
                layoutData.addWidget(widgetId, widgetData);
            }
            if (widgetData != null && widgetData.hasAttribute(USE_ATOM_ABILITY) && (value = widgetData.removeAttribute(USE_ATOM_ABILITY)) != null && Boolean.parseBoolean(value)) {
                widgetSet.add(Integer.valueOf(widgetId));
            }
        }
    }
}
