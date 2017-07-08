package com.android.server.rms.test;

import android.content.Context;
import android.os.Bundle;
import android.rms.HwSysResManager;
import android.util.Log;
import com.android.server.rms.handler.MemoryHandler;
import com.android.server.rms.resource.HwSysInnerResImpl;
import com.android.server.rms.resource.MemoryInnerResource;

public final class TestMemory {
    private static final String TAG = "TestMemory";

    public static final void testMemQuery(Context context) {
        Bundle data = HwSysInnerResImpl.getResource(20).query();
        if (data != null) {
            Log.d(TAG, "data=" + data.toString());
        }
    }

    public static final void testMemAcquire(Context context) {
        HwSysInnerResImpl.getResource(20).acquire(null, null, null);
    }

    public static final void testMemAppAcquire(String[] args) {
        HwSysResManager sysResManager = HwSysResManager.getInstance();
        if (args.length == 2 && args[1] != null) {
            long free = Long.parseLong(args[1]);
            Bundle data = new Bundle();
            data.putLong("MemorySize", free);
            if (sysResManager != null) {
                sysResManager.acquireSysRes(20, null, null, data);
            }
        }
    }

    public static final void testDisableMemLog(Context context) {
        MemoryInnerResource.getInstance();
        MemoryInnerResource.disableDebug();
        MemoryHandler.getInstance(context);
        MemoryHandler.disableDebug();
    }

    public static final void testEnableMemLog(Context context) {
        MemoryInnerResource.getInstance();
        MemoryInnerResource.enableDebug();
        MemoryHandler.getInstance(context);
        MemoryHandler.enableDebug();
    }

    public static final void testMemInterrupt(Context context) {
        MemoryHandler.getInstance(context).interrupt();
    }
}
