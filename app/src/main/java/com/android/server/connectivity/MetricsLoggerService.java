package com.android.server.connectivity;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.net.ConnectivityMetricsEvent;
import android.net.ConnectivityMetricsEvent.Reference;
import android.net.IConnectivityMetricsLogger.Stub;
import android.os.Binder;
import android.os.Parcel;
import android.text.format.DateUtils;
import android.util.Log;
import com.android.server.SystemService;
import com.android.server.am.ProcessList;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class MetricsLoggerService extends SystemService {
    private static final boolean DBG = true;
    private static String TAG;
    private static final boolean VDBG = false;
    private final int EVENTS_NOTIFICATION_THRESHOLD;
    private final int MAX_NUMBER_OF_EVENTS;
    private final int THROTTLING_MAX_NUMBER_OF_MESSAGES_PER_COMPONENT;
    private final int THROTTLING_TIME_INTERVAL_MILLIS;
    private final Stub mBinder;
    private DnsEventListenerService mDnsListener;
    private int mEventCounter;
    private final ArrayDeque<ConnectivityMetricsEvent> mEvents;
    private long mLastEventReference;
    private final int[] mThrottlingCounters;
    private long mThrottlingIntervalBoundaryMillis;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.connectivity.MetricsLoggerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.connectivity.MetricsLoggerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.MetricsLoggerService.<clinit>():void");
    }

    public MetricsLoggerService(Context context) {
        super(context);
        this.MAX_NUMBER_OF_EVENTS = ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE;
        this.EVENTS_NOTIFICATION_THRESHOLD = 300;
        this.THROTTLING_TIME_INTERVAL_MILLIS = 3600000;
        this.THROTTLING_MAX_NUMBER_OF_MESSAGES_PER_COMPONENT = ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE;
        this.mEventCounter = 0;
        this.mLastEventReference = 0;
        this.mThrottlingCounters = new int[5];
        this.mEvents = new ArrayDeque();
        this.mBinder = new Stub() {
            private final ArrayList<PendingIntent> mPendingIntents;

            {
                this.mPendingIntents = new ArrayList();
            }

            protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                if (MetricsLoggerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                    pw.println("Permission Denial: can't dump ConnectivityMetricsLoggerService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                    return;
                }
                boolean dumpSerializedSize = false;
                boolean dumpEvents = false;
                boolean dumpDebugInfo = false;
                for (String arg : args) {
                    if (arg.equals("--debug")) {
                        dumpDebugInfo = MetricsLoggerService.DBG;
                    } else if (arg.equals("--events")) {
                        dumpEvents = MetricsLoggerService.DBG;
                    } else if (arg.equals("--size")) {
                        dumpSerializedSize = MetricsLoggerService.DBG;
                    } else if (arg.equals("--all")) {
                        dumpDebugInfo = MetricsLoggerService.DBG;
                        dumpEvents = MetricsLoggerService.DBG;
                        dumpSerializedSize = MetricsLoggerService.DBG;
                    }
                }
                synchronized (MetricsLoggerService.this.mEvents) {
                    pw.println("Number of events: " + MetricsLoggerService.this.mEvents.size());
                    pw.println("Counter: " + MetricsLoggerService.this.mEventCounter);
                    if (MetricsLoggerService.this.mEvents.size() > 0) {
                        pw.println("Time span: " + DateUtils.formatElapsedTime((System.currentTimeMillis() - ((ConnectivityMetricsEvent) MetricsLoggerService.this.mEvents.peekFirst()).timestamp) / 1000));
                    }
                    if (dumpSerializedSize) {
                        Parcel p = Parcel.obtain();
                        for (ConnectivityMetricsEvent e : MetricsLoggerService.this.mEvents) {
                            p.writeParcelable(e, 0);
                        }
                        pw.println("Serialized data size: " + p.dataSize());
                        p.recycle();
                    }
                    if (dumpEvents) {
                        pw.println();
                        pw.println("Events:");
                        for (ConnectivityMetricsEvent e2 : MetricsLoggerService.this.mEvents) {
                            pw.println(e2.toString());
                        }
                    }
                }
                if (dumpDebugInfo) {
                    synchronized (MetricsLoggerService.this.mThrottlingCounters) {
                        pw.println();
                        for (int i = 0; i < 5; i++) {
                            if (MetricsLoggerService.this.mThrottlingCounters[i] > 0) {
                                pw.println("Throttling Counter #" + i + ": " + MetricsLoggerService.this.mThrottlingCounters[i]);
                            }
                        }
                        pw.println("Throttling Time Remaining: " + DateUtils.formatElapsedTime((MetricsLoggerService.this.mThrottlingIntervalBoundaryMillis - System.currentTimeMillis()) / 1000));
                    }
                }
                synchronized (this.mPendingIntents) {
                    if (!this.mPendingIntents.isEmpty()) {
                        pw.println();
                        pw.println("Pending intents:");
                        for (PendingIntent pi : this.mPendingIntents) {
                            pw.println(pi.toString());
                        }
                    }
                }
                pw.println();
                MetricsLoggerService.this.mDnsListener.dump(pw);
            }

            public long logEvent(ConnectivityMetricsEvent event) {
                return logEvents(new ConnectivityMetricsEvent[]{event});
            }

            public long logEvents(ConnectivityMetricsEvent[] events) {
                MetricsLoggerService.this.enforceConnectivityInternalPermission();
                if (events == null || events.length == 0) {
                    Log.wtf(MetricsLoggerService.TAG, "No events passed to logEvents()");
                    return -1;
                }
                int componentTag = events[0].componentTag;
                if (componentTag < 0 || componentTag >= 5) {
                    Log.wtf(MetricsLoggerService.TAG, "Unexpected tag: " + componentTag);
                    return -1;
                }
                synchronized (MetricsLoggerService.this.mThrottlingCounters) {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis > MetricsLoggerService.this.mThrottlingIntervalBoundaryMillis) {
                        MetricsLoggerService.this.resetThrottlingCounters(currentTimeMillis);
                    }
                    int[] -get5 = MetricsLoggerService.this.mThrottlingCounters;
                    -get5[componentTag] = -get5[componentTag] + events.length;
                    if (MetricsLoggerService.this.mThrottlingCounters[componentTag] > 1000) {
                        Log.w(MetricsLoggerService.TAG, "Too many events from #" + componentTag + ". Block until " + MetricsLoggerService.this.mThrottlingIntervalBoundaryMillis);
                        return MetricsLoggerService.this.mThrottlingIntervalBoundaryMillis;
                    }
                    boolean sendPendingIntents = false;
                    synchronized (MetricsLoggerService.this.mEvents) {
                        for (ConnectivityMetricsEvent e : events) {
                            if (e.componentTag != componentTag) {
                                Log.wtf(MetricsLoggerService.TAG, "Unexpected tag: " + e.componentTag);
                                return -1;
                            }
                            MetricsLoggerService.this.addEvent(e);
                        }
                        MetricsLoggerService metricsLoggerService = MetricsLoggerService.this;
                        metricsLoggerService.mLastEventReference = metricsLoggerService.mLastEventReference + ((long) events.length);
                        metricsLoggerService = MetricsLoggerService.this;
                        metricsLoggerService.mEventCounter = metricsLoggerService.mEventCounter + events.length;
                        if (MetricsLoggerService.this.mEventCounter >= 300) {
                            MetricsLoggerService.this.mEventCounter = 0;
                            sendPendingIntents = MetricsLoggerService.DBG;
                        }
                        if (sendPendingIntents) {
                            synchronized (this.mPendingIntents) {
                                for (PendingIntent pi : this.mPendingIntents) {
                                    try {
                                        pi.send(MetricsLoggerService.this.getContext(), 0, null, null, null);
                                    } catch (CanceledException e2) {
                                        Log.e(MetricsLoggerService.TAG, "Pending intent canceled: " + pi);
                                        this.mPendingIntents.remove(pi);
                                    }
                                }
                            }
                        }
                        return 0;
                    }
                }
            }

            public ConnectivityMetricsEvent[] getEvents(Reference reference) {
                MetricsLoggerService.this.enforceDumpPermission();
                long ref = reference.getValue();
                synchronized (MetricsLoggerService.this.mEvents) {
                    if (ref > MetricsLoggerService.this.mLastEventReference) {
                        Log.e(MetricsLoggerService.TAG, "Invalid reference");
                        reference.setValue(MetricsLoggerService.this.mLastEventReference);
                        return null;
                    }
                    if (ref < MetricsLoggerService.this.mLastEventReference - ((long) MetricsLoggerService.this.mEvents.size())) {
                        ref = MetricsLoggerService.this.mLastEventReference - ((long) MetricsLoggerService.this.mEvents.size());
                    }
                    int numEventsToSkip = MetricsLoggerService.this.mEvents.size() - ((int) (MetricsLoggerService.this.mLastEventReference - ref));
                    ConnectivityMetricsEvent[] result = new ConnectivityMetricsEvent[(MetricsLoggerService.this.mEvents.size() - numEventsToSkip)];
                    int i = 0;
                    for (ConnectivityMetricsEvent e : MetricsLoggerService.this.mEvents) {
                        int i2;
                        if (numEventsToSkip > 0) {
                            numEventsToSkip--;
                            i2 = i;
                        } else {
                            i2 = i + 1;
                            result[i] = e;
                        }
                        i = i2;
                    }
                    reference.setValue(MetricsLoggerService.this.mLastEventReference);
                    return result;
                }
            }

            public boolean register(PendingIntent newEventsIntent) {
                MetricsLoggerService.this.enforceDumpPermission();
                synchronized (this.mPendingIntents) {
                    if (this.mPendingIntents.remove(newEventsIntent)) {
                        Log.w(MetricsLoggerService.TAG, "Replacing registered pending intent");
                    }
                    this.mPendingIntents.add(newEventsIntent);
                }
                return MetricsLoggerService.DBG;
            }

            public void unregister(PendingIntent newEventsIntent) {
                MetricsLoggerService.this.enforceDumpPermission();
                synchronized (this.mPendingIntents) {
                    if (!this.mPendingIntents.remove(newEventsIntent)) {
                        Log.e(MetricsLoggerService.TAG, "Pending intent is not registered");
                    }
                }
            }
        };
    }

    public void onStart() {
        resetThrottlingCounters(System.currentTimeMillis());
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
            Log.d(TAG, "onBootPhase: PHASE_SYSTEM_SERVICES_READY");
            publishBinderService("connectivity_metrics_logger", this.mBinder);
            this.mDnsListener = new DnsEventListenerService(getContext());
            publishBinderService(DnsEventListenerService.SERVICE_NAME, this.mDnsListener);
        }
    }

    private void enforceConnectivityInternalPermission() {
        getContext().enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "MetricsLoggerService");
    }

    private void enforceDumpPermission() {
        getContext().enforceCallingOrSelfPermission("android.permission.DUMP", "MetricsLoggerService");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void resetThrottlingCounters(long currentTimeMillis) {
        synchronized (this.mThrottlingCounters) {
            int i = 0;
            while (true) {
                if (i < this.mThrottlingCounters.length) {
                    this.mThrottlingCounters[i] = 0;
                    i++;
                } else {
                    this.mThrottlingIntervalBoundaryMillis = 3600000 + currentTimeMillis;
                }
            }
        }
    }

    private void addEvent(ConnectivityMetricsEvent e) {
        while (this.mEvents.size() >= ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            this.mEvents.removeFirst();
        }
        this.mEvents.addLast(e);
    }
}
