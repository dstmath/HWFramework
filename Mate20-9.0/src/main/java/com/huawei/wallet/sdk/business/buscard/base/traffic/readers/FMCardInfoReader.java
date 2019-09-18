package com.huawei.wallet.sdk.business.buscard.base.traffic.readers;

import com.huawei.wallet.sdk.business.buscard.base.model.ApduCommandInfo;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import java.util.ArrayList;
import java.util.List;

public class FMCardInfoReader extends InfoReader<String> {
    public FMCardInfoReader(IAPDUService omaService) {
        super(omaService);
    }

    /* access modifiers changed from: protected */
    public String handleResult(List<List<String>> list) throws AppletCardException {
        return null;
    }

    public String readInfo() throws AppletCardException {
        int resultCode;
        if (this.commandList != null) {
            List<ApduCommand> stepList = new ArrayList<>();
            for (ApduCommand command : this.commandList) {
                if (!(command instanceof ApduCommandInfo)) {
                    LogX.i("FMCardInfoReader readInfo, command is not instanceof ApduCommandInfo.");
                } else {
                    stepList.add((ApduCommandInfo) command);
                }
            }
            TaskResult<ChannelID> excuteResult = this.omaService.excuteApduList(stepList, this.channelID);
            this.channelID = excuteResult.getData();
            if (excuteResult.getResultCode() != 0) {
                String msg = excuteResult.getMsg();
                int resultCode2 = excuteResult.getResultCode();
                if (resultCode2 == 2005) {
                    resultCode = 3;
                } else if (resultCode2 == 4002) {
                    resultCode = checkVerifyPinSW(excuteResult.getLastExcutedCommand().getApdu(), excuteResult.getLastExcutedCommand().getSw());
                } else if (resultCode2 == 4001) {
                    resultCode = 9;
                } else {
                    resultCode = 6;
                }
                LogX.i("FMCardInfoReader readFMInfo, readFMInfo excuteApduList failed. resultCode = " + resultCode + ", msg = " + msg);
                throw new AppletCardException(resultCode, "readInfo excuteApduList failed. " + msg);
            }
            StringBuilder sb = new StringBuilder();
            int length = stepList.size();
            for (int i = 0; i < length; i++) {
                ApduCommand command2 = stepList.get(i);
                sb.append(command2.getApdu());
                sb.append("|");
                sb.append(command2.getRapdu());
                sb.append(command2.getSw());
                if (i < length - 1) {
                    sb.append(SNBConstant.FILTER);
                }
            }
            return sb.toString();
        }
        LogX.i("FMInfoReader readInfo, commandList is null.");
        throw new AppletCardException(1, "readInfo commandList is null");
    }
}
