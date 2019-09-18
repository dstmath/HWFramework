package android.os;

public interface IStatsCompanionService extends IInterface {

    public static abstract class Stub extends Binder implements IStatsCompanionService {
        private static final String DESCRIPTOR = "android.os.IStatsCompanionService";
        static final int TRANSACTION_cancelAlarmForSubscriberTriggering = 7;
        static final int TRANSACTION_cancelAnomalyAlarm = 3;
        static final int TRANSACTION_cancelPullingAlarm = 5;
        static final int TRANSACTION_pullData = 8;
        static final int TRANSACTION_sendDataBroadcast = 9;
        static final int TRANSACTION_sendSubscriberBroadcast = 10;
        static final int TRANSACTION_setAlarmForSubscriberTriggering = 6;
        static final int TRANSACTION_setAnomalyAlarm = 2;
        static final int TRANSACTION_setPullingAlarm = 4;
        static final int TRANSACTION_statsdReady = 1;
        static final int TRANSACTION_triggerUidSnapshot = 11;

        private static class Proxy implements IStatsCompanionService {
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

            public void statsdReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setAnomalyAlarm(long timestampMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(timestampMs);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void cancelAnomalyAlarm() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setPullingAlarm(long nextPullTimeMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(nextPullTimeMs);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void cancelPullingAlarm() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setAlarmForSubscriberTriggering(long timestampMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(timestampMs);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void cancelAlarmForSubscriberTriggering() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public StatsLogEventWrapper[] pullData(int pullCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pullCode);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return (StatsLogEventWrapper[]) _reply.createTypedArray(StatsLogEventWrapper.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendDataBroadcast(IBinder intentSender, long lastReportTimeNs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(intentSender);
                    _data.writeLong(lastReportTimeNs);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendSubscriberBroadcast(IBinder intentSender, long configUid, long configId, long subscriptionId, long subscriptionRuleId, String[] cookies, StatsDimensionsValue dimensionsValue) throws RemoteException {
                StatsDimensionsValue statsDimensionsValue = dimensionsValue;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(intentSender);
                    } catch (Throwable th) {
                        th = th;
                        long j = configUid;
                        long j2 = configId;
                        long j3 = subscriptionId;
                        long j4 = subscriptionRuleId;
                        String[] strArr = cookies;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(configUid);
                        try {
                            _data.writeLong(configId);
                            try {
                                _data.writeLong(subscriptionId);
                            } catch (Throwable th2) {
                                th = th2;
                                long j42 = subscriptionRuleId;
                                String[] strArr2 = cookies;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            long j32 = subscriptionId;
                            long j422 = subscriptionRuleId;
                            String[] strArr22 = cookies;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        long j22 = configId;
                        long j322 = subscriptionId;
                        long j4222 = subscriptionRuleId;
                        String[] strArr222 = cookies;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(subscriptionRuleId);
                        try {
                            _data.writeStringArray(cookies);
                            if (statsDimensionsValue != null) {
                                _data.writeInt(1);
                                statsDimensionsValue.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                this.mRemote.transact(10, _data, null, 1);
                                _data.recycle();
                            } catch (Throwable th5) {
                                th = th5;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        String[] strArr2222 = cookies;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    IBinder iBinder = intentSender;
                    long j5 = configUid;
                    long j222 = configId;
                    long j3222 = subscriptionId;
                    long j42222 = subscriptionRuleId;
                    String[] strArr22222 = cookies;
                    _data.recycle();
                    throw th;
                }
            }

            public void triggerUidSnapshot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IStatsCompanionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IStatsCompanionService)) {
                return new Proxy(obj);
            }
            return (IStatsCompanionService) iin;
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
        public boolean onTransact(int r28, android.os.Parcel r29, android.os.Parcel r30, int r31) throws android.os.RemoteException {
            /*
                r27 = this;
                r12 = r27
                r13 = r28
                r14 = r29
                r15 = r30
                java.lang.String r10 = "android.os.IStatsCompanionService"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r8 = 1
                if (r13 == r0) goto L_0x00d3
                switch(r13) {
                    case 1: goto L_0x00ca;
                    case 2: goto L_0x00bd;
                    case 3: goto L_0x00b4;
                    case 4: goto L_0x00a7;
                    case 5: goto L_0x009e;
                    case 6: goto L_0x0091;
                    case 7: goto L_0x0088;
                    case 8: goto L_0x0074;
                    case 9: goto L_0x0060;
                    case 10: goto L_0x001f;
                    case 11: goto L_0x0018;
                    default: goto L_0x0013;
                }
            L_0x0013:
                boolean r0 = super.onTransact(r28, r29, r30, r31)
                return r0
            L_0x0018:
                r14.enforceInterface(r10)
                r27.triggerUidSnapshot()
                return r8
            L_0x001f:
                r14.enforceInterface(r10)
                android.os.IBinder r16 = r29.readStrongBinder()
                long r17 = r29.readLong()
                long r19 = r29.readLong()
                long r21 = r29.readLong()
                long r23 = r29.readLong()
                java.lang.String[] r25 = r29.createStringArray()
                int r0 = r29.readInt()
                if (r0 == 0) goto L_0x004a
                android.os.Parcelable$Creator<android.os.StatsDimensionsValue> r0 = android.os.StatsDimensionsValue.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.os.StatsDimensionsValue r0 = (android.os.StatsDimensionsValue) r0
            L_0x0048:
                r11 = r0
                goto L_0x004c
            L_0x004a:
                r0 = 0
                goto L_0x0048
            L_0x004c:
                r0 = r12
                r1 = r16
                r2 = r17
                r4 = r19
                r6 = r21
                r13 = r8
                r8 = r23
                r26 = r10
                r10 = r25
                r0.sendSubscriberBroadcast(r1, r2, r4, r6, r8, r10, r11)
                return r13
            L_0x0060:
                r13 = r8
                r26 = r10
                r0 = r26
                r14.enforceInterface(r0)
                android.os.IBinder r1 = r29.readStrongBinder()
                long r2 = r29.readLong()
                r12.sendDataBroadcast(r1, r2)
                return r13
            L_0x0074:
                r13 = r8
                r0 = r10
                r14.enforceInterface(r0)
                int r1 = r29.readInt()
                android.os.StatsLogEventWrapper[] r2 = r12.pullData(r1)
                r30.writeNoException()
                r15.writeTypedArray(r2, r13)
                return r13
            L_0x0088:
                r13 = r8
                r0 = r10
                r14.enforceInterface(r0)
                r27.cancelAlarmForSubscriberTriggering()
                return r13
            L_0x0091:
                r13 = r8
                r0 = r10
                r14.enforceInterface(r0)
                long r1 = r29.readLong()
                r12.setAlarmForSubscriberTriggering(r1)
                return r13
            L_0x009e:
                r13 = r8
                r0 = r10
                r14.enforceInterface(r0)
                r27.cancelPullingAlarm()
                return r13
            L_0x00a7:
                r13 = r8
                r0 = r10
                r14.enforceInterface(r0)
                long r1 = r29.readLong()
                r12.setPullingAlarm(r1)
                return r13
            L_0x00b4:
                r13 = r8
                r0 = r10
                r14.enforceInterface(r0)
                r27.cancelAnomalyAlarm()
                return r13
            L_0x00bd:
                r13 = r8
                r0 = r10
                r14.enforceInterface(r0)
                long r1 = r29.readLong()
                r12.setAnomalyAlarm(r1)
                return r13
            L_0x00ca:
                r13 = r8
                r0 = r10
                r14.enforceInterface(r0)
                r27.statsdReady()
                return r13
            L_0x00d3:
                r13 = r8
                r0 = r10
                r15.writeString(r0)
                return r13
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.IStatsCompanionService.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    void cancelAlarmForSubscriberTriggering() throws RemoteException;

    void cancelAnomalyAlarm() throws RemoteException;

    void cancelPullingAlarm() throws RemoteException;

    StatsLogEventWrapper[] pullData(int i) throws RemoteException;

    void sendDataBroadcast(IBinder iBinder, long j) throws RemoteException;

    void sendSubscriberBroadcast(IBinder iBinder, long j, long j2, long j3, long j4, String[] strArr, StatsDimensionsValue statsDimensionsValue) throws RemoteException;

    void setAlarmForSubscriberTriggering(long j) throws RemoteException;

    void setAnomalyAlarm(long j) throws RemoteException;

    void setPullingAlarm(long j) throws RemoteException;

    void statsdReady() throws RemoteException;

    void triggerUidSnapshot() throws RemoteException;
}
