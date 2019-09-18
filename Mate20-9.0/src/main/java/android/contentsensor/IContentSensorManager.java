package android.contentsensor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public interface IContentSensorManager {
    void copyNode(Bundle bundle);

    void processImageAndWebView(Bundle bundle);

    void updateToken(int i, Activity activity);

    void notifyFocusChanged(View focusedView) {
    }
}
