package com.android.internal.telephony.uicc;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.android.app.ActivityManagerNativeEx;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.RegistrantEx;
import com.huawei.android.os.RegistrantListEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionInfoEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.OperatorInfoEx;
import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.internal.telephony.uicc.IccFileHandlerEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.internal.telephony.uicc.UsimServiceTableEx;
import java.util.ArrayList;

public class HwIccRecordsEx extends Handler implements IHwIccRecordsEx, IccConstantsEx {
    private static final String ACTION_SIM_ICCID_READY = "android.intent.action.ACTION_SIM_ICCID_READY";
    protected static final boolean DBG = true;
    private static final String EXTRA_SIM_ICCID = "iccid";
    public static final int HW_CARRIER_FILE_C_IMSI = 4;
    public static final int HW_CARRIER_FILE_G_IMSI = 2;
    public static final int HW_CARRIER_FILE_G_MCCMNC = 3;
    public static final int HW_CARRIER_FILE_ICCID = 1;
    protected static final boolean IS_HW_CHINA_TELECOM;
    protected static final boolean IS_HW_SIM_REFRESH = SystemPropertiesEx.getBoolean("ro.config.hwft_simrefresh", false);
    public static final int KT_SKT_CARD = 1;
    public static final int LGU_CARD = 0;
    public static final int LGU_PSEUDO_CARD = 3;
    public static final int MIN_IMSI_LENGTH = 3;
    public static final int OTHERS_CARD = 2;
    private static final String TAG = "HwIccRecordsEx";
    protected static final int UNINITIALIZED = -1;
    protected static final int UNKNOWN = 0;
    protected CommandsInterfaceEx mCi;
    protected Context mContext;
    protected RegistrantListEx mCsgRecordsLoadedRegistrants = new RegistrantListEx();
    protected IccFileHandlerEx mFh;
    protected RegistrantListEx mIccIDLoadRegistrants = new RegistrantListEx();
    protected IIccRecordsInner mIccRecordsInner;
    protected RegistrantListEx mIccRefreshRegistrants = new RegistrantListEx();
    protected boolean mIs3Gphonebook = false;
    protected boolean mIsCsglexist = false;
    protected boolean mIsGetPBRDone = false;
    protected boolean mIsImsiLoad = false;
    protected UiccCardApplicationEx mParentApp;

    static {
        boolean z = false;
        if (SystemPropertiesEx.get("ro.config.hw_opta", "0").equals("92") && SystemPropertiesEx.get("ro.config.hw_optb", "0").equals("156")) {
            z = true;
        }
        IS_HW_CHINA_TELECOM = z;
    }

    public HwIccRecordsEx(IIccRecordsInner iccRecordsInner, UiccCardApplicationEx app, Context context, CommandsInterfaceEx ci) {
        this.mIccRecordsInner = iccRecordsInner;
        this.mContext = context;
        this.mCi = ci;
        this.mFh = app.getIccFileHandler();
        this.mParentApp = app;
    }

    public void registerForCsgRecordsLoaded(Handler h, int what, Object obj) {
        this.mCsgRecordsLoadedRegistrants.add(new RegistrantEx(h, what, obj));
    }

    public void unregisterForCsgRecordsLoaded(Handler h) {
        this.mCsgRecordsLoadedRegistrants.remove(h);
    }

    public void notifyRegisterForCsgRecordsLoaded() {
        this.mCsgRecordsLoadedRegistrants.notifyRegistrants();
    }

    public synchronized void registerForIccRefresh(Handler h, int what, Object obj) {
        this.mIccRefreshRegistrants.add(new RegistrantEx(h, what, obj));
    }

    public synchronized void unRegisterForIccRefresh(Handler h) {
        this.mIccRefreshRegistrants.remove(h);
    }

    public synchronized void notifyRegisterForIccRefresh() {
        this.mIccRefreshRegistrants.notifyRegistrants();
    }

    public boolean getIccidSwitch() {
        return SystemPropertiesEx.getBoolean("ro.config.readiccid_switch", false);
    }

    public void sendIccidDoneBroadcast(String iccId) {
        Intent intent = new Intent(ACTION_SIM_ICCID_READY);
        intent.putExtra(EXTRA_SIM_ICCID, iccId);
        RlogEx.i(TAG, " SimRecords sendIccidDoneBroadcast EXTRA_SIM_ICCID=" + SubscriptionInfoEx.givePrintableIccid(iccId));
        ActivityManagerNativeEx.broadcastStickyIntent(intent, (String) null, 0);
    }

    public String getOperatorNumericEx(ContentResolver cr, String name) {
        return null;
    }

    public boolean has3Gphonebook() {
        return this.mIs3Gphonebook;
    }

    public boolean isGetPBRDone() {
        return this.mIsGetPBRDone;
    }

    public boolean checkFileInServiceTable(int efid, UsimServiceTableEx usimServiceTable, byte[] data) {
        return true;
    }

    public ArrayList<OperatorInfoEx> getEonsForAvailableNetworks(ArrayList<OperatorInfoEx> arrayList) {
        return null;
    }

    public synchronized void registerForLoadIccID(Handler h, int what, Object obj) {
        this.mIccIDLoadRegistrants.add(new RegistrantEx(h, what, obj));
        String iccId = this.mIccRecordsInner.getIccIdHw();
        if (!TextUtils.isEmpty(iccId)) {
            RlogEx.e(TAG, "mIccId exist before registerForLoadIccID. mIccId = " + SubscriptionInfoEx.givePrintableIccid(iccId));
            Message message = Message.obtain(h, what, obj);
            AsyncResultEx.forMessage(message, iccId, (Throwable) null);
            message.sendToTarget();
        }
    }

    public synchronized void unRegisterForLoadIccID(Handler h) {
        this.mIccIDLoadRegistrants.remove(h);
    }

    public void notifyRegisterLoadIccID(Object userObj, Object result, Throwable exception) {
        this.mIccIDLoadRegistrants.notifyRegistrants(userObj, result, exception);
    }

    public String[] getEhplmnOfSim() {
        return new String[0];
    }

    public boolean getImsiReady() {
        return this.mIsImsiLoad;
    }

    public void setImsiReady(boolean isReady) {
        this.mIsImsiLoad = isReady;
    }

    public boolean getCsglexist() {
        return this.mIsCsglexist;
    }

    public void setCsglexist(boolean isCglExist) {
        this.mIsCsglexist = isCglExist;
    }

    /* access modifiers changed from: package-private */
    public int getSlotId() {
        return this.mIccRecordsInner.getSlotId();
    }
}
