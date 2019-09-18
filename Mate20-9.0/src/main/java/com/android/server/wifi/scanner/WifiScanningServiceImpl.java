package com.android.server.wifi.scanner;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.IWifiScanner;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner;
import android.os.Binder;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.ArrayMap;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.Clock;
import com.android.server.wifi.FrameworkFacade;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.WifiConnectivityHelper;
import com.android.server.wifi.WifiConnectivityManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiLog;
import com.android.server.wifi.WifiMetrics;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.hotspot2.anqp.NAIRealmData;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.scanner.WifiScannerImpl;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.WifiHandler;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class WifiScanningServiceImpl extends IWifiScanner.Stub {
    private static final int BASE = 160000;
    private static final int CMD_DRIVER_LOADED = 160006;
    private static final int CMD_DRIVER_UNLOADED = 160007;
    private static final int CMD_FULL_SCAN_RESULTS = 160001;
    private static final int CMD_PNO_NETWORK_FOUND = 160011;
    private static final int CMD_PNO_SCAN_FAILED = 160012;
    private static final int CMD_SCAN_FAILED = 160010;
    private static final int CMD_SCAN_PAUSED = 160008;
    private static final int CMD_SCAN_RESTARTED = 160009;
    private static final int CMD_SCAN_RESULTS_AVAILABLE = 160000;
    private static final boolean DBG = false;
    private static final String TAG = "WifiScanningService";
    private static final int UNKNOWN_PID = -1;
    /* access modifiers changed from: private */
    public static boolean mSendScanResultsBroadcast = false;
    private final AlarmManager mAlarmManager;
    /* access modifiers changed from: private */
    public WifiBackgroundScanStateMachine mBackgroundScanStateMachine;
    /* access modifiers changed from: private */
    public BackgroundScanScheduler mBackgroundScheduler;
    /* access modifiers changed from: private */
    public final IBatteryStats mBatteryStats;
    /* access modifiers changed from: private */
    public ChannelHelper mChannelHelper;
    /* access modifiers changed from: private */
    public ClientHandler mClientHandler;
    /* access modifiers changed from: private */
    public final ArrayMap<Messenger, ClientInfo> mClients;
    /* access modifiers changed from: private */
    public final Clock mClock;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final FrameworkFacade mFrameworkFacade;
    /* access modifiers changed from: private */
    public final LocalLog mLocalLog = new LocalLog(512);
    private WifiLog mLog;
    /* access modifiers changed from: private */
    public final Looper mLooper;
    /* access modifiers changed from: private */
    public WifiPnoScanStateMachine mPnoScanStateMachine;
    /* access modifiers changed from: private */
    public WifiNative.ScanSettings mPreviousSchedule;
    /* access modifiers changed from: private */
    public WifiScannerImpl mScannerImpl;
    /* access modifiers changed from: private */
    public final WifiScannerImpl.WifiScannerImplFactory mScannerImplFactory;
    /* access modifiers changed from: private */
    public final RequestList<Void> mSingleScanListeners = new RequestList<>();
    /* access modifiers changed from: private */
    public WifiSingleScanStateMachine mSingleScanStateMachine;
    /* access modifiers changed from: private */
    public final WifiMetrics mWifiMetrics;

    private class ClientHandler extends WifiHandler {
        ClientHandler(String tag, Looper looper) {
            super(tag, looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 69633:
                    if (msg.replyTo == null) {
                        WifiScanningServiceImpl.this.logw("msg.replyTo is null");
                        return;
                    }
                    ExternalClientInfo client = (ExternalClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                    if (client != null) {
                        WifiScanningServiceImpl wifiScanningServiceImpl = WifiScanningServiceImpl.this;
                        wifiScanningServiceImpl.logw("duplicate client connection: " + msg.sendingUid + ", messenger=" + msg.replyTo);
                        client.mChannel.replyToMessage(msg, 69634, 3);
                        return;
                    }
                    AsyncChannel ac = WifiScanningServiceImpl.this.mFrameworkFacade.makeWifiAsyncChannel(WifiScanningServiceImpl.TAG);
                    ac.connected(WifiScanningServiceImpl.this.mContext, this, msg.replyTo);
                    ExternalClientInfo client2 = new ExternalClientInfo(msg.sendingUid, msg.replyTo, ac);
                    client2.register();
                    ac.replyToMessage(msg, 69634, 0);
                    WifiScanningServiceImpl wifiScanningServiceImpl2 = WifiScanningServiceImpl.this;
                    wifiScanningServiceImpl2.localLog("client connected: " + client2);
                    return;
                case 69635:
                    ExternalClientInfo client3 = (ExternalClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                    if (client3 != null) {
                        client3.mChannel.disconnect();
                    }
                    return;
                case 69636:
                    ExternalClientInfo client4 = (ExternalClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                    if (!(client4 == null || msg.arg1 == 2 || msg.arg1 == 3)) {
                        WifiScanningServiceImpl wifiScanningServiceImpl3 = WifiScanningServiceImpl.this;
                        wifiScanningServiceImpl3.localLog("client disconnected: " + client4 + ", reason: " + msg.arg1);
                        client4.cleanup();
                    }
                    return;
                default:
                    try {
                        WifiScanningServiceImpl.this.enforceLocationHardwarePermission(msg.sendingUid);
                        if (msg.what == 159748) {
                            WifiScanningServiceImpl.this.mBackgroundScanStateMachine.sendMessage(Message.obtain(msg));
                            return;
                        } else if (msg.what == 159773) {
                            WifiScanningServiceImpl.this.mSingleScanStateMachine.sendMessage(Message.obtain(msg));
                            return;
                        } else {
                            ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                            if (ci == null) {
                                WifiScanningServiceImpl wifiScanningServiceImpl4 = WifiScanningServiceImpl.this;
                                wifiScanningServiceImpl4.loge("Could not find client info for message " + msg.replyTo + ", msg=" + msg);
                                WifiScanningServiceImpl.this.replyFailed(msg, -2, "Could not find listener");
                                return;
                            }
                            switch (msg.what) {
                                case 159746:
                                case 159747:
                                    WifiScanningServiceImpl.this.mBackgroundScanStateMachine.sendMessage(Message.obtain(msg));
                                    break;
                                case 159765:
                                    WifiScanningServiceImpl.this.localLog(WifiScanner.getScanKey(msg.arg2), "20", "start single scan!");
                                    WifiScanningServiceImpl.this.mSingleScanStateMachine.sendMessage(Message.obtain(msg));
                                    break;
                                case 159766:
                                    WifiScanningServiceImpl.this.localLog(WifiScanner.getScanKey(msg.arg2), "21", "stop single scan!");
                                    WifiScanningServiceImpl.this.mSingleScanStateMachine.sendMessage(Message.obtain(msg));
                                    break;
                                case 159768:
                                case 159769:
                                    WifiScanningServiceImpl.this.mPnoScanStateMachine.sendMessage(Message.obtain(msg));
                                    break;
                                case 159771:
                                    WifiScanningServiceImpl.this.logScanRequest("registerScanListener", ci, msg.arg2, null, null, null);
                                    WifiScanningServiceImpl.this.mSingleScanListeners.addRequest(ci, msg.arg2, null, null);
                                    WifiScanningServiceImpl.this.replySucceeded(msg);
                                    break;
                                case 159772:
                                    WifiScanningServiceImpl.this.logScanRequest("deregisterScanListener", ci, msg.arg2, null, null, null);
                                    WifiScanningServiceImpl.this.mSingleScanListeners.removeRequest(ci, msg.arg2);
                                    break;
                                default:
                                    WifiScanningServiceImpl.this.replyFailed(msg, -3, "Invalid request");
                                    break;
                            }
                            return;
                        }
                    } catch (SecurityException e) {
                        WifiScanningServiceImpl wifiScanningServiceImpl5 = WifiScanningServiceImpl.this;
                        wifiScanningServiceImpl5.localLog("failed to authorize app: " + e);
                        WifiScanningServiceImpl.this.replyFailed(msg, -4, "Not authorized");
                        return;
                    }
            }
        }
    }

    private abstract class ClientInfo {
        protected final Messenger mMessenger;
        private boolean mScanWorkReported = false;
        /* access modifiers changed from: private */
        public final int mUid;
        private final WorkSource mWorkSource;

        public abstract void reportEvent(int i, int i2, int i3, Object obj);

        ClientInfo(int uid, Messenger messenger) {
            this.mUid = uid;
            this.mMessenger = messenger;
            this.mWorkSource = new WorkSource(uid);
        }

        public void register() {
            WifiScanningServiceImpl.this.mClients.put(this.mMessenger, this);
        }

        private void unregister() {
            WifiScanningServiceImpl.this.mClients.remove(this.mMessenger);
        }

        public void cleanup() {
            WifiScanningServiceImpl.this.mSingleScanListeners.removeAllForClient(this);
            WifiScanningServiceImpl.this.mSingleScanStateMachine.removeSingleScanRequests(this);
            WifiScanningServiceImpl.this.mBackgroundScanStateMachine.removeBackgroundScanSettings(this);
            unregister();
            WifiScanningServiceImpl wifiScanningServiceImpl = WifiScanningServiceImpl.this;
            wifiScanningServiceImpl.localLog("Successfully stopped all requests for client " + this);
        }

        public int getUid() {
            return this.mUid;
        }

        public void reportEvent(int what, int arg1, int arg2) {
            reportEvent(what, arg1, arg2, null);
        }

        private void reportBatchedScanStart() {
            if (this.mUid != 0) {
                try {
                    WifiScanningServiceImpl.this.mBatteryStats.noteWifiBatchedScanStartedFromSource(this.mWorkSource, getCsph());
                } catch (RemoteException e) {
                    WifiScanningServiceImpl wifiScanningServiceImpl = WifiScanningServiceImpl.this;
                    wifiScanningServiceImpl.logw("failed to report scan work: " + e.toString());
                }
            }
        }

        private void reportBatchedScanStop() {
            if (this.mUid != 0) {
                try {
                    WifiScanningServiceImpl.this.mBatteryStats.noteWifiBatchedScanStoppedFromSource(this.mWorkSource);
                } catch (RemoteException e) {
                    WifiScanningServiceImpl wifiScanningServiceImpl = WifiScanningServiceImpl.this;
                    wifiScanningServiceImpl.logw("failed to cleanup scan work: " + e.toString());
                }
            }
        }

        private int getCsph() {
            int totalScanDurationPerHour = 0;
            for (WifiScanner.ScanSettings settings : WifiScanningServiceImpl.this.mBackgroundScanStateMachine.getBackgroundScanSettings(this)) {
                totalScanDurationPerHour += WifiScanningServiceImpl.this.mChannelHelper.estimateScanDuration(settings) * (settings.periodInMs == 0 ? 1 : 3600000 / settings.periodInMs);
            }
            return totalScanDurationPerHour / ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS;
        }

        private void reportScanWorkUpdate() {
            if (this.mScanWorkReported) {
                reportBatchedScanStop();
                this.mScanWorkReported = false;
            }
            if (WifiScanningServiceImpl.this.mBackgroundScanStateMachine.getBackgroundScanSettings(this).isEmpty()) {
                reportBatchedScanStart();
                this.mScanWorkReported = true;
            }
        }

        public String toString() {
            return "ClientInfo[uid=" + this.mUid + "," + this.mMessenger + "]";
        }
    }

    private class ExternalClientInfo extends ClientInfo {
        /* access modifiers changed from: private */
        public final AsyncChannel mChannel;
        private boolean mDisconnected = false;

        ExternalClientInfo(int uid, Messenger messenger, AsyncChannel c) {
            super(uid, messenger);
            this.mChannel = c;
        }

        public void reportEvent(int what, int arg1, int arg2, Object obj) {
            if (!this.mDisconnected) {
                this.mChannel.sendMessage(what, arg1, arg2, obj);
            }
        }

        public void cleanup() {
            this.mDisconnected = true;
            WifiScanningServiceImpl.this.mPnoScanStateMachine.removePnoSettings(this);
            super.cleanup();
        }
    }

    private class InternalClientInfo extends ClientInfo {
        private static final int INTERNAL_CLIENT_HANDLER = 0;

        InternalClientInfo(int requesterUid, Messenger messenger) {
            super(requesterUid, messenger);
        }

        public void reportEvent(int what, int arg1, int arg2, Object obj) {
            Message message = Message.obtain();
            message.what = what;
            message.arg1 = arg1;
            message.arg2 = arg2;
            message.obj = obj;
            try {
                this.mMessenger.send(message);
            } catch (RemoteException e) {
                WifiScanningServiceImpl wifiScanningServiceImpl = WifiScanningServiceImpl.this;
                wifiScanningServiceImpl.loge("Failed to send message: " + what);
            }
        }

        public void sendRequestToClientHandler(int what, WifiScanner.ScanSettings settings, WorkSource workSource) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg2 = 0;
            if (settings != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("ScanSettings", settings);
                bundle.putParcelable("WorkSource", workSource);
                msg.obj = bundle;
            }
            msg.replyTo = this.mMessenger;
            msg.sendingUid = getUid();
            WifiScanningServiceImpl.this.mClientHandler.sendMessage(msg);
        }

        public void sendRequestToClientHandler(int what) {
            sendRequestToClientHandler(what, null, null);
        }

        public String toString() {
            return "InternalClientInfo[]";
        }
    }

    private class RequestInfo<T> {
        final ClientInfo clientInfo;
        final int handlerId;
        final T settings;
        final WorkSource workSource;

        RequestInfo(ClientInfo clientInfo2, int handlerId2, WorkSource requestedWorkSource, T settings2) {
            this.clientInfo = clientInfo2;
            this.handlerId = handlerId2;
            this.settings = settings2;
            this.workSource = WifiScanningServiceImpl.this.computeWorkSource(clientInfo2, requestedWorkSource);
        }

        /* access modifiers changed from: package-private */
        public void reportEvent(int what, int arg1, Object obj) {
            this.clientInfo.reportEvent(what, arg1, this.handlerId, obj);
        }
    }

    private class RequestList<T> extends ArrayList<RequestInfo<T>> {
        private RequestList() {
        }

        /* access modifiers changed from: package-private */
        public void addRequest(ClientInfo ci, int handler, WorkSource reqworkSource, T settings) {
            RequestInfo requestInfo = new RequestInfo(ci, handler, reqworkSource, settings);
            add(requestInfo);
        }

        /* access modifiers changed from: package-private */
        public T removeRequest(ClientInfo ci, int handlerId) {
            T removed = null;
            Iterator<RequestInfo<T>> iter = iterator();
            while (iter.hasNext()) {
                RequestInfo<T> entry = iter.next();
                if (entry.clientInfo == ci && entry.handlerId == handlerId) {
                    removed = entry.settings;
                    iter.remove();
                }
            }
            return removed;
        }

        /* access modifiers changed from: package-private */
        public Collection<T> getAllSettings() {
            ArrayList<T> settingsList = new ArrayList<>();
            Iterator<RequestInfo<T>> iter = iterator();
            while (iter.hasNext()) {
                settingsList.add(iter.next().settings);
            }
            return settingsList;
        }

        /* access modifiers changed from: package-private */
        public Collection<T> getAllSettingsForClient(ClientInfo ci) {
            ArrayList<T> settingsList = new ArrayList<>();
            Iterator<RequestInfo<T>> iter = iterator();
            while (iter.hasNext()) {
                RequestInfo<T> entry = iter.next();
                if (entry.clientInfo == ci) {
                    settingsList.add(entry.settings);
                }
            }
            return settingsList;
        }

        /* access modifiers changed from: package-private */
        public void removeAllForClient(ClientInfo ci) {
            Iterator<RequestInfo<T>> iter = iterator();
            while (iter.hasNext()) {
                if (iter.next().clientInfo == ci) {
                    iter.remove();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public WorkSource createMergedWorkSource() {
            WorkSource mergedSource = new WorkSource();
            Iterator it = iterator();
            while (it.hasNext()) {
                mergedSource.add(((RequestInfo) it.next()).workSource);
            }
            return mergedSource;
        }
    }

    class WifiBackgroundScanStateMachine extends StateMachine implements WifiNative.ScanEventHandler {
        /* access modifiers changed from: private */
        public final RequestList<WifiScanner.ScanSettings> mActiveBackgroundScans = new RequestList<>();
        /* access modifiers changed from: private */
        public final DefaultState mDefaultState = new DefaultState();
        /* access modifiers changed from: private */
        public final PausedState mPausedState = new PausedState();
        /* access modifiers changed from: private */
        public final StartedState mStartedState = new StartedState();

        class DefaultState extends State {
            DefaultState() {
            }

            public void enter() {
                WifiBackgroundScanStateMachine.this.mActiveBackgroundScans.clear();
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 159746:
                    case 159747:
                    case 159748:
                    case 159765:
                    case 159766:
                        WifiScanningServiceImpl.this.replyFailed(msg, -1, "not available");
                        break;
                    case WifiScanningServiceImpl.CMD_DRIVER_LOADED /*160006*/:
                        WifiScannerImpl unused = WifiScanningServiceImpl.this.mScannerImpl = WifiScanningServiceImpl.this.mScannerImplFactory.create(WifiScanningServiceImpl.this.mContext, WifiScanningServiceImpl.this.mLooper, WifiScanningServiceImpl.this.mClock);
                        if (WifiScanningServiceImpl.this.mScannerImpl == null) {
                            WifiBackgroundScanStateMachine.this.loge("Failed to start bgscan scan state machine because scanner impl is null");
                            return true;
                        }
                        WifiScanningServiceImpl.this.mScannerImpl.setWifiScanLogger(WifiScanningServiceImpl.this.mLocalLog);
                        ChannelHelper unused2 = WifiScanningServiceImpl.this.mChannelHelper = WifiScanningServiceImpl.this.mScannerImpl.getChannelHelper();
                        BackgroundScanScheduler unused3 = WifiScanningServiceImpl.this.mBackgroundScheduler = new BackgroundScanScheduler(WifiScanningServiceImpl.this.mChannelHelper);
                        WifiNative.ScanCapabilities capabilities = new WifiNative.ScanCapabilities();
                        if (!WifiScanningServiceImpl.this.mScannerImpl.getScanCapabilities(capabilities)) {
                            WifiBackgroundScanStateMachine.this.loge("could not get scan capabilities");
                            return true;
                        } else if (capabilities.max_scan_buckets <= 0) {
                            WifiBackgroundScanStateMachine wifiBackgroundScanStateMachine = WifiBackgroundScanStateMachine.this;
                            wifiBackgroundScanStateMachine.loge("invalid max buckets in scan capabilities " + capabilities.max_scan_buckets);
                            return true;
                        } else {
                            WifiScanningServiceImpl.this.mBackgroundScheduler.setMaxBuckets(capabilities.max_scan_buckets);
                            WifiScanningServiceImpl.this.mBackgroundScheduler.setMaxApPerScan(capabilities.max_ap_cache_per_scan);
                            Log.i(WifiScanningServiceImpl.TAG, "wifi driver loaded with scan capabilities: max buckets=" + capabilities.max_scan_buckets);
                            WifiBackgroundScanStateMachine.this.transitionTo(WifiBackgroundScanStateMachine.this.mStartedState);
                            return true;
                        }
                    case WifiScanningServiceImpl.CMD_DRIVER_UNLOADED /*160007*/:
                        Log.i(WifiScanningServiceImpl.TAG, "wifi driver unloaded");
                        WifiBackgroundScanStateMachine.this.transitionTo(WifiBackgroundScanStateMachine.this.mDefaultState);
                        break;
                }
                return true;
            }
        }

        class PausedState extends State {
            PausedState() {
            }

            public void enter() {
            }

            public boolean processMessage(Message msg) {
                if (msg.what != WifiScanningServiceImpl.CMD_SCAN_RESTARTED) {
                    WifiBackgroundScanStateMachine.this.deferMessage(msg);
                } else {
                    WifiBackgroundScanStateMachine.this.transitionTo(WifiBackgroundScanStateMachine.this.mStartedState);
                }
                return true;
            }
        }

        class StartedState extends State {
            StartedState() {
            }

            public void enter() {
            }

            public void exit() {
                WifiBackgroundScanStateMachine.this.sendBackgroundScanFailedToAllAndClear(-1, "Scan was interrupted");
                if (WifiScanningServiceImpl.this.mScannerImpl != null) {
                    WifiScanningServiceImpl.this.mScannerImpl.cleanup();
                }
            }

            public boolean processMessage(Message msg) {
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                switch (msg.what) {
                    case 159746:
                        WifiScanningServiceImpl.this.mWifiMetrics.incrementBackgroundScanCount();
                        Bundle scanParams = (Bundle) msg.obj;
                        if (scanParams != null) {
                            scanParams.setDefusable(true);
                            if (!WifiBackgroundScanStateMachine.this.addBackgroundScanRequest(ci, msg.arg2, scanParams.getParcelable("ScanSettings"), (WorkSource) scanParams.getParcelable("WorkSource"))) {
                                WifiScanningServiceImpl.this.replyFailed(msg, -3, "bad request");
                                break;
                            } else {
                                WifiScanningServiceImpl.this.replySucceeded(msg);
                                break;
                            }
                        } else {
                            WifiScanningServiceImpl.this.replyFailed(msg, -3, "params null");
                            return true;
                        }
                    case 159747:
                        WifiBackgroundScanStateMachine.this.removeBackgroundScanRequest(ci, msg.arg2);
                        break;
                    case 159748:
                        WifiBackgroundScanStateMachine.this.reportScanResults(WifiScanningServiceImpl.this.mScannerImpl.getLatestBatchedScanResults(true));
                        WifiScanningServiceImpl.this.replySucceeded(msg);
                        break;
                    case WifiConnectivityManager.MAX_PERIODIC_SCAN_INTERVAL_MS:
                        WifiBackgroundScanStateMachine.this.reportScanResults(WifiScanningServiceImpl.this.mScannerImpl.getLatestBatchedScanResults(true));
                        break;
                    case WifiScanningServiceImpl.CMD_FULL_SCAN_RESULTS /*160001*/:
                        WifiBackgroundScanStateMachine.this.reportFullScanResult((ScanResult) msg.obj, msg.arg2);
                        break;
                    case WifiScanningServiceImpl.CMD_DRIVER_LOADED /*160006*/:
                        Log.e(WifiScanningServiceImpl.TAG, "wifi driver loaded received while already loaded");
                        return true;
                    case WifiScanningServiceImpl.CMD_DRIVER_UNLOADED /*160007*/:
                        return false;
                    case WifiScanningServiceImpl.CMD_SCAN_PAUSED /*160008*/:
                        WifiBackgroundScanStateMachine.this.reportScanResults((WifiScanner.ScanData[]) msg.obj);
                        WifiBackgroundScanStateMachine.this.transitionTo(WifiBackgroundScanStateMachine.this.mPausedState);
                        break;
                    case WifiScanningServiceImpl.CMD_SCAN_FAILED /*160010*/:
                        Log.e(WifiScanningServiceImpl.TAG, "WifiScanner background scan gave CMD_SCAN_FAILED");
                        WifiBackgroundScanStateMachine.this.sendBackgroundScanFailedToAllAndClear(-1, "Background Scan failed");
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        WifiBackgroundScanStateMachine(Looper looper) {
            super("WifiBackgroundScanStateMachine", looper);
            setLogRecSize(512);
            setLogOnlyTransitions(false);
            addState(this.mDefaultState);
            addState(this.mStartedState, this.mDefaultState);
            addState(this.mPausedState, this.mDefaultState);
            setInitialState(this.mDefaultState);
        }

        public Collection<WifiScanner.ScanSettings> getBackgroundScanSettings(ClientInfo ci) {
            return this.mActiveBackgroundScans.getAllSettingsForClient(ci);
        }

        public void removeBackgroundScanSettings(ClientInfo ci) {
            this.mActiveBackgroundScans.removeAllForClient(ci);
            updateSchedule();
        }

        public void onScanStatus(int event) {
            switch (event) {
                case 0:
                case 1:
                case 2:
                    sendMessage(WifiConnectivityManager.MAX_PERIODIC_SCAN_INTERVAL_MS);
                    return;
                case 3:
                    sendMessage(WifiScanningServiceImpl.CMD_SCAN_FAILED);
                    return;
                default:
                    Log.e(WifiScanningServiceImpl.TAG, "Unknown scan status event: " + event);
                    return;
            }
        }

        public void onFullScanResult(ScanResult fullScanResult, int bucketsScanned) {
            sendMessage(WifiScanningServiceImpl.CMD_FULL_SCAN_RESULTS, 0, bucketsScanned, fullScanResult);
        }

        public void onScanPaused(WifiScanner.ScanData[] scanData) {
            sendMessage(WifiScanningServiceImpl.CMD_SCAN_PAUSED, scanData);
        }

        public void onScanRestarted() {
            sendMessage(WifiScanningServiceImpl.CMD_SCAN_RESTARTED);
        }

        /* access modifiers changed from: private */
        public boolean addBackgroundScanRequest(ClientInfo ci, int handler, WifiScanner.ScanSettings settings, WorkSource workSource) {
            if (ci == null) {
                Log.d(WifiScanningServiceImpl.TAG, "Failing scan request ClientInfo not found " + handler);
                return false;
            } else if (settings.periodInMs < 1000) {
                loge("Failing scan request because periodInMs is " + settings.periodInMs + ", min scan period is: " + 1000);
                return false;
            } else if (settings.band == 0 && settings.channels == null) {
                loge("Channels was null with unspecified band");
                return false;
            } else if (settings.band == 0 && settings.channels.length == 0) {
                loge("No channels specified");
                return false;
            } else {
                int minSupportedPeriodMs = WifiScanningServiceImpl.this.mChannelHelper.estimateScanDuration(settings);
                if (settings.periodInMs < minSupportedPeriodMs) {
                    loge("Failing scan request because minSupportedPeriodMs is " + minSupportedPeriodMs + " but the request wants " + settings.periodInMs);
                    return false;
                }
                if (!(settings.maxPeriodInMs == 0 || settings.maxPeriodInMs == settings.periodInMs)) {
                    if (settings.maxPeriodInMs < settings.periodInMs) {
                        loge("Failing scan request because maxPeriodInMs is " + settings.maxPeriodInMs + " but less than periodInMs " + settings.periodInMs);
                        return false;
                    } else if (settings.maxPeriodInMs > 1024000) {
                        loge("Failing scan request because maxSupportedPeriodMs is 1024000 but the request wants " + settings.maxPeriodInMs);
                        return false;
                    } else if (settings.stepCount < 1) {
                        loge("Failing scan request because stepCount is " + settings.stepCount + " which is less than 1");
                        return false;
                    }
                }
                WifiScanningServiceImpl.this.logScanRequest("addBackgroundScanRequest", ci, handler, null, settings, null);
                this.mActiveBackgroundScans.addRequest(ci, handler, workSource, settings);
                if (updateSchedule()) {
                    return true;
                }
                this.mActiveBackgroundScans.removeRequest(ci, handler);
                WifiScanningServiceImpl.this.localLog("Failing scan request because failed to reset scan");
                return false;
            }
        }

        private boolean updateSchedule() {
            if (WifiScanningServiceImpl.this.mChannelHelper == null || WifiScanningServiceImpl.this.mBackgroundScheduler == null || WifiScanningServiceImpl.this.mScannerImpl == null) {
                loge("Failed to update schedule because WifiScanningService is not initialized");
                return false;
            }
            WifiScanningServiceImpl.this.mChannelHelper.updateChannels();
            WifiScanningServiceImpl.this.mBackgroundScheduler.updateSchedule(this.mActiveBackgroundScans.getAllSettings());
            WifiNative.ScanSettings schedule = WifiScanningServiceImpl.this.mBackgroundScheduler.getSchedule();
            if (ScanScheduleUtil.scheduleEquals(WifiScanningServiceImpl.this.mPreviousSchedule, schedule)) {
                Log.i("WifiScanLog", "schedule updated with no change");
                return true;
            }
            StringBuilder keys = new StringBuilder();
            Iterator it = this.mActiveBackgroundScans.iterator();
            while (it.hasNext()) {
                keys.append(WifiScanner.getScanKey(((RequestInfo) it.next()).handlerId));
            }
            schedule.handlerId = keys.toString();
            WifiNative.ScanSettings unused = WifiScanningServiceImpl.this.mPreviousSchedule = schedule;
            if (schedule.num_buckets == 0) {
                WifiScanningServiceImpl.this.mScannerImpl.stopBatchedScan();
                Log.i("WifiScanLog", schedule.handlerId + "scan stopped");
                return true;
            }
            WifiScanningServiceImpl wifiScanningServiceImpl = WifiScanningServiceImpl.this;
            wifiScanningServiceImpl.localLog("starting scan: base period=" + schedule.base_period_ms + ", max ap per scan=" + schedule.max_ap_per_scan + ", batched scans=" + schedule.report_threshold_num_scans);
            for (int b = 0; b < schedule.num_buckets; b++) {
                WifiNative.BucketSettings bucket = schedule.buckets[b];
                WifiScanningServiceImpl wifiScanningServiceImpl2 = WifiScanningServiceImpl.this;
                wifiScanningServiceImpl2.localLog("bucket " + bucket.bucket + " (" + bucket.period_ms + "ms)[" + bucket.report_events + "]: " + ChannelHelper.toString(bucket));
            }
            if (WifiScanningServiceImpl.this.mScannerImpl.startBatchedScan(schedule, this)) {
                Log.i("WifiScanLog", schedule.handlerId + "startBatchedScan success!");
                return true;
            }
            Log.i("WifiScanLog", schedule.handlerId + "startBatchedScan failed!");
            WifiNative.ScanSettings unused2 = WifiScanningServiceImpl.this.mPreviousSchedule = null;
            loge("error starting scan: base period=" + schedule.base_period_ms + ", max ap per scan=" + schedule.max_ap_per_scan + ", batched scans=" + schedule.report_threshold_num_scans);
            for (int b2 = 0; b2 < schedule.num_buckets; b2++) {
                WifiNative.BucketSettings bucket2 = schedule.buckets[b2];
                loge("bucket " + bucket2.bucket + " (" + bucket2.period_ms + "ms)[" + bucket2.report_events + "]: " + ChannelHelper.toString(bucket2));
            }
            return false;
        }

        /* access modifiers changed from: private */
        public void removeBackgroundScanRequest(ClientInfo ci, int handler) {
            if (ci != null) {
                WifiScanningServiceImpl.this.logScanRequest("removeBackgroundScanRequest", ci, handler, null, this.mActiveBackgroundScans.removeRequest(ci, handler), null);
                updateSchedule();
            }
        }

        /* access modifiers changed from: private */
        public void reportFullScanResult(ScanResult result, int bucketsScanned) {
            Iterator it = this.mActiveBackgroundScans.iterator();
            while (it.hasNext()) {
                RequestInfo<WifiScanner.ScanSettings> entry = (RequestInfo) it.next();
                ClientInfo ci = entry.clientInfo;
                int handler = entry.handlerId;
                if (WifiScanningServiceImpl.this.mBackgroundScheduler.shouldReportFullScanResultForSettings(result, bucketsScanned, (WifiScanner.ScanSettings) entry.settings)) {
                    ScanResult newResult = new ScanResult(result);
                    if (result.informationElements != null) {
                        newResult.informationElements = (ScanResult.InformationElement[]) result.informationElements.clone();
                    } else {
                        newResult.informationElements = null;
                    }
                    ci.reportEvent(159764, 0, handler, newResult);
                }
            }
        }

        /* access modifiers changed from: private */
        public void reportScanResults(WifiScanner.ScanData[] results) {
            if (results == null) {
                Log.d(WifiScanningServiceImpl.TAG, "The results is null, nothing to report.");
                return;
            }
            for (WifiScanner.ScanData result : results) {
                if (!(result == null || result.getResults() == null)) {
                    if (result.getResults().length > 0) {
                        WifiScanningServiceImpl.this.mWifiMetrics.incrementNonEmptyScanResultCount();
                    } else {
                        WifiScanningServiceImpl.this.mWifiMetrics.incrementEmptyScanResultCount();
                    }
                }
            }
            Iterator it = this.mActiveBackgroundScans.iterator();
            while (it.hasNext()) {
                RequestInfo<WifiScanner.ScanSettings> entry = (RequestInfo) it.next();
                ClientInfo ci = entry.clientInfo;
                int handler = entry.handlerId;
                WifiScanner.ScanData[] resultsToDeliver = WifiScanningServiceImpl.this.mBackgroundScheduler.filterResultsForSettings(results, (WifiScanner.ScanSettings) entry.settings);
                if (resultsToDeliver != null) {
                    WifiScanningServiceImpl.this.logCallback("backgroundScanResults", ci, handler, WifiScanningServiceImpl.describeForLog(resultsToDeliver));
                    ci.reportEvent(159749, 0, handler, new WifiScanner.ParcelableScanData(resultsToDeliver));
                }
            }
        }

        /* access modifiers changed from: private */
        public void sendBackgroundScanFailedToAllAndClear(int reason, String description) {
            Iterator it = this.mActiveBackgroundScans.iterator();
            while (it.hasNext()) {
                RequestInfo<WifiScanner.ScanSettings> entry = (RequestInfo) it.next();
                entry.clientInfo.reportEvent(159762, 0, entry.handlerId, new WifiScanner.OperationResult(reason, description));
            }
            this.mActiveBackgroundScans.clear();
        }
    }

    class WifiPnoScanStateMachine extends StateMachine implements WifiNative.PnoEventHandler {
        private final RequestList<Pair<WifiScanner.PnoSettings, WifiScanner.ScanSettings>> mActivePnoScans = new RequestList<>();
        /* access modifiers changed from: private */
        public final DefaultState mDefaultState = new DefaultState();
        /* access modifiers changed from: private */
        public final HwPnoScanState mHwPnoScanState = new HwPnoScanState();
        private InternalClientInfo mInternalClientInfo;
        /* access modifiers changed from: private */
        public final SingleScanState mSingleScanState = new SingleScanState();
        /* access modifiers changed from: private */
        public final StartedState mStartedState = new StartedState();

        class DefaultState extends State {
            DefaultState() {
            }

            public void enter() {
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 159749:
                    case 159762:
                    case WifiScanningServiceImpl.CMD_PNO_NETWORK_FOUND /*160011*/:
                    case WifiScanningServiceImpl.CMD_PNO_SCAN_FAILED /*160012*/:
                        WifiPnoScanStateMachine wifiPnoScanStateMachine = WifiPnoScanStateMachine.this;
                        wifiPnoScanStateMachine.loge("Unexpected message " + msg.what);
                        break;
                    case 159768:
                    case 159769:
                        WifiScanningServiceImpl.this.replyFailed(msg, -1, "not available");
                        break;
                    case WifiScanningServiceImpl.CMD_DRIVER_LOADED /*160006*/:
                        if (WifiScanningServiceImpl.this.mScannerImpl != null) {
                            WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                            break;
                        } else {
                            WifiPnoScanStateMachine.this.loge("Failed to start pno scan state machine because scanner impl is null");
                            return true;
                        }
                    case WifiScanningServiceImpl.CMD_DRIVER_UNLOADED /*160007*/:
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mDefaultState);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        class HwPnoScanState extends State {
            HwPnoScanState() {
            }

            public void enter() {
            }

            public void exit() {
                WifiScanningServiceImpl.this.mScannerImpl.resetHwPnoList();
                WifiPnoScanStateMachine.this.removeInternalClient();
            }

            public boolean processMessage(Message msg) {
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                switch (msg.what) {
                    case 159768:
                        Bundle pnoParams = (Bundle) msg.obj;
                        if (pnoParams != null) {
                            pnoParams.setDefusable(true);
                            WifiScanner.ScanSettings scanSettings = pnoParams.getParcelable("ScanSettings");
                            if (!WifiPnoScanStateMachine.this.addHwPnoScanRequest(ci, msg.arg2, scanSettings, pnoParams.getParcelable("PnoSettings"))) {
                                WifiScanningServiceImpl.this.replyFailed(msg, -3, "bad request");
                                WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                                break;
                            } else {
                                WifiScanningServiceImpl.this.replySucceeded(msg);
                                break;
                            }
                        } else {
                            WifiScanningServiceImpl.this.replyFailed(msg, -3, "params null");
                            return true;
                        }
                    case 159769:
                        WifiPnoScanStateMachine.this.removeHwPnoScanRequest(ci, msg.arg2);
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                        break;
                    case WifiScanningServiceImpl.CMD_PNO_NETWORK_FOUND /*160011*/:
                        if (!WifiPnoScanStateMachine.this.isSingleScanNeeded((ScanResult[]) msg.obj)) {
                            WifiPnoScanStateMachine.this.reportPnoNetworkFound((ScanResult[]) msg.obj);
                            break;
                        } else {
                            WifiScanner.ScanSettings activeScanSettings = WifiPnoScanStateMachine.this.getScanSettings();
                            if (activeScanSettings != null) {
                                WifiPnoScanStateMachine.this.addSingleScanRequest(activeScanSettings);
                                WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mSingleScanState);
                                break;
                            } else {
                                WifiPnoScanStateMachine.this.sendPnoScanFailedToAllAndClear(-1, "couldn't retrieve setting");
                                WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                                break;
                            }
                        }
                    case WifiScanningServiceImpl.CMD_PNO_SCAN_FAILED /*160012*/:
                        WifiPnoScanStateMachine.this.sendPnoScanFailedToAllAndClear(-1, "pno scan failed");
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        class SingleScanState extends State {
            SingleScanState() {
            }

            public void enter() {
            }

            public boolean processMessage(Message msg) {
                WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                int i = msg.what;
                if (i == 159749) {
                    WifiScanner.ScanData[] scanDatas = ((WifiScanner.ParcelableScanData) msg.obj).getResults();
                    WifiPnoScanStateMachine.this.reportPnoNetworkFound(scanDatas[scanDatas.length - 1].getResults());
                    WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mHwPnoScanState);
                } else if (i != 159762) {
                    return false;
                } else {
                    WifiPnoScanStateMachine.this.sendPnoScanFailedToAllAndClear(-1, "single scan failed");
                    WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                }
                return true;
            }
        }

        class StartedState extends State {
            StartedState() {
            }

            public void enter() {
            }

            public void exit() {
                WifiPnoScanStateMachine.this.sendPnoScanFailedToAllAndClear(-1, "Scan was interrupted");
            }

            public boolean processMessage(Message msg) {
                WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                int i = msg.what;
                if (i == WifiScanningServiceImpl.CMD_DRIVER_LOADED) {
                    return true;
                }
                switch (i) {
                    case 159768:
                        Bundle pnoParams = (Bundle) msg.obj;
                        if (pnoParams != null) {
                            pnoParams.setDefusable(true);
                            if (!WifiScanningServiceImpl.this.mScannerImpl.isHwPnoSupported(pnoParams.getParcelable("PnoSettings").isConnected)) {
                                Log.i("WifiScanLog", WifiScanner.getScanKey(msg.arg2) + "isHwPnoSupported false");
                                WifiScanningServiceImpl.this.replyFailed(msg, -3, "not supported");
                                break;
                            } else {
                                WifiPnoScanStateMachine.this.deferMessage(msg);
                                Log.i("WifiScanLog", WifiScanner.getScanKey(msg.arg2) + "isHwPnoSupported true");
                                WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mHwPnoScanState);
                                break;
                            }
                        } else {
                            WifiScanningServiceImpl.this.replyFailed(msg, -3, "params null");
                            return true;
                        }
                    case 159769:
                        WifiScanningServiceImpl.this.replyFailed(msg, -1, "no scan running");
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        WifiPnoScanStateMachine(Looper looper) {
            super("WifiPnoScanStateMachine", looper);
            setLogRecSize(256);
            setLogOnlyTransitions(false);
            addState(this.mDefaultState);
            addState(this.mStartedState, this.mDefaultState);
            addState(this.mHwPnoScanState, this.mStartedState);
            addState(this.mSingleScanState, this.mHwPnoScanState);
            setInitialState(this.mDefaultState);
        }

        public void removePnoSettings(ClientInfo ci) {
            this.mActivePnoScans.removeAllForClient(ci);
            transitionTo(this.mStartedState);
        }

        public void onPnoNetworkFound(ScanResult[] results) {
            sendMessage(WifiScanningServiceImpl.CMD_PNO_NETWORK_FOUND, 0, 0, results);
        }

        public void onPnoScanFailed() {
            sendMessage(WifiScanningServiceImpl.CMD_PNO_SCAN_FAILED, 0, 0, null);
        }

        private WifiNative.PnoSettings convertSettingsToPnoNative(WifiScanner.ScanSettings scanSettings, WifiScanner.PnoSettings pnoSettings) {
            WifiNative.PnoSettings nativePnoSetting = new WifiNative.PnoSettings();
            nativePnoSetting.periodInMs = scanSettings.periodInMs;
            nativePnoSetting.min5GHzRssi = pnoSettings.min5GHzRssi;
            nativePnoSetting.min24GHzRssi = pnoSettings.min24GHzRssi;
            nativePnoSetting.initialScoreMax = pnoSettings.initialScoreMax;
            nativePnoSetting.currentConnectionBonus = pnoSettings.currentConnectionBonus;
            nativePnoSetting.sameNetworkBonus = pnoSettings.sameNetworkBonus;
            nativePnoSetting.secureBonus = pnoSettings.secureBonus;
            nativePnoSetting.band5GHzBonus = pnoSettings.band5GHzBonus;
            nativePnoSetting.isConnected = pnoSettings.isConnected;
            nativePnoSetting.networkList = new WifiNative.PnoNetwork[pnoSettings.networkList.length];
            for (int i = 0; i < pnoSettings.networkList.length; i++) {
                nativePnoSetting.networkList[i] = new WifiNative.PnoNetwork();
                nativePnoSetting.networkList[i].ssid = pnoSettings.networkList[i].ssid;
                nativePnoSetting.networkList[i].flags = pnoSettings.networkList[i].flags;
                nativePnoSetting.networkList[i].auth_bit_field = pnoSettings.networkList[i].authBitField;
            }
            return nativePnoSetting;
        }

        /* access modifiers changed from: private */
        public WifiScanner.ScanSettings getScanSettings() {
            Iterator<Pair<WifiScanner.PnoSettings, WifiScanner.ScanSettings>> it = this.mActivePnoScans.getAllSettings().iterator();
            if (it.hasNext()) {
                return (WifiScanner.ScanSettings) it.next().second;
            }
            return null;
        }

        /* access modifiers changed from: private */
        public void removeInternalClient() {
            if (this.mInternalClientInfo != null) {
                this.mInternalClientInfo.cleanup();
                this.mInternalClientInfo = null;
                return;
            }
            Log.w(WifiScanningServiceImpl.TAG, "No Internal client for PNO");
        }

        private void addInternalClient(ClientInfo ci) {
            if (this.mInternalClientInfo == null) {
                this.mInternalClientInfo = new InternalClientInfo(ci.getUid(), new Messenger(getHandler()));
                this.mInternalClientInfo.register();
                return;
            }
            Log.w(WifiScanningServiceImpl.TAG, "Internal client for PNO already exists");
        }

        private void addPnoScanRequest(ClientInfo ci, int handler, WifiScanner.ScanSettings scanSettings, WifiScanner.PnoSettings pnoSettings) {
            WifiScanningServiceImpl.this.localLog(WifiScanner.getScanKey(handler), "26", "addPnoScanRequest");
            this.mActivePnoScans.addRequest(ci, handler, WifiStateMachine.WIFI_WORK_SOURCE, Pair.create(pnoSettings, scanSettings));
            addInternalClient(ci);
        }

        private Pair<WifiScanner.PnoSettings, WifiScanner.ScanSettings> removePnoScanRequest(ClientInfo ci, int handler) {
            return this.mActivePnoScans.removeRequest(ci, handler);
        }

        /* access modifiers changed from: private */
        public boolean addHwPnoScanRequest(ClientInfo ci, int handler, WifiScanner.ScanSettings scanSettings, WifiScanner.PnoSettings pnoSettings) {
            if (ci == null) {
                Log.d(WifiScanningServiceImpl.TAG, "Failing scan request ClientInfo not found " + WifiScanner.getScanKey(handler));
                return false;
            } else if (!this.mActivePnoScans.isEmpty()) {
                loge("Failing scan request because there is already an active scan" + WifiScanner.getScanKey(handler));
                return false;
            } else {
                if (!WifiScanningServiceImpl.this.mScannerImpl.setHwPnoList(convertSettingsToPnoNative(scanSettings, pnoSettings), WifiScanningServiceImpl.this.mPnoScanStateMachine)) {
                    loge("Failing setHwPnoList" + WifiScanner.getScanKey(handler));
                    return false;
                }
                WifiScanningServiceImpl.this.logScanRequest("addHwPnoScanRequest", ci, handler, null, scanSettings, pnoSettings);
                addPnoScanRequest(ci, handler, scanSettings, pnoSettings);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public void removeHwPnoScanRequest(ClientInfo ci, int handler) {
            if (ci != null) {
                Pair<WifiScanner.PnoSettings, WifiScanner.ScanSettings> settings = removePnoScanRequest(ci, handler);
                if (settings != null) {
                    WifiScanningServiceImpl.this.logScanRequest("removeHwPnoScanRequest", ci, handler, null, (WifiScanner.ScanSettings) settings.second, (WifiScanner.PnoSettings) settings.first);
                    return;
                }
                Log.d(WifiScanningServiceImpl.TAG, "removeHwPnoScanRequest: settings is null");
            }
        }

        /* access modifiers changed from: private */
        public void reportPnoNetworkFound(ScanResult[] results) {
            WifiScanner.ParcelableScanResults parcelableScanResults = new WifiScanner.ParcelableScanResults(results);
            Iterator it = this.mActivePnoScans.iterator();
            while (it.hasNext()) {
                RequestInfo<Pair<WifiScanner.PnoSettings, WifiScanner.ScanSettings>> entry = (RequestInfo) it.next();
                ClientInfo ci = entry.clientInfo;
                int handler = entry.handlerId;
                WifiScanningServiceImpl.this.logCallback("pnoNetworkFound", ci, handler, WifiScanningServiceImpl.describeForLog(results));
                ci.reportEvent(159770, 0, handler, parcelableScanResults);
            }
        }

        /* access modifiers changed from: private */
        public void sendPnoScanFailedToAllAndClear(int reason, String description) {
            Iterator it = this.mActivePnoScans.iterator();
            while (it.hasNext()) {
                RequestInfo<Pair<WifiScanner.PnoSettings, WifiScanner.ScanSettings>> entry = (RequestInfo) it.next();
                entry.clientInfo.reportEvent(159762, 0, entry.handlerId, new WifiScanner.OperationResult(reason, description));
            }
            this.mActivePnoScans.clear();
        }

        /* access modifiers changed from: private */
        public void addSingleScanRequest(WifiScanner.ScanSettings settings) {
            if (this.mInternalClientInfo != null) {
                this.mInternalClientInfo.sendRequestToClientHandler(159765, settings, WifiStateMachine.WIFI_WORK_SOURCE);
            }
        }

        /* access modifiers changed from: private */
        public boolean isSingleScanNeeded(ScanResult[] scanResults) {
            for (ScanResult scanResult : scanResults) {
                if (scanResult.informationElements != null && scanResult.informationElements.length > 0) {
                    return false;
                }
            }
            return true;
        }
    }

    class WifiSingleScanStateMachine extends StateMachine implements WifiNative.ScanEventHandler {
        @VisibleForTesting
        public static final int CACHED_SCAN_RESULTS_MAX_AGE_IN_MILLIS = 180000;
        /* access modifiers changed from: private */
        public WifiNative.ScanSettings mActiveScanSettings = null;
        /* access modifiers changed from: private */
        public RequestList<WifiScanner.ScanSettings> mActiveScans = new RequestList<>();
        /* access modifiers changed from: private */
        public final List<ScanResult> mCachedScanResults = new ArrayList();
        /* access modifiers changed from: private */
        public final DefaultState mDefaultState = new DefaultState();
        private final DriverStartedState mDriverStartedState = new DriverStartedState();
        /* access modifiers changed from: private */
        public final IdleState mIdleState = new IdleState();
        /* access modifiers changed from: private */
        public RequestList<WifiScanner.ScanSettings> mPendingScans = new RequestList<>();
        /* access modifiers changed from: private */
        public final ScanningState mScanningState = new ScanningState();

        class DefaultState extends State {
            DefaultState() {
            }

            public void enter() {
                WifiSingleScanStateMachine.this.mActiveScans.clear();
                WifiSingleScanStateMachine.this.mPendingScans.clear();
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 159765:
                    case 159766:
                        WifiScanningServiceImpl.this.replyFailed(msg, -1, "not available");
                        return true;
                    case 159773:
                        msg.obj = new WifiScanner.ParcelableScanResults(filterCachedScanResultsByAge());
                        WifiScanningServiceImpl.this.replySucceeded(msg);
                        return true;
                    case WifiConnectivityManager.MAX_PERIODIC_SCAN_INTERVAL_MS:
                        return true;
                    case WifiScanningServiceImpl.CMD_FULL_SCAN_RESULTS /*160001*/:
                        return true;
                    case WifiScanningServiceImpl.CMD_DRIVER_LOADED /*160006*/:
                        if (WifiScanningServiceImpl.this.mScannerImpl == null) {
                            WifiSingleScanStateMachine.this.loge("Failed to start single scan state machine because scanner impl is null");
                            return true;
                        }
                        WifiSingleScanStateMachine.this.transitionTo(WifiSingleScanStateMachine.this.mIdleState);
                        return true;
                    case WifiScanningServiceImpl.CMD_DRIVER_UNLOADED /*160007*/:
                        WifiSingleScanStateMachine.this.transitionTo(WifiSingleScanStateMachine.this.mDefaultState);
                        return true;
                    default:
                        return false;
                }
            }

            private ScanResult[] filterCachedScanResultsByAge() {
                ScanResult[] filterCachedScanResults = (ScanResult[]) WifiSingleScanStateMachine.this.mCachedScanResults.stream().filter(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0025: CHECK_CAST  (r2v5 'filterCachedScanResults' android.net.wifi.ScanResult[]) = (android.net.wifi.ScanResult[]) (wrap: java.lang.Object[]
                      0x0021: INVOKE  (r2v4 java.lang.Object[]) = (wrap: java.util.stream.Stream
                      0x001b: INVOKE  (r2v3 java.util.stream.Stream) = (wrap: java.util.stream.Stream
                      0x0012: INVOKE  (r2v2 java.util.stream.Stream) = (wrap: java.util.List
                      0x000e: INVOKE  (r2v1 java.util.List) = (wrap: com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine
                      0x000c: IGET  (r2v0 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) = (r5v0 'this' com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState A[THIS]) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.DefaultState.this$1 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.access$2100(com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine):java.util.List type: STATIC) java.util.List.stream():java.util.stream.Stream type: INTERFACE), (wrap: com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI
                      0x0018: CONSTRUCTOR  (r3v0 com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI) = (wrap: long
                      0x0008: INVOKE  (r0v3 'currentTimeInMillis' long) = (wrap: com.android.server.wifi.Clock
                      0x0004: INVOKE  (r0v2 com.android.server.wifi.Clock) = (wrap: com.android.server.wifi.scanner.WifiScanningServiceImpl
                      0x0002: IGET  (r0v1 com.android.server.wifi.scanner.WifiScanningServiceImpl) = (wrap: com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine
                      0x0000: IGET  (r0v0 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) = (r5v0 'this' com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState A[THIS]) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.DefaultState.this$1 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.this$0 com.android.server.wifi.scanner.WifiScanningServiceImpl) com.android.server.wifi.scanner.WifiScanningServiceImpl.access$2000(com.android.server.wifi.scanner.WifiScanningServiceImpl):com.android.server.wifi.Clock type: STATIC) com.android.server.wifi.Clock.getElapsedSinceBootMillis():long type: VIRTUAL) com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI.<init>(long):void CONSTRUCTOR) java.util.stream.Stream.filter(java.util.function.Predicate):java.util.stream.Stream type: INTERFACE), (wrap: com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$IadGqqQgFfoD3kqhYRHB92f1PGI
                      0x001f: SGET  (r3v1 com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$IadGqqQgFfoD3kqhYRHB92f1PGI) =  com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$IadGqqQgFfoD3kqhYRHB92f1PGI.INSTANCE com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$IadGqqQgFfoD3kqhYRHB92f1PGI) java.util.stream.Stream.toArray(java.util.function.IntFunction):java.lang.Object[] type: INTERFACE) in method: com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.DefaultState.filterCachedScanResultsByAge():android.net.wifi.ScanResult[], dex: wifi-service_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0021: INVOKE  (r2v4 java.lang.Object[]) = (wrap: java.util.stream.Stream
                      0x001b: INVOKE  (r2v3 java.util.stream.Stream) = (wrap: java.util.stream.Stream
                      0x0012: INVOKE  (r2v2 java.util.stream.Stream) = (wrap: java.util.List
                      0x000e: INVOKE  (r2v1 java.util.List) = (wrap: com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine
                      0x000c: IGET  (r2v0 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) = (r5v0 'this' com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState A[THIS]) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.DefaultState.this$1 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.access$2100(com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine):java.util.List type: STATIC) java.util.List.stream():java.util.stream.Stream type: INTERFACE), (wrap: com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI
                      0x0018: CONSTRUCTOR  (r3v0 com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI) = (wrap: long
                      0x0008: INVOKE  (r0v3 'currentTimeInMillis' long) = (wrap: com.android.server.wifi.Clock
                      0x0004: INVOKE  (r0v2 com.android.server.wifi.Clock) = (wrap: com.android.server.wifi.scanner.WifiScanningServiceImpl
                      0x0002: IGET  (r0v1 com.android.server.wifi.scanner.WifiScanningServiceImpl) = (wrap: com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine
                      0x0000: IGET  (r0v0 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) = (r5v0 'this' com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState A[THIS]) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.DefaultState.this$1 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.this$0 com.android.server.wifi.scanner.WifiScanningServiceImpl) com.android.server.wifi.scanner.WifiScanningServiceImpl.access$2000(com.android.server.wifi.scanner.WifiScanningServiceImpl):com.android.server.wifi.Clock type: STATIC) com.android.server.wifi.Clock.getElapsedSinceBootMillis():long type: VIRTUAL) com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI.<init>(long):void CONSTRUCTOR) java.util.stream.Stream.filter(java.util.function.Predicate):java.util.stream.Stream type: INTERFACE), (wrap: com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$IadGqqQgFfoD3kqhYRHB92f1PGI
                      0x001f: SGET  (r3v1 com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$IadGqqQgFfoD3kqhYRHB92f1PGI) =  com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$IadGqqQgFfoD3kqhYRHB92f1PGI.INSTANCE com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$IadGqqQgFfoD3kqhYRHB92f1PGI) java.util.stream.Stream.toArray(java.util.function.IntFunction):java.lang.Object[] type: INTERFACE in method: com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.DefaultState.filterCachedScanResultsByAge():android.net.wifi.ScanResult[], dex: wifi-service_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:280)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 23 more
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x001b: INVOKE  (r2v3 java.util.stream.Stream) = (wrap: java.util.stream.Stream
                      0x0012: INVOKE  (r2v2 java.util.stream.Stream) = (wrap: java.util.List
                      0x000e: INVOKE  (r2v1 java.util.List) = (wrap: com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine
                      0x000c: IGET  (r2v0 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) = (r5v0 'this' com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState A[THIS]) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.DefaultState.this$1 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.access$2100(com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine):java.util.List type: STATIC) java.util.List.stream():java.util.stream.Stream type: INTERFACE), (wrap: com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI
                      0x0018: CONSTRUCTOR  (r3v0 com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI) = (wrap: long
                      0x0008: INVOKE  (r0v3 'currentTimeInMillis' long) = (wrap: com.android.server.wifi.Clock
                      0x0004: INVOKE  (r0v2 com.android.server.wifi.Clock) = (wrap: com.android.server.wifi.scanner.WifiScanningServiceImpl
                      0x0002: IGET  (r0v1 com.android.server.wifi.scanner.WifiScanningServiceImpl) = (wrap: com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine
                      0x0000: IGET  (r0v0 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) = (r5v0 'this' com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState A[THIS]) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.DefaultState.this$1 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.this$0 com.android.server.wifi.scanner.WifiScanningServiceImpl) com.android.server.wifi.scanner.WifiScanningServiceImpl.access$2000(com.android.server.wifi.scanner.WifiScanningServiceImpl):com.android.server.wifi.Clock type: STATIC) com.android.server.wifi.Clock.getElapsedSinceBootMillis():long type: VIRTUAL) com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI.<init>(long):void CONSTRUCTOR) java.util.stream.Stream.filter(java.util.function.Predicate):java.util.stream.Stream type: INTERFACE in method: com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.DefaultState.filterCachedScanResultsByAge():android.net.wifi.ScanResult[], dex: wifi-service_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.addArgDot(InsnGen.java:91)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:686)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 26 more
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0018: CONSTRUCTOR  (r3v0 com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI) = (wrap: long
                      0x0008: INVOKE  (r0v3 'currentTimeInMillis' long) = (wrap: com.android.server.wifi.Clock
                      0x0004: INVOKE  (r0v2 com.android.server.wifi.Clock) = (wrap: com.android.server.wifi.scanner.WifiScanningServiceImpl
                      0x0002: IGET  (r0v1 com.android.server.wifi.scanner.WifiScanningServiceImpl) = (wrap: com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine
                      0x0000: IGET  (r0v0 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) = (r5v0 'this' com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState A[THIS]) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.DefaultState.this$1 com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine) com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.this$0 com.android.server.wifi.scanner.WifiScanningServiceImpl) com.android.server.wifi.scanner.WifiScanningServiceImpl.access$2000(com.android.server.wifi.scanner.WifiScanningServiceImpl):com.android.server.wifi.Clock type: STATIC) com.android.server.wifi.Clock.getElapsedSinceBootMillis():long type: VIRTUAL) com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI.<init>(long):void CONSTRUCTOR in method: com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.DefaultState.filterCachedScanResultsByAge():android.net.wifi.ScanResult[], dex: wifi-service_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 31 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 36 more
                    */
                /*
                    this = this;
                    com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine r0 = com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.this
                    com.android.server.wifi.scanner.WifiScanningServiceImpl r0 = com.android.server.wifi.scanner.WifiScanningServiceImpl.this
                    com.android.server.wifi.Clock r0 = r0.mClock
                    long r0 = r0.getElapsedSinceBootMillis()
                    com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine r2 = com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.this
                    java.util.List r2 = r2.mCachedScanResults
                    java.util.stream.Stream r2 = r2.stream()
                    com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI r3 = new com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$InbNEkwBcgp-s8u0tfPo_eYbuRI
                    r3.<init>(r0)
                    java.util.stream.Stream r2 = r2.filter(r3)
                    com.android.server.wifi.scanner.-$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$IadGqqQgFfoD3kqhYRHB92f1PGI r3 = com.android.server.wifi.scanner.$$Lambda$WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState$IadGqqQgFfoD3kqhYRHB92f1PGI.INSTANCE
                    java.lang.Object[] r2 = r2.toArray(r3)
                    android.net.wifi.ScanResult[] r2 = (android.net.wifi.ScanResult[]) r2
                    int r3 = r2.length
                    if (r3 != 0) goto L_0x0043
                    com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine r3 = com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.this
                    java.util.List r3 = r3.mCachedScanResults
                    com.android.server.wifi.scanner.WifiScanningServiceImpl$WifiSingleScanStateMachine r4 = com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.this
                    java.util.List r4 = r4.mCachedScanResults
                    int r4 = r4.size()
                    android.net.wifi.ScanResult[] r4 = new android.net.wifi.ScanResult[r4]
                    java.lang.Object[] r3 = r3.toArray(r4)
                    android.net.wifi.ScanResult[] r3 = (android.net.wifi.ScanResult[]) r3
                    goto L_0x0044
                L_0x0043:
                    r3 = r2
                L_0x0044:
                    return r3
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.scanner.WifiScanningServiceImpl.WifiSingleScanStateMachine.DefaultState.filterCachedScanResultsByAge():android.net.wifi.ScanResult[]");
            }

            static /* synthetic */ boolean lambda$filterCachedScanResultsByAge$0(long currentTimeInMillis, ScanResult scanResult) {
                return currentTimeInMillis - (scanResult.timestamp / 1000) < 180000;
            }

            static /* synthetic */ ScanResult[] lambda$filterCachedScanResultsByAge$1(int x$0) {
                return new ScanResult[x$0];
            }
        }

        class DriverStartedState extends State {
            DriverStartedState() {
            }

            public void exit() {
                WifiSingleScanStateMachine.this.mCachedScanResults.clear();
                WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(2, WifiSingleScanStateMachine.this.mPendingScans.size());
                WifiSingleScanStateMachine.this.sendOpFailedToAllAndClear(WifiSingleScanStateMachine.this.mPendingScans, -1, "Scan was interrupted");
            }

            public boolean processMessage(Message msg) {
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                int i = msg.what;
                if (i == WifiScanningServiceImpl.CMD_DRIVER_LOADED) {
                    return true;
                }
                switch (i) {
                    case 159765:
                        WifiScanningServiceImpl.this.mWifiMetrics.incrementOneshotScanCount();
                        int handler = msg.arg2;
                        Bundle scanParams = (Bundle) msg.obj;
                        if (scanParams == null) {
                            if (ci != null) {
                                WifiScanningServiceImpl.this.logCallback("singleScanInvalidRequest", ci, handler, "null params");
                            }
                            Log.w("WifiScanLog", WifiScanner.getScanKey(handler) + " params null");
                            WifiScanningServiceImpl.this.replyFailed(msg, -3, "params null");
                            return true;
                        }
                        scanParams.setDefusable(true);
                        WifiScanner.ScanSettings scanSettings = scanParams.getParcelable("ScanSettings");
                        WorkSource workSource = (WorkSource) scanParams.getParcelable("WorkSource");
                        if (WifiSingleScanStateMachine.this.validateScanRequest(ci, handler, scanSettings)) {
                            WifiScanningServiceImpl.this.logScanRequest("addSingleScanRequest", ci, handler, workSource, scanSettings, null);
                            WifiScanningServiceImpl.this.replySucceeded(msg);
                            if (WifiSingleScanStateMachine.this.getCurrentState() == WifiSingleScanStateMachine.this.mScanningState) {
                                if (WifiSingleScanStateMachine.this.activeScanSatisfies(scanSettings)) {
                                    WifiSingleScanStateMachine.this.mActiveScans.addRequest(ci, handler, workSource, scanSettings);
                                    WifiScanningServiceImpl.this.localLog(WifiScanner.getScanKey(handler), "22", "ScanningState add to mActiveScans");
                                } else {
                                    WifiSingleScanStateMachine.this.mPendingScans.addRequest(ci, handler, workSource, scanSettings);
                                    WifiScanningServiceImpl.this.localLog(WifiScanner.getScanKey(handler), "23", "ScanningState add to mPendingScans");
                                }
                                WifiScanningServiceImpl.this.mScannerImpl.logWifiScan("getCurrentState is ScanningState, do not start new scan");
                            } else {
                                WifiSingleScanStateMachine.this.mPendingScans.addRequest(ci, handler, workSource, scanSettings);
                                WifiScanningServiceImpl.this.localLog(WifiScanner.getScanKey(handler), "24", "add to mPendingScans, start new scan!");
                                WifiSingleScanStateMachine.this.tryToStartNewScan();
                            }
                        } else {
                            if (ci != null) {
                                WifiScanningServiceImpl.this.logCallback("singleScanInvalidRequest", ci, handler, "bad request");
                            }
                            WifiScanningServiceImpl.this.replyFailed(msg, -3, "bad request");
                            WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(3, 1);
                        }
                        return true;
                    case 159766:
                        WifiSingleScanStateMachine.this.removeSingleScanRequest(ci, msg.arg2);
                        return true;
                    default:
                        return false;
                }
            }
        }

        class IdleState extends State {
            IdleState() {
            }

            public void enter() {
                WifiSingleScanStateMachine.this.tryToStartNewScan();
            }

            public boolean processMessage(Message msg) {
                return false;
            }
        }

        class ScanningState extends State {
            private WorkSource mScanWorkSource;

            ScanningState() {
            }

            public void enter() {
                this.mScanWorkSource = WifiSingleScanStateMachine.this.mActiveScans.createMergedWorkSource();
                try {
                    WifiScanningServiceImpl.this.mBatteryStats.noteWifiScanStartedFromSource(this.mScanWorkSource);
                } catch (RemoteException e) {
                    WifiSingleScanStateMachine.this.loge(e.toString());
                }
            }

            public void exit() {
                WifiNative.ScanSettings unused = WifiSingleScanStateMachine.this.mActiveScanSettings = null;
                try {
                    WifiScanningServiceImpl.this.mBatteryStats.noteWifiScanStoppedFromSource(this.mScanWorkSource);
                } catch (RemoteException e) {
                    WifiSingleScanStateMachine.this.loge(e.toString());
                }
                WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(0, WifiSingleScanStateMachine.this.mActiveScans.size());
                WifiSingleScanStateMachine.this.sendOpFailedToAllAndClear(WifiSingleScanStateMachine.this.mActiveScans, -1, "Scan was interrupted");
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                if (i != WifiScanningServiceImpl.CMD_SCAN_FAILED) {
                    switch (i) {
                        case WifiConnectivityManager.MAX_PERIODIC_SCAN_INTERVAL_MS:
                            WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(1, WifiSingleScanStateMachine.this.mActiveScans.size());
                            WifiSingleScanStateMachine.this.reportScanResults(WifiScanningServiceImpl.this.mScannerImpl.getLatestSingleScanResults());
                            WifiSingleScanStateMachine.this.mActiveScans.clear();
                            WifiSingleScanStateMachine.this.transitionTo(WifiSingleScanStateMachine.this.mIdleState);
                            return true;
                        case WifiScanningServiceImpl.CMD_FULL_SCAN_RESULTS /*160001*/:
                            WifiSingleScanStateMachine.this.reportFullScanResult((ScanResult) msg.obj, msg.arg2);
                            return true;
                        default:
                            return false;
                    }
                } else {
                    WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(0, WifiSingleScanStateMachine.this.mActiveScans.size());
                    WifiSingleScanStateMachine.this.sendOpFailedToAllAndClear(WifiSingleScanStateMachine.this.mActiveScans, -1, "Scan failed");
                    WifiSingleScanStateMachine.this.transitionTo(WifiSingleScanStateMachine.this.mIdleState);
                    return true;
                }
            }
        }

        WifiSingleScanStateMachine(Looper looper) {
            super("WifiSingleScanStateMachine", looper);
            setLogRecSize(128);
            setLogOnlyTransitions(false);
            addState(this.mDefaultState);
            addState(this.mDriverStartedState, this.mDefaultState);
            addState(this.mIdleState, this.mDriverStartedState);
            addState(this.mScanningState, this.mDriverStartedState);
            setInitialState(this.mDefaultState);
        }

        public void onScanStatus(int event) {
            switch (event) {
                case 0:
                case 1:
                case 2:
                    sendMessage(WifiConnectivityManager.MAX_PERIODIC_SCAN_INTERVAL_MS);
                    return;
                case 3:
                    sendMessage(WifiScanningServiceImpl.CMD_SCAN_FAILED);
                    return;
                default:
                    Log.e(WifiScanningServiceImpl.TAG, "Unknown scan status event: " + event);
                    return;
            }
        }

        public void onFullScanResult(ScanResult fullScanResult, int bucketsScanned) {
            sendMessage(WifiScanningServiceImpl.CMD_FULL_SCAN_RESULTS, 0, bucketsScanned, fullScanResult);
        }

        public void onScanPaused(WifiScanner.ScanData[] scanData) {
            Log.e(WifiScanningServiceImpl.TAG, "Got scan paused for single scan");
        }

        public void onScanRestarted() {
            Log.e(WifiScanningServiceImpl.TAG, "Got scan restarted for single scan");
        }

        /* access modifiers changed from: package-private */
        public boolean validateScanType(int type) {
            return type == 0 || type == 1 || type == 2;
        }

        /* access modifiers changed from: package-private */
        public boolean validateScanRequest(ClientInfo ci, int handler, WifiScanner.ScanSettings settings) {
            if (ci == null || settings == null) {
                Log.d("WifiScanLog", WifiScanner.getScanKey(handler) + " Failing single scan request ClientInfo not found " + handler + " ci is :" + ci + " settings is :" + settings);
                return false;
            } else if (settings.band == 0 && (settings.channels == null || settings.channels.length == 0)) {
                Log.d("WifiScanLog", WifiScanner.getScanKey(handler) + "Failing single scan because channel list was empty");
                return false;
            } else if (!validateScanType(settings.type)) {
                Log.e(WifiScanningServiceImpl.TAG, "Invalid scan type " + settings.type);
                return false;
            } else {
                if (WifiScanningServiceImpl.this.mContext.checkPermission("android.permission.NETWORK_STACK", -1, ci.getUid()) == -1) {
                    if (!ArrayUtils.isEmpty(settings.hiddenNetworks)) {
                        Log.e(WifiScanningServiceImpl.TAG, "Failing single scan because app " + ci.getUid() + " does not have permission to set hidden networks");
                        return false;
                    } else if (settings.type != 0) {
                        Log.e(WifiScanningServiceImpl.TAG, "Failing single scan because app " + ci.getUid() + " does not have permission to set type");
                        return false;
                    }
                }
                return true;
            }
        }

        /* access modifiers changed from: package-private */
        public int getNativeScanType(int type) {
            switch (type) {
                case 0:
                    return 0;
                case 1:
                    return 1;
                case 2:
                    return 2;
                default:
                    throw new IllegalArgumentException("Invalid scan type " + type);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean activeScanTypeSatisfies(int requestScanType) {
            boolean z = true;
            switch (this.mActiveScanSettings.scanType) {
                case 0:
                case 1:
                    if (requestScanType == 2) {
                        z = false;
                    }
                    return z;
                case 2:
                    return true;
                default:
                    throw new IllegalArgumentException("Invalid scan type " + this.mActiveScanSettings.scanType);
            }
        }

        /* access modifiers changed from: package-private */
        public int mergeScanTypes(int existingScanType, int newScanType) {
            switch (existingScanType) {
                case 0:
                case 1:
                    return newScanType;
                case 2:
                    return existingScanType;
                default:
                    throw new IllegalArgumentException("Invalid scan type " + existingScanType);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean activeScanSatisfies(WifiScanner.ScanSettings settings) {
            if (this.mActiveScanSettings == null || !activeScanTypeSatisfies(getNativeScanType(settings.type))) {
                return false;
            }
            WifiNative.BucketSettings activeBucket = this.mActiveScanSettings.buckets[0];
            ChannelHelper.ChannelCollection activeChannels = WifiScanningServiceImpl.this.mChannelHelper.createChannelCollection();
            activeChannels.addChannels(activeBucket);
            if (!activeChannels.containsSettings(settings)) {
                return false;
            }
            if ((settings.reportEvents & 2) != 0 && (activeBucket.report_events & 2) == 0) {
                return false;
            }
            if (!ArrayUtils.isEmpty(settings.hiddenNetworks)) {
                if (ArrayUtils.isEmpty(this.mActiveScanSettings.hiddenNetworks)) {
                    return false;
                }
                List<WifiNative.HiddenNetwork> activeHiddenNetworks = new ArrayList<>();
                for (WifiNative.HiddenNetwork hiddenNetwork : this.mActiveScanSettings.hiddenNetworks) {
                    activeHiddenNetworks.add(hiddenNetwork);
                }
                for (WifiScanner.ScanSettings.HiddenNetwork hiddenNetwork2 : settings.hiddenNetworks) {
                    WifiNative.HiddenNetwork nativeHiddenNetwork = new WifiNative.HiddenNetwork();
                    nativeHiddenNetwork.ssid = hiddenNetwork2.ssid;
                    if (!activeHiddenNetworks.contains(nativeHiddenNetwork)) {
                        return false;
                    }
                }
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public void removeSingleScanRequest(ClientInfo ci, int handler) {
            if (ci != null) {
                WifiScanningServiceImpl.this.logScanRequest("removeSingleScanRequest", ci, handler, null, null, null);
                this.mPendingScans.removeRequest(ci, handler);
                this.mActiveScans.removeRequest(ci, handler);
            }
        }

        /* access modifiers changed from: package-private */
        public void removeSingleScanRequests(ClientInfo ci) {
            if (ci != null) {
                WifiScanningServiceImpl.this.logScanRequest("removeSingleScanRequests", ci, -1, null, null, null);
                this.mPendingScans.removeAllForClient(ci);
                this.mActiveScans.removeAllForClient(ci);
            }
        }

        /* access modifiers changed from: package-private */
        public void tryToStartNewScan() {
            if (this.mPendingScans.size() != 0) {
                WifiScanningServiceImpl.this.mChannelHelper.updateChannels();
                WifiNative.ScanSettings settings = new WifiNative.ScanSettings();
                settings.num_buckets = 1;
                WifiNative.BucketSettings bucketSettings = new WifiNative.BucketSettings();
                bucketSettings.bucket = 0;
                bucketSettings.period_ms = 0;
                bucketSettings.report_events = 1;
                ChannelHelper.ChannelCollection channels = WifiScanningServiceImpl.this.mChannelHelper.createChannelCollection();
                List<WifiNative.HiddenNetwork> hiddenNetworkList = new ArrayList<>();
                StringBuilder keys = new StringBuilder();
                Iterator it = this.mPendingScans.iterator();
                while (it.hasNext()) {
                    RequestInfo<WifiScanner.ScanSettings> entry = (RequestInfo) it.next();
                    settings.scanType = mergeScanTypes(settings.scanType, getNativeScanType(((WifiScanner.ScanSettings) entry.settings).type));
                    keys.append(WifiScanner.getScanKey(entry.handlerId));
                    channels.addChannels((WifiScanner.ScanSettings) entry.settings);
                    if (((WifiScanner.ScanSettings) entry.settings).hiddenNetworks != null) {
                        for (WifiScanner.ScanSettings.HiddenNetwork hiddenNetwork : ((WifiScanner.ScanSettings) entry.settings).hiddenNetworks) {
                            WifiNative.HiddenNetwork hiddenNetwork2 = new WifiNative.HiddenNetwork();
                            hiddenNetwork2.ssid = hiddenNetwork.ssid;
                            hiddenNetworkList.add(hiddenNetwork2);
                        }
                    }
                    if ((((WifiScanner.ScanSettings) entry.settings).reportEvents & 2) != 0) {
                        bucketSettings.report_events |= 2;
                    }
                    if (((WifiScanner.ScanSettings) entry.settings).isHiddenSigleScan) {
                        settings.isHiddenSingleScan = true;
                        Log.d(WifiScanningServiceImpl.TAG, "tryToStartNewScan isHiddenSingleScan = true");
                    }
                }
                if (hiddenNetworkList.size() > 0) {
                    settings.hiddenNetworks = new WifiNative.HiddenNetwork[hiddenNetworkList.size()];
                    int numHiddenNetworks = 0;
                    for (WifiNative.HiddenNetwork hiddenNetwork3 : hiddenNetworkList) {
                        settings.hiddenNetworks[numHiddenNetworks] = hiddenNetwork3;
                        numHiddenNetworks++;
                    }
                }
                channels.fillBucketSettings(bucketSettings, ScoringParams.Values.MAX_EXPID);
                settings.buckets = new WifiNative.BucketSettings[]{bucketSettings};
                WifiScanningServiceImpl.this.localLog(keys.toString(), "25", "tryToStartNewScan in WifiScanningServiceImpl");
                settings.handlerId = keys.toString();
                if (WifiScanningServiceImpl.this.mScannerImpl == null || !WifiScanningServiceImpl.this.mScannerImpl.startSingleScan(settings, this)) {
                    Log.w("WifiScanLog", keys + "start single scan failed ");
                    WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(0, this.mPendingScans.size());
                    sendOpFailedToAllAndClear(this.mPendingScans, -1, "Failed to start single scan");
                } else {
                    this.mActiveScanSettings = settings;
                    RequestList<WifiScanner.ScanSettings> tmp = this.mActiveScans;
                    this.mActiveScans = this.mPendingScans;
                    this.mPendingScans = tmp;
                    this.mPendingScans.clear();
                    transitionTo(this.mScanningState);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void sendOpFailedToAllAndClear(RequestList<?> clientHandlers, int reason, String description) {
            StringBuilder keys = new StringBuilder();
            Iterator it = clientHandlers.iterator();
            while (it.hasNext()) {
                RequestInfo<?> entry = (RequestInfo) it.next();
                WifiScanningServiceImpl wifiScanningServiceImpl = WifiScanningServiceImpl.this;
                ClientInfo clientInfo = entry.clientInfo;
                int i = entry.handlerId;
                wifiScanningServiceImpl.logCallback("singleScanFailed", clientInfo, i, "reason=" + reason + ", " + description);
                entry.reportEvent(159762, 0, new WifiScanner.OperationResult(reason, description));
                keys.append(WifiScanner.getScanKey(entry.handlerId));
            }
            if (clientHandlers.size() > 0) {
                Log.w("WifiScanLog", keys + "WifiSingleScanStateMachine sendOpFailedToAllAndClear scan failed des:" + description);
            }
            clientHandlers.clear();
        }

        /* access modifiers changed from: package-private */
        public void reportFullScanResult(ScanResult result, int bucketsScanned) {
            Iterator it = this.mActiveScans.iterator();
            while (it.hasNext()) {
                RequestInfo<WifiScanner.ScanSettings> entry = (RequestInfo) it.next();
                if (ScanScheduleUtil.shouldReportFullScanResultForSettings(WifiScanningServiceImpl.this.mChannelHelper, result, bucketsScanned, (WifiScanner.ScanSettings) entry.settings, -1)) {
                    entry.reportEvent(159764, 0, result);
                }
            }
            Iterator it2 = WifiScanningServiceImpl.this.mSingleScanListeners.iterator();
            while (it2.hasNext()) {
                ((RequestInfo) it2.next()).reportEvent(159764, 0, result);
            }
        }

        /* access modifiers changed from: package-private */
        public void reportScanResults(WifiScanner.ScanData results) {
            if (!(results == null || results.getResults() == null)) {
                if (results.getResults().length > 0) {
                    WifiScanningServiceImpl.this.mWifiMetrics.incrementNonEmptyScanResultCount();
                } else {
                    WifiScanningServiceImpl.this.mWifiMetrics.incrementEmptyScanResultCount();
                }
            }
            WifiScanner.ScanData[] allResults = {results};
            StringBuilder keys = new StringBuilder();
            Iterator it = this.mActiveScans.iterator();
            while (it.hasNext()) {
                RequestInfo<WifiScanner.ScanSettings> entry = (RequestInfo) it.next();
                WifiScanner.ScanData[] resultsToDeliver = ScanScheduleUtil.filterResultsForSettings(WifiScanningServiceImpl.this.mChannelHelper, allResults, (WifiScanner.ScanSettings) entry.settings, -1);
                WifiScanner.ParcelableScanData parcelableResultsToDeliver = new WifiScanner.ParcelableScanData(resultsToDeliver);
                WifiScanningServiceImpl.this.logCallback("singleScanResults", entry.clientInfo, entry.handlerId, WifiScanningServiceImpl.describeForLog(resultsToDeliver));
                keys.append(WifiScanner.getScanKey(entry.handlerId));
                entry.reportEvent(159749, 0, parcelableResultsToDeliver);
                entry.reportEvent(159767, 0, null);
                if (!WifiScanningServiceImpl.mSendScanResultsBroadcast && shouldSendScanResultsBroadcast(entry, false)) {
                    boolean unused = WifiScanningServiceImpl.mSendScanResultsBroadcast = true;
                }
            }
            if (!WifiScanningServiceImpl.mSendScanResultsBroadcast) {
                Iterator it2 = this.mPendingScans.iterator();
                while (true) {
                    if (it2.hasNext()) {
                        if (shouldSendScanResultsBroadcast((RequestInfo) it2.next(), true)) {
                            boolean unused2 = WifiScanningServiceImpl.mSendScanResultsBroadcast = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            WifiScanner.ParcelableScanData parcelableAllResults = new WifiScanner.ParcelableScanData(allResults);
            Iterator it3 = WifiScanningServiceImpl.this.mSingleScanListeners.iterator();
            while (it3.hasNext()) {
                RequestInfo<Void> entry2 = (RequestInfo) it3.next();
                WifiScanningServiceImpl.this.logCallback("singleScanResults", entry2.clientInfo, entry2.handlerId, WifiScanningServiceImpl.describeForLog(allResults));
                entry2.reportEvent(159774, 0, keys.toString());
                entry2.reportEvent(159749, 0, parcelableAllResults);
            }
            if (results != null) {
                this.mCachedScanResults.clear();
                this.mCachedScanResults.addAll(Arrays.asList(results.getResults()));
                WifiScanningServiceImpl.this.updateScanResultByWifiPro(this.mCachedScanResults);
            } else {
                Log.w(WifiScanningServiceImpl.TAG, "LatestSingleScanResult is null");
                Log.w(WifiScanningServiceImpl.TAG, "reportScanResults: not add scan results, not send broadcast");
            }
            boolean unused3 = WifiScanningServiceImpl.mSendScanResultsBroadcast = false;
        }

        /* access modifiers changed from: package-private */
        public List<ScanResult> getCachedScanResultsAsList() {
            return this.mCachedScanResults;
        }

        private boolean shouldSendScanResultsBroadcast(RequestInfo<WifiScanner.ScanSettings> requestInfo, boolean isPendingScans) {
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = ((ActivityManager) WifiScanningServiceImpl.this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (runningTaskInfos != null && !runningTaskInfos.isEmpty()) {
                ComponentName cn = runningTaskInfos.get(0).topActivity;
                if (!(cn == null || cn.getClassName() == null || !cn.getClassName().startsWith("com.android.settings.Settings$WifiSettingsActivity"))) {
                    Log.d(WifiScanningServiceImpl.TAG, "shouldSendScanResultsBroadcast:true. WifiSettingsActivity");
                    return true;
                }
            }
            if (requestInfo.workSource != null) {
                for (int index = 0; index < requestInfo.workSource.size(); index++) {
                    if (1010 != requestInfo.workSource.get(index)) {
                        Log.d(WifiScanningServiceImpl.TAG, "shouldSendScanResultsBroadcast:true. Not only WIFI_UID scans. isPendingScans:" + isPendingScans);
                        return true;
                    }
                }
            }
            if (isPendingScans || 7 != ((WifiScanner.ScanSettings) requestInfo.settings).band) {
                return false;
            }
            Log.d(WifiScanningServiceImpl.TAG, "shouldSendScanResultsBroadcast:true. Band is WIFI_BAND_BOTH_WITH_DFS");
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void localLog(String message) {
        this.mLocalLog.log(message);
    }

    /* access modifiers changed from: private */
    public void logw(String message) {
        Log.w(TAG, message);
        this.mLocalLog.log(message);
    }

    /* access modifiers changed from: private */
    public void loge(String message) {
        Log.e(TAG, message);
        this.mLocalLog.log(message);
    }

    public Messenger getMessenger() {
        if (this.mClientHandler != null) {
            this.mLog.trace("getMessenger() uid=%").c((long) Binder.getCallingUid()).flush();
            return new Messenger(this.mClientHandler);
        }
        loge("WifiScanningServiceImpl trying to get messenger w/o initialization");
        return null;
    }

    public Bundle getAvailableChannels(int band) {
        this.mChannelHelper.updateChannels();
        WifiScanner.ChannelSpec[] channelSpecs = this.mChannelHelper.getAvailableScanChannels(band);
        ArrayList<Integer> list = new ArrayList<>(channelSpecs.length);
        for (WifiScanner.ChannelSpec channelSpec : channelSpecs) {
            list.add(Integer.valueOf(channelSpec.frequency));
        }
        Bundle b = new Bundle();
        b.putIntegerArrayList("Channels", list);
        this.mLog.trace("getAvailableChannels uid=%").c((long) Binder.getCallingUid()).flush();
        return b;
    }

    /* access modifiers changed from: private */
    public void enforceLocationHardwarePermission(int uid) {
        this.mContext.enforcePermission("android.permission.LOCATION_HARDWARE", -1, uid, "LocationHardware");
    }

    public WifiScanningServiceImpl(Context context, Looper looper, WifiScannerImpl.WifiScannerImplFactory scannerImplFactory, IBatteryStats batteryStats, WifiInjector wifiInjector) {
        this.mContext = context;
        this.mLooper = looper;
        this.mScannerImplFactory = scannerImplFactory;
        this.mBatteryStats = batteryStats;
        this.mClients = new ArrayMap<>();
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mWifiMetrics = wifiInjector.getWifiMetrics();
        this.mClock = wifiInjector.getClock();
        this.mLog = wifiInjector.makeLog(TAG);
        this.mFrameworkFacade = wifiInjector.getFrameworkFacade();
        this.mPreviousSchedule = null;
    }

    public void startService() {
        this.mBackgroundScanStateMachine = new WifiBackgroundScanStateMachine(this.mLooper);
        this.mSingleScanStateMachine = new WifiSingleScanStateMachine(this.mLooper);
        this.mPnoScanStateMachine = new WifiPnoScanStateMachine(this.mLooper);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra("scan_enabled", 1);
                if (state == 3) {
                    WifiScanningServiceImpl.this.mBackgroundScanStateMachine.sendMessage(WifiScanningServiceImpl.CMD_DRIVER_LOADED);
                    WifiScanningServiceImpl.this.mSingleScanStateMachine.sendMessage(WifiScanningServiceImpl.CMD_DRIVER_LOADED);
                    WifiScanningServiceImpl.this.mPnoScanStateMachine.sendMessage(WifiScanningServiceImpl.CMD_DRIVER_LOADED);
                } else if (state == 1) {
                    WifiScanningServiceImpl.this.mBackgroundScanStateMachine.sendMessage(WifiScanningServiceImpl.CMD_DRIVER_UNLOADED);
                    WifiScanningServiceImpl.this.mSingleScanStateMachine.sendMessage(WifiScanningServiceImpl.CMD_DRIVER_UNLOADED);
                    WifiScanningServiceImpl.this.mPnoScanStateMachine.sendMessage(WifiScanningServiceImpl.CMD_DRIVER_UNLOADED);
                }
            }
        }, new IntentFilter("wifi_scan_available"));
        this.mBackgroundScanStateMachine.start();
        this.mSingleScanStateMachine.start();
        this.mPnoScanStateMachine.start();
        this.mClientHandler = new ClientHandler(TAG, this.mLooper);
    }

    @VisibleForTesting
    public void setWifiHandlerLogForTest(WifiLog log) {
        this.mClientHandler.setWifiLog(log);
    }

    /* access modifiers changed from: private */
    public WorkSource computeWorkSource(ClientInfo ci, WorkSource requestedWorkSource) {
        if (requestedWorkSource != null) {
            requestedWorkSource.clearNames();
            if (!requestedWorkSource.isEmpty()) {
                return requestedWorkSource;
            }
        }
        if (ci.getUid() > 0) {
            return new WorkSource(ci.getUid());
        }
        loge("Unable to compute workSource for client: " + ci + ", requested: " + requestedWorkSource);
        return new WorkSource();
    }

    /* access modifiers changed from: package-private */
    public void replySucceeded(Message msg) {
        if (msg.replyTo != null) {
            Message reply = Message.obtain();
            reply.what = 159761;
            reply.arg2 = msg.arg2;
            if (msg.obj != null) {
                reply.obj = msg.obj;
            }
            try {
                msg.replyTo.send(reply);
                this.mLog.trace("replySucceeded recvdMessage=%").c((long) msg.what).flush();
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void replyFailed(Message msg, int reason, String description) {
        if (msg.replyTo != null) {
            Message reply = Message.obtain();
            reply.what = 159762;
            reply.arg2 = msg.arg2;
            reply.obj = new WifiScanner.OperationResult(reason, description);
            try {
                msg.replyTo.send(reply);
                this.mLog.trace("replyFailed recvdMessage=% reason=%").c((long) msg.what).c((long) reason).flush();
            } catch (RemoteException e) {
            }
        }
    }

    private static String toString(int uid, WifiScanner.ScanSettings settings) {
        StringBuilder sb = new StringBuilder();
        sb.append("ScanSettings[uid=");
        sb.append(uid);
        sb.append(", period=");
        sb.append(settings.periodInMs);
        sb.append(", report=");
        sb.append(settings.reportEvents);
        if (settings.reportEvents == 0 && settings.numBssidsPerScan > 0 && settings.maxScansToCache > 1) {
            sb.append(", batch=");
            sb.append(settings.maxScansToCache);
            sb.append(", numAP=");
            sb.append(settings.numBssidsPerScan);
        }
        sb.append(", ");
        sb.append(ChannelHelper.toString(settings));
        sb.append("]");
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump WifiScanner from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        pw.println("WifiScanningService - Log Begin ----");
        this.mLocalLog.dump(fd, pw, args);
        pw.println("WifiScanningService - Log End ----");
        pw.println();
        pw.println("clients:");
        Iterator<ClientInfo> it = this.mClients.values().iterator();
        while (it.hasNext()) {
            pw.println("  " + it.next());
        }
        pw.println("listeners:");
        for (ClientInfo client : this.mClients.values()) {
            Iterator<WifiScanner.ScanSettings> it2 = this.mBackgroundScanStateMachine.getBackgroundScanSettings(client).iterator();
            while (it2.hasNext()) {
                pw.println("  " + toString(client.mUid, it2.next()));
            }
        }
        if (this.mBackgroundScheduler != null) {
            WifiNative.ScanSettings schedule = this.mBackgroundScheduler.getSchedule();
            if (schedule != null) {
                pw.println("schedule:");
                pw.println("  base period: " + schedule.base_period_ms);
                pw.println("  max ap per scan: " + schedule.max_ap_per_scan);
                pw.println("  batched scans: " + schedule.report_threshold_num_scans);
                pw.println("  buckets:");
                for (int b = 0; b < schedule.num_buckets; b++) {
                    WifiNative.BucketSettings bucket = schedule.buckets[b];
                    pw.println("    bucket " + bucket.bucket + " (" + bucket.period_ms + "ms)[" + bucket.report_events + "]: " + ChannelHelper.toString(bucket));
                }
            }
        }
        if (this.mPnoScanStateMachine != null) {
            this.mPnoScanStateMachine.dump(fd, pw, args);
        }
        pw.println();
        if (this.mSingleScanStateMachine != null) {
            this.mSingleScanStateMachine.dump(fd, pw, args);
            pw.println();
            pw.println("Latest scan results:");
            ScanResultUtil.dumpScanResults(pw, this.mSingleScanStateMachine.getCachedScanResultsAsList(), this.mClock.getElapsedSinceBootMillis());
            pw.println();
        }
        if (this.mScannerImpl != null) {
            this.mScannerImpl.dump(fd, pw, args);
        }
    }

    /* access modifiers changed from: package-private */
    public void logScanRequest(String request, ClientInfo ci, int id, WorkSource workSource, WifiScanner.ScanSettings settings, WifiScanner.PnoSettings pnoSettings) {
        StringBuilder sb = new StringBuilder();
        sb.append(request);
        sb.append(": ");
        sb.append(ci == null ? "ClientInfo[unknown]" : ci.toString());
        sb.append(",Id=");
        sb.append(id);
        if (workSource != null) {
            sb.append(",");
            sb.append(workSource);
        }
        if (settings != null) {
            sb.append(", ");
            describeTo(sb, settings);
        }
        if (pnoSettings != null) {
            sb.append(", ");
            describeTo(sb, pnoSettings);
        }
        localLog(sb.toString());
    }

    /* access modifiers changed from: package-private */
    public void logCallback(String callback, ClientInfo ci, int id, String extra) {
        StringBuilder sb = new StringBuilder();
        sb.append(callback);
        sb.append(": ");
        sb.append(ci == null ? "ClientInfo[unknown]" : ci.toString());
        sb.append(",Id=");
        sb.append(WifiScanner.getScanKey(id));
        if (extra != null) {
            sb.append(",");
            sb.append(extra);
        }
        localLog(sb.toString());
    }

    static String describeForLog(WifiScanner.ScanData[] results) {
        StringBuilder sb = new StringBuilder();
        sb.append("results=");
        for (int i = 0; i < results.length; i++) {
            if (i > 0) {
                sb.append(NAIRealmData.NAI_REALM_STRING_SEPARATOR);
            }
            sb.append(results[i].getResults().length);
        }
        return sb.toString();
    }

    static String describeForLog(ScanResult[] results) {
        return "results=" + results.length;
    }

    static String getScanTypeString(int type) {
        switch (type) {
            case 0:
                return "LOW LATENCY";
            case 1:
                return "LOW POWER";
            case 2:
                return "HIGH ACCURACY";
            default:
                throw new IllegalArgumentException("Invalid scan type " + type);
        }
    }

    static String describeTo(StringBuilder sb, WifiScanner.ScanSettings scanSettings) {
        sb.append("ScanSettings { ");
        sb.append(" type:");
        sb.append(getScanTypeString(scanSettings.type));
        sb.append(" band:");
        sb.append(ChannelHelper.bandToString(scanSettings.band));
        sb.append(" period:");
        sb.append(scanSettings.periodInMs);
        sb.append(" reportEvents:");
        sb.append(scanSettings.reportEvents);
        sb.append(" numBssidsPerScan:");
        sb.append(scanSettings.numBssidsPerScan);
        sb.append(" maxScansToCache:");
        sb.append(scanSettings.maxScansToCache);
        sb.append(" channels:[ ");
        if (scanSettings.channels != null) {
            for (WifiScanner.ChannelSpec channelSpec : scanSettings.channels) {
                sb.append(channelSpec.frequency);
                sb.append(" ");
            }
        }
        sb.append(" ] ");
        sb.append(" } ");
        return sb.toString();
    }

    static String describeTo(StringBuilder sb, WifiScanner.PnoSettings pnoSettings) {
        sb.append("PnoSettings { ");
        sb.append(" min5GhzRssi:");
        sb.append(pnoSettings.min5GHzRssi);
        sb.append(" min24GhzRssi:");
        sb.append(pnoSettings.min24GHzRssi);
        sb.append(" initialScoreMax:");
        sb.append(pnoSettings.initialScoreMax);
        sb.append(" currentConnectionBonus:");
        sb.append(pnoSettings.currentConnectionBonus);
        sb.append(" sameNetworkBonus:");
        sb.append(pnoSettings.sameNetworkBonus);
        sb.append(" secureBonus:");
        sb.append(pnoSettings.secureBonus);
        sb.append(" band5GhzBonus:");
        sb.append(pnoSettings.band5GHzBonus);
        sb.append(" isConnected:");
        sb.append(pnoSettings.isConnected);
        sb.append(" networks:[ ");
        if (pnoSettings.networkList != null) {
            for (WifiScanner.PnoSettings.PnoNetwork pnoNetwork : pnoSettings.networkList) {
                sb.append(pnoNetwork.ssid);
                sb.append(",");
            }
        }
        sb.append(" ] ");
        sb.append(" } ");
        return sb.toString();
    }

    public void updateScanResultByWifiPro(List<ScanResult> list) {
    }

    /* access modifiers changed from: package-private */
    public void localLog(String scanKey, String eventKey, String log) {
        localLog(scanKey, eventKey, log, null);
    }

    /* access modifiers changed from: package-private */
    public void localLog(String scanKey, String eventKey, String log, Object... params) {
        WifiConnectivityHelper.localLog(this.mLocalLog, scanKey, eventKey, log, params);
    }
}
