package com.android.ims;

import android.content.Context;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.aidl.IImsConfig;
import android.telephony.ims.aidl.IImsMmTelFeature;
import android.telephony.ims.aidl.IImsRegistration;
import android.telephony.ims.aidl.IImsRegistrationCallback;
import android.telephony.ims.aidl.IImsSmsListener;
import android.telephony.ims.feature.CapabilityChangeRequest;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import android.util.Log;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsServiceFeatureCallback;
import com.android.ims.internal.IImsUt;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MmTelFeatureConnection {
    protected static final String TAG = "MmTelFeatureConnection";
    protected IBinder mBinder;
    private final CapabilityCallbackManager mCapabilityCallbackManager = new CapabilityCallbackManager();
    private IImsConfig mConfigBinder;
    private Context mContext;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        public final void binderDied() {
            MmTelFeatureConnection.lambda$new$0(MmTelFeatureConnection.this);
        }
    };
    /* access modifiers changed from: private */
    public Integer mFeatureStateCached = null;
    /* access modifiers changed from: private */
    public volatile boolean mIsAvailable = false;
    private final IImsServiceFeatureCallback mListenerBinder = new IImsServiceFeatureCallback.Stub() {
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0054, code lost:
            return;
         */
        public void imsFeatureCreated(int slotId, int feature) throws RemoteException {
            synchronized (MmTelFeatureConnection.this.mLock) {
                if (MmTelFeatureConnection.this.mSlotId == slotId) {
                    switch (feature) {
                        case 0:
                            boolean unused = MmTelFeatureConnection.this.mSupportsEmergencyCalling = true;
                            Log.i(MmTelFeatureConnection.TAG, "Emergency calling enabled on slotId: " + slotId);
                            break;
                        case 1:
                            if (!MmTelFeatureConnection.this.mIsAvailable) {
                                Log.i(MmTelFeatureConnection.TAG, "MmTel enabled on slotId: " + slotId);
                                boolean unused2 = MmTelFeatureConnection.this.mIsAvailable = true;
                                break;
                            }
                            break;
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x004c, code lost:
            return;
         */
        public void imsFeatureRemoved(int slotId, int feature) throws RemoteException {
            synchronized (MmTelFeatureConnection.this.mLock) {
                if (MmTelFeatureConnection.this.mSlotId == slotId) {
                    switch (feature) {
                        case 0:
                            boolean unused = MmTelFeatureConnection.this.mSupportsEmergencyCalling = false;
                            Log.i(MmTelFeatureConnection.TAG, "Emergency calling disabled on slotId: " + slotId);
                            break;
                        case 1:
                            Log.i(MmTelFeatureConnection.TAG, "MmTel removed on slotId: " + slotId);
                            MmTelFeatureConnection.this.onRemovedOrDied();
                            break;
                    }
                }
            }
        }

        public void imsStatusChanged(int slotId, int feature, int status) throws RemoteException {
            synchronized (MmTelFeatureConnection.this.mLock) {
                Log.i(MmTelFeatureConnection.TAG, "imsStatusChanged: slot: " + slotId + " feature: " + feature + " status: " + status);
                if (MmTelFeatureConnection.this.mSlotId == slotId && feature == 1) {
                    Integer unused = MmTelFeatureConnection.this.mFeatureStateCached = Integer.valueOf(status);
                    if (MmTelFeatureConnection.this.mStatusCallback != null) {
                        MmTelFeatureConnection.this.mStatusCallback.notifyStateChanged();
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private IImsRegistration mRegistrationBinder;
    private ImsRegistrationCallbackAdapter mRegistrationCallbackManager = new ImsRegistrationCallbackAdapter();
    protected final int mSlotId;
    /* access modifiers changed from: private */
    public IFeatureUpdate mStatusCallback;
    /* access modifiers changed from: private */
    public boolean mSupportsEmergencyCalling = false;

    private abstract class CallbackAdapterManager<T> {
        private static final String TAG = "CallbackAdapterManager";
        private boolean mHasConnected;
        protected final Set<T> mLocalCallbacks;

        /* access modifiers changed from: package-private */
        public abstract boolean createConnection() throws RemoteException;

        /* access modifiers changed from: package-private */
        public abstract void removeConnection();

        private CallbackAdapterManager() {
            this.mLocalCallbacks = Collections.newSetFromMap(new ConcurrentHashMap());
            this.mHasConnected = false;
        }

        public void addCallback(T localCallback) throws RemoteException {
            synchronized (MmTelFeatureConnection.this.mLock) {
                if (!this.mHasConnected) {
                    if (createConnection()) {
                        this.mHasConnected = true;
                    } else {
                        throw new RemoteException("Can not create connection!");
                    }
                }
            }
            Log.i(TAG, "Local callback added: " + localCallback);
            this.mLocalCallbacks.add(localCallback);
        }

        public void removeCallback(T localCallback) {
            Log.i(TAG, "Local callback removed: " + localCallback);
            this.mLocalCallbacks.remove(localCallback);
            synchronized (MmTelFeatureConnection.this.mLock) {
                if (this.mHasConnected && this.mLocalCallbacks.isEmpty()) {
                    removeConnection();
                    this.mHasConnected = false;
                }
            }
        }

        public void close() {
            synchronized (MmTelFeatureConnection.this.mLock) {
                if (this.mHasConnected) {
                    removeConnection();
                    this.mHasConnected = false;
                }
            }
            Log.i(TAG, "Closing connection and clearing callbacks");
            this.mLocalCallbacks.clear();
        }
    }

    private class CapabilityCallbackManager extends CallbackAdapterManager<ImsFeature.CapabilityCallback> {
        private final CapabilityCallbackAdapter mCallbackAdapter;

        private class CapabilityCallbackAdapter extends ImsFeature.CapabilityCallback {
            private CapabilityCallbackAdapter() {
            }

            public void onCapabilitiesStatusChanged(ImsFeature.Capabilities config) {
                CapabilityCallbackManager.this.mLocalCallbacks.forEach(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0009: INVOKE  (wrap: java.util.Set
                      0x0002: IGET  (r0v1 java.util.Set) = (wrap: com.android.ims.MmTelFeatureConnection$CapabilityCallbackManager
                      0x0000: IGET  (r0v0 com.android.ims.MmTelFeatureConnection$CapabilityCallbackManager) = (r2v0 'this' com.android.ims.MmTelFeatureConnection$CapabilityCallbackManager$CapabilityCallbackAdapter A[THIS]) com.android.ims.MmTelFeatureConnection.CapabilityCallbackManager.CapabilityCallbackAdapter.this$1 com.android.ims.MmTelFeatureConnection$CapabilityCallbackManager) com.android.ims.MmTelFeatureConnection.CapabilityCallbackManager.mLocalCallbacks java.util.Set), (wrap: com.android.ims.-$$Lambda$MmTelFeatureConnection$CapabilityCallbackManager$CapabilityCallbackAdapter$Fu_TJxPrz_icRRAcE-hESmVfVRI
                      0x0006: CONSTRUCTOR  (r1v0 com.android.ims.-$$Lambda$MmTelFeatureConnection$CapabilityCallbackManager$CapabilityCallbackAdapter$Fu_TJxPrz_icRRAcE-hESmVfVRI) = (r3v0 'config' android.telephony.ims.feature.ImsFeature$Capabilities) com.android.ims.-$$Lambda$MmTelFeatureConnection$CapabilityCallbackManager$CapabilityCallbackAdapter$Fu_TJxPrz_icRRAcE-hESmVfVRI.<init>(android.telephony.ims.feature.ImsFeature$Capabilities):void CONSTRUCTOR) java.util.Set.forEach(java.util.function.Consumer):void type: INTERFACE in method: com.android.ims.MmTelFeatureConnection.CapabilityCallbackManager.CapabilityCallbackAdapter.onCapabilitiesStatusChanged(android.telephony.ims.feature.ImsFeature$Capabilities):void, dex: boot-ims-common_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0006: CONSTRUCTOR  (r1v0 com.android.ims.-$$Lambda$MmTelFeatureConnection$CapabilityCallbackManager$CapabilityCallbackAdapter$Fu_TJxPrz_icRRAcE-hESmVfVRI) = (r3v0 'config' android.telephony.ims.feature.ImsFeature$Capabilities) com.android.ims.-$$Lambda$MmTelFeatureConnection$CapabilityCallbackManager$CapabilityCallbackAdapter$Fu_TJxPrz_icRRAcE-hESmVfVRI.<init>(android.telephony.ims.feature.ImsFeature$Capabilities):void CONSTRUCTOR in method: com.android.ims.MmTelFeatureConnection.CapabilityCallbackManager.CapabilityCallbackAdapter.onCapabilitiesStatusChanged(android.telephony.ims.feature.ImsFeature$Capabilities):void, dex: boot-ims-common_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 23 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.ims.-$$Lambda$MmTelFeatureConnection$CapabilityCallbackManager$CapabilityCallbackAdapter$Fu_TJxPrz_icRRAcE-hESmVfVRI, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 28 more
                    */
                /*
                    this = this;
                    com.android.ims.MmTelFeatureConnection$CapabilityCallbackManager r0 = com.android.ims.MmTelFeatureConnection.CapabilityCallbackManager.this
                    java.util.Set r0 = r0.mLocalCallbacks
                    com.android.ims.-$$Lambda$MmTelFeatureConnection$CapabilityCallbackManager$CapabilityCallbackAdapter$Fu_TJxPrz_icRRAcE-hESmVfVRI r1 = new com.android.ims.-$$Lambda$MmTelFeatureConnection$CapabilityCallbackManager$CapabilityCallbackAdapter$Fu_TJxPrz_icRRAcE-hESmVfVRI
                    r1.<init>(r3)
                    r0.forEach(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.ims.MmTelFeatureConnection.CapabilityCallbackManager.CapabilityCallbackAdapter.onCapabilitiesStatusChanged(android.telephony.ims.feature.ImsFeature$Capabilities):void");
            }
        }

        private CapabilityCallbackManager() {
            super();
            this.mCallbackAdapter = new CapabilityCallbackAdapter();
        }

        /* access modifiers changed from: package-private */
        public boolean createConnection() throws RemoteException {
            IImsMmTelFeature binder;
            synchronized (MmTelFeatureConnection.this.mLock) {
                MmTelFeatureConnection.this.checkServiceIsReady();
                binder = MmTelFeatureConnection.this.getServiceInterface(MmTelFeatureConnection.this.mBinder);
            }
            if (binder != null) {
                binder.addCapabilityCallback(this.mCallbackAdapter);
                return true;
            }
            Log.w(MmTelFeatureConnection.TAG, "create: Couldn't get IImsMmTelFeature binder");
            return false;
        }

        /* access modifiers changed from: package-private */
        public void removeConnection() {
            IImsMmTelFeature binder = null;
            synchronized (MmTelFeatureConnection.this.mLock) {
                try {
                    MmTelFeatureConnection.this.checkServiceIsReady();
                    binder = MmTelFeatureConnection.this.getServiceInterface(MmTelFeatureConnection.this.mBinder);
                } catch (RemoteException e) {
                }
            }
            if (binder != null) {
                try {
                    binder.removeCapabilityCallback(this.mCallbackAdapter);
                } catch (RemoteException e2) {
                    Log.w(MmTelFeatureConnection.TAG, "remove: IImsMmTelFeature binder is dead");
                }
            } else {
                Log.w(MmTelFeatureConnection.TAG, "remove: Couldn't get IImsMmTelFeature binder");
            }
        }
    }

    public interface IFeatureUpdate {
        void notifyStateChanged();

        void notifyUnavailable();
    }

    private class ImsRegistrationCallbackAdapter extends CallbackAdapterManager<ImsRegistrationImplBase.Callback> {
        private final RegistrationCallbackAdapter mRegistrationCallbackAdapter;

        private class RegistrationCallbackAdapter extends IImsRegistrationCallback.Stub {
            private RegistrationCallbackAdapter() {
            }

            public void onRegistered(int imsRadioTech) {
                Log.i(MmTelFeatureConnection.TAG, "onRegistered ::");
                ImsRegistrationCallbackAdapter.this.mLocalCallbacks.forEach(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0010: INVOKE  (wrap: java.util.Set
                      0x0009: IGET  (r0v2 java.util.Set) = (wrap: com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter
                      0x0007: IGET  (r0v1 com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter) = (r2v0 'this' com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter A[THIS]) com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.this$1 com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter) com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.mLocalCallbacks java.util.Set), (wrap: com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$K3hccJ541Q6pLDm26Z8TPlTWIJY
                      0x000d: CONSTRUCTOR  (r1v1 com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$K3hccJ541Q6pLDm26Z8TPlTWIJY) = (r3v0 'imsRadioTech' int) com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$K3hccJ541Q6pLDm26Z8TPlTWIJY.<init>(int):void CONSTRUCTOR) java.util.Set.forEach(java.util.function.Consumer):void type: INTERFACE in method: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onRegistered(int):void, dex: boot-ims-common_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000d: CONSTRUCTOR  (r1v1 com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$K3hccJ541Q6pLDm26Z8TPlTWIJY) = (r3v0 'imsRadioTech' int) com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$K3hccJ541Q6pLDm26Z8TPlTWIJY.<init>(int):void CONSTRUCTOR in method: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onRegistered(int):void, dex: boot-ims-common_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 23 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$K3hccJ541Q6pLDm26Z8TPlTWIJY, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 28 more
                    */
                /*
                    this = this;
                    java.lang.String r0 = "MmTelFeatureConnection"
                    java.lang.String r1 = "onRegistered ::"
                    android.util.Log.i(r0, r1)
                    com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter r0 = com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.this
                    java.util.Set r0 = r0.mLocalCallbacks
                    com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$K3hccJ541Q6pLDm26Z8TPlTWIJY r1 = new com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$K3hccJ541Q6pLDm26Z8TPlTWIJY
                    r1.<init>(r3)
                    r0.forEach(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onRegistered(int):void");
            }

            public void onRegistering(int imsRadioTech) {
                Log.i(MmTelFeatureConnection.TAG, "onRegistering ::");
                ImsRegistrationCallbackAdapter.this.mLocalCallbacks.forEach(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0010: INVOKE  (wrap: java.util.Set
                      0x0009: IGET  (r0v2 java.util.Set) = (wrap: com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter
                      0x0007: IGET  (r0v1 com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter) = (r2v0 'this' com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter A[THIS]) com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.this$1 com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter) com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.mLocalCallbacks java.util.Set), (wrap: com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$u4ZBOw30LePcwafim6pu64v4hNM
                      0x000d: CONSTRUCTOR  (r1v1 com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$u4ZBOw30LePcwafim6pu64v4hNM) = (r3v0 'imsRadioTech' int) com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$u4ZBOw30LePcwafim6pu64v4hNM.<init>(int):void CONSTRUCTOR) java.util.Set.forEach(java.util.function.Consumer):void type: INTERFACE in method: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onRegistering(int):void, dex: boot-ims-common_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000d: CONSTRUCTOR  (r1v1 com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$u4ZBOw30LePcwafim6pu64v4hNM) = (r3v0 'imsRadioTech' int) com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$u4ZBOw30LePcwafim6pu64v4hNM.<init>(int):void CONSTRUCTOR in method: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onRegistering(int):void, dex: boot-ims-common_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 23 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$u4ZBOw30LePcwafim6pu64v4hNM, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 28 more
                    */
                /*
                    this = this;
                    java.lang.String r0 = "MmTelFeatureConnection"
                    java.lang.String r1 = "onRegistering ::"
                    android.util.Log.i(r0, r1)
                    com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter r0 = com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.this
                    java.util.Set r0 = r0.mLocalCallbacks
                    com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$u4ZBOw30LePcwafim6pu64v4hNM r1 = new com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$u4ZBOw30LePcwafim6pu64v4hNM
                    r1.<init>(r3)
                    r0.forEach(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onRegistering(int):void");
            }

            public void onDeregistered(ImsReasonInfo imsReasonInfo) {
                Log.i(MmTelFeatureConnection.TAG, "onDeregistered ::");
                ImsRegistrationCallbackAdapter.this.mLocalCallbacks.forEach(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0010: INVOKE  (wrap: java.util.Set
                      0x0009: IGET  (r0v2 java.util.Set) = (wrap: com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter
                      0x0007: IGET  (r0v1 com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter) = (r2v0 'this' com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter A[THIS]) com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.this$1 com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter) com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.mLocalCallbacks java.util.Set), (wrap: com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$vxFS2t25rwEiTAgHUI462y3Hz90
                      0x000d: CONSTRUCTOR  (r1v1 com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$vxFS2t25rwEiTAgHUI462y3Hz90) = (r3v0 'imsReasonInfo' android.telephony.ims.ImsReasonInfo) com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$vxFS2t25rwEiTAgHUI462y3Hz90.<init>(android.telephony.ims.ImsReasonInfo):void CONSTRUCTOR) java.util.Set.forEach(java.util.function.Consumer):void type: INTERFACE in method: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onDeregistered(android.telephony.ims.ImsReasonInfo):void, dex: boot-ims-common_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000d: CONSTRUCTOR  (r1v1 com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$vxFS2t25rwEiTAgHUI462y3Hz90) = (r3v0 'imsReasonInfo' android.telephony.ims.ImsReasonInfo) com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$vxFS2t25rwEiTAgHUI462y3Hz90.<init>(android.telephony.ims.ImsReasonInfo):void CONSTRUCTOR in method: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onDeregistered(android.telephony.ims.ImsReasonInfo):void, dex: boot-ims-common_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 23 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$vxFS2t25rwEiTAgHUI462y3Hz90, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 28 more
                    */
                /*
                    this = this;
                    java.lang.String r0 = "MmTelFeatureConnection"
                    java.lang.String r1 = "onDeregistered ::"
                    android.util.Log.i(r0, r1)
                    com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter r0 = com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.this
                    java.util.Set r0 = r0.mLocalCallbacks
                    com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$vxFS2t25rwEiTAgHUI462y3Hz90 r1 = new com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$vxFS2t25rwEiTAgHUI462y3Hz90
                    r1.<init>(r3)
                    r0.forEach(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onDeregistered(android.telephony.ims.ImsReasonInfo):void");
            }

            public void onTechnologyChangeFailed(int targetRadioTech, ImsReasonInfo imsReasonInfo) {
                Log.i(MmTelFeatureConnection.TAG, "onTechnologyChangeFailed :: targetAccessTech=" + targetRadioTech + ", imsReasonInfo=" + imsReasonInfo);
                ImsRegistrationCallbackAdapter.this.mLocalCallbacks.forEach(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0027: INVOKE  (wrap: java.util.Set
                      0x0020: IGET  (r0v2 java.util.Set) = (wrap: com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter
                      0x001e: IGET  (r0v1 com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter) = (r3v0 'this' com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter A[THIS]) com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.this$1 com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter) com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.mLocalCallbacks java.util.Set), (wrap: com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$MXrzNMmn7kmMT_nTAM0W7J2nTFU
                      0x0024: CONSTRUCTOR  (r1v2 com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$MXrzNMmn7kmMT_nTAM0W7J2nTFU) = (r4v0 'targetRadioTech' int), (r5v0 'imsReasonInfo' android.telephony.ims.ImsReasonInfo) com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$MXrzNMmn7kmMT_nTAM0W7J2nTFU.<init>(int, android.telephony.ims.ImsReasonInfo):void CONSTRUCTOR) java.util.Set.forEach(java.util.function.Consumer):void type: INTERFACE in method: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onTechnologyChangeFailed(int, android.telephony.ims.ImsReasonInfo):void, dex: boot-ims-common_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0024: CONSTRUCTOR  (r1v2 com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$MXrzNMmn7kmMT_nTAM0W7J2nTFU) = (r4v0 'targetRadioTech' int), (r5v0 'imsReasonInfo' android.telephony.ims.ImsReasonInfo) com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$MXrzNMmn7kmMT_nTAM0W7J2nTFU.<init>(int, android.telephony.ims.ImsReasonInfo):void CONSTRUCTOR in method: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onTechnologyChangeFailed(int, android.telephony.ims.ImsReasonInfo):void, dex: boot-ims-common_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 23 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$MXrzNMmn7kmMT_nTAM0W7J2nTFU, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 28 more
                    */
                /*
                    this = this;
                    java.lang.String r0 = "MmTelFeatureConnection"
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder
                    r1.<init>()
                    java.lang.String r2 = "onTechnologyChangeFailed :: targetAccessTech="
                    r1.append(r2)
                    r1.append(r4)
                    java.lang.String r2 = ", imsReasonInfo="
                    r1.append(r2)
                    r1.append(r5)
                    java.lang.String r1 = r1.toString()
                    android.util.Log.i(r0, r1)
                    com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter r0 = com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.this
                    java.util.Set r0 = r0.mLocalCallbacks
                    com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$MXrzNMmn7kmMT_nTAM0W7J2nTFU r1 = new com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$MXrzNMmn7kmMT_nTAM0W7J2nTFU
                    r1.<init>(r4, r5)
                    r0.forEach(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onTechnologyChangeFailed(int, android.telephony.ims.ImsReasonInfo):void");
            }

            public void onSubscriberAssociatedUriChanged(Uri[] uris) {
                Log.i(MmTelFeatureConnection.TAG, "onSubscriberAssociatedUriChanged");
                ImsRegistrationCallbackAdapter.this.mLocalCallbacks.forEach(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0010: INVOKE  (wrap: java.util.Set
                      0x0009: IGET  (r0v2 java.util.Set) = (wrap: com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter
                      0x0007: IGET  (r0v1 com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter) = (r2v0 'this' com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter A[THIS]) com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.this$1 com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter) com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.mLocalCallbacks java.util.Set), (wrap: com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$0vZ6D8L8NEmVenYChls3pkTpxsQ
                      0x000d: CONSTRUCTOR  (r1v1 com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$0vZ6D8L8NEmVenYChls3pkTpxsQ) = (r3v0 'uris' android.net.Uri[]) com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$0vZ6D8L8NEmVenYChls3pkTpxsQ.<init>(android.net.Uri[]):void CONSTRUCTOR) java.util.Set.forEach(java.util.function.Consumer):void type: INTERFACE in method: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onSubscriberAssociatedUriChanged(android.net.Uri[]):void, dex: boot-ims-common_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000d: CONSTRUCTOR  (r1v1 com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$0vZ6D8L8NEmVenYChls3pkTpxsQ) = (r3v0 'uris' android.net.Uri[]) com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$0vZ6D8L8NEmVenYChls3pkTpxsQ.<init>(android.net.Uri[]):void CONSTRUCTOR in method: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onSubscriberAssociatedUriChanged(android.net.Uri[]):void, dex: boot-ims-common_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 23 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$0vZ6D8L8NEmVenYChls3pkTpxsQ, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 28 more
                    */
                /*
                    this = this;
                    java.lang.String r0 = "MmTelFeatureConnection"
                    java.lang.String r1 = "onSubscriberAssociatedUriChanged"
                    android.util.Log.i(r0, r1)
                    com.android.ims.MmTelFeatureConnection$ImsRegistrationCallbackAdapter r0 = com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.this
                    java.util.Set r0 = r0.mLocalCallbacks
                    com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$0vZ6D8L8NEmVenYChls3pkTpxsQ r1 = new com.android.ims.-$$Lambda$MmTelFeatureConnection$ImsRegistrationCallbackAdapter$RegistrationCallbackAdapter$0vZ6D8L8NEmVenYChls3pkTpxsQ
                    r1.<init>(r3)
                    r0.forEach(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.ims.MmTelFeatureConnection.ImsRegistrationCallbackAdapter.RegistrationCallbackAdapter.onSubscriberAssociatedUriChanged(android.net.Uri[]):void");
            }
        }

        private ImsRegistrationCallbackAdapter() {
            super();
            this.mRegistrationCallbackAdapter = new RegistrationCallbackAdapter();
        }

        /* access modifiers changed from: package-private */
        public boolean createConnection() throws RemoteException {
            if (MmTelFeatureConnection.this.getRegistration() != null) {
                MmTelFeatureConnection.this.getRegistration().addRegistrationCallback(this.mRegistrationCallbackAdapter);
                return true;
            }
            Log.e(MmTelFeatureConnection.TAG, "ImsRegistration is null");
            return false;
        }

        /* access modifiers changed from: package-private */
        public void removeConnection() {
            if (MmTelFeatureConnection.this.getRegistration() != null) {
                try {
                    MmTelFeatureConnection.this.getRegistration().removeRegistrationCallback(this.mRegistrationCallbackAdapter);
                } catch (RemoteException e) {
                    Log.w(MmTelFeatureConnection.TAG, "removeConnection: couldn't remove registration callback");
                }
            } else {
                Log.e(MmTelFeatureConnection.TAG, "ImsRegistration is null");
            }
        }
    }

    public static /* synthetic */ void lambda$new$0(MmTelFeatureConnection mmTelFeatureConnection) {
        Log.w(TAG, "DeathRecipient triggered, binder died.");
        mmTelFeatureConnection.onRemovedOrDied();
    }

    public static MmTelFeatureConnection create(Context context, int slotId) {
        MmTelFeatureConnection serviceProxy = new MmTelFeatureConnection(context, slotId);
        TelephonyManager tm = getTelephonyManager(context);
        if (tm == null) {
            Rlog.w(TAG, "create: TelephonyManager is null!");
            return serviceProxy;
        }
        IImsMmTelFeature binder = tm.getImsMmTelFeatureAndListen(slotId, serviceProxy.getListener());
        if (binder != null) {
            serviceProxy.setBinder(binder.asBinder());
            serviceProxy.getFeatureState();
        } else {
            Rlog.w(TAG, "create: binder is null! Slot Id: " + slotId);
        }
        return serviceProxy;
    }

    public static TelephonyManager getTelephonyManager(Context context) {
        return (TelephonyManager) context.getSystemService("phone");
    }

    public MmTelFeatureConnection(Context context, int slotId) {
        this.mSlotId = slotId;
        this.mContext = context;
    }

    /* access modifiers changed from: private */
    public void onRemovedOrDied() {
        synchronized (this.mLock) {
            if (this.mIsAvailable) {
                this.mIsAvailable = false;
                this.mRegistrationBinder = null;
                this.mConfigBinder = null;
                if (this.mBinder != null) {
                    this.mBinder.unlinkToDeath(this.mDeathRecipient, 0);
                }
                if (this.mStatusCallback != null) {
                    this.mStatusCallback.notifyUnavailable();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0012, code lost:
        if (r1 == null) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0014, code lost:
        r0 = r1.getImsRegistration(r4.mSlotId, 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        r2 = r0;
        r3 = r4.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0020, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0023, code lost:
        if (r4.mRegistrationBinder != null) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0025, code lost:
        r4.mRegistrationBinder = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0027, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002a, code lost:
        return r4.mRegistrationBinder;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000c, code lost:
        r1 = getTelephonyManager(r4.mContext);
     */
    public IImsRegistration getRegistration() {
        synchronized (this.mLock) {
            if (this.mRegistrationBinder != null) {
                IImsRegistration iImsRegistration = this.mRegistrationBinder;
                return iImsRegistration;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0012, code lost:
        if (r1 == null) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0014, code lost:
        r0 = r1.getImsConfig(r4.mSlotId, 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        r2 = r0;
        r3 = r4.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0020, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0023, code lost:
        if (r4.mConfigBinder != null) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0025, code lost:
        r4.mConfigBinder = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0027, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002a, code lost:
        return r4.mConfigBinder;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000c, code lost:
        r1 = getTelephonyManager(r4.mContext);
     */
    private IImsConfig getConfig() {
        synchronized (this.mLock) {
            if (this.mConfigBinder != null) {
                IImsConfig iImsConfig = this.mConfigBinder;
                return iImsConfig;
            }
        }
    }

    public boolean isEmergencyMmTelAvailable() {
        return this.mSupportsEmergencyCalling;
    }

    public IImsServiceFeatureCallback getListener() {
        return this.mListenerBinder;
    }

    public void setBinder(IBinder binder) {
        synchronized (this.mLock) {
            this.mBinder = binder;
            try {
                if (this.mBinder != null) {
                    this.mBinder.linkToDeath(this.mDeathRecipient, 0);
                }
            } catch (RemoteException e) {
            }
        }
    }

    public void openConnection(MmTelFeature.Listener listener) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).setListener(listener);
        }
    }

    public void closeConnection() {
        this.mRegistrationCallbackManager.close();
        this.mCapabilityCallbackManager.close();
        try {
            synchronized (this.mLock) {
                if (isBinderAlive()) {
                    getServiceInterface(this.mBinder).setListener(null);
                }
            }
        } catch (RemoteException e) {
            Log.w(TAG, "closeConnection: couldn't remove listener!");
        }
    }

    public void addRegistrationCallback(ImsRegistrationImplBase.Callback callback) throws RemoteException {
        this.mRegistrationCallbackManager.addCallback(callback);
    }

    public void removeRegistrationCallback(ImsRegistrationImplBase.Callback callback) throws RemoteException {
        this.mRegistrationCallbackManager.removeCallback(callback);
    }

    public void addCapabilityCallback(ImsFeature.CapabilityCallback callback) throws RemoteException {
        this.mCapabilityCallbackManager.addCallback(callback);
    }

    public void removeCapabilityCallback(ImsFeature.CapabilityCallback callback) throws RemoteException {
        this.mCapabilityCallbackManager.removeCallback(callback);
    }

    public void changeEnabledCapabilities(CapabilityChangeRequest request, ImsFeature.CapabilityCallback callback) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).changeCapabilitiesConfiguration(request, callback);
        }
    }

    public void queryEnabledCapabilities(int capability, int radioTech, ImsFeature.CapabilityCallback callback) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).queryCapabilityConfiguration(capability, radioTech, callback);
        }
    }

    public MmTelFeature.MmTelCapabilities queryCapabilityStatus() throws RemoteException {
        MmTelFeature.MmTelCapabilities mmTelCapabilities;
        synchronized (this.mLock) {
            checkServiceIsReady();
            mmTelCapabilities = new MmTelFeature.MmTelCapabilities(getServiceInterface(this.mBinder).queryCapabilityStatus());
        }
        return mmTelCapabilities;
    }

    public ImsCallProfile createCallProfile(int callServiceType, int callType) throws RemoteException {
        ImsCallProfile createCallProfile;
        synchronized (this.mLock) {
            checkServiceIsReady();
            createCallProfile = getServiceInterface(this.mBinder).createCallProfile(callServiceType, callType);
        }
        return createCallProfile;
    }

    public IImsCallSession createCallSession(ImsCallProfile profile) throws RemoteException {
        IImsCallSession createCallSession;
        synchronized (this.mLock) {
            checkServiceIsReady();
            createCallSession = getServiceInterface(this.mBinder).createCallSession(profile);
        }
        return createCallSession;
    }

    public IImsUt getUtInterface() throws RemoteException {
        IImsUt utInterface;
        synchronized (this.mLock) {
            checkServiceIsReady();
            utInterface = getServiceInterface(this.mBinder).getUtInterface();
        }
        return utInterface;
    }

    public IImsConfig getConfigInterface() throws RemoteException {
        return getConfig();
    }

    public int getRegistrationTech() throws RemoteException {
        IImsRegistration registration = getRegistration();
        if (registration != null) {
            return registration.getRegistrationTechnology();
        }
        return -1;
    }

    public IImsEcbm getEcbmInterface() throws RemoteException {
        IImsEcbm ecbmInterface;
        synchronized (this.mLock) {
            checkServiceIsReady();
            ecbmInterface = getServiceInterface(this.mBinder).getEcbmInterface();
        }
        return ecbmInterface;
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).setUiTtyMode(uiTtyMode, onComplete);
        }
    }

    public IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException {
        IImsMultiEndpoint multiEndpointInterface;
        synchronized (this.mLock) {
            checkServiceIsReady();
            multiEndpointInterface = getServiceInterface(this.mBinder).getMultiEndpointInterface();
        }
        return multiEndpointInterface;
    }

    public void sendSms(int token, int messageRef, String format, String smsc, boolean isRetry, byte[] pdu) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).sendSms(token, messageRef, format, smsc, isRetry, pdu);
        }
    }

    public void acknowledgeSms(int token, int messageRef, int result) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).acknowledgeSms(token, messageRef, result);
        }
    }

    public void acknowledgeSmsReport(int token, int messageRef, int result) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).acknowledgeSmsReport(token, messageRef, result);
        }
    }

    public String getSmsFormat() throws RemoteException {
        String smsFormat;
        synchronized (this.mLock) {
            checkServiceIsReady();
            smsFormat = getServiceInterface(this.mBinder).getSmsFormat();
        }
        return smsFormat;
    }

    public void onSmsReady() throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).onSmsReady();
        }
    }

    public void setSmsListener(IImsSmsListener listener) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).setSmsListener(listener);
        }
    }

    public int shouldProcessCall(boolean isEmergency, String[] numbers) throws RemoteException {
        int shouldProcessCall;
        if (!isEmergency || isEmergencyMmTelAvailable()) {
            synchronized (this.mLock) {
                checkServiceIsReady();
                shouldProcessCall = getServiceInterface(this.mBinder).shouldProcessCall(numbers);
            }
            return shouldProcessCall;
        }
        Log.i(TAG, "MmTel does not support emergency over IMS, fallback to CS.");
        return 1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0016, code lost:
        r1 = retrieveFeatureState();
        r2 = r4.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        if (r1 != null) goto L_0x0024;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0021, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0022, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0024, code lost:
        r4.mFeatureStateCached = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0026, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0027, code lost:
        android.util.Log.i(TAG, "getFeatureState - returning " + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0041, code lost:
        return r1.intValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0043, code lost:
        throw r0;
     */
    public int getFeatureState() {
        synchronized (this.mLock) {
            if (isBinderAlive() && this.mFeatureStateCached != null) {
                int intValue = this.mFeatureStateCached.intValue();
                return intValue;
            }
        }
    }

    private Integer retrieveFeatureState() {
        if (this.mBinder != null) {
            try {
                return Integer.valueOf(getServiceInterface(this.mBinder).getFeatureState());
            } catch (RemoteException e) {
            }
        }
        return null;
    }

    public void setStatusCallback(IFeatureUpdate c) {
        this.mStatusCallback = c;
    }

    public boolean isBinderReady() {
        return isBinderAlive() && getFeatureState() == 2;
    }

    public boolean isBinderAlive() {
        return this.mIsAvailable && this.mBinder != null && this.mBinder.isBinderAlive();
    }

    /* access modifiers changed from: protected */
    public void checkServiceIsReady() throws RemoteException {
        if (!isBinderReady()) {
            throw new RemoteException("ImsServiceProxy is not ready to accept commands.");
        }
    }

    /* access modifiers changed from: private */
    public IImsMmTelFeature getServiceInterface(IBinder b) {
        return IImsMmTelFeature.Stub.asInterface(b);
    }

    /* access modifiers changed from: protected */
    public void checkBinderConnection() throws RemoteException {
        if (!isBinderAlive()) {
            throw new RemoteException("ImsServiceProxy is not available for that feature.");
        }
    }
}
