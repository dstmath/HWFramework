package com.android.server.wm;

import android.content.ComponentName;
import android.content.pm.PackageList;
import android.content.pm.PackageManagerInternal;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SplitNotificationUtils;
import android.util.Xml;
import android.view.DisplayInfo;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.LocalServices;
import com.android.server.wm.LaunchParamsController;
import com.android.server.wm.LaunchParamsPersister;
import com.android.server.wm.PersisterQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public class LaunchParamsPersister {
    private static final char ESCAPED_COMPONENT_SEPARATOR = '_';
    private static final String LAUNCH_PARAMS_DIRNAME = "launch_params";
    private static final String LAUNCH_PARAMS_FILE_SUFFIX = ".xml";
    private static final char ORIGINAL_COMPONENT_SEPARATOR = '/';
    private static final String TAG = "LaunchParamsPersister";
    private static final String TAG_LAUNCH_PARAMS = "launch_params";
    private List<String> mFreeFormList;
    private final SparseArray<ArrayMap<ComponentName, PersistableLaunchParams>> mMap;
    private PackageList mPackageList;
    private final PersisterQueue mPersisterQueue;
    private final ActivityStackSupervisor mSupervisor;
    private final IntFunction<File> mUserFolderGetter;

    LaunchParamsPersister(PersisterQueue persisterQueue, ActivityStackSupervisor supervisor) {
        this(persisterQueue, supervisor, $$Lambda$OuObUsm0bB9g5X0kIXYkBYHvodY.INSTANCE);
    }

    @VisibleForTesting
    LaunchParamsPersister(PersisterQueue persisterQueue, ActivityStackSupervisor supervisor, IntFunction<File> userFolderGetter) {
        this.mMap = new SparseArray<>();
        this.mPersisterQueue = persisterQueue;
        this.mSupervisor = supervisor;
        this.mUserFolderGetter = userFolderGetter;
    }

    /* access modifiers changed from: package-private */
    public void onSystemReady() {
        this.mPackageList = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getPackageList(new PackageListObserver());
        this.mFreeFormList = SplitNotificationUtils.getInstance(this.mSupervisor.mService.mContext).getListPkgName(3);
    }

    /* access modifiers changed from: package-private */
    public void onUnlockUser(int userId) {
        loadLaunchParams(userId);
    }

    /* access modifiers changed from: package-private */
    public void onCleanupUser(int userId) {
        this.mMap.remove(userId);
    }

    private void loadLaunchParams(int userId) {
        File[] paramsFiles;
        Set<String> packages;
        File launchParamsFolder;
        Throwable th;
        Exception e;
        List<File> filesToDelete = new ArrayList<>();
        File launchParamsFolder2 = getLaunchParamFolder(userId);
        if (!launchParamsFolder2.isDirectory()) {
            Slog.i(TAG, "Didn't find launch param folder for user " + userId);
            return;
        }
        Set<String> packages2 = new ArraySet<>(this.mPackageList.getPackageNames());
        File[] paramsFiles2 = launchParamsFolder2.listFiles();
        ArrayMap<ComponentName, PersistableLaunchParams> map = new ArrayMap<>(paramsFiles2.length);
        this.mMap.put(userId, map);
        int length = paramsFiles2.length;
        int i = 0;
        int i2 = 0;
        while (i2 < length) {
            File paramsFile = paramsFiles2[i2];
            if (!paramsFile.isFile()) {
                Slog.w(TAG, paramsFile.getAbsolutePath() + " is not a file.");
                launchParamsFolder = launchParamsFolder2;
                packages = packages2;
                paramsFiles = paramsFiles2;
            } else if (!paramsFile.getName().endsWith(LAUNCH_PARAMS_FILE_SUFFIX)) {
                Slog.w(TAG, "Unexpected params file name: " + paramsFile.getName());
                filesToDelete.add(paramsFile);
                launchParamsFolder = launchParamsFolder2;
                packages = packages2;
                paramsFiles = paramsFiles2;
            } else {
                String paramsFileName = paramsFile.getName();
                ComponentName name = ComponentName.unflattenFromString(paramsFileName.substring(i, paramsFileName.length() - LAUNCH_PARAMS_FILE_SUFFIX.length()).replace(ESCAPED_COMPONENT_SEPARATOR, ORIGINAL_COMPONENT_SEPARATOR));
                if (name == null) {
                    Slog.w(TAG, "Unexpected file name: " + paramsFileName);
                    filesToDelete.add(paramsFile);
                    launchParamsFolder = launchParamsFolder2;
                    packages = packages2;
                    paramsFiles = paramsFiles2;
                } else if (!packages2.contains(name.getPackageName())) {
                    filesToDelete.add(paramsFile);
                    launchParamsFolder = launchParamsFolder2;
                    packages = packages2;
                    paramsFiles = paramsFiles2;
                } else {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new FileReader(paramsFile));
                        PersistableLaunchParams params = new PersistableLaunchParams();
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(reader);
                        while (true) {
                            launchParamsFolder = launchParamsFolder2;
                            try {
                                int event = parser.next();
                                packages = packages2;
                                if (event != 1) {
                                    if (event == 3) {
                                        paramsFiles = paramsFiles2;
                                        break;
                                    } else if (event != 2) {
                                        launchParamsFolder2 = launchParamsFolder;
                                        packages2 = packages;
                                    } else {
                                        try {
                                            String tagName = parser.getName();
                                            if (!"launch_params".equals(tagName)) {
                                                StringBuilder sb = new StringBuilder();
                                                paramsFiles = paramsFiles2;
                                                try {
                                                    sb.append("Unexpected tag name: ");
                                                    sb.append(tagName);
                                                    Slog.w(TAG, sb.toString());
                                                    launchParamsFolder2 = launchParamsFolder;
                                                    packages2 = packages;
                                                    paramsFiles2 = paramsFiles;
                                                } catch (Exception e2) {
                                                    e = e2;
                                                    try {
                                                        Slog.w(TAG, "Failed to restore launch params for " + name, e);
                                                        filesToDelete.add(paramsFile);
                                                        IoUtils.closeQuietly(reader);
                                                        i2++;
                                                        launchParamsFolder2 = launchParamsFolder;
                                                        packages2 = packages;
                                                        paramsFiles2 = paramsFiles;
                                                        i = 0;
                                                    } catch (Throwable th2) {
                                                        th = th2;
                                                    }
                                                }
                                            } else {
                                                paramsFiles = paramsFiles2;
                                                params.restoreFromXml(parser);
                                                launchParamsFolder2 = launchParamsFolder;
                                                packages2 = packages;
                                                paramsFiles2 = paramsFiles;
                                            }
                                        } catch (Exception e3) {
                                            e = e3;
                                            paramsFiles = paramsFiles2;
                                            Slog.w(TAG, "Failed to restore launch params for " + name, e);
                                            filesToDelete.add(paramsFile);
                                            IoUtils.closeQuietly(reader);
                                            i2++;
                                            launchParamsFolder2 = launchParamsFolder;
                                            packages2 = packages;
                                            paramsFiles2 = paramsFiles;
                                            i = 0;
                                        } catch (Throwable th3) {
                                            th = th3;
                                            IoUtils.closeQuietly(reader);
                                            throw th;
                                        }
                                    }
                                } else {
                                    paramsFiles = paramsFiles2;
                                    break;
                                }
                            } catch (Exception e4) {
                                e = e4;
                                packages = packages2;
                                paramsFiles = paramsFiles2;
                                Slog.w(TAG, "Failed to restore launch params for " + name, e);
                                filesToDelete.add(paramsFile);
                                IoUtils.closeQuietly(reader);
                                i2++;
                                launchParamsFolder2 = launchParamsFolder;
                                packages2 = packages;
                                paramsFiles2 = paramsFiles;
                                i = 0;
                            } catch (Throwable th4) {
                                th = th4;
                                IoUtils.closeQuietly(reader);
                                throw th;
                            }
                        }
                        map.put(name, params);
                    } catch (Exception e5) {
                        e = e5;
                        launchParamsFolder = launchParamsFolder2;
                        packages = packages2;
                        paramsFiles = paramsFiles2;
                        Slog.w(TAG, "Failed to restore launch params for " + name, e);
                        filesToDelete.add(paramsFile);
                        IoUtils.closeQuietly(reader);
                        i2++;
                        launchParamsFolder2 = launchParamsFolder;
                        packages2 = packages;
                        paramsFiles2 = paramsFiles;
                        i = 0;
                    } catch (Throwable th5) {
                        th = th5;
                        IoUtils.closeQuietly(reader);
                        throw th;
                    }
                    IoUtils.closeQuietly(reader);
                }
            }
            i2++;
            launchParamsFolder2 = launchParamsFolder;
            packages2 = packages;
            paramsFiles2 = paramsFiles;
            i = 0;
        }
        if (!filesToDelete.isEmpty()) {
            this.mPersisterQueue.addItem(new CleanUpComponentQueueItem(filesToDelete), true);
        }
    }

    /* access modifiers changed from: package-private */
    public void saveTask(TaskRecord task) {
        ArrayMap<ComponentName, PersistableLaunchParams> map;
        PersistableLaunchParams params;
        ComponentName name = task.realActivity;
        int userId = task.userId;
        ArrayMap<ComponentName, PersistableLaunchParams> map2 = this.mMap.get(userId);
        if (map2 == null) {
            ArrayMap<ComponentName, PersistableLaunchParams> map3 = new ArrayMap<>();
            this.mMap.put(userId, map3);
            map = map3;
        } else {
            map = map2;
        }
        PersistableLaunchParams params2 = map.get(name);
        if (params2 == null) {
            PersistableLaunchParams params3 = new PersistableLaunchParams();
            map.put(name, params3);
            params = params3;
        } else {
            params = params2;
        }
        if (saveTaskToLaunchParam(task, params)) {
            this.mPersisterQueue.updateLastOrAddItem(new LaunchParamsWriteQueueItem(userId, name, params), false);
        }
    }

    private boolean saveTaskToLaunchParam(TaskRecord task, PersistableLaunchParams params) {
        ActivityStack stack = task.getStack();
        boolean z = false;
        if (stack.inHwMultiStackWindowingMode()) {
            return false;
        }
        ActivityDisplay display = this.mSupervisor.mRootActivityContainer.getActivityDisplay(stack.mDisplayId);
        DisplayInfo info = new DisplayInfo();
        display.mDisplay.getDisplayInfo(info);
        boolean changed = !Objects.equals(params.mDisplayUniqueId, info.uniqueId);
        params.mDisplayUniqueId = info.uniqueId;
        if (params.mWindowingMode != stack.getWindowingMode()) {
            z = true;
        }
        boolean changed2 = z | changed;
        params.mWindowingMode = stack.getWindowingMode();
        if (task.mLastNonFullscreenBounds != null) {
            boolean changed3 = changed2 | (!Objects.equals(params.mBounds, task.mLastNonFullscreenBounds));
            params.mBounds.set(task.mLastNonFullscreenBounds);
            return changed3;
        }
        boolean changed4 = changed2 | (!params.mBounds.isEmpty());
        params.mBounds.setEmpty();
        return changed4;
    }

    /* access modifiers changed from: package-private */
    public void getLaunchParams(TaskRecord task, ActivityRecord activity, LaunchParamsController.LaunchParams outParams) {
        PersistableLaunchParams persistableParams;
        ComponentName name = task != null ? task.realActivity : activity.mActivityComponent;
        int userId = task != null ? task.userId : activity.mUserId;
        outParams.reset();
        Map<ComponentName, PersistableLaunchParams> map = this.mMap.get(userId);
        if (map != null && (persistableParams = map.get(name)) != null) {
            ActivityDisplay display = this.mSupervisor.mRootActivityContainer.getActivityDisplay(persistableParams.mDisplayUniqueId);
            if (display != null) {
                outParams.mPreferredDisplayId = display.mDisplayId;
            }
            outParams.mWindowingMode = persistableParams.mWindowingMode;
            outParams.mBounds.set(persistableParams.mBounds);
            if ((persistableParams.mWindowingMode == 5 && HwFreeFormUtils.isFreeFormEnable()) && (this.mFreeFormList.contains(name.getPackageName()) || TaskLaunchParamsModifier.APP_LOCK_NAME.equals(activity.shortComponentName) || TaskLaunchParamsModifier.APP_OPAQUE_LOCK_NAME.equals(activity.shortComponentName) || TaskLaunchParamsModifier.HW_CHOOSER_ACTIVITY.equals(activity.shortComponentName))) {
                outParams.mWindowingMode = 0;
            }
            if (task != null && task.inHwMagicWindowingMode()) {
                outParams.mWindowingMode = 0;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeRecordForPackage(String packageName) {
        List<File> fileToDelete = new ArrayList<>();
        for (int i = 0; i < this.mMap.size(); i++) {
            File launchParamsFolder = getLaunchParamFolder(this.mMap.keyAt(i));
            ArrayMap<ComponentName, PersistableLaunchParams> map = this.mMap.valueAt(i);
            for (int j = map.size() - 1; j >= 0; j--) {
                ComponentName name = map.keyAt(j);
                if (name.getPackageName().equals(packageName)) {
                    map.removeAt(j);
                    fileToDelete.add(getParamFile(launchParamsFolder, name));
                }
            }
        }
        synchronized (this.mPersisterQueue) {
            this.mPersisterQueue.removeItems(new Predicate(packageName) {
                /* class com.android.server.wm.$$Lambda$LaunchParamsPersister$Rc1cXPLhXa2WPSr18Q9Xc7SdV8 */
                private final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return ((LaunchParamsPersister.LaunchParamsWriteQueueItem) obj).mComponentName.getPackageName().equals(this.f$0);
                }
            }, LaunchParamsWriteQueueItem.class);
            this.mPersisterQueue.addItem(new CleanUpComponentQueueItem(fileToDelete), true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private File getParamFile(File launchParamFolder, ComponentName name) {
        String componentNameString = name.flattenToShortString().replace(ORIGINAL_COMPONENT_SEPARATOR, ESCAPED_COMPONENT_SEPARATOR);
        return new File(launchParamFolder, componentNameString + LAUNCH_PARAMS_FILE_SUFFIX);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private File getLaunchParamFolder(int userId) {
        return new File(this.mUserFolderGetter.apply(userId), "launch_params");
    }

    /* access modifiers changed from: private */
    public class PackageListObserver implements PackageManagerInternal.PackageListObserver {
        private PackageListObserver() {
        }

        public void onPackageAdded(String packageName, int uid) {
        }

        public void onPackageRemoved(String packageName, int uid) {
            LaunchParamsPersister.this.removeRecordForPackage(packageName);
        }
    }

    /* access modifiers changed from: private */
    public class LaunchParamsWriteQueueItem implements PersisterQueue.WriteQueueItem<LaunchParamsWriteQueueItem> {
        private final ComponentName mComponentName;
        private PersistableLaunchParams mLaunchParams;
        private final int mUserId;

        private LaunchParamsWriteQueueItem(int userId, ComponentName componentName, PersistableLaunchParams launchParams) {
            this.mUserId = userId;
            this.mComponentName = componentName;
            this.mLaunchParams = launchParams;
        }

        private StringWriter saveParamsToXml() {
            StringWriter writer = new StringWriter();
            XmlSerializer serializer = new FastXmlSerializer();
            try {
                serializer.setOutput(writer);
                serializer.startDocument(null, true);
                serializer.startTag(null, "launch_params");
                this.mLaunchParams.saveToXml(serializer);
                serializer.endTag(null, "launch_params");
                serializer.endDocument();
                serializer.flush();
                return writer;
            } catch (IOException e) {
                return null;
            }
        }

        @Override // com.android.server.wm.PersisterQueue.WriteQueueItem
        public void process() {
            StringWriter writer = saveParamsToXml();
            File launchParamFolder = LaunchParamsPersister.this.getLaunchParamFolder(this.mUserId);
            if (launchParamFolder.isDirectory() || launchParamFolder.mkdirs()) {
                AtomicFile atomicFile = new AtomicFile(LaunchParamsPersister.this.getParamFile(launchParamFolder, this.mComponentName));
                FileOutputStream stream = null;
                try {
                    stream = atomicFile.startWrite();
                    stream.write(writer.toString().getBytes());
                    atomicFile.finishWrite(stream);
                } catch (Exception e) {
                    Slog.e(LaunchParamsPersister.TAG, "Failed to write param file for " + this.mComponentName, e);
                    if (stream != null) {
                        atomicFile.failWrite(stream);
                    }
                }
            } else {
                Slog.w(LaunchParamsPersister.TAG, "Failed to create folder for " + this.mUserId);
            }
        }

        public boolean matches(LaunchParamsWriteQueueItem item) {
            return this.mUserId == item.mUserId && this.mComponentName.equals(item.mComponentName);
        }

        public void updateFrom(LaunchParamsWriteQueueItem item) {
            this.mLaunchParams = item.mLaunchParams;
        }
    }

    /* access modifiers changed from: private */
    public class CleanUpComponentQueueItem implements PersisterQueue.WriteQueueItem {
        private final List<File> mComponentFiles;

        private CleanUpComponentQueueItem(List<File> componentFiles) {
            this.mComponentFiles = componentFiles;
        }

        @Override // com.android.server.wm.PersisterQueue.WriteQueueItem
        public void process() {
            for (File file : this.mComponentFiles) {
                if (!file.delete()) {
                    Slog.w(LaunchParamsPersister.TAG, "Failed to delete " + file.getAbsolutePath());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class PersistableLaunchParams {
        private static final String ATTR_BOUNDS = "bounds";
        private static final String ATTR_DISPLAY_UNIQUE_ID = "display_unique_id";
        private static final String ATTR_WINDOWING_MODE = "windowing_mode";
        final Rect mBounds;
        String mDisplayUniqueId;
        int mWindowingMode;

        private PersistableLaunchParams() {
            this.mBounds = new Rect();
        }

        /* access modifiers changed from: package-private */
        public void saveToXml(XmlSerializer serializer) throws IOException {
            serializer.attribute(null, ATTR_DISPLAY_UNIQUE_ID, this.mDisplayUniqueId);
            serializer.attribute(null, ATTR_WINDOWING_MODE, Integer.toString(this.mWindowingMode));
            serializer.attribute(null, ATTR_BOUNDS, this.mBounds.flattenToString());
        }

        /* access modifiers changed from: package-private */
        public void restoreFromXml(XmlPullParser parser) {
            Rect bounds;
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attrValue = parser.getAttributeValue(i);
                String attributeName = parser.getAttributeName(i);
                char c = 65535;
                int hashCode = attributeName.hashCode();
                if (hashCode != -1499361012) {
                    if (hashCode != -1383205195) {
                        if (hashCode == 748872656 && attributeName.equals(ATTR_WINDOWING_MODE)) {
                            c = 1;
                        }
                    } else if (attributeName.equals(ATTR_BOUNDS)) {
                        c = 2;
                    }
                } else if (attributeName.equals(ATTR_DISPLAY_UNIQUE_ID)) {
                    c = 0;
                }
                if (c == 0) {
                    this.mDisplayUniqueId = attrValue;
                } else if (c == 1) {
                    this.mWindowingMode = Integer.parseInt(attrValue);
                } else if (c == 2 && (bounds = Rect.unflattenFromString(attrValue)) != null) {
                    this.mBounds.set(bounds);
                }
            }
        }

        public String toString() {
            StringBuilder builder = new StringBuilder("PersistableLaunchParams{");
            builder.append("windowingMode=" + this.mWindowingMode);
            builder.append(" displayUniqueId=" + this.mDisplayUniqueId);
            builder.append(" bounds=" + this.mBounds);
            builder.append(" }");
            return builder.toString();
        }
    }
}
