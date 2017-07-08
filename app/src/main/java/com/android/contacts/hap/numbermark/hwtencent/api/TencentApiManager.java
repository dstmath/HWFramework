package com.android.contacts.hap.numbermark.hwtencent.api;

import android.content.Context;
import android.database.Cursor;
import com.android.contacts.hap.service.NumberMarkInfo;

public final class TencentApiManager {
    private static final String CALL_TYPE_CALLED = "18";
    private static final String CALL_TYPE_CALLING = "17";
    private static final String CALL_TYPE_COMMON = "16";
    private static final String CONNECTION_TIMEOUT = "connect overtime";
    private static final String NUMBER_MARK_INFO_NO_ATTRIBUTE = "";
    private static final int PROP_TAG = 1;
    private static final int PROP_TAG_YELLOW = 3;
    private static final int PROP_YELLOW = 2;
    private static final String QUERY_TYPE_CLOUD = "1";
    private static final String QUERY_TYPE_LOCAL = "0";
    private static final String TAG = "TencentApiManager";
    private static final int TAG_TYPE_CRANK = 50;
    private static final int TAG_TYPE_EXPRESS = 55;
    private static final int TAG_TYPE_FRAUD = 54;
    private static final int TAG_TYPE_HOUSE_AGENT = 51;
    private static final int TAG_TYPE_PROMOTE_SALES = 53;
    private static final int TAG_TYPE_TAXI = 56;
    private static final boolean TENCENT_DEFAULT_CLOUD_MARK = true;
    private static final String TIME_OUT_LIMIT = "2000";
    private static final String URI_TENCENT = "content://com.huawei.systemmanager.BlockCheckProvider/numbermark";
    private static volatile TencentApiManager mInfoManager;
    private Context mContext;

    public com.android.contacts.hap.service.NumberMarkInfo cloudFetchNumberInfo(java.lang.String r12, java.lang.String r13) {
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
        r11 = this;
        r10 = 0;
        r0 = "content://com.huawei.systemmanager.BlockCheckProvider/numbermark";
        r6 = android.net.Uri.parse(r0);
        r1 = android.net.Uri.withAppendedPath(r6, r12);
        r9 = "16";
        r0 = "18";
        r0 = r0.equals(r13);
        if (r0 == 0) goto L_0x0058;
    L_0x0018:
        r9 = "18";
    L_0x001b:
        r7 = 0;
        r0 = r11.mContext;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r0 = r0.getContentResolver();	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r2 = 3;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r3 = "1";	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r4 = 0;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r3 = 1;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r2[r3] = r9;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r3 = "2000";	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r4 = 2;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r3 = 0;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r4 = 0;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r5 = 0;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r7 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        if (r7 != 0) goto L_0x0065;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
    L_0x003d:
        r0 = com.android.contacts.util.HwLog.HWFLOW;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        if (r0 == 0) goto L_0x004a;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
    L_0x0041:
        r0 = "TencentApiManager";	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r2 = "tencent connect timeout";	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        com.android.contacts.util.HwLog.d(r0, r2);	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
    L_0x004a:
        r0 = new com.android.contacts.hap.service.NumberMarkInfo;	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r2 = "connect overtime";	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        r0.<init>(r2);	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        if (r7 == 0) goto L_0x0057;
    L_0x0054:
        r7.close();
    L_0x0057:
        return r0;
    L_0x0058:
        r0 = "17";
        r0 = r0.equals(r13);
        if (r0 == 0) goto L_0x001b;
    L_0x0061:
        r9 = "17";
        goto L_0x001b;
    L_0x0065:
        r0 = r11.revertCursorToNumberMarkInfo(r7, r12);	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        if (r7 == 0) goto L_0x006e;
    L_0x006b:
        r7.close();
    L_0x006e:
        return r0;
    L_0x006f:
        r8 = move-exception;
        r8.printStackTrace();	 Catch:{ Exception -> 0x006f, all -> 0x0079 }
        if (r7 == 0) goto L_0x0078;
    L_0x0075:
        r7.close();
    L_0x0078:
        return r10;
    L_0x0079:
        r0 = move-exception;
        if (r7 == 0) goto L_0x007f;
    L_0x007c:
        r7.close();
    L_0x007f:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.numbermark.hwtencent.api.TencentApiManager.cloudFetchNumberInfo(java.lang.String, java.lang.String):com.android.contacts.hap.service.NumberMarkInfo");
    }

    public com.android.contacts.hap.service.NumberMarkInfo localFetchNumberInfo(java.lang.String r11) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0045 in list []
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
        r0 = "content://com.huawei.systemmanager.BlockCheckProvider/numbermark";
        r6 = android.net.Uri.parse(r0);
        r1 = android.net.Uri.withAppendedPath(r6, r11);
        r7 = 0;
        r0 = r10.mContext;	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        r0 = r0.getContentResolver();	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        r2 = 1;	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        r3 = "0";	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        r4 = 0;	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        r3 = 0;	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        r4 = 0;	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        r5 = 0;	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        r7 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        if (r7 != 0) goto L_0x0032;	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
    L_0x0025:
        r0 = com.android.contacts.util.HwLog.HWFLOW;	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        if (r0 == 0) goto L_0x0032;	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
    L_0x0029:
        r0 = "TencentApiManager";	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        r2 = "tencent preset db no this number info";	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        com.android.contacts.util.HwLog.i(r0, r2);	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
    L_0x0032:
        r0 = r10.revertCursorToNumberMarkInfo(r7, r11);	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        if (r7 == 0) goto L_0x003b;
    L_0x0038:
        r7.close();
    L_0x003b:
        return r0;
    L_0x003c:
        r8 = move-exception;
        r8.printStackTrace();	 Catch:{ Exception -> 0x003c, all -> 0x0046 }
        if (r7 == 0) goto L_0x0045;
    L_0x0042:
        r7.close();
    L_0x0045:
        return r9;
    L_0x0046:
        r0 = move-exception;
        if (r7 == 0) goto L_0x004c;
    L_0x0049:
        r7.close();
    L_0x004c:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.numbermark.hwtencent.api.TencentApiManager.localFetchNumberInfo(java.lang.String):com.android.contacts.hap.service.NumberMarkInfo");
    }

    private TencentApiManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static TencentApiManager getInstance(Context context) {
        if (mInfoManager == null) {
            mInfoManager = new TencentApiManager(context);
        }
        return mInfoManager;
    }

    private NumberMarkInfo revertCursorToNumberMarkInfo(Cursor cursor, String num) {
        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }
        int property = cursor.getInt(cursor.getColumnIndex("property"));
        int tagCount = cursor.getInt(cursor.getColumnIndex("tagCount"));
        String tagName = NUMBER_MARK_INFO_NO_ATTRIBUTE;
        int tagType = cursor.getInt(cursor.getColumnIndex("tagType"));
        String name = NUMBER_MARK_INFO_NO_ATTRIBUTE;
        switch (property) {
            case PROP_TAG /*1*/:
                name = cursor.getString(cursor.getColumnIndex("tagName"));
                switch (tagType) {
                    case TAG_TYPE_CRANK /*50*/:
                        tagName = "crank";
                        break;
                    case TAG_TYPE_HOUSE_AGENT /*51*/:
                        tagName = "house agent";
                        break;
                    case TAG_TYPE_PROMOTE_SALES /*53*/:
                        tagName = "promote sales";
                        break;
                    case TAG_TYPE_FRAUD /*54*/:
                        tagName = "fraud";
                        break;
                    case TAG_TYPE_EXPRESS /*55*/:
                        tagName = "express";
                        break;
                    case TAG_TYPE_TAXI /*56*/:
                        tagName = "taxi";
                        break;
                    default:
                        return null;
                }
                return new NumberMarkInfo(num, NUMBER_MARK_INFO_NO_ATTRIBUTE, name, tagName, TENCENT_DEFAULT_CLOUD_MARK, tagCount, "tencent");
            case PROP_YELLOW /*2*/:
            case PROP_TAG_YELLOW /*3*/:
                return null;
            default:
                return null;
        }
    }
}
