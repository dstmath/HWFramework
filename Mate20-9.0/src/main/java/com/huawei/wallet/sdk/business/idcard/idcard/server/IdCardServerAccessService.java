package com.huawei.wallet.sdk.business.idcard.idcard.server;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessDeleteAppletRequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessBaseResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessDeleteAppletResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.response.BaseResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.response.DeleteAppletResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.oma.OmaApduManager;
import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.PhoneFeatureAdaptUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IdCardServerAccessService {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "IDCard:ServerAccessService";
    private static volatile IdCardServerAccessService instance = null;
    private String apduError = "";
    private IdCardServer cardServer = null;
    private Context mContext = null;
    private boolean mExist;
    private int mMediaType;
    private IAPDUService omaService = null;

    public void cancel() {
        this.mExist = true;
    }

    public void cleanCancelFlag() {
        this.mExist = false;
    }

    private IdCardServerAccessService(Context context) {
        this.mContext = context.getApplicationContext();
        this.cardServer = new IdCardServer(this.mContext);
        this.mMediaType = getMediaType();
    }

    public static IdCardServerAccessService getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null && context != null) {
                    instance = new IdCardServerAccessService(context);
                }
            }
        }
        return instance;
    }

    public int mediaType() {
        return this.mMediaType;
    }

    public DeleteAppletResponse deleteApplet(ServerAccessDeleteAppletRequest req) {
        ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest = req;
        cleanCancelFlag();
        DeleteAppletResponse response = new DeleteAppletResponse();
        if (serverAccessDeleteAppletRequest == null) {
            LogX.e(TAG, "deleteApplet, invalid param");
            response.setResultCode(1);
            response.setResultDesc("client check, invalid param");
            return response;
        }
        LogX.i(TAG, "deleteApp source : " + req.getSource());
        ServerAccessDeleteAppletResponse res = this.cardServer.deleteApplet(serverAccessDeleteAppletRequest);
        if (res != null) {
            LogX.i(TAG, "deleteApplet, response = " + res.returnCode);
            if (res.returnCode == 0) {
                String transactionId = res.getTransactionId();
                List<ServerAccessAPDU> apduList = res.getApduList();
                if (apduList == null) {
                    List<ServerAccessAPDU> list = apduList;
                } else if (apduList.isEmpty()) {
                    List<ServerAccessAPDU> list2 = apduList;
                } else {
                    ChannelID channel = new ChannelID();
                    channel.setMediaType(this.mMediaType);
                    ChannelID channelID = channel;
                    List<ServerAccessAPDU> list3 = apduList;
                    executeCommand(transactionId, apduList, response, req.getIssuerId(), req.getAid(), req.getCplc(), req.getDeviceModel(), req.getSeChipManuFacturer(), channel, 1, res.getNextStep());
                }
                response.setResultCode(0);
            } else {
                translateErrorCode(res, response);
            }
        }
        return response;
    }

    private boolean apduListIsEmpty(List<ServerAccessAPDU> apduList) {
        return apduList == null || apduList.isEmpty();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0149, code lost:
        return;
     */
    private void executeCommand(String transactionId, List<ServerAccessAPDU> apduList, BaseResponse response, String issuerId, String appletAid, String cplc, String deviceModel, String seChipManuFacturer, ChannelID channel, Boolean closeChannel, String nextStep) {
        ChannelID channel2;
        TaskResult<ChannelID> result;
        List<ApduCommand> list;
        ChannelID channel3;
        List<ServerAccessAPDU> apduList2;
        List<ServerAccessAPDU> apduList3;
        TaskResult<ChannelID> result2;
        List<ApduCommand> apduCommandList;
        BaseResponse baseResponse = response;
        String str = transactionId;
        if (!StringUtil.isEmpty(str, true)) {
            List<ServerAccessAPDU> apduList4 = apduList;
            if (!apduListIsEmpty(apduList4)) {
                synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
                    String nextStep2 = nextStep;
                    ChannelID channel4 = channel;
                    while (!this.mExist) {
                        try {
                            List<ApduCommand> apduCommandList2 = changeServerAccessAPDU2ApduCommand(apduList4);
                            try {
                                obtainOmaServiceObject();
                                result = this.omaService.excuteApduList(apduCommandList2, channel2);
                            } catch (Throwable th) {
                                th = th;
                                List<ApduCommand> list2 = apduCommandList2;
                                throw th;
                            }
                            try {
                                channel3 = result.getData();
                                try {
                                    LogX.i(TAG, "executeCommand, oma execute command, " + result.getPrintMsg());
                                    this.apduError = "";
                                    this.apduError = getApduError(result.getLastExcutedCommand(), result);
                                    apduList2 = changeApduCommand2ServerAccessAPDU(apduCommandList2, result.getLastExcutedCommand());
                                } catch (Throwable th2) {
                                    th = th2;
                                    list = apduCommandList2;
                                    TaskResult<ChannelID> taskResult = result;
                                    ChannelID channelID = channel3;
                                    List<ApduCommand> apduCommandList3 = list;
                                    throw th;
                                }
                                try {
                                    r3 = r3;
                                    String str2 = str;
                                    apduList3 = apduList2;
                                    result2 = result;
                                    apduCommandList = apduCommandList2;
                                    ServerAccessApplyAPDURequest serverAccessApplyAPDURequest = new ServerAccessApplyAPDURequest(issuerId, appletAid, cplc, str2, apduList2.size(), apduList2, deviceModel, seChipManuFacturer);
                                    ServerAccessApplyAPDURequest req = serverAccessApplyAPDURequest;
                                    req.setCurrentStep(nextStep2);
                                    ServerAccessApplyAPDUResponse resResponse = this.cardServer.applyAPDU(req);
                                    if (resResponse != null) {
                                        LogX.i(TAG, "executeCommand, apply apdu response = " + resResponse.returnCode);
                                        if (resResponse.returnCode == 0) {
                                            nextStep2 = resResponse.getNextStep();
                                            List<ServerAccessAPDU> apduList5 = resResponse.getApduList();
                                            try {
                                                if (apduListIsEmpty(apduList5)) {
                                                    baseResponse.setResultCode(0);
                                                }
                                                if (apduListIsEmpty(apduList5)) {
                                                    List<ServerAccessAPDU> list3 = apduList5;
                                                    TaskResult<ChannelID> taskResult2 = result2;
                                                    channel2 = channel3;
                                                    break;
                                                }
                                                apduList4 = apduList5;
                                                TaskResult<ChannelID> taskResult3 = result2;
                                                channel4 = channel3;
                                                List<ApduCommand> list4 = apduCommandList;
                                                str = transactionId;
                                            } catch (Throwable th3) {
                                                th = th3;
                                                List<ServerAccessAPDU> list5 = apduList5;
                                                TaskResult<ChannelID> taskResult4 = result2;
                                                ChannelID channelID2 = channel3;
                                                throw th;
                                            }
                                        } else {
                                            resResponse.setErrorCodeInfo(resResponse.getErrorInfo());
                                            translateErrorCode(resResponse, baseResponse);
                                            baseResponse.setApduError(this.apduError);
                                            baseResponse.setResultDesc(resResponse.getResultDesc() + response.getClass().getSimpleName() + ", OMA result : " + result2.getPrintMsg());
                                        }
                                    } else {
                                        LogX.e(TAG, "executeCommand, invalid apply apdu response");
                                    }
                                    TaskResult<ChannelID> taskResult5 = result2;
                                    channel2 = channel3;
                                    List<ServerAccessAPDU> list6 = apduList3;
                                    break;
                                } catch (Throwable th4) {
                                    th = th4;
                                    TaskResult<ChannelID> taskResult6 = result;
                                    ChannelID channelID3 = channel3;
                                    List<ServerAccessAPDU> list7 = apduList2;
                                    List<ApduCommand> list8 = apduCommandList2;
                                    throw th;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                list = apduCommandList2;
                                TaskResult<ChannelID> taskResult7 = result;
                                List<ApduCommand> apduCommandList32 = list;
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            throw th;
                        }
                    }
                    LogX.e(TAG, "executeCommand has been interrupted.");
                    if (response.getResultCode() != 0 || closeChannel.booleanValue()) {
                        obtainOmaServiceObject();
                        this.omaService.closeChannel(channel2);
                    }
                }
            }
        }
        LogX.e(TAG, "executeCommand, invalid param");
        baseResponse.setResultDesc("client check, invalid param");
        baseResponse.setResultCode(1);
    }

    private void obtainOmaServiceObject() {
        if (this.omaService == null) {
            this.omaService = OmaApduManager.getInstance(this.mContext);
        }
    }

    private String getApduError(ApduCommand command, TaskResult<ChannelID> result) {
        if (command == null) {
            return "";
        }
        return "resultCode_" + result.getResultCode() + "_idx_" + command.getIndex() + "_rapdu_" + command.getRapdu() + "_sw_" + command.getSw();
    }

    private List<ApduCommand> changeServerAccessAPDU2ApduCommand(List<ServerAccessAPDU> apduList) {
        List<ApduCommand> apduCommands = new ArrayList<>();
        for (ServerAccessAPDU accessApdu : apduList) {
            String apduId = accessApdu.getApduId();
            if (StringUtil.isEmpty(apduId, true)) {
                LogX.e(TAG, "changeServerAccessAPDU2ApduCommand, invalid apduId");
            } else {
                try {
                    ApduCommand apduComman = new ApduCommand();
                    apduComman.setApdu(accessApdu.getApduContent());
                    apduComman.setIndex(Integer.parseInt(apduId));
                    if (accessApdu.getApduStatus() != null) {
                        apduComman.setChecker(accessApdu.getApduStatus().split("[|]"));
                    }
                    apduCommands.add(apduComman);
                } catch (NumberFormatException e) {
                    LogX.e(TAG, "changeServerAccessAPDU2ApduCommand, NumberFormatException.");
                }
            }
        }
        return apduCommands;
    }

    private List<ServerAccessAPDU> changeApduCommand2ServerAccessAPDU(List<ApduCommand> apduCommandList, ApduCommand lastApduCommand) {
        List<ServerAccessAPDU> apduList = new ArrayList<>();
        if (lastApduCommand != null) {
            for (ApduCommand apduCommand : apduCommandList) {
                ServerAccessAPDU serverAccessAPDU = new ServerAccessAPDU();
                serverAccessAPDU.setApduId(String.valueOf(apduCommand.getIndex()));
                if (!StringUtil.isEmpty(apduCommand.getSw(), true)) {
                    serverAccessAPDU.setApduStatus(apduCommand.getSw());
                    serverAccessAPDU.setApduContent(apduCommand.getRapdu() + apduCommand.getSw());
                    serverAccessAPDU.setChecker(apduCommand.getChecker());
                    serverAccessAPDU.setCommand(apduCommand.getApdu());
                } else {
                    serverAccessAPDU.setApduContent("");
                    serverAccessAPDU.setApduStatus(null);
                }
                apduList.add(serverAccessAPDU);
            }
        }
        return apduList;
    }

    private void translateErrorCode(ServerAccessBaseResponse oldResponse, BaseResponse response) {
        int oldReturnCode = oldResponse.returnCode;
        response.setOriginResultCode(oldReturnCode);
        response.setErrorInfo(oldResponse.getErrorInfo());
        if (oldReturnCode != -4) {
            switch (oldReturnCode) {
                case -99:
                case CardServerBaseResponse.RESPONSE_CODE_CANNOT_BE_RESOLVED /*-98*/:
                    break;
                default:
                    switch (oldReturnCode) {
                        case -2:
                            response.setResultCode(3);
                            response.setResultDesc(oldResponse.getResultDesc());
                            return;
                        case -1:
                            response.setResultCode(2);
                            response.setResultDesc(oldResponse.getResultDesc());
                            return;
                        default:
                            switch (oldReturnCode) {
                                case 1:
                                    response.setResultCode(1);
                                    response.setResultDesc(oldResponse.getResultDesc());
                                    Map<String, String> params = new HashMap<>();
                                    params.put("fail_code", "" + oldReturnCode);
                                    params.put("fail_reason", "ServerAccessService Interface, " + oldResponse.getResultDesc() + ", scene : " + response.getClass());
                                    return;
                                case 2:
                                    response.setResultCode(4);
                                    response.setResultDesc(oldResponse.getResultDesc());
                                    return;
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                    break;
                                default:
                                    response.setResultCode(oldReturnCode);
                                    response.setResultDesc(oldResponse.getResultDesc());
                                    return;
                            }
                    }
            }
        }
        response.setResultCode(-99);
        response.setResultDesc(oldResponse.getResultDesc());
        Map<String, String> param = new HashMap<>();
        param.put("fail_reason", "ServerAccessService Interface, " + oldResponse.getResultDesc() + ", scene : " + response.getClass());
        StringBuilder sb = new StringBuilder();
        sb.append("");
        sb.append(oldReturnCode);
        param.put("fail_code", sb.toString());
    }

    private int getMediaType() {
        if (PhoneFeatureAdaptUtil.isMultiEseDevice()) {
            int type = PhoneFeatureAdaptUtil.getCardActiveModeAndSeReaderType(this.mContext, 3)[1];
            if (type == 0) {
                LogX.i(TAG, "mediaType is eSE");
            }
            if (type == 3) {
                LogX.i(TAG, "mediaType is inSE");
            }
            return type;
        }
        LogX.i(TAG, "mediaType is eSE");
        return 0;
    }

    public String getCplc() {
        String cplc;
        LogC.i(TAG, "getCplc begin", false);
        if (this.mMediaType == 3) {
            cplc = ESEInfoManager.getInstance(this.mContext).queryinSECplc();
        } else {
            cplc = ESEInfoManager.getInstance(this.mContext).queryCplc();
        }
        LogC.i(TAG, "getCplc end", false);
        return cplc;
    }

    public void setCardServer(IdCardServer cardServer2) {
        this.cardServer = cardServer2;
    }
}
