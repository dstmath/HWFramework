package com.huawei.internal.telephony.uicc;

import android.content.ContentResolver;
import android.os.Handler;
import com.android.internal.telephony.uicc.IAdnRecordCacheInner;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.huawei.internal.telephony.OperatorInfoEx;
import java.util.ArrayList;

public class IccRecordsEx {
    public static final int EVENT_EONS = 100;
    private IccRecords mIccRecords;

    public static boolean getAdnLongNumberSupport() {
        return IccRecords.getAdnLongNumberSupport();
    }

    public static boolean getEmailAnrSupport() {
        return IccRecords.getEmailAnrSupport();
    }

    public static int getEventSpn() {
        return 2;
    }

    public static String getPropertyMccMatchingFyrom() {
        return IccRecords.PROPERTY_MCC_MATCHING_FYROM;
    }

    public IccRecords getIccRecords() {
        return this.mIccRecords;
    }

    public void setIccRecords(IccRecords iccRecords) {
        this.mIccRecords = iccRecords;
    }

    public String getOperatorNumeric() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords == null) {
            return null;
        }
        return iccRecords.getOperatorNumeric();
    }

    public boolean isLoaded() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords == null) {
            return false;
        }
        return iccRecords.isLoaded();
    }

    public boolean isEonsDisabled() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords == null) {
            return false;
        }
        return iccRecords.isEonsDisabled();
    }

    public String getEons() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords == null) {
            return null;
        }
        return iccRecords.getEons();
    }

    public boolean updateEons(String regOperator, int lac) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords == null) {
            return false;
        }
        return iccRecords.updateEons(regOperator, lac);
    }

    public String getOperatorNumericEx(ContentResolver cr, String name) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords == null) {
            return null;
        }
        return iccRecords.getOperatorNumericEx(cr, name);
    }

    public String getIccId() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords == null) {
            return null;
        }
        return iccRecords.getIccId();
    }

    public void registerForRecordsEvents(Handler h, int what, Object obj) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.registerForRecordsEvents(h, what, obj);
        }
    }

    public void unregisterForRecordsEvents(Handler h) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.unregisterForRecordsEvents(h);
        }
    }

    public String[] getEhplmnOfSim() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords == null) {
            return new String[0];
        }
        return iccRecords.getEhplmnOfSim();
    }

    public void registerForIccRefresh(Handler h, int what, Object obj) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.registerForIccRefresh(h, what, obj);
        }
    }

    public void unRegisterForIccRefresh(Handler h) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.unRegisterForIccRefresh(h);
        }
    }

    public void registerForImsiReady(Handler h, int what, Object obj) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.registerForImsiReady(h, what, obj);
        }
    }

    public void unregisterForImsiReady(Handler handler) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.unregisterForImsiReady(handler);
        }
    }

    public void registerForRecordsLoaded(Handler h, int what, Object obj) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.registerForRecordsLoaded(h, what, obj);
        }
    }

    public void unregisterForRecordsLoaded(Handler handler) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.unregisterForRecordsLoaded(handler);
        }
    }

    public void registerForFdnRecordsLoaded(Handler h, int what, Object obj) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.registerForFdnRecordsLoaded(h, what, obj);
        }
    }

    public void unregisterForFdnRecordsLoaded(Handler handler) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.unregisterForFdnRecordsLoaded(handler);
        }
    }

    public String getIMSI() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            return iccRecords.getIMSI();
        }
        return null;
    }

    public String getServiceProviderName() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            return iccRecords.getServiceProviderName();
        }
        return null;
    }

    public String getGid1() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            return iccRecords.getGid1();
        }
        return null;
    }

    public byte[] getGID1() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            return iccRecords.getGID1Hw();
        }
        return new byte[0];
    }

    public String getGid2() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            return iccRecords.getGid2();
        }
        return null;
    }

    public String getIccIdHw() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            return iccRecords.getIccIdHw();
        }
        return null;
    }

    public String getImsiHw() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            return iccRecords.getImsiHw();
        }
        return null;
    }

    public boolean isInstanceOfRuim() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            return iccRecords instanceof RuimRecords;
        }
        return false;
    }

    public void registerForLoadIccID(Handler h, int what, Object obj) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.registerForLoadIccID(h, what, obj);
        }
    }

    public void unRegisterForLoadIccID(Handler h) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.unRegisterForLoadIccID(h);
        }
    }

    public ArrayList<OperatorInfoEx> getEonsForAvailableNetworks(ArrayList<OperatorInfoEx> avlNetworks) {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            return iccRecords.getEonsForAvailableNetworks(avlNetworks);
        }
        return new ArrayList<>(0);
    }

    public String getCdmaGsmImsi() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            return iccRecords.getCdmaGsmImsi();
        }
        return null;
    }

    public IAdnRecordCacheInner getAdnCache() {
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            return iccRecords.getAdnCache();
        }
        return null;
    }
}
