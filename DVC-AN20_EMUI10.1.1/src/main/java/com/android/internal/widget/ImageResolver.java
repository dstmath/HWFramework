package com.android.internal.widget;

import android.graphics.drawable.Drawable;
import android.net.Uri;

public interface ImageResolver {
    Drawable loadImage(Uri uri);
}
