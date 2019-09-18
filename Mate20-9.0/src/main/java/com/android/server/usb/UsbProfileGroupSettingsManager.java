package com.android.server.usb;

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
import android.net.util.NetworkConstants;
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

class UsbProfileGroupSettingsManager {
    protected static final String BACKUP_SUB_ID = "subId";
    protected static final String BACKUP_SUB_NAME = "subName";
    private static final boolean DEBUG = false;
    private static final String TAG = UsbProfileGroupSettingsManager.class.getSimpleName();
    private static final File sSingleUserSettingsFile = new File("/data/system/usb_device_manager.xml");
    @GuardedBy("mLock")
    private final HashMap<AccessoryFilter, UserPackage> mAccessoryPreferenceMap = new HashMap<>();
    private final Context mContext;
    @GuardedBy("mLock")
    private final HashMap<DeviceFilter, UserPackage> mDevicePreferenceMap = new HashMap<>();
    private final boolean mDisablePermissionDialogs;
    @GuardedBy("mLock")
    private boolean mIsWriteSettingsScheduled;
    private final Object mLock = new Object();
    private final MtpNotificationManager mMtpNotificationManager;
    private final PackageManager mPackageManager;
    MyPackageMonitor mPackageMonitor = new MyPackageMonitor();
    /* access modifiers changed from: private */
    public final UserHandle mParentUser;
    private final AtomicFile mSettingsFile;
    private final UsbSettingsManager mSettingsManager;
    /* access modifiers changed from: private */
    public final UserManager mUserManager;

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

    @Immutable
    private static class UserPackage {
        final String packageName;
        final UserHandle user;

        private UserPackage(String packageName2, UserHandle user2) {
            this.packageName = packageName2;
            this.user = user2;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof UserPackage)) {
                return false;
            }
            UserPackage other = (UserPackage) obj;
            if (this.user.equals(other.user) && this.packageName.equals(other.packageName)) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (31 * this.user.hashCode()) + this.packageName.hashCode();
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

    UsbProfileGroupSettingsManager(Context context, UserHandle user, UsbSettingsManager settingsManager) {
        try {
            Context parentUserContext = context.createPackageContextAsUser(PackageManagerService.PLATFORM_PACKAGE_NAME, 0, user);
            this.mContext = context;
            this.mPackageManager = context.getPackageManager();
            this.mSettingsManager = settingsManager;
            this.mUserManager = (UserManager) context.getSystemService("user");
            this.mParentUser = user;
            this.mSettingsFile = new AtomicFile(new File(Environment.getUserSystemDirectory(user.getIdentifier()), "usb_device_manager.xml"), "usb-state");
            this.mDisablePermissionDialogs = context.getResources().getBoolean(17956931);
            synchronized (this.mLock) {
                if (UserHandle.SYSTEM.equals(user)) {
                    upgradeSingleUserLocked();
                }
                readSettingsLocked();
            }
            this.mPackageMonitor.register(context, null, UserHandle.ALL, true);
            this.mMtpNotificationManager = new MtpNotificationManager(parentUserContext, new MtpNotificationManager.OnOpenInAppListener() {
                public final void onOpenInApp(UsbDevice usbDevice) {
                    UsbProfileGroupSettingsManager.this.resolveActivity(UsbProfileGroupSettingsManager.createDeviceAttachedIntent(usbDevice), usbDevice, false);
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Missing android package");
        }
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

    @GuardedBy("mLock")
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
                IoUtils.closeQuietly(null);
                throw th;
            }
            IoUtils.closeQuietly(fis);
            scheduleWriteSettingsLocked();
            sSingleUserSettingsFile.delete();
        }
    }

    @GuardedBy("mLock")
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
            IoUtils.closeQuietly(null);
            throw th;
        }
        IoUtils.closeQuietly(stream);
    }

    @GuardedBy("mLock")
    private void scheduleWriteSettingsLocked() {
        if (!this.mIsWriteSettingsScheduled) {
            this.mIsWriteSettingsScheduled = true;
            AsyncTask.execute(new Runnable() {
                public final void run() {
                    UsbProfileGroupSettingsManager.lambda$scheduleWriteSettingsLocked$1(UsbProfileGroupSettingsManager.this);
                }
            });
        }
    }

    public static /* synthetic */ void lambda$scheduleWriteSettingsLocked$1(UsbProfileGroupSettingsManager usbProfileGroupSettingsManager) {
        synchronized (usbProfileGroupSettingsManager.mLock) {
            try {
                FileOutputStream fos = usbProfileGroupSettingsManager.mSettingsFile.startWrite();
                FastXmlSerializer serializer = new FastXmlSerializer();
                serializer.setOutput(fos, StandardCharsets.UTF_8.name());
                serializer.startDocument(null, true);
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startTag(null, "settings");
                for (DeviceFilter filter : usbProfileGroupSettingsManager.mDevicePreferenceMap.keySet()) {
                    serializer.startTag(null, "preference");
                    serializer.attribute(null, "package", usbProfileGroupSettingsManager.mDevicePreferenceMap.get(filter).packageName);
                    serializer.attribute(null, "user", String.valueOf(usbProfileGroupSettingsManager.getSerial(usbProfileGroupSettingsManager.mDevicePreferenceMap.get(filter).user)));
                    filter.write(serializer);
                    serializer.endTag(null, "preference");
                }
                for (AccessoryFilter filter2 : usbProfileGroupSettingsManager.mAccessoryPreferenceMap.keySet()) {
                    serializer.startTag(null, "preference");
                    serializer.attribute(null, "package", usbProfileGroupSettingsManager.mAccessoryPreferenceMap.get(filter2).packageName);
                    serializer.attribute(null, "user", String.valueOf(usbProfileGroupSettingsManager.getSerial(usbProfileGroupSettingsManager.mAccessoryPreferenceMap.get(filter2).user)));
                    filter2.write(serializer);
                    serializer.endTag(null, "preference");
                }
                serializer.endTag(null, "settings");
                serializer.endDocument();
                usbProfileGroupSettingsManager.mSettingsFile.finishWrite(fos);
            } catch (IOException e) {
                Slog.e(TAG, "Failed to write settings", e);
                if (0 != 0) {
                    usbProfileGroupSettingsManager.mSettingsFile.failWrite(null);
                }
            }
            usbProfileGroupSettingsManager.mIsWriteSettingsScheduled = false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x007d, code lost:
        if (r2 != null) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x007f, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00a0, code lost:
        if (r2 == null) goto L_0x00a3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00a3, code lost:
        return false;
     */
    private boolean packageMatchesLocked(ResolveInfo info, String metaDataName, UsbDevice device, UsbAccessory accessory) {
        if (isForwardMatch(info)) {
            return true;
        }
        XmlResourceParser parser = null;
        try {
            parser = info.activityInfo.loadXmlMetaData(this.mPackageManager, metaDataName);
            if (parser == null) {
                String str = TAG;
                Slog.w(str, "no meta-data for " + info);
                if (parser != null) {
                    parser.close();
                }
                return false;
            }
            XmlUtils.nextElement(parser);
            while (parser.getEventType() != 1) {
                String tagName = parser.getName();
                if (device == null || !"usb-device".equals(tagName)) {
                    if (accessory != null) {
                        if ("usb-accessory".equals(tagName) && AccessoryFilter.read(parser).matches(accessory)) {
                            if (parser != null) {
                                parser.close();
                            }
                            return true;
                        }
                    }
                } else if (DeviceFilter.read(parser).matches(device)) {
                    if (parser != null) {
                        parser.close();
                    }
                    return true;
                }
                XmlUtils.nextElement(parser);
            }
        } catch (Exception e) {
            String str2 = TAG;
            Slog.w(str2, "Unable to load component info " + info.toString(), e);
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    private ArrayList<ResolveInfo> queryIntentActivitiesForAllProfiles(Intent intent) {
        List<UserInfo> profiles = this.mUserManager.getEnabledProfiles(this.mParentUser.getIdentifier());
        ArrayList<ResolveInfo> resolveInfos = new ArrayList<>();
        int numProfiles = profiles.size();
        for (int i = 0; i < numProfiles; i++) {
            resolveInfos.addAll(this.mPackageManager.queryIntentActivitiesAsUser(intent, 128, profiles.get(i).id));
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
                    highestPriorityMatchesByUserId.put(match.targetUserId, new ArrayList());
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
        int numNonParentActivityMatches = 0;
        int numParentActivityMatches = 0;
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
        if (numParentActivityMatches != 0 && numNonParentActivityMatches != 0) {
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
            if (packageMatchesLocked(resolveInfo, intent.getAction(), device, null)) {
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
            if (packageMatchesLocked(resolveInfo, intent.getAction(), null, accessory)) {
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

    /* access modifiers changed from: private */
    public void resolveActivity(Intent intent, UsbDevice device, boolean showMtpNotification) {
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
        this.mContext.sendBroadcast(intent);
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
        UserHandle user;
        if (matches.size() == 0) {
            if (accessory != null) {
                String uri = accessory.getUri();
                if (uri != null && uri.length() > 0) {
                    Intent dialogIntent = new Intent();
                    dialogIntent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbAccessoryUriActivity");
                    dialogIntent.addFlags(268435456);
                    dialogIntent.putExtra("accessory", accessory);
                    dialogIntent.putExtra("uri", uri);
                    try {
                        this.mContext.startActivityAsUser(dialogIntent, this.mParentUser);
                    } catch (ActivityNotFoundException e) {
                        Slog.e(TAG, "unable to start UsbAccessoryUriActivity");
                    }
                }
            }
            return;
        }
        if (defaultActivity != null) {
            UsbUserSettingsManager defaultRIUserSettings = this.mSettingsManager.getSettingsForUser(UserHandle.getUserId(defaultActivity.applicationInfo.uid));
            if (device != null) {
                defaultRIUserSettings.grantDevicePermission(device, defaultActivity.applicationInfo.uid);
            } else if (accessory != null) {
                defaultRIUserSettings.grantAccessoryPermission(accessory, defaultActivity.applicationInfo.uid);
            }
            try {
                intent.setComponent(new ComponentName(defaultActivity.packageName, defaultActivity.name));
                this.mContext.startActivityAsUser(intent, UserHandle.getUserHandleForUid(defaultActivity.applicationInfo.uid));
            } catch (ActivityNotFoundException e2) {
                Slog.e(TAG, "startActivity failed", e2);
            }
        } else {
            Intent resolverIntent = new Intent();
            resolverIntent.addFlags(268435456);
            if (matches.size() == 1) {
                ResolveInfo rInfo = matches.get(0);
                resolverIntent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbConfirmActivity");
                resolverIntent.putExtra("rinfo", rInfo);
                user = UserHandle.getUserHandleForUid(rInfo.activityInfo.applicationInfo.uid);
                if (device != null) {
                    resolverIntent.putExtra("device", device);
                } else {
                    resolverIntent.putExtra("accessory", accessory);
                }
            } else {
                user = this.mParentUser;
                resolverIntent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbResolverActivity");
                resolverIntent.putParcelableArrayListExtra("rlist", matches);
                resolverIntent.putExtra("android.intent.extra.INTENT", intent);
            }
            try {
                this.mContext.startActivityAsUser(resolverIntent, user);
            } catch (ActivityNotFoundException e3) {
                String str = TAG;
                Slog.e(str, "unable to start activity " + resolverIntent, e3);
            }
        }
    }

    private ActivityInfo getDefaultActivityLocked(ArrayList<ResolveInfo> matches, UserPackage userPackage) {
        if (userPackage != null) {
            Iterator<ResolveInfo> it = matches.iterator();
            while (it.hasNext()) {
                ResolveInfo info = it.next();
                if (info.activityInfo != null && userPackage.equals(new UserPackage(info.activityInfo.packageName, UserHandle.getUserHandleForUid(info.activityInfo.applicationInfo.uid)))) {
                    return info.activityInfo;
                }
            }
        }
        if (matches.size() == 1) {
            ActivityInfo activityInfo = matches.get(0).activityInfo;
            if (activityInfo != null) {
                if (this.mDisablePermissionDialogs) {
                    return activityInfo;
                }
                if (activityInfo.applicationInfo == null || (1 & activityInfo.applicationInfo.flags) == 0) {
                    return null;
                }
                return activityInfo;
            }
        }
        return null;
    }

    @GuardedBy("mLock")
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

    @GuardedBy("mLock")
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

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004d, code lost:
        if (r0 != null) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004f, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0070, code lost:
        if (r0 == null) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0073, code lost:
        return r2;
     */
    @GuardedBy("mLock")
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
        } catch (Exception e) {
            Slog.w(TAG, "Unable to load component info " + aInfo.toString(), e);
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0040, code lost:
        return;
     */
    public void handlePackageAdded(UserPackage userPackage) {
        synchronized (this.mLock) {
            boolean changed = false;
            try {
                ActivityInfo[] activities = this.mPackageManager.getPackageInfoAsUser(userPackage.packageName, NetworkConstants.ICMPV6_ECHO_REPLY_TYPE, userPackage.user.getIdentifier()).activities;
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
                Slog.e(TAG, "handlePackageUpdate could not find package " + userPackage, e);
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
        boolean changed;
        DeviceFilter filter = new DeviceFilter(device);
        synchronized (this.mLock) {
            changed = true;
            if (packageName == null) {
                try {
                    if (this.mDevicePreferenceMap.remove(filter) == null) {
                        changed = false;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                UserPackage userPackage = new UserPackage(packageName, user);
                changed = true ^ userPackage.equals(this.mDevicePreferenceMap.get(filter));
                if (changed) {
                    this.mDevicePreferenceMap.put(filter, userPackage);
                }
            }
            if (changed) {
                scheduleWriteSettingsLocked();
            }
        }
        boolean z = changed;
    }

    /* access modifiers changed from: package-private */
    public void setAccessoryPackage(UsbAccessory accessory, String packageName, UserHandle user) {
        boolean changed;
        AccessoryFilter filter = new AccessoryFilter(accessory);
        synchronized (this.mLock) {
            changed = true;
            if (packageName == null) {
                try {
                    if (this.mAccessoryPreferenceMap.remove(filter) == null) {
                        changed = false;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                UserPackage userPackage = new UserPackage(packageName, user);
                changed = true ^ userPackage.equals(this.mAccessoryPreferenceMap.get(filter));
                if (changed) {
                    this.mAccessoryPreferenceMap.put(filter, userPackage);
                }
            }
            if (changed) {
                scheduleWriteSettingsLocked();
            }
        }
        boolean z = changed;
    }

    /* access modifiers changed from: package-private */
    public boolean hasDefaults(String packageName, UserHandle user) {
        UserPackage userPackage = new UserPackage(packageName, user);
        synchronized (this.mLock) {
            if (this.mDevicePreferenceMap.values().contains(userPackage)) {
                return true;
            }
            boolean contains = this.mAccessoryPreferenceMap.values().contains(userPackage);
            return contains;
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

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x006f, code lost:
        return r0;
     */
    private boolean clearPackageDefaultsLocked(UserPackage userPackage) {
        boolean cleared = false;
        synchronized (this.mLock) {
            try {
                if (this.mDevicePreferenceMap.containsValue(userPackage)) {
                    DeviceFilter[] keys = (DeviceFilter[]) this.mDevicePreferenceMap.keySet().toArray(new DeviceFilter[0]);
                    boolean cleared2 = false;
                    int i = 0;
                    while (i < keys.length) {
                        try {
                            DeviceFilter key = keys[i];
                            if (userPackage.equals(this.mDevicePreferenceMap.get(key))) {
                                this.mDevicePreferenceMap.remove(key);
                                cleared2 = true;
                            }
                            i++;
                        } catch (Throwable th) {
                            th = th;
                            boolean z = cleared2;
                            throw th;
                        }
                    }
                    cleared = cleared2;
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
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    public void dump(DualDumpOutputStream dump, String idName, long id) {
        DualDumpOutputStream dualDumpOutputStream = dump;
        long token = dump.start(idName, id);
        synchronized (this.mLock) {
            dualDumpOutputStream.write("parent_user_id", 1120986464257L, this.mParentUser.getIdentifier());
            for (DeviceFilter filter : this.mDevicePreferenceMap.keySet()) {
                long devicePrefToken = dualDumpOutputStream.start("device_preferences", 2246267895810L);
                filter.dump(dualDumpOutputStream, "filter", 1146756268033L);
                this.mDevicePreferenceMap.get(filter).dump(dualDumpOutputStream, "user_package", 1146756268034L);
                dualDumpOutputStream.end(devicePrefToken);
            }
            for (AccessoryFilter filter2 : this.mAccessoryPreferenceMap.keySet()) {
                long accessoryPrefToken = dualDumpOutputStream.start("accessory_preferences", 2246267895811L);
                filter2.dump(dualDumpOutputStream, "filter", 1146756268033L);
                this.mAccessoryPreferenceMap.get(filter2).dump(dualDumpOutputStream, "user_package", 1146756268034L);
                dualDumpOutputStream.end(accessoryPrefToken);
            }
        }
        dualDumpOutputStream.end(token);
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
