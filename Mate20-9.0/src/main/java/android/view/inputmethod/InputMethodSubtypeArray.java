package android.view.inputmethod;

import android.os.Parcel;
import android.util.Slog;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class InputMethodSubtypeArray {
    private static final String TAG = "InputMethodSubtypeArray";
    private volatile byte[] mCompressedData;
    private final int mCount;
    private volatile int mDecompressedSize;
    private volatile InputMethodSubtype[] mInstance;
    private final Object mLockObject = new Object();

    public InputMethodSubtypeArray(List<InputMethodSubtype> subtypes) {
        if (subtypes == null) {
            this.mCount = 0;
            return;
        }
        this.mCount = subtypes.size();
        this.mInstance = (InputMethodSubtype[]) subtypes.toArray(new InputMethodSubtype[this.mCount]);
    }

    public InputMethodSubtypeArray(Parcel source) {
        this.mCount = source.readInt();
        if (this.mCount > 0) {
            this.mDecompressedSize = source.readInt();
            this.mCompressedData = source.createByteArray();
        }
    }

    public void writeToParcel(Parcel dest) {
        if (this.mCount == 0) {
            dest.writeInt(this.mCount);
            return;
        }
        byte[] compressedData = this.mCompressedData;
        int decompressedSize = this.mDecompressedSize;
        if (compressedData == null && decompressedSize == 0) {
            synchronized (this.mLockObject) {
                compressedData = this.mCompressedData;
                decompressedSize = this.mDecompressedSize;
                if (compressedData == null && decompressedSize == 0) {
                    byte[] decompressedData = marshall(this.mInstance);
                    compressedData = compress(decompressedData);
                    if (compressedData == null) {
                        decompressedSize = -1;
                        Slog.i(TAG, "Failed to compress data.");
                    } else {
                        decompressedSize = decompressedData.length;
                    }
                    this.mDecompressedSize = decompressedSize;
                    this.mCompressedData = compressedData;
                }
            }
        }
        if (compressedData == null || decompressedSize <= 0) {
            Slog.i(TAG, "Unexpected state. Behaving as an empty array.");
            dest.writeInt(0);
        } else {
            dest.writeInt(this.mCount);
            dest.writeInt(decompressedSize);
            dest.writeByteArray(compressedData);
        }
    }

    public InputMethodSubtype get(int index) {
        if (index < 0 || this.mCount <= index) {
            throw new ArrayIndexOutOfBoundsException();
        }
        InputMethodSubtype[] instance = this.mInstance;
        if (instance == null) {
            synchronized (this.mLockObject) {
                instance = this.mInstance;
                if (instance == null) {
                    byte[] decompressedData = decompress(this.mCompressedData, this.mDecompressedSize);
                    this.mCompressedData = null;
                    this.mDecompressedSize = 0;
                    if (decompressedData != null) {
                        instance = unmarshall(decompressedData);
                    } else {
                        Slog.e(TAG, "Failed to decompress data. Returns null as fallback.");
                        instance = new InputMethodSubtype[this.mCount];
                    }
                    this.mInstance = instance;
                }
            }
        }
        return instance[index];
    }

    public int getCount() {
        return this.mCount;
    }

    private static byte[] marshall(InputMethodSubtype[] array) {
        Parcel parcel = null;
        try {
            parcel = Parcel.obtain();
            parcel.writeTypedArray(array, 0);
            byte[] marshall = parcel.marshall();
            return marshall;
        } finally {
            if (parcel != null) {
                parcel.recycle();
            }
        }
    }

    private static InputMethodSubtype[] unmarshall(byte[] data) {
        Parcel parcel = null;
        try {
            parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            InputMethodSubtype[] inputMethodSubtypeArr = (InputMethodSubtype[]) parcel.createTypedArray(InputMethodSubtype.CREATOR);
            return inputMethodSubtypeArr;
        } finally {
            if (parcel != null) {
                parcel.recycle();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001e, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0022, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0023, code lost:
        r5 = r4;
        r4 = r3;
        r3 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x002a, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x002b, code lost:
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x002f, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0030, code lost:
        r5 = r3;
        r3 = r2;
        r2 = r5;
     */
    private static byte[] compress(byte[] data) {
        Throwable th;
        Throwable th2;
        Throwable th3;
        Throwable th4;
        try {
            ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
            GZIPOutputStream zipper = new GZIPOutputStream(resultStream);
            zipper.write(data);
            zipper.finish();
            byte[] byteArray = resultStream.toByteArray();
            $closeResource(null, zipper);
            $closeResource(null, resultStream);
            return byteArray;
            $closeResource(th, resultStream);
            throw th2;
            $closeResource(th3, zipper);
            throw th4;
        } catch (Exception e) {
            Slog.e(TAG, "Failed to compress the data.", e);
            return null;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x002f, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0030, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0034, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0035, code lost:
        r7 = r4;
        r4 = r3;
        r3 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x003c, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x003d, code lost:
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0041, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0042, code lost:
        r7 = r3;
        r3 = r2;
        r2 = r7;
     */
    private static byte[] decompress(byte[] data, int expectedSize) {
        Throwable th;
        Throwable th2;
        Throwable th3;
        Throwable th4;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            GZIPInputStream unzipper = new GZIPInputStream(inputStream);
            byte[] result = new byte[expectedSize];
            int totalReadBytes = 0;
            while (totalReadBytes < result.length) {
                int readBytes = unzipper.read(result, totalReadBytes, result.length - totalReadBytes);
                if (readBytes < 0) {
                    break;
                }
                totalReadBytes += readBytes;
            }
            if (expectedSize != totalReadBytes) {
                $closeResource(null, unzipper);
                $closeResource(null, inputStream);
                return null;
            }
            $closeResource(null, unzipper);
            $closeResource(null, inputStream);
            return result;
            $closeResource(th, inputStream);
            throw th2;
            $closeResource(th3, unzipper);
            throw th4;
        } catch (Exception e) {
            Slog.e(TAG, "Failed to decompress the data.", e);
            return null;
        }
    }
}
