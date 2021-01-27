package com.huawei.displayengine;

import android.graphics.PointF;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import com.huawei.displayengine.XmlData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XmlElement<T extends XmlData> {
    protected final boolean HW_DEBUG;
    protected final boolean HW_FLOW;
    protected final String TAG = getClass().getSimpleName();
    private Map<String, XmlElement> mChildBranchs;
    private List<Pair<String, Predicate<T>>> mChildCheckers;
    private Map<String, BiConsumer<String, T>> mChildParsers;

    public XmlElement() {
        boolean z = false;
        this.HW_DEBUG = Log.HWLog || (Log.HWModuleLog && Log.isLoggable(this.TAG, 3));
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(this.TAG, 4))) {
            z = true;
        }
        this.HW_FLOW = z;
    }

    /* access modifiers changed from: protected */
    public Map<String, BiConsumer<String, T>> getParser() {
        return Collections.emptyMap();
    }

    /* access modifiers changed from: protected */
    public List<Pair<String, Predicate<T>>> getChecker() {
        return Collections.emptyList();
    }

    /* access modifiers changed from: protected */
    public String getBranchName() {
        return null;
    }

    public final XmlElement registerChildElement(XmlElement element) {
        if (element == null) {
            Slog.e(this.TAG, "registerChildElement() error! input element is null!");
            return element;
        }
        Map<String, BiConsumer<String, T>> parserMap = element.getParser();
        if (parserMap == null || parserMap.isEmpty()) {
            String branchName = element.getBranchName();
            if (branchName != null) {
                if (this.mChildBranchs == null) {
                    this.mChildBranchs = new HashMap();
                }
                this.mChildBranchs.put(branchName, element);
            } else {
                Slog.e(this.TAG, "registerChildElement() error! can't get branch name");
            }
        } else {
            if (this.mChildParsers == null) {
                this.mChildParsers = new HashMap();
            }
            this.mChildParsers.putAll(parserMap);
        }
        List<Pair<String, Predicate<T>>> checker = element.getChecker();
        if (checker != null && !checker.isEmpty()) {
            List<Pair<String, Predicate<T>>> list = this.mChildCheckers;
            if (list == null) {
                this.mChildCheckers = checker;
            } else {
                list.addAll(checker);
            }
        }
        return element;
    }

    /* access modifiers changed from: package-private */
    public final void parse(XmlPullParser parser, T data) throws XmlPullParserException, IOException {
        int elementDepth = parser.getDepth();
        int type = parser.nextTag();
        while (parser.getDepth() > elementDepth) {
            if (type == 2) {
                parseSingleTag(parser, data);
            }
            type = parser.nextTag();
        }
    }

    private void parseSingleTag(XmlPullParser parser, T data) throws XmlPullParserException, IOException {
        XmlElement branchElement;
        String name = parser.getName();
        Map<String, BiConsumer<String, T>> map = this.mChildParsers;
        if (map != null) {
            BiConsumer<String, T> function = map.get(name);
            if (function != null) {
                function.accept(parser.nextText(), data);
                return;
            }
            String value = this.TAG;
            Slog.w(value, "invalid tag " + name);
            parser.nextText();
        }
        Map<String, XmlElement> map2 = this.mChildBranchs;
        if (map2 != null && (branchElement = map2.get(name)) != null) {
            branchElement.parse(parser, data);
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean check(T data) {
        boolean isCheckCorrect = true;
        List<Pair<String, Predicate<T>>> list = this.mChildCheckers;
        if (list != null) {
            for (Pair<String, Predicate<T>> checkerPair : list) {
                Predicate<T> function = (Predicate) checkerPair.second;
                if (function != null) {
                    isCheckCorrect = function.test(data);
                    continue;
                }
                if (!isCheckCorrect) {
                    String str = this.TAG;
                    Slog.e(str, "check() error! incorrect tag name is " + ((String) checkerPair.first));
                    return false;
                }
            }
        }
        Map<String, XmlElement> map = this.mChildBranchs;
        if (map != null) {
            for (XmlElement element : map.values()) {
                isCheckCorrect = element.check(data);
                if (!isCheckCorrect) {
                    return false;
                }
            }
        }
        return isCheckCorrect;
    }

    protected static boolean string2Boolean(String str) {
        if (!TextUtils.isEmpty(str)) {
            return Boolean.parseBoolean(str);
        }
        throw new IllegalArgumentException("string2Boolean() input str is null or empty!");
    }

    protected static int string2Int(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("string2Int() " + e);
            }
        } else {
            throw new IllegalArgumentException("string2Int() input str is null or empty!");
        }
    }

    protected static long string2Long(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("string2Long() " + e);
            }
        } else {
            throw new IllegalArgumentException("string2Long() input str is null or empty!");
        }
    }

    protected static float string2Float(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("string2Float() " + e);
            }
        } else {
            throw new IllegalArgumentException("string2Float() input str is null or empty!");
        }
    }

    protected static double string2Double(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("string2Double() " + e);
            }
        } else {
            throw new IllegalArgumentException("string2Double() input str is null or empty!");
        }
    }

    protected static List<PointF> string2PointList(String str, List<PointF> list) {
        if (!TextUtils.isEmpty(str)) {
            String[] pointSplited = str.split(",");
            if (pointSplited.length == 2) {
                float valueX = string2Float(pointSplited[0]);
                float valueY = string2Float(pointSplited[1]);
                List<PointF> result = list;
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(new PointF(valueX, valueY));
                return result;
            }
            throw new IllegalArgumentException("string2PointList() split failed, text=" + str);
        }
        throw new IllegalArgumentException("string2PointList() input str is null or empty!");
    }

    /* access modifiers changed from: protected */
    public String[] parseStringArray(String text, int size, String split) {
        if (size <= 0) {
            Slog.e(this.TAG, "parseStringArray size is invalid.");
            throw new IllegalArgumentException("parseStringArray() input size is invalid!");
        } else if (text != null) {
            String[] splitedContent = text.split(split, size);
            if (splitedContent.length >= size) {
                return splitedContent;
            }
            Slog.e(this.TAG, "input array length is not enough.");
            throw new IllegalArgumentException("parseStringArray() input array length is not enough!");
        } else {
            Slog.e(this.TAG, "parseStringArray text is null.");
            throw new IllegalArgumentException("parseStringArray() input str is null or empty!");
        }
    }

    /* access modifiers changed from: protected */
    public int[] parseIntArray(String text, int size, String split) {
        String[] splitedContent = parseStringArray(text, size, split);
        int[] parsedArray = new int[size];
        for (int index = 0; index < size; index++) {
            parsedArray[index] = string2Int(splitedContent[index]);
        }
        return parsedArray;
    }

    /* access modifiers changed from: protected */
    public double[] parseDoubleArray(String text, int size, String split) {
        String[] splitedContent = parseStringArray(text, size, split);
        double[] parsedArray = new double[size];
        for (int index = 0; index < size; index++) {
            parsedArray[index] = string2Double(splitedContent[index]);
        }
        return parsedArray;
    }
}
