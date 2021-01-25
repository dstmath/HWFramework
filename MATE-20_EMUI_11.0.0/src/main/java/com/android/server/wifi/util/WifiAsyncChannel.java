package com.android.server.wifi.util;

import android.os.Message;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiLog;

public class WifiAsyncChannel extends AsyncChannel {
    private static final String LOG_TAG = "WifiAsyncChannel";
    private WifiLog mLog;
    private String mTag;

    public WifiAsyncChannel(String serviceTag) {
        this.mTag = "WifiAsyncChannel." + serviceTag;
    }

    private WifiLog getOrInitLog() {
        if (this.mLog == null) {
            this.mLog = WifiInjector.getInstance().makeLog(this.mTag);
        }
        return this.mLog;
    }

    public void sendMessage(Message msg) {
        getOrInitLog().trace("sendMessage message=%").c((long) msg.what).flush();
        WifiAsyncChannel.super.sendMessage(msg);
    }

    public void replyToMessage(Message srcMsg, Message dstMsg) {
        getOrInitLog().trace("replyToMessage recvdMessage=% sendingUid=% sentMessage=%").c((long) srcMsg.what).c((long) srcMsg.sendingUid).c((long) dstMsg.what).flush();
        WifiAsyncChannel.super.replyToMessage(srcMsg, dstMsg);
    }

    public Message sendMessageSynchronously(Message msg) {
        getOrInitLog().trace("sendMessageSynchronously.send message=%").c((long) msg.what).flush();
        Message replyMessage = WifiAsyncChannel.super.sendMessageSynchronously(msg);
        if (replyMessage != null) {
            getOrInitLog().trace("sendMessageSynchronously.recv message=% sendingUid=%").c((long) replyMessage.what).c((long) replyMessage.sendingUid).flush();
        }
        return replyMessage;
    }

    @VisibleForTesting
    public void setWifiLog(WifiLog log) {
        this.mLog = log;
    }
}
