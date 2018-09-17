package com.android.server.wifi.scanner;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.IWifiScanner.Stub;
import android.net.wifi.ScanResult;
import android.net.wifi.ScanResult.InformationElement;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.OperationResult;
import android.net.wifi.WifiScanner.ParcelableScanData;
import android.net.wifi.WifiScanner.ParcelableScanResults;
import android.net.wifi.WifiScanner.PnoSettings;
import android.net.wifi.WifiScanner.ScanData;
import android.os.Binder;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.ArrayMap;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.Clock;
import com.android.server.wifi.FrameworkFacade;
import com.android.server.wifi.WifiConnectivityHelper;
import com.android.server.wifi.WifiConnectivityManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiLog;
import com.android.server.wifi.WifiMetrics;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiNative.BucketSettings;
import com.android.server.wifi.WifiNative.HiddenNetwork;
import com.android.server.wifi.WifiNative.PnoEventHandler;
import com.android.server.wifi.WifiNative.PnoNetwork;
import com.android.server.wifi.WifiNative.ScanCapabilities;
import com.android.server.wifi.WifiNative.ScanEventHandler;
import com.android.server.wifi.WifiNative.ScanSettings;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.hotspot2.anqp.NAIRealmData;
import com.android.server.wifi.scanner.ChannelHelper.ChannelCollection;
import com.android.server.wifi.scanner.WifiScannerImpl.WifiScannerImplFactory;
import com.android.server.wifi.util.WifiHandler;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
    private static boolean mSendScanResultsBroadcast = false;
    private final AlarmManager mAlarmManager;
    private WifiBackgroundScanStateMachine mBackgroundScanStateMachine;
    private BackgroundScanScheduler mBackgroundScheduler;
    private final IBatteryStats mBatteryStats;
    private ChannelHelper mChannelHelper;
    private ClientHandler mClientHandler;
    private final ArrayMap<Messenger, ClientInfo> mClients;
    private final Clock mClock;
    private final Context mContext;
    private final FrameworkFacade mFrameworkFacade;
    private final LocalLog mLocalLog = new LocalLog(512);
    private WifiLog mLog;
    private final Looper mLooper;
    private WifiPnoScanStateMachine mPnoScanStateMachine;
    private ScanSettings mPreviousSchedule;
    private WifiScannerImpl mScannerImpl;
    private final WifiScannerImplFactory mScannerImplFactory;
    private final RequestList<Void> mSingleScanListeners = new RequestList(this, null);
    private WifiSingleScanStateMachine mSingleScanStateMachine;
    private final WifiMetrics mWifiMetrics;

    private class ClientHandler extends WifiHandler {
        ClientHandler(String tag, Looper looper) {
            super(tag, looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ExternalClientInfo client;
            switch (msg.what) {
                case 69633:
                    client = (ExternalClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                    if (client != null) {
                        WifiScanningServiceImpl.this.logw("duplicate client connection: " + msg.sendingUid + ", messenger=" + msg.replyTo);
                        client.mChannel.replyToMessage(msg, 69634, 3);
                        return;
                    }
                    AsyncChannel ac = WifiScanningServiceImpl.this.mFrameworkFacade.makeWifiAsyncChannel(WifiScanningServiceImpl.TAG);
                    ac.connected(WifiScanningServiceImpl.this.mContext, this, msg.replyTo);
                    client = new ExternalClientInfo(msg.sendingUid, msg.replyTo, ac);
                    client.register();
                    ac.replyToMessage(msg, 69634, 0);
                    WifiScanningServiceImpl.this.localLog("client connected: " + client);
                    return;
                case 69635:
                    client = (ExternalClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                    if (client != null) {
                        client.mChannel.disconnect();
                    }
                    return;
                case 69636:
                    client = (ExternalClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                    if (!(client == null || msg.arg1 == 2 || msg.arg1 == 3)) {
                        WifiScanningServiceImpl.this.localLog("client disconnected: " + client + ", reason: " + msg.arg1);
                        client.cleanup();
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
                                WifiScanningServiceImpl.this.loge("Could not find client info for message " + msg.replyTo + ", msg=" + msg);
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
                        WifiScanningServiceImpl.this.localLog("failed to authorize app: " + e);
                        WifiScanningServiceImpl.this.replyFailed(msg, -4, "Not authorized");
                        return;
                    }
            }
        }
    }

    private abstract class ClientInfo {
        protected final Messenger mMessenger;
        private boolean mScanWorkReported = false;
        private final int mUid;
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
            return totalScanDurationPerHour / 200;
        }

        public void reportScanWorkUpdate() {
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
        private final AsyncChannel mChannel;
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

        public String toString() {
            return "InternalClientInfo[]";
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
        /* synthetic */ RequestList(WifiScanningServiceImpl this$0, RequestList -this1) {
            this();
        }

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

    class WifiBackgroundScanStateMachine extends StateMachine implements ScanEventHandler {
        private final RequestList<WifiScanner.ScanSettings> mActiveBackgroundScans = new RequestList(WifiScanningServiceImpl.this, null);
        private final DefaultState mDefaultState = new DefaultState();
        private final PausedState mPausedState = new PausedState();
        private final StartedState mStartedState = new StartedState();

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
                        if (WifiScanningServiceImpl.this.mScannerImpl == null) {
                            WifiScanningServiceImpl.this.mScannerImpl = WifiScanningServiceImpl.this.mScannerImplFactory.create(WifiScanningServiceImpl.this.mContext, WifiScanningServiceImpl.this.mLooper, WifiScanningServiceImpl.this.mClock);
                            WifiScanningServiceImpl.this.mScannerImpl.setWifiScanLogger(WifiScanningServiceImpl.this.mLocalLog);
                            WifiScanningServiceImpl.this.mChannelHelper = WifiScanningServiceImpl.this.mScannerImpl.getChannelHelper();
                        }
                        WifiScanningServiceImpl.this.mBackgroundScheduler = new BackgroundScanScheduler(WifiScanningServiceImpl.this.mChannelHelper);
                        ScanCapabilities capabilities = new ScanCapabilities();
                        if (!WifiScanningServiceImpl.this.mScannerImpl.getScanCapabilities(capabilities)) {
                            WifiBackgroundScanStateMachine.this.loge("could not get scan capabilities");
                            return true;
                        } else if (capabilities.max_scan_buckets <= 0) {
                            WifiBackgroundScanStateMachine.this.loge("invalid max buckets in scan capabilities " + capabilities.max_scan_buckets);
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
                WifiBackgroundScanStateMachine.this.sendBackgroundScanFailedToAllAndClear(-1, "Scan was interrupted");
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
                    case WifiConnectivityManager.MAX_PERIODIC_SCAN_INTERVAL_MS /*160000*/:
                        WifiBackgroundScanStateMachine.this.reportScanResults(WifiScanningServiceImpl.this.mScannerImpl.getLatestBatchedScanResults(true));
                        break;
                    case WifiScanningServiceImpl.CMD_FULL_SCAN_RESULTS /*160001*/:
                        WifiBackgroundScanStateMachine.this.reportFullScanResult((ScanResult) msg.obj, msg.arg2);
                        break;
                    case WifiScanningServiceImpl.CMD_DRIVER_LOADED /*160006*/:
                        return false;
                    case WifiScanningServiceImpl.CMD_DRIVER_UNLOADED /*160007*/:
                        return false;
                    case WifiScanningServiceImpl.CMD_SCAN_PAUSED /*160008*/:
                        WifiBackgroundScanStateMachine.this.reportScanResults((ScanData[]) msg.obj);
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

        public void onScanPaused(ScanData[] scanData) {
            sendMessage(WifiScanningServiceImpl.CMD_SCAN_PAUSED, scanData);
        }

        public void onScanRestarted() {
            sendMessage(WifiScanningServiceImpl.CMD_SCAN_RESTARTED);
        }

        private boolean addBackgroundScanRequest(ClientInfo ci, int handler, WifiScanner.ScanSettings settings, WorkSource workSource) {
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
            ScanSettings schedule = WifiScanningServiceImpl.this.mBackgroundScheduler.getSchedule();
            if (ScanScheduleUtil.scheduleEquals(WifiScanningServiceImpl.this.mPreviousSchedule, schedule)) {
                Log.i("WifiScanLog", "schedule updated with no change");
                return true;
            }
            StringBuilder keys = new StringBuilder();
            for (RequestInfo<WifiScanner.ScanSettings> entry : this.mActiveBackgroundScans) {
                keys.append(WifiScanner.getScanKey(entry.handlerId));
            }
            schedule.handlerId = keys.toString();
            WifiScanningServiceImpl.this.mPreviousSchedule = schedule;
            if (schedule.num_buckets == 0) {
                WifiScanningServiceImpl.this.mScannerImpl.stopBatchedScan();
                Log.i("WifiScanLog", schedule.handlerId + "scan stopped");
                return true;
            }
            int b;
            BucketSettings bucket;
            WifiScanningServiceImpl.this.localLog("starting scan: base period=" + schedule.base_period_ms + ", max ap per scan=" + schedule.max_ap_per_scan + ", batched scans=" + schedule.report_threshold_num_scans);
            for (b = 0; b < schedule.num_buckets; b++) {
                bucket = schedule.buckets[b];
                WifiScanningServiceImpl.this.localLog("bucket " + bucket.bucket + " (" + bucket.period_ms + "ms)" + "[" + bucket.report_events + "]: " + ChannelHelper.toString(bucket));
            }
            if (WifiScanningServiceImpl.this.mScannerImpl.startBatchedScan(schedule, this)) {
                Log.i("WifiScanLog", schedule.handlerId + "startBatchedScan success!");
                return true;
            }
            Log.i("WifiScanLog", schedule.handlerId + "startBatchedScan failed!");
            WifiScanningServiceImpl.this.mPreviousSchedule = null;
            loge("error starting scan: base period=" + schedule.base_period_ms + ", max ap per scan=" + schedule.max_ap_per_scan + ", batched scans=" + schedule.report_threshold_num_scans);
            for (b = 0; b < schedule.num_buckets; b++) {
                bucket = schedule.buckets[b];
                loge("bucket " + bucket.bucket + " (" + bucket.period_ms + "ms)" + "[" + bucket.report_events + "]: " + ChannelHelper.toString(bucket));
            }
            return false;
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
            if (results == null) {
                Log.d(WifiScanningServiceImpl.TAG, "The results is null, nothing to report.");
                return;
            }
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
    }

    class WifiPnoScanStateMachine extends StateMachine implements PnoEventHandler {
        private final RequestList<Pair<PnoSettings, WifiScanner.ScanSettings>> mActivePnoScans = new RequestList(WifiScanningServiceImpl.this, null);
        private final DefaultState mDefaultState = new DefaultState();
        private final HwPnoScanState mHwPnoScanState = new HwPnoScanState();
        private InternalClientInfo mInternalClientInfo;
        private final SingleScanState mSingleScanState = new SingleScanState();
        private final StartedState mStartedState = new StartedState();
        private final SwPnoScanState mSwPnoScanState = new SwPnoScanState();

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
                        WifiScanningServiceImpl.this.replyFailed(msg, -1, "not available");
                        break;
                    case WifiScanningServiceImpl.CMD_DRIVER_LOADED /*160006*/:
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                        break;
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
                        WifiPnoScanStateMachine.this.sendPnoScanFailedToAllAndClear(-1, "couldn't retrieve setting");
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                        break;
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
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                switch (msg.what) {
                    case 159749:
                        ScanData[] scanDatas = msg.obj.getResults();
                        WifiPnoScanStateMachine.this.reportPnoNetworkFound(scanDatas[scanDatas.length - 1].getResults());
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mHwPnoScanState);
                        break;
                    case 159762:
                        WifiPnoScanStateMachine.this.sendPnoScanFailedToAllAndClear(-1, "single scan failed");
                        WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mStartedState);
                        break;
                    default:
                        return false;
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
                ClientInfo ci = (ClientInfo) WifiScanningServiceImpl.this.mClients.get(msg.replyTo);
                switch (msg.what) {
                    case 159768:
                        Bundle pnoParams = msg.obj;
                        if (pnoParams != null) {
                            pnoParams.setDefusable(true);
                            PnoSettings pnoSettings = (PnoSettings) pnoParams.getParcelable("PnoSettings");
                            WifiPnoScanStateMachine.this.deferMessage(msg);
                            if (!WifiScanningServiceImpl.this.mScannerImpl.isHwPnoSupported(pnoSettings.isConnected)) {
                                Log.i("WifiScanLog", WifiScanner.getScanKey(msg.arg2) + "isHwPnoSupported false");
                                WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mSwPnoScanState);
                                break;
                            }
                            Log.i("WifiScanLog", WifiScanner.getScanKey(msg.arg2) + "isHwPnoSupported true");
                            WifiPnoScanStateMachine.this.transitionTo(WifiPnoScanStateMachine.this.mHwPnoScanState);
                            break;
                        }
                        WifiScanningServiceImpl.this.replyFailed(msg, -3, "params null");
                        return true;
                    case 159769:
                        WifiScanningServiceImpl.this.replyFailed(msg, -1, "no scan running");
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        class SwPnoScanState extends State {
            private final ArrayList<ScanResult> mSwPnoFullScanResults = new ArrayList();

            SwPnoScanState() {
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
                        WifiPnoScanStateMachine.this.sendPnoScanFailedToAllAndClear(-1, "background scan failed");
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

        private WifiNative.PnoSettings convertSettingsToPnoNative(WifiScanner.ScanSettings scanSettings, PnoSettings pnoSettings) {
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
            nativePnoSetting.networkList = new PnoNetwork[pnoSettings.networkList.length];
            for (int i = 0; i < pnoSettings.networkList.length; i++) {
                nativePnoSetting.networkList[i] = new PnoNetwork();
                nativePnoSetting.networkList[i].ssid = pnoSettings.networkList[i].ssid;
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
            WifiScanningServiceImpl.this.localLog(WifiScanner.getScanKey(handler), "26", "addPnoScanRequest");
            this.mActivePnoScans.addRequest(ci, handler, WifiStateMachine.WIFI_WORK_SOURCE, Pair.create(pnoSettings, scanSettings));
            addInternalClient(ci);
        }

        private Pair<PnoSettings, WifiScanner.ScanSettings> removePnoScanRequest(ClientInfo ci, int handler) {
            return (Pair) this.mActivePnoScans.removeRequest(ci, handler);
        }

        private boolean addHwPnoScanRequest(ClientInfo ci, int handler, WifiScanner.ScanSettings scanSettings, PnoSettings pnoSettings) {
            if (ci == null) {
                Log.d(WifiScanningServiceImpl.TAG, "Failing scan request ClientInfo not found " + WifiScanner.getScanKey(handler));
                return false;
            } else if (this.mActivePnoScans.isEmpty()) {
                if (WifiScanningServiceImpl.this.mScannerImpl.setHwPnoList(convertSettingsToPnoNative(scanSettings, pnoSettings), WifiScanningServiceImpl.this.mPnoScanStateMachine)) {
                    WifiScanningServiceImpl.this.logScanRequest("addHwPnoScanRequest", ci, handler, null, scanSettings, pnoSettings);
                    addPnoScanRequest(ci, handler, scanSettings, pnoSettings);
                    if (WifiScanningServiceImpl.this.mScannerImpl.shouldScheduleBackgroundScanForHwPno()) {
                        addBackgroundScanRequest(scanSettings);
                    }
                    return true;
                }
                loge("Failing setHwPnoList" + WifiScanner.getScanKey(handler));
                return false;
            } else {
                loge("Failing scan request because there is already an active scan" + WifiScanner.getScanKey(handler));
                return false;
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
                Log.d(WifiScanningServiceImpl.TAG, "Failing scan request ClientInfo not found " + WifiScanner.getScanKey(handler));
                return false;
            } else if (this.mActivePnoScans.isEmpty()) {
                WifiScanningServiceImpl.this.logScanRequest("addSwPnoScanRequest", ci, handler, null, scanSettings, pnoSettings);
                addPnoScanRequest(ci, handler, scanSettings, pnoSettings);
                scanSettings.reportEvents = 3;
                addBackgroundScanRequest(scanSettings);
                return true;
            } else {
                loge("Failing scan request because there is already an active scan" + WifiScanner.getScanKey(handler));
                return false;
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
                    return false;
                }
            }
            return true;
        }
    }

    class WifiSingleScanStateMachine extends StateMachine implements ScanEventHandler {
        public static final int CACHED_SCAN_RESULTS_MAX_AGE_IN_MILLIS = 180000;
        private ScanSettings mActiveScanSettings = null;
        private RequestList<WifiScanner.ScanSettings> mActiveScans = new RequestList(WifiScanningServiceImpl.this, null);
        private final List<ScanResult> mCachedScanResults = new ArrayList();
        private final DefaultState mDefaultState = new DefaultState();
        private final DriverStartedState mDriverStartedState = new DriverStartedState();
        private final IdleState mIdleState = new IdleState();
        private RequestList<WifiScanner.ScanSettings> mPendingScans = new RequestList(WifiScanningServiceImpl.this, null);
        private final ScanningState mScanningState = new ScanningState();

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
                        msg.obj = new ParcelableScanResults(filterCachedScanResultsByAge());
                        WifiScanningServiceImpl.this.replySucceeded(msg);
                        return true;
                    case WifiConnectivityManager.MAX_PERIODIC_SCAN_INTERVAL_MS /*160000*/:
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
                        return false;
                }
            }

            private ScanResult[] filterCachedScanResultsByAge() {
                ScanResult[] filterCachedScanResults = (ScanResult[]) WifiSingleScanStateMachine.this.mCachedScanResults.stream().filter(new com.android.server.wifi.scanner.-$Lambda$ckIrrmbHBOVG4LZY2cRLHtMBPV4.AnonymousClass1(WifiScanningServiceImpl.this.mClock.getElapsedSinceBootMillis())).toArray(new -$Lambda$ckIrrmbHBOVG4LZY2cRLHtMBPV4());
                return filterCachedScanResults.length == 0 ? (ScanResult[]) WifiSingleScanStateMachine.this.mCachedScanResults.toArray(new ScanResult[WifiSingleScanStateMachine.this.mCachedScanResults.size()]) : filterCachedScanResults;
            }

            static /* synthetic */ boolean lambda$-com_android_server_wifi_scanner_WifiScanningServiceImpl$WifiSingleScanStateMachine$DefaultState_25189(long currentTimeInMillis, ScanResult scanResult) {
                return currentTimeInMillis - (scanResult.timestamp / 1000) < 180000;
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
                switch (msg.what) {
                    case 159765:
                        WifiScanningServiceImpl.this.mWifiMetrics.incrementOneshotScanCount();
                        int handler = msg.arg2;
                        Bundle scanParams = msg.obj;
                        if (scanParams == null) {
                            if (ci != null) {
                                WifiScanningServiceImpl.this.logCallback("singleScanInvalidRequest", ci, handler, "null params");
                            }
                            Log.w("WifiScanLog", WifiScanner.getScanKey(handler) + " params null");
                            WifiScanningServiceImpl.this.replyFailed(msg, -3, "params null");
                            return true;
                        }
                        scanParams.setDefusable(true);
                        WifiScanner.ScanSettings scanSettings = (WifiScanner.ScanSettings) scanParams.getParcelable("ScanSettings");
                        WorkSource workSource = (WorkSource) scanParams.getParcelable("WorkSource");
                        if (WifiSingleScanStateMachine.this.validateScanRequest(ci, handler, scanSettings, workSource)) {
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
                WifiSingleScanStateMachine.this.mActiveScanSettings = null;
                try {
                    WifiScanningServiceImpl.this.mBatteryStats.noteWifiScanStoppedFromSource(this.mScanWorkSource);
                } catch (RemoteException e) {
                    WifiSingleScanStateMachine.this.loge(e.toString());
                }
                WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(0, WifiSingleScanStateMachine.this.mActiveScans.size());
                WifiSingleScanStateMachine.this.sendOpFailedToAllAndClear(WifiSingleScanStateMachine.this.mActiveScans, -1, "Scan was interrupted");
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case WifiConnectivityManager.MAX_PERIODIC_SCAN_INTERVAL_MS /*160000*/:
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
                        WifiSingleScanStateMachine.this.sendScanResultBroadcast(false);
                        WifiSingleScanStateMachine.this.sendOpFailedToAllAndClear(WifiSingleScanStateMachine.this.mActiveScans, -1, "Scan failed");
                        WifiSingleScanStateMachine.this.transitionTo(WifiSingleScanStateMachine.this.mIdleState);
                        return true;
                    default:
                        return false;
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

        public void onScanPaused(ScanData[] scanData) {
            Log.e(WifiScanningServiceImpl.TAG, "Got scan paused for single scan");
        }

        public void onScanRestarted() {
            Log.e(WifiScanningServiceImpl.TAG, "Got scan restarted for single scan");
        }

        boolean validateScanRequest(ClientInfo ci, int handler, WifiScanner.ScanSettings settings, WorkSource workSource) {
            if (ci == null || settings == null) {
                Log.d("WifiScanLog", WifiScanner.getScanKey(handler) + " Failing single scan request ClientInfo not found " + handler + " ci is :" + ci + " settings is :" + settings);
                return false;
            } else if (settings.band != 0 || (settings.channels != null && settings.channels.length != 0)) {
                return true;
            } else {
                Log.d("WifiScanLog", WifiScanner.getScanKey(handler) + "Failing single scan because channel list was empty");
                return false;
            }
        }

        boolean activeScanSatisfies(WifiScanner.ScanSettings settings) {
            if (this.mActiveScanSettings == null) {
                return false;
            }
            BucketSettings activeBucket = this.mActiveScanSettings.buckets[0];
            ChannelCollection activeChannels = WifiScanningServiceImpl.this.mChannelHelper.createChannelCollection();
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
                List<HiddenNetwork> activeHiddenNetworks = new ArrayList();
                for (HiddenNetwork hiddenNetwork : this.mActiveScanSettings.hiddenNetworks) {
                    activeHiddenNetworks.add(hiddenNetwork);
                }
                for (WifiScanner.ScanSettings.HiddenNetwork hiddenNetwork2 : settings.hiddenNetworks) {
                    HiddenNetwork nativeHiddenNetwork = new HiddenNetwork();
                    nativeHiddenNetwork.ssid = hiddenNetwork2.ssid;
                    if (!activeHiddenNetworks.contains(nativeHiddenNetwork)) {
                        return false;
                    }
                }
            }
            return true;
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
                WifiScanningServiceImpl.this.logScanRequest("removeSingleScanRequests", ci, -1, null, null, null);
                this.mPendingScans.removeAllForClient(ci);
                this.mActiveScans.removeAllForClient(ci);
            }
        }

        void tryToStartNewScan() {
            if (this.mPendingScans.size() != 0) {
                HiddenNetwork hiddenNetwork;
                WifiScanningServiceImpl.this.mChannelHelper.updateChannels();
                ScanSettings settings = new ScanSettings();
                settings.num_buckets = 1;
                BucketSettings bucketSettings = new BucketSettings();
                bucketSettings.bucket = 0;
                bucketSettings.period_ms = 0;
                bucketSettings.report_events = 1;
                ChannelCollection channels = WifiScanningServiceImpl.this.mChannelHelper.createChannelCollection();
                List<HiddenNetwork> hiddenNetworkList = new ArrayList();
                StringBuilder keys = new StringBuilder();
                for (RequestInfo<WifiScanner.ScanSettings> entry : this.mPendingScans) {
                    keys.append(WifiScanner.getScanKey(entry.handlerId));
                    channels.addChannels((WifiScanner.ScanSettings) entry.settings);
                    if (((WifiScanner.ScanSettings) entry.settings).hiddenNetworks != null) {
                        for (WifiScanner.ScanSettings.HiddenNetwork hiddenNetwork2 : ((WifiScanner.ScanSettings) entry.settings).hiddenNetworks) {
                            hiddenNetwork = new HiddenNetwork();
                            hiddenNetwork.ssid = hiddenNetwork2.ssid;
                            hiddenNetworkList.add(hiddenNetwork);
                        }
                    }
                    if ((((WifiScanner.ScanSettings) entry.settings).reportEvents & 2) != 0) {
                        bucketSettings.report_events |= 2;
                    }
                }
                if (hiddenNetworkList.size() > 0) {
                    settings.hiddenNetworks = new HiddenNetwork[hiddenNetworkList.size()];
                    int numHiddenNetworks = 0;
                    for (HiddenNetwork hiddenNetwork3 : hiddenNetworkList) {
                        int numHiddenNetworks2 = numHiddenNetworks + 1;
                        settings.hiddenNetworks[numHiddenNetworks] = hiddenNetwork3;
                        numHiddenNetworks = numHiddenNetworks2;
                    }
                }
                channels.fillBucketSettings(bucketSettings, Integer.MAX_VALUE);
                settings.buckets = new BucketSettings[]{bucketSettings};
                WifiScanningServiceImpl.this.localLog(keys.toString(), "25", "tryToStartNewScan in WifiScanningServiceImpl");
                settings.handlerId = keys.toString();
                if (WifiScanningServiceImpl.this.mScannerImpl.startSingleScan(settings, this)) {
                    this.mActiveScanSettings = settings;
                    RequestList<WifiScanner.ScanSettings> tmp = this.mActiveScans;
                    this.mActiveScans = this.mPendingScans;
                    this.mPendingScans = tmp;
                    this.mPendingScans.clear();
                    transitionTo(this.mScanningState);
                } else {
                    Log.w("WifiScanLog", keys + "start single scan failed ");
                    WifiScanningServiceImpl.this.mWifiMetrics.incrementScanReturnEntry(0, this.mPendingScans.size());
                    sendOpFailedToAllAndClear(this.mPendingScans, -1, "Failed to start single scan");
                }
            }
        }

        void sendOpFailedToAllAndClear(RequestList<?> clientHandlers, int reason, String description) {
            StringBuilder keys = new StringBuilder();
            Iterator entry$iterator = clientHandlers.iterator();
            while (entry$iterator.hasNext()) {
                RequestInfo<?> entry = (RequestInfo) entry$iterator.next();
                WifiScanningServiceImpl.this.logCallback("singleScanFailed", entry.clientInfo, entry.handlerId, "reason=" + reason + ", " + description);
                entry.reportEvent(159762, 0, new OperationResult(reason, description));
                keys.append(WifiScanner.getScanKey(entry.handlerId));
            }
            if (clientHandlers.size() > 0) {
                Log.w("WifiScanLog", keys + "WifiSingleScanStateMachine sendOpFailedToAllAndClear scan failed des:" + description);
            }
            clientHandlers.clear();
        }

        void reportFullScanResult(ScanResult result, int bucketsScanned) {
            for (RequestInfo<WifiScanner.ScanSettings> entry : this.mActiveScans) {
                if (ScanScheduleUtil.shouldReportFullScanResultForSettings(WifiScanningServiceImpl.this.mChannelHelper, result, bucketsScanned, (WifiScanner.ScanSettings) entry.settings, -1)) {
                    entry.reportEvent(159764, 0, result);
                }
            }
            for (RequestInfo<Void> entry2 : WifiScanningServiceImpl.this.mSingleScanListeners) {
                entry2.reportEvent(159764, 0, result);
            }
        }

        private void sendScanResultBroadcast(boolean scanSucceeded) {
            Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
            intent.addFlags(67108864);
            intent.putExtra("resultsUpdated", scanSucceeded);
            WifiScanningServiceImpl.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }

        void reportScanResults(ScanData results) {
            if (!(results == null || results.getResults() == null)) {
                if (results.getResults().length > 0) {
                    WifiScanningServiceImpl.this.mWifiMetrics.incrementNonEmptyScanResultCount();
                } else {
                    WifiScanningServiceImpl.this.mWifiMetrics.incrementEmptyScanResultCount();
                }
            }
            ScanData[] allResults = new ScanData[]{results};
            StringBuilder keys = new StringBuilder();
            for (RequestInfo<WifiScanner.ScanSettings> entry : this.mActiveScans) {
                ScanData[] resultsToDeliver = ScanScheduleUtil.filterResultsForSettings(WifiScanningServiceImpl.this.mChannelHelper, allResults, (WifiScanner.ScanSettings) entry.settings, -1);
                ParcelableScanData parcelableResultsToDeliver = new ParcelableScanData(resultsToDeliver);
                WifiScanningServiceImpl.this.logCallback("singleScanResults", entry.clientInfo, entry.handlerId, WifiScanningServiceImpl.describeForLog(resultsToDeliver));
                keys.append(WifiScanner.getScanKey(entry.handlerId));
                entry.reportEvent(159749, 0, parcelableResultsToDeliver);
                entry.reportEvent(159767, 0, null);
                if (!WifiScanningServiceImpl.mSendScanResultsBroadcast && shouldSendScanResultsBroadcast(entry, false)) {
                    WifiScanningServiceImpl.mSendScanResultsBroadcast = true;
                }
            }
            if (!WifiScanningServiceImpl.mSendScanResultsBroadcast) {
                for (RequestInfo<WifiScanner.ScanSettings> entry2 : this.mPendingScans) {
                    if (shouldSendScanResultsBroadcast(entry2, true)) {
                        WifiScanningServiceImpl.mSendScanResultsBroadcast = true;
                        break;
                    }
                }
            }
            ParcelableScanData parcelableAllResults = new ParcelableScanData(allResults);
            for (RequestInfo<Void> entry3 : WifiScanningServiceImpl.this.mSingleScanListeners) {
                WifiScanningServiceImpl.this.logCallback("singleScanResults", entry3.clientInfo, entry3.handlerId, WifiScanningServiceImpl.describeForLog(allResults));
                entry3.reportEvent(159774, 0, keys.toString());
                entry3.reportEvent(159749, 0, parcelableAllResults);
            }
            if (results != null) {
                this.mCachedScanResults.clear();
                this.mCachedScanResults.addAll(Arrays.asList(results.getResults()));
                WifiScanningServiceImpl.this.updateScanResultByWifiPro(this.mCachedScanResults);
                sendScanResultBroadcast(true);
            } else {
                Log.w(WifiScanningServiceImpl.TAG, "LatestSingleScanResult is null");
                Log.w(WifiScanningServiceImpl.TAG, "reportScanResults: not add scan results, not send broadcast");
            }
            WifiScanningServiceImpl.mSendScanResultsBroadcast = false;
        }

        List<ScanResult> getCachedScanResultsAsList() {
            return this.mCachedScanResults;
        }

        private boolean shouldSendScanResultsBroadcast(RequestInfo<WifiScanner.ScanSettings> requestInfo, boolean isPendingScans) {
            List<RunningTaskInfo> runningTaskInfos = ((ActivityManager) WifiScanningServiceImpl.this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (!(runningTaskInfos == null || (runningTaskInfos.isEmpty() ^ 1) == 0)) {
                ComponentName cn = ((RunningTaskInfo) runningTaskInfos.get(0)).topActivity;
                String WIFI_SETTINGS = "com.android.settings.Settings$WifiSettingsActivity";
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
            this.mLog.trace("getMessenger() uid=%").c((long) Binder.getCallingUid()).flush();
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
        this.mLog.trace("getAvailableChannels uid=%").c((long) Binder.getCallingUid()).flush();
        return b;
    }

    private void enforceLocationHardwarePermission(int uid) {
        this.mContext.enforcePermission("android.permission.LOCATION_HARDWARE", -1, uid, "LocationHardware");
    }

    public WifiScanningServiceImpl(Context context, Looper looper, WifiScannerImplFactory scannerImplFactory, IBatteryStats batteryStats, WifiInjector wifiInjector) {
        this.mContext = context;
        this.mLooper = looper;
        this.mScannerImplFactory = scannerImplFactory;
        this.mBatteryStats = batteryStats;
        this.mClients = new ArrayMap();
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mWifiMetrics = wifiInjector.getWifiMetrics();
        this.mClock = wifiInjector.getClock();
        this.mLog = wifiInjector.makeLog(TAG);
        this.mFrameworkFacade = wifiInjector.getFrameworkFacade();
        this.mPreviousSchedule = null;
    }

    public void startService() {
        this.mClientHandler = new ClientHandler(TAG, this.mLooper);
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
    }

    public void setWifiHandlerLogForTest(WifiLog log) {
        this.mClientHandler.setWifiLog(log);
    }

    private static boolean isWorkSourceValid(WorkSource workSource) {
        return workSource != null && workSource.size() > 0 && workSource.get(0) >= 0;
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

    void replyFailed(Message msg, int reason, String description) {
        if (msg.replyTo != null) {
            Message reply = Message.obtain();
            reply.what = 159762;
            reply.arg2 = msg.arg2;
            reply.obj = new OperationResult(reason, description);
            try {
                msg.replyTo.send(reply);
                this.mLog.trace("replyFailed recvdMessage=% reason=%").c((long) msg.what).c((long) reason).flush();
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
        pw.println();
        if (this.mSingleScanStateMachine != null) {
            this.mSingleScanStateMachine.dump(fd, pw, args);
            pw.println();
            pw.println("Latest scan results:");
            List<ScanResult> scanResults = this.mSingleScanStateMachine.getCachedScanResultsAsList();
            long nowMs = System.currentTimeMillis();
            if (!(scanResults == null || scanResults.size() == 0)) {
                pw.println("    BSSID              Frequency  RSSI  Age(sec)   SSID                                 Flags");
                for (ScanResult r : scanResults) {
                    String age;
                    if (r.seen <= 0) {
                        age = "___?___";
                    } else if (nowMs < r.seen) {
                        age = "  0.000";
                    } else if (r.seen < nowMs - 1000000) {
                        age = ">1000.0";
                    } else {
                        age = String.format("%3.3f", new Object[]{Double.valueOf(((double) (nowMs - r.seen)) / 1000.0d)});
                    }
                    String ssid = r.SSID == null ? "" : r.SSID;
                    r20 = new Object[6];
                    r20[4] = String.format("%1.32s", new Object[]{ssid});
                    r20[5] = r.capabilities;
                    pw.printf("  %17s  %9d  %5d   %7s    %-32s  %s\n", r20);
                }
            }
            pw.println();
        }
    }

    void logScanRequest(String request, ClientInfo ci, int id, WorkSource workSource, WifiScanner.ScanSettings settings, PnoSettings pnoSettings) {
        StringBuilder sb = new StringBuilder();
        sb.append(request).append(": ").append(ci == null ? "ClientInfo[unknown]" : ci.toString()).append(",Id=").append(id);
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
        sb.append(callback).append(": ").append(ci == null ? "ClientInfo[unknown]" : ci.toString()).append(",Id=").append(WifiScanner.getScanKey(id));
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
                sb.append(NAIRealmData.NAI_REALM_STRING_SEPARATOR);
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
            for (PnoSettings.PnoNetwork pnoNetwork : pnoSettings.networkList) {
                sb.append(pnoNetwork.ssid).append(",");
            }
        }
        sb.append(" ] ").append(" } ");
        return sb.toString();
    }

    public void updateScanResultByWifiPro(List<ScanResult> list) {
    }

    void localLog(String scanKey, String eventKey, String log) {
        localLog(scanKey, eventKey, log, null);
    }

    void localLog(String scanKey, String eventKey, String log, Object... params) {
        WifiConnectivityHelper.localLog(this.mLocalLog, scanKey, eventKey, log, params);
    }
}
