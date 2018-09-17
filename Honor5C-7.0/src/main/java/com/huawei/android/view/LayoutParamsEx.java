package com.huawei.android.view;

import android.view.AbsLayoutParams;
import android.view.WindowManager.LayoutParams;

public class LayoutParamsEx extends AbsLayoutParams {
    LayoutParams attrs;

    private void setHwFlags(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.view.LayoutParamsEx.setHwFlags(int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.view.LayoutParamsEx.setHwFlags(int, int):void");
    }

    public LayoutParamsEx(LayoutParams lp) {
        this.attrs = lp;
    }

    public int getHwFlags() {
        return this.attrs.hwFlags;
    }

    public void addHwFlags(int hwFlags) {
        setHwFlags(hwFlags, hwFlags);
    }

    public void clearHwFlags(int hwFlags) {
        setHwFlags(0, hwFlags);
    }
}
