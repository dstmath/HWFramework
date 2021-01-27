package ohos.media.camera.params.adapter;

import ohos.media.camera.params.Face;
import ohos.media.camera.params.ResultKey;

public class InnerResultKey {
    public static final ResultKey.Key<Integer> AE_STATE = new ResultKey.Key<>("ohos.camera.aeState", Integer.class);
    public static final ResultKey.Key<Integer> AF_STATE = new ResultKey.Key<>("ohos.camera.afState", Integer.class);
    public static final ResultKey.Key<Face[]> FACE_DETECT = new ResultKey.Key<>("ohos.camera.faceDetect", Face[].class);
    public static final ResultKey.Key<int[]> FACE_SMILE_SCORE = new ResultKey.Key<>("ohos.camera.faceSmileScore", int[].class);
    public static final ResultKey.Key<int[]> I_FRAME_INFO = new ResultKey.Key<>("harmonyos.camera.iFrameInfo", int[].class);
    public static final ResultKey.Key<Integer> SMART_SUGGEST_HINT = new ResultKey.Key<>("ohos.camera.smartSuggestHint", Integer.class);
}
