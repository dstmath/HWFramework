package com.android.internal.colorextraction.types;

import android.app.WallpaperColors;
import com.android.internal.colorextraction.ColorExtractor;

public interface ExtractionType {
    void extractInto(WallpaperColors wallpaperColors, ColorExtractor.GradientColors gradientColors, ColorExtractor.GradientColors gradientColors2, ColorExtractor.GradientColors gradientColors3);
}
