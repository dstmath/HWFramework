package android.content.res;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import java.lang.ref.WeakReference;

public class CompatResources extends Resources {
    private WeakReference<Context> mContext = new WeakReference<>(null);

    public CompatResources(ClassLoader cls) {
        super(cls);
    }

    public void setContext(Context context) {
        this.mContext = new WeakReference<>(context);
    }

    @Override // android.content.res.Resources
    public Drawable getDrawable(int id) throws Resources.NotFoundException {
        return getDrawable(id, getTheme());
    }

    @Override // android.content.res.Resources
    public Drawable getDrawableForDensity(int id, int density) throws Resources.NotFoundException {
        return getDrawableForDensity(id, density, getTheme());
    }

    @Override // android.content.res.Resources
    public int getColor(int id) throws Resources.NotFoundException {
        return getColor(id, getTheme());
    }

    @Override // android.content.res.Resources
    public ColorStateList getColorStateList(int id) throws Resources.NotFoundException {
        return getColorStateList(id, getTheme());
    }

    private Resources.Theme getTheme() {
        Context c = this.mContext.get();
        if (c != null) {
            return c.getTheme();
        }
        return null;
    }
}
