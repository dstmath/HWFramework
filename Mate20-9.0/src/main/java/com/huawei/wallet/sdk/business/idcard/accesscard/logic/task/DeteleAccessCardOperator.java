package com.huawei.wallet.sdk.business.idcard.accesscard.logic.task;

import android.content.Context;
import android.os.Build;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.resulthandler.HandleNullifyResultHandler;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.server.card.CommonCardServer;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.ese.ESEApiFactory;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.oma.OmaApduManager;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessDeleteAppletRequest;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessDeleteAppletResponse;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeteleAccessCardOperator {
    private static final String TAG = "DeteleAccessCardOperato";
    private CommonCardServer cardServer;
    private HandleNullifyResultHandler handle;
    private String mAid;
    private final Context mContext;
    private String mIssureId;
    private IAPDUService omaService = null;
    private String uid;

    public DeteleAccessCardOperator(Context context, String issureId, String aid, boolean updateTA, HandleNullifyResultHandler handle2) {
        this.mContext = context.getApplicationContext();
        this.handle = handle2;
        this.mAid = aid;
        this.mIssureId = issureId;
        this.uid = "0";
        this.cardServer = new CommonCardServer(context);
        this.omaService = OmaApduManager.getInstance(this.mContext);
    }

    public void uninstall() {
        int deleteAccessCardResultCode;
        TACardInfo taInfo = WalletTaManager.getInstance(this.mContext).getCardInfoByAid(this.mAid);
        if (taInfo != null) {
            this.uid = taInfo.getFpanDigest();
        }
        String deviceModel = Build.MODEL;
        String cplc = ESEApiFactory.createESEInfoManagerApi(this.mContext).queryCplc();
        String str = cplc;
        ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest = new ServerAccessDeleteAppletRequest(this.mIssureId, str, this.mAid, deviceModel, ProductConfigUtil.geteSEManufacturer());
        ServerAccessDeleteAppletRequest deleteAppletRequest = serverAccessDeleteAppletRequest;
        ServerAccessDeleteAppletResponse deleteResponse = this.cardServer.deleteApplet(deleteAppletRequest);
        int deleteAccessCardResultCode2 = deleteResponse.returnCode;
        if (deleteAccessCardResultCode2 != 0) {
            LogX.w("DeteleAccessCardOperator:  deleteApplet  fail : " + deleteAccessCardResultCode2);
            handleResult(deleteAccessCardResultCode2, deleteResponse.getErrorInfo());
            return;
        }
        LogX.i("DeteleAccessCardOperator:  deleteApplet requestOpenCard success ");
        String transactionId = deleteResponse.getTransactionId();
        List<ServerAccessAPDU> apduList = deleteResponse.getApduList();
        if (apduList == null) {
            deleteAccessCardResultCode = deleteAccessCardResultCode2;
            ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse = deleteResponse;
            ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest2 = deleteAppletRequest;
        } else if (apduList.isEmpty()) {
            List<ServerAccessAPDU> list = apduList;
            deleteAccessCardResultCode = deleteAccessCardResultCode2;
            ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse2 = deleteResponse;
            ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest3 = deleteAppletRequest;
        } else {
            LogX.i(TAG, "executeCommand start: ");
            List<ServerAccessAPDU> list2 = apduList;
            ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse3 = deleteResponse;
            ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest4 = deleteAppletRequest;
            executeCommand(transactionId, apduList, deleteResponse, this.mIssureId, this.mAid, null, 1, deleteResponse.getNextStep(), this.uid, transactionId);
            LogX.i(TAG, "executeCommand end: ");
            int i = deleteAccessCardResultCode2;
        }
        LogX.w("DeteleAccessCardOperator:  apduList()  fail : " + deleteAccessCardResultCode);
        handleDeleteSuccess(this.mAid);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        handleResult(r4.returnCode, r4.getErrorInfo());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x012a, code lost:
        return;
     */
    private void executeCommand(String transactionId, List<ServerAccessAPDU> apduList, ServerAccessDeleteAppletResponse response, String issuerId, String appletAid, ChannelID channel, Boolean closeChannel, String nextStep, String uid2, String srcTransactionID) {
        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest;
        List<ApduCommand> apduCommandList;
        TaskResult<ChannelID> result;
        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest2;
        Map<String, String> paramMap;
        String str;
        ChannelID channel2;
        TaskResult<ChannelID> result2;
        ServerAccessApplyAPDURequest req;
        String str2 = appletAid;
        String str3 = transactionId;
        if (StringUtil.isEmpty(str3, true) || apduList == null || apduList.isEmpty()) {
            ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse = response;
            String str4 = issuerId;
            String str5 = srcTransactionID;
            LogX.e("DeteleAccessCardOperator executeCommand, invalid param");
            handleResult(1, null);
            return;
        }
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            try {
                String deviceModel = Build.MODEL;
                String cplc = ESEApiFactory.createESEInfoManagerApi(this.mContext).queryCplc();
                List<ServerAccessAPDU> apduList2 = apduList;
                String nextStep2 = nextStep;
                ServerAccessApplyAPDURequest req2 = null;
                String seChipManuFacturer = ProductConfigUtil.geteSEManufacturer();
                TaskResult<ChannelID> result3 = null;
                ChannelID channel3 = channel;
                while (true) {
                    Object obj = "";
                    try {
                        apduCommandList = changeServerAccessAPDU2ApduCommand(apduList2);
                        try {
                            result = this.omaService.excuteApduList(apduCommandList, channel3);
                        } catch (Throwable th) {
                            th = th;
                            ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse2 = response;
                            String str6 = srcTransactionID;
                            serverAccessApplyAPDURequest = req2;
                            String str7 = issuerId;
                            List<ServerAccessAPDU> list = apduList2;
                            ChannelID channelID = channel3;
                            TaskResult<ChannelID> taskResult = result3;
                            List<ApduCommand> list2 = apduCommandList;
                            ServerAccessApplyAPDURequest req3 = serverAccessApplyAPDURequest;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse3 = response;
                        String str8 = srcTransactionID;
                        serverAccessApplyAPDURequest = req2;
                        String str9 = issuerId;
                        List<ServerAccessAPDU> list3 = apduList2;
                        ChannelID channelID2 = channel3;
                        TaskResult<ChannelID> taskResult2 = result3;
                        ServerAccessApplyAPDURequest req32 = serverAccessApplyAPDURequest;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                    try {
                        ChannelID channel4 = result.getData();
                        try {
                            LogX.i("DeteleAccessCardOperator executeCommand, oma execute command, " + result.getPrintMsg());
                            List<ServerAccessAPDU> apduList3 = changeApduCommand2ServerAccessAPDU(apduCommandList, result.getLastExcutedCommand());
                            try {
                                Map<String, String> paramMap2 = new HashMap<>();
                                paramMap2.put(ServerAccessApplyAPDURequest.ReqKey.AID, str2);
                                ServerAccessApplyAPDURequest req4 = req2;
                                try {
                                    paramMap2.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, issuerId);
                                    paramMap2.put("cplc", cplc);
                                    serverAccessApplyAPDURequest2 = serverAccessApplyAPDURequest2;
                                    paramMap = paramMap2;
                                    str = str3;
                                    channel2 = channel4;
                                    result2 = result;
                                } catch (Throwable th4) {
                                    th = th4;
                                    ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse4 = response;
                                    List<ServerAccessAPDU> list4 = apduList3;
                                    ChannelID channelID3 = channel4;
                                    TaskResult<ChannelID> taskResult3 = result;
                                    String str10 = srcTransactionID;
                                    List<ApduCommand> list5 = apduCommandList;
                                    ServerAccessApplyAPDURequest serverAccessApplyAPDURequest3 = req4;
                                    TaskResult<ChannelID> taskResult4 = taskResult3;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                                try {
                                    serverAccessApplyAPDURequest2 = new ServerAccessApplyAPDURequest(str, paramMap, apduList3.size(), apduList3, deviceModel, seChipManuFacturer);
                                    req = serverAccessApplyAPDURequest2;
                                } catch (Throwable th5) {
                                    th = th5;
                                    ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse5 = response;
                                    String str11 = srcTransactionID;
                                    List<ApduCommand> list6 = apduCommandList;
                                    ServerAccessApplyAPDURequest serverAccessApplyAPDURequest4 = req4;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse6 = response;
                                List<ServerAccessAPDU> list7 = apduList3;
                                ChannelID channelID4 = channel4;
                                TaskResult<ChannelID> taskResult5 = result;
                                ServerAccessApplyAPDURequest serverAccessApplyAPDURequest5 = req2;
                                String str12 = issuerId;
                                String str13 = srcTransactionID;
                                List<ApduCommand> list8 = apduCommandList;
                                ServerAccessApplyAPDURequest serverAccessApplyAPDURequest6 = serverAccessApplyAPDURequest5;
                                TaskResult<ChannelID> taskResult6 = taskResult5;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse7 = response;
                            ChannelID channelID5 = channel4;
                            TaskResult<ChannelID> taskResult7 = result;
                            ServerAccessApplyAPDURequest serverAccessApplyAPDURequest7 = req2;
                            String str14 = issuerId;
                            String str15 = srcTransactionID;
                            List<ServerAccessAPDU> list9 = apduList2;
                            List<ApduCommand> list10 = apduCommandList;
                            ServerAccessApplyAPDURequest serverAccessApplyAPDURequest8 = serverAccessApplyAPDURequest7;
                            TaskResult<ChannelID> taskResult8 = taskResult7;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse8 = response;
                        TaskResult<ChannelID> taskResult9 = result;
                        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest9 = req2;
                        String str16 = issuerId;
                        String str17 = srcTransactionID;
                        List<ServerAccessAPDU> list11 = apduList2;
                        ChannelID channelID6 = channel3;
                        List<ApduCommand> list12 = apduCommandList;
                        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest10 = serverAccessApplyAPDURequest9;
                        TaskResult<ChannelID> taskResult10 = taskResult9;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                    try {
                        req.setCurrentStep(nextStep2);
                        req.setSn(PhoneDeviceUtil.getSerialNumber());
                        try {
                            req.setSrcTransactionID(srcTransactionID);
                            ServerAccessApplyAPDUResponse res = this.cardServer.applyAPDU(req);
                            if (res == null) {
                                LogX.e("DeteleAccessCardOperator executeCommand, invalid apply apdu response");
                                break;
                            }
                            LogX.i("DeteleAccessCardOperator executeCommand, apply apdu response = " + res.returnCode);
                            if (res.returnCode != 0) {
                                break;
                            }
                            List<ServerAccessAPDU> apduList4 = res.getApduList();
                            try {
                                nextStep2 = res.getNextStep();
                                if (apduList4 == null || apduList4.isEmpty()) {
                                    handleDeleteSuccess(str2);
                                }
                                if (apduList4 == null || apduList4.isEmpty()) {
                                    List<ServerAccessAPDU> list13 = apduList4;
                                } else {
                                    req2 = req;
                                    apduList2 = apduList4;
                                    channel3 = channel2;
                                    List<ApduCommand> list14 = apduCommandList;
                                    result3 = result2;
                                    str3 = transactionId;
                                }
                            } catch (Throwable th9) {
                                th = th9;
                                ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse9 = response;
                                ServerAccessApplyAPDURequest serverAccessApplyAPDURequest11 = req;
                                List<ServerAccessAPDU> list15 = apduList4;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } catch (Throwable th10) {
                            th = th10;
                            ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse10 = response;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse11 = response;
                        String str18 = srcTransactionID;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
                if (!closeChannel.booleanValue()) {
                    try {
                        if (response.returnCode != 0) {
                        }
                    } catch (Throwable th12) {
                        th = th12;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } else {
                    ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse12 = response;
                }
                this.omaService.closeChannel(channel2);
            } catch (Throwable th13) {
                th = th13;
                ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse13 = response;
                String str19 = issuerId;
                String str20 = srcTransactionID;
                List<ServerAccessAPDU> list16 = apduList;
                ChannelID channelID7 = channel;
                String str21 = nextStep;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    private void handleDeleteSuccess(String appletAid) {
        if (deleteTA(appletAid) == 0) {
            LogX.i("DeteleAccessCardOperator delete accesscard OK----");
            handleResult(0, null);
        }
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

    private List<ApduCommand> changeServerAccessAPDU2ApduCommand(List<ServerAccessAPDU> apduList) {
        List<ApduCommand> apduCommandList = new ArrayList<>();
        for (ServerAccessAPDU apdu : apduList) {
            String apduId = apdu.getApduId();
            if (StringUtil.isEmpty(apduId, true)) {
                LogX.e("DeteleAccessCardOperator changeServerAccessAPDU2ApduCommand, invalid apduId");
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
                    LogX.e("DeteleAccessCardOperator changeServerAccessAPDU2ApduCommand, NumberFormatException");
                }
            }
        }
        return apduCommandList;
    }

    private int deleteTA(String aid) {
        ErrorInfo errorInfo = null;
        if (WalletTaManager.getInstance(this.mContext).getCardInfoByAid(aid) != null) {
            boolean isRemoveSuccess = false;
            try {
                WalletTaManager.getInstance(this.mContext).removeCardByAid(aid);
                isRemoveSuccess = true;
            } catch (WalletTaException.WalletTaCardNotExistException e) {
                LogX.e("WalletTaCardNotExistException ", e.getMessage());
                isRemoveSuccess = true;
            } catch (WalletTaException.WalletTaSystemErrorException e2) {
                LogX.e("WalletTaSystemErrorException ", (Throwable) e2);
                errorInfo = new ErrorInfo();
                errorInfo.setDisplayOverview("WalletTaSystemErrorException");
                errorInfo.setDisplayDetail(e2.getMessage());
                errorInfo.setSuggestion("ee");
            }
            if (isRemoveSuccess) {
                handleResult(0, null);
                return 0;
            }
            handleResult(99, errorInfo);
            return 99;
        }
        LogX.i(TAG, "ta not exsit");
        handleResult(0, null);
        return 0;
    }

    private void handleResult(int result, ErrorInfo errorInfo) {
        if (this.handle != null) {
            this.handle.handleResult(result);
        }
    }
}
