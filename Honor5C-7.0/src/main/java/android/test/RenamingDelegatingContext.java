package android.test;

import android.content.ContentProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.FileUtils;
import android.util.Log;
import com.google.android.collect.Sets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Set;

@Deprecated
public class RenamingDelegatingContext extends ContextWrapper {
    private File mCacheDir;
    private Set<String> mDatabaseNames;
    private Context mFileContext;
    private Set<String> mFileNames;
    private String mFilePrefix;
    private final Object mSync;

    public static <T extends ContentProvider> T providerWithRenamedContext(Class<T> contentProvider, Context c, String filePrefix) throws IllegalAccessException, InstantiationException {
        return providerWithRenamedContext(contentProvider, c, filePrefix, false);
    }

    public static <T extends ContentProvider> T providerWithRenamedContext(Class<T> contentProvider, Context c, String filePrefix, boolean allowAccessToExistingFilesAndDbs) throws IllegalAccessException, InstantiationException {
        Class<T> mProviderClass = contentProvider;
        ContentProvider mProvider = (ContentProvider) contentProvider.newInstance();
        RenamingDelegatingContext mContext = new RenamingDelegatingContext(c, filePrefix);
        if (allowAccessToExistingFilesAndDbs) {
            mContext.makeExistingFilesAndDbsAccessible();
        }
        mProvider.attachInfoForTesting(mContext, null);
        return mProvider;
    }

    public void makeExistingFilesAndDbsAccessible() {
        String diskName;
        int i = 0;
        for (String diskName2 : this.mFileContext.databaseList()) {
            if (shouldDiskNameBeVisible(diskName2)) {
                this.mDatabaseNames.add(publicNameFromDiskName(diskName2));
            }
        }
        String[] fileList = this.mFileContext.fileList();
        int length = fileList.length;
        while (i < length) {
            diskName2 = fileList[i];
            if (shouldDiskNameBeVisible(diskName2)) {
                this.mFileNames.add(publicNameFromDiskName(diskName2));
            }
            i++;
        }
    }

    boolean shouldDiskNameBeVisible(String diskName) {
        return diskName.startsWith(this.mFilePrefix);
    }

    String publicNameFromDiskName(String diskName) {
        if (shouldDiskNameBeVisible(diskName)) {
            return diskName.substring(this.mFilePrefix.length(), diskName.length());
        }
        throw new IllegalArgumentException("disk file should not be visible: " + diskName);
    }

    public RenamingDelegatingContext(Context context, String filePrefix) {
        super(context);
        this.mFilePrefix = null;
        this.mSync = new Object();
        this.mDatabaseNames = Sets.newHashSet();
        this.mFileNames = Sets.newHashSet();
        this.mFileContext = context;
        this.mFilePrefix = filePrefix;
    }

    public RenamingDelegatingContext(Context context, Context fileContext, String filePrefix) {
        super(context);
        this.mFilePrefix = null;
        this.mSync = new Object();
        this.mDatabaseNames = Sets.newHashSet();
        this.mFileNames = Sets.newHashSet();
        this.mFileContext = fileContext;
        this.mFilePrefix = filePrefix;
    }

    public String getDatabasePrefix() {
        return this.mFilePrefix;
    }

    private String renamedFileName(String name) {
        return this.mFilePrefix + name;
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
        String internalName = renamedFileName(name);
        if (!this.mDatabaseNames.contains(name)) {
            this.mDatabaseNames.add(name);
            this.mFileContext.deleteDatabase(internalName);
        }
        return this.mFileContext.openOrCreateDatabase(internalName, mode, factory);
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
        String internalName = renamedFileName(name);
        if (!this.mDatabaseNames.contains(name)) {
            this.mDatabaseNames.add(name);
            this.mFileContext.deleteDatabase(internalName);
        }
        return this.mFileContext.openOrCreateDatabase(internalName, mode, factory, errorHandler);
    }

    public boolean deleteDatabase(String name) {
        if (!this.mDatabaseNames.contains(name)) {
            return false;
        }
        this.mDatabaseNames.remove(name);
        return this.mFileContext.deleteDatabase(renamedFileName(name));
    }

    public File getDatabasePath(String name) {
        return this.mFileContext.getDatabasePath(renamedFileName(name));
    }

    public String[] databaseList() {
        return (String[]) this.mDatabaseNames.toArray(new String[0]);
    }

    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        String internalName = renamedFileName(name);
        if (this.mFileNames.contains(name)) {
            return this.mFileContext.openFileInput(internalName);
        }
        throw new FileNotFoundException(internalName);
    }

    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        this.mFileNames.add(name);
        return this.mFileContext.openFileOutput(renamedFileName(name), mode);
    }

    public File getFileStreamPath(String name) {
        return this.mFileContext.getFileStreamPath(renamedFileName(name));
    }

    public boolean deleteFile(String name) {
        if (!this.mFileNames.contains(name)) {
            return false;
        }
        this.mFileNames.remove(name);
        return this.mFileContext.deleteFile(renamedFileName(name));
    }

    public String[] fileList() {
        return (String[]) this.mFileNames.toArray(new String[0]);
    }

    public File getCacheDir() {
        synchronized (this.mSync) {
            if (this.mCacheDir == null) {
                this.mCacheDir = new File(this.mFileContext.getCacheDir(), renamedFileName("cache"));
            }
            if (!this.mCacheDir.exists()) {
                if (this.mCacheDir.mkdirs()) {
                    FileUtils.setPermissions(this.mCacheDir.getPath(), 505, -1, -1);
                } else {
                    Log.w("RenamingDelegatingContext", "Unable to create cache directory");
                    return null;
                }
            }
            return this.mCacheDir;
        }
    }
}
