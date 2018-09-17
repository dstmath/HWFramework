package android.gesture;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public final class GestureLibraries {

    private static class FileGestureLibrary extends GestureLibrary {
        private final File mPath;

        public FileGestureLibrary(File path) {
            this.mPath = path;
        }

        public boolean isReadOnly() {
            return this.mPath.canWrite() ^ 1;
        }

        public boolean save() {
            if (!this.mStore.hasChanged()) {
                return true;
            }
            File file = this.mPath;
            File parentFile = file.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                return false;
            }
            boolean result = false;
            try {
                file.createNewFile();
                this.mStore.save(new FileOutputStream(file), true);
                result = true;
            } catch (FileNotFoundException e) {
                Log.d(GestureConstants.LOG_TAG, "Could not save the gesture library in " + this.mPath, e);
            } catch (IOException e2) {
                Log.d(GestureConstants.LOG_TAG, "Could not save the gesture library in " + this.mPath, e2);
            }
            return result;
        }

        public boolean load() {
            File file = this.mPath;
            if (!file.exists() || !file.canRead()) {
                return false;
            }
            try {
                this.mStore.load(new FileInputStream(file), true);
                return true;
            } catch (FileNotFoundException e) {
                Log.d(GestureConstants.LOG_TAG, "Could not load the gesture library from " + this.mPath, e);
                return false;
            } catch (IOException e2) {
                Log.d(GestureConstants.LOG_TAG, "Could not load the gesture library from " + this.mPath, e2);
                return false;
            }
        }
    }

    private static class ResourceGestureLibrary extends GestureLibrary {
        private final WeakReference<Context> mContext;
        private final int mResourceId;

        public ResourceGestureLibrary(Context context, int resourceId) {
            this.mContext = new WeakReference(context);
            this.mResourceId = resourceId;
        }

        public boolean isReadOnly() {
            return true;
        }

        public boolean save() {
            return false;
        }

        public boolean load() {
            Context context = (Context) this.mContext.get();
            if (context == null) {
                return false;
            }
            try {
                this.mStore.load(context.getResources().openRawResource(this.mResourceId), true);
                return true;
            } catch (IOException e) {
                Log.d(GestureConstants.LOG_TAG, "Could not load the gesture library from raw resource " + context.getResources().getResourceName(this.mResourceId), e);
                return false;
            }
        }
    }

    private GestureLibraries() {
    }

    public static GestureLibrary fromFile(String path) {
        return fromFile(new File(path));
    }

    public static GestureLibrary fromFile(File path) {
        return new FileGestureLibrary(path);
    }

    public static GestureLibrary fromPrivateFile(Context context, String name) {
        return fromFile(context.getFileStreamPath(name));
    }

    public static GestureLibrary fromRawResource(Context context, int resourceId) {
        return new ResourceGestureLibrary(context, resourceId);
    }
}
