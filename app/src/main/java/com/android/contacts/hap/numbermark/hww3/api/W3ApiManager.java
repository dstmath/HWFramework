package com.android.contacts.hap.numbermark.hww3.api;

import android.content.Context;
import com.android.contacts.util.HwLog;

public final class W3ApiManager {
    private static final String CONNECTION_TIMEOUT = "connect overtime";
    private static final String NUMBER_MARK_INFO_NO_ATTRIBUTE = "";
    private static final int RETURN_CODE_BUSINESS_EXCEPTION = 103;
    private static final int RETURN_CODE_MUTI_RESULT = 101;
    private static final int RETURN_CODE_PARAM_ERROR = 105;
    private static final int RETURN_CODE_SUCCESS = 100;
    private static final int RETURN_CODE_TIME_OUT = 102;
    private static final int RETURN_CODE_W3_LOG_OUT = 104;
    private static final String TAG = "W3ApiManager";
    private static final boolean W3_DEFAULT_CLOUD_MARK = true;
    private static final String W3_DEFAULT_MARK_CLASSIFY = "w3";
    private static final int W3_DEFAULT_MARK_COUNT = -1;
    private static final String W3_QUERY_URI = "content://huawei.w3.contact/query/";
    private static final String W3_TIMEOUT_LIMIT = "&2";
    private static volatile W3ApiManager mInfoManager;
    private Context mContext;

    public com.android.contacts.hap.service.NumberMarkInfo getMarkInfoFromW3Server(java.lang.String r19) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00fa in list []
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
        r18 = this;
        r1 = android.text.TextUtils.isEmpty(r19);
        if (r1 == 0) goto L_0x0008;
    L_0x0006:
        r1 = 0;
        return r1;
    L_0x0008:
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r0 = r19;
        r1 = r1.append(r0);
        r3 = "&2";
        r1 = r1.append(r3);
        r16 = r1.toString();
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r3 = "content://huawei.w3.contact/query/";
        r1 = r1.append(r3);
        r0 = r16;
        r1 = r1.append(r0);
        r1 = r1.toString();
        r2 = android.net.Uri.parse(r1);
        r13 = 0;
        r0 = r18;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = r0.mContext;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = r1.getContentResolver();	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r3 = 0;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r4 = 0;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r5 = 0;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r6 = 0;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r13 = r1.query(r2, r3, r4, r5, r6);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        if (r13 != 0) goto L_0x0052;
    L_0x004b:
        r1 = 0;
        if (r13 == 0) goto L_0x0051;
    L_0x004e:
        r13.close();
    L_0x0051:
        return r1;
    L_0x0052:
        r1 = r13.moveToFirst();	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        if (r1 == 0) goto L_0x0121;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
    L_0x0058:
        r1 = "code";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = r13.getColumnIndex(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r17 = r13.getInt(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        switch(r17) {
            case 100: goto L_0x006d;
            case 101: goto L_0x00e7;
            case 102: goto L_0x00d1;
            case 103: goto L_0x00fc;
            case 104: goto L_0x010d;
            case 105: goto L_0x0117;
            default: goto L_0x0066;
        };
    L_0x0066:
        r1 = 0;
        if (r13 == 0) goto L_0x006c;
    L_0x0069:
        r13.close();
    L_0x006c:
        return r1;
    L_0x006d:
        r1 = "name";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = r13.getColumnIndex(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r15 = r13.getString(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = "account";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = r13.getColumnIndex(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r12 = r13.getString(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = "department";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = r13.getColumnIndex(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r11 = r13.getString(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = android.text.TextUtils.isEmpty(r15);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        if (r1 != 0) goto L_0x009a;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
    L_0x0094:
        r1 = android.text.TextUtils.isEmpty(r12);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        if (r1 == 0) goto L_0x00a1;
    L_0x009a:
        r1 = 0;
        if (r13 == 0) goto L_0x00a0;
    L_0x009d:
        r13.close();
    L_0x00a0:
        return r1;
    L_0x00a1:
        r3 = new com.android.contacts.hap.service.NumberMarkInfo;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r5 = "";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1.<init>();	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = r1.append(r15);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r4 = " ";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = r1.append(r4);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = r1.append(r12);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r6 = r1.toString();	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r7 = "w3";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r10 = "w3";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r8 = 1;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r9 = -1;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r4 = r19;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r3.<init>(r4, r5, r6, r7, r8, r9, r10, r11);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        if (r13 == 0) goto L_0x00d0;
    L_0x00cd:
        r13.close();
    L_0x00d0:
        return r3;
    L_0x00d1:
        r1 = "w3 time out error";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r0 = r18;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r0.w3log(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1 = new com.android.contacts.hap.service.NumberMarkInfo;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r3 = "connect overtime";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r1.<init>(r3);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        if (r13 == 0) goto L_0x00e6;
    L_0x00e3:
        r13.close();
    L_0x00e6:
        return r1;
    L_0x00e7:
        r1 = "w3 muti result error";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r0 = r18;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r0.w3log(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        goto L_0x0066;
    L_0x00f1:
        r14 = move-exception;
        r14.printStackTrace();	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        if (r13 == 0) goto L_0x00fa;
    L_0x00f7:
        r13.close();
    L_0x00fa:
        r1 = 0;
        return r1;
    L_0x00fc:
        r1 = "w3 business error";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r0 = r18;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r0.w3log(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        goto L_0x0066;
    L_0x0106:
        r1 = move-exception;
        if (r13 == 0) goto L_0x010c;
    L_0x0109:
        r13.close();
    L_0x010c:
        throw r1;
    L_0x010d:
        r1 = "w3 log out error";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r0 = r18;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r0.w3log(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        goto L_0x0066;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
    L_0x0117:
        r1 = "w3 param error";	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r0 = r18;	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        r0.w3log(r1);	 Catch:{ Exception -> 0x00f1, all -> 0x0106 }
        goto L_0x0066;
    L_0x0121:
        if (r13 == 0) goto L_0x00fa;
    L_0x0123:
        r13.close();
        goto L_0x00fa;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.numbermark.hww3.api.W3ApiManager.getMarkInfoFromW3Server(java.lang.String):com.android.contacts.hap.service.NumberMarkInfo");
    }

    private W3ApiManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static W3ApiManager getInstance(Context context) {
        if (mInfoManager == null) {
            mInfoManager = new W3ApiManager(context);
        }
        return mInfoManager;
    }

    private void w3log(String msg) {
        HwLog.i(TAG, msg);
    }
}
