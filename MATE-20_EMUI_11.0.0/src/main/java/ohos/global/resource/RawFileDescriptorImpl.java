package ohos.global.resource;

import java.io.FileDescriptor;
import java.io.IOException;
import ohos.global.innerkit.asset.AfdAdapter;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class RawFileDescriptorImpl extends RawFileDescriptor {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "RawFileDescriptorImpl");
    private AfdAdapter afd;
    private long declaredLength;
    private FileDescriptor fileDescriptor;
    private long startOffset;

    private RawFileDescriptorImpl(FileDescriptor fileDescriptor2, long j, long j2) {
        this.fileDescriptor = fileDescriptor2;
        this.startOffset = j;
        this.declaredLength = j2;
    }

    public RawFileDescriptorImpl(AfdAdapter afdAdapter) {
        this(afdAdapter.getFileDescriptor(), afdAdapter.getStartOffset(), afdAdapter.getDeclaredLength());
        this.afd = afdAdapter;
    }

    @Override // ohos.global.resource.RawFileDescriptor, ohos.global.resource.BaseFileDescriptor
    public FileDescriptor getFileDescriptor() {
        return this.fileDescriptor;
    }

    @Override // ohos.global.resource.RawFileDescriptor, ohos.global.resource.BaseFileDescriptor
    public long getFileSize() {
        return this.declaredLength;
    }

    @Override // ohos.global.resource.RawFileDescriptor, ohos.global.resource.BaseFileDescriptor
    public long getStartPosition() {
        return this.startOffset;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        try {
            this.afd.close();
        } catch (IOException unused) {
            HiLog.error(LABEL, "close rawfile fail", new Object[0]);
        }
    }
}
