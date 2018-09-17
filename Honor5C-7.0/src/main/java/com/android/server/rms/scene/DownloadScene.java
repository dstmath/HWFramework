package com.android.server.rms.scene;

import android.content.Context;
import com.android.server.rms.IScene;

public class DownloadScene implements IScene {
    private static final String TAG = "RMS.DownloadScene";
    private final Context mContext;

    public boolean identify(android.os.Bundle r10) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0078 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r9 = this;
        r8 = 0;
        r5 = r9.mContext;
        if (r5 != 0) goto L_0x0006;
    L_0x0005:
        return r8;
    L_0x0006:
        r5 = r9.mContext;
        r6 = "download";
        r1 = r5.getSystemService(r6);
        r1 = (android.app.DownloadManager) r1;
        if (r1 != 0) goto L_0x001d;
    L_0x0013:
        r5 = "RMS.DownloadScene";
        r6 = "DownloadScene dm is null";
        android.util.Log.w(r5, r6);
        return r8;
    L_0x001d:
        r0 = 0;
        r3 = new android.app.DownloadManager$Query;	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        r3.<init>();	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        r5 = 3;	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        r3.setFilterByStatus(r5);	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        r0 = r1.query(r3);	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        if (r0 != 0) goto L_0x003c;	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
    L_0x002d:
        r5 = "RMS.DownloadScene";	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        r6 = "DownloadScene cursor is null";	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        android.util.Log.w(r5, r6);	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        if (r0 == 0) goto L_0x003b;
    L_0x0038:
        r0.close();
    L_0x003b:
        return r8;
    L_0x003c:
        r5 = r0.getCount();	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        if (r5 <= 0) goto L_0x0067;	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
    L_0x0042:
        r4 = 1;	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
    L_0x0043:
        r5 = com.android.server.rms.utils.Utils.DEBUG;	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        if (r5 == 0) goto L_0x0061;	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
    L_0x0047:
        r5 = "RMS.DownloadScene";	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        r6.<init>();	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        r7 = "DownloadScene state is ";	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        r6 = r6.append(r4);	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        android.util.Log.d(r5, r6);	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
    L_0x0061:
        if (r0 == 0) goto L_0x0066;
    L_0x0063:
        r0.close();
    L_0x0066:
        return r4;
    L_0x0067:
        r4 = 0;
        goto L_0x0043;
    L_0x0069:
        r2 = move-exception;
        r5 = "RMS.DownloadScene";	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        r6 = "DownloadManager Query failed:";	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        android.util.Log.e(r5, r6, r2);	 Catch:{ Exception -> 0x0069, all -> 0x0079 }
        if (r0 == 0) goto L_0x0078;
    L_0x0075:
        r0.close();
    L_0x0078:
        return r8;
    L_0x0079:
        r5 = move-exception;
        if (r0 == 0) goto L_0x007f;
    L_0x007c:
        r0.close();
    L_0x007f:
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.scene.DownloadScene.identify(android.os.Bundle):boolean");
    }

    public DownloadScene(Context context) {
        this.mContext = context;
    }
}
