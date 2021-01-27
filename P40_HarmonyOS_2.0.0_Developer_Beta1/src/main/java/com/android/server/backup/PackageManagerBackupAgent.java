package com.android.server.backup;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.UiModeManagerService;
import com.android.server.backup.utils.AppBackupUtils;
import com.android.server.pm.DumpState;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PackageManagerBackupAgent extends BackupAgent {
    private static final String ANCESTRAL_RECORD_KEY = "@ancestral_record@";
    private static final int ANCESTRAL_RECORD_VERSION = 1;
    private static final boolean DEBUG = false;
    private static final String DEFAULT_HOME_KEY = "@home@";
    private static final String GLOBAL_METADATA_KEY = "@meta@";
    private static final String STATE_FILE_HEADER = "=state=";
    private static final int STATE_FILE_VERSION = 2;
    private static final String TAG = "PMBA";
    private static final int UNDEFINED_ANCESTRAL_RECORD_VERSION = -1;
    private static String mIncrementalVersion = getIncrementalVersion();
    private List<PackageInfo> mAllPackages;
    private final HashSet<String> mExisting = new HashSet<>();
    private boolean mHasMetadata;
    private PackageManager mPackageManager;
    private ComponentName mRestoredHome;
    private String mRestoredHomeInstaller;
    private ArrayList<byte[]> mRestoredHomeSigHashes;
    private long mRestoredHomeVersion;
    private HashMap<String, Metadata> mRestoredSignatures;
    private HashMap<String, Metadata> mStateVersions = new HashMap<>();
    private ComponentName mStoredHomeComponent;
    private ArrayList<byte[]> mStoredHomeSigHashes;
    private long mStoredHomeVersion;
    private String mStoredIncrementalVersion;
    private int mStoredSdkVersion;
    private int mUserId;

    interface RestoreDataConsumer {
        void consumeRestoreData(BackupDataInput backupDataInput) throws IOException;
    }

    public class Metadata {
        public ArrayList<byte[]> sigHashes;
        public long versionCode;

        Metadata(long version, ArrayList<byte[]> hashes) {
            this.versionCode = version;
            this.sigHashes = hashes;
        }
    }

    public PackageManagerBackupAgent(PackageManager packageMgr, List<PackageInfo> packages, int userId) {
        init(packageMgr, packages, userId);
    }

    public PackageManagerBackupAgent(PackageManager packageMgr, int userId) {
        init(packageMgr, null, userId);
        evaluateStorablePackages();
    }

    private void init(PackageManager packageMgr, List<PackageInfo> packages, int userId) {
        this.mPackageManager = packageMgr;
        this.mAllPackages = packages;
        this.mRestoredSignatures = null;
        this.mHasMetadata = false;
        this.mStoredSdkVersion = Build.VERSION.SDK_INT;
        this.mStoredIncrementalVersion = mIncrementalVersion;
        this.mUserId = userId;
    }

    public void evaluateStorablePackages() {
        this.mAllPackages = getStorableApplications(this.mPackageManager, this.mUserId);
    }

    public static List<PackageInfo> getStorableApplications(PackageManager pm, int userId) {
        List<PackageInfo> pkgs = pm.getInstalledPackagesAsUser(DumpState.DUMP_HWFEATURES, userId);
        for (int a = pkgs.size() - 1; a >= 0; a--) {
            if (!AppBackupUtils.appIsEligibleForBackup(pkgs.get(a).applicationInfo, userId)) {
                pkgs.remove(a);
            }
        }
        return pkgs;
    }

    public boolean hasMetadata() {
        return this.mHasMetadata;
    }

    public Metadata getRestoredMetadata(String packageName) {
        HashMap<String, Metadata> hashMap = this.mRestoredSignatures;
        if (hashMap != null) {
            return hashMap.get(packageName);
        }
        Slog.w(TAG, "getRestoredMetadata() before metadata read!");
        return null;
    }

    public Set<String> getRestoredPackages() {
        HashMap<String, Metadata> hashMap = this.mRestoredSignatures;
        if (hashMap != null) {
            return hashMap.keySet();
        }
        Slog.w(TAG, "getRestoredPackages() before metadata read!");
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x00d1 A[SYNTHETIC, Splitter:B:35:0x00d1] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0128  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x013a  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x014b  */
    @Override // android.app.backup.BackupAgent
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
        String homeInstaller;
        PackageInfo homeInfo;
        long homeVersion;
        boolean z;
        boolean needHomeBackup;
        Iterator<PackageInfo> it;
        boolean needHomeBackup2;
        String str;
        Iterator<PackageInfo> it2;
        long homeVersion2;
        String str2 = GLOBAL_METADATA_KEY;
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
        parseStateFile(oldState);
        String str3 = this.mStoredIncrementalVersion;
        if (str3 == null || !str3.equals(mIncrementalVersion)) {
            Slog.i(TAG, "Previous metadata " + this.mStoredIncrementalVersion + " mismatch vs " + mIncrementalVersion + " - rewriting");
            this.mExisting.clear();
        }
        try {
            outputBufferStream.writeInt(1);
            writeEntity(data, ANCESTRAL_RECORD_KEY, outputBuffer.toByteArray());
            ArrayList<byte[]> homeSigHashes = null;
            PackageInfo homeInfo2 = null;
            String homeInstaller2 = null;
            ComponentName home = getPreferredHomeComponent();
            if (home != null) {
                try {
                    homeVersion2 = 0;
                    try {
                        homeInfo2 = this.mPackageManager.getPackageInfoAsUser(home.getPackageName(), DumpState.DUMP_HWFEATURES, this.mUserId);
                        homeInstaller2 = this.mPackageManager.getInstallerPackageName(home.getPackageName());
                        long homeVersion3 = homeInfo2.getLongVersionCode();
                        try {
                            SigningInfo signingInfo = homeInfo2.signingInfo;
                            if (signingInfo == null) {
                                Slog.e(TAG, "Home app has no signing information");
                            } else {
                                homeSigHashes = BackupUtils.hashSignatureArray(signingInfo.getApkContentsSigners());
                            }
                            homeInfo = homeInfo2;
                            homeInstaller = homeInstaller2;
                            homeVersion = homeVersion3;
                        } catch (PackageManager.NameNotFoundException e) {
                            homeVersion2 = homeVersion3;
                            Slog.w(TAG, "Can't access preferred home info");
                            home = null;
                            homeInfo = homeInfo2;
                            homeInstaller = homeInstaller2;
                            homeVersion = homeVersion2;
                            String packName = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                            if (homeVersion == this.mStoredHomeVersion) {
                            }
                            z = true;
                            needHomeBackup = z;
                            if (needHomeBackup) {
                            }
                            outputBuffer.reset();
                            if (!this.mExisting.contains(str2)) {
                            }
                            it = this.mAllPackages.iterator();
                            while (it.hasNext()) {
                            }
                            writeStateFile(this.mAllPackages, home, homeVersion, homeSigHashes, newState);
                        }
                    } catch (PackageManager.NameNotFoundException e2) {
                        Slog.w(TAG, "Can't access preferred home info");
                        home = null;
                        homeInfo = homeInfo2;
                        homeInstaller = homeInstaller2;
                        homeVersion = homeVersion2;
                        String packName2 = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                        if (homeVersion == this.mStoredHomeVersion) {
                        }
                        z = true;
                        needHomeBackup = z;
                        if (needHomeBackup) {
                        }
                        outputBuffer.reset();
                        if (!this.mExisting.contains(str2)) {
                        }
                        it = this.mAllPackages.iterator();
                        while (it.hasNext()) {
                        }
                        writeStateFile(this.mAllPackages, home, homeVersion, homeSigHashes, newState);
                    }
                } catch (PackageManager.NameNotFoundException e3) {
                    homeVersion2 = 0;
                    Slog.w(TAG, "Can't access preferred home info");
                    home = null;
                    homeInfo = homeInfo2;
                    homeInstaller = homeInstaller2;
                    homeVersion = homeVersion2;
                    String packName22 = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                    if (homeVersion == this.mStoredHomeVersion) {
                    }
                    z = true;
                    needHomeBackup = z;
                    if (needHomeBackup) {
                    }
                    outputBuffer.reset();
                    if (!this.mExisting.contains(str2)) {
                    }
                    it = this.mAllPackages.iterator();
                    while (it.hasNext()) {
                    }
                    writeStateFile(this.mAllPackages, home, homeVersion, homeSigHashes, newState);
                }
            } else {
                homeInfo = null;
                homeInstaller = null;
                homeVersion = 0;
            }
            try {
                String packName222 = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                if (homeVersion == this.mStoredHomeVersion) {
                    try {
                        if (Objects.equals(home, this.mStoredHomeComponent) && (home == null || BackupUtils.signaturesMatch(this.mStoredHomeSigHashes, homeInfo, packName222))) {
                            z = false;
                            needHomeBackup = z;
                            if (needHomeBackup) {
                                if (home != null) {
                                    outputBuffer.reset();
                                    outputBufferStream.writeUTF(home.flattenToString());
                                    outputBufferStream.writeLong(homeVersion);
                                    outputBufferStream.writeUTF(homeInstaller != null ? homeInstaller : "");
                                    writeSignatureHashArray(outputBufferStream, homeSigHashes);
                                    writeEntity(data, DEFAULT_HOME_KEY, outputBuffer.toByteArray());
                                } else {
                                    data.writeEntityHeader(DEFAULT_HOME_KEY, -1);
                                }
                            }
                            outputBuffer.reset();
                            if (!this.mExisting.contains(str2)) {
                                outputBufferStream.writeInt(Build.VERSION.SDK_INT);
                                outputBufferStream.writeUTF(mIncrementalVersion);
                                writeEntity(data, str2, outputBuffer.toByteArray());
                            } else {
                                this.mExisting.remove(str2);
                            }
                            it = this.mAllPackages.iterator();
                            while (it.hasNext()) {
                                String packName3 = it.next().packageName;
                                if (packName3.equals(str2)) {
                                    packName222 = packName222;
                                } else {
                                    try {
                                        str = str2;
                                        try {
                                            needHomeBackup2 = needHomeBackup;
                                        } catch (PackageManager.NameNotFoundException e4) {
                                            needHomeBackup2 = needHomeBackup;
                                            this.mExisting.add(packName3);
                                            packName222 = packName222;
                                            str2 = str;
                                            needHomeBackup = needHomeBackup2;
                                            it = it;
                                        }
                                        try {
                                            PackageInfo info = this.mPackageManager.getPackageInfoAsUser(packName3, DumpState.DUMP_HWFEATURES, this.mUserId);
                                            if (this.mExisting.contains(packName3)) {
                                                this.mExisting.remove(packName3);
                                                it2 = it;
                                                if (info.getLongVersionCode() == this.mStateVersions.get(packName3).versionCode) {
                                                    packName222 = packName222;
                                                    str2 = str;
                                                    needHomeBackup = needHomeBackup2;
                                                    it = it2;
                                                }
                                            } else {
                                                it2 = it;
                                            }
                                            SigningInfo signingInfo2 = info.signingInfo;
                                            if (signingInfo2 == null) {
                                                Slog.w(TAG, "Not backing up package " + packName3 + " since it appears to have no signatures.");
                                                packName222 = packName222;
                                                str2 = str;
                                                needHomeBackup = needHomeBackup2;
                                                it = it2;
                                            } else {
                                                outputBuffer.reset();
                                                if (info.versionCodeMajor != 0) {
                                                    outputBufferStream.writeInt(Integer.MIN_VALUE);
                                                    outputBufferStream.writeLong(info.getLongVersionCode());
                                                } else {
                                                    outputBufferStream.writeInt(info.versionCode);
                                                }
                                                writeSignatureHashArray(outputBufferStream, BackupUtils.hashSignatureArray(signingInfo2.getApkContentsSigners()));
                                                writeEntity(data, packName3, outputBuffer.toByteArray());
                                                packName222 = packName222;
                                                str2 = str;
                                                needHomeBackup = needHomeBackup2;
                                                it = it2;
                                            }
                                        } catch (PackageManager.NameNotFoundException e5) {
                                            this.mExisting.add(packName3);
                                            packName222 = packName222;
                                            str2 = str;
                                            needHomeBackup = needHomeBackup2;
                                            it = it;
                                        }
                                    } catch (PackageManager.NameNotFoundException e6) {
                                        str = str2;
                                        needHomeBackup2 = needHomeBackup;
                                        this.mExisting.add(packName3);
                                        packName222 = packName222;
                                        str2 = str;
                                        needHomeBackup = needHomeBackup2;
                                        it = it;
                                    }
                                }
                            }
                            writeStateFile(this.mAllPackages, home, homeVersion, homeSigHashes, newState);
                        }
                    } catch (IOException e7) {
                        Slog.e(TAG, "Unable to write package backup data file!");
                    }
                }
                z = true;
                needHomeBackup = z;
                if (needHomeBackup) {
                }
                outputBuffer.reset();
                if (!this.mExisting.contains(str2)) {
                }
                it = this.mAllPackages.iterator();
                while (it.hasNext()) {
                }
                writeStateFile(this.mAllPackages, home, homeVersion, homeSigHashes, newState);
            } catch (IOException e8) {
                Slog.e(TAG, "Unable to write package backup data file!");
            }
        } catch (IOException e9) {
            Slog.e(TAG, "Unable to write package backup data file!");
        }
    }

    private static void writeEntity(BackupDataOutput data, String key, byte[] bytes) throws IOException {
        data.writeEntityHeader(key, bytes.length);
        data.writeEntityData(bytes, bytes.length);
    }

    @Override // android.app.backup.BackupAgent
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        RestoreDataConsumer consumer = getRestoreDataConsumer(getAncestralRecordVersionValue(data));
        if (consumer == null) {
            Slog.w(TAG, "Ancestral restore set version is unknown to this Android version; not restoring");
        } else {
            consumer.consumeRestoreData(data);
        }
    }

    private int getAncestralRecordVersionValue(BackupDataInput data) throws IOException {
        if (!data.readNextHeader()) {
            return -1;
        }
        String key = data.getKey();
        int dataSize = data.getDataSize();
        if (!ANCESTRAL_RECORD_KEY.equals(key)) {
            return -1;
        }
        byte[] inputBytes = new byte[dataSize];
        data.readEntityData(inputBytes, 0, dataSize);
        return new DataInputStream(new ByteArrayInputStream(inputBytes)).readInt();
    }

    private RestoreDataConsumer getRestoreDataConsumer(int ancestralRecordVersion) {
        if (ancestralRecordVersion == -1) {
            return new LegacyRestoreDataConsumer();
        }
        if (ancestralRecordVersion == 1) {
            return new AncestralVersion1RestoreDataConsumer();
        }
        Slog.e(TAG, "Unrecognized ANCESTRAL_RECORD_VERSION: " + ancestralRecordVersion);
        return null;
    }

    private static void writeSignatureHashArray(DataOutputStream out, ArrayList<byte[]> hashes) throws IOException {
        out.writeInt(hashes.size());
        Iterator<byte[]> it = hashes.iterator();
        while (it.hasNext()) {
            byte[] buffer = it.next();
            out.writeInt(buffer.length);
            out.write(buffer);
        }
    }

    /* access modifiers changed from: private */
    public static ArrayList<byte[]> readSignatureHashArray(DataInputStream in) {
        try {
            int num = in.readInt();
            if (num <= 20) {
                boolean nonHashFound = false;
                try {
                    ArrayList<byte[]> sigs = new ArrayList<>(num);
                    for (int i = 0; i < num; i++) {
                        int len = in.readInt();
                        byte[] readHash = new byte[len];
                        in.read(readHash);
                        sigs.add(readHash);
                        if (len != 32) {
                            nonHashFound = true;
                        }
                    }
                    if (nonHashFound) {
                        return BackupUtils.hashSignatureArray(sigs);
                    }
                    return sigs;
                } catch (IOException e) {
                    Slog.e(TAG, "Unable to read signatures");
                    return null;
                }
            } else {
                Slog.e(TAG, "Suspiciously large sig count in restore data; aborting");
                throw new IllegalStateException("Bad restore state");
            }
        } catch (EOFException e2) {
            Slog.w(TAG, "Read empty signature block");
            return null;
        }
    }

    private void parseStateFile(ParcelFileDescriptor stateFile) {
        long versionCode;
        this.mExisting.clear();
        this.mStateVersions.clear();
        this.mStoredSdkVersion = 0;
        this.mStoredIncrementalVersion = null;
        this.mStoredHomeComponent = null;
        this.mStoredHomeVersion = 0;
        this.mStoredHomeSigHashes = null;
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(stateFile.getFileDescriptor())));
        boolean ignoreExisting = false;
        try {
            String pkg = in.readUTF();
            if (pkg.equals(STATE_FILE_HEADER)) {
                int stateVersion = in.readInt();
                if (stateVersion > 2) {
                    Slog.w(TAG, "Unsupported state file version " + stateVersion + ", redoing from start");
                    return;
                }
                pkg = in.readUTF();
            } else {
                Slog.i(TAG, "Older version of saved state - rewriting");
                ignoreExisting = true;
            }
            if (pkg.equals(DEFAULT_HOME_KEY)) {
                this.mStoredHomeComponent = ComponentName.unflattenFromString(in.readUTF());
                this.mStoredHomeVersion = in.readLong();
                this.mStoredHomeSigHashes = readSignatureHashArray(in);
                pkg = in.readUTF();
            }
            if (pkg.equals(GLOBAL_METADATA_KEY)) {
                this.mStoredSdkVersion = in.readInt();
                this.mStoredIncrementalVersion = in.readUTF();
                if (!ignoreExisting) {
                    this.mExisting.add(GLOBAL_METADATA_KEY);
                }
                while (true) {
                    String pkg2 = in.readUTF();
                    int versionCodeInt = in.readInt();
                    if (versionCodeInt == Integer.MIN_VALUE) {
                        versionCode = in.readLong();
                    } else {
                        versionCode = (long) versionCodeInt;
                    }
                    if (!ignoreExisting) {
                        this.mExisting.add(pkg2);
                    }
                    this.mStateVersions.put(pkg2, new Metadata(versionCode, null));
                }
            } else {
                Slog.e(TAG, "No global metadata in state file!");
            }
        } catch (EOFException e) {
        } catch (IOException e2) {
            Slog.e(TAG, "Unable to read Package Manager state file: " + e2);
        }
    }

    private ComponentName getPreferredHomeComponent() {
        return this.mPackageManager.getHomeActivities(new ArrayList());
    }

    private void writeStateFile(List<PackageInfo> pkgs, ComponentName preferredHome, long homeVersion, ArrayList<byte[]> homeSigHashes, ParcelFileDescriptor stateFile) {
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(stateFile.getFileDescriptor())));
        try {
            out.writeUTF(STATE_FILE_HEADER);
            out.writeInt(2);
            if (preferredHome != null) {
                out.writeUTF(DEFAULT_HOME_KEY);
                out.writeUTF(preferredHome.flattenToString());
                out.writeLong(homeVersion);
                writeSignatureHashArray(out, homeSigHashes);
            }
            out.writeUTF(GLOBAL_METADATA_KEY);
            out.writeInt(Build.VERSION.SDK_INT);
            out.writeUTF(mIncrementalVersion);
            for (PackageInfo pkg : pkgs) {
                out.writeUTF(pkg.packageName);
                if (pkg.versionCodeMajor != 0) {
                    out.writeInt(Integer.MIN_VALUE);
                    out.writeLong(pkg.getLongVersionCode());
                } else {
                    out.writeInt(pkg.versionCode);
                }
            }
            out.flush();
        } catch (IOException e) {
            Slog.e(TAG, "Unable to write package manager state file!");
        }
    }

    /* access modifiers changed from: private */
    public class LegacyRestoreDataConsumer implements RestoreDataConsumer {
        private LegacyRestoreDataConsumer() {
        }

        @Override // com.android.server.backup.PackageManagerBackupAgent.RestoreDataConsumer
        public void consumeRestoreData(BackupDataInput data) throws IOException {
            List<ApplicationInfo> restoredApps;
            long versionCode;
            List<ApplicationInfo> restoredApps2 = new ArrayList<>();
            HashMap<String, Metadata> sigMap = new HashMap<>();
            while (true) {
                String key = data.getKey();
                int dataSize = data.getDataSize();
                byte[] inputBytes = new byte[dataSize];
                data.readEntityData(inputBytes, 0, dataSize);
                DataInputStream inputBufferStream = new DataInputStream(new ByteArrayInputStream(inputBytes));
                if (key.equals(PackageManagerBackupAgent.GLOBAL_METADATA_KEY)) {
                    PackageManagerBackupAgent.this.mStoredSdkVersion = inputBufferStream.readInt();
                    PackageManagerBackupAgent.this.mStoredIncrementalVersion = inputBufferStream.readUTF();
                    PackageManagerBackupAgent.this.mHasMetadata = true;
                    restoredApps = restoredApps2;
                } else if (key.equals(PackageManagerBackupAgent.DEFAULT_HOME_KEY)) {
                    String cn = inputBufferStream.readUTF();
                    PackageManagerBackupAgent.this.mRestoredHome = ComponentName.unflattenFromString(cn);
                    PackageManagerBackupAgent.this.mRestoredHomeVersion = inputBufferStream.readLong();
                    PackageManagerBackupAgent.this.mRestoredHomeInstaller = inputBufferStream.readUTF();
                    PackageManagerBackupAgent.this.mRestoredHomeSigHashes = PackageManagerBackupAgent.readSignatureHashArray(inputBufferStream);
                    restoredApps = restoredApps2;
                } else {
                    int versionCodeInt = inputBufferStream.readInt();
                    if (versionCodeInt == Integer.MIN_VALUE) {
                        versionCode = inputBufferStream.readLong();
                    } else {
                        versionCode = (long) versionCodeInt;
                    }
                    ArrayList<byte[]> sigs = PackageManagerBackupAgent.readSignatureHashArray(inputBufferStream);
                    if (sigs == null || sigs.size() == 0) {
                        Slog.w(PackageManagerBackupAgent.TAG, "Not restoring package " + key + " since it appears to have no signatures.");
                        restoredApps2 = restoredApps2;
                    } else {
                        ApplicationInfo app = new ApplicationInfo();
                        app.packageName = key;
                        restoredApps2.add(app);
                        restoredApps = restoredApps2;
                        sigMap.put(key, new Metadata(versionCode, sigs));
                    }
                }
                if (!data.readNextHeader()) {
                    PackageManagerBackupAgent.this.mRestoredSignatures = sigMap;
                    return;
                }
                restoredApps2 = restoredApps;
            }
        }
    }

    /* access modifiers changed from: private */
    public class AncestralVersion1RestoreDataConsumer implements RestoreDataConsumer {
        private AncestralVersion1RestoreDataConsumer() {
        }

        @Override // com.android.server.backup.PackageManagerBackupAgent.RestoreDataConsumer
        public void consumeRestoreData(BackupDataInput data) throws IOException {
            List<ApplicationInfo> restoredApps;
            long versionCode;
            List<ApplicationInfo> restoredApps2 = new ArrayList<>();
            HashMap<String, Metadata> sigMap = new HashMap<>();
            while (data.readNextHeader()) {
                String key = data.getKey();
                int dataSize = data.getDataSize();
                byte[] inputBytes = new byte[dataSize];
                data.readEntityData(inputBytes, 0, dataSize);
                DataInputStream inputBufferStream = new DataInputStream(new ByteArrayInputStream(inputBytes));
                if (key.equals(PackageManagerBackupAgent.GLOBAL_METADATA_KEY)) {
                    PackageManagerBackupAgent.this.mStoredSdkVersion = inputBufferStream.readInt();
                    PackageManagerBackupAgent.this.mStoredIncrementalVersion = inputBufferStream.readUTF();
                    PackageManagerBackupAgent.this.mHasMetadata = true;
                    restoredApps = restoredApps2;
                } else if (key.equals(PackageManagerBackupAgent.DEFAULT_HOME_KEY)) {
                    String cn = inputBufferStream.readUTF();
                    PackageManagerBackupAgent.this.mRestoredHome = ComponentName.unflattenFromString(cn);
                    PackageManagerBackupAgent.this.mRestoredHomeVersion = inputBufferStream.readLong();
                    PackageManagerBackupAgent.this.mRestoredHomeInstaller = inputBufferStream.readUTF();
                    PackageManagerBackupAgent.this.mRestoredHomeSigHashes = PackageManagerBackupAgent.readSignatureHashArray(inputBufferStream);
                    restoredApps = restoredApps2;
                } else {
                    int versionCodeInt = inputBufferStream.readInt();
                    if (versionCodeInt == Integer.MIN_VALUE) {
                        versionCode = inputBufferStream.readLong();
                    } else {
                        versionCode = (long) versionCodeInt;
                    }
                    ArrayList<byte[]> sigs = PackageManagerBackupAgent.readSignatureHashArray(inputBufferStream);
                    if (sigs == null || sigs.size() == 0) {
                        Slog.w(PackageManagerBackupAgent.TAG, "Not restoring package " + key + " since it appears to have no signatures.");
                        restoredApps2 = restoredApps2;
                    } else {
                        ApplicationInfo app = new ApplicationInfo();
                        app.packageName = key;
                        restoredApps2.add(app);
                        restoredApps = restoredApps2;
                        sigMap.put(key, new Metadata(versionCode, sigs));
                    }
                }
                restoredApps2 = restoredApps;
            }
            PackageManagerBackupAgent.this.mRestoredSignatures = sigMap;
        }
    }

    private static String getIncrementalVersion() {
        String incrementalVersion = getBuildExValue("INCREMENTAL");
        if (TextUtils.isEmpty(incrementalVersion) || UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN.equals(incrementalVersion)) {
            return Build.VERSION.INCREMENTAL;
        }
        return incrementalVersion;
    }

    private static String getBuildExValue(String buildExKey) {
        try {
            Field field = Class.forName("com.huawei.system.BuildEx").getDeclaredField(buildExKey);
            field.setAccessible(true);
            return (String) field.get(null);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
            Slog.e(TAG, "getBuildExValue exception: " + e.getMessage());
            return "";
        } catch (Exception e2) {
            Slog.e(TAG, "getBuildExValue exception");
            return "";
        }
    }
}
