package ohos.utils.zson;

import java.io.IOException;
import java.lang.reflect.Field;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.JSONReader;
import ohos.utils.fastjson.parser.DefaultJSONParser;
import ohos.utils.fastjson.parser.JSONLexer;

public class ZSONTools {

    /* access modifiers changed from: package-private */
    public interface Caller<M> {
        M call();
    }

    interface VoidCaller {
        void call() throws IOException;
    }

    private static boolean isBlank(char c) {
        return c == ' ' || c == '\t' || c == '\b' || c == '\f' || c == '\n' || c == '\r';
    }

    private static boolean isDecimalPoint(char c) {
        return c == '.';
    }

    private static boolean isExponent(char c) {
        return c == 'e' || c == 'E';
    }

    private static boolean isLiteralMinus(char c) {
        return c == '-';
    }

    private static boolean isLiteralNumber(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isLiteralPlus(char c) {
        return c == '+';
    }

    private static boolean isSeparator(char c) {
        return c == ' ' || c == ',' || c == '}' || c == ']' || c == '\n' || c == '\r' || c == '\t' || c == 26 || c == '\f' || c == '\b' || c == ':';
    }

    private ZSONTools() {
    }

    public static <M> M callFastJson(Caller<M> caller) {
        try {
            return caller.call();
        } catch (JSONException e) {
            throw new ZSONException(e);
        }
    }

    public static void callFastJson(VoidCaller voidCaller) {
        try {
            voidCaller.call();
        } catch (IOException | JSONException e) {
            throw new ZSONException(e);
        }
    }

    public static int nextJsonToken(JSONReader jSONReader) {
        Object reflectFieldValue = reflectFieldValue(jSONReader, "parser");
        if (reflectFieldValue instanceof DefaultJSONParser) {
            DefaultJSONParser defaultJSONParser = (DefaultJSONParser) reflectFieldValue;
            Object reflectFieldValue2 = reflectFieldValue(defaultJSONParser.lexer, "ch");
            Object reflectFieldValue3 = reflectFieldValue(defaultJSONParser.lexer, "bp");
            Object reflectFieldValue4 = reflectFieldValue(defaultJSONParser.lexer, "len");
            Object reflectFieldValue5 = reflectFieldValue(defaultJSONParser.lexer, "text");
            if (reflectFieldValue2 != null && reflectFieldValue3 != null && reflectFieldValue4 != null && reflectFieldValue5 != null) {
                return nextToken(((Character) reflectFieldValue2).charValue(), ((Integer) reflectFieldValue3).intValue(), ((Integer) reflectFieldValue4).intValue(), (String) reflectFieldValue5);
            }
            throw new ZSONException("JSONParser lexer field error");
        }
        throw new ZSONException("JSONParser instance error");
    }

    private static int nextToken(char c, int i, int i2, String str) {
        while (c != '[') {
            if (c == '{') {
                return 12;
            }
            if (c == '\"') {
                return 4;
            }
            if (isLiteralNumber(c) || isLiteralMinus(c)) {
                return parseNumberType(c, i, i2, str);
            }
            if (isBlank(c)) {
                i++;
                c = nextChar(i, i2, str);
            } else if (c == 't') {
                if (!str.startsWith("true", i) || !isSeparator(nextChar(i + 4, i2, str))) {
                    return 23;
                }
                return 6;
            } else if (c == 'f') {
                if (!str.startsWith("false", i) || !isSeparator(nextChar(i + 5, i2, str))) {
                    return 23;
                }
                return 7;
            } else if (c != 'n' || !str.startsWith("null", i) || !isSeparator(nextChar(i + 4, i2, str))) {
                return 23;
            } else {
                return 8;
            }
        }
        return 14;
    }

    private static char nextChar(int i, int i2, String str) {
        return i >= i2 ? JSONLexer.EOI : str.charAt(i);
    }

    private static int parseNumberType(char c, int i, int i2, String str) {
        boolean z;
        if (isLiteralMinus(c)) {
            i++;
            c = nextChar(i, i2, str);
        }
        while (isLiteralNumber(c)) {
            i++;
            c = nextChar(i, i2, str);
        }
        if (isDecimalPoint(c)) {
            i++;
            c = nextChar(i, i2, str);
            while (isLiteralNumber(c)) {
                i++;
                c = nextChar(i, i2, str);
            }
            z = true;
        } else {
            z = false;
        }
        if (isExponent(c)) {
            int i3 = i + 1;
            c = nextChar(i3, i2, str);
            if (isLiteralPlus(c) || isLiteralMinus(c)) {
                i3++;
                c = nextChar(i3, i2, str);
            }
            while (isLiteralNumber(c)) {
                i3++;
                c = nextChar(i3, i2, str);
            }
            z = true;
        }
        if (!isSeparator(c)) {
            return 23;
        }
        return z ? 3 : 2;
    }

    private static Object reflectFieldValue(Object obj, String str) {
        Field field = null;
        try {
            field = obj.getClass().getDeclaredField(str);
            field.setAccessible(true);
            Object obj2 = field.get(obj);
            field.setAccessible(false);
            return obj2;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new ZSONException(e);
        } catch (Throwable th) {
            if (field != null) {
                field.setAccessible(false);
            }
            throw th;
        }
    }
}
