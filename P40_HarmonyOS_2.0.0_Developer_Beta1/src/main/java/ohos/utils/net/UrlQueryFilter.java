package ohos.utils.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.httpaccess.HttpConstant;
import ohos.utils.Pair;

public class UrlQueryFilter {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218119424, LOG_TAG);
    private static final String LOG_TAG = "UrlQueryFilter";
    private boolean allowUnregisteredParams = false;
    private boolean firstTimePriority = false;
    private final HashMap<String, ValueFilter> paramFiltersMap = new HashMap<>();
    private final ArrayList<Pair<String, String>> paramsList = new ArrayList<>();
    private final HashMap<String, String> paramsMap = new HashMap<>();
    private ValueFilter unregisteredParamFilter = new ValueFilter(0);

    private int hexCharToInt(char c) {
        char c2 = 'A';
        if (c < 'A' || c > 'F') {
            c2 = 'a';
            if (c < 'a' || c > 'f') {
                if (c < '0' || c > '9') {
                    return -1;
                }
                return c - '0';
            }
        }
        return (c - c2) + 10;
    }

    public static class ValueFilter {
        public static final int ALLOW_ALL = 2047;
        public static final int ALLOW_ALL_BUT_NUL = 2045;
        public static final int ALLOW_ESCHAR = 4;
        public static final int ALLOW_NOTHING = 0;
        public static final int ALLOW_NUL = 2;
        public static final int ALLOW_SPACE = 1;
        private int filterFlags;

        public ValueFilter(int i) {
            this.filterFlags = i;
        }

        public String filterUrl(String str) {
            if (str == null) {
                return null;
            }
            if ((this.filterFlags & 4) == 0) {
                str = filterSpaces(str);
            }
            int length = str.length();
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                char charAt = str.charAt(i);
                if (isCharLegal(charAt)) {
                    sb.append(charAt);
                } else if ((this.filterFlags & 1) != 0) {
                    sb.append(' ');
                } else {
                    sb.append('_');
                }
            }
            return sb.toString();
        }

        private String filterSpaces(String str) {
            int length = str.length() - 1;
            int i = 0;
            while (true) {
                if (i <= length) {
                    char charAt = str.charAt(i);
                    if (charAt != ' ' && charAt != '\t' && charAt != '\f' && charAt != '\n' && charAt != '\r' && charAt != 11) {
                        HiLog.debug(UrlQueryFilter.LOG_LABEL, "first non space found", new Object[0]);
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            }
            while (true) {
                if (length >= i) {
                    char charAt2 = str.charAt(length);
                    if (charAt2 != ' ' && charAt2 != '\t' && charAt2 != '\f' && charAt2 != '\n' && charAt2 != '\r' && charAt2 != 11) {
                        HiLog.debug(UrlQueryFilter.LOG_LABEL, "last non space found", new Object[0]);
                        break;
                    }
                    length--;
                } else {
                    break;
                }
            }
            if (i == 0 && length == str.length() - 1) {
                return str;
            }
            return str.substring(i, length + 1);
        }

        private boolean isCharLegal(char c) {
            if (c == 0) {
                return (this.filterFlags & 2) != 0;
            }
            if (c == ' ') {
                return (this.filterFlags & 1) != 0;
            }
            switch (c) {
                case '\t':
                case '\n':
                case 11:
                case '\f':
                case '\r':
                    return (this.filterFlags & 4) != 0;
                default:
                    return c >= ' ' && c <= 127;
            }
        }
    }

    public static final ValueFilter filterNulWithSpace() {
        return new ValueFilter(2045);
    }

    public List<Pair<String, String>> getParamsListFiltered() {
        return this.paramsList;
    }

    public Set<String> getParamsSetFiltered() {
        return this.paramsMap.keySet();
    }

    public void setFirstPriority(boolean z) {
        this.firstTimePriority = z;
    }

    public boolean getFirstPriority() {
        return this.firstTimePriority;
    }

    public String getParamValue(String str) {
        return this.paramsMap.get(str);
    }

    public void parseUrl(String str) {
        int indexOf = str.indexOf(63);
        if (indexOf > 0) {
            parseUrlQuery(str.substring(indexOf + 1));
        } else {
            parseUrlQuery("");
        }
    }

    public void parseUrlQuery(String str) {
        clearCurrentUrl();
        String[] split = str.split(HttpConstant.URL_PARAM_DELIMITER);
        for (String str2 : split) {
            if (str2.length() > 0) {
                int indexOf = str2.indexOf(61);
                if (indexOf < 0) {
                    parseParamPair(str2, "");
                } else {
                    parseParamPair(str2.substring(0, indexOf), str2.substring(indexOf + 1));
                }
            }
        }
    }

    public void registerParameterFilter(String str, ValueFilter valueFilter) {
        if (valueFilter != null) {
            this.paramFiltersMap.put(str, valueFilter);
        } else {
            this.paramFiltersMap.remove(str);
        }
    }

    public ValueFilter getParameterFilter(String str) {
        return this.paramFiltersMap.get(str);
    }

    public void setUnregisteredParamFilter(ValueFilter valueFilter) {
        this.unregisteredParamFilter = valueFilter;
    }

    public ValueFilter getUnregisteredParamFilter() {
        return this.unregisteredParamFilter;
    }

    public void allowUnregisteredParameters(boolean z) {
        this.allowUnregisteredParams = z;
    }

    public boolean isAllowUnregisteredParameters() {
        return this.allowUnregisteredParams;
    }

    private void parseParamPair(String str, String str2) {
        String unescapeString = unescapeString(str);
        ValueFilter parameterFilter = getParameterFilter(str);
        if (parameterFilter == null && this.allowUnregisteredParams) {
            HiLog.error(LOG_LABEL, "use unregistered filter!", new Object[0]);
            parameterFilter = this.unregisteredParamFilter;
        }
        if (parameterFilter != null) {
            addParams(new Pair<>(unescapeString, parameterFilter.filterUrl(unescapeString(str2))));
        }
    }

    private String unescapeString(String str) {
        int i;
        Matcher matcher = Pattern.compile("[+%]").matcher(str);
        if (!matcher.find()) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        sb.append(str.substring(0, matcher.start()));
        int start = matcher.start();
        while (start < str.length()) {
            if (str.charAt(start) == '+') {
                sb.append(' ');
            } else if (str.charAt(start) != '%' || (i = start + 2) >= str.length()) {
                sb.append(str.charAt(start));
            } else {
                int hexCharToInt = hexCharToInt(str.charAt(start + 1));
                int hexCharToInt2 = hexCharToInt(str.charAt(i));
                if (hexCharToInt >= 0 && hexCharToInt2 >= 0) {
                    sb.append((char) ((hexCharToInt * 16) + hexCharToInt2));
                    start = i;
                }
            }
            start++;
        }
        return sb.toString();
    }

    private void addParams(Pair<String, String> pair) {
        this.paramsList.add(pair);
        if (!this.firstTimePriority || !this.paramsMap.containsKey(pair.f)) {
            this.paramsMap.put(pair.f, pair.s);
        }
    }

    private void clearCurrentUrl() {
        this.paramsMap.clear();
        this.paramsList.clear();
    }
}
