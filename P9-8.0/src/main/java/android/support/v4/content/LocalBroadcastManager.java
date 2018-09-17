package android.support.v4.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.huawei.nearbysdk.DTCP.DTCPValueDef;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public final class LocalBroadcastManager {
    private static final boolean DEBUG = false;
    static final int MSG_EXEC_PENDING_BROADCASTS = 1;
    private static final String TAG = "LocalBroadcastManager";
    private static LocalBroadcastManager mInstance;
    private static final Object mLock = new Object();
    private final HashMap<String, ArrayList<ReceiverRecord>> mActions = new HashMap();
    private final Context mAppContext;
    private final Handler mHandler;
    private final ArrayList<BroadcastRecord> mPendingBroadcasts = new ArrayList();
    private final HashMap<BroadcastReceiver, ArrayList<ReceiverRecord>> mReceivers = new HashMap();

    private static final class BroadcastRecord {
        final Intent intent;
        final ArrayList<ReceiverRecord> receivers;

        BroadcastRecord(Intent _intent, ArrayList<ReceiverRecord> _receivers) {
            this.intent = _intent;
            this.receivers = _receivers;
        }
    }

    private static final class ReceiverRecord {
        boolean broadcasting;
        boolean dead;
        final IntentFilter filter;
        final BroadcastReceiver receiver;

        ReceiverRecord(IntentFilter _filter, BroadcastReceiver _receiver) {
            this.filter = _filter;
            this.receiver = _receiver;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder(128);
            builder.append("Receiver{");
            builder.append(this.receiver);
            builder.append(" filter=");
            builder.append(this.filter);
            if (this.dead) {
                builder.append(" DEAD");
            }
            builder.append("}");
            return builder.toString();
        }
    }

    public static LocalBroadcastManager getInstance(Context context) {
        LocalBroadcastManager localBroadcastManager;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new LocalBroadcastManager(context.getApplicationContext());
            }
            localBroadcastManager = mInstance;
        }
        return localBroadcastManager;
    }

    private LocalBroadcastManager(Context context) {
        this.mAppContext = context;
        this.mHandler = new Handler(context.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        LocalBroadcastManager.this.executePendingBroadcasts();
                        return;
                    default:
                        super.handleMessage(msg);
                        return;
                }
            }
        };
    }

    public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        synchronized (this.mReceivers) {
            ReceiverRecord entry = new ReceiverRecord(filter, receiver);
            ArrayList<ReceiverRecord> filters = (ArrayList) this.mReceivers.get(receiver);
            if (filters == null) {
                filters = new ArrayList(1);
                this.mReceivers.put(receiver, filters);
            }
            filters.add(entry);
            for (int i = 0; i < filter.countActions(); i++) {
                String action = filter.getAction(i);
                ArrayList<ReceiverRecord> entries = (ArrayList) this.mActions.get(action);
                if (entries == null) {
                    entries = new ArrayList(1);
                    this.mActions.put(action, entries);
                }
                entries.add(entry);
            }
        }
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        synchronized (this.mReceivers) {
            ArrayList<ReceiverRecord> filters = (ArrayList) this.mReceivers.remove(receiver);
            if (filters == null) {
                return;
            }
            for (int i = filters.size() - 1; i >= 0; i--) {
                ReceiverRecord filter = (ReceiverRecord) filters.get(i);
                filter.dead = true;
                for (int j = 0; j < filter.filter.countActions(); j++) {
                    String action = filter.filter.getAction(j);
                    ArrayList<ReceiverRecord> receivers = (ArrayList) this.mActions.get(action);
                    if (receivers != null) {
                        for (int k = receivers.size() - 1; k >= 0; k--) {
                            ReceiverRecord rec = (ReceiverRecord) receivers.get(k);
                            if (rec.receiver == receiver) {
                                rec.dead = true;
                                receivers.remove(k);
                            }
                        }
                        if (receivers.size() <= 0) {
                            this.mActions.remove(action);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:56:0x018b, code:
            return DEBUG;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean sendBroadcast(Intent intent) {
        synchronized (this.mReceivers) {
            String action = intent.getAction();
            String type = intent.resolveTypeIfNeeded(this.mAppContext.getContentResolver());
            Uri data = intent.getData();
            String scheme = intent.getScheme();
            Set<String> categories = intent.getCategories();
            boolean debug = (intent.getFlags() & 8) != 0 ? true : DEBUG;
            if (debug) {
                Log.v(TAG, "Resolving type " + type + " scheme " + scheme + " of intent " + intent);
            }
            ArrayList<ReceiverRecord> entries = (ArrayList) this.mActions.get(intent.getAction());
            if (entries != null) {
                int i;
                if (debug) {
                    Log.v(TAG, "Action list: " + entries);
                }
                ArrayList receivers = null;
                for (i = 0; i < entries.size(); i++) {
                    ReceiverRecord receiver = (ReceiverRecord) entries.get(i);
                    if (debug) {
                        Log.v(TAG, "Matching against filter " + receiver.filter);
                    }
                    if (!receiver.broadcasting) {
                        int match = receiver.filter.match(action, type, scheme, data, categories, TAG);
                        if (match >= 0) {
                            if (debug) {
                                Log.v(TAG, "  Filter matched!  match=0x" + Integer.toHexString(match));
                            }
                            if (receivers == null) {
                                receivers = new ArrayList();
                            }
                            receivers.add(receiver);
                            receiver.broadcasting = true;
                        } else if (debug) {
                            String reason;
                            switch (match) {
                                case DTCPValueDef.ERROR_IO /*-4*/:
                                    reason = "category";
                                    break;
                                case -3:
                                    reason = "action";
                                    break;
                                case -2:
                                    reason = "data";
                                    break;
                                case -1:
                                    reason = "type";
                                    break;
                                default:
                                    reason = "unknown reason";
                                    break;
                            }
                            Log.v(TAG, "  Filter did not match: " + reason);
                        } else {
                            continue;
                        }
                    } else if (debug) {
                        Log.v(TAG, "  Filter's target already added");
                    }
                }
                if (receivers != null) {
                    for (i = 0; i < receivers.size(); i++) {
                        ((ReceiverRecord) receivers.get(i)).broadcasting = DEBUG;
                    }
                    this.mPendingBroadcasts.add(new BroadcastRecord(intent, receivers));
                    if (!this.mHandler.hasMessages(1)) {
                        this.mHandler.sendEmptyMessage(1);
                    }
                    return true;
                }
            }
        }
    }

    public void sendBroadcastSync(Intent intent) {
        if (sendBroadcast(intent)) {
            executePendingBroadcasts();
        }
    }

    /* JADX WARNING: Missing block: B:10:0x001a, code:
            r3 = 0;
     */
    /* JADX WARNING: Missing block: B:12:0x001c, code:
            if (r3 >= r2.length) goto L_0x0000;
     */
    /* JADX WARNING: Missing block: B:13:0x001e, code:
            r1 = r2[r3];
            r5 = r1.receivers.size();
            r4 = 0;
     */
    /* JADX WARNING: Missing block: B:14:0x0027, code:
            if (r4 >= r5) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:15:0x0029, code:
            r6 = (android.support.v4.content.LocalBroadcastManager.ReceiverRecord) r1.receivers.get(r4);
     */
    /* JADX WARNING: Missing block: B:16:0x0033, code:
            if (r6.dead != false) goto L_0x003e;
     */
    /* JADX WARNING: Missing block: B:17:0x0035, code:
            r6.receiver.onReceive(r10.mAppContext, r1.intent);
     */
    /* JADX WARNING: Missing block: B:18:0x003e, code:
            r4 = r4 + 1;
     */
    /* JADX WARNING: Missing block: B:22:0x0044, code:
            r3 = r3 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void executePendingBroadcasts() {
        while (true) {
            synchronized (this.mReceivers) {
                int N = this.mPendingBroadcasts.size();
                if (N <= 0) {
                    return;
                }
                BroadcastRecord[] brs = new BroadcastRecord[N];
                this.mPendingBroadcasts.toArray(brs);
                this.mPendingBroadcasts.clear();
            }
        }
    }
}
