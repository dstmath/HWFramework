package android.media;

import android.app.backup.FullBackup;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Hashtable;

public class MiniThumbFile {
    public static final int BYTES_PER_MINTHUMB = 10000;
    private static final int HEADER_SIZE = 13;
    private static final int MINI_THUMB_DATA_FILE_VERSION = 4;
    private static final String TAG = "MiniThumbFile";
    private static final Hashtable<String, MiniThumbFile> sThumbFiles = null;
    private long MAX_THUMB_COUNT;
    private ByteBuffer mBuffer;
    private final HashMap<Long, FileChannel> mChannels;
    private final HashMap<Long, RandomAccessFile> mMiniThumbFiles;
    private Uri mUri;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MiniThumbFile.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MiniThumbFile.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.<clinit>():void");
    }

    public static synchronized void reset() {
        synchronized (MiniThumbFile.class) {
            for (MiniThumbFile file : sThumbFiles.values()) {
                file.deactivate();
            }
            sThumbFiles.clear();
        }
    }

    public static synchronized MiniThumbFile instance(Uri uri) {
        MiniThumbFile file;
        synchronized (MiniThumbFile.class) {
            String type = (String) uri.getPathSegments().get(1);
            file = (MiniThumbFile) sThumbFiles.get(type);
            if (file == null) {
                file = new MiniThumbFile(Uri.parse("content://media/external/" + type + "/media"));
                sThumbFiles.put(type, file);
            }
        }
        return file;
    }

    private String randomAccessFilePath(int version) {
        return (Environment.getExternalStorageDirectory().toString() + "/DCIM/.thumbnails") + "/.thumbdata" + version + "-" + this.mUri.hashCode();
    }

    private void removeOldFile() {
        File oldFile = new File(randomAccessFilePath(3));
        if (oldFile.exists()) {
            try {
                oldFile.delete();
            } catch (SecurityException e) {
            }
        }
    }

    private RandomAccessFile miniThumbDataFile(long id) {
        long fileindex = getFileIndex(id);
        RandomAccessFile miniThumbFile = (RandomAccessFile) this.mMiniThumbFiles.get(Long.valueOf(fileindex));
        if (miniThumbFile == null) {
            removeOldFile();
            String path = randomAccessFilePath(MINI_THUMB_DATA_FILE_VERSION, id);
            File directory = new File(path).getParentFile();
            if (!(directory.isDirectory() || directory.mkdirs())) {
                Log.e(TAG, "Unable to create .thumbnails directory " + directory.toString());
            }
            File f = new File(path);
            try {
                miniThumbFile = new RandomAccessFile(f, "rw");
            } catch (IOException e) {
                try {
                    miniThumbFile = new RandomAccessFile(f, FullBackup.ROOT_TREE_TOKEN);
                } catch (IOException e2) {
                }
            }
            if (miniThumbFile != null) {
                FileChannel channel = miniThumbFile.getChannel();
                this.mMiniThumbFiles.put(Long.valueOf(fileindex), miniThumbFile);
                this.mChannels.put(Long.valueOf(fileindex), channel);
            }
        }
        return miniThumbFile;
    }

    public MiniThumbFile(Uri uri) {
        this.mMiniThumbFiles = new HashMap();
        this.mChannels = new HashMap();
        this.MAX_THUMB_COUNT = 5000;
        this.mUri = uri;
        this.mBuffer = ByteBuffer.allocateDirect(BYTES_PER_MINTHUMB);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void deactivate() {
        for (Long key : this.mMiniThumbFiles.keySet()) {
            RandomAccessFile miniThumbFile = (RandomAccessFile) this.mMiniThumbFiles.get(key);
            FileChannel channel = (FileChannel) this.mChannels.get(key);
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ex) {
                    Log.e(TAG, "deactivate: IOException!", ex);
                }
            }
            if (miniThumbFile != null) {
                miniThumbFile.close();
            }
        }
        this.mMiniThumbFiles.clear();
        this.mChannels.clear();
    }

    public synchronized long getMagic(long id) {
        if (miniThumbDataFile(id) != null) {
            long pos = getFilePos(id);
            FileLock fileLock = null;
            try {
                this.mBuffer.clear();
                this.mBuffer.limit(9);
                FileChannel channel = getFileChannel(id);
                fileLock = channel.lock(pos, 9, true);
                if (channel.read(this.mBuffer, pos) == 9) {
                    this.mBuffer.position(0);
                    if (this.mBuffer.get() == 1) {
                        long j = this.mBuffer.getLong();
                        if (fileLock != null) {
                            try {
                                fileLock.release();
                            } catch (IOException e) {
                            }
                        }
                        return j;
                    }
                }
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e2) {
                    }
                }
            } catch (IOException ex) {
                Log.v(TAG, "Got exception checking file magic: ", ex);
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e3) {
                    }
                }
            } catch (RuntimeException ex2) {
                Log.e(TAG, "Got exception when reading magic, id = " + id + ", disk full or mount read-only? " + ex2.getClass());
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e4) {
                    }
                }
            } catch (Throwable th) {
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e5) {
                    }
                }
            }
        }
        return 0;
    }

    public synchronized void saveMiniThumbToFile(byte[] data, long id, long magic) throws IOException {
        if (miniThumbDataFile(id) != null) {
            long pos = getFilePos(id);
            FileLock fileLock = null;
            if (data != null) {
                try {
                    if (data.length <= GLES20.GL_LINEAR_MIPMAP_LINEAR) {
                        this.mBuffer.clear();
                        this.mBuffer.put((byte) 1);
                        this.mBuffer.putLong(magic);
                        this.mBuffer.putInt(data.length);
                        this.mBuffer.put(data);
                        this.mBuffer.flip();
                        FileChannel channel = getFileChannel(id);
                        fileLock = channel.lock(pos, 10000, false);
                        channel.write(this.mBuffer, pos);
                    } else {
                        return;
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "couldn't save mini thumbnail data for " + id + "; ", ex);
                    throw ex;
                } catch (RuntimeException ex2) {
                    Log.e(TAG, "couldn't save mini thumbnail data for " + id + "; disk full or mount read-only? " + ex2.getClass());
                    if (fileLock != null) {
                        try {
                            fileLock.release();
                        } catch (IOException e) {
                        }
                    }
                } catch (Throwable th) {
                    if (fileLock != null) {
                        try {
                            fileLock.release();
                        } catch (IOException e2) {
                        }
                    }
                }
            }
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e3) {
                }
            }
        }
    }

    public synchronized byte[] getMiniThumbFromFile(long id, byte[] data) {
        if (miniThumbDataFile(id) == null) {
            return null;
        }
        long pos = getFilePos(id);
        FileLock fileLock = null;
        try {
            this.mBuffer.clear();
            FileChannel channel = getFileChannel(id);
            fileLock = channel.lock(pos, 10000, true);
            int size = channel.read(this.mBuffer, pos);
            if (size > HEADER_SIZE) {
                this.mBuffer.position(0);
                byte flag = this.mBuffer.get();
                long magic = this.mBuffer.getLong();
                int length = this.mBuffer.getInt();
                if (size >= length + HEADER_SIZE && length != 0 && magic != 0 && flag == 1 && data.length >= length) {
                    this.mBuffer.get(data, 0, length);
                    if (fileLock != null) {
                        try {
                            fileLock.release();
                        } catch (IOException e) {
                        }
                    }
                    return data;
                }
            }
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e2) {
                }
            }
        } catch (IOException ex) {
            Log.w(TAG, "got exception when reading thumbnail id=" + id + ", exception: " + ex);
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e3) {
                }
            }
        } catch (RuntimeException ex2) {
            Log.e(TAG, "Got exception when reading thumbnail, id = " + id + ", disk full or mount read-only? " + ex2.getClass());
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e4) {
                }
            }
        } catch (Throwable th) {
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e5) {
                }
            }
        }
        return null;
    }

    private long getFileIndex(long id) {
        return id / this.MAX_THUMB_COUNT;
    }

    private FileChannel getFileChannel(long id) {
        return (FileChannel) this.mChannels.get(Long.valueOf(getFileIndex(id)));
    }

    private long getFilePos(long id) {
        return (id % this.MAX_THUMB_COUNT) * 10000;
    }

    private String randomAccessFilePath(int version, long id) {
        return (Environment.getExternalStorageDirectory().toString() + "/DCIM/.thumbnails") + "/.thumbdata" + version + "-" + this.mUri.hashCode() + "_" + getFileIndex(id);
    }
}
