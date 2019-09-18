package com.huawei.android.app;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwDAMonitorCallback extends IInterface {

    public static abstract class Stub extends Binder implements IHwDAMonitorCallback {
        private static final String DESCRIPTOR = "com.huawei.android.app.IHwDAMonitorCallback";
        static final int TRANSACTION_DAMonitorReport = 6;
        static final int TRANSACTION_addPssToMap = 15;
        static final int TRANSACTION_getActivityImportCount = 1;
        static final int TRANSACTION_getCPUConfigGroupBG = 4;
        static final int TRANSACTION_getFirstDevSchedEventId = 5;
        static final int TRANSACTION_getRecentTask = 2;
        static final int TRANSACTION_isCPUConfigWhiteList = 3;
        static final int TRANSACTION_isExcludedInBGCheck = 28;
        static final int TRANSACTION_isFastKillSwitch = 30;
        static final int TRANSACTION_isResourceNeeded = 26;
        static final int TRANSACTION_killProcessGroupForQuickKill = 17;
        static final int TRANSACTION_noteActivityDisplayedStart = 29;
        static final int TRANSACTION_noteActivityStart = 14;
        static final int TRANSACTION_noteProcessStart = 18;
        static final int TRANSACTION_notifyActivityState = 7;
        static final int TRANSACTION_notifyAppEventToIaware = 12;
        static final int TRANSACTION_notifyProcessDied = 24;
        static final int TRANSACTION_notifyProcessGroupChange = 21;
        static final int TRANSACTION_notifyProcessGroupChangeCpu = 10;
        static final int TRANSACTION_notifyProcessStatusChange = 22;
        static final int TRANSACTION_notifyProcessWillDie = 23;
        static final int TRANSACTION_onPointerEvent = 13;
        static final int TRANSACTION_onWakefulnessChanged = 19;
        static final int TRANSACTION_recognizeFakeActivity = 20;
        static final int TRANSACTION_reportAppDiedMsg = 16;
        static final int TRANSACTION_reportCamera = 9;
        static final int TRANSACTION_reportData = 27;
        static final int TRANSACTION_reportScreenRecord = 8;
        static final int TRANSACTION_resetAppMngOomAdj = 25;
        static final int TRANSACTION_setVipThread = 11;

        private static class Proxy implements IHwDAMonitorCallback {
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

            public int getActivityImportCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getRecentTask() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int isCPUConfigWhiteList(String processName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCPUConfigGroupBG() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getFirstDevSchedEventId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int DAMonitorReport(int tag, String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tag);
                    _data.writeString(msg);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyActivityState(String activityInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(activityInfo);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportScreenRecord(int uid, int pid, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(status);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportCamera(int uid, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(status);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyProcessGroupChangeCpu(int pid, int uid, int grp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(grp);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVipThread(int pid, int renderThreadTid, boolean isSet) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(renderThreadTid);
                    _data.writeInt(isSet);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyAppEventToIaware(int type, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(packageName);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onPointerEvent(int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteActivityStart(String packageName, String processName, String activityName, int pid, int uid, boolean started) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(processName);
                    _data.writeString(activityName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(started);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPssToMap(String packageName, String procName, int uid, int pid, int procState, long pss, long uss, long swapPss, boolean test) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(procName);
                    try {
                        _data.writeInt(uid);
                        try {
                            _data.writeInt(pid);
                        } catch (Throwable th) {
                            th = th;
                            int i = procState;
                            long j = pss;
                            long j2 = uss;
                            long j3 = swapPss;
                            boolean z = test;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        int i2 = pid;
                        int i3 = procState;
                        long j4 = pss;
                        long j22 = uss;
                        long j32 = swapPss;
                        boolean z2 = test;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(procState);
                        try {
                            _data.writeLong(pss);
                            try {
                                _data.writeLong(uss);
                            } catch (Throwable th3) {
                                th = th3;
                                long j322 = swapPss;
                                boolean z22 = test;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            long j222 = uss;
                            long j3222 = swapPss;
                            boolean z222 = test;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        long j42 = pss;
                        long j2222 = uss;
                        long j32222 = swapPss;
                        boolean z2222 = test;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(swapPss);
                        try {
                            _data.writeInt(test ? 1 : 0);
                            try {
                                this.mRemote.transact(15, _data, _reply, 0);
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                            } catch (Throwable th6) {
                                th = th6;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        boolean z22222 = test;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    int i4 = uid;
                    int i22 = pid;
                    int i32 = procState;
                    long j422 = pss;
                    long j22222 = uss;
                    long j322222 = swapPss;
                    boolean z222222 = test;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public void reportAppDiedMsg(int userId, String processName, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(processName);
                    _data.writeString(reason);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int killProcessGroupForQuickKill(int uid, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(processName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(started);
                    _data.writeString(launcherMode);
                    _data.writeString(reason);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onWakefulnessChanged(int wakefulness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(wakefulness);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void recognizeFakeActivity(String compName, boolean isScreenOn, int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(compName);
                    _data.writeInt(isScreenOn);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyProcessGroupChange(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyProcessStatusChange(String pkg, String process, String hostingType, int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(process);
                    _data.writeString(hostingType);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyProcessWillDie(boolean byForceStop, boolean crashed, boolean byAnr, String packageName, int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(byForceStop);
                    _data.writeInt(crashed);
                    _data.writeInt(byAnr);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyProcessDied(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int resetAppMngOomAdj(int maxAdj, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxAdj);
                    _data.writeString(packageName);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isResourceNeeded(String resourceid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(resourceid);
                    boolean _result = false;
                    this.mRemote.transact(26, _data, _reply, 0);
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

            public void reportData(String resourceid, long timestamp, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(resourceid);
                    _data.writeLong(timestamp);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isExcludedInBGCheck(String pkg, String action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(action);
                    boolean _result = false;
                    this.mRemote.transact(28, _data, _reply, 0);
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

            public void noteActivityDisplayedStart(String componentName, int uid, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(componentName);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isFastKillSwitch(String processName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    boolean _result = false;
                    this.mRemote.transact(30, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwDAMonitorCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwDAMonitorCallback)) {
                return new Proxy(obj);
            }
            return (IHwDAMonitorCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
            */
        public boolean onTransact(int r30, android.os.Parcel r31, android.os.Parcel r32, int r33) throws android.os.RemoteException {
            /*
                r29 = this;
                r13 = r29
                r14 = r30
                r15 = r31
                r10 = r32
                java.lang.String r11 = "com.huawei.android.app.IHwDAMonitorCallback"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r16 = 1
                if (r14 == r0) goto L_0x0325
                r0 = 0
                switch(r14) {
                    case 1: goto L_0x0315;
                    case 2: goto L_0x0305;
                    case 3: goto L_0x02f1;
                    case 4: goto L_0x02e1;
                    case 5: goto L_0x02d1;
                    case 6: goto L_0x02b8;
                    case 7: goto L_0x02a9;
                    case 8: goto L_0x0292;
                    case 9: goto L_0x027f;
                    case 10: goto L_0x0268;
                    case 11: goto L_0x024c;
                    case 12: goto L_0x0239;
                    case 13: goto L_0x022a;
                    case 14: goto L_0x01fb;
                    case 15: goto L_0x01b5;
                    case 16: goto L_0x019f;
                    case 17: goto L_0x0189;
                    case 18: goto L_0x0153;
                    case 19: goto L_0x0145;
                    case 20: goto L_0x0126;
                    case 21: goto L_0x0114;
                    case 22: goto L_0x00f0;
                    case 23: goto L_0x00b8;
                    case 24: goto L_0x00a6;
                    case 25: goto L_0x0090;
                    case 26: goto L_0x007e;
                    case 27: goto L_0x005c;
                    case 28: goto L_0x0046;
                    case 29: goto L_0x0030;
                    case 30: goto L_0x001a;
                    default: goto L_0x0015;
                }
            L_0x0015:
                boolean r0 = super.onTransact(r30, r31, r32, r33)
                return r0
            L_0x001a:
                r15.enforceInterface(r11)
                java.lang.String r0 = r31.readString()
                int r1 = r31.readInt()
                boolean r2 = r13.isFastKillSwitch(r0, r1)
                r32.writeNoException()
                r10.writeInt(r2)
                return r16
            L_0x0030:
                r15.enforceInterface(r11)
                java.lang.String r0 = r31.readString()
                int r1 = r31.readInt()
                int r2 = r31.readInt()
                r13.noteActivityDisplayedStart(r0, r1, r2)
                r32.writeNoException()
                return r16
            L_0x0046:
                r15.enforceInterface(r11)
                java.lang.String r0 = r31.readString()
                java.lang.String r1 = r31.readString()
                boolean r2 = r13.isExcludedInBGCheck(r0, r1)
                r32.writeNoException()
                r10.writeInt(r2)
                return r16
            L_0x005c:
                r15.enforceInterface(r11)
                java.lang.String r0 = r31.readString()
                long r1 = r31.readLong()
                int r3 = r31.readInt()
                if (r3 == 0) goto L_0x0076
                android.os.Parcelable$Creator r3 = android.os.Bundle.CREATOR
                java.lang.Object r3 = r3.createFromParcel(r15)
                android.os.Bundle r3 = (android.os.Bundle) r3
                goto L_0x0077
            L_0x0076:
                r3 = 0
            L_0x0077:
                r13.reportData(r0, r1, r3)
                r32.writeNoException()
                return r16
            L_0x007e:
                r15.enforceInterface(r11)
                java.lang.String r0 = r31.readString()
                boolean r1 = r13.isResourceNeeded(r0)
                r32.writeNoException()
                r10.writeInt(r1)
                return r16
            L_0x0090:
                r15.enforceInterface(r11)
                int r0 = r31.readInt()
                java.lang.String r1 = r31.readString()
                int r2 = r13.resetAppMngOomAdj(r0, r1)
                r32.writeNoException()
                r10.writeInt(r2)
                return r16
            L_0x00a6:
                r15.enforceInterface(r11)
                int r0 = r31.readInt()
                int r1 = r31.readInt()
                r13.notifyProcessDied(r0, r1)
                r32.writeNoException()
                return r16
            L_0x00b8:
                r15.enforceInterface(r11)
                int r1 = r31.readInt()
                if (r1 == 0) goto L_0x00c4
                r1 = r16
                goto L_0x00c5
            L_0x00c4:
                r1 = r0
            L_0x00c5:
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x00ce
                r2 = r16
                goto L_0x00cf
            L_0x00ce:
                r2 = r0
            L_0x00cf:
                int r3 = r31.readInt()
                if (r3 == 0) goto L_0x00d8
                r3 = r16
                goto L_0x00d9
            L_0x00d8:
                r3 = r0
            L_0x00d9:
                java.lang.String r7 = r31.readString()
                int r8 = r31.readInt()
                int r9 = r31.readInt()
                r0 = r13
                r4 = r7
                r5 = r8
                r6 = r9
                r0.notifyProcessWillDie(r1, r2, r3, r4, r5, r6)
                r32.writeNoException()
                return r16
            L_0x00f0:
                r15.enforceInterface(r11)
                java.lang.String r6 = r31.readString()
                java.lang.String r7 = r31.readString()
                java.lang.String r8 = r31.readString()
                int r9 = r31.readInt()
                int r12 = r31.readInt()
                r0 = r13
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r5 = r12
                r0.notifyProcessStatusChange(r1, r2, r3, r4, r5)
                r32.writeNoException()
                return r16
            L_0x0114:
                r15.enforceInterface(r11)
                int r0 = r31.readInt()
                int r1 = r31.readInt()
                r13.notifyProcessGroupChange(r0, r1)
                r32.writeNoException()
                return r16
            L_0x0126:
                r15.enforceInterface(r11)
                java.lang.String r1 = r31.readString()
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x0136
                r0 = r16
            L_0x0136:
                int r2 = r31.readInt()
                int r3 = r31.readInt()
                r13.recognizeFakeActivity(r1, r0, r2, r3)
                r32.writeNoException()
                return r16
            L_0x0145:
                r15.enforceInterface(r11)
                int r0 = r31.readInt()
                r13.onWakefulnessChanged(r0)
                r32.writeNoException()
                return r16
            L_0x0153:
                r15.enforceInterface(r11)
                java.lang.String r8 = r31.readString()
                java.lang.String r9 = r31.readString()
                int r12 = r31.readInt()
                int r17 = r31.readInt()
                int r1 = r31.readInt()
                if (r1 == 0) goto L_0x016f
                r5 = r16
                goto L_0x0170
            L_0x016f:
                r5 = r0
            L_0x0170:
                java.lang.String r18 = r31.readString()
                java.lang.String r19 = r31.readString()
                r0 = r13
                r1 = r8
                r2 = r9
                r3 = r12
                r4 = r17
                r6 = r18
                r7 = r19
                r0.noteProcessStart(r1, r2, r3, r4, r5, r6, r7)
                r32.writeNoException()
                return r16
            L_0x0189:
                r15.enforceInterface(r11)
                int r0 = r31.readInt()
                int r1 = r31.readInt()
                int r2 = r13.killProcessGroupForQuickKill(r0, r1)
                r32.writeNoException()
                r10.writeInt(r2)
                return r16
            L_0x019f:
                r15.enforceInterface(r11)
                int r0 = r31.readInt()
                java.lang.String r1 = r31.readString()
                java.lang.String r2 = r31.readString()
                r13.reportAppDiedMsg(r0, r1, r2)
                r32.writeNoException()
                return r16
            L_0x01b5:
                r15.enforceInterface(r11)
                java.lang.String r17 = r31.readString()
                java.lang.String r18 = r31.readString()
                int r19 = r31.readInt()
                int r20 = r31.readInt()
                int r21 = r31.readInt()
                long r22 = r31.readLong()
                long r24 = r31.readLong()
                long r26 = r31.readLong()
                int r1 = r31.readInt()
                if (r1 == 0) goto L_0x01e1
                r12 = r16
                goto L_0x01e2
            L_0x01e1:
                r12 = r0
            L_0x01e2:
                r0 = r13
                r1 = r17
                r2 = r18
                r3 = r19
                r4 = r20
                r5 = r21
                r6 = r22
                r8 = r24
                r14 = r11
                r10 = r26
                r0.addPssToMap(r1, r2, r3, r4, r5, r6, r8, r10, r12)
                r32.writeNoException()
                return r16
            L_0x01fb:
                r14 = r11
                r15.enforceInterface(r14)
                java.lang.String r7 = r31.readString()
                java.lang.String r8 = r31.readString()
                java.lang.String r9 = r31.readString()
                int r10 = r31.readInt()
                int r11 = r31.readInt()
                int r1 = r31.readInt()
                if (r1 == 0) goto L_0x021c
                r6 = r16
                goto L_0x021d
            L_0x021c:
                r6 = r0
            L_0x021d:
                r0 = r13
                r1 = r7
                r2 = r8
                r3 = r9
                r4 = r10
                r5 = r11
                r0.noteActivityStart(r1, r2, r3, r4, r5, r6)
                r32.writeNoException()
                return r16
            L_0x022a:
                r14 = r11
                r15.enforceInterface(r14)
                int r0 = r31.readInt()
                r13.onPointerEvent(r0)
                r32.writeNoException()
                return r16
            L_0x0239:
                r14 = r11
                r15.enforceInterface(r14)
                int r0 = r31.readInt()
                java.lang.String r1 = r31.readString()
                r13.notifyAppEventToIaware(r0, r1)
                r32.writeNoException()
                return r16
            L_0x024c:
                r14 = r11
                r15.enforceInterface(r14)
                int r1 = r31.readInt()
                int r2 = r31.readInt()
                int r3 = r31.readInt()
                if (r3 == 0) goto L_0x0261
                r0 = r16
            L_0x0261:
                r13.setVipThread(r1, r2, r0)
                r32.writeNoException()
                return r16
            L_0x0268:
                r14 = r11
                r15.enforceInterface(r14)
                int r0 = r31.readInt()
                int r1 = r31.readInt()
                int r2 = r31.readInt()
                r13.notifyProcessGroupChangeCpu(r0, r1, r2)
                r32.writeNoException()
                return r16
            L_0x027f:
                r14 = r11
                r15.enforceInterface(r14)
                int r0 = r31.readInt()
                int r1 = r31.readInt()
                r13.reportCamera(r0, r1)
                r32.writeNoException()
                return r16
            L_0x0292:
                r14 = r11
                r15.enforceInterface(r14)
                int r0 = r31.readInt()
                int r1 = r31.readInt()
                int r2 = r31.readInt()
                r13.reportScreenRecord(r0, r1, r2)
                r32.writeNoException()
                return r16
            L_0x02a9:
                r14 = r11
                r15.enforceInterface(r14)
                java.lang.String r0 = r31.readString()
                r13.notifyActivityState(r0)
                r32.writeNoException()
                return r16
            L_0x02b8:
                r14 = r11
                r15.enforceInterface(r14)
                int r0 = r31.readInt()
                java.lang.String r1 = r31.readString()
                int r2 = r13.DAMonitorReport(r0, r1)
                r32.writeNoException()
                r3 = r32
                r3.writeInt(r2)
                return r16
            L_0x02d1:
                r3 = r10
                r14 = r11
                r15.enforceInterface(r14)
                int r0 = r29.getFirstDevSchedEventId()
                r32.writeNoException()
                r3.writeInt(r0)
                return r16
            L_0x02e1:
                r3 = r10
                r14 = r11
                r15.enforceInterface(r14)
                int r0 = r29.getCPUConfigGroupBG()
                r32.writeNoException()
                r3.writeInt(r0)
                return r16
            L_0x02f1:
                r3 = r10
                r14 = r11
                r15.enforceInterface(r14)
                java.lang.String r0 = r31.readString()
                int r1 = r13.isCPUConfigWhiteList(r0)
                r32.writeNoException()
                r3.writeInt(r1)
                return r16
            L_0x0305:
                r3 = r10
                r14 = r11
                r15.enforceInterface(r14)
                java.lang.String r0 = r29.getRecentTask()
                r32.writeNoException()
                r3.writeString(r0)
                return r16
            L_0x0315:
                r3 = r10
                r14 = r11
                r15.enforceInterface(r14)
                int r0 = r29.getActivityImportCount()
                r32.writeNoException()
                r3.writeInt(r0)
                return r16
            L_0x0325:
                r3 = r10
                r14 = r11
                r3.writeString(r14)
                return r16
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.app.IHwDAMonitorCallback.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    int DAMonitorReport(int i, String str) throws RemoteException;

    void addPssToMap(String str, String str2, int i, int i2, int i3, long j, long j2, long j3, boolean z) throws RemoteException;

    int getActivityImportCount() throws RemoteException;

    int getCPUConfigGroupBG() throws RemoteException;

    int getFirstDevSchedEventId() throws RemoteException;

    String getRecentTask() throws RemoteException;

    int isCPUConfigWhiteList(String str) throws RemoteException;

    boolean isExcludedInBGCheck(String str, String str2) throws RemoteException;

    boolean isFastKillSwitch(String str, int i) throws RemoteException;

    boolean isResourceNeeded(String str) throws RemoteException;

    int killProcessGroupForQuickKill(int i, int i2) throws RemoteException;

    void noteActivityDisplayedStart(String str, int i, int i2) throws RemoteException;

    void noteActivityStart(String str, String str2, String str3, int i, int i2, boolean z) throws RemoteException;

    void noteProcessStart(String str, String str2, int i, int i2, boolean z, String str3, String str4) throws RemoteException;

    void notifyActivityState(String str) throws RemoteException;

    void notifyAppEventToIaware(int i, String str) throws RemoteException;

    void notifyProcessDied(int i, int i2) throws RemoteException;

    void notifyProcessGroupChange(int i, int i2) throws RemoteException;

    void notifyProcessGroupChangeCpu(int i, int i2, int i3) throws RemoteException;

    void notifyProcessStatusChange(String str, String str2, String str3, int i, int i2) throws RemoteException;

    void notifyProcessWillDie(boolean z, boolean z2, boolean z3, String str, int i, int i2) throws RemoteException;

    void onPointerEvent(int i) throws RemoteException;

    void onWakefulnessChanged(int i) throws RemoteException;

    void recognizeFakeActivity(String str, boolean z, int i, int i2) throws RemoteException;

    void reportAppDiedMsg(int i, String str, String str2) throws RemoteException;

    void reportCamera(int i, int i2) throws RemoteException;

    void reportData(String str, long j, Bundle bundle) throws RemoteException;

    void reportScreenRecord(int i, int i2, int i3) throws RemoteException;

    int resetAppMngOomAdj(int i, String str) throws RemoteException;

    void setVipThread(int i, int i2, boolean z) throws RemoteException;
}
