package com.android.internal.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

public class UserIcons {
    private static final int[] USER_ICON_COLORS = {17170803, 17170804, 17170805, 17170806, 17170807, 17170808, 17170809, 17170810};

    public static Bitmap convertToBitmap(Drawable icon) {
        if (icon == null) {
            return null;
        }
        int width = icon.getIntrinsicWidth();
        int height = icon.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.setBounds(0, 0, width, height);
        icon.draw(canvas);
        return bitmap;
    }

    public static Drawable getDefaultUserIcon(Resources resources, int userId, boolean light) {
        int colorResId = light ? 17170812 : 17170811;
        if (userId != -10000) {
            colorResId = USER_ICON_COLORS[userId % USER_ICON_COLORS.length];
        }
        Drawable icon = resources.getDrawable(33751223, null).mutate();
        icon.setColorFilter(resources.getColor(colorResId, null), PorterDuff.Mode.SRC_IN);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        return icon;
    }
}
