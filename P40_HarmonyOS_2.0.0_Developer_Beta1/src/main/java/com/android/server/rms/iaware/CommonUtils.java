package com.android.server.rms.iaware;

import android.rms.iaware.AwareLog;
import java.io.Closeable;
import java.io.IOException;

public class CommonUtils {
    private static final String TAG = "CommonUtils";

    public enum AwareVersion {
        FIRST(1),
        SECOND(2),
        FIFTH(5),
        SIXTH(6);
        
        private final int mVersion;

        private AwareVersion(int version) {
            this.mVersion = version;
        }

        public int getVersion() {
            return this.mVersion;
        }
    }

    public static void closeStream(Closeable io, String tag, String msg) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                String useTag = tag == null ? TAG : tag;
                if (msg != null) {
                    AwareLog.w(useTag, msg);
                } else {
                    AwareLog.w(useTag, "closeStream IOException");
                }
            }
        }
    }
}
