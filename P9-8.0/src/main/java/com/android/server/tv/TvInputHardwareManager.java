package com.android.server.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.HdmiHotplugEvent;
import android.hardware.hdmi.IHdmiControlService;
import android.hardware.hdmi.IHdmiDeviceEventListener;
import android.hardware.hdmi.IHdmiDeviceEventListener.Stub;
import android.hardware.hdmi.IHdmiHotplugEventListener;
import android.hardware.hdmi.IHdmiSystemAudioModeChangeListener;
import android.media.AudioDevicePort;
import android.media.AudioFormat;
import android.media.AudioGain;
import android.media.AudioGainConfig;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioPortUpdateListener;
import android.media.AudioPatch;
import android.media.AudioPort;
import android.media.AudioPortConfig;
import android.media.tv.ITvInputHardware;
import android.media.tv.ITvInputHardwareCallback;
import android.media.tv.TvInputHardwareInfo;
import android.media.tv.TvInputInfo;
import android.media.tv.TvStreamConfig;
import android.os.Handler;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.Surface;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.tv.TvInputHal.Callback;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class TvInputHardwareManager implements Callback {
    private static final String TAG = TvInputHardwareManager.class.getSimpleName();
    private final AudioManager mAudioManager;
    private final SparseArray<Connection> mConnections = new SparseArray();
    private final Context mContext;
    private int mCurrentIndex = 0;
    private int mCurrentMaxIndex = 0;
    private final TvInputHal mHal = new TvInputHal(this);
    private final Handler mHandler = new ListenerHandler(this, null);
    private final SparseArray<String> mHardwareInputIdMap = new SparseArray();
    private final List<TvInputHardwareInfo> mHardwareList = new ArrayList();
    private final IHdmiDeviceEventListener mHdmiDeviceEventListener = new HdmiDeviceEventListener(this, null);
    private final List<HdmiDeviceInfo> mHdmiDeviceList = new LinkedList();
    private final IHdmiHotplugEventListener mHdmiHotplugEventListener = new HdmiHotplugEventListener(this, null);
    private final SparseArray<String> mHdmiInputIdMap = new SparseArray();
    private final SparseBooleanArray mHdmiStateMap = new SparseBooleanArray();
    private final IHdmiSystemAudioModeChangeListener mHdmiSystemAudioModeChangeListener = new HdmiSystemAudioModeChangeListener(this, null);
    private final Map<String, TvInputInfo> mInputMap = new ArrayMap();
    private final Listener mListener;
    private final Object mLock = new Object();
    private final List<Message> mPendingHdmiDeviceEvents = new LinkedList();
    private final BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            TvInputHardwareManager.this.handleVolumeChange(context, intent);
        }
    };

    private class Connection implements DeathRecipient {
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
            if (this.mHardware != null && this.mCallback != null) {
                try {
                    this.mCallback.onStreamConfigChanged(getConfigsLocked());
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

        public void binderDied() {
            synchronized (TvInputHardwareManager.this.mLock) {
                resetLocked(null, null, null, null, null);
            }
        }

        public String toString() {
            return "Connection{ mHardwareInfo: " + this.mHardwareInfo + ", mInfo: " + this.mInfo + ", mCallback: " + this.mCallback + ", mConfigs: " + Arrays.toString(this.mConfigs) + ", mCallingUid: " + this.mCallingUid + ", mResolvedUserId: " + this.mResolvedUserId + " }";
        }

        private int getConfigsLengthLocked() {
            return this.mConfigs == null ? 0 : this.mConfigs.length;
        }

        private int getInputStateLocked() {
            if (getConfigsLengthLocked() > 0) {
                return 0;
            }
            switch (this.mHardwareInfo.getCableConnectionStatus()) {
                case 1:
                    return 0;
                case 2:
                    return 2;
                default:
                    return 1;
            }
        }
    }

    private final class HdmiDeviceEventListener extends Stub {
        /* synthetic */ HdmiDeviceEventListener(TvInputHardwareManager this$0, HdmiDeviceEventListener -this1) {
            this();
        }

        private HdmiDeviceEventListener() {
        }

        /* JADX WARNING: Missing block: B:12:0x002f, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onStatusChanged(HdmiDeviceInfo deviceInfo, int status) {
            if (deviceInfo.isSourceType()) {
                synchronized (TvInputHardwareManager.this.mLock) {
                    int messageType = 0;
                    Object obj = null;
                    HdmiDeviceInfo obj2;
                    switch (status) {
                        case 1:
                            if (findHdmiDeviceInfo(deviceInfo.getId()) == null) {
                                TvInputHardwareManager.this.mHdmiDeviceList.add(deviceInfo);
                                messageType = 4;
                                obj2 = deviceInfo;
                            } else {
                                Slog.w(TvInputHardwareManager.TAG, "The list already contains " + deviceInfo + "; ignoring.");
                                return;
                            }
                        case 2:
                            if (TvInputHardwareManager.this.mHdmiDeviceList.remove(findHdmiDeviceInfo(deviceInfo.getId()))) {
                                messageType = 5;
                                obj2 = deviceInfo;
                            } else {
                                Slog.w(TvInputHardwareManager.TAG, "The list doesn't contain " + deviceInfo + "; ignoring.");
                                return;
                            }
                        case 3:
                            if (TvInputHardwareManager.this.mHdmiDeviceList.remove(findHdmiDeviceInfo(deviceInfo.getId()))) {
                                TvInputHardwareManager.this.mHdmiDeviceList.add(deviceInfo);
                                messageType = 6;
                                obj2 = deviceInfo;
                            } else {
                                Slog.w(TvInputHardwareManager.TAG, "The list doesn't contain " + deviceInfo + "; ignoring.");
                                return;
                            }
                        default:
                            Message msg = TvInputHardwareManager.this.mHandler.obtainMessage(messageType, 0, 0, obj2);
                            if (TvInputHardwareManager.this.findHardwareInfoForHdmiPortLocked(deviceInfo.getPortId()) == null) {
                                TvInputHardwareManager.this.mPendingHdmiDeviceEvents.add(msg);
                                break;
                            } else {
                                msg.sendToTarget();
                                break;
                            }
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

    private final class HdmiHotplugEventListener extends IHdmiHotplugEventListener.Stub {
        /* synthetic */ HdmiHotplugEventListener(TvInputHardwareManager this$0, HdmiHotplugEventListener -this1) {
            this();
        }

        private HdmiHotplugEventListener() {
        }

        public void onReceived(HdmiHotplugEvent event) {
            synchronized (TvInputHardwareManager.this.mLock) {
                TvInputHardwareManager.this.mHdmiStateMap.put(event.getPort(), event.isConnected());
                TvInputHardwareInfo hardwareInfo = TvInputHardwareManager.this.findHardwareInfoForHdmiPortLocked(event.getPort());
                if (hardwareInfo == null) {
                    return;
                }
                String inputId = (String) TvInputHardwareManager.this.mHardwareInputIdMap.get(hardwareInfo.getDeviceId());
                if (inputId == null) {
                    return;
                }
                int state;
                if (event.isConnected()) {
                    state = 0;
                } else {
                    state = 1;
                }
                TvInputHardwareManager.this.mHandler.obtainMessage(1, state, 0, inputId).sendToTarget();
            }
        }
    }

    private final class HdmiSystemAudioModeChangeListener extends IHdmiSystemAudioModeChangeListener.Stub {
        /* synthetic */ HdmiSystemAudioModeChangeListener(TvInputHardwareManager this$0, HdmiSystemAudioModeChangeListener -this1) {
            this();
        }

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

    interface Listener {
        void onHardwareDeviceAdded(TvInputHardwareInfo tvInputHardwareInfo);

        void onHardwareDeviceRemoved(TvInputHardwareInfo tvInputHardwareInfo);

        void onHdmiDeviceAdded(HdmiDeviceInfo hdmiDeviceInfo);

        void onHdmiDeviceRemoved(HdmiDeviceInfo hdmiDeviceInfo);

        void onHdmiDeviceUpdated(String str, HdmiDeviceInfo hdmiDeviceInfo);

        void onStateChanged(String str, int i);
    }

    private class ListenerHandler extends Handler {
        private static final int HARDWARE_DEVICE_ADDED = 2;
        private static final int HARDWARE_DEVICE_REMOVED = 3;
        private static final int HDMI_DEVICE_ADDED = 4;
        private static final int HDMI_DEVICE_REMOVED = 5;
        private static final int HDMI_DEVICE_UPDATED = 6;
        private static final int STATE_CHANGED = 1;

        /* synthetic */ ListenerHandler(TvInputHardwareManager this$0, ListenerHandler -this1) {
            this();
        }

        private ListenerHandler() {
        }

        public final void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    TvInputHardwareManager.this.mListener.onStateChanged(msg.obj, msg.arg1);
                    return;
                case 2:
                    TvInputHardwareManager.this.mListener.onHardwareDeviceAdded(msg.obj);
                    return;
                case 3:
                    TvInputHardwareManager.this.mListener.onHardwareDeviceRemoved((TvInputHardwareInfo) msg.obj);
                    return;
                case 4:
                    TvInputHardwareManager.this.mListener.onHdmiDeviceAdded(msg.obj);
                    return;
                case 5:
                    TvInputHardwareManager.this.mListener.onHdmiDeviceRemoved((HdmiDeviceInfo) msg.obj);
                    return;
                case 6:
                    String inputId;
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
                    Slog.w(TvInputHardwareManager.TAG, "Unhandled message: " + msg);
                    return;
            }
        }
    }

    private class TvInputHardwareImpl extends ITvInputHardware.Stub {
        private TvStreamConfig mActiveConfig = null;
        private final OnAudioPortUpdateListener mAudioListener = new OnAudioPortUpdateListener() {
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
                        TvInputHardwareManager.this.mAudioManager;
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
            ArrayList<AudioDevicePort> devicePorts = new ArrayList();
            TvInputHardwareManager.this.mAudioManager;
            if (AudioManager.listAudioDevicePorts(devicePorts) == 0) {
                int sinkDevice = TvInputHardwareManager.this.mAudioManager.getDevicesForStream(3);
                for (AudioDevicePort port : devicePorts) {
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
            ArrayList<AudioDevicePort> devicePorts = new ArrayList();
            TvInputHardwareManager.this.mAudioManager;
            if (AudioManager.listAudioDevicePorts(devicePorts) != 0) {
                return null;
            }
            for (AudioDevicePort port : devicePorts) {
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
                    TvInputHardwareManager.this.mAudioManager;
                    AudioManager.releaseAudioPatch(this.mAudioPatch);
                    this.mAudioPatch = null;
                }
                this.mReleased = true;
            }
        }

        /* JADX WARNING: Missing block: B:19:0x0037, code:
            return r1;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean setSurface(Surface surface, TvStreamConfig config) throws RemoteException {
            boolean z = true;
            synchronized (this.mImplLock) {
                if (this.mReleased) {
                    throw new IllegalStateException("Device already released.");
                }
                int result = 0;
                if (surface == null) {
                    if (this.mActiveConfig != null) {
                        result = TvInputHardwareManager.this.mHal.removeStream(this.mInfo.getDeviceId(), this.mActiveConfig);
                        this.mActiveConfig = null;
                    } else {
                        return true;
                    }
                } else if (config == null) {
                    return false;
                } else {
                    if (!(this.mActiveConfig == null || (config.equals(this.mActiveConfig) ^ 1) == 0)) {
                        result = TvInputHardwareManager.this.mHal.removeStream(this.mInfo.getDeviceId(), this.mActiveConfig);
                        if (result != 0) {
                            this.mActiveConfig = null;
                        }
                    }
                    if (result == 0) {
                        result = TvInputHardwareManager.this.mHal.addOrUpdateStream(this.mInfo.getDeviceId(), surface, config);
                        if (result == 0) {
                            this.mActiveConfig = config;
                        }
                    }
                }
                updateAudioConfigLocked();
                if (result != 0) {
                    z = false;
                }
            }
        }

        private void updateAudioConfigLocked() {
            boolean sinkUpdated = updateAudioSinkLocked();
            boolean sourceUpdated = updateAudioSourceLocked();
            if (this.mAudioSource == null || this.mAudioSink.isEmpty() || this.mActiveConfig == null) {
                if (this.mAudioPatch != null) {
                    TvInputHardwareManager.this.mAudioManager;
                    AudioManager.releaseAudioPatch(this.mAudioPatch);
                    this.mAudioPatch = null;
                }
                return;
            }
            AudioPortConfig sinkConfig;
            TvInputHardwareManager.this.updateVolume();
            float volume = this.mSourceVolume * TvInputHardwareManager.this.getMediaStreamVolume();
            AudioGainConfig sourceGainConfig = null;
            if (this.mAudioSource.gains().length > 0 && volume != this.mCommittedVolume) {
                AudioGain sourceGain = null;
                for (AudioGain gain : this.mAudioSource.gains()) {
                    if ((gain.mode() & 1) != 0) {
                        sourceGain = gain;
                        break;
                    }
                }
                if (sourceGain != null) {
                    int steps = (sourceGain.maxValue() - sourceGain.minValue()) / sourceGain.stepValue();
                    int gainValue = sourceGain.minValue();
                    if (volume < 1.0f) {
                        gainValue += sourceGain.stepValue() * ((int) (((double) (((float) steps) * volume)) + 0.5d));
                    } else {
                        gainValue = sourceGain.maxValue();
                    }
                    sourceGainConfig = sourceGain.buildConfig(1, sourceGain.channelMask(), new int[]{gainValue}, 0);
                } else {
                    Slog.w(TvInputHardwareManager.TAG, "No audio source gain with MODE_JOINT support exists.");
                }
            }
            AudioPortConfig sourceConfig = this.mAudioSource.activeConfig();
            List<AudioPortConfig> sinkConfigs = new ArrayList();
            AudioPatch[] audioPatchArray = new AudioPatch[]{this.mAudioPatch};
            boolean shouldRecreateAudioPatch = !sourceUpdated ? sinkUpdated : true;
            for (AudioDevicePort audioSink : this.mAudioSink) {
                sinkConfig = audioSink.activeConfig();
                int sinkSamplingRate = this.mDesiredSamplingRate;
                int sinkChannelMask = this.mDesiredChannelMask;
                int sinkFormat = this.mDesiredFormat;
                if (sinkConfig != null) {
                    if (sinkSamplingRate == 0) {
                        sinkSamplingRate = sinkConfig.samplingRate();
                    }
                    if (sinkChannelMask == 1) {
                        sinkChannelMask = sinkConfig.channelMask();
                    }
                    if (sinkFormat == 1) {
                        sinkChannelMask = sinkConfig.format();
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
                    sinkConfig = audioSink.buildConfig(sinkSamplingRate, sinkChannelMask, sinkFormat, null);
                    shouldRecreateAudioPatch = true;
                }
                sinkConfigs.add(sinkConfig);
            }
            sinkConfig = (AudioPortConfig) sinkConfigs.get(0);
            if (sourceConfig == null || sourceGainConfig != null) {
                int sourceSamplingRate = 0;
                if (TvInputHardwareManager.intArrayContains(this.mAudioSource.samplingRates(), sinkConfig.samplingRate())) {
                    sourceSamplingRate = sinkConfig.samplingRate();
                } else if (this.mAudioSource.samplingRates().length > 0) {
                    sourceSamplingRate = this.mAudioSource.samplingRates()[0];
                }
                int sourceChannelMask = 1;
                for (int inChannelMask : this.mAudioSource.channelMasks()) {
                    if (AudioFormat.channelCountFromOutChannelMask(sinkConfig.channelMask()) == AudioFormat.channelCountFromInChannelMask(inChannelMask)) {
                        sourceChannelMask = inChannelMask;
                        break;
                    }
                }
                int sourceFormat = 1;
                if (TvInputHardwareManager.intArrayContains(this.mAudioSource.formats(), sinkConfig.format())) {
                    sourceFormat = sinkConfig.format();
                }
                sourceConfig = this.mAudioSource.buildConfig(sourceSamplingRate, sourceChannelMask, sourceFormat, sourceGainConfig);
                shouldRecreateAudioPatch = true;
            }
            if (shouldRecreateAudioPatch) {
                this.mCommittedVolume = volume;
                if (this.mAudioPatch != null) {
                    TvInputHardwareManager.this.mAudioManager;
                    AudioManager.releaseAudioPatch(this.mAudioPatch);
                }
                TvInputHardwareManager.this.mAudioManager;
                AudioManager.createAudioPatch(audioPatchArray, new AudioPortConfig[]{sourceConfig}, (AudioPortConfig[]) sinkConfigs.toArray(new AudioPortConfig[sinkConfigs.size()]));
                this.mAudioPatch = audioPatchArray[0];
                if (sourceGainConfig != null) {
                    TvInputHardwareManager.this.mAudioManager;
                    AudioManager.setAudioPortGain(this.mAudioSource, sourceGainConfig);
                }
            }
        }

        public void setStreamVolume(float volume) throws RemoteException {
            synchronized (this.mImplLock) {
                if (this.mReleased) {
                    throw new IllegalStateException("Device already released.");
                }
                this.mSourceVolume = volume;
                updateAudioConfigLocked();
            }
        }

        public boolean dispatchKeyEventToHdmi(KeyEvent event) throws RemoteException {
            synchronized (this.mImplLock) {
                if (this.mReleased) {
                    throw new IllegalStateException("Device already released.");
                }
            }
            return this.mInfo.getType() != 9 ? false : false;
        }

        /* JADX WARNING: Missing block: B:10:0x000f, code:
            return false;
     */
        /* JADX WARNING: Missing block: B:22:0x002d, code:
            return r1;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean startCapture(Surface surface, TvStreamConfig config) {
            boolean z = false;
            synchronized (this.mImplLock) {
                if (this.mReleased) {
                    return false;
                } else if (surface == null || config == null) {
                } else if (config.getType() != 2) {
                    return false;
                } else if (TvInputHardwareManager.this.mHal.addOrUpdateStream(this.mInfo.getDeviceId(), surface, config) == 0) {
                    z = true;
                }
            }
        }

        /* JADX WARNING: Missing block: B:15:0x0022, code:
            return r1;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean stopCapture(TvStreamConfig config) {
            boolean z = false;
            synchronized (this.mImplLock) {
                if (this.mReleased) {
                    return false;
                } else if (config == null) {
                    return false;
                } else if (TvInputHardwareManager.this.mHal.removeStream(this.mInfo.getDeviceId(), config) == 0) {
                    z = true;
                }
            }
        }

        private boolean updateAudioSourceLocked() {
            boolean z = false;
            if (this.mInfo.getAudioType() == 0) {
                return false;
            }
            AudioDevicePort previousSource = this.mAudioSource;
            this.mAudioSource = findAudioDevicePort(this.mInfo.getAudioType(), this.mInfo.getAudioAddress());
            if (this.mAudioSource != null) {
                z = this.mAudioSource.equals(previousSource) ^ 1;
            } else if (previousSource != null) {
                z = true;
            }
            return z;
        }

        private boolean updateAudioSinkLocked() {
            if (this.mInfo.getAudioType() == 0) {
                return false;
            }
            List<AudioDevicePort> previousSink = this.mAudioSink;
            this.mAudioSink = new ArrayList();
            if (this.mOverrideAudioType == 0) {
                findAudioSinkFromAudioPolicy(this.mAudioSink);
            } else {
                AudioDevicePort audioSink = findAudioDevicePort(this.mOverrideAudioType, this.mOverrideAudioAddress);
                if (audioSink != null) {
                    this.mAudioSink.add(audioSink);
                }
            }
            if (this.mAudioSink.size() != previousSink.size()) {
                return true;
            }
            previousSink.removeAll(this.mAudioSink);
            return previousSink.isEmpty() ^ 1;
        }

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
            this.mHardwareList.add(((Connection) this.mConnections.valueAt(i)).getHardwareInfoLocked());
        }
    }

    public void onDeviceUnavailable(int deviceId) {
        synchronized (this.mLock) {
            Connection connection = (Connection) this.mConnections.get(deviceId);
            if (connection == null) {
                Slog.e(TAG, "onDeviceUnavailable: Cannot find a connection with " + deviceId);
                return;
            }
            connection.resetLocked(null, null, null, null, null);
            this.mConnections.remove(deviceId);
            buildHardwareListLocked();
            TvInputHardwareInfo info = connection.getHardwareInfoLocked();
            if (info.getType() == 9) {
                Iterator<HdmiDeviceInfo> it = this.mHdmiDeviceList.iterator();
                while (it.hasNext()) {
                    HdmiDeviceInfo deviceInfo = (HdmiDeviceInfo) it.next();
                    if (deviceInfo.getPortId() == info.getHdmiPortId()) {
                        this.mHandler.obtainMessage(5, 0, 0, deviceInfo).sendToTarget();
                        it.remove();
                    }
                }
            }
            this.mHandler.obtainMessage(3, 0, 0, info).sendToTarget();
        }
    }

    public void onStreamConfigurationChanged(int deviceId, TvStreamConfig[] configs) {
        Object obj = 1;
        synchronized (this.mLock) {
            Connection connection = (Connection) this.mConnections.get(deviceId);
            if (connection == null) {
                Slog.e(TAG, "StreamConfigurationChanged: Cannot find a connection with " + deviceId);
                return;
            }
            int previousConfigsLength = connection.getConfigsLengthLocked();
            connection.updateConfigsLocked(configs);
            String inputId = (String) this.mHardwareInputIdMap.get(deviceId);
            if (inputId != null) {
                Object obj2;
                if (previousConfigsLength == 0) {
                    obj2 = 1;
                } else {
                    obj2 = null;
                }
                if (connection.getConfigsLengthLocked() != 0) {
                    obj = null;
                }
                if (obj2 != obj) {
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

    /* JADX WARNING: Missing block: B:13:0x0036, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onFirstFrameCaptured(int deviceId, int streamId) {
        synchronized (this.mLock) {
            Connection connection = (Connection) this.mConnections.get(deviceId);
            if (connection == null) {
                Slog.e(TAG, "FirstFrameCaptured: Cannot find a connection with " + deviceId);
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
        if (connectionCallingUid == null || connectionResolvedUserId == null || connectionCallingUid.intValue() != callingUid || connectionResolvedUserId.intValue() != resolvedUserId) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:28:0x00c5, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addHardwareInput(int deviceId, TvInputInfo info) {
        synchronized (this.mLock) {
            String oldInputId = (String) this.mHardwareInputIdMap.get(deviceId);
            if (oldInputId != null) {
                Slog.w(TAG, "Trying to override previous registration: old = " + this.mInputMap.get(oldInputId) + ":" + deviceId + ", new = " + info + ":" + deviceId);
            }
            this.mHardwareInputIdMap.put(deviceId, info.getId());
            this.mInputMap.put(info.getId(), info);
            for (int i = 0; i < this.mHdmiStateMap.size(); i++) {
                TvInputHardwareInfo hardwareInfo = findHardwareInfoForHdmiPortLocked(this.mHdmiStateMap.keyAt(i));
                if (hardwareInfo != null) {
                    String inputId = (String) this.mHardwareInputIdMap.get(hardwareInfo.getDeviceId());
                    if (inputId != null && inputId.equals(info.getId())) {
                        int state;
                        if (this.mHdmiStateMap.valueAt(i)) {
                            state = 0;
                        } else {
                            state = 1;
                        }
                        this.mHandler.obtainMessage(1, state, 0, inputId).sendToTarget();
                        return;
                    }
                }
            }
            Connection connection = (Connection) this.mConnections.get(deviceId);
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

    private static boolean intArrayContains(int[] array, int value) {
        for (int element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    public void addHdmiInput(int id, TvInputInfo info) {
        if (info.getType() != 1007) {
            throw new IllegalArgumentException("info (" + info + ") has non-HDMI type.");
        }
        synchronized (this.mLock) {
            if (indexOfEqualValue(this.mHardwareInputIdMap, info.getParentId()) < 0) {
                throw new IllegalArgumentException("info (" + info + ") has invalid parentId.");
            }
            String oldInputId = (String) this.mHdmiInputIdMap.get(id);
            if (oldInputId != null) {
                Slog.w(TAG, "Trying to override previous registration: old = " + this.mInputMap.get(oldInputId) + ":" + id + ", new = " + info + ":" + id);
            }
            this.mHdmiInputIdMap.put(id, info.getId());
            this.mInputMap.put(info.getId(), info);
        }
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
        if (callback == null) {
            throw new NullPointerException();
        }
        synchronized (this.mLock) {
            Connection connection = (Connection) this.mConnections.get(deviceId);
            if (connection == null) {
                Slog.e(TAG, "Invalid deviceId : " + deviceId);
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
            ITvInputHardware hardwareLocked = connection.getHardwareLocked();
            return hardwareLocked;
        }
    }

    /* JADX WARNING: Missing block: B:14:0x0035, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void releaseHardware(int deviceId, ITvInputHardware hardware, int callingUid, int resolvedUserId) {
        synchronized (this.mLock) {
            Connection connection = (Connection) this.mConnections.get(deviceId);
            if (connection == null) {
                Slog.e(TAG, "Invalid deviceId : " + deviceId);
            } else if (connection.getHardwareLocked() != hardware || checkUidChangedLocked(connection, callingUid, resolvedUserId)) {
            } else {
                connection.resetLocked(null, null, null, null, null);
            }
        }
    }

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
            if (((Connection) this.mConnections.get(i)).getInfoLocked().getId().equals(inputId)) {
                return i;
            }
        }
        return -1;
    }

    public List<TvStreamConfig> getAvailableTvStreamConfigList(String inputId, int callingUid, int resolvedUserId) {
        List<TvStreamConfig> configsList = new ArrayList();
        synchronized (this.mLock) {
            int deviceId = findDeviceIdForInputIdLocked(inputId);
            if (deviceId < 0) {
                Slog.e(TAG, "Invalid inputId : " + inputId);
                return configsList;
            }
            for (TvStreamConfig config : ((Connection) this.mConnections.get(deviceId)).getConfigsLocked()) {
                if (config.getType() == 2) {
                    configsList.add(config);
                }
            }
            return configsList;
        }
    }

    /* JADX WARNING: Missing block: B:18:0x004f, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean captureFrame(String inputId, Surface surface, final TvStreamConfig config, int callingUid, int resolvedUserId) {
        synchronized (this.mLock) {
            int deviceId = findDeviceIdForInputIdLocked(inputId);
            if (deviceId < 0) {
                Slog.e(TAG, "Invalid inputId : " + inputId);
                return false;
            }
            Connection connection = (Connection) this.mConnections.get(deviceId);
            final TvInputHardwareImpl hardwareImpl = connection.getHardwareImplLocked();
            if (hardwareImpl != null) {
                Runnable runnable = connection.getOnFirstFrameCapturedLocked();
                if (runnable != null) {
                    runnable.run();
                    connection.setOnFirstFrameCapturedLocked(null);
                }
                boolean result = hardwareImpl.startCapture(surface, config);
                if (result) {
                    connection.setOnFirstFrameCapturedLocked(new Runnable() {
                        public void run() {
                            hardwareImpl.stopCapture(config);
                        }
                    });
                }
            } else {
                return false;
            }
        }
    }

    private void processPendingHdmiDeviceEventsLocked() {
        Iterator<Message> it = this.mPendingHdmiDeviceEvents.iterator();
        while (it.hasNext()) {
            Message msg = (Message) it.next();
            if (findHardwareInfoForHdmiPortLocked(msg.obj.getPortId()) != null) {
                msg.sendToTarget();
                it.remove();
            }
        }
    }

    private void updateVolume() {
        this.mCurrentMaxIndex = this.mAudioManager.getStreamMaxVolume(3);
        this.mCurrentIndex = this.mAudioManager.getStreamVolume(3);
    }

    private void handleVolumeChange(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.media.VOLUME_CHANGED_ACTION")) {
            if (intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1) == 3) {
                int index = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0);
                if (index != this.mCurrentIndex) {
                    this.mCurrentIndex = index;
                } else {
                    return;
                }
            }
            return;
        } else if (!action.equals("android.media.STREAM_MUTE_CHANGED_ACTION")) {
            Slog.w(TAG, "Unrecognized intent: " + intent);
            return;
        } else if (intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1) != 3) {
            return;
        }
        synchronized (this.mLock) {
            for (int i = 0; i < this.mConnections.size(); i++) {
                TvInputHardwareImpl hardwareImpl = ((Connection) this.mConnections.valueAt(i)).getHardwareImplLocked();
                if (hardwareImpl != null) {
                    hardwareImpl.onMediaStreamVolumeChanged();
                }
            }
        }
    }

    private float getMediaStreamVolume() {
        return ((float) this.mCurrentIndex) / ((float) this.mCurrentMaxIndex);
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            synchronized (this.mLock) {
                int i;
                String inputId;
                pw.println("TvInputHardwareManager Info:");
                pw.increaseIndent();
                pw.println("mConnections: deviceId -> Connection");
                pw.increaseIndent();
                for (i = 0; i < this.mConnections.size(); i++) {
                    Connection mConnection = (Connection) this.mConnections.valueAt(i);
                    pw.println(this.mConnections.keyAt(i) + ": " + mConnection);
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
                for (i = 0; i < this.mHardwareInputIdMap.size(); i++) {
                    inputId = (String) this.mHardwareInputIdMap.valueAt(i);
                    pw.println(this.mHardwareInputIdMap.keyAt(i) + ": " + inputId);
                }
                pw.decreaseIndent();
                pw.println("mHdmiInputIdMap: id -> inputId");
                pw.increaseIndent();
                for (i = 0; i < this.mHdmiInputIdMap.size(); i++) {
                    inputId = (String) this.mHdmiInputIdMap.valueAt(i);
                    pw.println(this.mHdmiInputIdMap.keyAt(i) + ": " + inputId);
                }
                pw.decreaseIndent();
                pw.println("mInputMap: inputId -> inputInfo");
                pw.increaseIndent();
                for (Entry<String, TvInputInfo> entry : this.mInputMap.entrySet()) {
                    pw.println(((String) entry.getKey()) + ": " + entry.getValue());
                }
                pw.decreaseIndent();
                pw.decreaseIndent();
            }
        }
    }
}
