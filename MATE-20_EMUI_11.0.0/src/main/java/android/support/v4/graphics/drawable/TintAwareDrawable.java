package android.support.v4.graphics.drawable;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.annotation.RestrictTo;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public interface TintAwareDrawable {
    @Override // android.support.v4.graphics.drawable.TintAwareDrawable
    void setTint(@ColorInt int i);

    @Override // android.support.v4.graphics.drawable.TintAwareDrawable
    void setTintList(ColorStateList colorStateList);

    @Override // android.support.v4.graphics.drawable.TintAwareDrawable
    void setTintMode(PorterDuff.Mode mode);
}
