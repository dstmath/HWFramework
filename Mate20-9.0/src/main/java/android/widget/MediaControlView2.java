package android.widget;

import android.content.Context;
import android.media.SessionToken2;
import android.media.session.MediaController;
import android.media.update.ApiLoader;
import android.media.update.MediaControlView2Provider;
import android.media.update.ViewGroupHelper;
import android.media.update.ViewGroupProvider;
import android.util.AttributeSet;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MediaControlView2 extends ViewGroupHelper<MediaControlView2Provider> {
    public static final int BUTTON_ASPECT_RATIO = 10;
    public static final int BUTTON_FFWD = 2;
    public static final int BUTTON_FULL_SCREEN = 7;
    public static final int BUTTON_MUTE = 9;
    public static final int BUTTON_NEXT = 4;
    public static final int BUTTON_OVERFLOW = 8;
    public static final int BUTTON_PLAY_PAUSE = 1;
    public static final int BUTTON_PREV = 5;
    public static final int BUTTON_REW = 3;
    public static final int BUTTON_SETTINGS = 11;
    public static final int BUTTON_SUBTITLE = 6;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Button {
    }

    public interface OnFullScreenListener {
        void onFullScreen(View view, boolean z);
    }

    public MediaControlView2(Context context) {
        this(context, null);
    }

    public MediaControlView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaControlView2(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MediaControlView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(new ViewGroupHelper.ProviderCreator(defStyleAttr, defStyleRes) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final ViewGroupProvider createProvider(ViewGroupHelper viewGroupHelper, ViewGroupProvider viewGroupProvider, ViewGroupProvider viewGroupProvider2) {
                return ApiLoader.getProvider().createMediaControlView2((MediaControlView2) viewGroupHelper, viewGroupProvider, viewGroupProvider2, AttributeSet.this, this.f$1, this.f$2);
            }
        }, context, attrs, defStyleAttr, defStyleRes);
        this.mProvider.initialize(attrs, defStyleAttr, defStyleRes);
    }

    public void setMediaSessionToken(SessionToken2 token) {
        this.mProvider.setMediaSessionToken_impl(token);
    }

    public void setOnFullScreenListener(OnFullScreenListener l) {
        this.mProvider.setOnFullScreenListener_impl(l);
    }

    public void setController(MediaController controller) {
        this.mProvider.setController_impl(controller);
    }

    public void setButtonVisibility(int button, int visibility) {
        this.mProvider.setButtonVisibility_impl(button, visibility);
    }

    public void requestPlayButtonFocus() {
        this.mProvider.requestPlayButtonFocus_impl();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mProvider.onLayout_impl(changed, l, t, r, b);
    }
}
