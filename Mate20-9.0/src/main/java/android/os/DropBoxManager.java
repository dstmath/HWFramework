package android.os;

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
    public static final String EXTRA_TAG = "tag";
    public static final String EXTRA_TIME = "time";
    private static final int HAS_BYTE_ARRAY = 8;
    public static final int IS_EMPTY = 1;
    public static final int IS_GZIPPED = 4;
    public static final int IS_TEXT = 2;
    private static final String TAG = "DropBoxManager";
    private final Context mContext;
    private final IDropBoxManagerService mService;

    public static class Entry implements Parcelable, Closeable {
        public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator() {
            public Entry[] newArray(int size) {
                return new Entry[size];
            }

            public Entry createFromParcel(Parcel in) {
                String tag = in.readString();
                long millis = in.readLong();
                int flags = in.readInt();
                if ((flags & 8) != 0) {
                    Entry entry = new Entry(tag, millis, in.createByteArray(), flags & -9);
                    return entry;
                }
                Entry entry2 = new Entry(tag, millis, ParcelFileDescriptor.CREATOR.createFromParcel(in), flags);
                return entry2;
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
                boolean z = false;
                if (((flags & 1) != 0) == (data == null ? true : z)) {
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
                boolean z = false;
                if (((flags & 1) != 0) == (data == null ? true : z)) {
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
            if (this.mData != null) {
                return new String(this.mData, 0, Math.min(maxBytes, this.mData.length));
            }
            InputStream is = null;
            try {
                is = getInputStream();
                if (is == null) {
                    if (is != null) {
                        try {
                            is.close();
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
                    n = is.read(buf, readBytes, maxBytes - readBytes);
                }
                String str = new String(buf, 0, readBytes);
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e2) {
                    }
                }
                return str;
            } catch (IOException e3) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e4) {
                    }
                }
                return null;
            } catch (Throwable th) {
                if (is != null) {
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
            if (this.mData != null) {
                is = new ByteArrayInputStream(this.mData);
            } else if (this.mFileDescriptor == null) {
                return null;
            } else {
                is = new ParcelFileDescriptor.AutoCloseInputStream(this.mFileDescriptor);
            }
            return (this.mFlags & 4) != 0 ? new GZIPInputStream(is) : is;
        }

        public int describeContents() {
            return this.mFileDescriptor != null ? 1 : 0;
        }

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
            if (this.mService != null) {
                this.mService.add(new Entry(tag, 0, data));
            }
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
                IDropBoxManagerService iDropBoxManagerService = this.mService;
                Entry entry = new Entry(tag, 0, data, flags);
                iDropBoxManagerService.add(entry);
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
            Entry entry2 = entry;
            try {
                this.mService.add(entry2);
                entry2.close();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                entry2.close();
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
            return this.mService.getNextEntry(tag, msec);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
