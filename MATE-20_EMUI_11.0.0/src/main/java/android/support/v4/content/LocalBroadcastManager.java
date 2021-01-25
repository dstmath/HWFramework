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

    /* access modifiers changed from: private */
    public static final class ReceiverRecord {
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

    /* access modifiers changed from: private */
    public static final class BroadcastRecord {
        final Intent intent;
        final ArrayList<ReceiverRecord> receivers;

        BroadcastRecord(Intent _intent, ArrayList<ReceiverRecord> _receivers) {
            this.intent = _intent;
            this.receivers = _receivers;
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
            /* class android.support.v4.content.LocalBroadcastManager.AnonymousClass1 */

            @Override // android.os.Handler
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

    /* JADX INFO: Multiple debug info for r0v9 java.util.ArrayList<android.support.v4.content.LocalBroadcastManager$ReceiverRecord>: [D('type' java.lang.String), D('receivers' java.util.ArrayList<android.support.v4.content.LocalBroadcastManager$ReceiverRecord>)] */
    public boolean sendBroadcast(@NonNull Intent intent) {
        String type;
        int i;
        ArrayList<ReceiverRecord> receivers;
        String reason;
        synchronized (this.mReceivers) {
            String action = intent.getAction();
            String type2 = intent.resolveTypeIfNeeded(this.mAppContext.getContentResolver());
            Uri data = intent.getData();
            String scheme = intent.getScheme();
            Set<String> categories = intent.getCategories();
            boolean debug = (intent.getFlags() & 8) != 0 ? true : DEBUG;
            if (debug) {
                Log.v(TAG, "Resolving type " + type2 + " scheme " + scheme + " of intent " + intent);
            }
            ArrayList<ReceiverRecord> entries = this.mActions.get(intent.getAction());
            if (entries != null) {
                if (debug) {
                    Log.v(TAG, "Action list: " + entries);
                }
                ArrayList<ReceiverRecord> receivers2 = null;
                int i2 = 0;
                while (i2 < entries.size()) {
                    ReceiverRecord receiver = entries.get(i2);
                    if (debug) {
                        Log.v(TAG, "Matching against filter " + receiver.filter);
                    }
                    if (receiver.broadcasting) {
                        if (debug) {
                            Log.v(TAG, "  Filter's target already added");
                        }
                        type = type2;
                        i = i2;
                        receivers = receivers2;
                    } else {
                        i = i2;
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
                            receivers.add(receiver);
                            receiver.broadcasting = true;
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
                }
                if (receivers2 != null) {
                    for (int i3 = 0; i3 < receivers2.size(); i3++) {
                        receivers2.get(i3).broadcasting = DEBUG;
                    }
                    this.mPendingBroadcasts.add(new BroadcastRecord(intent, receivers2));
                    if (!this.mHandler.hasMessages(1)) {
                        this.mHandler.sendEmptyMessage(1);
                    }
                    return true;
                }
            }
            return DEBUG;
        }
    }

    public void sendBroadcastSync(@NonNull Intent intent) {
        if (sendBroadcast(intent)) {
            executePendingBroadcasts();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void executePendingBroadcasts() {
        BroadcastRecord[] brs;
        while (true) {
            synchronized (this.mReceivers) {
                int N = this.mPendingBroadcasts.size();
                if (N > 0) {
                    brs = new BroadcastRecord[N];
                    this.mPendingBroadcasts.toArray(brs);
                    this.mPendingBroadcasts.clear();
                } else {
                    return;
                }
            }
            for (BroadcastRecord br : brs) {
                int nbr = br.receivers.size();
                for (int j = 0; j < nbr; j++) {
                    ReceiverRecord rec = br.receivers.get(j);
                    if (!rec.dead) {
                        rec.receiver.onReceive(this.mAppContext, br.intent);
                    }
                }
            }
        }
    }
}
