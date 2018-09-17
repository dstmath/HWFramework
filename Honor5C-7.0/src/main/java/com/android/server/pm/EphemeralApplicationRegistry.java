package com.android.server.pm;

import android.content.Context;
import android.content.pm.EphemeralApplicationInfo;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageUserState;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Environment;
import android.provider.Settings.Global;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import com.android.server.display.RampAnimator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import libcore.io.IoUtils;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class EphemeralApplicationRegistry {
    private static final String ATTR_GRANTED = "granted";
    private static final String ATTR_LABEL = "label";
    private static final String ATTR_NAME = "name";
    private static final boolean DEBUG = false;
    private static final long DEFAULT_UNINSTALLED_EPHEMERAL_APP_CACHE_DURATION_MILLIS = 2592000000L;
    private static final boolean ENABLED = false;
    private static final String EPHEMERAL_APPS_FOLDER = "ephemeral";
    private static final String EPHEMERAL_APP_COOKIE_FILE_PREFIX = "cookie_";
    private static final String EPHEMERAL_APP_COOKIE_FILE_SIFFIX = ".dat";
    private static final String EPHEMERAL_APP_ICON_FILE = "icon.png";
    private static final String EPHEMERAL_APP_METADATA_FILE = "metadata.xml";
    private static final char[] HEX_ARRAY = null;
    private static final String LOG_TAG = "EphemeralAppRegistry";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_PERM = "perm";
    private static final String TAG_PERMS = "perms";
    private final PackageManagerService mService;
    @GuardedBy("mService.mPackages")
    private SparseArray<List<UninstalledEphemeralAppState>> mUninstalledEphemeralApps;

    private static final class UninstalledEphemeralAppState {
        final EphemeralApplicationInfo mEphemeralApplicationInfo;
        final long mTimestamp;

        public UninstalledEphemeralAppState(EphemeralApplicationInfo ephemeralApp, long timestamp) {
            this.mEphemeralApplicationInfo = ephemeralApp;
            this.mTimestamp = timestamp;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.EphemeralApplicationRegistry.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.EphemeralApplicationRegistry.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.EphemeralApplicationRegistry.<clinit>():void");
    }

    public EphemeralApplicationRegistry(PackageManagerService service) {
        this.mService = service;
    }

    public byte[] getEphemeralApplicationCookieLPw(String packageName, int userId) {
        return EmptyArray.BYTE;
    }

    public boolean setEphemeralApplicationCookieLPw(String packageName, byte[] cookie, int userId) {
        return ENABLED;
    }

    public Bitmap getEphemeralApplicationIconLPw(String packageName, int userId) {
        return null;
    }

    public List<EphemeralApplicationInfo> getEphemeralApplicationsLPw(int userId) {
        return Collections.emptyList();
    }

    public void onPackageInstalledLPw(Package pkg) {
    }

    public void onPackageUninstalledLPw(Package pkg) {
    }

    public void onUserRemovedLPw(int userId) {
    }

    private void addUninstalledEphemeralAppLPw(Package pkg, int userId) {
        EphemeralApplicationInfo uninstalledApp = createEphemeralAppInfoForPackage(pkg, userId);
        if (uninstalledApp != null) {
            if (this.mUninstalledEphemeralApps == null) {
                this.mUninstalledEphemeralApps = new SparseArray();
            }
            List<UninstalledEphemeralAppState> uninstalledAppStates = (List) this.mUninstalledEphemeralApps.get(userId);
            if (uninstalledAppStates == null) {
                uninstalledAppStates = new ArrayList();
                this.mUninstalledEphemeralApps.put(userId, uninstalledAppStates);
            }
            uninstalledAppStates.add(new UninstalledEphemeralAppState(uninstalledApp, System.currentTimeMillis()));
            writeUninstalledEphemeralAppMetadata(uninstalledApp, userId);
            writeEphemeralApplicationIconLPw(pkg, userId);
        }
    }

    private void writeEphemeralApplicationIconLPw(Package pkg, int userId) {
        Exception e;
        Throwable th;
        Throwable th2 = null;
        if (getEphemeralApplicationDir(pkg.packageName, userId).exists()) {
            Bitmap bitmap;
            Drawable icon = pkg.applicationInfo.loadIcon(this.mService.mContext.getPackageManager());
            if (icon instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) icon).getBitmap();
            } else {
                bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Config.ARGB_8888);
                icon.draw(new Canvas(bitmap));
            }
            FileOutputStream fileOutputStream = null;
            try {
                FileOutputStream out = new FileOutputStream(new File(getEphemeralApplicationDir(pkg.packageName, userId), EPHEMERAL_APP_ICON_FILE));
                try {
                    bitmap.compress(CompressFormat.PNG, 100, out);
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (Exception e2) {
                            e = e2;
                            fileOutputStream = out;
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    fileOutputStream = out;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable th5) {
                            if (th2 == null) {
                                th2 = th5;
                            } else if (th2 != th5) {
                                th2.addSuppressed(th5);
                            }
                        }
                    }
                    if (th2 == null) {
                        try {
                            throw th2;
                        } catch (Exception e3) {
                            e = e3;
                            Slog.e(LOG_TAG, "Error writing ephemeral app icon", e);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (th2 == null) {
                    throw th;
                }
                throw th2;
            }
        }
    }

    private void pruneUninstalledEphemeralAppsLPw(int userId) {
        long maxCacheDurationMillis = Global.getLong(this.mService.mContext.getContentResolver(), "uninstalled_ephemeral_app_cache_duration_millis", DEFAULT_UNINSTALLED_EPHEMERAL_APP_CACHE_DURATION_MILLIS);
        if (this.mUninstalledEphemeralApps != null) {
            List<UninstalledEphemeralAppState> uninstalledAppStates = (List) this.mUninstalledEphemeralApps.get(userId);
            if (uninstalledAppStates != null) {
                for (int j = uninstalledAppStates.size() - 1; j >= 0; j--) {
                    if (System.currentTimeMillis() - ((UninstalledEphemeralAppState) uninstalledAppStates.get(j)).mTimestamp > maxCacheDurationMillis) {
                        uninstalledAppStates.remove(j);
                    }
                }
                if (uninstalledAppStates.isEmpty()) {
                    this.mUninstalledEphemeralApps.remove(userId);
                }
            }
        }
        File ephemeralAppsDir = getEphemeralApplicationsDir(userId);
        if (ephemeralAppsDir.exists()) {
            File[] files = ephemeralAppsDir.listFiles();
            if (files != null) {
                for (File ephemeralDir : files) {
                    if (ephemeralDir.isDirectory()) {
                        File metadataFile = new File(ephemeralDir, EPHEMERAL_APP_METADATA_FILE);
                        if (metadataFile.exists() && System.currentTimeMillis() - metadataFile.lastModified() > maxCacheDurationMillis) {
                            deleteDir(ephemeralDir);
                        }
                    }
                }
            }
        }
    }

    private List<EphemeralApplicationInfo> getInstalledEphemeralApplicationsLPr(int userId) {
        List<EphemeralApplicationInfo> result = null;
        int packageCount = this.mService.mPackages.size();
        for (int i = 0; i < packageCount; i++) {
            Package pkg = (Package) this.mService.mPackages.valueAt(i);
            if (pkg.applicationInfo.isEphemeralApp()) {
                EphemeralApplicationInfo info = createEphemeralAppInfoForPackage(pkg, userId);
                if (info != null) {
                    if (result == null) {
                        result = new ArrayList();
                    }
                    result.add(info);
                }
            }
        }
        return result;
    }

    private EphemeralApplicationInfo createEphemeralAppInfoForPackage(Package pkg, int userId) {
        PackageSetting ps = pkg.mExtras;
        if (ps == null) {
            return null;
        }
        PackageUserState userState = ps.readUserState(userId);
        if (userState == null || !userState.installed || userState.hidden) {
            return null;
        }
        String[] requestedPermissions = new String[pkg.requestedPermissions.size()];
        pkg.requestedPermissions.toArray(requestedPermissions);
        Set<String> permissions = ps.getPermissionsState().getPermissions(userId);
        String[] grantedPermissions = new String[permissions.size()];
        permissions.toArray(grantedPermissions);
        return new EphemeralApplicationInfo(pkg.applicationInfo, requestedPermissions, grantedPermissions);
    }

    private List<EphemeralApplicationInfo> getUninstalledEphemeralApplicationsLPr(int userId) {
        List<UninstalledEphemeralAppState> uninstalledAppStates = getUninstalledEphemeralAppStatesLPr(userId);
        if (uninstalledAppStates == null || uninstalledAppStates.isEmpty()) {
            return Collections.emptyList();
        }
        List<EphemeralApplicationInfo> uninstalledApps = new ArrayList();
        int stateCount = uninstalledAppStates.size();
        for (int i = 0; i < stateCount; i++) {
            uninstalledApps.add(((UninstalledEphemeralAppState) uninstalledAppStates.get(i)).mEphemeralApplicationInfo);
        }
        return uninstalledApps;
    }

    private void propagateEphemeralAppPermissionsIfNeeded(Package pkg, int userId) {
        EphemeralApplicationInfo appInfo = getOrParseUninstalledEphemeralAppInfo(pkg.packageName, userId);
        if (appInfo != null && !ArrayUtils.isEmpty(appInfo.getGrantedPermissions())) {
            long identity = Binder.clearCallingIdentity();
            try {
                for (String grantedPermission : appInfo.getGrantedPermissions()) {
                    this.mService.grantRuntimePermission(pkg.packageName, grantedPermission, userId);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private EphemeralApplicationInfo getOrParseUninstalledEphemeralAppInfo(String packageName, int userId) {
        UninstalledEphemeralAppState uninstalledAppState;
        if (this.mUninstalledEphemeralApps != null) {
            List<UninstalledEphemeralAppState> uninstalledAppStates = (List) this.mUninstalledEphemeralApps.get(userId);
            if (uninstalledAppStates != null) {
                int appCount = uninstalledAppStates.size();
                for (int i = 0; i < appCount; i++) {
                    uninstalledAppState = (UninstalledEphemeralAppState) uninstalledAppStates.get(i);
                    if (uninstalledAppState.mEphemeralApplicationInfo.getPackageName().equals(packageName)) {
                        return uninstalledAppState.mEphemeralApplicationInfo;
                    }
                }
            }
        }
        uninstalledAppState = parseMetadataFile(new File(getEphemeralApplicationDir(packageName, userId), EPHEMERAL_APP_METADATA_FILE));
        if (uninstalledAppState == null) {
            return null;
        }
        return uninstalledAppState.mEphemeralApplicationInfo;
    }

    private List<UninstalledEphemeralAppState> getUninstalledEphemeralAppStatesLPr(int userId) {
        List<UninstalledEphemeralAppState> list = null;
        if (this.mUninstalledEphemeralApps != null) {
            list = (List) this.mUninstalledEphemeralApps.get(userId);
            if (list != null) {
                return list;
            }
        }
        File ephemeralAppsDir = getEphemeralApplicationsDir(userId);
        if (ephemeralAppsDir.exists()) {
            File[] files = ephemeralAppsDir.listFiles();
            if (files != null) {
                for (File ephemeralDir : files) {
                    if (ephemeralDir.isDirectory()) {
                        UninstalledEphemeralAppState uninstalledAppState = parseMetadataFile(new File(ephemeralDir, EPHEMERAL_APP_METADATA_FILE));
                        if (uninstalledAppState != null) {
                            if (list == null) {
                                list = new ArrayList();
                            }
                            list.add(uninstalledAppState);
                        }
                    }
                }
            }
        }
        if (list != null) {
            if (this.mUninstalledEphemeralApps == null) {
                this.mUninstalledEphemeralApps = new SparseArray();
            }
            this.mUninstalledEphemeralApps.put(userId, list);
        }
        return list;
    }

    private static boolean isValidCookie(Context context, byte[] cookie) {
        boolean z = true;
        if (ArrayUtils.isEmpty(cookie)) {
            return true;
        }
        if (cookie.length > context.getPackageManager().getEphemeralCookieMaxSizeBytes()) {
            z = ENABLED;
        }
        return z;
    }

    private static UninstalledEphemeralAppState parseMetadataFile(File metadataFile) {
        if (!metadataFile.exists()) {
            return null;
        }
        try {
            FileInputStream in = new AtomicFile(metadataFile).openRead();
            File ephemeralDir = metadataFile.getParentFile();
            long timestamp = metadataFile.lastModified();
            String packageName = ephemeralDir.getName();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(in, StandardCharsets.UTF_8.name());
                UninstalledEphemeralAppState uninstalledEphemeralAppState = new UninstalledEphemeralAppState(parseMetadata(parser, packageName), timestamp);
                IoUtils.closeQuietly(in);
                return uninstalledEphemeralAppState;
            } catch (Exception e) {
                throw new IllegalStateException("Failed parsing ephemeral metadata file: " + metadataFile, e);
            } catch (Throwable th) {
                IoUtils.closeQuietly(in);
            }
        } catch (FileNotFoundException e2) {
            Slog.i(LOG_TAG, "No ephemeral metadata file");
            return null;
        }
    }

    private static File computeEphemeralCookieFile(Package pkg, int userId) {
        return new File(getEphemeralApplicationDir(pkg.packageName, userId), EPHEMERAL_APP_COOKIE_FILE_PREFIX + computePackageCertDigest(pkg) + EPHEMERAL_APP_COOKIE_FILE_SIFFIX);
    }

    private static File peekEphemeralCookieFile(String packageName, int userId) {
        File appDir = getEphemeralApplicationDir(packageName, userId);
        if (!appDir.exists()) {
            return null;
        }
        for (File file : appDir.listFiles()) {
            if (!file.isDirectory() && file.getName().startsWith(EPHEMERAL_APP_COOKIE_FILE_PREFIX) && file.getName().endsWith(EPHEMERAL_APP_COOKIE_FILE_SIFFIX)) {
                return file;
            }
        }
        return null;
    }

    private static EphemeralApplicationInfo parseMetadata(XmlPullParser parser, String packageName) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (TAG_PACKAGE.equals(parser.getName())) {
                return parsePackage(parser, packageName);
            }
        }
        return null;
    }

    private static EphemeralApplicationInfo parsePackage(XmlPullParser parser, String packageName) throws IOException, XmlPullParserException {
        String label = parser.getAttributeValue(null, ATTR_LABEL);
        List<String> outRequestedPermissions = new ArrayList();
        List<String> outGrantedPermissions = new ArrayList();
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (TAG_PERMS.equals(parser.getName())) {
                parsePermissions(parser, outRequestedPermissions, outGrantedPermissions);
            }
        }
        String[] requestedPermissions = new String[outRequestedPermissions.size()];
        outRequestedPermissions.toArray(requestedPermissions);
        String[] grantedPermissions = new String[outGrantedPermissions.size()];
        outGrantedPermissions.toArray(grantedPermissions);
        return new EphemeralApplicationInfo(packageName, label, requestedPermissions, grantedPermissions);
    }

    private static void parsePermissions(XmlPullParser parser, List<String> outRequestedPermissions, List<String> outGrantedPermissions) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (TAG_PERM.equals(parser.getName())) {
                String permission = XmlUtils.readStringAttribute(parser, ATTR_NAME);
                outRequestedPermissions.add(permission);
                if (XmlUtils.readBooleanAttribute(parser, ATTR_GRANTED)) {
                    outGrantedPermissions.add(permission);
                }
            }
        }
    }

    private void writeUninstalledEphemeralAppMetadata(EphemeralApplicationInfo ephemeralApp, int userId) {
        File appDir = getEphemeralApplicationDir(ephemeralApp.getPackageName(), userId);
        if (appDir.exists() || appDir.mkdirs()) {
            AtomicFile destination = new AtomicFile(new File(appDir, EPHEMERAL_APP_METADATA_FILE));
            AutoCloseable autoCloseable = null;
            try {
                autoCloseable = destination.startWrite();
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(autoCloseable, StandardCharsets.UTF_8.name());
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startDocument(null, Boolean.valueOf(true));
                serializer.startTag(null, TAG_PACKAGE);
                serializer.attribute(null, ATTR_LABEL, ephemeralApp.loadLabel(this.mService.mContext.getPackageManager()).toString());
                serializer.startTag(null, TAG_PERMS);
                for (String permission : ephemeralApp.getRequestedPermissions()) {
                    serializer.startTag(null, TAG_PERM);
                    serializer.attribute(null, ATTR_NAME, permission);
                    if (ArrayUtils.contains(ephemeralApp.getGrantedPermissions(), permission)) {
                        serializer.attribute(null, ATTR_GRANTED, String.valueOf(true));
                    }
                    serializer.endTag(null, TAG_PERM);
                }
                serializer.endTag(null, TAG_PERMS);
                serializer.endTag(null, TAG_PACKAGE);
                serializer.endDocument();
                destination.finishWrite(autoCloseable);
            } catch (Throwable t) {
                Slog.wtf(LOG_TAG, "Failed to write ephemeral state, restoring backup", t);
                destination.failWrite(null);
            } finally {
                IoUtils.closeQuietly(autoCloseable);
            }
        }
    }

    private static String computePackageCertDigest(Package pkg) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
            messageDigest.update(pkg.mSignatures[0].toByteArray());
            byte[] digest = messageDigest.digest();
            int digestLength = digest.length;
            char[] chars = new char[(digestLength * 2)];
            for (int i = 0; i < digestLength; i++) {
                int byteHex = digest[i] & RampAnimator.DEFAULT_MAX_BRIGHTNESS;
                chars[i * 2] = HEX_ARRAY[byteHex >>> 4];
                chars[(i * 2) + 1] = HEX_ARRAY[byteHex & 15];
            }
            return new String(chars);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static File getEphemeralApplicationsDir(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), EPHEMERAL_APPS_FOLDER);
    }

    private static File getEphemeralApplicationDir(String packageName, int userId) {
        return new File(getEphemeralApplicationsDir(userId), packageName);
    }

    private static void deleteDir(File dir) {
        if (dir.listFiles() != null) {
            for (File file : dir.listFiles()) {
                deleteDir(file);
            }
        }
        dir.delete();
    }
}
