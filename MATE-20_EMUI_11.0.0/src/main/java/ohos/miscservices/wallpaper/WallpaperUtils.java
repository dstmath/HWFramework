package ohos.miscservices.wallpaper;

import android.app.WallpaperColors;
import android.content.Context;
import android.graphics.Color;
import java.util.ArrayList;
import ohos.agp.colors.RgbColor;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class WallpaperUtils {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "WallpaperUtils");

    private WallpaperUtils() {
    }

    public static WallpaperColorsCollection convertToWallpaperColorsCollection(WallpaperColors wallpaperColors) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new RgbColor(RgbColor.fromArgbInt(wallpaperColors.getPrimaryColor().toArgb())));
        Color secondaryColor = wallpaperColors.getSecondaryColor();
        if (secondaryColor != null) {
            arrayList.add(new RgbColor(RgbColor.fromArgbInt(secondaryColor.toArgb())));
        }
        Color tertiaryColor = wallpaperColors.getTertiaryColor();
        if (tertiaryColor != null) {
            arrayList.add(new RgbColor(RgbColor.fromArgbInt(tertiaryColor.toArgb())));
        }
        return new WallpaperColorsCollection(arrayList);
    }

    public static Context convertFromHospContext(ohos.app.Context context) {
        if (context == null) {
            HiLog.error(TAG, "convertFromHospContext:abilityContext is null!", new Object[0]);
            return null;
        }
        Object hostContext = context.getHostContext();
        if (hostContext instanceof Context) {
            return (Context) hostContext;
        }
        HiLog.error(TAG, "convertFromHospContext:get Context failed!", new Object[0]);
        return null;
    }
}
