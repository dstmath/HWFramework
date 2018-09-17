package android.os;

import android.content.Context;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.Parcelable.Creator;
import android.security.keymaster.KeymasterDefs;
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
        public static final Creator<Entry> CREATOR = null;
        private final byte[] mData;
        private final ParcelFileDescriptor mFileDescriptor;
        private final int mFlags;
        private final String mTag;
        private final long mTimeMillis;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.DropBoxManager.Entry.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.DropBoxManager.Entry.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.DropBoxManager.Entry.<clinit>():void");
        }

        public Entry(String tag, long millis) {
            if (tag == null) {
                throw new NullPointerException("tag == null");
            }
            this.mTag = tag;
            this.mTimeMillis = millis;
            this.mData = null;
            this.mFileDescriptor = null;
            this.mFlags = DropBoxManager.IS_EMPTY;
        }

        public Entry(String tag, long millis, String text) {
            if (tag == null) {
                throw new NullPointerException("tag == null");
            } else if (text == null) {
                throw new NullPointerException("text == null");
            } else {
                this.mTag = tag;
                this.mTimeMillis = millis;
                this.mData = text.getBytes();
                this.mFileDescriptor = null;
                this.mFlags = DropBoxManager.IS_TEXT;
            }
        }

        public Entry(String tag, long millis, byte[] data, int flags) {
            Object obj = DropBoxManager.IS_EMPTY;
            if (tag == null) {
                throw new NullPointerException("tag == null");
            }
            Object obj2;
            if ((flags & DropBoxManager.IS_EMPTY) != 0) {
                obj2 = DropBoxManager.IS_EMPTY;
            } else {
                obj2 = null;
            }
            if (data != null) {
                obj = null;
            }
            if (obj2 != obj) {
                throw new IllegalArgumentException("Bad flags: " + flags);
            }
            this.mTag = tag;
            this.mTimeMillis = millis;
            this.mData = data;
            this.mFileDescriptor = null;
            this.mFlags = flags;
        }

        public Entry(String tag, long millis, ParcelFileDescriptor data, int flags) {
            Object obj = DropBoxManager.IS_EMPTY;
            if (tag == null) {
                throw new NullPointerException("tag == null");
            }
            Object obj2;
            if ((flags & DropBoxManager.IS_EMPTY) != 0) {
                obj2 = DropBoxManager.IS_EMPTY;
            } else {
                obj2 = null;
            }
            if (data != null) {
                obj = null;
            }
            if (obj2 != obj) {
                throw new IllegalArgumentException("Bad flags: " + flags);
            }
            this.mTag = tag;
            this.mTimeMillis = millis;
            this.mData = null;
            this.mFileDescriptor = data;
            this.mFlags = flags;
        }

        public Entry(String tag, long millis, File data, int flags) throws IOException {
            if (tag == null) {
                throw new NullPointerException("tag == null");
            } else if ((flags & DropBoxManager.IS_EMPTY) != 0) {
                throw new IllegalArgumentException("Bad flags: " + flags);
            } else {
                this.mTag = tag;
                this.mTimeMillis = millis;
                this.mData = null;
                this.mFileDescriptor = ParcelFileDescriptor.open(data, KeymasterDefs.KM_ENUM);
                this.mFlags = flags;
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
            if ((this.mFlags & DropBoxManager.IS_TEXT) == 0) {
                return null;
            }
            if (this.mData != null) {
                return new String(this.mData, 0, Math.min(maxBytes, this.mData.length));
            }
            InputStream inputStream = null;
            try {
                inputStream = getInputStream();
                if (inputStream == null) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                        }
                    }
                    return null;
                }
                byte[] buf = new byte[maxBytes];
                int readBytes = 0;
                int n = 0;
                while (n >= 0) {
                    readBytes += n;
                    if (readBytes >= maxBytes) {
                        break;
                    }
                    n = inputStream.read(buf, readBytes, maxBytes - readBytes);
                }
                String str = new String(buf, 0, readBytes);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                    }
                }
                return str;
            } catch (IOException e3) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                    }
                }
                return null;
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                    }
                }
            }
        }

        public InputStream getInputStream() throws IOException {
            InputStream is;
            if (this.mData != null) {
                is = new ByteArrayInputStream(this.mData);
            } else if (this.mFileDescriptor == null) {
                return null;
            } else {
                is = new AutoCloseInputStream(this.mFileDescriptor);
            }
            if ((this.mFlags & DropBoxManager.IS_GZIPPED) != 0) {
                is = new GZIPInputStream(is);
            }
            return is;
        }

        public int describeContents() {
            return this.mFileDescriptor != null ? DropBoxManager.IS_EMPTY : 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(this.mTag);
            out.writeLong(this.mTimeMillis);
            if (this.mFileDescriptor != null) {
                out.writeInt(this.mFlags & -9);
                this.mFileDescriptor.writeToParcel(out, flags);
                return;
            }
            out.writeInt(this.mFlags | DropBoxManager.HAS_BYTE_ARRAY);
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
        if (data == null) {
            throw new NullPointerException("data == null");
        }
        try {
            this.mService.add(new Entry(tag, 0, data, flags));
        } catch (RemoteException e) {
            if (!(e instanceof TransactionTooLargeException) || this.mContext.getApplicationInfo().targetSdkVersion >= 24) {
                throw e.rethrowFromSystemServer();
            }
            Log.e(TAG, "App sent too much data, so it was ignored", e);
        }
    }

    public void addFile(String tag, File file, int flags) throws IOException {
        if (file == null) {
            throw new NullPointerException("file == null");
        }
        Entry entry = new Entry(tag, 0, file, flags);
        try {
            this.mService.add(entry);
            entry.close();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Throwable th) {
            entry.close();
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
