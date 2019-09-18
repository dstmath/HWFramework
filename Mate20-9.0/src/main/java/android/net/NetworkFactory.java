package android.net;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class NetworkFactory extends Handler {
    private static final int BASE = 536576;
    public static final int CMD_CANCEL_REQUEST = 536577;
    public static final int CMD_REQUEST_NETWORK = 536576;
    private static final int CMD_SET_FILTER = 536579;
    private static final int CMD_SET_SCORE = 536578;
    private static final boolean DBG = true;
    private static final boolean VDBG = false;
    static boolean mDualCellDataEnable;
    private final String LOG_TAG;
    private NetworkCapabilities mCapabilityFilter;
    private final Context mContext;
    private Messenger mMessenger = null;
    private final SparseArray<NetworkRequestInfo> mNetworkRequests = new SparseArray<>();
    private int mRefCount = 0;
    private int mScore;

    private class NetworkRequestInfo {
        public final NetworkRequest request;
        public boolean requested = false;
        public int score;

        public NetworkRequestInfo(NetworkRequest request2, int score2) {
            this.request = request2;
            this.score = score2;
        }

        public String toString() {
            return "{" + this.request + ", score=" + this.score + ", requested=" + this.requested + "}";
        }
    }

    public NetworkFactory(Looper looper, Context context, String logTag, NetworkCapabilities filter) {
        super(looper);
        this.LOG_TAG = logTag;
        this.mContext = context;
        this.mCapabilityFilter = filter;
    }

    public void register() {
        log("Registering NetworkFactory");
        if (this.mMessenger == null) {
            this.mMessenger = new Messenger(this);
            ConnectivityManager.from(this.mContext).registerNetworkFactory(this.mMessenger, this.LOG_TAG);
        }
    }

    public void unregister() {
        log("Unregistering NetworkFactory");
        if (this.mMessenger != null) {
            ConnectivityManager.from(this.mContext).unregisterNetworkFactory(this.mMessenger);
            this.mMessenger = null;
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 536576:
                handleAddRequest((NetworkRequest) msg.obj, msg.arg1);
                return;
            case CMD_CANCEL_REQUEST /*536577*/:
                handleRemoveRequest((NetworkRequest) msg.obj);
                return;
            case CMD_SET_SCORE /*536578*/:
                handleSetScore(msg.arg1);
                return;
            case CMD_SET_FILTER /*536579*/:
                handleSetFilter((NetworkCapabilities) msg.obj);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void handleAddRequest(NetworkRequest request, int score) {
        NetworkRequestInfo n = this.mNetworkRequests.get(request.requestId);
        if (request.networkCapabilities.isDualCellData()) {
            setDualCellDataEnable(true);
            addNonDdsInternetCap();
        }
        if (n == null) {
            log("got request " + request + " with score " + score);
            n = new NetworkRequestInfo(request, score);
            this.mNetworkRequests.put(n.request.requestId, n);
        } else {
            n.score = score;
        }
        evalRequest(n);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void handleRemoveRequest(NetworkRequest request) {
        NetworkRequestInfo n = this.mNetworkRequests.get(request.requestId);
        if (n != null) {
            this.mNetworkRequests.remove(request.requestId);
            if (n.requested) {
                releaseNetworkFor(n.request);
            }
        }
    }

    private void handleSetScore(int score) {
        this.mScore = score;
        evalRequests();
    }

    private void handleSetFilter(NetworkCapabilities netCap) {
        this.mCapabilityFilter = netCap;
        evalRequests();
    }

    public boolean acceptRequest(NetworkRequest request, int score) {
        return true;
    }

    private void evalRequest(NetworkRequestInfo n) {
        if (!n.requested && n.score < this.mScore && n.request.networkCapabilities.satisfiedByNetworkCapabilities(this.mCapabilityFilter) && acceptRequest(n.request, n.score)) {
            needNetworkFor(n.request, n.score);
            n.requested = true;
        } else if (!n.requested) {
        } else {
            if (n.score > this.mScore || !n.request.networkCapabilities.satisfiedByNetworkCapabilities(this.mCapabilityFilter) || !acceptRequest(n.request, n.score)) {
                releaseNetworkFor(n.request);
                n.requested = false;
            }
        }
    }

    /* access modifiers changed from: private */
    public void evalRequests() {
        for (int i = 0; i < this.mNetworkRequests.size(); i++) {
            evalRequest(this.mNetworkRequests.valueAt(i));
        }
    }

    /* access modifiers changed from: protected */
    public void reevaluateAllRequests() {
        post(new Runnable() {
            public final void run() {
                NetworkFactory.this.evalRequests();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void startNetwork() {
    }

    /* access modifiers changed from: protected */
    public void stopNetwork() {
    }

    /* access modifiers changed from: protected */
    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        int i = this.mRefCount + 1;
        this.mRefCount = i;
        if (i == 1) {
            startNetwork();
        }
    }

    /* access modifiers changed from: protected */
    public void releaseNetworkFor(NetworkRequest networkRequest) {
        int i = this.mRefCount - 1;
        this.mRefCount = i;
        if (i == 0) {
            stopNetwork();
        }
    }

    public void addNetworkRequest(NetworkRequest networkRequest, int score) {
        sendMessage(obtainMessage(536576, new NetworkRequestInfo(networkRequest, score)));
    }

    public void removeNetworkRequest(NetworkRequest networkRequest) {
        sendMessage(obtainMessage(CMD_CANCEL_REQUEST, networkRequest));
    }

    public void setScoreFilter(int score) {
        sendMessage(obtainMessage(CMD_SET_SCORE, score, 0));
    }

    public void setCapabilityFilter(NetworkCapabilities netCap) {
        sendMessage(obtainMessage(CMD_SET_FILTER, new NetworkCapabilities(netCap)));
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getRequestCount() {
        return this.mNetworkRequests.size();
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Log.d(this.LOG_TAG, s);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println(toString());
        pw.increaseIndent();
        for (int i = 0; i < this.mNetworkRequests.size(); i++) {
            pw.println(this.mNetworkRequests.valueAt(i));
        }
        pw.decreaseIndent();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        sb.append(this.LOG_TAG);
        sb.append(" - ScoreFilter=");
        sb.append(this.mScore);
        sb.append(", Filter=");
        sb.append(this.mCapabilityFilter);
        sb.append(", requests=");
        sb.append(this.mNetworkRequests.size());
        sb.append(", refCount=");
        sb.append(this.mRefCount);
        return sb.append("}").toString();
    }

    public static boolean isDualCellDataEnable() {
        return mDualCellDataEnable;
    }

    public static void setDualCellDataEnable(boolean enable) {
        mDualCellDataEnable = enable;
    }

    /* access modifiers changed from: protected */
    public void addNonDdsInternetCap() {
    }

    public NetworkRequest getTopPriorityRequest() {
        return null;
    }

    public NetworkRequest getFirstMMSRequest() {
        return null;
    }
}
