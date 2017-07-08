package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import com.google.android.collect.Sets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class UserRestrictionsUtils {
    private static final Set<String> DEVICE_OWNER_ONLY_RESTRICTIONS = null;
    private static final Set<String> GLOBAL_RESTRICTIONS = null;
    private static final Set<String> IMMUTABLE_BY_OWNERS = null;
    private static final Set<String> NON_PERSIST_USER_RESTRICTIONS = null;
    private static final String TAG = "UserRestrictionsUtils";
    public static final Set<String> USER_RESTRICTIONS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.UserRestrictionsUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.UserRestrictionsUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.UserRestrictionsUtils.<clinit>():void");
    }

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
        Slog.e(TAG, "Unknown restriction: " + restriction);
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

    public static void readRestrictions(XmlPullParser parser, Bundle restrictions) throws IOException {
        for (String key : USER_RESTRICTIONS) {
            String value = parser.getAttributeValue(null, key);
            if (value != null) {
                restrictions.putBoolean(key, Boolean.parseBoolean(value));
            }
        }
    }

    public static Bundle nonNull(Bundle in) {
        return in != null ? in : new Bundle();
    }

    public static boolean isEmpty(Bundle in) {
        return in == null || in.size() == 0;
    }

    public static Bundle clone(Bundle in) {
        return in != null ? new Bundle(in) : new Bundle();
    }

    public static void merge(Bundle dest, Bundle in) {
        boolean z;
        Preconditions.checkNotNull(dest);
        if (dest != in) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z);
        if (in != null) {
            for (String key : in.keySet()) {
                if (in.getBoolean(key, false)) {
                    dest.putBoolean(key, true);
                }
            }
        }
    }

    public static boolean canDeviceOwnerChange(String restriction) {
        return !IMMUTABLE_BY_OWNERS.contains(restriction);
    }

    public static boolean canProfileOwnerChange(String restriction, int userId) {
        if (IMMUTABLE_BY_OWNERS.contains(restriction)) {
            return false;
        }
        if (userId == 0 || !DEVICE_OWNER_ONLY_RESTRICTIONS.contains(restriction)) {
            return true;
        }
        return false;
    }

    public static void sortToGlobalAndLocal(Bundle in, Bundle global, Bundle local) {
        if (in != null && in.size() != 0) {
            for (String key : in.keySet()) {
                if (in.getBoolean(key)) {
                    if (DEVICE_OWNER_ONLY_RESTRICTIONS.contains(key) || GLOBAL_RESTRICTIONS.contains(key)) {
                        global.putBoolean(key, true);
                    } else {
                        local.putBoolean(key, true);
                    }
                }
            }
        }
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
        int i = 0;
        ContentResolver cr = context.getContentResolver();
        long id = Binder.clearCallingIdentity();
        try {
            if (key.equals("no_config_wifi")) {
                if (newValue) {
                    Secure.putIntForUser(cr, "wifi_networks_available_notification_on", 0, userId);
                }
            } else if (key.equals("no_data_roaming")) {
                if (newValue) {
                    List<SubscriptionInfo> subscriptionInfoList = new SubscriptionManager(context).getActiveSubscriptionInfoList();
                    if (subscriptionInfoList != null) {
                        for (SubscriptionInfo subInfo : subscriptionInfoList) {
                            Global.putStringForUser(cr, "data_roaming" + subInfo.getSubscriptionId(), "0", userId);
                        }
                    }
                    Global.putStringForUser(cr, "data_roaming", "0", userId);
                }
            } else if (key.equals("no_share_location")) {
                if (newValue) {
                    Secure.putIntForUser(cr, "location_mode", 0, userId);
                }
            } else if (key.equals("no_debugging_features")) {
                if (newValue && userId == 0) {
                    Global.putStringForUser(cr, "adb_enabled", "0", userId);
                }
            } else if (key.equals("ensure_verify_apps")) {
                if (newValue) {
                    Global.putStringForUser(context.getContentResolver(), "package_verifier_enable", "1", userId);
                    Global.putStringForUser(context.getContentResolver(), "verifier_verify_adb_installs", "1", userId);
                }
            } else if (key.equals("no_install_unknown_sources")) {
                if (newValue) {
                    Secure.putIntForUser(cr, "install_non_market_apps", 0, userId);
                }
            } else if (key.equals("no_run_in_background")) {
                if (!(!newValue || ActivityManager.getCurrentUser() == userId || userId == 0)) {
                    ActivityManagerNative.getDefault().stopUser(userId, false, null);
                }
            } else if (key.equals("no_safe_boot")) {
                ContentResolver contentResolver = context.getContentResolver();
                String str = "safe_boot_disallowed";
                if (newValue) {
                    i = 1;
                }
                Global.putInt(contentResolver, str, i);
            }
            Binder.restoreCallingIdentity(id);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(id);
        }
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
}
