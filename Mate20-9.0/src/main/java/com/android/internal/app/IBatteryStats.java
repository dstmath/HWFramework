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
import android.os.connectivity.CellularBatteryStats;
import android.os.connectivity.GpsBatteryStats;
import android.os.connectivity.WifiBatteryStats;
import android.os.health.HealthStatsParceler;
import android.telephony.ModemActivityInfo;
import android.telephony.SignalStrength;

public interface IBatteryStats extends IInterface {

    public static abstract class Stub extends Binder implements IBatteryStats {
        private static final String DESCRIPTOR = "com.android.internal.app.IBatteryStats";
        static final int TRANSACTION_computeBatteryTimeRemaining = 18;
        static final int TRANSACTION_computeChargeTimeRemaining = 19;
        static final int TRANSACTION_getAwakeTimeBattery = 75;
        static final int TRANSACTION_getAwakeTimePlugged = 76;
        static final int TRANSACTION_getCellularBatteryStats = 81;
        static final int TRANSACTION_getGpsBatteryStats = 83;
        static final int TRANSACTION_getStatistics = 15;
        static final int TRANSACTION_getStatisticsStream = 16;
        static final int TRANSACTION_getWifiBatteryStats = 82;
        static final int TRANSACTION_isCharging = 17;
        static final int TRANSACTION_noteBleScanResults = 80;
        static final int TRANSACTION_noteBleScanStarted = 77;
        static final int TRANSACTION_noteBleScanStopped = 78;
        static final int TRANSACTION_noteBluetoothControllerActivity = 86;
        static final int TRANSACTION_noteChangeWakelockFromSource = 28;
        static final int TRANSACTION_noteConnectivityChanged = 43;
        static final int TRANSACTION_noteDeviceIdleMode = 73;
        static final int TRANSACTION_noteEvent = 20;
        static final int TRANSACTION_noteFlashlightOff = 10;
        static final int TRANSACTION_noteFlashlightOn = 9;
        static final int TRANSACTION_noteFullWifiLockAcquired = 58;
        static final int TRANSACTION_noteFullWifiLockAcquiredFromSource = 64;
        static final int TRANSACTION_noteFullWifiLockReleased = 59;
        static final int TRANSACTION_noteFullWifiLockReleasedFromSource = 65;
        static final int TRANSACTION_noteGpsChanged = 36;
        static final int TRANSACTION_noteGpsSignalQuality = 37;
        static final int TRANSACTION_noteInteractive = 42;
        static final int TRANSACTION_noteJobFinish = 24;
        static final int TRANSACTION_noteJobStart = 23;
        static final int TRANSACTION_noteLongPartialWakelockFinish = 32;
        static final int TRANSACTION_noteLongPartialWakelockFinishFromSource = 33;
        static final int TRANSACTION_noteLongPartialWakelockStart = 30;
        static final int TRANSACTION_noteLongPartialWakelockStartFromSource = 31;
        static final int TRANSACTION_noteMobileRadioPowerState = 44;
        static final int TRANSACTION_noteModemControllerActivity = 87;
        static final int TRANSACTION_noteNetworkInterfaceType = 71;
        static final int TRANSACTION_noteNetworkStatsEnabled = 72;
        static final int TRANSACTION_notePhoneDataConnectionState = 48;
        static final int TRANSACTION_notePhoneOff = 46;
        static final int TRANSACTION_notePhoneOn = 45;
        static final int TRANSACTION_notePhoneSignalStrength = 47;
        static final int TRANSACTION_notePhoneState = 49;
        static final int TRANSACTION_noteResetAudio = 8;
        static final int TRANSACTION_noteResetBleScan = 79;
        static final int TRANSACTION_noteResetCamera = 13;
        static final int TRANSACTION_noteResetFlashlight = 14;
        static final int TRANSACTION_noteResetVideo = 7;
        static final int TRANSACTION_noteScreenBrightness = 39;
        static final int TRANSACTION_noteScreenState = 38;
        static final int TRANSACTION_noteStartAudio = 5;
        static final int TRANSACTION_noteStartCamera = 11;
        static final int TRANSACTION_noteStartSensor = 1;
        static final int TRANSACTION_noteStartVideo = 3;
        static final int TRANSACTION_noteStartWakelock = 25;
        static final int TRANSACTION_noteStartWakelockFromSource = 27;
        static final int TRANSACTION_noteStopAudio = 6;
        static final int TRANSACTION_noteStopCamera = 12;
        static final int TRANSACTION_noteStopSensor = 2;
        static final int TRANSACTION_noteStopVideo = 4;
        static final int TRANSACTION_noteStopWakelock = 26;
        static final int TRANSACTION_noteStopWakelockFromSource = 29;
        static final int TRANSACTION_noteSyncFinish = 22;
        static final int TRANSACTION_noteSyncStart = 21;
        static final int TRANSACTION_noteUserActivity = 40;
        static final int TRANSACTION_noteVibratorOff = 35;
        static final int TRANSACTION_noteVibratorOn = 34;
        static final int TRANSACTION_noteWakeUp = 41;
        static final int TRANSACTION_noteWifiBatchedScanStartedFromSource = 68;
        static final int TRANSACTION_noteWifiBatchedScanStoppedFromSource = 69;
        static final int TRANSACTION_noteWifiControllerActivity = 88;
        static final int TRANSACTION_noteWifiMulticastDisabled = 63;
        static final int TRANSACTION_noteWifiMulticastEnabled = 62;
        static final int TRANSACTION_noteWifiOff = 51;
        static final int TRANSACTION_noteWifiOn = 50;
        static final int TRANSACTION_noteWifiRadioPowerState = 70;
        static final int TRANSACTION_noteWifiRssiChanged = 57;
        static final int TRANSACTION_noteWifiRunning = 52;
        static final int TRANSACTION_noteWifiRunningChanged = 53;
        static final int TRANSACTION_noteWifiScanStarted = 60;
        static final int TRANSACTION_noteWifiScanStartedFromSource = 66;
        static final int TRANSACTION_noteWifiScanStopped = 61;
        static final int TRANSACTION_noteWifiScanStoppedFromSource = 67;
        static final int TRANSACTION_noteWifiState = 55;
        static final int TRANSACTION_noteWifiStopped = 54;
        static final int TRANSACTION_noteWifiSupplicantStateChanged = 56;
        static final int TRANSACTION_setBatteryState = 74;
        static final int TRANSACTION_takeUidSnapshot = 84;
        static final int TRANSACTION_takeUidSnapshots = 85;

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
                    this.mRemote.transact(1, _data, _reply, 0);
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
                    this.mRemote.transact(2, _data, _reply, 0);
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
                    this.mRemote.transact(3, _data, _reply, 0);
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
                    this.mRemote.transact(4, _data, _reply, 0);
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
                    this.mRemote.transact(5, _data, _reply, 0);
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
                    this.mRemote.transact(6, _data, _reply, 0);
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
                    this.mRemote.transact(7, _data, _reply, 0);
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
                    this.mRemote.transact(8, _data, _reply, 0);
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
                    this.mRemote.transact(9, _data, _reply, 0);
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
                    this.mRemote.transact(10, _data, _reply, 0);
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
                    this.mRemote.transact(11, _data, _reply, 0);
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
                    this.mRemote.transact(12, _data, _reply, 0);
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
                    this.mRemote.transact(13, _data, _reply, 0);
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
                    this.mRemote.transact(14, _data, _reply, 0);
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
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor getStatisticsStream() throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCharging() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long computeBatteryTimeRemaining() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
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
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
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
                    this.mRemote.transact(20, _data, _reply, 0);
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
                    this.mRemote.transact(21, _data, _reply, 0);
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
                    this.mRemote.transact(22, _data, _reply, 0);
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
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteJobFinish(String name, int uid, int stopReason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    _data.writeInt(stopReason);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStartWakelock(int uid, int pid, String name, String historyName, int type, boolean unimportantForLogging) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeString(name);
                    _data.writeString(historyName);
                    _data.writeInt(type);
                    _data.writeInt(unimportantForLogging);
                    this.mRemote.transact(25, _data, _reply, 0);
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
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteStartWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type, boolean unimportantForLogging) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(pid);
                    _data.writeString(name);
                    _data.writeString(historyName);
                    _data.writeInt(type);
                    _data.writeInt(unimportantForLogging);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteChangeWakelockFromSource(WorkSource ws, int pid, String name, String histyoryName, int type, WorkSource newWs, int newPid, String newName, String newHistoryName, int newType, boolean newUnimportantForLogging) throws RemoteException {
                WorkSource workSource = ws;
                WorkSource workSource2 = newWs;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (workSource != null) {
                        _data.writeInt(1);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeInt(pid);
                        try {
                            _data.writeString(name);
                            try {
                                _data.writeString(histyoryName);
                            } catch (Throwable th) {
                                th = th;
                                int i = type;
                                int i2 = newPid;
                                String str = newName;
                                String str2 = newHistoryName;
                                int i3 = newType;
                                boolean z = newUnimportantForLogging;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            String str3 = histyoryName;
                            int i4 = type;
                            int i22 = newPid;
                            String str4 = newName;
                            String str22 = newHistoryName;
                            int i32 = newType;
                            boolean z2 = newUnimportantForLogging;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        String str5 = name;
                        String str32 = histyoryName;
                        int i42 = type;
                        int i222 = newPid;
                        String str42 = newName;
                        String str222 = newHistoryName;
                        int i322 = newType;
                        boolean z22 = newUnimportantForLogging;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(type);
                        if (workSource2 != null) {
                            _data.writeInt(1);
                            workSource2.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(newPid);
                            try {
                                _data.writeString(newName);
                                try {
                                    _data.writeString(newHistoryName);
                                } catch (Throwable th4) {
                                    th = th4;
                                    int i3222 = newType;
                                    boolean z222 = newUnimportantForLogging;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                String str2222 = newHistoryName;
                                int i32222 = newType;
                                boolean z2222 = newUnimportantForLogging;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            String str422 = newName;
                            String str22222 = newHistoryName;
                            int i322222 = newType;
                            boolean z22222 = newUnimportantForLogging;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(newType);
                            try {
                                _data.writeInt(newUnimportantForLogging ? 1 : 0);
                                try {
                                    this.mRemote.transact(28, _data, _reply, 0);
                                    _reply.readException();
                                    _reply.recycle();
                                    _data.recycle();
                                } catch (Throwable th7) {
                                    th = th7;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th8) {
                                th = th8;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th9) {
                            th = th9;
                            boolean z222222 = newUnimportantForLogging;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th10) {
                        th = th10;
                        int i2222 = newPid;
                        String str4222 = newName;
                        String str222222 = newHistoryName;
                        int i3222222 = newType;
                        boolean z2222222 = newUnimportantForLogging;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th11) {
                    th = th11;
                    int i5 = pid;
                    String str52 = name;
                    String str322 = histyoryName;
                    int i422 = type;
                    int i22222 = newPid;
                    String str42222 = newName;
                    String str2222222 = newHistoryName;
                    int i32222222 = newType;
                    boolean z22222222 = newUnimportantForLogging;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public void noteStopWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(pid);
                    _data.writeString(name);
                    _data.writeString(historyName);
                    _data.writeInt(type);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteLongPartialWakelockStart(String name, String historyName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(historyName);
                    _data.writeInt(uid);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteLongPartialWakelockStartFromSource(String name, String historyName, WorkSource workSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(historyName);
                    if (workSource != null) {
                        _data.writeInt(1);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteLongPartialWakelockFinish(String name, String historyName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(historyName);
                    _data.writeInt(uid);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteLongPartialWakelockFinishFromSource(String name, String historyName, WorkSource workSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(historyName);
                    if (workSource != null) {
                        _data.writeInt(1);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(33, _data, _reply, 0);
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
                    this.mRemote.transact(34, _data, _reply, 0);
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
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteGpsChanged(WorkSource oldSource, WorkSource newSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oldSource != null) {
                        _data.writeInt(1);
                        oldSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (newSource != null) {
                        _data.writeInt(1);
                        newSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteGpsSignalQuality(int signalLevel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(signalLevel);
                    this.mRemote.transact(37, _data, _reply, 0);
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
                    this.mRemote.transact(38, _data, _reply, 0);
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
                    this.mRemote.transact(39, _data, _reply, 0);
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
                    this.mRemote.transact(40, _data, _reply, 0);
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
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteInteractive(boolean interactive) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(interactive);
                    this.mRemote.transact(42, _data, _reply, 0);
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
                    this.mRemote.transact(43, _data, _reply, 0);
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
                    this.mRemote.transact(44, _data, _reply, 0);
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
                    this.mRemote.transact(45, _data, _reply, 0);
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
                    this.mRemote.transact(46, _data, _reply, 0);
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
                        _data.writeInt(1);
                        signalStrength.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notePhoneDataConnectionState(int dataType, boolean hasData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dataType);
                    _data.writeInt(hasData);
                    this.mRemote.transact(48, _data, _reply, 0);
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
                    this.mRemote.transact(49, _data, _reply, 0);
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
                    this.mRemote.transact(50, _data, _reply, 0);
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
                    this.mRemote.transact(51, _data, _reply, 0);
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
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(52, _data, _reply, 0);
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
                        _data.writeInt(1);
                        oldWs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (newWs != null) {
                        _data.writeInt(1);
                        newWs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(53, _data, _reply, 0);
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
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(54, _data, _reply, 0);
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
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiSupplicantStateChanged(int supplState, boolean failedAuth) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(supplState);
                    _data.writeInt(failedAuth);
                    this.mRemote.transact(56, _data, _reply, 0);
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
                    this.mRemote.transact(57, _data, _reply, 0);
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
                    this.mRemote.transact(58, _data, _reply, 0);
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
                    this.mRemote.transact(59, _data, _reply, 0);
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
                    this.mRemote.transact(60, _data, _reply, 0);
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
                    this.mRemote.transact(61, _data, _reply, 0);
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
                    this.mRemote.transact(62, _data, _reply, 0);
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
                    this.mRemote.transact(63, _data, _reply, 0);
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
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(64, _data, _reply, 0);
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
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(65, _data, _reply, 0);
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
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(66, _data, _reply, 0);
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
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(67, _data, _reply, 0);
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
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(csph);
                    this.mRemote.transact(68, _data, _reply, 0);
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
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(69, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteWifiRadioPowerState(int powerState, long timestampNs, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(powerState);
                    _data.writeLong(timestampNs);
                    _data.writeInt(uid);
                    this.mRemote.transact(70, _data, _reply, 0);
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
                    this.mRemote.transact(71, _data, _reply, 0);
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
                    this.mRemote.transact(72, _data, _reply, 0);
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
                    this.mRemote.transact(73, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBatteryState(int status, int health, int plugType, int level, int temp, int volt, int chargeUAh, int chargeFullUAh) throws RemoteException {
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
                    _data.writeInt(chargeFullUAh);
                    this.mRemote.transact(74, _data, _reply, 0);
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
                    this.mRemote.transact(75, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
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
                    this.mRemote.transact(76, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteBleScanStarted(WorkSource ws, boolean isUnoptimized) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(isUnoptimized);
                    this.mRemote.transact(77, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteBleScanStopped(WorkSource ws, boolean isUnoptimized) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(isUnoptimized);
                    this.mRemote.transact(78, _data, _reply, 0);
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
                    this.mRemote.transact(79, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteBleScanResults(WorkSource ws, int numNewResults) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(numNewResults);
                    this.mRemote.transact(80, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CellularBatteryStats getCellularBatteryStats() throws RemoteException {
                CellularBatteryStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(81, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (CellularBatteryStats) CellularBatteryStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WifiBatteryStats getWifiBatteryStats() throws RemoteException {
                WifiBatteryStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(82, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (WifiBatteryStats) WifiBatteryStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public GpsBatteryStats getGpsBatteryStats() throws RemoteException {
                GpsBatteryStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(83, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (GpsBatteryStats) GpsBatteryStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HealthStatsParceler takeUidSnapshot(int uid) throws RemoteException {
                HealthStatsParceler _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(84, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (HealthStatsParceler) HealthStatsParceler.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(85, _data, _reply, 0);
                    _reply.readException();
                    return (HealthStatsParceler[]) _reply.createTypedArray(HealthStatsParceler.CREATOR);
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
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(86, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void noteModemControllerActivity(ModemActivityInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(87, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void noteWifiControllerActivity(WifiActivityEnergyInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(88, _data, null, 1);
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v7, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v15, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v20, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v24, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v34, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v25, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v27, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v52, resolved type: android.telephony.SignalStrength} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v31, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v61, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v32, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v34, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v44, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v35, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v37, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v71, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v38, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v41, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v85, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v42, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v44, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v89, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v45, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v47, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v93, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v48, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v50, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v97, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v51, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v53, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v101, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v54, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v56, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v105, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v57, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v61, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v55, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v63, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v65, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v59, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v67, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v69, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v119, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v70, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v71, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v73, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v124, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v74, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v75, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v76, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v128, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v77, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v78, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v79, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v132, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v80, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v81, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v82, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v83, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v84, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v85, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v86, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v87, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v88, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v89, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v90, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v91, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v92, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v93, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v94, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v95, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v96, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v97, resolved type: android.os.WorkSource} */
        /* JADX WARNING: type inference failed for: r2v29, types: [android.telephony.SignalStrength] */
        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
            */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int r26, android.os.Parcel r27, android.os.Parcel r28, int r29) throws android.os.RemoteException {
            /*
                r25 = this;
                r12 = r25
                r13 = r26
                r14 = r27
                r15 = r28
                java.lang.String r10 = "com.android.internal.app.IBatteryStats"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r9 = 1
                if (r13 == r0) goto L_0x07f5
                r0 = 0
                r2 = 0
                switch(r13) {
                    case 1: goto L_0x07e1;
                    case 2: goto L_0x07cd;
                    case 3: goto L_0x07bd;
                    case 4: goto L_0x07ad;
                    case 5: goto L_0x079d;
                    case 6: goto L_0x078d;
                    case 7: goto L_0x0781;
                    case 8: goto L_0x0775;
                    case 9: goto L_0x0765;
                    case 10: goto L_0x0755;
                    case 11: goto L_0x0745;
                    case 12: goto L_0x0735;
                    case 13: goto L_0x0729;
                    case 14: goto L_0x071d;
                    case 15: goto L_0x070d;
                    case 16: goto L_0x06f4;
                    case 17: goto L_0x06e4;
                    case 18: goto L_0x06d4;
                    case 19: goto L_0x06c4;
                    case 20: goto L_0x06ac;
                    case 21: goto L_0x0698;
                    case 22: goto L_0x0684;
                    case 23: goto L_0x0670;
                    case 24: goto L_0x0658;
                    case 25: goto L_0x0628;
                    case 26: goto L_0x0602;
                    case 27: goto L_0x05c5;
                    case 28: goto L_0x055d;
                    case 29: goto L_0x052d;
                    case 30: goto L_0x0517;
                    case 31: goto L_0x04f5;
                    case 32: goto L_0x04df;
                    case 33: goto L_0x04bd;
                    case 34: goto L_0x04ab;
                    case 35: goto L_0x049d;
                    case 36: goto L_0x0471;
                    case 37: goto L_0x0463;
                    case 38: goto L_0x0455;
                    case 39: goto L_0x0447;
                    case 40: goto L_0x0435;
                    case 41: goto L_0x0423;
                    case 42: goto L_0x0411;
                    case 43: goto L_0x03ff;
                    case 44: goto L_0x03e9;
                    case 45: goto L_0x03df;
                    case 46: goto L_0x03d5;
                    case 47: goto L_0x03b9;
                    case 48: goto L_0x03a3;
                    case 49: goto L_0x0395;
                    case 50: goto L_0x038b;
                    case 51: goto L_0x0381;
                    case 52: goto L_0x0365;
                    case 53: goto L_0x0339;
                    case 54: goto L_0x031d;
                    case 55: goto L_0x030b;
                    case 56: goto L_0x02f5;
                    case 57: goto L_0x02e7;
                    case 58: goto L_0x02d9;
                    case 59: goto L_0x02cb;
                    case 60: goto L_0x02bd;
                    case 61: goto L_0x02af;
                    case 62: goto L_0x02a1;
                    case 63: goto L_0x0293;
                    case 64: goto L_0x0277;
                    case 65: goto L_0x025b;
                    case 66: goto L_0x023f;
                    case 67: goto L_0x0223;
                    case 68: goto L_0x0203;
                    case 69: goto L_0x01e7;
                    case 70: goto L_0x01d1;
                    case 71: goto L_0x01bf;
                    case 72: goto L_0x01b5;
                    case 73: goto L_0x019f;
                    case 74: goto L_0x0165;
                    case 75: goto L_0x0157;
                    case 76: goto L_0x0149;
                    case 77: goto L_0x0125;
                    case 78: goto L_0x0101;
                    case 79: goto L_0x00f7;
                    case 80: goto L_0x00d7;
                    case 81: goto L_0x00c0;
                    case 82: goto L_0x00a9;
                    case 83: goto L_0x0092;
                    case 84: goto L_0x0077;
                    case 85: goto L_0x0065;
                    case 86: goto L_0x004c;
                    case 87: goto L_0x0033;
                    case 88: goto L_0x001a;
                    default: goto L_0x0015;
                }
            L_0x0015:
                boolean r0 = super.onTransact(r26, r27, r28, r29)
                return r0
            L_0x001a:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x002d
                android.os.Parcelable$Creator r0 = android.net.wifi.WifiActivityEnergyInfo.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.net.wifi.WifiActivityEnergyInfo r2 = (android.net.wifi.WifiActivityEnergyInfo) r2
                goto L_0x002e
            L_0x002d:
            L_0x002e:
                r0 = r2
                r12.noteWifiControllerActivity(r0)
                return r9
            L_0x0033:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x0046
                android.os.Parcelable$Creator r0 = android.telephony.ModemActivityInfo.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.telephony.ModemActivityInfo r2 = (android.telephony.ModemActivityInfo) r2
                goto L_0x0047
            L_0x0046:
            L_0x0047:
                r0 = r2
                r12.noteModemControllerActivity(r0)
                return r9
            L_0x004c:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x005f
                android.os.Parcelable$Creator r0 = android.bluetooth.BluetoothActivityEnergyInfo.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.bluetooth.BluetoothActivityEnergyInfo r2 = (android.bluetooth.BluetoothActivityEnergyInfo) r2
                goto L_0x0060
            L_0x005f:
            L_0x0060:
                r0 = r2
                r12.noteBluetoothControllerActivity(r0)
                return r9
            L_0x0065:
                r14.enforceInterface(r10)
                int[] r0 = r27.createIntArray()
                android.os.health.HealthStatsParceler[] r1 = r12.takeUidSnapshots(r0)
                r28.writeNoException()
                r15.writeTypedArray(r1, r9)
                return r9
            L_0x0077:
                r14.enforceInterface(r10)
                int r1 = r27.readInt()
                android.os.health.HealthStatsParceler r2 = r12.takeUidSnapshot(r1)
                r28.writeNoException()
                if (r2 == 0) goto L_0x008e
                r15.writeInt(r9)
                r2.writeToParcel(r15, r9)
                goto L_0x0091
            L_0x008e:
                r15.writeInt(r0)
            L_0x0091:
                return r9
            L_0x0092:
                r14.enforceInterface(r10)
                android.os.connectivity.GpsBatteryStats r1 = r25.getGpsBatteryStats()
                r28.writeNoException()
                if (r1 == 0) goto L_0x00a5
                r15.writeInt(r9)
                r1.writeToParcel(r15, r9)
                goto L_0x00a8
            L_0x00a5:
                r15.writeInt(r0)
            L_0x00a8:
                return r9
            L_0x00a9:
                r14.enforceInterface(r10)
                android.os.connectivity.WifiBatteryStats r1 = r25.getWifiBatteryStats()
                r28.writeNoException()
                if (r1 == 0) goto L_0x00bc
                r15.writeInt(r9)
                r1.writeToParcel(r15, r9)
                goto L_0x00bf
            L_0x00bc:
                r15.writeInt(r0)
            L_0x00bf:
                return r9
            L_0x00c0:
                r14.enforceInterface(r10)
                android.os.connectivity.CellularBatteryStats r1 = r25.getCellularBatteryStats()
                r28.writeNoException()
                if (r1 == 0) goto L_0x00d3
                r15.writeInt(r9)
                r1.writeToParcel(r15, r9)
                goto L_0x00d6
            L_0x00d3:
                r15.writeInt(r0)
            L_0x00d6:
                return r9
            L_0x00d7:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x00ea
                android.os.Parcelable$Creator r0 = android.os.WorkSource.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x00eb
            L_0x00ea:
            L_0x00eb:
                r0 = r2
                int r1 = r27.readInt()
                r12.noteBleScanResults(r0, r1)
                r28.writeNoException()
                return r9
            L_0x00f7:
                r14.enforceInterface(r10)
                r25.noteResetBleScan()
                r28.writeNoException()
                return r9
            L_0x0101:
                r14.enforceInterface(r10)
                int r1 = r27.readInt()
                if (r1 == 0) goto L_0x0114
                android.os.Parcelable$Creator r1 = android.os.WorkSource.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r14)
                r2 = r1
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x0115
            L_0x0114:
            L_0x0115:
                r1 = r2
                int r2 = r27.readInt()
                if (r2 == 0) goto L_0x011e
                r0 = r9
            L_0x011e:
                r12.noteBleScanStopped(r1, r0)
                r28.writeNoException()
                return r9
            L_0x0125:
                r14.enforceInterface(r10)
                int r1 = r27.readInt()
                if (r1 == 0) goto L_0x0138
                android.os.Parcelable$Creator r1 = android.os.WorkSource.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r14)
                r2 = r1
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x0139
            L_0x0138:
            L_0x0139:
                r1 = r2
                int r2 = r27.readInt()
                if (r2 == 0) goto L_0x0142
                r0 = r9
            L_0x0142:
                r12.noteBleScanStarted(r1, r0)
                r28.writeNoException()
                return r9
            L_0x0149:
                r14.enforceInterface(r10)
                long r0 = r25.getAwakeTimePlugged()
                r28.writeNoException()
                r15.writeLong(r0)
                return r9
            L_0x0157:
                r14.enforceInterface(r10)
                long r0 = r25.getAwakeTimeBattery()
                r28.writeNoException()
                r15.writeLong(r0)
                return r9
            L_0x0165:
                r14.enforceInterface(r10)
                int r11 = r27.readInt()
                int r16 = r27.readInt()
                int r17 = r27.readInt()
                int r18 = r27.readInt()
                int r19 = r27.readInt()
                int r20 = r27.readInt()
                int r21 = r27.readInt()
                int r22 = r27.readInt()
                r0 = r12
                r1 = r11
                r2 = r16
                r3 = r17
                r4 = r18
                r5 = r19
                r6 = r20
                r7 = r21
                r8 = r22
                r0.setBatteryState(r1, r2, r3, r4, r5, r6, r7, r8)
                r28.writeNoException()
                return r9
            L_0x019f:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                java.lang.String r1 = r27.readString()
                int r2 = r27.readInt()
                r12.noteDeviceIdleMode(r0, r1, r2)
                r28.writeNoException()
                return r9
            L_0x01b5:
                r14.enforceInterface(r10)
                r25.noteNetworkStatsEnabled()
                r28.writeNoException()
                return r9
            L_0x01bf:
                r14.enforceInterface(r10)
                java.lang.String r0 = r27.readString()
                int r1 = r27.readInt()
                r12.noteNetworkInterfaceType(r0, r1)
                r28.writeNoException()
                return r9
            L_0x01d1:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                long r1 = r27.readLong()
                int r3 = r27.readInt()
                r12.noteWifiRadioPowerState(r0, r1, r3)
                r28.writeNoException()
                return r9
            L_0x01e7:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x01fa
                android.os.Parcelable$Creator r0 = android.os.WorkSource.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x01fb
            L_0x01fa:
            L_0x01fb:
                r0 = r2
                r12.noteWifiBatchedScanStoppedFromSource(r0)
                r28.writeNoException()
                return r9
            L_0x0203:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x0216
                android.os.Parcelable$Creator r0 = android.os.WorkSource.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x0217
            L_0x0216:
            L_0x0217:
                r0 = r2
                int r1 = r27.readInt()
                r12.noteWifiBatchedScanStartedFromSource(r0, r1)
                r28.writeNoException()
                return r9
            L_0x0223:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x0236
                android.os.Parcelable$Creator r0 = android.os.WorkSource.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x0237
            L_0x0236:
            L_0x0237:
                r0 = r2
                r12.noteWifiScanStoppedFromSource(r0)
                r28.writeNoException()
                return r9
            L_0x023f:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x0252
                android.os.Parcelable$Creator r0 = android.os.WorkSource.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x0253
            L_0x0252:
            L_0x0253:
                r0 = r2
                r12.noteWifiScanStartedFromSource(r0)
                r28.writeNoException()
                return r9
            L_0x025b:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x026e
                android.os.Parcelable$Creator r0 = android.os.WorkSource.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x026f
            L_0x026e:
            L_0x026f:
                r0 = r2
                r12.noteFullWifiLockReleasedFromSource(r0)
                r28.writeNoException()
                return r9
            L_0x0277:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x028a
                android.os.Parcelable$Creator r0 = android.os.WorkSource.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x028b
            L_0x028a:
            L_0x028b:
                r0 = r2
                r12.noteFullWifiLockAcquiredFromSource(r0)
                r28.writeNoException()
                return r9
            L_0x0293:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                r12.noteWifiMulticastDisabled(r0)
                r28.writeNoException()
                return r9
            L_0x02a1:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                r12.noteWifiMulticastEnabled(r0)
                r28.writeNoException()
                return r9
            L_0x02af:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                r12.noteWifiScanStopped(r0)
                r28.writeNoException()
                return r9
            L_0x02bd:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                r12.noteWifiScanStarted(r0)
                r28.writeNoException()
                return r9
            L_0x02cb:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                r12.noteFullWifiLockReleased(r0)
                r28.writeNoException()
                return r9
            L_0x02d9:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                r12.noteFullWifiLockAcquired(r0)
                r28.writeNoException()
                return r9
            L_0x02e7:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                r12.noteWifiRssiChanged(r0)
                r28.writeNoException()
                return r9
            L_0x02f5:
                r14.enforceInterface(r10)
                int r1 = r27.readInt()
                int r2 = r27.readInt()
                if (r2 == 0) goto L_0x0304
                r0 = r9
            L_0x0304:
                r12.noteWifiSupplicantStateChanged(r1, r0)
                r28.writeNoException()
                return r9
            L_0x030b:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                java.lang.String r1 = r27.readString()
                r12.noteWifiState(r0, r1)
                r28.writeNoException()
                return r9
            L_0x031d:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x0330
                android.os.Parcelable$Creator r0 = android.os.WorkSource.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x0331
            L_0x0330:
            L_0x0331:
                r0 = r2
                r12.noteWifiStopped(r0)
                r28.writeNoException()
                return r9
            L_0x0339:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x034b
                android.os.Parcelable$Creator r0 = android.os.WorkSource.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.os.WorkSource r0 = (android.os.WorkSource) r0
                goto L_0x034c
            L_0x034b:
                r0 = r2
            L_0x034c:
                int r1 = r27.readInt()
                if (r1 == 0) goto L_0x035c
                android.os.Parcelable$Creator r1 = android.os.WorkSource.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r14)
                r2 = r1
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x035d
            L_0x035c:
            L_0x035d:
                r1 = r2
                r12.noteWifiRunningChanged(r0, r1)
                r28.writeNoException()
                return r9
            L_0x0365:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x0378
                android.os.Parcelable$Creator r0 = android.os.WorkSource.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x0379
            L_0x0378:
            L_0x0379:
                r0 = r2
                r12.noteWifiRunning(r0)
                r28.writeNoException()
                return r9
            L_0x0381:
                r14.enforceInterface(r10)
                r25.noteWifiOff()
                r28.writeNoException()
                return r9
            L_0x038b:
                r14.enforceInterface(r10)
                r25.noteWifiOn()
                r28.writeNoException()
                return r9
            L_0x0395:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                r12.notePhoneState(r0)
                r28.writeNoException()
                return r9
            L_0x03a3:
                r14.enforceInterface(r10)
                int r1 = r27.readInt()
                int r2 = r27.readInt()
                if (r2 == 0) goto L_0x03b2
                r0 = r9
            L_0x03b2:
                r12.notePhoneDataConnectionState(r1, r0)
                r28.writeNoException()
                return r9
            L_0x03b9:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x03cc
                android.os.Parcelable$Creator r0 = android.telephony.SignalStrength.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                r2 = r0
                android.telephony.SignalStrength r2 = (android.telephony.SignalStrength) r2
                goto L_0x03cd
            L_0x03cc:
            L_0x03cd:
                r0 = r2
                r12.notePhoneSignalStrength(r0)
                r28.writeNoException()
                return r9
            L_0x03d5:
                r14.enforceInterface(r10)
                r25.notePhoneOff()
                r28.writeNoException()
                return r9
            L_0x03df:
                r14.enforceInterface(r10)
                r25.notePhoneOn()
                r28.writeNoException()
                return r9
            L_0x03e9:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                long r1 = r27.readLong()
                int r3 = r27.readInt()
                r12.noteMobileRadioPowerState(r0, r1, r3)
                r28.writeNoException()
                return r9
            L_0x03ff:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                java.lang.String r1 = r27.readString()
                r12.noteConnectivityChanged(r0, r1)
                r28.writeNoException()
                return r9
            L_0x0411:
                r14.enforceInterface(r10)
                int r1 = r27.readInt()
                if (r1 == 0) goto L_0x041c
                r0 = r9
            L_0x041c:
                r12.noteInteractive(r0)
                r28.writeNoException()
                return r9
            L_0x0423:
                r14.enforceInterface(r10)
                java.lang.String r0 = r27.readString()
                int r1 = r27.readInt()
                r12.noteWakeUp(r0, r1)
                r28.writeNoException()
                return r9
            L_0x0435:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                int r1 = r27.readInt()
                r12.noteUserActivity(r0, r1)
                r28.writeNoException()
                return r9
            L_0x0447:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                r12.noteScreenBrightness(r0)
                r28.writeNoException()
                return r9
            L_0x0455:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                r12.noteScreenState(r0)
                r28.writeNoException()
                return r9
            L_0x0463:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                r12.noteGpsSignalQuality(r0)
                r28.writeNoException()
                return r9
            L_0x0471:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x0483
                android.os.Parcelable$Creator r0 = android.os.WorkSource.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.os.WorkSource r0 = (android.os.WorkSource) r0
                goto L_0x0484
            L_0x0483:
                r0 = r2
            L_0x0484:
                int r1 = r27.readInt()
                if (r1 == 0) goto L_0x0494
                android.os.Parcelable$Creator r1 = android.os.WorkSource.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r14)
                r2 = r1
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x0495
            L_0x0494:
            L_0x0495:
                r1 = r2
                r12.noteGpsChanged(r0, r1)
                r28.writeNoException()
                return r9
            L_0x049d:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                r12.noteVibratorOff(r0)
                r28.writeNoException()
                return r9
            L_0x04ab:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                long r1 = r27.readLong()
                r12.noteVibratorOn(r0, r1)
                r28.writeNoException()
                return r9
            L_0x04bd:
                r14.enforceInterface(r10)
                java.lang.String r0 = r27.readString()
                java.lang.String r1 = r27.readString()
                int r3 = r27.readInt()
                if (r3 == 0) goto L_0x04d7
                android.os.Parcelable$Creator r2 = android.os.WorkSource.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r14)
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x04d8
            L_0x04d7:
            L_0x04d8:
                r12.noteLongPartialWakelockFinishFromSource(r0, r1, r2)
                r28.writeNoException()
                return r9
            L_0x04df:
                r14.enforceInterface(r10)
                java.lang.String r0 = r27.readString()
                java.lang.String r1 = r27.readString()
                int r2 = r27.readInt()
                r12.noteLongPartialWakelockFinish(r0, r1, r2)
                r28.writeNoException()
                return r9
            L_0x04f5:
                r14.enforceInterface(r10)
                java.lang.String r0 = r27.readString()
                java.lang.String r1 = r27.readString()
                int r3 = r27.readInt()
                if (r3 == 0) goto L_0x050f
                android.os.Parcelable$Creator r2 = android.os.WorkSource.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r14)
                android.os.WorkSource r2 = (android.os.WorkSource) r2
                goto L_0x0510
            L_0x050f:
            L_0x0510:
                r12.noteLongPartialWakelockStartFromSource(r0, r1, r2)
                r28.writeNoException()
                return r9
            L_0x0517:
                r14.enforceInterface(r10)
                java.lang.String r0 = r27.readString()
                java.lang.String r1 = r27.readString()
                int r2 = r27.readInt()
                r12.noteLongPartialWakelockStart(r0, r1, r2)
                r28.writeNoException()
                return r9
            L_0x052d:
                r14.enforceInterface(r10)
                int r0 = r27.readInt()
                if (r0 == 0) goto L_0x0540
                android.os.Parcelable$Creator r0 = android.os.WorkSource.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.os.WorkSource r0 = (android.os.WorkSource) r0
                r1 = r0
                goto L_0x0541
            L_0x0540:
                r1 = r2
            L_0x0541:
                int r6 = r27.readInt()
                java.lang.String r7 = r27.readString()
                java.lang.String r8 = r27.readString()
                int r11 = r27.readInt()
                r0 = r12
                r2 = r6
                r3 = r7
                r4 = r8
                r5 = r11
                r0.noteStopWakelockFromSource(r1, r2, r3, r4, r5)
                r28.writeNoException()
                return r9
            L_0x055d:
                r14.enforceInterface(r10)
                int r1 = r27.readInt()
                if (r1 == 0) goto L_0x056f
                android.os.Parcelable$Creator r1 = android.os.WorkSource.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r14)
                android.os.WorkSource r1 = (android.os.WorkSource) r1
                goto L_0x0570
            L_0x056f:
                r1 = r2
            L_0x0570:
                int r16 = r27.readInt()
                java.lang.String r17 = r27.readString()
                java.lang.String r18 = r27.readString()
                int r19 = r27.readInt()
                int r3 = r27.readInt()
                if (r3 == 0) goto L_0x0590
                android.os.Parcelable$Creator r2 = android.os.WorkSource.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r14)
                android.os.WorkSource r2 = (android.os.WorkSource) r2
            L_0x058e:
                r6 = r2
                goto L_0x0591
            L_0x0590:
                goto L_0x058e
            L_0x0591:
                int r20 = r27.readInt()
                java.lang.String r21 = r27.readString()
                java.lang.String r22 = r27.readString()
                int r23 = r27.readInt()
                int r2 = r27.readInt()
                if (r2 == 0) goto L_0x05a9
                r11 = r9
                goto L_0x05aa
            L_0x05a9:
                r11 = r0
            L_0x05aa:
                r0 = r12
                r2 = r16
                r3 = r17
                r4 = r18
                r5 = r19
                r7 = r20
                r8 = r21
                r13 = r9
                r9 = r22
                r24 = r10
                r10 = r23
                r0.noteChangeWakelockFromSource(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
                r28.writeNoException()
                return r13
            L_0x05c5:
                r13 = r9
                r24 = r10
                r7 = r24
                r14.enforceInterface(r7)
                int r1 = r27.readInt()
                if (r1 == 0) goto L_0x05dc
                android.os.Parcelable$Creator r1 = android.os.WorkSource.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r14)
                android.os.WorkSource r1 = (android.os.WorkSource) r1
                goto L_0x05dd
            L_0x05dc:
                r1 = r2
            L_0x05dd:
                int r8 = r27.readInt()
                java.lang.String r9 = r27.readString()
                java.lang.String r10 = r27.readString()
                int r11 = r27.readInt()
                int r2 = r27.readInt()
                if (r2 == 0) goto L_0x05f5
                r6 = r13
                goto L_0x05f6
            L_0x05f5:
                r6 = r0
            L_0x05f6:
                r0 = r12
                r2 = r8
                r3 = r9
                r4 = r10
                r5 = r11
                r0.noteStartWakelockFromSource(r1, r2, r3, r4, r5, r6)
                r28.writeNoException()
                return r13
            L_0x0602:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r6 = r27.readInt()
                int r8 = r27.readInt()
                java.lang.String r9 = r27.readString()
                java.lang.String r10 = r27.readString()
                int r11 = r27.readInt()
                r0 = r12
                r1 = r6
                r2 = r8
                r3 = r9
                r4 = r10
                r5 = r11
                r0.noteStopWakelock(r1, r2, r3, r4, r5)
                r28.writeNoException()
                return r13
            L_0x0628:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r8 = r27.readInt()
                int r9 = r27.readInt()
                java.lang.String r10 = r27.readString()
                java.lang.String r11 = r27.readString()
                int r16 = r27.readInt()
                int r1 = r27.readInt()
                if (r1 == 0) goto L_0x0649
                r6 = r13
                goto L_0x064a
            L_0x0649:
                r6 = r0
            L_0x064a:
                r0 = r12
                r1 = r8
                r2 = r9
                r3 = r10
                r4 = r11
                r5 = r16
                r0.noteStartWakelock(r1, r2, r3, r4, r5, r6)
                r28.writeNoException()
                return r13
            L_0x0658:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                java.lang.String r0 = r27.readString()
                int r1 = r27.readInt()
                int r2 = r27.readInt()
                r12.noteJobFinish(r0, r1, r2)
                r28.writeNoException()
                return r13
            L_0x0670:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                java.lang.String r0 = r27.readString()
                int r1 = r27.readInt()
                r12.noteJobStart(r0, r1)
                r28.writeNoException()
                return r13
            L_0x0684:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                java.lang.String r0 = r27.readString()
                int r1 = r27.readInt()
                r12.noteSyncFinish(r0, r1)
                r28.writeNoException()
                return r13
            L_0x0698:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                java.lang.String r0 = r27.readString()
                int r1 = r27.readInt()
                r12.noteSyncStart(r0, r1)
                r28.writeNoException()
                return r13
            L_0x06ac:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r0 = r27.readInt()
                java.lang.String r1 = r27.readString()
                int r2 = r27.readInt()
                r12.noteEvent(r0, r1, r2)
                r28.writeNoException()
                return r13
            L_0x06c4:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                long r0 = r25.computeChargeTimeRemaining()
                r28.writeNoException()
                r15.writeLong(r0)
                return r13
            L_0x06d4:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                long r0 = r25.computeBatteryTimeRemaining()
                r28.writeNoException()
                r15.writeLong(r0)
                return r13
            L_0x06e4:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                boolean r0 = r25.isCharging()
                r28.writeNoException()
                r15.writeInt(r0)
                return r13
            L_0x06f4:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                android.os.ParcelFileDescriptor r1 = r25.getStatisticsStream()
                r28.writeNoException()
                if (r1 == 0) goto L_0x0709
                r15.writeInt(r13)
                r1.writeToParcel(r15, r13)
                goto L_0x070c
            L_0x0709:
                r15.writeInt(r0)
            L_0x070c:
                return r13
            L_0x070d:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                byte[] r0 = r25.getStatistics()
                r28.writeNoException()
                r15.writeByteArray(r0)
                return r13
            L_0x071d:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                r25.noteResetFlashlight()
                r28.writeNoException()
                return r13
            L_0x0729:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                r25.noteResetCamera()
                r28.writeNoException()
                return r13
            L_0x0735:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r0 = r27.readInt()
                r12.noteStopCamera(r0)
                r28.writeNoException()
                return r13
            L_0x0745:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r0 = r27.readInt()
                r12.noteStartCamera(r0)
                r28.writeNoException()
                return r13
            L_0x0755:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r0 = r27.readInt()
                r12.noteFlashlightOff(r0)
                r28.writeNoException()
                return r13
            L_0x0765:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r0 = r27.readInt()
                r12.noteFlashlightOn(r0)
                r28.writeNoException()
                return r13
            L_0x0775:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                r25.noteResetAudio()
                r28.writeNoException()
                return r13
            L_0x0781:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                r25.noteResetVideo()
                r28.writeNoException()
                return r13
            L_0x078d:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r0 = r27.readInt()
                r12.noteStopAudio(r0)
                r28.writeNoException()
                return r13
            L_0x079d:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r0 = r27.readInt()
                r12.noteStartAudio(r0)
                r28.writeNoException()
                return r13
            L_0x07ad:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r0 = r27.readInt()
                r12.noteStopVideo(r0)
                r28.writeNoException()
                return r13
            L_0x07bd:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r0 = r27.readInt()
                r12.noteStartVideo(r0)
                r28.writeNoException()
                return r13
            L_0x07cd:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r0 = r27.readInt()
                int r1 = r27.readInt()
                r12.noteStopSensor(r0, r1)
                r28.writeNoException()
                return r13
            L_0x07e1:
                r13 = r9
                r7 = r10
                r14.enforceInterface(r7)
                int r0 = r27.readInt()
                int r1 = r27.readInt()
                r12.noteStartSensor(r0, r1)
                r28.writeNoException()
                return r13
            L_0x07f5:
                r13 = r9
                r7 = r10
                r15.writeString(r7)
                return r13
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.IBatteryStats.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    long computeBatteryTimeRemaining() throws RemoteException;

    long computeChargeTimeRemaining() throws RemoteException;

    long getAwakeTimeBattery() throws RemoteException;

    long getAwakeTimePlugged() throws RemoteException;

    CellularBatteryStats getCellularBatteryStats() throws RemoteException;

    GpsBatteryStats getGpsBatteryStats() throws RemoteException;

    byte[] getStatistics() throws RemoteException;

    ParcelFileDescriptor getStatisticsStream() throws RemoteException;

    WifiBatteryStats getWifiBatteryStats() throws RemoteException;

    boolean isCharging() throws RemoteException;

    void noteBleScanResults(WorkSource workSource, int i) throws RemoteException;

    void noteBleScanStarted(WorkSource workSource, boolean z) throws RemoteException;

    void noteBleScanStopped(WorkSource workSource, boolean z) throws RemoteException;

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

    void noteGpsChanged(WorkSource workSource, WorkSource workSource2) throws RemoteException;

    void noteGpsSignalQuality(int i) throws RemoteException;

    void noteInteractive(boolean z) throws RemoteException;

    void noteJobFinish(String str, int i, int i2) throws RemoteException;

    void noteJobStart(String str, int i) throws RemoteException;

    void noteLongPartialWakelockFinish(String str, String str2, int i) throws RemoteException;

    void noteLongPartialWakelockFinishFromSource(String str, String str2, WorkSource workSource) throws RemoteException;

    void noteLongPartialWakelockStart(String str, String str2, int i) throws RemoteException;

    void noteLongPartialWakelockStartFromSource(String str, String str2, WorkSource workSource) throws RemoteException;

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

    void noteStartSensor(int i, int i2) throws RemoteException;

    void noteStartVideo(int i) throws RemoteException;

    void noteStartWakelock(int i, int i2, String str, String str2, int i3, boolean z) throws RemoteException;

    void noteStartWakelockFromSource(WorkSource workSource, int i, String str, String str2, int i2, boolean z) throws RemoteException;

    void noteStopAudio(int i) throws RemoteException;

    void noteStopCamera(int i) throws RemoteException;

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

    void noteWifiMulticastEnabled(int i) throws RemoteException;

    void noteWifiOff() throws RemoteException;

    void noteWifiOn() throws RemoteException;

    void noteWifiRadioPowerState(int i, long j, int i2) throws RemoteException;

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

    void setBatteryState(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) throws RemoteException;

    HealthStatsParceler takeUidSnapshot(int i) throws RemoteException;

    HealthStatsParceler[] takeUidSnapshots(int[] iArr) throws RemoteException;
}
