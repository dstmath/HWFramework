package android.net.util;

import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.FileDescriptor;
import libcore.io.IoUtils;

public abstract class PacketReader {
    public static final int DEFAULT_RECV_BUF_SIZE = 2048;
    private static final int FD_EVENTS = 5;
    private static final int UNREGISTER_THIS_FD = 0;
    private FileDescriptor mFd;
    private final Handler mHandler;
    private final byte[] mPacket;
    private long mPacketsReceived;
    private final MessageQueue mQueue;

    /* access modifiers changed from: protected */
    public abstract FileDescriptor createFd();

    protected static void closeFd(FileDescriptor fd) {
        IoUtils.closeQuietly(fd);
    }

    protected PacketReader(Handler h) {
        this(h, 2048);
    }

    protected PacketReader(Handler h, int recvbufsize) {
        this.mHandler = h;
        this.mQueue = this.mHandler.getLooper().getQueue();
        this.mPacket = new byte[Math.max(recvbufsize, 2048)];
    }

    public final void start() {
        if (onCorrectThread()) {
            createAndRegisterFd();
        } else {
            this.mHandler.post(new Runnable() {
                public final void run() {
                    PacketReader.lambda$start$0(PacketReader.this);
                }
            });
        }
    }

    public static /* synthetic */ void lambda$start$0(PacketReader packetReader) {
        packetReader.logError("start() called from off-thread", null);
        packetReader.createAndRegisterFd();
    }

    public final void stop() {
        if (onCorrectThread()) {
            unregisterAndDestroyFd();
        } else {
            this.mHandler.post(new Runnable() {
                public final void run() {
                    PacketReader.lambda$stop$1(PacketReader.this);
                }
            });
        }
    }

    public static /* synthetic */ void lambda$stop$1(PacketReader packetReader) {
        packetReader.logError("stop() called from off-thread", null);
        packetReader.unregisterAndDestroyFd();
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public final int recvBufSize() {
        return this.mPacket.length;
    }

    public final long numPacketsReceived() {
        return this.mPacketsReceived;
    }

    /* access modifiers changed from: protected */
    public int readPacket(FileDescriptor fd, byte[] packetBuffer) throws Exception {
        return Os.read(fd, packetBuffer, 0, packetBuffer.length);
    }

    /* access modifiers changed from: protected */
    public void handlePacket(byte[] recvbuf, int length) {
    }

    /* access modifiers changed from: protected */
    public void logError(String msg, Exception e) {
    }

    /* access modifiers changed from: protected */
    public void onStart() {
    }

    /* access modifiers changed from: protected */
    public void onStop() {
    }

    private void createAndRegisterFd() {
        if (this.mFd == null) {
            try {
                this.mFd = createFd();
                if (this.mFd != null) {
                    IoUtils.setBlocking(this.mFd, false);
                }
                if (this.mFd != null) {
                    this.mQueue.addOnFileDescriptorEventListener(this.mFd, 5, new MessageQueue.OnFileDescriptorEventListener() {
                        public int onFileDescriptorEvents(FileDescriptor fd, int events) {
                            if (PacketReader.this.isRunning() && PacketReader.this.handleInput()) {
                                return 5;
                            }
                            PacketReader.this.unregisterAndDestroyFd();
                            return 0;
                        }
                    });
                    onStart();
                }
            } catch (Exception e) {
                logError("Failed to create socket: ", e);
                closeFd(this.mFd);
                this.mFd = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isRunning() {
        return this.mFd != null && this.mFd.valid();
    }

    /* access modifiers changed from: private */
    public boolean handleInput() {
        while (true) {
            if (!isRunning()) {
                break;
            }
            try {
                int bytesRead = readPacket(this.mFd, this.mPacket);
                if (bytesRead >= 1) {
                    this.mPacketsReceived++;
                    try {
                        handlePacket(this.mPacket, bytesRead);
                    } catch (Exception e) {
                        logError("handlePacket error: ", e);
                    }
                } else if (isRunning()) {
                    logError("Socket closed, exiting", null);
                }
            } catch (ErrnoException e2) {
                if (e2.errno == OsConstants.EAGAIN) {
                    return true;
                }
                if (e2.errno != OsConstants.EINTR) {
                    if (isRunning()) {
                        logError("readPacket error: ", e2);
                    }
                }
            } catch (Exception e3) {
                if (isRunning()) {
                    logError("readPacket error: ", e3);
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void unregisterAndDestroyFd() {
        if (this.mFd != null) {
            this.mQueue.removeOnFileDescriptorEventListener(this.mFd);
            closeFd(this.mFd);
            this.mFd = null;
            onStop();
        }
    }

    private boolean onCorrectThread() {
        return this.mHandler.getLooper() == Looper.myLooper();
    }
}
