package com.android.server.wm;

import android.app.ActivityManager.TaskSnapshot;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.util.Slog;
import com.android.server.wm.nano.WindowManagerProtos.TaskSnapshotProto;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class TaskSnapshotLoader {
    private static final String TAG = "WindowManager";
    private final TaskSnapshotPersister mPersister;

    TaskSnapshotLoader(TaskSnapshotPersister persister) {
        this.mPersister = persister;
    }

    TaskSnapshot loadTask(int taskId, int userId, boolean reducedResolution) {
        File bitmapFile;
        File protoFile = this.mPersister.getProtoFile(taskId, userId);
        if (reducedResolution) {
            bitmapFile = this.mPersister.getReducedResolutionBitmapFile(taskId, userId);
        } else {
            bitmapFile = this.mPersister.getBitmapFile(taskId, userId);
        }
        if (!protoFile.exists() || (bitmapFile.exists() ^ 1) != 0) {
            return null;
        }
        try {
            TaskSnapshotProto proto = TaskSnapshotProto.parseFrom(Files.readAllBytes(protoFile.toPath()));
            Options options = new Options();
            options.inPreferredConfig = Config.HARDWARE;
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
            return new TaskSnapshot(buffer, proto.orientation, new Rect(proto.insetLeft, proto.insetTop, proto.insetRight, proto.insetBottom), reducedResolution, reducedResolution ? 0.5f : 1.0f);
        } catch (IOException e) {
            Slog.w(TAG, "Unable to load task snapshot data for taskId=" + taskId);
            return null;
        }
    }
}
