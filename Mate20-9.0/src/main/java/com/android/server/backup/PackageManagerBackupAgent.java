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
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.backup.utils.AppBackupUtils;
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
    private List<PackageInfo> mAllPackages;
    private final HashSet<String> mExisting = new HashSet<>();
    /* access modifiers changed from: private */
    public boolean mHasMetadata;
    private PackageManager mPackageManager;
    /* access modifiers changed from: private */
    public ComponentName mRestoredHome;
    /* access modifiers changed from: private */
    public String mRestoredHomeInstaller;
    /* access modifiers changed from: private */
    public ArrayList<byte[]> mRestoredHomeSigHashes;
    /* access modifiers changed from: private */
    public long mRestoredHomeVersion;
    /* access modifiers changed from: private */
    public HashMap<String, Metadata> mRestoredSignatures;
    private HashMap<String, Metadata> mStateVersions = new HashMap<>();
    private ComponentName mStoredHomeComponent;
    private ArrayList<byte[]> mStoredHomeSigHashes;
    private long mStoredHomeVersion;
    /* access modifiers changed from: private */
    public String mStoredIncrementalVersion;
    /* access modifiers changed from: private */
    public int mStoredSdkVersion;

    private class AncestralVersion1RestoreDataConsumer implements RestoreDataConsumer {
        private AncestralVersion1RestoreDataConsumer() {
        }

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
                    int unused = PackageManagerBackupAgent.this.mStoredSdkVersion = inputBufferStream.readInt();
                    String unused2 = PackageManagerBackupAgent.this.mStoredIncrementalVersion = inputBufferStream.readUTF();
                    boolean unused3 = PackageManagerBackupAgent.this.mHasMetadata = true;
                } else if (key.equals(PackageManagerBackupAgent.DEFAULT_HOME_KEY)) {
                    ComponentName unused4 = PackageManagerBackupAgent.this.mRestoredHome = ComponentName.unflattenFromString(inputBufferStream.readUTF());
                    long unused5 = PackageManagerBackupAgent.this.mRestoredHomeVersion = inputBufferStream.readLong();
                    String unused6 = PackageManagerBackupAgent.this.mRestoredHomeInstaller = inputBufferStream.readUTF();
                    ArrayList unused7 = PackageManagerBackupAgent.this.mRestoredHomeSigHashes = PackageManagerBackupAgent.readSignatureHashArray(inputBufferStream);
                } else {
                    int versionCodeInt = inputBufferStream.readInt();
                    if (versionCodeInt == Integer.MIN_VALUE) {
                        versionCode = inputBufferStream.readLong();
                    } else {
                        versionCode = (long) versionCodeInt;
                    }
                    ArrayList<byte[]> sigs = PackageManagerBackupAgent.readSignatureHashArray(inputBufferStream);
                    if (sigs == null) {
                        restoredApps = restoredApps2;
                    } else if (sigs.size() == 0) {
                        restoredApps = restoredApps2;
                    } else {
                        ApplicationInfo app = new ApplicationInfo();
                        app.packageName = key;
                        restoredApps2.add(app);
                        restoredApps = restoredApps2;
                        sigMap.put(key, new Metadata(versionCode, sigs));
                        restoredApps2 = restoredApps;
                    }
                    Slog.w(PackageManagerBackupAgent.TAG, "Not restoring package " + key + " since it appears to have no signatures.");
                    restoredApps2 = restoredApps;
                }
                restoredApps = restoredApps2;
                restoredApps2 = restoredApps;
            }
            BackupDataInput backupDataInput = data;
            List<ApplicationInfo> list = restoredApps2;
            HashMap unused8 = PackageManagerBackupAgent.this.mRestoredSignatures = sigMap;
        }
    }

    private class LegacyRestoreDataConsumer implements RestoreDataConsumer {
        private LegacyRestoreDataConsumer() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:25:0x00b5 A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x00db A[SYNTHETIC] */
        public void consumeRestoreData(BackupDataInput data) throws IOException {
            List<ApplicationInfo> restoredApps;
            long versionCode;
            List<ApplicationInfo> restoredApps2;
            List<ApplicationInfo> restoredApps3 = new ArrayList<>();
            HashMap<String, Metadata> sigMap = new HashMap<>();
            while (true) {
                String key = data.getKey();
                int dataSize = data.getDataSize();
                byte[] inputBytes = new byte[dataSize];
                data.readEntityData(inputBytes, 0, dataSize);
                DataInputStream inputBufferStream = new DataInputStream(new ByteArrayInputStream(inputBytes));
                if (key.equals(PackageManagerBackupAgent.GLOBAL_METADATA_KEY)) {
                    int unused = PackageManagerBackupAgent.this.mStoredSdkVersion = inputBufferStream.readInt();
                    String unused2 = PackageManagerBackupAgent.this.mStoredIncrementalVersion = inputBufferStream.readUTF();
                    boolean unused3 = PackageManagerBackupAgent.this.mHasMetadata = true;
                } else if (key.equals(PackageManagerBackupAgent.DEFAULT_HOME_KEY)) {
                    ComponentName unused4 = PackageManagerBackupAgent.this.mRestoredHome = ComponentName.unflattenFromString(inputBufferStream.readUTF());
                    long unused5 = PackageManagerBackupAgent.this.mRestoredHomeVersion = inputBufferStream.readLong();
                    String unused6 = PackageManagerBackupAgent.this.mRestoredHomeInstaller = inputBufferStream.readUTF();
                    ArrayList unused7 = PackageManagerBackupAgent.this.mRestoredHomeSigHashes = PackageManagerBackupAgent.readSignatureHashArray(inputBufferStream);
                } else {
                    int versionCodeInt = inputBufferStream.readInt();
                    if (versionCodeInt == Integer.MIN_VALUE) {
                        versionCode = inputBufferStream.readLong();
                    } else {
                        versionCode = (long) versionCodeInt;
                    }
                    ArrayList<byte[]> sigs = PackageManagerBackupAgent.readSignatureHashArray(inputBufferStream);
                    if (sigs == null) {
                        restoredApps2 = restoredApps3;
                    } else if (sigs.size() == 0) {
                        restoredApps2 = restoredApps3;
                    } else {
                        ApplicationInfo app = new ApplicationInfo();
                        app.packageName = key;
                        restoredApps3.add(app);
                        restoredApps = restoredApps3;
                        sigMap.put(key, new Metadata(versionCode, sigs));
                        if (data.readNextHeader()) {
                            HashMap unused8 = PackageManagerBackupAgent.this.mRestoredSignatures = sigMap;
                            return;
                        }
                        restoredApps3 = restoredApps;
                    }
                    Slog.w(PackageManagerBackupAgent.TAG, "Not restoring package " + key + " since it appears to have no signatures.");
                    restoredApps3 = restoredApps;
                }
                restoredApps = restoredApps3;
                if (data.readNextHeader()) {
                }
            }
        }
    }

    public class Metadata {
        public ArrayList<byte[]> sigHashes;
        public long versionCode;

        Metadata(long version, ArrayList<byte[]> hashes) {
            this.versionCode = version;
            this.sigHashes = hashes;
        }
    }

    interface RestoreDataConsumer {
        void consumeRestoreData(BackupDataInput backupDataInput) throws IOException;
    }

    public PackageManagerBackupAgent(PackageManager packageMgr, List<PackageInfo> packages) {
        init(packageMgr, packages);
    }

    public PackageManagerBackupAgent(PackageManager packageMgr) {
        init(packageMgr, null);
        evaluateStorablePackages();
    }

    private void init(PackageManager packageMgr, List<PackageInfo> packages) {
        this.mPackageManager = packageMgr;
        this.mAllPackages = packages;
        this.mRestoredSignatures = null;
        this.mHasMetadata = false;
        this.mStoredSdkVersion = Build.VERSION.SDK_INT;
        this.mStoredIncrementalVersion = Build.VERSION.INCREMENTAL;
    }

    public void evaluateStorablePackages() {
        this.mAllPackages = getStorableApplications(this.mPackageManager);
    }

    public static List<PackageInfo> getStorableApplications(PackageManager pm) {
        List<PackageInfo> pkgs = pm.getInstalledPackages(134217728);
        for (int a = pkgs.size() - 1; a >= 0; a--) {
            if (!AppBackupUtils.appIsEligibleForBackup(pkgs.get(a).applicationInfo, pm)) {
                pkgs.remove(a);
            }
        }
        return pkgs;
    }

    public boolean hasMetadata() {
        return this.mHasMetadata;
    }

    public Metadata getRestoredMetadata(String packageName) {
        if (this.mRestoredSignatures != null) {
            return this.mRestoredSignatures.get(packageName);
        }
        Slog.w(TAG, "getRestoredMetadata() before metadata read!");
        return null;
    }

    public Set<String> getRestoredPackages() {
        if (this.mRestoredSignatures != null) {
            return this.mRestoredSignatures.keySet();
        }
        Slog.w(TAG, "getRestoredPackages() before metadata read!");
        return null;
    }

    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
        String homeInstaller;
        ComponentName home;
        long homeVersion;
        PackageInfo homeInfo;
        ArrayList<byte[]> homeSigHashes;
        boolean needHomeBackup;
        Iterator<PackageInfo> it;
        boolean needHomeBackup2;
        PackageManagerInternal pmi;
        BackupDataOutput backupDataOutput = data;
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
        parseStateFile(oldState);
        if (this.mStoredIncrementalVersion == null || !this.mStoredIncrementalVersion.equals(Build.VERSION.INCREMENTAL)) {
            Slog.i(TAG, "Previous metadata " + this.mStoredIncrementalVersion + " mismatch vs " + Build.VERSION.INCREMENTAL + " - rewriting");
            this.mExisting.clear();
        }
        boolean needHomeBackup3 = true;
        try {
            outputBufferStream.writeInt(1);
            writeEntity(backupDataOutput, ANCESTRAL_RECORD_KEY, outputBuffer.toByteArray());
            long homeVersion2 = 0;
            ArrayList<byte[]> homeSigHashes2 = null;
            PackageInfo homeInfo2 = null;
            String homeInstaller2 = null;
            ComponentName home2 = getPreferredHomeComponent();
            int i = 134217728;
            if (home2 != null) {
                try {
                    homeInfo2 = this.mPackageManager.getPackageInfo(home2.getPackageName(), 134217728);
                    homeInstaller2 = this.mPackageManager.getInstallerPackageName(home2.getPackageName());
                    homeVersion2 = homeInfo2.getLongVersionCode();
                    SigningInfo signingInfo = homeInfo2.signingInfo;
                    if (signingInfo == null) {
                        Slog.e(TAG, "Home app has no signing information");
                    } else {
                        homeSigHashes2 = BackupUtils.hashSignatureArray(signingInfo.getApkContentsSigners());
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.w(TAG, "Can't access preferred home info");
                    home = null;
                    homeVersion = 0;
                    homeInfo = null;
                    homeInstaller = null;
                    homeSigHashes = null;
                }
            }
            homeVersion = homeVersion2;
            homeInstaller = homeInstaller2;
            home = home2;
            homeSigHashes = homeSigHashes2;
            homeInfo = homeInfo2;
            try {
                PackageManagerInternal pmi2 = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                if (homeVersion == this.mStoredHomeVersion) {
                    try {
                        if (Objects.equals(home, this.mStoredHomeComponent)) {
                            if (home == null || BackupUtils.signaturesMatch(this.mStoredHomeSigHashes, homeInfo, pmi2)) {
                                needHomeBackup3 = false;
                            }
                        }
                    } catch (IOException e2) {
                        ArrayList<byte[]> arrayList = homeSigHashes;
                        PackageInfo packageInfo = homeInfo;
                    }
                }
                if (needHomeBackup) {
                    if (home != null) {
                        outputBuffer.reset();
                        outputBufferStream.writeUTF(home.flattenToString());
                        outputBufferStream.writeLong(homeVersion);
                        outputBufferStream.writeUTF(homeInstaller != null ? homeInstaller : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                        writeSignatureHashArray(outputBufferStream, homeSigHashes);
                        writeEntity(backupDataOutput, DEFAULT_HOME_KEY, outputBuffer.toByteArray());
                    } else {
                        backupDataOutput.writeEntityHeader(DEFAULT_HOME_KEY, -1);
                    }
                }
                outputBuffer.reset();
                if (!this.mExisting.contains(GLOBAL_METADATA_KEY)) {
                    outputBufferStream.writeInt(Build.VERSION.SDK_INT);
                    outputBufferStream.writeUTF(Build.VERSION.INCREMENTAL);
                    writeEntity(backupDataOutput, GLOBAL_METADATA_KEY, outputBuffer.toByteArray());
                } else {
                    this.mExisting.remove(GLOBAL_METADATA_KEY);
                }
                Iterator<PackageInfo> it2 = this.mAllPackages.iterator();
                while (it2.hasNext()) {
                    String packName = it2.next().packageName;
                    if (!packName.equals(GLOBAL_METADATA_KEY)) {
                        try {
                            PackageInfo info = this.mPackageManager.getPackageInfo(packName, i);
                            if (this.mExisting.contains(packName)) {
                                this.mExisting.remove(packName);
                                needHomeBackup2 = needHomeBackup;
                                pmi = pmi2;
                                if (info.getLongVersionCode() == this.mStateVersions.get(packName).versionCode) {
                                    pmi2 = pmi;
                                    needHomeBackup = needHomeBackup2;
                                    i = 134217728;
                                }
                            } else {
                                needHomeBackup2 = needHomeBackup;
                                pmi = pmi2;
                            }
                            SigningInfo signingInfo2 = info.signingInfo;
                            if (signingInfo2 == null) {
                                StringBuilder sb = new StringBuilder();
                                it = it2;
                                sb.append("Not backing up package ");
                                sb.append(packName);
                                sb.append(" since it appears to have no signatures.");
                                Slog.w(TAG, sb.toString());
                            } else {
                                it = it2;
                                outputBuffer.reset();
                                if (info.versionCodeMajor != 0) {
                                    outputBufferStream.writeInt(Integer.MIN_VALUE);
                                    outputBufferStream.writeLong(info.getLongVersionCode());
                                } else {
                                    outputBufferStream.writeInt(info.versionCode);
                                }
                                writeSignatureHashArray(outputBufferStream, BackupUtils.hashSignatureArray(signingInfo2.getApkContentsSigners()));
                                writeEntity(backupDataOutput, packName, outputBuffer.toByteArray());
                            }
                        } catch (PackageManager.NameNotFoundException e3) {
                            needHomeBackup2 = needHomeBackup;
                            pmi = pmi2;
                            it = it2;
                            this.mExisting.add(packName);
                        }
                        pmi2 = pmi;
                        needHomeBackup = needHomeBackup2;
                        it2 = it;
                        i = 134217728;
                    }
                }
                ArrayList<byte[]> arrayList2 = homeSigHashes;
                PackageInfo packageInfo2 = homeInfo;
                writeStateFile(this.mAllPackages, home, homeVersion, homeSigHashes, newState);
            } catch (IOException e4) {
                ArrayList<byte[]> arrayList3 = homeSigHashes;
                PackageInfo packageInfo3 = homeInfo;
                Slog.e(TAG, "Unable to write package backup data file!");
            }
        } catch (IOException e5) {
            Slog.e(TAG, "Unable to write package backup data file!");
        }
    }

    private static void writeEntity(BackupDataOutput data, String key, byte[] bytes) throws IOException {
        data.writeEntityHeader(key, bytes.length);
        data.writeEntityData(bytes, bytes.length);
    }

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
                        sigs = BackupUtils.hashSignatureArray((List<byte[]>) sigs);
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
            out.writeUTF(Build.VERSION.INCREMENTAL);
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
}
