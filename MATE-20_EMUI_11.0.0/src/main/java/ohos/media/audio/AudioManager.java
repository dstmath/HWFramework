package ohos.media.audio;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import ohos.app.Context;
import ohos.media.audio.AudioDeviceDescriptor;
import ohos.media.audiofwk.AudioManagerImpl;
import ohos.media.audiofwk.AudioManagerImplException;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.media.utils.trace.Tracer;
import ohos.media.utils.trace.TracerFactory;
import ohos.utils.net.Uri;

public class AudioManager {
    public static final int DEFAULT_VOLUME = -1;
    public static final int DEVICE_ID_BLUETOOTH = 2;
    public static final int DEVICE_ID_SPEAKERPHONE = 1;
    public static final int DEVICE_ID_WIRED_HEADSET = 3;
    private static final String INVALID_PARAM = "Invalid request parameter";
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioManager.class);
    public static final int RINGER_MODE_MAX = AudioRingMode.RINGER_MODE_NORMAL.getValue();
    private static final Tracer TRACER = TracerFactory.getAudioTracer();
    private static final int UNINITIALIZED_PARAM = 0;
    private final AudioManagerImpl audioManagerImpl;
    private volatile Optional<Boolean> canBluetoothScoUseNotInCall;

    public enum AudioVolumeType {
        STREAM_VOICE_CALL(0),
        STREAM_SYSTEM(1),
        STREAM_RING(2),
        STREAM_MUSIC(3),
        STREAM_ALARM(4),
        STREAM_NOTIFICATION(5),
        STREAM_BLUETOOTH_SCO(6),
        STREAM_DTMF(8),
        STREAM_TTS(9),
        STREAM_ACCESSIBILITY(10);
        
        private final int contentTypeValue;

        private AudioVolumeType(int i) {
            this.contentTypeValue = i;
        }

        public int getValue() {
            return this.contentTypeValue;
        }
    }

    public enum AudioRingMode {
        RINGER_MODE_SILENT(0),
        RINGER_MODE_VIBRATE(1),
        RINGER_MODE_NORMAL(2);
        
        private final int contentTypeValue;

        private AudioRingMode(int i) {
            this.contentTypeValue = i;
        }

        public int getValue() {
            return this.contentTypeValue;
        }
    }

    public AudioManager() {
        this.canBluetoothScoUseNotInCall = Optional.empty();
        this.audioManagerImpl = new AudioManagerImpl();
    }

    public AudioManager(String str) {
        this.canBluetoothScoUseNotInCall = Optional.empty();
        this.audioManagerImpl = new AudioManagerImpl(str);
    }

    public AudioManager(Context context) {
        this.canBluetoothScoUseNotInCall = Optional.empty();
        this.audioManagerImpl = new AudioManagerImpl(context);
    }

    public int getVersion() {
        return this.audioManagerImpl.getVersion();
    }

    public boolean setVolume(AudioVolumeType audioVolumeType, int i) {
        if (audioVolumeType == null) {
            LOGGER.error("setVolume error: %{public}s", INVALID_PARAM);
            return false;
        }
        String format = String.format(Locale.ROOT, "AudioManager_setVolume_kits:volume=%d", Integer.valueOf(i));
        TRACER.startTrace(format);
        try {
            return this.audioManagerImpl.setVolume(audioVolumeType.getValue(), i);
        } catch (AudioManagerImplException e) {
            LOGGER.error("public setVolume error: %{public}s", e.toString());
            return false;
        } finally {
            TRACER.finishTrace(format);
        }
    }

    public int getVolume(AudioVolumeType audioVolumeType) throws AudioRemoteException {
        if (audioVolumeType != null) {
            TRACER.startTrace("AudioManager_getVolume_kits");
            try {
                int volume = this.audioManagerImpl.getVolume(audioVolumeType.getValue());
                TRACER.finishTrace("AudioManager_getVolume_kits");
                return volume;
            } catch (AudioManagerImplException e) {
                LOGGER.error("public getVolume error: %{public}s", e.toString());
                throw new AudioRemoteException(e.toString());
            } catch (Throwable th) {
                TRACER.finishTrace("AudioManager_getVolume_kits");
                throw th;
            }
        } else {
            LOGGER.error("getVolume error: %{public}s", INVALID_PARAM);
            throw new AudioRemoteException(INVALID_PARAM);
        }
    }

    public int getMinVolume(AudioVolumeType audioVolumeType) throws AudioRemoteException {
        if (audioVolumeType != null) {
            String format = String.format(Locale.ROOT, "AudioManager_getMinVolume_kits:type=%s", audioVolumeType);
            TRACER.startTrace(format);
            try {
                int minVolume = this.audioManagerImpl.getMinVolume(audioVolumeType.getValue());
                TRACER.finishTrace(format);
                return minVolume;
            } catch (AudioManagerImplException e) {
                LOGGER.error("public getMinVolume error %{public}s", e.toString());
                throw new AudioRemoteException(e.toString());
            } catch (Throwable th) {
                TRACER.finishTrace(format);
                throw th;
            }
        } else {
            LOGGER.error("getMinVolume error: %{public}s", INVALID_PARAM);
            throw new AudioRemoteException(INVALID_PARAM);
        }
    }

    public int getMaxVolume(AudioVolumeType audioVolumeType) throws AudioRemoteException {
        if (audioVolumeType != null) {
            String format = String.format(Locale.ROOT, "AudioManager_getMaxVolume_kits:type=%s", audioVolumeType);
            TRACER.startTrace(format);
            try {
                int maxVolume = this.audioManagerImpl.getMaxVolume(audioVolumeType.getValue());
                TRACER.finishTrace(format);
                return maxVolume;
            } catch (AudioManagerImplException e) {
                LOGGER.error("public getMaxVolume error %{public}s", e.toString());
                throw new AudioRemoteException(e.toString());
            } catch (Throwable th) {
                TRACER.finishTrace(format);
                throw th;
            }
        } else {
            LOGGER.error("getMaxVolume error: %{public}s", INVALID_PARAM);
            throw new AudioRemoteException(INVALID_PARAM);
        }
    }

    public boolean mute(AudioVolumeType audioVolumeType) {
        if (audioVolumeType == null) {
            LOGGER.error("mute error: %{public}s", INVALID_PARAM);
            return false;
        }
        String format = String.format(Locale.ROOT, "AudioManager_mute_kits:type=%s", audioVolumeType);
        TRACER.startTrace(format);
        try {
            this.audioManagerImpl.setMute(audioVolumeType.getValue(), true);
            return true;
        } catch (AudioManagerImplException e) {
            LOGGER.error("public mute error %{public}s", e.toString());
            return false;
        } finally {
            TRACER.finishTrace(format);
        }
    }

    public boolean unmute(AudioVolumeType audioVolumeType) {
        if (audioVolumeType == null) {
            LOGGER.error("unmute error: %{public}s", INVALID_PARAM);
            return false;
        }
        String format = String.format(Locale.ROOT, "AudioManager_unmute_kits:type=%s", audioVolumeType);
        TRACER.startTrace(format);
        try {
            this.audioManagerImpl.setMute(audioVolumeType.getValue(), false);
            return true;
        } catch (AudioManagerImplException e) {
            LOGGER.error("public unmute error %{public}s", e.toString());
            return false;
        } finally {
            TRACER.finishTrace(format);
        }
    }

    public boolean isMute(AudioVolumeType audioVolumeType) throws AudioRemoteException {
        if (audioVolumeType != null) {
            String format = String.format(Locale.ROOT, "AudioManager_isMute_kits:type=%s", audioVolumeType);
            TRACER.startTrace(format);
            try {
                boolean isMute = this.audioManagerImpl.isMute(audioVolumeType.getValue());
                TRACER.finishTrace(format);
                return isMute;
            } catch (AudioManagerImplException e) {
                LOGGER.error("public get mute state error %{public}s", e.toString());
                throw new AudioRemoteException(e.toString());
            } catch (Throwable th) {
                TRACER.finishTrace(format);
                throw th;
            }
        } else {
            LOGGER.error("isMute error: %{public}s", INVALID_PARAM);
            throw new AudioRemoteException(INVALID_PARAM);
        }
    }

    public boolean setRingerMode(AudioRingMode audioRingMode) {
        if (audioRingMode == null) {
            LOGGER.error("setRingerMode error: %{public}s", INVALID_PARAM);
            return false;
        }
        String format = String.format(Locale.ROOT, "AudioManager_setRingerMode_kits:mode=%s", audioRingMode);
        TRACER.startTrace(format);
        try {
            this.audioManagerImpl.setRingerMode(audioRingMode.getValue());
            return true;
        } catch (AudioManagerImplException e) {
            LOGGER.error("public setRingerMode error %{public}s", e.toString());
            return false;
        } finally {
            TRACER.finishTrace(format);
        }
    }

    public int getRingerMode() throws AudioRemoteException {
        TRACER.startTrace("AudioManager_getRingerMode_kits");
        try {
            int ringerMode = this.audioManagerImpl.getRingerMode();
            TRACER.finishTrace("AudioManager_getRingerMode_kits");
            return ringerMode;
        } catch (AudioManagerImplException e) {
            LOGGER.error("public getRingerMode error %{public}s", e.toString());
            throw new AudioRemoteException(e.toString());
        } catch (Throwable th) {
            TRACER.finishTrace("AudioManager_getRingerMode_kits");
            throw th;
        }
    }

    public boolean setDeviceActive(int i, boolean z) {
        String format = String.format(Locale.ROOT, "AudioManager_setDeviceActive_kits:deviceType=%d,state=%s", Integer.valueOf(i), Boolean.valueOf(z));
        TRACER.startTrace(format);
        if (i == 1) {
            this.audioManagerImpl.setSpeakerOn(z);
        } else if (i != 2) {
            try {
                LOGGER.error("public setDeviceActive error,invalid device type %{public}d;", Integer.valueOf(i));
            } catch (AudioManagerImplException e) {
                LOGGER.error("public setDeviceActive error %{public}s", e.toString());
            } catch (Throwable th) {
                TRACER.finishTrace(format);
                throw th;
            }
            TRACER.finishTrace(format);
            return false;
        } else {
            this.audioManagerImpl.setBlueToothOn(z);
        }
        TRACER.finishTrace(format);
        return true;
    }

    public boolean isDeviceActive(int i) throws AudioRemoteException {
        boolean isSpeakerOn;
        String format = String.format(Locale.ROOT, "AudioManager_isDeviceActive_kits:deviceType=%d", Integer.valueOf(i));
        TRACER.startTrace(format);
        if (i == 1) {
            isSpeakerOn = this.audioManagerImpl.isSpeakerOn();
        } else if (i == 2) {
            isSpeakerOn = this.audioManagerImpl.isBluetoothOn();
        } else if (i == 3) {
            try {
                isSpeakerOn = this.audioManagerImpl.isWiredHeadsetOn();
            } catch (AudioManagerImplException e) {
                LOGGER.error("public get Device state error %{public}s", e.toString());
                throw new AudioRemoteException(e.toString());
            } catch (Throwable th) {
                TRACER.finishTrace(format);
                throw th;
            }
        } else {
            LOGGER.error("public get Device state error,invalid device type %{public}d", Integer.valueOf(i));
            throw new AudioRemoteException("get Device state error,invalid device type");
        }
        TRACER.finishTrace(format);
        return isSpeakerOn;
    }

    public boolean activateAudioInterrupt(AudioInterrupt audioInterrupt) {
        TRACER.startTrace("AudioManager_activateAudioInterrupt_kits");
        boolean activateAudioInterrupt = this.audioManagerImpl.activateAudioInterrupt(audioInterrupt);
        TRACER.finishTrace("AudioManager_activateAudioInterrupt_kits");
        return activateAudioInterrupt;
    }

    public boolean deactivateAudioInterrupt(AudioInterrupt audioInterrupt) {
        TRACER.startTrace("AudioManager_deactivateAudioInterrupt_kits");
        boolean deactivateAudioInterrupt = this.audioManagerImpl.deactivateAudioInterrupt(audioInterrupt);
        TRACER.finishTrace("AudioManager_deactivateAudioInterrupt_kits");
        return deactivateAudioInterrupt;
    }

    public boolean setAudioParameter(String str, String str2) {
        if (str == null || str2 == null) {
            LOGGER.error("Input parameters invalid", new Object[0]);
            return false;
        }
        return this.audioManagerImpl.setAudioParameters(str + "=" + str2);
    }

    public String getAudioParameter(String str) {
        return this.audioManagerImpl.getAudioParameters(str);
    }

    public void registerAudioCapturerCallback(AudioCapturerCallback audioCapturerCallback) throws IllegalArgumentException {
        if (audioCapturerCallback != null) {
            this.audioManagerImpl.registerAudioCapturerCallback(audioCapturerCallback);
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioCapturerCallback argument");
    }

    public void registerAudioRendererCallback(AudioRendererCallback audioRendererCallback) {
        if (audioRendererCallback != null) {
            this.audioManagerImpl.registerAudioRendererCallback(audioRendererCallback);
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioRendererCallback argument");
    }

    public void unregisterAudioCapturerCallback(AudioCapturerCallback audioCapturerCallback) {
        this.audioManagerImpl.unregisterAudioCapturerCallback(audioCapturerCallback);
    }

    public void unregisterAudioRendererCallback(AudioRendererCallback audioRendererCallback) {
        this.audioManagerImpl.unregisterAudioRendererCallback(audioRendererCallback);
    }

    public List<AudioCapturerConfig> getActiveCapturerConfigs() {
        return this.audioManagerImpl.getActiveCapturerConfigs();
    }

    public List<AudioRendererInfo> getActiveRendererConfigs() {
        return this.audioManagerImpl.getActiveRendererConfigs();
    }

    public static AudioDeviceDescriptor[] getDevices(AudioDeviceDescriptor.DeviceFlag deviceFlag) {
        if (deviceFlag != null) {
            return AudioManagerImpl.getDevices(deviceFlag);
        }
        LOGGER.error("getDevices error: %{public}s", INVALID_PARAM);
        return new AudioDeviceDescriptor[0];
    }

    public boolean setMasterMute(boolean z) {
        String format = String.format(Locale.ROOT, "AudioManager_setMasterMute_kits:isMute=%s", Boolean.valueOf(z));
        TRACER.startTrace(format);
        try {
            this.audioManagerImpl.setMasterMute(z);
            TRACER.finishTrace(format);
            return true;
        } catch (AudioManagerImplException e) {
            TRACER.finishTrace(format);
            LOGGER.error("public set MasterMute error %{public}s", e.toString());
            return false;
        }
    }

    public boolean setMicrophoneMute(boolean z) {
        String format = String.format(Locale.ROOT, "AudioManager_setMicrophoneMute_kits:isMute=%s", Boolean.valueOf(z));
        TRACER.startTrace(format);
        try {
            this.audioManagerImpl.setMicrophoneMute(z);
            TRACER.finishTrace(format);
            return true;
        } catch (AudioManagerImplException e) {
            TRACER.finishTrace(format);
            LOGGER.error("public set MicrophoneMute error %{public}s", e.toString());
            return false;
        }
    }

    public boolean isMicrophoneMute() {
        return this.audioManagerImpl.isMicrophoneMute();
    }

    public static Uri getRingerUri(Context context, RingtoneType ringtoneType) {
        if (context == null || ringtoneType == null) {
            LOGGER.error("getRingerUri error: %{public}s", INVALID_PARAM);
            return null;
        }
        String format = String.format(Locale.ROOT, "AudioManager_getRingerUri_kits:type=%s", ringtoneType);
        TRACER.startTrace(format);
        Uri ringerUri = AudioManagerImpl.getRingerUri(context, ringtoneType.getValue());
        TRACER.finishTrace(format);
        return ringerUri;
    }

    public static void setRingerUri(Context context, RingtoneType ringtoneType, Uri uri) {
        if (context == null || ringtoneType == null || uri == null) {
            LOGGER.error("setRingerUri error: %{public}s", INVALID_PARAM);
            return;
        }
        String format = String.format(Locale.ROOT, "AudioManager_setRingerUri_kits:type=%s", ringtoneType);
        TRACER.startTrace(format);
        AudioManagerImpl.setRingerUri(context, ringtoneType.getValue(), uri);
        TRACER.finishTrace(format);
    }

    public PhoneState getPhoneState() throws AudioRemoteException {
        TRACER.startTrace("AudioManager_getPhoneState_kits");
        try {
            PhoneState enumByValue = PhoneState.getEnumByValue(this.audioManagerImpl.getPhoneState());
            TRACER.finishTrace("AudioManager_getPhoneState_kits");
            return enumByValue;
        } catch (AudioManagerImplException e) {
            LOGGER.error("public set getPhoneState error %{public}s", e.toString());
            throw new AudioRemoteException(e.toString());
        } catch (Throwable th) {
            TRACER.finishTrace("AudioManager_getPhoneState_kits");
            throw th;
        }
    }

    public void setPhoneState(PhoneState phoneState) {
        if (phoneState == null) {
            LOGGER.error("setPhoneState error: %{public}s", INVALID_PARAM);
            return;
        }
        TRACER.startTrace("AudioManager_setPhoneState_kits");
        this.audioManagerImpl.setPhoneState(phoneState.getValue());
        TRACER.finishTrace("AudioManager_setPhoneState_kits");
    }

    public enum RingtoneType {
        RING(1),
        NOTIFICATION(2),
        ALARM(4);
        
        private final int type;

        private RingtoneType(int i) {
            this.type = i;
        }

        public int getValue() {
            return this.type;
        }
    }

    public enum PhoneState {
        IDLE(0),
        RINGTONE(1),
        IN_CALL(2),
        IN_VOIP(3);
        
        private static final String INVALID_VALUE = "invalid value";
        private final int state;

        private PhoneState(int i) {
            this.state = i;
        }

        public int getValue() {
            return this.state;
        }

        public static PhoneState getEnumByValue(int i) throws AudioManagerImplException {
            PhoneState[] values = values();
            for (PhoneState phoneState : values) {
                if (phoneState.getValue() == i) {
                    return phoneState;
                }
            }
            throw new AudioManagerImplException(INVALID_VALUE);
        }
    }

    public boolean changeVolumeBy(AudioVolumeType audioVolumeType, int i) {
        if (audioVolumeType == null || i < -1 || i > 1) {
            LOGGER.error("changeVolumeBy error: %{public}s", INVALID_PARAM);
            return false;
        }
        try {
            if (getVolIncrease(audioVolumeType, i) == 0) {
                LOGGER.info("public changeVolumeBy no need to change", new Object[0]);
                return true;
            }
            try {
                return this.audioManagerImpl.changeVolumeBy(audioVolumeType.getValue(), i);
            } catch (AudioManagerImplException e) {
                LOGGER.error("public changeVolumeBy error: %{public}s", e.toString());
                return false;
            }
        } catch (AudioRemoteException unused) {
            LOGGER.error("changeVolumeBy get volume failed.", new Object[0]);
            return false;
        }
    }

    private int getVolIncrease(AudioVolumeType audioVolumeType, int i) throws AudioRemoteException {
        int minVolume;
        int volume = getVolume(audioVolumeType);
        if (i <= 0 || Integer.MAX_VALUE - i <= volume) {
            return (i >= 0 || Integer.MIN_VALUE - i >= volume || volume + i >= (minVolume = getMinVolume(audioVolumeType))) ? i : minVolume - volume;
        }
        int maxVolume = getMaxVolume(audioVolumeType);
        return volume + i > maxVolume ? maxVolume - volume : i;
    }

    public static int getMasterOutputSampleRate() {
        int masterOutputSampleRate = AudioManagerImpl.getMasterOutputSampleRate();
        if (masterOutputSampleRate <= 0) {
            return 0;
        }
        return masterOutputSampleRate;
    }

    public static int getMasterOutputFrameCount() {
        int masterOutputFrameCount = AudioManagerImpl.getMasterOutputFrameCount();
        if (masterOutputFrameCount <= 0) {
            return 0;
        }
        return masterOutputFrameCount;
    }

    public static boolean isStreamActive(AudioVolumeType audioVolumeType) {
        if (audioVolumeType != null) {
            return AudioManagerImpl.isStreamActive(audioVolumeType.getValue());
        }
        LOGGER.error("isStreamActive error: %{public}s", INVALID_PARAM);
        return false;
    }

    public boolean connectBluetoothSco() {
        try {
            this.audioManagerImpl.connectBluetoothSco();
            return true;
        } catch (AudioManagerImplException unused) {
            return false;
        }
    }

    public boolean disconnectBluetoothSco() {
        try {
            this.audioManagerImpl.disconnectBluetoothSco();
            return true;
        } catch (AudioManagerImplException unused) {
            return false;
        }
    }

    public static int makeSessionId() {
        return AudioManagerImpl.makeSessionId();
    }

    public boolean isMasterMute() throws AudioRemoteException {
        try {
            return this.audioManagerImpl.isMasterMute();
        } catch (AudioManagerImplException e) {
            throw new AudioRemoteException(e.toString());
        }
    }
}
