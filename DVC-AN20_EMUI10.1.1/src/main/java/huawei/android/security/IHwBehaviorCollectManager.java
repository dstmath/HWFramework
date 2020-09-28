package huawei.android.security;

import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.media.AudioSystem;
import android.os.BatteryStats;

public interface IHwBehaviorCollectManager {
    public static final int ACCOUNTMANAGER = 37;
    public static final int ACTIVITYMANAGER = 35;
    public static final int ACTIVITYSTARTER = 34;
    public static final int APISEQ_OFFSET = 10;
    public static final int APPLICATION_INSTALL = 1;
    public static final int APPLICATION_PROCESS_DIED = 4;
    public static final int APPLICATION_PROCESS_START = 3;
    public static final int APPLICATION_UNINSTALL = 2;
    public static final int APPOPS = 32;
    public static final int APPWIGDET = 38;
    public static final int AUDIO = 31;
    public static final int BATTERYSTATS = 33;
    public static final int BEHAVIOR_APISEQ_WIDTH = 8;
    public static final int BEHAVIOR_PARAM_WIDTH = 10;
    public static final int BROADCASTQUEUE = 36;
    public static final int CLIPBOARD = 30;
    public static final int CONNECTIVITY = 28;
    public static final int CONSUMERIR = 27;
    public static final int CONTENT = 29;
    public static final int CONTENTPROVIDER = 39;
    public static final int COUNTRYDETECTOR = 26;
    public static final int DEVICEPOLICYMANAGER = 8;
    public static final int JOBSCHEDULER = 25;
    public static final int LAUNCHERAPPS = 17;
    public static final int LOCATIONMANAGER = 24;
    public static final int LOCKSETTINGS = 23;
    public static final int MEDIASESSION = 22;
    public static final int MMSBROKER = 21;
    public static final int MODULEID_OFFSET = 18;
    public static final int MODULE_ID_START = 0;
    public static final int NETWORKMANAGEMENT = 19;
    public static final int NETWORKPOLICYMANAGER = 20;
    public static final int NOTIFICATIONMANAGER = 18;
    public static final int PACKAGEINSTALLER = 16;
    public static final int PACKAGEMANAGER = 15;
    public static final int PHONEINTERFACEMANAGER = 1;
    public static final int RESTRICTIONSMANAGER = 7;
    public static final int STATUSBARMANAGER = 13;
    public static final int TELEPHONYPHONESUBINFO = 4;
    public static final int TELEPHONYREGISTRY = 12;
    public static final int TELEPHONYSUBSCRIPTION = 3;
    public static final int TELEPHONYUICCSMS = 2;
    public static final int USAGESTATES = 6;
    public static final int USERMANAGER = 14;
    public static final int VIBRATOR = 11;
    public static final int WALLPAPERMANAGER = 10;
    public static final int WIFI = 5;
    public static final int WINDOWNMANAGER = 9;

    public enum AccountManagerApi {
        removeAccountInternal,
        getAccountsInternal,
        addAccountInternal,
        addAccount
    }

    public enum ActivityManagerApi {
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
        bindService
    }

    public enum ActivityStarterApi {
        startActivityMayWait
    }

    public enum AppOpsApi {
        setMode
    }

    public enum AppWidgetApi {
        bindAppWidgetId,
        createAppWidgetConfigIntentSender
    }

    public enum AudioApi {
        setStreamVolume,
        setSpeakerphoneOn,
        setMode,
        setMicrophoneMute,
        setMasterMute,
        requestAudioFocus,
        playSoundEffectVolume,
        adjustStreamVolume
    }

    public enum BatteryStatsApi {
        takeUidSnapshots
    }

    public enum BroadcastQueueApi {
        processCurBroadcastLocked,
        performReceiveLocked,
        finishReceiverLocked
    }

    public enum ClipboardApi {
        getPrimaryClip,
        addPrimaryClipChangedListener
    }

    public enum ConnectivityApi {
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

    public enum ConsumerIrApi {
        transmit
    }

    public enum ContentApi {
        registerContentObserver
    }

    public enum ContentProviderApi {
        query,
        update,
        insert,
        delete
    }

    public enum CountryDetectorApi {
        detectCountry
    }

    public enum DevicePolicyManagerApi {
        uninstallCaCerts,
        removeActiveAdmin,
        installCaCert,
        createAndManageUser,
        resetPassword
    }

    public enum JobSchedulerApi {
        schedule
    }

    public enum LauncherAppsApi {
        startShortcut,
        pinShortcuts,
        getLauncherActivities,
        addOnAppsChangedListener
    }

    public enum LocationManagerApi {
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

    public enum LockSettingsApi {
        setString,
        setLong
    }

    public enum MediaSessionApi {
        dispatchAdjustVolume
    }

    public enum MmsBrokerApi {
        sendMessage
    }

    public enum NetworkManagementApi {
        registerObserver,
        addRoute
    }

    public enum NetworkPolicyManagerApi {
        registerListener
    }

    public enum NotificationManagerApi {
        enqueueToast,
        enqueueNotificationWithTag,
        cancelNotificationWithTag
    }

    public enum PackageInstallerApi {
        uninstall
    }

    public enum PackageManagerApi {
        setComponentEnabledSetting,
        setApplicationEnabledSetting,
        getPermissionInfo,
        getInstalledPackages,
        getInstalledApplications,
        clearPackagePreferredActivities,
        addPreferredActivity
    }

    public enum PhoneInterfaceManagerApi {
        getLine1NumberForDisplay,
        getImeiForSlot,
        getDeviceId,
        getActivePhoneTypeForSlot
    }

    public enum PhoneSubInfoApi {
        getVoiceMailNumberForSubscriber,
        getVoiceMailAlphaTagForSubscriber,
        getSubscriberIdForSubscriber,
        getLine1NumberForSubscriber,
        getIccSerialNumberForSubscriber,
        getGroupIdLevel1ForSubscriber
    }

    public enum RestrictionsManagerApi {
        requestPermission,
        notifyPermissionResponse
    }

    public enum StatusBarManagerApi {
        setSystemUiVisibility,
        setIconVisibility,
        setIcon,
        disable
    }

    public enum SubScriptionApi {
        getActiveSubscriptionInfoList,
        getActiveSubscriptionInfoForSimSlotIndex
    }

    public enum TelephonyRegistryApi {
        listenForSubscriber
    }

    public enum UiccSmsApi {
        sendTextForSubscriber,
        sendMultipartTextForSubscriber,
        sendDataForSubscriber
    }

    public enum UsageStatsApi {
        queryUsageStats
    }

    public enum UserManagerApi {
        getApplicationRestrictions
    }

    public enum VibratorApi {
        vibrate,
        cancelVibrate
    }

    public enum WallpaperManagerApi {
        setWallpaper
    }

    public enum WifiApi {
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

    public enum WindowManagerApi {
        reenableKeyguard,
        lockNow,
        isKeyguardSecure,
        isKeyguardLocked,
        exitKeyguardSecurely,
        dismissKeyguard,
        disableKeyguard,
        addWindow
    }

    void regUntrustedAppToMonitorService(ApplicationInfo applicationInfo);

    void sendBehavior(int i);

    void sendBehavior(int i, int i2, BehaviorId behaviorId);

    void sendBehavior(int i, int i2, BehaviorId behaviorId, Object... objArr);

    void sendBehavior(BehaviorId behaviorId);

    void sendBehavior(BehaviorId behaviorId, Object... objArr);

    void sendEvent(int i, int i2, int i3, String str, String str2);

    public enum BehaviorId {
        PHONEINTERFACE_GETLINE1NUMBERFORDISPLAY((PhoneInterfaceManagerApi.getLine1NumberForDisplay.ordinal() << 10) + 262144),
        PHONEINTERFACE_GETIMEIFORSLOT((PhoneInterfaceManagerApi.getImeiForSlot.ordinal() << 10) + 262144),
        PHONEINTERFACE_GETDEVICEID((PhoneInterfaceManagerApi.getDeviceId.ordinal() << 10) + 262144),
        PHONEINTERFACE_GETACTIVEPHONETYPEFORSLOT((PhoneInterfaceManagerApi.getActivePhoneTypeForSlot.ordinal() << 10) + 262144),
        TELEPHONY_SENDTEXTFORSUBSCRIBER((UiccSmsApi.sendTextForSubscriber.ordinal() << 10) + 524288),
        TELEPHONY_SENDMULTIPARTTEXTFORSUBSCRIBER((UiccSmsApi.sendMultipartTextForSubscriber.ordinal() << 10) + 524288),
        TELEPHONY_SENDDATAFORSUBSCRIBER((UiccSmsApi.sendDataForSubscriber.ordinal() << 10) + 524288),
        TELEPHONY_GETACTIVESUBSCRIPTIONINFOLIST((SubScriptionApi.getActiveSubscriptionInfoList.ordinal() << 10) + 786432),
        TELEPHONY_GETACTIVESUBSCRIPTIONINFOFORSIMSLOTINDEX((SubScriptionApi.getActiveSubscriptionInfoForSimSlotIndex.ordinal() << 10) + 786432),
        TELEPHONY_GETVOICEMAILNUMBERFORSUBSCRIBER((PhoneSubInfoApi.getVoiceMailNumberForSubscriber.ordinal() << 10) + 1048576),
        TELEPHONY_GETVOICEMAILALPHATAGFORSUBSCRIBER((PhoneSubInfoApi.getVoiceMailAlphaTagForSubscriber.ordinal() << 10) + 1048576),
        TELEPHONY_GETSUBSCRIBERIDFORSUBSCRIBER((PhoneSubInfoApi.getSubscriberIdForSubscriber.ordinal() << 10) + 1048576),
        TELEPHONY_GETLINE1NUMBERFORSUBSCRIBER((PhoneSubInfoApi.getLine1NumberForSubscriber.ordinal() << 10) + 1048576),
        TELEPHONY_GETICCSERIALNUMBERFORSUBSCRIBER((PhoneSubInfoApi.getIccSerialNumberForSubscriber.ordinal() << 10) + 1048576),
        TELEPHONY_GETGROUPIDLEVEL1FORSUBSCRIBER((PhoneSubInfoApi.getGroupIdLevel1ForSubscriber.ordinal() << 10) + 1048576),
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
        RESTRICTIONS_REQUESTPERMISSION((RestrictionsManagerApi.requestPermission.ordinal() << 10) + BatteryStats.HistoryItem.MOST_INTERESTING_STATES),
        RESTRICTIONS_NOTIFYPERMISSIONRESPONSE((RestrictionsManagerApi.notifyPermissionResponse.ordinal() << 10) + BatteryStats.HistoryItem.MOST_INTERESTING_STATES),
        DEVICE_POLICY_UNINSTALLCACERTS((DevicePolicyManagerApi.uninstallCaCerts.ordinal() << 10) + 2097152),
        DEVICE_POLICY_REMOVEACTIVEADMIN((DevicePolicyManagerApi.removeActiveAdmin.ordinal() << 10) + 2097152),
        DEVICE_POLICY_INSTALLCACERT((DevicePolicyManagerApi.installCaCert.ordinal() << 10) + 2097152),
        DEVICE_POLICY_CREATEANDMANAGEUSER((DevicePolicyManagerApi.createAndManageUser.ordinal() << 10) + 2097152),
        DEVICE_POLICY_RESETPASSWORD((DevicePolicyManagerApi.resetPassword.ordinal() << 10) + 2097152),
        WINDOWNMANAGER_REENABLEKEYGUARD((WindowManagerApi.reenableKeyguard.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_LOCKNOW((WindowManagerApi.lockNow.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_ISKEYGUARDSECURE((WindowManagerApi.isKeyguardSecure.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_ISKEYGUARDLOCKED((WindowManagerApi.isKeyguardLocked.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_EXITKEYGUARDSECURELY((WindowManagerApi.exitKeyguardSecurely.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_DISMISSKEYGUARD((WindowManagerApi.dismissKeyguard.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_DISABLEKEYGUARD((WindowManagerApi.disableKeyguard.ordinal() << 10) + 2359296),
        WINDOWNMANAGER_ADDWINDOW((WindowManagerApi.addWindow.ordinal() << 10) + 2359296),
        WALLPAPERMANAGER_SETWALLPAPER((WallpaperManagerApi.setWallpaper.ordinal() << 10) + 2621440),
        VIBRATOR_VIBRATE((VibratorApi.vibrate.ordinal() << 10) + AudioSystem.DEVICE_OUT_ALL_HDMI_SYSTEM_AUDIO),
        VIBRATOR_CANCELVIBRATE((VibratorApi.cancelVibrate.ordinal() << 10) + AudioSystem.DEVICE_OUT_ALL_HDMI_SYSTEM_AUDIO),
        TELEPHONYREGISTRY_LISTENFORSUBSCRIBER((TelephonyRegistryApi.listenForSubscriber.ordinal() << 10) + IntentFilter.MATCH_CATEGORY_HOST),
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
        PACKAGEINSTALLER_UNINSTALL((PackageInstallerApi.uninstall.ordinal() << 10) + 4194304),
        LAUNCHERAPPS_STARTSHORTCUT((LauncherAppsApi.startShortcut.ordinal() << 10) + 4456448),
        LAUNCHERAPPS_PINSHORTCUTS((LauncherAppsApi.pinShortcuts.ordinal() << 10) + 4456448),
        LAUNCHERAPPS_GETLAUNCHERACTIVITIES((LauncherAppsApi.getLauncherActivities.ordinal() << 10) + 4456448),
        LAUNCHERAPPS_ADDONAPPSCHANGEDLISTENER((LauncherAppsApi.addOnAppsChangedListener.ordinal() << 10) + 4456448),
        NOTIFICATIONMANAGER_ENQUEUETOAST((NotificationManagerApi.enqueueToast.ordinal() << 10) + 4718592),
        NOTIFICATIONMANAGER_ENQUEUENOTIFICATIONWITHTAG((NotificationManagerApi.enqueueNotificationWithTag.ordinal() << 10) + 4718592),
        NOTIFICATIONMANAGER_CANCELNOTIFICATIONWITHTAG((NotificationManagerApi.cancelNotificationWithTag.ordinal() << 10) + 4718592),
        NETWORKMANAGEMENT_REGISTEROBSERVER((NetworkManagementApi.registerObserver.ordinal() << 10) + 4980736),
        NETWORKMANAGEMENT_ADDROUTE((NetworkManagementApi.addRoute.ordinal() << 10) + 4980736),
        NETWORKPOLICYMANAGER_REGISTERLISTENER((NetworkPolicyManagerApi.registerListener.ordinal() << 10) + IntentFilter.MATCH_CATEGORY_PATH),
        MMSBROKER_SENDMESSAGE((MmsBrokerApi.sendMessage.ordinal() << 10) + 5505024),
        MEDIASESSION_DISPATCHADJUSTVOLUME((MediaSessionApi.dispatchAdjustVolume.ordinal() << 10) + IntentFilter.MATCH_CATEGORY_SCHEME_SPECIFIC_PART),
        LOCKSETTINGS_SETSTRING((LockSettingsApi.setString.ordinal() << 10) + 6029312),
        LOCKSETTINGS_SETLONG((LockSettingsApi.setLong.ordinal() << 10) + 6029312),
        LOCATIONMANAGER_SENDEXTRACOMMAND((LocationManagerApi.sendExtraCommand.ordinal() << 10) + IntentFilter.MATCH_CATEGORY_TYPE),
        LOCATIONMANAGER_REQUESTLOCATIONUPDATES((LocationManagerApi.requestLocationUpdates.ordinal() << 10) + IntentFilter.MATCH_CATEGORY_TYPE),
        LOCATIONMANAGER_REQUESTGEOFENCE((LocationManagerApi.requestGeofence.ordinal() << 10) + IntentFilter.MATCH_CATEGORY_TYPE),
        LOCATIONMANAGER_REMOVEUPDATES((LocationManagerApi.removeUpdates.ordinal() << 10) + IntentFilter.MATCH_CATEGORY_TYPE),
        LOCATIONMANAGER_REGISTERGNSSSTATUSCALLBACK((LocationManagerApi.registerGnssStatusCallback.ordinal() << 10) + IntentFilter.MATCH_CATEGORY_TYPE),
        LOCATIONMANAGER_ISPROVIDERENABLED((LocationManagerApi.isProviderEnabled.ordinal() << 10) + IntentFilter.MATCH_CATEGORY_TYPE),
        LOCATIONMANAGER_GETPROVIDERS((LocationManagerApi.getProviders.ordinal() << 10) + IntentFilter.MATCH_CATEGORY_TYPE),
        LOCATIONMANAGER_GETLASTLOCATION((LocationManagerApi.getLastLocation.ordinal() << 10) + IntentFilter.MATCH_CATEGORY_TYPE),
        LOCATIONMANAGER_GETBESTPROVIDER((LocationManagerApi.getBestProvider.ordinal() << 10) + IntentFilter.MATCH_CATEGORY_TYPE),
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
        AUDIO_SETSTREAMVOLUME((AudioApi.setStreamVolume.ordinal() << 10) + 8126464),
        AUDIO_SETSPEAKERPHONEON((AudioApi.setSpeakerphoneOn.ordinal() << 10) + 8126464),
        AUDIO_SETMODE((AudioApi.setMode.ordinal() << 10) + 8126464),
        AUDIO_SETMICROPHONEMUTE((AudioApi.setMicrophoneMute.ordinal() << 10) + 8126464),
        AUDIO_SETMASTERMUTE((AudioApi.setMasterMute.ordinal() << 10) + 8126464),
        AUDIO_REQUESTAUDIOFOCUS((AudioApi.requestAudioFocus.ordinal() << 10) + 8126464),
        AUDIO_PLAYSOUNDEFFECTVOLUME((AudioApi.playSoundEffectVolume.ordinal() << 10) + 8126464),
        AUDIO_ADJUSTSTREAMVOLUME((AudioApi.adjustStreamVolume.ordinal() << 10) + 8126464),
        APPOPS_SETMODE((AppOpsApi.setMode.ordinal() << 10) + 8388608),
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
        CONTENTPROVIDER_DELETE((ContentProviderApi.delete.ordinal() << 10) + 10223616);
        
        private int value;

        private BehaviorId(int value2) {
            this.value = value2;
        }

        public int getValue() {
            return this.value;
        }
    }
}
