package huawei.android.widget.plume.initializer;

import android.content.Context;
import android.util.Log;
import huawei.android.widget.plume.HwPlumeManager;
import huawei.android.widget.plume.model.MetaData;
import huawei.android.widget.plume.model.PlumeData;
import huawei.android.widget.plume.util.ParseUtil;
import java.util.Map;
import org.json.JSONObject;

public class MetaInfoAsyncInitializer extends BaseAsyncInitializer {
    private static final boolean DEBUG = false;
    private static final String META_INFO_FILE_NAME = "meta_info.json";
    private static final String TAG = MetaInfoAsyncInitializer.class.getSimpleName();

    public MetaInfoAsyncInitializer(Context context, Map<Integer, PlumeData> data) {
        super(context, data);
    }

    /* access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Object doInBackground(Object[] objects) {
        if (this.mContext == null || this.mData == null) {
            return null;
        }
        HwPlumeManager.getInstance(this.mContext).setMetaInfoLoadStatus(parsePlume(ParseUtil.createJsonObject(ParseUtil.readJson(this.mContext, META_INFO_FILE_NAME))));
        return null;
    }

    private int parsePlume(JSONObject jsonObject) {
        if (jsonObject == null) {
            Log.e(TAG, "Plume: Json object is null!");
            return -1;
        }
        MetaData metaData = new MetaData();
        ParseUtil.parseAttrs(jsonObject, metaData);
        if (this.mData.putIfAbsent(Integer.valueOf(metaData.getTargetId()), metaData) == null) {
            return 1;
        }
        Log.i(TAG, "Plume: meta info data is already parsed!");
        return 1;
    }
}
