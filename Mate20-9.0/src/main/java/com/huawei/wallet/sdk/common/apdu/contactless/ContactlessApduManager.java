package com.huawei.wallet.sdk.common.apdu.contactless;

import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import java.util.List;

public class ContactlessApduManager implements IAPDUService {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile ContactlessApduManager instance;
    private final ExcuteApduContactlessService mExcuteApduContactlessService = new ExcuteApduContactlessService();

    private ContactlessApduManager() {
    }

    public static ContactlessApduManager getInstance() {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new ContactlessApduManager();
                }
            }
        }
        return instance;
    }

    public TaskResult<Integer> getReaderId(int mediaType) {
        return null;
    }

    public TaskResult<ChannelID> excuteApduList(List<ApduCommand> apdus, ChannelID channelId) {
        TaskResult<ChannelID> excuteApduList;
        synchronized (CONTACTLESSC_LOCK) {
            excuteApduList = this.mExcuteApduContactlessService.excuteApduList(apdus, channelId);
        }
        return excuteApduList;
    }

    public TaskResult<Integer> closeChannel(ChannelID channelID) {
        return null;
    }

    public TaskResult<Integer> closeAllChannel() {
        return null;
    }

    public TaskResult<Integer> closeSEService() {
        return null;
    }

    public void setProcessPrefix(String processPrefix, String tag) {
    }

    public void resetProcessPrefix() {
    }
}
