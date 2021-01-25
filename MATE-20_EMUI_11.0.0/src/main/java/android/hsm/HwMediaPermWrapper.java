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
    private static final String FILE_OP_MODE = "rws";
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

    public Surface setPreviewDisplay(Surface surface) {
        try {
            if (confirmCameraPermissionWithResult()) {
                return null;
            }
            return surface;
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "confirm camera IllegalArgumentException.");
            return surface;
        } catch (Exception e2) {
            Log.w(TAG, "confirm camera permission fail.");
            return surface;
        }
    }

    private void closeFile(RandomAccessFile fileOp) {
        if (fileOp != null) {
            try {
                fileOp.close();
            } catch (IOException e) {
                Log.w(TAG, "close output stream fail");
            }
        }
    }

    private void fileOperate(MediaRecorder recorder, String filePath) {
        RandomAccessFile fileOp = null;
        try {
            fileOp = new RandomAccessFile(filePath, FILE_OP_MODE);
        } catch (FileNotFoundException e) {
            try {
                fileOp = new ExternalStorageRandomAccessFileImpl(filePath, FILE_OP_MODE);
                Log.i(TAG, "setOutputFile externalStorage SD.");
            } catch (FileNotFoundException e2) {
                Log.i(TAG, "setOutputFile notNormalE exception");
            }
        }
        if (fileOp != null) {
            try {
                recorder.setInterOutputFile(fileOp.getFD());
            } catch (IOException e3) {
                Log.i(TAG, "fileOperate exception");
            } catch (Throwable th) {
                closeFile(fileOp);
                throw th;
            }
        }
        closeFile(fileOp);
    }

    private void fileOperateExternalStorage(MediaRecorder recorder, File file) {
        try {
            RandomAccessFile fileOp = new ExternalStorageRandomAccessFileImpl(file, FILE_OP_MODE);
            try {
                recorder.setInterOutputFile(fileOp.getFD());
            } catch (IOException e) {
                Log.i(TAG, "setInterOutputFile error");
            } catch (Throwable th) {
                closeFile(fileOp);
                throw th;
            }
            closeFile(fileOp);
        } catch (FileNotFoundException e2) {
            Log.i(TAG, "illegal input file");
        }
    }

    public void setOutputFile(MediaRecorder recorder, FileDescriptor fd) throws IllegalStateException, IOException {
        if (recorder == null) {
            Log.e(TAG, "param error");
        } else if (confirmMediaPreparePermission()) {
            HwSystemManager.setOutputFile(recorder);
        } else {
            recorder.setInterOutputFile(fd);
        }
    }

    public void setOutputFile(MediaRecorder recorder, String filePath) throws IllegalStateException, IOException {
        if (recorder == null) {
            Log.e(TAG, "param error");
        } else if (confirmMediaPreparePermission()) {
            HwSystemManager.setOutputFile(recorder);
        } else {
            fileOperate(recorder, filePath);
        }
    }

    public void setOutputFile(MediaRecorder recorder, File file) throws IllegalStateException, IOException {
        if (recorder == null) {
            Log.e(TAG, "param error");
        } else if (confirmMediaPreparePermission()) {
            HwSystemManager.setOutputFile(recorder);
        } else {
            fileOperateExternalStorage(recorder, file);
        }
    }
}
