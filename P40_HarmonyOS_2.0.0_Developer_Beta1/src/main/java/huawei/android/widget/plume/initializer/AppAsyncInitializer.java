package huawei.android.widget.plume.initializer;

import android.content.Context;
import android.util.Log;
import huawei.android.widget.plume.HwPlumeManager;
import huawei.android.widget.plume.model.AppData;
import huawei.android.widget.plume.model.PlumeData;
import huawei.android.widget.plume.util.ParseUtil;
import java.util.Map;
import org.json.JSONObject;

public class AppAsyncInitializer extends BaseAsyncInitializer {
    private static final String APP_FILE_NAME = "app_plume.json";
    private static final boolean DEBUG = false;
    private static final String TAG = AppAsyncInitializer.class.getSimpleName();

    public AppAsyncInitializer(Context context, Map<Integer, PlumeData> data) {
        super(context, data);
    }

    /* access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Object doInBackground(Object[] objects) {
        if (this.mContext == null || this.mData == null) {
            return null;
        }
        HwPlumeManager.getInstance(this.mContext).setAppLoadStatus(parsePlume(ParseUtil.createJsonObject(ParseUtil.readJson(this.mContext, APP_FILE_NAME))));
        return null;
    }

    private int parsePlume(JSONObject jsonObject) {
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
