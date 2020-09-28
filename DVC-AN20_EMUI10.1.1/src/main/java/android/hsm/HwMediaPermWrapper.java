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
        HwAudioPermWrapper hwAudioPermWrapper;
        if (this.mCamera == null) {
            if (this.mAudio == null) {
                this.mAudio = new HwAudioPermWrapper();
            }
            this.mAudio.confirmPermission();
        }
        HwCameraPermWrapper hwCameraPermWrapper = this.mCamera;
        return (hwCameraPermWrapper != null && hwCameraPermWrapper.isBlocked()) || ((hwAudioPermWrapper = this.mAudio) != null && hwAudioPermWrapper.isBlocked());
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
        if (confirmMediaPreparePermission()) {
            HwSystemManager.setOutputFile(recorder);
            return;
        }
        RandomAccessFile fos = null;
        try {
            fos = new RandomAccessFile(filePath, "rws");
        } catch (FileNotFoundException e) {
            try {
                fos = new ExternalStorageRandomAccessFileImpl(filePath, "rws");
                Log.i(TAG, "setOutputFile externalStorage SD.");
            } catch (FileNotFoundException e2) {
                Log.i(TAG, "setOutputFile  notNormalE exception");
            }
        }
        if (fos != null) {
            try {
                recorder.setInterOutputFile(fos.getFD());
            } catch (Throwable th) {
                try {
                    fos.close();
                } catch (IOException e3) {
                    Log.w(TAG, "close output stream fail");
                }
                throw th;
            }
        }
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e4) {
                Log.w(TAG, "close output stream fail");
            }
        }
    }

    public void setOutputFile(MediaRecorder recorder, File file) throws IllegalStateException, IOException {
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
                Log.w(TAG, "close output stream fail");
            }
        }
    }
}
