package huawei.com.android.server;

import android.content.Context;
import com.huawei.android.app.HiEventEx;
import com.huawei.android.app.HiViewEx;

public class HiEventProxy {
    private static final int APP_INFO_INIT_EVENT_ID = 992711011;
    private static final int NR_SLICE_SUPPORTED_EVENT_ID = 992711010;
    private static final int REGISTER_LISTENER_EVENT_ID = 992711012;
    private static final int RELEASE_NETWORK_SLICE_EVENT_ID = 992711015;
    private static final int REQUEST_NETWORK_SLICE_EVENT_ID = 992711014;
    private static final int SERVER_BASE_EVENT_ID = 992711000;
    private static final String TAG = "HiEventProxy";
    private static final String TAG_ACTIVATE_TYPE = "activateType";
    private static final String TAG_APP_ID = "appId";
    private static final String TAG_REQUEST_ID = "requestId";
    private static final String TAG_RESULT = "result";
    private static final String TAG_UID = "uid";
    private static final int UNREGISTER_LISTENER_EVENT_ID = 992711013;
    private Context mContext;

    public void init(Context context) {
        this.mContext = context;
    }

    public void nrSliceSupportedHiEvent() {
        if (this.mContext != null) {
            HiEventEx event = new HiEventEx((int) NR_SLICE_SUPPORTED_EVENT_ID);
            event.putAppInfo(this.mContext);
            HiViewEx.report(event);
        }
    }

    public void appInfoInitHiEvent(int uid, String appId, boolean result) {
        if (this.mContext != null) {
            HiEventEx event = new HiEventEx((int) APP_INFO_INIT_EVENT_ID);
            event.putInt("uid", uid).putString(TAG_APP_ID, appId).putBool(TAG_RESULT, result).putAppInfo(this.mContext);
            HiViewEx.report(event);
        }
    }

    public void registerListenerHiEvent() {
        if (this.mContext != null) {
            HiEventEx event = new HiEventEx((int) REGISTER_LISTENER_EVENT_ID);
            event.putAppInfo(this.mContext);
            HiViewEx.report(event);
        }
    }

    public void unregisterListenerHiEvent() {
        if (this.mContext != null) {
            HiEventEx event = new HiEventEx((int) UNREGISTER_LISTENER_EVENT_ID);
            event.putAppInfo(this.mContext);
            HiViewEx.report(event);
        }
    }

    public void requestNetworkSliceHiEvent(int uid, int requestId, int activateType, boolean result) {
        if (this.mContext != null) {
            HiEventEx event = new HiEventEx((int) REQUEST_NETWORK_SLICE_EVENT_ID);
            event.putInt("uid", uid).putInt(TAG_REQUEST_ID, requestId).putInt(TAG_ACTIVATE_TYPE, activateType).putBool(TAG_RESULT, result).putAppInfo(this.mContext);
            HiViewEx.report(event);
        }
    }

    public void releaseNetworkSliceHiEvent(int uid, int requestId, boolean result) {
        if (this.mContext != null) {
            HiEventEx event = new HiEventEx((int) RELEASE_NETWORK_SLICE_EVENT_ID);
            event.putInt("uid", uid).putInt(TAG_REQUEST_ID, requestId).putBool(TAG_RESULT, result).putAppInfo(this.mContext);
            HiViewEx.report(event);
        }
    }

    public static HiEventProxy getInstance() {
        return SingletonInstance.INSTANCE;
    }

    private static class SingletonInstance {
        private static final HiEventProxy INSTANCE = new HiEventProxy();

        private SingletonInstance() {
        }
    }
}
