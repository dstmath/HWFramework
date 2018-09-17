package com.android.internal.telephony;

import android.content.ContentValues;
import android.os.RemoteException;
import android.telephony.Rlog;

public class HwUiccPhoneBookController extends UiccPhoneBookController {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "HwUiccPhoneBookController";
    private static UiccPhoneBookControllerUtils utils;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwUiccPhoneBookController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwUiccPhoneBookController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwUiccPhoneBookController.<clinit>():void");
    }

    public HwUiccPhoneBookController(Phone[] phone) {
        super(phone);
    }

    public boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int efid, ContentValues values, String pin2) throws RemoteException {
        return updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(utils.getDefaultSubscription(this), efid, values, pin2);
    }

    public boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(int subId, int efid, ContentValues values, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = utils.getIccPhoneBookInterfaceManager(this, subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsWithContentValuesInEfBySearchHW(efid, values, pin2);
        }
        loge("updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW iccPbkIntMgr is null for Subscription:" + subId);
        return DBG;
    }

    public boolean updateUsimAdnRecordsInEfByIndexHW(int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) throws RemoteException {
        return updateUsimAdnRecordsInEfByIndexUsingSubIdHW(utils.getDefaultSubscription(this), efid, newTag, newPhoneNumber, newEmails, newAnrNumbers, sEf_id, index, pin2);
    }

    public boolean updateUsimAdnRecordsInEfByIndexUsingSubIdHW(int subId, int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = utils.getIccPhoneBookInterfaceManager(this, subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateUsimAdnRecordsInEfByIndexHW(efid, newTag, newPhoneNumber, newEmails, newAnrNumbers, sEf_id, index, pin2);
        }
        loge("updateUsimAdnRecordsInEfByIndexUsingSubIdHW iccPbkIntMgr is null for Subscription:" + subId);
        return DBG;
    }

    public int getAdnCountHW() throws RemoteException {
        return getAdnCountUsingSubIdHW(utils.getDefaultSubscription(this));
    }

    public int getAdnCountUsingSubIdHW(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = utils.getIccPhoneBookInterfaceManager(this, subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAdnCountHW();
        }
        loge("getAdnCountHW iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    public int getAnrCountHW() throws RemoteException {
        return getAnrCountUsingSubIdHW(utils.getDefaultSubscription(this));
    }

    public int getAnrCountUsingSubIdHW(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = utils.getIccPhoneBookInterfaceManager(this, subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAnrCountHW();
        }
        loge("getAnrCountHW iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    public int getEmailCountHW() throws RemoteException {
        return getEmailCountUsingSubIdHW(utils.getDefaultSubscription(this));
    }

    public int getEmailCountUsingSubIdHW(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = utils.getIccPhoneBookInterfaceManager(this, subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getEmailCountHW();
        }
        loge("getEmailCountHW iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    public int getSpareAnrCountHW() throws RemoteException {
        return getSpareAnrCountUsingSubIdHW(utils.getDefaultSubscription(this));
    }

    public int getSpareAnrCountUsingSubIdHW(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = utils.getIccPhoneBookInterfaceManager(this, subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getSpareAnrCountHW();
        }
        loge("getSpareAnrCountHW iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    public int getSpareEmailCountHW() throws RemoteException {
        return getSpareEmailCountUsingSubIdHW(utils.getDefaultSubscription(this));
    }

    public int getSpareEmailCountUsingSubIdHW(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = utils.getIccPhoneBookInterfaceManager(this, subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getSpareEmailCountHW();
        }
        loge("getSpareEmailCountHW iccPbkIntMgr isnull for Subscription:" + subId);
        return 0;
    }

    public int[] getRecordsSizeHW() throws RemoteException {
        return getRecordsSizeUsingSubIdHW(utils.getDefaultSubscription(this));
    }

    public int[] getRecordsSizeUsingSubIdHW(int subId) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = utils.getIccPhoneBookInterfaceManager(this, subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getRecordsSizeHW();
        }
        loge("getRecordsSizeHW iccPbkIntMgr isnull for Subscription:" + subId);
        return new int[9];
    }

    public int getSpareExt1CountUsingSubIdHW(int subId) {
        IccPhoneBookInterfaceManager iccPbkIntMgr = utils.getIccPhoneBookInterfaceManager(this, subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getSpareExt1CountHW();
        }
        loge("getRecordsSizeHW iccPbkIntMgr isnull for Subscription:" + subId);
        return -1;
    }

    private void loge(String msg) {
        Rlog.i(LOG_TAG, msg);
    }
}
