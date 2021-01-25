package com.android.server.connectivity;

import android.content.Context;
import android.net.ISocketKeepaliveCallback;
import android.net.KeepalivePacketData;
import android.net.NattKeepalivePacketData;
import android.net.NetworkUtils;
import android.net.SocketKeepalive;
import android.net.util.KeepaliveUtils;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.util.Pair;
import com.android.internal.util.HexDump;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class KeepaliveTracker {
    private static final boolean DBG = false;
    public static final String PERMISSION = "android.permission.PACKET_KEEPALIVE_OFFLOAD";
    private static final String TAG = "KeepaliveTracker";
    private final int mAllowedUnprivilegedSlotsForUid;
    private final Handler mConnectivityServiceHandler;
    private final Context mContext;
    private final HashMap<NetworkAgentInfo, HashMap<Integer, KeepaliveInfo>> mKeepalives = new HashMap<>();
    private final int mReservedPrivilegedSlots;
    private final int[] mSupportedKeepalives;
    private final TcpKeepaliveController mTcpController;

    public KeepaliveTracker(Context context, Handler handler) {
        this.mConnectivityServiceHandler = handler;
        this.mTcpController = new TcpKeepaliveController(handler);
        this.mContext = context;
        this.mSupportedKeepalives = KeepaliveUtils.getSupportedKeepalives(this.mContext);
        this.mReservedPrivilegedSlots = this.mContext.getResources().getInteger(17694878);
        this.mAllowedUnprivilegedSlotsForUid = this.mContext.getResources().getInteger(17694733);
    }

    /* access modifiers changed from: package-private */
    public class KeepaliveInfo implements IBinder.DeathRecipient {
        private static final int NOT_STARTED = 1;
        private static final int STARTED = 3;
        private static final int STARTING = 2;
        private static final int STOPPING = 4;
        public static final int TYPE_NATT = 1;
        public static final int TYPE_TCP = 2;
        private final ISocketKeepaliveCallback mCallback;
        private final FileDescriptor mFd;
        private final int mInterval;
        private final NetworkAgentInfo mNai;
        private final KeepalivePacketData mPacket;
        private final int mPid;
        private final boolean mPrivileged;
        private int mSlot = -1;
        private int mStartedState;
        private final int mType;
        private final int mUid;

        KeepaliveInfo(ISocketKeepaliveCallback callback, NetworkAgentInfo nai, KeepalivePacketData packet, int interval, int type, FileDescriptor fd) throws SocketKeepalive.InvalidSocketException {
            boolean z = true;
            this.mStartedState = 1;
            this.mCallback = callback;
            this.mPid = Binder.getCallingPid();
            this.mUid = Binder.getCallingUid();
            this.mPrivileged = KeepaliveTracker.this.mContext.checkPermission(KeepaliveTracker.PERMISSION, this.mPid, this.mUid) != 0 ? false : z;
            this.mNai = nai;
            this.mPacket = packet;
            this.mInterval = interval;
            this.mType = type;
            if (fd != null) {
                try {
                    this.mFd = Os.dup(fd);
                } catch (ErrnoException e) {
                    Log.e(KeepaliveTracker.TAG, "Cannot dup fd");
                    throw new SocketKeepalive.InvalidSocketException(-25, e);
                }
            } else {
                Log.d(KeepaliveTracker.TAG, toString() + " calls with null fd");
                if (!this.mPrivileged) {
                    throw new SecurityException("null fd is not allowed for unprivileged access.");
                } else if (this.mType != 2) {
                    this.mFd = null;
                } else {
                    throw new IllegalArgumentException("null fd is not allowed for tcp socket keepalives.");
                }
            }
            try {
                this.mCallback.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e2) {
                binderDied();
            }
        }

        public NetworkAgentInfo getNai() {
            return this.mNai;
        }

        private String startedStateString(int state) {
            if (state == 1) {
                return "NOT_STARTED";
            }
            if (state == 2) {
                return "STARTING";
            }
            if (state == 3) {
                return "STARTED";
            }
            if (state == 4) {
                return "STOPPING";
            }
            throw new IllegalArgumentException("Unknown state");
        }

        @Override // java.lang.Object
        public String toString() {
            return "KeepaliveInfo [ type=" + this.mType + " network=" + this.mNai.network + " startedState=" + startedStateString(this.mStartedState) + " ip changed interval=" + this.mInterval + " uid=" + this.mUid + " pid=" + this.mPid + " privileged=" + this.mPrivileged + " packetData=" + HexDump.toHexString(this.mPacket.getPacket()) + " ]";
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            stop(-10);
        }

        /* access modifiers changed from: package-private */
        public void unlinkDeathRecipient() {
            ISocketKeepaliveCallback iSocketKeepaliveCallback = this.mCallback;
            if (iSocketKeepaliveCallback != null) {
                iSocketKeepaliveCallback.asBinder().unlinkToDeath(this, 0);
            }
        }

        private int checkNetworkConnected() {
            if (!this.mNai.networkInfo.isConnectedOrConnecting()) {
                return -20;
            }
            return 0;
        }

        private int checkSourceAddress() {
            for (InetAddress address : this.mNai.linkProperties.getAddresses()) {
                if (address.equals(this.mPacket.srcAddress)) {
                    return 0;
                }
            }
            return -21;
        }

        private int checkInterval() {
            int i = this.mInterval;
            if (i < 10 || i > 3600) {
                return -24;
            }
            return 0;
        }

        private int checkPermission() {
            HashMap<Integer, KeepaliveInfo> networkKeepalives = (HashMap) KeepaliveTracker.this.mKeepalives.get(this.mNai);
            if (networkKeepalives == null) {
                return -20;
            }
            if (this.mPrivileged) {
                return 0;
            }
            int supported = KeepaliveUtils.getSupportedKeepalivesForNetworkCapabilities(KeepaliveTracker.this.mSupportedKeepalives, this.mNai.networkCapabilities);
            int takenUnprivilegedSlots = 0;
            for (KeepaliveInfo ki : networkKeepalives.values()) {
                if (!ki.mPrivileged) {
                    takenUnprivilegedSlots++;
                }
            }
            if (takenUnprivilegedSlots > supported - KeepaliveTracker.this.mReservedPrivilegedSlots) {
                return -32;
            }
            int unprivilegedCountSameUid = 0;
            for (HashMap<Integer, KeepaliveInfo> kaForNetwork : KeepaliveTracker.this.mKeepalives.values()) {
                for (KeepaliveInfo ki2 : kaForNetwork.values()) {
                    if (ki2.mUid == this.mUid) {
                        unprivilegedCountSameUid++;
                    }
                }
            }
            if (unprivilegedCountSameUid > KeepaliveTracker.this.mAllowedUnprivilegedSlotsForUid) {
                return -32;
            }
            return 0;
        }

        private int checkLimit() {
            HashMap<Integer, KeepaliveInfo> networkKeepalives = (HashMap) KeepaliveTracker.this.mKeepalives.get(this.mNai);
            if (networkKeepalives == null) {
                return -20;
            }
            int supported = KeepaliveUtils.getSupportedKeepalivesForNetworkCapabilities(KeepaliveTracker.this.mSupportedKeepalives, this.mNai.networkCapabilities);
            if (supported == 0) {
                return -30;
            }
            if (networkKeepalives.size() > supported) {
                return -32;
            }
            return 0;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int isValid() {
            int error;
            synchronized (this.mNai) {
                error = checkInterval();
                if (error == 0) {
                    error = checkLimit();
                }
                if (error == 0) {
                    error = checkPermission();
                }
                if (error == 0) {
                    error = checkNetworkConnected();
                }
                if (error == 0) {
                    error = checkSourceAddress();
                }
            }
            return error;
        }

        /* access modifiers changed from: package-private */
        public void start(int slot) {
            this.mSlot = slot;
            int error = isValid();
            if (error == 0) {
                Log.d(KeepaliveTracker.TAG, "Starting keepalive " + this.mSlot + " on " + this.mNai.name());
                int i = this.mType;
                if (i == 1) {
                    this.mNai.asyncChannel.sendMessage(528400, slot, 0, this.mPacket);
                    this.mNai.asyncChannel.sendMessage(528395, slot, this.mInterval, this.mPacket);
                } else if (i != 2) {
                    Log.wtf(KeepaliveTracker.TAG, "Starting keepalive with unknown type: " + this.mType);
                    KeepaliveTracker.this.handleStopKeepalive(this.mNai, this.mSlot, error);
                    return;
                } else {
                    try {
                        KeepaliveTracker.this.mTcpController.startSocketMonitor(this.mFd, this, this.mSlot);
                        this.mNai.asyncChannel.sendMessage(528400, slot, 0, this.mPacket);
                        this.mNai.asyncChannel.sendMessage(528395, slot, this.mInterval, this.mPacket);
                    } catch (SocketKeepalive.InvalidSocketException e) {
                        KeepaliveTracker.this.handleStopKeepalive(this.mNai, this.mSlot, -25);
                        return;
                    }
                }
                this.mStartedState = 2;
                return;
            }
            KeepaliveTracker.this.handleStopKeepalive(this.mNai, this.mSlot, error);
        }

        /* access modifiers changed from: package-private */
        public void stop(int reason) {
            Binder.getCallingUid();
            int i = this.mUid;
            Log.d(KeepaliveTracker.TAG, "Stopping keepalive " + this.mSlot + " on " + this.mNai.name() + ": " + reason);
            int i2 = this.mStartedState;
            if (i2 == 1) {
                KeepaliveTracker.this.cleanupStoppedKeepalive(this.mNai, this.mSlot);
            } else if (i2 != 4) {
                this.mStartedState = 4;
                int i3 = this.mType;
                if (i3 != 1) {
                    if (i3 != 2) {
                        Log.wtf(KeepaliveTracker.TAG, "Stopping keepalive with unknown type: " + this.mType);
                    } else {
                        KeepaliveTracker.this.mTcpController.stopSocketMonitor(this.mSlot);
                    }
                }
                this.mNai.asyncChannel.sendMessage(528396, this.mSlot);
                this.mNai.asyncChannel.sendMessage(528401, this.mSlot);
            } else {
                return;
            }
            FileDescriptor fileDescriptor = this.mFd;
            if (fileDescriptor != null) {
                try {
                    Os.close(fileDescriptor);
                } catch (ErrnoException e) {
                    Log.wtf(KeepaliveTracker.TAG, "Error closing fd for keepalive " + this.mSlot + ": " + e);
                }
            }
            if (reason == 0) {
                try {
                    this.mCallback.onStopped();
                } catch (RemoteException e2) {
                    Log.w(KeepaliveTracker.TAG, "Discarded onStop callback: " + reason);
                }
            } else if (reason == -2) {
                try {
                    this.mCallback.onDataReceived();
                } catch (RemoteException e3) {
                    Log.w(KeepaliveTracker.TAG, "Discarded onDataReceived callback: " + reason);
                }
            } else {
                KeepaliveTracker.this.notifyErrorCallback(this.mCallback, reason);
            }
            unlinkDeathRecipient();
        }

        /* access modifiers changed from: package-private */
        public void onFileDescriptorInitiatedStop(int socketKeepaliveReason) {
            KeepaliveTracker.this.handleStopKeepalive(this.mNai, this.mSlot, socketKeepaliveReason);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyErrorCallback(ISocketKeepaliveCallback cb, int error) {
        try {
            cb.onError(error);
        } catch (RemoteException e) {
            Log.w(TAG, "Discarded onError(" + error + ") callback");
        }
    }

    private int findFirstFreeSlot(NetworkAgentInfo nai) {
        HashMap networkKeepalives = this.mKeepalives.get(nai);
        if (networkKeepalives == null) {
            networkKeepalives = new HashMap();
            this.mKeepalives.put(nai, networkKeepalives);
        }
        int slot = 1;
        while (slot <= networkKeepalives.size() && networkKeepalives.get(Integer.valueOf(slot)) != null) {
            slot++;
        }
        return slot;
    }

    public void handleStartKeepalive(Message message) {
        KeepaliveInfo ki = (KeepaliveInfo) message.obj;
        NetworkAgentInfo nai = ki.getNai();
        int slot = findFirstFreeSlot(nai);
        this.mKeepalives.get(nai).put(Integer.valueOf(slot), ki);
        ki.start(slot);
    }

    public void handleStopAllKeepalives(NetworkAgentInfo nai, int reason) {
        HashMap<Integer, KeepaliveInfo> networkKeepalives = this.mKeepalives.get(nai);
        if (networkKeepalives != null) {
            Iterator<KeepaliveInfo> it = new ArrayList<>(networkKeepalives.values()).iterator();
            while (it.hasNext()) {
                KeepaliveInfo ki = it.next();
                ki.stop(reason);
                cleanupStoppedKeepalive(nai, ki.mSlot);
            }
        }
    }

    public void handleStopKeepalive(NetworkAgentInfo nai, int slot, int reason) {
        String networkName = nai == null ? "(null)" : nai.name();
        HashMap<Integer, KeepaliveInfo> networkKeepalives = this.mKeepalives.get(nai);
        if (networkKeepalives == null) {
            Log.e(TAG, "Attempt to stop keepalive on nonexistent network " + networkName);
            return;
        }
        KeepaliveInfo ki = networkKeepalives.get(Integer.valueOf(slot));
        if (ki == null) {
            Log.e(TAG, "Attempt to stop nonexistent keepalive " + slot + " on " + networkName);
            return;
        }
        ki.stop(reason);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanupStoppedKeepalive(NetworkAgentInfo nai, int slot) {
        String networkName = nai == null ? "(null)" : nai.name();
        HashMap<Integer, KeepaliveInfo> networkKeepalives = this.mKeepalives.get(nai);
        if (networkKeepalives == null) {
            Log.e(TAG, "Attempt to remove keepalive on nonexistent network " + networkName);
        } else if (networkKeepalives.get(Integer.valueOf(slot)) == null) {
            Log.e(TAG, "Attempt to remove nonexistent keepalive " + slot + " on " + networkName);
        } else {
            networkKeepalives.remove(Integer.valueOf(slot));
            Log.d(TAG, "Remove keepalive " + slot + " on " + networkName + ", " + networkKeepalives.size() + " remains.");
            if (networkKeepalives.isEmpty()) {
                this.mKeepalives.remove(nai);
            }
        }
    }

    public void handleCheckKeepalivesStillValid(NetworkAgentInfo nai) {
        HashMap<Integer, KeepaliveInfo> networkKeepalives = this.mKeepalives.get(nai);
        if (networkKeepalives != null) {
            ArrayList<Pair<Integer, Integer>> invalidKeepalives = new ArrayList<>();
            for (Integer num : networkKeepalives.keySet()) {
                int slot = num.intValue();
                int error = networkKeepalives.get(Integer.valueOf(slot)).isValid();
                if (error != 0) {
                    invalidKeepalives.add(Pair.create(Integer.valueOf(slot), Integer.valueOf(error)));
                }
            }
            Iterator<Pair<Integer, Integer>> it = invalidKeepalives.iterator();
            while (it.hasNext()) {
                Pair<Integer, Integer> slotAndError = it.next();
                handleStopKeepalive(nai, ((Integer) slotAndError.first).intValue(), ((Integer) slotAndError.second).intValue());
            }
        }
    }

    public void handleEventSocketKeepalive(NetworkAgentInfo nai, Message message) {
        int slot = message.arg1;
        int reason = message.arg2;
        KeepaliveInfo ki = null;
        try {
            ki = this.mKeepalives.get(nai).get(Integer.valueOf(slot));
        } catch (NullPointerException e) {
        }
        if (ki == null) {
            Log.e(TAG, "Event " + message.what + "," + slot + "," + reason + " for unknown keepalive " + slot + " on " + nai.name());
        } else if (2 == ki.mStartedState) {
            if (reason == 0) {
                Log.d(TAG, "Started keepalive " + slot + " on " + nai.name());
                ki.mStartedState = 3;
                try {
                    ki.mCallback.onStarted(slot);
                } catch (RemoteException e2) {
                    Log.w(TAG, "Discarded onStarted(" + slot + ") callback");
                }
            } else {
                Log.d(TAG, "Failed to start keepalive " + slot + " on " + nai.name() + ": " + reason);
                handleStopKeepalive(nai, slot, reason);
            }
        } else if (4 == ki.mStartedState) {
            Log.d(TAG, "Stopped keepalive " + slot + " on " + nai.name() + " stopped: " + reason);
            ki.mStartedState = 1;
            cleanupStoppedKeepalive(nai, slot);
        } else {
            Log.wtf(TAG, "Event " + message.what + "," + slot + "," + reason + " for keepalive in wrong state: " + ki.toString());
        }
    }

    public void startNattKeepalive(NetworkAgentInfo nai, FileDescriptor fd, int intervalSeconds, ISocketKeepaliveCallback cb, String srcAddrString, int srcPort, String dstAddrString, int dstPort) {
        if (nai == null) {
            notifyErrorCallback(cb, -20);
            return;
        }
        try {
            try {
                try {
                    KeepaliveInfo ki = new KeepaliveInfo(cb, nai, NattKeepalivePacketData.nattKeepalivePacket(NetworkUtils.numericToInetAddress(srcAddrString), srcPort, NetworkUtils.numericToInetAddress(dstAddrString), 4500), intervalSeconds, 1, fd);
                    Log.d(TAG, "Created keepalive: " + ki.toString());
                    this.mConnectivityServiceHandler.obtainMessage(528395, ki).sendToTarget();
                } catch (SocketKeepalive.InvalidSocketException | IllegalArgumentException | SecurityException e) {
                    Log.e(TAG, "Fail to construct keepalive" + e.getClass().getSimpleName());
                    notifyErrorCallback(cb, -25);
                }
            } catch (SocketKeepalive.InvalidPacketException e2) {
                notifyErrorCallback(cb, e2.error);
            }
        } catch (IllegalArgumentException e3) {
            notifyErrorCallback(cb, -21);
        }
    }

    public void startTcpKeepalive(NetworkAgentInfo nai, FileDescriptor fd, int intervalSeconds, ISocketKeepaliveCallback cb) {
        if (nai == null) {
            notifyErrorCallback(cb, -20);
            return;
        }
        try {
            try {
                KeepaliveInfo ki = new KeepaliveInfo(cb, nai, TcpKeepaliveController.getTcpKeepalivePacket(fd), intervalSeconds, 2, fd);
                Log.d(TAG, "Created keepalive: " + ki.toString());
                this.mConnectivityServiceHandler.obtainMessage(528395, ki).sendToTarget();
            } catch (SocketKeepalive.InvalidSocketException | IllegalArgumentException | SecurityException e) {
                Log.e(TAG, "Fail to construct keepalive e=" + e.getClass().getSimpleName());
                notifyErrorCallback(cb, -25);
            }
        } catch (SocketKeepalive.InvalidPacketException | SocketKeepalive.InvalidSocketException e2) {
            notifyErrorCallback(cb, e2.error);
        }
    }

    public void startNattKeepalive(NetworkAgentInfo nai, FileDescriptor fd, int resourceId, int intervalSeconds, ISocketKeepaliveCallback cb, String srcAddrString, String dstAddrString, int dstPort) {
        int srcPort;
        if (!isNattKeepaliveSocketValid(fd, resourceId)) {
            notifyErrorCallback(cb, -25);
        }
        try {
            srcPort = ((InetSocketAddress) Os.getsockname(fd)).getPort();
        } catch (ErrnoException e) {
            notifyErrorCallback(cb, -25);
            srcPort = 0;
        }
        startNattKeepalive(nai, fd, intervalSeconds, cb, srcAddrString, srcPort, dstAddrString, dstPort);
    }

    public static boolean isNattKeepaliveSocketValid(FileDescriptor fd, int resourceId) {
        if (fd == null) {
            return false;
        }
        return true;
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("Supported Socket keepalives: " + Arrays.toString(this.mSupportedKeepalives));
        pw.println("Reserved Privileged keepalives: " + this.mReservedPrivilegedSlots);
        pw.println("Allowed Unprivileged keepalives per uid: " + this.mAllowedUnprivilegedSlotsForUid);
        pw.println("Socket keepalives:");
        pw.increaseIndent();
        for (NetworkAgentInfo nai : this.mKeepalives.keySet()) {
            pw.println(nai.name());
            pw.increaseIndent();
            for (Integer num : this.mKeepalives.get(nai).keySet()) {
                int slot = num.intValue();
                pw.println(slot + ": " + this.mKeepalives.get(nai).get(Integer.valueOf(slot)).toString());
            }
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }
}
