package com.android.internal.os;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.SystemClock;
import android.os.Trace;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import android.util.Log;
import android.util.Slog;
import dalvik.system.ZygoteHooks;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/* access modifiers changed from: package-private */
public class ZygoteServer {
    public static final String TAG = "ZygoteServer";
    private static final String USAP_POOL_SIZE_MAX_DEFAULT = "6";
    private static final int USAP_POOL_SIZE_MAX_LIMIT = 100;
    private static final String USAP_POOL_SIZE_MIN_DEFAULT = "1";
    private static final int USAP_POOL_SIZE_MIN_LIMIT = 1;
    private boolean mCloseSocketFd;
    private boolean mIsFirstPropertyCheck;
    private boolean mIsForkChild;
    private long mLastPropCheckTimestamp;
    private boolean mUsapPoolEnabled;
    private FileDescriptor mUsapPoolEventFD;
    private int mUsapPoolRefillThreshold;
    private int mUsapPoolSizeMax;
    private int mUsapPoolSizeMin;
    private LocalServerSocket mUsapPoolSocket;
    private final boolean mUsapPoolSupported;
    private LocalServerSocket mZygoteSocket;

    ZygoteServer() {
        this.mUsapPoolEnabled = false;
        this.mUsapPoolSizeMax = 0;
        this.mUsapPoolSizeMin = 0;
        this.mUsapPoolRefillThreshold = 0;
        this.mIsFirstPropertyCheck = true;
        this.mLastPropCheckTimestamp = 0;
        this.mUsapPoolEventFD = null;
        this.mZygoteSocket = null;
        this.mUsapPoolSocket = null;
        this.mUsapPoolSupported = false;
    }

    ZygoteServer(boolean isPrimaryZygote) {
        this.mUsapPoolEnabled = false;
        this.mUsapPoolSizeMax = 0;
        this.mUsapPoolSizeMin = 0;
        this.mUsapPoolRefillThreshold = 0;
        this.mIsFirstPropertyCheck = true;
        this.mLastPropCheckTimestamp = 0;
        if (ZygoteInit.sIsMygote) {
            this.mUsapPoolEventFD = Zygote.getUsapPoolEventFD();
            if (isPrimaryZygote) {
                this.mZygoteSocket = Zygote.createManagedSocketFromInitSocket(Zygote.PRIMARY_MYGOTE_SOCKET_NAME);
                this.mUsapPoolSocket = Zygote.createManagedSocketFromInitSocket(Zygote.USAP_MYGOTE_POOL_PRIMARY_SOCKET_NAME);
            } else {
                this.mZygoteSocket = Zygote.createManagedSocketFromInitSocket(Zygote.SECONDARY_MYGOTE_SOCKET_NAME);
                this.mUsapPoolSocket = Zygote.createManagedSocketFromInitSocket(Zygote.USAP_MYGOTE_POOL_SECONDARY_SOCKET_NAME);
            }
        } else {
            this.mUsapPoolEventFD = Zygote.getUsapPoolEventFD();
            if (isPrimaryZygote) {
                this.mZygoteSocket = Zygote.createManagedSocketFromInitSocket(Zygote.PRIMARY_SOCKET_NAME);
                this.mUsapPoolSocket = Zygote.createManagedSocketFromInitSocket(Zygote.USAP_POOL_PRIMARY_SOCKET_NAME);
            } else {
                this.mZygoteSocket = Zygote.createManagedSocketFromInitSocket(Zygote.SECONDARY_SOCKET_NAME);
                this.mUsapPoolSocket = Zygote.createManagedSocketFromInitSocket(Zygote.USAP_POOL_SECONDARY_SOCKET_NAME);
            }
        }
        fetchUsapPoolPolicyProps();
        this.mUsapPoolSupported = true;
    }

    /* access modifiers changed from: package-private */
    public void setForkChild() {
        this.mIsForkChild = true;
    }

    public boolean isUsapPoolEnabled() {
        return this.mUsapPoolEnabled;
    }

    /* access modifiers changed from: package-private */
    public void registerServerSocketAtAbstractName(String socketName) {
        if (this.mZygoteSocket == null) {
            try {
                this.mZygoteSocket = new LocalServerSocket(socketName);
                this.mCloseSocketFd = false;
            } catch (IOException ex) {
                throw new RuntimeException("Error binding to abstract socket '" + socketName + "'", ex);
            }
        }
    }

    private ZygoteConnection acceptCommandPeer(String abiList) {
        try {
            return createNewConnection(this.mZygoteSocket.accept(), abiList);
        } catch (IOException ex) {
            throw new RuntimeException("IOException during accept()", ex);
        }
    }

    /* access modifiers changed from: protected */
    public ZygoteConnection createNewConnection(LocalSocket socket, String abiList) throws IOException {
        return new ZygoteConnection(socket, abiList);
    }

    /* access modifiers changed from: package-private */
    public void closeServerSocket() {
        try {
            if (this.mZygoteSocket != null) {
                FileDescriptor fd = this.mZygoteSocket.getFileDescriptor();
                this.mZygoteSocket.close();
                if (fd != null && this.mCloseSocketFd) {
                    Os.close(fd);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Zygote:  error closing sockets", ex);
        } catch (ErrnoException ex2) {
            Log.e(TAG, "Zygote:  error closing descriptor", ex2);
        }
        this.mZygoteSocket = null;
    }

    /* access modifiers changed from: package-private */
    public FileDescriptor getZygoteSocketFileDescriptor() {
        return this.mZygoteSocket.getFileDescriptor();
    }

    private void fetchUsapPoolPolicyProps() {
        if (this.mUsapPoolSupported) {
            String usapPoolSizeMaxPropString = Zygote.getConfigurationProperty(ZygoteConfig.USAP_POOL_SIZE_MAX, "6");
            if (!usapPoolSizeMaxPropString.isEmpty()) {
                this.mUsapPoolSizeMax = Integer.min(Integer.parseInt(usapPoolSizeMaxPropString), 100);
            }
            String usapPoolSizeMinPropString = Zygote.getConfigurationProperty(ZygoteConfig.USAP_POOL_SIZE_MIN, "1");
            if (!usapPoolSizeMinPropString.isEmpty()) {
                this.mUsapPoolSizeMin = Integer.max(Integer.parseInt(usapPoolSizeMinPropString), 1);
            }
            String usapPoolRefillThresholdPropString = Zygote.getConfigurationProperty(ZygoteConfig.USAP_POOL_REFILL_THRESHOLD, Integer.toString(this.mUsapPoolSizeMax / 2));
            if (!usapPoolRefillThresholdPropString.isEmpty()) {
                this.mUsapPoolRefillThreshold = Integer.min(Integer.parseInt(usapPoolRefillThresholdPropString), this.mUsapPoolSizeMax);
            }
            if (this.mUsapPoolSizeMin >= this.mUsapPoolSizeMax) {
                Log.w(TAG, "The max size of the USAP pool must be greater than the minimum size.  Restoring default values.");
                this.mUsapPoolSizeMax = Integer.parseInt("6");
                this.mUsapPoolSizeMin = Integer.parseInt("1");
                this.mUsapPoolRefillThreshold = this.mUsapPoolSizeMax / 2;
            }
        }
    }

    private void fetchUsapPoolPolicyPropsWithMinInterval() {
        long currentTimestamp = SystemClock.elapsedRealtime();
        if (this.mIsFirstPropertyCheck || currentTimestamp - this.mLastPropCheckTimestamp >= 60000) {
            this.mIsFirstPropertyCheck = false;
            this.mLastPropCheckTimestamp = currentTimestamp;
            fetchUsapPoolPolicyProps();
        }
    }

    /* access modifiers changed from: package-private */
    public Runnable fillUsapPool(int[] sessionSocketRawFDs) {
        Trace.traceBegin(64, "Zygote:FillUsapPool");
        fetchUsapPoolPolicyPropsWithMinInterval();
        int usapPoolCount = Zygote.getUsapPoolCount();
        int numUsapsToSpawn = this.mUsapPoolSizeMax - usapPoolCount;
        if (usapPoolCount < this.mUsapPoolSizeMin || numUsapsToSpawn >= this.mUsapPoolRefillThreshold) {
            ZygoteHooks.preFork();
            Zygote.resetNicePriority();
            while (true) {
                int usapPoolCount2 = usapPoolCount + 1;
                if (usapPoolCount >= this.mUsapPoolSizeMax) {
                    ZygoteHooks.postForkCommon();
                    Log.i(Zygote.PRIMARY_SOCKET_NAME, "Filled the USAP pool. New USAPs: " + numUsapsToSpawn);
                    break;
                }
                Runnable caller = Zygote.forkUsap(this.mUsapPoolSocket, sessionSocketRawFDs);
                if (caller != null) {
                    return caller;
                }
                usapPoolCount = usapPoolCount2;
            }
        }
        Trace.traceEnd(64);
        return null;
    }

    /* access modifiers changed from: package-private */
    public Runnable setUsapPoolStatus(boolean newStatus, LocalSocket sessionSocket) {
        if (!this.mUsapPoolSupported) {
            Log.w(TAG, "Attempting to enable a USAP pool for a Zygote that doesn't support it.");
            return null;
        } else if (this.mUsapPoolEnabled == newStatus) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("USAP Pool status change: ");
            sb.append(newStatus ? "ENABLED" : "DISABLED");
            Log.i(TAG, sb.toString());
            this.mUsapPoolEnabled = newStatus;
            if (newStatus) {
                return fillUsapPool(new int[]{sessionSocket.getFileDescriptor().getInt$()});
            }
            Zygote.emptyUsapPool();
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x01ab, code lost:
        if (r11 == false) goto L_0x001b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x01ae, code lost:
        r6 = fillUsapPool(r0.subList(1, r0.size()).stream().mapToInt(com.android.internal.os.$$Lambda$ZygoteServer$NJVbduCrCzDq0RHpPga7lyCk4eE.INSTANCE).toArray());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x01c9, code lost:
        if (r6 == null) goto L_0x001b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x01cb, code lost:
        return r6;
     */
    public Runnable runSelectLoop(String abiList) {
        int[] usapPipeFDs;
        StructPollfd[] pollFDs;
        int pollIndex;
        ArrayList<FileDescriptor> socketFDs = new ArrayList<>();
        ArrayList<ZygoteConnection> peers = new ArrayList<>();
        socketFDs.add(this.mZygoteSocket.getFileDescriptor());
        peers.add(null);
        while (true) {
            fetchUsapPoolPolicyPropsWithMinInterval();
            if (this.mUsapPoolEnabled) {
                int[] usapPipeFDs2 = Zygote.getUsapPipeFDs();
                pollFDs = new StructPollfd[(socketFDs.size() + 1 + usapPipeFDs2.length)];
                usapPipeFDs = usapPipeFDs2;
            } else {
                pollFDs = new StructPollfd[socketFDs.size()];
                usapPipeFDs = null;
            }
            int pollIndex2 = 0;
            Iterator<FileDescriptor> it = socketFDs.iterator();
            while (it.hasNext()) {
                pollFDs[pollIndex2] = new StructPollfd();
                pollFDs[pollIndex2].fd = it.next();
                pollFDs[pollIndex2].events = (short) OsConstants.POLLIN;
                pollIndex2++;
            }
            if (this.mUsapPoolEnabled) {
                pollFDs[pollIndex2] = new StructPollfd();
                pollFDs[pollIndex2].fd = this.mUsapPoolEventFD;
                pollFDs[pollIndex2].events = (short) OsConstants.POLLIN;
                pollIndex = pollIndex2 + 1;
                for (int usapPipeFD : usapPipeFDs) {
                    FileDescriptor managedFd = new FileDescriptor();
                    managedFd.setInt$(usapPipeFD);
                    pollFDs[pollIndex] = new StructPollfd();
                    pollFDs[pollIndex].fd = managedFd;
                    pollFDs[pollIndex].events = (short) OsConstants.POLLIN;
                    pollIndex++;
                }
            } else {
                pollIndex = pollIndex2;
            }
            int i = -1;
            try {
                Os.poll(pollFDs, -1);
                boolean usapPoolFDRead = false;
                while (true) {
                    pollIndex += i;
                    if (pollIndex < 0) {
                        break;
                    }
                    if ((pollFDs[pollIndex].revents & OsConstants.POLLIN) != 0) {
                        if (pollIndex == 0) {
                            ZygoteConnection newPeer = acceptCommandPeer(abiList);
                            peers.add(newPeer);
                            socketFDs.add(newPeer.getFileDescriptor());
                        } else if (pollIndex < pollIndex2) {
                            try {
                                ZygoteConnection connection = peers.get(pollIndex);
                                Runnable command = connection.processOneCommand(this);
                                if (this.mIsForkChild) {
                                    if (command != null) {
                                        this.mIsForkChild = false;
                                        return command;
                                    }
                                    throw new IllegalStateException("command == null");
                                } else if (command == null) {
                                    if (connection.isClosedByPeer()) {
                                        connection.closeSocket();
                                        peers.remove(pollIndex);
                                        socketFDs.remove(pollIndex);
                                    }
                                    this.mIsForkChild = false;
                                } else {
                                    throw new IllegalStateException("command != null");
                                }
                            } catch (Exception e) {
                                if (!this.mIsForkChild) {
                                    Slog.e(TAG, "Exception executing zygote command: ", e);
                                    peers.remove(pollIndex).closeSocket();
                                    socketFDs.remove(pollIndex);
                                } else {
                                    Log.e(TAG, "Caught post-fork exception in child process.", e);
                                    throw e;
                                }
                            } catch (Throwable th) {
                                this.mIsForkChild = false;
                                throw th;
                            }
                        } else {
                            try {
                                byte[] buffer = new byte[8];
                                int readBytes = Os.read(pollFDs[pollIndex].fd, buffer, 0, buffer.length);
                                if (readBytes == 8) {
                                    long messagePayload = new DataInputStream(new ByteArrayInputStream(buffer)).readLong();
                                    if (pollIndex > pollIndex2) {
                                        Zygote.removeUsapTableEntry((int) messagePayload);
                                    }
                                    usapPoolFDRead = true;
                                    i = -1;
                                } else {
                                    Log.e(TAG, "Incomplete read from USAP management FD of size " + readBytes);
                                }
                            } catch (Exception ex) {
                                if (pollIndex == pollIndex2) {
                                    Log.e(TAG, "Failed to read from USAP pool event FD: " + ex.getMessage());
                                } else {
                                    Log.e(TAG, "Failed to read from USAP reporting pipe: " + ex.getMessage());
                                }
                            }
                        }
                    }
                    i = -1;
                }
            } catch (ErrnoException ex2) {
                throw new RuntimeException("poll failed", ex2);
            }
        }
    }
}
