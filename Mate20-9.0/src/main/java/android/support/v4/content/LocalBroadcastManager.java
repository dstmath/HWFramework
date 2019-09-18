package android.support.v4.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public final class LocalBroadcastManager {
    private static final boolean DEBUG = false;
    static final int MSG_EXEC_PENDING_BROADCASTS = 1;
    private static final String TAG = "LocalBroadcastManager";
    private static LocalBroadcastManager mInstance;
    private static final Object mLock = new Object();
    private final HashMap<String, ArrayList<ReceiverRecord>> mActions = new HashMap<>();
    private final Context mAppContext;
    private final Handler mHandler;
    private final ArrayList<BroadcastRecord> mPendingBroadcasts = new ArrayList<>();
    private final HashMap<BroadcastReceiver, ArrayList<ReceiverRecord>> mReceivers = new HashMap<>();

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

    @NonNull
    public static LocalBroadcastManager getInstance(@NonNull Context context) {
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
                if (msg.what != 1) {
                    super.handleMessage(msg);
                } else {
                    LocalBroadcastManager.this.executePendingBroadcasts();
                }
            }
        };
    }

    public void registerReceiver(@NonNull BroadcastReceiver receiver, @NonNull IntentFilter filter) {
        synchronized (this.mReceivers) {
            ReceiverRecord entry = new ReceiverRecord(filter, receiver);
            ArrayList<ReceiverRecord> filters = this.mReceivers.get(receiver);
            if (filters == null) {
                filters = new ArrayList<>(1);
                this.mReceivers.put(receiver, filters);
            }
            filters.add(entry);
            for (int i = 0; i < filter.countActions(); i++) {
                String action = filter.getAction(i);
                ArrayList<ReceiverRecord> entries = this.mActions.get(action);
                if (entries == null) {
                    entries = new ArrayList<>(1);
                    this.mActions.put(action, entries);
                }
                entries.add(entry);
            }
        }
    }

    public void unregisterReceiver(@NonNull BroadcastReceiver receiver) {
        synchronized (this.mReceivers) {
            ArrayList<ReceiverRecord> filters = this.mReceivers.remove(receiver);
            if (filters != null) {
                for (int i = filters.size() - 1; i >= 0; i--) {
                    ReceiverRecord filter = filters.get(i);
                    filter.dead = true;
                    for (int j = 0; j < filter.filter.countActions(); j++) {
                        String action = filter.filter.getAction(j);
                        ArrayList<ReceiverRecord> receivers = this.mActions.get(action);
                        if (receivers != null) {
                            for (int k = receivers.size() - 1; k >= 0; k--) {
                                ReceiverRecord rec = receivers.get(k);
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
    }

    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0160, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0163, code lost:
        return DEBUG;
     */
    public boolean sendBroadcast(@NonNull Intent intent) {
        String type;
        int i;
        ArrayList<ReceiverRecord> receivers;
        String reason;
        Intent intent2 = intent;
        synchronized (this.mReceivers) {
            String action = intent.getAction();
            String type2 = intent2.resolveTypeIfNeeded(this.mAppContext.getContentResolver());
            Uri data = intent.getData();
            String scheme = intent.getScheme();
            Set<String> categories = intent.getCategories();
            boolean debug = (intent.getFlags() & 8) != 0 ? true : DEBUG;
            if (debug) {
                Log.v(TAG, "Resolving type " + type2 + " scheme " + scheme + " of intent " + intent2);
            }
            ArrayList<ReceiverRecord> entries = this.mActions.get(intent.getAction());
            if (entries != null) {
                if (debug) {
                    Log.v(TAG, "Action list: " + entries);
                }
                ArrayList<ReceiverRecord> receivers2 = null;
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 < entries.size()) {
                        ReceiverRecord receiver = entries.get(i3);
                        if (debug) {
                            Log.v(TAG, "Matching against filter " + receiver.filter);
                        }
                        if (receiver.broadcasting) {
                            if (debug) {
                                Log.v(TAG, "  Filter's target already added");
                            }
                            type = type2;
                            i = i3;
                            receivers = receivers2;
                        } else {
                            ReceiverRecord receiver2 = receiver;
                            i = i3;
                            type = type2;
                            receivers = receivers2;
                            int match = receiver.filter.match(action, type2, scheme, data, categories, TAG);
                            if (match >= 0) {
                                if (debug) {
                                    Log.v(TAG, "  Filter matched!  match=0x" + Integer.toHexString(match));
                                }
                                if (receivers == null) {
                                    receivers = new ArrayList<>();
                                }
                                receivers.add(receiver2);
                                receiver2.broadcasting = true;
                                receivers2 = receivers;
                                i2 = i + 1;
                                type2 = type;
                            } else if (debug) {
                                switch (match) {
                                    case -4:
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
                            }
                        }
                        receivers2 = receivers;
                        i2 = i + 1;
                        type2 = type;
                    } else {
                        ArrayList<ReceiverRecord> receivers3 = receivers2;
                        if (receivers3 != null) {
                            for (int i4 = 0; i4 < receivers3.size(); i4++) {
                                receivers3.get(i4).broadcasting = DEBUG;
                            }
                            this.mPendingBroadcasts.add(new BroadcastRecord(intent2, receivers3));
                            if (!this.mHandler.hasMessages(1)) {
                                this.mHandler.sendEmptyMessage(1);
                            }
                        }
                    }
                }
            }
        }
    }

    public void sendBroadcastSync(@NonNull Intent intent) {
        if (sendBroadcast(intent)) {
            executePendingBroadcasts();
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
        if (r2 >= r0.length) goto L_0x0001;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0021, code lost:
        r3 = r0[r2];
        r4 = r3.receivers.size();
        r5 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
        if (r5 >= r4) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002c, code lost:
        r6 = r3.receivers.get(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0036, code lost:
        if (r6.dead != false) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0038, code lost:
        r6.receiver.onReceive(r10.mAppContext, r3.intent);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0041, code lost:
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0044, code lost:
        r2 = r2 + 1;
     */
    public void executePendingBroadcasts() {
        while (true) {
            synchronized (this.mReceivers) {
                int N = this.mPendingBroadcasts.size();
                if (N > 0) {
                    BroadcastRecord[] brs = new BroadcastRecord[N];
                    this.mPendingBroadcasts.toArray(brs);
                    this.mPendingBroadcasts.clear();
                } else {
                    return;
                }
            }
        }
        while (true) {
        }
    }
}
