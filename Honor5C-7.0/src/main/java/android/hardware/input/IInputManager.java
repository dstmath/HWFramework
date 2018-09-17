package android.hardware.input;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.PointerIcon;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;

public interface IInputManager extends IInterface {

    public static abstract class Stub extends Binder implements IInputManager {
        private static final String DESCRIPTOR = "android.hardware.input.IInputManager";
        static final int TRANSACTION_addKeyboardLayoutForInputDevice = 14;
        static final int TRANSACTION_cancelVibrate = 22;
        static final int TRANSACTION_getCurrentKeyboardLayoutForInputDevice = 11;
        static final int TRANSACTION_getEnabledKeyboardLayoutsForInputDevice = 13;
        static final int TRANSACTION_getInputDevice = 1;
        static final int TRANSACTION_getInputDeviceIds = 2;
        static final int TRANSACTION_getKeyboardLayout = 10;
        static final int TRANSACTION_getKeyboardLayoutForInputDevice = 16;
        static final int TRANSACTION_getKeyboardLayouts = 8;
        static final int TRANSACTION_getKeyboardLayoutsForInputDevice = 9;
        static final int TRANSACTION_getTouchCalibrationForInputDevice = 6;
        static final int TRANSACTION_hasKeys = 3;
        static final int TRANSACTION_injectInputEvent = 5;
        static final int TRANSACTION_isInTabletMode = 19;
        static final int TRANSACTION_registerInputDevicesChangedListener = 18;
        static final int TRANSACTION_registerTabletModeChangedListener = 20;
        static final int TRANSACTION_removeKeyboardLayoutForInputDevice = 15;
        static final int TRANSACTION_setCurrentKeyboardLayoutForInputDevice = 12;
        static final int TRANSACTION_setCustomPointerIcon = 24;
        static final int TRANSACTION_setKeyboardLayoutForInputDevice = 17;
        static final int TRANSACTION_setMirrorLinkInputStatus = 25;
        static final int TRANSACTION_setPointerIconType = 23;
        static final int TRANSACTION_setTouchCalibrationForInputDevice = 7;
        static final int TRANSACTION_tryPointerSpeed = 4;
        static final int TRANSACTION_vibrate = 21;

        private static class Proxy implements IInputManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public InputDevice getInputDevice(int deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    InputDevice inputDevice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    this.mRemote.transact(Stub.TRANSACTION_getInputDevice, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        inputDevice = (InputDevice) InputDevice.CREATOR.createFromParcel(_reply);
                    } else {
                        inputDevice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return inputDevice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getInputDeviceIds() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getInputDeviceIds, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasKeys(int deviceId, int sourceMask, int[] keyCodes, boolean[] keyExists) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    _data.writeInt(sourceMask);
                    _data.writeIntArray(keyCodes);
                    if (keyExists == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(keyExists.length);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_hasKeys, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.readBooleanArray(keyExists);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void tryPointerSpeed(int speed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(speed);
                    this.mRemote.transact(Stub.TRANSACTION_tryPointerSpeed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean injectInputEvent(InputEvent ev, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ev != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        ev.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    this.mRemote.transact(Stub.TRANSACTION_injectInputEvent, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public TouchCalibration getTouchCalibrationForInputDevice(String inputDeviceDescriptor, int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    TouchCalibration touchCalibration;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputDeviceDescriptor);
                    _data.writeInt(rotation);
                    this.mRemote.transact(Stub.TRANSACTION_getTouchCalibrationForInputDevice, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        touchCalibration = (TouchCalibration) TouchCalibration.CREATOR.createFromParcel(_reply);
                    } else {
                        touchCalibration = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return touchCalibration;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTouchCalibrationForInputDevice(String inputDeviceDescriptor, int rotation, TouchCalibration calibration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputDeviceDescriptor);
                    _data.writeInt(rotation);
                    if (calibration != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        calibration.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setTouchCalibrationForInputDevice, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public KeyboardLayout[] getKeyboardLayouts() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getKeyboardLayouts, _data, _reply, 0);
                    _reply.readException();
                    KeyboardLayout[] _result = (KeyboardLayout[]) _reply.createTypedArray(KeyboardLayout.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public KeyboardLayout[] getKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (identifier != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        identifier.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getKeyboardLayoutsForInputDevice, _data, _reply, 0);
                    _reply.readException();
                    KeyboardLayout[] _result = (KeyboardLayout[]) _reply.createTypedArray(KeyboardLayout.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public KeyboardLayout getKeyboardLayout(String keyboardLayoutDescriptor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    KeyboardLayout keyboardLayout;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(keyboardLayoutDescriptor);
                    this.mRemote.transact(Stub.TRANSACTION_getKeyboardLayout, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        keyboardLayout = (KeyboardLayout) KeyboardLayout.CREATOR.createFromParcel(_reply);
                    } else {
                        keyboardLayout = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return keyboardLayout;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (identifier != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        identifier.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentKeyboardLayoutForInputDevice, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (identifier != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        identifier.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(keyboardLayoutDescriptor);
                    this.mRemote.transact(Stub.TRANSACTION_setCurrentKeyboardLayoutForInputDevice, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getEnabledKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (identifier != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        identifier.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getEnabledKeyboardLayoutsForInputDevice, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (identifier != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        identifier.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(keyboardLayoutDescriptor);
                    this.mRemote.transact(Stub.TRANSACTION_addKeyboardLayoutForInputDevice, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (identifier != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        identifier.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(keyboardLayoutDescriptor);
                    this.mRemote.transact(Stub.TRANSACTION_removeKeyboardLayoutForInputDevice, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public KeyboardLayout getKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, InputMethodInfo imeInfo, InputMethodSubtype imeSubtype) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    KeyboardLayout keyboardLayout;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (identifier != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        identifier.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (imeInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        imeInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (imeSubtype != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        imeSubtype.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getKeyboardLayoutForInputDevice, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        keyboardLayout = (KeyboardLayout) KeyboardLayout.CREATOR.createFromParcel(_reply);
                    } else {
                        keyboardLayout = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return keyboardLayout;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, InputMethodInfo imeInfo, InputMethodSubtype imeSubtype, String keyboardLayoutDescriptor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (identifier != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        identifier.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (imeInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        imeInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (imeSubtype != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        imeSubtype.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(keyboardLayoutDescriptor);
                    this.mRemote.transact(Stub.TRANSACTION_setKeyboardLayoutForInputDevice, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerInputDevicesChangedListener(IInputDevicesChangedListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerInputDevicesChangedListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int isInTabletMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isInTabletMode, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerTabletModeChangedListener(ITabletModeChangedListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerTabletModeChangedListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void vibrate(int deviceId, long[] pattern, int repeat, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    _data.writeLongArray(pattern);
                    _data.writeInt(repeat);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_vibrate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelVibrate(int deviceId, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_cancelVibrate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPointerIconType(int typeId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(typeId);
                    this.mRemote.transact(Stub.TRANSACTION_setPointerIconType, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCustomPointerIcon(PointerIcon icon) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (icon != null) {
                        _data.writeInt(Stub.TRANSACTION_getInputDevice);
                        icon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setCustomPointerIcon, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMirrorLinkInputStatus(boolean status) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (status) {
                        i = Stub.TRANSACTION_getInputDevice;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setMirrorLinkInputStatus, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInputManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInputManager)) {
                return new Proxy(obj);
            }
            return (IInputManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg1;
            boolean _result;
            KeyboardLayout[] _result2;
            InputDeviceIdentifier inputDeviceIdentifier;
            KeyboardLayout _result3;
            InputMethodInfo inputMethodInfo;
            InputMethodSubtype inputMethodSubtype;
            switch (code) {
                case TRANSACTION_getInputDevice /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    InputDevice _result4 = getInputDevice(data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_getInputDevice);
                        _result4.writeToParcel(reply, TRANSACTION_getInputDevice);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getInputDeviceIds /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    int[] _result5 = getInputDeviceIds();
                    reply.writeNoException();
                    reply.writeIntArray(_result5);
                    return true;
                case TRANSACTION_hasKeys /*3*/:
                    boolean[] zArr;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    int[] _arg2 = data.createIntArray();
                    int _arg3_length = data.readInt();
                    if (_arg3_length < 0) {
                        zArr = null;
                    } else {
                        zArr = new boolean[_arg3_length];
                    }
                    _result = hasKeys(_arg0, _arg1, _arg2, zArr);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getInputDevice : 0);
                    reply.writeBooleanArray(zArr);
                    return true;
                case TRANSACTION_tryPointerSpeed /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    tryPointerSpeed(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_injectInputEvent /*5*/:
                    InputEvent inputEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputEvent = (InputEvent) InputEvent.CREATOR.createFromParcel(data);
                    } else {
                        inputEvent = null;
                    }
                    _result = injectInputEvent(inputEvent, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getInputDevice : 0);
                    return true;
                case TRANSACTION_getTouchCalibrationForInputDevice /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    TouchCalibration _result6 = getTouchCalibrationForInputDevice(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_getInputDevice);
                        _result6.writeToParcel(reply, TRANSACTION_getInputDevice);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setTouchCalibrationForInputDevice /*7*/:
                    TouchCalibration touchCalibration;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        touchCalibration = (TouchCalibration) TouchCalibration.CREATOR.createFromParcel(data);
                    } else {
                        touchCalibration = null;
                    }
                    setTouchCalibrationForInputDevice(_arg02, _arg1, touchCalibration);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getKeyboardLayouts /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getKeyboardLayouts();
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, TRANSACTION_getInputDevice);
                    return true;
                case TRANSACTION_getKeyboardLayoutsForInputDevice /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputDeviceIdentifier = (InputDeviceIdentifier) InputDeviceIdentifier.CREATOR.createFromParcel(data);
                    } else {
                        inputDeviceIdentifier = null;
                    }
                    _result2 = getKeyboardLayoutsForInputDevice(inputDeviceIdentifier);
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, TRANSACTION_getInputDevice);
                    return true;
                case TRANSACTION_getKeyboardLayout /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getKeyboardLayout(data.readString());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getInputDevice);
                        _result3.writeToParcel(reply, TRANSACTION_getInputDevice);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getCurrentKeyboardLayoutForInputDevice /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputDeviceIdentifier = (InputDeviceIdentifier) InputDeviceIdentifier.CREATOR.createFromParcel(data);
                    } else {
                        inputDeviceIdentifier = null;
                    }
                    String _result7 = getCurrentKeyboardLayoutForInputDevice(inputDeviceIdentifier);
                    reply.writeNoException();
                    reply.writeString(_result7);
                    return true;
                case TRANSACTION_setCurrentKeyboardLayoutForInputDevice /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputDeviceIdentifier = (InputDeviceIdentifier) InputDeviceIdentifier.CREATOR.createFromParcel(data);
                    } else {
                        inputDeviceIdentifier = null;
                    }
                    setCurrentKeyboardLayoutForInputDevice(inputDeviceIdentifier, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getEnabledKeyboardLayoutsForInputDevice /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputDeviceIdentifier = (InputDeviceIdentifier) InputDeviceIdentifier.CREATOR.createFromParcel(data);
                    } else {
                        inputDeviceIdentifier = null;
                    }
                    String[] _result8 = getEnabledKeyboardLayoutsForInputDevice(inputDeviceIdentifier);
                    reply.writeNoException();
                    reply.writeStringArray(_result8);
                    return true;
                case TRANSACTION_addKeyboardLayoutForInputDevice /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputDeviceIdentifier = (InputDeviceIdentifier) InputDeviceIdentifier.CREATOR.createFromParcel(data);
                    } else {
                        inputDeviceIdentifier = null;
                    }
                    addKeyboardLayoutForInputDevice(inputDeviceIdentifier, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeKeyboardLayoutForInputDevice /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputDeviceIdentifier = (InputDeviceIdentifier) InputDeviceIdentifier.CREATOR.createFromParcel(data);
                    } else {
                        inputDeviceIdentifier = null;
                    }
                    removeKeyboardLayoutForInputDevice(inputDeviceIdentifier, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getKeyboardLayoutForInputDevice /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputDeviceIdentifier = (InputDeviceIdentifier) InputDeviceIdentifier.CREATOR.createFromParcel(data);
                    } else {
                        inputDeviceIdentifier = null;
                    }
                    if (data.readInt() != 0) {
                        inputMethodInfo = (InputMethodInfo) InputMethodInfo.CREATOR.createFromParcel(data);
                    } else {
                        inputMethodInfo = null;
                    }
                    if (data.readInt() != 0) {
                        inputMethodSubtype = (InputMethodSubtype) InputMethodSubtype.CREATOR.createFromParcel(data);
                    } else {
                        inputMethodSubtype = null;
                    }
                    _result3 = getKeyboardLayoutForInputDevice(inputDeviceIdentifier, inputMethodInfo, inputMethodSubtype);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getInputDevice);
                        _result3.writeToParcel(reply, TRANSACTION_getInputDevice);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setKeyboardLayoutForInputDevice /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputDeviceIdentifier = (InputDeviceIdentifier) InputDeviceIdentifier.CREATOR.createFromParcel(data);
                    } else {
                        inputDeviceIdentifier = null;
                    }
                    if (data.readInt() != 0) {
                        inputMethodInfo = (InputMethodInfo) InputMethodInfo.CREATOR.createFromParcel(data);
                    } else {
                        inputMethodInfo = null;
                    }
                    if (data.readInt() != 0) {
                        inputMethodSubtype = (InputMethodSubtype) InputMethodSubtype.CREATOR.createFromParcel(data);
                    } else {
                        inputMethodSubtype = null;
                    }
                    setKeyboardLayoutForInputDevice(inputDeviceIdentifier, inputMethodInfo, inputMethodSubtype, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerInputDevicesChangedListener /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerInputDevicesChangedListener(android.hardware.input.IInputDevicesChangedListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isInTabletMode /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result9 = isInTabletMode();
                    reply.writeNoException();
                    reply.writeInt(_result9);
                    return true;
                case TRANSACTION_registerTabletModeChangedListener /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerTabletModeChangedListener(android.hardware.input.ITabletModeChangedListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_vibrate /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    vibrate(data.readInt(), data.createLongArray(), data.readInt(), data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelVibrate /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelVibrate(data.readInt(), data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setPointerIconType /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPointerIconType(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setCustomPointerIcon /*24*/:
                    PointerIcon pointerIcon;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        pointerIcon = (PointerIcon) PointerIcon.CREATOR.createFromParcel(data);
                    } else {
                        pointerIcon = null;
                    }
                    setCustomPointerIcon(pointerIcon);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setMirrorLinkInputStatus /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    setMirrorLinkInputStatus(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addKeyboardLayoutForInputDevice(InputDeviceIdentifier inputDeviceIdentifier, String str) throws RemoteException;

    void cancelVibrate(int i, IBinder iBinder) throws RemoteException;

    String getCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier inputDeviceIdentifier) throws RemoteException;

    String[] getEnabledKeyboardLayoutsForInputDevice(InputDeviceIdentifier inputDeviceIdentifier) throws RemoteException;

    InputDevice getInputDevice(int i) throws RemoteException;

    int[] getInputDeviceIds() throws RemoteException;

    KeyboardLayout getKeyboardLayout(String str) throws RemoteException;

    KeyboardLayout getKeyboardLayoutForInputDevice(InputDeviceIdentifier inputDeviceIdentifier, InputMethodInfo inputMethodInfo, InputMethodSubtype inputMethodSubtype) throws RemoteException;

    KeyboardLayout[] getKeyboardLayouts() throws RemoteException;

    KeyboardLayout[] getKeyboardLayoutsForInputDevice(InputDeviceIdentifier inputDeviceIdentifier) throws RemoteException;

    TouchCalibration getTouchCalibrationForInputDevice(String str, int i) throws RemoteException;

    boolean hasKeys(int i, int i2, int[] iArr, boolean[] zArr) throws RemoteException;

    boolean injectInputEvent(InputEvent inputEvent, int i) throws RemoteException;

    int isInTabletMode() throws RemoteException;

    void registerInputDevicesChangedListener(IInputDevicesChangedListener iInputDevicesChangedListener) throws RemoteException;

    void registerTabletModeChangedListener(ITabletModeChangedListener iTabletModeChangedListener) throws RemoteException;

    void removeKeyboardLayoutForInputDevice(InputDeviceIdentifier inputDeviceIdentifier, String str) throws RemoteException;

    void setCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier inputDeviceIdentifier, String str) throws RemoteException;

    void setCustomPointerIcon(PointerIcon pointerIcon) throws RemoteException;

    void setKeyboardLayoutForInputDevice(InputDeviceIdentifier inputDeviceIdentifier, InputMethodInfo inputMethodInfo, InputMethodSubtype inputMethodSubtype, String str) throws RemoteException;

    void setMirrorLinkInputStatus(boolean z) throws RemoteException;

    void setPointerIconType(int i) throws RemoteException;

    void setTouchCalibrationForInputDevice(String str, int i, TouchCalibration touchCalibration) throws RemoteException;

    void tryPointerSpeed(int i) throws RemoteException;

    void vibrate(int i, long[] jArr, int i2, IBinder iBinder) throws RemoteException;
}
