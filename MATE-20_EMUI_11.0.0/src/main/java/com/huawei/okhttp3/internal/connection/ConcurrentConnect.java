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

public class ConcurrentConnect {
    private final ArrayList<InetSocketAddress> addressList;
    private final int addressListCount;
    private int attemptDelayMs;
    private final ArrayList<ChannelWrapper> channelList = new ArrayList<>();
    private final ArrayList<InetSocketAddress> failedAddressList = new ArrayList<>();
    private volatile boolean isCancelled;
    private Selector selector;

    public ConcurrentConnect(ArrayList<InetSocketAddress> addressList2, int attemptDelayMs2) {
        int i = 0;
        this.isCancelled = false;
        this.addressList = new ArrayList<>(addressList2);
        this.attemptDelayMs = attemptDelayMs2;
        this.addressListCount = addressList2 != null ? addressList2.size() : i;
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
                this.isCancelled = true;
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
        boolean willOpenChannel;
        boolean willOpenChannel2;
        int i = this.attemptDelayMs;
        SocketChannel selectedChannel = null;
        long tmpMs = (long) i;
        boolean willOpenChannel3 = true;
        long timeoutMs = connectTimeoutMs - ((long) i);
        while (true) {
            if (this.isCancelled) {
                break;
            }
            if (this.addressList.size() <= 0 || !willOpenChannel3) {
                willOpenChannel = willOpenChannel3;
            } else if (!prepareSocketChannel(this.addressList.remove(0), connectTimeoutMs, proxy, call, eventListener)) {
                continue;
            } else {
                timeoutMs += (long) this.attemptDelayMs;
                willOpenChannel = false;
            }
            if (this.channelList.isEmpty() && this.addressList.isEmpty()) {
                return null;
            }
            long wait = this.channelList.size() > 0 ? this.channelList.get(0).getRemainingMs() : timeoutMs;
            if (this.addressList.size() > 0 && wait > tmpMs) {
                wait = tmpMs;
            }
            long start = System.nanoTime();
            try {
                this.selector.select(wait);
                if (!this.isCancelled) {
                    long timeElapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                    selectedChannel = findConnectedChannel();
                    if (selectedChannel == null) {
                        checkForTimeout(timeElapsed);
                        if (this.failedAddressList.size() == this.addressListCount) {
                            willOpenChannel2 = willOpenChannel;
                            break;
                        } else if (timeElapsed >= timeoutMs) {
                            willOpenChannel2 = willOpenChannel;
                            break;
                        } else {
                            timeoutMs -= timeElapsed;
                            if (this.addressList.size() <= 0) {
                                willOpenChannel3 = willOpenChannel;
                            } else if (timeElapsed >= tmpMs) {
                                int i2 = this.attemptDelayMs;
                                tmpMs = i2 == 0 ? 0 : ((long) i2) - ((timeElapsed - tmpMs) % ((long) i2));
                                willOpenChannel3 = true;
                            } else {
                                tmpMs -= timeElapsed;
                                willOpenChannel3 = willOpenChannel;
                            }
                        }
                    } else {
                        willOpenChannel2 = willOpenChannel;
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
            SelectionKey selectionKey = channelWrapper.channel.register(this.selector, 8);
            if (selectionKey == null) {
                return false;
            }
            selectionKey.attach(channelWrapper);
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
        if (channelWrapper != null) {
            this.failedAddressList.add(channelWrapper.inetSocketAddress);
            this.channelList.remove(channelWrapper);
            channelWrapper.close();
        }
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
        ChannelWrapper channelWrapper;
        Iterator<SelectionKey> keyIterator = this.selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            if (key.isConnectable() && (channelWrapper = (ChannelWrapper) key.attachment()) != null) {
                try {
                    SocketChannel channel = channelWrapper.channel;
                    if (channel != null) {
                        if (channel.finishConnect()) {
                            key.cancel();
                            this.channelList.remove(channelWrapper);
                            return channel;
                        }
                        key.cancel();
                        handleFailedChannel(channelWrapper);
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
            if (eventListener != null) {
                eventListener.connectStart(call, inetSocketAddress2, proxy);
            }
            if (this.channel != null) {
                this.channel = SocketChannel.open();
                this.channel.configureBlocking(false);
                this.channel.connect(inetSocketAddress2);
            }
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
