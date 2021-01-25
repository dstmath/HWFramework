package android.os;

import android.content.Context;
import android.os.storage.StorageManager;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;
import libcore.io.IoUtils;
import libcore.util.EmptyArray;

public class RedactingFileDescriptor {
    private static final boolean DEBUG = true;
    private static final String TAG = "RedactingFileDescriptor";
    private final ProxyFileDescriptorCallback mCallback = new ProxyFileDescriptorCallback() {
        /* class android.os.RedactingFileDescriptor.AnonymousClass1 */

        @Override // android.os.ProxyFileDescriptorCallback
        public long onGetSize() throws ErrnoException {
            return Os.fstat(RedactingFileDescriptor.this.mInner).st_size;
        }

        /* JADX INFO: Multiple debug info for r0v5 long: [D('ranges' long[]), D('freeEnd' long)] */
        @Override // android.os.ProxyFileDescriptorCallback
        public int onRead(long offset, int size, byte[] data) throws ErrnoException {
            AnonymousClass1 r1 = this;
            long j = offset;
            int n = 0;
            while (n < size) {
                try {
                    int res = Os.pread(RedactingFileDescriptor.this.mInner, data, n, size - n, j + ((long) n));
                    if (res == 0) {
                        break;
                    }
                    n += res;
                } catch (InterruptedIOException e) {
                    n += e.bytesTransferred;
                }
            }
            long[] ranges = RedactingFileDescriptor.this.mRedactRanges;
            int i = 0;
            while (i < ranges.length) {
                long start = Math.max(j, ranges[i]);
                long end = Math.min(((long) size) + j, ranges[i + 1]);
                for (long j2 = start; j2 < end; j2++) {
                    data[(int) (j2 - j)] = 0;
                }
                long[] jArr = RedactingFileDescriptor.this.mFreeOffsets;
                int length = jArr.length;
                int i2 = 0;
                while (i2 < length) {
                    long freeOffset = jArr[i2];
                    long freeEnd = freeOffset + 4;
                    long redactFreeStart = Math.max(freeOffset, start);
                    long redactFreeEnd = Math.min(freeEnd, end);
                    long j3 = redactFreeStart;
                    while (j3 < redactFreeEnd) {
                        data[(int) (j3 - j)] = (byte) "free".charAt((int) (j3 - freeOffset));
                        j3++;
                        j = offset;
                        freeEnd = freeEnd;
                    }
                    i2++;
                    j = offset;
                    ranges = ranges;
                }
                i += 2;
                r1 = this;
                j = offset;
            }
            return n;
        }

        @Override // android.os.ProxyFileDescriptorCallback
        public int onWrite(long offset, int size, byte[] data) throws ErrnoException {
            int n = 0;
            while (n < size) {
                try {
                    int res = Os.pwrite(RedactingFileDescriptor.this.mInner, data, n, size - n, offset + ((long) n));
                    if (res == 0) {
                        break;
                    }
                    n += res;
                } catch (InterruptedIOException e) {
                    n += e.bytesTransferred;
                }
            }
            RedactingFileDescriptor redactingFileDescriptor = RedactingFileDescriptor.this;
            redactingFileDescriptor.mRedactRanges = RedactingFileDescriptor.removeRange(redactingFileDescriptor.mRedactRanges, offset, ((long) n) + offset);
            return n;
        }

        @Override // android.os.ProxyFileDescriptorCallback
        public void onFsync() throws ErrnoException {
            Os.fsync(RedactingFileDescriptor.this.mInner);
        }

        @Override // android.os.ProxyFileDescriptorCallback
        public void onRelease() {
            Slog.v(RedactingFileDescriptor.TAG, "onRelease()");
            IoUtils.closeQuietly(RedactingFileDescriptor.this.mInner);
        }
    };
    private volatile long[] mFreeOffsets;
    private FileDescriptor mInner = null;
    private ParcelFileDescriptor mOuter = null;
    private volatile long[] mRedactRanges;

    private RedactingFileDescriptor(Context context, File file, int mode, long[] redactRanges, long[] freeOffsets) throws IOException {
        this.mRedactRanges = checkRangesArgument(redactRanges);
        this.mFreeOffsets = freeOffsets;
        try {
            this.mInner = Os.open(file.getAbsolutePath(), FileUtils.translateModePfdToPosix(mode), 0);
            this.mOuter = ((StorageManager) context.getSystemService(StorageManager.class)).openProxyFileDescriptor(mode, this.mCallback);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        } catch (IOException e2) {
            IoUtils.closeQuietly(this.mInner);
            IoUtils.closeQuietly(this.mOuter);
            throw e2;
        }
    }

    private static long[] checkRangesArgument(long[] ranges) {
        if (ranges.length % 2 == 0) {
            for (int i = 0; i < ranges.length - 1; i += 2) {
                if (ranges[i] > ranges[i + 1]) {
                    throw new IllegalArgumentException();
                }
            }
            return ranges;
        }
        throw new IllegalArgumentException();
    }

    public static ParcelFileDescriptor open(Context context, File file, int mode, long[] redactRanges, long[] freePositions) throws IOException {
        return new RedactingFileDescriptor(context, file, mode, redactRanges, freePositions).mOuter;
    }

    @VisibleForTesting
    public static long[] removeRange(long[] ranges, long start, long end) {
        if (start == end) {
            return ranges;
        }
        if (start <= end) {
            long[] res = EmptyArray.LONG;
            for (int i = 0; i < ranges.length; i += 2) {
                if (start > ranges[i] || end < ranges[i + 1]) {
                    if (start < ranges[i] || end > ranges[i + 1]) {
                        res = Arrays.copyOf(res, res.length + 2);
                        if (end < ranges[i] || end > ranges[i + 1]) {
                            res[res.length - 2] = ranges[i];
                        } else {
                            res[res.length - 2] = Math.max(ranges[i], end);
                        }
                        if (start < ranges[i] || start > ranges[i + 1]) {
                            res[res.length - 1] = ranges[i + 1];
                        } else {
                            res[res.length - 1] = Math.min(ranges[i + 1], start);
                        }
                    } else {
                        res = Arrays.copyOf(res, res.length + 4);
                        res[res.length - 4] = ranges[i];
                        res[res.length - 3] = start;
                        res[res.length - 2] = end;
                        res[res.length - 1] = ranges[i + 1];
                    }
                }
            }
            return res;
        }
        throw new IllegalArgumentException();
    }
}
