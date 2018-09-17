package com.huawei.android.pushselfshow.utils;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.huawei.android.pushagent.a.a.c;
import java.lang.reflect.InvocationTargetException;

public class RelativeLayoutForBckgColor extends RelativeLayout {
    private WallpaperManager a;
    private Drawable b;

    public RelativeLayoutForBckgColor(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        a();
    }

    public RelativeLayoutForBckgColor(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        a();
    }

    private static Object a(WallpaperManager wallpaperManager, Rect rect) {
        Object obj = null;
        try {
            Class cls = Class.forName("com.huawei.android.app.WallpaperManagerEx");
            return cls.getDeclaredMethod("getBlurBitmap", new Class[]{WallpaperManager.class, Rect.class}).invoke(cls, new Object[]{wallpaperManager, rect});
        } catch (ClassNotFoundException e) {
            c.d("PushSelfShowLog", " WallpaperManagerEx getBlurBitmap wrong " + e.toString());
            return obj;
        } catch (NoSuchMethodException e2) {
            c.d("PushSelfShowLog", " WallpaperManagerEx getBlurBitmap wrong " + e2.toString());
            return obj;
        } catch (IllegalAccessException e3) {
            c.d("PushSelfShowLog", " WallpaperManagerEx getBlurBitmap wrong " + e3.toString());
            return obj;
        } catch (IllegalArgumentException e4) {
            c.d("PushSelfShowLog", " WallpaperManagerEx getBlurBitmap wrong " + e4.toString());
            return obj;
        } catch (InvocationTargetException e5) {
            c.d("PushSelfShowLog", " WallpaperManagerEx getBlurBitmap wrong " + e5.toString());
            return obj;
        }
    }

    private void b() {
        int color = getContext().getResources().getColor(d.f(getContext(), "hwpush_bgcolor"));
        try {
            int[] iArr = new int[2];
            getLocationOnScreen(iArr);
            Rect rect = new Rect(iArr[0], iArr[1], iArr[0] + getWidth(), iArr[1] + getHeight());
            if (rect.width() <= 0 || rect.height() <= 0) {
                return;
            }
            if (a.d()) {
                int j = a.j(getContext());
                if (j != 0) {
                    setBackgroundColor(j);
                } else {
                    setBackgroundColor(color);
                }
            } else if (a(this.a, rect) == null) {
                c.d("PushSelfShowLog", "not emui 3.0,can not use wallpaper as background");
                setBackgroundColor(color);
            } else {
                this.b = new BitmapDrawable((Bitmap) a(this.a, rect));
                setBackgroundDrawable(this.b);
            }
        } catch (NotFoundException e) {
            c.d("PushSelfShowLog", "setBlurWallpaperBackground error, use default backgroud");
            setBackgroundColor(color);
        } catch (Exception e2) {
            c.d("PushSelfShowLog", "setBlurWallpaperBackground error, use default backgroud");
            setBackgroundColor(color);
        }
    }

    @SuppressLint({"ServiceCast"})
    public final void a() {
        this.a = (WallpaperManager) getContext().getSystemService("wallpaper");
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        b();
    }
}
