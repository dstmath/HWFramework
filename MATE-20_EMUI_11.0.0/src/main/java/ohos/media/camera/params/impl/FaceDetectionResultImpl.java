package ohos.media.camera.params.impl;

import java.util.Map;
import ohos.media.camera.params.Face;
import ohos.media.camera.params.FaceDetectionResult;

public class FaceDetectionResultImpl implements FaceDetectionResult {
    private final Face[] faceResult;
    private final Map<Integer, Integer> smiles;

    public FaceDetectionResultImpl(Face[] faceArr, Map<Integer, Integer> map) {
        this.faceResult = faceArr;
        this.smiles = map;
    }

    public static FaceDetectionResultImpl getDefault() {
        return new FaceDetectionResultImpl(null, null);
    }

    @Override // ohos.media.camera.params.FaceDetectionResult
    public int getState() {
        if (this.faceResult == null) {
            return -1;
        }
        return this.smiles == null ? 1 : 3;
    }

    @Override // ohos.media.camera.params.FaceDetectionResult
    public Face[] getFaces() {
        return this.faceResult;
    }

    @Override // ohos.media.camera.params.FaceDetectionResult
    public Map<Integer, Integer> getSmiles() {
        return this.smiles;
    }
}
