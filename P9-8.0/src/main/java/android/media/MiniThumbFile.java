package android.media;

import android.app.backup.FullBackup;
import android.app.job.JobInfo;
import android.net.Uri;
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
    private static final Hashtable<String, MiniThumbFile> sThumbFiles = new Hashtable();
    private long MAX_THUMB_COUNT = 5000;
    private ByteBuffer mBuffer;
    private final HashMap<Long, FileChannel> mChannels = new HashMap();
    private ByteBuffer mEmptyBuffer;
    private final HashMap<Long, RandomAccessFile> mMiniThumbFiles = new HashMap();
    private Uri mUri;

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
            String path = randomAccessFilePath(4, id);
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

    private MiniThumbFile(Uri uri) {
        this.mUri = uri;
        this.mBuffer = ByteBuffer.allocateDirect(10000);
        this.mEmptyBuffer = ByteBuffer.allocateDirect(10000);
    }

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
        long j;
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
                    if (this.mBuffer.get() == (byte) 1) {
                        j = this.mBuffer.getLong();
                        if (fileLock != null) {
                            try {
                                fileLock.release();
                            } catch (IOException e) {
                            }
                        }
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
        return j;
    }

    public synchronized void eraseMiniThumb(long id) {
        if (miniThumbDataFile(id) != null) {
            long pos = id * JobInfo.MIN_BACKOFF_MILLIS;
            FileLock fileLock = null;
            try {
                this.mBuffer.clear();
                this.mBuffer.limit(9);
                FileChannel channel = getFileChannel(id);
                fileLock = channel.lock(pos, JobInfo.MIN_BACKOFF_MILLIS, false);
                if (channel.read(this.mBuffer, pos) == 9) {
                    this.mBuffer.position(0);
                    if (this.mBuffer.get() == (byte) 1) {
                        if (this.mBuffer.getLong() == 0) {
                            Log.i(TAG, "no thumbnail for id " + id);
                            if (fileLock != null) {
                                try {
                                    fileLock.release();
                                } catch (IOException e) {
                                }
                            }
                        } else {
                            channel.write(this.mEmptyBuffer, pos);
                        }
                    }
                }
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e2) {
                    }
                }
            } catch (IOException e3) {
                Log.v(TAG, "Got exception checking file magic");
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e4) {
                    }
                }
            } catch (RuntimeException e5) {
                Log.e(TAG, "Got exception when reading magic, id = " + id + ", disk full or mount read-only? ");
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e6) {
                    }
                }
            } catch (Throwable th) {
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e7) {
                    }
                }
            }
        }
        return;
        return;
    }

    public synchronized void saveMiniThumbToFile(byte[] data, long id, long magic) throws IOException {
        if (miniThumbDataFile(id) != null) {
            long pos = getFilePos(id);
            FileLock fileLock = null;
            if (data != null) {
                try {
                    if (data.length <= 9987) {
                        this.mBuffer.clear();
                        this.mBuffer.put((byte) 1);
                        this.mBuffer.putLong(magic);
                        this.mBuffer.putInt(data.length);
                        this.mBuffer.put(data);
                        this.mBuffer.flip();
                        FileChannel channel = getFileChannel(id);
                        fileLock = channel.lock(pos, JobInfo.MIN_BACKOFF_MILLIS, false);
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
        } else {
            return;
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
            fileLock = channel.lock(pos, JobInfo.MIN_BACKOFF_MILLIS, true);
            int size = channel.read(this.mBuffer, pos);
            if (size > 13) {
                this.mBuffer.position(0);
                byte flag = this.mBuffer.get();
                long magic = this.mBuffer.getLong();
                int length = this.mBuffer.getInt();
                if (size >= length + 13 && length != 0 && magic != 0 && flag == (byte) 1 && data.length >= length) {
                    this.mBuffer.get(data, 0, length);
                    if (fileLock != null) {
                        try {
                            fileLock.release();
                        } catch (IOException e) {
                        }
                    }
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
        return data;
    }

    private long getFileIndex(long id) {
        return id / this.MAX_THUMB_COUNT;
    }

    private FileChannel getFileChannel(long id) {
        return (FileChannel) this.mChannels.get(Long.valueOf(getFileIndex(id)));
    }

    private long getFilePos(long id) {
        return (id % this.MAX_THUMB_COUNT) * JobInfo.MIN_BACKOFF_MILLIS;
    }

    private String randomAccessFilePath(int version, long id) {
        return (Environment.getExternalStorageDirectory().toString() + "/DCIM/.thumbnails") + "/.thumbdata" + version + "-" + this.mUri.hashCode() + "_" + getFileIndex(id);
    }
}
