package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.EventListener;
import com.huawei.okhttp3.internal.platform.Platform;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/* access modifiers changed from: package-private */
public final class ConcurrentConnect {
    private final ArrayList<InetSocketAddress> addressList;
    private final int addressListCount;
    private int attemptDelayMs;
    private volatile boolean cancelled = false;
    private final ArrayList<ChannelWrapper> channelList = new ArrayList<>();
    private final ArrayList<InetSocketAddress> failedAddressList = new ArrayList<>();
    private Selector selector;

    public ConcurrentConnect(ArrayList<InetSocketAddress> addressList2, int attemptDelayMs2) {
        this.addressList = new ArrayList<>(addressList2);
        this.attemptDelayMs = attemptDelayMs2;
        this.addressListCount = addressList2.size();
    }

    public Socket getConnectedSocket(long connectTimeoutMs, Proxy proxy, Call call, EventListener eventListener) {
        try {
            this.selector = Selector.open();
            SocketChannel channel = null;
            try {
                channel = getConnectedSocketChannel(connectTimeoutMs, proxy, call, eventListener);
                if (channel != null) {
                    try {
                        channel.configureBlocking(true);
                    } catch (IOException e) {
                        try {
                            channel.close();
                        } catch (IOException ce) {
                            logMessage("Socket channel close error", ce);
                        } catch (Throwable th) {
                            throw th;
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
        } catch (IOException e3) {
            return null;
        }
    }

    public ArrayList<InetSocketAddress> failedAddressList() {
        return this.failedAddressList;
    }

    public void cancel() {
        Selector selector2 = this.selector;
        if (selector2 != null) {
            try {
                this.cancelled = true;
                selector2.close();
            } catch (IOException e) {
                logMessage("Selector close error", e);
            } catch (Throwable th) {
                this.selector = null;
                throw th;
            }
            this.selector = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logMessage(String message, Throwable e) {
        Platform.get().log(4, message, e);
    }

    private SocketChannel getConnectedSocketChannel(long connectTimeoutMs, Proxy proxy, Call call, EventListener eventListener) {
        boolean toOpenChannel;
        boolean toOpenChannel2;
        int i = this.attemptDelayMs;
        SocketChannel selectedChannel = null;
        long pendingAttemptDelayMs = (long) i;
        boolean toOpenChannel3 = true;
        long timeoutMs = connectTimeoutMs - ((long) i);
        while (true) {
            if (this.cancelled) {
                break;
            }
            if (this.addressList.size() <= 0 || !toOpenChannel3) {
                toOpenChannel = toOpenChannel3;
            } else if (!prepareSocketChannel(this.addressList.remove(0), connectTimeoutMs, proxy, call, eventListener)) {
                continue;
            } else {
                timeoutMs += (long) this.attemptDelayMs;
                toOpenChannel = false;
            }
            long wait = this.channelList.size() > 0 ? this.channelList.get(0).getRemainingMs() : timeoutMs;
            if (this.addressList.size() > 0 && wait > pendingAttemptDelayMs) {
                wait = pendingAttemptDelayMs;
            }
            long start = System.nanoTime();
            try {
                this.selector.select(wait);
                if (!this.cancelled) {
                    long timeElapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                    selectedChannel = findConnectedChannel();
                    if (selectedChannel == null) {
                        checkForTimeout(timeElapsed);
                        if (this.failedAddressList.size() == this.addressListCount) {
                            toOpenChannel2 = toOpenChannel;
                            break;
                        } else if (timeElapsed >= timeoutMs) {
                            toOpenChannel2 = toOpenChannel;
                            break;
                        } else {
                            timeoutMs -= timeElapsed;
                            if (this.addressList.size() <= 0) {
                                toOpenChannel3 = toOpenChannel;
                            } else if (timeElapsed >= pendingAttemptDelayMs) {
                                int i2 = this.attemptDelayMs;
                                toOpenChannel3 = true;
                                pendingAttemptDelayMs = ((long) i2) - ((timeElapsed - pendingAttemptDelayMs) % ((long) i2));
                            } else {
                                pendingAttemptDelayMs -= timeElapsed;
                                toOpenChannel3 = toOpenChannel;
                            }
                        }
                    } else {
                        toOpenChannel2 = toOpenChannel;
                        break;
                    }
                } else {
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
        }
        return selectedChannel;
    }

    private boolean prepareSocketChannel(InetSocketAddress inetSocketAddress, long fixTimeoutMs, Proxy proxy, Call call, EventListener eventListener) {
        ChannelWrapper channelWrapper = new ChannelWrapper();
        try {
            channelWrapper.open(inetSocketAddress, fixTimeoutMs, proxy, call, eventListener);
            channelWrapper.channel.register(this.selector, 8).attach(channelWrapper);
            this.channelList.add(channelWrapper);
            return true;
        } catch (IOException e) {
            logMessage("Failed to parepare socket channel for " + inetSocketAddress.toString(), e);
            this.failedAddressList.add(inetSocketAddress);
            channelWrapper.close();
            return false;
        }
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
                        this.channelList.remove(channelWrapper);
                        return channel;
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
        Selector selector2 = this.selector;
        if (selector2 != null) {
            try {
                selector2.close();
            } catch (IOException e) {
                logMessage("Selector close error", e);
            } catch (Throwable th) {
                this.selector = null;
                throw th;
            }
            this.selector = null;
        }
    }

    /* access modifiers changed from: private */
    public final class ChannelWrapper {
        SocketChannel channel;
        long expiredTimeStamp;
        InetSocketAddress inetSocketAddress;

        private ChannelWrapper() {
            this.channel = null;
            this.inetSocketAddress = null;
            this.expiredTimeStamp = 0;
        }

        /* access modifiers changed from: package-private */
        public void open(InetSocketAddress inetSocketAddress2, long timeoutMs, Proxy proxy, Call call, EventListener eventListener) throws IOException {
            this.inetSocketAddress = inetSocketAddress2;
            this.expiredTimeStamp = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) + timeoutMs;
            eventListener.connectStart(call, inetSocketAddress2, proxy);
            this.channel = SocketChannel.open();
            this.channel.configureBlocking(false);
            this.channel.connect(inetSocketAddress2);
        }

        /* access modifiers changed from: package-private */
        public void close() {
            SocketChannel socketChannel = this.channel;
            if (socketChannel != null) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    ConcurrentConnect.this.logMessage("Socket channel close error", e);
                } catch (Throwable th) {
                    this.channel = null;
                    throw th;
                }
                this.channel = null;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isExpired() {
            return TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) >= this.expiredTimeStamp;
        }

        /* access modifiers changed from: package-private */
        public long getRemainingMs() {
            long now = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            long j = this.expiredTimeStamp;
            if (now >= j) {
                return 0;
            }
            return j - now;
        }
    }
}
