package com.huawei.hiai.awareness.service;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.hiai.awareness.client.AwarenessEnvelope;
import com.huawei.hiai.awareness.service.IAwarenessListener;
import com.huawei.hiai.awareness.service.IRequestCallBack;

public interface IAwarenessService extends IInterface {
    boolean accept(AwarenessEnvelope awarenessEnvelope) throws RemoteException;

    boolean checkSdkVersion(int i) throws RemoteException;

    boolean checkServerVersion(int i) throws RemoteException;

    String getAwarenessApiVersion() throws RemoteException;

    RequestResult getCurrentAwareness(int i, boolean z, Bundle bundle, String str) throws RemoteException;

    RequestResult getCurrentStatus(int i) throws RemoteException;

    RequestResult getExtendFenceTriggerResult(ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    RequestResult getFenceTriggerResult(AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    RequestResult getSupportAwarenessCapability(int i) throws RemoteException;

    boolean isIntegrateSensorHub() throws RemoteException;

    boolean registerAppLifeChangeFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerAppUseTotalTimeFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerAwarenessListener(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, IAwarenessListener iAwarenessListener) throws RemoteException;

    boolean registerBroadcastEventFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerCustomLocationFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerDatabaseMonitorFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerDeviceStatusFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerDeviceUseTotalTimeFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerLocationFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerMapInfoReportFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerMotionFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerMovementFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerOneAppContinuousUseTimeFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerScreenUnlockFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerScreenUnlockTotalNumberFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerSwingFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerTimeFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean setClientInfo(String str, Bundle bundle) throws RemoteException;

    RequestResult setReportPeriod(ExtendAwarenessFence extendAwarenessFence) throws RemoteException;

    int setSwingController(int i) throws RemoteException;

    boolean unRegisterAwarenessListener(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, IAwarenessListener iAwarenessListener) throws RemoteException;

    boolean unRegisterExtendFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean unRegisterFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    public static abstract class Stub extends Binder implements IAwarenessService {
        private static final String DESCRIPTOR = "com.huawei.hiai.awareness.service.IAwarenessService";
        static final int TRANSACTION_accept = 33;
        static final int TRANSACTION_checkSdkVersion = 32;
        static final int TRANSACTION_checkServerVersion = 12;
        static final int TRANSACTION_getAwarenessApiVersion = 13;
        static final int TRANSACTION_getCurrentAwareness = 14;
        static final int TRANSACTION_getCurrentStatus = 1;
        static final int TRANSACTION_getExtendFenceTriggerResult = 18;
        static final int TRANSACTION_getFenceTriggerResult = 11;
        static final int TRANSACTION_getSupportAwarenessCapability = 19;
        static final int TRANSACTION_isIntegrateSensorHub = 23;
        static final int TRANSACTION_registerAppLifeChangeFence = 24;
        static final int TRANSACTION_registerAppUseTotalTimeFence = 5;
        static final int TRANSACTION_registerAwarenessListener = 27;
        static final int TRANSACTION_registerBroadcastEventFence = 16;
        static final int TRANSACTION_registerCustomLocationFence = 15;
        static final int TRANSACTION_registerDatabaseMonitorFence = 31;
        static final int TRANSACTION_registerDeviceStatusFence = 21;
        static final int TRANSACTION_registerDeviceUseTotalTimeFence = 7;
        static final int TRANSACTION_registerLocationFence = 4;
        static final int TRANSACTION_registerMapInfoReportFence = 30;
        static final int TRANSACTION_registerMotionFence = 2;
        static final int TRANSACTION_registerMovementFence = 20;
        static final int TRANSACTION_registerOneAppContinuousUseTimeFence = 6;
        static final int TRANSACTION_registerScreenUnlockFence = 9;
        static final int TRANSACTION_registerScreenUnlockTotalNumberFence = 8;
        static final int TRANSACTION_registerSwingFence = 25;
        static final int TRANSACTION_registerTimeFence = 3;
        static final int TRANSACTION_setClientInfo = 29;
        static final int TRANSACTION_setReportPeriod = 22;
        static final int TRANSACTION_setSwingController = 26;
        static final int TRANSACTION_unRegisterAwarenessListener = 28;
        static final int TRANSACTION_unRegisterExtendFence = 17;
        static final int TRANSACTION_unRegisterFence = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAwarenessService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAwarenessService)) {
                return new Proxy(obj);
            }
            return (IAwarenessService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            AwarenessFence _arg1;
            Bundle _arg2;
            PendingIntent _arg3;
            AwarenessFence _arg12;
            Bundle _arg22;
            PendingIntent _arg32;
            AwarenessFence _arg13;
            Bundle _arg23;
            PendingIntent _arg33;
            AwarenessFence _arg14;
            Bundle _arg24;
            PendingIntent _arg34;
            AwarenessFence _arg15;
            Bundle _arg25;
            PendingIntent _arg35;
            AwarenessFence _arg16;
            Bundle _arg26;
            PendingIntent _arg36;
            AwarenessFence _arg17;
            Bundle _arg27;
            PendingIntent _arg37;
            AwarenessFence _arg18;
            Bundle _arg28;
            PendingIntent _arg38;
            AwarenessFence _arg19;
            Bundle _arg29;
            PendingIntent _arg39;
            AwarenessFence _arg0;
            Bundle _arg110;
            PendingIntent _arg210;
            Bundle _arg211;
            ExtendAwarenessFence _arg111;
            Bundle _arg212;
            PendingIntent _arg310;
            ExtendAwarenessFence _arg112;
            Bundle _arg213;
            PendingIntent _arg311;
            ExtendAwarenessFence _arg113;
            Bundle _arg214;
            PendingIntent _arg312;
            ExtendAwarenessFence _arg02;
            Bundle _arg114;
            PendingIntent _arg215;
            ExtendAwarenessFence _arg115;
            Bundle _arg216;
            PendingIntent _arg313;
            ExtendAwarenessFence _arg116;
            Bundle _arg217;
            PendingIntent _arg314;
            ExtendAwarenessFence _arg03;
            ExtendAwarenessFence _arg117;
            Bundle _arg218;
            PendingIntent _arg315;
            ExtendAwarenessFence _arg118;
            Bundle _arg219;
            PendingIntent _arg316;
            ExtendAwarenessFence _arg119;
            ExtendAwarenessFence _arg120;
            Bundle _arg121;
            ExtendAwarenessFence _arg122;
            Bundle _arg220;
            PendingIntent _arg317;
            ExtendAwarenessFence _arg123;
            Bundle _arg221;
            PendingIntent _arg318;
            AwarenessEnvelope _arg04;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        RequestResult _result = getCurrentStatus(data.readInt());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg05 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = AwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        boolean registerMotionFence = registerMotionFence(_arg05, _arg1, _arg2, _arg3);
                        reply.writeNoException();
                        reply.writeInt(registerMotionFence ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg06 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg12 = AwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        boolean registerTimeFence = registerTimeFence(_arg06, _arg12, _arg22, _arg32);
                        reply.writeNoException();
                        reply.writeInt(registerTimeFence ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg07 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = AwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg33 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg33 = null;
                        }
                        boolean registerLocationFence = registerLocationFence(_arg07, _arg13, _arg23, _arg33);
                        reply.writeNoException();
                        reply.writeInt(registerLocationFence ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg08 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg14 = AwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg24 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg34 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg34 = null;
                        }
                        boolean registerAppUseTotalTimeFence = registerAppUseTotalTimeFence(_arg08, _arg14, _arg24, _arg34);
                        reply.writeNoException();
                        reply.writeInt(registerAppUseTotalTimeFence ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg09 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg15 = AwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg25 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg25 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg35 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg35 = null;
                        }
                        boolean registerOneAppContinuousUseTimeFence = registerOneAppContinuousUseTimeFence(_arg09, _arg15, _arg25, _arg35);
                        reply.writeNoException();
                        reply.writeInt(registerOneAppContinuousUseTimeFence ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg010 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg16 = AwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg26 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg26 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg36 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg36 = null;
                        }
                        boolean registerDeviceUseTotalTimeFence = registerDeviceUseTotalTimeFence(_arg010, _arg16, _arg26, _arg36);
                        reply.writeNoException();
                        reply.writeInt(registerDeviceUseTotalTimeFence ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg011 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg17 = AwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg27 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg27 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg37 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg37 = null;
                        }
                        boolean registerScreenUnlockTotalNumberFence = registerScreenUnlockTotalNumberFence(_arg011, _arg17, _arg27, _arg37);
                        reply.writeNoException();
                        reply.writeInt(registerScreenUnlockTotalNumberFence ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg012 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg18 = AwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg28 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg28 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg38 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg38 = null;
                        }
                        boolean registerScreenUnlockFence = registerScreenUnlockFence(_arg012, _arg18, _arg28, _arg38);
                        reply.writeNoException();
                        reply.writeInt(registerScreenUnlockFence ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg013 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg19 = AwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg19 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg29 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg29 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg39 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg39 = null;
                        }
                        boolean unRegisterFence = unRegisterFence(_arg013, _arg19, _arg29, _arg39);
                        reply.writeNoException();
                        reply.writeInt(unRegisterFence ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = AwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg110 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg110 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg210 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg210 = null;
                        }
                        RequestResult _result2 = getFenceTriggerResult(_arg0, _arg110, _arg210);
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean checkServerVersion = checkServerVersion(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(checkServerVersion ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getAwarenessApiVersion();
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg014 = data.readInt();
                        boolean _arg124 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg211 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg211 = null;
                        }
                        RequestResult _result4 = getCurrentAwareness(_arg014, _arg124, _arg211, data.readString());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg015 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg111 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg111 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg212 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg212 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg310 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg310 = null;
                        }
                        boolean registerCustomLocationFence = registerCustomLocationFence(_arg015, _arg111, _arg212, _arg310);
                        reply.writeNoException();
                        reply.writeInt(registerCustomLocationFence ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg016 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg112 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg112 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg213 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg213 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg311 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg311 = null;
                        }
                        boolean registerBroadcastEventFence = registerBroadcastEventFence(_arg016, _arg112, _arg213, _arg311);
                        reply.writeNoException();
                        reply.writeInt(registerBroadcastEventFence ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg017 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg113 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg113 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg214 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg214 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg312 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg312 = null;
                        }
                        boolean unRegisterExtendFence = unRegisterExtendFence(_arg017, _arg113, _arg214, _arg312);
                        reply.writeNoException();
                        reply.writeInt(unRegisterExtendFence ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg114 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg114 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg215 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg215 = null;
                        }
                        RequestResult _result5 = getExtendFenceTriggerResult(_arg02, _arg114, _arg215);
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        RequestResult _result6 = getSupportAwarenessCapability(data.readInt());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg018 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg115 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg115 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg216 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg216 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg313 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg313 = null;
                        }
                        boolean registerMovementFence = registerMovementFence(_arg018, _arg115, _arg216, _arg313);
                        reply.writeNoException();
                        reply.writeInt(registerMovementFence ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg019 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg116 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg116 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg217 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg217 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg314 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg314 = null;
                        }
                        boolean registerDeviceStatusFence = registerDeviceStatusFence(_arg019, _arg116, _arg217, _arg314);
                        reply.writeNoException();
                        reply.writeInt(registerDeviceStatusFence ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        RequestResult _result7 = setReportPeriod(_arg03);
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isIntegrateSensorHub = isIntegrateSensorHub();
                        reply.writeNoException();
                        reply.writeInt(isIntegrateSensorHub ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg020 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg117 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg117 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg218 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg218 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg315 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg315 = null;
                        }
                        boolean registerAppLifeChangeFence = registerAppLifeChangeFence(_arg020, _arg117, _arg218, _arg315);
                        reply.writeNoException();
                        reply.writeInt(registerAppLifeChangeFence ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg021 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg118 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg118 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg219 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg219 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg316 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg316 = null;
                        }
                        boolean registerSwingFence = registerSwingFence(_arg021, _arg118, _arg219, _arg316);
                        reply.writeNoException();
                        reply.writeInt(registerSwingFence ? 1 : 0);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = setSwingController(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg022 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg119 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg119 = null;
                        }
                        boolean registerAwarenessListener = registerAwarenessListener(_arg022, _arg119, IAwarenessListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerAwarenessListener ? 1 : 0);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg023 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg120 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg120 = null;
                        }
                        boolean unRegisterAwarenessListener = unRegisterAwarenessListener(_arg023, _arg120, IAwarenessListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unRegisterAwarenessListener ? 1 : 0);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg024 = data.readString();
                        if (data.readInt() != 0) {
                            _arg121 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg121 = null;
                        }
                        boolean clientInfo = setClientInfo(_arg024, _arg121);
                        reply.writeNoException();
                        reply.writeInt(clientInfo ? 1 : 0);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg025 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg122 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg122 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg220 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg220 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg317 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg317 = null;
                        }
                        boolean registerMapInfoReportFence = registerMapInfoReportFence(_arg025, _arg122, _arg220, _arg317);
                        reply.writeNoException();
                        reply.writeInt(registerMapInfoReportFence ? 1 : 0);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        IRequestCallBack _arg026 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg123 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                        } else {
                            _arg123 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg221 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg221 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg318 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg318 = null;
                        }
                        boolean registerDatabaseMonitorFence = registerDatabaseMonitorFence(_arg026, _arg123, _arg221, _arg318);
                        reply.writeNoException();
                        reply.writeInt(registerDatabaseMonitorFence ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        boolean checkSdkVersion = checkSdkVersion(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(checkSdkVersion ? 1 : 0);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = AwarenessEnvelope.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        boolean accept = accept(_arg04);
                        reply.writeNoException();
                        reply.writeInt(accept ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAwarenessService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public RequestResult getCurrentStatus(int type) throws RemoteException {
                RequestResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RequestResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerMotionFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerTimeFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerLocationFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerAppUseTotalTimeFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerOneAppContinuousUseTimeFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerDeviceUseTotalTimeFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerScreenUnlockTotalNumberFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerScreenUnlockFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean unRegisterFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public RequestResult getFenceTriggerResult(AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                RequestResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RequestResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean checkServerVersion(int apiVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(apiVersion);
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
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

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public String getAwarenessApiVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public RequestResult getCurrentAwareness(int type, boolean isCustom, Bundle bundle, String callerPackageName) throws RemoteException {
                RequestResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(isCustom ? 1 : 0);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackageName);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RequestResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerCustomLocationFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerBroadcastEventFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean unRegisterExtendFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public RequestResult getExtendFenceTriggerResult(ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                RequestResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RequestResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public RequestResult getSupportAwarenessCapability(int type) throws RemoteException {
                RequestResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RequestResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerMovementFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerDeviceStatusFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public RequestResult setReportPeriod(ExtendAwarenessFence awarenessFence) throws RemoteException {
                RequestResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RequestResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean isIntegrateSensorHub() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(23, _data, _reply, 0);
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

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerAppLifeChangeFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerSwingFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public int setSwingController(int controlCmd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(controlCmd);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerAwarenessListener(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, IAwarenessListener awarenessListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (awarenessListener != null) {
                        iBinder = awarenessListener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean unRegisterAwarenessListener(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, IAwarenessListener awarenessListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (awarenessListener != null) {
                        iBinder = awarenessListener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean setClientInfo(String callerPackageName, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    boolean _result = true;
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerMapInfoReportFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean registerDatabaseMonitorFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = true;
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pendingOperation != null) {
                        _data.writeInt(1);
                        pendingOperation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean checkSdkVersion(int sdkVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sdkVersion);
                    boolean _result = false;
                    this.mRemote.transact(32, _data, _reply, 0);
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

            @Override // com.huawei.hiai.awareness.service.IAwarenessService
            public boolean accept(AwarenessEnvelope request) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
