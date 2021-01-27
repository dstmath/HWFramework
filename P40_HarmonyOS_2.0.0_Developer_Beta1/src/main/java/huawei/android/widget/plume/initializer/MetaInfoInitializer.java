package huawei.android.widget.plume.initializer;

import android.content.Context;
import android.util.Log;
import huawei.android.widget.plume.model.MetaData;
import huawei.android.widget.plume.model.PlumeData;
import huawei.android.widget.plume.util.ParseUtil;
import java.util.Map;
import org.json.JSONObject;

public class MetaInfoInitializer extends BaseInitializer {
    private static final boolean DEBUG = false;
    private static final String META_INFO_FILE_NAME = "meta_info.json";
    private static final String TAG = MetaInfoInitializer.class.getSimpleName();

    public MetaInfoInitializer(Context context, Map<Integer, PlumeData> data) {
        super(context, data);
    }

    @Override // huawei.android.widget.plume.initializer.BaseInitializer
    public int initialize() {
        if (this.mContext == null || this.mData == null) {
            return -1;
        }
        return parsePlume(META_INFO_FILE_NAME, ParseUtil.createJsonObject(ParseUtil.readJson(this.mContext, META_INFO_FILE_NAME)));
    }

    /* access modifiers changed from: protected */
    @Override // huawei.android.widget.plume.initializer.BaseInitializer
    public int parsePlume(String fileName, JSONObject jsonObject) {
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
