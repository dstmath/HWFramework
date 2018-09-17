package android.net;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.util.SparseArray;
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
    private final SparseArray<NetworkRequestInfo> mNetworkRequests = new SparseArray();
    private int mRefCount = 0;
    private int mScore;

    private class NetworkRequestInfo {
        public final NetworkRequest request;
        public boolean requested = false;
        public int score;

        public NetworkRequestInfo(NetworkRequest request, int score) {
            this.request = request;
            this.score = score;
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
            this.mMessenger = new Messenger((Handler) this);
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

    protected void handleAddRequest(NetworkRequest request, int score) {
        NetworkRequestInfo n = (NetworkRequestInfo) this.mNetworkRequests.get(request.requestId);
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

    protected void handleRemoveRequest(NetworkRequest request) {
        NetworkRequestInfo n = (NetworkRequestInfo) this.mNetworkRequests.get(request.requestId);
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

    private void evalRequests() {
        for (int i = 0; i < this.mNetworkRequests.size(); i++) {
            evalRequest((NetworkRequestInfo) this.mNetworkRequests.valueAt(i));
        }
    }

    protected void startNetwork() {
    }

    protected void stopNetwork() {
    }

    protected void needNetworkFor(NetworkRequest networkRequest, int score) {
        int i = this.mRefCount + 1;
        this.mRefCount = i;
        if (i == 1) {
            startNetwork();
        }
    }

    protected void releaseNetworkFor(NetworkRequest networkRequest) {
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

    protected int getRequestCount() {
        return this.mNetworkRequests.size();
    }

    protected void log(String s) {
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
        return "{" + this.LOG_TAG + " - ScoreFilter=" + this.mScore + ", Filter=" + this.mCapabilityFilter + ", requests=" + this.mNetworkRequests.size() + ", refCount=" + this.mRefCount + "}";
    }

    public static boolean isDualCellDataEnable() {
        return mDualCellDataEnable;
    }

    public static void setDualCellDataEnable(boolean enable) {
        mDualCellDataEnable = enable;
    }

    protected void addNonDdsInternetCap() {
    }

    public NetworkRequest getTopPriorityRequest() {
        return null;
    }

    public NetworkRequest getFirstMMSRequest() {
        return null;
    }
}
