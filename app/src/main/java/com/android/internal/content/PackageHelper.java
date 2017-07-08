package com.android.internal.content;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser.PackageLite;
import android.os.Environment;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.storage.IMountService.Stub;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.provider.Settings.Global;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.content.NativeLibraryHelper.Handle;
import com.android.internal.util.Protocol;
import com.huawei.hwperformance.HwPerformance;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.microedition.khronos.opengles.GL10;
import libcore.io.IoUtils;

public class PackageHelper {
    public static final int APP_INSTALL_AUTO = 0;
    public static final int APP_INSTALL_EXTERNAL = 2;
    public static final int APP_INSTALL_INTERNAL = 1;
    public static final int RECOMMEND_FAILED_ALREADY_EXISTS = -4;
    public static final int RECOMMEND_FAILED_INSUFFICIENT_STORAGE = -1;
    public static final int RECOMMEND_FAILED_INVALID_APK = -2;
    public static final int RECOMMEND_FAILED_INVALID_LOCATION = -3;
    public static final int RECOMMEND_FAILED_INVALID_URI = -6;
    public static final int RECOMMEND_FAILED_VERSION_DOWNGRADE = -7;
    public static final int RECOMMEND_INSTALL_EPHEMERAL = 3;
    public static final int RECOMMEND_INSTALL_EXTERNAL = 2;
    public static final int RECOMMEND_INSTALL_INTERNAL = 1;
    public static final int RECOMMEND_MEDIA_UNAVAILABLE = -5;
    private static final String TAG = "PackageHelper";
    private static final boolean localLOGV = false;
    private static HwCustPackageHelper mCustPkgHelper;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.content.PackageHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.content.PackageHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.content.PackageHelper.<clinit>():void");
    }

    public static IMountService getMountService() throws RemoteException {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return Stub.asInterface(service);
        }
        Log.e(TAG, "Can't get mount service");
        throw new RemoteException("Could not contact mount service");
    }

    public static String createSdDir(long sizeBytes, String cid, String sdEncKey, int uid, boolean isExternal) {
        int sizeMb = ((int) ((1048576 + sizeBytes) / 1048576)) + RECOMMEND_INSTALL_INTERNAL;
        try {
            IMountService mountService = getMountService();
            if (mountService.createSecureContainer(cid, sizeMb, "ext4", sdEncKey, uid, isExternal) == 0) {
                return mountService.getSecureContainerPath(cid);
            }
            Log.e(TAG, "Failed to create secure container " + cid);
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "MountService running?");
            return null;
        }
    }

    public static boolean resizeSdDir(long sizeBytes, String cid, String sdEncKey) {
        try {
            if (getMountService().resizeSecureContainer(cid, ((int) ((sizeBytes + 1048576) / 1048576)) + RECOMMEND_INSTALL_INTERNAL, sdEncKey) == 0) {
                return true;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "MountService running?");
        }
        Log.e(TAG, "Failed to create secure container " + cid);
        return false;
    }

    public static String mountSdDir(String cid, String key, int ownerUid) {
        return mountSdDir(cid, key, ownerUid, true);
    }

    public static String mountSdDir(String cid, String key, int ownerUid, boolean readOnly) {
        try {
            int rc = getMountService().mountSecureContainer(cid, key, ownerUid, readOnly);
            if (rc == 0) {
                return getMountService().getSecureContainerPath(cid);
            }
            Log.i(TAG, "Failed to mount container " + cid + " rc : " + rc);
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "MountService running?");
            return null;
        }
    }

    public static boolean unMountSdDir(String cid) {
        try {
            int rc = getMountService().unmountSecureContainer(cid, true);
            if (rc == 0) {
                return true;
            }
            Log.e(TAG, "Failed to unmount " + cid + " with rc " + rc);
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "MountService running?");
            return false;
        }
    }

    public static boolean renameSdDir(String oldId, String newId) {
        try {
            int rc = getMountService().renameSecureContainer(oldId, newId);
            if (rc == 0) {
                return true;
            }
            Log.e(TAG, "Failed to rename " + oldId + " to " + newId + "with rc " + rc);
            return false;
        } catch (RemoteException e) {
            Log.i(TAG, "Failed ot rename  " + oldId + " to " + newId + " with exception : " + e);
            return false;
        }
    }

    public static String getSdDir(String cid) {
        try {
            return getMountService().getSecureContainerPath(cid);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get container path for " + cid + " with exception " + e);
            return null;
        }
    }

    public static String getSdFilesystem(String cid) {
        try {
            return getMountService().getSecureContainerFilesystemPath(cid);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get container path for " + cid + " with exception " + e);
            return null;
        }
    }

    public static boolean finalizeSdDir(String cid) {
        try {
            if (getMountService().finalizeSecureContainer(cid) == 0) {
                return true;
            }
            Log.i(TAG, "Failed to finalize container " + cid);
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to finalize container " + cid + " with exception " + e);
            return false;
        }
    }

    public static boolean destroySdDir(String cid) {
        try {
            if (getMountService().destroySecureContainer(cid, true) == 0) {
                return true;
            }
            Log.i(TAG, "Failed to destroy container " + cid);
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to destroy container " + cid + " with exception " + e);
            return false;
        }
    }

    public static String[] getSecureContainerList() {
        try {
            return getMountService().getSecureContainerList();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get secure container list with exception" + e);
            return null;
        }
    }

    public static boolean isContainerMounted(String cid) {
        try {
            return getMountService().isSecureContainerMounted(cid);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to find out if container " + cid + " mounted");
            return false;
        }
    }

    public static long extractPublicFiles(File apkFile, File publicZipFile) throws IOException {
        FileOutputStream fstr;
        AutoCloseable autoCloseable;
        if (publicZipFile == null) {
            fstr = null;
            autoCloseable = null;
        } else {
            fstr = new FileOutputStream(publicZipFile);
            autoCloseable = new ZipOutputStream(fstr);
            Log.d(TAG, "Extracting " + apkFile + " to " + publicZipFile);
        }
        long size = 0;
        ZipFile privateZip;
        try {
            privateZip = new ZipFile(apkFile.getAbsolutePath());
            for (ZipEntry zipEntry : Collections.list(privateZip.entries())) {
                String zipEntryName = zipEntry.getName();
                if ("AndroidManifest.xml".equals(zipEntryName) || "resources.arsc".equals(zipEntryName) || zipEntryName.startsWith("res/")) {
                    size += zipEntry.getSize();
                    if (publicZipFile != null) {
                        copyZipEntry(zipEntry, privateZip, autoCloseable);
                    }
                }
            }
            try {
                privateZip.close();
            } catch (IOException e) {
            }
            if (publicZipFile != null) {
                autoCloseable.finish();
                autoCloseable.flush();
                FileUtils.sync(fstr);
                autoCloseable.close();
                FileUtils.setPermissions(publicZipFile.getAbsolutePath(), DisplayMetrics.DENSITY_420, RECOMMEND_FAILED_INSUFFICIENT_STORAGE, RECOMMEND_FAILED_INSUFFICIENT_STORAGE);
            }
            IoUtils.closeQuietly(autoCloseable);
            return size;
        } catch (Throwable th) {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    private static void copyZipEntry(ZipEntry zipEntry, ZipFile inZipFile, ZipOutputStream outZipStream) throws IOException {
        ZipEntry newEntry;
        byte[] buffer = new byte[HwPerformance.PERF_EVENT_RAW_REQ];
        if (zipEntry.getMethod() == 0) {
            newEntry = new ZipEntry(zipEntry);
        } else {
            newEntry = new ZipEntry(zipEntry.getName());
        }
        outZipStream.putNextEntry(newEntry);
        InputStream data = inZipFile.getInputStream(zipEntry);
        while (true) {
            try {
                int num = data.read(buffer);
                if (num <= 0) {
                    break;
                }
                outZipStream.write(buffer, APP_INSTALL_AUTO, num);
            } finally {
                IoUtils.closeQuietly(data);
            }
        }
        outZipStream.flush();
    }

    public static boolean fixSdPermissions(String cid, int gid, String filename) {
        try {
            if (getMountService().fixPermissionsSecureContainer(cid, gid, filename) == 0) {
                return true;
            }
            Log.i(TAG, "Failed to fixperms container " + cid);
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to fixperms container " + cid + " with exception " + e);
            return false;
        }
    }

    public static String resolveInstallVolume(Context context, String packageName, int installLocation, long sizeBytes) throws IOException {
        boolean forceAllowOnExternal = Global.getInt(context.getContentResolver(), "force_allow_on_external", APP_INSTALL_AUTO) != 0;
        ApplicationInfo existingInfo = null;
        try {
            existingInfo = context.getPackageManager().getApplicationInfo(packageName, AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        } catch (NameNotFoundException e) {
        }
        StorageManager storageManager = (StorageManager) context.getSystemService(StorageManager.class);
        boolean fitsOnInternal = fitsOnInternal(context, sizeBytes);
        ArraySet<String> allCandidates = new ArraySet();
        VolumeInfo bestCandidate = null;
        long bestCandidateAvailBytes = Long.MIN_VALUE;
        for (VolumeInfo vol : storageManager.getVolumes()) {
            int i = vol.type;
            if (r0 == RECOMMEND_INSTALL_INTERNAL && vol.isMountedWritable()) {
                long availBytes = storageManager.getStorageBytesUntilLow(new File(vol.path));
                if (availBytes >= sizeBytes) {
                    allCandidates.add(vol.fsUuid);
                }
                if (availBytes >= bestCandidateAvailBytes) {
                    bestCandidate = vol;
                    bestCandidateAvailBytes = availBytes;
                }
            }
        }
        if (existingInfo != null && existingInfo.isSystemApp()) {
            installLocation = RECOMMEND_INSTALL_INTERNAL;
        }
        if (!forceAllowOnExternal && installLocation == RECOMMEND_INSTALL_INTERNAL) {
            if (existingInfo != null) {
                if (!Objects.equals(existingInfo.volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL)) {
                    throw new IOException("Cannot automatically move " + packageName + " from " + existingInfo.volumeUuid + " to internal storage");
                }
            }
            if (fitsOnInternal) {
                return StorageManager.UUID_PRIVATE_INTERNAL;
            }
            throw new IOException("Requested internal only, but not enough space");
        } else if (existingInfo != null) {
            if (Objects.equals(existingInfo.volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL) && fitsOnInternal) {
                return StorageManager.UUID_PRIVATE_INTERNAL;
            }
            if (allCandidates.contains(existingInfo.volumeUuid)) {
                return existingInfo.volumeUuid;
            }
            throw new IOException("Not enough space on existing volume " + existingInfo.volumeUuid + " for " + packageName + " upgrade");
        } else if (bestCandidate != null) {
            return bestCandidate.fsUuid;
        } else {
            if (fitsOnInternal) {
                return StorageManager.UUID_PRIVATE_INTERNAL;
            }
            throw new IOException("No special requests, but no room anywhere");
        }
    }

    public static boolean fitsOnInternal(Context context, long sizeBytes) {
        return sizeBytes <= ((StorageManager) context.getSystemService(StorageManager.class)).getStorageBytesUntilLow(Environment.getDataDirectory());
    }

    public static boolean fitsOnExternal(Context context, long sizeBytes) {
        StorageManager storage = (StorageManager) context.getSystemService(StorageManager.class);
        StorageVolume primary = storage.getPrimaryVolume();
        if (sizeBytes <= 0 || primary.isEmulated() || !"mounted".equals(primary.getState()) || sizeBytes > storage.getStorageBytesUntilLow(primary.getPathFile())) {
            return false;
        }
        return true;
    }

    public static int resolveInstallLocation(Context context, String packageName, int installLocation, long sizeBytes, int installFlags) {
        int prefer;
        boolean checkBoth;
        ApplicationInfo existingInfo = null;
        try {
            existingInfo = context.getPackageManager().getApplicationInfo(packageName, AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        } catch (NameNotFoundException e) {
        }
        boolean ephemeral = false;
        if ((installFlags & GL10.GL_EXP) != 0) {
            prefer = RECOMMEND_INSTALL_INTERNAL;
            ephemeral = true;
            checkBoth = false;
        } else if ((installFlags & 16) != 0) {
            prefer = RECOMMEND_INSTALL_INTERNAL;
            checkBoth = false;
        } else if ((installFlags & 8) != 0) {
            prefer = RECOMMEND_INSTALL_EXTERNAL;
            checkBoth = false;
        } else if (installLocation == RECOMMEND_INSTALL_INTERNAL) {
            prefer = RECOMMEND_INSTALL_INTERNAL;
            checkBoth = false;
        } else if (installLocation == RECOMMEND_INSTALL_EXTERNAL) {
            prefer = RECOMMEND_INSTALL_EXTERNAL;
            checkBoth = true;
        } else if (installLocation == 0) {
            if (existingInfo == null) {
                prefer = RECOMMEND_INSTALL_INTERNAL;
            } else if ((existingInfo.flags & Protocol.BASE_DATA_CONNECTION) != 0) {
                prefer = RECOMMEND_INSTALL_EXTERNAL;
            } else {
                prefer = RECOMMEND_INSTALL_INTERNAL;
            }
            checkBoth = true;
        } else {
            prefer = RECOMMEND_INSTALL_INTERNAL;
            checkBoth = false;
        }
        boolean fitsOnInternal = false;
        if (checkBoth || prefer == RECOMMEND_INSTALL_INTERNAL) {
            fitsOnInternal = fitsOnInternal(context, sizeBytes);
        }
        boolean fitsOnExternal = false;
        if (checkBoth || prefer == RECOMMEND_INSTALL_EXTERNAL) {
            if (mCustPkgHelper == null || !mCustPkgHelper.isSdInstallEnabled()) {
                fitsOnExternal = fitsOnExternal(context, sizeBytes);
            } else {
                fitsOnExternal = mCustPkgHelper.fitsOnExternalEx(context, sizeBytes);
            }
        }
        if (prefer == RECOMMEND_INSTALL_INTERNAL) {
            if (fitsOnInternal) {
                int i;
                if (ephemeral) {
                    i = RECOMMEND_INSTALL_EPHEMERAL;
                } else {
                    i = RECOMMEND_INSTALL_INTERNAL;
                }
                return i;
            }
        } else if (prefer == RECOMMEND_INSTALL_EXTERNAL && fitsOnExternal) {
            return RECOMMEND_INSTALL_EXTERNAL;
        }
        if (checkBoth) {
            if (fitsOnInternal) {
                return RECOMMEND_INSTALL_INTERNAL;
            }
            if (fitsOnExternal) {
                return RECOMMEND_INSTALL_EXTERNAL;
            }
        }
        return RECOMMEND_FAILED_INSUFFICIENT_STORAGE;
    }

    public static long calculateInstalledSize(PackageLite pkg, boolean isForwardLocked, String abiOverride) throws IOException {
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = Handle.create(pkg);
            long calculateInstalledSize = calculateInstalledSize(pkg, autoCloseable, isForwardLocked, abiOverride);
            return calculateInstalledSize;
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    public static long calculateInstalledSize(PackageLite pkg, Handle handle, boolean isForwardLocked, String abiOverride) throws IOException {
        long sizeBytes = 0;
        for (String codePath : pkg.getAllCodePaths()) {
            File codeFile = new File(codePath);
            sizeBytes += codeFile.length();
            if (isForwardLocked) {
                sizeBytes += extractPublicFiles(codeFile, null);
            }
        }
        return sizeBytes + NativeLibraryHelper.sumNativeBinariesWithOverride(handle, abiOverride);
    }

    public static String replaceEnd(String str, String before, String after) {
        if (str.endsWith(before)) {
            return str.substring(APP_INSTALL_AUTO, str.length() - before.length()) + after;
        }
        throw new IllegalArgumentException("Expected " + str + " to end with " + before);
    }
}
