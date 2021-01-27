package ohos.global.icu.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.global.icu.text.StringTransform;
import ohos.global.icu.text.SymbolTable;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.Freezable;

public class UnicodeRegex implements Cloneable, Freezable<UnicodeRegex>, StringTransform {
    private static final UnicodeRegex STANDARD = new UnicodeRegex();
    private Comparator<Object> LongestFirst = new Comparator<Object>() {
        /* class ohos.global.icu.impl.UnicodeRegex.AnonymousClass1 */

        @Override // java.util.Comparator
        public int compare(Object obj, Object obj2) {
            String obj3 = obj.toString();
            String obj4 = obj2.toString();
            int length = obj3.length();
            int length2 = obj4.length();
            if (length != length2) {
                return length2 - length;
            }
            return obj3.compareTo(obj4);
        }
    };
    private String bnfCommentString = DMSDPConfig.SPLIT;
    private String bnfLineSeparator = "\n";
    private String bnfVariableInfix = "=";
    private SymbolTable symbolTable;

    public UnicodeRegex freeze() {
        return this;
    }

    public boolean isFrozen() {
        return true;
    }

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    public UnicodeRegex setSymbolTable(SymbolTable symbolTable2) {
        this.symbolTable = symbolTable2;
        return this;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002f, code lost:
        if (r0 != '\\') goto L_0x0066;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0038, code lost:
        if (r0 == 'Q') goto L_0x0066;
     */
    public String transform(String str) {
        StringBuilder sb = new StringBuilder();
        UnicodeSet unicodeSet = new UnicodeSet();
        ParsePosition parsePosition = new ParsePosition(0);
        int i = 0;
        char c = 0;
        while (i < str.length()) {
            char charAt = str.charAt(i);
            char c2 = 2;
            if (c != 0) {
                if (c != 1) {
                    if (c != 2) {
                        if (c == 3) {
                            if (charAt != 'E') {
                            }
                        }
                    } else if (charAt == '\\') {
                        c2 = 3;
                        sb.append(charAt);
                        c = c2;
                        i++;
                    }
                }
                c2 = 0;
                sb.append(charAt);
                c = c2;
                i++;
            } else {
                if (charAt == '\\') {
                    if (UnicodeSet.resemblesPattern(str, i)) {
                        i = processSet(str, i, sb, unicodeSet, parsePosition);
                    } else {
                        c2 = 1;
                        sb.append(charAt);
                        c = c2;
                    }
                } else if (charAt == '[' && UnicodeSet.resemblesPattern(str, i)) {
                    i = processSet(str, i, sb, unicodeSet, parsePosition);
                }
                i++;
            }
            c2 = c;
            sb.append(charAt);
            c = c2;
            i++;
        }
        return sb.toString();
    }

    public static String fix(String str) {
        return STANDARD.transform(str);
    }

    public static Pattern compile(String str) {
        return Pattern.compile(STANDARD.transform(str));
    }

    public static Pattern compile(String str, int i) {
        return Pattern.compile(STANDARD.transform(str), i);
    }

    public String compileBnf(String str) {
        return compileBnf(Arrays.asList(str.split("\\r\\n?|\\n")));
    }

    public String compileBnf(List<String> list) {
        Map<String, String> variables = getVariables(list);
        LinkedHashSet linkedHashSet = new LinkedHashSet(variables.keySet());
        for (int i = 0; i < 2; i++) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                for (Map.Entry<String, String> entry2 : variables.entrySet()) {
                    String key2 = entry2.getKey();
                    String value2 = entry2.getValue();
                    if (!key.equals(key2)) {
                        String replace = value2.replace(key, value);
                        if (!replace.equals(value2)) {
                            linkedHashSet.remove(key);
                            variables.put(key2, replace);
                        }
                    }
                }
            }
        }
        if (linkedHashSet.size() == 1) {
            return variables.get(linkedHashSet.iterator().next());
        }
        throw new IllegalArgumentException("Not a single root: " + linkedHashSet);
    }

    public String getBnfCommentString() {
        return this.bnfCommentString;
    }

    public void setBnfCommentString(String str) {
        this.bnfCommentString = str;
    }

    public String getBnfVariableInfix() {
        return this.bnfVariableInfix;
    }

    public void setBnfVariableInfix(String str) {
        this.bnfVariableInfix = str;
    }

    public String getBnfLineSeparator() {
        return this.bnfLineSeparator;
    }

    public void setBnfLineSeparator(String str) {
        this.bnfLineSeparator = str;
    }

    public static List<String> appendLines(List<String> list, String str, String str2) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(str);
        try {
            return appendLines(list, fileInputStream, str2);
        } finally {
            fileInputStream.close();
        }
    }

    public static List<String> appendLines(List<String> list, InputStream inputStream, String str) throws UnsupportedEncodingException, IOException {
        if (str == null) {
            str = "UTF-8";
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, str));
        while (true) {
            String readLine = bufferedReader.readLine();
            if (readLine == null) {
                return list;
            }
            list.add(readLine);
        }
    }

    public UnicodeRegex cloneAsThawed() {
        try {
            return (UnicodeRegex) clone();
        } catch (CloneNotSupportedException unused) {
            throw new IllegalArgumentException();
        }
    }

    private int processSet(String str, int i, StringBuilder sb, UnicodeSet unicodeSet, ParsePosition parsePosition) {
        try {
            parsePosition.setIndex(i);
            UnicodeSet applyPattern = unicodeSet.clear().applyPattern(str, parsePosition, this.symbolTable, 0);
            applyPattern.complement().complement();
            sb.append(applyPattern.toPattern(false));
            return parsePosition.getIndex() - 1;
        } catch (Exception e) {
            throw ((IllegalArgumentException) new IllegalArgumentException("Error in " + str).initCause(e));
        }
    }

    private Map<String, String> getVariables(List<String> list) {
        int indexOf;
        TreeMap treeMap = new TreeMap(this.LongestFirst);
        StringBuffer stringBuffer = new StringBuffer();
        String str = null;
        int i = 0;
        for (String str2 : list) {
            i++;
            if (str2.length() != 0) {
                if (str2.charAt(0) == 65279) {
                    str2 = str2.substring(1);
                }
                String str3 = this.bnfCommentString;
                if (str3 != null && (indexOf = str2.indexOf(str3)) >= 0) {
                    str2 = str2.substring(0, indexOf);
                }
                String trim = str2.trim();
                if (!(trim.length() == 0 || str2.trim().length() == 0)) {
                    boolean endsWith = trim.endsWith(DMSDPConfig.LIST_TO_STRING_SPLIT);
                    String substring = endsWith ? str2.substring(0, str2.lastIndexOf(59)) : str2;
                    int indexOf2 = substring.indexOf(this.bnfVariableInfix);
                    if (indexOf2 >= 0) {
                        if (str == null) {
                            str = substring.substring(0, indexOf2).trim();
                            if (!treeMap.containsKey(str)) {
                                stringBuffer.append(substring.substring(indexOf2 + 1).trim());
                            } else {
                                throw new IllegalArgumentException("Duplicate variable definition in " + str2);
                            }
                        } else {
                            throw new IllegalArgumentException("Missing ';' before " + i + ") " + str2);
                        }
                    } else if (str != null) {
                        stringBuffer.append(this.bnfLineSeparator);
                        stringBuffer.append(substring);
                    } else {
                        throw new IllegalArgumentException("Missing '=' at " + i + ") " + str2);
                    }
                    if (endsWith) {
                        treeMap.put(str, stringBuffer.toString());
                        stringBuffer.setLength(0);
                        str = null;
                    }
                }
            }
        }
        if (str == null) {
            return treeMap;
        }
        throw new IllegalArgumentException("Missing ';' at end");
    }
}
