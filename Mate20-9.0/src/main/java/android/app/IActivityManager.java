package android.app;

import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.app.IActivityController;
import android.app.IApplicationThread;
import android.app.IAssistDataReceiver;
import android.app.IInstrumentationWatcher;
import android.app.IProcessObserver;
import android.app.IServiceConnection;
import android.app.IStopUserCallback;
import android.app.ITaskStackListener;
import android.app.IUiAutomationConnection;
import android.app.IUidObserver;
import android.app.IUserSwitchObserver;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IProgressListener;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.WorkSource;
import android.service.voice.IVoiceInteractionSession;
import android.text.TextUtils;
import android.view.IRecentsAnimationRunner;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationDefinition;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.IResultReceiver;
import com.android.internal.policy.IKeyguardDismissCallback;
import java.util.List;

public interface IActivityManager extends IInterface {

    public static abstract class Stub extends Binder implements IActivityManager {
        private static final String DESCRIPTOR = "android.app.IActivityManager";
        static final int TRANSACTION_activityDestroyed = 58;
        static final int TRANSACTION_activityIdle = 15;
        static final int TRANSACTION_activityPaused = 16;
        static final int TRANSACTION_activityRelaunched = 259;
        static final int TRANSACTION_activityResumed = 35;
        static final int TRANSACTION_activitySlept = 121;
        static final int TRANSACTION_activityStopped = 17;
        static final int TRANSACTION_addAppTask = 208;
        static final int TRANSACTION_addInstrumentationResults = 40;
        static final int TRANSACTION_addPackageDependency = 93;
        static final int TRANSACTION_alwaysShowUnsupportedCompileSdkWarning = 306;
        static final int TRANSACTION_appNotRespondingViaProvider = 184;
        static final int TRANSACTION_attachApplication = 14;
        static final int TRANSACTION_backgroundWhitelistUid = 299;
        static final int TRANSACTION_backupAgentCreated = 89;
        static final int TRANSACTION_bindBackupAgent = 88;
        static final int TRANSACTION_bindService = 32;
        static final int TRANSACTION_bootAnimationComplete = 213;
        static final int TRANSACTION_broadcastIntent = 11;
        static final int TRANSACTION_cancelIntentSender = 60;
        static final int TRANSACTION_cancelRecentsAnimation = 197;
        static final int TRANSACTION_cancelTaskWindowTransition = 292;
        static final int TRANSACTION_checkGrantUriPermission = 117;
        static final int TRANSACTION_checkPermission = 49;
        static final int TRANSACTION_checkPermissionWithToken = 217;
        static final int TRANSACTION_checkUriPermission = 50;
        static final int TRANSACTION_clearApplicationUserData = 76;
        static final int TRANSACTION_clearGrantedUriPermissions = 265;
        static final int TRANSACTION_clearPendingBackup = 160;
        static final int TRANSACTION_closeSystemDialogs = 95;
        static final int TRANSACTION_convertFromTranslucent = 175;
        static final int TRANSACTION_convertToTranslucent = 176;
        static final int TRANSACTION_crashApplication = 112;
        static final int TRANSACTION_createStackOnDisplay = 221;
        static final int TRANSACTION_dismissKeyguard = 290;
        static final int TRANSACTION_dismissPip = 247;
        static final int TRANSACTION_dismissSplitScreenMode = 246;
        static final int TRANSACTION_dumpHeap = 118;
        static final int TRANSACTION_dumpHeapFinished = 227;
        static final int TRANSACTION_enterPictureInPictureMode = 256;
        static final int TRANSACTION_enterSafeMode = 64;
        static final int TRANSACTION_exitFreeformMode = 243;
        static final int TRANSACTION_finishActivity = 8;
        static final int TRANSACTION_finishActivityAffinity = 146;
        static final int TRANSACTION_finishHeavyWeightApp = 107;
        static final int TRANSACTION_finishInstrumentation = 41;
        static final int TRANSACTION_finishReceiver = 13;
        static final int TRANSACTION_finishSubActivity = 28;
        static final int TRANSACTION_finishVoiceTask = 203;
        static final int TRANSACTION_forceStopPackage = 77;
        static final int TRANSACTION_getActivityClassForToken = 45;
        static final int TRANSACTION_getActivityDisplayId = 186;
        static final int TRANSACTION_getActivityOptions = 199;
        static final int TRANSACTION_getAllStackInfos = 171;
        static final int TRANSACTION_getAppTaskThumbnailSize = 210;
        static final int TRANSACTION_getAppTasks = 200;
        static final int TRANSACTION_getAssistContextExtras = 162;
        static final int TRANSACTION_getCallingActivity = 19;
        static final int TRANSACTION_getCallingPackage = 18;
        static final int TRANSACTION_getConfiguration = 42;
        static final int TRANSACTION_getContentProvider = 25;
        static final int TRANSACTION_getContentProviderExternal = 138;
        static final int TRANSACTION_getCurrentUser = 142;
        static final int TRANSACTION_getDeviceConfigurationInfo = 82;
        static final int TRANSACTION_getFilteredTasks = 21;
        static final int TRANSACTION_getFocusedStackInfo = 173;
        static final int TRANSACTION_getFrontActivityScreenCompatMode = 122;
        static final int TRANSACTION_getGrantedUriPermissions = 264;
        static final int TRANSACTION_getHwInnerService = 307;
        static final int TRANSACTION_getIntentForIntentSender = 161;
        static final int TRANSACTION_getIntentSender = 59;
        static final int TRANSACTION_getLastResumedActivityUserId = 298;
        static final int TRANSACTION_getLaunchedFromPackage = 164;
        static final int TRANSACTION_getLaunchedFromUid = 147;
        static final int TRANSACTION_getLockTaskModeState = 225;
        static final int TRANSACTION_getMaxNumPictureInPictureActions = 258;
        static final int TRANSACTION_getMemoryInfo = 74;
        static final int TRANSACTION_getMemoryTrimLevel = 276;
        static final int TRANSACTION_getMyMemoryState = 140;
        static final int TRANSACTION_getPackageAskScreenCompat = 126;
        static final int TRANSACTION_getPackageForIntentSender = 61;
        static final int TRANSACTION_getPackageForToken = 46;
        static final int TRANSACTION_getPackageProcessState = 232;
        static final int TRANSACTION_getPackageScreenCompatMode = 124;
        static final int TRANSACTION_getPersistedUriPermissions = 183;
        static final int TRANSACTION_getProcessLimit = 48;
        static final int TRANSACTION_getProcessMemoryInfo = 96;
        static final int TRANSACTION_getProcessPss = 135;
        static final int TRANSACTION_getProcessesInErrorState = 75;
        static final int TRANSACTION_getProviderMimeType = 113;
        static final int TRANSACTION_getRecentTasks = 56;
        static final int TRANSACTION_getRequestedOrientation = 69;
        static final int TRANSACTION_getRunningAppProcesses = 81;
        static final int TRANSACTION_getRunningExternalApplications = 106;
        static final int TRANSACTION_getRunningServiceControlPanel = 29;
        static final int TRANSACTION_getRunningUserIds = 155;
        static final int TRANSACTION_getServices = 79;
        static final int TRANSACTION_getStackInfo = 174;
        static final int TRANSACTION_getTagForIntentSender = 188;
        static final int TRANSACTION_getTaskBounds = 185;
        static final int TRANSACTION_getTaskDescription = 80;
        static final int TRANSACTION_getTaskDescriptionIcon = 214;
        static final int TRANSACTION_getTaskForActivity = 24;
        static final int TRANSACTION_getTaskSnapshot = 293;
        static final int TRANSACTION_getTasks = 20;
        static final int TRANSACTION_getUidForIntentSender = 91;
        static final int TRANSACTION_getUidProcessState = 236;
        static final int TRANSACTION_getUriPermissionOwnerForActivity = 260;
        static final int TRANSACTION_grantUriPermission = 51;
        static final int TRANSACTION_grantUriPermissionFromOwner = 115;
        static final int TRANSACTION_handleApplicationCrash = 5;
        static final int TRANSACTION_handleApplicationStrictModeViolation = 108;
        static final int TRANSACTION_handleApplicationWtf = 100;
        static final int TRANSACTION_handleIncomingUser = 92;
        static final int TRANSACTION_hang = 167;
        static final int TRANSACTION_inputDispatchingTimedOut = 159;
        static final int TRANSACTION_isAppForeground = 266;
        static final int TRANSACTION_isAppStartModeDisabled = 251;
        static final int TRANSACTION_isAssistDataAllowedOnCurrentActivity = 237;
        static final int TRANSACTION_isBackgroundRestricted = 283;
        static final int TRANSACTION_isImmersive = 109;
        static final int TRANSACTION_isInLockTaskMode = 192;
        static final int TRANSACTION_isInMultiWindowMode = 253;
        static final int TRANSACTION_isInPictureInPictureMode = 254;
        static final int TRANSACTION_isIntentSenderAForegroundService = 150;
        static final int TRANSACTION_isIntentSenderAnActivity = 149;
        static final int TRANSACTION_isIntentSenderTargetedToPackage = 133;
        static final int TRANSACTION_isRootVoiceInteraction = 239;
        static final int TRANSACTION_isTopActivityImmersive = 111;
        static final int TRANSACTION_isTopOfTask = 204;
        static final int TRANSACTION_isUidActive = 4;
        static final int TRANSACTION_isUserAMonkey = 102;
        static final int TRANSACTION_isUserRunning = 120;
        static final int TRANSACTION_isVrModePackageEnabled = 278;
        static final int TRANSACTION_keyguardGoingAway = 235;
        static final int TRANSACTION_killAllBackgroundProcesses = 137;
        static final int TRANSACTION_killApplication = 94;
        static final int TRANSACTION_killApplicationProcess = 97;
        static final int TRANSACTION_killBackgroundProcesses = 101;
        static final int TRANSACTION_killPackageDependents = 255;
        static final int TRANSACTION_killPids = 78;
        static final int TRANSACTION_killProcessesBelowForeground = 141;
        static final int TRANSACTION_killUid = 165;
        static final int TRANSACTION_launchAssistIntent = 215;
        static final int TRANSACTION_makePackageIdle = 275;
        static final int TRANSACTION_moveActivityTaskToBack = 73;
        static final int TRANSACTION_moveStackToDisplay = 288;
        static final int TRANSACTION_moveTaskBackwards = 23;
        static final int TRANSACTION_moveTaskToFront = 22;
        static final int TRANSACTION_moveTaskToStack = 169;
        static final int TRANSACTION_moveTasksToFullscreenStack = 249;
        static final int TRANSACTION_moveTopActivityToPinnedStack = 250;
        static final int TRANSACTION_navigateUpTo = 144;
        static final int TRANSACTION_newUriPermissionOwner = 114;
        static final int TRANSACTION_noteAlarmFinish = 231;
        static final int TRANSACTION_noteAlarmStart = 230;
        static final int TRANSACTION_noteWakeupAlarm = 66;
        static final int TRANSACTION_notifyActivityDrawn = 177;
        static final int TRANSACTION_notifyCleartextNetwork = 220;
        static final int TRANSACTION_notifyEnterAnimationComplete = 206;
        static final int TRANSACTION_notifyLaunchTaskBehindComplete = 205;
        static final int TRANSACTION_notifyLockedProfile = 279;
        static final int TRANSACTION_notifyPinnedStackAnimationEnded = 271;
        static final int TRANSACTION_notifyPinnedStackAnimationStarted = 270;
        static final int TRANSACTION_openContentUri = 1;
        static final int TRANSACTION_overridePendingTransition = 99;
        static final int TRANSACTION_peekService = 83;
        static final int TRANSACTION_performIdleMaintenance = 180;
        static final int TRANSACTION_positionTaskInStack = 242;
        static final int TRANSACTION_profileControl = 84;
        static final int TRANSACTION_publishContentProviders = 26;
        static final int TRANSACTION_publishService = 34;
        static final int TRANSACTION_refContentProvider = 27;
        static final int TRANSACTION_registerIntentSenderCancelListener = 62;
        static final int TRANSACTION_registerProcessObserver = 131;
        static final int TRANSACTION_registerReceiver = 9;
        static final int TRANSACTION_registerRemoteAnimationForNextActivityStart = 305;
        static final int TRANSACTION_registerRemoteAnimations = 304;
        static final int TRANSACTION_registerTaskStackListener = 218;
        static final int TRANSACTION_registerUidObserver = 2;
        static final int TRANSACTION_registerUserSwitchObserver = 153;
        static final int TRANSACTION_releaseActivityInstance = 211;
        static final int TRANSACTION_releasePersistableUriPermission = 182;
        static final int TRANSACTION_releaseSomeActivities = 212;
        static final int TRANSACTION_removeContentProvider = 67;
        static final int TRANSACTION_removeContentProviderExternal = 139;
        static final int TRANSACTION_removeStack = 272;
        static final int TRANSACTION_removeStacksInWindowingModes = 273;
        static final int TRANSACTION_removeStacksWithActivityTypes = 274;
        static final int TRANSACTION_removeTask = 130;
        static final int TRANSACTION_reportActivityFullyDrawn = 178;
        static final int TRANSACTION_reportAssistContextExtras = 163;
        static final int TRANSACTION_reportSizeConfigurations = 244;
        static final int TRANSACTION_requestAssistContextExtras = 223;
        static final int TRANSACTION_requestAutofillData = 289;
        static final int TRANSACTION_requestBugReport = 156;
        static final int TRANSACTION_requestTelephonyBugReport = 157;
        static final int TRANSACTION_requestWifiBugReport = 158;
        static final int TRANSACTION_resizeDockedStack = 261;
        static final int TRANSACTION_resizePinnedStack = 277;
        static final int TRANSACTION_resizeStack = 170;
        static final int TRANSACTION_resizeTask = 224;
        static final int TRANSACTION_restart = 179;
        static final int TRANSACTION_restartUserInBackground = 291;
        static final int TRANSACTION_resumeAppSwitches = 87;
        static final int TRANSACTION_revokeUriPermission = 52;
        static final int TRANSACTION_revokeUriPermissionFromOwner = 116;
        static final int TRANSACTION_scheduleApplicationInfoChanged = 294;
        static final int TRANSACTION_sendIdleJobTrigger = 281;
        static final int TRANSACTION_sendIntentSender = 282;
        static final int TRANSACTION_serviceDoneExecuting = 57;
        static final int TRANSACTION_setActivityController = 53;
        static final int TRANSACTION_setAgentApp = 37;
        static final int TRANSACTION_setAlwaysFinish = 38;
        static final int TRANSACTION_setDebugApp = 36;
        static final int TRANSACTION_setDisablePreviewScreenshots = 297;
        static final int TRANSACTION_setDumpHeapDebugLimit = 226;
        static final int TRANSACTION_setExitInfo = 209;
        static final int TRANSACTION_setFocusedStack = 172;
        static final int TRANSACTION_setFocusedTask = 129;
        static final int TRANSACTION_setFrontActivityScreenCompatMode = 123;
        static final int TRANSACTION_setHasTopUi = 286;
        static final int TRANSACTION_setImmersive = 110;
        static final int TRANSACTION_setLockScreenShown = 145;
        static final int TRANSACTION_setPackageAskScreenCompat = 127;
        static final int TRANSACTION_setPackageScreenCompatMode = 125;
        static final int TRANSACTION_setPersistentVrThread = 295;
        static final int TRANSACTION_setPictureInPictureParams = 257;
        static final int TRANSACTION_setProcessImportant = 71;
        static final int TRANSACTION_setProcessLimit = 47;
        static final int TRANSACTION_setProcessMemoryTrimLevel = 187;
        static final int TRANSACTION_setRenderThread = 285;
        static final int TRANSACTION_setRequestedOrientation = 68;
        static final int TRANSACTION_setServiceForeground = 72;
        static final int TRANSACTION_setShowWhenLocked = 301;
        static final int TRANSACTION_setSplitScreenResizing = 262;
        static final int TRANSACTION_setTaskDescription = 193;
        static final int TRANSACTION_setTaskResizeable = 222;
        static final int TRANSACTION_setTaskWindowingMode = 168;
        static final int TRANSACTION_setTaskWindowingModeSplitScreenPrimary = 245;
        static final int TRANSACTION_setTurnScreenOn = 302;
        static final int TRANSACTION_setUserIsMonkey = 166;
        static final int TRANSACTION_setVoiceKeepAwake = 228;
        static final int TRANSACTION_setVrMode = 263;
        static final int TRANSACTION_setVrThread = 284;
        static final int TRANSACTION_shouldUpRecreateTask = 143;
        static final int TRANSACTION_showAssistFromActivity = 238;
        static final int TRANSACTION_showBootMessage = 136;
        static final int TRANSACTION_showLockTaskEscapeMessage = 233;
        static final int TRANSACTION_showWaitingForDebugger = 54;
        static final int TRANSACTION_shutdown = 85;
        static final int TRANSACTION_signalPersistentProcesses = 55;
        static final int TRANSACTION_startActivities = 119;
        static final int TRANSACTION_startActivity = 6;
        static final int TRANSACTION_startActivityAndWait = 103;
        static final int TRANSACTION_startActivityAsCaller = 207;
        static final int TRANSACTION_startActivityAsUser = 151;
        static final int TRANSACTION_startActivityFromRecents = 198;
        static final int TRANSACTION_startActivityIntentSender = 98;
        static final int TRANSACTION_startActivityWithConfig = 105;
        static final int TRANSACTION_startAssistantActivity = 195;
        static final int TRANSACTION_startBinderTracking = 240;
        static final int TRANSACTION_startConfirmDeviceCredentialIntent = 280;
        static final int TRANSACTION_startInPlaceAnimationOnFrontMostApplication = 216;
        static final int TRANSACTION_startInstrumentation = 39;
        static final int TRANSACTION_startLocalVoiceInteraction = 267;
        static final int TRANSACTION_startLockTaskModeByToken = 190;
        static final int TRANSACTION_startNextMatchingActivity = 65;
        static final int TRANSACTION_startRecentsActivity = 196;
        static final int TRANSACTION_startService = 30;
        static final int TRANSACTION_startSystemLockTaskMode = 201;
        static final int TRANSACTION_startUserInBackground = 189;
        static final int TRANSACTION_startUserInBackgroundWithListener = 303;
        static final int TRANSACTION_startVoiceActivity = 194;
        static final int TRANSACTION_stopAppSwitches = 86;
        static final int TRANSACTION_stopBinderTrackingAndDump = 241;
        static final int TRANSACTION_stopLocalVoiceInteraction = 268;
        static final int TRANSACTION_stopLockTaskModeByToken = 191;
        static final int TRANSACTION_stopService = 31;
        static final int TRANSACTION_stopServiceToken = 44;
        static final int TRANSACTION_stopSystemLockTaskMode = 202;
        static final int TRANSACTION_stopUser = 152;
        static final int TRANSACTION_supportsLocalVoiceInteraction = 269;
        static final int TRANSACTION_suppressResizeConfigChanges = 248;
        static final int TRANSACTION_swapDockedAndFullscreenStack = 308;
        static final int TRANSACTION_switchUser = 128;
        static final int TRANSACTION_takePersistableUriPermission = 181;
        static final int TRANSACTION_unbindBackupAgent = 90;
        static final int TRANSACTION_unbindFinished = 70;
        static final int TRANSACTION_unbindService = 33;
        static final int TRANSACTION_unbroadcastIntent = 12;
        static final int TRANSACTION_unhandledBack = 7;
        static final int TRANSACTION_unlockUser = 252;
        static final int TRANSACTION_unregisterIntentSenderCancelListener = 63;
        static final int TRANSACTION_unregisterProcessObserver = 132;
        static final int TRANSACTION_unregisterReceiver = 10;
        static final int TRANSACTION_unregisterTaskStackListener = 219;
        static final int TRANSACTION_unregisterUidObserver = 3;
        static final int TRANSACTION_unregisterUserSwitchObserver = 154;
        static final int TRANSACTION_unstableProviderDied = 148;
        static final int TRANSACTION_updateConfiguration = 43;
        static final int TRANSACTION_updateDeviceOwner = 234;
        static final int TRANSACTION_updateDisplayOverrideConfiguration = 287;
        static final int TRANSACTION_updateLockTaskFeatures = 300;
        static final int TRANSACTION_updateLockTaskPackages = 229;
        static final int TRANSACTION_updatePersistentConfiguration = 134;
        static final int TRANSACTION_waitForNetworkStateUpdate = 296;
        static final int TRANSACTION_willActivityBeVisible = 104;

        private static class Proxy implements IActivityManager {
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

            public ParcelFileDescriptor openContentUri(String uriString) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uriString);
                    this.mRemote.transact(1, _data, _reply, 0);
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

            public void registerUidObserver(IUidObserver observer, int which, int cutpoint, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(which);
                    _data.writeInt(cutpoint);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterUidObserver(IUidObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUidActive(int uid, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
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

            public void handleApplicationCrash(IBinder app, ApplicationErrorReport.ParcelableCrashInfo crashInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(app);
                    if (crashInfo != null) {
                        _data.writeInt(1);
                        crashInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startActivity(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(callingPackage);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeStrongBinder(resultTo);
                    _data.writeString(resultWho);
                    _data.writeInt(requestCode);
                    _data.writeInt(flags);
                    if (profilerInfo != null) {
                        _data.writeInt(1);
                        profilerInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unhandledBack() throws RemoteException {
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

            public boolean finishActivity(IBinder token, int code, Intent data, int finishTask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(code);
                    boolean _result = true;
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(finishTask);
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

            public Intent registerReceiver(IApplicationThread caller, String callerPackage, IIntentReceiver receiver, IntentFilter filter, String requiredPermission, int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    Intent _result = null;
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(callerPackage);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (filter != null) {
                        _data.writeInt(1);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(requiredPermission);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Intent.CREATOR.createFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterReceiver(IIntentReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int broadcastIntent(IApplicationThread caller, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle map, String[] requiredPermissions, int appOp, Bundle options, boolean serialized, boolean sticky, int userId) throws RemoteException {
                Intent intent2 = intent;
                Bundle bundle = map;
                Bundle bundle2 = options;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (intent2 != null) {
                        _data.writeInt(1);
                        intent2.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeString(resolvedType);
                        if (resultTo != null) {
                            iBinder = resultTo.asBinder();
                        }
                        _data.writeStrongBinder(iBinder);
                        try {
                            _data.writeInt(resultCode);
                        } catch (Throwable th) {
                            th = th;
                            String str = resultData;
                            String[] strArr = requiredPermissions;
                            int i = appOp;
                            boolean z = serialized;
                            boolean z2 = sticky;
                            int i2 = userId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(resultData);
                            if (bundle != null) {
                                _data.writeInt(1);
                                bundle.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                _data.writeStringArray(requiredPermissions);
                                try {
                                    _data.writeInt(appOp);
                                    if (bundle2 != null) {
                                        _data.writeInt(1);
                                        bundle2.writeToParcel(_data, 0);
                                    } else {
                                        _data.writeInt(0);
                                    }
                                    try {
                                        _data.writeInt(serialized ? 1 : 0);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        boolean z22 = sticky;
                                        int i22 = userId;
                                        _reply.recycle();
                                        _data.recycle();
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    boolean z3 = serialized;
                                    boolean z222 = sticky;
                                    int i222 = userId;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                int i3 = appOp;
                                boolean z32 = serialized;
                                boolean z2222 = sticky;
                                int i2222 = userId;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            String[] strArr2 = requiredPermissions;
                            int i32 = appOp;
                            boolean z322 = serialized;
                            boolean z22222 = sticky;
                            int i22222 = userId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        int i4 = resultCode;
                        String str2 = resultData;
                        String[] strArr22 = requiredPermissions;
                        int i322 = appOp;
                        boolean z3222 = serialized;
                        boolean z222222 = sticky;
                        int i222222 = userId;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(sticky ? 1 : 0);
                        try {
                            _data.writeInt(userId);
                            this.mRemote.transact(11, _data, _reply, 0);
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        } catch (Throwable th7) {
                            th = th7;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        int i2222222 = userId;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    String str3 = resolvedType;
                    int i42 = resultCode;
                    String str22 = resultData;
                    String[] strArr222 = requiredPermissions;
                    int i3222 = appOp;
                    boolean z32222 = serialized;
                    boolean z2222222 = sticky;
                    int i22222222 = userId;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public void unbroadcastIntent(IApplicationThread caller, Intent intent, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishReceiver(IBinder who, int resultCode, String resultData, Bundle map, boolean abortBroadcast, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(who);
                    _data.writeInt(resultCode);
                    _data.writeString(resultData);
                    if (map != null) {
                        _data.writeInt(1);
                        map.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(abortBroadcast);
                    _data.writeInt(flags);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void attachApplication(IApplicationThread app, long startSeq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(app != null ? app.asBinder() : null);
                    _data.writeLong(startSeq);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void activityIdle(IBinder token, Configuration config, boolean stopProfiling) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(stopProfiling);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void activityPaused(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void activityStopped(IBinder token, Bundle state, PersistableBundle persistentState, CharSequence description) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (state != null) {
                        _data.writeInt(1);
                        state.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (persistentState != null) {
                        _data.writeInt(1);
                        persistentState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (description != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(description, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public String getCallingPackage(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getCallingActivity(IBinder token) throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ActivityManager.RunningTaskInfo> getTasks(int maxNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxNum);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ActivityManager.RunningTaskInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ActivityManager.RunningTaskInfo> getFilteredTasks(int maxNum, int ignoreActivityType, int ignoreWindowingMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxNum);
                    _data.writeInt(ignoreActivityType);
                    _data.writeInt(ignoreWindowingMode);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ActivityManager.RunningTaskInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void moveTaskToFront(int task, int flags, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(task);
                    _data.writeInt(flags);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void moveTaskBackwards(int task) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(task);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTaskForActivity(IBinder token, boolean onlyRoot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(onlyRoot);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ContentProviderHolder getContentProvider(IApplicationThread caller, String name, int userId, boolean stable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    ContentProviderHolder _result = null;
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(name);
                    _data.writeInt(userId);
                    _data.writeInt(stable);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ContentProviderHolder.CREATOR.createFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void publishContentProviders(IApplicationThread caller, List<ContentProviderHolder> providers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeTypedList(providers);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean refContentProvider(IBinder connection, int stableDelta, int unstableDelta) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection);
                    _data.writeInt(stableDelta);
                    _data.writeInt(unstableDelta);
                    boolean _result = false;
                    this.mRemote.transact(27, _data, _reply, 0);
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

            public void finishSubActivity(IBinder token, String resultWho, int requestCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(resultWho);
                    _data.writeInt(requestCode);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PendingIntent getRunningServiceControlPanel(ComponentName service) throws RemoteException {
                PendingIntent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PendingIntent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName startService(IApplicationThread caller, Intent service, String resolvedType, boolean requireForeground, String callingPackage, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    ComponentName _result = null;
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(requireForeground);
                    _data.writeString(callingPackage);
                    _data.writeInt(userId);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int stopService(IApplicationThread caller, Intent service, String resolvedType, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(userId);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int bindService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String callingPackage, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeStrongBinder(token);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    if (connection != null) {
                        iBinder = connection.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(flags);
                    _data.writeString(callingPackage);
                    _data.writeInt(userId);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unbindService(IServiceConnection connection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection != null ? connection.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(33, _data, _reply, 0);
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

            public void publishService(IBinder token, Intent intent, IBinder service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(service);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void activityResumed(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDebugApp(String packageName, boolean waitForDebugger, boolean persistent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(waitForDebugger);
                    _data.writeInt(persistent);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAgentApp(String packageName, String agent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(agent);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAlwaysFinish(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startInstrumentation(ComponentName className, String profileFile, int flags, Bundle arguments, IInstrumentationWatcher watcher, IUiAutomationConnection connection, int userId, String abiOverride) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(profileFile);
                    _data.writeInt(flags);
                    if (arguments != null) {
                        _data.writeInt(1);
                        arguments.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    IBinder iBinder = null;
                    _data.writeStrongBinder(watcher != null ? watcher.asBinder() : null);
                    if (connection != null) {
                        iBinder = connection.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    _data.writeString(abiOverride);
                    this.mRemote.transact(39, _data, _reply, 0);
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

            public void addInstrumentationResults(IApplicationThread target, Bundle results) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(target != null ? target.asBinder() : null);
                    if (results != null) {
                        _data.writeInt(1);
                        results.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishInstrumentation(IApplicationThread target, int resultCode, Bundle results) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(target != null ? target.asBinder() : null);
                    _data.writeInt(resultCode);
                    if (results != null) {
                        _data.writeInt(1);
                        results.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Configuration getConfiguration() throws RemoteException {
                Configuration _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Configuration.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateConfiguration(Configuration values) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (values != null) {
                        _data.writeInt(1);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(43, _data, _reply, 0);
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

            public boolean stopServiceToken(ComponentName className, IBinder token, int startId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(token);
                    _data.writeInt(startId);
                    this.mRemote.transact(44, _data, _reply, 0);
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

            public ComponentName getActivityClassForToken(IBinder token) throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getPackageForToken(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setProcessLimit(int max) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(max);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getProcessLimit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkPermission(String permission, int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkUriPermission(Uri uri, int pid, int uid, int mode, int userId, IBinder callerToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(mode);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(callerToken);
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void grantUriPermission(IApplicationThread caller, String targetPkg, Uri uri, int mode, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(targetPkg);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    _data.writeInt(userId);
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void revokeUriPermission(IApplicationThread caller, String targetPkg, Uri uri, int mode, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(targetPkg);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    _data.writeInt(userId);
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setActivityController(IActivityController watcher, boolean imAMonkey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(watcher != null ? watcher.asBinder() : null);
                    _data.writeInt(imAMonkey);
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showWaitingForDebugger(IApplicationThread who, boolean waiting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(who != null ? who.asBinder() : null);
                    _data.writeInt(waiting);
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void signalPersistentProcesses(int signal) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(signal);
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getRecentTasks(int maxNum, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxNum);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    this.mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void serviceDoneExecuting(IBinder token, int type, int startId, int res) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(type);
                    _data.writeInt(startId);
                    _data.writeInt(res);
                    this.mRemote.transact(57, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void activityDestroyed(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(58, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public IIntentSender getIntentSender(int type, String packageName, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle options, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(token);
                    _data.writeString(resultWho);
                    _data.writeInt(requestCode);
                    _data.writeTypedArray(intents, 0);
                    _data.writeStringArray(resolvedTypes);
                    _data.writeInt(flags);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                    return IIntentSender.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelIntentSender(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getPackageForIntentSender(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerIntentSenderCancelListener(IIntentSender sender, IResultReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (receiver != null) {
                        iBinder = receiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(62, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterIntentSenderCancelListener(IIntentSender sender, IResultReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (receiver != null) {
                        iBinder = receiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(63, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enterSafeMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startNextMatchingActivity(IBinder callingActivity, Intent intent, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callingActivity);
                    boolean _result = true;
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(65, _data, _reply, 0);
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

            public void noteWakeupAlarm(IIntentSender sender, WorkSource workSource, int sourceUid, String sourcePkg, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (workSource != null) {
                        _data.writeInt(1);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sourceUid);
                    _data.writeString(sourcePkg);
                    _data.writeString(tag);
                    this.mRemote.transact(66, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeContentProvider(IBinder connection, boolean stable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection);
                    _data.writeInt(stable);
                    this.mRemote.transact(67, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRequestedOrientation(IBinder token, int requestedOrientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(requestedOrientation);
                    this.mRemote.transact(68, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRequestedOrientation(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(69, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unbindFinished(IBinder token, Intent service, boolean doRebind) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(doRebind);
                    this.mRemote.transact(70, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setProcessImportant(IBinder token, int pid, boolean isForeground, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(pid);
                    _data.writeInt(isForeground);
                    _data.writeString(reason);
                    this.mRemote.transact(71, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(token);
                    _data.writeInt(id);
                    if (notification != null) {
                        _data.writeInt(1);
                        notification.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(72, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(nonRoot);
                    boolean _result = false;
                    this.mRemote.transact(73, _data, _reply, 0);
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

            public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(74, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        outInfo.readFromParcel(_reply);
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(75, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ActivityManager.ProcessErrorStateInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean clearApplicationUserData(String packageName, boolean keepState, IPackageDataObserver observer, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(keepState);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(76, _data, _reply, 0);
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

            public void forceStopPackage(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(77, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean killPids(int[] pids, String reason, boolean secure) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(pids);
                    _data.writeString(reason);
                    _data.writeInt(secure);
                    boolean _result = false;
                    this.mRemote.transact(78, _data, _reply, 0);
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

            public List<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxNum);
                    _data.writeInt(flags);
                    this.mRemote.transact(79, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ActivityManager.RunningServiceInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ActivityManager.TaskDescription getTaskDescription(int taskId) throws RemoteException {
                ActivityManager.TaskDescription _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(80, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityManager.TaskDescription.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(81, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ActivityManager.RunningAppProcessInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ConfigurationInfo getDeviceConfigurationInfo() throws RemoteException {
                ConfigurationInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(82, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ConfigurationInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder peekService(Intent service, String resolvedType, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(83, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readStrongBinder();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean profileControl(String process, int userId, boolean start, ProfilerInfo profilerInfo, int profileType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(process);
                    _data.writeInt(userId);
                    _data.writeInt(start);
                    boolean _result = true;
                    if (profilerInfo != null) {
                        _data.writeInt(1);
                        profilerInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(profileType);
                    this.mRemote.transact(84, _data, _reply, 0);
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

            public boolean shutdown(int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(timeout);
                    boolean _result = false;
                    this.mRemote.transact(85, _data, _reply, 0);
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

            public void stopAppSwitches() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(86, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resumeAppSwitches() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(87, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean bindBackupAgent(String packageName, int backupRestoreMode, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(backupRestoreMode);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(88, _data, _reply, 0);
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

            public void backupAgentCreated(String packageName, IBinder agent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(agent);
                    this.mRemote.transact(89, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unbindBackupAgent(ApplicationInfo appInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (appInfo != null) {
                        _data.writeInt(1);
                        appInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(90, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUidForIntentSender(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    this.mRemote.transact(91, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll, boolean requireFull, String name, String callerPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callingPid);
                    _data.writeInt(callingUid);
                    _data.writeInt(userId);
                    _data.writeInt(allowAll);
                    _data.writeInt(requireFull);
                    _data.writeString(name);
                    _data.writeString(callerPackage);
                    this.mRemote.transact(92, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPackageDependency(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(93, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void killApplication(String pkg, int appId, int userId, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(appId);
                    _data.writeInt(userId);
                    _data.writeString(reason);
                    this.mRemote.transact(94, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void closeSystemDialogs(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    this.mRemote.transact(95, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(pids);
                    this.mRemote.transact(96, _data, _reply, 0);
                    _reply.readException();
                    return (Debug.MemoryInfo[]) _reply.createTypedArray(Debug.MemoryInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void killApplicationProcess(String processName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    this.mRemote.transact(97, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startActivityIntentSender(IApplicationThread caller, IIntentSender target, IBinder whitelistToken, Intent fillInIntent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flagsMask, int flagsValues, Bundle options) throws RemoteException {
                Intent intent = fillInIntent;
                Bundle bundle = options;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (target != null) {
                        iBinder = target.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    try {
                        _data.writeStrongBinder(whitelistToken);
                        if (intent != null) {
                            _data.writeInt(1);
                            intent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeString(resolvedType);
                        } catch (Throwable th) {
                            th = th;
                            IBinder iBinder2 = resultTo;
                            String str = resultWho;
                            int i = requestCode;
                            int i2 = flagsMask;
                            int i3 = flagsValues;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        String str2 = resolvedType;
                        IBinder iBinder22 = resultTo;
                        String str3 = resultWho;
                        int i4 = requestCode;
                        int i22 = flagsMask;
                        int i32 = flagsValues;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStrongBinder(resultTo);
                        try {
                            _data.writeString(resultWho);
                            try {
                                _data.writeInt(requestCode);
                            } catch (Throwable th3) {
                                th = th3;
                                int i222 = flagsMask;
                                int i322 = flagsValues;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            int i42 = requestCode;
                            int i2222 = flagsMask;
                            int i3222 = flagsValues;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        String str32 = resultWho;
                        int i422 = requestCode;
                        int i22222 = flagsMask;
                        int i32222 = flagsValues;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(flagsMask);
                        try {
                            _data.writeInt(flagsValues);
                            if (bundle != null) {
                                _data.writeInt(1);
                                bundle.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                this.mRemote.transact(98, _data, _reply, 0);
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
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
                        int i322222 = flagsValues;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    IBinder iBinder3 = whitelistToken;
                    String str22 = resolvedType;
                    IBinder iBinder222 = resultTo;
                    String str322 = resultWho;
                    int i4222 = requestCode;
                    int i222222 = flagsMask;
                    int i3222222 = flagsValues;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public void overridePendingTransition(IBinder token, String packageName, int enterAnim, int exitAnim) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(packageName);
                    _data.writeInt(enterAnim);
                    _data.writeInt(exitAnim);
                    this.mRemote.transact(99, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean handleApplicationWtf(IBinder app, String tag, boolean system, ApplicationErrorReport.ParcelableCrashInfo crashInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(app);
                    _data.writeString(tag);
                    _data.writeInt(system);
                    boolean _result = true;
                    if (crashInfo != null) {
                        _data.writeInt(1);
                        crashInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(100, _data, _reply, 0);
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

            public void killBackgroundProcesses(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(101, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUserAMonkey() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(102, _data, _reply, 0);
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

            public WaitResult startActivityAndWait(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
                WaitResult _result;
                Intent intent2 = intent;
                ProfilerInfo profilerInfo2 = profilerInfo;
                Bundle bundle = options;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    try {
                        _data.writeString(callingPackage);
                        if (intent2 != null) {
                            _data.writeInt(1);
                            intent2.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeString(resolvedType);
                            try {
                                _data.writeStrongBinder(resultTo);
                                try {
                                    _data.writeString(resultWho);
                                } catch (Throwable th) {
                                    th = th;
                                    int i = requestCode;
                                    int i2 = flags;
                                    int i3 = userId;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                String str = resultWho;
                                int i4 = requestCode;
                                int i22 = flags;
                                int i32 = userId;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            IBinder iBinder = resultTo;
                            String str2 = resultWho;
                            int i42 = requestCode;
                            int i222 = flags;
                            int i322 = userId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(requestCode);
                            try {
                                _data.writeInt(flags);
                                if (profilerInfo2 != null) {
                                    _data.writeInt(1);
                                    profilerInfo2.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                                if (bundle != null) {
                                    _data.writeInt(1);
                                    bundle.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                int i3222 = userId;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                            try {
                                _data.writeInt(userId);
                                try {
                                    this.mRemote.transact(103, _data, _reply, 0);
                                    _reply.readException();
                                    if (_reply.readInt() != 0) {
                                        _result = WaitResult.CREATOR.createFromParcel(_reply);
                                    } else {
                                        _result = null;
                                    }
                                    WaitResult _result2 = _result;
                                    _reply.recycle();
                                    _data.recycle();
                                    return _result2;
                                } catch (Throwable th5) {
                                    th = th5;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            int i2222 = flags;
                            int i32222 = userId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        String str3 = resolvedType;
                        IBinder iBinder2 = resultTo;
                        String str22 = resultWho;
                        int i422 = requestCode;
                        int i22222 = flags;
                        int i322222 = userId;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    String str4 = callingPackage;
                    String str32 = resolvedType;
                    IBinder iBinder22 = resultTo;
                    String str222 = resultWho;
                    int i4222 = requestCode;
                    int i222222 = flags;
                    int i3222222 = userId;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public boolean willActivityBeVisible(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    this.mRemote.transact(104, _data, _reply, 0);
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

            public int startActivityWithConfig(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, Configuration newConfig, Bundle options, int userId) throws RemoteException {
                Intent intent2 = intent;
                Configuration configuration = newConfig;
                Bundle bundle = options;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    try {
                        _data.writeString(callingPackage);
                        if (intent2 != null) {
                            _data.writeInt(1);
                            intent2.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeString(resolvedType);
                            try {
                                _data.writeStrongBinder(resultTo);
                                try {
                                    _data.writeString(resultWho);
                                } catch (Throwable th) {
                                    th = th;
                                    int i = requestCode;
                                    int i2 = startFlags;
                                    int i3 = userId;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                String str = resultWho;
                                int i4 = requestCode;
                                int i22 = startFlags;
                                int i32 = userId;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            IBinder iBinder = resultTo;
                            String str2 = resultWho;
                            int i42 = requestCode;
                            int i222 = startFlags;
                            int i322 = userId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(requestCode);
                            try {
                                _data.writeInt(startFlags);
                                if (configuration != null) {
                                    _data.writeInt(1);
                                    configuration.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                                if (bundle != null) {
                                    _data.writeInt(1);
                                    bundle.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                int i3222 = userId;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                            try {
                                _data.writeInt(userId);
                                try {
                                    this.mRemote.transact(105, _data, _reply, 0);
                                    _reply.readException();
                                    int _result = _reply.readInt();
                                    _reply.recycle();
                                    _data.recycle();
                                    return _result;
                                } catch (Throwable th5) {
                                    th = th5;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            int i2222 = startFlags;
                            int i32222 = userId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        String str3 = resolvedType;
                        IBinder iBinder2 = resultTo;
                        String str22 = resultWho;
                        int i422 = requestCode;
                        int i22222 = startFlags;
                        int i322222 = userId;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    String str4 = callingPackage;
                    String str32 = resolvedType;
                    IBinder iBinder22 = resultTo;
                    String str222 = resultWho;
                    int i4222 = requestCode;
                    int i222222 = startFlags;
                    int i3222222 = userId;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public List<ApplicationInfo> getRunningExternalApplications() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(106, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ApplicationInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishHeavyWeightApp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(107, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void handleApplicationStrictModeViolation(IBinder app, int violationMask, StrictMode.ViolationInfo crashInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(app);
                    _data.writeInt(violationMask);
                    if (crashInfo != null) {
                        _data.writeInt(1);
                        crashInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(108, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isImmersive(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    this.mRemote.transact(109, _data, _reply, 0);
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

            public void setImmersive(IBinder token, boolean immersive) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(immersive);
                    this.mRemote.transact(110, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTopActivityImmersive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(111, _data, _reply, 0);
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

            public void crashApplication(int uid, int initialPid, String packageName, int userId, String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(initialPid);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeString(message);
                    this.mRemote.transact(112, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getProviderMimeType(Uri uri, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(113, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder newUriPermissionOwner(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(114, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readStrongBinder();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void grantUriPermissionFromOwner(IBinder owner, int fromUid, String targetPkg, Uri uri, int mode, int sourceUserId, int targetUserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(owner);
                    _data.writeInt(fromUid);
                    _data.writeString(targetPkg);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    _data.writeInt(sourceUserId);
                    _data.writeInt(targetUserId);
                    this.mRemote.transact(115, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void revokeUriPermissionFromOwner(IBinder owner, Uri uri, int mode, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(owner);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    _data.writeInt(userId);
                    this.mRemote.transact(116, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkGrantUriPermission(int callingUid, String targetPkg, Uri uri, int modeFlags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callingUid);
                    _data.writeString(targetPkg);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(modeFlags);
                    _data.writeInt(userId);
                    this.mRemote.transact(117, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean dumpHeap(String process, int userId, boolean managed, boolean mallocInfo, boolean runGc, String path, ParcelFileDescriptor fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(process);
                    _data.writeInt(userId);
                    _data.writeInt(managed);
                    _data.writeInt(mallocInfo);
                    _data.writeInt(runGc);
                    _data.writeString(path);
                    boolean _result = true;
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(118, _data, _reply, 0);
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

            public int startActivities(IApplicationThread caller, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle options, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(callingPackage);
                    _data.writeTypedArray(intents, 0);
                    _data.writeStringArray(resolvedTypes);
                    _data.writeStrongBinder(resultTo);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(119, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUserRunning(int userid, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    _data.writeInt(flags);
                    boolean _result = false;
                    this.mRemote.transact(120, _data, _reply, 0);
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

            public void activitySlept(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(121, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public int getFrontActivityScreenCompatMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(122, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFrontActivityScreenCompatMode(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    this.mRemote.transact(123, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPackageScreenCompatMode(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(124, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPackageScreenCompatMode(String packageName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(mode);
                    this.mRemote.transact(125, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getPackageAskScreenCompat(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(126, _data, _reply, 0);
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

            public void setPackageAskScreenCompat(String packageName, boolean ask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(ask);
                    this.mRemote.transact(127, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean switchUser(int userid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    boolean _result = false;
                    this.mRemote.transact(128, _data, _reply, 0);
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

            public void setFocusedTask(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(129, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean removeTask(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    boolean _result = false;
                    this.mRemote.transact(130, _data, _reply, 0);
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

            public void registerProcessObserver(IProcessObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(131, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterProcessObserver(IProcessObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(132, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isIntentSenderTargetedToPackage(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(133, _data, _reply, 0);
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

            public void updatePersistentConfiguration(Configuration values) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (values != null) {
                        _data.writeInt(1);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(134, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long[] getProcessPss(int[] pids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(pids);
                    this.mRemote.transact(135, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createLongArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showBootMessage(CharSequence msg, boolean always) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (msg != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(msg, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(always);
                    this.mRemote.transact(136, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void killAllBackgroundProcesses() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(137, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ContentProviderHolder getContentProviderExternal(String name, int userId, IBinder token) throws RemoteException {
                ContentProviderHolder _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(138, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ContentProviderHolder.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeContentProviderExternal(String name, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(139, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getMyMemoryState(ActivityManager.RunningAppProcessInfo outInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(140, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        outInfo.readFromParcel(_reply);
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean killProcessesBelowForeground(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    boolean _result = false;
                    this.mRemote.transact(141, _data, _reply, 0);
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

            public UserInfo getCurrentUser() throws RemoteException {
                UserInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(142, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UserInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean shouldUpRecreateTask(IBinder token, String destAffinity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(destAffinity);
                    boolean _result = false;
                    this.mRemote.transact(143, _data, _reply, 0);
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

            public boolean navigateUpTo(IBinder token, Intent target, int resultCode, Intent resultData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = true;
                    if (target != null) {
                        _data.writeInt(1);
                        target.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resultCode);
                    if (resultData != null) {
                        _data.writeInt(1);
                        resultData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(144, _data, _reply, 0);
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

            public void setLockScreenShown(boolean showingKeyguard, boolean showingAod, int secondaryDisplayShowing) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(showingKeyguard);
                    _data.writeInt(showingAod);
                    _data.writeInt(secondaryDisplayShowing);
                    this.mRemote.transact(145, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean finishActivityAffinity(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    this.mRemote.transact(146, _data, _reply, 0);
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

            public int getLaunchedFromUid(IBinder activityToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    this.mRemote.transact(147, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unstableProviderDied(IBinder connection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection);
                    this.mRemote.transact(148, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isIntentSenderAnActivity(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(149, _data, _reply, 0);
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

            public boolean isIntentSenderAForegroundService(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(150, _data, _reply, 0);
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

            public int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
                Intent intent2 = intent;
                ProfilerInfo profilerInfo2 = profilerInfo;
                Bundle bundle = options;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    try {
                        _data.writeString(callingPackage);
                        if (intent2 != null) {
                            _data.writeInt(1);
                            intent2.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeString(resolvedType);
                            try {
                                _data.writeStrongBinder(resultTo);
                                try {
                                    _data.writeString(resultWho);
                                } catch (Throwable th) {
                                    th = th;
                                    int i = requestCode;
                                    int i2 = flags;
                                    int i3 = userId;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                String str = resultWho;
                                int i4 = requestCode;
                                int i22 = flags;
                                int i32 = userId;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            IBinder iBinder = resultTo;
                            String str2 = resultWho;
                            int i42 = requestCode;
                            int i222 = flags;
                            int i322 = userId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(requestCode);
                            try {
                                _data.writeInt(flags);
                                if (profilerInfo2 != null) {
                                    _data.writeInt(1);
                                    profilerInfo2.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                                if (bundle != null) {
                                    _data.writeInt(1);
                                    bundle.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                int i3222 = userId;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                            try {
                                _data.writeInt(userId);
                                try {
                                    this.mRemote.transact(151, _data, _reply, 0);
                                    _reply.readException();
                                    int _result = _reply.readInt();
                                    _reply.recycle();
                                    _data.recycle();
                                    return _result;
                                } catch (Throwable th5) {
                                    th = th5;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            int i2222 = flags;
                            int i32222 = userId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        String str3 = resolvedType;
                        IBinder iBinder2 = resultTo;
                        String str22 = resultWho;
                        int i422 = requestCode;
                        int i22222 = flags;
                        int i322222 = userId;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    String str4 = callingPackage;
                    String str32 = resolvedType;
                    IBinder iBinder22 = resultTo;
                    String str222 = resultWho;
                    int i4222 = requestCode;
                    int i222222 = flags;
                    int i3222222 = userId;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int stopUser(int userid, boolean force, IStopUserCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    _data.writeInt(force);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(152, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerUserSwitchObserver(IUserSwitchObserver observer, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeString(name);
                    this.mRemote.transact(153, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterUserSwitchObserver(IUserSwitchObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(154, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getRunningUserIds() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(155, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestBugReport(int bugreportType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bugreportType);
                    this.mRemote.transact(156, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestTelephonyBugReport(String shareTitle, String shareDescription) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(shareTitle);
                    _data.writeString(shareDescription);
                    this.mRemote.transact(157, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestWifiBugReport(String shareTitle, String shareDescription) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(shareTitle);
                    _data.writeString(shareDescription);
                    this.mRemote.transact(158, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long inputDispatchingTimedOut(int pid, boolean aboveSystem, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(aboveSystem);
                    _data.writeString(reason);
                    this.mRemote.transact(159, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearPendingBackup() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(160, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Intent getIntentForIntentSender(IIntentSender sender) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    Intent _result = null;
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    this.mRemote.transact(161, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Intent.CREATOR.createFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getAssistContextExtras(int requestType) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestType);
                    this.mRemote.transact(162, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportAssistContextExtras(IBinder token, Bundle extras, AssistStructure structure, AssistContent content, Uri referrer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (structure != null) {
                        _data.writeInt(1);
                        structure.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (content != null) {
                        _data.writeInt(1);
                        content.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (referrer != null) {
                        _data.writeInt(1);
                        referrer.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(163, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getLaunchedFromPackage(IBinder activityToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    this.mRemote.transact(164, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void killUid(int appId, int userId, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appId);
                    _data.writeInt(userId);
                    _data.writeString(reason);
                    this.mRemote.transact(165, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUserIsMonkey(boolean monkey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(monkey);
                    this.mRemote.transact(166, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void hang(IBinder who, boolean allowRestart) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(who);
                    _data.writeInt(allowRestart);
                    this.mRemote.transact(167, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTaskWindowingMode(int taskId, int windowingMode, boolean toTop) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(windowingMode);
                    _data.writeInt(toTop);
                    this.mRemote.transact(168, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void moveTaskToStack(int taskId, int stackId, boolean toTop) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(stackId);
                    _data.writeInt(toTop);
                    this.mRemote.transact(169, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resizeStack(int stackId, Rect bounds, boolean allowResizeInDockedMode, boolean preserveWindows, boolean animate, int animationDuration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stackId);
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(allowResizeInDockedMode);
                    _data.writeInt(preserveWindows);
                    _data.writeInt(animate);
                    _data.writeInt(animationDuration);
                    this.mRemote.transact(170, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ActivityManager.StackInfo> getAllStackInfos() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(171, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ActivityManager.StackInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFocusedStack(int stackId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stackId);
                    this.mRemote.transact(172, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ActivityManager.StackInfo getFocusedStackInfo() throws RemoteException {
                ActivityManager.StackInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(173, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityManager.StackInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ActivityManager.StackInfo getStackInfo(int windowingMode, int activityType) throws RemoteException {
                ActivityManager.StackInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(windowingMode);
                    _data.writeInt(activityType);
                    this.mRemote.transact(174, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityManager.StackInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean convertFromTranslucent(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    this.mRemote.transact(175, _data, _reply, 0);
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

            public boolean convertToTranslucent(IBinder token, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = true;
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(176, _data, _reply, 0);
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

            public void notifyActivityDrawn(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(177, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportActivityFullyDrawn(IBinder token, boolean restoredFromBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(restoredFromBundle);
                    this.mRemote.transact(178, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restart() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(179, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void performIdleMaintenance() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(180, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void takePersistableUriPermission(Uri uri, int modeFlags, String toPackage, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(modeFlags);
                    _data.writeString(toPackage);
                    _data.writeInt(userId);
                    this.mRemote.transact(181, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void releasePersistableUriPermission(Uri uri, int modeFlags, String toPackage, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(modeFlags);
                    _data.writeString(toPackage);
                    _data.writeInt(userId);
                    this.mRemote.transact(182, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getPersistedUriPermissions(String packageName, boolean incoming) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(incoming);
                    this.mRemote.transact(183, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void appNotRespondingViaProvider(IBinder connection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection);
                    this.mRemote.transact(184, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Rect getTaskBounds(int taskId) throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(185, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getActivityDisplayId(IBinder activityToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    this.mRemote.transact(186, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setProcessMemoryTrimLevel(String process, int uid, int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(process);
                    _data.writeInt(uid);
                    _data.writeInt(level);
                    boolean _result = false;
                    this.mRemote.transact(187, _data, _reply, 0);
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

            public String getTagForIntentSender(IIntentSender sender, String prefix) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    _data.writeString(prefix);
                    this.mRemote.transact(188, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startUserInBackground(int userid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    boolean _result = false;
                    this.mRemote.transact(189, _data, _reply, 0);
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

            public void startLockTaskModeByToken(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(190, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopLockTaskModeByToken(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(191, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isInLockTaskMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(192, _data, _reply, 0);
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

            public void setTaskDescription(IBinder token, ActivityManager.TaskDescription values) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (values != null) {
                        _data.writeInt(1);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(193, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startVoiceActivity(String callingPackage, int callingPid, int callingUid, Intent intent, String resolvedType, IVoiceInteractionSession session, IVoiceInteractor interactor, int flags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
                Intent intent2 = intent;
                ProfilerInfo profilerInfo2 = profilerInfo;
                Bundle bundle = options;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(callingPackage);
                        try {
                            _data.writeInt(callingPid);
                        } catch (Throwable th) {
                            th = th;
                            int i = callingUid;
                            String str = resolvedType;
                            int i2 = flags;
                            int i3 = userId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        int i4 = callingPid;
                        int i5 = callingUid;
                        String str2 = resolvedType;
                        int i22 = flags;
                        int i32 = userId;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(callingUid);
                        if (intent2 != null) {
                            _data.writeInt(1);
                            intent2.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeString(resolvedType);
                            IBinder iBinder = null;
                            _data.writeStrongBinder(session != null ? session.asBinder() : null);
                            if (interactor != null) {
                                iBinder = interactor.asBinder();
                            }
                            _data.writeStrongBinder(iBinder);
                            try {
                                _data.writeInt(flags);
                                if (profilerInfo2 != null) {
                                    _data.writeInt(1);
                                    profilerInfo2.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                                if (bundle != null) {
                                    _data.writeInt(1);
                                    bundle.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                int i322 = userId;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                            try {
                                _data.writeInt(userId);
                            } catch (Throwable th4) {
                                th = th4;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            int i222 = flags;
                            int i3222 = userId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            this.mRemote.transact(194, _data, _reply, 0);
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        String str22 = resolvedType;
                        int i2222 = flags;
                        int i32222 = userId;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    String str3 = callingPackage;
                    int i42 = callingPid;
                    int i52 = callingUid;
                    String str222 = resolvedType;
                    int i22222 = flags;
                    int i322222 = userId;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int startAssistantActivity(String callingPackage, int callingPid, int callingUid, Intent intent, String resolvedType, Bundle options, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(callingPid);
                    _data.writeInt(callingUid);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(195, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startRecentsActivity(Intent intent, IAssistDataReceiver assistDataReceiver, IRecentsAnimationRunner recentsAnimationRunner) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    IBinder iBinder = null;
                    _data.writeStrongBinder(assistDataReceiver != null ? assistDataReceiver.asBinder() : null);
                    if (recentsAnimationRunner != null) {
                        iBinder = recentsAnimationRunner.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(196, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelRecentsAnimation(boolean restoreHomeStackPosition) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(restoreHomeStackPosition);
                    this.mRemote.transact(197, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startActivityFromRecents(int taskId, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(198, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getActivityOptions(IBinder token) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(199, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<IBinder> getAppTasks(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(200, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createBinderArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startSystemLockTaskMode(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(201, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopSystemLockTaskMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(202, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishVoiceTask(IVoiceInteractionSession session) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    this.mRemote.transact(203, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTopOfTask(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    this.mRemote.transact(204, _data, _reply, 0);
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

            public void notifyLaunchTaskBehindComplete(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(205, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyEnterAnimationComplete(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(206, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startActivityAsCaller(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options, boolean ignoreTargetSecurity, int userId) throws RemoteException {
                Intent intent2 = intent;
                ProfilerInfo profilerInfo2 = profilerInfo;
                Bundle bundle = options;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    try {
                        _data.writeString(callingPackage);
                        if (intent2 != null) {
                            _data.writeInt(1);
                            intent2.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeString(resolvedType);
                            try {
                                _data.writeStrongBinder(resultTo);
                                try {
                                    _data.writeString(resultWho);
                                } catch (Throwable th) {
                                    th = th;
                                    int i = requestCode;
                                    int i2 = flags;
                                    boolean z = ignoreTargetSecurity;
                                    int i3 = userId;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                String str = resultWho;
                                int i4 = requestCode;
                                int i22 = flags;
                                boolean z2 = ignoreTargetSecurity;
                                int i32 = userId;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            IBinder iBinder = resultTo;
                            String str2 = resultWho;
                            int i42 = requestCode;
                            int i222 = flags;
                            boolean z22 = ignoreTargetSecurity;
                            int i322 = userId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(requestCode);
                            try {
                                _data.writeInt(flags);
                                if (profilerInfo2 != null) {
                                    _data.writeInt(1);
                                    profilerInfo2.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                                if (bundle != null) {
                                    _data.writeInt(1);
                                    bundle.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                boolean z222 = ignoreTargetSecurity;
                                int i3222 = userId;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                            try {
                                _data.writeInt(ignoreTargetSecurity ? 1 : 0);
                                try {
                                    _data.writeInt(userId);
                                    try {
                                        this.mRemote.transact(207, _data, _reply, 0);
                                        _reply.readException();
                                        int _result = _reply.readInt();
                                        _reply.recycle();
                                        _data.recycle();
                                        return _result;
                                    } catch (Throwable th5) {
                                        th = th5;
                                        _reply.recycle();
                                        _data.recycle();
                                        throw th;
                                    }
                                } catch (Throwable th6) {
                                    th = th6;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th7) {
                                th = th7;
                                int i32222 = userId;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th8) {
                            th = th8;
                            int i2222 = flags;
                            boolean z2222 = ignoreTargetSecurity;
                            int i322222 = userId;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th9) {
                        th = th9;
                        String str3 = resolvedType;
                        IBinder iBinder2 = resultTo;
                        String str22 = resultWho;
                        int i422 = requestCode;
                        int i22222 = flags;
                        boolean z22222 = ignoreTargetSecurity;
                        int i3222222 = userId;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th10) {
                    th = th10;
                    String str4 = callingPackage;
                    String str32 = resolvedType;
                    IBinder iBinder22 = resultTo;
                    String str222 = resultWho;
                    int i4222 = requestCode;
                    int i222222 = flags;
                    boolean z222222 = ignoreTargetSecurity;
                    int i32222222 = userId;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public int addAppTask(IBinder activityToken, Intent intent, ActivityManager.TaskDescription description, Bitmap thumbnail) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (description != null) {
                        _data.writeInt(1);
                        description.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (thumbnail != null) {
                        _data.writeInt(1);
                        thumbnail.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(208, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setExitInfo(float pivotX, float pivotY, int iconWidth, int iconHeight, Bitmap iconBitmap, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(pivotX);
                    _data.writeFloat(pivotY);
                    _data.writeInt(iconWidth);
                    _data.writeInt(iconHeight);
                    if (iconBitmap != null) {
                        _data.writeInt(1);
                        iconBitmap.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flag);
                    this.mRemote.transact(209, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Point getAppTaskThumbnailSize() throws RemoteException {
                Point _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(210, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Point.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean releaseActivityInstance(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    this.mRemote.transact(211, _data, _reply, 0);
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

            public void releaseSomeActivities(IApplicationThread app) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(app != null ? app.asBinder() : null);
                    this.mRemote.transact(212, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void bootAnimationComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(213, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bitmap getTaskDescriptionIcon(String filename, int userId) throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filename);
                    _data.writeInt(userId);
                    this.mRemote.transact(214, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean launchAssistIntent(Intent intent, int requestType, String hint, int userHandle, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(requestType);
                    _data.writeString(hint);
                    _data.writeInt(userHandle);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(215, _data, _reply, 0);
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

            public void startInPlaceAnimationOnFrontMostApplication(Bundle opts) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (opts != null) {
                        _data.writeInt(1);
                        opts.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(216, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkPermissionWithToken(String permission, int pid, int uid, IBinder callerToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(callerToken);
                    this.mRemote.transact(217, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerTaskStackListener(ITaskStackListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(218, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterTaskStackListener(ITaskStackListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(219, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyCleartextNetwork(int uid, byte[] firstPacket) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeByteArray(firstPacket);
                    this.mRemote.transact(220, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createStackOnDisplay(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(221, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTaskResizeable(int taskId, int resizeableMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(resizeableMode);
                    this.mRemote.transact(222, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean requestAssistContextExtras(int requestType, IAssistDataReceiver receiver, Bundle receiverExtras, IBinder activityToken, boolean focused, boolean newSessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestType);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    boolean _result = true;
                    if (receiverExtras != null) {
                        _data.writeInt(1);
                        receiverExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(activityToken);
                    _data.writeInt(focused);
                    _data.writeInt(newSessionId);
                    this.mRemote.transact(223, _data, _reply, 0);
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

            public void resizeTask(int taskId, Rect bounds, int resizeMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resizeMode);
                    this.mRemote.transact(224, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLockTaskModeState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(225, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDumpHeapDebugLimit(String processName, int uid, long maxMemSize, String reportPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    _data.writeLong(maxMemSize);
                    _data.writeString(reportPackage);
                    this.mRemote.transact(226, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dumpHeapFinished(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    this.mRemote.transact(227, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVoiceKeepAwake(IVoiceInteractionSession session, boolean keepAwake) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    _data.writeInt(keepAwake);
                    this.mRemote.transact(228, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateLockTaskPackages(int userId, String[] packages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeStringArray(packages);
                    this.mRemote.transact(229, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteAlarmStart(IIntentSender sender, WorkSource workSource, int sourceUid, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (workSource != null) {
                        _data.writeInt(1);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sourceUid);
                    _data.writeString(tag);
                    this.mRemote.transact(230, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void noteAlarmFinish(IIntentSender sender, WorkSource workSource, int sourceUid, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sender != null ? sender.asBinder() : null);
                    if (workSource != null) {
                        _data.writeInt(1);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sourceUid);
                    _data.writeString(tag);
                    this.mRemote.transact(231, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPackageProcessState(String packageName, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(232, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showLockTaskEscapeMessage(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(233, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updateDeviceOwner(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(234, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void keyguardGoingAway(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    this.mRemote.transact(235, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUidProcessState(int uid, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(236, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAssistDataAllowedOnCurrentActivity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(237, _data, _reply, 0);
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

            public boolean showAssistFromActivity(IBinder token, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = true;
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(238, _data, _reply, 0);
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

            public boolean isRootVoiceInteraction(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    this.mRemote.transact(239, _data, _reply, 0);
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

            public boolean startBinderTracking() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(240, _data, _reply, 0);
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

            public boolean stopBinderTrackingAndDump(ParcelFileDescriptor fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(241, _data, _reply, 0);
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

            public void positionTaskInStack(int taskId, int stackId, int position) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(stackId);
                    _data.writeInt(position);
                    this.mRemote.transact(242, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void exitFreeformMode(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(243, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportSizeConfigurations(IBinder token, int[] horizontalSizeConfiguration, int[] verticalSizeConfigurations, int[] smallestWidthConfigurations) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeIntArray(horizontalSizeConfiguration);
                    _data.writeIntArray(verticalSizeConfigurations);
                    _data.writeIntArray(smallestWidthConfigurations);
                    this.mRemote.transact(244, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setTaskWindowingModeSplitScreenPrimary(int taskId, int createMode, boolean toTop, boolean animate, Rect initialBounds, boolean showRecents) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(createMode);
                    _data.writeInt(toTop);
                    _data.writeInt(animate);
                    boolean _result = true;
                    if (initialBounds != null) {
                        _data.writeInt(1);
                        initialBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(showRecents);
                    this.mRemote.transact(245, _data, _reply, 0);
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

            public void dismissSplitScreenMode(boolean toTop) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(toTop);
                    this.mRemote.transact(246, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dismissPip(boolean animate, int animationDuration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(animate);
                    _data.writeInt(animationDuration);
                    this.mRemote.transact(247, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void suppressResizeConfigChanges(boolean suppress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(suppress);
                    this.mRemote.transact(248, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void moveTasksToFullscreenStack(int fromStackId, boolean onTop) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fromStackId);
                    _data.writeInt(onTop);
                    this.mRemote.transact(249, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean moveTopActivityToPinnedStack(int stackId, Rect bounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stackId);
                    boolean _result = true;
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(250, _data, _reply, 0);
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

            public boolean isAppStartModeDisabled(int uid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(251, _data, _reply, 0);
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

            public boolean unlockUser(int userid, byte[] token, byte[] secret, IProgressListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    _data.writeByteArray(token);
                    _data.writeByteArray(secret);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(252, _data, _reply, 0);
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

            public boolean isInMultiWindowMode(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    this.mRemote.transact(253, _data, _reply, 0);
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

            public boolean isInPictureInPictureMode(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    this.mRemote.transact(254, _data, _reply, 0);
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

            public void killPackageDependents(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(255, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enterPictureInPictureMode(IBinder token, PictureInPictureParams params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = true;
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(256, _data, _reply, 0);
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

            public void setPictureInPictureParams(IBinder token, PictureInPictureParams params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(257, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMaxNumPictureInPictureActions(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(258, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void activityRelaunched(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(259, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getUriPermissionOwnerForActivity(IBinder activityToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    this.mRemote.transact(260, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readStrongBinder();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resizeDockedStack(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dockedBounds != null) {
                        _data.writeInt(1);
                        dockedBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (tempDockedTaskBounds != null) {
                        _data.writeInt(1);
                        tempDockedTaskBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (tempDockedTaskInsetBounds != null) {
                        _data.writeInt(1);
                        tempDockedTaskInsetBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (tempOtherTaskBounds != null) {
                        _data.writeInt(1);
                        tempOtherTaskBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (tempOtherTaskInsetBounds != null) {
                        _data.writeInt(1);
                        tempOtherTaskInsetBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(261, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSplitScreenResizing(boolean resizing) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resizing);
                    this.mRemote.transact(262, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setVrMode(IBinder token, boolean enabled, ComponentName packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(enabled);
                    if (packageName != null) {
                        _data.writeInt(1);
                        packageName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(263, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getGrantedUriPermissions(String packageName, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(264, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearGrantedUriPermissions(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(265, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAppForeground(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    this.mRemote.transact(266, _data, _reply, 0);
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

            public void startLocalVoiceInteraction(IBinder token, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(267, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopLocalVoiceInteraction(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(268, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean supportsLocalVoiceInteraction() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(269, _data, _reply, 0);
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

            public void notifyPinnedStackAnimationStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(270, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyPinnedStackAnimationEnded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(271, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeStack(int stackId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stackId);
                    this.mRemote.transact(272, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeStacksInWindowingModes(int[] windowingModes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(windowingModes);
                    this.mRemote.transact(273, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeStacksWithActivityTypes(int[] activityTypes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(activityTypes);
                    this.mRemote.transact(274, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void makePackageIdle(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(275, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMemoryTrimLevel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(276, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resizePinnedStack(Rect pinnedBounds, Rect tempPinnedTaskBounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pinnedBounds != null) {
                        _data.writeInt(1);
                        pinnedBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (tempPinnedTaskBounds != null) {
                        _data.writeInt(1);
                        tempPinnedTaskBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(277, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVrModePackageEnabled(ComponentName packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (packageName != null) {
                        _data.writeInt(1);
                        packageName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(278, _data, _reply, 0);
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

            public void notifyLockedProfile(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(279, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startConfirmDeviceCredentialIntent(Intent intent, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(280, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendIdleJobTrigger() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(281, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendIntentSender(IIntentSender target, IBinder whitelistToken, int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(target != null ? target.asBinder() : null);
                    _data.writeStrongBinder(whitelistToken);
                    _data.writeInt(code);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    if (finishedReceiver != null) {
                        iBinder = finishedReceiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(requiredPermission);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(282, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBackgroundRestricted(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(283, _data, _reply, 0);
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

            public void setVrThread(int tid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tid);
                    this.mRemote.transact(284, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRenderThread(int tid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tid);
                    this.mRemote.transact(285, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setHasTopUi(boolean hasTopUi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hasTopUi);
                    this.mRemote.transact(286, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateDisplayOverrideConfiguration(Configuration values, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (values != null) {
                        _data.writeInt(1);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(displayId);
                    this.mRemote.transact(287, _data, _reply, 0);
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

            public void moveStackToDisplay(int stackId, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stackId);
                    _data.writeInt(displayId);
                    this.mRemote.transact(288, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean requestAutofillData(IAssistDataReceiver receiver, Bundle receiverExtras, IBinder activityToken, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    boolean _result = true;
                    if (receiverExtras != null) {
                        _data.writeInt(1);
                        receiverExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(activityToken);
                    _data.writeInt(flags);
                    this.mRemote.transact(289, _data, _reply, 0);
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

            public void dismissKeyguard(IBinder token, IKeyguardDismissCallback callback, CharSequence message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (message != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(message, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(290, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int restartUserInBackground(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(291, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelTaskWindowTransition(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(292, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution) throws RemoteException {
                ActivityManager.TaskSnapshot _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(reducedResolution);
                    this.mRemote.transact(293, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityManager.TaskSnapshot.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void scheduleApplicationInfoChanged(List<String> packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    _data.writeInt(userId);
                    this.mRemote.transact(294, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPersistentVrThread(int tid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tid);
                    this.mRemote.transact(295, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void waitForNetworkStateUpdate(long procStateSeq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(procStateSeq);
                    this.mRemote.transact(296, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDisablePreviewScreenshots(IBinder token, boolean disable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(disable);
                    this.mRemote.transact(297, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLastResumedActivityUserId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(298, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void backgroundWhitelistUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(299, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateLockTaskFeatures(int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    this.mRemote.transact(300, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setShowWhenLocked(IBinder token, boolean showWhenLocked) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(showWhenLocked);
                    this.mRemote.transact(301, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTurnScreenOn(IBinder token, boolean turnScreenOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(turnScreenOn);
                    this.mRemote.transact(302, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startUserInBackgroundWithListener(int userid, IProgressListener unlockProgressListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    _data.writeStrongBinder(unlockProgressListener != null ? unlockProgressListener.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(303, _data, _reply, 0);
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

            public void registerRemoteAnimations(IBinder token, RemoteAnimationDefinition definition) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (definition != null) {
                        _data.writeInt(1);
                        definition.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(304, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerRemoteAnimationForNextActivityStart(String packageName, RemoteAnimationAdapter adapter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (adapter != null) {
                        _data.writeInt(1);
                        adapter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(305, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void alwaysShowUnsupportedCompileSdkWarning(ComponentName activity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (activity != null) {
                        _data.writeInt(1);
                        activity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(306, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(307, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readStrongBinder();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void swapDockedAndFullscreenStack() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(308, _data, _reply, 0);
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

        public static IActivityManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IActivityManager)) {
                return new Proxy(obj);
            }
            return (IActivityManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: android.app.ApplicationErrorReport$ParcelableCrashInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v28, resolved type: android.content.res.Configuration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v41, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v53, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v75, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v79, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v84, resolved type: android.content.res.Configuration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v88, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v125, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v157, resolved type: android.content.pm.ApplicationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v171, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v181, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v189, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v195, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v219, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v224, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v234, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v277, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v283, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v287, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v302, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v308, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v314, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v327, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v338, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v351, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v361, resolved type: android.os.StrictMode$ViolationInfo} */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            PersistableBundle _arg2;
            Intent _arg12;
            Intent _arg13;
            Intent _arg14;
            ActivityManager.TaskDescription _arg22;
            Rect _arg0;
            Intent _arg02;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                StrictMode.ViolationInfo _arg23 = null;
                boolean _arg15 = false;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParcelFileDescriptor _result = openContentUri(data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            parcel2.writeInt(1);
                            _result.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerUidObserver(IUidObserver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterUidObserver(IUidObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result2 = isUidActive(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg03 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = (ApplicationErrorReport.ParcelableCrashInfo) ApplicationErrorReport.ParcelableCrashInfo.CREATOR.createFromParcel(parcel);
                        }
                        handleApplicationCrash(_arg03, _arg23);
                        reply.writeNoException();
                        return true;
                    case 6:
                        return onTransact$startActivity$(parcel, parcel2);
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        unhandledBack();
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg04 = data.readStrongBinder();
                        int _arg16 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result3 = finishActivity(_arg04, _arg16, _arg23, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 9:
                        return onTransact$registerReceiver$(parcel, parcel2);
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterReceiver(IIntentReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 11:
                        return onTransact$broadcastIntent$(parcel, parcel2);
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg05 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg23 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        unbroadcastIntent(_arg05, _arg23, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        return onTransact$finishReceiver$(parcel, parcel2);
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        attachApplication(IApplicationThread.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg06 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = (Configuration) Configuration.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        activityIdle(_arg06, _arg23, _arg15);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        activityPaused(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg07 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        }
                        activityStopped(_arg07, _arg1, _arg2, _arg23);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result4 = getCallingPackage(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeString(_result4);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        ComponentName _result5 = getCallingActivity(data.readStrongBinder());
                        reply.writeNoException();
                        if (_result5 != null) {
                            parcel2.writeInt(1);
                            _result5.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.RunningTaskInfo> _result6 = getTasks(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result6);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.RunningTaskInfo> _result7 = getFilteredTasks(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result7);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        int _arg17 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        moveTaskToFront(_arg08, _arg17, _arg23);
                        reply.writeNoException();
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        moveTaskBackwards(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg09 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        int _result8 = getTaskForActivity(_arg09, _arg15);
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        ContentProviderHolder _result9 = getContentProvider(IApplicationThread.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result9 != null) {
                            parcel2.writeInt(1);
                            _result9.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        publishContentProviders(IApplicationThread.Stub.asInterface(data.readStrongBinder()), parcel.createTypedArrayList(ContentProviderHolder.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result10 = refContentProvider(data.readStrongBinder(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        finishSubActivity(data.readStrongBinder(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        PendingIntent _result11 = getRunningServiceControlPanel(_arg23);
                        reply.writeNoException();
                        if (_result11 != null) {
                            parcel2.writeInt(1);
                            _result11.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 30:
                        return onTransact$startService$(parcel, parcel2);
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg010 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg23 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        int _result12 = stopService(_arg010, _arg23, data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 32:
                        return onTransact$bindService$(parcel, parcel2);
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result13 = unbindService(IServiceConnection.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg011 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        publishService(_arg011, _arg23, data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        activityResumed(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        boolean _arg18 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setDebugApp(_arg012, _arg18, _arg15);
                        reply.writeNoException();
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        setAgentApp(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setAlwaysFinish(_arg15);
                        reply.writeNoException();
                        return true;
                    case 39:
                        return onTransact$startInstrumentation$(parcel, parcel2);
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg013 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        addInstrumentationResults(_arg013, _arg23);
                        reply.writeNoException();
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg014 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        int _arg19 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        finishInstrumentation(_arg014, _arg19, _arg23);
                        reply.writeNoException();
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        Configuration _result14 = getConfiguration();
                        reply.writeNoException();
                        if (_result14 != null) {
                            parcel2.writeInt(1);
                            _result14.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (Configuration) Configuration.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result15 = updateConfiguration(_arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 44:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result16 = stopServiceToken(_arg23, data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 45:
                        parcel.enforceInterface(DESCRIPTOR);
                        ComponentName _result17 = getActivityClassForToken(data.readStrongBinder());
                        reply.writeNoException();
                        if (_result17 != null) {
                            parcel2.writeInt(1);
                            _result17.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 46:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result18 = getPackageForToken(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeString(_result18);
                        return true;
                    case 47:
                        parcel.enforceInterface(DESCRIPTOR);
                        setProcessLimit(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 48:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result19 = getProcessLimit();
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 49:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result20 = checkPermission(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 50:
                        return onTransact$checkUriPermission$(parcel, parcel2);
                    case 51:
                        parcel.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg015 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg110 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        }
                        grantUriPermission(_arg015, _arg110, _arg23, data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 52:
                        parcel.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg016 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg111 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        }
                        revokeUriPermission(_arg016, _arg111, _arg23, data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 53:
                        parcel.enforceInterface(DESCRIPTOR);
                        IActivityController _arg017 = IActivityController.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setActivityController(_arg017, _arg15);
                        reply.writeNoException();
                        return true;
                    case 54:
                        parcel.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg018 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        showWaitingForDebugger(_arg018, _arg15);
                        reply.writeNoException();
                        return true;
                    case 55:
                        parcel.enforceInterface(DESCRIPTOR);
                        signalPersistentProcesses(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 56:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result21 = getRecentTasks(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result21 != null) {
                            parcel2.writeInt(1);
                            _result21.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 57:
                        parcel.enforceInterface(DESCRIPTOR);
                        serviceDoneExecuting(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 58:
                        parcel.enforceInterface(DESCRIPTOR);
                        activityDestroyed(data.readStrongBinder());
                        return true;
                    case 59:
                        return onTransact$getIntentSender$(parcel, parcel2);
                    case 60:
                        parcel.enforceInterface(DESCRIPTOR);
                        cancelIntentSender(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 61:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result22 = getPackageForIntentSender(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeString(_result22);
                        return true;
                    case 62:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerIntentSenderCancelListener(IIntentSender.Stub.asInterface(data.readStrongBinder()), IResultReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 63:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterIntentSenderCancelListener(IIntentSender.Stub.asInterface(data.readStrongBinder()), IResultReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 64:
                        parcel.enforceInterface(DESCRIPTOR);
                        enterSafeMode();
                        reply.writeNoException();
                        return true;
                    case 65:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg019 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = Intent.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result23 = startNextMatchingActivity(_arg019, _arg12, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result23);
                        return true;
                    case 66:
                        return onTransact$noteWakeupAlarm$(parcel, parcel2);
                    case 67:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg020 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        removeContentProvider(_arg020, _arg15);
                        reply.writeNoException();
                        return true;
                    case 68:
                        parcel.enforceInterface(DESCRIPTOR);
                        setRequestedOrientation(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 69:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result24 = getRequestedOrientation(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result24);
                        return true;
                    case 70:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg021 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        unbindFinished(_arg021, _arg23, _arg15);
                        reply.writeNoException();
                        return true;
                    case 71:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg022 = data.readStrongBinder();
                        int _arg112 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setProcessImportant(_arg022, _arg112, _arg15, data.readString());
                        reply.writeNoException();
                        return true;
                    case 72:
                        return onTransact$setServiceForeground$(parcel, parcel2);
                    case 73:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg023 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean _result25 = moveActivityTaskToBack(_arg023, _arg15);
                        reply.writeNoException();
                        parcel2.writeInt(_result25);
                        return true;
                    case 74:
                        parcel.enforceInterface(DESCRIPTOR);
                        ActivityManager.MemoryInfo _arg024 = new ActivityManager.MemoryInfo();
                        getMemoryInfo(_arg024);
                        reply.writeNoException();
                        parcel2.writeInt(1);
                        _arg024.writeToParcel(parcel2, 1);
                        return true;
                    case 75:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.ProcessErrorStateInfo> _result26 = getProcessesInErrorState();
                        reply.writeNoException();
                        parcel2.writeTypedList(_result26);
                        return true;
                    case 76:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg025 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean _result27 = clearApplicationUserData(_arg025, _arg15, IPackageDataObserver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result27);
                        return true;
                    case 77:
                        parcel.enforceInterface(DESCRIPTOR);
                        forceStopPackage(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 78:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _arg026 = data.createIntArray();
                        String _arg113 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean _result28 = killPids(_arg026, _arg113, _arg15);
                        reply.writeNoException();
                        parcel2.writeInt(_result28);
                        return true;
                    case 79:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.RunningServiceInfo> _result29 = getServices(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result29);
                        return true;
                    case 80:
                        parcel.enforceInterface(DESCRIPTOR);
                        ActivityManager.TaskDescription _result30 = getTaskDescription(data.readInt());
                        reply.writeNoException();
                        if (_result30 != null) {
                            parcel2.writeInt(1);
                            _result30.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 81:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.RunningAppProcessInfo> _result31 = getRunningAppProcesses();
                        reply.writeNoException();
                        parcel2.writeTypedList(_result31);
                        return true;
                    case 82:
                        parcel.enforceInterface(DESCRIPTOR);
                        ConfigurationInfo _result32 = getDeviceConfigurationInfo();
                        reply.writeNoException();
                        if (_result32 != null) {
                            parcel2.writeInt(1);
                            _result32.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 83:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        IBinder _result33 = peekService(_arg23, data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeStrongBinder(_result33);
                        return true;
                    case 84:
                        return onTransact$profileControl$(parcel, parcel2);
                    case 85:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result34 = shutdown(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result34);
                        return true;
                    case 86:
                        parcel.enforceInterface(DESCRIPTOR);
                        stopAppSwitches();
                        reply.writeNoException();
                        return true;
                    case 87:
                        parcel.enforceInterface(DESCRIPTOR);
                        resumeAppSwitches();
                        reply.writeNoException();
                        return true;
                    case 88:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result35 = bindBackupAgent(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result35);
                        return true;
                    case 89:
                        parcel.enforceInterface(DESCRIPTOR);
                        backupAgentCreated(data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 90:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (ApplicationInfo) ApplicationInfo.CREATOR.createFromParcel(parcel);
                        }
                        unbindBackupAgent(_arg23);
                        reply.writeNoException();
                        return true;
                    case 91:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result36 = getUidForIntentSender(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result36);
                        return true;
                    case 92:
                        return onTransact$handleIncomingUser$(parcel, parcel2);
                    case 93:
                        parcel.enforceInterface(DESCRIPTOR);
                        addPackageDependency(data.readString());
                        reply.writeNoException();
                        return true;
                    case 94:
                        parcel.enforceInterface(DESCRIPTOR);
                        killApplication(data.readString(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 95:
                        parcel.enforceInterface(DESCRIPTOR);
                        closeSystemDialogs(data.readString());
                        reply.writeNoException();
                        return true;
                    case 96:
                        parcel.enforceInterface(DESCRIPTOR);
                        Debug.MemoryInfo[] _result37 = getProcessMemoryInfo(data.createIntArray());
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result37, 1);
                        return true;
                    case 97:
                        parcel.enforceInterface(DESCRIPTOR);
                        killApplicationProcess(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 98:
                        return onTransact$startActivityIntentSender$(parcel, parcel2);
                    case 99:
                        parcel.enforceInterface(DESCRIPTOR);
                        overridePendingTransition(data.readStrongBinder(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 100:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg027 = data.readStrongBinder();
                        String _arg114 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (ApplicationErrorReport.ParcelableCrashInfo) ApplicationErrorReport.ParcelableCrashInfo.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result38 = handleApplicationWtf(_arg027, _arg114, _arg15, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result38);
                        return true;
                    case 101:
                        parcel.enforceInterface(DESCRIPTOR);
                        killBackgroundProcesses(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 102:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result39 = isUserAMonkey();
                        reply.writeNoException();
                        parcel2.writeInt(_result39);
                        return true;
                    case 103:
                        return onTransact$startActivityAndWait$(parcel, parcel2);
                    case 104:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result40 = willActivityBeVisible(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result40);
                        return true;
                    case 105:
                        return onTransact$startActivityWithConfig$(parcel, parcel2);
                    case 106:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<ApplicationInfo> _result41 = getRunningExternalApplications();
                        reply.writeNoException();
                        parcel2.writeTypedList(_result41);
                        return true;
                    case 107:
                        parcel.enforceInterface(DESCRIPTOR);
                        finishHeavyWeightApp();
                        reply.writeNoException();
                        return true;
                    case 108:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg028 = data.readStrongBinder();
                        int _arg115 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (StrictMode.ViolationInfo) StrictMode.ViolationInfo.CREATOR.createFromParcel(parcel);
                        }
                        handleApplicationStrictModeViolation(_arg028, _arg115, _arg23);
                        reply.writeNoException();
                        return true;
                    case 109:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result42 = isImmersive(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result42);
                        return true;
                    case 110:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg029 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setImmersive(_arg029, _arg15);
                        reply.writeNoException();
                        return true;
                    case 111:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result43 = isTopActivityImmersive();
                        reply.writeNoException();
                        parcel2.writeInt(_result43);
                        return true;
                    case 112:
                        return onTransact$crashApplication$(parcel, parcel2);
                    case 113:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        }
                        String _result44 = getProviderMimeType(_arg23, data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result44);
                        return true;
                    case 114:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _result45 = newUriPermissionOwner(data.readString());
                        reply.writeNoException();
                        parcel2.writeStrongBinder(_result45);
                        return true;
                    case 115:
                        return onTransact$grantUriPermissionFromOwner$(parcel, parcel2);
                    case 116:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg030 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        }
                        revokeUriPermissionFromOwner(_arg030, _arg23, data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 117:
                        return onTransact$checkGrantUriPermission$(parcel, parcel2);
                    case 118:
                        return onTransact$dumpHeap$(parcel, parcel2);
                    case 119:
                        return onTransact$startActivities$(parcel, parcel2);
                    case 120:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result46 = isUserRunning(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result46);
                        return true;
                    case 121:
                        parcel.enforceInterface(DESCRIPTOR);
                        activitySlept(data.readStrongBinder());
                        return true;
                    case 122:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result47 = getFrontActivityScreenCompatMode();
                        reply.writeNoException();
                        parcel2.writeInt(_result47);
                        return true;
                    case 123:
                        parcel.enforceInterface(DESCRIPTOR);
                        setFrontActivityScreenCompatMode(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 124:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result48 = getPackageScreenCompatMode(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result48);
                        return true;
                    case 125:
                        parcel.enforceInterface(DESCRIPTOR);
                        setPackageScreenCompatMode(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 126:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result49 = getPackageAskScreenCompat(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result49);
                        return true;
                    case 127:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg031 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setPackageAskScreenCompat(_arg031, _arg15);
                        reply.writeNoException();
                        return true;
                    case 128:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result50 = switchUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result50);
                        return true;
                    case 129:
                        parcel.enforceInterface(DESCRIPTOR);
                        setFocusedTask(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 130:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result51 = removeTask(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result51);
                        return true;
                    case 131:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerProcessObserver(IProcessObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 132:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterProcessObserver(IProcessObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 133:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result52 = isIntentSenderTargetedToPackage(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result52);
                        return true;
                    case 134:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (Configuration) Configuration.CREATOR.createFromParcel(parcel);
                        }
                        updatePersistentConfiguration(_arg23);
                        reply.writeNoException();
                        return true;
                    case 135:
                        parcel.enforceInterface(DESCRIPTOR);
                        long[] _result53 = getProcessPss(data.createIntArray());
                        reply.writeNoException();
                        parcel2.writeLongArray(_result53);
                        return true;
                    case 136:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        showBootMessage(_arg23, _arg15);
                        reply.writeNoException();
                        return true;
                    case 137:
                        parcel.enforceInterface(DESCRIPTOR);
                        killAllBackgroundProcesses();
                        reply.writeNoException();
                        return true;
                    case 138:
                        parcel.enforceInterface(DESCRIPTOR);
                        ContentProviderHolder _result54 = getContentProviderExternal(data.readString(), data.readInt(), data.readStrongBinder());
                        reply.writeNoException();
                        if (_result54 != null) {
                            parcel2.writeInt(1);
                            _result54.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 139:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeContentProviderExternal(data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 140:
                        parcel.enforceInterface(DESCRIPTOR);
                        ActivityManager.RunningAppProcessInfo _arg032 = new ActivityManager.RunningAppProcessInfo();
                        getMyMemoryState(_arg032);
                        reply.writeNoException();
                        parcel2.writeInt(1);
                        _arg032.writeToParcel(parcel2, 1);
                        return true;
                    case 141:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result55 = killProcessesBelowForeground(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result55);
                        return true;
                    case 142:
                        parcel.enforceInterface(DESCRIPTOR);
                        UserInfo _result56 = getCurrentUser();
                        reply.writeNoException();
                        if (_result56 != null) {
                            parcel2.writeInt(1);
                            _result56.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 143:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result57 = shouldUpRecreateTask(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result57);
                        return true;
                    case 144:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg033 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg13 = Intent.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg13 = null;
                        }
                        int _arg24 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result58 = navigateUpTo(_arg033, _arg13, _arg24, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result58);
                        return true;
                    case 145:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _arg034 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setLockScreenShown(_arg034, _arg15, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 146:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result59 = finishActivityAffinity(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result59);
                        return true;
                    case 147:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result60 = getLaunchedFromUid(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result60);
                        return true;
                    case 148:
                        parcel.enforceInterface(DESCRIPTOR);
                        unstableProviderDied(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 149:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result61 = isIntentSenderAnActivity(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result61);
                        return true;
                    case 150:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result62 = isIntentSenderAForegroundService(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result62);
                        return true;
                    case 151:
                        return onTransact$startActivityAsUser$(parcel, parcel2);
                    case 152:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg035 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        int _result63 = stopUser(_arg035, _arg15, IStopUserCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result63);
                        return true;
                    case 153:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerUserSwitchObserver(IUserSwitchObserver.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 154:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterUserSwitchObserver(IUserSwitchObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 155:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result64 = getRunningUserIds();
                        reply.writeNoException();
                        parcel2.writeIntArray(_result64);
                        return true;
                    case 156:
                        parcel.enforceInterface(DESCRIPTOR);
                        requestBugReport(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 157:
                        parcel.enforceInterface(DESCRIPTOR);
                        requestTelephonyBugReport(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 158:
                        parcel.enforceInterface(DESCRIPTOR);
                        requestWifiBugReport(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 159:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg036 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        long _result65 = inputDispatchingTimedOut(_arg036, _arg15, data.readString());
                        reply.writeNoException();
                        parcel2.writeLong(_result65);
                        return true;
                    case 160:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearPendingBackup();
                        reply.writeNoException();
                        return true;
                    case 161:
                        parcel.enforceInterface(DESCRIPTOR);
                        Intent _result66 = getIntentForIntentSender(IIntentSender.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result66 != null) {
                            parcel2.writeInt(1);
                            _result66.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 162:
                        parcel.enforceInterface(DESCRIPTOR);
                        Bundle _result67 = getAssistContextExtras(data.readInt());
                        reply.writeNoException();
                        if (_result67 != null) {
                            parcel2.writeInt(1);
                            _result67.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 163:
                        return onTransact$reportAssistContextExtras$(parcel, parcel2);
                    case 164:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result68 = getLaunchedFromPackage(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeString(_result68);
                        return true;
                    case 165:
                        parcel.enforceInterface(DESCRIPTOR);
                        killUid(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 166:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setUserIsMonkey(_arg15);
                        reply.writeNoException();
                        return true;
                    case 167:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg037 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        hang(_arg037, _arg15);
                        reply.writeNoException();
                        return true;
                    case 168:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg038 = data.readInt();
                        int _arg116 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setTaskWindowingMode(_arg038, _arg116, _arg15);
                        reply.writeNoException();
                        return true;
                    case 169:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg039 = data.readInt();
                        int _arg117 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        moveTaskToStack(_arg039, _arg117, _arg15);
                        reply.writeNoException();
                        return true;
                    case 170:
                        return onTransact$resizeStack$(parcel, parcel2);
                    case 171:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.StackInfo> _result69 = getAllStackInfos();
                        reply.writeNoException();
                        parcel2.writeTypedList(_result69);
                        return true;
                    case 172:
                        parcel.enforceInterface(DESCRIPTOR);
                        setFocusedStack(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 173:
                        parcel.enforceInterface(DESCRIPTOR);
                        ActivityManager.StackInfo _result70 = getFocusedStackInfo();
                        reply.writeNoException();
                        if (_result70 != null) {
                            parcel2.writeInt(1);
                            _result70.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 174:
                        parcel.enforceInterface(DESCRIPTOR);
                        ActivityManager.StackInfo _result71 = getStackInfo(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result71 != null) {
                            parcel2.writeInt(1);
                            _result71.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 175:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result72 = convertFromTranslucent(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result72);
                        return true;
                    case 176:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg040 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result73 = convertToTranslucent(_arg040, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result73);
                        return true;
                    case 177:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyActivityDrawn(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 178:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg041 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        reportActivityFullyDrawn(_arg041, _arg15);
                        reply.writeNoException();
                        return true;
                    case 179:
                        parcel.enforceInterface(DESCRIPTOR);
                        restart();
                        reply.writeNoException();
                        return true;
                    case 180:
                        parcel.enforceInterface(DESCRIPTOR);
                        performIdleMaintenance();
                        reply.writeNoException();
                        return true;
                    case 181:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        }
                        takePersistableUriPermission(_arg23, data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 182:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        }
                        releasePersistableUriPermission(_arg23, data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 183:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result74 = getPersistedUriPermissions(data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result74 != null) {
                            parcel2.writeInt(1);
                            _result74.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 184:
                        parcel.enforceInterface(DESCRIPTOR);
                        appNotRespondingViaProvider(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 185:
                        parcel.enforceInterface(DESCRIPTOR);
                        Rect _result75 = getTaskBounds(data.readInt());
                        reply.writeNoException();
                        if (_result75 != null) {
                            parcel2.writeInt(1);
                            _result75.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 186:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result76 = getActivityDisplayId(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result76);
                        return true;
                    case 187:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result77 = setProcessMemoryTrimLevel(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result77);
                        return true;
                    case 188:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result78 = getTagForIntentSender(IIntentSender.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result78);
                        return true;
                    case 189:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result79 = startUserInBackground(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result79);
                        return true;
                    case 190:
                        parcel.enforceInterface(DESCRIPTOR);
                        startLockTaskModeByToken(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 191:
                        parcel.enforceInterface(DESCRIPTOR);
                        stopLockTaskModeByToken(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 192:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result80 = isInLockTaskMode();
                        reply.writeNoException();
                        parcel2.writeInt(_result80);
                        return true;
                    case 193:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg042 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = (ActivityManager.TaskDescription) ActivityManager.TaskDescription.CREATOR.createFromParcel(parcel);
                        }
                        setTaskDescription(_arg042, _arg23);
                        reply.writeNoException();
                        return true;
                    case 194:
                        return onTransact$startVoiceActivity$(parcel, parcel2);
                    case 195:
                        return onTransact$startAssistantActivity$(parcel, parcel2);
                    case 196:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        startRecentsActivity(_arg23, IAssistDataReceiver.Stub.asInterface(data.readStrongBinder()), IRecentsAnimationRunner.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 197:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        cancelRecentsAnimation(_arg15);
                        reply.writeNoException();
                        return true;
                    case 198:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg043 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        int _result81 = startActivityFromRecents(_arg043, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result81);
                        return true;
                    case 199:
                        parcel.enforceInterface(DESCRIPTOR);
                        Bundle _result82 = getActivityOptions(data.readStrongBinder());
                        reply.writeNoException();
                        if (_result82 != null) {
                            parcel2.writeInt(1);
                            _result82.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 200:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<IBinder> _result83 = getAppTasks(data.readString());
                        reply.writeNoException();
                        parcel2.writeBinderList(_result83);
                        return true;
                    case 201:
                        parcel.enforceInterface(DESCRIPTOR);
                        startSystemLockTaskMode(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 202:
                        parcel.enforceInterface(DESCRIPTOR);
                        stopSystemLockTaskMode();
                        reply.writeNoException();
                        return true;
                    case 203:
                        parcel.enforceInterface(DESCRIPTOR);
                        finishVoiceTask(IVoiceInteractionSession.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 204:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result84 = isTopOfTask(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result84);
                        return true;
                    case 205:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyLaunchTaskBehindComplete(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 206:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyEnterAnimationComplete(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 207:
                        return onTransact$startActivityAsCaller$(parcel, parcel2);
                    case 208:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg044 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg14 = Intent.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg14 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = ActivityManager.TaskDescription.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (Bitmap) Bitmap.CREATOR.createFromParcel(parcel);
                        }
                        int _result85 = addAppTask(_arg044, _arg14, _arg22, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result85);
                        return true;
                    case 209:
                        return onTransact$setExitInfo$(parcel, parcel2);
                    case 210:
                        parcel.enforceInterface(DESCRIPTOR);
                        Point _result86 = getAppTaskThumbnailSize();
                        reply.writeNoException();
                        if (_result86 != null) {
                            parcel2.writeInt(1);
                            _result86.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 211:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result87 = releaseActivityInstance(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result87);
                        return true;
                    case 212:
                        parcel.enforceInterface(DESCRIPTOR);
                        releaseSomeActivities(IApplicationThread.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 213:
                        parcel.enforceInterface(DESCRIPTOR);
                        bootAnimationComplete();
                        reply.writeNoException();
                        return true;
                    case 214:
                        parcel.enforceInterface(DESCRIPTOR);
                        Bitmap _result88 = getTaskDescriptionIcon(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result88 != null) {
                            parcel2.writeInt(1);
                            _result88.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 215:
                        return onTransact$launchAssistIntent$(parcel, parcel2);
                    case 216:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        startInPlaceAnimationOnFrontMostApplication(_arg23);
                        reply.writeNoException();
                        return true;
                    case 217:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result89 = checkPermissionWithToken(data.readString(), data.readInt(), data.readInt(), data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result89);
                        return true;
                    case 218:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerTaskStackListener(ITaskStackListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 219:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterTaskStackListener(ITaskStackListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 220:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyCleartextNetwork(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 221:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result90 = createStackOnDisplay(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result90);
                        return true;
                    case 222:
                        parcel.enforceInterface(DESCRIPTOR);
                        setTaskResizeable(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 223:
                        return onTransact$requestAssistContextExtras$(parcel, parcel2);
                    case 224:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg045 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (Rect) Rect.CREATOR.createFromParcel(parcel);
                        }
                        resizeTask(_arg045, _arg23, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 225:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result91 = getLockTaskModeState();
                        reply.writeNoException();
                        parcel2.writeInt(_result91);
                        return true;
                    case 226:
                        parcel.enforceInterface(DESCRIPTOR);
                        setDumpHeapDebugLimit(data.readString(), data.readInt(), data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 227:
                        parcel.enforceInterface(DESCRIPTOR);
                        dumpHeapFinished(data.readString());
                        reply.writeNoException();
                        return true;
                    case 228:
                        parcel.enforceInterface(DESCRIPTOR);
                        IVoiceInteractionSession _arg046 = IVoiceInteractionSession.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setVoiceKeepAwake(_arg046, _arg15);
                        reply.writeNoException();
                        return true;
                    case 229:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateLockTaskPackages(data.readInt(), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 230:
                        parcel.enforceInterface(DESCRIPTOR);
                        IIntentSender _arg047 = IIntentSender.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg23 = (WorkSource) WorkSource.CREATOR.createFromParcel(parcel);
                        }
                        noteAlarmStart(_arg047, _arg23, data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 231:
                        parcel.enforceInterface(DESCRIPTOR);
                        IIntentSender _arg048 = IIntentSender.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg23 = (WorkSource) WorkSource.CREATOR.createFromParcel(parcel);
                        }
                        noteAlarmFinish(_arg048, _arg23, data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 232:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result92 = getPackageProcessState(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result92);
                        return true;
                    case 233:
                        parcel.enforceInterface(DESCRIPTOR);
                        showLockTaskEscapeMessage(data.readStrongBinder());
                        return true;
                    case 234:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateDeviceOwner(data.readString());
                        reply.writeNoException();
                        return true;
                    case 235:
                        parcel.enforceInterface(DESCRIPTOR);
                        keyguardGoingAway(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 236:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result93 = getUidProcessState(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result93);
                        return true;
                    case 237:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result94 = isAssistDataAllowedOnCurrentActivity();
                        reply.writeNoException();
                        parcel2.writeInt(_result94);
                        return true;
                    case 238:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg049 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result95 = showAssistFromActivity(_arg049, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result95);
                        return true;
                    case 239:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result96 = isRootVoiceInteraction(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result96);
                        return true;
                    case 240:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result97 = startBinderTracking();
                        reply.writeNoException();
                        parcel2.writeInt(_result97);
                        return true;
                    case 241:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result98 = stopBinderTrackingAndDump(_arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result98);
                        return true;
                    case 242:
                        parcel.enforceInterface(DESCRIPTOR);
                        positionTaskInStack(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 243:
                        parcel.enforceInterface(DESCRIPTOR);
                        exitFreeformMode(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 244:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportSizeConfigurations(data.readStrongBinder(), data.createIntArray(), data.createIntArray(), data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 245:
                        return onTransact$setTaskWindowingModeSplitScreenPrimary$(parcel, parcel2);
                    case 246:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        dismissSplitScreenMode(_arg15);
                        reply.writeNoException();
                        return true;
                    case 247:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        dismissPip(_arg15, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 248:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        suppressResizeConfigChanges(_arg15);
                        reply.writeNoException();
                        return true;
                    case 249:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg050 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        moveTasksToFullscreenStack(_arg050, _arg15);
                        reply.writeNoException();
                        return true;
                    case 250:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg051 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (Rect) Rect.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result99 = moveTopActivityToPinnedStack(_arg051, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result99);
                        return true;
                    case 251:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result100 = isAppStartModeDisabled(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result100);
                        return true;
                    case 252:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result101 = unlockUser(data.readInt(), data.createByteArray(), data.createByteArray(), IProgressListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result101);
                        return true;
                    case 253:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result102 = isInMultiWindowMode(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result102);
                        return true;
                    case 254:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result103 = isInPictureInPictureMode(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result103);
                        return true;
                    case 255:
                        parcel.enforceInterface(DESCRIPTOR);
                        killPackageDependents(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 256:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg052 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = (PictureInPictureParams) PictureInPictureParams.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result104 = enterPictureInPictureMode(_arg052, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result104);
                        return true;
                    case 257:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg053 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = (PictureInPictureParams) PictureInPictureParams.CREATOR.createFromParcel(parcel);
                        }
                        setPictureInPictureParams(_arg053, _arg23);
                        reply.writeNoException();
                        return true;
                    case 258:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result105 = getMaxNumPictureInPictureActions(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result105);
                        return true;
                    case 259:
                        parcel.enforceInterface(DESCRIPTOR);
                        activityRelaunched(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 260:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _result106 = getUriPermissionOwnerForActivity(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeStrongBinder(_result106);
                        return true;
                    case 261:
                        return onTransact$resizeDockedStack$(parcel, parcel2);
                    case 262:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setSplitScreenResizing(_arg15);
                        reply.writeNoException();
                        return true;
                    case 263:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg054 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        int _result107 = setVrMode(_arg054, _arg15, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result107);
                        return true;
                    case 264:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result108 = getGrantedUriPermissions(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result108 != null) {
                            parcel2.writeInt(1);
                            _result108.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 265:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearGrantedUriPermissions(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 266:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result109 = isAppForeground(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result109);
                        return true;
                    case 267:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg055 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        startLocalVoiceInteraction(_arg055, _arg23);
                        reply.writeNoException();
                        return true;
                    case 268:
                        parcel.enforceInterface(DESCRIPTOR);
                        stopLocalVoiceInteraction(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 269:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result110 = supportsLocalVoiceInteraction();
                        reply.writeNoException();
                        parcel2.writeInt(_result110);
                        return true;
                    case 270:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyPinnedStackAnimationStarted();
                        reply.writeNoException();
                        return true;
                    case 271:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyPinnedStackAnimationEnded();
                        reply.writeNoException();
                        return true;
                    case 272:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeStack(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 273:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeStacksInWindowingModes(data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 274:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeStacksWithActivityTypes(data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 275:
                        parcel.enforceInterface(DESCRIPTOR);
                        makePackageIdle(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 276:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result111 = getMemoryTrimLevel();
                        reply.writeNoException();
                        parcel2.writeInt(_result111);
                        return true;
                    case 277:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Rect.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (Rect) Rect.CREATOR.createFromParcel(parcel);
                        }
                        resizePinnedStack(_arg0, _arg23);
                        reply.writeNoException();
                        return true;
                    case 278:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result112 = isVrModePackageEnabled(_arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result112);
                        return true;
                    case 279:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyLockedProfile(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 280:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Intent.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        startConfirmDeviceCredentialIntent(_arg02, _arg23);
                        reply.writeNoException();
                        return true;
                    case 281:
                        parcel.enforceInterface(DESCRIPTOR);
                        sendIdleJobTrigger();
                        reply.writeNoException();
                        return true;
                    case 282:
                        return onTransact$sendIntentSender$(parcel, parcel2);
                    case 283:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result113 = isBackgroundRestricted(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result113);
                        return true;
                    case 284:
                        parcel.enforceInterface(DESCRIPTOR);
                        setVrThread(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 285:
                        parcel.enforceInterface(DESCRIPTOR);
                        setRenderThread(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 286:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setHasTopUi(_arg15);
                        reply.writeNoException();
                        return true;
                    case 287:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (Configuration) Configuration.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result114 = updateDisplayOverrideConfiguration(_arg23, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result114);
                        return true;
                    case 288:
                        parcel.enforceInterface(DESCRIPTOR);
                        moveStackToDisplay(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 289:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAssistDataReceiver _arg056 = IAssistDataReceiver.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result115 = requestAutofillData(_arg056, _arg23, data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result115);
                        return true;
                    case 290:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg057 = data.readStrongBinder();
                        IKeyguardDismissCallback _arg118 = IKeyguardDismissCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg23 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        }
                        dismissKeyguard(_arg057, _arg118, _arg23);
                        reply.writeNoException();
                        return true;
                    case 291:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result116 = restartUserInBackground(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result116);
                        return true;
                    case 292:
                        parcel.enforceInterface(DESCRIPTOR);
                        cancelTaskWindowTransition(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 293:
                        parcel.enforceInterface(DESCRIPTOR);
                        ActivityManager.TaskSnapshot _result117 = getTaskSnapshot(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result117 != null) {
                            parcel2.writeInt(1);
                            _result117.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 294:
                        parcel.enforceInterface(DESCRIPTOR);
                        scheduleApplicationInfoChanged(data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 295:
                        parcel.enforceInterface(DESCRIPTOR);
                        setPersistentVrThread(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 296:
                        parcel.enforceInterface(DESCRIPTOR);
                        waitForNetworkStateUpdate(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 297:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg058 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setDisablePreviewScreenshots(_arg058, _arg15);
                        reply.writeNoException();
                        return true;
                    case 298:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result118 = getLastResumedActivityUserId();
                        reply.writeNoException();
                        parcel2.writeInt(_result118);
                        return true;
                    case 299:
                        parcel.enforceInterface(DESCRIPTOR);
                        backgroundWhitelistUid(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 300:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateLockTaskFeatures(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 301:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg059 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setShowWhenLocked(_arg059, _arg15);
                        reply.writeNoException();
                        return true;
                    case 302:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg060 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setTurnScreenOn(_arg060, _arg15);
                        reply.writeNoException();
                        return true;
                    case 303:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result119 = startUserInBackgroundWithListener(data.readInt(), IProgressListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result119);
                        return true;
                    case 304:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg061 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = (RemoteAnimationDefinition) RemoteAnimationDefinition.CREATOR.createFromParcel(parcel);
                        }
                        registerRemoteAnimations(_arg061, _arg23);
                        reply.writeNoException();
                        return true;
                    case 305:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg062 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = (RemoteAnimationAdapter) RemoteAnimationAdapter.CREATOR.createFromParcel(parcel);
                        }
                        registerRemoteAnimationForNextActivityStart(_arg062, _arg23);
                        reply.writeNoException();
                        return true;
                    case 306:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg23 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        alwaysShowUnsupportedCompileSdkWarning(_arg23);
                        reply.writeNoException();
                        return true;
                    case 307:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _result120 = getHwInnerService();
                        reply.writeNoException();
                        parcel2.writeStrongBinder(_result120);
                        return true;
                    case 308:
                        parcel.enforceInterface(DESCRIPTOR);
                        swapDockedAndFullscreenStack();
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private boolean onTransact$startActivity$(Parcel data, Parcel reply) throws RemoteException {
            Intent _arg2;
            ProfilerInfo _arg8;
            Bundle _arg9;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
            String _arg1 = data.readString();
            if (data.readInt() != 0) {
                _arg2 = Intent.CREATOR.createFromParcel(parcel);
            } else {
                _arg2 = null;
            }
            String _arg3 = data.readString();
            IBinder _arg4 = data.readStrongBinder();
            String _arg5 = data.readString();
            int _arg6 = data.readInt();
            int _arg7 = data.readInt();
            if (data.readInt() != 0) {
                _arg8 = ProfilerInfo.CREATOR.createFromParcel(parcel);
            } else {
                _arg8 = null;
            }
            if (data.readInt() != 0) {
                _arg9 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            } else {
                _arg9 = null;
            }
            int _result = startActivity(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9);
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$registerReceiver$(Parcel data, Parcel reply) throws RemoteException {
            IntentFilter intentFilter;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            parcel.enforceInterface(DESCRIPTOR);
            IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
            String _arg1 = data.readString();
            IIntentReceiver _arg2 = IIntentReceiver.Stub.asInterface(data.readStrongBinder());
            if (data.readInt() != 0) {
                intentFilter = IntentFilter.CREATOR.createFromParcel(parcel);
            } else {
                intentFilter = null;
            }
            IntentFilter _arg3 = intentFilter;
            Intent _result = registerReceiver(_arg0, _arg1, _arg2, _arg3, data.readString(), data.readInt(), data.readInt());
            reply.writeNoException();
            if (_result != null) {
                parcel2.writeInt(1);
                _result.writeToParcel(parcel2, 1);
            } else {
                parcel2.writeInt(0);
            }
            return true;
        }

        private boolean onTransact$broadcastIntent$(Parcel data, Parcel reply) throws RemoteException {
            Intent _arg1;
            Bundle _arg6;
            Bundle _arg9;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
            if (data.readInt() != 0) {
                _arg1 = Intent.CREATOR.createFromParcel(parcel);
            } else {
                _arg1 = null;
            }
            String _arg2 = data.readString();
            IIntentReceiver _arg3 = IIntentReceiver.Stub.asInterface(data.readStrongBinder());
            int _arg4 = data.readInt();
            String _arg5 = data.readString();
            if (data.readInt() != 0) {
                _arg6 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            } else {
                _arg6 = null;
            }
            String[] _arg7 = data.createStringArray();
            int _arg8 = data.readInt();
            if (data.readInt() != 0) {
                _arg9 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            } else {
                _arg9 = null;
            }
            int _result = broadcastIntent(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, data.readInt() != 0, data.readInt() != 0, data.readInt());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$finishReceiver$(Parcel data, Parcel reply) throws RemoteException {
            Bundle _arg3;
            data.enforceInterface(DESCRIPTOR);
            IBinder _arg0 = data.readStrongBinder();
            int _arg1 = data.readInt();
            String _arg2 = data.readString();
            if (data.readInt() != 0) {
                _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
            } else {
                _arg3 = null;
            }
            finishReceiver(_arg0, _arg1, _arg2, _arg3, data.readInt() != 0, data.readInt());
            return true;
        }

        private boolean onTransact$startService$(Parcel data, Parcel reply) throws RemoteException {
            Intent _arg1;
            data.enforceInterface(DESCRIPTOR);
            IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
            if (data.readInt() != 0) {
                _arg1 = Intent.CREATOR.createFromParcel(data);
            } else {
                _arg1 = null;
            }
            ComponentName _result = startService(_arg0, _arg1, data.readString(), data.readInt() != 0, data.readString(), data.readInt());
            reply.writeNoException();
            if (_result != null) {
                reply.writeInt(1);
                _result.writeToParcel(reply, 1);
            } else {
                reply.writeInt(0);
            }
            return true;
        }

        private boolean onTransact$bindService$(Parcel data, Parcel reply) throws RemoteException {
            Intent intent;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
            IBinder _arg1 = data.readStrongBinder();
            if (data.readInt() != 0) {
                intent = Intent.CREATOR.createFromParcel(parcel);
            } else {
                intent = null;
            }
            Intent _arg2 = intent;
            int _result = bindService(_arg0, _arg1, _arg2, data.readString(), IServiceConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString(), data.readInt());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$startInstrumentation$(Parcel data, Parcel reply) throws RemoteException {
            ComponentName _arg0;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            Bundle _arg3 = null;
            if (data.readInt() != 0) {
                _arg0 = ComponentName.CREATOR.createFromParcel(parcel);
            } else {
                _arg0 = null;
            }
            String _arg1 = data.readString();
            int _arg2 = data.readInt();
            if (data.readInt() != 0) {
                _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            }
            boolean _result = startInstrumentation(_arg0, _arg1, _arg2, _arg3, IInstrumentationWatcher.Stub.asInterface(data.readStrongBinder()), IUiAutomationConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$checkUriPermission$(Parcel data, Parcel reply) throws RemoteException {
            Uri uri;
            data.enforceInterface(DESCRIPTOR);
            if (data.readInt() != 0) {
                uri = Uri.CREATOR.createFromParcel(data);
            } else {
                uri = null;
            }
            Uri _arg0 = uri;
            int _result = checkUriPermission(_arg0, data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readStrongBinder());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$getIntentSender$(Parcel data, Parcel reply) throws RemoteException {
            Bundle _arg8;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            int _arg0 = data.readInt();
            String _arg1 = data.readString();
            IBinder _arg2 = data.readStrongBinder();
            String _arg3 = data.readString();
            int _arg4 = data.readInt();
            Intent[] _arg5 = (Intent[]) parcel.createTypedArray(Intent.CREATOR);
            String[] _arg6 = data.createStringArray();
            int _arg7 = data.readInt();
            IBinder iBinder = null;
            if (data.readInt() != 0) {
                _arg8 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            } else {
                _arg8 = null;
            }
            IIntentSender _result = getIntentSender(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, data.readInt());
            reply.writeNoException();
            if (_result != null) {
                iBinder = _result.asBinder();
            }
            reply.writeStrongBinder(iBinder);
            return true;
        }

        private boolean onTransact$noteWakeupAlarm$(Parcel data, Parcel reply) throws RemoteException {
            WorkSource workSource;
            data.enforceInterface(DESCRIPTOR);
            IIntentSender _arg0 = IIntentSender.Stub.asInterface(data.readStrongBinder());
            if (data.readInt() != 0) {
                workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
            } else {
                workSource = null;
            }
            WorkSource _arg1 = workSource;
            noteWakeupAlarm(_arg0, _arg1, data.readInt(), data.readString(), data.readString());
            reply.writeNoException();
            return true;
        }

        private boolean onTransact$setServiceForeground$(Parcel data, Parcel reply) throws RemoteException {
            ComponentName _arg0;
            data.enforceInterface(DESCRIPTOR);
            Notification _arg3 = null;
            if (data.readInt() != 0) {
                _arg0 = ComponentName.CREATOR.createFromParcel(data);
            } else {
                _arg0 = null;
            }
            IBinder _arg1 = data.readStrongBinder();
            int _arg2 = data.readInt();
            if (data.readInt() != 0) {
                _arg3 = Notification.CREATOR.createFromParcel(data);
            }
            setServiceForeground(_arg0, _arg1, _arg2, _arg3, data.readInt());
            reply.writeNoException();
            return true;
        }

        private boolean onTransact$profileControl$(Parcel data, Parcel reply) throws RemoteException {
            ProfilerInfo _arg3;
            data.enforceInterface(DESCRIPTOR);
            String _arg0 = data.readString();
            int _arg1 = data.readInt();
            boolean _arg2 = data.readInt() != 0;
            if (data.readInt() != 0) {
                _arg3 = ProfilerInfo.CREATOR.createFromParcel(data);
            } else {
                _arg3 = null;
            }
            boolean _result = profileControl(_arg0, _arg1, _arg2, _arg3, data.readInt());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$handleIncomingUser$(Parcel data, Parcel reply) throws RemoteException {
            data.enforceInterface(DESCRIPTOR);
            int _result = handleIncomingUser(data.readInt(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readString(), data.readString());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$startActivityIntentSender$(Parcel data, Parcel reply) throws RemoteException {
            Intent _arg3;
            Bundle _arg10;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
            IIntentSender _arg1 = IIntentSender.Stub.asInterface(data.readStrongBinder());
            IBinder _arg2 = data.readStrongBinder();
            if (data.readInt() != 0) {
                _arg3 = Intent.CREATOR.createFromParcel(parcel);
            } else {
                _arg3 = null;
            }
            String _arg4 = data.readString();
            IBinder _arg5 = data.readStrongBinder();
            String _arg6 = data.readString();
            int _arg7 = data.readInt();
            int _arg8 = data.readInt();
            int _arg9 = data.readInt();
            if (data.readInt() != 0) {
                _arg10 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            } else {
                _arg10 = null;
            }
            int _result = startActivityIntentSender(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, _arg10);
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$startActivityAndWait$(Parcel data, Parcel reply) throws RemoteException {
            Intent _arg2;
            ProfilerInfo _arg8;
            Bundle _arg9;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            parcel.enforceInterface(DESCRIPTOR);
            IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
            String _arg1 = data.readString();
            if (data.readInt() != 0) {
                _arg2 = Intent.CREATOR.createFromParcel(parcel);
            } else {
                _arg2 = null;
            }
            String _arg3 = data.readString();
            IBinder _arg4 = data.readStrongBinder();
            String _arg5 = data.readString();
            int _arg6 = data.readInt();
            int _arg7 = data.readInt();
            if (data.readInt() != 0) {
                _arg8 = ProfilerInfo.CREATOR.createFromParcel(parcel);
            } else {
                _arg8 = null;
            }
            if (data.readInt() != 0) {
                _arg9 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            } else {
                _arg9 = null;
            }
            WaitResult _result = startActivityAndWait(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, data.readInt());
            reply.writeNoException();
            if (_result != null) {
                parcel2.writeInt(1);
                _result.writeToParcel(parcel2, 1);
            } else {
                parcel2.writeInt(0);
            }
            return true;
        }

        private boolean onTransact$startActivityWithConfig$(Parcel data, Parcel reply) throws RemoteException {
            Intent _arg2;
            Configuration _arg8;
            Bundle _arg9;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
            String _arg1 = data.readString();
            if (data.readInt() != 0) {
                _arg2 = Intent.CREATOR.createFromParcel(parcel);
            } else {
                _arg2 = null;
            }
            String _arg3 = data.readString();
            IBinder _arg4 = data.readStrongBinder();
            String _arg5 = data.readString();
            int _arg6 = data.readInt();
            int _arg7 = data.readInt();
            if (data.readInt() != 0) {
                _arg8 = Configuration.CREATOR.createFromParcel(parcel);
            } else {
                _arg8 = null;
            }
            if (data.readInt() != 0) {
                _arg9 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            } else {
                _arg9 = null;
            }
            int _result = startActivityWithConfig(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, data.readInt());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$crashApplication$(Parcel data, Parcel reply) throws RemoteException {
            data.enforceInterface(DESCRIPTOR);
            crashApplication(data.readInt(), data.readInt(), data.readString(), data.readInt(), data.readString());
            reply.writeNoException();
            return true;
        }

        private boolean onTransact$grantUriPermissionFromOwner$(Parcel data, Parcel reply) throws RemoteException {
            Uri uri;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            IBinder _arg0 = data.readStrongBinder();
            int _arg1 = data.readInt();
            String _arg2 = data.readString();
            if (data.readInt() != 0) {
                uri = Uri.CREATOR.createFromParcel(parcel);
            } else {
                uri = null;
            }
            Uri _arg3 = uri;
            grantUriPermissionFromOwner(_arg0, _arg1, _arg2, _arg3, data.readInt(), data.readInt(), data.readInt());
            reply.writeNoException();
            return true;
        }

        private boolean onTransact$checkGrantUriPermission$(Parcel data, Parcel reply) throws RemoteException {
            Uri uri;
            data.enforceInterface(DESCRIPTOR);
            int _arg0 = data.readInt();
            String _arg1 = data.readString();
            if (data.readInt() != 0) {
                uri = Uri.CREATOR.createFromParcel(data);
            } else {
                uri = null;
            }
            Uri _arg2 = uri;
            int _result = checkGrantUriPermission(_arg0, _arg1, _arg2, data.readInt(), data.readInt());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$dumpHeap$(Parcel data, Parcel reply) throws RemoteException {
            ParcelFileDescriptor _arg6;
            data.enforceInterface(DESCRIPTOR);
            String _arg0 = data.readString();
            int _arg1 = data.readInt();
            boolean _arg2 = data.readInt() != 0;
            boolean _arg3 = data.readInt() != 0;
            boolean _arg4 = data.readInt() != 0;
            String _arg5 = data.readString();
            if (data.readInt() != 0) {
                _arg6 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
            } else {
                _arg6 = null;
            }
            boolean _result = dumpHeap(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6);
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$startActivities$(Parcel data, Parcel reply) throws RemoteException {
            Bundle _arg5;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
            String _arg1 = data.readString();
            Intent[] _arg2 = (Intent[]) parcel.createTypedArray(Intent.CREATOR);
            String[] _arg3 = data.createStringArray();
            IBinder _arg4 = data.readStrongBinder();
            if (data.readInt() != 0) {
                _arg5 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            } else {
                _arg5 = null;
            }
            int _result = startActivities(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, data.readInt());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$startActivityAsUser$(Parcel data, Parcel reply) throws RemoteException {
            Intent _arg2;
            ProfilerInfo _arg8;
            Bundle _arg9;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
            String _arg1 = data.readString();
            if (data.readInt() != 0) {
                _arg2 = Intent.CREATOR.createFromParcel(parcel);
            } else {
                _arg2 = null;
            }
            String _arg3 = data.readString();
            IBinder _arg4 = data.readStrongBinder();
            String _arg5 = data.readString();
            int _arg6 = data.readInt();
            int _arg7 = data.readInt();
            if (data.readInt() != 0) {
                _arg8 = ProfilerInfo.CREATOR.createFromParcel(parcel);
            } else {
                _arg8 = null;
            }
            if (data.readInt() != 0) {
                _arg9 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            } else {
                _arg9 = null;
            }
            int _result = startActivityAsUser(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, data.readInt());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$reportAssistContextExtras$(Parcel data, Parcel reply) throws RemoteException {
            Bundle _arg1;
            AssistStructure _arg2;
            AssistContent _arg3;
            Uri _arg4;
            data.enforceInterface(DESCRIPTOR);
            IBinder _arg0 = data.readStrongBinder();
            if (data.readInt() != 0) {
                _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
            } else {
                _arg1 = null;
            }
            if (data.readInt() != 0) {
                _arg2 = AssistStructure.CREATOR.createFromParcel(data);
            } else {
                _arg2 = null;
            }
            if (data.readInt() != 0) {
                _arg3 = AssistContent.CREATOR.createFromParcel(data);
            } else {
                _arg3 = null;
            }
            if (data.readInt() != 0) {
                _arg4 = Uri.CREATOR.createFromParcel(data);
            } else {
                _arg4 = null;
            }
            reportAssistContextExtras(_arg0, _arg1, _arg2, _arg3, _arg4);
            reply.writeNoException();
            return true;
        }

        private boolean onTransact$resizeStack$(Parcel data, Parcel reply) throws RemoteException {
            Rect _arg1;
            data.enforceInterface(DESCRIPTOR);
            int _arg0 = data.readInt();
            if (data.readInt() != 0) {
                _arg1 = Rect.CREATOR.createFromParcel(data);
            } else {
                _arg1 = null;
            }
            resizeStack(_arg0, _arg1, data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readInt());
            reply.writeNoException();
            return true;
        }

        private boolean onTransact$startVoiceActivity$(Parcel data, Parcel reply) throws RemoteException {
            Intent _arg3;
            ProfilerInfo _arg8;
            Bundle _arg9;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            String _arg0 = data.readString();
            int _arg1 = data.readInt();
            int _arg2 = data.readInt();
            if (data.readInt() != 0) {
                _arg3 = Intent.CREATOR.createFromParcel(parcel);
            } else {
                _arg3 = null;
            }
            String _arg4 = data.readString();
            IVoiceInteractionSession _arg5 = IVoiceInteractionSession.Stub.asInterface(data.readStrongBinder());
            IVoiceInteractor _arg6 = IVoiceInteractor.Stub.asInterface(data.readStrongBinder());
            int _arg7 = data.readInt();
            if (data.readInt() != 0) {
                _arg8 = ProfilerInfo.CREATOR.createFromParcel(parcel);
            } else {
                _arg8 = null;
            }
            if (data.readInt() != 0) {
                _arg9 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            } else {
                _arg9 = null;
            }
            int _result = startVoiceActivity(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, data.readInt());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$startAssistantActivity$(Parcel data, Parcel reply) throws RemoteException {
            Intent _arg3;
            Bundle _arg5;
            data.enforceInterface(DESCRIPTOR);
            String _arg0 = data.readString();
            int _arg1 = data.readInt();
            int _arg2 = data.readInt();
            if (data.readInt() != 0) {
                _arg3 = Intent.CREATOR.createFromParcel(data);
            } else {
                _arg3 = null;
            }
            String _arg4 = data.readString();
            if (data.readInt() != 0) {
                _arg5 = (Bundle) Bundle.CREATOR.createFromParcel(data);
            } else {
                _arg5 = null;
            }
            int _result = startAssistantActivity(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, data.readInt());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$startActivityAsCaller$(Parcel data, Parcel reply) throws RemoteException {
            Intent _arg2;
            ProfilerInfo _arg8;
            Bundle _arg9;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
            String _arg1 = data.readString();
            if (data.readInt() != 0) {
                _arg2 = Intent.CREATOR.createFromParcel(parcel);
            } else {
                _arg2 = null;
            }
            String _arg3 = data.readString();
            IBinder _arg4 = data.readStrongBinder();
            String _arg5 = data.readString();
            int _arg6 = data.readInt();
            int _arg7 = data.readInt();
            if (data.readInt() != 0) {
                _arg8 = ProfilerInfo.CREATOR.createFromParcel(parcel);
            } else {
                _arg8 = null;
            }
            if (data.readInt() != 0) {
                _arg9 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            } else {
                _arg9 = null;
            }
            int _result = startActivityAsCaller(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, data.readInt() != 0, data.readInt());
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$setExitInfo$(Parcel data, Parcel reply) throws RemoteException {
            Bitmap _arg4;
            data.enforceInterface(DESCRIPTOR);
            float _arg0 = data.readFloat();
            float _arg1 = data.readFloat();
            int _arg2 = data.readInt();
            int _arg3 = data.readInt();
            if (data.readInt() != 0) {
                _arg4 = Bitmap.CREATOR.createFromParcel(data);
            } else {
                _arg4 = null;
            }
            setExitInfo(_arg0, _arg1, _arg2, _arg3, _arg4, data.readInt());
            reply.writeNoException();
            return true;
        }

        private boolean onTransact$launchAssistIntent$(Parcel data, Parcel reply) throws RemoteException {
            Intent _arg0;
            data.enforceInterface(DESCRIPTOR);
            Bundle _arg4 = null;
            if (data.readInt() != 0) {
                _arg0 = Intent.CREATOR.createFromParcel(data);
            } else {
                _arg0 = null;
            }
            int _arg1 = data.readInt();
            String _arg2 = data.readString();
            int _arg3 = data.readInt();
            if (data.readInt() != 0) {
                _arg4 = (Bundle) Bundle.CREATOR.createFromParcel(data);
            }
            boolean _result = launchAssistIntent(_arg0, _arg1, _arg2, _arg3, _arg4);
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$requestAssistContextExtras$(Parcel data, Parcel reply) throws RemoteException {
            Bundle _arg2;
            data.enforceInterface(DESCRIPTOR);
            int _arg0 = data.readInt();
            IAssistDataReceiver _arg1 = IAssistDataReceiver.Stub.asInterface(data.readStrongBinder());
            if (data.readInt() != 0) {
                _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
            } else {
                _arg2 = null;
            }
            boolean _result = requestAssistContextExtras(_arg0, _arg1, _arg2, data.readStrongBinder(), data.readInt() != 0, data.readInt() != 0);
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$setTaskWindowingModeSplitScreenPrimary$(Parcel data, Parcel reply) throws RemoteException {
            Rect _arg4;
            data.enforceInterface(DESCRIPTOR);
            int _arg0 = data.readInt();
            int _arg1 = data.readInt();
            boolean _arg2 = data.readInt() != 0;
            boolean _arg3 = data.readInt() != 0;
            if (data.readInt() != 0) {
                _arg4 = Rect.CREATOR.createFromParcel(data);
            } else {
                _arg4 = null;
            }
            boolean _result = setTaskWindowingModeSplitScreenPrimary(_arg0, _arg1, _arg2, _arg3, _arg4, data.readInt() != 0);
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }

        private boolean onTransact$resizeDockedStack$(Parcel data, Parcel reply) throws RemoteException {
            Rect _arg0;
            Rect _arg1;
            Rect _arg2;
            Rect _arg3;
            data.enforceInterface(DESCRIPTOR);
            Rect _arg4 = null;
            if (data.readInt() != 0) {
                _arg0 = Rect.CREATOR.createFromParcel(data);
            } else {
                _arg0 = null;
            }
            if (data.readInt() != 0) {
                _arg1 = Rect.CREATOR.createFromParcel(data);
            } else {
                _arg1 = null;
            }
            if (data.readInt() != 0) {
                _arg2 = Rect.CREATOR.createFromParcel(data);
            } else {
                _arg2 = null;
            }
            if (data.readInt() != 0) {
                _arg3 = Rect.CREATOR.createFromParcel(data);
            } else {
                _arg3 = null;
            }
            if (data.readInt() != 0) {
                _arg4 = Rect.CREATOR.createFromParcel(data);
            }
            resizeDockedStack(_arg0, _arg1, _arg2, _arg3, _arg4);
            reply.writeNoException();
            return true;
        }

        private boolean onTransact$sendIntentSender$(Parcel data, Parcel reply) throws RemoteException {
            Intent _arg3;
            Bundle _arg7;
            Parcel parcel = data;
            parcel.enforceInterface(DESCRIPTOR);
            IIntentSender _arg0 = IIntentSender.Stub.asInterface(data.readStrongBinder());
            IBinder _arg1 = data.readStrongBinder();
            int _arg2 = data.readInt();
            if (data.readInt() != 0) {
                _arg3 = Intent.CREATOR.createFromParcel(parcel);
            } else {
                _arg3 = null;
            }
            String _arg4 = data.readString();
            IIntentReceiver _arg5 = IIntentReceiver.Stub.asInterface(data.readStrongBinder());
            String _arg6 = data.readString();
            if (data.readInt() != 0) {
                _arg7 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
            } else {
                _arg7 = null;
            }
            int _result = sendIntentSender(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7);
            reply.writeNoException();
            reply.writeInt(_result);
            return true;
        }
    }

    void activityDestroyed(IBinder iBinder) throws RemoteException;

    void activityIdle(IBinder iBinder, Configuration configuration, boolean z) throws RemoteException;

    void activityPaused(IBinder iBinder) throws RemoteException;

    void activityRelaunched(IBinder iBinder) throws RemoteException;

    void activityResumed(IBinder iBinder) throws RemoteException;

    void activitySlept(IBinder iBinder) throws RemoteException;

    void activityStopped(IBinder iBinder, Bundle bundle, PersistableBundle persistableBundle, CharSequence charSequence) throws RemoteException;

    int addAppTask(IBinder iBinder, Intent intent, ActivityManager.TaskDescription taskDescription, Bitmap bitmap) throws RemoteException;

    void addInstrumentationResults(IApplicationThread iApplicationThread, Bundle bundle) throws RemoteException;

    void addPackageDependency(String str) throws RemoteException;

    void alwaysShowUnsupportedCompileSdkWarning(ComponentName componentName) throws RemoteException;

    void appNotRespondingViaProvider(IBinder iBinder) throws RemoteException;

    void attachApplication(IApplicationThread iApplicationThread, long j) throws RemoteException;

    void backgroundWhitelistUid(int i) throws RemoteException;

    void backupAgentCreated(String str, IBinder iBinder) throws RemoteException;

    boolean bindBackupAgent(String str, int i, int i2) throws RemoteException;

    int bindService(IApplicationThread iApplicationThread, IBinder iBinder, Intent intent, String str, IServiceConnection iServiceConnection, int i, String str2, int i2) throws RemoteException;

    void bootAnimationComplete() throws RemoteException;

    int broadcastIntent(IApplicationThread iApplicationThread, Intent intent, String str, IIntentReceiver iIntentReceiver, int i, String str2, Bundle bundle, String[] strArr, int i2, Bundle bundle2, boolean z, boolean z2, int i3) throws RemoteException;

    void cancelIntentSender(IIntentSender iIntentSender) throws RemoteException;

    void cancelRecentsAnimation(boolean z) throws RemoteException;

    void cancelTaskWindowTransition(int i) throws RemoteException;

    int checkGrantUriPermission(int i, String str, Uri uri, int i2, int i3) throws RemoteException;

    int checkPermission(String str, int i, int i2) throws RemoteException;

    int checkPermissionWithToken(String str, int i, int i2, IBinder iBinder) throws RemoteException;

    int checkUriPermission(Uri uri, int i, int i2, int i3, int i4, IBinder iBinder) throws RemoteException;

    boolean clearApplicationUserData(String str, boolean z, IPackageDataObserver iPackageDataObserver, int i) throws RemoteException;

    void clearGrantedUriPermissions(String str, int i) throws RemoteException;

    void clearPendingBackup() throws RemoteException;

    void closeSystemDialogs(String str) throws RemoteException;

    boolean convertFromTranslucent(IBinder iBinder) throws RemoteException;

    boolean convertToTranslucent(IBinder iBinder, Bundle bundle) throws RemoteException;

    void crashApplication(int i, int i2, String str, int i3, String str2) throws RemoteException;

    int createStackOnDisplay(int i) throws RemoteException;

    void dismissKeyguard(IBinder iBinder, IKeyguardDismissCallback iKeyguardDismissCallback, CharSequence charSequence) throws RemoteException;

    void dismissPip(boolean z, int i) throws RemoteException;

    void dismissSplitScreenMode(boolean z) throws RemoteException;

    boolean dumpHeap(String str, int i, boolean z, boolean z2, boolean z3, String str2, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void dumpHeapFinished(String str) throws RemoteException;

    boolean enterPictureInPictureMode(IBinder iBinder, PictureInPictureParams pictureInPictureParams) throws RemoteException;

    void enterSafeMode() throws RemoteException;

    void exitFreeformMode(IBinder iBinder) throws RemoteException;

    boolean finishActivity(IBinder iBinder, int i, Intent intent, int i2) throws RemoteException;

    boolean finishActivityAffinity(IBinder iBinder) throws RemoteException;

    void finishHeavyWeightApp() throws RemoteException;

    void finishInstrumentation(IApplicationThread iApplicationThread, int i, Bundle bundle) throws RemoteException;

    void finishReceiver(IBinder iBinder, int i, String str, Bundle bundle, boolean z, int i2) throws RemoteException;

    void finishSubActivity(IBinder iBinder, String str, int i) throws RemoteException;

    void finishVoiceTask(IVoiceInteractionSession iVoiceInteractionSession) throws RemoteException;

    void forceStopPackage(String str, int i) throws RemoteException;

    ComponentName getActivityClassForToken(IBinder iBinder) throws RemoteException;

    int getActivityDisplayId(IBinder iBinder) throws RemoteException;

    Bundle getActivityOptions(IBinder iBinder) throws RemoteException;

    List<ActivityManager.StackInfo> getAllStackInfos() throws RemoteException;

    Point getAppTaskThumbnailSize() throws RemoteException;

    List<IBinder> getAppTasks(String str) throws RemoteException;

    Bundle getAssistContextExtras(int i) throws RemoteException;

    ComponentName getCallingActivity(IBinder iBinder) throws RemoteException;

    String getCallingPackage(IBinder iBinder) throws RemoteException;

    Configuration getConfiguration() throws RemoteException;

    ContentProviderHolder getContentProvider(IApplicationThread iApplicationThread, String str, int i, boolean z) throws RemoteException;

    ContentProviderHolder getContentProviderExternal(String str, int i, IBinder iBinder) throws RemoteException;

    UserInfo getCurrentUser() throws RemoteException;

    ConfigurationInfo getDeviceConfigurationInfo() throws RemoteException;

    List<ActivityManager.RunningTaskInfo> getFilteredTasks(int i, int i2, int i3) throws RemoteException;

    ActivityManager.StackInfo getFocusedStackInfo() throws RemoteException;

    int getFrontActivityScreenCompatMode() throws RemoteException;

    ParceledListSlice getGrantedUriPermissions(String str, int i) throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    Intent getIntentForIntentSender(IIntentSender iIntentSender) throws RemoteException;

    IIntentSender getIntentSender(int i, String str, IBinder iBinder, String str2, int i2, Intent[] intentArr, String[] strArr, int i3, Bundle bundle, int i4) throws RemoteException;

    int getLastResumedActivityUserId() throws RemoteException;

    String getLaunchedFromPackage(IBinder iBinder) throws RemoteException;

    int getLaunchedFromUid(IBinder iBinder) throws RemoteException;

    int getLockTaskModeState() throws RemoteException;

    int getMaxNumPictureInPictureActions(IBinder iBinder) throws RemoteException;

    void getMemoryInfo(ActivityManager.MemoryInfo memoryInfo) throws RemoteException;

    int getMemoryTrimLevel() throws RemoteException;

    void getMyMemoryState(ActivityManager.RunningAppProcessInfo runningAppProcessInfo) throws RemoteException;

    boolean getPackageAskScreenCompat(String str) throws RemoteException;

    String getPackageForIntentSender(IIntentSender iIntentSender) throws RemoteException;

    String getPackageForToken(IBinder iBinder) throws RemoteException;

    int getPackageProcessState(String str, String str2) throws RemoteException;

    int getPackageScreenCompatMode(String str) throws RemoteException;

    ParceledListSlice getPersistedUriPermissions(String str, boolean z) throws RemoteException;

    int getProcessLimit() throws RemoteException;

    Debug.MemoryInfo[] getProcessMemoryInfo(int[] iArr) throws RemoteException;

    long[] getProcessPss(int[] iArr) throws RemoteException;

    List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState() throws RemoteException;

    String getProviderMimeType(Uri uri, int i) throws RemoteException;

    ParceledListSlice getRecentTasks(int i, int i2, int i3) throws RemoteException;

    int getRequestedOrientation(IBinder iBinder) throws RemoteException;

    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException;

    List<ApplicationInfo> getRunningExternalApplications() throws RemoteException;

    PendingIntent getRunningServiceControlPanel(ComponentName componentName) throws RemoteException;

    int[] getRunningUserIds() throws RemoteException;

    List<ActivityManager.RunningServiceInfo> getServices(int i, int i2) throws RemoteException;

    ActivityManager.StackInfo getStackInfo(int i, int i2) throws RemoteException;

    String getTagForIntentSender(IIntentSender iIntentSender, String str) throws RemoteException;

    Rect getTaskBounds(int i) throws RemoteException;

    ActivityManager.TaskDescription getTaskDescription(int i) throws RemoteException;

    Bitmap getTaskDescriptionIcon(String str, int i) throws RemoteException;

    int getTaskForActivity(IBinder iBinder, boolean z) throws RemoteException;

    ActivityManager.TaskSnapshot getTaskSnapshot(int i, boolean z) throws RemoteException;

    List<ActivityManager.RunningTaskInfo> getTasks(int i) throws RemoteException;

    int getUidForIntentSender(IIntentSender iIntentSender) throws RemoteException;

    int getUidProcessState(int i, String str) throws RemoteException;

    IBinder getUriPermissionOwnerForActivity(IBinder iBinder) throws RemoteException;

    void grantUriPermission(IApplicationThread iApplicationThread, String str, Uri uri, int i, int i2) throws RemoteException;

    void grantUriPermissionFromOwner(IBinder iBinder, int i, String str, Uri uri, int i2, int i3, int i4) throws RemoteException;

    void handleApplicationCrash(IBinder iBinder, ApplicationErrorReport.ParcelableCrashInfo parcelableCrashInfo) throws RemoteException;

    void handleApplicationStrictModeViolation(IBinder iBinder, int i, StrictMode.ViolationInfo violationInfo) throws RemoteException;

    boolean handleApplicationWtf(IBinder iBinder, String str, boolean z, ApplicationErrorReport.ParcelableCrashInfo parcelableCrashInfo) throws RemoteException;

    int handleIncomingUser(int i, int i2, int i3, boolean z, boolean z2, String str, String str2) throws RemoteException;

    void hang(IBinder iBinder, boolean z) throws RemoteException;

    long inputDispatchingTimedOut(int i, boolean z, String str) throws RemoteException;

    boolean isAppForeground(int i) throws RemoteException;

    boolean isAppStartModeDisabled(int i, String str) throws RemoteException;

    boolean isAssistDataAllowedOnCurrentActivity() throws RemoteException;

    boolean isBackgroundRestricted(String str) throws RemoteException;

    boolean isImmersive(IBinder iBinder) throws RemoteException;

    boolean isInLockTaskMode() throws RemoteException;

    boolean isInMultiWindowMode(IBinder iBinder) throws RemoteException;

    boolean isInPictureInPictureMode(IBinder iBinder) throws RemoteException;

    boolean isIntentSenderAForegroundService(IIntentSender iIntentSender) throws RemoteException;

    boolean isIntentSenderAnActivity(IIntentSender iIntentSender) throws RemoteException;

    boolean isIntentSenderTargetedToPackage(IIntentSender iIntentSender) throws RemoteException;

    boolean isRootVoiceInteraction(IBinder iBinder) throws RemoteException;

    boolean isTopActivityImmersive() throws RemoteException;

    boolean isTopOfTask(IBinder iBinder) throws RemoteException;

    boolean isUidActive(int i, String str) throws RemoteException;

    boolean isUserAMonkey() throws RemoteException;

    boolean isUserRunning(int i, int i2) throws RemoteException;

    boolean isVrModePackageEnabled(ComponentName componentName) throws RemoteException;

    void keyguardGoingAway(int i) throws RemoteException;

    void killAllBackgroundProcesses() throws RemoteException;

    void killApplication(String str, int i, int i2, String str2) throws RemoteException;

    void killApplicationProcess(String str, int i) throws RemoteException;

    void killBackgroundProcesses(String str, int i) throws RemoteException;

    void killPackageDependents(String str, int i) throws RemoteException;

    boolean killPids(int[] iArr, String str, boolean z) throws RemoteException;

    boolean killProcessesBelowForeground(String str) throws RemoteException;

    void killUid(int i, int i2, String str) throws RemoteException;

    boolean launchAssistIntent(Intent intent, int i, String str, int i2, Bundle bundle) throws RemoteException;

    void makePackageIdle(String str, int i) throws RemoteException;

    boolean moveActivityTaskToBack(IBinder iBinder, boolean z) throws RemoteException;

    void moveStackToDisplay(int i, int i2) throws RemoteException;

    void moveTaskBackwards(int i) throws RemoteException;

    void moveTaskToFront(int i, int i2, Bundle bundle) throws RemoteException;

    void moveTaskToStack(int i, int i2, boolean z) throws RemoteException;

    void moveTasksToFullscreenStack(int i, boolean z) throws RemoteException;

    boolean moveTopActivityToPinnedStack(int i, Rect rect) throws RemoteException;

    boolean navigateUpTo(IBinder iBinder, Intent intent, int i, Intent intent2) throws RemoteException;

    IBinder newUriPermissionOwner(String str) throws RemoteException;

    void noteAlarmFinish(IIntentSender iIntentSender, WorkSource workSource, int i, String str) throws RemoteException;

    void noteAlarmStart(IIntentSender iIntentSender, WorkSource workSource, int i, String str) throws RemoteException;

    void noteWakeupAlarm(IIntentSender iIntentSender, WorkSource workSource, int i, String str, String str2) throws RemoteException;

    void notifyActivityDrawn(IBinder iBinder) throws RemoteException;

    void notifyCleartextNetwork(int i, byte[] bArr) throws RemoteException;

    void notifyEnterAnimationComplete(IBinder iBinder) throws RemoteException;

    void notifyLaunchTaskBehindComplete(IBinder iBinder) throws RemoteException;

    void notifyLockedProfile(int i) throws RemoteException;

    void notifyPinnedStackAnimationEnded() throws RemoteException;

    void notifyPinnedStackAnimationStarted() throws RemoteException;

    ParcelFileDescriptor openContentUri(String str) throws RemoteException;

    void overridePendingTransition(IBinder iBinder, String str, int i, int i2) throws RemoteException;

    IBinder peekService(Intent intent, String str, String str2) throws RemoteException;

    void performIdleMaintenance() throws RemoteException;

    void positionTaskInStack(int i, int i2, int i3) throws RemoteException;

    boolean profileControl(String str, int i, boolean z, ProfilerInfo profilerInfo, int i2) throws RemoteException;

    void publishContentProviders(IApplicationThread iApplicationThread, List<ContentProviderHolder> list) throws RemoteException;

    void publishService(IBinder iBinder, Intent intent, IBinder iBinder2) throws RemoteException;

    boolean refContentProvider(IBinder iBinder, int i, int i2) throws RemoteException;

    void registerIntentSenderCancelListener(IIntentSender iIntentSender, IResultReceiver iResultReceiver) throws RemoteException;

    void registerProcessObserver(IProcessObserver iProcessObserver) throws RemoteException;

    Intent registerReceiver(IApplicationThread iApplicationThread, String str, IIntentReceiver iIntentReceiver, IntentFilter intentFilter, String str2, int i, int i2) throws RemoteException;

    void registerRemoteAnimationForNextActivityStart(String str, RemoteAnimationAdapter remoteAnimationAdapter) throws RemoteException;

    void registerRemoteAnimations(IBinder iBinder, RemoteAnimationDefinition remoteAnimationDefinition) throws RemoteException;

    void registerTaskStackListener(ITaskStackListener iTaskStackListener) throws RemoteException;

    void registerUidObserver(IUidObserver iUidObserver, int i, int i2, String str) throws RemoteException;

    void registerUserSwitchObserver(IUserSwitchObserver iUserSwitchObserver, String str) throws RemoteException;

    boolean releaseActivityInstance(IBinder iBinder) throws RemoteException;

    void releasePersistableUriPermission(Uri uri, int i, String str, int i2) throws RemoteException;

    void releaseSomeActivities(IApplicationThread iApplicationThread) throws RemoteException;

    void removeContentProvider(IBinder iBinder, boolean z) throws RemoteException;

    void removeContentProviderExternal(String str, IBinder iBinder) throws RemoteException;

    void removeStack(int i) throws RemoteException;

    void removeStacksInWindowingModes(int[] iArr) throws RemoteException;

    void removeStacksWithActivityTypes(int[] iArr) throws RemoteException;

    boolean removeTask(int i) throws RemoteException;

    void reportActivityFullyDrawn(IBinder iBinder, boolean z) throws RemoteException;

    void reportAssistContextExtras(IBinder iBinder, Bundle bundle, AssistStructure assistStructure, AssistContent assistContent, Uri uri) throws RemoteException;

    void reportSizeConfigurations(IBinder iBinder, int[] iArr, int[] iArr2, int[] iArr3) throws RemoteException;

    boolean requestAssistContextExtras(int i, IAssistDataReceiver iAssistDataReceiver, Bundle bundle, IBinder iBinder, boolean z, boolean z2) throws RemoteException;

    boolean requestAutofillData(IAssistDataReceiver iAssistDataReceiver, Bundle bundle, IBinder iBinder, int i) throws RemoteException;

    void requestBugReport(int i) throws RemoteException;

    void requestTelephonyBugReport(String str, String str2) throws RemoteException;

    void requestWifiBugReport(String str, String str2) throws RemoteException;

    void resizeDockedStack(Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5) throws RemoteException;

    void resizePinnedStack(Rect rect, Rect rect2) throws RemoteException;

    void resizeStack(int i, Rect rect, boolean z, boolean z2, boolean z3, int i2) throws RemoteException;

    void resizeTask(int i, Rect rect, int i2) throws RemoteException;

    void restart() throws RemoteException;

    int restartUserInBackground(int i) throws RemoteException;

    void resumeAppSwitches() throws RemoteException;

    void revokeUriPermission(IApplicationThread iApplicationThread, String str, Uri uri, int i, int i2) throws RemoteException;

    void revokeUriPermissionFromOwner(IBinder iBinder, Uri uri, int i, int i2) throws RemoteException;

    void scheduleApplicationInfoChanged(List<String> list, int i) throws RemoteException;

    void sendIdleJobTrigger() throws RemoteException;

    int sendIntentSender(IIntentSender iIntentSender, IBinder iBinder, int i, Intent intent, String str, IIntentReceiver iIntentReceiver, String str2, Bundle bundle) throws RemoteException;

    void serviceDoneExecuting(IBinder iBinder, int i, int i2, int i3) throws RemoteException;

    void setActivityController(IActivityController iActivityController, boolean z) throws RemoteException;

    void setAgentApp(String str, String str2) throws RemoteException;

    void setAlwaysFinish(boolean z) throws RemoteException;

    void setDebugApp(String str, boolean z, boolean z2) throws RemoteException;

    void setDisablePreviewScreenshots(IBinder iBinder, boolean z) throws RemoteException;

    void setDumpHeapDebugLimit(String str, int i, long j, String str2) throws RemoteException;

    void setExitInfo(float f, float f2, int i, int i2, Bitmap bitmap, int i3) throws RemoteException;

    void setFocusedStack(int i) throws RemoteException;

    void setFocusedTask(int i) throws RemoteException;

    void setFrontActivityScreenCompatMode(int i) throws RemoteException;

    void setHasTopUi(boolean z) throws RemoteException;

    void setImmersive(IBinder iBinder, boolean z) throws RemoteException;

    void setLockScreenShown(boolean z, boolean z2, int i) throws RemoteException;

    void setPackageAskScreenCompat(String str, boolean z) throws RemoteException;

    void setPackageScreenCompatMode(String str, int i) throws RemoteException;

    void setPersistentVrThread(int i) throws RemoteException;

    void setPictureInPictureParams(IBinder iBinder, PictureInPictureParams pictureInPictureParams) throws RemoteException;

    void setProcessImportant(IBinder iBinder, int i, boolean z, String str) throws RemoteException;

    void setProcessLimit(int i) throws RemoteException;

    boolean setProcessMemoryTrimLevel(String str, int i, int i2) throws RemoteException;

    void setRenderThread(int i) throws RemoteException;

    void setRequestedOrientation(IBinder iBinder, int i) throws RemoteException;

    void setServiceForeground(ComponentName componentName, IBinder iBinder, int i, Notification notification, int i2) throws RemoteException;

    void setShowWhenLocked(IBinder iBinder, boolean z) throws RemoteException;

    void setSplitScreenResizing(boolean z) throws RemoteException;

    void setTaskDescription(IBinder iBinder, ActivityManager.TaskDescription taskDescription) throws RemoteException;

    void setTaskResizeable(int i, int i2) throws RemoteException;

    void setTaskWindowingMode(int i, int i2, boolean z) throws RemoteException;

    boolean setTaskWindowingModeSplitScreenPrimary(int i, int i2, boolean z, boolean z2, Rect rect, boolean z3) throws RemoteException;

    void setTurnScreenOn(IBinder iBinder, boolean z) throws RemoteException;

    void setUserIsMonkey(boolean z) throws RemoteException;

    void setVoiceKeepAwake(IVoiceInteractionSession iVoiceInteractionSession, boolean z) throws RemoteException;

    int setVrMode(IBinder iBinder, boolean z, ComponentName componentName) throws RemoteException;

    void setVrThread(int i) throws RemoteException;

    boolean shouldUpRecreateTask(IBinder iBinder, String str) throws RemoteException;

    boolean showAssistFromActivity(IBinder iBinder, Bundle bundle) throws RemoteException;

    void showBootMessage(CharSequence charSequence, boolean z) throws RemoteException;

    void showLockTaskEscapeMessage(IBinder iBinder) throws RemoteException;

    void showWaitingForDebugger(IApplicationThread iApplicationThread, boolean z) throws RemoteException;

    boolean shutdown(int i) throws RemoteException;

    void signalPersistentProcesses(int i) throws RemoteException;

    int startActivities(IApplicationThread iApplicationThread, String str, Intent[] intentArr, String[] strArr, IBinder iBinder, Bundle bundle, int i) throws RemoteException;

    int startActivity(IApplicationThread iApplicationThread, String str, Intent intent, String str2, IBinder iBinder, String str3, int i, int i2, ProfilerInfo profilerInfo, Bundle bundle) throws RemoteException;

    WaitResult startActivityAndWait(IApplicationThread iApplicationThread, String str, Intent intent, String str2, IBinder iBinder, String str3, int i, int i2, ProfilerInfo profilerInfo, Bundle bundle, int i3) throws RemoteException;

    int startActivityAsCaller(IApplicationThread iApplicationThread, String str, Intent intent, String str2, IBinder iBinder, String str3, int i, int i2, ProfilerInfo profilerInfo, Bundle bundle, boolean z, int i3) throws RemoteException;

    int startActivityAsUser(IApplicationThread iApplicationThread, String str, Intent intent, String str2, IBinder iBinder, String str3, int i, int i2, ProfilerInfo profilerInfo, Bundle bundle, int i3) throws RemoteException;

    int startActivityFromRecents(int i, Bundle bundle) throws RemoteException;

    int startActivityIntentSender(IApplicationThread iApplicationThread, IIntentSender iIntentSender, IBinder iBinder, Intent intent, String str, IBinder iBinder2, String str2, int i, int i2, int i3, Bundle bundle) throws RemoteException;

    int startActivityWithConfig(IApplicationThread iApplicationThread, String str, Intent intent, String str2, IBinder iBinder, String str3, int i, int i2, Configuration configuration, Bundle bundle, int i3) throws RemoteException;

    int startAssistantActivity(String str, int i, int i2, Intent intent, String str2, Bundle bundle, int i3) throws RemoteException;

    boolean startBinderTracking() throws RemoteException;

    void startConfirmDeviceCredentialIntent(Intent intent, Bundle bundle) throws RemoteException;

    void startInPlaceAnimationOnFrontMostApplication(Bundle bundle) throws RemoteException;

    boolean startInstrumentation(ComponentName componentName, String str, int i, Bundle bundle, IInstrumentationWatcher iInstrumentationWatcher, IUiAutomationConnection iUiAutomationConnection, int i2, String str2) throws RemoteException;

    void startLocalVoiceInteraction(IBinder iBinder, Bundle bundle) throws RemoteException;

    void startLockTaskModeByToken(IBinder iBinder) throws RemoteException;

    boolean startNextMatchingActivity(IBinder iBinder, Intent intent, Bundle bundle) throws RemoteException;

    void startRecentsActivity(Intent intent, IAssistDataReceiver iAssistDataReceiver, IRecentsAnimationRunner iRecentsAnimationRunner) throws RemoteException;

    ComponentName startService(IApplicationThread iApplicationThread, Intent intent, String str, boolean z, String str2, int i) throws RemoteException;

    void startSystemLockTaskMode(int i) throws RemoteException;

    boolean startUserInBackground(int i) throws RemoteException;

    boolean startUserInBackgroundWithListener(int i, IProgressListener iProgressListener) throws RemoteException;

    int startVoiceActivity(String str, int i, int i2, Intent intent, String str2, IVoiceInteractionSession iVoiceInteractionSession, IVoiceInteractor iVoiceInteractor, int i3, ProfilerInfo profilerInfo, Bundle bundle, int i4) throws RemoteException;

    void stopAppSwitches() throws RemoteException;

    boolean stopBinderTrackingAndDump(ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void stopLocalVoiceInteraction(IBinder iBinder) throws RemoteException;

    void stopLockTaskModeByToken(IBinder iBinder) throws RemoteException;

    int stopService(IApplicationThread iApplicationThread, Intent intent, String str, int i) throws RemoteException;

    boolean stopServiceToken(ComponentName componentName, IBinder iBinder, int i) throws RemoteException;

    void stopSystemLockTaskMode() throws RemoteException;

    int stopUser(int i, boolean z, IStopUserCallback iStopUserCallback) throws RemoteException;

    boolean supportsLocalVoiceInteraction() throws RemoteException;

    void suppressResizeConfigChanges(boolean z) throws RemoteException;

    void swapDockedAndFullscreenStack() throws RemoteException;

    boolean switchUser(int i) throws RemoteException;

    void takePersistableUriPermission(Uri uri, int i, String str, int i2) throws RemoteException;

    void unbindBackupAgent(ApplicationInfo applicationInfo) throws RemoteException;

    void unbindFinished(IBinder iBinder, Intent intent, boolean z) throws RemoteException;

    boolean unbindService(IServiceConnection iServiceConnection) throws RemoteException;

    void unbroadcastIntent(IApplicationThread iApplicationThread, Intent intent, int i) throws RemoteException;

    void unhandledBack() throws RemoteException;

    boolean unlockUser(int i, byte[] bArr, byte[] bArr2, IProgressListener iProgressListener) throws RemoteException;

    void unregisterIntentSenderCancelListener(IIntentSender iIntentSender, IResultReceiver iResultReceiver) throws RemoteException;

    void unregisterProcessObserver(IProcessObserver iProcessObserver) throws RemoteException;

    void unregisterReceiver(IIntentReceiver iIntentReceiver) throws RemoteException;

    void unregisterTaskStackListener(ITaskStackListener iTaskStackListener) throws RemoteException;

    void unregisterUidObserver(IUidObserver iUidObserver) throws RemoteException;

    void unregisterUserSwitchObserver(IUserSwitchObserver iUserSwitchObserver) throws RemoteException;

    void unstableProviderDied(IBinder iBinder) throws RemoteException;

    boolean updateConfiguration(Configuration configuration) throws RemoteException;

    void updateDeviceOwner(String str) throws RemoteException;

    boolean updateDisplayOverrideConfiguration(Configuration configuration, int i) throws RemoteException;

    void updateLockTaskFeatures(int i, int i2) throws RemoteException;

    void updateLockTaskPackages(int i, String[] strArr) throws RemoteException;

    void updatePersistentConfiguration(Configuration configuration) throws RemoteException;

    void waitForNetworkStateUpdate(long j) throws RemoteException;

    boolean willActivityBeVisible(IBinder iBinder) throws RemoteException;
}
