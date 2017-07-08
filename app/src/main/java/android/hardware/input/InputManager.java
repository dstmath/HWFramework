package android.hardware.input;

import android.content.Context;
import android.graphics.Color;
import android.hardware.input.IInputDevicesChangedListener.Stub;
import android.media.AudioAttributes;
import android.media.tv.TvContract;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Vibrator;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.security.keymaster.KeymasterArguments;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.PointerIcon;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.os.SomeArgs;
import java.util.ArrayList;
import java.util.List;

public final class InputManager {
    public static final String ACTION_QUERY_KEYBOARD_LAYOUTS = "android.hardware.input.action.QUERY_KEYBOARD_LAYOUTS";
    private static final boolean DEBUG = false;
    public static final int DEFAULT_POINTER_SPEED = 0;
    public static final int INJECT_INPUT_EVENT_MODE_ASYNC = 0;
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
    private final IInputManager mIm;
    private final ArrayList<InputDeviceListenerDelegate> mInputDeviceListeners;
    private SparseArray<InputDevice> mInputDevices;
    private InputDevicesChangedListener mInputDevicesChangedListener;
    private final Object mInputDevicesLock;
    private List<OnTabletModeChangedListenerDelegate> mOnTabletModeChangedListeners;
    private TabletModeChangedListener mTabletModeChangedListener;
    private final Object mTabletModeLock;

    public interface InputDeviceListener {
        void onInputDeviceAdded(int i);

        void onInputDeviceChanged(int i);

        void onInputDeviceRemoved(int i);
    }

    private static final class InputDeviceListenerDelegate extends Handler {
        public final InputDeviceListener mListener;

        public InputDeviceListenerDelegate(InputDeviceListener listener, Handler handler) {
            super(handler != null ? handler.getLooper() : Looper.myLooper());
            this.mListener = listener;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case InputManager.SWITCH_STATE_ON /*1*/:
                    this.mListener.onInputDeviceAdded(msg.arg1);
                case InputManager.MSG_DEVICE_REMOVED /*2*/:
                    this.mListener.onInputDeviceRemoved(msg.arg1);
                case InputManager.MSG_DEVICE_CHANGED /*3*/:
                    this.mListener.onInputDeviceChanged(msg.arg1);
                default:
            }
        }
    }

    private final class InputDeviceVibrator extends Vibrator {
        private final int mDeviceId;
        private final Binder mToken;

        public InputDeviceVibrator(int deviceId) {
            this.mDeviceId = deviceId;
            this.mToken = new Binder();
        }

        public boolean hasVibrator() {
            return true;
        }

        public void vibrate(int uid, String opPkg, long milliseconds, AudioAttributes attributes) {
            long[] jArr = new long[InputManager.MSG_DEVICE_REMOVED];
            jArr[InputManager.SWITCH_STATE_OFF] = 0;
            jArr[InputManager.SWITCH_STATE_ON] = milliseconds;
            vibrate(jArr, (int) InputManager.SWITCH_STATE_UNKNOWN);
        }

        public void vibrate(int uid, String opPkg, long[] pattern, int repeat, AudioAttributes attributes) {
            if (repeat >= pattern.length) {
                throw new ArrayIndexOutOfBoundsException();
            }
            try {
                InputManager.this.mIm.vibrate(this.mDeviceId, pattern, repeat, this.mToken);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }

        public void cancel() {
            try {
                InputManager.this.mIm.cancelVibrate(this.mDeviceId, this.mToken);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    private final class InputDevicesChangedListener extends Stub {
        private InputDevicesChangedListener() {
        }

        public void onInputDevicesChanged(int[] deviceIdAndGeneration) throws RemoteException {
            InputManager.this.onInputDevicesChanged(deviceIdAndGeneration);
        }
    }

    public interface OnTabletModeChangedListener {
        void onTabletModeChanged(long j, boolean z);
    }

    private static final class OnTabletModeChangedListenerDelegate extends Handler {
        private static final int MSG_TABLET_MODE_CHANGED = 0;
        public final OnTabletModeChangedListener mListener;

        public OnTabletModeChangedListenerDelegate(OnTabletModeChangedListener listener, Handler handler) {
            super(handler != null ? handler.getLooper() : Looper.myLooper());
            this.mListener = listener;
        }

        public void sendTabletModeChanged(long whenNanos, boolean inTabletMode) {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = (int) (-1 & whenNanos);
            args.argi2 = (int) (whenNanos >> 32);
            args.arg1 = Boolean.valueOf(inTabletMode);
            obtainMessage(InputManager.SWITCH_STATE_OFF, args).sendToTarget();
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case InputManager.SWITCH_STATE_OFF /*0*/:
                    SomeArgs args = msg.obj;
                    this.mListener.onTabletModeChanged((((long) args.argi1) & KeymasterArguments.UINT32_MAX_VALUE) | (((long) args.argi2) << 32), ((Boolean) args.arg1).booleanValue());
                default:
            }
        }
    }

    private final class TabletModeChangedListener extends ITabletModeChangedListener.Stub {
        private TabletModeChangedListener() {
        }

        public void onTabletModeChanged(long whenNanos, boolean inTabletMode) {
            InputManager.this.onTabletModeChanged(whenNanos, inTabletMode);
        }
    }

    private InputManager(IInputManager im) {
        this.mInputDevicesLock = new Object();
        this.mInputDeviceListeners = new ArrayList();
        this.mTabletModeLock = new Object();
        this.mIm = im;
    }

    public static InputManager getInstance() {
        InputManager inputManager;
        synchronized (InputManager.class) {
            if (sInstance == null) {
                sInstance = new InputManager(IInputManager.Stub.asInterface(ServiceManager.getService(TvContract.PARAM_INPUT)));
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
            InputDevice inputDevice = (InputDevice) this.mInputDevices.valueAt(index);
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
        if (descriptor == null) {
            throw new IllegalArgumentException("descriptor must not be null.");
        }
        synchronized (this.mInputDevicesLock) {
            populateInputDevicesLocked();
            int numDevices = this.mInputDevices.size();
            for (int i = SWITCH_STATE_OFF; i < numDevices; i += SWITCH_STATE_ON) {
                InputDevice inputDevice = (InputDevice) this.mInputDevices.valueAt(i);
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

    public int[] getInputDeviceIds() {
        int[] ids;
        synchronized (this.mInputDevicesLock) {
            populateInputDevicesLocked();
            int count = this.mInputDevices.size();
            ids = new int[count];
            for (int i = SWITCH_STATE_OFF; i < count; i += SWITCH_STATE_ON) {
                ids[i] = this.mInputDevices.keyAt(i);
            }
        }
        return ids;
    }

    public void registerInputDeviceListener(InputDeviceListener listener, Handler handler) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        synchronized (this.mInputDevicesLock) {
            populateInputDevicesLocked();
            if (findInputDeviceListenerLocked(listener) < 0) {
                this.mInputDeviceListeners.add(new InputDeviceListenerDelegate(listener, handler));
            }
        }
    }

    public void unregisterInputDeviceListener(InputDeviceListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        synchronized (this.mInputDevicesLock) {
            int index = findInputDeviceListenerLocked(listener);
            if (index >= 0) {
                ((InputDeviceListenerDelegate) this.mInputDeviceListeners.get(index)).removeCallbacksAndMessages(null);
                this.mInputDeviceListeners.remove(index);
            }
        }
    }

    private int findInputDeviceListenerLocked(InputDeviceListener listener) {
        int numListeners = this.mInputDeviceListeners.size();
        for (int i = SWITCH_STATE_OFF; i < numListeners; i += SWITCH_STATE_ON) {
            if (((InputDeviceListenerDelegate) this.mInputDeviceListeners.get(i)).mListener == listener) {
                return i;
            }
        }
        return SWITCH_STATE_UNKNOWN;
    }

    public int isInTabletMode() {
        try {
            return this.mIm.isInTabletMode();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void registerOnTabletModeChangedListener(OnTabletModeChangedListener listener, Handler handler) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        synchronized (this.mTabletModeLock) {
            if (this.mOnTabletModeChangedListeners == null) {
                initializeTabletModeListenerLocked();
            }
            if (findOnTabletModeChangedListenerLocked(listener) < 0) {
                this.mOnTabletModeChangedListeners.add(new OnTabletModeChangedListenerDelegate(listener, handler));
            }
        }
    }

    public void unregisterOnTabletModeChangedListener(OnTabletModeChangedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        synchronized (this.mTabletModeLock) {
            int idx = findOnTabletModeChangedListenerLocked(listener);
            if (idx >= 0) {
                ((OnTabletModeChangedListenerDelegate) this.mOnTabletModeChangedListeners.remove(idx)).removeCallbacksAndMessages(null);
            }
        }
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
        for (int i = SWITCH_STATE_OFF; i < N; i += SWITCH_STATE_ON) {
            if (((OnTabletModeChangedListenerDelegate) this.mOnTabletModeChangedListeners.get(i)).mListener == listener) {
                return i;
            }
        }
        return SWITCH_STATE_UNKNOWN;
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
        if (keyboardLayoutDescriptor == null) {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        }
        try {
            return this.mIm.getKeyboardLayout(keyboardLayoutDescriptor);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
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
        } else if (keyboardLayoutDescriptor == null) {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        } else {
            try {
                this.mIm.setCurrentKeyboardLayoutForInputDevice(identifier, keyboardLayoutDescriptor);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    public String[] getEnabledKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("inputDeviceDescriptor must not be null");
        }
        try {
            return this.mIm.getEnabledKeyboardLayoutsForInputDevice(identifier);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void addKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) {
        if (identifier == null) {
            throw new IllegalArgumentException("inputDeviceDescriptor must not be null");
        } else if (keyboardLayoutDescriptor == null) {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        } else {
            try {
                this.mIm.addKeyboardLayoutForInputDevice(identifier, keyboardLayoutDescriptor);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    public void removeKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) {
        if (identifier == null) {
            throw new IllegalArgumentException("inputDeviceDescriptor must not be null");
        } else if (keyboardLayoutDescriptor == null) {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        } else {
            try {
                this.mIm.removeKeyboardLayoutForInputDevice(identifier, keyboardLayoutDescriptor);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    public KeyboardLayout getKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, InputMethodInfo inputMethodInfo, InputMethodSubtype inputMethodSubtype) {
        try {
            return this.mIm.getKeyboardLayoutForInputDevice(identifier, inputMethodInfo, inputMethodSubtype);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void setKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, InputMethodInfo inputMethodInfo, InputMethodSubtype inputMethodSubtype, String keyboardLayoutDescriptor) {
        try {
            this.mIm.setKeyboardLayoutForInputDevice(identifier, inputMethodInfo, inputMethodSubtype, keyboardLayoutDescriptor);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
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
        int speed = SWITCH_STATE_OFF;
        try {
            speed = System.getInt(context.getContentResolver(), System.POINTER_SPEED);
        } catch (SettingNotFoundException e) {
        }
        return speed;
    }

    public void setPointerSpeed(Context context, int speed) {
        if (speed < MIN_POINTER_SPEED || speed > MAX_POINTER_SPEED) {
            throw new IllegalArgumentException("speed out of range");
        }
        System.putInt(context.getContentResolver(), System.POINTER_SPEED, speed);
    }

    public void tryPointerSpeed(int speed) {
        if (speed < MIN_POINTER_SPEED || speed > MAX_POINTER_SPEED) {
            throw new IllegalArgumentException("speed out of range");
        }
        try {
            this.mIm.tryPointerSpeed(speed);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public boolean[] deviceHasKeys(int[] keyCodes) {
        return deviceHasKeys(SWITCH_STATE_UNKNOWN, keyCodes);
    }

    public boolean[] deviceHasKeys(int id, int[] keyCodes) {
        boolean[] ret = new boolean[keyCodes.length];
        try {
            this.mIm.hasKeys(id, Color.YELLOW, keyCodes, ret);
            return ret;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean injectInputEvent(InputEvent event, int mode) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        } else if (mode != 0 && mode != MSG_DEVICE_REMOVED && mode != SWITCH_STATE_ON) {
            throw new IllegalArgumentException("mode is invalid");
        } else if (this.mIm == null) {
            return DEBUG;
        } else {
            try {
                return this.mIm.injectInputEvent(event, mode);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

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

    public void setMirrorLinkInputStatus(boolean status) {
        try {
            this.mIm.setMirrorLinkInputStatus(status);
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
                this.mInputDevices = new SparseArray();
                for (int i = SWITCH_STATE_OFF; i < ids.length; i += SWITCH_STATE_ON) {
                    this.mInputDevices.put(ids[i], null);
                }
            } catch (RemoteException ex2) {
                throw ex2.rethrowFromSystemServer();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onInputDevicesChanged(int[] deviceIdAndGeneration) {
        synchronized (this.mInputDevicesLock) {
            int i = this.mInputDevices.size();
            while (true) {
                i += SWITCH_STATE_UNKNOWN;
                if (i <= 0) {
                    break;
                }
                int deviceId = this.mInputDevices.keyAt(i);
                if (!containsDeviceId(deviceIdAndGeneration, deviceId)) {
                    this.mInputDevices.removeAt(i);
                    sendMessageToInputDeviceListenersLocked(MSG_DEVICE_REMOVED, deviceId);
                }
            }
            i = SWITCH_STATE_OFF;
            while (true) {
                if (i < deviceIdAndGeneration.length) {
                    deviceId = deviceIdAndGeneration[i];
                    int index = this.mInputDevices.indexOfKey(deviceId);
                    if (index >= 0) {
                        InputDevice device = (InputDevice) this.mInputDevices.valueAt(index);
                        if (!(device == null || device.getGeneration() == deviceIdAndGeneration[i + SWITCH_STATE_ON])) {
                            this.mInputDevices.setValueAt(index, null);
                            sendMessageToInputDeviceListenersLocked(MSG_DEVICE_CHANGED, deviceId);
                        }
                    } else {
                        this.mInputDevices.put(deviceId, null);
                        sendMessageToInputDeviceListenersLocked(SWITCH_STATE_ON, deviceId);
                    }
                    i += MSG_DEVICE_REMOVED;
                }
            }
        }
    }

    private void sendMessageToInputDeviceListenersLocked(int what, int deviceId) {
        int numListeners = this.mInputDeviceListeners.size();
        for (int i = SWITCH_STATE_OFF; i < numListeners; i += SWITCH_STATE_ON) {
            InputDeviceListenerDelegate listener = (InputDeviceListenerDelegate) this.mInputDeviceListeners.get(i);
            listener.sendMessage(listener.obtainMessage(what, deviceId, SWITCH_STATE_OFF));
        }
    }

    private static boolean containsDeviceId(int[] deviceIdAndGeneration, int deviceId) {
        for (int i = SWITCH_STATE_OFF; i < deviceIdAndGeneration.length; i += MSG_DEVICE_REMOVED) {
            if (deviceIdAndGeneration[i] == deviceId) {
                return true;
            }
        }
        return DEBUG;
    }

    private void onTabletModeChanged(long whenNanos, boolean inTabletMode) {
        synchronized (this.mTabletModeLock) {
            int N = this.mOnTabletModeChangedListeners.size();
            for (int i = SWITCH_STATE_OFF; i < N; i += SWITCH_STATE_ON) {
                ((OnTabletModeChangedListenerDelegate) this.mOnTabletModeChangedListeners.get(i)).sendTabletModeChanged(whenNanos, inTabletMode);
            }
        }
    }

    public Vibrator getInputDeviceVibrator(int deviceId) {
        return new InputDeviceVibrator(deviceId);
    }
}
