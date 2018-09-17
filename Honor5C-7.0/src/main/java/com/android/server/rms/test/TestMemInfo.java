package com.android.server.rms.test;

import android.content.Context;
import android.util.Log;
import com.android.server.rms.collector.MemInfoReader;
import com.android.server.rms.collector.MemoryFragReader;
import com.android.server.rms.collector.ProcMemInfoReader;

public final class TestMemInfo {
    public static final void testMemoryInfoReader(Context context) {
        MemInfoReader memInfo = new MemInfoReader();
        memInfo.readMemInfo();
        Log.d("RMS.test", "Memory Info is: MemTotal=" + memInfo.getTotalSizeKb() + " MemFree=" + memInfo.getFreeSizeKb() + " Buffers=" + memInfo.getBuffersSizeKb() + " Cache=" + memInfo.getCachedSizeKb() + " SwapTotal=" + memInfo.getSwapTotalSizeKb() + " SwapFree=" + memInfo.getSwapFreeSizeKb() + " Slab=" + memInfo.getSlabSizeKb() + " SUnreclaim=" + memInfo.getSUnreclaimSizeKb());
    }

    public static final void testMemoryFragReader(Context context) {
        MemoryFragReader memFragInfo = new MemoryFragReader();
        memFragInfo.readMemFragInfo();
        int i = 0;
        for (int b : memFragInfo.getMemFragInfo()) {
            Log.d("RMS.test", "buddyinfo oder [" + i + "]:" + b);
            i++;
        }
    }

    public static final void testProcMemoryReader(Context context, String[] args) {
        ProcMemInfoReader procMemInfoReader = new ProcMemInfoReader();
        if (args.length == 2 && args[1] != null) {
            Log.d("RMS.test", "procName:" + args[1] + "pss:" + procMemInfoReader.getProcessPss(args[1]) + " KB");
        }
    }
}
