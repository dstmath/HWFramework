package android.support.v4.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParserException;

public class FileProvider extends ContentProvider {
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PATH = "path";
    private static final String[] COLUMNS = {"_display_name", "_size"};
    private static final File DEVICE_ROOT = new File("/");
    private static final String META_DATA_FILE_PROVIDER_PATHS = "android.support.FILE_PROVIDER_PATHS";
    private static final String TAG_CACHE_PATH = "cache-path";
    private static final String TAG_EXTERNAL = "external-path";
    private static final String TAG_EXTERNAL_CACHE = "external-cache-path";
    private static final String TAG_EXTERNAL_FILES = "external-files-path";
    private static final String TAG_EXTERNAL_MEDIA = "external-media-path";
    private static final String TAG_FILES_PATH = "files-path";
    private static final String TAG_ROOT_PATH = "root-path";
    @GuardedBy("sCache")
    private static HashMap<String, PathStrategy> sCache = new HashMap<>();
    private PathStrategy mStrategy;

    /* access modifiers changed from: package-private */
    public interface PathStrategy {
        File getFileForUri(Uri uri);

        Uri getUriForFile(File file);
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return true;
    }

    @Override // android.content.ContentProvider
    public void attachInfo(@NonNull Context context, @NonNull ProviderInfo info) {
        super.attachInfo(context, info);
        if (info.exported) {
            throw new SecurityException("Provider must not be exported");
        } else if (info.grantUriPermissions) {
            this.mStrategy = getPathStrategy(context, info.authority);
        } else {
            throw new SecurityException("Provider must grant uri permissions");
        }
    }

    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        return getPathStrategy(context, authority).getUriForFile(file);
    }

    @Override // android.content.ContentProvider
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int i;
        File file = this.mStrategy.getFileForUri(uri);
        if (projection == null) {
            projection = COLUMNS;
        }
        String[] cols = new String[projection.length];
        Object[] values = new Object[projection.length];
        int i2 = 0;
        for (String col : projection) {
            if ("_display_name".equals(col)) {
                cols[i2] = "_display_name";
                i = i2 + 1;
                values[i2] = file.getName();
            } else if ("_size".equals(col)) {
                cols[i2] = "_size";
                i = i2 + 1;
                values[i2] = Long.valueOf(file.length());
            }
            i2 = i;
        }
        String[] cols2 = copyOf(cols, i2);
        Object[] values2 = copyOf(values, i2);
        MatrixCursor cursor = new MatrixCursor(cols2, 1);
        cursor.addRow(values2);
        return cursor;
    }

    @Override // android.content.ContentProvider
    public String getType(@NonNull Uri uri) {
        String mime;
        File file = this.mStrategy.getFileForUri(uri);
        int lastDot = file.getName().lastIndexOf(46);
        if (lastDot < 0 || (mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.getName().substring(lastDot + 1))) == null) {
            return "application/octet-stream";
        }
        return mime;
    }

    @Override // android.content.ContentProvider
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("No external inserts");
    }

    @Override // android.content.ContentProvider
    public int update(@NonNull Uri uri, ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("No external updates");
    }

    @Override // android.content.ContentProvider
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return this.mStrategy.getFileForUri(uri).delete() ? 1 : 0;
    }

    @Override // android.content.ContentProvider
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        return ParcelFileDescriptor.open(this.mStrategy.getFileForUri(uri), modeToMode(mode));
    }

    private static PathStrategy getPathStrategy(Context context, String authority) {
        PathStrategy strat;
        synchronized (sCache) {
            strat = sCache.get(authority);
            if (strat == null) {
                try {
                    strat = parsePathStrategy(context, authority);
                    sCache.put(authority, strat);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Failed to parse android.support.FILE_PROVIDER_PATHS meta-data", e);
                } catch (XmlPullParserException e2) {
                    throw new IllegalArgumentException("Failed to parse android.support.FILE_PROVIDER_PATHS meta-data", e2);
                }
            }
        }
        return strat;
    }

    private static PathStrategy parsePathStrategy(Context context, String authority) throws IOException, XmlPullParserException {
        SimplePathStrategy strat = new SimplePathStrategy(authority);
        XmlResourceParser in = context.getPackageManager().resolveContentProvider(authority, 128).loadXmlMetaData(context.getPackageManager(), META_DATA_FILE_PROVIDER_PATHS);
        if (in != null) {
            while (true) {
                int type = in.next();
                if (type == 1) {
                    return strat;
                }
                if (type == 2) {
                    String tag = in.getName();
                    String name = in.getAttributeValue(null, ATTR_NAME);
                    String path = in.getAttributeValue(null, ATTR_PATH);
                    File target = null;
                    if (TAG_ROOT_PATH.equals(tag)) {
                        target = DEVICE_ROOT;
                    } else if (TAG_FILES_PATH.equals(tag)) {
                        target = context.getFilesDir();
                    } else if (TAG_CACHE_PATH.equals(tag)) {
                        target = context.getCacheDir();
                    } else if (TAG_EXTERNAL.equals(tag)) {
                        target = Environment.getExternalStorageDirectory();
                    } else if (TAG_EXTERNAL_FILES.equals(tag)) {
                        File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(context, null);
                        if (externalFilesDirs.length > 0) {
                            target = externalFilesDirs[0];
                        }
                    } else if (TAG_EXTERNAL_CACHE.equals(tag)) {
                        File[] externalCacheDirs = ContextCompat.getExternalCacheDirs(context);
                        if (externalCacheDirs.length > 0) {
                            target = externalCacheDirs[0];
                        }
                    } else if (Build.VERSION.SDK_INT >= 21 && TAG_EXTERNAL_MEDIA.equals(tag)) {
                        File[] externalMediaDirs = context.getExternalMediaDirs();
                        if (externalMediaDirs.length > 0) {
                            target = externalMediaDirs[0];
                        }
                    }
                    if (target != null) {
                        strat.addRoot(name, buildPath(target, path));
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Missing android.support.FILE_PROVIDER_PATHS meta-data");
        }
    }

    /* access modifiers changed from: package-private */
    public static class SimplePathStrategy implements PathStrategy {
        private final String mAuthority;
        private final HashMap<String, File> mRoots = new HashMap<>();

        SimplePathStrategy(String authority) {
            this.mAuthority = authority;
        }

        /* access modifiers changed from: package-private */
        public void addRoot(String name, File root) {
            if (!TextUtils.isEmpty(name)) {
                try {
                    this.mRoots.put(name, root.getCanonicalFile());
                } catch (IOException e) {
                    throw new IllegalArgumentException("Failed to resolve canonical path for " + root, e);
                }
            } else {
                throw new IllegalArgumentException("Name must not be empty");
            }
        }

        @Override // android.support.v4.content.FileProvider.PathStrategy
        public Uri getUriForFile(File file) {
            String path;
            try {
                String path2 = file.getCanonicalPath();
                Map.Entry<String, File> mostSpecific = null;
                for (Map.Entry<String, File> root : this.mRoots.entrySet()) {
                    String rootPath = root.getValue().getPath();
                    if (path2.startsWith(rootPath) && (mostSpecific == null || rootPath.length() > mostSpecific.getValue().getPath().length())) {
                        mostSpecific = root;
                    }
                }
                if (mostSpecific != null) {
                    String rootPath2 = mostSpecific.getValue().getPath();
                    if (rootPath2.endsWith("/")) {
                        path = path2.substring(rootPath2.length());
                    } else {
                        path = path2.substring(rootPath2.length() + 1);
                    }
                    return new Uri.Builder().scheme("content").authority(this.mAuthority).encodedPath(Uri.encode(mostSpecific.getKey()) + '/' + Uri.encode(path, "/")).build();
                }
                throw new IllegalArgumentException("Failed to find configured root that contains " + path2);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to resolve canonical path for " + file);
            }
        }

        @Override // android.support.v4.content.FileProvider.PathStrategy
        public File getFileForUri(Uri uri) {
            String path = uri.getEncodedPath();
            int splitIndex = path.indexOf(47, 1);
            String tag = Uri.decode(path.substring(1, splitIndex));
            String path2 = Uri.decode(path.substring(splitIndex + 1));
            File root = this.mRoots.get(tag);
            if (root != null) {
                File file = new File(root, path2);
                try {
                    File file2 = file.getCanonicalFile();
                    if (file2.getPath().startsWith(root.getPath())) {
                        return file2;
                    }
                    throw new SecurityException("Resolved path jumped beyond configured root");
                } catch (IOException e) {
                    throw new IllegalArgumentException("Failed to resolve canonical path for " + file);
                }
            } else {
                throw new IllegalArgumentException("Unable to find configured root for " + uri);
            }
        }
    }

    private static int modeToMode(String mode) {
        if ("r".equals(mode)) {
            return 268435456;
        }
        if ("w".equals(mode) || "wt".equals(mode)) {
            return 738197504;
        }
        if ("wa".equals(mode)) {
            return 704643072;
        }
        if ("rw".equals(mode)) {
            return 939524096;
        }
        if ("rwt".equals(mode)) {
            return 1006632960;
        }
        throw new IllegalArgumentException("Invalid mode: " + mode);
    }

    private static File buildPath(File base, String... segments) {
        File cur = base;
        for (String segment : segments) {
            if (segment != null) {
                cur = new File(cur, segment);
            }
        }
        return cur;
    }

    private static String[] copyOf(String[] original, int newLength) {
        String[] result = new String[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    private static Object[] copyOf(Object[] original, int newLength) {
        Object[] result = new Object[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }
}
