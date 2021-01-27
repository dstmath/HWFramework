package com.android.internal.telephony.cat;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.SystemProperties;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.cat.HwCustCatService;
import com.android.internal.telephony.uicc.UiccController;

public class HwCustCatServiceImpl extends HwCustCatService {
    private static final boolean CUST_ENABLE_OTA_BIP = SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_cust", (boolean) CUST_ENABLE_OTA_BIP);
    private static final boolean DBG = true;
    private static final String ENV_CHANGE_NUMBER = "D1340202838106069833111111110B26E40A9833111111117F160C01091557323614027000000F0D00012020B0000600000000000005";
    private static final String ENV_OPEN_CARD = "D1340202838106069833111111110B26E40A9833111111117F160C01091557323614027000000F0D00012020B0000600000000000002";
    private static final boolean SIM_FILE_REFRESH = SystemProperties.getBoolean("ro.config.hw_sim_file_refresh", (boolean) CUST_ENABLE_OTA_BIP);
    private static final boolean SUPPORT_DCM_ESIM = SystemProperties.getBoolean("ro.config.hw_support_cust_esim", (boolean) CUST_ENABLE_OTA_BIP);
    private static final boolean SUPPORT_DCM_UIM_LOCK = SystemProperties.getBoolean("ro.config.hw_support_dcm_ulock", (boolean) CUST_ENABLE_OTA_BIP);

    public HwCustCatServiceImpl(CatService obj, Context mConText) {
        super(obj, mConText);
    }

    public void sendBroadcastToOtaUI(int OtaType, boolean processResult) {
        Intent intent;
        if (CUST_ENABLE_OTA_BIP) {
            if (OtaType != 0) {
                if (OtaType == 1) {
                    if (processResult) {
                        Intent intent2 = new Intent("android.intent.action.change_number_result");
                        intent2.putExtra("result_code", 0);
                        intent = intent2;
                    } else {
                        intent = new Intent("android.intent.action.change_number_result");
                        intent.putExtra("result_code", 2);
                    }
                } else {
                    return;
                }
            } else if (processResult) {
                Intent intent3 = new Intent("android.intent.action.open_service_result");
                intent3.putExtra("result_code", 0);
                intent = intent3;
            } else {
                intent = new Intent("android.intent.action.open_service_result");
                intent.putExtra("result_code", 2);
            }
            this.mCatService.OTA_TYPE = 255;
            this.mContext.sendBroadcast(intent);
        }
    }

    public void handleOpenServiceCommand(CommandsInterface ci) {
        if (CUST_ENABLE_OTA_BIP) {
            CatLog.d(this, "sendOpenService Cmd, cmd = D1340202838106069833111111110B26E40A9833111111117F160C01091557323614027000000F0D00012020B0000600000000000002");
            ci.sendEnvelope(ENV_OPEN_CARD, (Message) null);
        }
    }

    public void handleChangeImsiCommand(CommandsInterface ci) {
        if (CUST_ENABLE_OTA_BIP) {
            CatLog.d(this, "sendChangeImsi Cmd, cmd = D1340202838106069833111111110B26E40A9833111111117F160C01091557323614027000000F0D00012020B0000600000000000005");
            ci.sendEnvelope(ENV_CHANGE_NUMBER, (Message) null);
        }
    }

    public void onOtaCommand(int otaType) {
        CatLog.d(this, "hwcustCatService impl ota type = " + otaType);
        if (CUST_ENABLE_OTA_BIP) {
            HwCustCatService.OtaCmdMessage mOtaCmdMessage = new HwCustCatService.OtaCmdMessage(otaType);
            CatLog.d(this, "hwcustCatService impl ota type2 = " + otaType);
            this.mCatService.obtainMessage(11, mOtaCmdMessage).sendToTarget();
        }
    }

    public void sendFailToUIWhenNoOpenChannelReceived(int otaType) {
        if (CUST_ENABLE_OTA_BIP) {
            HwCustCatService.OtaCmdMessage mOtaCmdMessage = new HwCustCatService.OtaCmdMessage(otaType);
            CatLog.d(this, "send msg to catservice when not recieved, Delayed delivery time  =  25000");
            this.mCatService.sendMessageDelayed(this.mCatService.obtainMessage(12, mOtaCmdMessage), 25000);
        }
    }

    public void handleOtaCommand(HwCustCatService.OtaCmdMessage resMsg, CommandsInterface ci) {
        if (CUST_ENABLE_OTA_BIP) {
            if (resMsg == null) {
                CatLog.d(this, "ota cmd is null ");
                return;
            }
            CatLog.d(this, "otatype is " + resMsg.otaType);
            this.mCatService.otaCmdType = resMsg.otaType;
            int i = resMsg.otaType;
            if (i == 0) {
                this.mCatService.OTA_TYPE = 0;
                handleOpenServiceCommand(ci);
            } else if (i == 1) {
                this.mCatService.OTA_TYPE = 1;
                handleChangeImsiCommand(ci);
            } else {
                return;
            }
            sendFailToUIWhenNoOpenChannelReceived(this.mCatService.otaCmdType);
        }
    }

    public boolean supportSimFileRefresh() {
        return SIM_FILE_REFRESH;
    }

    public boolean supportDocomoEsim() {
        return SUPPORT_DCM_ESIM;
    }

    public boolean handleRefreshNotification(UiccController uiccController, CommandParams cmdParams, int slotId) {
        CatLog.d(this, "handleRefreshNotification slotId = " + slotId);
        if (!(cmdParams instanceof FileChangeNotificationParams) || uiccController == null) {
            return CUST_ENABLE_OTA_BIP;
        }
        uiccController.onRefresh(slotId, ((FileChangeNotificationParams) cmdParams).fileList);
        return DBG;
    }

    public void broadcastFileChangeNotification(int slotId) {
        CatLog.d(this, "broadcast File Change Notification slotId = " + slotId);
        Intent intent = new Intent("com.nttdocomo.android.lpa.intent.action.stk.refresh_file_change_notify");
        intent.setFlags(16777216);
        intent.putExtra("SLOT_ID", slotId);
        this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_STK_COMMANDS");
    }

    public boolean supportDocomoUimLock() {
        return SUPPORT_DCM_UIM_LOCK;
    }

    public void broadcastUimLockNotification(int slotId) {
        CatLog.d(this, "broadcast Uim Lock Notification slotId = " + slotId);
        Intent intent = new Intent("com.huawei.android.intent.action.EF_LOCK_UPDATED_INTERNAL");
        intent.setClassName("jp.co.fsi.felicalock", "jp.co.fsi.felicalock.FelicaLockReceiver");
        intent.putExtra("SLOT_ID", slotId);
        this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_STK_COMMANDS");
    }
}
