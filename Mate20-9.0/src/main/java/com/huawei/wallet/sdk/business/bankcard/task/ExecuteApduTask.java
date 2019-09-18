package com.huawei.wallet.sdk.business.bankcard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.server.BankCardServer;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.oma.OmaApduManager;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.common.http.response.BaseResponse;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExecuteApduTask {
    public static final String DEVICE_MODEL = "deviceModel";
    public static final String MANU_FACTURER = "manuFacturer";
    public static final String NEXT_STEP = "nextStep";
    public static final String PARTNER_ID = "partnerId";
    public static final String SRC_TRANSACTION_ID = "srcTransactionID";
    private static final String TAG = "ExecuteApduTask";
    public static final String TRANSACTION_ID = "transactionId";
    private static ExecuteApduTask instance;
    private static final byte[] lock = new byte[0];
    private final BankCardServer cardServer;
    private ChannelID mChannel = null;
    private IAPDUService omaService = null;

    private ExecuteApduTask(Context context) {
        this.cardServer = new BankCardServer(context);
        this.omaService = OmaApduManager.getInstance(context);
    }

    public static ExecuteApduTask getInstance(Context context) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ExecuteApduTask(context);
                }
            }
        }
        return instance;
    }

    public void executeCommand(List<ServerAccessAPDU> apduList, Map<String, String> params, BaseResponse response, boolean closeChannel, boolean isNoNeedCommandResp) {
        ChannelID channel;
        ChannelID channel2;
        Map<String, String> map = params;
        BaseResponse baseResponse = response;
        if (map == null || apduList == null || apduList.isEmpty()) {
            LogX.e("ExecuteApduTask executeCommand, invalid param");
            baseResponse.setResultCode(1);
            baseResponse.setResultDesc("client check, invalid param");
            return;
        }
        String srcTransactionID = map.get(SRC_TRANSACTION_ID);
        String partnerId = map.get(PARTNER_ID);
        String deviceModel = map.get(DEVICE_MODEL);
        String manuFacturer = map.get(MANU_FACTURER);
        ChannelID channel3 = null;
        String apduError = "";
        List<ServerAccessAPDU> apduList2 = apduList;
        String transactionId = map.get(TRANSACTION_ID);
        String nextStep = map.get(NEXT_STEP);
        while (true) {
            String transactionId2 = apduError;
            List<ApduCommand> apduCommandList = changeServerAccessAPDU2ApduCommand(apduList2);
            TaskResult<ChannelID> result = this.omaService.excuteApduList(apduCommandList, channel3);
            LogX.i("ExecuteApduTask executeCommand, oma execute command, " + result.getPrintMsg());
            ApduCommand command = result.getLastExcutedCommand();
            String apduError2 = getApduError(command, result);
            List<ServerAccessAPDU> apduList3 = changeApduCommand2ServerAccessAPDU(apduCommandList, result.getLastExcutedCommand());
            String apduError3 = apduError2;
            ApduCommand apduCommand = command;
            channel = result.getData();
            TaskResult<ChannelID> result2 = result;
            ServerAccessApplyAPDURequest req = new ServerAccessApplyAPDURequest(transactionId, map, apduList3.size(), apduList3, deviceModel, manuFacturer);
            req.setCurrentStep(nextStep);
            req.setSn(PhoneDeviceUtil.getSerialNumber());
            req.setPartnerId(partnerId);
            req.setSrcTransactionID(srcTransactionID);
            ServerAccessApplyAPDUResponse res = this.cardServer.applyApdu(req);
            if (res == null) {
                TaskResult<ChannelID> taskResult = result2;
                LogX.e("ExecuteApduTask executeCommand, invalid apply apdu response");
                break;
            }
            baseResponse.setTransactionId(res.getSrcTranID());
            if (!StringUtil.isEmpty(res.getTransactionId(), true)) {
                transactionId = res.getTransactionId();
            }
            LogX.i("ServerAccessServiceImpl executeCommand, apply apdu response = " + res.returnCode);
            if (res.returnCode != 0) {
                baseResponse.setApduError(apduError3);
                baseResponse.setResultDesc(res.getResultDesc() + response.getClass().getSimpleName() + ", OMA result : " + result2.getPrintMsg());
                break;
            }
            List<ServerAccessAPDU> apduList4 = res.getApduList();
            nextStep = res.getNextStep();
            if (apduList4 == null || apduList4.isEmpty()) {
                baseResponse.setResultCode(0);
            }
            if (apduList4 == null || apduList4.isEmpty()) {
                List<ServerAccessAPDU> list = apduList4;
                String str = nextStep;
                String str2 = apduError3;
                TaskResult<ChannelID> taskResult2 = result2;
            } else {
                ServerAccessApplyAPDURequest serverAccessApplyAPDURequest = req;
                apduList2 = apduList4;
                List<ApduCommand> list2 = apduCommandList;
                apduError = apduError3;
                channel3 = channel;
                TaskResult<ChannelID> taskResult3 = result2;
            }
        }
        if (closeChannel) {
            channel2 = channel;
        } else if (response.getResultCode() != 0) {
            channel2 = channel;
        } else {
            this.mChannel = channel;
        }
        this.omaService.closeChannel(channel2);
        this.mChannel = null;
    }

    private String getApduError(ApduCommand command, TaskResult<ChannelID> result) {
        String apduError = "";
        if (command != null) {
            apduError = "resultCode_" + result.getResultCode() + "_idx_" + command.getIndex() + "_rapdu_" + command.getRapdu() + "_sw_" + command.getSw();
            if (result.getResultCode() != 0) {
                String apduExcutedError = " resultCode:" + result.getResultCode() + "," + command.toString();
                LogC.w(TAG + apduExcutedError, false);
            }
        }
        return apduError;
    }

    private List<ApduCommand> changeServerAccessAPDU2ApduCommand(List<ServerAccessAPDU> apduList) {
        List<ApduCommand> apduCommandList = new ArrayList<>();
        for (ServerAccessAPDU apdu : apduList) {
            String apduId = apdu.getApduId();
            if (StringUtil.isEmpty(apduId, true)) {
                LogX.e("ServerAccessServiceImpl changeServerAccessAPDU2ApduCommand, invalid apduId");
            } else {
                try {
                    ApduCommand apduCommand = new ApduCommand();
                    apduCommand.setIndex(Integer.parseInt(apduId));
                    apduCommand.setApdu(apdu.getApduContent());
                    if (apdu.getApduStatus() != null) {
                        apduCommand.setChecker(apdu.getApduStatus().split("[|]"));
                    }
                    apduCommandList.add(apduCommand);
                } catch (NumberFormatException e) {
                    LogX.e("ServerAccessServiceImpl changeServerAccessAPDU2ApduCommand, NumberFormatException");
                }
            }
        }
        return apduCommandList;
    }

    private List<ServerAccessAPDU> changeApduCommand2ServerAccessAPDU(List<ApduCommand> apduCommandList, ApduCommand lastApduCommand) {
        List<ServerAccessAPDU> apduList = new ArrayList<>();
        if (lastApduCommand != null) {
            for (ApduCommand apduCommand : apduCommandList) {
                ServerAccessAPDU apdu = new ServerAccessAPDU();
                apdu.setApduId(String.valueOf(apduCommand.getIndex()));
                if (!StringUtil.isEmpty(apduCommand.getSw(), true)) {
                    apdu.setApduContent(apduCommand.getRapdu() + apduCommand.getSw());
                    apdu.setApduStatus(apduCommand.getSw());
                    apdu.setCommand(apduCommand.getApdu());
                    apdu.setChecker(apduCommand.getChecker());
                } else {
                    apdu.setApduContent("");
                    apdu.setApduStatus(null);
                }
                apduList.add(apdu);
            }
        }
        return apduList;
    }
}
