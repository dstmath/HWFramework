package huawei.android.widget.plume.initializer;

import android.content.Context;
import android.os.AsyncTask;
import huawei.android.widget.plume.model.PlumeData;
import java.util.Map;

public abstract class BaseAsyncInitializer extends AsyncTask {
    protected static final int INIT_FAIL = -1;
    protected static final int INIT_SUCCESS = 1;
    Context mContext;
    Map<Integer, PlumeData> mData;

    public BaseAsyncInitializer(Context context, Map<Integer, PlumeData> data) {
        this.mContext = context;
        this.mData = data;
    }
}
