package huawei.com.android.internal.app;

import com.android.internal.app.HwLocaleHelperManagerEx;
import java.util.Locale;

public class HwLocaleHelperManagerExImpl implements HwLocaleHelperManagerEx {
    private static HwLocaleHelperManagerEx mInstance;

    public static HwLocaleHelperManagerEx getDefault() {
        if (mInstance == null) {
            mInstance = new HwLocaleHelperManagerExImpl();
        }
        return mInstance;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public String replaceTaiwan2TaiwanChina(Locale locale, Locale displayLocale, String result) {
        char c;
        String localCountry = locale.getCountry();
        String localLanguage = displayLocale.getLanguage();
        String localScript = displayLocale.getScript();
        if (result == null) {
            return "";
        }
        if ("TW".equals(localCountry)) {
            switch (localLanguage.hashCode()) {
                case 3116:
                    if (localLanguage.equals("am")) {
                        c = 1;
                        break;
                    }
                case 3121:
                    if (localLanguage.equals("ar")) {
                        c = 2;
                        break;
                    }
                case 3122:
                    if (localLanguage.equals("as")) {
                        c = 5;
                        break;
                    }
                case 3139:
                    if (localLanguage.equals("be")) {
                        c = 6;
                        break;
                    }
                case 3141:
                    if (localLanguage.equals("bg")) {
                        c = 7;
                        break;
                    }
                case 3149:
                    if (localLanguage.equals("bo")) {
                        c = 19;
                        break;
                    }
                case 3166:
                    if (localLanguage.equals("ca")) {
                        c = 8;
                        break;
                    }
                case 3247:
                    if (localLanguage.equals("et")) {
                        c = 16;
                        break;
                    }
                case 3301:
                    if (localLanguage.equals("gl")) {
                        c = 9;
                        break;
                    }
                case 3374:
                    if (localLanguage.equals("iw")) {
                        c = 4;
                        break;
                    }
                case 3404:
                    if (localLanguage.equals("jv")) {
                        c = 18;
                        break;
                    }
                case 3464:
                    if (localLanguage.equals("lt")) {
                        c = 17;
                        break;
                    }
                case 3484:
                    if (localLanguage.equals("mi")) {
                        c = 10;
                        break;
                    }
                case 3487:
                    if (localLanguage.equals("ml")) {
                        c = 15;
                        break;
                    }
                case 3500:
                    if (localLanguage.equals("my")) {
                        c = 3;
                        break;
                    }
                case 3555:
                    if (localLanguage.equals("or")) {
                        c = 11;
                        break;
                    }
                case 3569:
                    if (localLanguage.equals("pa")) {
                        c = 12;
                        break;
                    }
                case 3684:
                    if (localLanguage.equals("sw")) {
                        c = 13;
                        break;
                    }
                case 3730:
                    if (localLanguage.equals("ug")) {
                        c = 20;
                        break;
                    }
                case 3741:
                    if (localLanguage.equals("ur")) {
                        c = 14;
                        break;
                    }
                case 3886:
                    if (localLanguage.equals("zh")) {
                        c = 0;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    if (!"Hant".equals(localScript)) {
                        result = "中国台湾";
                        break;
                    } else {
                        result = "中國台灣";
                        break;
                    }
                case 1:
                    result = "ታይዋን፣ ቻይና";
                    break;
                case 2:
                    result = "تايوان، الصين";
                    break;
                case 3:
                    if (Locale.getDefault().getCountry().indexOf("ZG") == -1) {
                        result = "ထိုင်ဝမ်၊ တရုတ်";
                        break;
                    } else {
                        result = "ထိုင္ဝမ္၊ တရုတ္";
                        break;
                    }
                case 4:
                    result = "טייוואן, סין";
                    break;
                case 5:
                    result = "টাইৱান, চীন";
                    break;
                case 6:
                    result = "Тайвань, Кітай";
                    break;
                case 7:
                    result = "Тайван, Китай";
                    break;
                case 8:
                    result = "Taiwan, Xina";
                    break;
                case 9:
                    result = "Taiwán, China";
                    break;
                case 10:
                    result = "Taiwana, Haina";
                    break;
                case 11:
                    result = "ତାଇୱାନ, ଚାଇନା";
                    break;
                case 12:
                    result = "ਤਾਈਵਾਨ, ਚੀਨ";
                    break;
                case 13:
                    result = "Taiwani, Uchina";
                    break;
                case 14:
                    result = "تائیوان، چین";
                    break;
                case 15:
                    result = "തായ്വാൻ, ചൈന";
                    break;
                case 16:
                    result = "Taiwan, China";
                    break;
                case 17:
                    result = "Taivanas (Kinija)";
                    break;
                case 18:
                    result = "Taiwan, Tiongkok";
                    break;
                case 19:
                    result = "ཀྲུང་གོའི་ཐའེ་ཝན།";
                    break;
                case 20:
                    result = "تەيۋەن، جوڭگو";
                    break;
                default:
                    new Locale(localLanguage, "CN");
                    result = result + ", " + cnLocale.getDisplayCountry();
                    break;
            }
        }
        return result;
    }
}
