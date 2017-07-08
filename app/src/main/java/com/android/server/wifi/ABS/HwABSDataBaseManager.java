package com.android.server.wifi.ABS;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl;

public class HwABSDataBaseManager {
    private static final String TAG = "DataBaseManager";
    private static HwABSDataBaseManager mHwABSDataBaseManager;
    private SQLiteDatabase mDatabase;
    private HwABSDataBaseHelper mHelper;
    private Object mLock;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.ABS.HwABSDataBaseManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.ABS.HwABSDataBaseManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.ABS.HwABSDataBaseManager.<clinit>():void");
    }

    public com.android.server.wifi.ABS.HwABSApInfoData getApInfoByBssid(java.lang.String r20) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00b9 in list []
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
        r19 = this;
        r3 = 0;
        if (r20 != 0) goto L_0x0004;
    L_0x0003:
        return r3;
    L_0x0004:
        r2 = 0;
        r0 = r19;	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r4 = r0.mDatabase;	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r5 = "SELECT * FROM MIMOApInfoTable where bssid like ?";	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r6 = 1;	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r6 = new java.lang.String[r6];	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r7 = 0;	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r6[r7] = r20;	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r2 = r4.rawQuery(r5, r6);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r4 = r2.moveToNext();	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        if (r4 == 0) goto L_0x0093;	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
    L_0x001c:
        r3 = new com.android.server.wifi.ABS.HwABSApInfoData;	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r4 = "bssid";	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r4 = r2.getColumnIndex(r4);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r4 = r2.getString(r4);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r5 = "ssid";	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r5 = r2.getColumnIndex(r5);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r5 = r2.getString(r5);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r6 = "switch_mimo_type";	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r6 = r2.getColumnIndex(r6);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r6 = r2.getInt(r6);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r7 = "switch_siso_type";	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r7 = r2.getColumnIndex(r7);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r7 = r2.getInt(r7);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r8 = "auth_type";	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r8 = r2.getColumnIndex(r8);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r8 = r2.getInt(r8);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r9 = "in_back_list";	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r9 = r2.getColumnIndex(r9);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r9 = r2.getInt(r9);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r10 = "mimo_time";	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r10 = r2.getColumnIndex(r10);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r10 = r2.getLong(r10);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r12 = "siso_time";	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r12 = r2.getColumnIndex(r12);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r12 = r2.getLong(r12);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r14 = "total_time";	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r14 = r2.getColumnIndex(r14);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r14 = r2.getLong(r14);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r16 = "last_connect_time";	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r0 = r16;	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r16 = r2.getColumnIndex(r0);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r0 = r16;	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r16 = r2.getLong(r0);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r3.<init>(r4, r5, r6, r7, r8, r9, r10, r12, r14, r16);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
    L_0x0093:
        if (r2 == 0) goto L_0x0098;
    L_0x0095:
        r2.close();
    L_0x0098:
        return r3;
    L_0x0099:
        r18 = move-exception;
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r4.<init>();	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r5 = "getApInfoByBssid:";	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r0 = r18;	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r4 = r4.append(r0);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r4 = r4.toString();	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        com.android.server.wifi.ABS.HwABSUtils.logE(r4);	 Catch:{ Exception -> 0x0099, all -> 0x00ba }
        r4 = 0;
        if (r2 == 0) goto L_0x00b9;
    L_0x00b6:
        r2.close();
    L_0x00b9:
        return r4;
    L_0x00ba:
        r4 = move-exception;
        if (r2 == 0) goto L_0x00c0;
    L_0x00bd:
        r2.close();
    L_0x00c0:
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.ABS.HwABSDataBaseManager.getApInfoByBssid(java.lang.String):com.android.server.wifi.ABS.HwABSApInfoData");
    }

    private HwABSDataBaseManager(Context context) {
        this.mLock = new Object();
        HwABSUtils.logD("HwABSDataBaseManager()");
        this.mHelper = new HwABSDataBaseHelper(context);
        this.mDatabase = this.mHelper.getWritableDatabase();
    }

    public static HwABSDataBaseManager getInstance(Context context) {
        if (mHwABSDataBaseManager == null) {
            mHwABSDataBaseManager = new HwABSDataBaseManager(context);
        }
        return mHwABSDataBaseManager;
    }

    public void closeDB() {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return;
            }
            HwABSUtils.logD("HwABSDataBaseManager closeDB()");
            this.mDatabase.close();
        }
    }

    public void addOrUpdateApInfos(HwABSApInfoData data) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || data == null) {
                return;
            }
            if (getApInfoByBssid(data.mBssid) == null) {
                HwABSUtils.logD("addOrUpdateApInfos inlineAddApInfo");
                inlineAddApInfo(data);
            } else {
                HwABSUtils.logD("addOrUpdateApInfos inlineUpdateApInfo");
                HwABSUtils.logD("addOrUpdateApInfos mTotal_time = " + data.mTotal_time + " mSiso_time = " + data.mSiso_time + " mMimo_time = " + data.mMimo_time);
                inlineUpdateApInfo(data);
            }
        }
    }

    public void deleteAPInfosByBssid(HwABSApInfoData data) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || data == null) {
                return;
            }
            inlineDeleteApInfoByBssid(data.mBssid);
        }
    }

    public void deleteAPInfosBySsid(HwABSApInfoData data) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || data == null) {
                return;
            }
            inlineDeleteApInfoBySsid(data.mSsid);
        }
    }

    private void inlineDeleteApInfoBySsid(String ssid) {
        if (this.mDatabase != null && this.mDatabase.isOpen() && ssid != null) {
            this.mDatabase.delete(HwABSDataBaseHelper.MIMO_AP_TABLE_NAME, "ssid like ?", new String[]{ssid});
        }
    }

    private void inlineDeleteApInfoByBssid(String bssid) {
        if (this.mDatabase != null && this.mDatabase.isOpen() && bssid != null) {
            this.mDatabase.delete(HwABSDataBaseHelper.MIMO_AP_TABLE_NAME, "bssid like ?", new String[]{bssid});
        }
    }

    private void inlineAddApInfo(HwABSApInfoData data) {
        if (this.mDatabase != null && this.mDatabase.isOpen() && data.mBssid != null) {
            this.mDatabase.execSQL("INSERT INTO MIMOApInfoTable VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{data.mBssid, data.mSsid, Integer.valueOf(data.mSwitch_mimo_type), Integer.valueOf(data.mSwitch_siso_type), Integer.valueOf(data.mAuth_type), Integer.valueOf(data.mIn_black_List), Long.valueOf(data.mMimo_time), Long.valueOf(data.mSiso_time), Long.valueOf(data.mTotal_time), Long.valueOf(data.mLast_connect_time), Integer.valueOf(0)});
        }
    }

    private void inlineUpdateApInfo(HwABSApInfoData data) {
        if (this.mDatabase != null && this.mDatabase.isOpen() && data.mBssid != null) {
            HwABSUtils.logD("inlineUpdateApInfo ssid = " + data.mSsid);
            ContentValues values = new ContentValues();
            values.put(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_BSSID, data.mBssid);
            values.put(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_SSID, data.mSsid);
            values.put("switch_mimo_type", Integer.valueOf(data.mSwitch_mimo_type));
            values.put("switch_siso_type", Integer.valueOf(data.mSwitch_siso_type));
            values.put("auth_type", Integer.valueOf(data.mAuth_type));
            values.put("in_back_list", Integer.valueOf(data.mIn_black_List));
            values.put("mimo_time", Long.valueOf(data.mMimo_time));
            values.put("siso_time", Long.valueOf(data.mSiso_time));
            values.put("total_time", Long.valueOf(data.mTotal_time));
            values.put("last_connect_time", Long.valueOf(data.mLast_connect_time));
            this.mDatabase.update(HwABSDataBaseHelper.MIMO_AP_TABLE_NAME, values, "bssid like ?", new String[]{data.mBssid});
        }
    }
}
