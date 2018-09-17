package com.tencent.tcuser.util;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.utils.f;
import tmsdkobf.me;

public class Encode {
    private static String TAG = "--Encode--";
    private static boolean isLoaded = false;

    public static class ProcessInfo {
        public String name;
        public int pid;
        public int ppid;
        public int uid;

        public ProcessInfo() {
            this.pid = 0;
            this.ppid = 0;
            this.name = null;
            this.uid = 0;
        }

        public ProcessInfo(int i, int i2, String str, int i3) {
            this.pid = i;
            this.ppid = i2;
            this.name = str;
            this.uid = i3;
        }

        public String toString() {
            return "PID=" + this.pid + " PPID=" + this.ppid + " NAME=" + this.name + " UID=" + this.uid;
        }
    }

    public static final native String cs(String str);

    private static synchronized void loadLib() {
        synchronized (Encode.class) {
            try {
                System.loadLibrary("xy");
                isLoaded = true;
            } catch (Throwable th) {
                me.a(new Thread(), th, "System.loadLibrary error", null);
                f.g(TAG, th);
            }
        }
        return;
    }

    private static final native void nativePs(List<String> list, List<ProcessInfo> list2);

    public static final synchronized List<ProcessInfo> ps(List<String> list) {
        List<ProcessInfo> arrayList;
        synchronized (Encode.class) {
            if (!isLoaded) {
                loadLib();
            }
            arrayList = new ArrayList();
            nativePs(list, arrayList);
        }
        return arrayList;
    }

    public static final native int pu(int i);

    public static native byte[] x(Context context, byte[] bArr);

    public static native byte[] y(Context context, byte[] bArr);
}
