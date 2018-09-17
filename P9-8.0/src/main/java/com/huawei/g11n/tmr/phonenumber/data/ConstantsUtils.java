package com.huawei.g11n.tmr.phonenumber.data;

import java.util.HashMap;

public class ConstantsUtils {
    public static final String AI = "aite";
    public static final String DATE = "date";
    public static final String DATE1 = "date1";
    public static final String DATE2 = "date2";
    public static final String DP = "dateperiod";
    public static final String EMAIL = "email";
    public static final String EXP = "exp";
    public static final String FLOAT_1 = "float_1";
    public static final String FLOAT_2 = "float_2";
    public static final String SAME_NUM = "samenum";
    public static final String TIME = "time";
    public static final String URL = "url";
    public static final String YEAR_PERIOD = "yearperiod";
    private HashMap<String, String> map;

    public ConstantsUtils() {
        init();
    }

    private void init() {
        this.map = new HashMap<String, String>() {
            {
                put(ConstantsUtils.AI, "[@#][a-zA-Z_0-9-]{0,20}[0-9]{4,}[a-zA-Z0-9_-]{0,20}");
                put(ConstantsUtils.EMAIL, "[a-zA-Z_0-9]{1,20}@[a-zA-Z_0-9]{1,20}\\.[A-Za-z]{1,10}");
                put(ConstantsUtils.URL, "(?<![a-zA-Z_0-9.@])((https?|ftp)://)?([a-zA-Z_0-9][a-zA-Z0-9_-]*(\\.[a-zA-Z0-9_-]{1,20})*\\.(org|com|edu|net|[a-z]{2})|(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2})(?![a-zA-Z0-9_.])(:[1-9][0-9]{0,4})?(/([a-zA-Z0-9/_.\\p{Punct}]*(\\?\\S+)?)?)?(?![a-zA-Z_0-9])");
                put(ConstantsUtils.DATE, "(?<!\\d)(?:[012]?\\d|3[01])\\p{Blank}{0,2}(\\.|-|\\/)\\p{Blank}{0,2}(0?[1-9]|1[0-2])\\p{Blank}{0,2}\\1\\p{Blank}{0,2}(20[01][0-9]|19\\d{2})(?!\\d)");
                put(ConstantsUtils.DATE1, "(?<!\\d)(?:(?:0?[1-9]|[1-2]\\d|3[01])\\p{Blank}{0,2}(\\.|\\s|-|\\/)\\p{Blank}{0,2}(?:0?[1-9]|[1-2]\\d|3[01])(?:\\p{Blank}{0,2}\\1\\p{Blank}{0,2}(?:20[01][1-9]|19\\d{2}))?|(?:20[01][0-9]|19\\d{2})\\p{Blank}{0,2}(\\.|\\s|-|\\/)\\p{Blank}{0,2}(?:0?[1-9]|[1-2]\\d|3[01])\\p{Blank}{0,2}\\2\\p{Blank}{0,2}(?:0?[1-9]|[1-2]\\d|3[01])\\p{Blank}{0,2})\\p{Blank}{0,2},?\\p{Blank}{0,2}(?:[01]?\\d|2[0-4])\\p{Blank}{0,2}:\\p{Blank}{0,2}[0-5]\\d(?:\\p{Blank}{0,2}:\\p{Blank}{0,2}[0-5]\\d)?(?!(\\d|\\.\\d))");
                put(ConstantsUtils.DATE2, "(?<!\\d)(?:[01]?\\d|2[0-4])\\p{Blank}{0,2}:\\p{Blank}{0,2}[0-5]\\d(?:\\p{Blank}{0,2}:\\p{Blank}{0,2}[0-5]\\d)?\\p{Blank}{0,2}(?:(?:0?[1-9]|[1-2]\\d|3[01])\\p{Blank}{0,2}(\\.|\\s|-|\\/)\\p{Blank}{0,2}(?:0?[1-9]|[1-2]\\d|3[01])(?:\\p{Blank}{0,2}\\1\\p{Blank}{0,2}(?:20[01][0-9]|19\\d{2}))?|(?:20[01][0-9]|19\\d{2})\\p{Blank}{0,2}(\\.|\\s|-|\\/)\\p{Blank}{0,2}(?:0?[1-9]|[1-5]\\d)\\p{Blank}{0,2}\\3\\p{Blank}{0,2}(?:0?[1-9]|[1-5]\\d)\\p{Blank}{0,2})(?!(\\d|\\.\\d))");
                put(ConstantsUtils.TIME, "(?<![\\d:.])(?:[01]?\\d|2[0-4])(:)[0-5]\\d(?:\\1[0-5]\\d)?");
                put(ConstantsUtils.YEAR_PERIOD, "20[01]\\d\\p{Blank}{0,2}-\\p{Blank}{0,2}20[01]\\d");
                put(ConstantsUtils.DP, "(?<![\\d:.])(?:[01]?\\d|2[0-4])([.:])[0-5]\\d(?:\\1[0-5]\\d)?\\p{Blank}{0,2}-\\p{Blank}{0,2}(?:[01]?\\d|2[0-4])\\1[0-5]\\d(?:\\1[0-5]\\d)?(?!(\\d|[.]\\d))");
                put(ConstantsUtils.SAME_NUM, "(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,})");
                put(ConstantsUtils.EXP, "[0-9]+\\p{Blank}{0,2}([Xx*/+-])\\p{Blank}{0,2}[0-9]+(\\p{Blank}{0,2}([Xx*/+-])\\p{Blank}{0,2}[0-9]+)*\\p{Blank}{0,2}=\\p{Blank}{0,2}[0-9]+");
                put(ConstantsUtils.FLOAT_2, "(?<!\\d)[1-9][0-9]{0,2}(?:(?:[,\\p{Blank}]\\d{3})*|\\d{0,13})(\\.\\d{1,3})(?!\\d)");
                put(ConstantsUtils.FLOAT_1, "(?<!\\d)[1-9][0-9]{0,2}(?:(?:[.\\p{Blank}]\\d{3})*|\\d{0,13})(\\,\\d{1,3})(?!\\d)");
            }
        };
    }

    public String getValues(String key) {
        if (this.map.containsKey(key.trim())) {
            return (String) this.map.get(key.trim());
        }
        return "";
    }
}
