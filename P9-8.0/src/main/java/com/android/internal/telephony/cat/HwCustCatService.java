package com.android.internal.telephony.cat;

import android.content.Context;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.UiccController;

public class HwCustCatService {
    CatService mCatService;
    Context mContext;

    public static class OtaCmdMessage {
        int otaType;

        public OtaCmdMessage(int OtaType) {
            this.otaType = OtaType;
        }
    }

    public HwCustCatService(CatService obj, Context mConText) {
        this.mCatService = obj;
        this.mContext = mConText;
    }

    public void sendBroadcastToOtaUI(int OtaType, boolean processResult) {
    }

    public void handleOpenServiceCommand(CommandsInterface ci) {
    }

    public void handleChangeImsiCommand(CommandsInterface ci) {
    }

    public void onOtaCommand(int otaType) {
    }

    public void sendFailToUIWhenNoOpenChannelReceived(int otaType) {
    }

    public void handleOtaCommand(OtaCmdMessage resMsg, CommandsInterface ci) {
    }

    public boolean supportSimFileRefresh() {
        return false;
    }

    public boolean supportDocomoEsim() {
        return false;
    }

    public boolean handleRefreshNotification(UiccController uiccController, CommandParams cmdParams, int slotId) {
        return false;
    }

    public void broadcastFileChangeNotification(int slotId) {
    }
}
