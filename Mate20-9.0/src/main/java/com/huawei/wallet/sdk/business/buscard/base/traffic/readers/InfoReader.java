package com.huawei.wallet.sdk.business.buscard.base.traffic.readers;

import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletCardResult;
import com.huawei.wallet.sdk.business.buscard.base.model.ApduCommandInfo;
import com.huawei.wallet.sdk.business.buscard.base.operation.Operation;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;

public abstract class InfoReader<T> {
    protected ChannelID channelID;
    protected List<ApduCommand> commandList;
    protected IAPDUService omaService;

    /* access modifiers changed from: protected */
    public abstract T handleResult(List<List<String>> list) throws AppletCardException;

    protected InfoReader(IAPDUService omaService2) {
        this.omaService = omaService2;
    }

    public T readInfo() throws AppletCardException {
        int resultCd;
        if (this.commandList != null) {
            List<List<ApduCommand>> steps = new ArrayList<>();
            List<ApduCommand> stepList = new ArrayList<>();
            for (ApduCommand command : this.commandList) {
                if (!(command instanceof ApduCommandInfo)) {
                    LogX.i("InfoReader readInfo, command is not instanceof ApduCommandInfo.");
                } else {
                    ApduCommandInfo commandInfo = (ApduCommandInfo) command;
                    stepList.add(commandInfo);
                    if (commandInfo.getOperations() != null) {
                        steps.add(stepList);
                        stepList = new ArrayList<>();
                    }
                }
            }
            List<List<String>> results = new ArrayList<>();
            int sz = steps.size();
            if (sz > 0) {
                int step = 0;
                while (step < sz) {
                    List<ApduCommand> list = steps.get(step);
                    TaskResult<ChannelID> excuteResult = this.omaService.excuteApduList(list, this.channelID);
                    this.channelID = excuteResult.getData();
                    if (excuteResult.getResultCode() != 0) {
                        String msg = excuteResult.getMsg();
                        int resultCd2 = excuteResult.getResultCode();
                        if (resultCd2 == 2005) {
                            resultCd = 3;
                        } else if (resultCd2 == 4002) {
                            resultCd = checkVerifyPinSW(excuteResult.getLastExcutedCommand().getApdu(), excuteResult.getLastExcutedCommand().getSw());
                        } else if (resultCd2 == 4001) {
                            resultCd = 9;
                        } else {
                            resultCd = 6;
                        }
                        LogX.i("InfoReader readInfo, readInfo excuteApduList failed. resultCd = " + resultCd + ", msg = " + msg);
                        throw new AppletCardException(resultCd, "readInfo excuteApduList failed. " + msg);
                    }
                    ApduCommandInfo dataCommandInfo = (ApduCommandInfo) list.get(list.size() - 1);
                    List<String> result = null;
                    try {
                        result = handlerRespData(dataCommandInfo.getRapdu(), dataCommandInfo.getOperations());
                    } catch (AppletCardException e) {
                        LogX.i("readInfo step " + step + "/" + sz + " failed. msg : " + e.getMessage() + " apdu info " + dataCommandInfo.toString());
                    }
                    step = getNextStep(dataCommandInfo, step, sz);
                    results.add(result);
                }
                return handleResult(results);
            }
            LogX.i("InfoReader readInfo, readInfo no operation exists in any APDU command.");
            throw new AppletCardException(2, "readInfo no operation exists in any APDU command.");
        }
        LogX.i("InfoReader readInfo, commandList is null.");
        throw new AppletCardException(1, "readInfo commandList is null");
    }

    /* access modifiers changed from: protected */
    public int getNextStep(ApduCommandInfo dataCommandInfo, int currentStep, int totleStep) {
        return currentStep + 1;
    }

    /* access modifiers changed from: protected */
    public int checkVerifyPinSW(String command, String sw) {
        if (!command.startsWith("00200000")) {
            return 9;
        }
        if (sw.startsWith("63C")) {
            return AppletCardResult.RESULT_FAILED_TRAFFIC_CARD_INFO_VERIFY_PIN_FAILED;
        }
        if ("6983".equals(sw)) {
            return AppletCardResult.RESULT_FAILED_TRAFFIC_CARD_INFO_PIN_LOCKED;
        }
        return 9;
    }

    public T readInfoFromData(String resp, List<Operation> operations) throws AppletCardException {
        if (StringUtil.isEmpty(resp, true) || operations == null || operations.isEmpty()) {
            throw new AppletCardException(1, "readInfoFromData param is illegal");
        }
        List<String> result = handlerRespData(resp, operations);
        List<List<String>> results = new ArrayList<>();
        results.add(result);
        return handleResult(results);
    }

    private List<String> handlerRespData(String resp, List<Operation> operations) throws AppletCardException {
        if (operations != null) {
            List<String> results = new ArrayList<>(operations.size() + 1);
            results.add(resp);
            for (int i = 0; i < operations.size(); i++) {
                Operation operation = operations.get(i);
                if (operation.isNeedChangeParamWithData()) {
                    operation.changeParamWithData(results);
                }
                results.add(operation.checkAndHandleData(results));
            }
            return results;
        }
        throw new AppletCardException(1, "handlerRespData param is illegal");
    }

    /* access modifiers changed from: protected */
    public void checkData(String buniessType, List<String> datas) throws AppletCardException {
        if (datas == null || datas.isEmpty()) {
            throw new AppletCardException(AppletCardResult.RESULT_FAILED_INNER_EXCEPTION, buniessType + " the data is empty");
        }
    }

    public ChannelID getChannelID() {
        return this.channelID;
    }

    public void setChannelID(ChannelID channelID2) {
        this.channelID = channelID2;
    }

    public void setCommandList(List<ApduCommand> commandList2) {
        this.commandList = commandList2;
    }
}
