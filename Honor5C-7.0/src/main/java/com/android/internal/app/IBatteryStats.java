package com.android.internal.app;

import android.bluetooth.BluetoothActivityEnergyInfo;
import android.net.wifi.WifiActivityEnergyInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.WorkSource;
import android.os.health.HealthStatsParceler;
import android.telephony.ModemActivityInfo;
import android.telephony.SignalStrength;

public interface IBatteryStats extends IInterface {

    public static abstract class Stub extends Binder implements IBatteryStats {
        private static final String DESCRIPTOR = "com.android.internal.app.IBatteryStats";
        static final int TRANSACTION_computeBatteryTimeRemaining = 18;
        static final int TRANSACTION_computeChargeTimeRemaining = 19;
        static final int TRANSACTION_getAwakeTimeBattery = 73;
        static final int TRANSACTION_getAwakeTimePlugged = 74;
        static final int TRANSACTION_getStatistics = 15;
        static final int TRANSACTION_getStatisticsStream = 16;
        static final int TRANSACTION_isCharging = 17;
        static final int TRANSACTION_noteBleScanStarted = 75;
        static final int TRANSACTION_noteBleScanStopped = 76;
        static final int TRANSACTION_noteBluetoothControllerActivity = 80;
        static final int TRANSACTION_noteChangeWakelockFromSource = 28;
        static final int TRANSACTION_noteConnectivityChanged = 39;
        static final int TRANSACTION_noteDeviceIdleMode = 71;
        static final int TRANSACTION_noteEvent = 20;
        static final int TRANSACTION_noteFlashlightOff = 10;
        static final int TRANSACTION_noteFlashlightOn = 9;
        static final int TRANSACTION_noteFullWifiLockAcquired = 54;
        static final int TRANSACTION_noteFullWifiLockAcquiredFromSource = 60;
        static final int TRANSACTION_noteFullWifiLockReleased = 55;
        static final int TRANSACTION_noteFullWifiLockReleasedFromSource = 61;
        static final int TRANSACTION_noteInteractive = 38;
        static final int TRANSACTION_noteJobFinish = 24;
        static final int TRANSACTION_noteJobStart = 23;
        static final int TRANSACTION_noteMobileRadioPowerState = 40;
        static final int TRANSACTION_noteModemControllerActivity = 81;
        static final int TRANSACTION_noteNetworkInterfaceType = 69;
        static final int TRANSACTION_noteNetworkStatsEnabled = 70;
        static final int TRANSACTION_notePhoneDataConnectionState = 44;
        static final int TRANSACTION_notePhoneOff = 42;
        static final int TRANSACTION_notePhoneOn = 41;
        static final int TRANSACTION_notePhoneSignalStrength = 43;
        static final int TRANSACTION_notePhoneState = 45;
        static final int TRANSACTION_noteResetAudio = 8;
        static final int TRANSACTION_noteResetBleScan = 77;
        static final int TRANSACTION_noteResetCamera = 13;
        static final int TRANSACTION_noteResetFlashlight = 14;
        static final int TRANSACTION_noteResetVideo = 7;
        static final int TRANSACTION_noteScreenBrightness = 35;
        static final int TRANSACTION_noteScreenState = 34;
        static final int TRANSACTION_noteStartAudio = 5;
        static final int TRANSACTION_noteStartCamera = 11;
        static final int TRANSACTION_noteStartGps = 32;
        static final int TRANSACTION_noteStartSensor = 1;
        static final int TRANSACTION_noteStartVideo = 3;
        static final int TRANSACTION_noteStartWakelock = 25;
        static final int TRANSACTION_noteStartWakelockFromSource = 27;
        static final int TRANSACTION_noteStopAudio = 6;
        static final int TRANSACTION_noteStopCamera = 12;
        static final int TRANSACTION_noteStopGps = 33;
        static final int TRANSACTION_noteStopSensor = 2;
        static final int TRANSACTION_noteStopVideo = 4;
        static final int TRANSACTION_noteStopWakelock = 26;
        static final int TRANSACTION_noteStopWakelockFromSource = 29;
        static final int TRANSACTION_noteSyncFinish = 22;
        static final int TRANSACTION_noteSyncStart = 21;
        static final int TRANSACTION_noteUserActivity = 36;
        static final int TRANSACTION_noteVibratorOff = 31;
        static final int TRANSACTION_noteVibratorOn = 30;
        static final int TRANSACTION_noteWakeUp = 37;
        static final int TRANSACTION_noteWifiBatchedScanStartedFromSource = 64;
        static final int TRANSACTION_noteWifiBatchedScanStoppedFromSource = 65;
        static final int TRANSACTION_noteWifiControllerActivity = 82;
        static final int TRANSACTION_noteWifiMulticastDisabled = 59;
        static final int TRANSACTION_noteWifiMulticastDisabledFromSource = 67;
        static final int TRANSACTION_noteWifiMulticastEnabled = 58;
        static final int TRANSACTION_noteWifiMulticastEnabledFromSource = 66;
        static final int TRANSACTION_noteWifiOff = 47;
        static final int TRANSACTION_noteWifiOn = 46;
        static final int TRANSACTION_noteWifiRadioPowerState = 68;
        static final int TRANSACTION_noteWifiRssiChanged = 53;
        static final int TRANSACTION_noteWifiRunning = 48;
        static final int TRANSACTION_noteWifiRunningChanged = 49;
        static final int TRANSACTION_noteWifiScanStarted = 56;
        static final int TRANSACTION_noteWifiScanStartedFromSource = 62;
        static final int TRANSACTION_noteWifiScanStopped = 57;
        static final int TRANSACTION_noteWifiScanStoppedFromSource = 63;
        static final int TRANSACTION_noteWifiState = 51;
        static final int TRANSACTION_noteWifiStopped = 50;
        static final int TRANSACTION_noteWifiSupplicantStateChanged = 52;
        static final int TRANSACTION_setBatteryState = 72;
        static final int TRANSACTION_takeUidSnapshot = 78;
        static final int TRANSACTION_takeUidSnapshots = 79;

        private static class Proxy implements IBatteryStats {
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

            public void noteStartSensor(int uid, int sensor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(sensor);
                    this.mRemote.transact(Stub.TRANSACTION_noteStartSensor, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStopSensor(int uid, int sensor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(sensor);
                    this.mRemote.transact(Stub.TRANSACTION_noteStopSensor, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStartVideo(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteStartVideo, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStopVideo(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteStopVideo, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStartAudio(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteStartAudio, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStopAudio(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteStopAudio, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteResetVideo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_noteResetVideo, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteResetAudio() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_noteResetAudio, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteFlashlightOn(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteFlashlightOn, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteFlashlightOff(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteFlashlightOff, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStartCamera(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteStartCamera, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStopCamera(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteStopCamera, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteResetCamera() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_noteResetCamera, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteResetFlashlight() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_noteResetFlashlight, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getStatistics() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getStatistics, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor getStatisticsStream() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getStatisticsStream, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCharging() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isCharging, _data, _reply, 0);
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

            public long computeBatteryTimeRemaining() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_computeBatteryTimeRemaining, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long computeChargeTimeRemaining() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_computeChargeTimeRemaining, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteEvent(int code, String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(code);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteEvent, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteSyncStart(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteSyncStart, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteSyncFinish(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteSyncFinish, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteJobStart(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteJobStart, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteJobFinish(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteJobFinish, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStartWakelock(int uid, int pid, String name, String historyName, int type, boolean unimportantForLogging) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeString(name);
                    _data.writeString(historyName);
                    _data.writeInt(type);
                    if (unimportantForLogging) {
                        i = Stub.TRANSACTION_noteStartSensor;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_noteStartWakelock, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStopWakelock(int uid, int pid, String name, String historyName, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeString(name);
                    _data.writeString(historyName);
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_noteStopWakelock, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStartWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type, boolean unimportantForLogging) throws RemoteException {
                int i = Stub.TRANSACTION_noteStartSensor;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(pid);
                    _data.writeString(name);
                    _data.writeString(historyName);
                    _data.writeInt(type);
                    if (!unimportantForLogging) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_noteStartWakelockFromSource, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteChangeWakelockFromSource(WorkSource ws, int pid, String name, String histyoryName, int type, WorkSource newWs, int newPid, String newName, String newHistoryName, int newType, boolean newUnimportantForLogging) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(pid);
                    _data.writeString(name);
                    _data.writeString(histyoryName);
                    _data.writeInt(type);
                    if (newWs != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        newWs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(newPid);
                    _data.writeString(newName);
                    _data.writeString(newHistoryName);
                    _data.writeInt(newType);
                    _data.writeInt(newUnimportantForLogging ? Stub.TRANSACTION_noteStartSensor : 0);
                    this.mRemote.transact(Stub.TRANSACTION_noteChangeWakelockFromSource, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStopWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(pid);
                    _data.writeString(name);
                    _data.writeString(historyName);
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_noteStopWakelockFromSource, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteVibratorOn(int uid, long durationMillis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeLong(durationMillis);
                    this.mRemote.transact(Stub.TRANSACTION_noteVibratorOn, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteVibratorOff(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteVibratorOff, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStartGps(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteStartGps, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStopGps(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteStopGps, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteScreenState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(Stub.TRANSACTION_noteScreenState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteScreenBrightness(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    this.mRemote.transact(Stub.TRANSACTION_noteScreenBrightness, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteUserActivity(int uid, int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(event);
                    this.mRemote.transact(Stub.TRANSACTION_noteUserActivity, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWakeUp(String reason, int reasonUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    _data.writeInt(reasonUid);
                    this.mRemote.transact(Stub.TRANSACTION_noteWakeUp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteInteractive(boolean interactive) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (interactive) {
                        i = Stub.TRANSACTION_noteStartSensor;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_noteInteractive, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteConnectivityChanged(int type, String extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(extra);
                    this.mRemote.transact(Stub.TRANSACTION_noteConnectivityChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteMobileRadioPowerState(int powerState, long timestampNs, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(powerState);
                    _data.writeLong(timestampNs);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteMobileRadioPowerState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notePhoneOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_notePhoneOn, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notePhoneOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_notePhoneOff, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notePhoneSignalStrength(SignalStrength signalStrength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (signalStrength != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        signalStrength.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_notePhoneSignalStrength, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notePhoneDataConnectionState(int dataType, boolean hasData) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dataType);
                    if (hasData) {
                        i = Stub.TRANSACTION_noteStartSensor;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_notePhoneDataConnectionState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notePhoneState(int phoneState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneState);
                    this.mRemote.transact(Stub.TRANSACTION_notePhoneState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiOn, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiOff, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiRunning(WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiRunning, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiRunningChanged(WorkSource oldWs, WorkSource newWs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oldWs != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        oldWs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (newWs != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        newWs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiRunningChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiStopped(WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiStopped, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiState(int wifiState, String accessPoint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(wifiState);
                    _data.writeString(accessPoint);
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiSupplicantStateChanged(int supplState, boolean failedAuth) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(supplState);
                    if (failedAuth) {
                        i = Stub.TRANSACTION_noteStartSensor;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiSupplicantStateChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiRssiChanged(int newRssi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newRssi);
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiRssiChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteFullWifiLockAcquired(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteFullWifiLockAcquired, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteFullWifiLockReleased(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteFullWifiLockReleased, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiScanStarted(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiScanStarted, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiScanStopped(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiScanStopped, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiMulticastEnabled(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiMulticastEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiMulticastDisabled(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiMulticastDisabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteFullWifiLockAcquiredFromSource(WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteFullWifiLockAcquiredFromSource, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteFullWifiLockReleasedFromSource(WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteFullWifiLockReleasedFromSource, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiScanStartedFromSource(WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiScanStartedFromSource, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiScanStoppedFromSource(WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiScanStoppedFromSource, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiBatchedScanStartedFromSource(WorkSource ws, int csph) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(csph);
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiBatchedScanStartedFromSource, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiBatchedScanStoppedFromSource(WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiBatchedScanStoppedFromSource, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiMulticastEnabledFromSource(WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiMulticastEnabledFromSource, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiMulticastDisabledFromSource(WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiMulticastDisabledFromSource, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiRadioPowerState(int powerState, long timestampNs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(powerState);
                    _data.writeLong(timestampNs);
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiRadioPowerState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteNetworkInterfaceType(String iface, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_noteNetworkInterfaceType, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteNetworkStatsEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_noteNetworkStatsEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteDeviceIdleMode(int mode, String activeReason, int activeUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeString(activeReason);
                    _data.writeInt(activeUid);
                    this.mRemote.transact(Stub.TRANSACTION_noteDeviceIdleMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBatteryState(int status, int health, int plugType, int level, int temp, int volt, int chargeUAh) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(health);
                    _data.writeInt(plugType);
                    _data.writeInt(level);
                    _data.writeInt(temp);
                    _data.writeInt(volt);
                    _data.writeInt(chargeUAh);
                    this.mRemote.transact(Stub.TRANSACTION_setBatteryState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getAwakeTimeBattery() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAwakeTimeBattery, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getAwakeTimePlugged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAwakeTimePlugged, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteBleScanStarted(WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteBleScanStarted, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteBleScanStopped(WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteBleScanStopped, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteResetBleScan() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_noteResetBleScan, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HealthStatsParceler takeUidSnapshot(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    HealthStatsParceler healthStatsParceler;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_takeUidSnapshot, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        healthStatsParceler = (HealthStatsParceler) HealthStatsParceler.CREATOR.createFromParcel(_reply);
                    } else {
                        healthStatsParceler = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return healthStatsParceler;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HealthStatsParceler[] takeUidSnapshots(int[] uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(uid);
                    this.mRemote.transact(Stub.TRANSACTION_takeUidSnapshots, _data, _reply, 0);
                    _reply.readException();
                    HealthStatsParceler[] _result = (HealthStatsParceler[]) _reply.createTypedArray(HealthStatsParceler.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteBluetoothControllerActivity(BluetoothActivityEnergyInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteBluetoothControllerActivity, _data, null, Stub.TRANSACTION_noteStartSensor);
                } finally {
                    _data.recycle();
                }
            }

            public void noteModemControllerActivity(ModemActivityInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteModemControllerActivity, _data, null, Stub.TRANSACTION_noteStartSensor);
                } finally {
                    _data.recycle();
                }
            }

            public void noteWifiControllerActivity(WifiActivityEnergyInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(Stub.TRANSACTION_noteStartSensor);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_noteWifiControllerActivity, _data, null, Stub.TRANSACTION_noteStartSensor);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBatteryStats asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBatteryStats)) {
                return new Proxy(obj);
            }
            return (IBatteryStats) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            long _result;
            WorkSource workSource;
            switch (code) {
                case TRANSACTION_noteStartSensor /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteStartSensor(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStopSensor /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteStopSensor(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStartVideo /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteStartVideo(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStopVideo /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteStopVideo(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStartAudio /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteStartAudio(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStopAudio /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteStopAudio(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteResetVideo /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteResetVideo();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteResetAudio /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteResetAudio();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteFlashlightOn /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteFlashlightOn(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteFlashlightOff /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteFlashlightOff(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStartCamera /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteStartCamera(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStopCamera /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteStopCamera(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteResetCamera /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteResetCamera();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteResetFlashlight /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteResetFlashlight();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getStatistics /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result2 = getStatistics();
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case TRANSACTION_getStatisticsStream /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    ParcelFileDescriptor _result3 = getStatisticsStream();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_noteStartSensor);
                        _result3.writeToParcel(reply, TRANSACTION_noteStartSensor);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isCharging /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result4 = isCharging();
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_noteStartSensor : 0);
                    return true;
                case TRANSACTION_computeBatteryTimeRemaining /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = computeBatteryTimeRemaining();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_computeChargeTimeRemaining /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = computeChargeTimeRemaining();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_noteEvent /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteEvent(data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteSyncStart /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteSyncStart(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteSyncFinish /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteSyncFinish(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteJobStart /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteJobStart(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteJobFinish /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteJobFinish(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStartWakelock /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteStartWakelock(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStopWakelock /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteStopWakelock(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStartWakelockFromSource /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteStartWakelockFromSource(workSource, data.readInt(), data.readString(), data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteChangeWakelockFromSource /*28*/:
                    WorkSource workSource2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    int _arg1 = data.readInt();
                    String _arg2 = data.readString();
                    String _arg3 = data.readString();
                    int _arg4 = data.readInt();
                    if (data.readInt() != 0) {
                        workSource2 = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource2 = null;
                    }
                    noteChangeWakelockFromSource(workSource, _arg1, _arg2, _arg3, _arg4, workSource2, data.readInt(), data.readString(), data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStopWakelockFromSource /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteStopWakelockFromSource(workSource, data.readInt(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteVibratorOn /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteVibratorOn(data.readInt(), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteVibratorOff /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteVibratorOff(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStartGps /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteStartGps(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteStopGps /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteStopGps(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteScreenState /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteScreenState(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteScreenBrightness /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteScreenBrightness(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteUserActivity /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteUserActivity(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWakeUp /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteWakeUp(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteInteractive /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteInteractive(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteConnectivityChanged /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteConnectivityChanged(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteMobileRadioPowerState /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteMobileRadioPowerState(data.readInt(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notePhoneOn /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    notePhoneOn();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notePhoneOff /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    notePhoneOff();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notePhoneSignalStrength /*43*/:
                    SignalStrength signalStrength;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        signalStrength = (SignalStrength) SignalStrength.CREATOR.createFromParcel(data);
                    } else {
                        signalStrength = null;
                    }
                    notePhoneSignalStrength(signalStrength);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notePhoneDataConnectionState /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    notePhoneDataConnectionState(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notePhoneState /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    notePhoneState(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiOn /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteWifiOn();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiOff /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteWifiOff();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiRunning /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteWifiRunning(workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiRunningChanged /*49*/:
                    WorkSource workSource3;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    if (data.readInt() != 0) {
                        workSource3 = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource3 = null;
                    }
                    noteWifiRunningChanged(workSource, workSource3);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiStopped /*50*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteWifiStopped(workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiState /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteWifiState(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiSupplicantStateChanged /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteWifiSupplicantStateChanged(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiRssiChanged /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteWifiRssiChanged(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteFullWifiLockAcquired /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteFullWifiLockAcquired(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteFullWifiLockReleased /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteFullWifiLockReleased(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiScanStarted /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteWifiScanStarted(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiScanStopped /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteWifiScanStopped(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiMulticastEnabled /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteWifiMulticastEnabled(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiMulticastDisabled /*59*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteWifiMulticastDisabled(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteFullWifiLockAcquiredFromSource /*60*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteFullWifiLockAcquiredFromSource(workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteFullWifiLockReleasedFromSource /*61*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteFullWifiLockReleasedFromSource(workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiScanStartedFromSource /*62*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteWifiScanStartedFromSource(workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiScanStoppedFromSource /*63*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteWifiScanStoppedFromSource(workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiBatchedScanStartedFromSource /*64*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteWifiBatchedScanStartedFromSource(workSource, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiBatchedScanStoppedFromSource /*65*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteWifiBatchedScanStoppedFromSource(workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiMulticastEnabledFromSource /*66*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteWifiMulticastEnabledFromSource(workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiMulticastDisabledFromSource /*67*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteWifiMulticastDisabledFromSource(workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteWifiRadioPowerState /*68*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteWifiRadioPowerState(data.readInt(), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteNetworkInterfaceType /*69*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteNetworkInterfaceType(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteNetworkStatsEnabled /*70*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteNetworkStatsEnabled();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteDeviceIdleMode /*71*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteDeviceIdleMode(data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setBatteryState /*72*/:
                    data.enforceInterface(DESCRIPTOR);
                    setBatteryState(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAwakeTimeBattery /*73*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAwakeTimeBattery();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_getAwakeTimePlugged /*74*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAwakeTimePlugged();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_noteBleScanStarted /*75*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteBleScanStarted(workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteBleScanStopped /*76*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    noteBleScanStopped(workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_noteResetBleScan /*77*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteResetBleScan();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_takeUidSnapshot /*78*/:
                    data.enforceInterface(DESCRIPTOR);
                    HealthStatsParceler _result5 = takeUidSnapshot(data.readInt());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_noteStartSensor);
                        _result5.writeToParcel(reply, TRANSACTION_noteStartSensor);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_takeUidSnapshots /*79*/:
                    data.enforceInterface(DESCRIPTOR);
                    HealthStatsParceler[] _result6 = takeUidSnapshots(data.createIntArray());
                    reply.writeNoException();
                    reply.writeTypedArray(_result6, TRANSACTION_noteStartSensor);
                    return true;
                case TRANSACTION_noteBluetoothControllerActivity /*80*/:
                    BluetoothActivityEnergyInfo bluetoothActivityEnergyInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothActivityEnergyInfo = (BluetoothActivityEnergyInfo) BluetoothActivityEnergyInfo.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothActivityEnergyInfo = null;
                    }
                    noteBluetoothControllerActivity(bluetoothActivityEnergyInfo);
                    return true;
                case TRANSACTION_noteModemControllerActivity /*81*/:
                    ModemActivityInfo modemActivityInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        modemActivityInfo = (ModemActivityInfo) ModemActivityInfo.CREATOR.createFromParcel(data);
                    } else {
                        modemActivityInfo = null;
                    }
                    noteModemControllerActivity(modemActivityInfo);
                    return true;
                case TRANSACTION_noteWifiControllerActivity /*82*/:
                    WifiActivityEnergyInfo wifiActivityEnergyInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        wifiActivityEnergyInfo = (WifiActivityEnergyInfo) WifiActivityEnergyInfo.CREATOR.createFromParcel(data);
                    } else {
                        wifiActivityEnergyInfo = null;
                    }
                    noteWifiControllerActivity(wifiActivityEnergyInfo);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    long computeBatteryTimeRemaining() throws RemoteException;

    long computeChargeTimeRemaining() throws RemoteException;

    long getAwakeTimeBattery() throws RemoteException;

    long getAwakeTimePlugged() throws RemoteException;

    byte[] getStatistics() throws RemoteException;

    ParcelFileDescriptor getStatisticsStream() throws RemoteException;

    boolean isCharging() throws RemoteException;

    void noteBleScanStarted(WorkSource workSource) throws RemoteException;

    void noteBleScanStopped(WorkSource workSource) throws RemoteException;

    void noteBluetoothControllerActivity(BluetoothActivityEnergyInfo bluetoothActivityEnergyInfo) throws RemoteException;

    void noteChangeWakelockFromSource(WorkSource workSource, int i, String str, String str2, int i2, WorkSource workSource2, int i3, String str3, String str4, int i4, boolean z) throws RemoteException;

    void noteConnectivityChanged(int i, String str) throws RemoteException;

    void noteDeviceIdleMode(int i, String str, int i2) throws RemoteException;

    void noteEvent(int i, String str, int i2) throws RemoteException;

    void noteFlashlightOff(int i) throws RemoteException;

    void noteFlashlightOn(int i) throws RemoteException;

    void noteFullWifiLockAcquired(int i) throws RemoteException;

    void noteFullWifiLockAcquiredFromSource(WorkSource workSource) throws RemoteException;

    void noteFullWifiLockReleased(int i) throws RemoteException;

    void noteFullWifiLockReleasedFromSource(WorkSource workSource) throws RemoteException;

    void noteInteractive(boolean z) throws RemoteException;

    void noteJobFinish(String str, int i) throws RemoteException;

    void noteJobStart(String str, int i) throws RemoteException;

    void noteMobileRadioPowerState(int i, long j, int i2) throws RemoteException;

    void noteModemControllerActivity(ModemActivityInfo modemActivityInfo) throws RemoteException;

    void noteNetworkInterfaceType(String str, int i) throws RemoteException;

    void noteNetworkStatsEnabled() throws RemoteException;

    void notePhoneDataConnectionState(int i, boolean z) throws RemoteException;

    void notePhoneOff() throws RemoteException;

    void notePhoneOn() throws RemoteException;

    void notePhoneSignalStrength(SignalStrength signalStrength) throws RemoteException;

    void notePhoneState(int i) throws RemoteException;

    void noteResetAudio() throws RemoteException;

    void noteResetBleScan() throws RemoteException;

    void noteResetCamera() throws RemoteException;

    void noteResetFlashlight() throws RemoteException;

    void noteResetVideo() throws RemoteException;

    void noteScreenBrightness(int i) throws RemoteException;

    void noteScreenState(int i) throws RemoteException;

    void noteStartAudio(int i) throws RemoteException;

    void noteStartCamera(int i) throws RemoteException;

    void noteStartGps(int i) throws RemoteException;

    void noteStartSensor(int i, int i2) throws RemoteException;

    void noteStartVideo(int i) throws RemoteException;

    void noteStartWakelock(int i, int i2, String str, String str2, int i3, boolean z) throws RemoteException;

    void noteStartWakelockFromSource(WorkSource workSource, int i, String str, String str2, int i2, boolean z) throws RemoteException;

    void noteStopAudio(int i) throws RemoteException;

    void noteStopCamera(int i) throws RemoteException;

    void noteStopGps(int i) throws RemoteException;

    void noteStopSensor(int i, int i2) throws RemoteException;

    void noteStopVideo(int i) throws RemoteException;

    void noteStopWakelock(int i, int i2, String str, String str2, int i3) throws RemoteException;

    void noteStopWakelockFromSource(WorkSource workSource, int i, String str, String str2, int i2) throws RemoteException;

    void noteSyncFinish(String str, int i) throws RemoteException;

    void noteSyncStart(String str, int i) throws RemoteException;

    void noteUserActivity(int i, int i2) throws RemoteException;

    void noteVibratorOff(int i) throws RemoteException;

    void noteVibratorOn(int i, long j) throws RemoteException;

    void noteWakeUp(String str, int i) throws RemoteException;

    void noteWifiBatchedScanStartedFromSource(WorkSource workSource, int i) throws RemoteException;

    void noteWifiBatchedScanStoppedFromSource(WorkSource workSource) throws RemoteException;

    void noteWifiControllerActivity(WifiActivityEnergyInfo wifiActivityEnergyInfo) throws RemoteException;

    void noteWifiMulticastDisabled(int i) throws RemoteException;

    void noteWifiMulticastDisabledFromSource(WorkSource workSource) throws RemoteException;

    void noteWifiMulticastEnabled(int i) throws RemoteException;

    void noteWifiMulticastEnabledFromSource(WorkSource workSource) throws RemoteException;

    void noteWifiOff() throws RemoteException;

    void noteWifiOn() throws RemoteException;

    void noteWifiRadioPowerState(int i, long j) throws RemoteException;

    void noteWifiRssiChanged(int i) throws RemoteException;

    void noteWifiRunning(WorkSource workSource) throws RemoteException;

    void noteWifiRunningChanged(WorkSource workSource, WorkSource workSource2) throws RemoteException;

    void noteWifiScanStarted(int i) throws RemoteException;

    void noteWifiScanStartedFromSource(WorkSource workSource) throws RemoteException;

    void noteWifiScanStopped(int i) throws RemoteException;

    void noteWifiScanStoppedFromSource(WorkSource workSource) throws RemoteException;

    void noteWifiState(int i, String str) throws RemoteException;

    void noteWifiStopped(WorkSource workSource) throws RemoteException;

    void noteWifiSupplicantStateChanged(int i, boolean z) throws RemoteException;

    void setBatteryState(int i, int i2, int i3, int i4, int i5, int i6, int i7) throws RemoteException;

    HealthStatsParceler takeUidSnapshot(int i) throws RemoteException;

    HealthStatsParceler[] takeUidSnapshots(int[] iArr) throws RemoteException;
}
