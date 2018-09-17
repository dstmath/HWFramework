package com.huawei.android.telephony;

import android.content.Intent;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.MSimTelephonyConstants;
import android.util.Log;
import com.android.internal.telephony.IIccPhoneBook;
import com.android.internal.telephony.IIccPhoneBook.Stub;

public final class IIccPhoneBookManagerEx {
    public static final String SLOT_ID = "subscription";
    private static final String TAG = "IIccPhoneBookManagerEx";
    private static IIccPhoneBookManagerEx sInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.telephony.IIccPhoneBookManagerEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.telephony.IIccPhoneBookManagerEx.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.telephony.IIccPhoneBookManagerEx.<clinit>():void");
    }

    private IIccPhoneBookManagerEx() {
    }

    public static IIccPhoneBookManagerEx getDefault() {
        return sInstance;
    }

    public int[] getRecordsSize(int subscription) throws RemoteException {
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService(MSimTelephonyConstants.SIMPHONEBOOK_SERVICE_NAME2));
            if (iccIpb != null) {
                return iccIpb.getRecordsSizeUsingSubIdHW(subscription);
            }
        } catch (SecurityException ex) {
            Log.d(TAG, ex.toString());
        }
        return new int[0];
    }

    public int getSpareExt1Count(int subscription) throws RemoteException {
        int spareExt1count = -1;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService(MSimTelephonyConstants.SIMPHONEBOOK_SERVICE_NAME2));
            if (iccIpb != null) {
                spareExt1count = iccIpb.getSpareExt1CountUsingSubIdHW(subscription);
            }
        } catch (SecurityException ex) {
            Log.d(TAG, ex.toString());
        }
        return spareExt1count;
    }

    public int[] getAdnRecordsSizeOnSubscription(int efid, int subscription) throws RemoteException {
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService(MSimTelephonyConstants.SIMPHONEBOOK_SERVICE_NAME2));
            if (iccIpb != null) {
                return iccIpb.getAdnRecordsSizeForSubscriber(subscription, efid);
            }
        } catch (SecurityException ex) {
            Log.d(TAG, ex.toString());
        }
        return new int[0];
    }

    public int getSoltIdInSimStateChangeIntent(Intent intent) {
        if (intent != null) {
            return intent.getIntExtra(SLOT_ID, -1);
        }
        return -1;
    }

    public int[] getRecordsSize() throws RemoteException {
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService(MSimTelephonyConstants.SIMPHONEBOOK_SERVICE_NAME2));
            if (iccIpb != null) {
                return iccIpb.getRecordsSizeHW();
            }
        } catch (SecurityException ex) {
            Log.d(TAG, ex.toString());
        }
        return new int[0];
    }

    public int[] getAdnRecordsSize(int efid) throws RemoteException {
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService(MSimTelephonyConstants.SIMPHONEBOOK_SERVICE_NAME2));
            if (iccIpb != null) {
                return iccIpb.getAdnRecordsSize(efid);
            }
        } catch (SecurityException ex) {
            Log.d(TAG, ex.toString());
        }
        return new int[0];
    }

    public int getAlphaTagEncodingLength(String alphaTag) throws RemoteException {
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService(MSimTelephonyConstants.SIMPHONEBOOK_SERVICE_NAME2));
            if (iccIpb != null) {
                return iccIpb.getAlphaTagEncodingLength(alphaTag);
            }
        } catch (SecurityException ex) {
            Log.d(TAG, ex.toString());
        }
        return -1;
    }

    public String getSecretCodeSubString(String input) {
        return IIccPhoneBookManagerChipsEx.getDefault().getSecretCodeSubString(input);
    }
}
