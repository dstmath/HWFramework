package ohos.accessibility.ability;

import ohos.agp.utils.Rect;

public interface DisplayResizeListener {
    void onDisplayResizerChanged(DisplayResizeController displayResizeController, Rect rect, float f, float f2, float f3);
}
