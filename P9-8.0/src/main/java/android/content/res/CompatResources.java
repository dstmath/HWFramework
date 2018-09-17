package android.content.res;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;
import java.lang.ref.WeakReference;

public class CompatResources extends Resources {
    private WeakReference<Context> mContext = new WeakReference(null);

    public CompatResources(ClassLoader cls) {
        super(cls);
    }

    public void setContext(Context context) {
        this.mContext = new WeakReference(context);
    }

    public Drawable getDrawable(int id) throws NotFoundException {
        return getDrawable(id, getTheme());
    }

    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        return getDrawableForDensity(id, density, getTheme());
    }

    public int getColor(int id) throws NotFoundException {
        return getColor(id, getTheme());
    }

    public ColorStateList getColorStateList(int id) throws NotFoundException {
        return getColorStateList(id, getTheme());
    }

    private Theme getTheme() {
        Context c = (Context) this.mContext.get();
        if (c != null) {
            return c.getTheme();
        }
        return null;
    }
}
