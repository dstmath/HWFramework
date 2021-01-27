package com.android.server.usb;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.XmlResourceParser;
import android.hardware.usb.AccessoryFilter;
import android.hardware.usb.DeviceFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.Immutable;
import com.android.internal.app.IntentForwarderActivity;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.server.pm.PackageManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.usb.MtpNotificationManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public class UsbProfileGroupSettingsManager {
    protected static final String BACKUP_SUB_ID = "subId";
    protected static final String BACKUP_SUB_NAME = "subName";
    private static final boolean DEBUG = false;
    private static final String TAG = UsbProfileGroupSettingsManager.class.getSimpleName();
    private static final File sSingleUserSettingsFile = new File("/data/system/usb_device_manager.xml");
    @GuardedBy({"mLock"})
    private final HashMap<AccessoryFilter, UserPackage> mAccessoryPreferenceMap = new HashMap<>();
    private final Context mContext;
    @GuardedBy({"mLock"})
    private final HashMap<DeviceFilter, UserPackage> mDevicePreferenceMap = new HashMap<>();
    private final boolean mDisablePermissionDialogs;
    @GuardedBy({"mLock"})
    private boolean mIsWriteSettingsScheduled;
    private final Object mLock = new Object();
    private final MtpNotificationManager mMtpNotificationManager;
    private final PackageManager mPackageManager;
    MyPackageMonitor mPackageMonitor = new MyPackageMonitor();
    private final UserHandle mParentUser;
    private final AtomicFile mSettingsFile;
    private final UsbSettingsManager mSettingsManager;
    private final UsbHandlerManager mUsbHandlerManager;
    private final UserManager mUserManager;

    /* access modifiers changed from: private */
    @Immutable
    public static class UserPackage {
        final String packageName;
        final UserHandle user;

        private UserPackage(String packageName2, UserHandle user2) {
            this.packageName = packageName2;
            this.user = user2;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof UserPackage)) {
                return false;
            }
            UserPackage other = (UserPackage) obj;
            if (!this.user.equals(other.user) || !this.packageName.equals(other.packageName)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return (this.user.hashCode() * 31) + this.packageName.hashCode();
        }

        public String toString() {
            return this.user.getIdentifier() + SliceClientPermissions.SliceAuthority.DELIMITER + this.packageName;
        }

        public void dump(DualDumpOutputStream dump, String idName, long id) {
            long token = dump.start(idName, id);
            dump.write("user_id", 1120986464257L, this.user.getIdentifier());
            dump.write("package_name", 1138166333442L, this.packageName);
            dump.end(token);
        }
    }

    private class MyPackageMonitor extends PackageMonitor {
        private MyPackageMonitor() {
        }

        public void onPackageAdded(String packageName, int uid) {
            if (UsbProfileGroupSettingsManager.this.mUserManager.isSameProfileGroup(UsbProfileGroupSettingsManager.this.mParentUser.getIdentifier(), UserHandle.getUserId(uid))) {
                UsbProfileGroupSettingsManager.this.handlePackageAdded(new UserPackage(packageName, UserHandle.getUserHandleForUid(uid)));
            }
        }

        public void onPackageRemoved(String packageName, int uid) {
            if (UsbProfileGroupSettingsManager.this.mUserManager.isSameProfileGroup(UsbProfileGroupSettingsManager.this.mParentUser.getIdentifier(), UserHandle.getUserId(uid))) {
                UsbProfileGroupSettingsManager.this.clearDefaults(packageName, UserHandle.getUserHandleForUid(uid));
            }
        }
    }

    UsbProfileGroupSettingsManager(Context context, UserHandle user, UsbSettingsManager settingsManager, UsbHandlerManager usbResolveActivityManager) {
        try {
            Context parentUserContext = context.createPackageContextAsUser(PackageManagerService.PLATFORM_PACKAGE_NAME, 0, user);
            this.mContext = context;
            this.mPackageManager = context.getPackageManager();
            this.mSettingsManager = settingsManager;
            this.mUserManager = (UserManager) context.getSystemService("user");
            this.mParentUser = user;
            this.mSettingsFile = new AtomicFile(new File(Environment.getUserSystemDirectory(user.getIdentifier()), "usb_device_manager.xml"), "usb-state");
            this.mDisablePermissionDialogs = context.getResources().getBoolean(17891410);
            synchronized (this.mLock) {
                if (UserHandle.SYSTEM.equals(user)) {
                    upgradeSingleUserLocked();
                }
                readSettingsLocked();
            }
            this.mPackageMonitor.register(context, null, UserHandle.ALL, true);
            this.mMtpNotificationManager = new MtpNotificationManager(parentUserContext, new MtpNotificationManager.OnOpenInAppListener() {
                /* class com.android.server.usb.$$Lambda$UsbProfileGroupSettingsManager$IQKTzU0q3lyaW9nLL_sbxJPW8ME */

                @Override // com.android.server.usb.MtpNotificationManager.OnOpenInAppListener
                public final void onOpenInApp(UsbDevice usbDevice) {
                    UsbProfileGroupSettingsManager.this.lambda$new$0$UsbProfileGroupSettingsManager(usbDevice);
                }
            });
            this.mUsbHandlerManager = usbResolveActivityManager;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Missing android package");
        }
    }

    public /* synthetic */ void lambda$new$0$UsbProfileGroupSettingsManager(UsbDevice device) {
        resolveActivity(createDeviceAttachedIntent(device), device, false);
    }

    /* access modifiers changed from: package-private */
    public void removeAllDefaultsForUser(UserHandle userToRemove) {
        synchronized (this.mLock) {
            boolean needToPersist = false;
            Iterator<Map.Entry<DeviceFilter, UserPackage>> devicePreferenceIt = this.mDevicePreferenceMap.entrySet().iterator();
            while (devicePreferenceIt.hasNext()) {
                if (devicePreferenceIt.next().getValue().user.equals(userToRemove)) {
                    devicePreferenceIt.remove();
                    needToPersist = true;
                }
            }
            Iterator<Map.Entry<AccessoryFilter, UserPackage>> accessoryPreferenceIt = this.mAccessoryPreferenceMap.entrySet().iterator();
            while (accessoryPreferenceIt.hasNext()) {
                if (accessoryPreferenceIt.next().getValue().user.equals(userToRemove)) {
                    accessoryPreferenceIt.remove();
                    needToPersist = true;
                }
            }
            if (needToPersist) {
                scheduleWriteSettingsLocked();
            }
        }
    }

    private void readPreference(XmlPullParser parser) throws XmlPullParserException, IOException {
        String packageName = null;
        UserHandle user = this.mParentUser;
        int count = parser.getAttributeCount();
        for (int i = 0; i < count; i++) {
            if ("package".equals(parser.getAttributeName(i))) {
                packageName = parser.getAttributeValue(i);
            }
            if ("user".equals(parser.getAttributeName(i))) {
                user = this.mUserManager.getUserForSerialNumber((long) Integer.parseInt(parser.getAttributeValue(i)));
            }
        }
        XmlUtils.nextElement(parser);
        if ("usb-device".equals(parser.getName())) {
            DeviceFilter filter = DeviceFilter.read(parser);
            if (user != null) {
                this.mDevicePreferenceMap.put(filter, new UserPackage(packageName, user));
            }
        } else if ("usb-accessory".equals(parser.getName())) {
            AccessoryFilter filter2 = AccessoryFilter.read(parser);
            if (user != null) {
                this.mAccessoryPreferenceMap.put(filter2, new UserPackage(packageName, user));
            }
        }
        XmlUtils.nextElement(parser);
    }

    @GuardedBy({"mLock"})
    private void upgradeSingleUserLocked() {
        if (sSingleUserSettingsFile.exists()) {
            this.mDevicePreferenceMap.clear();
            this.mAccessoryPreferenceMap.clear();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(sSingleUserSettingsFile);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fis, StandardCharsets.UTF_8.name());
                XmlUtils.nextElement(parser);
                while (parser.getEventType() != 1) {
                    if ("preference".equals(parser.getName())) {
                        readPreference(parser);
                    } else {
                        XmlUtils.nextElement(parser);
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                Log.wtf(TAG, "Failed to read single-user settings", e);
            } catch (Throwable th) {
                IoUtils.closeQuietly((AutoCloseable) null);
                throw th;
            }
            IoUtils.closeQuietly(fis);
            scheduleWriteSettingsLocked();
            sSingleUserSettingsFile.delete();
        }
    }

    @GuardedBy({"mLock"})
    private void readSettingsLocked() {
        this.mDevicePreferenceMap.clear();
        this.mAccessoryPreferenceMap.clear();
        FileInputStream stream = null;
        try {
            stream = this.mSettingsFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, StandardCharsets.UTF_8.name());
            XmlUtils.nextElement(parser);
            while (parser.getEventType() != 1) {
                if ("preference".equals(parser.getName())) {
                    readPreference(parser);
                } else {
                    XmlUtils.nextElement(parser);
                }
            }
        } catch (FileNotFoundException e) {
        } catch (Exception e2) {
            Slog.e(TAG, "error reading settings file, deleting to start fresh", e2);
            this.mSettingsFile.delete();
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
        IoUtils.closeQuietly(stream);
    }

    @GuardedBy({"mLock"})
    private void scheduleWriteSettingsLocked() {
        if (!this.mIsWriteSettingsScheduled) {
            this.mIsWriteSettingsScheduled = true;
            AsyncTask.execute(new Runnable() {
                /* class com.android.server.usb.$$Lambda$UsbProfileGroupSettingsManager$_G1PjxMa22pAIRMzYCwyomX8uhk */

                @Override // java.lang.Runnable
                public final void run() {
                    UsbProfileGroupSettingsManager.this.lambda$scheduleWriteSettingsLocked$1$UsbProfileGroupSettingsManager();
                }
            });
        }
    }

    public /* synthetic */ void lambda$scheduleWriteSettingsLocked$1$UsbProfileGroupSettingsManager() {
        synchronized (this.mLock) {
            try {
                FileOutputStream fos = this.mSettingsFile.startWrite();
                FastXmlSerializer serializer = new FastXmlSerializer();
                serializer.setOutput(fos, StandardCharsets.UTF_8.name());
                serializer.startDocument((String) null, true);
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startTag((String) null, "settings");
                for (DeviceFilter filter : this.mDevicePreferenceMap.keySet()) {
                    serializer.startTag((String) null, "preference");
                    serializer.attribute((String) null, "package", this.mDevicePreferenceMap.get(filter).packageName);
                    serializer.attribute((String) null, "user", String.valueOf(getSerial(this.mDevicePreferenceMap.get(filter).user)));
                    filter.write(serializer);
                    serializer.endTag((String) null, "preference");
                }
                for (AccessoryFilter filter2 : this.mAccessoryPreferenceMap.keySet()) {
                    serializer.startTag((String) null, "preference");
                    serializer.attribute((String) null, "package", this.mAccessoryPreferenceMap.get(filter2).packageName);
                    serializer.attribute((String) null, "user", String.valueOf(getSerial(this.mAccessoryPreferenceMap.get(filter2).user)));
                    filter2.write(serializer);
                    serializer.endTag((String) null, "preference");
                }
                serializer.endTag((String) null, "settings");
                serializer.endDocument();
                this.mSettingsFile.finishWrite(fos);
            } catch (IOException e) {
                Slog.e(TAG, "Failed to write settings", e);
                if (0 != 0) {
                    this.mSettingsFile.failWrite(null);
                }
            }
            this.mIsWriteSettingsScheduled = false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0078, code lost:
        if (0 == 0) goto L_0x007b;
     */
    static ArrayList<DeviceFilter> getDeviceFilters(PackageManager pm, ResolveInfo info) {
        ArrayList<DeviceFilter> filters = null;
        XmlResourceParser parser = null;
        try {
            parser = info.activityInfo.loadXmlMetaData(pm, "android.hardware.usb.action.USB_DEVICE_ATTACHED");
            if (parser == null) {
                String str = TAG;
                Slog.w(str, "no meta-data for " + info);
                if (parser != null) {
                    parser.close();
                }
                return null;
            }
            XmlUtils.nextElement(parser);
            while (parser.getEventType() != 1) {
                if ("usb-device".equals(parser.getName())) {
                    if (filters == null) {
                        filters = new ArrayList<>(1);
                    }
                    filters.add(DeviceFilter.read(parser));
                }
                XmlUtils.nextElement(parser);
            }
            parser.close();
            return filters;
        } catch (Exception e) {
            String str2 = TAG;
            Slog.w(str2, "Unable to load component info " + info.toString(), e);
        } catch (Throwable th) {
            if (0 != 0) {
                parser.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0078, code lost:
        if (0 == 0) goto L_0x007b;
     */
    static ArrayList<AccessoryFilter> getAccessoryFilters(PackageManager pm, ResolveInfo info) {
        ArrayList<AccessoryFilter> filters = null;
        XmlResourceParser parser = null;
        try {
            parser = info.activityInfo.loadXmlMetaData(pm, "android.hardware.usb.action.USB_ACCESSORY_ATTACHED");
            if (parser == null) {
                String str = TAG;
                Slog.w(str, "no meta-data for " + info);
                if (parser != null) {
                    parser.close();
                }
                return null;
            }
            XmlUtils.nextElement(parser);
            while (parser.getEventType() != 1) {
                if ("usb-accessory".equals(parser.getName())) {
                    if (filters == null) {
                        filters = new ArrayList<>(1);
                    }
                    filters.add(AccessoryFilter.read(parser));
                }
                XmlUtils.nextElement(parser);
            }
            parser.close();
            return filters;
        } catch (Exception e) {
            String str2 = TAG;
            Slog.w(str2, "Unable to load component info " + info.toString(), e);
        } catch (Throwable th) {
            if (0 != 0) {
                parser.close();
            }
            throw th;
        }
    }

    private boolean packageMatchesLocked(ResolveInfo info, UsbDevice device, UsbAccessory accessory) {
        ArrayList<AccessoryFilter> accessoryFilters;
        ArrayList<DeviceFilter> deviceFilters;
        if (isForwardMatch(info)) {
            return true;
        }
        if (!(device == null || (deviceFilters = getDeviceFilters(this.mPackageManager, info)) == null)) {
            int numDeviceFilters = deviceFilters.size();
            for (int i = 0; i < numDeviceFilters; i++) {
                if (deviceFilters.get(i).matches(device)) {
                    return true;
                }
            }
        }
        if (accessory == null || (accessoryFilters = getAccessoryFilters(this.mPackageManager, info)) == null) {
            return false;
        }
        int numAccessoryFilters = accessoryFilters.size();
        for (int i2 = 0; i2 < numAccessoryFilters; i2++) {
            if (accessoryFilters.get(i2).matches(accessory)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<ResolveInfo> queryIntentActivitiesForAllProfiles(Intent intent) {
        List<UserInfo> profiles = this.mUserManager.getEnabledProfiles(this.mParentUser.getIdentifier());
        ArrayList<ResolveInfo> resolveInfos = new ArrayList<>();
        int numProfiles = profiles.size();
        for (int i = 0; i < numProfiles; i++) {
            resolveInfos.addAll(this.mSettingsManager.getSettingsForUser(profiles.get(i).id).queryIntentActivities(intent));
        }
        return resolveInfos;
    }

    private boolean isForwardMatch(ResolveInfo match) {
        return match.getComponentInfo().name.equals(IntentForwarderActivity.FORWARD_INTENT_TO_MANAGED_PROFILE);
    }

    private ArrayList<ResolveInfo> preferHighPriority(ArrayList<ResolveInfo> matches) {
        SparseArray<ArrayList<ResolveInfo>> highestPriorityMatchesByUserId = new SparseArray<>();
        SparseIntArray highestPriorityByUserId = new SparseIntArray();
        ArrayList<ResolveInfo> forwardMatches = new ArrayList<>();
        int numMatches = matches.size();
        for (int matchNum = 0; matchNum < numMatches; matchNum++) {
            ResolveInfo match = matches.get(matchNum);
            if (isForwardMatch(match)) {
                forwardMatches.add(match);
            } else {
                if (highestPriorityByUserId.indexOfKey(match.targetUserId) < 0) {
                    highestPriorityByUserId.put(match.targetUserId, Integer.MIN_VALUE);
                    highestPriorityMatchesByUserId.put(match.targetUserId, new ArrayList<>());
                }
                int highestPriority = highestPriorityByUserId.get(match.targetUserId);
                ArrayList<ResolveInfo> highestPriorityMatches = highestPriorityMatchesByUserId.get(match.targetUserId);
                if (match.priority == highestPriority) {
                    highestPriorityMatches.add(match);
                } else if (match.priority > highestPriority) {
                    highestPriorityByUserId.put(match.targetUserId, match.priority);
                    highestPriorityMatches.clear();
                    highestPriorityMatches.add(match);
                }
            }
        }
        ArrayList<ResolveInfo> combinedMatches = new ArrayList<>(forwardMatches);
        int numMatchArrays = highestPriorityMatchesByUserId.size();
        for (int matchArrayNum = 0; matchArrayNum < numMatchArrays; matchArrayNum++) {
            combinedMatches.addAll(highestPriorityMatchesByUserId.valueAt(matchArrayNum));
        }
        return combinedMatches;
    }

    private ArrayList<ResolveInfo> removeForwardIntentIfNotNeeded(ArrayList<ResolveInfo> rawMatches) {
        int numRawMatches = rawMatches.size();
        int numParentActivityMatches = 0;
        int numNonParentActivityMatches = 0;
        for (int i = 0; i < numRawMatches; i++) {
            ResolveInfo rawMatch = rawMatches.get(i);
            if (!isForwardMatch(rawMatch)) {
                if (UserHandle.getUserHandleForUid(rawMatch.activityInfo.applicationInfo.uid).equals(this.mParentUser)) {
                    numParentActivityMatches++;
                } else {
                    numNonParentActivityMatches++;
                }
            }
        }
        if (!(numParentActivityMatches == 0 || numNonParentActivityMatches == 0)) {
            return rawMatches;
        }
        ArrayList<ResolveInfo> matches = new ArrayList<>(numParentActivityMatches + numNonParentActivityMatches);
        for (int i2 = 0; i2 < numRawMatches; i2++) {
            ResolveInfo rawMatch2 = rawMatches.get(i2);
            if (!isForwardMatch(rawMatch2)) {
                matches.add(rawMatch2);
            }
        }
        return matches;
    }

    private ArrayList<ResolveInfo> getDeviceMatchesLocked(UsbDevice device, Intent intent) {
        ArrayList<ResolveInfo> matches = new ArrayList<>();
        List<ResolveInfo> resolveInfos = queryIntentActivitiesForAllProfiles(intent);
        int count = resolveInfos.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = resolveInfos.get(i);
            if (packageMatchesLocked(resolveInfo, device, null)) {
                matches.add(resolveInfo);
            }
        }
        return removeForwardIntentIfNotNeeded(preferHighPriority(matches));
    }

    private ArrayList<ResolveInfo> getAccessoryMatchesLocked(UsbAccessory accessory, Intent intent) {
        ArrayList<ResolveInfo> matches = new ArrayList<>();
        List<ResolveInfo> resolveInfos = queryIntentActivitiesForAllProfiles(intent);
        int count = resolveInfos.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = resolveInfos.get(i);
            if (packageMatchesLocked(resolveInfo, null, accessory)) {
                matches.add(resolveInfo);
            }
        }
        return removeForwardIntentIfNotNeeded(preferHighPriority(matches));
    }

    public void deviceAttached(UsbDevice device) {
        Intent intent = createDeviceAttachedIntent(device);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        resolveActivity(intent, device, true);
    }

    private void resolveActivity(Intent intent, UsbDevice device, boolean showMtpNotification) {
        ArrayList<ResolveInfo> matches;
        ActivityInfo defaultActivity;
        synchronized (this.mLock) {
            matches = getDeviceMatchesLocked(device, intent);
            defaultActivity = getDefaultActivityLocked(matches, this.mDevicePreferenceMap.get(new DeviceFilter(device)));
        }
        if (!showMtpNotification || !MtpNotificationManager.shouldShowNotification(this.mPackageManager, device) || defaultActivity != null) {
            resolveActivity(intent, matches, defaultActivity, device, null);
        } else {
            this.mMtpNotificationManager.showNotification(device);
        }
    }

    public void deviceAttachedForFixedHandler(UsbDevice device, ComponentName component) {
        Intent intent = createDeviceAttachedIntent(device);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.of(ActivityManager.getCurrentUser()));
        try {
            ApplicationInfo appInfo = this.mPackageManager.getApplicationInfoAsUser(component.getPackageName(), 0, this.mParentUser.getIdentifier());
            this.mSettingsManager.getSettingsForUser(UserHandle.getUserId(appInfo.uid)).grantDevicePermission(device, appInfo.uid);
            Intent activityIntent = new Intent(intent);
            activityIntent.setComponent(component);
            try {
                this.mContext.startActivityAsUser(activityIntent, this.mParentUser);
            } catch (ActivityNotFoundException e) {
                String str = TAG;
                Slog.e(str, "unable to start activity " + activityIntent);
            }
        } catch (PackageManager.NameNotFoundException e2) {
            String str2 = TAG;
            Slog.e(str2, "Default USB handling package (" + component.getPackageName() + ") not found  for user " + this.mParentUser);
        }
    }

    /* access modifiers changed from: package-private */
    public void usbDeviceRemoved(UsbDevice device) {
        this.mMtpNotificationManager.hideNotification(device.getDeviceId());
    }

    public void accessoryAttached(UsbAccessory accessory) {
        ArrayList<ResolveInfo> matches;
        ActivityInfo defaultActivity;
        Intent intent = new Intent("android.hardware.usb.action.USB_ACCESSORY_ATTACHED");
        intent.putExtra("accessory", accessory);
        intent.addFlags(285212672);
        synchronized (this.mLock) {
            matches = getAccessoryMatchesLocked(accessory, intent);
            defaultActivity = getDefaultActivityLocked(matches, this.mAccessoryPreferenceMap.get(new AccessoryFilter(accessory)));
        }
        resolveActivity(intent, matches, defaultActivity, null, accessory);
    }

    private void resolveActivity(Intent intent, ArrayList<ResolveInfo> matches, ActivityInfo defaultActivity, UsbDevice device, UsbAccessory accessory) {
        if (matches.size() == 0) {
            if (accessory != null) {
                this.mUsbHandlerManager.showUsbAccessoryUriActivity(accessory, this.mParentUser);
            }
        } else if (defaultActivity != null) {
            UsbUserSettingsManager defaultRIUserSettings = this.mSettingsManager.getSettingsForUser(UserHandle.getUserId(defaultActivity.applicationInfo.uid));
            if (device != null) {
                defaultRIUserSettings.grantDevicePermission(device, defaultActivity.applicationInfo.uid);
            } else if (accessory != null) {
                defaultRIUserSettings.grantAccessoryPermission(accessory, defaultActivity.applicationInfo.uid);
            }
            try {
                intent.setComponent(new ComponentName(defaultActivity.packageName, defaultActivity.name));
                this.mContext.startActivityAsUser(intent, UserHandle.getUserHandleForUid(defaultActivity.applicationInfo.uid));
            } catch (ActivityNotFoundException e) {
                Slog.e(TAG, "startActivity failed", e);
            }
        } else if (matches.size() == 1) {
            this.mUsbHandlerManager.confirmUsbHandler(matches.get(0), device, accessory);
        } else {
            this.mUsbHandlerManager.selectUsbHandler(matches, this.mParentUser, intent);
        }
    }

    private ActivityInfo getDefaultActivityLocked(ArrayList<ResolveInfo> matches, UserPackage userPackage) {
        ActivityInfo activityInfo;
        if (userPackage != null) {
            Iterator<ResolveInfo> it = matches.iterator();
            while (it.hasNext()) {
                ResolveInfo info = it.next();
                if (info.activityInfo != null && userPackage.equals(new UserPackage(info.activityInfo.packageName, UserHandle.getUserHandleForUid(info.activityInfo.applicationInfo.uid)))) {
                    return info.activityInfo;
                }
            }
        }
        if (matches.size() == 1 && (activityInfo = matches.get(0).activityInfo) != null) {
            if (this.mDisablePermissionDialogs) {
                return activityInfo;
            }
            if (activityInfo.applicationInfo != null && (1 & activityInfo.applicationInfo.flags) != 0) {
                return activityInfo;
            }
        }
        return null;
    }

    @GuardedBy({"mLock"})
    private boolean clearCompatibleMatchesLocked(UserPackage userPackage, DeviceFilter filter) {
        ArrayList<DeviceFilter> keysToRemove = new ArrayList<>();
        for (DeviceFilter device : this.mDevicePreferenceMap.keySet()) {
            if (filter.contains(device) && !this.mDevicePreferenceMap.get(device).equals(userPackage)) {
                keysToRemove.add(device);
            }
        }
        if (!keysToRemove.isEmpty()) {
            Iterator<DeviceFilter> it = keysToRemove.iterator();
            while (it.hasNext()) {
                this.mDevicePreferenceMap.remove(it.next());
            }
        }
        return !keysToRemove.isEmpty();
    }

    @GuardedBy({"mLock"})
    private boolean clearCompatibleMatchesLocked(UserPackage userPackage, AccessoryFilter filter) {
        ArrayList<AccessoryFilter> keysToRemove = new ArrayList<>();
        for (AccessoryFilter accessory : this.mAccessoryPreferenceMap.keySet()) {
            if (filter.contains(accessory) && !this.mAccessoryPreferenceMap.get(accessory).equals(userPackage)) {
                keysToRemove.add(accessory);
            }
        }
        if (!keysToRemove.isEmpty()) {
            Iterator<AccessoryFilter> it = keysToRemove.iterator();
            while (it.hasNext()) {
                this.mAccessoryPreferenceMap.remove(it.next());
            }
        }
        return !keysToRemove.isEmpty();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0070, code lost:
        if (0 == 0) goto L_0x0073;
     */
    @GuardedBy({"mLock"})
    private boolean handlePackageAddedLocked(UserPackage userPackage, ActivityInfo aInfo, String metaDataName) {
        XmlResourceParser parser = null;
        boolean changed = false;
        try {
            parser = aInfo.loadXmlMetaData(this.mPackageManager, metaDataName);
            if (parser == null) {
                if (parser != null) {
                    parser.close();
                }
                return false;
            }
            XmlUtils.nextElement(parser);
            while (parser.getEventType() != 1) {
                String tagName = parser.getName();
                if ("usb-device".equals(tagName)) {
                    if (clearCompatibleMatchesLocked(userPackage, DeviceFilter.read(parser))) {
                        changed = true;
                    }
                } else if ("usb-accessory".equals(tagName) && clearCompatibleMatchesLocked(userPackage, AccessoryFilter.read(parser))) {
                    changed = true;
                }
                XmlUtils.nextElement(parser);
            }
            parser.close();
            return changed;
        } catch (Exception e) {
            String str = TAG;
            Slog.w(str, "Unable to load component info " + aInfo.toString(), e);
        } catch (Throwable th) {
            if (0 != 0) {
                parser.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePackageAdded(UserPackage userPackage) {
        synchronized (this.mLock) {
            boolean changed = false;
            try {
                ActivityInfo[] activities = this.mPackageManager.getPackageInfoAsUser(userPackage.packageName, 129, userPackage.user.getIdentifier()).activities;
                if (activities != null) {
                    for (int i = 0; i < activities.length; i++) {
                        if (handlePackageAddedLocked(userPackage, activities[i], "android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                            changed = true;
                        }
                        if (handlePackageAddedLocked(userPackage, activities[i], "android.hardware.usb.action.USB_ACCESSORY_ATTACHED")) {
                            changed = true;
                        }
                    }
                    if (changed) {
                        scheduleWriteSettingsLocked();
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                String str = TAG;
                Slog.e(str, "handlePackageUpdate could not find package " + userPackage, e);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private int getSerial(UserHandle user) {
        return this.mUserManager.getUserSerialNumber(user.getIdentifier());
    }

    /* access modifiers changed from: package-private */
    public void setDevicePackage(UsbDevice device, String packageName, UserHandle user) {
        DeviceFilter filter = new DeviceFilter(device);
        synchronized (this.mLock) {
            boolean changed = true;
            if (packageName != null) {
                UserPackage userPackage = new UserPackage(packageName, user);
                changed = true ^ userPackage.equals(this.mDevicePreferenceMap.get(filter));
                if (changed) {
                    this.mDevicePreferenceMap.put(filter, userPackage);
                }
            } else if (this.mDevicePreferenceMap.remove(filter) == null) {
                changed = false;
            }
            if (changed) {
                scheduleWriteSettingsLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setAccessoryPackage(UsbAccessory accessory, String packageName, UserHandle user) {
        AccessoryFilter filter = new AccessoryFilter(accessory);
        synchronized (this.mLock) {
            boolean changed = true;
            if (packageName != null) {
                UserPackage userPackage = new UserPackage(packageName, user);
                changed = true ^ userPackage.equals(this.mAccessoryPreferenceMap.get(filter));
                if (changed) {
                    this.mAccessoryPreferenceMap.put(filter, userPackage);
                }
            } else if (this.mAccessoryPreferenceMap.remove(filter) == null) {
                changed = false;
            }
            if (changed) {
                scheduleWriteSettingsLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasDefaults(String packageName, UserHandle user) {
        UserPackage userPackage = new UserPackage(packageName, user);
        synchronized (this.mLock) {
            if (this.mDevicePreferenceMap.values().contains(userPackage)) {
                return true;
            }
            return this.mAccessoryPreferenceMap.values().contains(userPackage);
        }
    }

    /* access modifiers changed from: package-private */
    public void clearDefaults(String packageName, UserHandle user) {
        UserPackage userPackage = new UserPackage(packageName, user);
        synchronized (this.mLock) {
            if (clearPackageDefaultsLocked(userPackage)) {
                scheduleWriteSettingsLocked();
            }
        }
    }

    private boolean clearPackageDefaultsLocked(UserPackage userPackage) {
        boolean cleared = false;
        synchronized (this.mLock) {
            if (this.mDevicePreferenceMap.containsValue(userPackage)) {
                DeviceFilter[] keys = (DeviceFilter[]) this.mDevicePreferenceMap.keySet().toArray(new DeviceFilter[0]);
                for (DeviceFilter key : keys) {
                    if (userPackage.equals(this.mDevicePreferenceMap.get(key))) {
                        this.mDevicePreferenceMap.remove(key);
                        cleared = true;
                    }
                }
            }
            if (this.mAccessoryPreferenceMap.containsValue(userPackage)) {
                AccessoryFilter[] keys2 = (AccessoryFilter[]) this.mAccessoryPreferenceMap.keySet().toArray(new AccessoryFilter[0]);
                for (AccessoryFilter key2 : keys2) {
                    if (userPackage.equals(this.mAccessoryPreferenceMap.get(key2))) {
                        this.mAccessoryPreferenceMap.remove(key2);
                        cleared = true;
                    }
                }
            }
        }
        return cleared;
    }

    public void dump(DualDumpOutputStream dump, String idName, long id) {
        long token = dump.start(idName, id);
        synchronized (this.mLock) {
            dump.write("parent_user_id", 1120986464257L, this.mParentUser.getIdentifier());
            for (DeviceFilter filter : this.mDevicePreferenceMap.keySet()) {
                long devicePrefToken = dump.start("device_preferences", 2246267895810L);
                filter.dump(dump, "filter", 1146756268033L);
                this.mDevicePreferenceMap.get(filter).dump(dump, "user_package", 1146756268034L);
                dump.end(devicePrefToken);
            }
            for (AccessoryFilter filter2 : this.mAccessoryPreferenceMap.keySet()) {
                long accessoryPrefToken = dump.start("accessory_preferences", 2246267895811L);
                filter2.dump(dump, "filter", 1146756268033L);
                this.mAccessoryPreferenceMap.get(filter2).dump(dump, "user_package", 1146756268034L);
                dump.end(accessoryPrefToken);
            }
        }
        dump.end(token);
    }

    private static Intent createDeviceAttachedIntent(UsbDevice device) {
        Intent intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intent.putExtra("device", device);
        intent.putExtra(BACKUP_SUB_ID, device.getBackupSubProductId());
        intent.putExtra(BACKUP_SUB_NAME, device.getBackupSubDeviceName());
        intent.addFlags(285212672);
        return intent;
    }
}
