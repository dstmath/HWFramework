package android.media;

import android.graphics.Bitmap;
import android.graphics.PointF;

public class FaceDetector {
    private static boolean sInitialized;
    private byte[] mBWBuffer;
    private long mDCR;
    private long mFD;
    private int mHeight;
    private int mMaxFaces;
    private long mSDK;
    private int mWidth;

    public class Face {
        public static final float CONFIDENCE_THRESHOLD = 0.4f;
        public static final int EULER_X = 0;
        public static final int EULER_Y = 1;
        public static final int EULER_Z = 2;
        private float mConfidence;
        private float mEyesDist;
        private float mMidPointX;
        private float mMidPointY;
        private float mPoseEulerX;
        private float mPoseEulerY;
        private float mPoseEulerZ;

        public float confidence() {
            return this.mConfidence;
        }

        public void getMidPoint(PointF point) {
            point.set(this.mMidPointX, this.mMidPointY);
        }

        public float eyesDistance() {
            return this.mEyesDist;
        }

        public float pose(int euler) {
            if (euler == 0) {
                return this.mPoseEulerX;
            }
            if (euler == EULER_Y) {
                return this.mPoseEulerY;
            }
            if (euler == EULER_Z) {
                return this.mPoseEulerZ;
            }
            throw new IllegalArgumentException();
        }

        private Face() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.FaceDetector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.FaceDetector.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.FaceDetector.<clinit>():void");
    }

    private native void fft_destroy();

    private native int fft_detect(Bitmap bitmap);

    private native void fft_get_face(Face face, int i);

    private native int fft_initialize(int i, int i2, int i3);

    private static native void nativeClassInit();

    public FaceDetector(int width, int height, int maxFaces) {
        if (sInitialized) {
            fft_initialize(width, height, maxFaces);
            this.mWidth = width;
            this.mHeight = height;
            this.mMaxFaces = maxFaces;
            this.mBWBuffer = new byte[(width * height)];
        }
    }

    public int findFaces(Bitmap bitmap, Face[] faces) {
        if (!sInitialized) {
            return 0;
        }
        if (bitmap.getWidth() != this.mWidth || bitmap.getHeight() != this.mHeight) {
            throw new IllegalArgumentException("bitmap size doesn't match initialization");
        } else if (faces.length < this.mMaxFaces) {
            throw new IllegalArgumentException("faces[] smaller than maxFaces");
        } else {
            int numFaces = fft_detect(bitmap);
            if (numFaces >= this.mMaxFaces) {
                numFaces = this.mMaxFaces;
            }
            for (int i = 0; i < numFaces; i++) {
                if (faces[i] == null) {
                    faces[i] = new Face();
                }
                fft_get_face(faces[i], i);
            }
            return numFaces;
        }
    }

    protected void finalize() throws Throwable {
        fft_destroy();
    }
}
