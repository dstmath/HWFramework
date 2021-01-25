package com.huawei.server.wm;

import android.util.Slog;

public class DefaultHwSingleHandAdapter implements IHwSingleHandAdapter {
    private static final String TAG = DefaultHwSingleHandAdapter.class.getSimpleName();
    private static DefaultHwSingleHandAdapter mAdapter;

    public void registorLocked() {
        Slog.i(TAG, "default adapter, lazy mode don't support");
    }

    public static synchronized IHwSingleHandAdapter getDefault() {
        DefaultHwSingleHandAdapter defaultHwSingleHandAdapter;
        synchronized (DefaultHwSingleHandAdapter.class) {
            if (mAdapter == null) {
                mAdapter = new DefaultHwSingleHandAdapter();
            }
            defaultHwSingleHandAdapter = mAdapter;
        }
        return defaultHwSingleHandAdapter;
    }
}
