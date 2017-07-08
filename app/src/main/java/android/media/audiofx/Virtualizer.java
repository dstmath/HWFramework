package android.media.audiofx;

import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.rms.HwSysResource;
import android.service.notification.NotificationRankerService;
import android.speech.SpeechRecognizer;
import android.telecom.AudioState;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringTokenizer;

public class Virtualizer extends AudioEffect {
    private static final boolean DEBUG = false;
    public static final int PARAM_FORCE_VIRTUALIZATION_MODE = 3;
    public static final int PARAM_STRENGTH = 1;
    public static final int PARAM_STRENGTH_SUPPORTED = 0;
    public static final int PARAM_VIRTUALIZATION_MODE = 4;
    public static final int PARAM_VIRTUAL_SPEAKER_ANGLES = 2;
    private static final String TAG = "Virtualizer";
    public static final int VIRTUALIZATION_MODE_AUTO = 1;
    public static final int VIRTUALIZATION_MODE_BINAURAL = 2;
    public static final int VIRTUALIZATION_MODE_OFF = 0;
    public static final int VIRTUALIZATION_MODE_TRANSAURAL = 3;
    private BaseParameterListener mBaseParamListener;
    private OnParameterChangeListener mParamListener;
    private final Object mParamListenerLock;
    private boolean mStrengthSupported;

    private class BaseParameterListener implements android.media.audiofx.AudioEffect.OnParameterChangeListener {
        private BaseParameterListener() {
        }

        public void onParameterChange(AudioEffect effect, int status, byte[] param, byte[] value) {
            OnParameterChangeListener l = null;
            synchronized (Virtualizer.this.mParamListenerLock) {
                if (Virtualizer.this.mParamListener != null) {
                    l = Virtualizer.this.mParamListener;
                }
            }
            if (l != null) {
                int p = -1;
                short v = (short) -1;
                if (param.length == Virtualizer.PARAM_VIRTUALIZATION_MODE) {
                    p = AudioEffect.byteArrayToInt(param, Virtualizer.VIRTUALIZATION_MODE_OFF);
                }
                if (value.length == Virtualizer.VIRTUALIZATION_MODE_BINAURAL) {
                    v = AudioEffect.byteArrayToShort(value, Virtualizer.VIRTUALIZATION_MODE_OFF);
                }
                if (p != -1 && v != (short) -1) {
                    l.onParameterChange(Virtualizer.this, status, p, v);
                }
            }
        }
    }

    public interface OnParameterChangeListener {
        void onParameterChange(Virtualizer virtualizer, int i, int i2, short s);
    }

    public static class Settings {
        public short strength;

        public Settings(String settings) {
            StringTokenizer st = new StringTokenizer(settings, "=;");
            int tokens = st.countTokens();
            if (st.countTokens() != Virtualizer.VIRTUALIZATION_MODE_TRANSAURAL) {
                throw new IllegalArgumentException("settings: " + settings);
            }
            String key = st.nextToken();
            if (key.equals(Virtualizer.TAG)) {
                try {
                    key = st.nextToken();
                    if (key.equals("strength")) {
                        this.strength = Short.parseShort(st.nextToken());
                        return;
                    }
                    throw new IllegalArgumentException("invalid key name: " + key);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("invalid value for key: " + key);
                }
            }
            throw new IllegalArgumentException("invalid settings for Virtualizer: " + key);
        }

        public String toString() {
            return new String("Virtualizer;strength=" + Short.toString(this.strength));
        }
    }

    public Virtualizer(int priority, int audioSession) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        boolean z = true;
        super(EFFECT_TYPE_VIRTUALIZER, EFFECT_TYPE_NULL, priority, audioSession);
        this.mStrengthSupported = DEBUG;
        this.mParamListener = null;
        this.mBaseParamListener = null;
        this.mParamListenerLock = new Object();
        if (audioSession == 0) {
            Log.w(TAG, "WARNING: attaching a Virtualizer to global output mix is deprecated!");
        }
        int[] value = new int[VIRTUALIZATION_MODE_AUTO];
        checkStatus(getParameter((int) VIRTUALIZATION_MODE_OFF, value));
        if (value[VIRTUALIZATION_MODE_OFF] == 0) {
            z = DEBUG;
        }
        this.mStrengthSupported = z;
    }

    public boolean getStrengthSupported() {
        return this.mStrengthSupported;
    }

    public void setStrength(short strength) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) VIRTUALIZATION_MODE_AUTO, strength));
    }

    public short getRoundedStrength() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        short[] value = new short[VIRTUALIZATION_MODE_AUTO];
        checkStatus(getParameter((int) VIRTUALIZATION_MODE_AUTO, value));
        return value[VIRTUALIZATION_MODE_OFF];
    }

    private boolean getAnglesInt(int inputChannelMask, int deviceType, int[] angles) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        if (inputChannelMask == 0) {
            throw new IllegalArgumentException("Virtualizer: illegal CHANNEL_INVALID channel mask");
        }
        int channelMask;
        if (inputChannelMask == VIRTUALIZATION_MODE_AUTO) {
            channelMask = 12;
        } else {
            channelMask = inputChannelMask;
        }
        int nbChannels = AudioFormat.channelCountFromOutChannelMask(channelMask);
        if (angles == null || angles.length >= nbChannels * VIRTUALIZATION_MODE_TRANSAURAL) {
            ByteBuffer paramsConverter = ByteBuffer.allocate(12);
            paramsConverter.order(ByteOrder.nativeOrder());
            paramsConverter.putInt(VIRTUALIZATION_MODE_BINAURAL);
            paramsConverter.putInt(AudioFormat.convertChannelOutMaskToNativeMask(channelMask));
            paramsConverter.putInt(AudioDeviceInfo.convertDeviceTypeToInternalDevice(deviceType));
            byte[] result = new byte[((nbChannels * PARAM_VIRTUALIZATION_MODE) * VIRTUALIZATION_MODE_TRANSAURAL)];
            int status = getParameter(paramsConverter.array(), result);
            if (status >= 0) {
                if (angles != null) {
                    ByteBuffer resultConverter = ByteBuffer.wrap(result);
                    resultConverter.order(ByteOrder.nativeOrder());
                    for (int i = VIRTUALIZATION_MODE_OFF; i < nbChannels; i += VIRTUALIZATION_MODE_AUTO) {
                        angles[i * VIRTUALIZATION_MODE_TRANSAURAL] = AudioFormat.convertNativeChannelMaskToOutMask(resultConverter.getInt((i * PARAM_VIRTUALIZATION_MODE) * VIRTUALIZATION_MODE_TRANSAURAL));
                        angles[(i * VIRTUALIZATION_MODE_TRANSAURAL) + VIRTUALIZATION_MODE_AUTO] = resultConverter.getInt(((i * PARAM_VIRTUALIZATION_MODE) * VIRTUALIZATION_MODE_TRANSAURAL) + PARAM_VIRTUALIZATION_MODE);
                        angles[(i * VIRTUALIZATION_MODE_TRANSAURAL) + VIRTUALIZATION_MODE_BINAURAL] = resultConverter.getInt(((i * PARAM_VIRTUALIZATION_MODE) * VIRTUALIZATION_MODE_TRANSAURAL) + 8);
                    }
                }
                return true;
            } else if (status == -4) {
                return DEBUG;
            } else {
                checkStatus(status);
                Log.e(TAG, "unexpected status code " + status + " after getParameter(PARAM_VIRTUAL_SPEAKER_ANGLES)");
                return DEBUG;
            }
        }
        Log.e(TAG, "Size of array for angles cannot accomodate number of channels in mask (" + nbChannels + ")");
        throw new IllegalArgumentException("Virtualizer: array for channel / angle pairs is too small: is " + angles.length + ", should be " + (nbChannels * VIRTUALIZATION_MODE_TRANSAURAL));
    }

    private static int getDeviceForModeQuery(int virtualizationMode) throws IllegalArgumentException {
        switch (virtualizationMode) {
            case VIRTUALIZATION_MODE_BINAURAL /*2*/:
                return PARAM_VIRTUALIZATION_MODE;
            case VIRTUALIZATION_MODE_TRANSAURAL /*3*/:
                return VIRTUALIZATION_MODE_BINAURAL;
            default:
                throw new IllegalArgumentException("Virtualizer: illegal virtualization mode " + virtualizationMode);
        }
    }

    private static int getDeviceForModeForce(int virtualizationMode) throws IllegalArgumentException {
        if (virtualizationMode == VIRTUALIZATION_MODE_AUTO) {
            return VIRTUALIZATION_MODE_OFF;
        }
        return getDeviceForModeQuery(virtualizationMode);
    }

    private static int deviceToMode(int deviceType) {
        switch (deviceType) {
            case VIRTUALIZATION_MODE_AUTO /*1*/:
            case VIRTUALIZATION_MODE_TRANSAURAL /*3*/:
            case PARAM_VIRTUALIZATION_MODE /*4*/:
            case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
                return VIRTUALIZATION_MODE_BINAURAL;
            case VIRTUALIZATION_MODE_BINAURAL /*2*/:
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
            case AudioState.ROUTE_SPEAKER /*8*/:
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
            case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
            case NotificationRankerService.REASON_LISTENER_CANCEL_ALL /*11*/:
            case NotificationRankerService.REASON_GROUP_SUMMARY_CANCELED /*12*/:
            case NotificationRankerService.REASON_GROUP_OPTIMIZATION /*13*/:
            case NotificationRankerService.REASON_PACKAGE_SUSPENDED /*14*/:
            case HwSysResource.APP /*19*/:
                return VIRTUALIZATION_MODE_TRANSAURAL;
            default:
                return VIRTUALIZATION_MODE_OFF;
        }
    }

    public boolean canVirtualize(int inputChannelMask, int virtualizationMode) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        return getAnglesInt(inputChannelMask, getDeviceForModeQuery(virtualizationMode), null);
    }

    public boolean getSpeakerAngles(int inputChannelMask, int virtualizationMode, int[] angles) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        if (angles != null) {
            return getAnglesInt(inputChannelMask, getDeviceForModeQuery(virtualizationMode), angles);
        }
        throw new IllegalArgumentException("Virtualizer: illegal null channel / angle array");
    }

    public boolean forceVirtualizationMode(int virtualizationMode) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        int status = setParameter((int) VIRTUALIZATION_MODE_TRANSAURAL, AudioDeviceInfo.convertDeviceTypeToInternalDevice(getDeviceForModeForce(virtualizationMode)));
        if (status >= 0) {
            return true;
        }
        if (status == -4) {
            return DEBUG;
        }
        checkStatus(status);
        Log.e(TAG, "unexpected status code " + status + " after setParameter(PARAM_FORCE_VIRTUALIZATION_MODE)");
        return DEBUG;
    }

    public int getVirtualizationMode() throws IllegalStateException, UnsupportedOperationException {
        int[] value = new int[VIRTUALIZATION_MODE_AUTO];
        int status = getParameter((int) PARAM_VIRTUALIZATION_MODE, value);
        if (status >= 0) {
            return deviceToMode(AudioDeviceInfo.convertInternalDeviceToDeviceType(value[VIRTUALIZATION_MODE_OFF]));
        }
        if (status == -4) {
            return VIRTUALIZATION_MODE_OFF;
        }
        checkStatus(status);
        Log.e(TAG, "unexpected status code " + status + " after getParameter(PARAM_VIRTUALIZATION_MODE)");
        return VIRTUALIZATION_MODE_OFF;
    }

    public void setParameterListener(OnParameterChangeListener listener) {
        synchronized (this.mParamListenerLock) {
            if (this.mParamListener == null) {
                this.mParamListener = listener;
                this.mBaseParamListener = new BaseParameterListener();
                super.setParameterListener(this.mBaseParamListener);
            }
        }
    }

    public Settings getProperties() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        Settings settings = new Settings();
        short[] value = new short[VIRTUALIZATION_MODE_AUTO];
        checkStatus(getParameter((int) VIRTUALIZATION_MODE_AUTO, value));
        settings.strength = value[VIRTUALIZATION_MODE_OFF];
        return settings;
    }

    public void setProperties(Settings settings) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) VIRTUALIZATION_MODE_AUTO, settings.strength));
    }
}
