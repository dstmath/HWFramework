package com.huawei.wallet.sdk.common.apdu.oma;

import android.content.Context;
import android.se.omapi.Reader;
import android.se.omapi.SEService;
import android.se.omapi.Session;
import android.text.TextUtils;
import android.util.SparseArray;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.OmaException;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.apdu.util.OmaUtil;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.NfcUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class OmaService extends WalletProcessTrace implements SEService.OnConnectedListener {
    private static final String REPORT_OPEN_OR_CLOSE_FAIRUE = "open_or_close_channel";
    private static final String SELECT_COMMANDER = "00A40400";
    private static final String TAG = "OmaService|";
    private static final Object serviceLock = new Object();
    private NfcChannelContainer channelContainer;
    private Context context;
    private SEService sEService;
    private SparseArray<NfcReaderObj> seReaders = new SparseArray<>();
    private SparseArray<Session> sessions = new SparseArray<>();

    OmaService(Context context2, NfcChannelContainer channelContainer2) {
        this.context = context2;
        this.channelContainer = channelContainer2;
        getSeService();
    }

    private void getSeService() {
        if (this.sEService == null) {
            synchronized (serviceLock) {
                this.sEService = new SEService(this.context, Executors.newSingleThreadExecutor(), this);
            }
        }
        if (!this.sEService.isConnected()) {
            synchronized (this) {
                try {
                    if (!this.sEService.isConnected()) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    LogC.e(getSubProcessPrefix() + "OmaService init SEService, se service lock wait interrupted.", false);
                }
            }
        }
    }

    public void onConnected() {
        synchronized (serviceLock) {
            synchronized (this) {
                if (this.sEService == null) {
                    LogC.e(getSubProcessPrefix() + "|onConnectedListener|sEService=null", false);
                } else if (!this.sEService.isConnected()) {
                    LogC.e(getSubProcessPrefix() + "|onConnectedListener|isConnected=false", false);
                } else {
                    LogC.i(getSubProcessPrefix() + "OmaService onConnectedListener", false);
                    notifyAll();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public TaskResult<Integer> getReaderId(int mediaType) {
        TaskResult result = new TaskResult();
        try {
            NfcReaderObj target = getReader(mediaType);
            if (target == null) {
                result.setResultCode(1002);
                result.setMsg("OmaService getReaderId target is null");
                return result;
            }
            result.setData(Integer.valueOf(target.getIdx()));
            return result;
        } catch (OmaException e) {
            result.setResultCode(e.getErrorCode());
            result.setMsg(e.getMessage());
            return result;
        }
    }

    /* access modifiers changed from: package-private */
    public TaskResult<ChannelID> excuteApduList(List<ApduCommand> apdus, ChannelID channelId) {
        ChannelID channelId2;
        int resultCode;
        ChannelID channelId3 = channelId;
        TaskResult result = new TaskResult();
        int i = false;
        if (apdus != null) {
            if (!apdus.isEmpty()) {
                if (channelId3 == null) {
                    channelId2 = new ChannelID();
                    ChannelID channelID = channelId2;
                } else {
                    channelId2 = channelId3;
                }
                NfcChannel channel = this.channelContainer.pullChannel(channelId2);
                resetApduCommondStatus(apdus);
                NfcChannel channel2 = channel;
                for (ApduCommand command : apdus) {
                    result.setLastExcutedCommand(command);
                    String apdu = command.getApdu();
                    boolean needCheckSw = true;
                    if (StringUtil.isEmpty(apdu, true)) {
                        return setResult(result, channelId2, 1001, "OmaService apdu of command is null");
                    }
                    if (apdu.toUpperCase(Locale.getDefault()).startsWith(SELECT_COMMANDER)) {
                        int base = SELECT_COMMANDER.length();
                        int base2 = base + 2;
                        String aid = apdu.substring(base2, (Integer.parseInt(apdu.substring(base, base + 2), 16) * 2) + base2);
                        closeAllChannel();
                        channelId2.setAid(aid);
                        try {
                            channel2 = fetchChannel(aid, channelId2.getChannelType(), channelId2.getMediaType());
                            String selectResp = channel2.getSelectResp();
                            command.parseRapduAndSw(selectResp);
                            String str = selectResp;
                            resultCode = i;
                            this.channelContainer.setProcessPrefix(getProcessPrefix(), null);
                            this.channelContainer.pushChannel(channelId2, channel2);
                            this.channelContainer.resetProcessPrefix();
                        } catch (OmaException e) {
                            int i2 = i;
                            int resultCode2 = e.getErrorCode();
                            String msg = "excuteApduList fetchChannel failed. " + e.getMessage();
                            command.setRapdu(e.getRapdu());
                            command.parseRapduAndSw(e.getRapdu());
                            return setResult(result, channelId2, resultCode2, msg);
                        }
                    } else {
                        resultCode = i;
                        if (channel2 == null) {
                            try {
                                closeAllChannel();
                                NfcChannel channel3 = fetchChannel(channelId2.getAid(), channelId2.getChannelType(), channelId2.getMediaType());
                                this.channelContainer.setProcessPrefix(getProcessPrefix(), null);
                                this.channelContainer.pushChannel(channelId2, channel3);
                                this.channelContainer.resetProcessPrefix();
                                channel2 = channel3;
                            } catch (OmaException e2) {
                                int resultCode3 = e2.getErrorCode();
                                String msg2 = "excuteApduList fetchChannel2 failed. " + e2.getMessage();
                                command.setRapdu(e2.getRapdu());
                                return setResult(result, channelId2, resultCode3, msg2);
                            }
                        }
                        try {
                            channel2.setProcessPrefix(getProcessPrefix(), null);
                            String resp = channel2.excuteApdu(apdu);
                            channel2.resetProcessPrefix();
                            if (StringUtil.isEmpty(resp, true) || resp.length() < 4) {
                                command.setRapdu(resp);
                                return setResult(result, channelId2, 4001, "excuteApduList excuteApdu[" + OmaUtil.getLogApdu(apdu) + "] failed. rapdu is small. resp : " + resp);
                            }
                            command.parseRapduAndSw(resp);
                            String checker = command.getChecker();
                            String upperCaseSw = command.getSw().toUpperCase(Locale.getDefault());
                            if (checker == null) {
                                needCheckSw = false;
                            }
                            if (needCheckSw && !upperCaseSw.matches(command.getChecker())) {
                                result.setLastExcutedCommand(command);
                                return setResult(result, channelId2, 4002, "excuteApduList excuteApdu failed. sw is not matched. rapdu : " + resp + " sw : " + command.getSw() + " checker : " + command.getChecker() + " apdu index : " + command.getIndex() + " apdu[" + OmaUtil.getLogApdu(apdu) + "]");
                            }
                        } catch (OmaException e3) {
                            return setResult(result, channelId2, e3.getErrorCode(), "excuteApduList excuteApdu failed. apdu index : " + command.getIndex() + e3.getMessage());
                        }
                    }
                    i = resultCode;
                }
                int i3 = i;
                result.setData(channelId2);
                return result;
            }
        }
        return setResult(result, channelId3, 1004, "OmaService excuteApduList failed.capdu is empty");
    }

    private TaskResult<ChannelID> setResult(TaskResult<ChannelID> result, ChannelID channelID, int resultCode, String msg) {
        result.setData(channelID);
        result.setResultCode(resultCode);
        result.setMsg(msg);
        return result;
    }

    /* access modifiers changed from: package-private */
    public TaskResult<Integer> closeChannel(ChannelID channelID) {
        TaskResult<Integer> result = new TaskResult<>();
        NfcChannel channel = this.channelContainer.removeChannel(channelID);
        if (channel != null) {
            try {
                channel.setProcessPrefix(getProcessPrefix(), null);
                channel.closeChannel();
                channel.resetProcessPrefix();
                LogC.i(getSubProcessPrefix() + IAPDUService.TAG + " OmaService close channel success.", false);
            } catch (OmaException e) {
                String msg = getSubProcessPrefix() + IAPDUService.TAG + " OmaService closeChannel failed, " + e.getMessage();
                LogC.e(msg, false);
                result.setResultCode(e.getErrorCode());
                result.setMsg(msg);
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public TaskResult<Integer> closeAllChannel() {
        TaskResult<Integer> result = new TaskResult<>();
        for (int i = 0; i < this.sessions.size(); i++) {
            try {
                Session session = this.sessions.valueAt(i);
                session.closeChannels();
                if (!session.isClosed()) {
                    session.close();
                }
            } catch (Exception e) {
                LogC.e(getProcessPrefix() + msg, false);
                result.setResultCode(5001);
                result.setMsg("OmaService closeAllChannel exception happened, size " + this.sessions.size() + " ,current " + i + " ,desc " + e.getMessage());
            }
        }
        LogC.i(getSubProcessPrefix() + IAPDUService.TAG + " Close all channel and session end, session size: " + this.sessions.size(), false);
        this.channelContainer.clearChannels();
        this.sessions.clear();
        this.seReaders.clear();
        return result;
    }

    /* access modifiers changed from: package-private */
    public TaskResult<Integer> closeSEService() {
        TaskResult<Integer> result = new TaskResult<>();
        closeAllChannel();
        try {
            if (this.sEService != null && this.sEService.isConnected()) {
                this.sEService.shutdown();
                this.sEService = null;
            }
        } catch (Exception e) {
            LogC.e("OmaService close SEService exception happened.", false);
            result.setResultCode(5002);
            result.setMsg("OmaService close SEService exception happened.");
        }
        LogC.i(getSubProcessPrefix() + IAPDUService.TAG + " close SEService end.", false);
        this.sEService = null;
        return result;
    }

    private NfcChannel fetchChannel(String aid, int channelType, int mediaType) throws OmaException {
        String msg;
        boolean z = true;
        if (!StringUtil.isEmpty(aid, true)) {
            if (this.sEService == null || !this.sEService.isConnected()) {
                StringBuilder sb = new StringBuilder();
                sb.append(getSubProcessPrefix());
                sb.append(IAPDUService.TAG);
                sb.append(" fetchChannel old SEService invalid need rebind. disconnected[");
                if (this.sEService == null) {
                    z = false;
                }
                sb.append(z);
                sb.append("]");
                LogC.i(sb.toString(), false);
                closeAllChannel();
                this.sEService = null;
            }
            try {
                Session session = this.sessions.get(mediaType);
                if (session == null) {
                    NfcReaderObj targetReader = getReader(mediaType);
                    if (targetReader != null) {
                        session = targetReader.getReader().openSession();
                    } else {
                        throw new OmaException(1002, "fetchChannel get InfoReader failed,for mediaType " + mediaType);
                    }
                }
                if (session != null) {
                    this.sessions.put(mediaType, session);
                    LogC.i(getSubProcessPrefix() + "Open channel, channelType " + channelType + " ,mediaType " + mediaType, false);
                    NfcChannel channel = new NfcChannel(session);
                    channel.setAid(aid);
                    channel.setChannelType(channelType);
                    channel.setProcessPrefix(getProcessPrefix(), null);
                    channel.openChannel();
                    channel.resetProcessPrefix();
                    return channel;
                }
                throw new OmaException(2001, "fetchChannel openSession failed,for mediaType " + mediaType);
            } catch (IOException e) {
                throw new OmaException(2001, "fetchChannel openSession failed,for mediaType " + mediaType);
            } catch (IllegalArgumentException e2) {
                throw new OmaException(1001, "fetchChannel openSession failed,for mediaType " + mediaType);
            } catch (OmaException e3) {
                String msg2 = e3.getMessage();
                OmaException omaException = new OmaException(e3.getErrorCode(), msg2 + ". isEnabledNFC[" + NfcUtil.isEnabledNFC(this.context) + "]");
                if (!TextUtils.isEmpty(e3.getRapdu())) {
                    omaException.setRapdu(e3.getRapdu());
                }
                throw omaException;
            } catch (Throwable t) {
                LogC.e(getSubProcessPrefix() + "ThrowableError: " + msg, false);
                throw new OmaException(IAPDUService.RETURN_APDU_EXCUTE_UNKNOWN_ERROR, msg);
            }
        } else {
            throw new OmaException(2005, "open channel failed. aid is null");
        }
    }

    private NfcReaderObj getReader(int mediaType) throws OmaException {
        NfcReaderObj target = this.seReaders.get(mediaType);
        if (target != null) {
            return target;
        }
        if (this.sEService == null || !this.sEService.isConnected()) {
            getSeService();
        }
        int i = 0;
        if (this.sEService == null) {
            LogC.e(getSubProcessPrefix() + "OmaService getReader sEService is null", false);
            return null;
        }
        try {
            Reader[] readers = this.sEService.getReaders();
            if (readers == null || readers.length <= 0) {
                throw new OmaException(1002, "OmaService getReader failed no readers.");
            }
            this.seReaders.clear();
            while (true) {
                int i2 = i;
                if (i2 >= readers.length) {
                    return this.seReaders.get(mediaType);
                }
                sortsReader(i2, readers[i2]);
                i = i2 + 1;
            }
        } catch (IllegalStateException e) {
            throw new OmaException(1003, "OmaService getReader failed IllegalStateException.");
        } catch (RuntimeException e2) {
            throw new OmaException(1003, "OmaService getReader failed IllegalStateException.");
        }
    }

    private void sortsReader(int i, Reader reader) {
        if (reader == null) {
            LogC.e("OmaService getReader reader is null" + i, false);
            return;
        }
        if (reader.getName().contains("eSE") && !reader.getName().contains("eSE2")) {
            this.seReaders.put(0, new NfcReaderObj(i, reader));
        } else if (reader.getName().contains("SD")) {
            this.seReaders.put(2, new NfcReaderObj(i, reader));
        } else if (reader.getName().contains("SIM")) {
            this.seReaders.put(1, new NfcReaderObj(i, reader));
        } else if (reader.getName().contains("eSE2")) {
            this.seReaders.put(3, new NfcReaderObj(i, reader));
        }
    }

    private void resetApduCommondStatus(List<ApduCommand> commands) {
        for (ApduCommand command : commands) {
            command.setRapdu("");
            command.setSw("");
        }
    }

    public void setProcessPrefix(String processPrefix, String tag) {
        super.setProcessPrefix(processPrefix, TAG);
    }
}
