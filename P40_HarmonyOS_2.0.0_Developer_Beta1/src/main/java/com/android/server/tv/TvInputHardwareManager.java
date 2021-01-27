package com.android.server.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.HdmiHotplugEvent;
import android.hardware.hdmi.IHdmiControlService;
import android.hardware.hdmi.IHdmiDeviceEventListener;
import android.hardware.hdmi.IHdmiHotplugEventListener;
import android.hardware.hdmi.IHdmiSystemAudioModeChangeListener;
import android.media.AudioDevicePort;
import android.media.AudioFormat;
import android.media.AudioGain;
import android.media.AudioGainConfig;
import android.media.AudioManager;
import android.media.AudioPatch;
import android.media.AudioPort;
import android.media.AudioPortConfig;
import android.media.tv.ITvInputHardware;
import android.media.tv.ITvInputHardwareCallback;
import android.media.tv.TvInputHardwareInfo;
import android.media.tv.TvInputInfo;
import android.media.tv.TvStreamConfig;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Surface;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.tv.TvInputHal;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
public class TvInputHardwareManager implements TvInputHal.Callback {
    private static final String TAG = TvInputHardwareManager.class.getSimpleName();
    private final AudioManager mAudioManager;
    private final SparseArray<Connection> mConnections = new SparseArray<>();
    private final Context mContext;
    private int mCurrentIndex = 0;
    private int mCurrentMaxIndex = 0;
    private final TvInputHal mHal = new TvInputHal(this);
    private final Handler mHandler = new ListenerHandler();
    private final SparseArray<String> mHardwareInputIdMap = new SparseArray<>();
    private final List<TvInputHardwareInfo> mHardwareList = new ArrayList();
    private final IHdmiDeviceEventListener mHdmiDeviceEventListener = new HdmiDeviceEventListener();
    private final List<HdmiDeviceInfo> mHdmiDeviceList = new LinkedList();
    private final IHdmiHotplugEventListener mHdmiHotplugEventListener = new HdmiHotplugEventListener();
    private final SparseArray<String> mHdmiInputIdMap = new SparseArray<>();
    private final SparseBooleanArray mHdmiStateMap = new SparseBooleanArray();
    private final IHdmiSystemAudioModeChangeListener mHdmiSystemAudioModeChangeListener = new HdmiSystemAudioModeChangeListener();
    private final Map<String, TvInputInfo> mInputMap = new ArrayMap();
    private final Listener mListener;
    private final Object mLock = new Object();
    private final List<Message> mPendingHdmiDeviceEvents = new LinkedList();
    private final BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        /* class com.android.server.tv.TvInputHardwareManager.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            TvInputHardwareManager.this.handleVolumeChange(context, intent);
        }
    };

    /* access modifiers changed from: package-private */
    public interface Listener {
        void onHardwareDeviceAdded(TvInputHardwareInfo tvInputHardwareInfo);

        void onHardwareDeviceRemoved(TvInputHardwareInfo tvInputHardwareInfo);

        void onHdmiDeviceAdded(HdmiDeviceInfo hdmiDeviceInfo);

        void onHdmiDeviceRemoved(HdmiDeviceInfo hdmiDeviceInfo);

        void onHdmiDeviceUpdated(String str, HdmiDeviceInfo hdmiDeviceInfo);

        void onStateChanged(String str, int i);
    }

    public TvInputHardwareManager(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mHal.init();
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            IHdmiControlService hdmiControlService = IHdmiControlService.Stub.asInterface(ServiceManager.getService("hdmi_control"));
            if (hdmiControlService != null) {
                try {
                    hdmiControlService.addHotplugEventListener(this.mHdmiHotplugEventListener);
                    hdmiControlService.addDeviceEventListener(this.mHdmiDeviceEventListener);
                    hdmiControlService.addSystemAudioModeChangeListener(this.mHdmiSystemAudioModeChangeListener);
                    this.mHdmiDeviceList.addAll(hdmiControlService.getInputDevices());
                } catch (RemoteException e) {
                    Slog.w(TAG, "Error registering listeners to HdmiControlService:", e);
                }
            } else {
                Slog.w(TAG, "HdmiControlService is not available");
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.media.VOLUME_CHANGED_ACTION");
            filter.addAction("android.media.STREAM_MUTE_CHANGED_ACTION");
            this.mContext.registerReceiver(this.mVolumeReceiver, filter);
            updateVolume();
        }
    }

    @Override // com.android.server.tv.TvInputHal.Callback
    public void onDeviceAvailable(TvInputHardwareInfo info, TvStreamConfig[] configs) {
        synchronized (this.mLock) {
            Connection connection = new Connection(info);
            connection.updateConfigsLocked(configs);
            this.mConnections.put(info.getDeviceId(), connection);
            buildHardwareListLocked();
            this.mHandler.obtainMessage(2, 0, 0, info).sendToTarget();
            if (info.getType() == 9) {
                processPendingHdmiDeviceEventsLocked();
            }
        }
    }

    private void buildHardwareListLocked() {
        this.mHardwareList.clear();
        for (int i = 0; i < this.mConnections.size(); i++) {
            this.mHardwareList.add(this.mConnections.valueAt(i).getHardwareInfoLocked());
        }
    }

    @Override // com.android.server.tv.TvInputHal.Callback
    public void onDeviceUnavailable(int deviceId) {
        synchronized (this.mLock) {
            Connection connection = this.mConnections.get(deviceId);
            if (connection == null) {
                String str = TAG;
                Slog.e(str, "onDeviceUnavailable: Cannot find a connection with " + deviceId);
                return;
            }
            connection.resetLocked(null, null, null, null, null);
            this.mConnections.remove(deviceId);
            buildHardwareListLocked();
            TvInputHardwareInfo info = connection.getHardwareInfoLocked();
            if (info.getType() == 9) {
                Iterator<HdmiDeviceInfo> it = this.mHdmiDeviceList.iterator();
                while (it.hasNext()) {
                    HdmiDeviceInfo deviceInfo = it.next();
                    if (deviceInfo.getPortId() == info.getHdmiPortId()) {
                        this.mHandler.obtainMessage(5, 0, 0, deviceInfo).sendToTarget();
                        it.remove();
                    }
                }
            }
            this.mHandler.obtainMessage(3, 0, 0, info).sendToTarget();
        }
    }

    @Override // com.android.server.tv.TvInputHal.Callback
    public void onStreamConfigurationChanged(int deviceId, TvStreamConfig[] configs) {
        synchronized (this.mLock) {
            Connection connection = this.mConnections.get(deviceId);
            if (connection == null) {
                String str = TAG;
                Slog.e(str, "StreamConfigurationChanged: Cannot find a connection with " + deviceId);
                return;
            }
            int previousConfigsLength = connection.getConfigsLengthLocked();
            connection.updateConfigsLocked(configs);
            String inputId = this.mHardwareInputIdMap.get(deviceId);
            if (inputId != null) {
                if ((previousConfigsLength == 0) != (connection.getConfigsLengthLocked() == 0)) {
                    this.mHandler.obtainMessage(1, connection.getInputStateLocked(), 0, inputId).sendToTarget();
                }
            }
            ITvInputHardwareCallback callback = connection.getCallbackLocked();
            if (callback != null) {
                try {
                    callback.onStreamConfigChanged(configs);
                } catch (RemoteException e) {
                    Slog.e(TAG, "error in onStreamConfigurationChanged", e);
                }
            }
        }
    }

    @Override // com.android.server.tv.TvInputHal.Callback
    public void onFirstFrameCaptured(int deviceId, int streamId) {
        synchronized (this.mLock) {
            Connection connection = this.mConnections.get(deviceId);
            if (connection == null) {
                String str = TAG;
                Slog.e(str, "FirstFrameCaptured: Cannot find a connection with " + deviceId);
                return;
            }
            Runnable runnable = connection.getOnFirstFrameCapturedLocked();
            if (runnable != null) {
                runnable.run();
                connection.setOnFirstFrameCapturedLocked(null);
            }
        }
    }

    public List<TvInputHardwareInfo> getHardwareList() {
        List<TvInputHardwareInfo> unmodifiableList;
        synchronized (this.mLock) {
            unmodifiableList = Collections.unmodifiableList(this.mHardwareList);
        }
        return unmodifiableList;
    }

    public List<HdmiDeviceInfo> getHdmiDeviceList() {
        List<HdmiDeviceInfo> unmodifiableList;
        synchronized (this.mLock) {
            unmodifiableList = Collections.unmodifiableList(this.mHdmiDeviceList);
        }
        return unmodifiableList;
    }

    private boolean checkUidChangedLocked(Connection connection, int callingUid, int resolvedUserId) {
        Integer connectionCallingUid = connection.getCallingUidLocked();
        Integer connectionResolvedUserId = connection.getResolvedUserIdLocked();
        return connectionCallingUid == null || connectionResolvedUserId == null || connectionCallingUid.intValue() != callingUid || connectionResolvedUserId.intValue() != resolvedUserId;
    }

    public void addHardwareInput(int deviceId, TvInputInfo info) {
        synchronized (this.mLock) {
            String oldInputId = this.mHardwareInputIdMap.get(deviceId);
            if (oldInputId != null) {
                Slog.w(TAG, "Trying to override previous registration: old = " + this.mInputMap.get(oldInputId) + ":" + deviceId + ", new = " + info + ":" + deviceId);
            }
            this.mHardwareInputIdMap.put(deviceId, info.getId());
            this.mInputMap.put(info.getId(), info);
            for (int i = 0; i < this.mHdmiStateMap.size(); i++) {
                TvInputHardwareInfo hardwareInfo = findHardwareInfoForHdmiPortLocked(this.mHdmiStateMap.keyAt(i));
                if (hardwareInfo != null) {
                    String inputId = this.mHardwareInputIdMap.get(hardwareInfo.getDeviceId());
                    if (inputId != null && inputId.equals(info.getId())) {
                        this.mHandler.obtainMessage(1, this.mHdmiStateMap.valueAt(i) ? 0 : 1, 0, inputId).sendToTarget();
                        return;
                    }
                }
            }
            Connection connection = this.mConnections.get(deviceId);
            if (connection != null) {
                this.mHandler.obtainMessage(1, connection.getInputStateLocked(), 0, info.getId()).sendToTarget();
            }
        }
    }

    private static <T> int indexOfEqualValue(SparseArray<T> map, T value) {
        for (int i = 0; i < map.size(); i++) {
            if (map.valueAt(i).equals(value)) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public static boolean intArrayContains(int[] array, int value) {
        for (int element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    public void addHdmiInput(int id, TvInputInfo info) {
        if (info.getType() == 1007) {
            synchronized (this.mLock) {
                if (indexOfEqualValue(this.mHardwareInputIdMap, info.getParentId()) >= 0) {
                    String oldInputId = this.mHdmiInputIdMap.get(id);
                    if (oldInputId != null) {
                        String str = TAG;
                        Slog.w(str, "Trying to override previous registration: old = " + this.mInputMap.get(oldInputId) + ":" + id + ", new = " + info + ":" + id);
                    }
                    this.mHdmiInputIdMap.put(id, info.getId());
                    this.mInputMap.put(info.getId(), info);
                } else {
                    throw new IllegalArgumentException("info (" + info + ") has invalid parentId.");
                }
            }
            return;
        }
        throw new IllegalArgumentException("info (" + info + ") has non-HDMI type.");
    }

    public void removeHardwareInput(String inputId) {
        synchronized (this.mLock) {
            this.mInputMap.remove(inputId);
            int hardwareIndex = indexOfEqualValue(this.mHardwareInputIdMap, inputId);
            if (hardwareIndex >= 0) {
                this.mHardwareInputIdMap.removeAt(hardwareIndex);
            }
            int deviceIndex = indexOfEqualValue(this.mHdmiInputIdMap, inputId);
            if (deviceIndex >= 0) {
                this.mHdmiInputIdMap.removeAt(deviceIndex);
            }
        }
    }

    public ITvInputHardware acquireHardware(int deviceId, ITvInputHardwareCallback callback, TvInputInfo info, int callingUid, int resolvedUserId) {
        if (callback != null) {
            synchronized (this.mLock) {
                Connection connection = this.mConnections.get(deviceId);
                if (connection == null) {
                    String str = TAG;
                    Slog.e(str, "Invalid deviceId : " + deviceId);
                    return null;
                }
                if (checkUidChangedLocked(connection, callingUid, resolvedUserId)) {
                    TvInputHardwareImpl hardware = new TvInputHardwareImpl(connection.getHardwareInfoLocked());
                    try {
                        callback.asBinder().linkToDeath(connection, 0);
                        connection.resetLocked(hardware, callback, info, Integer.valueOf(callingUid), Integer.valueOf(resolvedUserId));
                    } catch (RemoteException e) {
                        hardware.release();
                        return null;
                    }
                }
                return connection.getHardwareLocked();
            }
        }
        throw new NullPointerException();
    }

    public void releaseHardware(int deviceId, ITvInputHardware hardware, int callingUid, int resolvedUserId) {
        synchronized (this.mLock) {
            Connection connection = this.mConnections.get(deviceId);
            if (connection == null) {
                String str = TAG;
                Slog.e(str, "Invalid deviceId : " + deviceId);
                return;
            }
            if (connection.getHardwareLocked() == hardware) {
                if (!checkUidChangedLocked(connection, callingUid, resolvedUserId)) {
                    connection.resetLocked(null, null, null, null, null);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private TvInputHardwareInfo findHardwareInfoForHdmiPortLocked(int port) {
        for (TvInputHardwareInfo hardwareInfo : this.mHardwareList) {
            if (hardwareInfo.getType() == 9 && hardwareInfo.getHdmiPortId() == port) {
                return hardwareInfo;
            }
        }
        return null;
    }

    private int findDeviceIdForInputIdLocked(String inputId) {
        for (int i = 0; i < this.mConnections.size(); i++) {
            if (this.mConnections.get(i).getInfoLocked().getId().equals(inputId)) {
                return i;
            }
        }
        return -1;
    }

    public List<TvStreamConfig> getAvailableTvStreamConfigList(String inputId, int callingUid, int resolvedUserId) {
        List<TvStreamConfig> configsList = new ArrayList<>();
        synchronized (this.mLock) {
            int deviceId = findDeviceIdForInputIdLocked(inputId);
            if (deviceId < 0) {
                Slog.e(TAG, "Invalid inputId : " + inputId);
                return configsList;
            }
            TvStreamConfig[] configsLocked = this.mConnections.get(deviceId).getConfigsLocked();
            for (TvStreamConfig config : configsLocked) {
                if (config.getType() == 2) {
                    configsList.add(config);
                }
            }
            return configsList;
        }
    }

    public boolean captureFrame(String inputId, Surface surface, final TvStreamConfig config, int callingUid, int resolvedUserId) {
        synchronized (this.mLock) {
            int deviceId = findDeviceIdForInputIdLocked(inputId);
            if (deviceId < 0) {
                String str = TAG;
                Slog.e(str, "Invalid inputId : " + inputId);
                return false;
            }
            Connection connection = this.mConnections.get(deviceId);
            final TvInputHardwareImpl hardwareImpl = connection.getHardwareImplLocked();
            if (hardwareImpl == null) {
                return false;
            }
            Runnable runnable = connection.getOnFirstFrameCapturedLocked();
            if (runnable != null) {
                runnable.run();
                connection.setOnFirstFrameCapturedLocked(null);
            }
            boolean result = hardwareImpl.startCapture(surface, config);
            if (result) {
                connection.setOnFirstFrameCapturedLocked(new Runnable() {
                    /* class com.android.server.tv.TvInputHardwareManager.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        hardwareImpl.stopCapture(config);
                    }
                });
            }
            return result;
        }
    }

    private void processPendingHdmiDeviceEventsLocked() {
        Iterator<Message> it = this.mPendingHdmiDeviceEvents.iterator();
        while (it.hasNext()) {
            Message msg = it.next();
            if (findHardwareInfoForHdmiPortLocked(((HdmiDeviceInfo) msg.obj).getPortId()) != null) {
                msg.sendToTarget();
                it.remove();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVolume() {
        this.mCurrentMaxIndex = this.mAudioManager.getStreamMaxVolume(3);
        this.mCurrentIndex = this.mAudioManager.getStreamVolume(3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x002e  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x006a  */
    private void handleVolumeChange(Context context, Intent intent) {
        boolean z;
        int index;
        String action = intent.getAction();
        int hashCode = action.hashCode();
        if (hashCode != -1940635523) {
            if (hashCode == 1920758225 && action.equals("android.media.STREAM_MUTE_CHANGED_ACTION")) {
                z = true;
                if (z) {
                    if (!z) {
                        Slog.w(TAG, "Unrecognized intent: " + intent);
                        return;
                    } else if (intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1) != 3) {
                        return;
                    }
                } else if (intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1) == 3 && (index = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0)) != this.mCurrentIndex) {
                    this.mCurrentIndex = index;
                } else {
                    return;
                }
                synchronized (this.mLock) {
                    for (int i = 0; i < this.mConnections.size(); i++) {
                        TvInputHardwareImpl hardwareImpl = this.mConnections.valueAt(i).getHardwareImplLocked();
                        if (hardwareImpl != null) {
                            hardwareImpl.onMediaStreamVolumeChanged();
                        }
                    }
                }
                return;
            }
        } else if (action.equals("android.media.VOLUME_CHANGED_ACTION")) {
            z = false;
            if (z) {
            }
            synchronized (this.mLock) {
            }
        }
        z = true;
        if (z) {
        }
        synchronized (this.mLock) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getMediaStreamVolume() {
        return ((float) this.mCurrentIndex) / ((float) this.mCurrentMaxIndex);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            synchronized (this.mLock) {
                pw.println("TvInputHardwareManager Info:");
                pw.increaseIndent();
                pw.println("mConnections: deviceId -> Connection");
                pw.increaseIndent();
                for (int i = 0; i < this.mConnections.size(); i++) {
                    pw.println(this.mConnections.keyAt(i) + ": " + this.mConnections.valueAt(i));
                }
                pw.decreaseIndent();
                pw.println("mHardwareList:");
                pw.increaseIndent();
                for (TvInputHardwareInfo tvInputHardwareInfo : this.mHardwareList) {
                    pw.println(tvInputHardwareInfo);
                }
                pw.decreaseIndent();
                pw.println("mHdmiDeviceList:");
                pw.increaseIndent();
                for (HdmiDeviceInfo hdmiDeviceInfo : this.mHdmiDeviceList) {
                    pw.println(hdmiDeviceInfo);
                }
                pw.decreaseIndent();
                pw.println("mHardwareInputIdMap: deviceId -> inputId");
                pw.increaseIndent();
                for (int i2 = 0; i2 < this.mHardwareInputIdMap.size(); i2++) {
                    pw.println(this.mHardwareInputIdMap.keyAt(i2) + ": " + this.mHardwareInputIdMap.valueAt(i2));
                }
                pw.decreaseIndent();
                pw.println("mHdmiInputIdMap: id -> inputId");
                pw.increaseIndent();
                for (int i3 = 0; i3 < this.mHdmiInputIdMap.size(); i3++) {
                    pw.println(this.mHdmiInputIdMap.keyAt(i3) + ": " + this.mHdmiInputIdMap.valueAt(i3));
                }
                pw.decreaseIndent();
                pw.println("mInputMap: inputId -> inputInfo");
                pw.increaseIndent();
                for (Map.Entry<String, TvInputInfo> entry : this.mInputMap.entrySet()) {
                    pw.println(entry.getKey() + ": " + entry.getValue());
                }
                pw.decreaseIndent();
                pw.decreaseIndent();
            }
        }
    }

    /* access modifiers changed from: private */
    public class Connection implements IBinder.DeathRecipient {
        private ITvInputHardwareCallback mCallback;
        private Integer mCallingUid = null;
        private TvStreamConfig[] mConfigs = null;
        private TvInputHardwareImpl mHardware = null;
        private final TvInputHardwareInfo mHardwareInfo;
        private TvInputInfo mInfo;
        private Runnable mOnFirstFrameCaptured;
        private Integer mResolvedUserId = null;

        public Connection(TvInputHardwareInfo hardwareInfo) {
            this.mHardwareInfo = hardwareInfo;
        }

        public void resetLocked(TvInputHardwareImpl hardware, ITvInputHardwareCallback callback, TvInputInfo info, Integer callingUid, Integer resolvedUserId) {
            ITvInputHardwareCallback iTvInputHardwareCallback;
            if (this.mHardware != null) {
                try {
                    this.mCallback.onReleased();
                } catch (RemoteException e) {
                    Slog.e(TvInputHardwareManager.TAG, "error in Connection::resetLocked", e);
                }
                this.mHardware.release();
            }
            this.mHardware = hardware;
            this.mCallback = callback;
            this.mInfo = info;
            this.mCallingUid = callingUid;
            this.mResolvedUserId = resolvedUserId;
            this.mOnFirstFrameCaptured = null;
            if (this.mHardware != null && (iTvInputHardwareCallback = this.mCallback) != null) {
                try {
                    iTvInputHardwareCallback.onStreamConfigChanged(getConfigsLocked());
                } catch (RemoteException e2) {
                    Slog.e(TvInputHardwareManager.TAG, "error in Connection::resetLocked", e2);
                }
            }
        }

        public void updateConfigsLocked(TvStreamConfig[] configs) {
            this.mConfigs = configs;
        }

        public TvInputHardwareInfo getHardwareInfoLocked() {
            return this.mHardwareInfo;
        }

        public TvInputInfo getInfoLocked() {
            return this.mInfo;
        }

        public ITvInputHardware getHardwareLocked() {
            return this.mHardware;
        }

        public TvInputHardwareImpl getHardwareImplLocked() {
            return this.mHardware;
        }

        public ITvInputHardwareCallback getCallbackLocked() {
            return this.mCallback;
        }

        public TvStreamConfig[] getConfigsLocked() {
            return this.mConfigs;
        }

        public Integer getCallingUidLocked() {
            return this.mCallingUid;
        }

        public Integer getResolvedUserIdLocked() {
            return this.mResolvedUserId;
        }

        public void setOnFirstFrameCapturedLocked(Runnable runnable) {
            this.mOnFirstFrameCaptured = runnable;
        }

        public Runnable getOnFirstFrameCapturedLocked() {
            return this.mOnFirstFrameCaptured;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (TvInputHardwareManager.this.mLock) {
                resetLocked(null, null, null, null, null);
            }
        }

        @Override // java.lang.Object
        public String toString() {
            return "Connection{ mHardwareInfo: " + this.mHardwareInfo + ", mInfo: " + this.mInfo + ", mCallback: " + this.mCallback + ", mConfigs: " + Arrays.toString(this.mConfigs) + ", mCallingUid: " + this.mCallingUid + ", mResolvedUserId: " + this.mResolvedUserId + " }";
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getConfigsLengthLocked() {
            TvStreamConfig[] tvStreamConfigArr = this.mConfigs;
            if (tvStreamConfigArr == null) {
                return 0;
            }
            return tvStreamConfigArr.length;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getInputStateLocked() {
            int cableConnectionStatus;
            if (getConfigsLengthLocked() <= 0 && (cableConnectionStatus = this.mHardwareInfo.getCableConnectionStatus()) != 1) {
                return cableConnectionStatus != 2 ? 1 : 2;
            }
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public class TvInputHardwareImpl extends ITvInputHardware.Stub {
        private TvStreamConfig mActiveConfig = null;
        private final AudioManager.OnAudioPortUpdateListener mAudioListener = new AudioManager.OnAudioPortUpdateListener() {
            /* class com.android.server.tv.TvInputHardwareManager.TvInputHardwareImpl.AnonymousClass1 */

            public void onAudioPortListUpdate(AudioPort[] portList) {
                synchronized (TvInputHardwareImpl.this.mImplLock) {
                    TvInputHardwareImpl.this.updateAudioConfigLocked();
                }
            }

            public void onAudioPatchListUpdate(AudioPatch[] patchList) {
            }

            public void onServiceDied() {
                synchronized (TvInputHardwareImpl.this.mImplLock) {
                    TvInputHardwareImpl.this.mAudioSource = null;
                    TvInputHardwareImpl.this.mAudioSink.clear();
                    if (TvInputHardwareImpl.this.mAudioPatch != null) {
                        AudioManager unused = TvInputHardwareManager.this.mAudioManager;
                        AudioManager.releaseAudioPatch(TvInputHardwareImpl.this.mAudioPatch);
                        TvInputHardwareImpl.this.mAudioPatch = null;
                    }
                }
            }
        };
        private AudioPatch mAudioPatch = null;
        private List<AudioDevicePort> mAudioSink = new ArrayList();
        private AudioDevicePort mAudioSource;
        private float mCommittedVolume = -1.0f;
        private int mDesiredChannelMask = 1;
        private int mDesiredFormat = 1;
        private int mDesiredSamplingRate = 0;
        private final Object mImplLock = new Object();
        private final TvInputHardwareInfo mInfo;
        private String mOverrideAudioAddress = "";
        private int mOverrideAudioType = 0;
        private boolean mReleased = false;
        private float mSourceVolume = 0.0f;

        public TvInputHardwareImpl(TvInputHardwareInfo info) {
            this.mInfo = info;
            TvInputHardwareManager.this.mAudioManager.registerAudioPortUpdateListener(this.mAudioListener);
            if (this.mInfo.getAudioType() != 0) {
                this.mAudioSource = findAudioDevicePort(this.mInfo.getAudioType(), this.mInfo.getAudioAddress());
                findAudioSinkFromAudioPolicy(this.mAudioSink);
            }
        }

        private void findAudioSinkFromAudioPolicy(List<AudioDevicePort> sinks) {
            sinks.clear();
            ArrayList<AudioDevicePort> devicePorts = new ArrayList<>();
            AudioManager unused = TvInputHardwareManager.this.mAudioManager;
            if (AudioManager.listAudioDevicePorts(devicePorts) == 0) {
                int sinkDevice = TvInputHardwareManager.this.mAudioManager.getDevicesForStream(3);
                Iterator<AudioDevicePort> it = devicePorts.iterator();
                while (it.hasNext()) {
                    AudioDevicePort port = it.next();
                    if ((port.type() & sinkDevice) != 0 && (port.type() & Integer.MIN_VALUE) == 0) {
                        sinks.add(port);
                    }
                }
            }
        }

        private AudioDevicePort findAudioDevicePort(int type, String address) {
            if (type == 0) {
                return null;
            }
            ArrayList<AudioDevicePort> devicePorts = new ArrayList<>();
            AudioManager unused = TvInputHardwareManager.this.mAudioManager;
            if (AudioManager.listAudioDevicePorts(devicePorts) != 0) {
                return null;
            }
            Iterator<AudioDevicePort> it = devicePorts.iterator();
            while (it.hasNext()) {
                AudioDevicePort port = it.next();
                if (port.type() == type && port.address().equals(address)) {
                    return port;
                }
            }
            return null;
        }

        public void release() {
            synchronized (this.mImplLock) {
                TvInputHardwareManager.this.mAudioManager.unregisterAudioPortUpdateListener(this.mAudioListener);
                if (this.mAudioPatch != null) {
                    AudioManager unused = TvInputHardwareManager.this.mAudioManager;
                    AudioManager.releaseAudioPatch(this.mAudioPatch);
                    this.mAudioPatch = null;
                }
                this.mReleased = true;
            }
        }

        public boolean setSurface(Surface surface, TvStreamConfig config) throws RemoteException {
            synchronized (this.mImplLock) {
                if (!this.mReleased) {
                    int result = 0;
                    boolean z = true;
                    if (surface == null) {
                        if (this.mActiveConfig == null) {
                            return true;
                        }
                        result = TvInputHardwareManager.this.mHal.removeStream(this.mInfo.getDeviceId(), this.mActiveConfig);
                        this.mActiveConfig = null;
                    } else if (config == null) {
                        return false;
                    } else {
                        if (!(this.mActiveConfig == null || config.equals(this.mActiveConfig) || (result = TvInputHardwareManager.this.mHal.removeStream(this.mInfo.getDeviceId(), this.mActiveConfig)) == 0)) {
                            this.mActiveConfig = null;
                        }
                        if (result == 0 && (result = TvInputHardwareManager.this.mHal.addOrUpdateStream(this.mInfo.getDeviceId(), surface, config)) == 0) {
                            this.mActiveConfig = config;
                        }
                    }
                    updateAudioConfigLocked();
                    if (result != 0) {
                        z = false;
                    }
                    return z;
                }
                throw new IllegalStateException("Device already released.");
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updateAudioConfigLocked() {
            int gainValue;
            boolean sinkUpdated = updateAudioSinkLocked();
            boolean sourceUpdated = updateAudioSourceLocked();
            if (this.mAudioSource != null && !this.mAudioSink.isEmpty()) {
                if (this.mActiveConfig != null) {
                    TvInputHardwareManager.this.updateVolume();
                    float volume = this.mSourceVolume * TvInputHardwareManager.this.getMediaStreamVolume();
                    AudioGainConfig sourceGainConfig = null;
                    int i = 1;
                    if (this.mAudioSource.gains().length > 0 && volume != this.mCommittedVolume) {
                        AudioGain sourceGain = null;
                        AudioGain[] gains = this.mAudioSource.gains();
                        int length = gains.length;
                        int i2 = 0;
                        while (true) {
                            if (i2 >= length) {
                                break;
                            }
                            AudioGain gain = gains[i2];
                            if ((gain.mode() & 1) != 0) {
                                sourceGain = gain;
                                break;
                            }
                            i2++;
                        }
                        if (sourceGain != null) {
                            int steps = (sourceGain.maxValue() - sourceGain.minValue()) / sourceGain.stepValue();
                            int gainValue2 = sourceGain.minValue();
                            if (volume < 1.0f) {
                                gainValue = gainValue2 + (sourceGain.stepValue() * ((int) (((double) (((float) steps) * volume)) + 0.5d)));
                            } else {
                                gainValue = sourceGain.maxValue();
                            }
                            sourceGainConfig = sourceGain.buildConfig(1, sourceGain.channelMask(), new int[]{gainValue}, 0);
                        } else {
                            Slog.w(TvInputHardwareManager.TAG, "No audio source gain with MODE_JOINT support exists.");
                        }
                    }
                    AudioPortConfig sourceConfig = this.mAudioSource.activeConfig();
                    List<AudioPortConfig> sinkConfigs = new ArrayList<>();
                    AudioPatch[] audioPatchArray = {this.mAudioPatch};
                    boolean shouldRecreateAudioPatch = sourceUpdated || sinkUpdated;
                    for (AudioDevicePort audioSink : this.mAudioSink) {
                        AudioPortConfig sinkConfig = audioSink.activeConfig();
                        int sinkSamplingRate = this.mDesiredSamplingRate;
                        int sinkChannelMask = this.mDesiredChannelMask;
                        int sinkFormat = this.mDesiredFormat;
                        if (sinkConfig != null) {
                            if (sinkSamplingRate == 0) {
                                sinkSamplingRate = sinkConfig.samplingRate();
                            }
                            if (sinkChannelMask == i) {
                                sinkChannelMask = sinkConfig.channelMask();
                            }
                            if (sinkFormat == i) {
                                sinkFormat = sinkConfig.format();
                            }
                        }
                        if (sinkConfig == null || sinkConfig.samplingRate() != sinkSamplingRate || sinkConfig.channelMask() != sinkChannelMask || sinkConfig.format() != sinkFormat) {
                            if (!TvInputHardwareManager.intArrayContains(audioSink.samplingRates(), sinkSamplingRate) && audioSink.samplingRates().length > 0) {
                                sinkSamplingRate = audioSink.samplingRates()[0];
                            }
                            if (!TvInputHardwareManager.intArrayContains(audioSink.channelMasks(), sinkChannelMask)) {
                                sinkChannelMask = 1;
                            }
                            if (!TvInputHardwareManager.intArrayContains(audioSink.formats(), sinkFormat)) {
                                sinkFormat = 1;
                            }
                            sinkConfig = audioSink.buildConfig(sinkSamplingRate, sinkChannelMask, sinkFormat, (AudioGainConfig) null);
                            shouldRecreateAudioPatch = true;
                        }
                        sinkConfigs.add(sinkConfig);
                        i = 1;
                    }
                    AudioPortConfig sinkConfig2 = sinkConfigs.get(0);
                    if (sourceConfig == null || sourceGainConfig != null) {
                        int sourceSamplingRate = 0;
                        if (TvInputHardwareManager.intArrayContains(this.mAudioSource.samplingRates(), sinkConfig2.samplingRate())) {
                            sourceSamplingRate = sinkConfig2.samplingRate();
                        } else if (this.mAudioSource.samplingRates().length > 0) {
                            sourceSamplingRate = this.mAudioSource.samplingRates()[0];
                        }
                        int sourceChannelMask = 1;
                        int[] channelMasks = this.mAudioSource.channelMasks();
                        int length2 = channelMasks.length;
                        int i3 = 0;
                        while (true) {
                            if (i3 >= length2) {
                                break;
                            }
                            int inChannelMask = channelMasks[i3];
                            if (AudioFormat.channelCountFromOutChannelMask(sinkConfig2.channelMask()) == AudioFormat.channelCountFromInChannelMask(inChannelMask)) {
                                sourceChannelMask = inChannelMask;
                                break;
                            }
                            i3++;
                            sinkUpdated = sinkUpdated;
                            sourceUpdated = sourceUpdated;
                        }
                        int sourceFormat = 1;
                        if (TvInputHardwareManager.intArrayContains(this.mAudioSource.formats(), sinkConfig2.format())) {
                            sourceFormat = sinkConfig2.format();
                        }
                        sourceConfig = this.mAudioSource.buildConfig(sourceSamplingRate, sourceChannelMask, sourceFormat, sourceGainConfig);
                        shouldRecreateAudioPatch = true;
                    }
                    if (shouldRecreateAudioPatch) {
                        this.mCommittedVolume = volume;
                        if (this.mAudioPatch != null) {
                            AudioManager unused = TvInputHardwareManager.this.mAudioManager;
                            AudioManager.releaseAudioPatch(this.mAudioPatch);
                        }
                        AudioManager unused2 = TvInputHardwareManager.this.mAudioManager;
                        AudioManager.createAudioPatch(audioPatchArray, new AudioPortConfig[]{sourceConfig}, (AudioPortConfig[]) sinkConfigs.toArray(new AudioPortConfig[sinkConfigs.size()]));
                        this.mAudioPatch = audioPatchArray[0];
                        if (sourceGainConfig != null) {
                            AudioManager unused3 = TvInputHardwareManager.this.mAudioManager;
                            AudioManager.setAudioPortGain(this.mAudioSource, sourceGainConfig);
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
            if (this.mAudioPatch != null) {
                AudioManager unused4 = TvInputHardwareManager.this.mAudioManager;
                AudioManager.releaseAudioPatch(this.mAudioPatch);
                this.mAudioPatch = null;
            }
        }

        public void setStreamVolume(float volume) throws RemoteException {
            synchronized (this.mImplLock) {
                if (!this.mReleased) {
                    this.mSourceVolume = volume;
                    updateAudioConfigLocked();
                } else {
                    throw new IllegalStateException("Device already released.");
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean startCapture(Surface surface, TvStreamConfig config) {
            synchronized (this.mImplLock) {
                boolean z = false;
                if (this.mReleased) {
                    return false;
                }
                if (surface != null) {
                    if (config != null) {
                        if (config.getType() != 2) {
                            return false;
                        }
                        if (TvInputHardwareManager.this.mHal.addOrUpdateStream(this.mInfo.getDeviceId(), surface, config) == 0) {
                            z = true;
                        }
                        return z;
                    }
                }
                return false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean stopCapture(TvStreamConfig config) {
            synchronized (this.mImplLock) {
                boolean z = false;
                if (this.mReleased) {
                    return false;
                }
                if (config == null) {
                    return false;
                }
                if (TvInputHardwareManager.this.mHal.removeStream(this.mInfo.getDeviceId(), config) == 0) {
                    z = true;
                }
                return z;
            }
        }

        private boolean updateAudioSourceLocked() {
            if (this.mInfo.getAudioType() == 0) {
                return false;
            }
            AudioDevicePort previousSource = this.mAudioSource;
            this.mAudioSource = findAudioDevicePort(this.mInfo.getAudioType(), this.mInfo.getAudioAddress());
            AudioDevicePort audioDevicePort = this.mAudioSource;
            if (audioDevicePort == null) {
                if (previousSource != null) {
                    return true;
                }
                return false;
            } else if (!audioDevicePort.equals(previousSource)) {
                return true;
            } else {
                return false;
            }
        }

        private boolean updateAudioSinkLocked() {
            if (this.mInfo.getAudioType() == 0) {
                return false;
            }
            List<AudioDevicePort> previousSink = this.mAudioSink;
            this.mAudioSink = new ArrayList();
            int i = this.mOverrideAudioType;
            if (i == 0) {
                findAudioSinkFromAudioPolicy(this.mAudioSink);
            } else {
                AudioDevicePort audioSink = findAudioDevicePort(i, this.mOverrideAudioAddress);
                if (audioSink != null) {
                    this.mAudioSink.add(audioSink);
                }
            }
            if (this.mAudioSink.size() != previousSink.size()) {
                return true;
            }
            previousSink.removeAll(this.mAudioSink);
            return !previousSink.isEmpty();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleAudioSinkUpdated() {
            synchronized (this.mImplLock) {
                updateAudioConfigLocked();
            }
        }

        public void overrideAudioSink(int audioType, String audioAddress, int samplingRate, int channelMask, int format) {
            synchronized (this.mImplLock) {
                this.mOverrideAudioType = audioType;
                this.mOverrideAudioAddress = audioAddress;
                this.mDesiredSamplingRate = samplingRate;
                this.mDesiredChannelMask = channelMask;
                this.mDesiredFormat = format;
                updateAudioConfigLocked();
            }
        }

        public void onMediaStreamVolumeChanged() {
            synchronized (this.mImplLock) {
                updateAudioConfigLocked();
            }
        }
    }

    private class ListenerHandler extends Handler {
        private static final int HARDWARE_DEVICE_ADDED = 2;
        private static final int HARDWARE_DEVICE_REMOVED = 3;
        private static final int HDMI_DEVICE_ADDED = 4;
        private static final int HDMI_DEVICE_REMOVED = 5;
        private static final int HDMI_DEVICE_UPDATED = 6;
        private static final int STATE_CHANGED = 1;

        private ListenerHandler() {
        }

        @Override // android.os.Handler
        public final void handleMessage(Message msg) {
            String inputId;
            switch (msg.what) {
                case 1:
                    int state = msg.arg1;
                    TvInputHardwareManager.this.mListener.onStateChanged((String) msg.obj, state);
                    return;
                case 2:
                    TvInputHardwareManager.this.mListener.onHardwareDeviceAdded((TvInputHardwareInfo) msg.obj);
                    return;
                case 3:
                    TvInputHardwareManager.this.mListener.onHardwareDeviceRemoved((TvInputHardwareInfo) msg.obj);
                    return;
                case 4:
                    TvInputHardwareManager.this.mListener.onHdmiDeviceAdded((HdmiDeviceInfo) msg.obj);
                    return;
                case 5:
                    TvInputHardwareManager.this.mListener.onHdmiDeviceRemoved((HdmiDeviceInfo) msg.obj);
                    return;
                case 6:
                    HdmiDeviceInfo info = (HdmiDeviceInfo) msg.obj;
                    synchronized (TvInputHardwareManager.this.mLock) {
                        inputId = (String) TvInputHardwareManager.this.mHdmiInputIdMap.get(info.getId());
                    }
                    if (inputId != null) {
                        TvInputHardwareManager.this.mListener.onHdmiDeviceUpdated(inputId, info);
                        return;
                    } else {
                        Slog.w(TvInputHardwareManager.TAG, "Could not resolve input ID matching the device info; ignoring.");
                        return;
                    }
                default:
                    String str = TvInputHardwareManager.TAG;
                    Slog.w(str, "Unhandled message: " + msg);
                    return;
            }
        }
    }

    private final class HdmiHotplugEventListener extends IHdmiHotplugEventListener.Stub {
        private HdmiHotplugEventListener() {
        }

        public void onReceived(HdmiHotplugEvent event) {
            int state;
            synchronized (TvInputHardwareManager.this.mLock) {
                TvInputHardwareManager.this.mHdmiStateMap.put(event.getPort(), event.isConnected());
                TvInputHardwareInfo hardwareInfo = TvInputHardwareManager.this.findHardwareInfoForHdmiPortLocked(event.getPort());
                if (hardwareInfo != null) {
                    String inputId = (String) TvInputHardwareManager.this.mHardwareInputIdMap.get(hardwareInfo.getDeviceId());
                    if (inputId != null) {
                        if (event.isConnected()) {
                            state = 0;
                        } else {
                            state = 1;
                        }
                        TvInputHardwareManager.this.mHandler.obtainMessage(1, state, 0, inputId).sendToTarget();
                    }
                }
            }
        }
    }

    private final class HdmiDeviceEventListener extends IHdmiDeviceEventListener.Stub {
        private HdmiDeviceEventListener() {
        }

        public void onStatusChanged(HdmiDeviceInfo deviceInfo, int status) {
            if (deviceInfo.isSourceType()) {
                synchronized (TvInputHardwareManager.this.mLock) {
                    int messageType = 0;
                    Object obj = null;
                    if (status != 1) {
                        if (status == 2) {
                            if (!TvInputHardwareManager.this.mHdmiDeviceList.remove(findHdmiDeviceInfo(deviceInfo.getId()))) {
                                String str = TvInputHardwareManager.TAG;
                                Slog.w(str, "The list doesn't contain " + deviceInfo + "; ignoring.");
                                return;
                            }
                            messageType = 5;
                            obj = deviceInfo;
                        } else if (status == 3) {
                            try {
                                if (!TvInputHardwareManager.this.mHdmiDeviceList.remove(findHdmiDeviceInfo(deviceInfo.getId()))) {
                                    String str2 = TvInputHardwareManager.TAG;
                                    Slog.w(str2, "The list doesn't contain " + deviceInfo + "; ignoring.");
                                    return;
                                }
                                TvInputHardwareManager.this.mHdmiDeviceList.add(deviceInfo);
                                messageType = 6;
                                obj = deviceInfo;
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                    } else if (findHdmiDeviceInfo(deviceInfo.getId()) == null) {
                        TvInputHardwareManager.this.mHdmiDeviceList.add(deviceInfo);
                        messageType = 4;
                        obj = deviceInfo;
                    } else {
                        String str3 = TvInputHardwareManager.TAG;
                        Slog.w(str3, "The list already contains " + deviceInfo + "; ignoring.");
                        return;
                    }
                    Message msg = TvInputHardwareManager.this.mHandler.obtainMessage(messageType, 0, 0, obj);
                    if (TvInputHardwareManager.this.findHardwareInfoForHdmiPortLocked(deviceInfo.getPortId()) != null) {
                        msg.sendToTarget();
                    } else {
                        TvInputHardwareManager.this.mPendingHdmiDeviceEvents.add(msg);
                    }
                }
            }
        }

        private HdmiDeviceInfo findHdmiDeviceInfo(int id) {
            for (HdmiDeviceInfo info : TvInputHardwareManager.this.mHdmiDeviceList) {
                if (info.getId() == id) {
                    return info;
                }
            }
            return null;
        }
    }

    private final class HdmiSystemAudioModeChangeListener extends IHdmiSystemAudioModeChangeListener.Stub {
        private HdmiSystemAudioModeChangeListener() {
        }

        public void onStatusChanged(boolean enabled) throws RemoteException {
            synchronized (TvInputHardwareManager.this.mLock) {
                for (int i = 0; i < TvInputHardwareManager.this.mConnections.size(); i++) {
                    TvInputHardwareImpl impl = ((Connection) TvInputHardwareManager.this.mConnections.valueAt(i)).getHardwareImplLocked();
                    if (impl != null) {
                        impl.handleAudioSinkUpdated();
                    }
                }
            }
        }
    }
}
