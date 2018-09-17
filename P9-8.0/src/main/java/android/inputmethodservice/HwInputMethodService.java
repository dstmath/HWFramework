package android.inputmethodservice;

import android.content.Context;
import android.util.Log;

public class HwInputMethodService implements IHwInputMethodService {
    private static final boolean DEBUG = false;
    private static final String TAG = "HwInputMethodService";
    private static HwInputMethodService mHwInputMethodService;
    private Context mContext;

    private HwInputMethodService(Context context) {
        this.mContext = context;
        Log.d(TAG, "mContext : " + this.mContext);
    }

    public static synchronized HwInputMethodService getInstance(Context context) {
        HwInputMethodService hwInputMethodService;
        synchronized (HwInputMethodService.class) {
            if (mHwInputMethodService == null) {
                mHwInputMethodService = new HwInputMethodService(context);
            }
            hwInputMethodService = mHwInputMethodService;
        }
        return hwInputMethodService;
    }

    public boolean updateImeDockVisibility(boolean visibility) {
        return false;
    }

    public boolean updateImeDockConfiguration(boolean visibility) {
        return false;
    }

    public boolean handleImeDockDestroy() {
        return false;
    }

    public float getImeDockWidthFactor() {
        return 0.0f;
    }

    public void updateImeDockWidth() {
    }

    public void updateImeDockPosition(boolean reset) {
    }

    private int getSplitFocusPosition() {
        return 0;
    }
}
