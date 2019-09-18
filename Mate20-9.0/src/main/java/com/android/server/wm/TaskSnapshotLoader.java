package com.android.server.wm;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.util.Slog;
import com.android.server.wm.nano.WindowManagerProtos;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class TaskSnapshotLoader {
    private static final String TAG = "WindowManager";
    private final TaskSnapshotPersister mPersister;

    TaskSnapshotLoader(TaskSnapshotPersister persister) {
        this.mPersister = persister;
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.TaskSnapshot loadTask(int taskId, int userId, boolean reducedResolution) {
        File file;
        int i = taskId;
        int i2 = userId;
        File protoFile = this.mPersister.getProtoFile(i, i2);
        if (reducedResolution) {
            file = this.mPersister.getReducedResolutionBitmapFile(i, i2);
        } else {
            file = this.mPersister.getBitmapFile(i, i2);
        }
        File bitmapFile = file;
        if (bitmapFile == null || !protoFile.exists()) {
        } else if (!bitmapFile.exists()) {
            File file2 = bitmapFile;
        } else {
            try {
                byte[] bytes = Files.readAllBytes(protoFile.toPath());
                WindowManagerProtos.TaskSnapshotProto proto = WindowManagerProtos.TaskSnapshotProto.parseFrom(bytes);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.HARDWARE;
                Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath(), options);
                if (bitmap == null) {
                    try {
                        Slog.w(TAG, "Failed to load bitmap: " + bitmapFile.getPath());
                        return null;
                    } catch (IOException e) {
                        File file3 = bitmapFile;
                    }
                } else {
                    GraphicBuffer buffer = bitmap.createGraphicBufferHandle();
                    if (buffer == null) {
                        Slog.w(TAG, "Failed to retrieve gralloc buffer for bitmap: " + bitmapFile.getPath());
                        return null;
                    }
                    int i3 = proto.orientation;
                    Rect rect = new Rect(proto.insetLeft, proto.insetTop, proto.insetRight, proto.insetBottom);
                    float f = reducedResolution ? TaskSnapshotPersister.REDUCED_SCALE : 1.0f;
                    boolean z = proto.isRealSnapshot;
                    int i4 = proto.windowingMode;
                    int i5 = proto.systemUiVisibility;
                    byte[] bArr = bytes;
                    boolean z2 = proto.isTranslucent;
                    int i6 = i5;
                    r4 = r4;
                    Bitmap bitmap2 = bitmap;
                    BitmapFactory.Options options2 = options;
                    WindowManagerProtos.TaskSnapshotProto taskSnapshotProto = proto;
                    int i7 = i6;
                    File file4 = bitmapFile;
                    try {
                        ActivityManager.TaskSnapshot taskSnapshot = new ActivityManager.TaskSnapshot(buffer, i3, rect, reducedResolution, f, z, i4, i7, z2);
                        return taskSnapshot;
                    } catch (IOException e2) {
                        Slog.w(TAG, "Unable to load task snapshot data for taskId=" + i);
                        return null;
                    }
                }
            } catch (IOException e3) {
                File file5 = bitmapFile;
                Slog.w(TAG, "Unable to load task snapshot data for taskId=" + i);
                return null;
            }
        }
        return null;
    }
}
