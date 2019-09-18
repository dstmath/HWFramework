package android.net;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class NetworkAgent extends Handler {
    private static final int BASE = 528384;
    private static final long BW_REFRESH_MIN_WIN_MS = 500;
    public static final int CMD_CONNECT_TO_WLAN_AND_CELLULAR = 528487;
    public static final int CMD_PREVENT_AUTOMATIC_RECONNECT = 528399;
    public static final int CMD_PUSH_APF_PROGRAM = 528400;
    public static final int CMD_REPORT_NETWORK_STATUS = 528391;
    public static final int CMD_REQUEST_BANDWIDTH_UPDATE = 528394;
    public static final int CMD_SAVE_ACCEPT_UNVALIDATED = 528393;
    public static final int CMD_SET_SIGNAL_STRENGTH_THRESHOLDS = 528398;
    public static final int CMD_START_PACKET_KEEPALIVE = 528395;
    public static final int CMD_STOP_PACKET_KEEPALIVE = 528396;
    public static final int CMD_SUSPECT_BAD = 528384;
    public static final int CMD_UPDATE_WIFI_AP_TYPE = 528486;
    private static final boolean DBG = true;
    public static final int EVENT_NETWORK_CAPABILITIES_CHANGED = 528386;
    public static final int EVENT_NETWORK_INFO_CHANGED = 528385;
    public static final int EVENT_NETWORK_PROPERTIES_CHANGED = 528387;
    public static final int EVENT_NETWORK_SCORE_CHANGED = 528388;
    public static final int EVENT_PACKET_KEEPALIVE = 528397;
    public static final int EVENT_REMATCH_NETWORK_AND_REQUESTS = 528485;
    public static final int EVENT_SET_EXPLICITLY_SELECTED = 528392;
    public static final int INVALID_NETWORK = 2;
    public static String REDIRECT_URL_KEY = "redirect URL";
    public static final int VALID_NETWORK = 1;
    private static final boolean VDBG = false;
    public static final int WIFI_BASE_SCORE = 60;
    private final String LOG_TAG;
    private volatile AsyncChannel mAsyncChannel;
    private final Context mContext;
    private volatile long mLastBwRefreshTime;
    private AtomicBoolean mPollLcePending;
    private boolean mPollLceScheduled;
    private final ArrayList<Message> mPreConnectedQueue;
    public final int netId;

    /* access modifiers changed from: protected */
    public abstract void unwanted();

    public NetworkAgent(Looper looper, Context context, String logTag, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score) {
        this(looper, context, logTag, ni, nc, lp, score, null);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NetworkAgent(Looper looper, Context context, String logTag, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
        super(looper);
        NetworkInfo networkInfo = ni;
        NetworkCapabilities networkCapabilities = nc;
        LinkProperties linkProperties = lp;
        this.mPreConnectedQueue = new ArrayList<>();
        this.mLastBwRefreshTime = 0;
        this.mPollLceScheduled = false;
        this.mPollLcePending = new AtomicBoolean(false);
        this.LOG_TAG = logTag;
        this.mContext = context;
        if (networkInfo == null || networkCapabilities == null || linkProperties == null) {
            throw new IllegalArgumentException();
        }
        this.netId = ((ConnectivityManager) this.mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).registerNetworkAgent(new Messenger(this), new NetworkInfo(networkInfo), new LinkProperties(linkProperties), new NetworkCapabilities(networkCapabilities), score, misc);
    }

    public void handleMessage(Message msg) {
        boolean z = true;
        int i = 0;
        switch (msg.what) {
            case 69633:
                if (this.mAsyncChannel != null) {
                    log("Received new connection while already connected!");
                    return;
                }
                AsyncChannel ac = new AsyncChannel();
                ac.connected(null, this, msg.replyTo);
                ac.replyToMessage(msg, 69634, 0);
                synchronized (this.mPreConnectedQueue) {
                    this.mAsyncChannel = ac;
                    Iterator<Message> it = this.mPreConnectedQueue.iterator();
                    while (it.hasNext()) {
                        ac.sendMessage(it.next());
                    }
                    this.mPreConnectedQueue.clear();
                }
                return;
            case 69635:
                if (this.mAsyncChannel != null) {
                    this.mAsyncChannel.disconnect();
                    return;
                }
                return;
            case 69636:
                log("NetworkAgent channel lost");
                unwanted();
                synchronized (this.mPreConnectedQueue) {
                    this.mAsyncChannel = null;
                }
                return;
            case 528384:
                log("Unhandled Message " + msg);
                return;
            case CMD_REPORT_NETWORK_STATUS /*528391*/:
                String redirectUrl = null;
                if (msg.obj != null) {
                    redirectUrl = ((Bundle) msg.obj).getString(REDIRECT_URL_KEY);
                }
                networkStatus(msg.arg1, redirectUrl);
                return;
            case CMD_SAVE_ACCEPT_UNVALIDATED /*528393*/:
                if (msg.arg1 == 0) {
                    z = false;
                }
                saveAcceptUnvalidated(z);
                return;
            case CMD_REQUEST_BANDWIDTH_UPDATE /*528394*/:
                long currentTimeMs = System.currentTimeMillis();
                if (currentTimeMs >= this.mLastBwRefreshTime + BW_REFRESH_MIN_WIN_MS) {
                    this.mPollLceScheduled = false;
                    if (!this.mPollLcePending.getAndSet(true)) {
                        pollLceData();
                        return;
                    }
                    return;
                } else if (!this.mPollLceScheduled) {
                    this.mPollLceScheduled = sendEmptyMessageDelayed(CMD_REQUEST_BANDWIDTH_UPDATE, ((this.mLastBwRefreshTime + BW_REFRESH_MIN_WIN_MS) - currentTimeMs) + 1);
                    return;
                } else {
                    return;
                }
            case CMD_START_PACKET_KEEPALIVE /*528395*/:
                startPacketKeepalive(msg);
                return;
            case CMD_STOP_PACKET_KEEPALIVE /*528396*/:
                stopPacketKeepalive(msg);
                return;
            case CMD_SET_SIGNAL_STRENGTH_THRESHOLDS /*528398*/:
                ArrayList<Integer> thresholds = ((Bundle) msg.obj).getIntegerArrayList("thresholds");
                int[] intThresholds = new int[(thresholds != null ? thresholds.size() : 0)];
                while (true) {
                    int i2 = i;
                    if (i2 < intThresholds.length) {
                        intThresholds[i2] = thresholds.get(i2).intValue();
                        i = i2 + 1;
                    } else {
                        setSignalStrengthThresholds(intThresholds);
                        return;
                    }
                }
            case CMD_PREVENT_AUTOMATIC_RECONNECT /*528399*/:
                preventAutomaticReconnect();
                return;
            default:
                return;
        }
    }

    private void queueOrSendMessage(int what, Object obj) {
        queueOrSendMessage(what, 0, 0, obj);
    }

    private void queueOrSendMessage(int what, int arg1, int arg2) {
        queueOrSendMessage(what, arg1, arg2, null);
    }

    private void queueOrSendMessage(int what, int arg1, int arg2, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        queueOrSendMessage(msg);
    }

    private void queueOrSendMessage(Message msg) {
        synchronized (this.mPreConnectedQueue) {
            if (this.mAsyncChannel != null) {
                this.mAsyncChannel.sendMessage(msg);
            } else {
                this.mPreConnectedQueue.add(msg);
            }
        }
    }

    public void sendLinkProperties(LinkProperties linkProperties) {
        queueOrSendMessage(EVENT_NETWORK_PROPERTIES_CHANGED, new LinkProperties(linkProperties));
    }

    public void sendNetworkInfo(NetworkInfo networkInfo) {
        queueOrSendMessage(EVENT_NETWORK_INFO_CHANGED, new NetworkInfo(networkInfo));
    }

    public void sendNetworkCapabilities(NetworkCapabilities networkCapabilities) {
        this.mPollLcePending.set(false);
        this.mLastBwRefreshTime = System.currentTimeMillis();
        queueOrSendMessage(EVENT_NETWORK_CAPABILITIES_CHANGED, new NetworkCapabilities(networkCapabilities));
    }

    public void sendNetworkScore(int score) {
        if (score >= 0) {
            queueOrSendMessage(EVENT_NETWORK_SCORE_CHANGED, new Integer(score));
            return;
        }
        throw new IllegalArgumentException("Score must be >= 0");
    }

    public void sendWifiApType(int type) {
        queueOrSendMessage(CMD_UPDATE_WIFI_AP_TYPE, type, 0);
    }

    public void duplexSelected(int type, boolean acceptUnvalidated) {
        queueOrSendMessage(CMD_CONNECT_TO_WLAN_AND_CELLULAR, type, 0, Boolean.valueOf(acceptUnvalidated));
    }

    public void sendRematchNetworkAndRequests(NetworkInfo networkInfo) {
        queueOrSendMessage(EVENT_REMATCH_NETWORK_AND_REQUESTS, new NetworkInfo(networkInfo));
    }

    public void explicitlySelected(boolean acceptUnvalidated) {
        queueOrSendMessage(EVENT_SET_EXPLICITLY_SELECTED, Boolean.valueOf(acceptUnvalidated));
    }

    /* access modifiers changed from: protected */
    public void pollLceData() {
    }

    /* access modifiers changed from: protected */
    public void networkStatus(int status, String redirectUrl) {
    }

    /* access modifiers changed from: protected */
    public void saveAcceptUnvalidated(boolean accept) {
    }

    /* access modifiers changed from: protected */
    public void startPacketKeepalive(Message msg) {
        onPacketKeepaliveEvent(msg.arg1, -30);
    }

    /* access modifiers changed from: protected */
    public void stopPacketKeepalive(Message msg) {
        onPacketKeepaliveEvent(msg.arg1, -30);
    }

    public void onPacketKeepaliveEvent(int slot, int reason) {
        queueOrSendMessage(EVENT_PACKET_KEEPALIVE, slot, reason);
    }

    /* access modifiers changed from: protected */
    public void setSignalStrengthThresholds(int[] thresholds) {
    }

    /* access modifiers changed from: protected */
    public void preventAutomaticReconnect() {
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        String str = this.LOG_TAG;
        Log.d(str, "NetworkAgent: " + s);
    }
}
