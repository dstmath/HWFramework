package com.huawei.internal.telephony.vsim;

import android.content.Context;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class ExternalSimManagerEx {
    public static final int INVALID_ID = -1;
    private static ExternalSimManagerEx sInstance = null;
    private ExternalSimManager mExternalSimManager;

    private ExternalSimManagerEx() {
    }

    private void setExternalSimManager(ExternalSimManager manager) {
        this.mExternalSimManager = manager;
    }

    public static ExternalSimManagerEx make(Context context, CommandsInterfaceEx[] ciExs) {
        ExternalSimManagerEx externalSimManagerEx = sInstance;
        if (externalSimManagerEx != null) {
            return externalSimManagerEx;
        }
        if (context == null || ciExs == null) {
            return null;
        }
        CommandsInterface[] cis = new CommandsInterface[ciExs.length];
        for (int i = 0; i < ciExs.length; i++) {
            cis[i] = ciExs[i].getCommandsInterface();
        }
        ExternalSimManager manager = ExternalSimManager.make(context, cis);
        sInstance = new ExternalSimManagerEx();
        sInstance.setExternalSimManager(manager);
        return sInstance;
    }

    public ExternalSimManagerEx getInstance() {
        return sInstance;
    }

    public int sendVsimEvent(int slotId, int messageId, int dataLength, byte[] data, Message response) {
        ExternalSimManager externalSimManager = this.mExternalSimManager;
        if (externalSimManager != null) {
            return externalSimManager.sendVsimEvent(slotId, messageId, dataLength, data, response);
        }
        return -1;
    }

    public int sendVsimEvent(int slotId, int simType, int eventId, String challenge, Message response) {
        ExternalSimManager externalSimManager = this.mExternalSimManager;
        if (externalSimManager != null) {
            return externalSimManager.sendVsimEvent(slotId, simType, eventId, challenge, response);
        }
        return -1;
    }

    public void handleMessageDone(int transactionId) {
        ExternalSimManager externalSimManager = this.mExternalSimManager;
        if (externalSimManager != null) {
            externalSimManager.handleMessageDone(transactionId);
        }
    }
}
