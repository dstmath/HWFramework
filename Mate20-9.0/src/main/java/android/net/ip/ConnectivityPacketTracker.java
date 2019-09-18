package android.net.ip;

import android.net.NetworkUtils;
import android.net.util.ConnectivityPacketSummary;
import android.net.util.InterfaceParams;
import android.net.util.PacketReader;
import android.os.Handler;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import libcore.util.HexEncoding;

public class ConnectivityPacketTracker {
    private static final boolean DBG = false;
    private static final String MARK_NAMED_START = "--- START (%s) ---";
    private static final String MARK_NAMED_STOP = "--- STOP (%s) ---";
    private static final String MARK_START = "--- START ---";
    private static final String MARK_STOP = "--- STOP ---";
    private static final String TAG = ConnectivityPacketTracker.class.getSimpleName();
    /* access modifiers changed from: private */
    public String mDisplayName;
    /* access modifiers changed from: private */
    public final LocalLog mLog;
    private final PacketReader mPacketListener;
    /* access modifiers changed from: private */
    public boolean mRunning;
    /* access modifiers changed from: private */
    public final String mTag;

    private final class PacketListener extends PacketReader {
        private final InterfaceParams mInterface;

        PacketListener(Handler h, InterfaceParams ifParams) {
            super(h, ifParams.defaultMtu);
            this.mInterface = ifParams;
        }

        /* access modifiers changed from: protected */
        public FileDescriptor createFd() {
            FileDescriptor s = null;
            try {
                s = Os.socket(OsConstants.AF_PACKET, OsConstants.SOCK_RAW, 0);
                NetworkUtils.attachControlPacketFilter(s, OsConstants.ARPHRD_ETHER);
                Os.bind(s, new PacketSocketAddress((short) OsConstants.ETH_P_ALL, this.mInterface.index));
                return s;
            } catch (ErrnoException | IOException e) {
                logError("Failed to create packet tracking socket: ", e);
                closeFd(s);
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public void handlePacket(byte[] recvbuf, int length) {
            String summary = ConnectivityPacketSummary.summarize(this.mInterface.macAddr, recvbuf, length);
            if (summary != null) {
                addLogEntry(summary + "\n[" + new String(HexEncoding.encode(recvbuf, 0, length)) + "]");
            }
        }

        /* access modifiers changed from: protected */
        public void onStart() {
            String msg;
            if (TextUtils.isEmpty(ConnectivityPacketTracker.this.mDisplayName)) {
                msg = ConnectivityPacketTracker.MARK_START;
            } else {
                msg = String.format(ConnectivityPacketTracker.MARK_NAMED_START, new Object[]{ConnectivityPacketTracker.this.mDisplayName});
            }
            ConnectivityPacketTracker.this.mLog.log(msg);
        }

        /* access modifiers changed from: protected */
        public void onStop() {
            String msg;
            if (TextUtils.isEmpty(ConnectivityPacketTracker.this.mDisplayName)) {
                msg = ConnectivityPacketTracker.MARK_STOP;
            } else {
                msg = String.format(ConnectivityPacketTracker.MARK_NAMED_STOP, new Object[]{ConnectivityPacketTracker.this.mDisplayName});
            }
            if (!ConnectivityPacketTracker.this.mRunning) {
                msg = msg + " (packet listener stopped unexpectedly)";
            }
            ConnectivityPacketTracker.this.mLog.log(msg);
        }

        /* access modifiers changed from: protected */
        public void logError(String msg, Exception e) {
            Log.e(ConnectivityPacketTracker.this.mTag, msg, e);
            addLogEntry(msg + e);
        }

        private void addLogEntry(String entry) {
            ConnectivityPacketTracker.this.mLog.log(entry);
        }
    }

    public ConnectivityPacketTracker(Handler h, InterfaceParams ifParams, LocalLog log) {
        if (ifParams != null) {
            this.mTag = TAG + "." + ifParams.name;
            this.mLog = log;
            this.mPacketListener = new PacketListener(h, ifParams);
            return;
        }
        throw new IllegalArgumentException("null InterfaceParams");
    }

    public void start(String displayName) {
        this.mRunning = true;
        this.mDisplayName = displayName;
        this.mPacketListener.start();
    }

    public void stop() {
        this.mPacketListener.stop();
        this.mRunning = false;
        this.mDisplayName = null;
    }
}
