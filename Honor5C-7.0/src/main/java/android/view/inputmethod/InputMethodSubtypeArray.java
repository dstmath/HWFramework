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
    private final Object mLockObject;

    public InputMethodSubtypeArray(List<InputMethodSubtype> subtypes) {
        this.mLockObject = new Object();
        if (subtypes == null) {
            this.mCount = 0;
            return;
        }
        this.mCount = subtypes.size();
        this.mInstance = (InputMethodSubtype[]) subtypes.toArray(new InputMethodSubtype[this.mCount]);
    }

    public InputMethodSubtypeArray(Parcel source) {
        this.mLockObject = new Object();
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

    private static byte[] compress(byte[] data) {
        Throwable th;
        Exception e;
        Throwable th2;
        ByteArrayOutputStream byteArrayOutputStream = null;
        GZIPOutputStream gZIPOutputStream = null;
        Throwable th3;
        try {
            ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
            try {
                GZIPOutputStream zipper = new GZIPOutputStream(resultStream);
                try {
                    zipper.write(data);
                    zipper.finish();
                    byte[] toByteArray = resultStream.toByteArray();
                    if (zipper != null) {
                        try {
                            zipper.close();
                        } catch (Throwable th4) {
                            th3 = th4;
                        }
                    }
                    th3 = null;
                    if (resultStream != null) {
                        try {
                            resultStream.close();
                        } catch (Throwable th5) {
                            th = th5;
                            if (th3 != null) {
                                if (th3 != th) {
                                    th3.addSuppressed(th);
                                    th = th3;
                                }
                            }
                        }
                    }
                    th = th3;
                    if (th == null) {
                        return toByteArray;
                    }
                    try {
                        throw th;
                    } catch (Exception e2) {
                        e = e2;
                        byteArrayOutputStream = resultStream;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    th3 = null;
                    gZIPOutputStream = zipper;
                    byteArrayOutputStream = resultStream;
                    if (gZIPOutputStream != null) {
                        try {
                            gZIPOutputStream.close();
                        } catch (Throwable th7) {
                            th2 = th7;
                            if (th3 != null) {
                                if (th3 != th2) {
                                    th3.addSuppressed(th2);
                                    th2 = th3;
                                }
                            }
                        }
                    }
                    th2 = th3;
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable th8) {
                            th3 = th8;
                            if (th2 != null) {
                                if (th2 != th3) {
                                    th2.addSuppressed(th3);
                                    th3 = th2;
                                }
                            }
                        }
                    }
                    th3 = th2;
                    if (th3 != null) {
                        try {
                            throw th3;
                        } catch (Exception e3) {
                            e = e3;
                            Slog.e(TAG, "Failed to compress the data.", e);
                            return null;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th9) {
                th = th9;
                th3 = null;
                byteArrayOutputStream = resultStream;
                if (gZIPOutputStream != null) {
                    gZIPOutputStream.close();
                }
                th2 = th3;
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                th3 = th2;
                if (th3 != null) {
                    throw th;
                }
                throw th3;
            }
        } catch (Throwable th10) {
            th = th10;
            th3 = null;
            if (gZIPOutputStream != null) {
                gZIPOutputStream.close();
            }
            th2 = th3;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            th3 = th2;
            if (th3 != null) {
                throw th3;
            }
            throw th;
        }
    }

    private static byte[] decompress(byte[] data, int expectedSize) {
        Exception e;
        Throwable th;
        ByteArrayInputStream byteArrayInputStream = null;
        GZIPInputStream gZIPInputStream = null;
        Throwable th2;
        Throwable th3;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            try {
                GZIPInputStream unzipper = new GZIPInputStream(inputStream);
                try {
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
                        if (unzipper != null) {
                            try {
                                unzipper.close();
                            } catch (Throwable th4) {
                                th2 = th4;
                            }
                        }
                        th2 = null;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th5) {
                                th3 = th5;
                                if (th2 != null) {
                                    if (th2 != th3) {
                                        th2.addSuppressed(th3);
                                        th3 = th2;
                                    }
                                }
                            }
                        }
                        th3 = th2;
                        if (th3 == null) {
                            return null;
                        }
                        try {
                            throw th3;
                        } catch (Exception e2) {
                            e = e2;
                            byteArrayInputStream = inputStream;
                        }
                    } else {
                        if (unzipper != null) {
                            try {
                                unzipper.close();
                            } catch (Throwable th6) {
                                th2 = th6;
                            }
                        }
                        th2 = null;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th7) {
                                th3 = th7;
                                if (th2 != null) {
                                    if (th2 != th3) {
                                        th2.addSuppressed(th3);
                                        th3 = th2;
                                    }
                                }
                            }
                        }
                        th3 = th2;
                        if (th3 == null) {
                            return result;
                        }
                        throw th3;
                    }
                } catch (Throwable th8) {
                    th3 = th8;
                    th2 = null;
                    gZIPInputStream = unzipper;
                    byteArrayInputStream = inputStream;
                    if (gZIPInputStream != null) {
                        try {
                            gZIPInputStream.close();
                        } catch (Throwable th9) {
                            th = th9;
                            if (th2 != null) {
                                if (th2 != th) {
                                    th2.addSuppressed(th);
                                    th = th2;
                                }
                            }
                        }
                    }
                    th = th2;
                    if (byteArrayInputStream != null) {
                        try {
                            byteArrayInputStream.close();
                        } catch (Throwable th10) {
                            th2 = th10;
                            if (th != null) {
                                if (th != th2) {
                                    th.addSuppressed(th2);
                                    th2 = th;
                                }
                            }
                        }
                    }
                    th2 = th;
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (Exception e3) {
                            e = e3;
                            Slog.e(TAG, "Failed to decompress the data.", e);
                            return null;
                        }
                    }
                    throw th3;
                }
            } catch (Throwable th11) {
                th3 = th11;
                th2 = null;
                byteArrayInputStream = inputStream;
                if (gZIPInputStream != null) {
                    gZIPInputStream.close();
                }
                th = th2;
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
                th2 = th;
                if (th2 != null) {
                    throw th3;
                }
                throw th2;
            }
        } catch (Throwable th12) {
            th3 = th12;
            th2 = null;
            if (gZIPInputStream != null) {
                gZIPInputStream.close();
            }
            th = th2;
            if (byteArrayInputStream != null) {
                byteArrayInputStream.close();
            }
            th2 = th;
            if (th2 != null) {
                throw th2;
            }
            throw th3;
        }
    }
}
