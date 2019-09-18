package android.support.v4.widget;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public interface AutoSizeableTextView {
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static final boolean PLATFORM_SUPPORTS_AUTOSIZE = (Build.VERSION.SDK_INT >= 27);

    int getAutoSizeMaxTextSize();

    int getAutoSizeMinTextSize();

    int getAutoSizeStepGranularity();

    int[] getAutoSizeTextAvailableSizes();

    int getAutoSizeTextType();

    void setAutoSizeTextTypeUniformWithConfiguration(int i, int i2, int i3, int i4) throws IllegalArgumentException;

    void setAutoSizeTextTypeUniformWithPresetSizes(@NonNull int[] iArr, int i) throws IllegalArgumentException;

    void setAutoSizeTextTypeWithDefaults(int i);
}
