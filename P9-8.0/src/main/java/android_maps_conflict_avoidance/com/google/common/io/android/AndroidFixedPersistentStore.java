package android_maps_conflict_avoidance.com.google.common.io.android;

import android.util.Log;
import android_maps_conflict_avoidance.com.google.common.io.BasePersistentStore;
import android_maps_conflict_avoidance.com.google.common.io.PersistentStore;
import android_maps_conflict_avoidance.com.google.common.io.PersistentStore.PersistentStoreException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AndroidFixedPersistentStore extends BasePersistentStore implements PersistentStore {
    private File baseFile;

    public AndroidFixedPersistentStore(String basePath) {
        this.baseFile = new File(basePath);
        if (!this.baseFile.isDirectory()) {
            throw new IllegalStateException("Directory " + basePath + " must already exist");
        } else if (!this.baseFile.canWrite()) {
            throw new IllegalStateException("Directory " + basePath + " must be writeable");
        } else if (!this.baseFile.canRead()) {
            throw new IllegalStateException("Directory " + basePath + " must be readable");
        }
    }

    private String makeFilename(String s) {
        return getPrefix() + s;
    }

    private File makeFile(String s) {
        return new File(makeFilename(s));
    }

    private String unMakeFilename(String filename) {
        if (filename.startsWith(getPrefix())) {
            return filename.substring(getPrefix().length());
        }
        return null;
    }

    public boolean deleteBlock(String name) {
        return makeFile(name).delete();
    }

    public void deleteAllBlocks(String namePrefix) {
        File[] files = this.baseFile.getAbsoluteFile().listFiles();
        int i = 0;
        while (i < files.length) {
            if (files[i].getAbsolutePath().startsWith(makeFilename(namePrefix)) && !files[i].delete()) {
                Log.w("Fixed_Persistence_Store", "Couldn't delete file: " + files[i].getAbsolutePath());
            }
            i++;
        }
    }

    public int writeBlockX(byte[] data, String name) throws PersistentStoreException {
        try {
            FileOutputStream fos = new FileOutputStream(makeFile(name));
            fos.write(data);
            fos.close();
            return (((data.length - 1) / 4096) + 1) * 4096;
        } catch (FileNotFoundException e) {
            throw new PersistentStoreException(e.getMessage(), -1);
        } catch (IOException e2) {
            throw new PersistentStoreException(e2.getMessage(), -1);
        }
    }

    public int writeBlock(byte[] data, String name) {
        try {
            FileOutputStream fos = new FileOutputStream(makeFile(name));
            fos.write(data);
            fos.close();
            return (((data.length - 1) / 4096) + 1) * 4096;
        } catch (FileNotFoundException e) {
            Log.w("Fixed_Persistence_Store", "Couldn't write block:  " + e.getMessage());
            return -1;
        } catch (IOException e2) {
            Log.w("Fixed_Persistence_Store", "Couldn't write block:  " + e2.getMessage());
            return -1;
        }
    }

    public byte[] readBlock(String name) {
        try {
            FileInputStream fis = new FileInputStream(makeFile(name));
            int length = fis.available();
            byte[] data = new byte[length];
            if (fis.read(data, 0, length) < length) {
                Log.w("Fixed_Persistence_Store", "Didn't read full file:  " + name);
            }
            fis.close();
            return data;
        } catch (FileNotFoundException e) {
            Log.w("Fixed_Persistence_Store", "Couldn't find file:  " + e.getMessage());
            return null;
        } catch (IOException e2) {
            Log.w("Fixed_Persistence_Store", "Couldn't read file:  " + e2.getMessage());
            return null;
        }
    }

    public String[] listBlocks(String namePrefix) {
        File[] files = this.baseFile.getAbsoluteFile().listFiles();
        String[] temp = new String[files.length];
        int j = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getAbsolutePath().startsWith(makeFilename(namePrefix))) {
                String realName = unMakeFilename(files[i].getAbsolutePath());
                if (realName != null) {
                    int j2 = j + 1;
                    temp[j] = realName;
                    j = j2;
                }
            }
        }
        String[] names = new String[j];
        System.arraycopy(temp, 0, names, 0, j);
        return names;
    }

    protected String getPrefix() {
        return new File(this.baseFile, "FIXED_DATA_").getAbsolutePath();
    }
}
