package android.test;

import android.content.ContentProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ProviderInfo;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.test.mock.MockContentProvider;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Deprecated
public class RenamingDelegatingContext extends ContextWrapper {
    private File mCacheDir;
    private Set<String> mDatabaseNames = new HashSet();
    private Context mFileContext;
    private Set<String> mFileNames = new HashSet();
    private String mFilePrefix = null;
    private final Object mSync = new Object();

    public static <T extends ContentProvider> T providerWithRenamedContext(Class<T> contentProvider, Context c, String filePrefix) throws IllegalAccessException, InstantiationException {
        return (T) providerWithRenamedContext(contentProvider, c, filePrefix, false);
    }

    public static <T extends ContentProvider> T providerWithRenamedContext(Class<T> contentProvider, Context c, String filePrefix, boolean allowAccessToExistingFilesAndDbs) throws IllegalAccessException, InstantiationException {
        T mProvider = contentProvider.newInstance();
        RenamingDelegatingContext mContext = new RenamingDelegatingContext(c, filePrefix);
        if (allowAccessToExistingFilesAndDbs) {
            mContext.makeExistingFilesAndDbsAccessible();
        }
        MockContentProvider.attachInfoForTesting(mProvider, mContext, (ProviderInfo) null);
        return mProvider;
    }

    public void makeExistingFilesAndDbsAccessible() {
        String[] databaseList = this.mFileContext.databaseList();
        for (String diskName : databaseList) {
            if (shouldDiskNameBeVisible(diskName)) {
                this.mDatabaseNames.add(publicNameFromDiskName(diskName));
            }
        }
        String[] fileList = this.mFileContext.fileList();
        for (String diskName2 : fileList) {
            if (shouldDiskNameBeVisible(diskName2)) {
                this.mFileNames.add(publicNameFromDiskName(diskName2));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shouldDiskNameBeVisible(String diskName) {
        return diskName.startsWith(this.mFilePrefix);
    }

    /* access modifiers changed from: package-private */
    public String publicNameFromDiskName(String diskName) {
        if (shouldDiskNameBeVisible(diskName)) {
            return diskName.substring(this.mFilePrefix.length(), diskName.length());
        }
        throw new IllegalArgumentException("disk file should not be visible: " + diskName);
    }

    public RenamingDelegatingContext(Context context, String filePrefix) {
        super(context);
        this.mFileContext = context;
        this.mFilePrefix = filePrefix;
    }

    public RenamingDelegatingContext(Context context, Context fileContext, String filePrefix) {
        super(context);
        this.mFileContext = fileContext;
        this.mFilePrefix = filePrefix;
    }

    public String getDatabasePrefix() {
        return this.mFilePrefix;
    }

    private String renamedFileName(String name) {
        return this.mFilePrefix + name;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        String internalName = renamedFileName(name);
        if (!this.mDatabaseNames.contains(name)) {
            this.mDatabaseNames.add(name);
            this.mFileContext.deleteDatabase(internalName);
        }
        return this.mFileContext.openOrCreateDatabase(internalName, mode, factory);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        String internalName = renamedFileName(name);
        if (!this.mDatabaseNames.contains(name)) {
            this.mDatabaseNames.add(name);
            this.mFileContext.deleteDatabase(internalName);
        }
        return this.mFileContext.openOrCreateDatabase(internalName, mode, factory, errorHandler);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public boolean deleteDatabase(String name) {
        if (!this.mDatabaseNames.contains(name)) {
            return false;
        }
        this.mDatabaseNames.remove(name);
        return this.mFileContext.deleteDatabase(renamedFileName(name));
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public File getDatabasePath(String name) {
        return this.mFileContext.getDatabasePath(renamedFileName(name));
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public String[] databaseList() {
        return (String[]) this.mDatabaseNames.toArray(new String[0]);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        String internalName = renamedFileName(name);
        if (this.mFileNames.contains(name)) {
            return this.mFileContext.openFileInput(internalName);
        }
        throw new FileNotFoundException(internalName);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        this.mFileNames.add(name);
        return this.mFileContext.openFileOutput(renamedFileName(name), mode);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public File getFileStreamPath(String name) {
        return this.mFileContext.getFileStreamPath(renamedFileName(name));
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public boolean deleteFile(String name) {
        if (!this.mFileNames.contains(name)) {
            return false;
        }
        this.mFileNames.remove(name);
        return this.mFileContext.deleteFile(renamedFileName(name));
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public String[] fileList() {
        return (String[]) this.mFileNames.toArray(new String[0]);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public File getCacheDir() {
        synchronized (this.mSync) {
            if (this.mCacheDir == null) {
                this.mCacheDir = new File(this.mFileContext.getCacheDir(), renamedFileName("cache"));
            }
            if (!this.mCacheDir.exists()) {
                if (!this.mCacheDir.mkdirs()) {
                    Log.w("RenamingDelegatingContext", "Unable to create cache directory");
                    return null;
                }
                try {
                    Files.setPosixFilePermissions(this.mCacheDir.toPath(), EnumSet.allOf(PosixFilePermission.class));
                } catch (IOException e) {
                    Log.e("RenamingDelegatingContext", "Could not set permissions of test cacheDir", e);
                }
            }
            return this.mCacheDir;
        }
    }
}
