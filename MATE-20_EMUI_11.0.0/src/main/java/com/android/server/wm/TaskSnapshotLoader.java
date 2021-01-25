package com.android.server.wm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.util.Slog;
import com.android.server.wm.nano.WindowManagerProtos;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/* access modifiers changed from: package-private */
public class TaskSnapshotLoader {
    private static final String TAG = "WindowManager";
    private final TaskSnapshotPersister mPersister;

    TaskSnapshotLoader(TaskSnapshotPersister persister) {
        this.mPersister = persister;
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.TaskSnapshot loadTask(int taskId, int userId, boolean reducedResolution) {
        File bitmapFile;
        String str;
        float scale;
        File protoFile = this.mPersister.getProtoFile(taskId, userId);
        if (reducedResolution) {
            bitmapFile = this.mPersister.getReducedResolutionBitmapFile(taskId, userId);
        } else {
            bitmapFile = this.mPersister.getBitmapFile(taskId, userId);
        }
        if (bitmapFile == null || !protoFile.exists() || !bitmapFile.exists()) {
            return null;
        }
        try {
            WindowManagerProtos.TaskSnapshotProto proto = WindowManagerProtos.TaskSnapshotProto.parseFrom(Files.readAllBytes(protoFile.toPath()));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.HARDWARE;
            Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath(), options);
            if (bitmap == null) {
                Slog.w(TAG, "Failed to load bitmap: " + bitmapFile.getPath());
                return null;
            }
            GraphicBuffer buffer = bitmap.createGraphicBufferHandle();
            if (buffer == null) {
                Slog.w(TAG, "Failed to retrieve gralloc buffer for bitmap: " + bitmapFile.getPath());
                return null;
            }
            ComponentName topActivityComponent = ComponentName.unflattenFromString(proto.topActivityComponent);
            float legacyScale = reducedResolution ? this.mPersister.getReducedScale() : 1.0f;
            if (Float.compare(proto.scale, 0.0f) == 0 || Float.compare(proto.scale, 1.0f) == 0) {
                scale = legacyScale;
            } else {
                scale = proto.scale;
            }
            ColorSpace colorSpace = bitmap.getColorSpace();
            int i = proto.orientation;
            Rect rect = new Rect(proto.insetLeft, proto.insetTop, proto.insetRight, proto.insetBottom);
            boolean z = proto.isRealSnapshot;
            int i2 = proto.windowingMode;
            int i3 = proto.systemUiVisibility;
            boolean z2 = proto.isTranslucent;
            str = TAG;
            try {
                return new ActivityManager.TaskSnapshot(topActivityComponent, buffer, colorSpace, i, rect, reducedResolution, scale, z, i2, i3, z2);
            } catch (IOException e) {
                Slog.w(str, "Unable to load task snapshot data for taskId=" + taskId);
                return null;
            }
        } catch (IOException e2) {
            str = TAG;
            Slog.w(str, "Unable to load task snapshot data for taskId=" + taskId);
            return null;
        }
    }
}
