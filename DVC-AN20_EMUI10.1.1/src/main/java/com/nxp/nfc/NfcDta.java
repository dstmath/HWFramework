package com.nxp.nfc;

import android.os.RemoteException;

public final class NfcDta {
    private static final String TAG = "NfcDta";
    private static INfcDta sService;

    public NfcDta(INfcDta mDtaService) {
        sService = mDtaService;
    }

    public boolean snepDtaCmd(String cmdType, String serviceName, int serviceSap, int miu, int rwSize, int testCaseId) {
        try {
            return sService.snepDtaCmd(cmdType, serviceName, serviceSap, miu, rwSize, testCaseId);
        } catch (RemoteException e) {
            return false;
        }
    }
}
