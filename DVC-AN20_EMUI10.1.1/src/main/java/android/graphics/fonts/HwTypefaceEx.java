package android.graphics.fonts;

import android.graphics.FontListParser;
import android.os.SystemProperties;
import android.text.FontConfig;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

public class HwTypefaceEx {
    private static final String HWFONT_CONFIG_LOCATION = "/data/skin/fonts/";
    public static final String HWFONT_NAME = "HwFont.ttf";
    private static final String HWFONT_XMLCONFIG_TAG = "hw/";
    public static final String HW_CUSTOM_FONT_NAME = SystemProperties.get("ro.config.custom_font", null);
    public static final String HW_CUSTOM_FONT_PATH = "hw_product/fonts/";
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
        FontConfig.Family hwFamily = null;
        try {
            FontConfig.Family[] familyArray = FontListParser.parse(new FileInputStream(configFile), HWFONT_CONFIG_LOCATION).getFamilies();
            if (familyArray != null && familyArray.length > 0) {
                hwFamily = familyArray[0];
            }
            if (hwFamily == null) {
                return null;
            }
            FontConfig.Font[] fonts = hwFamily.getFonts();
            for (FontConfig.Font font : fonts) {
                if (font.getFontName() != null) {
                    font.setFontName(font.getFontName().replaceAll(HWFONT_XMLCONFIG_TAG, ""));
                }
            }
            return hwFamily;
        } catch (RuntimeException e) {
            Log.w(TAG, "Didn't create  family (most likely, non-Minikin build)", e);
            return null;
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "Error opening " + configFile);
            return null;
        } catch (IOException e3) {
            Log.e(TAG, "Error reading " + configFile, e3);
            return null;
        } catch (XmlPullParserException e4) {
            Log.e(TAG, "XML parse exception for " + configFile, e4);
            return null;
        }
    }

    public static void updateFont(FontFamily hwFontFamily) {
        if (hwFontFamily != null) {
            boolean currentHwFontsExist = false;
            FontConfig.Family hwMultyWeightFamily = null;
            File hwfontfile = new File(getHwFontConfig(), HW_DEFAULT_FONT_NAME);
            if (hwfontfile.exists()) {
                currentHwFontsExist = true;
            }
            if (!currentHwFontsExist) {
                hwMultyWeightFamily = getMultyWeightHwFamily();
            }
            hwFontFamily.resetFont();
            if (currentHwFontsExist) {
                hwFontFamily.buildHwFonts(hwfontfile, 0);
                hwFontFamily.buildHwFontFamily();
                hwFontFamily.setHwFontFamilyType(2);
            } else if (hwMultyWeightFamily != null) {
                FontConfig.Font[] fonts = hwMultyWeightFamily.getFonts();
                for (FontConfig.Font font : fonts) {
                    if (font.getFontName() != null) {
                        File fontfile = new File(font.getFontName());
                        if (fontfile.exists()) {
                            hwFontFamily.buildHwFonts(fontfile, font.getWeight());
                            hwFontFamily.buildHwFontFamily();
                        }
                        hwFontFamily.setHwFontFamilyType(2);
                    }
                }
            } else {
                hwFontFamily.setHwFontFamilyType(-1);
            }
            hwFontFamily.resetCoverage();
        }
    }
}
