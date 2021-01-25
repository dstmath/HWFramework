package ohos.miscservices.wallpaper;

import java.util.ArrayList;
import java.util.Iterator;
import ohos.agp.colors.RgbColor;

public class WallpaperColorsCollection {
    private int mCount;
    private Iterator<RgbColor> mWallpaperColors;
    private ArrayList<RgbColor> mWallpaperColorsList;

    WallpaperColorsCollection(ArrayList<RgbColor> arrayList) {
        this.mWallpaperColors = arrayList.iterator();
        this.mCount = arrayList.size();
        this.mWallpaperColorsList = arrayList;
    }

    public RgbColor next() {
        return this.mWallpaperColors.next();
    }

    public int count() {
        return this.mCount;
    }

    public boolean hasNext() {
        return this.mWallpaperColors.hasNext();
    }

    public RgbColor getNthRepresentativeColor(int i) {
        if (i > this.mCount - 1) {
            return null;
        }
        return this.mWallpaperColorsList.get(i);
    }
}
