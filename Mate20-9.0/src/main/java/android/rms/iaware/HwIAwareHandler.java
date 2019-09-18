package android.rms.iaware;

import android.os.Message;

public class HwIAwareHandler {
    private static final String TAG = "HwIAwareHandler";
    private static final String WECHAT_ATTR_SWITCH = "switch";
    private static final String WECHAT_ATTR_TAG = "messageTag";
    private static volatile HwIAwareHandler mHwIAwareHandler;
    private boolean mIsOptEnabled = false;
    private String mLuckyMoneyTag;

    public static HwIAwareHandler getInstance() {
        if (mHwIAwareHandler == null) {
            synchronized (HwIAwareHandler.class) {
                if (mHwIAwareHandler == null) {
                    mHwIAwareHandler = new HwIAwareHandler();
                }
            }
        }
        return mHwIAwareHandler;
    }

    private HwIAwareHandler() {
    }

    private void checkoutConfig() {
        if (this.mLuckyMoneyTag == null) {
            FastgrabConfigReader fastgrabConfigReader = FastgrabConfigReader.getInstance(null);
            if (fastgrabConfigReader != null) {
                boolean z = true;
                if (fastgrabConfigReader.getInt(WECHAT_ATTR_SWITCH) != 1) {
                    z = false;
                }
                this.mIsOptEnabled = z;
                this.mLuckyMoneyTag = fastgrabConfigReader.getString(WECHAT_ATTR_TAG);
                if (this.mLuckyMoneyTag != null && this.mLuckyMoneyTag.length() < 10) {
                    this.mIsOptEnabled = false;
                    AwareLog.e(TAG, "LuckyMoneyTag is too short!");
                }
            }
        }
    }

    public long resetDelayMills(Message msg, long delayMillis) {
        checkoutConfig();
        if (!this.mIsOptEnabled || this.mLuckyMoneyTag == null || !msg.getData().toString().contains(this.mLuckyMoneyTag)) {
            return delayMillis;
        }
        AwareLog.i(TAG, "LuckyMoney Catched!");
        return 0;
    }
}
