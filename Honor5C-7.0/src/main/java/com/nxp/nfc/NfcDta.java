package com.nxp.nfc;

import android.os.RemoteException;

public final class NfcDta {
    private static final String TAG = "NfcDta";
    private INfcDta mService;

    public NfcDta(INfcDta mDtaService) {
        this.mService = mDtaService;
    }

    public boolean snepDtaCmd(String cmdType, String serviceName, int serviceSap, int miu, int rwSize, int testCaseId) {
        try {
            return this.mService.snepDtaCmd(cmdType, serviceName, serviceSap, miu, rwSize, testCaseId);
        } catch (RemoteException e) {
            return false;
        }
    }
}
