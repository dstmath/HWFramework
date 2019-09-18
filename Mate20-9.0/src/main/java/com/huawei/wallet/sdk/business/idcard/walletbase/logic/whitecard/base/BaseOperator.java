package com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.base;

import android.content.Context;
import android.os.Build;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.server.card.CommonCardServer;
import com.huawei.wallet.sdk.business.idcard.walletbase.pass.PassTypeIdInfoFetchTask;
import com.huawei.wallet.sdk.business.idcard.walletbase.pass.PassTypeIdInfoRequest;
import com.huawei.wallet.sdk.business.idcard.walletbase.pass.PassTypeIdInfoResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.whitecard.BaseResultHandler;
import com.huawei.wallet.sdk.business.idcard.walletbase.whitecard.WhitecardServer;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.ese.ESEApiFactory;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.oma.OmaApduManager;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.ErrorInfoCreator;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BaseOperator {
    private static final String TAG = "BaseOperator";
    protected CommonCardServer cardServer;
    protected BaseResultHandler handle;
    protected Context mContext;
    protected IAPDUService omaService = null;
    protected WhitecardServer whitecardServer;

    public BaseOperator(Context context, BaseResultHandler handle2) {
        this.mContext = context;
        this.handle = handle2;
        this.whitecardServer = new WhitecardServer(context);
        this.cardServer = new CommonCardServer(this.mContext);
        this.omaService = OmaApduManager.getInstance(this.mContext);
    }

    /* access modifiers changed from: protected */
    public List<ServerAccessAPDU> changeApduCommand2ServerAccessAPDU(List<ApduCommand> apduCommandList, ApduCommand lastApduCommand) {
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

    /* access modifiers changed from: protected */
    public List<ApduCommand> changeServerAccessAPDU2ApduCommand(List<ServerAccessAPDU> apduList) {
        List<ApduCommand> apduCommandList = new ArrayList<>();
        for (ServerAccessAPDU apdu : apduList) {
            String apduId = apdu.getApduId();
            if (StringUtil.isEmpty(apduId, true)) {
                LogC.e("ServerAccessServiceImpl changeServerAccessAPDU2ApduCommand, invalid apduId", false);
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
                    LogC.e("ServerAccessServiceImpl changeServerAccessAPDU2ApduCommand, NumberFormatException", false);
                }
            }
        }
        return apduCommandList;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:51:?, code lost:
        handleResult(r3.returnCode, r3.getErrorInfo());
     */
    public boolean executeCommand(Context mContext2, String transactionId, List<ServerAccessAPDU> apduList, int resultCode, String passTypeGroup, String appletAid, String nextStep) {
        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest;
        List<ApduCommand> apduCommandList;
        TaskResult<ChannelID> taskResult;
        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest2;
        ChannelID channelID;
        List<ServerAccessAPDU> apduList2;
        HashMap hashMap;
        ServerAccessApplyAPDURequest req;
        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest3;
        ChannelID channel;
        TaskResult<ChannelID> taskResult2;
        String str = transactionId;
        if (StringUtil.isEmpty(str, true) || apduList == null || apduList.isEmpty()) {
            LogC.e(TAG, "executeCommand, invalid param", false);
            handleResult(1, ErrorInfoCreator.buildSimpleErrorInfo(1));
            return false;
        }
        ChannelID channel2 = null;
        synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
            try {
                String deviceModel = Build.MODEL;
                String cplc = ESEApiFactory.createESEInfoManagerApi(mContext2).queryCplc();
                List<ServerAccessAPDU> apduList3 = apduList;
                String nextStep2 = nextStep;
                boolean result = false;
                ServerAccessApplyAPDURequest req2 = null;
                String seChipManuFacturer = ProductConfigUtil.geteSEManufacturer();
                while (true) {
                    try {
                        apduCommandList = changeServerAccessAPDU2ApduCommand(apduList3);
                        try {
                            taskResult = this.omaService.excuteApduList(apduCommandList, channel2);
                        } catch (Throwable th) {
                            th = th;
                            serverAccessApplyAPDURequest = req2;
                            List<ServerAccessAPDU> list = apduList3;
                            boolean z = result;
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
                        serverAccessApplyAPDURequest = req2;
                        List<ServerAccessAPDU> list3 = apduList3;
                        boolean z2 = result;
                        ServerAccessApplyAPDURequest req32 = serverAccessApplyAPDURequest;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                    try {
                        ChannelID channel3 = taskResult.getData();
                        try {
                            LogC.i(TAG, "executeCommand, oma execute command, " + taskResult.getPrintMsg(), false);
                            apduList2 = changeApduCommand2ServerAccessAPDU(apduCommandList, taskResult.getLastExcutedCommand());
                            try {
                                hashMap = new HashMap();
                                hashMap.put(ServerAccessApplyAPDURequest.ReqKey.AID, appletAid);
                                req = req2;
                                try {
                                    hashMap.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, passTypeGroup);
                                    hashMap.put("cplc", cplc);
                                    serverAccessApplyAPDURequest3 = serverAccessApplyAPDURequest3;
                                    HashMap hashMap2 = hashMap;
                                    channel = channel3;
                                    taskResult2 = taskResult;
                                } catch (Throwable th4) {
                                    th = th4;
                                    TaskResult<ChannelID> taskResult3 = taskResult;
                                    ChannelID channelID2 = channel3;
                                    boolean z3 = result;
                                    List<ApduCommand> list4 = apduCommandList;
                                    ServerAccessApplyAPDURequest serverAccessApplyAPDURequest4 = req;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                TaskResult<ChannelID> taskResult4 = taskResult;
                                serverAccessApplyAPDURequest2 = req2;
                                channelID = channel3;
                                ChannelID channel4 = channelID;
                                List<ApduCommand> list5 = apduCommandList;
                                ServerAccessApplyAPDURequest req4 = serverAccessApplyAPDURequest2;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            TaskResult<ChannelID> taskResult5 = taskResult;
                            serverAccessApplyAPDURequest2 = req2;
                            channelID = channel3;
                            List<ServerAccessAPDU> list6 = apduList3;
                            ChannelID channel42 = channelID;
                            List<ApduCommand> list52 = apduCommandList;
                            ServerAccessApplyAPDURequest req42 = serverAccessApplyAPDURequest2;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                        try {
                            serverAccessApplyAPDURequest3 = new ServerAccessApplyAPDURequest(str, hashMap, apduList2.size(), apduList2, deviceModel, seChipManuFacturer);
                            ServerAccessApplyAPDURequest req5 = serverAccessApplyAPDURequest3;
                            try {
                                req5.setCurrentStep(nextStep2);
                                req5.setSn(PhoneDeviceUtil.getSerialNumber());
                                req5.setSrcTransactionID(str);
                                ServerAccessApplyAPDUResponse res = this.cardServer.applyAPDU(req5);
                                if (res == null) {
                                    break;
                                }
                                LogC.i(TAG, "executeCommand, apply apdu response = " + res.returnCode, false);
                                if (res.returnCode != 0) {
                                    break;
                                }
                                List<ServerAccessAPDU> apduList4 = res.getApduList();
                                try {
                                    nextStep2 = res.getNextStep();
                                    if (apduList4 == null || apduList4.isEmpty()) {
                                        result = true;
                                    }
                                    if (apduList4 == null || apduList4.isEmpty()) {
                                        List<ServerAccessAPDU> list7 = apduList4;
                                    } else {
                                        apduList3 = apduList4;
                                        channel2 = channel;
                                        List<ApduCommand> list8 = apduCommandList;
                                        TaskResult<ChannelID> taskResult6 = taskResult2;
                                        req2 = req5;
                                    }
                                } catch (Throwable th7) {
                                    th = th7;
                                    ServerAccessApplyAPDURequest serverAccessApplyAPDURequest5 = req5;
                                    List<ServerAccessAPDU> list9 = apduList4;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th8) {
                                th = th8;
                                ServerAccessApplyAPDURequest serverAccessApplyAPDURequest6 = req5;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } catch (Throwable th9) {
                            th = th9;
                            ChannelID channelID3 = channel;
                            boolean z4 = result;
                            List<ApduCommand> list10 = apduCommandList;
                            ServerAccessApplyAPDURequest serverAccessApplyAPDURequest7 = req;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th10) {
                        th = th10;
                        TaskResult<ChannelID> taskResult7 = taskResult;
                        List<ServerAccessAPDU> list11 = apduList3;
                        boolean z5 = result;
                        List<ApduCommand> list12 = apduCommandList;
                        ServerAccessApplyAPDURequest serverAccessApplyAPDURequest8 = req2;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
                this.omaService.closeChannel(channel);
                return result;
            } catch (Throwable th11) {
                th = th11;
                List<ServerAccessAPDU> list13 = apduList;
                String str2 = nextStep;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleResult(int result, ErrorInfo info) {
        if (this.handle != null) {
            this.handle.handleResult(result, info);
        }
    }

    /* access modifiers changed from: protected */
    public String getPassTypeGroup(String passTypeId) {
        PassTypeIdInfoFetchTask fetchTask = new PassTypeIdInfoFetchTask(this.mContext, AddressNameMgr.getInstance().getAddress("query.pass.type", AddressNameMgr.MODULE_NAME_WALLETPASS, null, this.mContext));
        PassTypeIdInfoRequest request = new PassTypeIdInfoRequest();
        request.setPassTypeId(passTypeId);
        PassTypeIdInfoResponse passTypeIdInfoResponse = (PassTypeIdInfoResponse) fetchTask.processTask(request);
        if (passTypeIdInfoResponse == null || passTypeIdInfoResponse.getReturnCode() != 0) {
            LogC.e(TAG, "getPassTypeGroup ,response is null or response.getReturnCode() is not successful", false);
            return "";
        }
        LogC.i(TAG, "getPassTypeGroup successful:" + passTypeIdInfoResponse.getPassTypeGroup(), false);
        return passTypeIdInfoResponse.getPassTypeGroup();
    }
}
