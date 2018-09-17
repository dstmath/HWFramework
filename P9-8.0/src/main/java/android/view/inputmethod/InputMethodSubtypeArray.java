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

    /* JADX WARNING: Removed duplicated region for block: B:36:0x0048 A:{SYNTHETIC, Splitter: B:36:0x0048} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x004e A:{SYNTHETIC, Splitter: B:40:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x006b A:{Catch:{ Exception -> 0x0055 }} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0054 A:{SYNTHETIC, Splitter: B:44:0x0054} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0048 A:{SYNTHETIC, Splitter: B:36:0x0048} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x004e A:{SYNTHETIC, Splitter: B:40:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0054 A:{SYNTHETIC, Splitter: B:44:0x0054} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x006b A:{Catch:{ Exception -> 0x0055 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static byte[] compress(byte[] data) {
        Throwable th;
        Throwable th2;
        Exception e;
        Throwable th3;
        ByteArrayOutputStream resultStream = null;
        GZIPOutputStream zipper = null;
        try {
            ByteArrayOutputStream resultStream2 = new ByteArrayOutputStream();
            try {
                GZIPOutputStream zipper2 = new GZIPOutputStream(resultStream2);
                try {
                    zipper2.write(data);
                    zipper2.finish();
                    byte[] toByteArray = resultStream2.toByteArray();
                    if (zipper2 != null) {
                        try {
                            zipper2.close();
                        } catch (Throwable th4) {
                            th = th4;
                        }
                    }
                    th = null;
                    if (resultStream2 != null) {
                        try {
                            resultStream2.close();
                        } catch (Throwable th5) {
                            th2 = th5;
                            if (th != null) {
                                if (th != th2) {
                                    th.addSuppressed(th2);
                                    th2 = th;
                                }
                            }
                        }
                    }
                    th2 = th;
                    if (th2 == null) {
                        return toByteArray;
                    }
                    try {
                        throw th2;
                    } catch (Exception e2) {
                        e = e2;
                        resultStream = resultStream2;
                    }
                } catch (Throwable th6) {
                    th2 = th6;
                    zipper = zipper2;
                    resultStream = resultStream2;
                    th = null;
                    if (zipper != null) {
                    }
                    th3 = th;
                    if (resultStream != null) {
                    }
                    th = th3;
                    if (th != null) {
                    }
                }
            } catch (Throwable th7) {
                th2 = th7;
                resultStream = resultStream2;
                th = null;
                if (zipper != null) {
                }
                th3 = th;
                if (resultStream != null) {
                }
                th = th3;
                if (th != null) {
                }
            }
        } catch (Throwable th8) {
            th2 = th8;
            th = null;
            if (zipper != null) {
                try {
                    zipper.close();
                } catch (Throwable th9) {
                    th3 = th9;
                    if (th != null) {
                        if (th != th3) {
                            th.addSuppressed(th3);
                            th3 = th;
                        }
                    }
                }
            }
            th3 = th;
            if (resultStream != null) {
                try {
                    resultStream.close();
                } catch (Throwable th10) {
                    th = th10;
                    if (th3 != null) {
                        if (th3 != th) {
                            th3.addSuppressed(th);
                            th = th3;
                        }
                    }
                }
            }
            th = th3;
            if (th != null) {
                try {
                    throw th;
                } catch (Exception e3) {
                    e = e3;
                    Slog.e(TAG, "Failed to compress the data.", e);
                    return null;
                }
            }
            throw th2;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x006d A:{SYNTHETIC, Splitter: B:60:0x006d} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0073 A:{SYNTHETIC, Splitter: B:64:0x0073} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0090 A:{Catch:{ Exception -> 0x007a }} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0079 A:{SYNTHETIC, Splitter: B:68:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x006d A:{SYNTHETIC, Splitter: B:60:0x006d} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0073 A:{SYNTHETIC, Splitter: B:64:0x0073} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0079 A:{SYNTHETIC, Splitter: B:68:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0090 A:{Catch:{ Exception -> 0x007a }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static byte[] decompress(byte[] data, int expectedSize) {
        Throwable th;
        Throwable th2;
        Exception e;
        Throwable th3;
        ByteArrayInputStream inputStream = null;
        GZIPInputStream unzipper = null;
        try {
            ByteArrayInputStream inputStream2 = new ByteArrayInputStream(data);
            try {
                GZIPInputStream unzipper2 = new GZIPInputStream(inputStream2);
                try {
                    byte[] result = new byte[expectedSize];
                    int totalReadBytes = 0;
                    while (totalReadBytes < result.length) {
                        int readBytes = unzipper2.read(result, totalReadBytes, result.length - totalReadBytes);
                        if (readBytes < 0) {
                            break;
                        }
                        totalReadBytes += readBytes;
                    }
                    if (expectedSize != totalReadBytes) {
                        if (unzipper2 != null) {
                            try {
                                unzipper2.close();
                            } catch (Throwable th4) {
                                th = th4;
                            }
                        }
                        th = null;
                        if (inputStream2 != null) {
                            try {
                                inputStream2.close();
                            } catch (Throwable th5) {
                                th2 = th5;
                                if (th != null) {
                                    if (th != th2) {
                                        th.addSuppressed(th2);
                                        th2 = th;
                                    }
                                }
                            }
                        }
                        th2 = th;
                        if (th2 == null) {
                            return null;
                        }
                        try {
                            throw th2;
                        } catch (Exception e2) {
                            e = e2;
                            inputStream = inputStream2;
                        }
                    } else {
                        if (unzipper2 != null) {
                            try {
                                unzipper2.close();
                            } catch (Throwable th6) {
                                th = th6;
                            }
                        }
                        th = null;
                        if (inputStream2 != null) {
                            try {
                                inputStream2.close();
                            } catch (Throwable th7) {
                                th2 = th7;
                                if (th != null) {
                                    if (th != th2) {
                                        th.addSuppressed(th2);
                                        th2 = th;
                                    }
                                }
                            }
                        }
                        th2 = th;
                        if (th2 == null) {
                            return result;
                        }
                        throw th2;
                    }
                } catch (Throwable th8) {
                    th2 = th8;
                    unzipper = unzipper2;
                    inputStream = inputStream2;
                    th = null;
                    if (unzipper != null) {
                    }
                    th3 = th;
                    if (inputStream != null) {
                    }
                    th = th3;
                    if (th != null) {
                    }
                }
            } catch (Throwable th9) {
                th2 = th9;
                inputStream = inputStream2;
                th = null;
                if (unzipper != null) {
                }
                th3 = th;
                if (inputStream != null) {
                }
                th = th3;
                if (th != null) {
                }
            }
        } catch (Throwable th10) {
            th2 = th10;
            th = null;
            if (unzipper != null) {
                try {
                    unzipper.close();
                } catch (Throwable th11) {
                    th3 = th11;
                    if (th != null) {
                        if (th != th3) {
                            th.addSuppressed(th3);
                            th3 = th;
                        }
                    }
                }
            }
            th3 = th;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th12) {
                    th = th12;
                    if (th3 != null) {
                        if (th3 != th) {
                            th3.addSuppressed(th);
                            th = th3;
                        }
                    }
                }
            }
            th = th3;
            if (th != null) {
                try {
                    throw th;
                } catch (Exception e3) {
                    e = e3;
                    Slog.e(TAG, "Failed to decompress the data.", e);
                    return null;
                }
            }
            throw th2;
        }
    }
}
