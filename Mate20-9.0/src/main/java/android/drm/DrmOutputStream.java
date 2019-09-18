package android.drm;

import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownServiceException;
import java.util.Arrays;
import libcore.io.IoBridge;
import libcore.io.Streams;

public class DrmOutputStream extends OutputStream {
    private static final String TAG = "DrmOutputStream";
    private final DrmManagerClient mClient;
    private final FileDescriptor mFd;
    private final ParcelFileDescriptor mPfd;
    private int mSessionId = -1;

    public DrmOutputStream(DrmManagerClient client, ParcelFileDescriptor pfd, String mimeType) throws IOException {
        this.mClient = client;
        this.mPfd = pfd;
        this.mFd = pfd.getFileDescriptor();
        this.mSessionId = this.mClient.openConvertSession(mimeType);
        if (this.mSessionId == -1) {
            throw new UnknownServiceException("Failed to open DRM session for " + mimeType);
        }
    }

    public void finish() throws IOException {
        DrmConvertedStatus status = this.mClient.closeConvertSession(this.mSessionId);
        if (status.statusCode == 1) {
            try {
                Os.lseek(this.mFd, (long) status.offset, OsConstants.SEEK_SET);
            } catch (ErrnoException e) {
                e.rethrowAsIOException();
            }
            IoBridge.write(this.mFd, status.convertedData, 0, status.convertedData.length);
            this.mSessionId = -1;
            return;
        }
        throw new IOException("Unexpected DRM status: " + status.statusCode);
    }

    public void close() throws IOException {
        if (this.mSessionId == -1) {
            Log.w(TAG, "Closing stream without finishing");
        }
        this.mPfd.close();
    }

    public void write(byte[] buffer, int offset, int count) throws IOException {
        byte[] exactBuffer;
        Arrays.checkOffsetAndCount(buffer.length, offset, count);
        if (count == buffer.length) {
            exactBuffer = buffer;
        } else {
            exactBuffer = new byte[count];
            System.arraycopy(buffer, offset, exactBuffer, 0, count);
        }
        DrmConvertedStatus status = this.mClient.convertData(this.mSessionId, exactBuffer);
        if (status.statusCode == 1) {
            IoBridge.write(this.mFd, status.convertedData, 0, status.convertedData.length);
            return;
        }
        throw new IOException("Unexpected DRM status: " + status.statusCode);
    }

    public void write(int oneByte) throws IOException {
        Streams.writeSingleByte(this, oneByte);
    }
}
