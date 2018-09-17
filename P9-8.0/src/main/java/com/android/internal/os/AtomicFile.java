package com.android.internal.os;

import android.os.FileUtils;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class AtomicFile {
    private final File mBackupName;
    private final File mBaseName;

    public AtomicFile(File baseName) {
        this.mBaseName = baseName;
        this.mBackupName = new File(baseName.getPath() + ".bak");
    }

    public File getBaseFile() {
        return this.mBaseName;
    }

    public FileOutputStream startWrite() throws IOException {
        if (this.mBaseName.exists()) {
            if (this.mBackupName.exists()) {
                this.mBaseName.delete();
            } else if (!this.mBaseName.renameTo(this.mBackupName)) {
                Log.w("AtomicFile", "Couldn't rename file " + this.mBaseName + " to backup file " + this.mBackupName);
            }
        }
        try {
            return new FileOutputStream(this.mBaseName);
        } catch (FileNotFoundException e) {
            File parent = this.mBaseName.getParentFile();
            if (parent.mkdir()) {
                FileUtils.setPermissions(parent.getPath(), 505, -1, -1);
                try {
                    return new FileOutputStream(this.mBaseName);
                } catch (FileNotFoundException e2) {
                    throw new IOException("Couldn't create " + this.mBaseName);
                }
            }
            throw new IOException("Couldn't create directory " + this.mBaseName);
        }
    }

    public void finishWrite(FileOutputStream str) {
        if (str != null) {
            FileUtils.sync(str);
            try {
                str.close();
                this.mBackupName.delete();
            } catch (IOException e) {
                Log.w("AtomicFile", "finishWrite: Got exception:", e);
            }
        }
    }

    public void failWrite(FileOutputStream str) {
        if (str != null) {
            FileUtils.sync(str);
            try {
                str.close();
                this.mBaseName.delete();
                this.mBackupName.renameTo(this.mBaseName);
            } catch (IOException e) {
                Log.w("AtomicFile", "failWrite: Got exception:", e);
            }
        }
    }

    public FileOutputStream openAppend() throws IOException {
        try {
            return new FileOutputStream(this.mBaseName, true);
        } catch (FileNotFoundException e) {
            throw new IOException("Couldn't append " + this.mBaseName);
        }
    }

    public void truncate() throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(this.mBaseName);
            FileUtils.sync(fos);
            fos.close();
        } catch (FileNotFoundException e) {
            throw new IOException("Couldn't append " + this.mBaseName);
        } catch (IOException e2) {
        }
    }

    public boolean exists() {
        return !this.mBaseName.exists() ? this.mBackupName.exists() : true;
    }

    public void delete() {
        this.mBaseName.delete();
        this.mBackupName.delete();
    }

    public FileInputStream openRead() throws FileNotFoundException {
        if (this.mBackupName.exists()) {
            this.mBaseName.delete();
            this.mBackupName.renameTo(this.mBaseName);
        }
        return new FileInputStream(this.mBaseName);
    }

    public byte[] readFully() throws IOException {
        FileInputStream stream = openRead();
        int pos = 0;
        try {
            byte[] data = new byte[stream.available()];
            while (true) {
                int amt = stream.read(data, pos, data.length - pos);
                if (amt <= 0) {
                    break;
                }
                pos += amt;
                int avail = stream.available();
                if (avail > data.length - pos) {
                    byte[] newData = new byte[(pos + avail)];
                    System.arraycopy(data, 0, newData, 0, pos);
                    data = newData;
                }
            }
            return data;
        } finally {
            stream.close();
        }
    }
}
