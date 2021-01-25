package android.media;

import android.content.Context;

public class HwCustAudioRecord {
    private static final String TAG = "HwCustAudioRecord";

    public String getAppName(Context mContext, int pid) {
        return "";
    }

    public void preStartEC() {
    }

    public void stopEC() {
    }

    public String getPADECName(String pad_key) {
        return "";
    }

    public Context getContext() {
        return null;
    }

    public boolean isSupportEc() {
        return false;
    }
}
