package android.mtp;

import android.content.ContentProviderClient;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.PowerManager;
import android.os.Process;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Video.VideoColumns;
import android.provider.OpenableColumns;
import android.provider.Settings.Bookmarks;
import android.provider.VoicemailContract.Voicemails;
import android.rms.iaware.AwareConstant.Database.HwUserData;
import android.util.Log;
import java.util.ArrayList;

class MtpPropertyGroup {
    private static final String FORMAT_WHERE = "format=?";
    private static final String ID_FORMAT_WHERE = "_id=? AND format=?";
    private static final String ID_WHERE = "_id=?";
    private static final String PARENT_FORMAT_WHERE = "parent=? AND format=?";
    private static final String PARENT_WHERE = "parent=?";
    private static final String TAG = "MtpPropertyGroup";
    private String[] mColumns;
    private final MtpDatabase mDatabase;
    private final Property[] mProperties;
    private final ContentProviderClient mProvider;
    private final Uri mUri;
    private final String mVolumeName;

    private class Property {
        int code;
        int column;
        int type;

        Property(int code, int type, int column) {
            this.code = code;
            this.type = type;
            this.column = column;
        }
    }

    public static native String format_date_time(long j);

    private java.lang.String queryAudio(int r11, java.lang.String r12) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x005d in list []
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
        r10 = this;
        r9 = 0;
        r7 = 0;
        r0 = r10.mVolumeName;
        r0 = android.provider.MediaStore.Audio.Media.getContentUri(r0);
        r0 = r0.buildUpon();
        r2 = "nonotify";
        r3 = "1";
        r0 = r0.appendQueryParameter(r2, r3);
        r1 = r0.build();
        r0 = r10.mProvider;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r2 = 2;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r3 = "_id";	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r4 = 0;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r3 = 1;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r2[r3] = r12;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r3 = "_id=?";	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r4 = 1;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r5 = java.lang.Integer.toString(r11);	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r6 = 0;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r4[r6] = r5;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r5 = 0;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r6 = 0;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r7 = r0.query(r1, r2, r3, r4, r5, r6);	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        if (r7 == 0) goto L_0x004e;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
    L_0x003d:
        r0 = r7.moveToNext();	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        if (r0 == 0) goto L_0x004e;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
    L_0x0043:
        r0 = 1;	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        r0 = r7.getString(r0);	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        if (r7 == 0) goto L_0x004d;
    L_0x004a:
        r7.close();
    L_0x004d:
        return r0;
    L_0x004e:
        r0 = "";	 Catch:{ Exception -> 0x0057, all -> 0x005e }
        if (r7 == 0) goto L_0x0056;
    L_0x0053:
        r7.close();
    L_0x0056:
        return r0;
    L_0x0057:
        r8 = move-exception;
        if (r7 == 0) goto L_0x005d;
    L_0x005a:
        r7.close();
    L_0x005d:
        return r9;
    L_0x005e:
        r0 = move-exception;
        if (r7 == 0) goto L_0x0064;
    L_0x0061:
        r7.close();
    L_0x0064:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpPropertyGroup.queryAudio(int, java.lang.String):java.lang.String");
    }

    private java.lang.String queryGenre(int r11) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x005e in list []
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
        r10 = this;
        r9 = 0;
        r7 = 0;
        r0 = r10.mVolumeName;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r0 = android.provider.MediaStore.Audio.Genres.getContentUriForAudioId(r0, r11);	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r0 = r0.buildUpon();	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r2 = "nonotify";	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r3 = "1";	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r0 = r0.appendQueryParameter(r2, r3);	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r1 = r0.build();	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r0 = r10.mProvider;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r2 = 2;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r3 = "_id";	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r4 = 0;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r2[r4] = r3;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r3 = "name";	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r4 = 1;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r2[r4] = r3;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r3 = 0;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r4 = 0;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r5 = 0;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r6 = 0;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r7 = r0.query(r1, r2, r3, r4, r5, r6);	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        if (r7 == 0) goto L_0x0046;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
    L_0x0035:
        r0 = r7.moveToNext();	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        if (r0 == 0) goto L_0x0046;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
    L_0x003b:
        r0 = 1;	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r0 = r7.getString(r0);	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        if (r7 == 0) goto L_0x0045;
    L_0x0042:
        r7.close();
    L_0x0045:
        return r0;
    L_0x0046:
        r0 = "";	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        if (r7 == 0) goto L_0x004e;
    L_0x004b:
        r7.close();
    L_0x004e:
        return r0;
    L_0x004f:
        r8 = move-exception;
        r0 = "MtpPropertyGroup";	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        r2 = "queryGenre exception";	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        android.util.Log.e(r0, r2, r8);	 Catch:{ Exception -> 0x004f, all -> 0x005f }
        if (r7 == 0) goto L_0x005e;
    L_0x005b:
        r7.close();
    L_0x005e:
        return r9;
    L_0x005f:
        r0 = move-exception;
        if (r7 == 0) goto L_0x0065;
    L_0x0062:
        r7.close();
    L_0x0065:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpPropertyGroup.queryGenre(int):java.lang.String");
    }

    private java.lang.String queryString(int r11, java.lang.String r12) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0047 in list []
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
        r10 = this;
        r9 = 0;
        r7 = 0;
        r0 = r10.mProvider;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r1 = r10.mUri;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r2 = 2;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r3 = "_id";	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r4 = 0;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r3 = 1;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r2[r3] = r12;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r3 = "_id=?";	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r4 = 1;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r5 = java.lang.Integer.toString(r11);	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r6 = 0;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r4[r6] = r5;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r5 = 0;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r6 = 0;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r7 = r0.query(r1, r2, r3, r4, r5, r6);	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        if (r7 == 0) goto L_0x0038;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
    L_0x0027:
        r0 = r7.moveToNext();	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        if (r0 == 0) goto L_0x0038;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
    L_0x002d:
        r0 = 1;	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        r0 = r7.getString(r0);	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        if (r7 == 0) goto L_0x0037;
    L_0x0034:
        r7.close();
    L_0x0037:
        return r0;
    L_0x0038:
        r0 = "";	 Catch:{ Exception -> 0x0041, all -> 0x0048 }
        if (r7 == 0) goto L_0x0040;
    L_0x003d:
        r7.close();
    L_0x0040:
        return r0;
    L_0x0041:
        r8 = move-exception;
        if (r7 == 0) goto L_0x0047;
    L_0x0044:
        r7.close();
    L_0x0047:
        return r9;
    L_0x0048:
        r0 = move-exception;
        if (r7 == 0) goto L_0x004e;
    L_0x004b:
        r7.close();
    L_0x004e:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpPropertyGroup.queryString(int, java.lang.String):java.lang.String");
    }

    android.mtp.MtpPropertyList getPropertyList(int r35, int r36, int r37) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0126 in list []
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
        r34 = this;
        r2 = 1;
        r0 = r37;
        if (r0 <= r2) goto L_0x000f;
    L_0x0005:
        r2 = new android.mtp.MtpPropertyList;
        r3 = 0;
        r4 = 43016; // 0xa808 float:6.0278E-41 double:2.12527E-319;
        r2.<init>(r3, r4);
        return r2;
    L_0x000f:
        if (r36 != 0) goto L_0x005a;
    L_0x0011:
        r2 = -1;
        r0 = r35;
        if (r0 != r2) goto L_0x0043;
    L_0x0016:
        r5 = 0;
        r6 = 0;
    L_0x0018:
        r20 = 0;
        if (r37 > 0) goto L_0x0021;
    L_0x001c:
        r2 = -1;
        r0 = r35;
        if (r0 != r2) goto L_0x008b;
    L_0x0021:
        r0 = r34;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r0.mProvider;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r34;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r3 = r0.mUri;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r34;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r4 = r0.mColumns;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r7 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r20 = r2.query(r3, r4, r5, r6, r7, r8);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r20 != 0) goto L_0x0093;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0035:
        r2 = new android.mtp.MtpPropertyList;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r3 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r4 = 8201; // 0x2009 float:1.1492E-41 double:4.052E-320;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2.<init>(r3, r4);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r20 == 0) goto L_0x0042;
    L_0x003f:
        r20.close();
    L_0x0042:
        return r2;
    L_0x0043:
        r2 = 1;
        r6 = new java.lang.String[r2];
        r2 = java.lang.Integer.toString(r35);
        r3 = 0;
        r6[r3] = r2;
        r2 = 1;
        r0 = r37;
        if (r0 != r2) goto L_0x0056;
    L_0x0052:
        r5 = "parent=?";
        goto L_0x0018;
    L_0x0056:
        r5 = "_id=?";
        goto L_0x0018;
    L_0x005a:
        r2 = -1;
        r0 = r35;
        if (r0 != r2) goto L_0x006d;
    L_0x005f:
        r5 = "format=?";
        r2 = 1;
        r6 = new java.lang.String[r2];
        r2 = java.lang.Integer.toString(r36);
        r3 = 0;
        r6[r3] = r2;
        goto L_0x0018;
    L_0x006d:
        r2 = 2;
        r6 = new java.lang.String[r2];
        r2 = java.lang.Integer.toString(r35);
        r3 = 0;
        r6[r3] = r2;
        r2 = java.lang.Integer.toString(r36);
        r3 = 1;
        r6[r3] = r2;
        r2 = 1;
        r0 = r37;
        if (r0 != r2) goto L_0x0087;
    L_0x0083:
        r5 = "parent=? AND format=?";
        goto L_0x0018;
    L_0x0087:
        r5 = "_id=? AND format=?";
        goto L_0x0018;
    L_0x008b:
        r0 = r34;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r0.mColumns;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r2.length;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r3 = 1;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r2 > r3) goto L_0x0021;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0093:
        if (r20 != 0) goto L_0x0107;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0095:
        r22 = 1;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0097:
        r8 = new android.mtp.MtpPropertyList;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r34;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r0.mProperties;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r2.length;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r2 * r22;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r3 = 8193; // 0x2001 float:1.1481E-41 double:4.048E-320;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.<init>(r2, r3);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r29 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x00a7:
        r0 = r29;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r1 = r22;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r0 >= r1) goto L_0x0279;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x00ad:
        r26 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r20 == 0) goto L_0x00d4;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x00b1:
        r20.moveToNext();	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r20;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r0.getLong(r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = (int) r2;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r35 = r0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = "media_type";	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r20;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r0.getColumnIndex(r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r20;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r27 = r0.getInt(r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = 2;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r27;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r0 != r2) goto L_0x010c;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x00d2:
        r26 = 1;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x00d4:
        r31 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x00d6:
        r0 = r34;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r0.mProperties;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r2.length;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r31;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r0 >= r2) goto L_0x0275;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x00df:
        r0 = r34;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r0.mProperties;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r30 = r2[r31];	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r30;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r10 = r0.code;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r30;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r0.column;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r21 = r0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        switch(r10) {
            case 56323: goto L_0x010f;
            case 56327: goto L_0x0127;
            case 56329: goto L_0x0178;
            case 56385: goto L_0x01ad;
            case 56388: goto L_0x0144;
            case 56390: goto L_0x01d6;
            case 56398: goto L_0x0178;
            case 56459: goto L_0x01c1;
            case 56460: goto L_0x0212;
            case 56473: goto L_0x0188;
            case 56474: goto L_0x01f4;
            case 56978: goto L_0x023b;
            case 56979: goto L_0x022d;
            case 56980: goto L_0x023b;
            case 56985: goto L_0x022d;
            case 56986: goto L_0x022d;
            default: goto L_0x00f2;
        };	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x00f2:
        r0 = r30;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r0.type;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r3 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r2 != r3) goto L_0x0249;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x00fb:
        r2 = r20.getString(r21);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.append(r0, r10, r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0104:
        r31 = r31 + 1;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x00d6;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0107:
        r22 = r20.getCount();	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0097;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x010c:
        r26 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x00d4;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x010f:
        r12 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r11 = 4;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r9 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.append(r9, r10, r11, r12);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;
    L_0x0118:
        r24 = move-exception;
        r2 = new android.mtp.MtpPropertyList;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r3 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r4 = 8194; // 0x2002 float:1.1482E-41 double:4.0484E-320;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2.<init>(r3, r4);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r20 == 0) goto L_0x0126;
    L_0x0123:
        r20.close();
    L_0x0126:
        return r2;
    L_0x0127:
        r32 = r20.getString(r21);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r32 == 0) goto L_0x013e;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x012d:
        r2 = nameFromPath(r32);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.append(r0, r10, r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;
    L_0x0137:
        r2 = move-exception;
        if (r20 == 0) goto L_0x013d;
    L_0x013a:
        r20.close();
    L_0x013d:
        throw r2;
    L_0x013e:
        r2 = 8201; // 0x2009 float:1.1492E-41 double:4.052E-320;
        r8.setResult(r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0144:
        r28 = r20.getString(r21);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r28 != 0) goto L_0x0155;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x014a:
        r2 = "name";	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r34;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r1 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r28 = r0.queryString(r1, r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0155:
        if (r28 != 0) goto L_0x0168;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0157:
        r2 = "_data";	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r34;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r1 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r28 = r0.queryString(r1, r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r28 == 0) goto L_0x0168;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0164:
        r28 = nameFromPath(r28);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0168:
        if (r28 == 0) goto L_0x0172;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x016a:
        r0 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r1 = r28;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.append(r0, r10, r1);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0172:
        r2 = 8201; // 0x2009 float:1.1492E-41 double:4.052E-320;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.setResult(r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0178:
        r2 = r20.getInt(r21);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = (long) r2;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = format_date_time(r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.append(r0, r10, r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0188:
        r33 = r20.getInt(r21);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2.<init>();	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r3 = java.lang.Integer.toString(r33);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r2.append(r3);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r3 = "0101T000000";	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r2.append(r3);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r23 = r2.toString();	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r1 = r23;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.append(r0, r10, r1);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x01ad:
        r12 = r20.getLong(r21);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = 32;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r12 = r12 << r2;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = (long) r0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r12 = r12 + r2;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r11 = 10;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r9 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.append(r9, r10, r11, r12);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x01c1:
        r2 = r20.getInt(r21);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r2 % 1000;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = (long) r2;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r18 = r0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r17 = 4;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r14 = r8;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r15 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r16 = r10;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r14.append(r15, r16, r17, r18);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x01d6:
        if (r26 == 0) goto L_0x01ea;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x01d8:
        r2 = "artist";	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r34;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r1 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r0.queryAudio(r1, r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.append(r0, r10, r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x01ea:
        r2 = "";	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.append(r0, r10, r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x01f4:
        if (r26 == 0) goto L_0x0208;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x01f6:
        r2 = "album";	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r34;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r1 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r0.queryAudio(r1, r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.append(r0, r10, r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0208:
        r2 = "";	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.append(r0, r10, r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0212:
        r25 = "";	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r26 == 0) goto L_0x021b;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0217:
        r25 = r34.queryGenre(r35);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x021b:
        if (r25 == 0) goto L_0x0226;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x021d:
        r0 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r1 = r25;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.append(r0, r10, r1);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0226:
        r2 = 8201; // 0x2009 float:1.1492E-41 double:4.052E-320;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r8.setResult(r2);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x022d:
        r18 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r17 = 6;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r14 = r8;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r15 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r16 = r10;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r14.append(r15, r16, r17, r18);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x023b:
        r18 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r17 = 4;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r14 = r8;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r15 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r16 = r10;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r14.append(r15, r16, r17, r18);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0249:
        r0 = r30;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r2 = r0.type;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        if (r2 != 0) goto L_0x0261;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x024f:
        r0 = r30;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r0.type;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r17 = r0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r18 = 0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r14 = r8;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r15 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r16 = r10;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r14.append(r15, r16, r17, r18);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
    L_0x0261:
        r0 = r30;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r0 = r0.type;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r17 = r0;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r18 = r20.getLong(r21);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r14 = r8;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r15 = r35;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r16 = r10;	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        r14.append(r15, r16, r17, r18);	 Catch:{ RemoteException -> 0x0118, all -> 0x0137 }
        goto L_0x0104;
    L_0x0275:
        r29 = r29 + 1;
        goto L_0x00a7;
    L_0x0279:
        if (r20 == 0) goto L_0x027e;
    L_0x027b:
        r20.close();
    L_0x027e:
        return r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpPropertyGroup.getPropertyList(int, int, int):android.mtp.MtpPropertyList");
    }

    public MtpPropertyGroup(MtpDatabase database, ContentProviderClient provider, String volumeName, int[] properties) {
        int i;
        this.mDatabase = database;
        this.mProvider = provider;
        this.mVolumeName = volumeName;
        this.mUri = Files.getMtpObjectsUri(volumeName).buildUpon().appendQueryParameter("nonotify", WifiEnterpriseConfig.ENGINE_ENABLE).build();
        int count = properties.length;
        ArrayList<String> columns = new ArrayList(count);
        columns.add(HwUserData._ID);
        columns.add(FileColumns.MEDIA_TYPE);
        this.mProperties = new Property[count];
        for (i = 0; i < count; i++) {
            this.mProperties[i] = createProperty(properties[i], columns);
        }
        count = columns.size();
        this.mColumns = new String[count];
        for (i = 0; i < count; i++) {
            this.mColumns[i] = (String) columns.get(i);
        }
    }

    private Property createProperty(int code, ArrayList<String> columns) {
        int type;
        Object column = null;
        switch (code) {
            case MtpConstants.PROPERTY_STORAGE_ID /*56321*/:
                column = FileColumns.STORAGE_ID;
                type = 6;
                break;
            case MtpConstants.PROPERTY_OBJECT_FORMAT /*56322*/:
                column = FileColumns.FORMAT;
                type = 4;
                break;
            case MtpConstants.PROPERTY_PROTECTION_STATUS /*56323*/:
                type = 4;
                break;
            case MtpConstants.PROPERTY_OBJECT_SIZE /*56324*/:
                column = OpenableColumns.SIZE;
                type = 8;
                break;
            case MtpConstants.PROPERTY_OBJECT_FILE_NAME /*56327*/:
                column = Voicemails._DATA;
                type = PowerManager.WAKE_LOCK_LEVEL_MASK;
                break;
            case MtpConstants.PROPERTY_DATE_MODIFIED /*56329*/:
                column = PlaylistsColumns.DATE_MODIFIED;
                type = PowerManager.WAKE_LOCK_LEVEL_MASK;
                break;
            case MtpConstants.PROPERTY_PARENT_OBJECT /*56331*/:
                column = FileColumns.PARENT;
                type = 6;
                break;
            case MtpConstants.PROPERTY_PERSISTENT_UID /*56385*/:
                column = FileColumns.STORAGE_ID;
                type = 10;
                break;
            case MtpConstants.PROPERTY_NAME /*56388*/:
                column = Bookmarks.TITLE;
                type = PowerManager.WAKE_LOCK_LEVEL_MASK;
                break;
            case MtpConstants.PROPERTY_ARTIST /*56390*/:
                type = PowerManager.WAKE_LOCK_LEVEL_MASK;
                break;
            case MtpConstants.PROPERTY_DESCRIPTION /*56392*/:
                column = VideoColumns.DESCRIPTION;
                type = PowerManager.WAKE_LOCK_LEVEL_MASK;
                break;
            case MtpConstants.PROPERTY_DATE_ADDED /*56398*/:
                column = PlaylistsColumns.DATE_ADDED;
                type = PowerManager.WAKE_LOCK_LEVEL_MASK;
                break;
            case MtpConstants.PROPERTY_DURATION /*56457*/:
                column = DevStatusProperty.VIBRATOR_DURATION;
                type = 6;
                break;
            case MtpConstants.PROPERTY_TRACK /*56459*/:
                column = AudioColumns.TRACK;
                type = 4;
                break;
            case MtpConstants.PROPERTY_GENRE /*56460*/:
                type = PowerManager.WAKE_LOCK_LEVEL_MASK;
                break;
            case MtpConstants.PROPERTY_COMPOSER /*56470*/:
                column = AudioColumns.COMPOSER;
                type = PowerManager.WAKE_LOCK_LEVEL_MASK;
                break;
            case MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE /*56473*/:
                column = AudioColumns.YEAR;
                type = PowerManager.WAKE_LOCK_LEVEL_MASK;
                break;
            case MtpConstants.PROPERTY_ALBUM_NAME /*56474*/:
                type = PowerManager.WAKE_LOCK_LEVEL_MASK;
                break;
            case MtpConstants.PROPERTY_ALBUM_ARTIST /*56475*/:
                column = AudioColumns.ALBUM_ARTIST;
                type = PowerManager.WAKE_LOCK_LEVEL_MASK;
                break;
            case MtpConstants.PROPERTY_DISPLAY_NAME /*56544*/:
                column = OpenableColumns.DISPLAY_NAME;
                type = PowerManager.WAKE_LOCK_LEVEL_MASK;
                break;
            case MtpConstants.PROPERTY_BITRATE_TYPE /*56978*/:
            case MtpConstants.PROPERTY_NUMBER_OF_CHANNELS /*56980*/:
                type = 4;
                break;
            case MtpConstants.PROPERTY_SAMPLE_RATE /*56979*/:
            case MtpConstants.PROPERTY_AUDIO_WAVE_CODEC /*56985*/:
            case MtpConstants.PROPERTY_AUDIO_BITRATE /*56986*/:
                type = 6;
                break;
            default:
                type = 0;
                Log.e(TAG, "unsupported property " + code);
                break;
        }
        if (column == null) {
            return new Property(code, type, -1);
        }
        columns.add(column);
        return new Property(code, type, columns.size() - 1);
    }

    private Long queryLong(int id, String column) {
        Cursor cursor = null;
        try {
            cursor = this.mProvider.query(this.mUri, new String[]{HwUserData._ID, column}, ID_WHERE, new String[]{Integer.toString(id)}, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            Long l = new Long(cursor.getLong(1));
            if (cursor != null) {
                cursor.close();
            }
            return l;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String nameFromPath(String path) {
        int start = 0;
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0) {
            start = lastSlash + 1;
        }
        int end = path.length();
        if (end - start > Process.PROC_TERM_MASK) {
            end = start + Process.PROC_TERM_MASK;
        }
        return path.substring(start, end);
    }
}
