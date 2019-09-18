package com.huawei.android.util;

import android.content.Context;
import android.provider.SettingsEx;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

public class HwPasswordUtils {
    private static final String[] simplePasswordSet = {"kiss", "princess", "pass", "password", "asdfghj", "fdsa", "pwd", "wert", "werty", "wertyu", "wertyui", "erty", "ertyu", "ertyui", "sdfg", "sdfgh", "sdfghj", "dfgh", "xcvb", "xcvbn", "cvbn", "cvbnm", "qwer", "qwert", "qwerty", "asdf", "asdfg", "asdfgh", "vcxz", "love", "zxcv", "zxcvb", "zxcvbn", "qwertyu", "iloveyou", "imissu", "football", "ilovey", "iloveyo", "ilove", "qwertyui", "rewq", "trewq", "passwrd", "qazwsx", "qweasd", "hello", "imiss", "abc123", "password1"};
    private static HashMap<String, String> simplePasswordTable = new HashMap<>();

    public static void loadSimplePasswordTable(Context context) {
        for (String str : simplePasswordSet) {
            simplePasswordTable.put(str, null);
        }
        String simplePasswordSequence = SettingsEx.Systemex.getString(context.getContentResolver(), "simple_password_table");
        if (simplePasswordSequence != null && !simplePasswordSequence.isEmpty()) {
            for (String str2 : simplePasswordSequence.split(",")) {
                simplePasswordTable.put(str2.trim().toLowerCase(Locale.getDefault()), null);
            }
        }
    }

    public static boolean isSimpleAlphaNumericPassword(String password) {
        if (password == null) {
            return true;
        }
        return Pattern.compile("(\\w)\\1{1,}|(\\w{2,})\\2{1,}|(\\w)\\3{1,}(\\w)\\4{1,}|(\\w)\\5{1,}(\\w)\\6{1,}(\\w)\\7{1,}").matcher(password).matches();
    }

    public static boolean isOrdinalCharatersPassword(String password) {
        if (password == null) {
            return true;
        }
        int length = password.length();
        for (int i = 0; i < length - 1; i++) {
            int difference = password.charAt(i) - password.charAt(i + 1);
            if (difference != 1 && difference != -1) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSimplePasswordInDictationary(String password) {
        if (password == null || simplePasswordTable.containsKey(password) || simplePasswordTable.containsKey(password.toLowerCase(Locale.getDefault()))) {
            return true;
        }
        return false;
    }
}
