package ohos.ai.cv.text;

import ohos.ai.cv.common.ICvBase;
import ohos.ai.cv.common.VisionCallback;
import ohos.ai.cv.common.VisionImage;

public interface ITextDetector extends ICvBase {
    int detect(VisionImage visionImage, Text text, VisionCallback<Text> visionCallback);

    void setVisionConfiguration(TextConfiguration textConfiguration);
}
