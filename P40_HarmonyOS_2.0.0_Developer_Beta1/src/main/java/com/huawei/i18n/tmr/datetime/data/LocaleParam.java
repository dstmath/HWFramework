package com.huawei.i18n.tmr.datetime.data;

import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.phonenumber.data.ConstantsUtils;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocaleParam {
    public static final String SAVE_OR = "\\u0604";
    private static final String TAG = "LocaleParam";
    private static final String WITHNOT_B = "\\u0605";
    private static HashMap<String, String> support = new HashMap<String, String>() {
        /* class com.huawei.i18n.tmr.datetime.data.LocaleParam.AnonymousClass1 */

        {
            put("am", "LocaleParamGetAm");
            put("ar", "LocaleParamGetAr");
            put("as", "LocaleParamGetAs");
            put("az", "LocaleParamGetAz");
            put("be", "LocaleParamGetBe");
            put("bg", "LocaleParamGetBg");
            put("bn", "LocaleParamGetBn");
            put("bo", "LocaleParamGetBo");
            put("bs", "LocaleParamGetBs");
            put("ca", "LocaleParamGetCa");
            put("cs", "LocaleParamGetCs");
            put("da", "LocaleParamGetDa");
            put("de", "LocaleParamGetDe");
            put("el", "LocaleParamGetEl");
            put("en", "LocaleParamGetEn");
            put("es", "LocaleParamGetEs");
            put("es_MX", "LocaleParamGetEsMX");
            put("et", "LocaleParamGetEt");
            put("eu", "LocaleParamGetEu");
            put("fa", "LocaleParamGetFa");
            put("fi", "LocaleParamGetFi");
            put("fil", "LocaleParamGetFil");
            put("fr", "LocaleParamGetFr");
            put("gl", "LocaleParamGetGl");
            put("gu", "LocaleParamGetGu");
            put("he", "LocaleParamGetHe");
            put("hi", "LocaleParamGetHi");
            put("hr", "LocaleParamGetHr");
            put("hu", "LocaleParamGetHu");
            put("id", "LocaleParamGetId");
            put("it", "LocaleParamGetIt");
            put("ja", "LocaleParamGetJa");
            put("jv", "LocaleParamGetJv");
            put("ka", "LocaleParamGetKa");
            put("kk", "LocaleParamGetKk");
            put("km", "LocaleParamGetKm");
            put("kn", "LocaleParamGetKn");
            put("ko", "LocaleParamGetKo");
            put("lo", "LocaleParamGetLo");
            put("lt", "LocaleParamGetLt");
            put("lv", "LocaleParamGetLv");
            put("mai", "LocaleParamGetMai");
            put("mi", "LocaleParamGetMi");
            put("mk", "LocaleParamGetMk");
            put("ml", "LocaleParamGetMl");
            put("mn", "LocaleParamGetMn");
            put("mr", "LocaleParamGetMr");
            put("ms", "LocaleParamGetMs");
            put("my", "LocaleParamGetMy");
            put("nb", "LocaleParamGetNb");
            put("ne", "LocaleParamGetNe");
            put("nl", "LocaleParamGetNl");
            put("or", "LocaleParamGetOr");
            put("pa", "LocaleParamGetPa");
            put("pl", "LocaleParamGetPl");
            put("pt", "LocaleParamGetPt");
            put("ro", "LocaleParamGetRo");
            put("ru", "LocaleParamGetRu");
            put("si", "LocaleParamGetSi");
            put("sk", "LocaleParamGetSk");
            put("sl", "LocaleParamGetSl");
            put("sr", "LocaleParamGetSr");
            put("sv", "LocaleParamGetSv");
            put("sw", "LocaleParamGetSw");
            put("ta", "LocaleParamGetTa");
            put("te", "LocaleParamGetTe");
            put("th", "LocaleParamGetTh");
            put("tr", "LocaleParamGetTr");
            put("ug", "LocaleParamGetUg");
            put("uk", "LocaleParamGetUk");
            put("ur", "LocaleParamGetUr");
            put("uz", "LocaleParamGetUz");
            put("vi", "LocaleParamGetVi");
            put("zh_hans", "LocaleParamGetZhHans");
        }
    };
    private HashMap<String, String> defaultParam = new HashMap<String, String>() {
        /* class com.huawei.i18n.tmr.datetime.data.LocaleParam.AnonymousClass2 */

        {
            put("param_tmark", ":");
        }
    };
    private HashMap<String, String> delimiter = new HashMap<String, String>() {
        /* class com.huawei.i18n.tmr.datetime.data.LocaleParam.AnonymousClass3 */

        {
            put("en", "\\b");
            put("zh_hans", StorageManagerExt.INVALID_KEY_DESC);
            put("ko", StorageManagerExt.INVALID_KEY_DESC);
            put("ja", StorageManagerExt.INVALID_KEY_DESC);
            put("hi", StorageManagerExt.INVALID_KEY_DESC);
            put("he", StorageManagerExt.INVALID_KEY_DESC);
            put("km", StorageManagerExt.INVALID_KEY_DESC);
            put("bo", StorageManagerExt.INVALID_KEY_DESC);
            put("si", StorageManagerExt.INVALID_KEY_DESC);
            put("my", StorageManagerExt.INVALID_KEY_DESC);
            put("ne", StorageManagerExt.INVALID_KEY_DESC);
            put("bn", StorageManagerExt.INVALID_KEY_DESC);
            put("be", StorageManagerExt.INVALID_KEY_DESC);
            put("lo", StorageManagerExt.INVALID_KEY_DESC);
            put("kk", StorageManagerExt.INVALID_KEY_DESC);
            put("th", StorageManagerExt.INVALID_KEY_DESC);
            put("as", StorageManagerExt.INVALID_KEY_DESC);
            put("gu", StorageManagerExt.INVALID_KEY_DESC);
            put("kn", StorageManagerExt.INVALID_KEY_DESC);
            put("mr", StorageManagerExt.INVALID_KEY_DESC);
            put("ml", StorageManagerExt.INVALID_KEY_DESC);
            put("mai", StorageManagerExt.INVALID_KEY_DESC);
            put("mi", StorageManagerExt.INVALID_KEY_DESC);
            put("te", StorageManagerExt.INVALID_KEY_DESC);
            put("ta", StorageManagerExt.INVALID_KEY_DESC);
            put("ug", StorageManagerExt.INVALID_KEY_DESC);
        }
    };
    private String locale = "en";
    private HashMap<String, String> param = null;

    public LocaleParam(String locale2) {
        if (isSupport(locale2)) {
            this.locale = locale2;
            initParam(locale2);
        }
        if (this.param == null) {
            this.param = new LocaleParamGetEn().date;
        }
    }

    private void initParam(String locale2) {
        try {
            Class cls = Class.forName((String) Objects.requireNonNull("com.huawei.i18n.tmr.datetime.data." + support.get(locale2)));
            Object obj = cls.getConstructor(new Class[0]).newInstance(new Object[0]);
            Field dataField = cls.getDeclaredField(ConstantsUtils.DATE);
            if (dataField.get(obj) instanceof HashMap) {
                this.param = (HashMap) dataField.get(obj);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            Log.e(TAG, "initParam ClassNotFoundException");
        } catch (NoSuchFieldException e2) {
            Log.e(TAG, "initParam NoSuchFieldException");
        }
    }

    public static boolean isSupport(String locale2) {
        return support.containsKey(locale2);
    }

    public String get(String ruleName) {
        HashMap<String, String> hashMap = this.param;
        if (hashMap == null) {
            return StorageManagerExt.INVALID_KEY_DESC;
        }
        String result = hashMap.get(ruleName) != null ? this.param.get(ruleName) : StorageManagerExt.INVALID_KEY_DESC;
        if (result.trim().isEmpty() && this.defaultParam.containsKey(ruleName)) {
            result = this.defaultParam.get(ruleName);
        }
        if (result == null || result.trim().isEmpty()) {
            return result;
        }
        String[] temps = result.split("\\|");
        StringBuffer sb = new StringBuffer();
        Map delimiterMap = this.delimiter;
        String mark = StorageManagerExt.INVALID_KEY_DESC;
        if (!delimiterMap.containsKey(this.locale)) {
            mark = "\\b";
        } else if (delimiterMap.get(this.locale) instanceof String) {
            mark = delimiterMap.get(this.locale);
        }
        String mark2 = mark;
        for (String temp : temps) {
            if (!temp.trim().isEmpty()) {
                if (temp.equals(SAVE_OR)) {
                    sb.append("|");
                } else if (temp.equals(WITHNOT_B)) {
                    mark2 = StorageManagerExt.INVALID_KEY_DESC;
                } else {
                    if (!temp.startsWith("\\b")) {
                        sb.append(mark2);
                    }
                    sb.append(temp);
                    if (!temp.endsWith("\\b") && !temp.endsWith(".")) {
                        sb.append(mark2);
                    }
                    sb.append("|");
                }
            }
        }
        String result2 = sb.toString();
        if (result2.endsWith("|")) {
            return result2.substring(0, result2.length() - 1);
        }
        return result2;
    }

    public String getWithoutB(String ruleName) {
        HashMap<String, String> hashMap = this.param;
        if (hashMap == null) {
            return StorageManagerExt.INVALID_KEY_DESC;
        }
        String result = hashMap.get(ruleName) != null ? this.param.get(ruleName) : StorageManagerExt.INVALID_KEY_DESC;
        if (result.indexOf(WITHNOT_B) == -1) {
            return result;
        }
        String[] temps = result.split("\\|");
        StringBuffer sb = new StringBuffer();
        for (String temp : temps) {
            if (!temp.trim().isEmpty() && !temp.equals(SAVE_OR) && !temp.equals(WITHNOT_B)) {
                sb.append(temp);
                sb.append("|");
            }
        }
        String result2 = sb.toString();
        if (result2.endsWith("|")) {
            return result2.substring(0, result2.length() - 1);
        }
        return result2;
    }

    public static boolean isRelDates(String hyphen, String locale2) {
        if (hyphen.trim().isEmpty()) {
            return true;
        }
        if (",".equals(hyphen.trim())) {
            if ("zh_hans".equalsIgnoreCase(locale2) || "ja".equalsIgnoreCase(locale2) || "ko".equalsIgnoreCase(locale2)) {
                return false;
            }
            return true;
        } else if (!"،".equals(hyphen.trim()) || (!"ur".equalsIgnoreCase(locale2) && !"ug".equalsIgnoreCase(locale2))) {
            return false;
        } else {
            return true;
        }
    }
}
