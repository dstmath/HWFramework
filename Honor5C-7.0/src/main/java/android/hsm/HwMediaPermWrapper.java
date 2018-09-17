package android.hsm;

import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;

public class HwMediaPermWrapper {
    private static final String TAG = null;
    private HwAudioPermWrapper mAudio;
    private HwCameraPermWrapper mCamera;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hsm.HwMediaPermWrapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hsm.HwMediaPermWrapper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hsm.HwMediaPermWrapper.<clinit>():void");
    }

    public HwMediaPermWrapper() {
        this.mCamera = null;
        this.mAudio = null;
    }

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

    public void setOutputFile(MediaRecorder recorder, FileDescriptor fd, long offset, long len) throws IllegalStateException, IOException {
        if (confirmMediaPreparePermission()) {
            HwSystemManager.setOutputFile(recorder, offset, len);
        } else {
            recorder._setOutputFile(fd, offset, len);
        }
    }

    public void setOutputFile(MediaRecorder recorder, String filePath, long offset, long len) throws IllegalStateException, IOException {
        if (confirmMediaPreparePermission()) {
            HwSystemManager.setOutputFile(recorder, offset, len);
            return;
        }
        RandomAccessFile fos = new RandomAccessFile(filePath, "rws");
        try {
            recorder._setOutputFile(fos.getFD(), offset, len);
        } finally {
            fos.close();
        }
    }
}
