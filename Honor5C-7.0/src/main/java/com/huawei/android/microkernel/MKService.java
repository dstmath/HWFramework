package com.huawei.android.microkernel;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import defpackage.aw;

public class MKService extends Service {
    private static final String TAG = "PushLog2828";
    private static Context appContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.microkernel.MKService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.microkernel.MKService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.microkernel.MKService.<clinit>():void");
    }

    public static Context getAppContext() {
        return appContext;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void stopService() {
        try {
            if (appContext == null) {
                aw.d(TAG, " stopService,stop Pushservice ");
                stopSelf();
                return;
            }
            ComponentName componentName = new ComponentName(appContext, "com.huawei.deviceCloud.microKernel.push.PushMKService");
            Intent intent = new Intent();
            intent.setComponent(componentName);
            intent.setPackage(appContext.getPackageName());
            appContext.stopService(intent);
            aw.d(TAG, " stopService,stop Push Microkernel service ");
        } catch (Exception e) {
            aw.d(TAG, "Stop service fail");
        }
    }
}
