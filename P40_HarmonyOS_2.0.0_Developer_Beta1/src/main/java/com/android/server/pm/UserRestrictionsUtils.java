package com.android.server.pm;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.IStopUserCallback;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.hardware.biometrics.face.V1_0.FaceAcquiredInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.Preconditions;
import com.google.android.collect.Sets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class UserRestrictionsUtils {
    private static final Set<String> DEFAULT_ENABLED_FOR_DEVICE_OWNERS = Sets.newArraySet(new String[]{"no_add_managed_profile"});
    private static final Set<String> DEFAULT_ENABLED_FOR_MANAGED_PROFILES = Sets.newArraySet(new String[]{"no_bluetooth_sharing"});
    private static final Set<String> DEVICE_OWNER_ONLY_RESTRICTIONS = Sets.newArraySet(new String[]{"no_user_switch", "disallow_config_private_dns"});
    private static final Set<String> GLOBAL_RESTRICTIONS = Sets.newArraySet(new String[]{"no_adjust_volume", "no_bluetooth_sharing", "no_config_date_time", "no_system_error_dialogs", "no_run_in_background", "no_unmute_microphone", "disallow_unmute_device"});
    private static final Set<String> IMMUTABLE_BY_OWNERS = Sets.newArraySet(new String[]{"no_record_audio", "no_wallpaper", "no_oem_unlock"});
    private static final Set<String> NON_PERSIST_USER_RESTRICTIONS = Sets.newArraySet(new String[]{"no_record_audio"});
    private static final Set<String> PRIMARY_USER_ONLY_RESTRICTIONS = Sets.newArraySet(new String[]{"no_bluetooth", "no_usb_file_transfer", "no_config_tethering", "no_network_reset", "no_factory_reset", "no_add_user", "no_config_cell_broadcasts", "no_config_mobile_networks", "no_physical_media", "no_sms", "no_fun", "no_safe_boot", "no_create_windows", "no_data_roaming", "no_airplane_mode"});
    private static final Set<String> PROFILE_GLOBAL_RESTRICTIONS = Sets.newArraySet(new String[]{"ensure_verify_apps", "no_airplane_mode", "no_install_unknown_sources_globally"});
    private static final String TAG = "UserRestrictionsUtils";
    public static final Set<String> USER_RESTRICTIONS = newSetWithUniqueCheck(new String[]{"no_config_wifi", "no_config_locale", "no_modify_accounts", "no_install_apps", "no_uninstall_apps", "no_share_location", "no_install_unknown_sources", "no_install_unknown_sources_globally", "no_config_bluetooth", "no_bluetooth", "no_bluetooth_sharing", "no_usb_file_transfer", "no_config_credentials", "no_remove_user", "no_remove_managed_profile", "no_debugging_features", "no_config_vpn", "no_config_date_time", "no_config_tethering", "no_network_reset", "no_factory_reset", "no_add_user", "no_add_managed_profile", "ensure_verify_apps", "no_config_cell_broadcasts", "no_config_mobile_networks", "no_control_apps", "no_physical_media", "no_unmute_microphone", "no_adjust_volume", "no_outgoing_calls", "no_sms", "no_fun", "no_create_windows", "no_system_error_dialogs", "no_cross_profile_copy_paste", "no_outgoing_beam", "no_wallpaper", "no_safe_boot", "allow_parent_profile_app_linking", "no_record_audio", "no_camera", "no_run_in_background", "no_data_roaming", "no_set_user_icon", "no_set_wallpaper", "no_oem_unlock", "disallow_unmute_device", "no_autofill", "no_content_capture", "no_content_suggestions", "no_user_switch", "no_unified_password", "no_config_location", "no_airplane_mode", "no_config_brightness", "no_sharing_into_profile", "no_ambient_display", "no_config_screen_timeout", "no_printing", "disallow_config_private_dns"});

    private UserRestrictionsUtils() {
    }

    private static Set<String> newSetWithUniqueCheck(String[] strings) {
        Set<String> ret = Sets.newArraySet(strings);
        Preconditions.checkState(ret.size() == strings.length);
        return ret;
    }

    public static boolean isValidRestriction(String restriction) {
        if (USER_RESTRICTIONS.contains(restriction)) {
            return true;
        }
        int uid = Binder.getCallingUid();
        String[] pkgs = null;
        try {
            pkgs = AppGlobals.getPackageManager().getPackagesForUid(uid);
        } catch (RemoteException e) {
        }
        StringBuilder msg = new StringBuilder("Unknown restriction queried by uid ");
        msg.append(uid);
        if (pkgs != null && pkgs.length > 0) {
            msg.append(" (");
            msg.append(pkgs[0]);
            if (pkgs.length > 1) {
                msg.append(" et al");
            }
            msg.append(")");
        }
        msg.append(": ");
        msg.append(restriction);
        if (restriction == null || !isSystemApp(uid, pkgs)) {
            Slog.e(TAG, msg.toString());
        } else {
            Slog.wtf(TAG, msg.toString());
        }
        return false;
    }

    private static boolean isSystemApp(int uid, String[] packageList) {
        if (UserHandle.isCore(uid)) {
            return true;
        }
        if (packageList == null) {
            return false;
        }
        IPackageManager pm = AppGlobals.getPackageManager();
        for (int i = 0; i < packageList.length; i++) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageList[i], 794624, UserHandle.getUserId(uid));
                if (appInfo != null && appInfo.isSystemApp()) {
                    return true;
                }
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    public static void writeRestrictions(XmlSerializer serializer, Bundle restrictions, String tag) throws IOException {
        if (restrictions != null) {
            serializer.startTag(null, tag);
            for (String key : restrictions.keySet()) {
                if (!NON_PERSIST_USER_RESTRICTIONS.contains(key)) {
                    if (!USER_RESTRICTIONS.contains(key)) {
                        Log.w(TAG, "Unknown user restriction detected: " + key);
                    } else if (restrictions.getBoolean(key)) {
                        serializer.attribute(null, key, "true");
                    }
                }
            }
            serializer.endTag(null, tag);
        }
    }

    public static void readRestrictions(XmlPullParser parser, Bundle restrictions) {
        restrictions.clear();
        for (String key : USER_RESTRICTIONS) {
            String value = parser.getAttributeValue(null, key);
            if (value != null) {
                restrictions.putBoolean(key, Boolean.parseBoolean(value));
            }
        }
    }

    public static Bundle readRestrictions(XmlPullParser parser) {
        Bundle result = new Bundle();
        readRestrictions(parser, result);
        return result;
    }

    public static Bundle nonNull(Bundle in) {
        return in != null ? in : new Bundle();
    }

    public static boolean isEmpty(Bundle in) {
        return in == null || in.size() == 0;
    }

    public static boolean contains(Bundle in, String restriction) {
        return in != null && in.getBoolean(restriction);
    }

    public static Bundle clone(Bundle in) {
        Bundle bundle;
        if (in == null) {
            bundle = new Bundle();
        }
        return bundle;
    }

    public static void merge(Bundle dest, Bundle in) {
        Preconditions.checkNotNull(dest);
        Preconditions.checkArgument(dest != in);
        if (in != null) {
            for (String key : in.keySet()) {
                if (in.getBoolean(key, false)) {
                    dest.putBoolean(key, true);
                }
            }
        }
    }

    public static Bundle mergeAll(SparseArray<Bundle> restrictions) {
        if (restrictions.size() == 0) {
            return null;
        }
        Bundle result = new Bundle();
        for (int i = 0; i < restrictions.size(); i++) {
            merge(result, restrictions.valueAt(i));
        }
        return result;
    }

    public static boolean canDeviceOwnerChange(String restriction) {
        return !IMMUTABLE_BY_OWNERS.contains(restriction);
    }

    public static boolean canProfileOwnerChange(String restriction, int userId) {
        return !IMMUTABLE_BY_OWNERS.contains(restriction) && !DEVICE_OWNER_ONLY_RESTRICTIONS.contains(restriction) && (userId == 0 || !PRIMARY_USER_ONLY_RESTRICTIONS.contains(restriction));
    }

    public static Set<String> getDefaultEnabledForDeviceOwner() {
        return DEFAULT_ENABLED_FOR_DEVICE_OWNERS;
    }

    public static Set<String> getDefaultEnabledForManagedProfiles() {
        return DEFAULT_ENABLED_FOR_MANAGED_PROFILES;
    }

    public static void sortToGlobalAndLocal(Bundle in, boolean isDeviceOwner, int cameraRestrictionScope, Bundle global, Bundle local) {
        if (cameraRestrictionScope == 2) {
            global.putBoolean("no_camera", true);
        } else if (cameraRestrictionScope == 1) {
            local.putBoolean("no_camera", true);
        }
        if (!(in == null || in.size() == 0)) {
            for (String key : in.keySet()) {
                if (in.getBoolean(key)) {
                    if (isGlobal(isDeviceOwner, key)) {
                        global.putBoolean(key, true);
                    } else {
                        local.putBoolean(key, true);
                    }
                }
            }
        }
    }

    private static boolean isGlobal(boolean isDeviceOwner, String key) {
        return (isDeviceOwner && (PRIMARY_USER_ONLY_RESTRICTIONS.contains(key) || GLOBAL_RESTRICTIONS.contains(key))) || PROFILE_GLOBAL_RESTRICTIONS.contains(key) || DEVICE_OWNER_ONLY_RESTRICTIONS.contains(key);
    }

    public static boolean areEqual(Bundle a, Bundle b) {
        if (a == b) {
            return true;
        }
        if (isEmpty(a)) {
            return isEmpty(b);
        }
        if (isEmpty(b)) {
            return false;
        }
        for (String key : a.keySet()) {
            if (a.getBoolean(key) != b.getBoolean(key)) {
                return false;
            }
        }
        for (String key2 : b.keySet()) {
            if (a.getBoolean(key2) != b.getBoolean(key2)) {
                return false;
            }
        }
        return true;
    }

    public static void applyUserRestrictions(Context context, int userId, Bundle newRestrictions, Bundle prevRestrictions) {
        for (String key : USER_RESTRICTIONS) {
            boolean newValue = newRestrictions.getBoolean(key);
            if (newValue != prevRestrictions.getBoolean(key)) {
                applyUserRestriction(context, userId, key, newValue);
            }
        }
    }

    private static void applyUserRestriction(Context context, int userId, String key, boolean newValue) {
        ContentResolver cr = context.getContentResolver();
        long id = Binder.clearCallingIdentity();
        char c = 65535;
        try {
            int i = 1;
            switch (key.hashCode()) {
                case -1475388515:
                    if (key.equals("no_ambient_display")) {
                        c = '\t';
                        break;
                    }
                    break;
                case -1315771401:
                    if (key.equals("ensure_verify_apps")) {
                        c = 3;
                        break;
                    }
                    break;
                case -1145953970:
                    if (key.equals("no_install_unknown_sources_globally")) {
                        c = 4;
                        break;
                    }
                    break;
                case -1082175374:
                    if (key.equals("no_airplane_mode")) {
                        c = '\b';
                        break;
                    }
                    break;
                case 387189153:
                    if (key.equals("no_install_unknown_sources")) {
                        c = 5;
                        break;
                    }
                    break;
                case 721128150:
                    if (key.equals("no_run_in_background")) {
                        c = 6;
                        break;
                    }
                    break;
                case 866097556:
                    if (key.equals("no_config_location")) {
                        c = '\n';
                        break;
                    }
                    break;
                case 928851522:
                    if (key.equals("no_data_roaming")) {
                        c = 0;
                        break;
                    }
                    break;
                case 995816019:
                    if (key.equals("no_share_location")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1095593830:
                    if (key.equals("no_safe_boot")) {
                        c = 7;
                        break;
                    }
                    break;
                case 1760762284:
                    if (key.equals("no_debugging_features")) {
                        c = 2;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    if (newValue) {
                        List<SubscriptionInfo> subscriptionInfoList = ((SubscriptionManager) context.getSystemService(SubscriptionManager.class)).getActiveSubscriptionInfoList();
                        if (subscriptionInfoList != null) {
                            Iterator<SubscriptionInfo> it = subscriptionInfoList.iterator();
                            while (it.hasNext()) {
                                Settings.Global.putStringForUser(cr, "data_roaming" + it.next().getSubscriptionId(), "0", userId);
                            }
                        }
                        Settings.Global.putStringForUser(cr, "data_roaming", "0", userId);
                        break;
                    }
                    break;
                case 1:
                    if (newValue) {
                        Settings.Secure.putIntForUser(cr, "location_mode", 0, userId);
                        break;
                    }
                    break;
                case 2:
                    if (newValue && userId == 0) {
                        Settings.Global.putStringForUser(cr, "adb_enabled", "0", userId);
                        break;
                    }
                case 3:
                    if (newValue) {
                        Settings.Global.putStringForUser(context.getContentResolver(), "package_verifier_enable", "1", userId);
                        Settings.Global.putStringForUser(context.getContentResolver(), "verifier_verify_adb_installs", "1", userId);
                        break;
                    }
                    break;
                case 4:
                    setInstallMarketAppsRestriction(cr, userId, getNewUserRestrictionSetting(context, userId, "no_install_unknown_sources", newValue));
                    break;
                case 5:
                    setInstallMarketAppsRestriction(cr, userId, getNewUserRestrictionSetting(context, userId, "no_install_unknown_sources_globally", newValue));
                    break;
                case 6:
                    if (!(!newValue || ActivityManager.getCurrentUser() == userId || userId == 0)) {
                        try {
                            ActivityManager.getService().stopUser(userId, false, (IStopUserCallback) null);
                            break;
                        } catch (RemoteException e) {
                            throw e.rethrowAsRuntimeException();
                        }
                    }
                case 7:
                    ContentResolver contentResolver = context.getContentResolver();
                    if (!newValue) {
                        i = 0;
                    }
                    Settings.Global.putInt(contentResolver, "safe_boot_disallowed", i);
                    break;
                case '\b':
                    if (newValue) {
                        if (Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 1) {
                            i = 0;
                        }
                        if (i != 0) {
                            Settings.Global.putInt(context.getContentResolver(), "airplane_mode_on", 0);
                            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
                            intent.putExtra("state", false);
                            context.sendBroadcastAsUser(intent, UserHandle.ALL);
                            break;
                        }
                    }
                    break;
                case '\t':
                    if (newValue) {
                        Settings.Secure.putIntForUser(context.getContentResolver(), "doze_enabled", 0, userId);
                        Settings.Secure.putIntForUser(context.getContentResolver(), "doze_always_on", 0, userId);
                        Settings.Secure.putIntForUser(context.getContentResolver(), "doze_pulse_on_pick_up", 0, userId);
                        Settings.Secure.putIntForUser(context.getContentResolver(), "doze_pulse_on_long_press", 0, userId);
                        Settings.Secure.putIntForUser(context.getContentResolver(), "doze_pulse_on_double_tap", 0, userId);
                        break;
                    }
                    break;
                case '\n':
                    if (newValue) {
                        Settings.Global.putString(context.getContentResolver(), "location_global_kill_switch", "0");
                        break;
                    }
                    break;
            }
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static boolean isSettingRestrictedForUser(Context context, String setting, int userId, String value, int callingUid) {
        char c;
        String restriction;
        Preconditions.checkNotNull(setting);
        UserManager mUserManager = (UserManager) context.getSystemService(UserManager.class);
        boolean checkAllUser = false;
        switch (setting.hashCode()) {
            case -1796809747:
                if (setting.equals("location_mode")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1500478207:
                if (setting.equals("location_providers_allowed")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1490222856:
                if (setting.equals("doze_enabled")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -1115710219:
                if (setting.equals("verifier_verify_adb_installs")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -970351711:
                if (setting.equals("adb_enabled")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -693072130:
                if (setting.equals("screen_brightness_mode")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case -623873498:
                if (setting.equals("always_on_vpn_app")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -416662510:
                if (setting.equals("preferred_network_mode")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -101820922:
                if (setting.equals("doze_always_on")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -32505807:
                if (setting.equals("doze_pulse_on_long_press")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 58027029:
                if (setting.equals("safe_boot_disallowed")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 258514750:
                if (setting.equals("screen_off_timeout")) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case 683724341:
                if (setting.equals("private_dns_mode")) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case 720635155:
                if (setting.equals("package_verifier_enable")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 926123534:
                if (setting.equals("airplane_mode_on")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 1073289638:
                if (setting.equals("doze_pulse_on_double_tap")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 1223734380:
                if (setting.equals("private_dns_specifier")) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case 1275530062:
                if (setting.equals("auto_time_zone")) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case 1307734371:
                if (setting.equals("location_global_kill_switch")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 1334097968:
                if (setting.equals("always_on_vpn_lockdown_whitelist")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 1602982312:
                if (setting.equals("doze_pulse_on_pick_up")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 1646894952:
                if (setting.equals("always_on_vpn_lockdown")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1661297501:
                if (setting.equals("auto_time")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case 1701140351:
                if (setting.equals("install_non_market_apps")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1735689732:
                if (setting.equals("screen_brightness")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                if (!mUserManager.hasUserRestriction("no_config_location", UserHandle.of(userId)) || callingUid == 1000) {
                    if (!String.valueOf(0).equals(value)) {
                        restriction = "no_share_location";
                        break;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            case 1:
                if (mUserManager.hasUserRestriction("no_config_location", UserHandle.of(userId)) && callingUid != 1000) {
                    return true;
                }
                if (value == null || !value.startsWith("-")) {
                    restriction = "no_share_location";
                    break;
                } else {
                    return false;
                }
            case 2:
                if (!"0".equals(value)) {
                    restriction = "no_install_unknown_sources";
                    break;
                } else {
                    return false;
                }
            case 3:
                if (!"0".equals(value)) {
                    restriction = "no_debugging_features";
                    break;
                } else {
                    return false;
                }
            case 4:
            case 5:
                if (!"1".equals(value)) {
                    restriction = "ensure_verify_apps";
                    break;
                } else {
                    return false;
                }
            case 6:
                restriction = "no_config_mobile_networks";
                break;
            case 7:
            case '\b':
            case '\t':
                int appId = UserHandle.getAppId(callingUid);
                if (appId != 1000 && appId != 0) {
                    restriction = "no_config_vpn";
                    break;
                } else {
                    return false;
                }
            case '\n':
                if (!"1".equals(value)) {
                    restriction = "no_safe_boot";
                    break;
                } else {
                    return false;
                }
            case 11:
                if (!"0".equals(value)) {
                    restriction = "no_airplane_mode";
                    break;
                } else {
                    return false;
                }
            case '\f':
            case '\r':
            case 14:
            case 15:
            case 16:
                if (!"0".equals(value)) {
                    restriction = "no_ambient_display";
                    break;
                } else {
                    return false;
                }
            case 17:
                if (!"0".equals(value)) {
                    restriction = "no_config_location";
                    checkAllUser = true;
                    break;
                } else {
                    return false;
                }
            case 18:
            case FaceAcquiredInfo.FACE_OBSCURED /* 19 */:
                if (callingUid != 1000) {
                    restriction = "no_config_brightness";
                    break;
                } else {
                    return false;
                }
            case 20:
                DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(DevicePolicyManager.class);
                if (dpm == null || !dpm.getAutoTimeRequired() || !"0".equals(value)) {
                    if (callingUid != 1000) {
                        restriction = "no_config_date_time";
                        break;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
                break;
            case 21:
                if (callingUid != 1000) {
                    restriction = "no_config_date_time";
                    break;
                } else {
                    return false;
                }
            case FaceAcquiredInfo.VENDOR /* 22 */:
                if (callingUid != 1000) {
                    restriction = "no_config_screen_timeout";
                    break;
                } else {
                    return false;
                }
            case 23:
            case 24:
                if (callingUid != 1000) {
                    restriction = "disallow_config_private_dns";
                    break;
                } else {
                    return false;
                }
            default:
                if (setting.startsWith("data_roaming") && !"0".equals(value)) {
                    restriction = "no_data_roaming";
                    break;
                } else {
                    return false;
                }
                break;
        }
        if (checkAllUser) {
            return mUserManager.hasUserRestrictionOnAnyUser(restriction);
        }
        return mUserManager.hasUserRestriction(restriction, UserHandle.of(userId));
    }

    public static void dumpRestrictions(PrintWriter pw, String prefix, Bundle restrictions) {
        boolean noneSet = true;
        if (restrictions != null) {
            for (String key : restrictions.keySet()) {
                if (restrictions.getBoolean(key, false)) {
                    pw.println(prefix + key);
                    noneSet = false;
                }
            }
            if (noneSet) {
                pw.println(prefix + "none");
                return;
            }
            return;
        }
        pw.println(prefix + "null");
    }

    public static void moveRestriction(String restrictionKey, SparseArray<Bundle> srcRestrictions, SparseArray<Bundle> destRestrictions) {
        int i = 0;
        while (i < srcRestrictions.size()) {
            int key = srcRestrictions.keyAt(i);
            Bundle from = srcRestrictions.valueAt(i);
            if (contains(from, restrictionKey)) {
                from.remove(restrictionKey);
                Bundle to = destRestrictions.get(key);
                if (to == null) {
                    to = new Bundle();
                    destRestrictions.append(key, to);
                }
                to.putBoolean(restrictionKey, true);
                if (from.isEmpty()) {
                    srcRestrictions.removeAt(i);
                    i--;
                }
            }
            i++;
        }
    }

    public static boolean restrictionsChanged(Bundle oldRestrictions, Bundle newRestrictions, String... restrictions) {
        if (restrictions.length == 0) {
            return areEqual(oldRestrictions, newRestrictions);
        }
        for (String restriction : restrictions) {
            if (oldRestrictions.getBoolean(restriction, false) != newRestrictions.getBoolean(restriction, false)) {
                return true;
            }
        }
        return false;
    }

    private static void setInstallMarketAppsRestriction(ContentResolver cr, int userId, int settingValue) {
        Settings.Secure.putIntForUser(cr, "install_non_market_apps", settingValue, userId);
    }

    private static int getNewUserRestrictionSetting(Context context, int userId, String userRestriction, boolean newValue) {
        return (newValue || UserManager.get(context).hasUserRestriction(userRestriction, UserHandle.of(userId))) ? 0 : 1;
    }
}
