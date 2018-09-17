package android.webkit;

import android.content.Context;
import android.view.View;

public interface PluginStub {
    View getEmbeddedView(int i, Context context);

    View getFullScreenView(int i, Context context);
}
