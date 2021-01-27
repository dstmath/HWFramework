package ohos.agp.window.wmc;

import android.content.res.Configuration;
import android.view.Surface;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.MouseEvent;
import ohos.multimodalinput.event.RotationEvent;
import ohos.multimodalinput.event.TouchEvent;

public interface IAGPEngineAdapter {
    public static final int MODE_2D = 1;
    public static final int MODE_3D = 2;
    public static final int MODE_ACE = 3;

    void loadEngine();

    default void processConfigurationChanged(Configuration configuration) {
    }

    void processDestroy();

    boolean processKeyEvent(KeyEvent keyEvent);

    boolean processMouseEvent(MouseEvent mouseEvent);

    boolean processRotationEvent(RotationEvent rotationEvent);

    void processSurfaceChanged(Surface surface, int i, int i2, int i3);

    void processSurfaceCreated(Surface surface);

    void processSurfaceDestroy(Surface surface);

    boolean processTouchEvent(TouchEvent touchEvent);

    void processVSync(long j);
}
