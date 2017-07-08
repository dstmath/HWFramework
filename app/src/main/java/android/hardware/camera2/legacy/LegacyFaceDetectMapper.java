package android.hardware.camera2.legacy;

import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.legacy.ParameterUtils.ZoomData;
import android.hardware.camera2.utils.ParamsUtils;
import android.speech.tts.TextToSpeech;
import android.telecom.AudioState;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.List;

public class LegacyFaceDetectMapper {
    private static final boolean DEBUG = false;
    private static String TAG;
    private final Camera mCamera;
    private boolean mFaceDetectEnabled;
    private boolean mFaceDetectReporting;
    private boolean mFaceDetectScenePriority;
    private final boolean mFaceDetectSupported;
    private Face[] mFaces;
    private Face[] mFacesPrev;
    private final Object mLock;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyFaceDetectMapper.<clinit>():void");
    }

    public LegacyFaceDetectMapper(Camera camera, CameraCharacteristics characteristics) {
        this.mFaceDetectEnabled = false;
        this.mFaceDetectScenePriority = false;
        this.mFaceDetectReporting = false;
        this.mLock = new Object();
        this.mCamera = (Camera) Preconditions.checkNotNull(camera, "camera must not be null");
        Preconditions.checkNotNull(characteristics, "characteristics must not be null");
        this.mFaceDetectSupported = ArrayUtils.contains((int[]) characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES), 1);
        if (this.mFaceDetectSupported) {
            this.mCamera.setFaceDetectionListener(new FaceDetectionListener() {
                public void onFaceDetection(Face[] faces, Camera camera) {
                    int lengthFaces = faces == null ? 0 : faces.length;
                    synchronized (LegacyFaceDetectMapper.this.mLock) {
                        if (LegacyFaceDetectMapper.this.mFaceDetectEnabled) {
                            LegacyFaceDetectMapper.this.mFaces = faces;
                        } else if (lengthFaces > 0) {
                            Log.d(LegacyFaceDetectMapper.TAG, "onFaceDetection - Ignored some incoming faces sinceface detection was disabled");
                        }
                    }
                }
            });
        }
    }

    public void processFaceDetectMode(CaptureRequest captureRequest, Parameters parameters) {
        Preconditions.checkNotNull(captureRequest, "captureRequest must not be null");
        int fdMode = ((Integer) ParamsUtils.getOrDefault(captureRequest, CaptureRequest.STATISTICS_FACE_DETECT_MODE, Integer.valueOf(0))).intValue();
        if (fdMode == 0 || this.mFaceDetectSupported) {
            int sceneMode = ((Integer) ParamsUtils.getOrDefault(captureRequest, CaptureRequest.CONTROL_SCENE_MODE, Integer.valueOf(0))).intValue();
            if (sceneMode != 1 || this.mFaceDetectSupported) {
                switch (fdMode) {
                    case TextToSpeech.SUCCESS /*0*/:
                    case AudioState.ROUTE_EARPIECE /*1*/:
                        break;
                    case AudioState.ROUTE_BLUETOOTH /*2*/:
                        Log.w(TAG, "processFaceDetectMode - statistics.faceDetectMode == FULL unsupported, downgrading to SIMPLE");
                        break;
                    default:
                        Log.w(TAG, "processFaceDetectMode - ignoring unknown statistics.faceDetectMode = " + fdMode);
                        return;
                }
                boolean enableFaceDetect = fdMode == 0 ? sceneMode == 1 : true;
                synchronized (this.mLock) {
                    if (enableFaceDetect != this.mFaceDetectEnabled) {
                        boolean z;
                        if (enableFaceDetect) {
                            this.mCamera.startFaceDetection();
                        } else {
                            this.mCamera.stopFaceDetection();
                            this.mFaces = null;
                        }
                        this.mFaceDetectEnabled = enableFaceDetect;
                        this.mFaceDetectScenePriority = sceneMode == 1;
                        if (fdMode != 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        this.mFaceDetectReporting = z;
                    }
                }
                return;
            }
            Log.w(TAG, "processFaceDetectMode - ignoring control.sceneMode == FACE_PRIORITY; face detection is not available");
            return;
        }
        Log.w(TAG, "processFaceDetectMode - Ignoring statistics.faceDetectMode; face detection is not available");
    }

    public void mapResultFaces(CameraMetadataNative result, LegacyRequest legacyRequest) {
        int fdMode;
        Preconditions.checkNotNull(result, "result must not be null");
        Preconditions.checkNotNull(legacyRequest, "legacyRequest must not be null");
        synchronized (this.mLock) {
            Face[] faceArr;
            fdMode = this.mFaceDetectReporting ? 1 : 0;
            if (this.mFaceDetectReporting) {
                faceArr = this.mFaces;
            } else {
                faceArr = null;
            }
            boolean fdScenePriority = this.mFaceDetectScenePriority;
            Face[] previousFaces = this.mFacesPrev;
            this.mFacesPrev = faceArr;
        }
        Rect activeArray = (Rect) legacyRequest.characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        ZoomData zoomData = ParameterUtils.convertScalerCropRegion(activeArray, (Rect) legacyRequest.captureRequest.get(CaptureRequest.SCALER_CROP_REGION), legacyRequest.previewSize, legacyRequest.parameters);
        List<android.hardware.camera2.params.Face> convertedFaces = new ArrayList();
        if (faceArr != null) {
            for (Face face : faceArr) {
                if (face != null) {
                    convertedFaces.add(ParameterUtils.convertFaceFromLegacy(face, activeArray, zoomData));
                } else {
                    Log.w(TAG, "mapResultFaces - read NULL face from camera1 device");
                }
            }
        }
        result.set(CaptureResult.STATISTICS_FACES, (android.hardware.camera2.params.Face[]) convertedFaces.toArray(new android.hardware.camera2.params.Face[0]));
        result.set(CaptureResult.STATISTICS_FACE_DETECT_MODE, Integer.valueOf(fdMode));
        if (fdScenePriority) {
            result.set(CaptureResult.CONTROL_SCENE_MODE, Integer.valueOf(1));
        }
    }
}
