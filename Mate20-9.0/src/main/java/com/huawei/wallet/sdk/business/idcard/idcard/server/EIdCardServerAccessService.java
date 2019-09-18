package com.huawei.wallet.sdk.business.idcard.idcard.server;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessCancelEidRequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.CancelEIDResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessBaseResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessCancelEidResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.response.BaseResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.oma.OmaApduManager;
import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;
import com.huawei.wallet.sdk.common.utils.PhoneFeatureAdaptUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EIdCardServerAccessService {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "IDCard:EIdCardServerAccessService";
    private static volatile EIdCardServerAccessService instance = null;
    private String apduError = "";
    private IdCardServer cardServer = null;
    private EIdCardServer eIdCardServer = null;
    private Context mContext = null;
    private boolean mExist;
    private int mediaType;
    private IAPDUService omaService = null;

    public void cancel() {
        this.mExist = true;
    }

    public void cleanCancelFlag() {
        this.mExist = false;
    }

    private EIdCardServerAccessService(Context context) {
        this.mContext = context.getApplicationContext();
        this.eIdCardServer = new EIdCardServer(this.mContext);
        this.cardServer = new IdCardServer(this.mContext);
        this.omaService = OmaApduManager.getInstance(this.mContext);
        this.mediaType = getMediaType();
    }

    public static EIdCardServerAccessService getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null && context != null) {
                    instance = new EIdCardServerAccessService(context);
                }
            }
        }
        return instance;
    }

    private String getApduError(ApduCommand command, TaskResult<ChannelID> result) {
        if (command == null) {
            return "";
        }
        return "resultCode_" + result.getResultCode() + "_idx_" + command.getIndex() + "_rapdu_" + command.getRapdu() + "_sw_" + command.getSw();
    }

    private boolean apduListIsEmpty(List<ServerAccessAPDU> apduList) {
        return apduList == null || apduList.isEmpty();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:53:0x013c, code lost:
        return;
     */
    private void executeCommand(String transactionId, List<ServerAccessAPDU> apduList, BaseResponse response, String issuerId, String appletAid, String cplc, String deviceModel, String seChipManuFacturer, ChannelID channel, Boolean closeChannel, String nextStep) {
        ChannelID channel2;
        TaskResult<ChannelID> result;
        List<ApduCommand> list;
        ChannelID channel3;
        List<ServerAccessAPDU> apduList2;
        TaskResult<ChannelID> result2;
        List<ApduCommand> apduCommandList;
        BaseResponse baseResponse = response;
        String str = transactionId;
        if (!StringUtil.isEmpty(str, true)) {
            List<ServerAccessAPDU> apduList3 = apduList;
            if (!apduListIsEmpty(apduList3)) {
                synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
                    String nextStep2 = nextStep;
                    ChannelID channel4 = channel;
                    while (!this.mExist) {
                        try {
                            this.apduError = "";
                            List<ApduCommand> apduCommandList2 = changeServerAccessAPDU2ApduCommand(apduList3);
                            try {
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
                                    this.apduError = getApduError(result.getLastExcutedCommand(), result);
                                    List<ServerAccessAPDU> apduList4 = changeApduCommand2ServerAccessAPDU(apduCommandList2, result.getLastExcutedCommand());
                                    try {
                                        r3 = r3;
                                        String str2 = str;
                                        apduList2 = apduList4;
                                        result2 = result;
                                        apduCommandList = apduCommandList2;
                                        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest = new ServerAccessApplyAPDURequest(issuerId, appletAid, cplc, str2, apduList4.size(), apduList4, deviceModel, seChipManuFacturer);
                                        ServerAccessApplyAPDURequest req = serverAccessApplyAPDURequest;
                                        req.setCurrentStep(nextStep2);
                                        ServerAccessApplyAPDUResponse res = this.cardServer.applyAPDU(req);
                                        if (res != null) {
                                            LogX.i(TAG, "executeCommand, apply apdu response = " + res.returnCode);
                                            if (res.returnCode == 0) {
                                                List<ServerAccessAPDU> apduList5 = res.getApduList();
                                                try {
                                                    nextStep2 = res.getNextStep();
                                                    if (apduListIsEmpty(apduList5)) {
                                                        baseResponse.setResultCode(0);
                                                    }
                                                    if (apduListIsEmpty(apduList5)) {
                                                        List<ServerAccessAPDU> list3 = apduList5;
                                                        TaskResult<ChannelID> taskResult = result2;
                                                        channel2 = channel3;
                                                        break;
                                                    }
                                                    apduList3 = apduList5;
                                                    TaskResult<ChannelID> taskResult2 = result2;
                                                    channel4 = channel3;
                                                    List<ApduCommand> list4 = apduCommandList;
                                                    str = transactionId;
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    List<ServerAccessAPDU> list5 = apduList5;
                                                    TaskResult<ChannelID> taskResult3 = result2;
                                                    ChannelID channelID = channel3;
                                                    throw th;
                                                }
                                            } else {
                                                translateErrorCode(res, baseResponse);
                                                baseResponse.setApduError(this.apduError);
                                                baseResponse.setResultDesc(res.getResultDesc() + response.getClass().getSimpleName() + ", OMA result : " + result2.getPrintMsg());
                                            }
                                        } else {
                                            LogX.e(TAG, "executeCommand, invalid apply apdu response");
                                        }
                                        TaskResult<ChannelID> taskResult4 = result2;
                                        channel2 = channel3;
                                        List<ServerAccessAPDU> list6 = apduList2;
                                        break;
                                    } catch (Throwable th3) {
                                        th = th3;
                                        TaskResult<ChannelID> taskResult5 = result;
                                        ChannelID channelID2 = channel3;
                                        List<ServerAccessAPDU> list7 = apduList4;
                                        List<ApduCommand> list8 = apduCommandList2;
                                        throw th;
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    list = apduCommandList2;
                                    TaskResult<ChannelID> taskResult6 = result;
                                    ChannelID channelID3 = channel3;
                                    List<ApduCommand> apduCommandList3 = list;
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
                    if (closeChannel.booleanValue() || response.getResultCode() != 0) {
                        this.omaService.closeChannel(channel2);
                    }
                }
            }
        }
        LogX.e(TAG, "executeCommand, invalid param");
        baseResponse.setResultCode(1);
        baseResponse.setResultDesc("client check, invalid param");
    }

    private List<ApduCommand> changeServerAccessAPDU2ApduCommand(List<ServerAccessAPDU> apduList) {
        List<ApduCommand> apduCommandList = new ArrayList<>();
        for (ServerAccessAPDU apdu : apduList) {
            String apduId = apdu.getApduId();
            if (StringUtil.isEmpty(apduId, true)) {
                LogX.e(TAG, "changeServerAccessAPDU2ApduCommand, invalid apduId");
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
                    LogX.e(TAG, "changeServerAccessAPDU2ApduCommand, NumberFormatException");
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

    private void translateErrorCode(ServerAccessBaseResponse oldResponse, BaseResponse newResponse) {
        newResponse.setOriginResultCode(oldResponse.returnCode);
        newResponse.setErrorInfo(oldResponse.getErrorInfo());
        int i = oldResponse.returnCode;
        if (i != -4) {
            switch (i) {
                case -99:
                case CardServerBaseResponse.RESPONSE_CODE_CANNOT_BE_RESOLVED /*-98*/:
                    break;
                default:
                    switch (i) {
                        case -2:
                            newResponse.setResultCode(3);
                            newResponse.setResultDesc(oldResponse.getResultDesc());
                            return;
                        case -1:
                            newResponse.setResultCode(2);
                            newResponse.setResultDesc(oldResponse.getResultDesc());
                            return;
                        default:
                            switch (i) {
                                case 1:
                                    newResponse.setResultCode(1);
                                    newResponse.setResultDesc(oldResponse.getResultDesc());
                                    Map<String, String> params = new HashMap<>();
                                    params.put("fail_code", "" + oldResponse.returnCode);
                                    params.put("fail_reason", "ServerAccessService Interface, " + oldResponse.getResultDesc() + ", scene : " + newResponse.getClass());
                                    return;
                                case 2:
                                    newResponse.setResultCode(4);
                                    newResponse.setResultDesc(oldResponse.getResultDesc());
                                    return;
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                    break;
                                default:
                                    newResponse.setResultCode(oldResponse.returnCode);
                                    newResponse.setResultDesc(oldResponse.getResultDesc());
                                    return;
                            }
                    }
            }
        }
        newResponse.setResultCode(-99);
        newResponse.setResultDesc(oldResponse.getResultDesc());
        Map<String, String> params2 = new HashMap<>();
        params2.put("fail_code", "" + oldResponse.returnCode);
        params2.put("fail_reason", "ServerAccessService Interface, " + oldResponse.getResultDesc() + ", scene : " + newResponse.getClass());
    }

    public CancelEIDResponse cancelEID(ServerAccessCancelEidRequest req) {
        cleanCancelFlag();
        CancelEIDResponse response = new CancelEIDResponse();
        if (req == null) {
            LogX.e(TAG, "CancelEID, invalid param");
            response.setResultCode(1);
            response.setResultDesc("client check, invalid param");
            return response;
        }
        ServerAccessCancelEidResponse res = this.eIdCardServer.cancelEid(req);
        if (res != null) {
            LogX.i(TAG, "CancelEID, response = " + res.returnCode);
            if (res.returnCode == 0 || res.noEid()) {
                response.setResultCode(0);
            } else {
                translateErrorCode(res, response);
            }
        }
        return response;
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
}
