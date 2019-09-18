package com.android.internal.graphics.palette;

import android.graphics.Color;
import android.util.TimingLogger;
import com.android.internal.graphics.ColorUtils;
import com.android.internal.graphics.palette.Palette;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

final class ColorCutQuantizer implements Quantizer {
    static final int COMPONENT_BLUE = -1;
    static final int COMPONENT_GREEN = -2;
    static final int COMPONENT_RED = -3;
    private static final String LOG_TAG = "ColorCutQuantizer";
    private static final boolean LOG_TIMINGS = false;
    private static final int QUANTIZE_WORD_MASK = 31;
    private static final int QUANTIZE_WORD_WIDTH = 5;
    private static final Comparator<Vbox> VBOX_COMPARATOR_VOLUME = new Comparator<Vbox>() {
        public int compare(Vbox lhs, Vbox rhs) {
            return rhs.getVolume() - lhs.getVolume();
        }
    };
    int[] mColors;
    Palette.Filter[] mFilters;
    int[] mHistogram;
    List<Palette.Swatch> mQuantizedColors;
    private final float[] mTempHsl = new float[3];
    TimingLogger mTimingLogger;

    private class Vbox {
        private int mLowerIndex;
        private int mMaxBlue;
        private int mMaxGreen;
        private int mMaxRed;
        private int mMinBlue;
        private int mMinGreen;
        private int mMinRed;
        private int mPopulation;
        private int mUpperIndex;

        Vbox(int lowerIndex, int upperIndex) {
            this.mLowerIndex = lowerIndex;
            this.mUpperIndex = upperIndex;
            fitBox();
        }

        /* access modifiers changed from: package-private */
        public final int getVolume() {
            return ((this.mMaxRed - this.mMinRed) + 1) * ((this.mMaxGreen - this.mMinGreen) + 1) * ((this.mMaxBlue - this.mMinBlue) + 1);
        }

        /* access modifiers changed from: package-private */
        public final boolean canSplit() {
            return getColorCount() > 1;
        }

        /* access modifiers changed from: package-private */
        public final int getColorCount() {
            return (1 + this.mUpperIndex) - this.mLowerIndex;
        }

        /* access modifiers changed from: package-private */
        public final void fitBox() {
            int[] colors = ColorCutQuantizer.this.mColors;
            int[] hist = ColorCutQuantizer.this.mHistogram;
            int minRed = Integer.MAX_VALUE;
            int minBlue = Integer.MAX_VALUE;
            int minGreen = Integer.MAX_VALUE;
            int maxRed = Integer.MIN_VALUE;
            int maxBlue = Integer.MIN_VALUE;
            int maxGreen = Integer.MIN_VALUE;
            int count = 0;
            for (int i = this.mLowerIndex; i <= this.mUpperIndex; i++) {
                int color = colors[i];
                count += hist[color];
                int r = ColorCutQuantizer.quantizedRed(color);
                int g = ColorCutQuantizer.quantizedGreen(color);
                int b = ColorCutQuantizer.quantizedBlue(color);
                if (r > maxRed) {
                    maxRed = r;
                }
                if (r < minRed) {
                    minRed = r;
                }
                if (g > maxGreen) {
                    maxGreen = g;
                }
                if (g < minGreen) {
                    minGreen = g;
                }
                if (b > maxBlue) {
                    maxBlue = b;
                }
                if (b < minBlue) {
                    minBlue = b;
                }
            }
            this.mMinRed = minRed;
            this.mMaxRed = maxRed;
            this.mMinGreen = minGreen;
            this.mMaxGreen = maxGreen;
            this.mMinBlue = minBlue;
            this.mMaxBlue = maxBlue;
            this.mPopulation = count;
        }

        /* access modifiers changed from: package-private */
        public final Vbox splitBox() {
            if (canSplit()) {
                int splitPoint = findSplitPoint();
                Vbox newBox = new Vbox(splitPoint + 1, this.mUpperIndex);
                this.mUpperIndex = splitPoint;
                fitBox();
                return newBox;
            }
            throw new IllegalStateException("Can not split a box with only 1 color");
        }

        /* access modifiers changed from: package-private */
        public final int getLongestColorDimension() {
            int redLength = this.mMaxRed - this.mMinRed;
            int greenLength = this.mMaxGreen - this.mMinGreen;
            int blueLength = this.mMaxBlue - this.mMinBlue;
            if (redLength >= greenLength && redLength >= blueLength) {
                return -3;
            }
            if (greenLength < redLength || greenLength < blueLength) {
                return -1;
            }
            return -2;
        }

        /* access modifiers changed from: package-private */
        public final int findSplitPoint() {
            int longestDimension = getLongestColorDimension();
            int[] colors = ColorCutQuantizer.this.mColors;
            int[] hist = ColorCutQuantizer.this.mHistogram;
            ColorCutQuantizer.modifySignificantOctet(colors, longestDimension, this.mLowerIndex, this.mUpperIndex);
            Arrays.sort(colors, this.mLowerIndex, this.mUpperIndex + 1);
            ColorCutQuantizer.modifySignificantOctet(colors, longestDimension, this.mLowerIndex, this.mUpperIndex);
            int midPoint = this.mPopulation / 2;
            int count = 0;
            for (int i = this.mLowerIndex; i <= this.mUpperIndex; i++) {
                count += hist[colors[i]];
                if (count >= midPoint) {
                    return Math.min(this.mUpperIndex - 1, i);
                }
            }
            return this.mLowerIndex;
        }

        /* access modifiers changed from: package-private */
        public final Palette.Swatch getAverageColor() {
            int[] colors = ColorCutQuantizer.this.mColors;
            int[] hist = ColorCutQuantizer.this.mHistogram;
            int redSum = 0;
            int greenSum = 0;
            int blueSum = 0;
            int totalPopulation = 0;
            for (int i = this.mLowerIndex; i <= this.mUpperIndex; i++) {
                int color = colors[i];
                int colorPopulation = hist[color];
                totalPopulation += colorPopulation;
                redSum += ColorCutQuantizer.quantizedRed(color) * colorPopulation;
                greenSum += ColorCutQuantizer.quantizedGreen(color) * colorPopulation;
                blueSum += ColorCutQuantizer.quantizedBlue(color) * colorPopulation;
            }
            return new Palette.Swatch(ColorCutQuantizer.approximateToRgb888(Math.round(((float) redSum) / ((float) totalPopulation)), Math.round(((float) greenSum) / ((float) totalPopulation)), Math.round(((float) blueSum) / ((float) totalPopulation))), totalPopulation);
        }
    }

    ColorCutQuantizer() {
    }

    public void quantize(int[] pixels, int maxColors, Palette.Filter[] filters) {
        this.mTimingLogger = null;
        this.mFilters = filters;
        int[] hist = new int[32768];
        this.mHistogram = hist;
        for (int i = 0; i < pixels.length; i++) {
            int quantizedColor = quantizeFromRgb888(pixels[i]);
            pixels[i] = quantizedColor;
            hist[quantizedColor] = hist[quantizedColor] + 1;
        }
        int distinctColorCount = 0;
        for (int color = 0; color < hist.length; color++) {
            if (hist[color] > 0 && shouldIgnoreColor(color)) {
                hist[color] = 0;
            }
            if (hist[color] > 0) {
                distinctColorCount++;
            }
        }
        int[] colors = new int[distinctColorCount];
        this.mColors = colors;
        int distinctColorIndex = 0;
        for (int color2 = 0; color2 < hist.length; color2++) {
            if (hist[color2] > 0) {
                colors[distinctColorIndex] = color2;
                distinctColorIndex++;
            }
        }
        if (distinctColorCount <= maxColors) {
            this.mQuantizedColors = new ArrayList();
            for (int color3 : colors) {
                this.mQuantizedColors.add(new Palette.Swatch(approximateToRgb888(color3), hist[color3]));
            }
            return;
        }
        this.mQuantizedColors = quantizePixels(maxColors);
    }

    public List<Palette.Swatch> getQuantizedColors() {
        return this.mQuantizedColors;
    }

    private List<Palette.Swatch> quantizePixels(int maxColors) {
        PriorityQueue<Vbox> pq = new PriorityQueue<>(maxColors, VBOX_COMPARATOR_VOLUME);
        pq.offer(new Vbox(0, this.mColors.length - 1));
        splitBoxes(pq, maxColors);
        return generateAverageColors(pq);
    }

    private void splitBoxes(PriorityQueue<Vbox> queue, int maxSize) {
        while (queue.size() < maxSize) {
            Vbox vbox = queue.poll();
            if (vbox != null && vbox.canSplit()) {
                queue.offer(vbox.splitBox());
                queue.offer(vbox);
            } else {
                return;
            }
        }
    }

    private List<Palette.Swatch> generateAverageColors(Collection<Vbox> vboxes) {
        ArrayList<Palette.Swatch> colors = new ArrayList<>(vboxes.size());
        for (Vbox vbox : vboxes) {
            Palette.Swatch swatch = vbox.getAverageColor();
            if (!shouldIgnoreColor(swatch)) {
                colors.add(swatch);
            }
        }
        return colors;
    }

    static void modifySignificantOctet(int[] a, int dimension, int lower, int upper) {
        switch (dimension) {
            case -2:
                for (int i = lower; i <= upper; i++) {
                    int color = a[i];
                    a[i] = (quantizedGreen(color) << 10) | (quantizedRed(color) << 5) | quantizedBlue(color);
                }
                return;
            case -1:
                for (int i2 = lower; i2 <= upper; i2++) {
                    int color2 = a[i2];
                    a[i2] = (quantizedBlue(color2) << 10) | (quantizedGreen(color2) << 5) | quantizedRed(color2);
                }
                return;
            default:
                return;
        }
    }

    private boolean shouldIgnoreColor(int color565) {
        int rgb = approximateToRgb888(color565);
        ColorUtils.colorToHSL(rgb, this.mTempHsl);
        return shouldIgnoreColor(rgb, this.mTempHsl);
    }

    private boolean shouldIgnoreColor(Palette.Swatch color) {
        return shouldIgnoreColor(color.getRgb(), color.getHsl());
    }

    private boolean shouldIgnoreColor(int rgb, float[] hsl) {
        if (this.mFilters != null && this.mFilters.length > 0) {
            for (Palette.Filter isAllowed : this.mFilters) {
                if (!isAllowed.isAllowed(rgb, hsl)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int quantizeFromRgb888(int color) {
        int r = modifyWordWidth(Color.red(color), 8, 5);
        int g = modifyWordWidth(Color.green(color), 8, 5);
        return (r << 10) | (g << 5) | modifyWordWidth(Color.blue(color), 8, 5);
    }

    static int approximateToRgb888(int r, int g, int b) {
        return Color.rgb(modifyWordWidth(r, 5, 8), modifyWordWidth(g, 5, 8), modifyWordWidth(b, 5, 8));
    }

    private static int approximateToRgb888(int color) {
        return approximateToRgb888(quantizedRed(color), quantizedGreen(color), quantizedBlue(color));
    }

    static int quantizedRed(int color) {
        return (color >> 10) & 31;
    }

    static int quantizedGreen(int color) {
        return (color >> 5) & 31;
    }

    static int quantizedBlue(int color) {
        return color & 31;
    }

    private static int modifyWordWidth(int value, int currentWidth, int targetWidth) {
        int newValue;
        if (targetWidth > currentWidth) {
            newValue = value << (targetWidth - currentWidth);
        } else {
            newValue = value >> (currentWidth - targetWidth);
        }
        return newValue & ((1 << targetWidth) - 1);
    }
}
