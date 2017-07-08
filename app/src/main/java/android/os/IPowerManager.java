package android.os;

public interface IPowerManager extends IInterface {

    public static abstract class Stub extends Binder implements IPowerManager {
        private static final String DESCRIPTOR = "android.os.IPowerManager";
        static final int TRANSACTION_acquireWakeLock = 1;
        static final int TRANSACTION_acquireWakeLockWithUid = 2;
        static final int TRANSACTION_boostScreenBrightness = 22;
        static final int TRANSACTION_convertBrightnessToSeekbarPercentage = 44;
        static final int TRANSACTION_convertSeekbarProgressToBrightness = 43;
        static final int TRANSACTION_crash = 20;
        static final int TRANSACTION_goToSleep = 10;
        static final int TRANSACTION_isDeviceIdleMode = 15;
        static final int TRANSACTION_isHighPrecision = 31;
        static final int TRANSACTION_isInteractive = 12;
        static final int TRANSACTION_isLightDeviceIdleMode = 16;
        static final int TRANSACTION_isPowerSaveMode = 13;
        static final int TRANSACTION_isScreenBrightnessBoosted = 23;
        static final int TRANSACTION_isUsingSkipWakeLock = 38;
        static final int TRANSACTION_isWakeLockLevelSupported = 7;
        static final int TRANSACTION_nap = 11;
        static final int TRANSACTION_powerHint = 5;
        static final int TRANSACTION_reboot = 17;
        static final int TRANSACTION_rebootSafeMode = 18;
        static final int TRANSACTION_regeditAodStateCallback = 41;
        static final int TRANSACTION_releaseWakeLock = 3;
        static final int TRANSACTION_setAodState = 39;
        static final int TRANSACTION_setAttentionLight = 29;
        static final int TRANSACTION_setColorTemperature = 27;
        static final int TRANSACTION_setDozeOverrideFromAod = 40;
        static final int TRANSACTION_setMirrorLinkPowerStatus = 37;
        static final int TRANSACTION_setPowerSaveMode = 14;
        static final int TRANSACTION_setStartDreamFromOtherFlag = 34;
        static final int TRANSACTION_setStayOnSetting = 21;
        static final int TRANSACTION_setTemporaryScreenAutoBrightnessAdjustmentSettingOverride = 26;
        static final int TRANSACTION_setTemporaryScreenAutoBrightnessSettingOverride = 25;
        static final int TRANSACTION_setTemporaryScreenBrightnessSettingOverride = 24;
        static final int TRANSACTION_shutdown = 19;
        static final int TRANSACTION_startDream = 35;
        static final int TRANSACTION_startWakeUpReady = 32;
        static final int TRANSACTION_stopDream = 36;
        static final int TRANSACTION_stopWakeUpReady = 33;
        static final int TRANSACTION_unregeditAodStateCallback = 42;
        static final int TRANSACTION_updateBlockedUids = 30;
        static final int TRANSACTION_updateRgbGamma = 28;
        static final int TRANSACTION_updateWakeLockUids = 4;
        static final int TRANSACTION_updateWakeLockWorkSource = 6;
        static final int TRANSACTION_userActivity = 8;
        static final int TRANSACTION_wakeUp = 9;

        private static class Proxy implements IPowerManager {
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

            public void acquireWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    _data.writeInt(flags);
                    _data.writeString(tag);
                    _data.writeString(packageName);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_acquireWakeLock);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(historyTag);
                    this.mRemote.transact(Stub.TRANSACTION_acquireWakeLock, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void acquireWakeLockWithUid(IBinder lock, int flags, String tag, String packageName, int uidtoblame) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    _data.writeInt(flags);
                    _data.writeString(tag);
                    _data.writeString(packageName);
                    _data.writeInt(uidtoblame);
                    this.mRemote.transact(Stub.TRANSACTION_acquireWakeLockWithUid, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void releaseWakeLock(IBinder lock, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_releaseWakeLock, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateWakeLockUids(IBinder lock, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    _data.writeIntArray(uids);
                    this.mRemote.transact(Stub.TRANSACTION_updateWakeLockUids, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void powerHint(int hintId, int data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hintId);
                    _data.writeInt(data);
                    this.mRemote.transact(Stub.TRANSACTION_powerHint, _data, null, Stub.TRANSACTION_acquireWakeLock);
                } finally {
                    _data.recycle();
                }
            }

            public void updateWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_acquireWakeLock);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(historyTag);
                    this.mRemote.transact(Stub.TRANSACTION_updateWakeLockWorkSource, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isWakeLockLevelSupported(int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    this.mRemote.transact(Stub.TRANSACTION_isWakeLockLevelSupported, _data, _reply, 0);
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

            public void userActivity(long time, int event, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    _data.writeInt(event);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_userActivity, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void wakeUp(long time, String reason, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    _data.writeString(reason);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_wakeUp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void goToSleep(long time, int reason, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    _data.writeInt(reason);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_goToSleep, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void nap(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    this.mRemote.transact(Stub.TRANSACTION_nap, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isInteractive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isInteractive, _data, _reply, 0);
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

            public boolean isPowerSaveMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isPowerSaveMode, _data, _reply, 0);
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

            public boolean setPowerSaveMode(boolean mode) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mode) {
                        i = Stub.TRANSACTION_acquireWakeLock;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setPowerSaveMode, _data, _reply, 0);
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

            public boolean isDeviceIdleMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isDeviceIdleMode, _data, _reply, 0);
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

            public boolean isLightDeviceIdleMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isLightDeviceIdleMode, _data, _reply, 0);
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

            public void reboot(boolean confirm, String reason, boolean wait) throws RemoteException {
                int i = Stub.TRANSACTION_acquireWakeLock;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (confirm) {
                        i2 = Stub.TRANSACTION_acquireWakeLock;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    _data.writeString(reason);
                    if (!wait) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_reboot, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void rebootSafeMode(boolean confirm, boolean wait) throws RemoteException {
                int i = Stub.TRANSACTION_acquireWakeLock;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (confirm) {
                        i2 = Stub.TRANSACTION_acquireWakeLock;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!wait) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_rebootSafeMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shutdown(boolean confirm, String reason, boolean wait) throws RemoteException {
                int i = Stub.TRANSACTION_acquireWakeLock;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (confirm) {
                        i2 = Stub.TRANSACTION_acquireWakeLock;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    _data.writeString(reason);
                    if (!wait) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_shutdown, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void crash(String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(message);
                    this.mRemote.transact(Stub.TRANSACTION_crash, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setStayOnSetting(int val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(val);
                    this.mRemote.transact(Stub.TRANSACTION_setStayOnSetting, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void boostScreenBrightness(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    this.mRemote.transact(Stub.TRANSACTION_boostScreenBrightness, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isScreenBrightnessBoosted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isScreenBrightnessBoosted, _data, _reply, 0);
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

            public void setTemporaryScreenBrightnessSettingOverride(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    this.mRemote.transact(Stub.TRANSACTION_setTemporaryScreenBrightnessSettingOverride, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTemporaryScreenAutoBrightnessSettingOverride(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    this.mRemote.transact(Stub.TRANSACTION_setTemporaryScreenAutoBrightnessSettingOverride, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(float adj) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(adj);
                    this.mRemote.transact(Stub.TRANSACTION_setTemporaryScreenAutoBrightnessAdjustmentSettingOverride, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setColorTemperature(int colorTemper) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(colorTemper);
                    this.mRemote.transact(Stub.TRANSACTION_setColorTemperature, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateRgbGamma(float red, float green, float blue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(red);
                    _data.writeFloat(green);
                    _data.writeFloat(blue);
                    this.mRemote.transact(Stub.TRANSACTION_updateRgbGamma, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAttentionLight(boolean on, int color) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (on) {
                        i = Stub.TRANSACTION_acquireWakeLock;
                    }
                    _data.writeInt(i);
                    _data.writeInt(color);
                    this.mRemote.transact(Stub.TRANSACTION_setAttentionLight, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateBlockedUids(int uid, boolean isBlocked) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (isBlocked) {
                        i = Stub.TRANSACTION_acquireWakeLock;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_updateBlockedUids, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isHighPrecision() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isHighPrecision, _data, _reply, 0);
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

            public void startWakeUpReady(long eventTime, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(eventTime);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_startWakeUpReady, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopWakeUpReady(long eventTime, boolean enableBright, String opPackageName) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(eventTime);
                    if (enableBright) {
                        i = Stub.TRANSACTION_acquireWakeLock;
                    }
                    _data.writeInt(i);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_stopWakeUpReady, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setStartDreamFromOtherFlag(boolean flag) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (flag) {
                        i = Stub.TRANSACTION_acquireWakeLock;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setStartDreamFromOtherFlag, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startDream() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_startDream, _data, _reply, 0);
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

            public boolean stopDream() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopDream, _data, _reply, 0);
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

            public void setMirrorLinkPowerStatus(boolean status) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (status) {
                        i = Stub.TRANSACTION_acquireWakeLock;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setMirrorLinkPowerStatus, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUsingSkipWakeLock(int uid, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(tag);
                    this.mRemote.transact(Stub.TRANSACTION_isUsingSkipWakeLock, _data, _reply, 0);
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

            public void setAodState(int globalState, int alpmMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(globalState);
                    _data.writeInt(alpmMode);
                    this.mRemote.transact(Stub.TRANSACTION_setAodState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDozeOverrideFromAod(int screenState, int screenBrightness, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(screenState);
                    _data.writeInt(screenBrightness);
                    _data.writeStrongBinder(binder);
                    this.mRemote.transact(Stub.TRANSACTION_setDozeOverrideFromAod, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void regeditAodStateCallback(IAodStateCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_regeditAodStateCallback, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregeditAodStateCallback(IAodStateCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregeditAodStateCallback, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int convertSeekbarProgressToBrightness(int progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(progress);
                    this.mRemote.transact(Stub.TRANSACTION_convertSeekbarProgressToBrightness, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float convertBrightnessToSeekbarPercentage(float brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(brightness);
                    this.mRemote.transact(Stub.TRANSACTION_convertBrightnessToSeekbarPercentage, _data, _reply, 0);
                    _reply.readException();
                    float _result = _reply.readFloat();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPowerManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPowerManager)) {
                return new Proxy(obj);
            }
            return (IPowerManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder _arg0;
            boolean _result;
            int _result2;
            switch (code) {
                case TRANSACTION_acquireWakeLock /*1*/:
                    WorkSource workSource;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    int _arg1 = data.readInt();
                    String _arg2 = data.readString();
                    String _arg3 = data.readString();
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    acquireWakeLock(_arg0, _arg1, _arg2, _arg3, workSource, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_acquireWakeLockWithUid /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    acquireWakeLockWithUid(data.readStrongBinder(), data.readInt(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_releaseWakeLock /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    releaseWakeLock(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateWakeLockUids /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateWakeLockUids(data.readStrongBinder(), data.createIntArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_powerHint /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    powerHint(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_updateWakeLockWorkSource /*6*/:
                    WorkSource workSource2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        workSource2 = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource2 = null;
                    }
                    updateWakeLockWorkSource(_arg0, workSource2, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isWakeLockLevelSupported /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isWakeLockLevelSupported(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_acquireWakeLock : 0);
                    return true;
                case TRANSACTION_userActivity /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    userActivity(data.readLong(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_wakeUp /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    wakeUp(data.readLong(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_goToSleep /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    goToSleep(data.readLong(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_nap /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    nap(data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isInteractive /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isInteractive();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_acquireWakeLock : 0);
                    return true;
                case TRANSACTION_isPowerSaveMode /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isPowerSaveMode();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_acquireWakeLock : 0);
                    return true;
                case TRANSACTION_setPowerSaveMode /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setPowerSaveMode(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_acquireWakeLock : 0);
                    return true;
                case TRANSACTION_isDeviceIdleMode /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isDeviceIdleMode();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_acquireWakeLock : 0);
                    return true;
                case TRANSACTION_isLightDeviceIdleMode /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isLightDeviceIdleMode();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_acquireWakeLock : 0);
                    return true;
                case TRANSACTION_reboot /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    reboot(data.readInt() != 0, data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_rebootSafeMode /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    rebootSafeMode(data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_shutdown /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    shutdown(data.readInt() != 0, data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_crash /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    crash(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setStayOnSetting /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    setStayOnSetting(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_boostScreenBrightness /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    boostScreenBrightness(data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isScreenBrightnessBoosted /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isScreenBrightnessBoosted();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_acquireWakeLock : 0);
                    return true;
                case TRANSACTION_setTemporaryScreenBrightnessSettingOverride /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    setTemporaryScreenBrightnessSettingOverride(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setTemporaryScreenAutoBrightnessSettingOverride /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    setTemporaryScreenAutoBrightnessSettingOverride(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setTemporaryScreenAutoBrightnessAdjustmentSettingOverride /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(data.readFloat());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setColorTemperature /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setColorTemperature(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_updateRgbGamma /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateRgbGamma(data.readFloat(), data.readFloat(), data.readFloat());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setAttentionLight /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAttentionLight(data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateBlockedUids /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateBlockedUids(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isHighPrecision /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isHighPrecision();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_acquireWakeLock : 0);
                    return true;
                case TRANSACTION_startWakeUpReady /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    startWakeUpReady(data.readLong(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopWakeUpReady /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopWakeUpReady(data.readLong(), data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setStartDreamFromOtherFlag /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    setStartDreamFromOtherFlag(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startDream /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = startDream();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_acquireWakeLock : 0);
                    return true;
                case TRANSACTION_stopDream /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = stopDream();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_acquireWakeLock : 0);
                    return true;
                case TRANSACTION_setMirrorLinkPowerStatus /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    setMirrorLinkPowerStatus(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isUsingSkipWakeLock /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isUsingSkipWakeLock(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_acquireWakeLock : 0);
                    return true;
                case TRANSACTION_setAodState /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAodState(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setDozeOverrideFromAod /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDozeOverrideFromAod(data.readInt(), data.readInt(), data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_regeditAodStateCallback /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    regeditAodStateCallback(android.os.IAodStateCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregeditAodStateCallback /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregeditAodStateCallback(android.os.IAodStateCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_convertSeekbarProgressToBrightness /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = convertSeekbarProgressToBrightness(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_convertBrightnessToSeekbarPercentage /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    float _result3 = convertBrightnessToSeekbarPercentage(data.readFloat());
                    reply.writeNoException();
                    reply.writeFloat(_result3);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void acquireWakeLock(IBinder iBinder, int i, String str, String str2, WorkSource workSource, String str3) throws RemoteException;

    void acquireWakeLockWithUid(IBinder iBinder, int i, String str, String str2, int i2) throws RemoteException;

    void boostScreenBrightness(long j) throws RemoteException;

    float convertBrightnessToSeekbarPercentage(float f) throws RemoteException;

    int convertSeekbarProgressToBrightness(int i) throws RemoteException;

    void crash(String str) throws RemoteException;

    void goToSleep(long j, int i, int i2) throws RemoteException;

    boolean isDeviceIdleMode() throws RemoteException;

    boolean isHighPrecision() throws RemoteException;

    boolean isInteractive() throws RemoteException;

    boolean isLightDeviceIdleMode() throws RemoteException;

    boolean isPowerSaveMode() throws RemoteException;

    boolean isScreenBrightnessBoosted() throws RemoteException;

    boolean isUsingSkipWakeLock(int i, String str) throws RemoteException;

    boolean isWakeLockLevelSupported(int i) throws RemoteException;

    void nap(long j) throws RemoteException;

    void powerHint(int i, int i2) throws RemoteException;

    void reboot(boolean z, String str, boolean z2) throws RemoteException;

    void rebootSafeMode(boolean z, boolean z2) throws RemoteException;

    void regeditAodStateCallback(IAodStateCallback iAodStateCallback) throws RemoteException;

    void releaseWakeLock(IBinder iBinder, int i) throws RemoteException;

    void setAodState(int i, int i2) throws RemoteException;

    void setAttentionLight(boolean z, int i) throws RemoteException;

    int setColorTemperature(int i) throws RemoteException;

    void setDozeOverrideFromAod(int i, int i2, IBinder iBinder) throws RemoteException;

    void setMirrorLinkPowerStatus(boolean z) throws RemoteException;

    boolean setPowerSaveMode(boolean z) throws RemoteException;

    void setStartDreamFromOtherFlag(boolean z) throws RemoteException;

    void setStayOnSetting(int i) throws RemoteException;

    void setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(float f) throws RemoteException;

    void setTemporaryScreenAutoBrightnessSettingOverride(int i) throws RemoteException;

    void setTemporaryScreenBrightnessSettingOverride(int i) throws RemoteException;

    void shutdown(boolean z, String str, boolean z2) throws RemoteException;

    boolean startDream() throws RemoteException;

    void startWakeUpReady(long j, String str) throws RemoteException;

    boolean stopDream() throws RemoteException;

    void stopWakeUpReady(long j, boolean z, String str) throws RemoteException;

    void unregeditAodStateCallback(IAodStateCallback iAodStateCallback) throws RemoteException;

    void updateBlockedUids(int i, boolean z) throws RemoteException;

    int updateRgbGamma(float f, float f2, float f3) throws RemoteException;

    void updateWakeLockUids(IBinder iBinder, int[] iArr) throws RemoteException;

    void updateWakeLockWorkSource(IBinder iBinder, WorkSource workSource, String str) throws RemoteException;

    void userActivity(long j, int i, int i2) throws RemoteException;

    void wakeUp(long j, String str, String str2) throws RemoteException;
}
