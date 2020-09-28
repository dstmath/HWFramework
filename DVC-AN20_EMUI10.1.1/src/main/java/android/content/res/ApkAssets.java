package android.content.res;

import android.annotation.UnsupportedAppUsage;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.FileDescriptor;
import java.io.IOException;

public final class ApkAssets {
    @GuardedBy({"this"})
    private final long mNativePtr;
    @GuardedBy({"this"})
    private boolean mOpen = true;
    @GuardedBy({"this"})
    private final StringBlock mStringBlock;

    private static native void nativeDestroy(long j);

    private static native String nativeGetAssetPath(long j);

    private static native String nativeGetPackageName(long j);

    private static native long nativeGetStringBlock(long j);

    private static native boolean nativeIsUpToDate(long j);

    private static native long nativeLoad(String str, boolean z, boolean z2, boolean z3) throws IOException;

    private static native long nativeLoadFromFd(FileDescriptor fileDescriptor, String str, boolean z, boolean z2) throws IOException;

    private static native long nativeOpenXml(long j, String str) throws IOException;

    public static ApkAssets loadFromPath(String path) throws IOException {
        return new ApkAssets(path, false, false, false);
    }

    public static ApkAssets loadFromPath(String path, boolean system) throws IOException {
        return new ApkAssets(path, system, false, false);
    }

    public static ApkAssets loadFromPath(String path, boolean system, boolean forceSharedLibrary) throws IOException {
        return new ApkAssets(path, system, forceSharedLibrary, false);
    }

    public static ApkAssets loadFromFd(FileDescriptor fd, String friendlyName, boolean system, boolean forceSharedLibrary) throws IOException {
        return new ApkAssets(fd, friendlyName, system, forceSharedLibrary);
    }

    public static ApkAssets loadOverlayFromPath(String idmapPath, boolean system) throws IOException {
        return new ApkAssets(idmapPath, system, false, true);
    }

    private ApkAssets(String path, boolean system, boolean forceSharedLib, boolean overlay) throws IOException {
        Preconditions.checkNotNull(path, "path");
        this.mNativePtr = nativeLoad(path, system, forceSharedLib, overlay);
        this.mStringBlock = new StringBlock(nativeGetStringBlock(this.mNativePtr), true);
    }

    private ApkAssets(FileDescriptor fd, String friendlyName, boolean system, boolean forceSharedLib) throws IOException {
        Preconditions.checkNotNull(fd, "fd");
        Preconditions.checkNotNull(friendlyName, "friendlyName");
        this.mNativePtr = nativeLoadFromFd(fd, friendlyName, system, forceSharedLib);
        this.mStringBlock = new StringBlock(nativeGetStringBlock(this.mNativePtr), true);
    }

    @UnsupportedAppUsage
    public String getAssetPath() {
        String nativeGetAssetPath;
        synchronized (this) {
            nativeGetAssetPath = nativeGetAssetPath(this.mNativePtr);
        }
        return nativeGetAssetPath;
    }

    /* access modifiers changed from: package-private */
    public CharSequence getStringFromPool(int idx) {
        CharSequence charSequence;
        synchronized (this) {
            charSequence = this.mStringBlock.get(idx);
        }
        return charSequence;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002d, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002e, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0031, code lost:
        throw r4;
     */
    public XmlResourceParser openXml(String fileName) throws IOException {
        XmlResourceParser parser;
        Preconditions.checkNotNull(fileName, "fileName");
        synchronized (this) {
            XmlBlock block = new XmlBlock(null, nativeOpenXml(this.mNativePtr, fileName));
            parser = block.newParser();
            if (parser != null) {
                block.close();
            } else {
                throw new AssertionError("block.newParser() returned a null parser");
            }
        }
        return parser;
    }

    public boolean isUpToDate() {
        boolean nativeIsUpToDate;
        synchronized (this) {
            nativeIsUpToDate = nativeIsUpToDate(this.mNativePtr);
        }
        return nativeIsUpToDate;
    }

    public String toString() {
        return "ApkAssets{path=" + getAssetPath() + "}";
    }

    public String getApkPackageName() {
        return nativeGetPackageName(this.mNativePtr);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        close();
    }

    public void close() throws Throwable {
        synchronized (this) {
            if (this.mOpen) {
                this.mOpen = false;
                this.mStringBlock.close();
                nativeDestroy(this.mNativePtr);
            }
        }
    }
}
