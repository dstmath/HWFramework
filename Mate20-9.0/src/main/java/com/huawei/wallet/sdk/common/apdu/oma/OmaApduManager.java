package com.huawei.wallet.sdk.common.apdu.oma;

import android.content.Context;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import java.util.List;

public final class OmaApduManager implements IAPDUService {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile OmaApduManager instance;
    private OmaService mOmaService;
    private String processPrefix = "";

    private OmaApduManager(Context context) {
        this.mOmaService = new OmaService(context, NfcChannelContainer.getInstance());
    }

    public static OmaApduManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new OmaApduManager(context);
                }
            }
        }
        return instance;
    }

    public TaskResult<Integer> getReaderId(int mediaType) {
        TaskResult<Integer> readerId;
        synchronized (OMA_ACCESS_SYNC_LOCK) {
            readerId = this.mOmaService.getReaderId(mediaType);
        }
        return readerId;
    }

    public TaskResult<ChannelID> excuteApduList(List<ApduCommand> apdus, ChannelID channelId) {
        TaskResult<ChannelID> taskResult;
        synchronized (OMA_ACCESS_SYNC_LOCK) {
            this.mOmaService.setProcessPrefix(this.processPrefix, null);
            taskResult = this.mOmaService.excuteApduList(apdus, channelId);
            this.mOmaService.resetProcessPrefix();
        }
        return taskResult;
    }

    public TaskResult<Integer> closeChannel(ChannelID channelID) {
        TaskResult<Integer> taskResult;
        synchronized (OMA_ACCESS_SYNC_LOCK) {
            this.mOmaService.setProcessPrefix(this.processPrefix, null);
            taskResult = this.mOmaService.closeChannel(channelID);
            this.mOmaService.resetProcessPrefix();
        }
        return taskResult;
    }

    public TaskResult<Integer> closeAllChannel() {
        TaskResult<Integer> taskResult;
        synchronized (OMA_ACCESS_SYNC_LOCK) {
            this.mOmaService.setProcessPrefix(this.processPrefix, null);
            taskResult = this.mOmaService.closeAllChannel();
            this.mOmaService.resetProcessPrefix();
        }
        return taskResult;
    }

    public TaskResult<Integer> closeSEService() {
        TaskResult<Integer> taskResult;
        synchronized (OMA_ACCESS_SYNC_LOCK) {
            this.mOmaService.setProcessPrefix(this.processPrefix, null);
            taskResult = this.mOmaService.closeSEService();
            this.mOmaService.resetProcessPrefix();
        }
        return taskResult;
    }

    public void setProcessPrefix(String processPrefix2, String tag) {
        this.processPrefix = processPrefix2;
    }

    public void resetProcessPrefix() {
        this.processPrefix = "";
    }
}
