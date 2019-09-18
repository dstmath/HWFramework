package android.graphics;

import android.os.SystemProperties;
import android.text.FontConfig;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

public class HwTypefaceUtil {
    private static final String HWFONT_CONFIG_LOCATION = "/data/skin/fonts/";
    public static final String HWFONT_NAME = "HwFont.ttf";
    private static final String HWFONT_XMLCONFIG_TAG = "hw";
    public static final String HW_CUSTOM_FONT_NAME = SystemProperties.get("ro.config.custom_font", null);
    public static final String HW_CUSTOM_FONT_PATH = "data/hw_init/product/fonts/";
    public static final String HW_DEFAULT_FONT_NAME = "DroidSansChinese.ttf";
    private static final String HW_FONTS_CONFIG_NAME = "HwEnglishfonts.xml";
    private static final String TAG = "HwTypeface";

    private static File getHwFontConfig() {
        return new File(HWFONT_CONFIG_LOCATION);
    }

    private static FontConfig.Family getMultyWeightHwFamily() {
        File systemFontConfigLocation = getHwFontConfig();
        if (!systemFontConfigLocation.exists()) {
            return null;
        }
        return parseHwFontConfig(new File(systemFontConfigLocation, HW_FONTS_CONFIG_NAME));
    }

    private static FontConfig.Family parseHwFontConfig(File configFile) {
        FontConfig.Family hwFamily;
        FontConfig.Family hwFamily2 = null;
        try {
            FontConfig.Family[] familyArray = FontListParser.parse(new FileInputStream(configFile)).getFamilies();
            if (familyArray != null && familyArray.length > 0) {
                hwFamily2 = familyArray[0];
            }
            if (hwFamily2 == null) {
                return null;
            }
            for (FontConfig.Font font : hwFamily2.getFonts()) {
                if (font.getFontName() != null) {
                    font.setFontName(font.getFontName().replaceAll(HWFONT_XMLCONFIG_TAG, getHwFontConfig().getAbsolutePath()));
                }
            }
            return hwFamily2;
        } catch (RuntimeException e) {
            hwFamily = null;
            Log.w(TAG, "Didn't create  family (most likely, non-Minikin build)", e);
            return hwFamily;
        } catch (FileNotFoundException e2) {
            hwFamily = null;
            Log.e(TAG, "Error opening " + configFile, e2);
            return hwFamily;
        } catch (IOException e3) {
            hwFamily = null;
            Log.e(TAG, "Error reading " + configFile, e3);
            return hwFamily;
        } catch (XmlPullParserException e4) {
            hwFamily = null;
            Log.e(TAG, "XML parse exception for " + configFile, e4);
            return hwFamily;
        }
    }

    public static void updateFont(FontFamily HwFontFamily) {
        if (HwFontFamily != null) {
            boolean currentHwFontsExist = false;
            FontConfig.Family hwMultyWeightFamily = null;
            File hwfontfile = new File(getHwFontConfig(), HW_DEFAULT_FONT_NAME);
            if (hwfontfile.exists()) {
                currentHwFontsExist = true;
            }
            if (!currentHwFontsExist) {
                hwMultyWeightFamily = getMultyWeightHwFamily();
            }
            HwFontFamily.resetFont();
            if (currentHwFontsExist) {
                HwFontFamily.hwAddFont(hwfontfile.getAbsolutePath(), 0, -1, -1);
                HwFontFamily.setHwFontFamilyType(2);
            } else if (hwMultyWeightFamily != null) {
                for (FontConfig.Font font : hwMultyWeightFamily.getFonts()) {
                    if (font.getFontName() != null && new File(font.getFontName()).exists()) {
                        String fontName = font.getFontName();
                        int weight = font.getWeight();
                        if (font.isItalic()) {
                        }
                        HwFontFamily.hwAddFont(fontName, 0, weight, 0);
                        HwFontFamily.setHwFontFamilyType(2);
                    }
                }
            } else {
                HwFontFamily.setHwFontFamilyType(-1);
            }
            HwFontFamily.resetCoverage();
        }
    }
}
