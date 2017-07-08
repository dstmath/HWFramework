package com.android.internal.content;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PackageLite;
import android.content.pm.PackageParser.PackageParserException;
import android.os.Build;
import android.os.SELinux;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Slog;
import android.view.inputmethod.EditorInfo;
import dalvik.system.CloseGuard;
import dalvik.system.VMRuntime;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class NativeLibraryHelper {
    private static final int BITCODE_PRESENT = 1;
    public static final String CLEAR_ABI_OVERRIDE = "-";
    private static final boolean DEBUG_NATIVE = false;
    private static final boolean HAS_NATIVE_BRIDGE = false;
    public static final String LIB64_DIR_NAME = "lib64";
    public static final String LIB_DIR_NAME = "lib";
    private static final String TAG = "NativeHelper";
    private static final Object mRestoreconSync = null;

    public static class Handle implements Closeable {
        final long[] apkHandles;
        final boolean extractNativeLibs;
        private volatile boolean mClosed;
        private final CloseGuard mGuard;
        final boolean multiArch;

        public static Handle create(File packageFile) throws IOException {
            try {
                return create(PackageParser.parsePackageLite(packageFile, 0));
            } catch (PackageParserException e) {
                throw new IOException("Failed to parse package: " + packageFile, e);
            }
        }

        public static Handle create(Package pkg) throws IOException {
            boolean z;
            boolean z2 = true;
            List allCodePaths = pkg.getAllCodePaths();
            if ((pkg.applicationInfo.flags & RtlSpacingHelper.UNDEFINED) != 0) {
                z = true;
            } else {
                z = NativeLibraryHelper.HAS_NATIVE_BRIDGE;
            }
            if ((pkg.applicationInfo.flags & EditorInfo.IME_FLAG_NO_EXTRACT_UI) == 0) {
                z2 = NativeLibraryHelper.HAS_NATIVE_BRIDGE;
            }
            return create(allCodePaths, z, z2);
        }

        public static Handle create(PackageLite lite) throws IOException {
            return create(lite.getAllCodePaths(), lite.multiArch, lite.extractNativeLibs);
        }

        private static Handle create(List<String> codePaths, boolean multiArch, boolean extractNativeLibs) throws IOException {
            int size = codePaths.size();
            long[] apkHandles = new long[size];
            for (int i = 0; i < size; i += NativeLibraryHelper.BITCODE_PRESENT) {
                String path = (String) codePaths.get(i);
                apkHandles[i] = NativeLibraryHelper.nativeOpenApk(path);
                if (apkHandles[i] == 0) {
                    for (int j = 0; j < i; j += NativeLibraryHelper.BITCODE_PRESENT) {
                        NativeLibraryHelper.nativeClose(apkHandles[j]);
                    }
                    throw new IOException("Unable to open APK: " + path);
                }
            }
            return new Handle(apkHandles, multiArch, extractNativeLibs);
        }

        Handle(long[] apkHandles, boolean multiArch, boolean extractNativeLibs) {
            this.mGuard = CloseGuard.get();
            this.apkHandles = apkHandles;
            this.multiArch = multiArch;
            this.extractNativeLibs = extractNativeLibs;
            this.mGuard.open("close");
        }

        public void close() {
            long[] jArr = this.apkHandles;
            int length = jArr.length;
            for (int i = 0; i < length; i += NativeLibraryHelper.BITCODE_PRESENT) {
                NativeLibraryHelper.nativeClose(jArr[i]);
            }
            this.mGuard.close();
            this.mClosed = true;
        }

        protected void finalize() throws Throwable {
            if (this.mGuard != null) {
                this.mGuard.warnIfOpen();
            }
            try {
                if (!this.mClosed) {
                    close();
                }
                super.finalize();
            } catch (Throwable th) {
                super.finalize();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.content.NativeLibraryHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.content.NativeLibraryHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.content.NativeLibraryHelper.<clinit>():void");
    }

    private static native int hasRenderscriptBitcode(long j);

    private static native void nativeClose(long j);

    private static native int nativeCopyNativeBinaries(long j, String str, String str2, boolean z, boolean z2);

    private static native int nativeFindSupportedAbi(long j, String[] strArr);

    private static native long nativeOpenApk(String str);

    private static native long nativeSumNativeBinaries(long j, String str);

    private static long sumNativeBinaries(Handle handle, String abi) {
        long sum = 0;
        long[] jArr = handle.apkHandles;
        for (int i = 0; i < jArr.length; i += BITCODE_PRESENT) {
            sum += nativeSumNativeBinaries(jArr[i], abi);
        }
        return sum;
    }

    public static int copyNativeBinaries(Handle handle, File sharedLibraryDir, String abi) {
        long[] jArr = handle.apkHandles;
        int length = jArr.length;
        for (int i = 0; i < length; i += BITCODE_PRESENT) {
            int res = nativeCopyNativeBinaries(jArr[i], sharedLibraryDir.getPath(), abi, handle.extractNativeLibs, HAS_NATIVE_BRIDGE);
            if (res != BITCODE_PRESENT) {
                return res;
            }
        }
        return BITCODE_PRESENT;
    }

    public static int findSupportedAbi(Handle handle, String[] supportedAbis) {
        int finalRes = -114;
        long[] jArr = handle.apkHandles;
        int length = jArr.length;
        for (int i = 0; i < length; i += BITCODE_PRESENT) {
            int res = nativeFindSupportedAbi(jArr[i], supportedAbis);
            if (res != -114) {
                if (res == -113) {
                    if (finalRes < 0) {
                        finalRes = -113;
                    }
                } else if (res < 0) {
                    return res;
                } else {
                    if (finalRes < 0 || res < finalRes) {
                        finalRes = res;
                    }
                }
            }
        }
        return finalRes;
    }

    public static void removeNativeBinariesLI(String nativeLibraryPath) {
        if (nativeLibraryPath != null) {
            removeNativeBinariesFromDirLI(new File(nativeLibraryPath), HAS_NATIVE_BRIDGE);
        }
    }

    public static void removeNativeBinariesFromDirLI(File nativeLibraryRoot, boolean deleteRootDir) {
        if (nativeLibraryRoot.exists()) {
            File[] files = nativeLibraryRoot.listFiles();
            if (files != null) {
                for (int nn = 0; nn < files.length; nn += BITCODE_PRESENT) {
                    if (files[nn].isDirectory()) {
                        removeNativeBinariesFromDirLI(files[nn], true);
                    } else if (!files[nn].delete()) {
                        Slog.w(TAG, "Could not delete native binary: " + files[nn].getPath());
                    }
                }
            }
            if (deleteRootDir && !nativeLibraryRoot.delete()) {
                Slog.w(TAG, "Could not delete native binary directory: " + nativeLibraryRoot.getPath());
            }
        }
    }

    private static void createNativeLibrarySubdir(File path) throws IOException {
        if (path.isDirectory()) {
            synchronized (mRestoreconSync) {
                if (SELinux.restorecon(path)) {
                } else {
                    throw new IOException("Cannot set SELinux context for " + path.getPath());
                }
            }
            return;
        }
        path.delete();
        if (path.mkdir()) {
            try {
                Os.chmod(path.getPath(), (((OsConstants.S_IRWXU | OsConstants.S_IRGRP) | OsConstants.S_IXGRP) | OsConstants.S_IROTH) | OsConstants.S_IXOTH);
                return;
            } catch (ErrnoException e) {
                throw new IOException("Cannot chmod native library directory " + path.getPath(), e);
            }
        }
        throw new IOException("Cannot create " + path.getPath());
    }

    private static long sumNativeBinariesForSupportedAbi(Handle handle, String[] abiList) {
        int abi = findSupportedAbi(handle, abiList);
        if (abi >= 0) {
            return sumNativeBinaries(handle, abiList[abi]);
        }
        return 0;
    }

    public static int copyNativeBinariesForSupportedAbi(Handle handle, File libraryRoot, String[] abiList, boolean useIsaSubdir) throws IOException {
        createNativeLibrarySubdir(libraryRoot);
        int abi = findSupportedAbi(handle, abiList);
        if (abi >= 0) {
            File subDir;
            String instructionSet = VMRuntime.getInstructionSet(abiList[abi]);
            if (useIsaSubdir) {
                File isaSubdir = new File(libraryRoot, instructionSet);
                createNativeLibrarySubdir(isaSubdir);
                subDir = isaSubdir;
            } else {
                subDir = libraryRoot;
            }
            int copyRet = copyNativeBinaries(handle, subDir, abiList[abi]);
            if (copyRet != BITCODE_PRESENT) {
                return copyRet;
            }
        }
        return abi;
    }

    public static int copyNativeBinariesWithOverride(Handle handle, File libraryRoot, String abiOverride) {
        try {
            int copyRet;
            if (handle.multiArch) {
                if (!(abiOverride == null || CLEAR_ABI_OVERRIDE.equals(abiOverride))) {
                    Slog.w(TAG, "Ignoring abiOverride for multi arch application.");
                }
                if (Build.SUPPORTED_32_BIT_ABIS.length > 0) {
                    copyRet = copyNativeBinariesForSupportedAbi(handle, libraryRoot, Build.SUPPORTED_32_BIT_ABIS, true);
                    if (!(copyRet >= 0 || copyRet == -114 || copyRet == -113)) {
                        Slog.w(TAG, "Failure copying 32 bit native libraries; copyRet=" + copyRet);
                        return copyRet;
                    }
                }
                if (Build.SUPPORTED_64_BIT_ABIS.length > 0) {
                    copyRet = copyNativeBinariesForSupportedAbi(handle, libraryRoot, Build.SUPPORTED_64_BIT_ABIS, true);
                    if (!(copyRet >= 0 || copyRet == -114 || copyRet == -113)) {
                        Slog.w(TAG, "Failure copying 64 bit native libraries; copyRet=" + copyRet);
                        return copyRet;
                    }
                }
            }
            String[] abiList;
            String cpuAbiOverride = null;
            if (CLEAR_ABI_OVERRIDE.equals(abiOverride)) {
                cpuAbiOverride = null;
            } else if (abiOverride != null) {
                cpuAbiOverride = abiOverride;
            }
            if (cpuAbiOverride != null) {
                abiList = new String[BITCODE_PRESENT];
                abiList[0] = cpuAbiOverride;
            } else {
                abiList = Build.SUPPORTED_ABIS;
            }
            if (Build.SUPPORTED_64_BIT_ABIS.length > 0 && cpuAbiOverride == null && hasRenderscriptBitcode(handle)) {
                abiList = Build.SUPPORTED_32_BIT_ABIS;
            }
            copyRet = copyNativeBinariesForSupportedAbi(handle, libraryRoot, abiList, true);
            if (copyRet < 0 && copyRet != -114) {
                Slog.w(TAG, "Failure copying native libraries [errorCode=" + copyRet + "]");
                return copyRet;
            }
            return BITCODE_PRESENT;
        } catch (IOException e) {
            Slog.e(TAG, "Copying native libraries failed", e);
            return -110;
        }
    }

    public static long sumNativeBinariesWithOverride(Handle handle, String abiOverride) throws IOException {
        long sum = 0;
        if (handle.multiArch) {
            if (!(abiOverride == null || CLEAR_ABI_OVERRIDE.equals(abiOverride))) {
                Slog.w(TAG, "Ignoring abiOverride for multi arch application.");
            }
            if (Build.SUPPORTED_32_BIT_ABIS.length > 0) {
                sum = 0 + sumNativeBinariesForSupportedAbi(handle, Build.SUPPORTED_32_BIT_ABIS);
            }
            return Build.SUPPORTED_64_BIT_ABIS.length > 0 ? sum + sumNativeBinariesForSupportedAbi(handle, Build.SUPPORTED_64_BIT_ABIS) : sum;
        } else {
            String[] abiList;
            String cpuAbiOverride = null;
            if (CLEAR_ABI_OVERRIDE.equals(abiOverride)) {
                cpuAbiOverride = null;
            } else if (abiOverride != null) {
                cpuAbiOverride = abiOverride;
            }
            if (cpuAbiOverride != null) {
                abiList = new String[BITCODE_PRESENT];
                abiList[0] = cpuAbiOverride;
            } else {
                abiList = Build.SUPPORTED_ABIS;
            }
            if (Build.SUPPORTED_64_BIT_ABIS.length > 0 && cpuAbiOverride == null && hasRenderscriptBitcode(handle)) {
                abiList = Build.SUPPORTED_32_BIT_ABIS;
            }
            return 0 + sumNativeBinariesForSupportedAbi(handle, abiList);
        }
    }

    public static boolean hasRenderscriptBitcode(Handle handle) throws IOException {
        long[] jArr = handle.apkHandles;
        int length = jArr.length;
        int i = 0;
        while (i < length) {
            int res = hasRenderscriptBitcode(jArr[i]);
            if (res < 0) {
                throw new IOException("Error scanning APK, code: " + res);
            } else if (res == BITCODE_PRESENT) {
                return true;
            } else {
                i += BITCODE_PRESENT;
            }
        }
        return HAS_NATIVE_BRIDGE;
    }
}
