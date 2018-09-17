package android.media;

import android.content.Context;
import android.net.ProxyInfo;

public class HwCustAudioRecord {
    private static final String TAG = "HwCustAudioRecord";

    public String getAppName(Context mContext, int pid) {
        return ProxyInfo.LOCAL_EXCL_LIST;
    }

    public void preStartEC() {
    }

    public void stopEC() {
    }

    public String getPADECName(String pad_key) {
        return ProxyInfo.LOCAL_EXCL_LIST;
    }

    public Context getContext() {
        return null;
    }

    public boolean isSupportEc() {
        return false;
    }
}
