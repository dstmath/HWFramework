package android.hsm;

import android.media.MediaRecorder;
import android.os.storage.ExternalStorageRandomAccessFileImpl;
import android.util.Log;
import android.view.Surface;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
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
        String str = TAG;
        Log.i(str, "confirmCameraPermission, blocked:" + this.mCamera.isBlocked());
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
        return (this.mCamera != null && this.mCamera.isBlocked()) || (this.mAudio != null && this.mAudio.isBlocked());
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
            recorder.setInterOutputFile(fd);
        }
    }

    public void setOutputFile(MediaRecorder recorder, String filePath) throws IllegalStateException, IOException {
        RandomAccessFile fos;
        String str;
        if (confirmMediaPreparePermission()) {
            HwSystemManager.setOutputFile(recorder);
            return;
        }
        try {
            fos = new RandomAccessFile(filePath, "rws");
        } catch (FileNotFoundException normalE) {
            try {
                fos = new ExternalStorageRandomAccessFileImpl(filePath, "rws");
                Log.i(TAG, "setOutputFile externalStorage SD.");
            } catch (FileNotFoundException notNormalE) {
                String str2 = TAG;
                Log.i(str2, "setOutputFile  notNormalE = " + notNormalE);
                throw normalE;
            }
        }
        try {
            recorder.setInterOutputFile(fos.getFD());
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                str = "close output stream fail";
                Log.w(TAG, str);
            }
        }
    }

    public void setOutputFile(MediaRecorder recorder, File file) throws IllegalStateException, IOException {
        String str;
        if (confirmMediaPreparePermission()) {
            HwSystemManager.setOutputFile(recorder);
            return;
        }
        ExternalStorageRandomAccessFileImpl fos = new ExternalStorageRandomAccessFileImpl(file, "rws");
        try {
            recorder.setInterOutputFile(fos.getFD());
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                str = "close output stream fail";
                Log.w(TAG, str);
            }
        }
    }
}
