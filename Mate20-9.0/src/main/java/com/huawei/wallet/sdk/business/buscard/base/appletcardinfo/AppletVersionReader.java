package com.huawei.wallet.sdk.business.buscard.base.appletcardinfo;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.utils.StringUtil;

public class AppletVersionReader {
    public static final String APPLET_VERSION_NO_READABLE = "4.241.08";
    public static final String SHIELD_RAPDU = "6D00";
    private ConfigData configData;
    private IAPDUService omaService;

    public AppletVersionReader(IAPDUService omaService2, ConfigData configData2) {
        this.omaService = omaService2;
        this.configData = configData2;
    }

    public String readAppletVersion(String aid) throws AppletCardException {
        String appletVersion;
        LogX.i("readAppletVersion  begin for aid : " + aid);
        if (!StringUtil.isEmpty(aid, true)) {
            TaskResult<ChannelID> result = this.omaService.excuteApduList(this.configData.getLocalApudList(aid, 8), new ChannelID());
            this.omaService.closeChannel(result.getData());
            if (result.getResultCode() == 0) {
                String rapdu = result.getLastExcutedCommand().getRapdu();
                LogX.i("AppletInfoReader readAppletVersion rapdu :" + rapdu);
                String str1 = rapdu.substring(6, 7);
                String str2 = rapdu.substring(7, 10);
                String str3 = rapdu.substring(14, 16);
                LogX.i("AppletInfoReader readAppletVersion appletVersion :" + appletVersion);
                LogX.i("readAppletVersion end. for aid : " + aid);
                return appletVersion;
            }
            String msg = "readAppletVersion failed, apdu excute failed. resultCode=" + 6 + ".result.getPrintMsg()=" + result.getPrintMsg();
            if (SHIELD_RAPDU.equals(result.getLastExcutedCommand().getRapdu())) {
                LogX.w("the applet is old appletversion. less than 4.241.08.rapdu:" + rapdu);
            } else {
                LogX.e(msg);
            }
            throw throwException(6, msg);
        }
        LogX.i("readAppletVersion failed, instanceId is null.");
        throw throwException(1, "readAppletVersion failed, instanceId is null.");
    }

    private AppletCardException throwException(int code, String msg) {
        return new AppletCardException(code, msg);
    }
}
