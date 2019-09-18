package com.huawei.wallet.sdk.common.apdu.oma;

import com.huawei.wallet.sdk.common.apdu.OmaException;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;
import com.huawei.wallet.sdk.common.log.LogC;
import java.util.HashMap;

public final class NfcChannelContainer extends WalletProcessTrace {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "NfcChannelContainer|";
    private static volatile NfcChannelContainer instance;
    private HashMap<ChannelID, NfcChannel> channels = new HashMap<>();

    private NfcChannelContainer() {
    }

    static NfcChannelContainer getInstance() {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new NfcChannelContainer();
                }
            }
        }
        return instance;
    }

    /* access modifiers changed from: package-private */
    public void pushChannel(ChannelID channelId, NfcChannel channel) {
        NfcChannel oldChannel = this.channels.get(channelId);
        if (oldChannel != null) {
            try {
                oldChannel.setProcessPrefix(getProcessPrefix(), null);
                oldChannel.closeChannel();
                oldChannel.resetProcessPrefix();
            } catch (OmaException e) {
                LogC.e("pushChannel close old channel failed. msg " + e.getMessage(), false);
            }
        }
        this.channels.put(channelId, channel);
    }

    /* access modifiers changed from: package-private */
    public NfcChannel pullChannel(ChannelID channelId) {
        return this.channels.get(channelId);
    }

    /* access modifiers changed from: package-private */
    public NfcChannel removeChannel(ChannelID channelId) {
        NfcChannel channel = this.channels.get(channelId);
        if (channel != null) {
            this.channels.remove(channelId);
        }
        return channel;
    }

    /* access modifiers changed from: package-private */
    public void clearChannels() {
        this.channels.clear();
    }

    public void setProcessPrefix(String processPrefix, String tag) {
        super.setProcessPrefix(processPrefix, TAG);
    }
}
