package android.app.backup;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.hwtheme.HwThemeManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FullBackup {
    public static final String APK_TREE_TOKEN = "a";
    public static final String APPS_PREFIX = "apps/";
    public static final String CACHE_TREE_TOKEN = "c";
    public static final String CONF_TOKEN_INTENT_EXTRA = "conftoken";
    public static final String DATABASE_TREE_TOKEN = "db";
    public static final String DEVICE_CACHE_TREE_TOKEN = "d_c";
    public static final String DEVICE_DATABASE_TREE_TOKEN = "d_db";
    public static final String DEVICE_FILES_TREE_TOKEN = "d_f";
    public static final String DEVICE_NO_BACKUP_TREE_TOKEN = "d_nb";
    public static final String DEVICE_ROOT_TREE_TOKEN = "d_r";
    public static final String DEVICE_SHAREDPREFS_TREE_TOKEN = "d_sp";
    public static final String FILES_TREE_TOKEN = "f";
    public static final String FLAG_REQUIRED_CLIENT_SIDE_ENCRYPTION = "clientSideEncryption";
    public static final String FLAG_REQUIRED_DEVICE_TO_DEVICE_TRANSFER = "deviceToDeviceTransfer";
    public static final String FLAG_REQUIRED_FAKE_CLIENT_SIDE_ENCRYPTION = "fakeClientSideEncryption";
    public static final String FULL_BACKUP_INTENT_ACTION = "fullback";
    public static final String FULL_RESTORE_INTENT_ACTION = "fullrest";
    public static final String KEY_VALUE_DATA_TOKEN = "k";
    public static final String MANAGED_EXTERNAL_TREE_TOKEN = "ef";
    public static final String NO_BACKUP_TREE_TOKEN = "nb";
    public static final String OBB_TREE_TOKEN = "obb";
    public static final String ROOT_TREE_TOKEN = "r";
    public static final String SHAREDPREFS_TREE_TOKEN = "sp";
    public static final String SHARED_PREFIX = "shared/";
    public static final String SHARED_STORAGE_TOKEN = "shared";
    static final String TAG = "FullBackup";
    static final String TAG_XML_PARSER = "BackupXmlParserLogging";
    private static final Map<String, BackupScheme> kPackageBackupSchemeMap = new ArrayMap();

    @UnsupportedAppUsage
    public static native int backupToTar(String str, String str2, String str3, String str4, String str5, FullBackupDataOutput fullBackupDataOutput);

    static synchronized BackupScheme getBackupScheme(Context context) {
        BackupScheme backupSchemeForPackage;
        synchronized (FullBackup.class) {
            backupSchemeForPackage = kPackageBackupSchemeMap.get(context.getPackageName());
            if (backupSchemeForPackage == null) {
                backupSchemeForPackage = new BackupScheme(context);
                kPackageBackupSchemeMap.put(context.getPackageName(), backupSchemeForPackage);
            }
        }
        return backupSchemeForPackage;
    }

    public static BackupScheme getBackupSchemeForTest(Context context) {
        BackupScheme testing = new BackupScheme(context);
        testing.mExcludes = new ArraySet<>();
        testing.mIncludes = new ArrayMap();
        return testing;
    }

    public static void restoreFile(ParcelFileDescriptor data, long size, int type, long mode, long mtime, File outFile) throws IOException {
        long j = 0;
        if (type != 2) {
            FileOutputStream out = null;
            if (outFile != null) {
                try {
                    File parent = outFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    out = new FileOutputStream(outFile);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to create/open file " + outFile.getPath(), e);
                }
            }
            byte[] buffer = new byte[32768];
            FileInputStream in = new FileInputStream(data.getFileDescriptor());
            long size2 = size;
            while (true) {
                if (size2 <= j) {
                    break;
                }
                int got = in.read(buffer, 0, size2 > ((long) buffer.length) ? buffer.length : (int) size2);
                if (got <= 0) {
                    Log.w(TAG, "Incomplete read: expected " + size2 + " but got " + (size - size2));
                    break;
                }
                if (out != null) {
                    try {
                        out.write(buffer, 0, got);
                    } catch (IOException e2) {
                        Log.e(TAG, "Unable to write to file " + outFile.getPath(), e2);
                        out.close();
                        outFile.delete();
                        out = null;
                    }
                }
                size2 -= (long) got;
                j = 0;
            }
            if (out != null) {
                out.close();
            }
        } else if (outFile != null) {
            outFile.mkdirs();
        }
        if (mode >= 0 && outFile != null) {
            try {
                Os.chmod(outFile.getPath(), (int) (mode & 448));
            } catch (ErrnoException e3) {
                e3.rethrowAsIOException();
            }
            outFile.setLastModified(mtime);
        }
    }

    @VisibleForTesting
    public static class BackupScheme {
        private static final String TAG_EXCLUDE = "exclude";
        private static final String TAG_INCLUDE = "include";
        private final File CACHE_DIR;
        private final File DATABASE_DIR;
        private final File DEVICE_CACHE_DIR;
        private final File DEVICE_DATABASE_DIR;
        private final File DEVICE_FILES_DIR;
        private final File DEVICE_NOBACKUP_DIR;
        private final File DEVICE_ROOT_DIR;
        private final File DEVICE_SHAREDPREF_DIR;
        private final File EXTERNAL_DIR;
        private final File FILES_DIR;
        private final File NOBACKUP_DIR;
        private final File ROOT_DIR;
        private final File SHAREDPREF_DIR;
        ArraySet<PathWithRequiredFlags> mExcludes;
        final int mFullBackupContent;
        Map<String, Set<PathWithRequiredFlags>> mIncludes;
        final PackageManager mPackageManager;
        final String mPackageName;
        final StorageManager mStorageManager;
        private StorageVolume[] mVolumes = null;

        /* access modifiers changed from: package-private */
        public String tokenToDirectoryPath(String domainToken) {
            try {
                if (domainToken.equals(FullBackup.FILES_TREE_TOKEN)) {
                    return this.FILES_DIR.getCanonicalPath();
                }
                if (domainToken.equals(FullBackup.DATABASE_TREE_TOKEN)) {
                    return this.DATABASE_DIR.getCanonicalPath();
                }
                if (domainToken.equals("r")) {
                    return this.ROOT_DIR.getCanonicalPath();
                }
                if (domainToken.equals(FullBackup.SHAREDPREFS_TREE_TOKEN)) {
                    return this.SHAREDPREF_DIR.getCanonicalPath();
                }
                if (domainToken.equals(FullBackup.CACHE_TREE_TOKEN)) {
                    return this.CACHE_DIR.getCanonicalPath();
                }
                if (domainToken.equals(FullBackup.NO_BACKUP_TREE_TOKEN)) {
                    return this.NOBACKUP_DIR.getCanonicalPath();
                }
                if (domainToken.equals(FullBackup.DEVICE_FILES_TREE_TOKEN)) {
                    return this.DEVICE_FILES_DIR.getCanonicalPath();
                }
                if (domainToken.equals(FullBackup.DEVICE_DATABASE_TREE_TOKEN)) {
                    return this.DEVICE_DATABASE_DIR.getCanonicalPath();
                }
                if (domainToken.equals(FullBackup.DEVICE_ROOT_TREE_TOKEN)) {
                    return this.DEVICE_ROOT_DIR.getCanonicalPath();
                }
                if (domainToken.equals(FullBackup.DEVICE_SHAREDPREFS_TREE_TOKEN)) {
                    return this.DEVICE_SHAREDPREF_DIR.getCanonicalPath();
                }
                if (domainToken.equals(FullBackup.DEVICE_CACHE_TREE_TOKEN)) {
                    return this.DEVICE_CACHE_DIR.getCanonicalPath();
                }
                if (domainToken.equals(FullBackup.DEVICE_NO_BACKUP_TREE_TOKEN)) {
                    return this.DEVICE_NOBACKUP_DIR.getCanonicalPath();
                }
                if (domainToken.equals(FullBackup.MANAGED_EXTERNAL_TREE_TOKEN)) {
                    if (this.EXTERNAL_DIR != null) {
                        return this.EXTERNAL_DIR.getCanonicalPath();
                    }
                    return null;
                } else if (domainToken.startsWith(FullBackup.SHARED_PREFIX)) {
                    return sharedDomainToPath(domainToken);
                } else {
                    Log.i(FullBackup.TAG, "Unrecognized domain " + domainToken);
                    return null;
                }
            } catch (Exception e) {
                Log.i(FullBackup.TAG, "Error reading directory for domain: " + domainToken);
                return null;
            }
        }

        private String sharedDomainToPath(String domain) throws IOException {
            String volume = domain.substring(FullBackup.SHARED_PREFIX.length());
            StorageVolume[] volumes = getVolumeList();
            int volNum = Integer.parseInt(volume);
            if (volNum < this.mVolumes.length) {
                return volumes[volNum].getPathFile().getCanonicalPath();
            }
            return null;
        }

        private StorageVolume[] getVolumeList() {
            StorageManager storageManager = this.mStorageManager;
            if (storageManager == null) {
                Log.e(FullBackup.TAG, "Unable to access Storage Manager");
            } else if (this.mVolumes == null) {
                this.mVolumes = storageManager.getVolumeList();
            }
            return this.mVolumes;
        }

        public static class PathWithRequiredFlags {
            private final String mPath;
            private final int mRequiredFlags;

            public PathWithRequiredFlags(String path, int requiredFlags) {
                this.mPath = path;
                this.mRequiredFlags = requiredFlags;
            }

            public String getPath() {
                return this.mPath;
            }

            public int getRequiredFlags() {
                return this.mRequiredFlags;
            }
        }

        BackupScheme(Context context) {
            this.mFullBackupContent = context.getApplicationInfo().fullBackupContent;
            this.mStorageManager = (StorageManager) context.getSystemService("storage");
            this.mPackageManager = context.getPackageManager();
            this.mPackageName = context.getPackageName();
            Context ceContext = context.createCredentialProtectedStorageContext();
            this.FILES_DIR = ceContext.getFilesDir();
            this.DATABASE_DIR = ceContext.getDatabasePath("foo").getParentFile();
            this.ROOT_DIR = ceContext.getDataDir();
            this.SHAREDPREF_DIR = ceContext.getSharedPreferencesPath("foo").getParentFile();
            this.CACHE_DIR = ceContext.getCacheDir();
            this.NOBACKUP_DIR = ceContext.getNoBackupFilesDir();
            Context deContext = context.createDeviceProtectedStorageContext();
            this.DEVICE_FILES_DIR = deContext.getFilesDir();
            this.DEVICE_DATABASE_DIR = deContext.getDatabasePath("foo").getParentFile();
            this.DEVICE_ROOT_DIR = deContext.getDataDir();
            this.DEVICE_SHAREDPREF_DIR = deContext.getSharedPreferencesPath("foo").getParentFile();
            this.DEVICE_CACHE_DIR = deContext.getCacheDir();
            this.DEVICE_NOBACKUP_DIR = deContext.getNoBackupFilesDir();
            if (Process.myUid() != 1000) {
                this.EXTERNAL_DIR = context.getExternalFilesDir(null);
            } else {
                this.EXTERNAL_DIR = null;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isFullBackupContentEnabled() {
            if (this.mFullBackupContent >= 0) {
                return true;
            }
            if (!Log.isLoggable(FullBackup.TAG_XML_PARSER, 2)) {
                return false;
            }
            Log.v(FullBackup.TAG_XML_PARSER, "android:fullBackupContent - \"false\"");
            return false;
        }

        public synchronized Map<String, Set<PathWithRequiredFlags>> maybeParseAndGetCanonicalIncludePaths() throws IOException, XmlPullParserException {
            if (this.mIncludes == null) {
                maybeParseBackupSchemeLocked();
            }
            return this.mIncludes;
        }

        public synchronized ArraySet<PathWithRequiredFlags> maybeParseAndGetCanonicalExcludePaths() throws IOException, XmlPullParserException {
            if (this.mExcludes == null) {
                maybeParseBackupSchemeLocked();
            }
            return this.mExcludes;
        }

        private void maybeParseBackupSchemeLocked() throws IOException, XmlPullParserException {
            this.mIncludes = new ArrayMap();
            this.mExcludes = new ArraySet<>();
            if (this.mFullBackupContent != 0) {
                if (Log.isLoggable(FullBackup.TAG_XML_PARSER, 2)) {
                    Log.v(FullBackup.TAG_XML_PARSER, "android:fullBackupContent - found xml resource");
                }
                XmlResourceParser parser = null;
                try {
                    parser = this.mPackageManager.getResourcesForApplication(this.mPackageName).getXml(this.mFullBackupContent);
                    parseBackupSchemeFromXmlLocked(parser, this.mExcludes, this.mIncludes);
                    if (parser != null) {
                        parser.close();
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    throw new IOException(e);
                } catch (Throwable th) {
                    if (parser != null) {
                        parser.close();
                    }
                    throw th;
                }
            } else if (Log.isLoggable(FullBackup.TAG_XML_PARSER, 2)) {
                Log.v(FullBackup.TAG_XML_PARSER, "android:fullBackupContent - \"true\"");
            }
        }

        @VisibleForTesting
        public void parseBackupSchemeFromXmlLocked(XmlPullParser parser, Set<PathWithRequiredFlags> excludes, Map<String, Set<PathWithRequiredFlags>> includes) throws IOException, XmlPullParserException {
            int i;
            int event;
            BackupScheme backupScheme = this;
            XmlPullParser xmlPullParser = parser;
            int event2 = parser.getEventType();
            while (true) {
                i = 2;
                if (event2 == 2) {
                    break;
                }
                event2 = parser.next();
            }
            if ("full-backup-content".equals(parser.getName())) {
                if (Log.isLoggable(FullBackup.TAG_XML_PARSER, 2)) {
                    Log.v(FullBackup.TAG_XML_PARSER, "\n");
                    Log.v(FullBackup.TAG_XML_PARSER, "====================================================");
                    Log.v(FullBackup.TAG_XML_PARSER, "Found valid fullBackupContent; parsing xml resource.");
                    Log.v(FullBackup.TAG_XML_PARSER, "====================================================");
                    Log.v(FullBackup.TAG_XML_PARSER, "");
                }
                while (true) {
                    int event3 = parser.next();
                    if (event3 == 1) {
                        break;
                    }
                    if (event3 != i) {
                        event = event3;
                    } else {
                        validateInnerTagContents(parser);
                        String domainFromXml = xmlPullParser.getAttributeValue(null, "domain");
                        File domainDirectory = backupScheme.getDirectoryForCriteriaDomain(domainFromXml);
                        if (domainDirectory != null) {
                            File canonicalFile = backupScheme.extractCanonicalFile(domainDirectory, xmlPullParser.getAttributeValue(null, "path"));
                            if (canonicalFile == null) {
                                event = event3;
                            } else {
                                int requiredFlags = 0;
                                if (TAG_INCLUDE.equals(parser.getName())) {
                                    requiredFlags = backupScheme.getRequiredFlagsFromString(xmlPullParser.getAttributeValue(null, "requireFlags"));
                                }
                                Set<PathWithRequiredFlags> activeSet = backupScheme.parseCurrentTagForDomain(xmlPullParser, excludes, includes, domainFromXml);
                                activeSet.add(new PathWithRequiredFlags(canonicalFile.getCanonicalPath(), requiredFlags));
                                if (Log.isLoggable(FullBackup.TAG_XML_PARSER, 2)) {
                                    Log.v(FullBackup.TAG_XML_PARSER, "...parsed " + canonicalFile.getCanonicalPath() + " for domain \"" + domainFromXml + "\", requiredFlags + \"" + requiredFlags + "\"");
                                }
                                if ("database".equals(domainFromXml) && !canonicalFile.isDirectory()) {
                                    String canonicalJournalPath = canonicalFile.getCanonicalPath() + "-journal";
                                    activeSet.add(new PathWithRequiredFlags(canonicalJournalPath, requiredFlags));
                                    if (Log.isLoggable(FullBackup.TAG_XML_PARSER, 2)) {
                                        Log.v(FullBackup.TAG_XML_PARSER, "...automatically generated " + canonicalJournalPath + ". Ignore if nonexistent.");
                                    }
                                    String canonicalWalPath = canonicalFile.getCanonicalPath() + "-wal";
                                    activeSet.add(new PathWithRequiredFlags(canonicalWalPath, requiredFlags));
                                    if (Log.isLoggable(FullBackup.TAG_XML_PARSER, 2)) {
                                        Log.v(FullBackup.TAG_XML_PARSER, "...automatically generated " + canonicalWalPath + ". Ignore if nonexistent.");
                                    }
                                }
                                if (!"sharedpref".equals(domainFromXml) || canonicalFile.isDirectory()) {
                                    event = event3;
                                } else if (!canonicalFile.getCanonicalPath().endsWith(".xml")) {
                                    StringBuilder sb = new StringBuilder();
                                    event = event3;
                                    sb.append(canonicalFile.getCanonicalPath());
                                    sb.append(".xml");
                                    String canonicalXmlPath = sb.toString();
                                    activeSet.add(new PathWithRequiredFlags(canonicalXmlPath, requiredFlags));
                                    if (Log.isLoggable(FullBackup.TAG_XML_PARSER, 2)) {
                                        Log.v(FullBackup.TAG_XML_PARSER, "...automatically generated " + canonicalXmlPath + ". Ignore if nonexistent.");
                                    }
                                } else {
                                    event = event3;
                                }
                            }
                        } else if (Log.isLoggable(FullBackup.TAG_XML_PARSER, i)) {
                            Log.v(FullBackup.TAG_XML_PARSER, "...parsing \"" + parser.getName() + "\": domain=\"" + domainFromXml + "\" invalid; skipping");
                            event = event3;
                        } else {
                            event = event3;
                        }
                    }
                    i = 2;
                    backupScheme = this;
                    xmlPullParser = parser;
                }
                if (Log.isLoggable(FullBackup.TAG_XML_PARSER, 2)) {
                    Log.v(FullBackup.TAG_XML_PARSER, "\n");
                    Log.v(FullBackup.TAG_XML_PARSER, "Xml resource parsing complete.");
                    Log.v(FullBackup.TAG_XML_PARSER, "Final tally.");
                    Log.v(FullBackup.TAG_XML_PARSER, "Includes:");
                    if (includes.isEmpty()) {
                        Log.v(FullBackup.TAG_XML_PARSER, "  ...nothing specified (This means the entirety of app data minus excludes)");
                    } else {
                        for (Map.Entry<String, Set<PathWithRequiredFlags>> entry : includes.entrySet()) {
                            Log.v(FullBackup.TAG_XML_PARSER, "  domain=" + entry.getKey());
                            for (PathWithRequiredFlags includeData : entry.getValue()) {
                                Log.v(FullBackup.TAG_XML_PARSER, " path: " + includeData.getPath() + " requiredFlags: " + includeData.getRequiredFlags());
                            }
                        }
                    }
                    Log.v(FullBackup.TAG_XML_PARSER, "Excludes:");
                    if (excludes.isEmpty()) {
                        Log.v(FullBackup.TAG_XML_PARSER, "  ...nothing to exclude.");
                    } else {
                        for (PathWithRequiredFlags excludeData : excludes) {
                            Log.v(FullBackup.TAG_XML_PARSER, " path: " + excludeData.getPath() + " requiredFlags: " + excludeData.getRequiredFlags());
                        }
                    }
                    Log.v(FullBackup.TAG_XML_PARSER, "  ");
                    Log.v(FullBackup.TAG_XML_PARSER, "====================================================");
                    Log.v(FullBackup.TAG_XML_PARSER, "\n");
                    return;
                }
                return;
            }
            throw new XmlPullParserException("Xml file didn't start with correct tag (<full-backup-content>). Found \"" + parser.getName() + "\"");
        }

        private int getRequiredFlagsFromString(String requiredFlags) {
            if (requiredFlags == null || requiredFlags.length() == 0) {
                return 0;
            }
            String[] flagsStr = requiredFlags.split("\\|");
            int flags = 0;
            for (String f : flagsStr) {
                char c = 65535;
                int hashCode = f.hashCode();
                if (hashCode != 482744282) {
                    if (hashCode != 1499007205) {
                        if (hashCode == 1935925810 && f.equals(FullBackup.FLAG_REQUIRED_DEVICE_TO_DEVICE_TRANSFER)) {
                            c = 1;
                        }
                    } else if (f.equals(FullBackup.FLAG_REQUIRED_CLIENT_SIDE_ENCRYPTION)) {
                        c = 0;
                    }
                } else if (f.equals(FullBackup.FLAG_REQUIRED_FAKE_CLIENT_SIDE_ENCRYPTION)) {
                    c = 2;
                }
                if (c == 0) {
                    flags |= 1;
                } else if (c != 1) {
                    if (c == 2) {
                        flags |= Integer.MIN_VALUE;
                    }
                    Log.w(FullBackup.TAG, "Unrecognized requiredFlag provided, value: \"" + f + "\"");
                } else {
                    flags |= 2;
                }
            }
            return flags;
        }

        private Set<PathWithRequiredFlags> parseCurrentTagForDomain(XmlPullParser parser, Set<PathWithRequiredFlags> excludes, Map<String, Set<PathWithRequiredFlags>> includes, String domain) throws XmlPullParserException {
            if (TAG_INCLUDE.equals(parser.getName())) {
                String domainToken = getTokenForXmlDomain(domain);
                Set<PathWithRequiredFlags> includeSet = includes.get(domainToken);
                if (includeSet != null) {
                    return includeSet;
                }
                Set<PathWithRequiredFlags> includeSet2 = new ArraySet<>();
                includes.put(domainToken, includeSet2);
                return includeSet2;
            } else if (TAG_EXCLUDE.equals(parser.getName())) {
                return excludes;
            } else {
                if (Log.isLoggable(FullBackup.TAG_XML_PARSER, 2)) {
                    Log.v(FullBackup.TAG_XML_PARSER, "Invalid tag found in xml \"" + parser.getName() + "\"; aborting operation.");
                }
                throw new XmlPullParserException("Unrecognised tag in backup criteria xml (" + parser.getName() + ")");
            }
        }

        private String getTokenForXmlDomain(String xmlDomain) {
            if (HwThemeManager.HWT_USER_ROOT.equals(xmlDomain)) {
                return "r";
            }
            if (ContentResolver.SCHEME_FILE.equals(xmlDomain)) {
                return FullBackup.FILES_TREE_TOKEN;
            }
            if ("database".equals(xmlDomain)) {
                return FullBackup.DATABASE_TREE_TOKEN;
            }
            if ("sharedpref".equals(xmlDomain)) {
                return FullBackup.SHAREDPREFS_TREE_TOKEN;
            }
            if ("device_root".equals(xmlDomain)) {
                return FullBackup.DEVICE_ROOT_TREE_TOKEN;
            }
            if ("device_file".equals(xmlDomain)) {
                return FullBackup.DEVICE_FILES_TREE_TOKEN;
            }
            if ("device_database".equals(xmlDomain)) {
                return FullBackup.DEVICE_DATABASE_TREE_TOKEN;
            }
            if ("device_sharedpref".equals(xmlDomain)) {
                return FullBackup.DEVICE_SHAREDPREFS_TREE_TOKEN;
            }
            if (MediaStore.VOLUME_EXTERNAL.equals(xmlDomain)) {
                return FullBackup.MANAGED_EXTERNAL_TREE_TOKEN;
            }
            return null;
        }

        private File extractCanonicalFile(File domain, String filePathFromXml) {
            if (filePathFromXml == null) {
                filePathFromXml = "";
            }
            if (filePathFromXml.contains("..")) {
                if (Log.isLoggable(FullBackup.TAG_XML_PARSER, 2)) {
                    Log.v(FullBackup.TAG_XML_PARSER, "...resolved \"" + domain.getPath() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + filePathFromXml + "\", but the \"..\" path is not permitted; skipping.");
                }
                return null;
            } else if (!filePathFromXml.contains("//")) {
                return new File(domain, filePathFromXml);
            } else {
                if (Log.isLoggable(FullBackup.TAG_XML_PARSER, 2)) {
                    Log.v(FullBackup.TAG_XML_PARSER, "...resolved \"" + domain.getPath() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + filePathFromXml + "\", which contains the invalid \"//\" sequence; skipping.");
                }
                return null;
            }
        }

        private File getDirectoryForCriteriaDomain(String domain) {
            if (TextUtils.isEmpty(domain)) {
                return null;
            }
            if (ContentResolver.SCHEME_FILE.equals(domain)) {
                return this.FILES_DIR;
            }
            if ("database".equals(domain)) {
                return this.DATABASE_DIR;
            }
            if (HwThemeManager.HWT_USER_ROOT.equals(domain)) {
                return this.ROOT_DIR;
            }
            if ("sharedpref".equals(domain)) {
                return this.SHAREDPREF_DIR;
            }
            if ("device_file".equals(domain)) {
                return this.DEVICE_FILES_DIR;
            }
            if ("device_database".equals(domain)) {
                return this.DEVICE_DATABASE_DIR;
            }
            if ("device_root".equals(domain)) {
                return this.DEVICE_ROOT_DIR;
            }
            if ("device_sharedpref".equals(domain)) {
                return this.DEVICE_SHAREDPREF_DIR;
            }
            if (MediaStore.VOLUME_EXTERNAL.equals(domain)) {
                return this.EXTERNAL_DIR;
            }
            return null;
        }

        private void validateInnerTagContents(XmlPullParser parser) throws XmlPullParserException {
            if (parser != null) {
                String name = parser.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -1321148966) {
                    if (hashCode == 1942574248 && name.equals(TAG_INCLUDE)) {
                        c = 0;
                    }
                } else if (name.equals(TAG_EXCLUDE)) {
                    c = 1;
                }
                if (c != 0) {
                    if (c != 1) {
                        throw new XmlPullParserException("A valid tag is one of \"<include/>\" or \"<exclude/>. You provided \"" + parser.getName() + "\"");
                    } else if (parser.getAttributeCount() > 2) {
                        throw new XmlPullParserException("At most 2 tag attributes allowed for \"exclude\" tag (\"domain\" & \"path\".");
                    }
                } else if (parser.getAttributeCount() > 3) {
                    throw new XmlPullParserException("At most 3 tag attributes allowed for \"include\" tag (\"domain\" & \"path\" & optional \"requiredFlags\").");
                }
            }
        }
    }
}
