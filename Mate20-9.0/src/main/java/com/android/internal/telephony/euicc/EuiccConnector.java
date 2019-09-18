package com.android.internal.telephony.euicc;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.service.euicc.GetDefaultDownloadableSubscriptionListResult;
import android.service.euicc.GetDownloadableSubscriptionMetadataResult;
import android.service.euicc.GetEuiccProfileInfoListResult;
import android.service.euicc.IDeleteSubscriptionCallback;
import android.service.euicc.IDownloadSubscriptionCallback;
import android.service.euicc.IEraseSubscriptionsCallback;
import android.service.euicc.IEuiccService;
import android.service.euicc.IGetDefaultDownloadableSubscriptionListCallback;
import android.service.euicc.IGetDownloadableSubscriptionMetadataCallback;
import android.service.euicc.IGetEidCallback;
import android.service.euicc.IGetEuiccInfoCallback;
import android.service.euicc.IGetEuiccProfileInfoListCallback;
import android.service.euicc.IGetOtaStatusCallback;
import android.service.euicc.IOtaStatusChangedCallback;
import android.service.euicc.IRetainSubscriptionsForFactoryResetCallback;
import android.service.euicc.ISwitchToSubscriptionCallback;
import android.service.euicc.IUpdateSubscriptionNicknameCallback;
import android.telephony.euicc.DownloadableSubscription;
import android.telephony.euicc.EuiccInfo;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EuiccConnector extends StateMachine implements ServiceConnection {
    private static final int BIND_TIMEOUT_MILLIS = 30000;
    private static final int CMD_COMMAND_COMPLETE = 6;
    private static final int CMD_CONNECT_TIMEOUT = 2;
    private static final int CMD_DELETE_SUBSCRIPTION = 106;
    private static final int CMD_DOWNLOAD_SUBSCRIPTION = 102;
    private static final int CMD_ERASE_SUBSCRIPTIONS = 109;
    private static final int CMD_GET_DEFAULT_DOWNLOADABLE_SUBSCRIPTION_LIST = 104;
    private static final int CMD_GET_DOWNLOADABLE_SUBSCRIPTION_METADATA = 101;
    private static final int CMD_GET_EID = 100;
    private static final int CMD_GET_EUICC_INFO = 105;
    private static final int CMD_GET_EUICC_PROFILE_INFO_LIST = 103;
    private static final int CMD_GET_OTA_STATUS = 111;
    private static final int CMD_LINGER_TIMEOUT = 3;
    private static final int CMD_PACKAGE_CHANGE = 1;
    private static final int CMD_RETAIN_SUBSCRIPTIONS = 110;
    private static final int CMD_SERVICE_CONNECTED = 4;
    private static final int CMD_SERVICE_DISCONNECTED = 5;
    private static final int CMD_START_OTA_IF_NECESSARY = 112;
    private static final int CMD_SWITCH_TO_SUBSCRIPTION = 107;
    private static final int CMD_UPDATE_SUBSCRIPTION_NICKNAME = 108;
    private static final int EUICC_QUERY_FLAGS = 269484096;
    @VisibleForTesting
    static final int LINGER_TIMEOUT_MILLIS = 60000;
    private static final String TAG = "EuiccConnector";
    /* access modifiers changed from: private */
    public Set<BaseEuiccCommandCallback> mActiveCommandCallbacks = new ArraySet();
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public AvailableState mAvailableState;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public BindingState mBindingState;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public ConnectedState mConnectedState;
    private Context mContext;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public DisconnectedState mDisconnectedState;
    /* access modifiers changed from: private */
    public IEuiccService mEuiccService;
    private final PackageMonitor mPackageMonitor = new EuiccPackageMonitor();
    private PackageManager mPm;
    /* access modifiers changed from: private */
    public ServiceInfo mSelectedComponent;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public UnavailableState mUnavailableState;
    private final BroadcastReceiver mUserUnlockedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                EuiccConnector.this.sendMessage(1);
            }
        }
    };

    private class AvailableState extends State {
        private AvailableState() {
        }

        public boolean processMessage(Message message) {
            if (!EuiccConnector.isEuiccCommand(message.what)) {
                return false;
            }
            EuiccConnector.this.deferMessage(message);
            EuiccConnector.this.transitionTo(EuiccConnector.this.mBindingState);
            return true;
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface BaseEuiccCommandCallback {
        void onEuiccServiceUnavailable();
    }

    private class BindingState extends State {
        private BindingState() {
        }

        public void enter() {
            if (EuiccConnector.this.createBinding()) {
                EuiccConnector.this.transitionTo(EuiccConnector.this.mDisconnectedState);
            } else {
                EuiccConnector.this.transitionTo(EuiccConnector.this.mAvailableState);
            }
        }

        public boolean processMessage(Message message) {
            EuiccConnector.this.deferMessage(message);
            return true;
        }
    }

    private class ConnectedState extends State {
        private ConnectedState() {
        }

        public void enter() {
            EuiccConnector.this.removeMessages(2);
            EuiccConnector.this.sendMessageDelayed(3, 60000);
        }

        public boolean processMessage(Message message) {
            if (message.what == 5) {
                IEuiccService unused = EuiccConnector.this.mEuiccService = null;
                EuiccConnector.this.transitionTo(EuiccConnector.this.mDisconnectedState);
                return true;
            } else if (message.what == 3) {
                EuiccConnector.this.unbind();
                EuiccConnector.this.transitionTo(EuiccConnector.this.mAvailableState);
                return true;
            } else if (message.what == 6) {
                ((Runnable) message.obj).run();
                return true;
            } else if (!EuiccConnector.isEuiccCommand(message.what)) {
                return false;
            } else {
                final BaseEuiccCommandCallback callback = EuiccConnector.getCallback(message);
                EuiccConnector.this.onCommandStart(callback);
                try {
                    switch (message.what) {
                        case 100:
                            EuiccConnector.this.mEuiccService.getEid(-1, new IGetEidCallback.Stub() {
                                public void onSuccess(String eid) {
                                    EuiccConnector.this.sendMessage(6, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                          0x0002: IGET  (r0v1 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                          0x0000: IGET  (r0v0 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$1 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.1.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$1$wTkmDdVlxcrtbVPcCl3t7xD490o
                                          0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$1$wTkmDdVlxcrtbVPcCl3t7xD490o) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$1 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$1 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.1.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'eid' java.lang.String) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$1$wTkmDdVlxcrtbVPcCl3t7xD490o.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$1, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, java.lang.String):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.1.onSuccess(java.lang.String):void, dex: boot-telephony-common_classes.dex
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
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$1$wTkmDdVlxcrtbVPcCl3t7xD490o) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$1 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$1 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.1.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'eid' java.lang.String) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$1$wTkmDdVlxcrtbVPcCl3t7xD490o.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$1, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, java.lang.String):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.1.onSuccess(java.lang.String):void, dex: boot-telephony-common_classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	... 60 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$1$wTkmDdVlxcrtbVPcCl3t7xD490o, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	... 65 more
                                        */
                                    /*
                                        this = this;
                                        com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r0 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                        com.android.internal.telephony.euicc.EuiccConnector r0 = com.android.internal.telephony.euicc.EuiccConnector.this
                                        com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r1 = r0
                                        com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$1$wTkmDdVlxcrtbVPcCl3t7xD490o r2 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$1$wTkmDdVlxcrtbVPcCl3t7xD490o
                                        r2.<init>(r3, r1, r4)
                                        r1 = 6
                                        r0.sendMessage(r1, r2)
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass1.onSuccess(java.lang.String):void");
                                }

                                public static /* synthetic */ void lambda$onSuccess$0(AnonymousClass1 r1, BaseEuiccCommandCallback callback, String eid) {
                                    ((GetEidCommandCallback) callback).onGetEidComplete(eid);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 101:
                            GetMetadataRequest request = (GetMetadataRequest) message.obj;
                            EuiccConnector.this.mEuiccService.getDownloadableSubscriptionMetadata(-1, request.mSubscription, request.mForceDeactivateSim, new IGetDownloadableSubscriptionMetadataCallback.Stub() {
                                public void onComplete(GetDownloadableSubscriptionMetadataResult result) {
                                    EuiccConnector.this.sendMessage(6, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                          0x0002: IGET  (r0v1 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                          0x0000: IGET  (r0v0 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$2 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.2.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$2$mYGM42yhe76zJekjTAzT10LdEMk
                                          0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$2$mYGM42yhe76zJekjTAzT10LdEMk) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$2 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$2 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.2.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' android.service.euicc.GetDownloadableSubscriptionMetadataResult) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$2$mYGM42yhe76zJekjTAzT10LdEMk.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$2, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, android.service.euicc.GetDownloadableSubscriptionMetadataResult):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.2.onComplete(android.service.euicc.GetDownloadableSubscriptionMetadataResult):void, dex: boot-telephony-common_classes.dex
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
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$2$mYGM42yhe76zJekjTAzT10LdEMk) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$2 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$2 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.2.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' android.service.euicc.GetDownloadableSubscriptionMetadataResult) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$2$mYGM42yhe76zJekjTAzT10LdEMk.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$2, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, android.service.euicc.GetDownloadableSubscriptionMetadataResult):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.2.onComplete(android.service.euicc.GetDownloadableSubscriptionMetadataResult):void, dex: boot-telephony-common_classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	... 60 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$2$mYGM42yhe76zJekjTAzT10LdEMk, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	... 65 more
                                        */
                                    /*
                                        this = this;
                                        com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r0 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                        com.android.internal.telephony.euicc.EuiccConnector r0 = com.android.internal.telephony.euicc.EuiccConnector.this
                                        com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r1 = r0
                                        com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$2$mYGM42yhe76zJekjTAzT10LdEMk r2 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$2$mYGM42yhe76zJekjTAzT10LdEMk
                                        r2.<init>(r3, r1, r4)
                                        r1 = 6
                                        r0.sendMessage(r1, r2)
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass2.onComplete(android.service.euicc.GetDownloadableSubscriptionMetadataResult):void");
                                }

                                public static /* synthetic */ void lambda$onComplete$0(AnonymousClass2 r1, BaseEuiccCommandCallback callback, GetDownloadableSubscriptionMetadataResult result) {
                                    ((GetMetadataCommandCallback) callback).onGetMetadataComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 102:
                            DownloadRequest request2 = (DownloadRequest) message.obj;
                            EuiccConnector.this.mEuiccService.downloadSubscription(-1, request2.mSubscription, request2.mSwitchAfterDownload, request2.mForceDeactivateSim, new IDownloadSubscriptionCallback.Stub() {
                                public void onComplete(int result) {
                                    EuiccConnector.this.sendMessage(6, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                          0x0002: IGET  (r0v1 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                          0x0000: IGET  (r0v0 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$3 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.3.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$3$kCYyTG6MMZu-1yQLS6p1_Mk7KM8
                                          0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$3$kCYyTG6MMZu-1yQLS6p1_Mk7KM8) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$3 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$3 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.3.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$3$kCYyTG6MMZu-1yQLS6p1_Mk7KM8.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$3, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.3.onComplete(int):void, dex: boot-telephony-common_classes.dex
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
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$3$kCYyTG6MMZu-1yQLS6p1_Mk7KM8) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$3 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$3 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.3.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$3$kCYyTG6MMZu-1yQLS6p1_Mk7KM8.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$3, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.3.onComplete(int):void, dex: boot-telephony-common_classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	... 60 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$3$kCYyTG6MMZu-1yQLS6p1_Mk7KM8, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	... 65 more
                                        */
                                    /*
                                        this = this;
                                        com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r0 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                        com.android.internal.telephony.euicc.EuiccConnector r0 = com.android.internal.telephony.euicc.EuiccConnector.this
                                        com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r1 = r0
                                        com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$3$kCYyTG6MMZu-1yQLS6p1_Mk7KM8 r2 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$3$kCYyTG6MMZu-1yQLS6p1_Mk7KM8
                                        r2.<init>(r3, r1, r4)
                                        r1 = 6
                                        r0.sendMessage(r1, r2)
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass3.onComplete(int):void");
                                }

                                public static /* synthetic */ void lambda$onComplete$0(AnonymousClass3 r1, BaseEuiccCommandCallback callback, int result) {
                                    ((DownloadCommandCallback) callback).onDownloadComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case EuiccConnector.CMD_GET_EUICC_PROFILE_INFO_LIST /*103*/:
                            EuiccConnector.this.mEuiccService.getEuiccProfileInfoList(-1, new IGetEuiccProfileInfoListCallback.Stub() {
                                public void onComplete(GetEuiccProfileInfoListResult result) {
                                    EuiccConnector.this.sendMessage(6, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                          0x0002: IGET  (r0v1 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                          0x0000: IGET  (r0v0 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$4 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.4.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$4$S52i3hpE3-FGho807KZ1LR5rXQM
                                          0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$4$S52i3hpE3-FGho807KZ1LR5rXQM) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$4 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$4 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.4.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' android.service.euicc.GetEuiccProfileInfoListResult) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$4$S52i3hpE3-FGho807KZ1LR5rXQM.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$4, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, android.service.euicc.GetEuiccProfileInfoListResult):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.4.onComplete(android.service.euicc.GetEuiccProfileInfoListResult):void, dex: boot-telephony-common_classes.dex
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
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$4$S52i3hpE3-FGho807KZ1LR5rXQM) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$4 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$4 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.4.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' android.service.euicc.GetEuiccProfileInfoListResult) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$4$S52i3hpE3-FGho807KZ1LR5rXQM.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$4, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, android.service.euicc.GetEuiccProfileInfoListResult):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.4.onComplete(android.service.euicc.GetEuiccProfileInfoListResult):void, dex: boot-telephony-common_classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	... 60 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$4$S52i3hpE3-FGho807KZ1LR5rXQM, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	... 65 more
                                        */
                                    /*
                                        this = this;
                                        com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r0 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                        com.android.internal.telephony.euicc.EuiccConnector r0 = com.android.internal.telephony.euicc.EuiccConnector.this
                                        com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r1 = r0
                                        com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$4$S52i3hpE3-FGho807KZ1LR5rXQM r2 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$4$S52i3hpE3-FGho807KZ1LR5rXQM
                                        r2.<init>(r3, r1, r4)
                                        r1 = 6
                                        r0.sendMessage(r1, r2)
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass4.onComplete(android.service.euicc.GetEuiccProfileInfoListResult):void");
                                }

                                public static /* synthetic */ void lambda$onComplete$0(AnonymousClass4 r1, BaseEuiccCommandCallback callback, GetEuiccProfileInfoListResult result) {
                                    ((GetEuiccProfileInfoListCommandCallback) callback).onListComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 104:
                            EuiccConnector.this.mEuiccService.getDefaultDownloadableSubscriptionList(-1, ((GetDefaultListRequest) message.obj).mForceDeactivateSim, new IGetDefaultDownloadableSubscriptionListCallback.Stub() {
                                public void onComplete(GetDefaultDownloadableSubscriptionListResult result) {
                                    EuiccConnector.this.sendMessage(6, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                          0x0002: IGET  (r0v1 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                          0x0000: IGET  (r0v0 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$5 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.5.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$5$fNoNRKwweNINlHKYo1LLy2Hd_RA
                                          0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$5$fNoNRKwweNINlHKYo1LLy2Hd_RA) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$5 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$5 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.5.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' android.service.euicc.GetDefaultDownloadableSubscriptionListResult) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$5$fNoNRKwweNINlHKYo1LLy2Hd_RA.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$5, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, android.service.euicc.GetDefaultDownloadableSubscriptionListResult):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.5.onComplete(android.service.euicc.GetDefaultDownloadableSubscriptionListResult):void, dex: boot-telephony-common_classes.dex
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
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$5$fNoNRKwweNINlHKYo1LLy2Hd_RA) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$5 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$5 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.5.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' android.service.euicc.GetDefaultDownloadableSubscriptionListResult) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$5$fNoNRKwweNINlHKYo1LLy2Hd_RA.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$5, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, android.service.euicc.GetDefaultDownloadableSubscriptionListResult):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.5.onComplete(android.service.euicc.GetDefaultDownloadableSubscriptionListResult):void, dex: boot-telephony-common_classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	... 60 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$5$fNoNRKwweNINlHKYo1LLy2Hd_RA, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	... 65 more
                                        */
                                    /*
                                        this = this;
                                        com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r0 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                        com.android.internal.telephony.euicc.EuiccConnector r0 = com.android.internal.telephony.euicc.EuiccConnector.this
                                        com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r1 = r0
                                        com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$5$fNoNRKwweNINlHKYo1LLy2Hd_RA r2 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$5$fNoNRKwweNINlHKYo1LLy2Hd_RA
                                        r2.<init>(r3, r1, r4)
                                        r1 = 6
                                        r0.sendMessage(r1, r2)
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass5.onComplete(android.service.euicc.GetDefaultDownloadableSubscriptionListResult):void");
                                }

                                public static /* synthetic */ void lambda$onComplete$0(AnonymousClass5 r1, BaseEuiccCommandCallback callback, GetDefaultDownloadableSubscriptionListResult result) {
                                    ((GetDefaultListCommandCallback) callback).onGetDefaultListComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 105:
                            EuiccConnector.this.mEuiccService.getEuiccInfo(-1, new IGetEuiccInfoCallback.Stub() {
                                public void onSuccess(EuiccInfo euiccInfo) {
                                    EuiccConnector.this.sendMessage(6, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                          0x0002: IGET  (r0v1 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                          0x0000: IGET  (r0v0 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$6 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.6.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$6$RMNCT6pukGHYhU_7k7HVxbm5IWE
                                          0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$6$RMNCT6pukGHYhU_7k7HVxbm5IWE) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$6 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$6 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.6.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'euiccInfo' android.telephony.euicc.EuiccInfo) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$6$RMNCT6pukGHYhU_7k7HVxbm5IWE.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$6, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, android.telephony.euicc.EuiccInfo):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.6.onSuccess(android.telephony.euicc.EuiccInfo):void, dex: boot-telephony-common_classes.dex
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
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$6$RMNCT6pukGHYhU_7k7HVxbm5IWE) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$6 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$6 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.6.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'euiccInfo' android.telephony.euicc.EuiccInfo) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$6$RMNCT6pukGHYhU_7k7HVxbm5IWE.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$6, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, android.telephony.euicc.EuiccInfo):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.6.onSuccess(android.telephony.euicc.EuiccInfo):void, dex: boot-telephony-common_classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	... 60 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$6$RMNCT6pukGHYhU_7k7HVxbm5IWE, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	... 65 more
                                        */
                                    /*
                                        this = this;
                                        com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r0 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                        com.android.internal.telephony.euicc.EuiccConnector r0 = com.android.internal.telephony.euicc.EuiccConnector.this
                                        com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r1 = r0
                                        com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$6$RMNCT6pukGHYhU_7k7HVxbm5IWE r2 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$6$RMNCT6pukGHYhU_7k7HVxbm5IWE
                                        r2.<init>(r3, r1, r4)
                                        r1 = 6
                                        r0.sendMessage(r1, r2)
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass6.onSuccess(android.telephony.euicc.EuiccInfo):void");
                                }

                                public static /* synthetic */ void lambda$onSuccess$0(AnonymousClass6 r1, BaseEuiccCommandCallback callback, EuiccInfo euiccInfo) {
                                    ((GetEuiccInfoCommandCallback) callback).onGetEuiccInfoComplete(euiccInfo);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 106:
                            EuiccConnector.this.mEuiccService.deleteSubscription(-1, ((DeleteRequest) message.obj).mIccid, new IDeleteSubscriptionCallback.Stub() {
                                public void onComplete(int result) {
                                    EuiccConnector.this.sendMessage(6, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                          0x0002: IGET  (r0v1 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                          0x0000: IGET  (r0v0 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$7 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.7.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$7$-Ogvr7PIASwQa0kQAqAyfdEKAG4
                                          0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$7$-Ogvr7PIASwQa0kQAqAyfdEKAG4) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$7 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$7 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.7.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$7$-Ogvr7PIASwQa0kQAqAyfdEKAG4.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$7, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.7.onComplete(int):void, dex: boot-telephony-common_classes.dex
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
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$7$-Ogvr7PIASwQa0kQAqAyfdEKAG4) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$7 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$7 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.7.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$7$-Ogvr7PIASwQa0kQAqAyfdEKAG4.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$7, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.7.onComplete(int):void, dex: boot-telephony-common_classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	... 60 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$7$-Ogvr7PIASwQa0kQAqAyfdEKAG4, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	... 65 more
                                        */
                                    /*
                                        this = this;
                                        com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r0 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                        com.android.internal.telephony.euicc.EuiccConnector r0 = com.android.internal.telephony.euicc.EuiccConnector.this
                                        com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r1 = r0
                                        com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$7$-Ogvr7PIASwQa0kQAqAyfdEKAG4 r2 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$7$-Ogvr7PIASwQa0kQAqAyfdEKAG4
                                        r2.<init>(r3, r1, r4)
                                        r1 = 6
                                        r0.sendMessage(r1, r2)
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass7.onComplete(int):void");
                                }

                                public static /* synthetic */ void lambda$onComplete$0(AnonymousClass7 r1, BaseEuiccCommandCallback callback, int result) {
                                    ((DeleteCommandCallback) callback).onDeleteComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case EuiccConnector.CMD_SWITCH_TO_SUBSCRIPTION /*107*/:
                            SwitchRequest request3 = (SwitchRequest) message.obj;
                            EuiccConnector.this.mEuiccService.switchToSubscription(-1, request3.mIccid, request3.mForceDeactivateSim, new ISwitchToSubscriptionCallback.Stub() {
                                public void onComplete(int result) {
                                    EuiccConnector.this.sendMessage(6, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                          0x0002: IGET  (r0v1 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                          0x0000: IGET  (r0v0 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$8 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.8.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$8$653ymvVUxXSmc5rF5YXkbNw3yw8
                                          0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$8$653ymvVUxXSmc5rF5YXkbNw3yw8) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$8 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$8 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.8.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$8$653ymvVUxXSmc5rF5YXkbNw3yw8.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$8, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.8.onComplete(int):void, dex: boot-telephony-common_classes.dex
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
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$8$653ymvVUxXSmc5rF5YXkbNw3yw8) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$8 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$8 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.8.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$8$653ymvVUxXSmc5rF5YXkbNw3yw8.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$8, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.8.onComplete(int):void, dex: boot-telephony-common_classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	... 60 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$8$653ymvVUxXSmc5rF5YXkbNw3yw8, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	... 65 more
                                        */
                                    /*
                                        this = this;
                                        com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r0 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                        com.android.internal.telephony.euicc.EuiccConnector r0 = com.android.internal.telephony.euicc.EuiccConnector.this
                                        com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r1 = r0
                                        com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$8$653ymvVUxXSmc5rF5YXkbNw3yw8 r2 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$8$653ymvVUxXSmc5rF5YXkbNw3yw8
                                        r2.<init>(r3, r1, r4)
                                        r1 = 6
                                        r0.sendMessage(r1, r2)
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass8.onComplete(int):void");
                                }

                                public static /* synthetic */ void lambda$onComplete$0(AnonymousClass8 r1, BaseEuiccCommandCallback callback, int result) {
                                    ((SwitchCommandCallback) callback).onSwitchComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 108:
                            UpdateNicknameRequest request4 = (UpdateNicknameRequest) message.obj;
                            EuiccConnector.this.mEuiccService.updateSubscriptionNickname(-1, request4.mIccid, request4.mNickname, new IUpdateSubscriptionNicknameCallback.Stub() {
                                public void onComplete(int result) {
                                    EuiccConnector.this.sendMessage(6, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                          0x0002: IGET  (r0v1 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                          0x0000: IGET  (r0v0 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$9 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.9.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$9$xm26YKGxl72UYoxSNyEMJslmmNk
                                          0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$9$xm26YKGxl72UYoxSNyEMJslmmNk) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$9 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$9 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.9.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$9$xm26YKGxl72UYoxSNyEMJslmmNk.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$9, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.9.onComplete(int):void, dex: boot-telephony-common_classes.dex
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
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$9$xm26YKGxl72UYoxSNyEMJslmmNk) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$9 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$9 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.9.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$9$xm26YKGxl72UYoxSNyEMJslmmNk.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$9, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.9.onComplete(int):void, dex: boot-telephony-common_classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	... 60 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$9$xm26YKGxl72UYoxSNyEMJslmmNk, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	... 65 more
                                        */
                                    /*
                                        this = this;
                                        com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r0 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                        com.android.internal.telephony.euicc.EuiccConnector r0 = com.android.internal.telephony.euicc.EuiccConnector.this
                                        com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r1 = r0
                                        com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$9$xm26YKGxl72UYoxSNyEMJslmmNk r2 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$9$xm26YKGxl72UYoxSNyEMJslmmNk
                                        r2.<init>(r3, r1, r4)
                                        r1 = 6
                                        r0.sendMessage(r1, r2)
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass9.onComplete(int):void");
                                }

                                public static /* synthetic */ void lambda$onComplete$0(AnonymousClass9 r1, BaseEuiccCommandCallback callback, int result) {
                                    ((UpdateNicknameCommandCallback) callback).onUpdateNicknameComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case EuiccConnector.CMD_ERASE_SUBSCRIPTIONS /*109*/:
                            EuiccConnector.this.mEuiccService.eraseSubscriptions(-1, new IEraseSubscriptionsCallback.Stub() {
                                public void onComplete(int result) {
                                    EuiccConnector.this.sendMessage(6, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                          0x0002: IGET  (r0v1 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                          0x0000: IGET  (r0v0 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$10 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.10.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$10$uMqDQsfFYIEEah_N7V76hMlEL94
                                          0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$10$uMqDQsfFYIEEah_N7V76hMlEL94) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$10 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$10 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.10.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$10$uMqDQsfFYIEEah_N7V76hMlEL94.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$10, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.10.onComplete(int):void, dex: boot-telephony-common_classes.dex
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
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$10$uMqDQsfFYIEEah_N7V76hMlEL94) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$10 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$10 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.10.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$10$uMqDQsfFYIEEah_N7V76hMlEL94.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$10, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.10.onComplete(int):void, dex: boot-telephony-common_classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	... 60 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$10$uMqDQsfFYIEEah_N7V76hMlEL94, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	... 65 more
                                        */
                                    /*
                                        this = this;
                                        com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r0 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                        com.android.internal.telephony.euicc.EuiccConnector r0 = com.android.internal.telephony.euicc.EuiccConnector.this
                                        com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r1 = r0
                                        com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$10$uMqDQsfFYIEEah_N7V76hMlEL94 r2 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$10$uMqDQsfFYIEEah_N7V76hMlEL94
                                        r2.<init>(r3, r1, r4)
                                        r1 = 6
                                        r0.sendMessage(r1, r2)
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass10.onComplete(int):void");
                                }

                                public static /* synthetic */ void lambda$onComplete$0(AnonymousClass10 r1, BaseEuiccCommandCallback callback, int result) {
                                    ((EraseCommandCallback) callback).onEraseComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 110:
                            EuiccConnector.this.mEuiccService.retainSubscriptionsForFactoryReset(-1, new IRetainSubscriptionsForFactoryResetCallback.Stub() {
                                public void onComplete(int result) {
                                    EuiccConnector.this.sendMessage(6, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                          0x0002: IGET  (r0v1 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                          0x0000: IGET  (r0v0 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$11 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.11.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$11$yvv0ylXs7V5vymCcYvu3RpgoeDw
                                          0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$11$yvv0ylXs7V5vymCcYvu3RpgoeDw) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$11 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$11 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.11.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$11$yvv0ylXs7V5vymCcYvu3RpgoeDw.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$11, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.11.onComplete(int):void, dex: boot-telephony-common_classes.dex
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
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$11$yvv0ylXs7V5vymCcYvu3RpgoeDw) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$11 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$11 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.11.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'result' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$11$yvv0ylXs7V5vymCcYvu3RpgoeDw.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$11, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.11.onComplete(int):void, dex: boot-telephony-common_classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	... 60 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$11$yvv0ylXs7V5vymCcYvu3RpgoeDw, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	... 65 more
                                        */
                                    /*
                                        this = this;
                                        com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r0 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                        com.android.internal.telephony.euicc.EuiccConnector r0 = com.android.internal.telephony.euicc.EuiccConnector.this
                                        com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r1 = r0
                                        com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$11$yvv0ylXs7V5vymCcYvu3RpgoeDw r2 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$11$yvv0ylXs7V5vymCcYvu3RpgoeDw
                                        r2.<init>(r3, r1, r4)
                                        r1 = 6
                                        r0.sendMessage(r1, r2)
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass11.onComplete(int):void");
                                }

                                public static /* synthetic */ void lambda$onComplete$0(AnonymousClass11 r1, BaseEuiccCommandCallback callback, int result) {
                                    ((RetainSubscriptionsCommandCallback) callback).onRetainSubscriptionsComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 111:
                            EuiccConnector.this.mEuiccService.getOtaStatus(-1, new IGetOtaStatusCallback.Stub() {
                                public void onSuccess(int status) {
                                    EuiccConnector.this.sendMessage(6, 
                                    /*  JADX ERROR: Method code generation error
                                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                          0x0002: IGET  (r0v1 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                          0x0000: IGET  (r0v0 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$12 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.12.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$12$wYal9P4llN7g9YAk_zACL8m3nS0
                                          0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$12$wYal9P4llN7g9YAk_zACL8m3nS0) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$12 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$12 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.12.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'status' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$12$wYal9P4llN7g9YAk_zACL8m3nS0.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$12, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.12.onSuccess(int):void, dex: boot-telephony-common_classes.dex
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
                                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$12$wYal9P4llN7g9YAk_zACL8m3nS0) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$12 A[THIS]), (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                          0x0004: IGET  (r1v0 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r3v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$12 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.12.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r4v0 'status' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$12$wYal9P4llN7g9YAk_zACL8m3nS0.<init>(com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$12, com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.12.onSuccess(int):void, dex: boot-telephony-common_classes.dex
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                        	... 60 more
                                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$12$wYal9P4llN7g9YAk_zACL8m3nS0, state: NOT_LOADED
                                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                        	... 65 more
                                        */
                                    /*
                                        this = this;
                                        com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r0 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                        com.android.internal.telephony.euicc.EuiccConnector r0 = com.android.internal.telephony.euicc.EuiccConnector.this
                                        com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r1 = r0
                                        com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$12$wYal9P4llN7g9YAk_zACL8m3nS0 r2 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$12$wYal9P4llN7g9YAk_zACL8m3nS0
                                        r2.<init>(r3, r1, r4)
                                        r1 = 6
                                        r0.sendMessage(r1, r2)
                                        return
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass12.onSuccess(int):void");
                                }

                                public static /* synthetic */ void lambda$onSuccess$0(AnonymousClass12 r1, BaseEuiccCommandCallback callback, int status) {
                                    ((GetOtaStatusCommandCallback) callback).onGetOtaStatusComplete(status);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 112:
                            EuiccConnector.this.mEuiccService.startOtaIfNecessary(-1, new IOtaStatusChangedCallback.Stub() {
                                public void onOtaStatusChanged(int status) throws RemoteException {
                                    if (status == 1) {
                                        EuiccConnector.this.sendMessage(6, 
                                        /*  JADX ERROR: Method code generation error
                                            jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000f: INVOKE  (wrap: com.android.internal.telephony.euicc.EuiccConnector
                                              0x0006: IGET  (r1v4 com.android.internal.telephony.euicc.EuiccConnector) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$ConnectedState
                                              0x0004: IGET  (r1v3 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) = (r4v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$13 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.13.this$1 com.android.internal.telephony.euicc.EuiccConnector$ConnectedState) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this$0 com.android.internal.telephony.euicc.EuiccConnector), (6 int), (wrap: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$13$5nh8TOHvAdIIa_S3V0gwsRICKC4
                                              0x000c: CONSTRUCTOR  (r3v1 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$13$5nh8TOHvAdIIa_S3V0gwsRICKC4) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                              0x0008: IGET  (r2v1 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r4v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$13 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.13.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r5v0 'status' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$13$5nh8TOHvAdIIa_S3V0gwsRICKC4.<init>(com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR) com.android.internal.telephony.euicc.EuiccConnector.sendMessage(int, java.lang.Object):void type: VIRTUAL in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.13.onOtaStatusChanged(int):void, dex: boot-telephony-common_classes.dex
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                            	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                            	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                            	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                            	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                            	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                            	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                                            	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                                            	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                                            	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                            	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                            	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                            	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                            	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                            	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:298)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                            	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                            	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                            	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                            	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                            	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                            	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                            	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                            	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                            	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                            	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                            	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                            	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                            	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                            	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: CONSTRUCTOR  (r3v1 com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$13$5nh8TOHvAdIIa_S3V0gwsRICKC4) = (wrap: com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback
                                              0x0008: IGET  (r2v1 com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback) = (r4v0 'this' com.android.internal.telephony.euicc.EuiccConnector$ConnectedState$13 A[THIS]) com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.13.val$callback com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback), (r5v0 'status' int) com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$13$5nh8TOHvAdIIa_S3V0gwsRICKC4.<init>(com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback, int):void CONSTRUCTOR in method: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.13.onOtaStatusChanged(int):void, dex: boot-telephony-common_classes.dex
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                            	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                            	... 65 more
                                            Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$13$5nh8TOHvAdIIa_S3V0gwsRICKC4, state: NOT_LOADED
                                            	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                            	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                            	... 70 more
                                            */
                                        /*
                                            this = this;
                                            r0 = 6
                                            r1 = 1
                                            if (r5 != r1) goto L_0x0013
                                            com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r1 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                            com.android.internal.telephony.euicc.EuiccConnector r1 = com.android.internal.telephony.euicc.EuiccConnector.this
                                            com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r2 = r0
                                            com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$13$5nh8TOHvAdIIa_S3V0gwsRICKC4 r3 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$13$5nh8TOHvAdIIa_S3V0gwsRICKC4
                                            r3.<init>(r2, r5)
                                            r1.sendMessage(r0, r3)
                                            goto L_0x0021
                                        L_0x0013:
                                            com.android.internal.telephony.euicc.EuiccConnector$ConnectedState r1 = com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.this
                                            com.android.internal.telephony.euicc.EuiccConnector r1 = com.android.internal.telephony.euicc.EuiccConnector.this
                                            com.android.internal.telephony.euicc.EuiccConnector$BaseEuiccCommandCallback r2 = r0
                                            com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$13$REfW_lBcrAssQONSKwOlO3PX83k r3 = new com.android.internal.telephony.euicc.-$$Lambda$EuiccConnector$ConnectedState$13$REfW_lBcrAssQONSKwOlO3PX83k
                                            r3.<init>(r4, r2, r5)
                                            r1.sendMessage(r0, r3)
                                        L_0x0021:
                                            return
                                        */
                                        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass13.onOtaStatusChanged(int):void");
                                    }

                                    public static /* synthetic */ void lambda$onOtaStatusChanged$1(AnonymousClass13 r1, BaseEuiccCommandCallback callback, int status) {
                                        ((OtaStatusChangedCallback) callback).onOtaStatusChanged(status);
                                        EuiccConnector.this.onCommandEnd(callback);
                                    }
                                });
                                break;
                            default:
                                Log.wtf(EuiccConnector.TAG, "Unimplemented eUICC command: " + message.what);
                                callback.onEuiccServiceUnavailable();
                                EuiccConnector.this.onCommandEnd(callback);
                                return true;
                        }
                    } catch (Exception e) {
                        Log.w(EuiccConnector.TAG, "Exception making binder call to EuiccService", e);
                        callback.onEuiccServiceUnavailable();
                        EuiccConnector.this.onCommandEnd(callback);
                    }
                    return true;
                }
            }

            public void exit() {
                EuiccConnector.this.removeMessages(3);
                for (BaseEuiccCommandCallback callback : EuiccConnector.this.mActiveCommandCallbacks) {
                    callback.onEuiccServiceUnavailable();
                }
                EuiccConnector.this.mActiveCommandCallbacks.clear();
            }
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public interface DeleteCommandCallback extends BaseEuiccCommandCallback {
            void onDeleteComplete(int i);
        }

        static class DeleteRequest {
            DeleteCommandCallback mCallback;
            String mIccid;

            DeleteRequest() {
            }
        }

        private class DisconnectedState extends State {
            private DisconnectedState() {
            }

            public void enter() {
                EuiccConnector.this.sendMessageDelayed(2, 30000);
            }

            public boolean processMessage(Message message) {
                boolean isSameComponent;
                if (message.what == 4) {
                    IEuiccService unused = EuiccConnector.this.mEuiccService = (IEuiccService) message.obj;
                    EuiccConnector.this.transitionTo(EuiccConnector.this.mConnectedState);
                    return true;
                }
                boolean forceRebind = false;
                if (message.what == 1) {
                    ServiceInfo bestComponent = EuiccConnector.this.findBestComponent();
                    String affectedPackage = (String) message.obj;
                    if (bestComponent == null) {
                        isSameComponent = EuiccConnector.this.mSelectedComponent != null;
                    } else {
                        isSameComponent = EuiccConnector.this.mSelectedComponent == null || Objects.equals(bestComponent.getComponentName(), EuiccConnector.this.mSelectedComponent.getComponentName());
                    }
                    if (bestComponent != null && Objects.equals(bestComponent.packageName, affectedPackage)) {
                        forceRebind = true;
                    }
                    if (!isSameComponent || forceRebind) {
                        EuiccConnector.this.unbind();
                        ServiceInfo unused2 = EuiccConnector.this.mSelectedComponent = bestComponent;
                        if (EuiccConnector.this.mSelectedComponent == null) {
                            EuiccConnector.this.transitionTo(EuiccConnector.this.mUnavailableState);
                        } else {
                            EuiccConnector.this.transitionTo(EuiccConnector.this.mBindingState);
                        }
                    }
                    return true;
                } else if (message.what == 2) {
                    EuiccConnector.this.transitionTo(EuiccConnector.this.mAvailableState);
                    return true;
                } else if (!EuiccConnector.isEuiccCommand(message.what)) {
                    return false;
                } else {
                    EuiccConnector.this.deferMessage(message);
                    return true;
                }
            }
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public interface DownloadCommandCallback extends BaseEuiccCommandCallback {
            void onDownloadComplete(int i);
        }

        static class DownloadRequest {
            DownloadCommandCallback mCallback;
            boolean mForceDeactivateSim;
            DownloadableSubscription mSubscription;
            boolean mSwitchAfterDownload;

            DownloadRequest() {
            }
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public interface EraseCommandCallback extends BaseEuiccCommandCallback {
            void onEraseComplete(int i);
        }

        private class EuiccPackageMonitor extends PackageMonitor {
            private EuiccPackageMonitor() {
            }

            public void onPackageAdded(String packageName, int reason) {
                sendPackageChange(packageName, true);
            }

            public void onPackageRemoved(String packageName, int reason) {
                sendPackageChange(packageName, true);
            }

            public void onPackageUpdateFinished(String packageName, int uid) {
                sendPackageChange(packageName, true);
            }

            public void onPackageModified(String packageName) {
                sendPackageChange(packageName, false);
            }

            public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
                if (doit) {
                    for (String packageName : packages) {
                        sendPackageChange(packageName, true);
                    }
                }
                return EuiccConnector.super.onHandleForceStop(intent, packages, uid, doit);
            }

            private void sendPackageChange(String packageName, boolean forceUnbindForThisPackage) {
                EuiccConnector.this.sendMessage(1, forceUnbindForThisPackage ? packageName : null);
            }
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public interface GetDefaultListCommandCallback extends BaseEuiccCommandCallback {
            void onGetDefaultListComplete(GetDefaultDownloadableSubscriptionListResult getDefaultDownloadableSubscriptionListResult);
        }

        static class GetDefaultListRequest {
            GetDefaultListCommandCallback mCallback;
            boolean mForceDeactivateSim;

            GetDefaultListRequest() {
            }
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public interface GetEidCommandCallback extends BaseEuiccCommandCallback {
            void onGetEidComplete(String str);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public interface GetEuiccInfoCommandCallback extends BaseEuiccCommandCallback {
            void onGetEuiccInfoComplete(EuiccInfo euiccInfo);
        }

        interface GetEuiccProfileInfoListCommandCallback extends BaseEuiccCommandCallback {
            void onListComplete(GetEuiccProfileInfoListResult getEuiccProfileInfoListResult);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public interface GetMetadataCommandCallback extends BaseEuiccCommandCallback {
            void onGetMetadataComplete(GetDownloadableSubscriptionMetadataResult getDownloadableSubscriptionMetadataResult);
        }

        static class GetMetadataRequest {
            GetMetadataCommandCallback mCallback;
            boolean mForceDeactivateSim;
            DownloadableSubscription mSubscription;

            GetMetadataRequest() {
            }
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public interface GetOtaStatusCommandCallback extends BaseEuiccCommandCallback {
            void onGetOtaStatusComplete(int i);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public interface OtaStatusChangedCallback extends BaseEuiccCommandCallback {
            void onOtaStatusChanged(int i);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public interface RetainSubscriptionsCommandCallback extends BaseEuiccCommandCallback {
            void onRetainSubscriptionsComplete(int i);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public interface SwitchCommandCallback extends BaseEuiccCommandCallback {
            void onSwitchComplete(int i);
        }

        static class SwitchRequest {
            SwitchCommandCallback mCallback;
            boolean mForceDeactivateSim;
            String mIccid;

            SwitchRequest() {
            }
        }

        private class UnavailableState extends State {
            private UnavailableState() {
            }

            public boolean processMessage(Message message) {
                if (message.what == 1) {
                    ServiceInfo unused = EuiccConnector.this.mSelectedComponent = EuiccConnector.this.findBestComponent();
                    if (EuiccConnector.this.mSelectedComponent != null) {
                        EuiccConnector.this.transitionTo(EuiccConnector.this.mAvailableState);
                    } else if (EuiccConnector.this.getCurrentState() != EuiccConnector.this.mUnavailableState) {
                        EuiccConnector.this.transitionTo(EuiccConnector.this.mUnavailableState);
                    }
                    return true;
                } else if (!EuiccConnector.isEuiccCommand(message.what)) {
                    return false;
                } else {
                    EuiccConnector.getCallback(message).onEuiccServiceUnavailable();
                    return true;
                }
            }
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public interface UpdateNicknameCommandCallback extends BaseEuiccCommandCallback {
            void onUpdateNicknameComplete(int i);
        }

        static class UpdateNicknameRequest {
            UpdateNicknameCommandCallback mCallback;
            String mIccid;
            String mNickname;

            UpdateNicknameRequest() {
            }
        }

        /* access modifiers changed from: private */
        public static boolean isEuiccCommand(int what) {
            return what >= 100;
        }

        public static ActivityInfo findBestActivity(PackageManager packageManager, Intent intent) {
            ActivityInfo bestComponent = (ActivityInfo) findBestComponent(packageManager, packageManager.queryIntentActivities(intent, EUICC_QUERY_FLAGS));
            if (bestComponent == null) {
                Log.w(TAG, "No valid component found for intent: " + intent);
            }
            return bestComponent;
        }

        public static ComponentInfo findBestComponent(PackageManager packageManager) {
            ComponentInfo bestComponent = findBestComponent(packageManager, packageManager.queryIntentServices(new Intent("android.service.euicc.EuiccService"), EUICC_QUERY_FLAGS));
            if (bestComponent == null) {
                Log.w(TAG, "No valid EuiccService implementation found");
            }
            return bestComponent;
        }

        EuiccConnector(Context context) {
            super(TAG);
            init(context);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public EuiccConnector(Context context, Looper looper) {
            super(TAG, looper);
            init(context);
        }

        private void init(Context context) {
            this.mContext = context;
            this.mPm = context.getPackageManager();
            this.mUnavailableState = new UnavailableState();
            addState(this.mUnavailableState);
            this.mAvailableState = new AvailableState();
            addState(this.mAvailableState, this.mUnavailableState);
            this.mBindingState = new BindingState();
            addState(this.mBindingState);
            this.mDisconnectedState = new DisconnectedState();
            addState(this.mDisconnectedState);
            this.mConnectedState = new ConnectedState();
            addState(this.mConnectedState, this.mDisconnectedState);
            this.mSelectedComponent = findBestComponent();
            setInitialState(this.mSelectedComponent != null ? this.mAvailableState : this.mUnavailableState);
            this.mPackageMonitor.register(this.mContext, null, false);
            this.mContext.registerReceiver(this.mUserUnlockedReceiver, new IntentFilter("android.intent.action.USER_UNLOCKED"));
            start();
        }

        public void onHalting() {
            this.mPackageMonitor.unregister();
            this.mContext.unregisterReceiver(this.mUserUnlockedReceiver);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void getEid(GetEidCommandCallback callback) {
            sendMessage(100, callback);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void getOtaStatus(GetOtaStatusCommandCallback callback) {
            sendMessage(111, callback);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void startOtaIfNecessary(OtaStatusChangedCallback callback) {
            sendMessage(112, callback);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void getDownloadableSubscriptionMetadata(DownloadableSubscription subscription, boolean forceDeactivateSim, GetMetadataCommandCallback callback) {
            GetMetadataRequest request = new GetMetadataRequest();
            request.mSubscription = subscription;
            request.mForceDeactivateSim = forceDeactivateSim;
            request.mCallback = callback;
            sendMessage(101, request);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void downloadSubscription(DownloadableSubscription subscription, boolean switchAfterDownload, boolean forceDeactivateSim, DownloadCommandCallback callback) {
            DownloadRequest request = new DownloadRequest();
            request.mSubscription = subscription;
            request.mSwitchAfterDownload = switchAfterDownload;
            request.mForceDeactivateSim = forceDeactivateSim;
            request.mCallback = callback;
            sendMessage(102, request);
        }

        /* access modifiers changed from: package-private */
        public void getEuiccProfileInfoList(GetEuiccProfileInfoListCommandCallback callback) {
            sendMessage(CMD_GET_EUICC_PROFILE_INFO_LIST, callback);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void getDefaultDownloadableSubscriptionList(boolean forceDeactivateSim, GetDefaultListCommandCallback callback) {
            GetDefaultListRequest request = new GetDefaultListRequest();
            request.mForceDeactivateSim = forceDeactivateSim;
            request.mCallback = callback;
            sendMessage(104, request);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void getEuiccInfo(GetEuiccInfoCommandCallback callback) {
            sendMessage(105, callback);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void deleteSubscription(String iccid, DeleteCommandCallback callback) {
            DeleteRequest request = new DeleteRequest();
            request.mIccid = iccid;
            request.mCallback = callback;
            sendMessage(106, request);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void switchToSubscription(String iccid, boolean forceDeactivateSim, SwitchCommandCallback callback) {
            SwitchRequest request = new SwitchRequest();
            request.mIccid = iccid;
            request.mForceDeactivateSim = forceDeactivateSim;
            request.mCallback = callback;
            sendMessage(CMD_SWITCH_TO_SUBSCRIPTION, request);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void updateSubscriptionNickname(String iccid, String nickname, UpdateNicknameCommandCallback callback) {
            UpdateNicknameRequest request = new UpdateNicknameRequest();
            request.mIccid = iccid;
            request.mNickname = nickname;
            request.mCallback = callback;
            sendMessage(108, request);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void eraseSubscriptions(EraseCommandCallback callback) {
            sendMessage(CMD_ERASE_SUBSCRIPTIONS, callback);
        }

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public void retainSubscriptions(RetainSubscriptionsCommandCallback callback) {
            sendMessage(110, callback);
        }

        /* access modifiers changed from: private */
        public static BaseEuiccCommandCallback getCallback(Message message) {
            switch (message.what) {
                case 100:
                case CMD_GET_EUICC_PROFILE_INFO_LIST /*103*/:
                case 105:
                case CMD_ERASE_SUBSCRIPTIONS /*109*/:
                case 110:
                case 111:
                case 112:
                    return (BaseEuiccCommandCallback) message.obj;
                case 101:
                    return ((GetMetadataRequest) message.obj).mCallback;
                case 102:
                    return ((DownloadRequest) message.obj).mCallback;
                case 104:
                    return ((GetDefaultListRequest) message.obj).mCallback;
                case 106:
                    return ((DeleteRequest) message.obj).mCallback;
                case CMD_SWITCH_TO_SUBSCRIPTION /*107*/:
                    return ((SwitchRequest) message.obj).mCallback;
                case 108:
                    return ((UpdateNicknameRequest) message.obj).mCallback;
                default:
                    throw new IllegalArgumentException("Unsupported message: " + message.what);
            }
        }

        /* access modifiers changed from: private */
        public void onCommandStart(BaseEuiccCommandCallback callback) {
            this.mActiveCommandCallbacks.add(callback);
            removeMessages(3);
        }

        /* access modifiers changed from: private */
        public void onCommandEnd(BaseEuiccCommandCallback callback) {
            if (!this.mActiveCommandCallbacks.remove(callback)) {
                Log.wtf(TAG, "Callback already removed from mActiveCommandCallbacks");
            }
            if (this.mActiveCommandCallbacks.isEmpty()) {
                sendMessageDelayed(3, 60000);
            }
        }

        /* access modifiers changed from: private */
        public ServiceInfo findBestComponent() {
            return (ServiceInfo) findBestComponent(this.mPm);
        }

        /* access modifiers changed from: private */
        public boolean createBinding() {
            if (this.mSelectedComponent == null) {
                Log.wtf(TAG, "Attempting to create binding but no component is selected");
                return false;
            }
            Intent intent = new Intent("android.service.euicc.EuiccService");
            intent.setComponent(this.mSelectedComponent.getComponentName());
            return this.mContext.bindService(intent, this, 67108865);
        }

        /* access modifiers changed from: private */
        public void unbind() {
            this.mEuiccService = null;
            this.mContext.unbindService(this);
        }

        private static ComponentInfo findBestComponent(PackageManager packageManager, List<ResolveInfo> resolveInfoList) {
            int bestPriority = Integer.MIN_VALUE;
            ComponentInfo bestComponent = null;
            if (resolveInfoList != null) {
                for (ResolveInfo resolveInfo : resolveInfoList) {
                    if (isValidEuiccComponent(packageManager, resolveInfo) && resolveInfo.filter.getPriority() > bestPriority) {
                        bestPriority = resolveInfo.filter.getPriority();
                        bestComponent = resolveInfo.getComponentInfo();
                    }
                }
            }
            return bestComponent;
        }

        private static boolean isValidEuiccComponent(PackageManager packageManager, ResolveInfo resolveInfo) {
            String permission;
            ComponentInfo componentInfo = resolveInfo.getComponentInfo();
            String packageName = componentInfo.getComponentName().getPackageName();
            if (packageManager.checkPermission("android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS", packageName) != 0) {
                Log.wtf(TAG, "Package " + packageName + " does not declare WRITE_EMBEDDED_SUBSCRIPTIONS");
                return false;
            }
            if (componentInfo instanceof ServiceInfo) {
                permission = ((ServiceInfo) componentInfo).permission;
            } else if (componentInfo instanceof ActivityInfo) {
                permission = ((ActivityInfo) componentInfo).permission;
            } else {
                throw new IllegalArgumentException("Can only verify services/activities");
            }
            if (!TextUtils.equals(permission, "android.permission.BIND_EUICC_SERVICE")) {
                Log.wtf(TAG, "Package " + packageName + " does not require the BIND_EUICC_SERVICE permission");
                return false;
            } else if (resolveInfo.filter != null && resolveInfo.filter.getPriority() != 0) {
                return true;
            } else {
                Log.wtf(TAG, "Package " + packageName + " does not specify a priority");
                return false;
            }
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            sendMessage(4, IEuiccService.Stub.asInterface(service));
        }

        public void onServiceDisconnected(ComponentName name) {
            sendMessage(5);
        }

        /* access modifiers changed from: protected */
        public void unhandledMessage(Message msg) {
            IState state = getCurrentState();
            StringBuilder sb = new StringBuilder();
            sb.append("Unhandled message ");
            sb.append(msg.what);
            sb.append(" in state ");
            sb.append(state == null ? "null" : state.getName());
            Log.wtf(TAG, sb.toString());
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            EuiccConnector.super.dump(fd, pw, args);
            pw.println("mSelectedComponent=" + this.mSelectedComponent);
            pw.println("mEuiccService=" + this.mEuiccService);
            pw.println("mActiveCommandCount=" + this.mActiveCommandCallbacks.size());
        }
    }
