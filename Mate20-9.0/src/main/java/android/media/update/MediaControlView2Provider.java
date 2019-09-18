package android.media.update;

import android.media.SessionToken2;
import android.media.session.MediaController;
import android.util.AttributeSet;
import android.widget.MediaControlView2;

public interface MediaControlView2Provider extends ViewGroupProvider {
    void initialize(AttributeSet attributeSet, int i, int i2);

    void requestPlayButtonFocus_impl();

    void setButtonVisibility_impl(int i, int i2);

    void setController_impl(MediaController mediaController);

    void setMediaSessionToken_impl(SessionToken2 sessionToken2);

    void setOnFullScreenListener_impl(MediaControlView2.OnFullScreenListener onFullScreenListener);
}
