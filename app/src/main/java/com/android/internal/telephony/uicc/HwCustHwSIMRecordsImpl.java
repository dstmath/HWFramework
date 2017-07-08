package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class HwCustHwSIMRecordsImpl extends HwCustHwSIMRecords {
    public static final String DATA_ROAMING_SIM2 = "data_roaming_sim2";
    private static final int EF_OCSGL = 20356;
    private static final int EVENT_GET_OCSGL_DONE = 1;
    private static final boolean HWDBG = true;
    private static String LAST_ICCID = null;
    private static final String LOG_TAG = "HwCustHwSIMRecordsImpl";
    private static boolean mIsSupportCsgSearch;
    private Handler custHandlerEx;
    boolean iccidChanged;
    private byte[] mEfOcsgl;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.<clinit>():void");
    }

    public HwCustHwSIMRecordsImpl(SIMRecords obj, Context mConText) {
        super(obj, mConText);
        this.iccidChanged = false;
        this.mEfOcsgl = null;
        this.custHandlerEx = new Handler() {
            public void handleMessage(android.os.Message r11) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x006e in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r10 = this;
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;
                r7 = r7.mSIMRecords;
                if (r7 == 0) goto L_0x0048;
            L_0x0006:
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;
                r7 = r7.mSIMRecords;
                r7 = r7.mDestroyed;
                r7 = r7.get();
                if (r7 == 0) goto L_0x0048;
            L_0x0012:
                r7 = "HwCustHwSIMRecordsImpl";
                r8 = new java.lang.StringBuilder;
                r8.<init>();
                r9 = "Received message ";
                r8 = r8.append(r9);
                r8 = r8.append(r11);
                r9 = "[";
                r8 = r8.append(r9);
                r9 = r11.what;
                r8 = r8.append(r9);
                r9 = "] ";
                r8 = r8.append(r9);
                r9 = " while being destroyed. Ignoring.";
                r8 = r8.append(r9);
                r8 = r8.toString();
                android.telephony.Rlog.e(r7, r8);
                return;
            L_0x0048:
                r7 = r11.what;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                switch(r7) {
                    case 1: goto L_0x006f;
                    default: goto L_0x004d;
                };	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x004d:
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8.<init>();	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r9 = "unknown Event: ";	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = r8.append(r9);	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r9 = r11.what;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = r8.append(r9);	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = r8.toString();	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7.log(r8);	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x0068:
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;
                r7 = r7.mSIMRecords;
                if (r7 == 0) goto L_0x006e;
            L_0x006e:
                return;
            L_0x006f:
                r0 = r11.obj;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r0 = (android.os.AsyncResult) r0;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7 = r0.exception;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                if (r7 != 0) goto L_0x007b;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x0077:
                r7 = r0.result;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                if (r7 != 0) goto L_0x00be;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x007b:
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8.<init>();	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r9 = "=csg= EVENT_GET_OCSGL_DONE exception = ";	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = r8.append(r9);	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r9 = r0.exception;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = r8.append(r9);	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = r8.toString();	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7.log(r8);	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = 0;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7.mEfOcsgl = r8;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                goto L_0x0068;
            L_0x009d:
                r2 = move-exception;
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8.<init>();	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r9 = "Exception parsing SIM record:";	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = r8.append(r9);	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = r8.append(r2);	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = r8.toString();	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7.log(r8);	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;
                r7 = r7.mSIMRecords;
                if (r7 == 0) goto L_0x006e;
            L_0x00bd:
                goto L_0x006e;
            L_0x00be:
                r1 = r0.result;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r1 = (java.util.ArrayList) r1;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r6 = 0;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = 0;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7.mEfOcsgl = r8;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r5 = r1.iterator();	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x00cd:
                r7 = r5.hasNext();	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                if (r7 == 0) goto L_0x00fb;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x00d3:
                r4 = r5.next();	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r4 = (byte[]) r4;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r3 = 0;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r3 = 0;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x00db:
                r7 = r4.length;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                if (r3 >= r7) goto L_0x00f8;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x00de:
                r7 = r4[r3];	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7 = r7 & 255;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = 255; // 0xff float:3.57E-43 double:1.26E-321;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                if (r7 == r8) goto L_0x0124;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x00e6:
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = r4.length;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = java.util.Arrays.copyOf(r4, r8);	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7.mEfOcsgl = r8;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = "=csg= SIMRecords:  OCSGL not empty.";	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7.log(r8);	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x00f8:
                r7 = r4.length;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                if (r3 >= r7) goto L_0x0127;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x00fb:
                r7 = r1.size();	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                if (r6 < r7) goto L_0x0111;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x0101:
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = 0;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = new byte[r8];	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7.mEfOcsgl = r8;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r8 = "=csg= SIMRecords:  OCSGL is empty. ";	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7.log(r8);	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
            L_0x0111:
                r7 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7 = r7.mSIMRecords;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7 = r7.mCsgRecordsLoadedRegistrants;	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                r7.notifyRegistrants();	 Catch:{ RuntimeException -> 0x009d, all -> 0x011c }
                goto L_0x0068;
            L_0x011c:
                r7 = move-exception;
                r8 = com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.this;
                r8 = r8.mSIMRecords;
                if (r8 == 0) goto L_0x0123;
            L_0x0123:
                throw r7;
            L_0x0124:
                r3 = r3 + 1;
                goto L_0x00db;
            L_0x0127:
                r6 = r6 + 1;
                goto L_0x00cd;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.1.handleMessage(android.os.Message):void");
            }
        };
    }

    public void setVmPriorityModeInClaro(VoiceMailConstants mVmConfig) {
        if (this.mContext != null && this.mSIMRecords != null) {
            int VoicemailPriorityMode = Systemex.getInt(this.mContext.getContentResolver(), "voicemailPrioritySpecial_" + this.mSIMRecords.getOperatorNumeric(), 0);
            log("The SIM card MCCMNC = " + this.mSIMRecords.getOperatorNumeric());
            if (VoicemailPriorityMode != 0 && mVmConfig != null) {
                mVmConfig.setVoicemailInClaro(VoicemailPriorityMode);
                log("VoicemailPriorityMode from custom = " + VoicemailPriorityMode);
            }
        }
    }

    public void refreshDataRoamingSettings() {
        String roamingAreaStr = Systemex.getString(this.mContext.getContentResolver(), "list_roaming_open_area");
        log("refreshDataRoamingSettings(): roamingAreaStr = " + roamingAreaStr);
        if (TextUtils.isEmpty(roamingAreaStr) || this.mSIMRecords == null) {
            log("refreshDataRoamingSettings(): roamingAreaStr is empty");
            return;
        }
        SharedPreferences sp = this.mContext.getSharedPreferences("DataRoamingSettingIccid", 0);
        String mIccid = this.mSIMRecords.getIccId();
        if (!TextUtils.isEmpty(mIccid)) {
            String oldIccid;
            String oldIccid2;
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                oldIccid = sp.getString(LAST_ICCID + this.mSIMRecords.getSlotId(), null);
            } else {
                oldIccid = sp.getString(LAST_ICCID, null);
            }
            if (oldIccid != null) {
                try {
                    oldIccid2 = new String(Base64.decode(oldIccid, 0), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    Rlog.d(LOG_TAG, "refreshDataRoamingSettings(): iccid not UnsupportedEncodingException");
                    oldIccid2 = oldIccid;
                }
            } else {
                oldIccid2 = oldIccid;
            }
            if (mIccid.equals(oldIccid2)) {
                this.iccidChanged = false;
            } else {
                this.iccidChanged = HWDBG;
            }
            if (this.iccidChanged) {
                try {
                    Editor editor = sp.edit();
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        editor.putString(LAST_ICCID + this.mSIMRecords.getSlotId(), new String(Base64.encode(mIccid.getBytes("utf-8"), 0), "utf-8"));
                    } else {
                        editor.putString(LAST_ICCID, new String(Base64.encode(mIccid.getBytes("utf-8"), 0), "utf-8"));
                    }
                    editor.commit();
                } catch (UnsupportedEncodingException e2) {
                    Rlog.d(LOG_TAG, "refreshDataRoamingSettings(): iccid not UnsupportedEncodingException");
                }
                String[] areaArray = roamingAreaStr.split(",");
                String operator = this.mSIMRecords.getOperatorNumeric();
                log("refreshDataRoamingSettings(): roamingAreaStr : " + roamingAreaStr + " operator : " + operator);
                int i;
                int length;
                String area;
                if (TelephonyManager.getDefault() != null && !TelephonyManager.getDefault().isMultiSimEnabled()) {
                    i = 0;
                    length = areaArray.length;
                    while (i < length) {
                        area = areaArray[i];
                        log("refreshDataRoamingSettings(): area : " + area);
                        if (!area.equals(operator)) {
                            Global.putInt(this.mContext.getContentResolver(), "data_roaming", 0);
                            i += EVENT_GET_OCSGL_DONE;
                        } else if (isSkipDataRoamingGid()) {
                            log("refreshDataRoamingSettings(): isSkipDataRoamingGid() returns true");
                        } else {
                            log("refreshDataRoamingSettings(): setting data roaming to true");
                            Systemex.putInt(this.mContext.getContentResolver(), "roaming_saving_on", EVENT_GET_OCSGL_DONE);
                            Global.putInt(this.mContext.getContentResolver(), "data_roaming", EVENT_GET_OCSGL_DONE);
                        }
                    }
                } else if (TelephonyManager.getDefault() != null && TelephonyManager.getDefault().isMultiSimEnabled()) {
                    log("######## MultiSimEnabled");
                    length = areaArray.length;
                    for (i = 0; i < length; i += EVENT_GET_OCSGL_DONE) {
                        area = areaArray[i];
                        log("refreshDataRoamingSettings(): else loop area : " + area);
                        if (area.equals(operator)) {
                            if (this.mSIMRecords.getSlotId() == 0) {
                                if (isSkipDataRoamingGid()) {
                                    log("refreshDataRoamingSettings(): isSkipDataRoamingGid() returns true for SIM1");
                                } else {
                                    Global.putInt(this.mContext.getContentResolver(), "data_roaming", EVENT_GET_OCSGL_DONE);
                                    log("refreshDataRoamingSettings(): setting data roaming to true else loop SIM1");
                                }
                            } else if (EVENT_GET_OCSGL_DONE != this.mSIMRecords.getSlotId()) {
                                log("doesn't contains the carrier" + operator + "for slotId" + this.mSIMRecords.getSlotId());
                            } else if (isSkipDataRoamingGid()) {
                                log("refreshDataRoamingSettings(): isSkipDataRoamingGid() returns true for SIM2");
                            } else {
                                Global.putInt(this.mContext.getContentResolver(), DATA_ROAMING_SIM2, EVENT_GET_OCSGL_DONE);
                            }
                        } else if (this.mSIMRecords.getSlotId() == 0) {
                            Global.putInt(this.mContext.getContentResolver(), "data_roaming", 0);
                        } else if (EVENT_GET_OCSGL_DONE == this.mSIMRecords.getSlotId()) {
                            Global.putInt(this.mContext.getContentResolver(), DATA_ROAMING_SIM2, 0);
                        }
                    }
                }
                return;
            }
            Rlog.d(LOG_TAG, "refreshDataRoamingSettings(): iccid not changed" + this.iccidChanged);
        }
    }

    private boolean isSkipDataRoamingGid() {
        String skipDataRoamingGid = Systemex.getString(this.mContext.getContentResolver(), "hw_skip_data_roaming_gid");
        Object gid1 = this.mSIMRecords != null ? this.mSIMRecords.getGID1() : null;
        log("isSkipDataRoamingGid(): skipDataRoamingGid : " + skipDataRoamingGid + " simGidbytes : " + gid1);
        boolean matched = false;
        if (TextUtils.isEmpty(skipDataRoamingGid) || gid1 == null || gid1.length <= 0) {
            return false;
        }
        String[] gidArray = skipDataRoamingGid.split(",");
        String simGid = IccUtils.bytesToHexString(gid1);
        if (simGid == null || simGid.length() < 2) {
            return false;
        }
        log("isSkipDataRoamingGid(): simGid : " + simGid);
        int length = gidArray.length;
        for (int i = 0; i < length; i += EVENT_GET_OCSGL_DONE) {
            String gid = gidArray[i];
            log("isSkipDataRoamingGid(): cust gid : " + gid);
            if (simGid.substring(0, 2).equals(gid)) {
                matched = HWDBG;
                break;
            }
        }
        log("isSkipDataRoamingGid() returning : " + matched);
        return matched;
    }

    public void refreshMobileDataAlwaysOnSettings() {
        String dataAlwaysOnAreaStr = System.getString(this.mContext.getContentResolver(), "list_mobile_data_always_on");
        log("refreshMobileDataAlwaysOnSettings(): dataAlwaysOnAreaStr = " + dataAlwaysOnAreaStr);
        if (TextUtils.isEmpty(dataAlwaysOnAreaStr) || this.mSIMRecords == null) {
            log("refreshMobileDataAlwaysOnSettings(): dataAlwaysOnAreaStr is empty");
        } else if (System.getInt(this.mContext.getContentResolver(), "whether_data_alwayson_init", 0) == EVENT_GET_OCSGL_DONE) {
            log("refreshMobileDataAlwaysOnSettings(): whether_data_alwayson_init is 1");
        } else {
            String[] areaArray = dataAlwaysOnAreaStr.split(",");
            String operator = this.mSIMRecords.getOperatorNumeric();
            int length = areaArray.length;
            for (int i = 0; i < length; i += EVENT_GET_OCSGL_DONE) {
                if (areaArray[i].equals(operator)) {
                    System.putInt(this.mContext.getContentResolver(), "power_saving_on", 0);
                    System.putInt(this.mContext.getContentResolver(), "whether_data_alwayson_init", EVENT_GET_OCSGL_DONE);
                    break;
                }
            }
        }
    }

    private void log(String message) {
        Rlog.d(LOG_TAG, message);
    }

    public void custLoadCardSpecialFile(int fileid) {
        switch (fileid) {
            case EF_OCSGL /*20356*/:
                if (mIsSupportCsgSearch) {
                    log("=csg= fetchSimRecords => CSG ... ");
                    if (this.mSIMRecords != null) {
                        this.mSIMRecords.mFh.loadEFLinearFixedAll(EF_OCSGL, this.custHandlerEx.obtainMessage(EVENT_GET_OCSGL_DONE));
                    } else {
                        log("IccRecords is null !!! ");
                    }
                }
            default:
                Rlog.d(LOG_TAG, "no fileid found for load");
        }
    }

    public byte[] getOcsgl() {
        if (this.mEfOcsgl == null || this.mSIMRecords == null) {
            if (this.mSIMRecords != null) {
                this.mSIMRecords.setCsglexist(false);
            }
            return new byte[0];
        }
        this.mSIMRecords.setCsglexist(HWDBG);
        if (this.mEfOcsgl.length > 0) {
            return Arrays.copyOf(this.mEfOcsgl, this.mEfOcsgl.length);
        }
        return new byte[0];
    }
}
