package com.android.server.backup;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION;
import android.os.ParcelFileDescriptor;
import android.util.Slog;
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
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PackageManagerBackupAgent extends BackupAgent {
    private static final int ANCESTRAL_RECORD_VERSION = 1;
    private static final boolean DEBUG = false;
    private static final String DEFAULT_HOME_KEY = "@home@";
    private static final String GLOBAL_METADATA_KEY = "@meta@";
    private static final String STATE_FILE_HEADER = "=state=";
    private static final int STATE_FILE_VERSION = 2;
    private static final String TAG = "PMBA";
    private List<PackageInfo> mAllPackages;
    private final HashSet<String> mExisting = new HashSet();
    private boolean mHasMetadata;
    private PackageManager mPackageManager;
    private ComponentName mRestoredHome;
    private String mRestoredHomeInstaller;
    private ArrayList<byte[]> mRestoredHomeSigHashes;
    private long mRestoredHomeVersion;
    private HashMap<String, Metadata> mRestoredSignatures;
    private HashMap<String, Metadata> mStateVersions = new HashMap();
    private ComponentName mStoredHomeComponent;
    private ArrayList<byte[]> mStoredHomeSigHashes;
    private long mStoredHomeVersion;
    private String mStoredIncrementalVersion;
    private int mStoredSdkVersion;

    public class Metadata {
        public ArrayList<byte[]> sigHashes;
        public int versionCode;

        Metadata(int version, ArrayList<byte[]> hashes) {
            this.versionCode = version;
            this.sigHashes = hashes;
        }
    }

    PackageManagerBackupAgent(PackageManager packageMgr, List<PackageInfo> packages) {
        init(packageMgr, packages);
    }

    PackageManagerBackupAgent(PackageManager packageMgr) {
        init(packageMgr, null);
        evaluateStorablePackages();
    }

    private void init(PackageManager packageMgr, List<PackageInfo> packages) {
        this.mPackageManager = packageMgr;
        this.mAllPackages = packages;
        this.mRestoredSignatures = null;
        this.mHasMetadata = false;
        this.mStoredSdkVersion = VERSION.SDK_INT;
        this.mStoredIncrementalVersion = VERSION.INCREMENTAL;
    }

    public void evaluateStorablePackages() {
        this.mAllPackages = getStorableApplications(this.mPackageManager);
    }

    public static List<PackageInfo> getStorableApplications(PackageManager pm) {
        List<PackageInfo> pkgs = pm.getInstalledPackages(64);
        for (int a = pkgs.size() - 1; a >= 0; a--) {
            if (!BackupManagerService.appIsEligibleForBackup(((PackageInfo) pkgs.get(a)).applicationInfo, pm)) {
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
            return (Metadata) this.mRestoredSignatures.get(packageName);
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
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputBuffer);
        parseStateFile(oldState);
        if (this.mStoredIncrementalVersion == null || (this.mStoredIncrementalVersion.equals(VERSION.INCREMENTAL) ^ 1) != 0) {
            Slog.i(TAG, "Previous metadata " + this.mStoredIncrementalVersion + " mismatch vs " + VERSION.INCREMENTAL + " - rewriting");
            this.mExisting.clear();
        }
        long homeVersion = 0;
        ArrayList homeSigHashes = null;
        PackageInfo packageInfo = null;
        String str = null;
        ComponentName home = getPreferredHomeComponent();
        if (home != null) {
            try {
                packageInfo = this.mPackageManager.getPackageInfo(home.getPackageName(), 64);
                str = this.mPackageManager.getInstallerPackageName(home.getPackageName());
                homeVersion = (long) packageInfo.versionCode;
                homeSigHashes = BackupUtils.hashSignatureArray(packageInfo.signatures);
            } catch (NameNotFoundException e) {
                Slog.w(TAG, "Can't access preferred home info");
                home = null;
            }
        }
        try {
            int needHomeBackup;
            if (homeVersion != this.mStoredHomeVersion || (Objects.equals(home, this.mStoredHomeComponent) ^ 1) != 0) {
                needHomeBackup = 1;
            } else if (home != null) {
                needHomeBackup = BackupUtils.signaturesMatch(this.mStoredHomeSigHashes, packageInfo) ^ 1;
            } else {
                needHomeBackup = 0;
            }
            if (needHomeBackup != 0) {
                if (home != null) {
                    dataOutputStream.writeUTF(home.flattenToString());
                    dataOutputStream.writeLong(homeVersion);
                    if (str == null) {
                        str = "";
                    }
                    dataOutputStream.writeUTF(str);
                    writeSignatureHashArray(dataOutputStream, homeSigHashes);
                    writeEntity(data, DEFAULT_HOME_KEY, outputBuffer.toByteArray());
                } else {
                    data.writeEntityHeader(DEFAULT_HOME_KEY, -1);
                }
            }
            outputBuffer.reset();
            if (this.mExisting.contains(GLOBAL_METADATA_KEY)) {
                this.mExisting.remove(GLOBAL_METADATA_KEY);
            } else {
                dataOutputStream.writeInt(VERSION.SDK_INT);
                dataOutputStream.writeUTF(VERSION.INCREMENTAL);
                writeEntity(data, GLOBAL_METADATA_KEY, outputBuffer.toByteArray());
            }
            for (PackageInfo pkg : this.mAllPackages) {
                String packName = pkg.packageName;
                if (!packName.equals(GLOBAL_METADATA_KEY)) {
                    try {
                        PackageInfo info = this.mPackageManager.getPackageInfo(packName, 64);
                        if (this.mExisting.contains(packName)) {
                            this.mExisting.remove(packName);
                            if (info.versionCode == ((Metadata) this.mStateVersions.get(packName)).versionCode) {
                            }
                        }
                        if (info.signatures == null || info.signatures.length == 0) {
                            Slog.w(TAG, "Not backing up package " + packName + " since it appears to have no signatures.");
                        } else {
                            outputBuffer.reset();
                            dataOutputStream.writeInt(info.versionCode);
                            writeSignatureHashArray(dataOutputStream, BackupUtils.hashSignatureArray(info.signatures));
                            writeEntity(data, packName, outputBuffer.toByteArray());
                        }
                    } catch (NameNotFoundException e2) {
                        this.mExisting.add(packName);
                    }
                }
            }
            writeStateFile(this.mAllPackages, home, homeVersion, homeSigHashes, newState);
        } catch (IOException e3) {
            Slog.e(TAG, "Unable to write package backup data file!");
        }
    }

    private static void writeEntity(BackupDataOutput data, String key, byte[] bytes) throws IOException {
        data.writeEntityHeader(key, bytes.length);
        data.writeEntityData(bytes, bytes.length);
    }

    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        List<ApplicationInfo> restoredApps = new ArrayList();
        HashMap<String, Metadata> sigMap = new HashMap();
        while (data.readNextHeader()) {
            String key = data.getKey();
            int dataSize = data.getDataSize();
            byte[] inputBytes = new byte[dataSize];
            data.readEntityData(inputBytes, 0, dataSize);
            DataInputStream inputBufferStream = new DataInputStream(new ByteArrayInputStream(inputBytes));
            if (key.equals(GLOBAL_METADATA_KEY)) {
                int storedSdkVersion = inputBufferStream.readInt();
                if (-1 > VERSION.SDK_INT) {
                    Slog.w(TAG, "Restore set was from a later version of Android; not restoring");
                    return;
                }
                this.mStoredSdkVersion = storedSdkVersion;
                this.mStoredIncrementalVersion = inputBufferStream.readUTF();
                this.mHasMetadata = true;
            } else if (key.equals(DEFAULT_HOME_KEY)) {
                this.mRestoredHome = ComponentName.unflattenFromString(inputBufferStream.readUTF());
                this.mRestoredHomeVersion = inputBufferStream.readLong();
                this.mRestoredHomeInstaller = inputBufferStream.readUTF();
                this.mRestoredHomeSigHashes = readSignatureHashArray(inputBufferStream);
            } else {
                int versionCode = inputBufferStream.readInt();
                ArrayList<byte[]> sigs = readSignatureHashArray(inputBufferStream);
                if (sigs == null || sigs.size() == 0) {
                    Slog.w(TAG, "Not restoring package " + key + " since it appears to have no signatures.");
                } else {
                    ApplicationInfo app = new ApplicationInfo();
                    app.packageName = key;
                    restoredApps.add(app);
                    sigMap.put(key, new Metadata(versionCode, sigs));
                }
            }
        }
        this.mRestoredSignatures = sigMap;
    }

    private static void writeSignatureHashArray(DataOutputStream out, ArrayList<byte[]> hashes) throws IOException {
        out.writeInt(hashes.size());
        for (byte[] buffer : hashes) {
            out.writeInt(buffer.length);
            out.write(buffer);
        }
    }

    private static ArrayList<byte[]> readSignatureHashArray(DataInputStream in) {
        try {
            int num = in.readInt();
            if (num > 20) {
                try {
                    Slog.e(TAG, "Suspiciously large sig count in restore data; aborting");
                    throw new IllegalStateException("Bad restore state");
                } catch (IOException e) {
                    Slog.e(TAG, "Unable to read signatures");
                    return null;
                }
            }
            boolean nonHashFound = false;
            ArrayList<byte[]> sigs = new ArrayList(num);
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
                sigs = BackupUtils.hashSignatureArray((List) sigs);
            }
            return sigs;
        } catch (EOFException e2) {
            Slog.w(TAG, "Read empty signature block");
            return null;
        }
    }

    private void parseStateFile(ParcelFileDescriptor stateFile) {
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
                    pkg = in.readUTF();
                    int versionCode = in.readInt();
                    if (!ignoreExisting) {
                        this.mExisting.add(pkg);
                    }
                    this.mStateVersions.put(pkg, new Metadata(versionCode, null));
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
            out.writeInt(VERSION.SDK_INT);
            out.writeUTF(VERSION.INCREMENTAL);
            for (PackageInfo pkg : pkgs) {
                out.writeUTF(pkg.packageName);
                out.writeInt(pkg.versionCode);
            }
            out.flush();
        } catch (IOException e) {
            Slog.e(TAG, "Unable to write package manager state file!");
        }
    }
}
