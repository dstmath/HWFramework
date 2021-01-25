package android.os;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;
import com.android.internal.os.IDropBoxManagerService;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class DropBoxManager {
    public static final String ACTION_DROPBOX_ENTRY_ADDED = "android.intent.action.DROPBOX_ENTRY_ADDED";
    public static final String EXTRA_DROPPED_COUNT = "android.os.extra.DROPPED_COUNT";
    public static final String EXTRA_TAG = "tag";
    public static final String EXTRA_TIME = "time";
    private static final int HAS_BYTE_ARRAY = 8;
    public static final int IS_EMPTY = 1;
    public static final int IS_GZIPPED = 4;
    public static final int IS_TEXT = 2;
    private static final String TAG = "DropBoxManager";
    private final Context mContext;
    @UnsupportedAppUsage
    private final IDropBoxManagerService mService;

    public static class Entry implements Parcelable, Closeable {
        public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator() {
            /* class android.os.DropBoxManager.Entry.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Entry[] newArray(int size) {
                return new Entry[size];
            }

            @Override // android.os.Parcelable.Creator
            public Entry createFromParcel(Parcel in) {
                String tag = in.readString();
                long millis = in.readLong();
                int flags = in.readInt();
                if ((flags & 8) != 0) {
                    return new Entry(tag, millis, in.createByteArray(), flags & -9);
                }
                return new Entry(tag, millis, ParcelFileDescriptor.CREATOR.createFromParcel(in), flags);
            }
        };
        private final byte[] mData;
        private final ParcelFileDescriptor mFileDescriptor;
        private final int mFlags;
        private final String mTag;
        private final long mTimeMillis;

        public Entry(String tag, long millis) {
            if (tag != null) {
                this.mTag = tag;
                this.mTimeMillis = millis;
                this.mData = null;
                this.mFileDescriptor = null;
                this.mFlags = 1;
                return;
            }
            throw new NullPointerException("tag == null");
        }

        public Entry(String tag, long millis, String text) {
            if (tag == null) {
                throw new NullPointerException("tag == null");
            } else if (text != null) {
                this.mTag = tag;
                this.mTimeMillis = millis;
                this.mData = text.getBytes();
                this.mFileDescriptor = null;
                this.mFlags = 2;
            } else {
                throw new NullPointerException("text == null");
            }
        }

        public Entry(String tag, long millis, byte[] data, int flags) {
            if (tag != null) {
                if (((flags & 1) != 0) == (data == null)) {
                    this.mTag = tag;
                    this.mTimeMillis = millis;
                    this.mData = data;
                    this.mFileDescriptor = null;
                    this.mFlags = flags;
                    return;
                }
                throw new IllegalArgumentException("Bad flags: " + flags);
            }
            throw new NullPointerException("tag == null");
        }

        public Entry(String tag, long millis, ParcelFileDescriptor data, int flags) {
            if (tag != null) {
                if (((flags & 1) != 0) == (data == null)) {
                    this.mTag = tag;
                    this.mTimeMillis = millis;
                    this.mData = null;
                    this.mFileDescriptor = data;
                    this.mFlags = flags;
                    return;
                }
                throw new IllegalArgumentException("Bad flags: " + flags);
            }
            throw new NullPointerException("tag == null");
        }

        public Entry(String tag, long millis, File data, int flags) throws IOException {
            if (tag == null) {
                throw new NullPointerException("tag == null");
            } else if ((flags & 1) == 0) {
                this.mTag = tag;
                this.mTimeMillis = millis;
                this.mData = null;
                this.mFileDescriptor = ParcelFileDescriptor.open(data, 268435456);
                this.mFlags = flags;
            } else {
                throw new IllegalArgumentException("Bad flags: " + flags);
            }
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable
        public void close() {
            try {
                if (this.mFileDescriptor != null) {
                    this.mFileDescriptor.close();
                }
            } catch (IOException e) {
            }
        }

        public String getTag() {
            return this.mTag;
        }

        public long getTimeMillis() {
            return this.mTimeMillis;
        }

        public int getFlags() {
            return this.mFlags & -5;
        }

        public String getText(int maxBytes) {
            if ((this.mFlags & 2) == 0) {
                return null;
            }
            byte[] bArr = this.mData;
            if (bArr != null) {
                return new String(bArr, 0, Math.min(maxBytes, bArr.length));
            }
            InputStream is = null;
            try {
                InputStream is2 = getInputStream();
                if (is2 == null) {
                    if (is2 != null) {
                        try {
                            is2.close();
                        } catch (IOException e) {
                        }
                    }
                    return null;
                }
                byte[] buf = new byte[maxBytes];
                int readBytes = 0;
                int n = 0;
                while (n >= 0) {
                    int i = readBytes + n;
                    readBytes = i;
                    if (i >= maxBytes) {
                        break;
                    }
                    n = is2.read(buf, readBytes, maxBytes - readBytes);
                }
                String str = new String(buf, 0, readBytes);
                try {
                    is2.close();
                } catch (IOException e2) {
                }
                return str;
            } catch (IOException e3) {
                if (0 != 0) {
                    try {
                        is.close();
                    } catch (IOException e4) {
                    }
                }
                return null;
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        is.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        }

        public InputStream getInputStream() throws IOException {
            InputStream is;
            byte[] bArr = this.mData;
            if (bArr != null) {
                is = new ByteArrayInputStream(bArr);
            } else {
                ParcelFileDescriptor parcelFileDescriptor = this.mFileDescriptor;
                if (parcelFileDescriptor == null) {
                    return null;
                }
                is = new ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor);
            }
            return (this.mFlags & 4) != 0 ? new GZIPInputStream(is) : is;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return this.mFileDescriptor != null ? 1 : 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(this.mTag);
            out.writeLong(this.mTimeMillis);
            if (this.mFileDescriptor != null) {
                out.writeInt(this.mFlags & -9);
                this.mFileDescriptor.writeToParcel(out, flags);
                return;
            }
            out.writeInt(this.mFlags | 8);
            out.writeByteArray(this.mData);
        }
    }

    public DropBoxManager(Context context, IDropBoxManagerService service) {
        this.mContext = context;
        this.mService = service;
    }

    protected DropBoxManager() {
        this.mContext = null;
        this.mService = null;
    }

    public void addText(String tag, String data) {
        try {
            this.mService.add(new Entry(tag, 0, data));
        } catch (RemoteException e) {
            if (!(e instanceof TransactionTooLargeException) || this.mContext.getApplicationInfo().targetSdkVersion >= 24) {
                throw e.rethrowFromSystemServer();
            }
            Log.e(TAG, "App sent too much data, so it was ignored", e);
        }
    }

    public void addData(String tag, byte[] data, int flags) {
        if (data != null) {
            try {
                this.mService.add(new Entry(tag, 0, data, flags));
            } catch (RemoteException e) {
                if (!(e instanceof TransactionTooLargeException) || this.mContext.getApplicationInfo().targetSdkVersion >= 24) {
                    throw e.rethrowFromSystemServer();
                }
                Log.e(TAG, "App sent too much data, so it was ignored", e);
            }
        } else {
            throw new NullPointerException("data == null");
        }
    }

    public void addFile(String tag, File file, int flags) throws IOException {
        if (file != null) {
            Entry entry = new Entry(tag, 0, file, flags);
            try {
                this.mService.add(entry);
                entry.close();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                entry.close();
                throw th;
            }
        } else {
            throw new NullPointerException("file == null");
        }
    }

    public boolean isTagEnabled(String tag) {
        try {
            return this.mService.isTagEnabled(tag);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Entry getNextEntry(String tag, long msec) {
        try {
            return this.mService.getNextEntry(tag, msec, this.mContext.getOpPackageName());
        } catch (SecurityException e) {
            if (this.mContext.getApplicationInfo().targetSdkVersion < 28) {
                Log.w(TAG, e.getMessage());
                return null;
            }
            throw e;
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }
}
