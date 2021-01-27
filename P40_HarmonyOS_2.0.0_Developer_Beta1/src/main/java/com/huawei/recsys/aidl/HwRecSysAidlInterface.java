package com.huawei.recsys.aidl;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import com.huawei.recsys.aidl.IHwRecSysCallBack;
import java.util.List;

public interface HwRecSysAidlInterface extends IInterface {
    List<String> clsRuleManger(String str, String str2, String str3, int i) throws RemoteException;

    void configFile(String str, String str2, int i) throws RemoteException;

    String doNotificationCollect(StatusBarNotification statusBarNotification) throws RemoteException;

    String doNotificationCollectTest(String str, boolean z) throws RemoteException;

    HwRecResult getCandisetId(String str, String str2) throws RemoteException;

    String getCurrentScene() throws RemoteException;

    int getOutputNum(String str) throws RemoteException;

    void getRecommendation(IHwRecSysCallBack iHwRecSysCallBack, String str) throws RemoteException;

    String getRequestNeed(boolean z) throws RemoteException;

    void handleEvent(int i, Bundle bundle) throws RemoteException;

    boolean isAllowABTest(String str) throws RemoteException;

    boolean isInitConfigFile(String str) throws RemoteException;

    void registerCallBack(IHwRecSysCallBack iHwRecSysCallBack, String str) throws RemoteException;

    void requestRecRes(IHwRecSysCallBack iHwRecSysCallBack, String str) throws RemoteException;

    int ruleManager(String str, String str2, int i) throws RemoteException;

    void setCandidateCount(int i) throws RemoteException;

    void setClickRecFeedBack(int i, int i2) throws RemoteException;

    void setReportDirectService(int i, int i2) throws RemoteException;

    void unregisterCallBack(IHwRecSysCallBack iHwRecSysCallBack, String str) throws RemoteException;

    void updateCandidateSet(String str) throws RemoteException;

    void updateCloudRules(String str) throws RemoteException;

    public static class Default implements HwRecSysAidlInterface {
        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public void getRecommendation(IHwRecSysCallBack callback, String key) throws RemoteException {
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public String getCurrentScene() throws RemoteException {
            return null;
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public void setReportDirectService(int type, int value) throws RemoteException {
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public void setClickRecFeedBack(int id, int serviceType) throws RemoteException {
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public void configFile(String key, String path, int operateType) throws RemoteException {
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public void requestRecRes(IHwRecSysCallBack callback, String jobName) throws RemoteException {
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public int ruleManager(String jobName, String rule, int op) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public void registerCallBack(IHwRecSysCallBack callback, String packageName) throws RemoteException {
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public void unregisterCallBack(IHwRecSysCallBack callback, String packageName) throws RemoteException {
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public String doNotificationCollect(StatusBarNotification statusBarNotification) throws RemoteException {
            return null;
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public List<String> clsRuleManger(String businessname, String ruleName, String key, int operator) throws RemoteException {
            return null;
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public String getRequestNeed(boolean isCandidateRquest) throws RemoteException {
            return null;
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public void updateCandidateSet(String candiJson) throws RemoteException {
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public void updateCloudRules(String cloudRulesJson) throws RemoteException {
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public void setCandidateCount(int candidateCount) throws RemoteException {
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public HwRecResult getCandisetId(String pkg, String service) throws RemoteException {
            return null;
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public String doNotificationCollectTest(String text, boolean isOngoing) throws RemoteException {
            return null;
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public boolean isAllowABTest(String key) throws RemoteException {
            return false;
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public int getOutputNum(String key) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public boolean isInitConfigFile(String key) throws RemoteException {
            return false;
        }

        @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
        public void handleEvent(int action, Bundle bundle) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements HwRecSysAidlInterface {
        private static final String DESCRIPTOR = "com.huawei.recsys.aidl.HwRecSysAidlInterface";
        static final int TRANSACTION_clsRuleManger = 11;
        static final int TRANSACTION_configFile = 5;
        static final int TRANSACTION_doNotificationCollect = 10;
        static final int TRANSACTION_doNotificationCollectTest = 17;
        static final int TRANSACTION_getCandisetId = 16;
        static final int TRANSACTION_getCurrentScene = 2;
        static final int TRANSACTION_getOutputNum = 19;
        static final int TRANSACTION_getRecommendation = 1;
        static final int TRANSACTION_getRequestNeed = 12;
        static final int TRANSACTION_handleEvent = 21;
        static final int TRANSACTION_isAllowABTest = 18;
        static final int TRANSACTION_isInitConfigFile = 20;
        static final int TRANSACTION_registerCallBack = 8;
        static final int TRANSACTION_requestRecRes = 6;
        static final int TRANSACTION_ruleManager = 7;
        static final int TRANSACTION_setCandidateCount = 15;
        static final int TRANSACTION_setClickRecFeedBack = 4;
        static final int TRANSACTION_setReportDirectService = 3;
        static final int TRANSACTION_unregisterCallBack = 9;
        static final int TRANSACTION_updateCandidateSet = 13;
        static final int TRANSACTION_updateCloudRules = 14;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static HwRecSysAidlInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof HwRecSysAidlInterface)) {
                return new Proxy(obj);
            }
            return (HwRecSysAidlInterface) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            StatusBarNotification _arg0;
            Bundle _arg1;
            if (code != 1598968902) {
                boolean _arg12 = false;
                switch (code) {
                    case TRANSACTION_getRecommendation /* 1 */:
                        data.enforceInterface(DESCRIPTOR);
                        getRecommendation(IHwRecSysCallBack.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getCurrentScene /* 2 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getCurrentScene();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case TRANSACTION_setReportDirectService /* 3 */:
                        data.enforceInterface(DESCRIPTOR);
                        setReportDirectService(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setClickRecFeedBack /* 4 */:
                        data.enforceInterface(DESCRIPTOR);
                        setClickRecFeedBack(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_configFile /* 5 */:
                        data.enforceInterface(DESCRIPTOR);
                        configFile(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_requestRecRes /* 6 */:
                        data.enforceInterface(DESCRIPTOR);
                        requestRecRes(IHwRecSysCallBack.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_ruleManager /* 7 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = ruleManager(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case TRANSACTION_registerCallBack /* 8 */:
                        data.enforceInterface(DESCRIPTOR);
                        registerCallBack(IHwRecSysCallBack.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_unregisterCallBack /* 9 */:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterCallBack(IHwRecSysCallBack.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_doNotificationCollect /* 10 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (StatusBarNotification) StatusBarNotification.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        String _result3 = doNotificationCollect(_arg0);
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case TRANSACTION_clsRuleManger /* 11 */:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result4 = clsRuleManger(data.readString(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result4);
                        return true;
                    case TRANSACTION_getRequestNeed /* 12 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg12 = TRANSACTION_getRecommendation;
                        }
                        String _result5 = getRequestNeed(_arg12);
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case TRANSACTION_updateCandidateSet /* 13 */:
                        data.enforceInterface(DESCRIPTOR);
                        updateCandidateSet(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_updateCloudRules /* 14 */:
                        data.enforceInterface(DESCRIPTOR);
                        updateCloudRules(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setCandidateCount /* 15 */:
                        data.enforceInterface(DESCRIPTOR);
                        setCandidateCount(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getCandisetId /* 16 */:
                        data.enforceInterface(DESCRIPTOR);
                        HwRecResult _result6 = getCandisetId(data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(TRANSACTION_getRecommendation);
                            _result6.writeToParcel(reply, TRANSACTION_getRecommendation);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_doNotificationCollectTest /* 17 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = TRANSACTION_getRecommendation;
                        }
                        String _result7 = doNotificationCollectTest(_arg02, _arg12);
                        reply.writeNoException();
                        reply.writeString(_result7);
                        return true;
                    case TRANSACTION_isAllowABTest /* 18 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAllowABTest = isAllowABTest(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isAllowABTest ? 1 : 0);
                        return true;
                    case TRANSACTION_getOutputNum /* 19 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getOutputNum(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case TRANSACTION_isInitConfigFile /* 20 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInitConfigFile = isInitConfigFile(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isInitConfigFile ? 1 : 0);
                        return true;
                    case TRANSACTION_handleEvent /* 21 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        handleEvent(_arg03, _arg1);
                        reply.writeNoException();
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
        public static class Proxy implements HwRecSysAidlInterface {
            public static HwRecSysAidlInterface sDefaultImpl;
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

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public void getRecommendation(IHwRecSysCallBack callback, String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(key);
                    if (this.mRemote.transact(Stub.TRANSACTION_getRecommendation, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getRecommendation(callback, key);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public String getCurrentScene() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCurrentScene, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentScene();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public void setReportDirectService(int type, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(value);
                    if (this.mRemote.transact(Stub.TRANSACTION_setReportDirectService, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setReportDirectService(type, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public void setClickRecFeedBack(int id, int serviceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(serviceType);
                    if (this.mRemote.transact(Stub.TRANSACTION_setClickRecFeedBack, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setClickRecFeedBack(id, serviceType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public void configFile(String key, String path, int operateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(path);
                    _data.writeInt(operateType);
                    if (this.mRemote.transact(Stub.TRANSACTION_configFile, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().configFile(key, path, operateType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public void requestRecRes(IHwRecSysCallBack callback, String jobName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(jobName);
                    if (this.mRemote.transact(Stub.TRANSACTION_requestRecRes, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestRecRes(callback, jobName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public int ruleManager(String jobName, String rule, int op) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(jobName);
                    _data.writeString(rule);
                    _data.writeInt(op);
                    if (!this.mRemote.transact(Stub.TRANSACTION_ruleManager, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ruleManager(jobName, rule, op);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public void registerCallBack(IHwRecSysCallBack callback, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(Stub.TRANSACTION_registerCallBack, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallBack(callback, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public void unregisterCallBack(IHwRecSysCallBack callback, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(Stub.TRANSACTION_unregisterCallBack, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterCallBack(callback, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public String doNotificationCollect(StatusBarNotification statusBarNotification) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (statusBarNotification != null) {
                        _data.writeInt(Stub.TRANSACTION_getRecommendation);
                        statusBarNotification.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(Stub.TRANSACTION_doNotificationCollect, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().doNotificationCollect(statusBarNotification);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public List<String> clsRuleManger(String businessname, String ruleName, String key, int operator) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(businessname);
                    _data.writeString(ruleName);
                    _data.writeString(key);
                    _data.writeInt(operator);
                    if (!this.mRemote.transact(Stub.TRANSACTION_clsRuleManger, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().clsRuleManger(businessname, ruleName, key, operator);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public String getRequestNeed(boolean isCandidateRquest) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isCandidateRquest ? Stub.TRANSACTION_getRecommendation : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getRequestNeed, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRequestNeed(isCandidateRquest);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public void updateCandidateSet(String candiJson) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(candiJson);
                    if (this.mRemote.transact(Stub.TRANSACTION_updateCandidateSet, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateCandidateSet(candiJson);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public void updateCloudRules(String cloudRulesJson) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cloudRulesJson);
                    if (this.mRemote.transact(Stub.TRANSACTION_updateCloudRules, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateCloudRules(cloudRulesJson);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public void setCandidateCount(int candidateCount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(candidateCount);
                    if (this.mRemote.transact(Stub.TRANSACTION_setCandidateCount, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCandidateCount(candidateCount);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public HwRecResult getCandisetId(String pkg, String service) throws RemoteException {
                HwRecResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(service);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCandisetId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCandisetId(pkg, service);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwRecResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public String doNotificationCollectTest(String text, boolean isOngoing) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(text);
                    _data.writeInt(isOngoing ? Stub.TRANSACTION_getRecommendation : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_doNotificationCollectTest, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().doNotificationCollectTest(text, isOngoing);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public boolean isAllowABTest(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isAllowABTest, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAllowABTest(key);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public int getOutputNum(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getOutputNum, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOutputNum(key);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public boolean isInitConfigFile(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isInitConfigFile, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInitConfigFile(key);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.HwRecSysAidlInterface
            public void handleEvent(int action, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    if (bundle != null) {
                        _data.writeInt(Stub.TRANSACTION_getRecommendation);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(Stub.TRANSACTION_handleEvent, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().handleEvent(action, bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(HwRecSysAidlInterface impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static HwRecSysAidlInterface getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
