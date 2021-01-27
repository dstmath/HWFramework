package huawei.android.widget.plume.initializer;

import android.content.Context;
import android.util.Log;
import huawei.android.widget.plume.model.AppData;
import huawei.android.widget.plume.model.PlumeData;
import huawei.android.widget.plume.util.ParseUtil;
import java.util.Map;
import org.json.JSONObject;

public class AppInitializer extends BaseInitializer {
    private static final String APP_FILE_NAME = "app_plume.json";
    private static final boolean DEBUG = false;
    private static final String TAG = AppInitializer.class.getSimpleName();

    public AppInitializer(Context context, Map<Integer, PlumeData> data) {
        super(context, data);
    }

    @Override // huawei.android.widget.plume.initializer.BaseInitializer
    public int initialize() {
        if (this.mContext == null || this.mData == null) {
            return -1;
        }
        return parsePlume(APP_FILE_NAME, ParseUtil.createJsonObject(ParseUtil.readJson(this.mContext, APP_FILE_NAME)));
    }

    /* access modifiers changed from: package-private */
    @Override // huawei.android.widget.plume.initializer.BaseInitializer
    public int parsePlume(String fileName, JSONObject jsonObject) {
        if (jsonObject == null) {
            Log.w(TAG, "Plume: Json object is null!");
            return -1;
        }
        AppData appData = new AppData();
        ParseUtil.parseAttrs(jsonObject, appData);
        if (this.mData.putIfAbsent(Integer.valueOf(appData.getTargetId()), appData) == null) {
            return 1;
        }
        Log.i(TAG, "Plume: app data is already parsed!");
        return 1;
    }
}
