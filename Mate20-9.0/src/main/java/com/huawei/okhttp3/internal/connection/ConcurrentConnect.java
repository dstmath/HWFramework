package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.internal.platform.Platform;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

final class ConcurrentConnect {
    private final ArrayList<InetSocketAddress> addressList;
    private int attemptDelayMs;
    private volatile boolean cancelled = false;
    private final ArrayList<ChannelWrapper> channelList = new ArrayList<>();
    private final ArrayList<InetSocketAddress> failedAddressList = new ArrayList<>();
    private Selector selector;

    private final class ChannelWrapper {
        SocketChannel channel;
        long expiredTimeStamp;
        InetSocketAddress inetSocketAddress;

        private ChannelWrapper() {
        }

        /* access modifiers changed from: package-private */
        public void open(InetSocketAddress inetSocketAddress2, long timeoutMs) throws IOException {
            this.inetSocketAddress = inetSocketAddress2;
            this.expiredTimeStamp = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) + timeoutMs;
            this.channel = SocketChannel.open();
            this.channel.configureBlocking(false);
            this.channel.connect(inetSocketAddress2);
        }

        /* access modifiers changed from: package-private */
        public void close() {
            try {
                this.channel.close();
            } catch (IOException e) {
                ConcurrentConnect.this.logMessage("Socket channel close error", e);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isExpired() {
            return TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) >= this.expiredTimeStamp;
        }

        /* access modifiers changed from: package-private */
        public long getRemainingMs() {
            long now = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            if (now >= this.expiredTimeStamp) {
                return 0;
            }
            return this.expiredTimeStamp - now;
        }
    }

    public ConcurrentConnect(ArrayList<InetSocketAddress> addressList2, int attemptDelayMs2) {
        this.addressList = new ArrayList<>(addressList2);
        this.attemptDelayMs = attemptDelayMs2;
    }

    public Socket getConnectedSocket(long timeoutMs) {
        SocketChannel channel = null;
        try {
            channel = getConnectedSocketChannel(timeoutMs);
            if (channel != null) {
                try {
                    channel.configureBlocking(true);
                } catch (IOException e) {
                    try {
                        channel.close();
                    } catch (IOException ce) {
                        logMessage("Socket channel close error", ce);
                    }
                    channel = null;
                }
            }
        } catch (ClosedSelectorException e2) {
            logMessage("Selector is already closed", e2);
        }
        clearResource();
        if (channel != null) {
            return channel.socket();
        }
        return null;
    }

    public ArrayList<InetSocketAddress> failedAddressList() {
        return this.failedAddressList;
    }

    public void cancel() {
        if (this.selector != null) {
            try {
                this.cancelled = true;
                this.selector.close();
            } catch (IOException e) {
                logMessage("Selector close error", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void logMessage(String message, Throwable e) {
        Platform.get().log(4, message, e);
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0083  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x009f A[SYNTHETIC] */
    private SocketChannel getConnectedSocketChannel(long timeoutMs) {
        SocketChannel selectedChannel;
        long wait;
        int addressListCount;
        int addressListCount2 = this.addressList.size();
        long fixTimeoutMs = timeoutMs;
        if (addressListCount2 == 0) {
            return null;
        }
        try {
            this.selector = Selector.open();
            long pendingAttemptDelayMs = (long) this.attemptDelayMs;
            long timeoutMs2 = timeoutMs - ((long) this.attemptDelayMs);
            SocketChannel selectedChannel2 = null;
            boolean toOpenChannel = true;
            while (true) {
                if (this.cancelled) {
                    selectedChannel = selectedChannel2;
                    break;
                } else if (this.addressList.size() <= 0 || !toOpenChannel) {
                    SocketChannel socketChannel = selectedChannel2;
                    wait = this.channelList.size() <= 0 ? this.channelList.get(0).getRemainingMs() : timeoutMs2;
                    if (this.addressList.size() > 0 && wait > pendingAttemptDelayMs) {
                        wait = pendingAttemptDelayMs;
                    }
                    long start = System.nanoTime();
                    try {
                        this.selector.select(wait);
                        if (this.cancelled) {
                            long timeElapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                            selectedChannel = findConnectedChannel();
                            if (selectedChannel == null) {
                                checkForTimeout(timeElapsed);
                                if (this.failedAddressList.size() == addressListCount2 || timeElapsed >= timeoutMs2) {
                                    break;
                                }
                                timeoutMs2 -= timeElapsed;
                                if (this.addressList.size() <= 0) {
                                    addressListCount = addressListCount2;
                                } else if (timeElapsed >= pendingAttemptDelayMs) {
                                    boolean z = toOpenChannel;
                                    addressListCount = addressListCount2;
                                    pendingAttemptDelayMs = ((long) this.attemptDelayMs) - ((timeElapsed - pendingAttemptDelayMs) % ((long) this.attemptDelayMs));
                                    toOpenChannel = true;
                                } else {
                                    boolean z2 = toOpenChannel;
                                    addressListCount = addressListCount2;
                                    pendingAttemptDelayMs -= timeElapsed;
                                }
                                selectedChannel2 = selectedChannel;
                                addressListCount2 = addressListCount;
                            } else {
                                break;
                            }
                        } else {
                            return null;
                        }
                    } catch (IOException e) {
                        boolean z3 = toOpenChannel;
                        int i = addressListCount2;
                        return null;
                    }
                } else {
                    InetSocketAddress inetSocketAddress = this.addressList.remove(0);
                    try {
                        prepareSocketChannel(inetSocketAddress, fixTimeoutMs);
                        SocketChannel socketChannel2 = selectedChannel2;
                        timeoutMs2 += (long) this.attemptDelayMs;
                        toOpenChannel = false;
                        if (this.channelList.size() <= 0) {
                        }
                        wait = pendingAttemptDelayMs;
                        long start2 = System.nanoTime();
                        this.selector.select(wait);
                        if (this.cancelled) {
                        }
                    } catch (IOException e2) {
                        logMessage("Failed to parepare socket channel for " + inetSocketAddress.toString(), e2);
                        this.failedAddressList.add(inetSocketAddress);
                        selectedChannel2 = selectedChannel2;
                    }
                }
            }
            int i2 = addressListCount2;
            return selectedChannel;
        } catch (IOException e3) {
            int i3 = addressListCount2;
            return null;
        }
    }

    private void prepareSocketChannel(InetSocketAddress inetSocketAddress, long fixTimeoutMs) throws IOException {
        ChannelWrapper channelWrapper = new ChannelWrapper();
        channelWrapper.open(inetSocketAddress, fixTimeoutMs);
        channelWrapper.channel.register(this.selector, 8).attach(channelWrapper);
        this.channelList.add(channelWrapper);
    }

    private void handleFailedChannel(ChannelWrapper channelWrapper) {
        this.failedAddressList.add(channelWrapper.inetSocketAddress);
        this.channelList.remove(channelWrapper);
        channelWrapper.close();
    }

    private void checkForTimeout(long timeElapsed) {
        while (this.channelList.size() > 0) {
            ChannelWrapper channelWrapper = this.channelList.get(0);
            if (channelWrapper.isExpired()) {
                handleFailedChannel(channelWrapper);
            } else {
                return;
            }
        }
    }

    private SocketChannel findConnectedChannel() {
        Iterator<SelectionKey> keyIterator = this.selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            if (key.isConnectable()) {
                ChannelWrapper channelWrapper = (ChannelWrapper) key.attachment();
                try {
                    SocketChannel channel = channelWrapper.channel;
                    if (channel.finishConnect()) {
                        key.cancel();
                        SocketChannel selectedChannel = channel;
                        this.channelList.remove(channelWrapper);
                        return selectedChannel;
                    }
                } catch (IOException e) {
                    key.cancel();
                    handleFailedChannel(channelWrapper);
                }
            }
        }
        return null;
    }

    private void clearResource() {
        Iterator<ChannelWrapper> it = this.channelList.iterator();
        while (it.hasNext()) {
            it.next().close();
        }
        this.channelList.clear();
        if (this.selector != null) {
            try {
                this.selector.close();
                this.selector = null;
            } catch (IOException e) {
                logMessage("Selector close error", e);
            }
        }
    }
}
