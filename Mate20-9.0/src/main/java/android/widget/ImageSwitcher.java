package android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

public class ImageSwitcher extends ViewSwitcher {
    public ImageSwitcher(Context context) {
        super(context);
    }

    public ImageSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setImageResource(int resid) {
        ((ImageView) getNextView()).setImageResource(resid);
        showNext();
    }

    public void setImageURI(Uri uri) {
        ((ImageView) getNextView()).setImageURI(uri);
        showNext();
    }

    public void setImageDrawable(Drawable drawable) {
        ((ImageView) getNextView()).setImageDrawable(drawable);
        showNext();
    }

    public CharSequence getAccessibilityClassName() {
        return ImageSwitcher.class.getName();
    }
}
