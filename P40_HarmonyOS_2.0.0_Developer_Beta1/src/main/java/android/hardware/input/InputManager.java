package android.hardware.input;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.hardware.input.IInputDevicesChangedListener;
import android.hardware.input.IInputManager;
import android.hardware.input.ITabletModeChangedListener;
import android.media.AudioAttributes;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputMonitor;
import android.view.PointerIcon;
import com.android.internal.os.SomeArgs;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public final class InputManager {
    public static final String ACTION_QUERY_KEYBOARD_LAYOUTS = "android.hardware.input.action.QUERY_KEYBOARD_LAYOUTS";
    private static final boolean DEBUG = false;
    public static final int DEFAULT_POINTER_SPEED = 0;
    public static final int INJECT_INPUT_EVENT_MODE_ASYNC = 0;
    @UnsupportedAppUsage
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = 1;
    public static final int MAX_POINTER_SPEED = 7;
    public static final String META_DATA_KEYBOARD_LAYOUTS = "android.hardware.input.metadata.KEYBOARD_LAYOUTS";
    public static final int MIN_POINTER_SPEED = -7;
    private static final int MSG_DEVICE_ADDED = 1;
    private static final int MSG_DEVICE_CHANGED = 3;
    private static final int MSG_DEVICE_REMOVED = 2;
    public static final int SWITCH_STATE_OFF = 0;
    public static final int SWITCH_STATE_ON = 1;
    public static final int SWITCH_STATE_UNKNOWN = -1;
    private static final String TAG = "InputManager";
    private static InputManager sInstance;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final IInputManager mIm;
    private final ArrayList<InputDeviceListenerDelegate> mInputDeviceListeners = new ArrayList<>();
    private SparseArray<InputDevice> mInputDevices;
    private InputDevicesChangedListener mInputDevicesChangedListener;
    private final Object mInputDevicesLock = new Object();
    private List<OnTabletModeChangedListenerDelegate> mOnTabletModeChangedListeners;
    private TabletModeChangedListener mTabletModeChangedListener;
    private final Object mTabletModeLock = new Object();

    public interface InputDeviceListener {
        void onInputDeviceAdded(int i);

        void onInputDeviceChanged(int i);

        void onInputDeviceRemoved(int i);
    }

    public interface OnTabletModeChangedListener {
        void onTabletModeChanged(long j, boolean z);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SwitchState {
    }

    private InputManager(IInputManager im) {
        this.mIm = im;
    }

    @UnsupportedAppUsage
    public static InputManager getInstance() {
        InputManager inputManager;
        synchronized (InputManager.class) {
            if (sInstance == null) {
                try {
                    sInstance = new InputManager(IInputManager.Stub.asInterface(ServiceManager.getServiceOrThrow("input")));
                } catch (ServiceManager.ServiceNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
            inputManager = sInstance;
        }
        return inputManager;
    }

    public InputDevice getInputDevice(int id) {
        synchronized (this.mInputDevicesLock) {
            populateInputDevicesLocked();
            int index = this.mInputDevices.indexOfKey(id);
            if (index < 0) {
                return null;
            }
            InputDevice inputDevice = this.mInputDevices.valueAt(index);
            if (inputDevice == null) {
                try {
                    inputDevice = this.mIm.getInputDevice(id);
                    if (inputDevice != null) {
                        this.mInputDevices.setValueAt(index, inputDevice);
                    }
                } catch (RemoteException ex) {
                    throw ex.rethrowFromSystemServer();
                }
            }
            return inputDevice;
        }
    }

    public InputDevice getInputDeviceByDescriptor(String descriptor) {
        if (descriptor != null) {
            synchronized (this.mInputDevicesLock) {
                populateInputDevicesLocked();
                int numDevices = this.mInputDevices.size();
                for (int i = 0; i < numDevices; i++) {
                    InputDevice inputDevice = this.mInputDevices.valueAt(i);
                    if (inputDevice == null) {
                        try {
                            inputDevice = this.mIm.getInputDevice(this.mInputDevices.keyAt(i));
                            if (inputDevice == null) {
                                continue;
                            } else {
                                this.mInputDevices.setValueAt(i, inputDevice);
                            }
                        } catch (RemoteException ex) {
                            throw ex.rethrowFromSystemServer();
                        }
                    }
                    if (descriptor.equals(inputDevice.getDescriptor())) {
                        return inputDevice;
                    }
                }
                return null;
            }
        }
        throw new IllegalArgumentException("descriptor must not be null.");
    }

    public int[] getInputDeviceIds() {
        int[] ids;
        synchronized (this.mInputDevicesLock) {
            populateInputDevicesLocked();
            int count = this.mInputDevices.size();
            ids = new int[count];
            for (int i = 0; i < count; i++) {
                ids[i] = this.mInputDevices.keyAt(i);
            }
        }
        return ids;
    }

    public boolean isInputDeviceEnabled(int id) {
        try {
            return this.mIm.isInputDeviceEnabled(id);
        } catch (RemoteException ex) {
            Log.w(TAG, "Could not check enabled status of input device with id = " + id);
            throw ex.rethrowFromSystemServer();
        }
    }

    public void enableInputDevice(int id) {
        try {
            this.mIm.enableInputDevice(id);
        } catch (RemoteException ex) {
            Log.w(TAG, "Could not enable input device with id = " + id);
            throw ex.rethrowFromSystemServer();
        }
    }

    public void disableInputDevice(int id) {
        try {
            this.mIm.disableInputDevice(id);
        } catch (RemoteException ex) {
            Log.w(TAG, "Could not disable input device with id = " + id);
            throw ex.rethrowFromSystemServer();
        }
    }

    public void registerInputDeviceListener(InputDeviceListener listener, Handler handler) {
        if (listener != null) {
            synchronized (this.mInputDevicesLock) {
                populateInputDevicesLocked();
                if (findInputDeviceListenerLocked(listener) < 0) {
                    this.mInputDeviceListeners.add(new InputDeviceListenerDelegate(listener, handler));
                }
            }
            return;
        }
        throw new IllegalArgumentException("listener must not be null");
    }

    public void unregisterInputDeviceListener(InputDeviceListener listener) {
        if (listener != null) {
            synchronized (this.mInputDevicesLock) {
                int index = findInputDeviceListenerLocked(listener);
                if (index >= 0) {
                    this.mInputDeviceListeners.get(index).removeCallbacksAndMessages(null);
                    this.mInputDeviceListeners.remove(index);
                }
            }
            return;
        }
        throw new IllegalArgumentException("listener must not be null");
    }

    private int findInputDeviceListenerLocked(InputDeviceListener listener) {
        int numListeners = this.mInputDeviceListeners.size();
        for (int i = 0; i < numListeners; i++) {
            if (this.mInputDeviceListeners.get(i).mListener == listener) {
                return i;
            }
        }
        return -1;
    }

    public int isInTabletMode() {
        try {
            return this.mIm.isInTabletMode();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void registerOnTabletModeChangedListener(OnTabletModeChangedListener listener, Handler handler) {
        if (listener != null) {
            synchronized (this.mTabletModeLock) {
                if (this.mOnTabletModeChangedListeners == null) {
                    initializeTabletModeListenerLocked();
                }
                if (findOnTabletModeChangedListenerLocked(listener) < 0) {
                    this.mOnTabletModeChangedListeners.add(new OnTabletModeChangedListenerDelegate(listener, handler));
                }
            }
            return;
        }
        throw new IllegalArgumentException("listener must not be null");
    }

    public void unregisterOnTabletModeChangedListener(OnTabletModeChangedListener listener) {
        if (listener != null) {
            synchronized (this.mTabletModeLock) {
                int idx = findOnTabletModeChangedListenerLocked(listener);
                if (idx >= 0) {
                    this.mOnTabletModeChangedListeners.remove(idx).removeCallbacksAndMessages(null);
                }
            }
            return;
        }
        throw new IllegalArgumentException("listener must not be null");
    }

    private void initializeTabletModeListenerLocked() {
        TabletModeChangedListener listener = new TabletModeChangedListener();
        try {
            this.mIm.registerTabletModeChangedListener(listener);
            this.mTabletModeChangedListener = listener;
            this.mOnTabletModeChangedListeners = new ArrayList();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    private int findOnTabletModeChangedListenerLocked(OnTabletModeChangedListener listener) {
        int N = this.mOnTabletModeChangedListeners.size();
        for (int i = 0; i < N; i++) {
            if (this.mOnTabletModeChangedListeners.get(i).mListener == listener) {
                return i;
            }
        }
        return -1;
    }

    public KeyboardLayout[] getKeyboardLayouts() {
        try {
            return this.mIm.getKeyboardLayouts();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public KeyboardLayout[] getKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier) {
        try {
            return this.mIm.getKeyboardLayoutsForInputDevice(identifier);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public KeyboardLayout getKeyboardLayout(String keyboardLayoutDescriptor) {
        if (keyboardLayoutDescriptor != null) {
            try {
                return this.mIm.getKeyboardLayout(keyboardLayoutDescriptor);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        }
    }

    public String getCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier) {
        try {
            return this.mIm.getCurrentKeyboardLayoutForInputDevice(identifier);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void setCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier must not be null");
        } else if (keyboardLayoutDescriptor != null) {
            try {
                this.mIm.setCurrentKeyboardLayoutForInputDevice(identifier, keyboardLayoutDescriptor);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        }
    }

    public String[] getEnabledKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier) {
        if (identifier != null) {
            try {
                return this.mIm.getEnabledKeyboardLayoutsForInputDevice(identifier);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("inputDeviceDescriptor must not be null");
        }
    }

    public void addKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) {
        if (identifier == null) {
            throw new IllegalArgumentException("inputDeviceDescriptor must not be null");
        } else if (keyboardLayoutDescriptor != null) {
            try {
                this.mIm.addKeyboardLayoutForInputDevice(identifier, keyboardLayoutDescriptor);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        }
    }

    public void removeKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) {
        if (identifier == null) {
            throw new IllegalArgumentException("inputDeviceDescriptor must not be null");
        } else if (keyboardLayoutDescriptor != null) {
            try {
                this.mIm.removeKeyboardLayoutForInputDevice(identifier, keyboardLayoutDescriptor);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        }
    }

    public TouchCalibration getTouchCalibration(String inputDeviceDescriptor, int surfaceRotation) {
        try {
            return this.mIm.getTouchCalibrationForInputDevice(inputDeviceDescriptor, surfaceRotation);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void setTouchCalibration(String inputDeviceDescriptor, int surfaceRotation, TouchCalibration calibration) {
        try {
            this.mIm.setTouchCalibrationForInputDevice(inputDeviceDescriptor, surfaceRotation, calibration);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public int getPointerSpeed(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.POINTER_SPEED);
        } catch (Settings.SettingNotFoundException e) {
            return 0;
        }
    }

    public void setPointerSpeed(Context context, int speed) {
        if (speed < -7 || speed > 7) {
            throw new IllegalArgumentException("speed out of range");
        }
        Settings.System.putInt(context.getContentResolver(), Settings.System.POINTER_SPEED, speed);
    }

    public void tryPointerSpeed(int speed) {
        if (speed < -7 || speed > 7) {
            throw new IllegalArgumentException("speed out of range");
        }
        try {
            this.mIm.tryPointerSpeed(speed);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public boolean[] deviceHasKeys(int[] keyCodes) {
        return deviceHasKeys(-1, keyCodes);
    }

    public boolean[] deviceHasKeys(int id, int[] keyCodes) {
        boolean[] ret = new boolean[keyCodes.length];
        try {
            this.mIm.hasKeys(id, -256, keyCodes, ret);
            return ret;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public boolean injectInputEvent(InputEvent event, int mode) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        } else if (mode == 0 || mode == 2 || mode == 1) {
            try {
                return this.mIm.injectInputEvent(event, mode);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("mode is invalid");
        }
    }

    @UnsupportedAppUsage
    public void setPointerIconType(int iconId) {
        try {
            this.mIm.setPointerIconType(iconId);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void setCustomPointerIcon(PointerIcon icon) {
        try {
            this.mIm.setCustomPointerIcon(icon);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void requestPointerCapture(IBinder windowToken, boolean enable) {
        try {
            this.mIm.requestPointerCapture(windowToken, enable);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public InputMonitor monitorGestureInput(String name, int displayId) {
        try {
            return this.mIm.monitorGestureInput(name, displayId);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    private void populateInputDevicesLocked() {
        if (this.mInputDevicesChangedListener == null) {
            InputDevicesChangedListener listener = new InputDevicesChangedListener();
            try {
                this.mIm.registerInputDevicesChangedListener(listener);
                this.mInputDevicesChangedListener = listener;
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
        if (this.mInputDevices == null) {
            try {
                int[] ids = this.mIm.getInputDeviceIds();
                this.mInputDevices = new SparseArray<>();
                for (int i : ids) {
                    this.mInputDevices.put(i, null);
                }
            } catch (RemoteException ex2) {
                throw ex2.rethrowFromSystemServer();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onInputDevicesChanged(int[] deviceIdAndGeneration) {
        synchronized (this.mInputDevicesLock) {
            int i = this.mInputDevices.size();
            while (true) {
                i--;
                if (i <= 0) {
                    break;
                }
                int deviceId = this.mInputDevices.keyAt(i);
                if (!containsDeviceId(deviceIdAndGeneration, deviceId)) {
                    this.mInputDevices.removeAt(i);
                    sendMessageToInputDeviceListenersLocked(2, deviceId);
                }
            }
            for (int i2 = 0; i2 < deviceIdAndGeneration.length; i2 += 2) {
                int deviceId2 = deviceIdAndGeneration[i2];
                int index = this.mInputDevices.indexOfKey(deviceId2);
                if (index >= 0) {
                    InputDevice device = this.mInputDevices.valueAt(index);
                    if (!(device == null || device.getGeneration() == deviceIdAndGeneration[i2 + 1])) {
                        this.mInputDevices.setValueAt(index, null);
                        sendMessageToInputDeviceListenersLocked(3, deviceId2);
                    }
                } else {
                    this.mInputDevices.put(deviceId2, null);
                    sendMessageToInputDeviceListenersLocked(1, deviceId2);
                }
            }
        }
    }

    private void sendMessageToInputDeviceListenersLocked(int what, int deviceId) {
        int numListeners = this.mInputDeviceListeners.size();
        for (int i = 0; i < numListeners; i++) {
            InputDeviceListenerDelegate listener = this.mInputDeviceListeners.get(i);
            listener.sendMessage(listener.obtainMessage(what, deviceId, 0));
        }
    }

    private static boolean containsDeviceId(int[] deviceIdAndGeneration, int deviceId) {
        for (int i = 0; i < deviceIdAndGeneration.length; i += 2) {
            if (deviceIdAndGeneration[i] == deviceId) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTabletModeChanged(long whenNanos, boolean inTabletMode) {
        synchronized (this.mTabletModeLock) {
            int N = this.mOnTabletModeChangedListeners.size();
            for (int i = 0; i < N; i++) {
                this.mOnTabletModeChangedListeners.get(i).sendTabletModeChanged(whenNanos, inTabletMode);
            }
        }
    }

    public Vibrator getInputDeviceVibrator(int deviceId) {
        return new InputDeviceVibrator(deviceId);
    }

    /* access modifiers changed from: private */
    public final class InputDevicesChangedListener extends IInputDevicesChangedListener.Stub {
        private InputDevicesChangedListener() {
        }

        @Override // android.hardware.input.IInputDevicesChangedListener
        public void onInputDevicesChanged(int[] deviceIdAndGeneration) throws RemoteException {
            InputManager.this.onInputDevicesChanged(deviceIdAndGeneration);
        }
    }

    /* access modifiers changed from: private */
    public static final class InputDeviceListenerDelegate extends Handler {
        public final InputDeviceListener mListener;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public InputDeviceListenerDelegate(InputDeviceListener listener, Handler handler) {
            super(handler != null ? handler.getLooper() : Looper.myLooper());
            this.mListener = listener;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                this.mListener.onInputDeviceAdded(msg.arg1);
            } else if (i == 2) {
                this.mListener.onInputDeviceRemoved(msg.arg1);
            } else if (i == 3) {
                this.mListener.onInputDeviceChanged(msg.arg1);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class TabletModeChangedListener extends ITabletModeChangedListener.Stub {
        private TabletModeChangedListener() {
        }

        @Override // android.hardware.input.ITabletModeChangedListener
        public void onTabletModeChanged(long whenNanos, boolean inTabletMode) {
            InputManager.this.onTabletModeChanged(whenNanos, inTabletMode);
        }
    }

    /* access modifiers changed from: private */
    public static final class OnTabletModeChangedListenerDelegate extends Handler {
        private static final int MSG_TABLET_MODE_CHANGED = 0;
        public final OnTabletModeChangedListener mListener;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public OnTabletModeChangedListenerDelegate(OnTabletModeChangedListener listener, Handler handler) {
            super(handler != null ? handler.getLooper() : Looper.myLooper());
            this.mListener = listener;
        }

        public void sendTabletModeChanged(long whenNanos, boolean inTabletMode) {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = (int) (-1 & whenNanos);
            args.argi2 = (int) (whenNanos >> 32);
            args.arg1 = Boolean.valueOf(inTabletMode);
            obtainMessage(0, args).sendToTarget();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                SomeArgs args = (SomeArgs) msg.obj;
                this.mListener.onTabletModeChanged((((long) args.argi1) & 4294967295L) | (((long) args.argi2) << 32), ((Boolean) args.arg1).booleanValue());
            }
        }
    }

    private final class InputDeviceVibrator extends Vibrator {
        private final int mDeviceId;
        private final Binder mToken = new Binder();

        public InputDeviceVibrator(int deviceId) {
            this.mDeviceId = deviceId;
        }

        @Override // android.os.Vibrator
        public boolean hasVibrator() {
            return true;
        }

        @Override // android.os.Vibrator
        public boolean hasAmplitudeControl() {
            return false;
        }

        @Override // android.os.Vibrator
        public void vibrate(int uid, String opPkg, VibrationEffect effect, String reason, AudioAttributes attributes) {
            long[] pattern;
            int repeat;
            if (effect instanceof VibrationEffect.OneShot) {
                pattern = new long[]{0, ((VibrationEffect.OneShot) effect).getDuration()};
                repeat = -1;
            } else if (effect instanceof VibrationEffect.Waveform) {
                VibrationEffect.Waveform waveform = (VibrationEffect.Waveform) effect;
                pattern = waveform.getTimings();
                repeat = waveform.getRepeatIndex();
            } else {
                Log.w(InputManager.TAG, "Pre-baked effects aren't supported on input devices");
                return;
            }
            try {
                InputManager.this.mIm.vibrate(this.mDeviceId, pattern, repeat, this.mToken);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }

        @Override // android.os.Vibrator
        public void cancel() {
            try {
                InputManager.this.mIm.cancelVibrate(this.mDeviceId, this.mToken);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }
}
