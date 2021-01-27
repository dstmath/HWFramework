package ohos.media.audiofwk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import ohos.app.Context;
import ohos.media.audio.AudioCapturerCallback;
import ohos.media.audio.AudioCapturerConfig;
import ohos.media.audio.AudioDeviceChangeObserver;
import ohos.media.audio.AudioDeviceDescriptor;
import ohos.media.audio.AudioInterrupt;
import ohos.media.audio.AudioRendererCallback;
import ohos.media.audio.AudioRendererInfo;
import ohos.media.audioimpl.adapter.AudioPortEventHandlerAdapter;
import ohos.media.audioimpl.adapter.AudioRemoteAdapterException;
import ohos.media.audioimpl.adapter.AudioServiceAdapter;
import ohos.media.audioimpl.adapter.AudioSystemAdapter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.rpc.IRemoteObject;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.system.Parameters;
import ohos.utils.net.Uri;

public class AudioManagerImpl {
    private static final int AUDIO_KIT_VERSION = 1;
    private static final AudioPortEventHandlerAdapter AUDIO_PORT_EVENT_HANDLER_ADAPTER = new AudioPortEventHandlerAdapter();
    private static final int AUDIO_SERVICE_ID = 3001;
    private static final String DEVICE_TYPE = "ro.build.characteristics";
    private static final int INVALID_VOLUME_INDEX = -1;
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioManagerImpl.class);
    private static final String TARGET_CAR = "car";
    private static boolean isCar;
    private final AudioInterruptImpl audioInterruptImpl;
    private final AudioServiceAdapter audioServiceAdapter;
    private Context contextImpl;
    private final List<AudioDeviceChangeObserver> deviceChangeObserverList;
    private AudioDevicePortChangeObserverImpl devicePortChangeObserver;
    private boolean isPortListenerRegistered;
    private long nativeAudioManagerImpl;
    private ArrayList<AudioDeviceDescriptor> prevDeviceList;

    private native String nativeGetAudioParameters(String str);

    private native boolean nativeIsMicrophoneMute();

    private native boolean nativeSetAudioParameters(String str);

    public int getVersion() {
        return 1;
    }

    static {
        isCar = false;
        System.loadLibrary("audiomanager_jni.z");
        isCar = TARGET_CAR.equals(Parameters.get(DEVICE_TYPE));
    }

    public AudioManagerImpl(String str) {
        this.deviceChangeObserverList = new ArrayList();
        this.devicePortChangeObserver = null;
        this.isPortListenerRegistered = false;
        this.prevDeviceList = new ArrayList<>();
        this.audioServiceAdapter = new AudioServiceAdapter(str);
        this.audioInterruptImpl = new AudioInterruptImpl(this.audioServiceAdapter);
    }

    public AudioManagerImpl() {
        this.deviceChangeObserverList = new ArrayList();
        this.devicePortChangeObserver = null;
        this.isPortListenerRegistered = false;
        this.prevDeviceList = new ArrayList<>();
        this.audioServiceAdapter = new AudioServiceAdapter();
        this.audioInterruptImpl = new AudioInterruptImpl(this.audioServiceAdapter);
    }

    public AudioManagerImpl(Context context) {
        this.deviceChangeObserverList = new ArrayList();
        this.devicePortChangeObserver = null;
        this.isPortListenerRegistered = false;
        this.prevDeviceList = new ArrayList<>();
        this.audioServiceAdapter = new AudioServiceAdapter();
        this.audioInterruptImpl = new AudioInterruptImpl(this.audioServiceAdapter);
        this.contextImpl = context;
    }

    public boolean setVolume(int i, int i2) throws AudioManagerImplException {
        if (isCar) {
            LOGGER.debug("setVolume(car) streamType = %{public}d, index = %{public}d", Integer.valueOf(i), Integer.valueOf(i2));
            IRemoteObject sysAbility = SysAbilityManager.getSysAbility(3001);
            if (sysAbility == null) {
                LOGGER.error("setVolume get service failed", new Object[0]);
                return false;
            }
            AudioServiceProxy audioServiceProxy = new AudioServiceProxy(sysAbility);
            ArrayList<Integer> arrayList = new ArrayList<>();
            int volumeByStreamType = audioServiceProxy.setVolumeByStreamType(i, i2, arrayList);
            if (volumeByStreamType < 0) {
                LOGGER.error("setVolume to service failed", new Object[0]);
                return false;
            }
            Iterator<Integer> it = arrayList.iterator();
            while (it.hasNext()) {
                String str = "car_volume_type_" + it.next().intValue();
                LOGGER.debug("setVolume(car) tag = %{public}s, index = %{public}d, save to Settings", str, Integer.valueOf(volumeByStreamType));
                AudioSystemAdapter.saveIntToSettings(this.contextImpl, str, volumeByStreamType);
            }
            return true;
        }
        try {
            this.audioServiceAdapter.setVolume(i, i2);
            return true;
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public int getVolume(int i) throws AudioManagerImplException {
        if (isCar) {
            LOGGER.debug("getVolume(car) streamType = %{public}d", Integer.valueOf(i));
            int intFromSettings = AudioSystemAdapter.getIntFromSettings(this.contextImpl, "car_volume_type_" + i, -1);
            if (intFromSettings != -1) {
                LOGGER.debug("getVolume(car) from database streamType = %{public}d, index = %{public}d", Integer.valueOf(i), Integer.valueOf(intFromSettings));
                return intFromSettings;
            }
            IRemoteObject sysAbility = SysAbilityManager.getSysAbility(3001);
            if (sysAbility != null) {
                return new AudioServiceProxy(sysAbility).getVolumeByStreamType(i);
            }
            LOGGER.error("getVolume get service failed", new Object[0]);
            return -1;
        }
        try {
            return this.audioServiceAdapter.getVolume(i);
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public int getMinVolume(int i) throws AudioManagerImplException {
        if (isCar) {
            LOGGER.debug("getMinVolume(car) streamType = %{public}d", Integer.valueOf(i));
            IRemoteObject sysAbility = SysAbilityManager.getSysAbility(3001);
            if (sysAbility != null) {
                return new AudioServiceProxy(sysAbility).getMinVolumeByStreamType(i);
            }
            LOGGER.error("getMinVolume get service failed", new Object[0]);
            return -1;
        }
        try {
            return this.audioServiceAdapter.getMinVolume(i);
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public int getMaxVolume(int i) throws AudioManagerImplException {
        if (isCar) {
            LOGGER.debug("getMaxVolume(car) streamType = %{public}d", Integer.valueOf(i));
            IRemoteObject sysAbility = SysAbilityManager.getSysAbility(3001);
            if (sysAbility != null) {
                return new AudioServiceProxy(sysAbility).getMaxVolumeByStreamType(i);
            }
            LOGGER.error("getMaxVolume get service failed", new Object[0]);
            return -1;
        }
        try {
            return this.audioServiceAdapter.getMaxVolume(i);
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public void setMute(int i, boolean z) throws AudioManagerImplException {
        try {
            this.audioServiceAdapter.setMute(i, z);
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public boolean isMute(int i) throws AudioManagerImplException {
        try {
            return this.audioServiceAdapter.isMute(i);
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public void setRingerMode(int i) throws AudioManagerImplException {
        try {
            this.audioServiceAdapter.setRingerMode(i);
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public int getRingerMode() throws AudioManagerImplException {
        try {
            return this.audioServiceAdapter.getRingerMode();
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public void setSpeakerOn(boolean z) throws AudioManagerImplException {
        try {
            this.audioServiceAdapter.setSpeakerOn(z);
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public void setBlueToothOn(boolean z) throws AudioManagerImplException {
        try {
            this.audioServiceAdapter.setBlueToothOn(z);
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public boolean isSpeakerOn() throws AudioManagerImplException {
        try {
            return this.audioServiceAdapter.isSpeakerOn();
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public boolean isBluetoothOn() throws AudioManagerImplException {
        try {
            return this.audioServiceAdapter.isBluetoothOn();
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public boolean activateAudioInterrupt(AudioInterrupt audioInterrupt) {
        return this.audioInterruptImpl.activateAudioInterrupt(audioInterrupt);
    }

    public boolean deactivateAudioInterrupt(AudioInterrupt audioInterrupt) {
        return this.audioInterruptImpl.deactivateAudioInterrupt(audioInterrupt);
    }

    public boolean setAudioParameters(String str) {
        return nativeSetAudioParameters(str);
    }

    public String getAudioParameters(String str) {
        return nativeGetAudioParameters(str);
    }

    public void registerAudioCapturerCallback(AudioCapturerCallback audioCapturerCallback) {
        this.audioServiceAdapter.registerAudioCapturerCallback(audioCapturerCallback);
    }

    public void registerAudioRendererCallback(AudioRendererCallback audioRendererCallback) {
        this.audioServiceAdapter.registerAudioRendererCallback(audioRendererCallback);
    }

    public void setAudioDeviceChangeObserver(AudioDeviceChangeObserver audioDeviceChangeObserver) {
        LOGGER.debug("setAudioDeviceChangeObserver start.", new Object[0]);
        synchronized (this.deviceChangeObserverList) {
            if (!this.isPortListenerRegistered) {
                if (this.devicePortChangeObserver == null) {
                    this.devicePortChangeObserver = new AudioDevicePortChangeObserverImpl();
                }
                AUDIO_PORT_EVENT_HANDLER_ADAPTER.registerListener(this.devicePortChangeObserver);
                this.isPortListenerRegistered = true;
            }
            if (!this.deviceChangeObserverList.contains(audioDeviceChangeObserver)) {
                this.deviceChangeObserverList.add(audioDeviceChangeObserver);
            }
        }
        LOGGER.debug("setAudioDeviceChangeObserver end.", new Object[0]);
    }

    public void unregisterAudioCapturerCallback(AudioCapturerCallback audioCapturerCallback) {
        this.audioServiceAdapter.unregisterAudioCapturerCallback(audioCapturerCallback);
    }

    public void unregisterAudioRendererCallback(AudioRendererCallback audioRendererCallback) {
        this.audioServiceAdapter.unregisterAudioRendererCallback(audioRendererCallback);
    }

    public List<AudioCapturerConfig> getActiveCapturerConfigs() {
        return this.audioServiceAdapter.getActiveCapturerConfigs();
    }

    public List<AudioRendererInfo> getActiveRendererConfigs() {
        return this.audioServiceAdapter.getActiveRendererConfigs();
    }

    public static AudioDeviceDescriptor[] getDevices(AudioDeviceDescriptor.DeviceFlag deviceFlag) {
        if (deviceFlag == AudioDeviceDescriptor.DeviceFlag.OUTPUT_DEVICES_FLAG) {
            return AudioServiceAdapter.getDevicesStatic(deviceFlag);
        }
        return AudioSystemAdapter.getDevices(deviceFlag);
    }

    public void setMasterMute(boolean z) throws AudioManagerImplException {
        try {
            this.audioServiceAdapter.setMasterMute(z);
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public void setMicrophoneMute(boolean z) throws AudioManagerImplException {
        try {
            this.audioServiceAdapter.setMicrophoneMute(z);
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public boolean isMicrophoneMute() {
        return nativeIsMicrophoneMute();
    }

    public static Uri getRingerUri(Context context, int i) {
        return AudioSystemAdapter.getRingerUri(context, i);
    }

    public static void setRingerUri(Context context, int i, Uri uri) {
        AudioSystemAdapter.setRingerUri(context, i, uri);
    }

    public int getCallState() throws AudioManagerImplException {
        try {
            return this.audioServiceAdapter.getCallState();
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public void setCallState(int i) {
        this.audioServiceAdapter.setCallState(i);
    }

    public boolean changeVolumeBy(int i, int i2) throws AudioManagerImplException {
        try {
            return this.audioServiceAdapter.changeVolumeBy(i, i2);
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public static int getMasterOutputSampleRate() {
        return AudioSystemAdapter.getMasterOutputSampleRate();
    }

    public static int getMasterOutputFrameCount() {
        return AudioSystemAdapter.getMasterOutputFrameCount();
    }

    public static boolean isStreamActive(int i) {
        return AudioSystemAdapter.isStreamActive(i);
    }

    public void connectBluetoothSco() throws AudioManagerImplException {
        try {
            this.audioServiceAdapter.connectBluetoothSco();
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public void disconnectBluetoothSco() throws AudioManagerImplException {
        try {
            this.audioServiceAdapter.disconnectBluetoothSco();
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public static int makeSessionId() {
        return AudioSystemAdapter.makeSessionId();
    }

    public boolean canBluetoothScoUseNotInCall(Context context) throws AudioManagerImplException {
        try {
            return this.audioServiceAdapter.canBluetoothScoUseNotInCall(context);
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public boolean isMasterMute() throws AudioManagerImplException {
        try {
            return this.audioServiceAdapter.isMasterMute();
        } catch (AudioRemoteAdapterException e) {
            throw new AudioManagerImplException(e.toString());
        }
    }

    public boolean isWiredHeadsetOn() {
        return AudioSystemAdapter.isWiredHeadsetOn();
    }

    /* access modifiers changed from: private */
    public class AudioDevicePortChangeObserverImpl implements AudioPortEventHandlerAdapter.AudioDevicePortChangeObserver {
        private AudioDevicePortChangeObserverImpl() {
        }

        @Override // ohos.media.audioimpl.adapter.AudioPortEventHandlerAdapter.AudioDevicePortChangeObserver
        public void onAudioDevicePortListChange(AudioDeviceDescriptor[] audioDeviceDescriptorArr) {
            synchronized (AudioManagerImpl.this.deviceChangeObserverList) {
                AudioManagerImpl.this.handleForAudioPortListUpdate();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleForAudioPortListUpdate() {
        ArrayList<AudioDeviceDescriptor> arrayList = new ArrayList<>(Arrays.asList(getDevices(AudioDeviceDescriptor.DeviceFlag.ALL_DEVICES_FLAG)));
        AudioDeviceDescriptor[] calcDeltaBThanA = calcDeltaBThanA(this.prevDeviceList, arrayList);
        AudioDeviceDescriptor[] calcDeltaBThanA2 = calcDeltaBThanA(arrayList, this.prevDeviceList);
        if (calcDeltaBThanA.length == 0 && calcDeltaBThanA2.length == 0) {
            LOGGER.warn("handleForAudioPortListUpdate: no device change.", new Object[0]);
            return;
        }
        for (AudioDeviceChangeObserver audioDeviceChangeObserver : this.deviceChangeObserverList) {
            if (calcDeltaBThanA2.length != 0) {
                audioDeviceChangeObserver.onAudioDeviceChange(1, calcDeltaBThanA2);
            }
            if (calcDeltaBThanA.length != 0) {
                audioDeviceChangeObserver.onAudioDeviceChange(0, calcDeltaBThanA);
            }
        }
        this.prevDeviceList = arrayList;
    }

    private static AudioDeviceDescriptor[] calcDeltaBThanA(ArrayList<AudioDeviceDescriptor> arrayList, ArrayList<AudioDeviceDescriptor> arrayList2) {
        ArrayList arrayList3 = new ArrayList();
        Iterator<AudioDeviceDescriptor> it = arrayList2.iterator();
        while (it.hasNext()) {
            AudioDeviceDescriptor next = it.next();
            boolean z = false;
            Iterator<AudioDeviceDescriptor> it2 = arrayList.iterator();
            while (true) {
                if (it2.hasNext()) {
                    if (it2.next().getId() == next.getId()) {
                        z = true;
                        break;
                    }
                } else {
                    break;
                }
            }
            if (!z) {
                arrayList3.add(next);
            }
        }
        return (AudioDeviceDescriptor[]) arrayList3.toArray(new AudioDeviceDescriptor[arrayList3.size()]);
    }
}
