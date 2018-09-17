package android.media.audiofx;

import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
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
    private BaseParameterListener mBaseParamListener = null;
    private OnParameterChangeListener mParamListener = null;
    private final Object mParamListenerLock = new Object();
    private boolean mStrengthSupported = false;

    private class BaseParameterListener implements android.media.audiofx.AudioEffect.OnParameterChangeListener {
        /* synthetic */ BaseParameterListener(Virtualizer this$0, BaseParameterListener -this1) {
            this();
        }

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
                if (param.length == 4) {
                    p = AudioEffect.byteArrayToInt(param, 0);
                }
                if (value.length == 2) {
                    v = AudioEffect.byteArrayToShort(value, 0);
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
            if (st.countTokens() != 3) {
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
        if (audioSession == 0) {
            Log.w(TAG, "WARNING: attaching a Virtualizer to global output mix is deprecated!");
        }
        int[] value = new int[1];
        checkStatus(getParameter(0, value));
        if (value[0] == 0) {
            z = false;
        }
        this.mStrengthSupported = z;
    }

    public boolean getStrengthSupported() {
        return this.mStrengthSupported;
    }

    public void setStrength(short strength) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(1, strength));
    }

    public short getRoundedStrength() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        short[] value = new short[1];
        checkStatus(getParameter(1, value));
        return value[0];
    }

    private boolean getAnglesInt(int inputChannelMask, int deviceType, int[] angles) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        if (inputChannelMask == 0) {
            throw new IllegalArgumentException("Virtualizer: illegal CHANNEL_INVALID channel mask");
        }
        int channelMask = inputChannelMask == 1 ? 12 : inputChannelMask;
        int nbChannels = AudioFormat.channelCountFromOutChannelMask(channelMask);
        if (angles == null || angles.length >= nbChannels * 3) {
            ByteBuffer paramsConverter = ByteBuffer.allocate(12);
            paramsConverter.order(ByteOrder.nativeOrder());
            paramsConverter.putInt(2);
            paramsConverter.putInt(AudioFormat.convertChannelOutMaskToNativeMask(channelMask));
            paramsConverter.putInt(AudioDeviceInfo.convertDeviceTypeToInternalDevice(deviceType));
            byte[] result = new byte[((nbChannels * 4) * 3)];
            int status = getParameter(paramsConverter.array(), result);
            if (status >= 0) {
                if (angles != null) {
                    ByteBuffer resultConverter = ByteBuffer.wrap(result);
                    resultConverter.order(ByteOrder.nativeOrder());
                    for (int i = 0; i < nbChannels; i++) {
                        angles[i * 3] = AudioFormat.convertNativeChannelMaskToOutMask(resultConverter.getInt((i * 4) * 3));
                        angles[(i * 3) + 1] = resultConverter.getInt(((i * 4) * 3) + 4);
                        angles[(i * 3) + 2] = resultConverter.getInt(((i * 4) * 3) + 8);
                    }
                }
                return true;
            } else if (status == -4) {
                return false;
            } else {
                checkStatus(status);
                Log.e(TAG, "unexpected status code " + status + " after getParameter(PARAM_VIRTUAL_SPEAKER_ANGLES)");
                return false;
            }
        }
        Log.e(TAG, "Size of array for angles cannot accomodate number of channels in mask (" + nbChannels + ")");
        throw new IllegalArgumentException("Virtualizer: array for channel / angle pairs is too small: is " + angles.length + ", should be " + (nbChannels * 3));
    }

    private static int getDeviceForModeQuery(int virtualizationMode) throws IllegalArgumentException {
        switch (virtualizationMode) {
            case 2:
                return 4;
            case 3:
                return 2;
            default:
                throw new IllegalArgumentException("Virtualizer: illegal virtualization mode " + virtualizationMode);
        }
    }

    private static int getDeviceForModeForce(int virtualizationMode) throws IllegalArgumentException {
        if (virtualizationMode == 1) {
            return 0;
        }
        return getDeviceForModeQuery(virtualizationMode);
    }

    private static int deviceToMode(int deviceType) {
        switch (deviceType) {
            case 1:
            case 3:
            case 4:
            case 7:
            case 22:
                return 2;
            case 2:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 19:
                return 3;
            default:
                return 0;
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
        int status = setParameter(3, AudioDeviceInfo.convertDeviceTypeToInternalDevice(getDeviceForModeForce(virtualizationMode)));
        if (status >= 0) {
            return true;
        }
        if (status == -4) {
            return false;
        }
        checkStatus(status);
        Log.e(TAG, "unexpected status code " + status + " after setParameter(PARAM_FORCE_VIRTUALIZATION_MODE)");
        return false;
    }

    public int getVirtualizationMode() throws IllegalStateException, UnsupportedOperationException {
        int[] value = new int[1];
        int status = getParameter(4, value);
        if (status >= 0) {
            return deviceToMode(AudioDeviceInfo.convertInternalDeviceToDeviceType(value[0]));
        }
        if (status == -4) {
            return 0;
        }
        checkStatus(status);
        Log.e(TAG, "unexpected status code " + status + " after getParameter(PARAM_VIRTUALIZATION_MODE)");
        return 0;
    }

    public void setParameterListener(OnParameterChangeListener listener) {
        synchronized (this.mParamListenerLock) {
            if (this.mParamListener == null) {
                this.mParamListener = listener;
                this.mBaseParamListener = new BaseParameterListener(this, null);
                super.setParameterListener(this.mBaseParamListener);
            }
        }
    }

    public Settings getProperties() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        Settings settings = new Settings();
        short[] value = new short[1];
        checkStatus(getParameter(1, value));
        settings.strength = value[0];
        return settings;
    }

    public void setProperties(Settings settings) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(1, settings.strength));
    }
}
