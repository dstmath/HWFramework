package android.support.v4.widget;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public interface TintableImageSourceView {
    @Nullable
    ColorStateList getSupportImageTintList();

    @Nullable
    PorterDuff.Mode getSupportImageTintMode();

    void setSupportImageTintList(@Nullable ColorStateList colorStateList);

    void setSupportImageTintMode(@Nullable PorterDuff.Mode mode);
}
