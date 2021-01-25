package huawei.android.security.secai.hookcase.utils;

import com.huawei.hsm.permission.StubController;

public class BehaviorIdCast {
    private static final int ACCESSIBILITY = 50;
    private static final int ACCOUNTMANAGER = 37;
    private static final int ACTIVITYMANAGER = 35;
    private static final int ACTIVITYSTACK = 47;
    private static final int ACTIVITYSTARTER = 34;
    private static final int ALERTDIALOG = 43;
    private static final int APISEQ_OFFSET = 10;
    private static final int APPLICATION_INSTALL = 1;
    private static final int APPLICATION_PROCESS_DIED = 4;
    private static final int APPLICATION_PROCESS_START = 3;
    private static final int APPLICATION_UNINSTALL = 2;
    private static final int APPOPS = 32;
    private static final int APPWIGDET = 38;
    private static final int AUDIO = 31;
    private static final int AUDIORECORD = 49;
    private static final int BASEBUNDLE = 57;
    private static final int BATTERYSTATS = 33;
    private static final int BEHAVIOR_APISEQ_WIDTH = 8;
    private static final int BEHAVIOR_PARAM_WIDTH = 10;
    private static final int BROADCASTQUEUE = 36;
    private static final int BROADCASTRECEIVER = 58;
    private static final int CAMERA = 48;
    private static final int CIPHER = 52;
    private static final int CLASS = 54;
    private static final int CLIPBOARD = 30;
    private static final int CONNECTIVITY = 28;
    private static final int CONSUMERIR = 27;
    private static final int CONTENT = 29;
    private static final int CONTENTPROVIDER = 39;
    private static final int COUNTRYDETECTOR = 26;
    private static final int DEVICEPOLICYMANAGER = 8;
    private static final int DEXFILE = 41;
    private static final int DOCUMENT = 51;
    private static final int EXECUTE = 46;
    private static final int HTTP = 44;
    private static final int JOBSCHEDULER = 25;
    private static final int LAUNCHERAPPS = 17;
    private static final int LOAD_ELF = 42;
    private static final int LOCATIONMANAGER = 24;
    private static final int LOCKSETTINGS = 23;
    private static final int MEDIASESSION = 22;
    private static final int MMSBROKER = 21;
    private static final int MODULEID_OFFSET = 18;
    private static final int MODULE_ID_START = 0;
    private static final int NETWORKMANAGEMENT = 19;
    private static final int NETWORKPOLICYMANAGER = 20;
    private static final int NOTIFICATIONMANAGER = 18;
    private static final int PACKAGEINSTALLER = 16;
    private static final int PACKAGEMANAGER = 15;
    private static final int PHONEINTERFACEMANAGER = 1;
    private static final int PROXY = 53;
    private static final int RESTRICTIONSMANAGER = 7;
    private static final int SMSMESSAGEBASE = 56;
    private static final int SOCKET = 45;
    private static final int STATUSBARMANAGER = 13;
    private static final int SYSTEM_PROPERTIES = 55;
    private static final int TELEPHONYPHONESUBINFO = 4;
    private static final int TELEPHONYREGISTRY = 12;
    private static final int TELEPHONYSUBSCRIPTION = 3;
    private static final int TELEPHONYUICCSMS = 2;
    private static final int USAGESTATES = 6;
    private static final int USERMANAGER = 14;
    private static final int VIBRATOR = 11;
    private static final int WALLPAPERMANAGER = 10;
    private static final int WEBVIEW = 40;
    private static final int WIFI = 5;
    private static final int WINDOWNMANAGER = 9;

    enum AccessibilityApi {
        onaccessibilityevent,
        dispatchServiceConnected
    }

    enum AccountManagerApi {
        removeAccountInternal,
        getAccountsInternal,
        addAccountInternal,
        addAccount
    }

    enum ActivityManagerApi {
        updateConfiguration,
        unregisterReceiver,
        unbindService,
        setTaskDescription,
        setRequestedOrientation,
        navigateUpTo,
        moveTaskToFront,
        killBackgroundProcesses,
        getRunningAppProcesses,
        getRecentTasks,
        getProcessMemoryInfo,
        getProcessesInErrorState,
        getAppTasks,
        finishActivity,
        closeSystemDialogs,
        broadcastIntent,
        bindService,
        forceStopPackageAsUser,
        getFilteredTasks
    }

    enum ActivityStackApi {
        registerActivityLifecycleCallbacks
    }

    enum ActivityStarterApi {
        startActivityMayWait
    }

    enum AlertDialogApi {
        setTitle,
        setCustomTitle,
        setMessage,
        setIcon,
        makeText
    }

    enum AppOpsApi {
        setMode
    }

    enum AppWidgetApi {
        bindAppWidgetId,
        createAppWidgetConfigIntentSender
    }

    enum AudioApi {
        setStreamVolume,
        setSpeakerphoneOn,
        setMode,
        setMicrophoneMute,
        setMasterMute,
        requestAudioFocus,
        playSoundEffectVolume,
        adjustStreamVolume
    }

    enum AudioRecordApi {
        startRecording,
        stop
    }

    enum BaseBundleApi {
        get
    }

    enum BatteryStatsApi {
        takeUidSnapshots
    }

    enum BroadcastQueueApi {
        processCurBroadcastLocked,
        performReceiveLocked,
        finishReceiverLocked
    }

    enum BroadcastReceiverApi {
        abortBroadcast
    }

    enum CameraApi {
        open,
        capture,
        startFaceDetection,
        disableShutterSound,
        captureBurst,
        setTorchMode,
        prepare
    }

    enum CipherApi {
        encode0,
        encode,
        create_cipher
    }

    enum ClassApi {
        loadClass,
        forName
    }

    enum ClipboardApi {
        getPrimaryClip,
        addPrimaryClipChangedListener,
        setPrimaryClip
    }

    enum ConnectivityApi {
        requestNetwork,
        prepareVpn,
        isActiveNetworkMetered,
        getNetworkInfo,
        getNetworkCapabilities,
        getLinkProperties,
        getAllNetworks,
        getAllNetworkInfo,
        getActiveNetworkInfo
    }

    enum ConsumerIrApi {
        transmit
    }

    enum ContentApi {
        registerContentObserver
    }

    enum ContentProviderApi {
        query,
        update,
        insert,
        delete
    }

    enum CountryDetectorApi {
        detectCountry
    }

    enum DevicePolicyManagerApi {
        uninstallCaCerts,
        removeActiveAdmin,
        installCaCert,
        createAndManageUser,
        resetPassword
    }

    enum DexFileApi {
        loadDex,
        DexFile,
        BaseDexClassLoader,
        DexPathList
    }

    enum DocumentApi {
        document_query,
        contentresolve_query
    }

    enum ExecuteApi {
        start
    }

    enum HttpApi {
        openConnection,
        connect
    }

    enum JobSchedulerApi {
        schedule
    }

    enum LauncherAppsApi {
        startShortcut,
        pinShortcuts,
        getLauncherActivities,
        addOnAppsChangedListener
    }

    enum LoadElfApi {
        loadlibrary0,
        load0
    }

    enum LocationManagerApi {
        sendExtraCommand,
        requestLocationUpdates,
        requestGeofence,
        removeUpdates,
        registerGnssStatusCallback,
        isProviderEnabled,
        getProviders,
        getLastLocation,
        getBestProvider
    }

    enum LockSettingsApi {
        setString,
        setLong
    }

    enum MediaSessionApi {
        dispatchAdjustVolume
    }

    enum MmsBrokerApi {
        sendMessage
    }

    enum NetworkManagementApi {
        registerObserver,
        addRoute
    }

    enum NetworkPolicyManagerApi {
        registerListener
    }

    enum NotificationManagerApi {
        enqueueToast,
        enqueueNotificationWithTag,
        cancelNotificationWithTag
    }

    enum PackageInstallerApi {
        uninstall,
        commit
    }

    enum PackageManagerApi {
        setComponentEnabledSetting,
        setApplicationEnabledSetting,
        getPermissionInfo,
        getInstalledPackages,
        getInstalledApplications,
        clearPackagePreferredActivities,
        addPreferredActivity
    }

    enum PhoneInterfaceManagerApi {
        getLine1NumberForDisplay,
        getImeiForSlot,
        getDeviceId,
        getActivePhoneTypeForSlot
    }

    enum PhoneSubInfoApi {
        getVoiceMailNumberForSubscriber,
        getVoiceMailAlphaTagForSubscriber,
        getSubscriberIdForSubscriber,
        getLine1NumberForSubscriber,
        getIccSerialNumberForSubscriber,
        getGroupIdLevel1ForSubscriber
    }

    enum ProxyApi {
        Proxy
    }

    enum RestrictionsManagerApi {
        requestPermission,
        notifyPermissionResponse
    }

    enum SmsMessageBaseApi {
        getOriginatingAddress,
        getMessageBody
    }

    enum SocketApi {
        connect
    }

    enum StatusBarManagerApi {
        setSystemUiVisibility,
        setIconVisibility,
        setIcon,
        disable
    }

    enum SubScriptionApi {
        getActiveSubscriptionInfoList,
        getActiveSubscriptionInfoForSimSlotIndex
    }

    enum SystemPropertiesApi {
        get
    }

    enum TelephonyRegistryApi {
        listenForSubscriber
    }

    enum UiccSmsApi {
        sendTextForSubscriber,
        sendMultipartTextForSubscriber,
        sendDataForSubscriber
    }

    enum UsageStatsApi {
        queryUsageStats
    }

    enum UserManagerApi {
        getApplicationRestrictions
    }

    enum VibratorApi {
        vibrate,
        cancelVibrate
    }

    enum WallpaperManagerApi {
        setWallpaper
    }

    enum WebviewApi {
        loadUrl,
        postUrl,
        loadDataWithBaseUrl
    }

    enum WifiApi {
        startScan,
        setWifiEnabled,
        saveConfiguration,
        removeNetwork,
        reconnect,
        reassociate,
        isScanAlwaysAvailable,
        getWifiEnabledState,
        getScanResults,
        getDhcpInfo,
        getConnectionInfo,
        getConfiguredNetworks,
        enableNetwork,
        disconnect,
        disableNetwork,
        addOrUpdateNetwork
    }

    enum WindowManagerApi {
        reenableKeyguard,
        lockNow,
        isKeyguardSecure,
        isKeyguardLocked,
        exitKeyguardSecurely,
        dismissKeyguard,
        disableKeyguard,
        addWindow,
        addView,
        removeView,
        updateViewLayout
    }

    public enum BehaviorId {
        PHONEINTERFACE_GETLINE1NUMBERFORDISPLAY((PhoneInterfaceManagerApi.getLine1NumberForDisplay.ordinal() << 10) + StubController.PERMISSION_CALLLOG_DELETE),
        PHONEINTERFACE_GETIMEIFORSLOT((PhoneInterfaceManagerApi.getImeiForSlot.ordinal() << 10) + StubController.PERMISSION_CALLLOG_DELETE),
        PHONEINTERFACE_GETDEVICEID((PhoneInterfaceManagerApi.getDeviceId.ordinal() << 10) + StubController.PERMISSION_CALLLOG_DELETE),
        PHONEINTERFACE_GETACTIVEPHONETYPEFORSLOT((PhoneInterfaceManagerApi.getActivePhoneTypeForSlot.ordinal() << 10) + StubController.PERMISSION_CALLLOG_DELETE),
        TELEPHONY_SENDTEXTFORSUBSCRIBER((UiccSmsApi.sendTextForSubscriber.ordinal() << 10) + StubController.PERMISSION_SMSLOG_DELETE),
        TELEPHONY_SENDMULTIPARTTEXTFORSUBSCRIBER((UiccSmsApi.sendMultipartTextForSubscriber.ordinal() << 10) + StubController.PERMISSION_SMSLOG_DELETE),
        TELEPHONY_SENDDATAFORSUBSCRIBER((UiccSmsApi.sendDataForSubscriber.ordinal() << 10) + StubController.PERMISSION_SMSLOG_DELETE),
        TELEPHONY_GETACTIVESUBSCRIPTIONINFOLIST((SubScriptionApi.getActiveSubscriptionInfoList.ordinal() << 10) + 786432),
        TELEPHONY_GETACTIVESUBSCRIPTIONINFOFORSIMSLOTINDEX((SubScriptionApi.getActiveSubscriptionInfoForSimSlotIndex.ordinal() << 10) + 786432),
        TELEPHONY_GETVOICEMAILNUMBERFORSUBSCRIBER((PhoneSubInfoApi.getVoiceMailNumberForSubscriber.ordinal() << 10) + StubController.PERMISSION_CALL_FORWARD),
        TELEPHONY_GETVOICEMAILALPHATAGFORSUBSCRIBER((PhoneSubInfoApi.getVoiceMailAlphaTagForSubscriber.ordinal() << 10) + StubController.PERMISSION_CALL_FORWARD),
        TELEPHONY_GETSUBSCRIBERIDFORSUBSCRIBER((PhoneSubInfoApi.getSubscriberIdForSubscriber.ordinal() << 10) + StubController.PERMISSION_CALL_FORWARD),
        TELEPHONY_GETLINE1NUMBERFORSUBSCRIBER((PhoneSubInfoApi.getLine1NumberForSubscriber.ordinal() << 10) + StubController.PERMISSION_CALL_FORWARD),
        TELEPHONY_GETICCSERIALNUMBERFORSUBSCRIBER((PhoneSubInfoApi.getIccSerialNumberForSubscriber.ordinal() << 10) + StubController.PERMISSION_CALL_FORWARD),
        TELEPHONY_GETGROUPIDLEVEL1FORSUBSCRIBER((PhoneSubInfoApi.getGroupIdLevel1ForSubscriber.ordinal() << 10) + StubController.PERMISSION_CALL_FORWARD),
        WIFI_STARTSCAN((WifiApi.startScan.ordinal() << 10) + 1310720),
        WIFI_SETWIFIENABLED((WifiApi.setWifiEnabled.ordinal() << 10) + 1310720),
        WIFI_SAVECONFIGURATION((WifiApi.saveConfiguration.ordinal() << 10) + 1310720),
        WIFI_REMOVENETWORK((WifiApi.removeNetwork.ordinal() << 10) + 1310720),
        WIFI_RECONNECT((WifiApi.reconnect.ordinal() << 10) + 1310720),
        WIFI_REASOCIATE((WifiApi.reassociate.ordinal() << 10) + 1310720),
        WIFI_ISSACNALWAYSAVAILABLE((WifiApi.isScanAlwaysAvailable.ordinal() << 10) + 1310720),
        WIFI_GETWIFIENABLESTATE((WifiApi.getWifiEnabledState.ordinal() << 10) + 1310720),
        WIFI_GETSCANRESULTS((WifiApi.getScanResults.ordinal() << 10) + 1310720),
        WIFI_GETPHCPINFO((WifiApi.getDhcpInfo.ordinal() << 10) + 1310720),
        WIFI_GETCONNECTIONINFO((WifiApi.getConnectionInfo.ordinal() << 10) + 1310720),
        WIFI_GETCONFIGUREDNETWORKS((WifiApi.getConfiguredNetworks.ordinal() << 10) + 1310720),
        WIFI_ENABLENETWORK((WifiApi.enableNetwork.ordinal() << 10) + 1310720),
        WIFI_DISCONNECT((WifiApi.disconnect.ordinal() << 10) + 1310720),
        WIFI_DISABLENETWORK((WifiApi.disableNetwork.ordinal() << 10) + 1310720),
        WIFI_ADDORUPDATENETWORK((WifiApi.addOrUpdateNetwork.ordinal() << 10) + 1310720),
        USAGESTATES_QUERYUSAGESTATS((UsageStatsApi.queryUsageStats.ordinal() << 10) + 1572864),
        RESTRICTIONS_REQUESTPERMISSION((RestrictionsManagerApi.requestPermission.ordinal() << 10) + 1835008),
        RESTRICTIONS_NOTIFYPERMISSIONRESPONSE((RestrictionsManagerApi.notifyPermissionResponse.ordinal() << 10) + 1835008),
        DEVICE_POLICY_UNINSTALLCACERTS((DevicePolicyManagerApi.uninstallCaCerts.ordinal() << 10) + StubController.PERMISSION_WIFI),
        DEVICE_POLICY_REMOVEACTIVEADMIN((DevicePolicyManagerApi.removeActiveAdmin.ordinal() << 10) + StubController.PERMISSION_WIFI),
        DEVICE_POLICY_INSTALLCACERT((DevicePolicyManagerApi.installCaCert.ordinal() << 10) + StubController.PERMISSION_WIFI),
        DEVICE_POLICY_CREATEANDMANAGEUSER((DevicePolicyManagerApi.createAndManageUser.ordinal() << 10) + StubController.PERMISSION_WIFI),
        DEVICE_POLICY_RESETPASSWORD((DevicePolicyManagerApi.resetPassword.ordinal() << 10) + StubController.PERMISSION_WIFI),
        WINDOWNMANAGER_REENABLEKEYGUARD((WindowManagerApi.reenableKeyguard.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_LOCKNOW((WindowManagerApi.lockNow.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_ISKEYGUARDSECURE((WindowManagerApi.isKeyguardSecure.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_ISKEYGUARDLOCKED((WindowManagerApi.isKeyguardLocked.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_EXITKEYGUARDSECURELY((WindowManagerApi.exitKeyguardSecurely.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_DISMISSKEYGUARD((WindowManagerApi.dismissKeyguard.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_DISABLEKEYGUARD((WindowManagerApi.disableKeyguard.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_ADDWINDOW((WindowManagerApi.addWindow.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_ADDVIEW((WindowManagerApi.addView.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_REMOVEVIEW((WindowManagerApi.removeView.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_UPDATEVIEWLAYOUT((WindowManagerApi.updateViewLayout.ordinal() << 10) + 2359296),
        WALLPAPERMANAGER_SETWALLPAPER((WallpaperManagerApi.setWallpaper.ordinal() << 10) + 2621440),
        VIBRATOR_VIBRATE((VibratorApi.vibrate.ordinal() << 10) + 2883584),
        VIBRATOR_CANCELVIBRATE((VibratorApi.cancelVibrate.ordinal() << 10) + 2883584),
        TELEPHONYREGISTRY_LISTENFORSUBSCRIBER((TelephonyRegistryApi.listenForSubscriber.ordinal() << 10) + 3145728),
        STATUSBAR_SETSYSTEMUIVISIBILITY((StatusBarManagerApi.setSystemUiVisibility.ordinal() << 10) + 3407872),
        STATUSBAR_SETICONVISIBILITY((StatusBarManagerApi.setIconVisibility.ordinal() << 10) + 3407872),
        STATUSBAR_SETICON((StatusBarManagerApi.setIcon.ordinal() << 10) + 3407872),
        STATUSBAR_DISABLE((StatusBarManagerApi.disable.ordinal() << 10) + 3407872),
        USERMANAGER_GETAPPLICATIONRESTRICTIONS((UserManagerApi.getApplicationRestrictions.ordinal() << 10) + 3670016),
        PACKAGEMANAGER_SETCOMPONENTENABLEDSETTING((PackageManagerApi.setComponentEnabledSetting.ordinal() << 10) + 3932160),
        PACKAGEMANAGER_SETAPPLICATIONENABLEDSETTING((PackageManagerApi.setApplicationEnabledSetting.ordinal() << 10) + 3932160),
        PACKAGEMANAGER_GETPERMISSIONINFO((PackageManagerApi.getPermissionInfo.ordinal() << 10) + 3932160),
        PACKAGEMANAGER_GETINSTALLEDPACKAGES((PackageManagerApi.getInstalledPackages.ordinal() << 10) + 3932160),
        PACKAGEMANAGER_GETINSTALLEDAPPLICATIONS((PackageManagerApi.getInstalledApplications.ordinal() << 10) + 3932160),
        PACKAGEMANAGER_CLEARPACKAGEPREFERREDACTIVITIES((PackageManagerApi.clearPackagePreferredActivities.ordinal() << 10) + 3932160),
        PACKAGEMANAGER_ADDPREFERREDACTIVITY((PackageManagerApi.addPreferredActivity.ordinal() << 10) + 3932160),
        PACKAGEINSTALLER_UNINSTALL((PackageInstallerApi.uninstall.ordinal() << 10) + StubController.PERMISSION_MOBILEDATE),
        PACKAGEINSTALLER_COMMIT((PackageInstallerApi.commit.ordinal() << 10) + StubController.PERMISSION_MOBILEDATE),
        LAUNCHERAPPS_STARTSHORTCUT((LauncherAppsApi.startShortcut.ordinal() << 10) + 4456448),
        LAUNCHERAPPS_PINSHORTCUTS((LauncherAppsApi.pinShortcuts.ordinal() << 10) + 4456448),
        LAUNCHERAPPS_GETLAUNCHERACTIVITIES((LauncherAppsApi.getLauncherActivities.ordinal() << 10) + 4456448),
        LAUNCHERAPPS_ADDONAPPSCHANGEDLISTENER((LauncherAppsApi.addOnAppsChangedListener.ordinal() << 10) + 4456448),
        NOTIFICATIONMANAGER_ENQUEUETOAST((NotificationManagerApi.enqueueToast.ordinal() << 10) + 4718592),
        NOTIFICATIONMANAGER_ENQUEUENOTIFICATIONWITHTAG((NotificationManagerApi.enqueueNotificationWithTag.ordinal() << 10) + 4718592),
        NOTIFICATIONMANAGER_CANCELNOTIFICATIONWITHTAG((NotificationManagerApi.cancelNotificationWithTag.ordinal() << 10) + 4718592),
        NETWORKMANAGEMENT_REGISTEROBSERVER((NetworkManagementApi.registerObserver.ordinal() << 10) + 4980736),
        NETWORKMANAGEMENT_ADDROUTE((NetworkManagementApi.addRoute.ordinal() << 10) + 4980736),
        NETWORKPOLICYMANAGER_REGISTERLISTENER((NetworkPolicyManagerApi.registerListener.ordinal() << 10) + 5242880),
        MMSBROKER_SENDMESSAGE((MmsBrokerApi.sendMessage.ordinal() << 10) + 5505024),
        MEDIASESSION_DISPATCHADJUSTVOLUME((MediaSessionApi.dispatchAdjustVolume.ordinal() << 10) + 5767168),
        LOCKSETTINGS_SETSTRING((LockSettingsApi.setString.ordinal() << 10) + 6029312),
        LOCKSETTINGS_SETLONG((LockSettingsApi.setLong.ordinal() << 10) + 6029312),
        LOCATIONMANAGER_SENDEXTRACOMMAND((LocationManagerApi.sendExtraCommand.ordinal() << 10) + 6291456),
        LOCATIONMANAGER_REQUESTLOCATIONUPDATES((LocationManagerApi.requestLocationUpdates.ordinal() << 10) + 6291456),
        LOCATIONMANAGER_REQUESTGEOFENCE((LocationManagerApi.requestGeofence.ordinal() << 10) + 6291456),
        LOCATIONMANAGER_REMOVEUPDATES((LocationManagerApi.removeUpdates.ordinal() << 10) + 6291456),
        LOCATIONMANAGER_REGISTERGNSSSTATUSCALLBACK((LocationManagerApi.registerGnssStatusCallback.ordinal() << 10) + 6291456),
        LOCATIONMANAGER_ISPROVIDERENABLED((LocationManagerApi.isProviderEnabled.ordinal() << 10) + 6291456),
        LOCATIONMANAGER_GETPROVIDERS((LocationManagerApi.getProviders.ordinal() << 10) + 6291456),
        LOCATIONMANAGER_GETLASTLOCATION((LocationManagerApi.getLastLocation.ordinal() << 10) + 6291456),
        LOCATIONMANAGER_GETBESTPROVIDER((LocationManagerApi.getBestProvider.ordinal() << 10) + 6291456),
        JOBSCHEDULER_SCHEDULE((JobSchedulerApi.schedule.ordinal() << 10) + 6553600),
        COUNTRYDETECTOR_DETECTCOUNTRY((CountryDetectorApi.detectCountry.ordinal() << 10) + 6815744),
        CONSUMERIR_TRANSMIT((ConsumerIrApi.transmit.ordinal() << 10) + 7077888),
        CONNECTIVITY_REQUESTNETWORK((ConnectivityApi.requestNetwork.ordinal() << 10) + 7340032),
        CONNECTIVITY_PREPAREVPN((ConnectivityApi.prepareVpn.ordinal() << 10) + 7340032),
        CONNECTIVITY_ISACTIVENETWORKMETERED((ConnectivityApi.isActiveNetworkMetered.ordinal() << 10) + 7340032),
        CONNECTIVITY_GETNETWORKINFO((ConnectivityApi.getNetworkInfo.ordinal() << 10) + 7340032),
        CONNECTIVITY_GETNETWORKCAPABILITIES((ConnectivityApi.getNetworkCapabilities.ordinal() << 10) + 7340032),
        CONNECTIVITY_GETLINKPROPERTIES((ConnectivityApi.getLinkProperties.ordinal() << 10) + 7340032),
        CONNECTIVITY_GETALLNETWORKS((ConnectivityApi.getAllNetworks.ordinal() << 10) + 7340032),
        CONNECTIVITY_GETAllNETWORKINFO((ConnectivityApi.getAllNetworkInfo.ordinal() << 10) + 7340032),
        CONNECTIVITY_GETACTIVENETWORKINFO((ConnectivityApi.getActiveNetworkInfo.ordinal() << 10) + 7340032),
        CONTENT_REGISTERCONTENTOBSERVER((ContentApi.registerContentObserver.ordinal() << 10) + 7602176),
        CLIPBOARD_GETPRIMARYCLIP((ClipboardApi.getPrimaryClip.ordinal() << 10) + 7864320),
        CLIPBOARD_ADDPRIMARYCLIPCHANGEDLISTENER((ClipboardApi.addPrimaryClipChangedListener.ordinal() << 10) + 7864320),
        CLIPBOARD_SETPRIMARYCLIP((ClipboardApi.setPrimaryClip.ordinal() << 10) + 7864320),
        AUDIO_SETSTREAMVOLUME((AudioApi.setStreamVolume.ordinal() << 10) + 8126464),
        AUDIO_SETSPEAKERPHONEON((AudioApi.setSpeakerphoneOn.ordinal() << 10) + 8126464),
        AUDIO_SETMODE((AudioApi.setMode.ordinal() << 10) + 8126464),
        AUDIO_SETMICROPHONEMUTE((AudioApi.setMicrophoneMute.ordinal() << 10) + 8126464),
        AUDIO_SETMASTERMUTE((AudioApi.setMasterMute.ordinal() << 10) + 8126464),
        AUDIO_REQUESTAUDIOFOCUS((AudioApi.requestAudioFocus.ordinal() << 10) + 8126464),
        AUDIO_PLAYSOUNDEFFECTVOLUME((AudioApi.playSoundEffectVolume.ordinal() << 10) + 8126464),
        AUDIO_ADJUSTSTREAMVOLUME((AudioApi.adjustStreamVolume.ordinal() << 10) + 8126464),
        APPOPS_SETMODE((AppOpsApi.setMode.ordinal() << 10) + StubController.PERMISSION_BLUETOOTH),
        BATTERYSTATS_TAKEUIDSNAPSHOTS((BatteryStatsApi.takeUidSnapshots.ordinal() << 10) + 8650752),
        ACTIVITYSTARTER_STARTACTIVITYMAYWAIT((ActivityStarterApi.startActivityMayWait.ordinal() << 10) + 8912896),
        ACTIVITYMANAGER_UPDATECONFIGURATION((ActivityManagerApi.updateConfiguration.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_UNREGISTERRECEIVER((ActivityManagerApi.unregisterReceiver.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_UNBINDSERVICE((ActivityManagerApi.unbindService.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_SETTASKDESCRIPTION((ActivityManagerApi.setTaskDescription.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_SETREQUESTEDORIENTATION((ActivityManagerApi.setRequestedOrientation.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_NAVIGATEUPTO((ActivityManagerApi.navigateUpTo.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_MOVETASKTOFRONT((ActivityManagerApi.moveTaskToFront.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_KILLBACKGROUNDPROCESSES((ActivityManagerApi.killBackgroundProcesses.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_GETRUNNINGAPPPROCESSES((ActivityManagerApi.getRunningAppProcesses.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_GETRECENTTASKS((ActivityManagerApi.getRecentTasks.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_GETPROCESSMEMORYINFO((ActivityManagerApi.getProcessMemoryInfo.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_GETPROCESSESINERRORSTATE((ActivityManagerApi.getProcessesInErrorState.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_GETAPPTASKS((ActivityManagerApi.getAppTasks.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_FINISHACTIVITY((ActivityManagerApi.finishActivity.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_CLOSESYSTEMDIALOGS((ActivityManagerApi.closeSystemDialogs.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_BROADCASTINTENT((ActivityManagerApi.broadcastIntent.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_BINDSERVICE((ActivityManagerApi.bindService.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_FORCESTOPPACKAGEASUSER((ActivityManagerApi.forceStopPackageAsUser.ordinal() << 10) + 9175040),
        ACTIVITYMANAGER_GETFILTEREDTASKS((ActivityManagerApi.getFilteredTasks.ordinal() << 10) + 9175040),
        BROADCASTQUEUE_PROCESSCURBROADCASTLOCKED((BroadcastQueueApi.processCurBroadcastLocked.ordinal() << 10) + 9437184),
        BROADCASTQUEUE_PERFORMRECEIVELOCKED((BroadcastQueueApi.performReceiveLocked.ordinal() << 10) + 9437184),
        BROADCASTQUEUE_FINISHRECEIVERLOCKED((BroadcastQueueApi.finishReceiverLocked.ordinal() << 10) + 9437184),
        ACCOUNTMANAGER_REMOVEACCOUNTINTERNAL((AccountManagerApi.removeAccountInternal.ordinal() << 10) + 9699328),
        ACCOUNTMANAGER_GETACCOUNTSINTERNAL((AccountManagerApi.getAccountsInternal.ordinal() << 10) + 9699328),
        ACCOUNTMANAGER_ADDACCOUNTINTERNAL((AccountManagerApi.addAccountInternal.ordinal() << 10) + 9699328),
        ACCOUNTMANAGER_ADDACCOUNT((AccountManagerApi.addAccount.ordinal() << 10) + 9699328),
        APPWIGDET_BINDAPPWIDGETID((AppWidgetApi.bindAppWidgetId.ordinal() << 10) + 9961472),
        APPWIGDET_CREATEAPPWIDGETCONFIGINTENTSENDER((AppWidgetApi.createAppWidgetConfigIntentSender.ordinal() << 10) + 9961472),
        CONTENTPROVIDER_UPDATE((ContentProviderApi.update.ordinal() << 10) + 10223616),
        CONTENTPROVIDER_QUERY((ContentProviderApi.query.ordinal() << 10) + 10223616),
        CONTENTPROVIDER_INSERT((ContentProviderApi.insert.ordinal() << 10) + 10223616),
        CONTENTPROVIDER_DELETE((ContentProviderApi.delete.ordinal() << 10) + 10223616),
        WEBVIEW_LOADURL((WebviewApi.loadUrl.ordinal() << 10) + 10485760),
        WEBVIEW_POSTURL((WebviewApi.postUrl.ordinal() << 10) + 10485760),
        WEBVIEW_LOADDATAWITHBASEURL((WebviewApi.loadDataWithBaseUrl.ordinal() << 10) + 10485760),
        DEXFILE_LOADDEX((DexFileApi.loadDex.ordinal() << 10) + 10747904),
        DEXFILE_DEXFILE((DexFileApi.DexFile.ordinal() << 10) + 10747904),
        DEXFILE_BASEDEXCLASSLOADER((DexFileApi.BaseDexClassLoader.ordinal() << 10) + 10747904),
        DEXFILE_DEXPATHLIST((DexFileApi.DexPathList.ordinal() << 10) + 10747904),
        LOAD_ELF_LOADLIBRARY((LoadElfApi.loadlibrary0.ordinal() << 10) + 11010048),
        LOAD_ELF_LOAD((LoadElfApi.load0.ordinal() << 10) + 11010048),
        CLASS_LOAD((ClassApi.loadClass.ordinal() << 10) + 14155776),
        CLASS_FORNAME((ClassApi.forName.ordinal() << 10) + 14155776),
        ALERTDIALOG_SETTITLE((AlertDialogApi.setTitle.ordinal() << 10) + 11272192),
        ALERTDIALOG_SETCUSTOMTITLE((AlertDialogApi.setCustomTitle.ordinal() << 10) + 11272192),
        ALERTDIALOG_SETMESSAGE((AlertDialogApi.setMessage.ordinal() << 10) + 11272192),
        ALERTDIALOG_SETICON((AlertDialogApi.setIcon.ordinal() << 10) + 11272192),
        TOAST_MAKETEXT((AlertDialogApi.makeText.ordinal() << 10) + 11272192),
        HTTP_OPENCONNECTION((HttpApi.openConnection.ordinal() << 10) + 11534336),
        HTTP_CONNECT((HttpApi.connect.ordinal() << 10) + 11534336),
        SOCKET_CONNECT((SocketApi.connect.ordinal() << 10) + 11796480),
        EXECUTE_START((ExecuteApi.start.ordinal() << 10) + 12058624),
        ACTIVITYSTACK_REGISTERACTIVITYLIFECYCLECALLBACKS((ActivityStackApi.registerActivityLifecycleCallbacks.ordinal() << 10) + 12320768),
        CAMERA_OPENCAMERA((CameraApi.open.ordinal() << 10) + 12582912),
        CAMERA_CAPTURE((CameraApi.capture.ordinal() << 10) + 12582912),
        CAMERA_CAPTUREBURST((CameraApi.captureBurst.ordinal() << 10) + 12582912),
        CAMERA_STARTFACEDETECTION((CameraApi.startFaceDetection.ordinal() << 10) + 12582912),
        CAMERA_DISABLESHUTTERSOUND((CameraApi.disableShutterSound.ordinal() << 10) + 12582912),
        CAMERA_SETTORCHMODE((CameraApi.setTorchMode.ordinal() << 10) + 12582912),
        CAMERA_MEDIARECORDERPREPARE((CameraApi.prepare.ordinal() << 10) + 12582912),
        AUDIORECORD_STARTRECORDING((AudioRecordApi.startRecording.ordinal() << 10) + 12845056),
        AUDIORECORD_STOP((AudioRecordApi.stop.ordinal() << 10) + 12845056),
        SYSTEM_PROPERTIES_GET((SystemPropertiesApi.get.ordinal() << 10) + 14417920),
        ACCESSIBILITY_ONACCESSIBILITYEVENT((AccessibilityApi.onaccessibilityevent.ordinal() << 10) + 13107200),
        ACCESSIBILITY_DISPATCHSERVICECONNECTED((AccessibilityApi.dispatchServiceConnected.ordinal() << 10) + 13107200),
        DOCUMENT_DOCUMENTQUERY((DocumentApi.document_query.ordinal() << 10) + 13369344),
        DOCUMENT_CONTENTRESOLVEQUERY((DocumentApi.contentresolve_query.ordinal() << 10) + 13369344),
        CIPHER_ENCODE0((CipherApi.encode0.ordinal() << 10) + 13631488),
        CIPHER_ENCODE((CipherApi.encode.ordinal() << 10) + 13631488),
        CIPHER_CREATECIPHER((CipherApi.create_cipher.ordinal() << 10) + 13631488),
        PROXY_SETPROXY((ProxyApi.Proxy.ordinal() << 10) + 13893632),
        SMSMESSAGEBASE_GETORIGINATINGADDRESS((SmsMessageBaseApi.getOriginatingAddress.ordinal() << 10) + 14680064),
        SMSMESSAGEBASE_GETMESSAGEBODY((SmsMessageBaseApi.getMessageBody.ordinal() << 10) + 14680064),
        BASEBUNDLE_GET((BaseBundleApi.get.ordinal() << 10) + 14942208),
        BROADCASTRECEIVER_ABORTBROADCAST((BroadcastReceiverApi.abortBroadcast.ordinal() << 10) + 15204352);
        
        private int mValue;

        private BehaviorId(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }
}
