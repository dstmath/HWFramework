package com.huawei.wallet.sdk.business.idcard.accesscard.logic.task;

import android.content.Context;
import android.os.Build;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.exception.AccessCardOperatorException;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.resulthandler.InitAccessCardResultHandler;
import com.huawei.wallet.sdk.business.idcard.accesscard.server.AccesscardServer;
import com.huawei.wallet.sdk.business.idcard.accesscard.server.request.OpenAccessCardRequest;
import com.huawei.wallet.sdk.business.idcard.accesscard.server.response.OpenAccessCardResponse;
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

public class InitAccessCardOperator {
    private static final String TAG = "InitAccessCardOperator";
    private AccesscardServer accesscardServer;
    private CommonCardServer cardServer;
    private InitAccessCardResultHandler handle;
    private Context mContext;
    private IAPDUService omaService = null;

    public InitAccessCardOperator(Context context, InitAccessCardResultHandler handle2) {
        this.handle = handle2;
        this.mContext = context;
        this.accesscardServer = new AccesscardServer(context);
        this.cardServer = new CommonCardServer(context);
        this.omaService = OmaApduManager.getInstance(this.mContext);
    }

    public void init(List<TACardInfo> list) {
        LogX.i(TAG, "init: start");
        if (list == null || list.size() < 1) {
            handleResult(-1);
            return;
        }
        try {
            for (TACardInfo cardInfo : list) {
                uninstall(cardInfo.getIssuerId(), cardInfo.getAid(), cardInfo.getFpanDigest(), true);
            }
            LogX.i(TAG, "remove all access card success");
            handleResult(0);
            LogX.i(TAG, "init: end");
        } catch (AccessCardOperatorException e) {
            LogX.i(TAG, "init: occur error in removing accesscard" + e.getMessage());
            handleResult(-1);
        }
    }

    private boolean openAccessCard(String aid, String issureId, String mUid) throws AccessCardOperatorException {
        int openAccessCardResultCode;
        String deviceModel = Build.MODEL;
        String cplc = ESEApiFactory.createESEInfoManagerApi(this.mContext).queryCplc();
        String seChipManuFacturer = ProductConfigUtil.geteSEManufacturer();
        OpenAccessCardRequest request = new OpenAccessCardRequest();
        String str = aid;
        request.setAppletAid(str);
        request.setCplc(cplc);
        request.setDeviceModel(deviceModel);
        String str2 = issureId;
        request.setIssuerid(str2);
        request.setUid(mUid);
        request.setLatitude("0");
        request.setLongitude("0");
        request.setSeChipManuFacturer(seChipManuFacturer);
        OpenAccessCardResponse openAccessCardResponse = this.accesscardServer.openAccessCard(request);
        int openAccessCardResultCode2 = openAccessCardResponse.getReturnCode();
        if (openAccessCardResultCode2 != 0) {
            return false;
        }
        String transactionId = openAccessCardResponse.getTransactionId();
        List<ServerAccessAPDU> apduList = openAccessCardResponse.getApduList();
        if (apduList == null) {
            openAccessCardResultCode = openAccessCardResultCode2;
            OpenAccessCardResponse openAccessCardResponse2 = openAccessCardResponse;
        } else if (apduList.isEmpty()) {
            List<ServerAccessAPDU> list = apduList;
            openAccessCardResultCode = openAccessCardResultCode2;
            OpenAccessCardResponse openAccessCardResponse3 = openAccessCardResponse;
        } else {
            LogX.i("OpenAccessCardOperator", "executeCommand start: ");
            List<ServerAccessAPDU> list2 = apduList;
            int i = openAccessCardResultCode2;
            OpenAccessCardResponse openAccessCardResponse4 = openAccessCardResponse;
            boolean b = executeCommand(transactionId, apduList, str2, str, null, 1, openAccessCardResponse.getNextStep(), mUid, openAccessCardResponse.getTransactionId());
            LogX.i("OpenAccessCardOperator", "executeCommand end: ");
            return b;
        }
        LogX.w("OpenAccessCardOperator:  apduList() requestOpenCard fail : " + openAccessCardResultCode);
        return true;
    }

    private void uninstall(String mIssureId, String mAid, String uid, boolean isFirst) throws AccessCardOperatorException {
        int deleteAccessCardResultCode;
        String str = mIssureId;
        String str2 = mAid;
        String str3 = uid;
        String deviceModel = Build.MODEL;
        ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest = new ServerAccessDeleteAppletRequest(str, ESEApiFactory.createESEInfoManagerApi(this.mContext).queryCplc(), str2, deviceModel, ProductConfigUtil.geteSEManufacturer());
        ServerAccessDeleteAppletRequest deleteAppletRequest = serverAccessDeleteAppletRequest;
        ServerAccessDeleteAppletResponse deleteResponse = this.cardServer.deleteApplet(deleteAppletRequest);
        int deleteAccessCardResultCode2 = deleteResponse.returnCode;
        if (deleteAccessCardResultCode2 != 0) {
            if (3 != deleteAccessCardResultCode2 || !isFirst) {
                LogX.w("InitAccessCardOperator:  deleteApplet  requestParam fail : " + deleteAccessCardResultCode2);
                throw new AccessCardOperatorException();
            } else if (openAccessCard(str2, str, str3)) {
                uninstall(str, str2, str3, false);
                return;
            }
        }
        LogX.i("InitAccessCardOperator:  deleteApplet requestParam success ");
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
            int deleteAccessCardResultCode3 = deleteAccessCardResultCode2;
            ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse3 = deleteResponse;
            ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest4 = deleteAppletRequest;
            if (executeCommand(transactionId, apduList, str, str2, null, 1, deleteResponse.getNextStep(), str3, transactionId)) {
                handleInitSuccess(str2, str3);
            }
            LogX.i(TAG, "executeCommand end: ");
            int i = deleteAccessCardResultCode3;
        }
        LogX.w("InitAccessCardOperator:  apduList()  fail : " + deleteAccessCardResultCode);
        handleInitSuccess(str2, str3);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0177, code lost:
        r17 = r13;
        r13 = r25;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x017f, code lost:
        if (r28.booleanValue() == false) goto L_0x0186;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0181, code lost:
        r1.omaService.closeChannel(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0187, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0188, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0189, code lost:
        r21 = r3;
        r3 = r4;
        r16 = r5;
        r4 = r6;
        r5 = r17;
     */
    private boolean executeCommand(String transactionId, List<ServerAccessAPDU> apduList, String issuerId, String appletAid, ChannelID channel, Boolean closeChannel, String nextStep, String uid, String srcTransactionID) throws AccessCardOperatorException {
        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest;
        List<ApduCommand> apduCommandList;
        TaskResult<ChannelID> result;
        TaskResult<ChannelID> taskResult;
        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest2;
        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest3;
        TaskResult<ChannelID> result2;
        ServerAccessApplyAPDURequest req;
        boolean flag;
        String str = transactionId;
        if (StringUtil.isEmpty(str, true) || apduList == null || apduList.isEmpty()) {
            String str2 = issuerId;
            LogX.e("InitAccessCardOperator executeCommand, invalid param");
            throw new AccessCardOperatorException();
        }
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            try {
                String deviceModel = Build.MODEL;
                String cplc = ESEApiFactory.createESEInfoManagerApi(this.mContext).queryCplc();
                String seChipManuFacturer = ProductConfigUtil.geteSEManufacturer();
                String nextStep2 = nextStep;
                boolean req2 = true;
                TaskResult<ChannelID> result3 = null;
                ServerAccessApplyAPDURequest req3 = null;
                ChannelID channel2 = channel;
                List<ApduCommand> apduCommandList2 = null;
                List<ServerAccessAPDU> apduList2 = apduList;
                while (true) {
                    boolean flag2 = req2;
                    if (!flag2) {
                        break;
                    }
                    try {
                        apduCommandList = changeServerAccessAPDU2ApduCommand(apduList2);
                        try {
                            result = this.omaService.excuteApduList(apduCommandList, channel2);
                        } catch (Throwable th) {
                            th = th;
                            String str3 = srcTransactionID;
                            serverAccessApplyAPDURequest = req3;
                            String str4 = issuerId;
                            List<ServerAccessAPDU> list = apduList2;
                            ChannelID channelID = channel2;
                            TaskResult<ChannelID> taskResult2 = result3;
                            List<ApduCommand> list2 = apduCommandList;
                            ServerAccessApplyAPDURequest req4 = serverAccessApplyAPDURequest;
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
                        serverAccessApplyAPDURequest = req3;
                        String str5 = issuerId;
                        List<ServerAccessAPDU> list3 = apduList2;
                        List<ApduCommand> list4 = apduCommandList2;
                        ChannelID channelID2 = channel2;
                        TaskResult<ChannelID> taskResult3 = result3;
                        ServerAccessApplyAPDURequest req42 = serverAccessApplyAPDURequest;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                    try {
                        ChannelID channel3 = result.getData();
                        try {
                            LogX.i("InitAccessCardOperator executeCommand, oma execute command, " + result.getPrintMsg());
                            List<ServerAccessAPDU> apduList3 = changeApduCommand2ServerAccessAPDU(apduCommandList, result.getLastExcutedCommand());
                            try {
                                HashMap hashMap = new HashMap();
                                hashMap.put(ServerAccessApplyAPDURequest.ReqKey.AID, appletAid);
                                ServerAccessApplyAPDURequest req5 = req3;
                                try {
                                    hashMap.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, issuerId);
                                    hashMap.put("cplc", cplc);
                                    serverAccessApplyAPDURequest3 = serverAccessApplyAPDURequest3;
                                    HashMap hashMap2 = hashMap;
                                    result2 = result;
                                } catch (Throwable th4) {
                                    th = th4;
                                    String str6 = srcTransactionID;
                                    List<ServerAccessAPDU> list5 = apduList3;
                                    List<ApduCommand> list6 = apduCommandList;
                                    ServerAccessApplyAPDURequest serverAccessApplyAPDURequest4 = req5;
                                    TaskResult<ChannelID> taskResult4 = result;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                                try {
                                    serverAccessApplyAPDURequest3 = new ServerAccessApplyAPDURequest(str, hashMap, apduList3.size(), apduList3, deviceModel, seChipManuFacturer);
                                    req = serverAccessApplyAPDURequest3;
                                    try {
                                        req.setCurrentStep(nextStep2);
                                        req.setSn(PhoneDeviceUtil.getSerialNumber());
                                    } catch (Throwable th5) {
                                        th = th5;
                                        String str7 = srcTransactionID;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th6) {
                                    th = th6;
                                    String str8 = srcTransactionID;
                                    List<ApduCommand> list7 = apduCommandList;
                                    ServerAccessApplyAPDURequest serverAccessApplyAPDURequest5 = req5;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th7) {
                                th = th7;
                                String str9 = srcTransactionID;
                                List<ServerAccessAPDU> list8 = apduList3;
                                ServerAccessApplyAPDURequest serverAccessApplyAPDURequest6 = req3;
                                String str10 = issuerId;
                                List<ApduCommand> list9 = apduCommandList;
                                ServerAccessApplyAPDURequest serverAccessApplyAPDURequest7 = serverAccessApplyAPDURequest6;
                                TaskResult<ChannelID> taskResult5 = result;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } catch (Throwable th8) {
                            th = th8;
                            String str11 = srcTransactionID;
                            taskResult = result;
                            serverAccessApplyAPDURequest2 = req3;
                            String str12 = issuerId;
                            List<ServerAccessAPDU> list10 = apduList2;
                            ServerAccessApplyAPDURequest req6 = serverAccessApplyAPDURequest2;
                            TaskResult<ChannelID> result4 = taskResult;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                        try {
                            req.setSrcTransactionID(srcTransactionID);
                            ServerAccessApplyAPDUResponse res = this.cardServer.applyAPDU(req);
                            if (res != null) {
                                LogX.i("InitAccessCardOperator executeCommand, apply apdu response = " + res.returnCode);
                                if (res.returnCode == 0) {
                                    List<ServerAccessAPDU> apduList4 = res.getApduList();
                                    try {
                                        nextStep2 = res.getNextStep();
                                        if (apduList4 == null || apduList4.isEmpty()) {
                                            flag = false;
                                        } else {
                                            flag = flag2;
                                        }
                                        req3 = req;
                                        apduList2 = apduList4;
                                        req2 = flag;
                                        apduCommandList2 = apduCommandList;
                                        channel2 = channel3;
                                        result3 = result2;
                                    } catch (Throwable th9) {
                                        th = th9;
                                        List<ServerAccessAPDU> list11 = apduList4;
                                        List<ApduCommand> list12 = apduCommandList;
                                        TaskResult<ChannelID> taskResult6 = result2;
                                        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest8 = req;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                } else {
                                    throw new AccessCardOperatorException();
                                }
                            } else {
                                LogX.e("InitAccessCardOperator executeCommand, invalid apply apdu response");
                                throw new AccessCardOperatorException();
                            }
                        } catch (Throwable th10) {
                            th = th10;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        String str13 = srcTransactionID;
                        taskResult = result;
                        serverAccessApplyAPDURequest2 = req3;
                        String str14 = issuerId;
                        List<ServerAccessAPDU> list13 = apduList2;
                        ChannelID channelID3 = channel2;
                        ServerAccessApplyAPDURequest req62 = serverAccessApplyAPDURequest2;
                        TaskResult<ChannelID> result42 = taskResult;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            } catch (Throwable th12) {
                th = th12;
                String str15 = issuerId;
                List<ServerAccessAPDU> list14 = apduList;
                ChannelID channelID4 = channel;
                String str16 = nextStep;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    private void handleInitSuccess(String appletAid, String uid) throws AccessCardOperatorException {
        if (deleteTA(appletAid) == 0) {
            LogX.i("InitAccessCardOperator delete accesscard OK----");
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
                LogX.e("InitAccessCardOperator changeServerAccessAPDU2ApduCommand, invalid apduId");
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
                    LogX.e("InitAccessCardOperator changeServerAccessAPDU2ApduCommand, NumberFormatException");
                }
            }
        }
        return apduCommandList;
    }

    private int deleteTA(String aid) throws AccessCardOperatorException {
        if (WalletTaManager.getInstance(this.mContext).getCardInfoByAid(aid) != null) {
            boolean isRemoveSuccess = false;
            try {
                WalletTaManager.getInstance(this.mContext).removeCardByAid(aid);
                isRemoveSuccess = true;
            } catch (WalletTaException.WalletTaCardNotExistException e) {
                LogX.e("WalletTaCardNotExistException ", e.getMessage());
                isRemoveSuccess = true;
            } catch (WalletTaException.WalletTaSystemErrorException e2) {
                LogX.e("WalletTaSystemErrorException ", e2.getMessage());
                ErrorInfo errorInfo = new ErrorInfo();
                errorInfo.setDisplayOverview("WalletTaSystemErrorException");
                errorInfo.setDisplayDetail(e2.getMessage());
                errorInfo.setSuggestion("ee");
            }
            if (isRemoveSuccess) {
                return 0;
            }
            throw new AccessCardOperatorException();
        }
        LogX.i(TAG, aid + " ta not exsit");
        throw new AccessCardOperatorException();
    }

    private void handleResult(int result) {
        if (this.handle != null) {
            this.handle.handleResult(result);
        }
    }
}
