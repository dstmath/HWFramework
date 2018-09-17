package android.hsm;

import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;

public class HwMediaPermWrapper {
    private static final String TAG = HwMediaPermWrapper.class.getSimpleName();
    private HwAudioPermWrapper mAudio = null;
    private HwCameraPermWrapper mCamera = null;

    public void confirmCameraPermission() {
        if (this.mCamera == null) {
            this.mCamera = new HwCameraPermWrapper();
        }
        this.mCamera.confirmPermission();
        Log.i(TAG, "confirmCameraPermission, blocked:" + this.mCamera.isBlocked());
    }

    private boolean confirmCameraPermissionWithResult() {
        if (this.mCamera == null) {
            this.mCamera = new HwCameraPermWrapper();
        }
        return this.mCamera.confirmPermissionWithResult();
    }

    public boolean confirmMediaPreparePermission() {
        if (this.mCamera == null) {
            if (this.mAudio == null) {
                this.mAudio = new HwAudioPermWrapper();
            }
            this.mAudio.confirmPermission();
        }
        if (this.mCamera == null || !this.mCamera.isBlocked()) {
            return this.mAudio != null ? this.mAudio.isBlocked() : false;
        } else {
            return true;
        }
    }

    public Surface setPreviewDisplay(Surface sv) {
        try {
            if (confirmCameraPermissionWithResult()) {
                return null;
            }
            return sv;
        } catch (Exception e) {
            Log.w(TAG, "confirm camera permission fail.");
            return sv;
        }
    }

    public void setOutputFile(MediaRecorder recorder, FileDescriptor fd) throws IllegalStateException, IOException {
        if (confirmMediaPreparePermission()) {
            HwSystemManager.setOutputFile(recorder);
        } else {
            recorder._setOutputFile(fd);
        }
    }

    public void setOutputFile(MediaRecorder recorder, String filePath) throws IllegalStateException, IOException {
        if (confirmMediaPreparePermission()) {
            HwSystemManager.setOutputFile(recorder);
            return;
        }
        RandomAccessFile fos = new RandomAccessFile(filePath, "rws");
        try {
            recorder._setOutputFile(fos.getFD());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.w(TAG, "close output stream fail");
                }
            }
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e2) {
                    Log.w(TAG, "close output stream fail");
                }
            }
        }
    }

    public void setOutputFile(MediaRecorder recorder, File file) throws IllegalStateException, IOException {
        if (confirmMediaPreparePermission()) {
            HwSystemManager.setOutputFile(recorder);
            return;
        }
        RandomAccessFile fos = new RandomAccessFile(file, "rws");
        try {
            recorder._setOutputFile(fos.getFD());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.w(TAG, "close output stream fail");
                }
            }
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e2) {
                    Log.w(TAG, "close output stream fail");
                }
            }
        }
    }
}
