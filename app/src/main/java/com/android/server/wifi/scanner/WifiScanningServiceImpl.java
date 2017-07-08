package com.android.server.wifi.scanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.IWifiScanner.Stub;
import android.net.wifi.ScanResult;
import android.net.wifi.ScanResult.InformationElement;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.BssidInfo;
import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.HotlistSettings;
import android.net.wifi.WifiScanner.OperationResult;
import android.net.wifi.WifiScanner.ParcelableScanData;
import android.net.wifi.WifiScanner.ParcelableScanResults;
import android.net.wifi.WifiScanner.PnoSettings;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.WifiChangeSettings;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.ArrayMap;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.Clock;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiMetrics;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiNative.BucketSettings;
import com.android.server.wifi.WifiNative.HotlistEventHandler;
import com.android.server.wifi.WifiNative.PnoEventHandler;
import com.android.server.wifi.WifiNative.PnoNetwork;
import com.android.server.wifi.WifiNative.ScanCapabilities;
import com.android.server.wifi.WifiNative.ScanEventHandler;
import com.android.server.wifi.WifiNative.ScanSettings;
import com.android.server.wifi.WifiNative.SignificantWifiChangeEventHandler;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.anqp.CivicLocationElement;
import com.android.server.wifi.scanner.ChannelHelper.ChannelCollection;
import com.android.server.wifi.scanner.WifiScannerImpl.WifiScannerImplFactory;
import com.android.server.wifi.util.ApConfigUtil;
import com.google.protobuf.nano.Extension;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class WifiScanningServiceImpl extends Stub {
    private static final int BASE = 160000;
    private static final int CMD_DRIVER_LOADED = 160006;
    private static final int CMD_DRIVER_UNLOADED = 160007;
    private static final int CMD_FULL_SCAN_RESULTS = 160001;
    private static final int CMD_HOTLIST_AP_FOUND = 160002;
    private static final int CMD_HOTLIST_AP_LOST = 160003;
    private static final int CMD_PNO_NETWORK_FOUND = 160011;
    private static final int CMD_PNO_SCAN_FAILED = 160012;
    private static final int CMD_SCAN_FAILED = 160010;
    private static final int CMD_SCAN_PAUSED = 160008;
    private static final int CMD_SCAN_RESTARTED = 160009;
    private static final int CMD_SCAN_RESULTS_AVAILABLE = 160000;
    private static final int CMD_WIFI_CHANGE_DETECTED = 160004;
    private static final int CMD_WIFI_CHANGE_TIMEOUT = 160005;
    private static final boolean DBG = false;
    private static final int MIN_PERIOD_PER_CHANNEL_MS = 200;
    private static final String TAG = "WifiScanningService";
    private static final int UNKNOWN_PID = -1;
    private final AlarmManager mAlarmManager;
    private WifiBackgroundScanStateMachine mBackgroundScanStateMachine;
    private BackgroundScanScheduler mBackgroundScheduler;
    private final IBatteryStats mBatteryStats;
    private ChannelHelper mChannelHelper;
    private ClientHandler mClientHandler;
    private final ArrayMap<Messenger, ClientInfo> mClients;
    private final Clock mClock;
    private final Context mContext;
    private final LocalLog mLocalLog;
    private final Looper mLooper;
    private WifiPnoScanStateMachine mPnoScanStateMachine;
    private ScanSettings mPreviousSchedule;
    private WifiScannerImpl mScannerImpl;
    private final WifiScannerImplFactory mScannerImplFactory;
    private WifiSingleScanStateMachine mSingleScanStateMachine;
    private WifiChangeStateMachine mWifiChangeStateMachine;
    private final WifiMetrics mWifiMetrics;

    private class ClientHandler extends Handler {
        ClientHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            ExternalClientInfo client;
            switch (msg.what) {
                case 69633:
                    client = (ExternalClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                    if (client != null) {
                        WifiScanningServiceImpl.this.logw("duplicate client connection: " + msg.sendingUid);
                        client.mChannel.replyToMessage(msg, 69634, 3);
                        return;
                    }
                    AsyncChannel ac = new AsyncChannel();
                    ac.connected(WifiScanningServiceImpl.this.mContext, this, msg.replyTo);
                    new ExternalClientInfo(msg.sendingUid, msg.replyTo, ac).register();
                    ac.replyToMessage(msg, 69634, 0);
                case 69635:
                    client = (ExternalClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                    if (client != null) {
                        client.mChannel.disconnect();
                    }
                case 69636:
                    client = (ExternalClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                    if (client != null) {
                        client.cleanup();
                    }
                default:
                    try {
                        WifiScanningServiceImpl.this.enforceLocationHardwarePermission(msg.sendingUid);
                        if (msg.what == 159748) {
                            WifiScanningServiceImpl.this.mBackgroundScanStateMachine.sendMessage(Message.obtain(msg));
                        } else if (((ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo)) == null) {
                            WifiScanningServiceImpl.this.loge("Could not find client info for message " + msg.replyTo);
                            WifiScanningServiceImpl.this.replyFailed(msg, -2, "Could not find listener");
                        } else {
                            switch (msg.what) {
                                case 159746:
                                case 159747:
                                case 159750:
                                case 159751:
                                    WifiScanningServiceImpl.this.mBackgroundScanStateMachine.sendMessage(Message.obtain(msg));
                                    break;
                                case 159755:
                                case 159756:
                                case 159757:
                                    WifiScanningServiceImpl.this.mWifiChangeStateMachine.sendMessage(Message.obtain(msg));
                                    break;
                                case 159765:
                                case 159766:
                                    WifiScanningServiceImpl.this.mSingleScanStateMachine.sendMessage(Message.obtain(msg));
                                    break;
                                case 159768:
                                case 159769:
                                    WifiScanningServiceImpl.this.mPnoScanStateMachine.sendMessage(Message.obtain(msg));
                                    break;
                                default:
                                    WifiScanningServiceImpl.this.replyFailed(msg, -3, "Invalid request");
                                    break;
                            }
                        }
                    } catch (SecurityException e) {
                        WifiScanningServiceImpl.this.localLog("failed to authorize app: " + e);
                        WifiScanningServiceImpl.this.replyFailed(msg, -4, "Not authorized");
                    }
            }
        }
    }

    private abstract class ClientInfo {
        protected final Messenger mMessenger;
        private boolean mScanWorkReported;
        private final int mUid;
        private final WorkSource mWorkSource;

        public abstract void reportEvent(int i, int i2, int i3, Object obj);

        ClientInfo(int uid, Messenger messenger) {
            this.mScanWorkReported = WifiScanningServiceImpl.DBG;
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
            WifiScanningServiceImpl.this.mSingleScanStateMachine.removeSingleScanRequests(this);
            WifiScanningServiceImpl.this.mBackgroundScanStateMachine.removeBackgroundScanSettings(this);
            WifiScanningServiceImpl.this.mBackgroundScanStateMachine.removeHotlistSettings(this);
            unregister();
            WifiScanningServiceImpl.this.localLog("Successfully stopped all requests for client " + this);
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
                    WifiScanningServiceImpl.this.logw("failed to report scan work: " + e.toString());
                }
            }
        }

        private void reportBatchedScanStop() {
            if (this.mUid != 0) {
                try {
                    WifiScanningServiceImpl.this.mBatteryStats.noteWifiBatchedScanStoppedFromSource(this.mWorkSource);
                } catch (RemoteException e) {
                    WifiScanningServiceImpl.this.logw("failed to cleanup scan work: " + e.toString());
                }
            }
        }

        private int getCsph() {
            int totalScanDurationPerHour = 0;
            for (WifiScanner.ScanSettings settings : WifiScanningServiceImpl.this.mBackgroundScanStateMachine.getBackgroundScanSettings(this)) {
                totalScanDurationPerHour += WifiScanningServiceImpl.this.mChannelHelper.estimateScanDuration(settings) * (settings.periodInMs == 0 ? 1 : 3600000 / settings.periodInMs);
            }
            return totalScanDurationPerHour / WifiScanningServiceImpl.MIN_PERIOD_PER_CHANNEL_MS;
        }

        public void reportScanWorkUpdate() {
            if (this.mScanWorkReported) {
                reportBatchedScanStop();
                this.mScanWorkReported = WifiScanningServiceImpl.DBG;
            }
            if (WifiScanningServiceImpl.this.mBackgroundScanStateMachine.getBackgroundScanSettings(this).isEmpty()) {
                reportBatchedScanStart();
                this.mScanWorkReported = true;
            }
        }

        public String toString() {
            return "ClientInfo[uid=" + this.mUid + "]";
        }
    }

    private class ExternalClientInfo extends ClientInfo {
        private final AsyncChannel mChannel;
        private boolean mDisconnected;

        ExternalClientInfo(int uid, Messenger messenger, AsyncChannel c) {
            super(uid, messenger);
            this.mDisconnected = WifiScanningServiceImpl.DBG;
            this.mChannel = c;
        }

        public void reportEvent(int what, int arg1, int arg2, Object obj) {
            if (!this.mDisconnected) {
                this.mChannel.sendMessage(what, arg1, arg2, obj);
            }
        }

        public void cleanup() {
            this.mDisconnected = true;
            WifiScanningServiceImpl.this.mWifiChangeStateMachine.removeWifiChangeHandler(this);
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
                WifiScanningServiceImpl.this.loge("Failed to send message: " + what);
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
    }

    private class RequestInfo<T> {
        final ClientInfo clientInfo;
        final int handlerId;
        final T settings;
        final WorkSource workSource;

        RequestInfo(ClientInfo clientInfo, int handlerId, WorkSource requestedWorkSource, T settings) {
            this.clientInfo = clientInfo;
            this.handlerId = handlerId;
            this.settings = settings;
            this.workSource = WifiScanningServiceImpl.this.computeWorkSource(clientInfo, requestedWorkSource);
        }

        void reportEvent(int what, int arg1, Object obj) {
            this.clientInfo.reportEvent(what, arg1, this.handlerId, obj);
        }
    }

    private class RequestList<T> extends ArrayList<RequestInfo<T>> {
        private RequestList() {
        }

        void addRequest(ClientInfo ci, int handler, WorkSource reqworkSource, T settings) {
            add(new RequestInfo(ci, handler, reqworkSource, settings));
        }

        T removeRequest(ClientInfo ci, int handlerId) {
            T removed = null;
            Iterator<RequestInfo<T>> iter = iterator();
            while (iter.hasNext()) {
                RequestInfo<T> entry = (RequestInfo) iter.next();
                if (entry.clientInfo == ci && entry.handlerId == handlerId) {
                    removed = entry.settings;
                    iter.remove();
                }
            }
            return removed;
        }

        Collection<T> getAllSettings() {
            ArrayList<T> settingsList = new ArrayList();
            Iterator<RequestInfo<T>> iter = iterator();
            while (iter.hasNext()) {
                settingsList.add(((RequestInfo) iter.next()).settings);
            }
            return settingsList;
        }

        Collection<T> getAllSettingsForClient(ClientInfo ci) {
            ArrayList<T> settingsList = new ArrayList();
            Iterator<RequestInfo<T>> iter = iterator();
            while (iter.hasNext()) {
                RequestInfo<T> entry = (RequestInfo) iter.next();
                if (entry.clientInfo == ci) {
                    settingsList.add(entry.settings);
                }
            }
            return settingsList;
        }

        void removeAllForClient(ClientInfo ci) {
            Iterator<RequestInfo<T>> iter = iterator();
            while (iter.hasNext()) {
                if (((RequestInfo) iter.next()).clientInfo == ci) {
                    iter.remove();
                }
            }
        }

        WorkSource createMergedWorkSource() {
            WorkSource mergedSource = new WorkSource();
            for (RequestInfo<T> entry : this) {
                mergedSource.add(entry.workSource);
            }
            return mergedSource;
        }
    }

    class WifiBackgroundScanStateMachine extends StateMachine implements ScanEventHandler, HotlistEventHandler {
        private final RequestList<WifiScanner.ScanSettings> mActiveBackgroundScans;
        private final RequestList<HotlistSettings> mActiveHotlistSettings;
        private final DefaultState mDefaultState;
        private final PausedState mPausedState;
        private final StartedState mStartedState;

        class DefaultState extends State {
            DefaultState() {
            }

            public void enter() {
                WifiBackgroundScanStateMachine.this.mActiveBackgroundScans.clear();
                WifiBackgroundScanStateMachine.this.mActiveHotlistSettings.clear();
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 159746:
                    case 159747:
                    case 159748:
                    case 159750:
                    case 159751:
                    case 159765:
                    case 159766:
                        WifiScanningServiceImpl.this.replyFailed(msg, WifiScanningServiceImpl.UNKNOWN_PID, "not available");
                        break;
                    case WifiScanningServiceImpl.CMD_DRIVER_LOADED /*160006*/:
                        if (WifiScanningServiceImpl.this.mScannerImpl == null) {
                            WifiScanningServiceImpl.this.mScannerImpl = WifiScanningServiceImpl.this.mScannerImplFactory.create(WifiScanningServiceImpl.this.mContext, WifiScanningServiceImpl.this.mLooper, WifiScanningServiceImpl.this.mClock);
                            WifiScanningServiceImpl.this.mChannelHelper = WifiScanningServiceImpl.this.mScannerImpl.getChannelHelper();
                        }
                        WifiScanningServiceImpl.this.mBackgroundScheduler = new BackgroundScanScheduler(WifiScanningServiceImpl.this.mChannelHelper);
                        ScanCapabilities capabilities = new ScanCapabilities();
                        if (WifiScanningServiceImpl.this.mScannerImpl.getScanCapabilities(capabilities)) {
                            WifiScanningServiceImpl.this.mBackgroundScheduler.setMaxBuckets(capabilities.max_scan_buckets);
                            WifiScanningServiceImpl.this.mBackgroundScheduler.setMaxApPerScan(capabilities.max_ap_cache_per_scan);
                            Log.i(WifiScanningServiceImpl.TAG, "wifi driver loaded with scan capabilities: max buckets=" + capabilities.max_scan_buckets);
                            WifiBackgroundScanStateMachine.this.transitionTo(WifiBackgroundScanStateMachine.this.mStartedState);
                            return true;
                        }
                        WifiBackgroundScanStateMachine.this.loge("could not get scan capabilities");
                        return true;
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
                switch (msg.what) {
                    case WifiScanningServiceImpl.CMD_SCAN_RESTARTED /*160009*/:
                        WifiBackgroundScanStateMachine.this.transitionTo(WifiBackgroundScanStateMachine.this.mStartedState);
                        break;
                    default:
                        WifiBackgroundScanStateMachine.this.deferMessage(msg);
                        break;
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
                WifiBackgroundScanStateMachine.this.sendBackgroundScanFailedToAllAndClear(WifiScanningServiceImpl.UNKNOWN_PID, "Scan was interrupted");
                WifiBackgroundScanStateMachine.this.sendHotlistFailedToAllAndClear(WifiScanningServiceImpl.UNKNOWN_PID, "Scan was interrupted");
                WifiScanningServiceImpl.this.mScannerImpl.cleanup();
            }

            public boolean processMessage(Message msg) {
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                switch (msg.what) {
                    case 159746:
                        WifiScanningServiceImpl.this.mWifiMetrics.incrementBackgroundScanCount();
                        Bundle scanParams = msg.obj;
                        if (scanParams != null) {
                            scanParams.setDefusable(true);
                            if (!WifiBackgroundScanStateMachine.this.addBackgroundScanRequest(ci, msg.arg2, (WifiScanner.ScanSettings) scanParams.getParcelable("ScanSettings"), (WorkSource) scanParams.getParcelable("WorkSource"))) {
                                WifiScanningServiceImpl.this.replyFailed(msg, -3, "bad request");
                                break;
                            }
                            WifiScanningServiceImpl.this.replySucceeded(msg);
                            break;
                        }
                        WifiScanningServiceImpl.this.replyFailed(msg, -3, "params null");
                        return true;
                    case 159747:
                        WifiBackgroundScanStateMachine.this.removeBackgroundScanRequest(ci, msg.arg2);
                        break;
                    case 159748:
                        WifiBackgroundScanStateMachine.this.reportScanResults(WifiScanningServiceImpl.this.mScannerImpl.getLatestBatchedScanResults(true));
                        WifiScanningServiceImpl.this.replySucceeded(msg);
                        break;
                    case 159750:
                        WifiBackgroundScanStateMachine.this.addHotlist(ci, msg.arg2, (HotlistSettings) msg.obj);
                        WifiScanningServiceImpl.this.replySucceeded(msg);
                        break;
                    case 159751:
                        WifiBackgroundScanStateMachine.this.removeHotlist(ci, msg.arg2);
                        break;
                    case WifiScanningServiceImpl.CMD_SCAN_RESULTS_AVAILABLE /*160000*/:
                        WifiBackgroundScanStateMachine.this.reportScanResults(WifiScanningServiceImpl.this.mScannerImpl.getLatestBatchedScanResults(true));
                        break;
                    case WifiScanningServiceImpl.CMD_FULL_SCAN_RESULTS /*160001*/:
                        WifiBackgroundScanStateMachine.this.reportFullScanResult((ScanResult) msg.obj, msg.arg2);
                        break;
                    case WifiScanningServiceImpl.CMD_HOTLIST_AP_FOUND /*160002*/:
                        WifiBackgroundScanStateMachine.this.reportHotlistResults(159753, (ScanResult[]) msg.obj);
                        break;
                    case WifiScanningServiceImpl.CMD_HOTLIST_AP_LOST /*160003*/:
                        WifiBackgroundScanStateMachine.this.reportHotlistResults(159754, (ScanResult[]) msg.obj);
                        break;
                    case WifiScanningServiceImpl.CMD_DRIVER_LOADED /*160006*/:
                        return WifiScanningServiceImpl.DBG;
                    case WifiScanningServiceImpl.CMD_DRIVER_UNLOADED /*160007*/:
                        return WifiScanningServiceImpl.DBG;
                    case WifiScanningServiceImpl.CMD_SCAN_PAUSED /*160008*/:
                        WifiBackgroundScanStateMachine.this.reportScanResults((ScanData[]) msg.obj);
                        WifiBackgroundScanStateMachine.this.transitionTo(WifiBackgroundScanStateMachine.this.mPausedState);
                        break;
                    case WifiScanningServiceImpl.CMD_SCAN_FAILED /*160010*/:
                        Log.e(WifiScanningServiceImpl.TAG, "WifiScanner background scan gave CMD_SCAN_FAILED");
                        WifiBackgroundScanStateMachine.this.sendBackgroundScanFailedToAllAndClear(WifiScanningServiceImpl.UNKNOWN_PID, "Background Scan failed");
                        break;
                    default:
                        return WifiScanningServiceImpl.DBG;
                }
                return true;
            }
        }

        WifiBackgroundScanStateMachine(Looper looper) {
            super("WifiBackgroundScanStateMachine", looper);
            this.mDefaultState = new DefaultState();
            this.mStartedState = new StartedState();
            this.mPausedState = new PausedState();
            this.mActiveBackgroundScans = new RequestList(null);
            this.mActiveHotlistSettings = new RequestList(null);
            setLogRecSize(512);
            setLogOnlyTransitions(WifiScanningServiceImpl.DBG);
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

        public void removeHotlistSettings(ClientInfo ci) {
            this.mActiveHotlistSettings.removeAllForClient(ci);
            resetHotlist();
        }

        public void onScanStatus(int event) {
            switch (event) {
                case ApConfigUtil.SUCCESS /*0*/:
                case Extension.TYPE_DOUBLE /*1*/:
                case Extension.TYPE_FLOAT /*2*/:
                    sendMessage(WifiScanningServiceImpl.CMD_SCAN_RESULTS_AVAILABLE);
                case Extension.TYPE_INT64 /*3*/:
                    sendMessage(WifiScanningServiceImpl.CMD_SCAN_FAILED);
                default:
                    Log.e(WifiScanningServiceImpl.TAG, "Unknown scan status event: " + event);
            }
        }

        public void onFullScanResult(ScanResult fullScanResult, int bucketsScanned) {
            sendMessage(WifiScanningServiceImpl.CMD_FULL_SCAN_RESULTS, 0, bucketsScanned, fullScanResult);
        }

        public void onScanPaused(ScanData[] scanData) {
            sendMessage(WifiScanningServiceImpl.CMD_SCAN_PAUSED, scanData);
        }

        public void onScanRestarted() {
            sendMessage(WifiScanningServiceImpl.CMD_SCAN_RESTARTED);
        }

        public void onHotlistApFound(ScanResult[] results) {
            sendMessage(WifiScanningServiceImpl.CMD_HOTLIST_AP_FOUND, 0, 0, results);
        }

        public void onHotlistApLost(ScanResult[] results) {
            sendMessage(WifiScanningServiceImpl.CMD_HOTLIST_AP_LOST, 0, 0, results);
        }

        private boolean addBackgroundScanRequest(ClientInfo ci, int handler, WifiScanner.ScanSettings settings, WorkSource workSource) {
            if (ci == null) {
                Log.d(WifiScanningServiceImpl.TAG, "Failing scan request ClientInfo not found " + handler);
                return WifiScanningServiceImpl.DBG;
            } else if (settings.periodInMs < 1000) {
                loge("Failing scan request because periodInMs is " + settings.periodInMs + ", min scan period is: " + 1000);
                return WifiScanningServiceImpl.DBG;
            } else if (settings.band == 0 && settings.channels == null) {
                loge("Channels was null with unspecified band");
                return WifiScanningServiceImpl.DBG;
            } else if (settings.band == 0 && settings.channels.length == 0) {
                loge("No channels specified");
                return WifiScanningServiceImpl.DBG;
            } else {
                int minSupportedPeriodMs = WifiScanningServiceImpl.this.mChannelHelper.estimateScanDuration(settings);
                if (settings.periodInMs < minSupportedPeriodMs) {
                    loge("Failing scan request because minSupportedPeriodMs is " + minSupportedPeriodMs + " but the request wants " + settings.periodInMs);
                    return WifiScanningServiceImpl.DBG;
                }
                if (!(settings.maxPeriodInMs == 0 || settings.maxPeriodInMs == settings.periodInMs)) {
                    if (settings.maxPeriodInMs < settings.periodInMs) {
                        loge("Failing scan request because maxPeriodInMs is " + settings.maxPeriodInMs + " but less than periodInMs " + settings.periodInMs);
                        return WifiScanningServiceImpl.DBG;
                    } else if (settings.maxPeriodInMs > 1024000) {
                        loge("Failing scan request because maxSupportedPeriodMs is 1024000 but the request wants " + settings.maxPeriodInMs);
                        return WifiScanningServiceImpl.DBG;
                    } else if (settings.stepCount < 1) {
                        loge("Failing scan request because stepCount is " + settings.stepCount + " which is less than 1");
                        return WifiScanningServiceImpl.DBG;
                    }
                }
                WifiScanningServiceImpl.this.logScanRequest("addBackgroundScanRequest", ci, handler, null, settings, null);
                this.mActiveBackgroundScans.addRequest(ci, handler, workSource, settings);
                if (updateSchedule()) {
                    return true;
                }
                this.mActiveBackgroundScans.removeRequest(ci, handler);
                WifiScanningServiceImpl.this.localLog("Failing scan request because failed to reset scan");
                return WifiScanningServiceImpl.DBG;
            }
        }

        private boolean updateSchedule() {
            if (WifiScanningServiceImpl.this.mChannelHelper == null || WifiScanningServiceImpl.this.mBackgroundScheduler == null || WifiScanningServiceImpl.this.mScannerImpl == null) {
                loge("Failed to update schedule because WifiScanningService is not initialized");
                return WifiScanningServiceImpl.DBG;
            }
            WifiScanningServiceImpl.this.mChannelHelper.updateChannels();
            WifiScanningServiceImpl.this.mBackgroundScheduler.updateSchedule(this.mActiveBackgroundScans.getAllSettings());
            ScanSettings schedule = WifiScanningServiceImpl.this.mBackgroundScheduler.getSchedule();
            if (ScanScheduleUtil.scheduleEquals(WifiScanningServiceImpl.this.mPreviousSchedule, schedule)) {
                return true;
            }
            WifiScanningServiceImpl.this.mPreviousSchedule = schedule;
            if (schedule.num_buckets == 0) {
                WifiScanningServiceImpl.this.mScannerImpl.stopBatchedScan();
                return true;
            }
            int b;
            Log.d(WifiScanningServiceImpl.TAG, "starting scan: base period=" + schedule.base_period_ms + ", max ap per scan=" + schedule.max_ap_per_scan + ", batched scans=" + schedule.report_threshold_num_scans);
            for (b = 0; b < schedule.num_buckets; b++) {
                BucketSettings bucket = schedule.buckets[b];
                Log.d(WifiScanningServiceImpl.TAG, "bucket " + bucket.bucket + " (" + bucket.period_ms + "ms)" + "[" + bucket.report_events + "]: " + ChannelHelper.toString(bucket));
            }
            if (WifiScanningServiceImpl.this.mScannerImpl.startBatchedScan(schedule, this)) {
                return true;
            }
            WifiScanningServiceImpl.this.mPreviousSchedule = null;
            loge("error starting scan: base period=" + schedule.base_period_ms + ", max ap per scan=" + schedule.max_ap_per_scan + ", batched scans=" + schedule.report_threshold_num_scans);
            for (b = 0; b < schedule.num_buckets; b++) {
                bucket = schedule.buckets[b];
                loge("bucket " + bucket.bucket + " (" + bucket.period_ms + "ms)" + "[" + bucket.report_events + "]: " + ChannelHelper.toString(bucket));
            }
            return WifiScanningServiceImpl.DBG;
        }

        private void removeBackgroundScanRequest(ClientInfo ci, int handler) {
            if (ci != null) {
                WifiScanningServiceImpl.this.logScanRequest("removeBackgroundScanRequest", ci, handler, null, (WifiScanner.ScanSettings) this.mActiveBackgroundScans.removeRequest(ci, handler), null);
                updateSchedule();
            }
        }

        private void reportFullScanResult(ScanResult result, int bucketsScanned) {
            for (RequestInfo<WifiScanner.ScanSettings> entry : this.mActiveBackgroundScans) {
                ClientInfo ci = entry.clientInfo;
                int handler = entry.handlerId;
                if (WifiScanningServiceImpl.this.mBackgroundScheduler.shouldReportFullScanResultForSettings(result, bucketsScanned, entry.settings)) {
                    ScanResult newResult = new ScanResult(result);
                    if (result.informationElements != null) {
                        newResult.informationElements = (InformationElement[]) result.informationElements.clone();
                    } else {
                        newResult.informationElements = null;
                    }
                    ci.reportEvent(159764, 0, handler, newResult);
                }
            }
        }

        private void reportScanResults(ScanData[] results) {
            for (ScanData result : results) {
                if (!(result == null || result.getResults() == null)) {
                    if (result.getResults().length > 0) {
                        WifiScanningServiceImpl.this.mWifiMetrics.incrementNonEmptyScanResultCount();
                    } else {
                        WifiScanningServiceImpl.this.mWifiMetrics.incrementEmptyScanResultCount();
                    }
                }
            }
            for (RequestInfo<WifiScanner.ScanSettings> entry : this.mActiveBackgroundScans) {
                ClientInfo ci = entry.clientInfo;
                int handler = entry.handlerId;
                ScanData[] resultsToDeliver = WifiScanningServiceImpl.this.mBackgroundScheduler.filterResultsForSettings(results, entry.settings);
                if (resultsToDeliver != null) {
                    WifiScanningServiceImpl.this.logCallback("backgroundScanResults", ci, handler, WifiScanningServiceImpl.describeForLog(resultsToDeliver));
                    ci.reportEvent(159749, 0, handler, new ParcelableScanData(resultsToDeliver));
                }
            }
        }

        private void sendBackgroundScanFailedToAllAndClear(int reason, String description) {
            for (RequestInfo<WifiScanner.ScanSettings> entry : this.mActiveBackgroundScans) {
                entry.clientInfo.reportEvent(159762, 0, entry.handlerId, new OperationResult(reason, description));
            }
            this.mActiveBackgroundScans.clear();
        }

        private void addHotlist(ClientInfo ci, int handler, HotlistSettings settings) {
            this.mActiveHotlistSettings.addRequest(ci, handler, null, settings);
            resetHotlist();
        }

        private void removeHotlist(ClientInfo ci, int handler) {
            this.mActiveHotlistSettings.removeRequest(ci, handler);
            resetHotlist();
        }

        private void resetHotlist() {
            if (WifiScanningServiceImpl.this.mScannerImpl == null) {
                loge("Failed to update hotlist because WifiScanningService is not initialized");
                return;
            }
            Collection<HotlistSettings> settings = this.mActiveHotlistSettings.getAllSettings();
            int num_hotlist_ap = 0;
            for (HotlistSettings s : settings) {
                num_hotlist_ap += s.bssidInfos.length;
            }
            if (num_hotlist_ap == 0) {
                WifiScanningServiceImpl.this.mScannerImpl.resetHotlist();
            } else {
                BssidInfo[] bssidInfos = new BssidInfo[num_hotlist_ap];
                int apLostThreshold = Integer.MAX_VALUE;
                int index = 0;
                for (HotlistSettings s2 : settings) {
                    int i = 0;
                    while (i < s2.bssidInfos.length) {
                        bssidInfos[index] = s2.bssidInfos[i];
                        i++;
                        index++;
                    }
                    if (s2.apLostThreshold < apLostThreshold) {
                        apLostThreshold = s2.apLostThreshold;
                    }
                }
                HotlistSettings mergedSettings = new HotlistSettings();
                mergedSettings.bssidInfos = bssidInfos;
                mergedSettings.apLostThreshold = apLostThreshold;
                WifiScanningServiceImpl.this.mScannerImpl.setHotlist(mergedSettings, this);
            }
        }

        private void reportHotlistResults(int what, ScanResult[] results) {
            for (RequestInfo<HotlistSettings> entry : this.mActiveHotlistSettings) {
                ClientInfo ci = entry.clientInfo;
                int handler = entry.handlerId;
                HotlistSettings settings = entry.settings;
                int num_results = 0;
                for (ScanResult result : results) {
                    for (BssidInfo BssidInfo : settings.bssidInfos) {
                        if (result.BSSID.equalsIgnoreCase(BssidInfo.bssid)) {
                            num_results++;
                            break;
                        }
                    }
                }
                if (num_results != 0) {
                    ScanResult[] results2 = new ScanResult[num_results];
                    int index = 0;
                    for (ScanResult result2 : results) {
                        for (BssidInfo BssidInfo2 : settings.bssidInfos) {
                            if (result2.BSSID.equalsIgnoreCase(BssidInfo2.bssid)) {
                                results2[index] = result2;
                                index++;
                            }
                        }
                    }
                    ci.reportEvent(what, 0, handler, new ParcelableScanResults(results2));
                } else {
                    return;
                }
            }
        }

        private void sendHotlistFailedToAllAndClear(int reason, String description) {
            for (RequestInfo<HotlistSettings> entry : this.mActiveHotlistSettings) {
                entry.clientInfo.reportEvent(159762, 0, entry.handlerId, new OperationResult(reason, description));
            }
            this.mActiveHotlistSettings.clear();
        }
    }

    class WifiChangeStateMachine extends StateMachine implements SignificantWifiChangeEventHandler {
        private static final String ACTION_TIMEOUT = "com.android.server.WifiScanningServiceImpl.action.TIMEOUT";
        private static final int MAX_APS_TO_TRACK = 3;
        private static final int MOVING_SCAN_PERIOD_MS = 10000;
        private static final int MOVING_STATE_TIMEOUT_MS = 30000;
        private static final int STATIONARY_SCAN_PERIOD_MS = 5000;
        private final Set<Pair<ClientInfo, Integer>> mActiveWifiChangeHandlers;
        private ScanResult[] mCurrentBssids;
        State mDefaultState;
        private InternalClientInfo mInternalClientInfo;
        State mMovingState;
        State mStationaryState;
        private PendingIntent mTimeoutIntent;

        class DefaultState extends State {
            DefaultState() {
            }

            public void enter() {
            }

            public boolean processMessage(Message msg) {
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                switch (msg.what) {
                    case 159749:
                    case 159756:
                        break;
                    case 159755:
                        WifiChangeStateMachine.this.addWifiChangeHandler(ci, msg.arg2);
                        WifiScanningServiceImpl.this.replySucceeded(msg);
                        WifiChangeStateMachine.this.transitionTo(WifiChangeStateMachine.this.mMovingState);
                        break;
                    case 159757:
                        WifiChangeStateMachine.this.deferMessage(msg);
                        break;
                    default:
                        return WifiScanningServiceImpl.DBG;
                }
                return true;
            }
        }

        class MovingState extends State {
            boolean mScanResultsPending;
            boolean mWifiChangeDetected;

            MovingState() {
                this.mWifiChangeDetected = WifiScanningServiceImpl.DBG;
                this.mScanResultsPending = WifiScanningServiceImpl.DBG;
            }

            public void enter() {
                if (WifiChangeStateMachine.this.mTimeoutIntent == null) {
                    WifiChangeStateMachine.this.mTimeoutIntent = PendingIntent.getBroadcast(WifiScanningServiceImpl.this.mContext, 0, new Intent(WifiChangeStateMachine.ACTION_TIMEOUT, null), 0);
                    WifiScanningServiceImpl.this.mContext.registerReceiver(new BroadcastReceiver() {
                        public void onReceive(Context context, Intent intent) {
                            WifiChangeStateMachine.this.sendMessage(WifiScanningServiceImpl.CMD_WIFI_CHANGE_TIMEOUT);
                        }
                    }, new IntentFilter(WifiChangeStateMachine.ACTION_TIMEOUT));
                }
                issueFullScan();
            }

            public boolean processMessage(Message msg) {
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                switch (msg.what) {
                    case 159749:
                        if (this.mScanResultsPending) {
                            WifiChangeStateMachine.this.reconfigureScan((ScanData[]) msg.obj, WifiChangeStateMachine.STATIONARY_SCAN_PERIOD_MS);
                            this.mWifiChangeDetected = WifiScanningServiceImpl.DBG;
                            WifiScanningServiceImpl.this.mAlarmManager.setExact(2, WifiScanningServiceImpl.this.mClock.elapsedRealtime() + 30000, WifiChangeStateMachine.this.mTimeoutIntent);
                            this.mScanResultsPending = WifiScanningServiceImpl.DBG;
                            break;
                        }
                        break;
                    case 159755:
                        WifiChangeStateMachine.this.addWifiChangeHandler(ci, msg.arg2);
                        WifiScanningServiceImpl.this.replySucceeded(msg);
                        break;
                    case 159756:
                        WifiChangeStateMachine.this.removeWifiChangeHandler(ci, msg.arg2);
                        break;
                    case 159757:
                        WifiChangeSettings settings = msg.obj;
                        WifiChangeStateMachine.this.reconfigureScan(settings);
                        this.mWifiChangeDetected = WifiScanningServiceImpl.DBG;
                        long unchangedDelay = (long) (settings.unchangedSampleSize * settings.periodInMs);
                        WifiScanningServiceImpl.this.mAlarmManager.cancel(WifiChangeStateMachine.this.mTimeoutIntent);
                        WifiScanningServiceImpl.this.mAlarmManager.setExact(2, WifiScanningServiceImpl.this.mClock.elapsedRealtime() + unchangedDelay, WifiChangeStateMachine.this.mTimeoutIntent);
                        break;
                    case WifiScanningServiceImpl.CMD_WIFI_CHANGE_DETECTED /*160004*/:
                        WifiScanningServiceImpl.this.mAlarmManager.cancel(WifiChangeStateMachine.this.mTimeoutIntent);
                        WifiChangeStateMachine.this.reportWifiChanged((ScanResult[]) msg.obj);
                        this.mWifiChangeDetected = true;
                        issueFullScan();
                        break;
                    case WifiScanningServiceImpl.CMD_WIFI_CHANGE_TIMEOUT /*160005*/:
                        if (!this.mWifiChangeDetected) {
                            WifiChangeStateMachine.this.transitionTo(WifiChangeStateMachine.this.mStationaryState);
                            break;
                        }
                        break;
                    default:
                        return WifiScanningServiceImpl.DBG;
                }
                return true;
            }

            public void exit() {
                WifiScanningServiceImpl.this.mAlarmManager.cancel(WifiChangeStateMachine.this.mTimeoutIntent);
            }

            void issueFullScan() {
                WifiScanner.ScanSettings settings = new WifiScanner.ScanSettings();
                settings.band = WifiChangeStateMachine.MAX_APS_TO_TRACK;
                settings.periodInMs = WifiChangeStateMachine.MOVING_SCAN_PERIOD_MS;
                settings.reportEvents = 1;
                WifiChangeStateMachine.this.addScanRequest(settings);
                this.mScanResultsPending = true;
            }
        }

        class StationaryState extends State {
            StationaryState() {
            }

            public void enter() {
                WifiChangeStateMachine.this.reportWifiStabilized(WifiChangeStateMachine.this.mCurrentBssids);
            }

            public boolean processMessage(Message msg) {
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                switch (msg.what) {
                    case 159749:
                        break;
                    case 159755:
                        WifiChangeStateMachine.this.addWifiChangeHandler(ci, msg.arg2);
                        WifiScanningServiceImpl.this.replySucceeded(msg);
                        break;
                    case 159756:
                        WifiChangeStateMachine.this.removeWifiChangeHandler(ci, msg.arg2);
                        break;
                    case 159757:
                        WifiChangeStateMachine.this.deferMessage(msg);
                        break;
                    case WifiScanningServiceImpl.CMD_WIFI_CHANGE_DETECTED /*160004*/:
                        WifiChangeStateMachine.this.reportWifiChanged((ScanResult[]) msg.obj);
                        WifiChangeStateMachine.this.transitionTo(WifiChangeStateMachine.this.mMovingState);
                        break;
                    default:
                        return WifiScanningServiceImpl.DBG;
                }
                return true;
            }
        }

        WifiChangeStateMachine(Looper looper) {
            super("SignificantChangeStateMachine", looper);
            this.mDefaultState = new DefaultState();
            this.mStationaryState = new StationaryState();
            this.mMovingState = new MovingState();
            this.mActiveWifiChangeHandlers = new HashSet();
            addState(this.mDefaultState);
            addState(this.mStationaryState, this.mDefaultState);
            addState(this.mMovingState, this.mDefaultState);
            setInitialState(this.mDefaultState);
        }

        public void removeWifiChangeHandler(ClientInfo ci) {
            Iterator<Pair<ClientInfo, Integer>> iter = this.mActiveWifiChangeHandlers.iterator();
            while (iter.hasNext()) {
                if (((Pair) iter.next()).first == ci) {
                    iter.remove();
                }
            }
            untrackSignificantWifiChangeOnEmpty();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void reconfigureScan(ScanData[] results, int period) {
            int length = results.length;
            if (r0 < MAX_APS_TO_TRACK) {
                WifiScanningServiceImpl.this.localLog("too few APs (" + results.length + ") available to track wifi change");
                return;
            }
            removeScanRequest();
            HashMap<String, ScanResult> bssidToScanResult = new HashMap();
            for (ScanResult result : results[0].getResults()) {
                ScanResult saved = (ScanResult) bssidToScanResult.get(result.BSSID);
                if (saved == null) {
                    bssidToScanResult.put(result.BSSID, result);
                } else {
                    if (saved.level > result.level) {
                        bssidToScanResult.put(result.BSSID, result);
                    }
                }
            }
            ScanResult[] brightest = new ScanResult[MAX_APS_TO_TRACK];
            for (ScanResult result2 : bssidToScanResult.values()) {
                int j = 0;
                while (true) {
                    length = brightest.length;
                    if (j >= r0) {
                        break;
                    } else if (brightest[j] == null || brightest[j].level < result2.level) {
                    } else {
                        j++;
                    }
                }
                for (int k = brightest.length; k > j + 1; k += WifiScanningServiceImpl.UNKNOWN_PID) {
                    brightest[k + WifiScanningServiceImpl.UNKNOWN_PID] = brightest[k - 2];
                }
                brightest[j] = result2;
            }
            ArrayList<Integer> channels = new ArrayList();
            int i = 0;
            while (true) {
                length = brightest.length;
                if (i >= r0) {
                    break;
                }
                boolean found = WifiScanningServiceImpl.DBG;
                j = i + 1;
                while (true) {
                    length = brightest.length;
                    if (j >= r0) {
                        break;
                    }
                    if (brightest[j].frequency == brightest[i].frequency) {
                        found = true;
                    }
                    j++;
                }
                if (!found) {
                    channels.add(Integer.valueOf(brightest[i].frequency));
                }
                i++;
            }
            WifiScanner.ScanSettings settings = new WifiScanner.ScanSettings();
            settings.band = 0;
            settings.channels = new ChannelSpec[channels.size()];
            for (i = 0; i < channels.size(); i++) {
                settings.channels[i] = new ChannelSpec(((Integer) channels.get(i)).intValue());
            }
            settings.periodInMs = period;
            addScanRequest(settings);
            WifiChangeSettings settings2 = new WifiChangeSettings();
            settings2.rssiSampleSize = MAX_APS_TO_TRACK;
            settings2.lostApSampleSize = MAX_APS_TO_TRACK;
            settings2.unchangedSampleSize = MAX_APS_TO_TRACK;
            settings2.minApsBreachingThreshold = 2;
            settings2.bssidInfos = new BssidInfo[brightest.length];
            i = 0;
            while (true) {
                length = brightest.length;
                if (i < r0) {
                    BssidInfo BssidInfo = new BssidInfo();
                    BssidInfo.bssid = brightest[i].BSSID;
                    int threshold = ((brightest[i].level + 100) / 32) + 2;
                    BssidInfo.low = brightest[i].level - threshold;
                    BssidInfo.high = brightest[i].level + threshold;
                    settings2.bssidInfos[i] = BssidInfo;
                    i++;
                } else {
                    trackSignificantWifiChange(settings2);
                    this.mCurrentBssids = brightest;
                    return;
                }
            }
        }

        private void reconfigureScan(WifiChangeSettings settings) {
            if (settings.bssidInfos.length < MAX_APS_TO_TRACK) {
                WifiScanningServiceImpl.this.localLog("too few APs (" + settings.bssidInfos.length + ") available to track wifi change");
                return;
            }
            int i;
            this.mCurrentBssids = new ScanResult[settings.bssidInfos.length];
            HashSet<Integer> channels = new HashSet();
            for (i = 0; i < settings.bssidInfos.length; i++) {
                ScanResult result = new ScanResult();
                result.BSSID = settings.bssidInfos[i].bssid;
                this.mCurrentBssids[i] = result;
                channels.add(Integer.valueOf(settings.bssidInfos[i].frequencyHint));
            }
            removeScanRequest();
            WifiScanner.ScanSettings settings2 = new WifiScanner.ScanSettings();
            settings2.band = 0;
            settings2.channels = new ChannelSpec[channels.size()];
            i = 0;
            for (Integer channel : channels) {
                int i2 = i + 1;
                settings2.channels[i] = new ChannelSpec(channel.intValue());
                i = i2;
            }
            settings2.periodInMs = settings.periodInMs;
            addScanRequest(settings2);
            trackSignificantWifiChange(settings);
        }

        public void onChangesFound(ScanResult[] results) {
            sendMessage(WifiScanningServiceImpl.CMD_WIFI_CHANGE_DETECTED, 0, 0, results);
        }

        private void addScanRequest(WifiScanner.ScanSettings settings) {
            if (this.mInternalClientInfo != null) {
                this.mInternalClientInfo.sendRequestToClientHandler(159746, settings, null);
            }
        }

        private void removeScanRequest() {
            if (this.mInternalClientInfo != null) {
                this.mInternalClientInfo.sendRequestToClientHandler(159747);
            }
        }

        private void trackSignificantWifiChange(WifiChangeSettings settings) {
            if (WifiScanningServiceImpl.this.mScannerImpl != null) {
                WifiScanningServiceImpl.this.mScannerImpl.untrackSignificantWifiChange();
                WifiScanningServiceImpl.this.mScannerImpl.trackSignificantWifiChange(settings, this);
            }
        }

        private void untrackSignificantWifiChange() {
            if (WifiScanningServiceImpl.this.mScannerImpl != null) {
                WifiScanningServiceImpl.this.mScannerImpl.untrackSignificantWifiChange();
            }
        }

        private void addWifiChangeHandler(ClientInfo ci, int handler) {
            this.mActiveWifiChangeHandlers.add(Pair.create(ci, Integer.valueOf(handler)));
            if (this.mInternalClientInfo == null) {
                this.mInternalClientInfo = new InternalClientInfo(ci.getUid(), new Messenger(getHandler()));
                this.mInternalClientInfo.register();
            }
        }

        private void removeWifiChangeHandler(ClientInfo ci, int handler) {
            this.mActiveWifiChangeHandlers.remove(Pair.create(ci, Integer.valueOf(handler)));
            untrackSignificantWifiChangeOnEmpty();
        }

        private void untrackSignificantWifiChangeOnEmpty() {
            if (this.mActiveWifiChangeHandlers.isEmpty()) {
                this.mCurrentBssids = null;
                untrackSignificantWifiChange();
                if (this.mInternalClientInfo != null) {
                    this.mInternalClientInfo.cleanup();
                    this.mInternalClientInfo = null;
                }
                transitionTo(this.mDefaultState);
            }
        }

        private void reportWifiChanged(ScanResult[] results) {
            ParcelableScanResults parcelableScanResults = new ParcelableScanResults(results);
            for (Pair<ClientInfo, Integer> entry : this.mActiveWifiChangeHandlers) {
                entry.first.reportEvent(159759, 0, ((Integer) entry.second).intValue(), parcelableScanResults);
            }
        }

        private void reportWifiStabilized(ScanResult[] results) {
            ParcelableScanResults parcelableScanResults = new ParcelableScanResults(results);
            for (Pair<ClientInfo, Integer> entry : this.mActiveWifiChangeHandlers) {
                entry.first.reportEvent(159760, 0, ((Integer) entry.second).intValue(), parcelableScanResults);
            }
        }
    }

    class WifiPnoScanStateMachine extends StateMachine implements PnoEventHandler {
        private final RequestList<Pair<PnoSettings, WifiScanner.ScanSettings>> mActivePnoScans;
        private final DefaultState mDefaultState;
        private final HwPnoScanState mHwPnoScanState;
        private InternalClientInfo mInternalClientInfo;
        private final SingleScanState mSingleScanState;
        private final StartedState mStartedState;
        private final SwPnoScanState mSwPnoScanState;

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
                        WifiPnoScanStateMachine.this.loge("Unexpected message " + msg.what);
                        break;
                    case 159768:
                    case 159769:
                        WifiScanningServiceImpl.this.replyFailed(msg, WifiScanningServiceImpl.UNKNOWN_PID, "not available");
                        break;
                    case WifiScanningServiceImpl.CMD_DRIVER_LOADED /*160006*/:
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                        break;
                    case WifiScanningServiceImpl.CMD_DRIVER_UNLOADED /*160007*/:
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mDefaultState);
                        break;
                    default:
                        return WifiScanningServiceImpl.DBG;
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
                        Bundle pnoParams = msg.obj;
                        if (pnoParams != null) {
                            pnoParams.setDefusable(true);
                            if (!WifiPnoScanStateMachine.this.addHwPnoScanRequest(ci, msg.arg2, (WifiScanner.ScanSettings) pnoParams.getParcelable("ScanSettings"), (PnoSettings) pnoParams.getParcelable("PnoSettings"))) {
                                WifiScanningServiceImpl.this.replyFailed(msg, -3, "bad request");
                                WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                                break;
                            }
                            WifiScanningServiceImpl.this.replySucceeded(msg);
                            break;
                        }
                        WifiScanningServiceImpl.this.replyFailed(msg, -3, "params null");
                        return true;
                    case 159769:
                        WifiPnoScanStateMachine.this.removeHwPnoScanRequest(ci, msg.arg2);
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                        break;
                    case WifiScanningServiceImpl.CMD_PNO_NETWORK_FOUND /*160011*/:
                        if (!WifiPnoScanStateMachine.this.isSingleScanNeeded(msg.obj)) {
                            WifiPnoScanStateMachine.this.reportPnoNetworkFound((ScanResult[]) msg.obj);
                            break;
                        }
                        WifiScanner.ScanSettings activeScanSettings = WifiPnoScanStateMachine.this.getScanSettings();
                        if (activeScanSettings != null) {
                            WifiPnoScanStateMachine.this.addSingleScanRequest(activeScanSettings);
                            WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mSingleScanState);
                            break;
                        }
                        WifiPnoScanStateMachine.this.sendPnoScanFailedToAllAndClear(WifiScanningServiceImpl.UNKNOWN_PID, "couldn't retrieve setting");
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                        break;
                    case WifiScanningServiceImpl.CMD_PNO_SCAN_FAILED /*160012*/:
                        WifiPnoScanStateMachine.this.sendPnoScanFailedToAllAndClear(WifiScanningServiceImpl.UNKNOWN_PID, "pno scan failed");
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                        break;
                    default:
                        return WifiScanningServiceImpl.DBG;
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
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                switch (msg.what) {
                    case 159749:
                        ScanData[] scanDatas = msg.obj.getResults();
                        WifiPnoScanStateMachine.this.reportPnoNetworkFound(scanDatas[scanDatas.length + WifiScanningServiceImpl.UNKNOWN_PID].getResults());
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mHwPnoScanState);
                        break;
                    case 159762:
                        WifiPnoScanStateMachine.this.sendPnoScanFailedToAllAndClear(WifiScanningServiceImpl.UNKNOWN_PID, "single scan failed");
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                        break;
                    default:
                        return WifiScanningServiceImpl.DBG;
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
                WifiPnoScanStateMachine.this.sendPnoScanFailedToAllAndClear(WifiScanningServiceImpl.UNKNOWN_PID, "Scan was interrupted");
            }

            public boolean processMessage(Message msg) {
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                switch (msg.what) {
                    case 159768:
                        Bundle pnoParams = msg.obj;
                        if (pnoParams != null) {
                            pnoParams.setDefusable(true);
                            PnoSettings pnoSettings = (PnoSettings) pnoParams.getParcelable("PnoSettings");
                            WifiPnoScanStateMachine.this.deferMessage(msg);
                            if (!WifiScanningServiceImpl.this.mScannerImpl.isHwPnoSupported(pnoSettings.isConnected)) {
                                WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mSwPnoScanState);
                                break;
                            }
                            WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mHwPnoScanState);
                            break;
                        }
                        WifiScanningServiceImpl.this.replyFailed(msg, -3, "params null");
                        return true;
                    case 159769:
                        WifiScanningServiceImpl.this.replyFailed(msg, WifiScanningServiceImpl.UNKNOWN_PID, "no scan running");
                        break;
                    default:
                        return WifiScanningServiceImpl.DBG;
                }
                return true;
            }
        }

        class SwPnoScanState extends State {
            private final ArrayList<ScanResult> mSwPnoFullScanResults;

            SwPnoScanState() {
                this.mSwPnoFullScanResults = new ArrayList();
            }

            public void enter() {
                this.mSwPnoFullScanResults.clear();
            }

            public void exit() {
                WifiPnoScanStateMachine.this.removeInternalClient();
            }

            public boolean processMessage(Message msg) {
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                switch (msg.what) {
                    case 159749:
                        WifiPnoScanStateMachine.this.reportPnoNetworkFound((ScanResult[]) this.mSwPnoFullScanResults.toArray(new ScanResult[this.mSwPnoFullScanResults.size()]));
                        this.mSwPnoFullScanResults.clear();
                        break;
                    case 159762:
                        WifiPnoScanStateMachine.this.sendPnoScanFailedToAllAndClear(WifiScanningServiceImpl.UNKNOWN_PID, "background scan failed");
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                        break;
                    case 159764:
                        this.mSwPnoFullScanResults.add((ScanResult) msg.obj);
                        break;
                    case 159768:
                        Bundle pnoParams = msg.obj;
                        if (pnoParams != null) {
                            pnoParams.setDefusable(true);
                            if (!WifiPnoScanStateMachine.this.addSwPnoScanRequest(ci, msg.arg2, (WifiScanner.ScanSettings) pnoParams.getParcelable("ScanSettings"), (PnoSettings) pnoParams.getParcelable("PnoSettings"))) {
                                WifiScanningServiceImpl.this.replyFailed(msg, -3, "bad request");
                                WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                                break;
                            }
                            WifiScanningServiceImpl.this.replySucceeded(msg);
                            break;
                        }
                        WifiScanningServiceImpl.this.replyFailed(msg, -3, "params null");
                        return true;
                    case 159769:
                        WifiPnoScanStateMachine.this.removeSwPnoScanRequest(ci, msg.arg2);
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                        break;
                    default:
                        return WifiScanningServiceImpl.DBG;
                }
                return true;
            }
        }

        WifiPnoScanStateMachine(Looper looper) {
            super("WifiPnoScanStateMachine", looper);
            this.mDefaultState = new DefaultState();
            this.mStartedState = new StartedState();
            this.mHwPnoScanState = new HwPnoScanState();
            this.mSwPnoScanState = new SwPnoScanState();
            this.mSingleScanState = new SingleScanState();
            this.mActivePnoScans = new RequestList(null);
            setLogRecSize(512);
            setLogOnlyTransitions(WifiScanningServiceImpl.DBG);
            addState(this.mDefaultState);
            addState(this.mStartedState, this.mDefaultState);
            addState(this.mHwPnoScanState, this.mStartedState);
            addState(this.mSingleScanState, this.mHwPnoScanState);
            addState(this.mSwPnoScanState, this.mStartedState);
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

        private WifiNative.PnoSettings convertPnoSettingsToNative(PnoSettings pnoSettings) {
            WifiNative.PnoSettings nativePnoSetting = new WifiNative.PnoSettings();
            nativePnoSetting.min5GHzRssi = pnoSettings.min5GHzRssi;
            nativePnoSetting.min24GHzRssi = pnoSettings.min24GHzRssi;
            nativePnoSetting.initialScoreMax = pnoSettings.initialScoreMax;
            nativePnoSetting.currentConnectionBonus = pnoSettings.currentConnectionBonus;
            nativePnoSetting.sameNetworkBonus = pnoSettings.sameNetworkBonus;
            nativePnoSetting.secureBonus = pnoSettings.secureBonus;
            nativePnoSetting.band5GHzBonus = pnoSettings.band5GHzBonus;
            nativePnoSetting.isConnected = pnoSettings.isConnected;
            nativePnoSetting.networkList = new PnoNetwork[pnoSettings.networkList.length];
            for (int i = 0; i < pnoSettings.networkList.length; i++) {
                nativePnoSetting.networkList[i] = new PnoNetwork();
                nativePnoSetting.networkList[i].ssid = pnoSettings.networkList[i].ssid;
                nativePnoSetting.networkList[i].networkId = pnoSettings.networkList[i].networkId;
                nativePnoSetting.networkList[i].priority = pnoSettings.networkList[i].priority;
                nativePnoSetting.networkList[i].flags = pnoSettings.networkList[i].flags;
                nativePnoSetting.networkList[i].auth_bit_field = pnoSettings.networkList[i].authBitField;
            }
            return nativePnoSetting;
        }

        private WifiScanner.ScanSettings getScanSettings() {
            Iterator settingsPair$iterator = this.mActivePnoScans.getAllSettings().iterator();
            if (settingsPair$iterator.hasNext()) {
                return (WifiScanner.ScanSettings) ((Pair) settingsPair$iterator.next()).second;
            }
            return null;
        }

        private void removeInternalClient() {
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

        private void addPnoScanRequest(ClientInfo ci, int handler, WifiScanner.ScanSettings scanSettings, PnoSettings pnoSettings) {
            this.mActivePnoScans.addRequest(ci, handler, WifiStateMachine.WIFI_WORK_SOURCE, Pair.create(pnoSettings, scanSettings));
            addInternalClient(ci);
        }

        private Pair<PnoSettings, WifiScanner.ScanSettings> removePnoScanRequest(ClientInfo ci, int handler) {
            return (Pair) this.mActivePnoScans.removeRequest(ci, handler);
        }

        private boolean addHwPnoScanRequest(ClientInfo ci, int handler, WifiScanner.ScanSettings scanSettings, PnoSettings pnoSettings) {
            if (ci == null) {
                Log.d(WifiScanningServiceImpl.TAG, "Failing scan request ClientInfo not found " + handler);
                return WifiScanningServiceImpl.DBG;
            } else if (this.mActivePnoScans.isEmpty()) {
                if (!WifiScanningServiceImpl.this.mScannerImpl.setHwPnoList(convertPnoSettingsToNative(pnoSettings), WifiScanningServiceImpl.this.mPnoScanStateMachine)) {
                    return WifiScanningServiceImpl.DBG;
                }
                WifiScanningServiceImpl.this.logScanRequest("addHwPnoScanRequest", ci, handler, null, scanSettings, pnoSettings);
                addPnoScanRequest(ci, handler, scanSettings, pnoSettings);
                if (WifiScanningServiceImpl.this.mScannerImpl.shouldScheduleBackgroundScanForHwPno()) {
                    addBackgroundScanRequest(scanSettings);
                }
                return true;
            } else {
                loge("Failing scan request because there is already an active scan");
                return WifiScanningServiceImpl.DBG;
            }
        }

        private void removeHwPnoScanRequest(ClientInfo ci, int handler) {
            if (ci != null) {
                Pair<PnoSettings, WifiScanner.ScanSettings> settings = removePnoScanRequest(ci, handler);
                WifiScanningServiceImpl.this.logScanRequest("removeHwPnoScanRequest", ci, handler, null, (WifiScanner.ScanSettings) settings.second, (PnoSettings) settings.first);
            }
        }

        private boolean addSwPnoScanRequest(ClientInfo ci, int handler, WifiScanner.ScanSettings scanSettings, PnoSettings pnoSettings) {
            if (ci == null) {
                Log.d(WifiScanningServiceImpl.TAG, "Failing scan request ClientInfo not found " + handler);
                return WifiScanningServiceImpl.DBG;
            } else if (this.mActivePnoScans.isEmpty()) {
                WifiScanningServiceImpl.this.logScanRequest("addSwPnoScanRequest", ci, handler, null, scanSettings, pnoSettings);
                addPnoScanRequest(ci, handler, scanSettings, pnoSettings);
                scanSettings.reportEvents = 3;
                addBackgroundScanRequest(scanSettings);
                return true;
            } else {
                loge("Failing scan request because there is already an active scan");
                return WifiScanningServiceImpl.DBG;
            }
        }

        private void removeSwPnoScanRequest(ClientInfo ci, int handler) {
            if (ci != null) {
                Pair<PnoSettings, WifiScanner.ScanSettings> settings = removePnoScanRequest(ci, handler);
                WifiScanningServiceImpl.this.logScanRequest("removeSwPnoScanRequest", ci, handler, null, (WifiScanner.ScanSettings) settings.second, (PnoSettings) settings.first);
            }
        }

        private void reportPnoNetworkFound(ScanResult[] results) {
            ParcelableScanResults parcelableScanResults = new ParcelableScanResults(results);
            for (RequestInfo<Pair<PnoSettings, WifiScanner.ScanSettings>> entry : this.mActivePnoScans) {
                ClientInfo ci = entry.clientInfo;
                int handler = entry.handlerId;
                WifiScanningServiceImpl.this.logCallback("pnoNetworkFound", ci, handler, WifiScanningServiceImpl.describeForLog(results));
                ci.reportEvent(159770, 0, handler, parcelableScanResults);
            }
        }

        private void sendPnoScanFailedToAllAndClear(int reason, String description) {
            for (RequestInfo<Pair<PnoSettings, WifiScanner.ScanSettings>> entry : this.mActivePnoScans) {
                entry.clientInfo.reportEvent(159762, 0, entry.handlerId, new OperationResult(reason, description));
            }
            this.mActivePnoScans.clear();
        }

        private void addBackgroundScanRequest(WifiScanner.ScanSettings settings) {
            if (this.mInternalClientInfo != null) {
                this.mInternalClientInfo.sendRequestToClientHandler(159746, settings, WifiStateMachine.WIFI_WORK_SOURCE);
            }
        }

        private void addSingleScanRequest(WifiScanner.ScanSettings settings) {
            if (this.mInternalClientInfo != null) {
                this.mInternalClientInfo.sendRequestToClientHandler(159765, settings, WifiStateMachine.WIFI_WORK_SOURCE);
            }
        }

        private boolean isSingleScanNeeded(ScanResult[] scanResults) {
            for (ScanResult scanResult : scanResults) {
                if (scanResult.informationElements != null && scanResult.informationElements.length > 0) {
                    return WifiScanningServiceImpl.DBG;
                }
            }
            return true;
        }
    }

    class WifiSingleScanStateMachine extends StateMachine implements ScanEventHandler {
        private RequestList<WifiScanner.ScanSettings> mActiveScans;
        private final DefaultState mDefaultState;
        private final DriverStartedState mDriverStartedState;
        private final IdleState mIdleState;
        private RequestList<WifiScanner.ScanSettings> mPendingScans;
        private final ScanningState mScanningState;

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
                        WifiScanningServiceImpl.this.replyFailed(msg, WifiScanningServiceImpl.UNKNOWN_PID, "not available");
                        return true;
                    case WifiScanningServiceImpl.CMD_SCAN_RESULTS_AVAILABLE /*160000*/:
                        return true;
                    case WifiScanningServiceImpl.CMD_FULL_SCAN_RESULTS /*160001*/:
                        return true;
                    case WifiScanningServiceImpl.CMD_DRIVER_LOADED /*160006*/:
                        WifiSingleScanStateMachine.this.transitionTo(WifiSingleScanStateMachine.this.mIdleState);
                        return true;
                    case WifiScanningServiceImpl.CMD_DRIVER_UNLOADED /*160007*/:
                        WifiSingleScanStateMachine.this.transitionTo(WifiSingleScanStateMachine.this.mDefaultState);
                        return true;
                    default:
                        return WifiScanningServiceImpl.DBG;
                }
            }
        }

        class DriverStartedState extends State {
            DriverStartedState() {
            }

            public void exit() {
                WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(2, WifiSingleScanStateMachine.this.mPendingScans.size());
                WifiSingleScanStateMachine.this.sendOpFailedToAllAndClear(WifiSingleScanStateMachine.this.mPendingScans, WifiScanningServiceImpl.UNKNOWN_PID, "Scan was interrupted");
            }

            public boolean processMessage(Message msg) {
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                switch (msg.what) {
                    case 159765:
                        WifiScanningServiceImpl.this.mWifiMetrics.incrementOneshotScanCount();
                        int handler = msg.arg2;
                        Bundle scanParams = msg.obj;
                        if (scanParams == null) {
                            if (ci != null) {
                                WifiScanningServiceImpl.this.logCallback("singleScanInvalidRequest", ci, handler, "null params");
                            }
                            WifiScanningServiceImpl.this.replyFailed(msg, -3, "params null");
                            return true;
                        }
                        scanParams.setDefusable(true);
                        if (WifiSingleScanStateMachine.this.validateAndAddToScanQueue(ci, handler, (WifiScanner.ScanSettings) scanParams.getParcelable("ScanSettings"), (WorkSource) scanParams.getParcelable("WorkSource"))) {
                            WifiScanningServiceImpl.this.replySucceeded(msg);
                            if (WifiSingleScanStateMachine.this.getCurrentState() != WifiSingleScanStateMachine.this.mScanningState) {
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
                        return WifiScanningServiceImpl.DBG;
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
                return WifiScanningServiceImpl.DBG;
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
                try {
                    WifiScanningServiceImpl.this.mBatteryStats.noteWifiScanStoppedFromSource(this.mScanWorkSource);
                } catch (RemoteException e) {
                    WifiSingleScanStateMachine.this.loge(e.toString());
                }
                WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(0, WifiSingleScanStateMachine.this.mActiveScans.size());
                WifiSingleScanStateMachine.this.sendOpFailedToAllAndClear(WifiSingleScanStateMachine.this.mActiveScans, WifiScanningServiceImpl.UNKNOWN_PID, "Scan was interrupted");
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case WifiScanningServiceImpl.CMD_SCAN_RESULTS_AVAILABLE /*160000*/:
                        WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(1, WifiSingleScanStateMachine.this.mActiveScans.size());
                        WifiSingleScanStateMachine.this.reportScanResults(WifiScanningServiceImpl.this.mScannerImpl.getLatestSingleScanResults());
                        WifiSingleScanStateMachine.this.mActiveScans.clear();
                        WifiSingleScanStateMachine.this.transitionTo(WifiSingleScanStateMachine.this.mIdleState);
                        return true;
                    case WifiScanningServiceImpl.CMD_FULL_SCAN_RESULTS /*160001*/:
                        WifiSingleScanStateMachine.this.reportFullScanResult((ScanResult) msg.obj, msg.arg2);
                        return true;
                    case WifiScanningServiceImpl.CMD_SCAN_FAILED /*160010*/:
                        WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(0, WifiSingleScanStateMachine.this.mActiveScans.size());
                        WifiSingleScanStateMachine.this.sendOpFailedToAllAndClear(WifiSingleScanStateMachine.this.mActiveScans, WifiScanningServiceImpl.UNKNOWN_PID, "Scan failed");
                        WifiSingleScanStateMachine.this.transitionTo(WifiSingleScanStateMachine.this.mIdleState);
                        return true;
                    default:
                        return WifiScanningServiceImpl.DBG;
                }
            }
        }

        WifiSingleScanStateMachine(Looper looper) {
            super("WifiSingleScanStateMachine", looper);
            this.mDefaultState = new DefaultState();
            this.mDriverStartedState = new DriverStartedState();
            this.mIdleState = new IdleState();
            this.mScanningState = new ScanningState();
            this.mActiveScans = new RequestList(null);
            this.mPendingScans = new RequestList(null);
            setLogRecSize(CivicLocationElement.SCRIPT);
            setLogOnlyTransitions(WifiScanningServiceImpl.DBG);
            addState(this.mDefaultState);
            addState(this.mDriverStartedState, this.mDefaultState);
            addState(this.mIdleState, this.mDriverStartedState);
            addState(this.mScanningState, this.mDriverStartedState);
            setInitialState(this.mDefaultState);
        }

        public void onScanStatus(int event) {
            switch (event) {
                case ApConfigUtil.SUCCESS /*0*/:
                case Extension.TYPE_DOUBLE /*1*/:
                case Extension.TYPE_FLOAT /*2*/:
                    sendMessage(WifiScanningServiceImpl.CMD_SCAN_RESULTS_AVAILABLE);
                case Extension.TYPE_INT64 /*3*/:
                    sendMessage(WifiScanningServiceImpl.CMD_SCAN_FAILED);
                default:
                    Log.e(WifiScanningServiceImpl.TAG, "Unknown scan status event: " + event);
            }
        }

        public void onFullScanResult(ScanResult fullScanResult, int bucketsScanned) {
            sendMessage(WifiScanningServiceImpl.CMD_FULL_SCAN_RESULTS, 0, bucketsScanned, fullScanResult);
        }

        public void onScanPaused(ScanData[] scanData) {
            Log.e(WifiScanningServiceImpl.TAG, "Got scan paused for single scan");
        }

        public void onScanRestarted() {
            Log.e(WifiScanningServiceImpl.TAG, "Got scan restarted for single scan");
        }

        boolean validateAndAddToScanQueue(ClientInfo ci, int handler, WifiScanner.ScanSettings settings, WorkSource workSource) {
            if (ci == null) {
                Log.d(WifiScanningServiceImpl.TAG, "Failing single scan request ClientInfo not found " + handler);
                return WifiScanningServiceImpl.DBG;
            } else if (settings.band == 0 && (settings.channels == null || settings.channels.length == 0)) {
                Log.d(WifiScanningServiceImpl.TAG, "Failing single scan because channel list was empty");
                return WifiScanningServiceImpl.DBG;
            } else {
                WifiScanningServiceImpl.this.logScanRequest("addSingleScanRequest", ci, handler, workSource, settings, null);
                this.mPendingScans.addRequest(ci, handler, workSource, settings);
                return true;
            }
        }

        void removeSingleScanRequest(ClientInfo ci, int handler) {
            if (ci != null) {
                WifiScanningServiceImpl.this.logScanRequest("removeSingleScanRequest", ci, handler, null, null, null);
                this.mPendingScans.removeRequest(ci, handler);
                this.mActiveScans.removeRequest(ci, handler);
            }
        }

        void removeSingleScanRequests(ClientInfo ci) {
            if (ci != null) {
                WifiScanningServiceImpl.this.logScanRequest("removeSingleScanRequests", ci, WifiScanningServiceImpl.UNKNOWN_PID, null, null, null);
                this.mPendingScans.removeAllForClient(ci);
                this.mActiveScans.removeAllForClient(ci);
            }
        }

        void tryToStartNewScan() {
            if (this.mPendingScans.size() != 0) {
                WifiScanningServiceImpl.this.mChannelHelper.updateChannels();
                ScanSettings settings = new ScanSettings();
                settings.num_buckets = 1;
                BucketSettings bucketSettings = new BucketSettings();
                bucketSettings.bucket = 0;
                bucketSettings.period_ms = 0;
                bucketSettings.report_events = 1;
                ChannelCollection channels = WifiScanningServiceImpl.this.mChannelHelper.createChannelCollection();
                HashSet<Integer> hiddenNetworkIdSet = new HashSet();
                for (RequestInfo<WifiScanner.ScanSettings> entry : this.mPendingScans) {
                    channels.addChannels((WifiScanner.ScanSettings) entry.settings);
                    if (((WifiScanner.ScanSettings) entry.settings).hiddenNetworkIds != null) {
                        for (int valueOf : ((WifiScanner.ScanSettings) entry.settings).hiddenNetworkIds) {
                            hiddenNetworkIdSet.add(Integer.valueOf(valueOf));
                        }
                    }
                    if ((((WifiScanner.ScanSettings) entry.settings).reportEvents & 2) != 0) {
                        bucketSettings.report_events |= 2;
                    }
                }
                if (hiddenNetworkIdSet.size() > 0) {
                    settings.hiddenNetworkIds = new int[hiddenNetworkIdSet.size()];
                    int numHiddenNetworks = 0;
                    for (Integer hiddenNetworkId : hiddenNetworkIdSet) {
                        int numHiddenNetworks2 = numHiddenNetworks + 1;
                        settings.hiddenNetworkIds[numHiddenNetworks] = hiddenNetworkId.intValue();
                        numHiddenNetworks = numHiddenNetworks2;
                    }
                }
                channels.fillBucketSettings(bucketSettings, Integer.MAX_VALUE);
                settings.buckets = new BucketSettings[]{bucketSettings};
                if (WifiScanningServiceImpl.this.mScannerImpl.startSingleScan(settings, this)) {
                    RequestList<WifiScanner.ScanSettings> tmp = this.mActiveScans;
                    this.mActiveScans = this.mPendingScans;
                    this.mPendingScans = tmp;
                    this.mPendingScans.clear();
                    transitionTo(this.mScanningState);
                } else {
                    WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(0, this.mPendingScans.size());
                    sendOpFailedToAllAndClear(this.mPendingScans, WifiScanningServiceImpl.UNKNOWN_PID, "Failed to start single scan");
                }
            }
        }

        void sendOpFailedToAllAndClear(RequestList<?> clientHandlers, int reason, String description) {
            Iterator entry$iterator = clientHandlers.iterator();
            while (entry$iterator.hasNext()) {
                RequestInfo<?> entry = (RequestInfo) entry$iterator.next();
                WifiScanningServiceImpl.this.logCallback("singleScanFailed", entry.clientInfo, entry.handlerId, "reason=" + reason + ", " + description);
                entry.reportEvent(159762, 0, new OperationResult(reason, description));
            }
            clientHandlers.clear();
        }

        void reportFullScanResult(ScanResult result, int bucketsScanned) {
            for (RequestInfo<WifiScanner.ScanSettings> entry : this.mActiveScans) {
                if (ScanScheduleUtil.shouldReportFullScanResultForSettings(WifiScanningServiceImpl.this.mChannelHelper, result, bucketsScanned, (WifiScanner.ScanSettings) entry.settings, WifiScanningServiceImpl.UNKNOWN_PID)) {
                    entry.reportEvent(159764, 0, result);
                }
            }
        }

        void reportScanResults(ScanData results) {
            if (!(results == null || results.getResults() == null)) {
                if (results.getResults().length > 0) {
                    WifiScanningServiceImpl.this.mWifiMetrics.incrementNonEmptyScanResultCount();
                } else {
                    WifiScanningServiceImpl.this.mWifiMetrics.incrementEmptyScanResultCount();
                }
            }
            for (RequestInfo<WifiScanner.ScanSettings> entry : this.mActiveScans) {
                ScanData[] resultsToDeliver = ScanScheduleUtil.filterResultsForSettings(WifiScanningServiceImpl.this.mChannelHelper, new ScanData[]{results}, (WifiScanner.ScanSettings) entry.settings, WifiScanningServiceImpl.UNKNOWN_PID);
                ParcelableScanData parcelableScanData = new ParcelableScanData(resultsToDeliver);
                WifiScanningServiceImpl.this.logCallback("singleScanResults", entry.clientInfo, entry.handlerId, WifiScanningServiceImpl.describeForLog(resultsToDeliver));
                entry.reportEvent(159749, 0, parcelableScanData);
                entry.reportEvent(159767, 0, null);
            }
        }
    }

    private void localLog(String message) {
        this.mLocalLog.log(message);
    }

    private void logw(String message) {
        Log.w(TAG, message);
        this.mLocalLog.log(message);
    }

    private void loge(String message) {
        Log.e(TAG, message);
        this.mLocalLog.log(message);
    }

    public Messenger getMessenger() {
        if (this.mClientHandler != null) {
            return new Messenger(this.mClientHandler);
        }
        loge("WifiScanningServiceImpl trying to get messenger w/o initialization");
        return null;
    }

    public Bundle getAvailableChannels(int band) {
        this.mChannelHelper.updateChannels();
        ChannelSpec[] channelSpecs = this.mChannelHelper.getAvailableScanChannels(band);
        ArrayList<Integer> list = new ArrayList(channelSpecs.length);
        for (ChannelSpec channelSpec : channelSpecs) {
            list.add(Integer.valueOf(channelSpec.frequency));
        }
        Bundle b = new Bundle();
        b.putIntegerArrayList("Channels", list);
        return b;
    }

    private void enforceLocationHardwarePermission(int uid) {
        this.mContext.enforcePermission("android.permission.LOCATION_HARDWARE", UNKNOWN_PID, uid, "LocationHardware");
    }

    WifiScanningServiceImpl(Context context, Looper looper, WifiScannerImplFactory scannerImplFactory, IBatteryStats batteryStats, WifiInjector wifiInjector) {
        this.mLocalLog = new LocalLog(1024);
        this.mContext = context;
        this.mLooper = looper;
        this.mScannerImplFactory = scannerImplFactory;
        this.mBatteryStats = batteryStats;
        this.mClients = new ArrayMap();
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mWifiMetrics = wifiInjector.getWifiMetrics();
        this.mClock = wifiInjector.getClock();
        this.mPreviousSchedule = null;
    }

    public void startService() {
        this.mClientHandler = new ClientHandler(this.mLooper);
        this.mBackgroundScanStateMachine = new WifiBackgroundScanStateMachine(this.mLooper);
        this.mWifiChangeStateMachine = new WifiChangeStateMachine(this.mLooper);
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
        this.mWifiChangeStateMachine.start();
        this.mSingleScanStateMachine.start();
        this.mPnoScanStateMachine.start();
    }

    private static boolean isWorkSourceValid(WorkSource workSource) {
        return (workSource == null || workSource.size() <= 0 || workSource.get(0) < 0) ? DBG : true;
    }

    private WorkSource computeWorkSource(ClientInfo ci, WorkSource requestedWorkSource) {
        if (requestedWorkSource != null) {
            if (isWorkSourceValid(requestedWorkSource)) {
                requestedWorkSource.clearNames();
                return requestedWorkSource;
            }
            loge("Got invalid work source request: " + requestedWorkSource.toString() + " from " + ci);
        }
        WorkSource callingWorkSource = new WorkSource(ci.getUid());
        if (isWorkSourceValid(callingWorkSource)) {
            return callingWorkSource;
        }
        loge("Client has invalid work source: " + callingWorkSource);
        return new WorkSource();
    }

    void replySucceeded(Message msg) {
        if (msg.replyTo != null) {
            Message reply = Message.obtain();
            reply.what = 159761;
            reply.arg2 = msg.arg2;
            try {
                msg.replyTo.send(reply);
            } catch (RemoteException e) {
            }
        }
    }

    void replyFailed(Message msg, int reason, String description) {
        if (msg.replyTo != null) {
            Message reply = Message.obtain();
            reply.what = 159762;
            reply.arg2 = msg.arg2;
            reply.obj = new OperationResult(reason, description);
            try {
                msg.replyTo.send(reply);
            } catch (RemoteException e) {
            }
        }
    }

    private static String toString(int uid, WifiScanner.ScanSettings settings) {
        StringBuilder sb = new StringBuilder();
        sb.append("ScanSettings[uid=").append(uid);
        sb.append(", period=").append(settings.periodInMs);
        sb.append(", report=").append(settings.reportEvents);
        if (settings.reportEvents == 0 && settings.numBssidsPerScan > 0 && settings.maxScansToCache > 1) {
            sb.append(", batch=").append(settings.maxScansToCache);
            sb.append(", numAP=").append(settings.numBssidsPerScan);
        }
        sb.append(", ").append(ChannelHelper.toString(settings));
        sb.append("]");
        return sb.toString();
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump WifiScanner from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        pw.println("WifiScanningService - Log Begin ----");
        this.mLocalLog.dump(fd, pw, args);
        pw.println("WifiScanningService - Log End ----");
        pw.println();
        pw.println("clients:");
        for (ClientInfo client : this.mClients.values()) {
            pw.println("  " + client);
        }
        pw.println("listeners:");
        for (ClientInfo client2 : this.mClients.values()) {
            for (WifiScanner.ScanSettings settings : this.mBackgroundScanStateMachine.getBackgroundScanSettings(client2)) {
                pw.println("  " + toString(client2.mUid, settings));
            }
        }
        if (this.mBackgroundScheduler != null) {
            ScanSettings schedule = this.mBackgroundScheduler.getSchedule();
            if (schedule != null) {
                pw.println("schedule:");
                pw.println("  base period: " + schedule.base_period_ms);
                pw.println("  max ap per scan: " + schedule.max_ap_per_scan);
                pw.println("  batched scans: " + schedule.report_threshold_num_scans);
                pw.println("  buckets:");
                for (int b = 0; b < schedule.num_buckets; b++) {
                    BucketSettings bucket = schedule.buckets[b];
                    pw.println("    bucket " + bucket.bucket + " (" + bucket.period_ms + "ms)[" + bucket.report_events + "]: " + ChannelHelper.toString(bucket));
                }
            }
        }
        if (this.mPnoScanStateMachine != null) {
            this.mPnoScanStateMachine.dump(fd, pw, args);
        }
    }

    void logScanRequest(String request, ClientInfo ci, int id, WorkSource workSource, WifiScanner.ScanSettings settings, PnoSettings pnoSettings) {
        StringBuilder sb = new StringBuilder();
        sb.append(request).append(": ").append(ci.toString()).append(",Id=").append(id);
        if (workSource != null) {
            sb.append(",").append(workSource);
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

    void logCallback(String callback, ClientInfo ci, int id, String extra) {
        StringBuilder sb = new StringBuilder();
        sb.append(callback).append(": ").append(ci.toString()).append(",Id=").append(id);
        if (extra != null) {
            sb.append(",").append(extra);
        }
        localLog(sb.toString());
    }

    static String describeForLog(ScanData[] results) {
        StringBuilder sb = new StringBuilder();
        sb.append("results=");
        for (int i = 0; i < results.length; i++) {
            if (i > 0) {
                sb.append(";");
            }
            sb.append(results[i].getResults().length);
        }
        return sb.toString();
    }

    static String describeForLog(ScanResult[] results) {
        return "results=" + results.length;
    }

    static String describeTo(StringBuilder sb, WifiScanner.ScanSettings scanSettings) {
        sb.append("ScanSettings { ").append(" band:").append(scanSettings.band).append(" period:").append(scanSettings.periodInMs).append(" reportEvents:").append(scanSettings.reportEvents).append(" numBssidsPerScan:").append(scanSettings.numBssidsPerScan).append(" maxScansToCache:").append(scanSettings.maxScansToCache).append(" channels:[ ");
        if (scanSettings.channels != null) {
            for (ChannelSpec channelSpec : scanSettings.channels) {
                sb.append(channelSpec.frequency).append(" ");
            }
        }
        sb.append(" ] ").append(" } ");
        return sb.toString();
    }

    static String describeTo(StringBuilder sb, PnoSettings pnoSettings) {
        sb.append("PnoSettings { ").append(" min5GhzRssi:").append(pnoSettings.min5GHzRssi).append(" min24GhzRssi:").append(pnoSettings.min24GHzRssi).append(" initialScoreMax:").append(pnoSettings.initialScoreMax).append(" currentConnectionBonus:").append(pnoSettings.currentConnectionBonus).append(" sameNetworkBonus:").append(pnoSettings.sameNetworkBonus).append(" secureBonus:").append(pnoSettings.secureBonus).append(" band5GhzBonus:").append(pnoSettings.band5GHzBonus).append(" isConnected:").append(pnoSettings.isConnected).append(" networks:[ ");
        if (pnoSettings.networkList != null) {
            for (int i = 0; i < pnoSettings.networkList.length; i++) {
                sb.append(pnoSettings.networkList[i].ssid).append(",").append(pnoSettings.networkList[i].networkId).append(" ");
            }
        }
        sb.append(" ] ").append(" } ");
        return sb.toString();
    }
}
