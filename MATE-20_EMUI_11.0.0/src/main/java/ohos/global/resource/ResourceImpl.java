package ohos.global.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import ohos.global.innerkit.asset.Package;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;

public class ResourceImpl extends Resource {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "ResourceImpl");
    private static final int MAX_FILE_PATH = 200;
    private long handle;
    private InputStream in;
    private boolean isSysResource = false;
    private Package resPackage;

    private native int nativeReadBytes(long j, byte[] bArr, int i, int i2);

    private native void nativeRelease(long j);

    static {
        System.loadLibrary("resource_jni.z");
    }

    public ResourceImpl(InputStream inputStream) {
        this.in = inputStream;
    }

    public ResourceImpl(Package r4, long j, boolean z) {
        HiLog.debug(LABEL, "ResourceImpl Constructor", new Object[0]);
        this.handle = j;
        this.resPackage = r4;
        this.isSysResource = z;
        getFileHandle();
    }

    private void getFileHandle() {
        byte[] bArr = new byte[200];
        int nativeReadBytes = nativeReadBytes(this.handle, bArr, 0, 200);
        if (nativeReadBytes <= 0) {
            HiLog.error(LABEL, "read file path fail", new Object[0]);
            return;
        }
        try {
            if (bArr[nativeReadBytes - 1] == 0) {
                nativeReadBytes--;
            }
            String substring = new String(bArr, ConstantValue.JPEG_FILE_NAME_ENCODE_CHARSET).substring(0, nativeReadBytes);
            if (this.isSysResource) {
                substring = Package.SYS_RESOURCE_PREFIX + substring;
            }
            this.in = this.resPackage.open(substring);
            if (this.in == null) {
                HiLog.error(LABEL, "pkg open file path fail", new Object[0]);
            }
        } catch (UnsupportedEncodingException e) {
            HiLog.error(LABEL, "UnsupportedEncodingException :%{public}s", e.getMessage());
        } catch (IOException unused) {
            HiLog.error(LABEL, "get resource failed", new Object[0]);
        }
    }

    @Override // ohos.global.resource.Resource, java.io.InputStream
    public int available() throws IOException {
        InputStream inputStream = this.in;
        if (inputStream != null) {
            return inputStream.available();
        }
        HiLog.error(LABEL, "when available, the IO handle is null", new Object[0]);
        return 0;
    }

    @Override // ohos.global.resource.Resource, java.io.InputStream
    public int read(byte[] bArr, int i, int i2) throws IOException, NullPointerException, IndexOutOfBoundsException {
        InputStream inputStream = this.in;
        if (inputStream != null) {
            return inputStream.read(bArr, i, i2);
        }
        HiLog.error(LABEL, "when read, the IO handle is null", new Object[0]);
        return -1;
    }

    @Override // ohos.global.resource.Resource, java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        InputStream inputStream = this.in;
        if (inputStream == null) {
            HiLog.error(LABEL, "when close, the IO handle is null", new Object[0]);
            return;
        }
        inputStream.close();
        this.in = null;
        nativeRelease(this.handle);
    }
}
