package huawei.android.widget.plume.initializer;

import android.content.Context;
import huawei.android.widget.plume.model.PlumeData;
import java.util.Map;
import org.json.JSONObject;

public abstract class BaseInitializer {
    protected static final int INIT_FAIL = -1;
    protected static final int INIT_SUCCESS = 1;
    Context mContext;
    Map<Integer, PlumeData> mData;

    public abstract int initialize();

    /* access modifiers changed from: package-private */
    public abstract int parsePlume(String str, JSONObject jSONObject);

    public BaseInitializer(Context context, Map<Integer, PlumeData> data) {
        this.mContext = context;
        this.mData = data;
    }
}
