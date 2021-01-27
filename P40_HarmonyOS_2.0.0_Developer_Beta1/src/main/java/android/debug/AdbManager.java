package android.debug;

import android.content.Context;

public class AdbManager {
    private static final String TAG = "AdbManager";
    private final Context mContext;
    private final IAdbManager mService;

    public AdbManager(Context context, IAdbManager service) {
        this.mContext = context;
        this.mService = service;
    }
}
