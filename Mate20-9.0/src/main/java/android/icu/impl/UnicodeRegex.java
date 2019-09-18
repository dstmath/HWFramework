package android.icu.impl;

import android.icu.text.StringTransform;
import android.icu.text.SymbolTable;
import android.icu.text.UnicodeSet;
import android.icu.util.Freezable;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class UnicodeRegex implements Cloneable, Freezable<UnicodeRegex>, StringTransform {
    private static final UnicodeRegex STANDARD = new UnicodeRegex();
    private Comparator<Object> LongestFirst = new Comparator<Object>() {
        public int compare(Object obj0, Object obj1) {
            String arg0 = obj0.toString();
            String arg1 = obj1.toString();
            int len0 = arg0.length();
            int len1 = arg1.length();
            if (len0 != len1) {
                return len1 - len0;
            }
            return arg0.compareTo(arg1);
        }
    };
    private String bnfCommentString = "#";
    private String bnfLineSeparator = "\n";
    private String bnfVariableInfix = "=";
    private SymbolTable symbolTable;

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    public UnicodeRegex setSymbolTable(SymbolTable symbolTable2) {
        this.symbolTable = symbolTable2;
        return this;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0037, code lost:
        r0 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0066, code lost:
        r13.append(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0025, code lost:
        r0 = r11;
     */
    public String transform(String regex) {
        String str = regex;
        StringBuilder result = new StringBuilder();
        UnicodeSet temp = new UnicodeSet();
        int i = 0;
        ParsePosition pos = new ParsePosition(0);
        int state = 0;
        while (true) {
            int i2 = i;
            if (i2 < regex.length()) {
                char ch = str.charAt(i2);
                switch (state) {
                    case 0:
                        if (ch != '\\') {
                            if (ch == '[' && UnicodeSet.resemblesPattern(str, i2)) {
                                char c = ch;
                                i2 = processSet(str, i2, result, temp, pos);
                                break;
                            }
                        } else if (!UnicodeSet.resemblesPattern(str, i2)) {
                            state = 1;
                            break;
                        } else {
                            i2 = processSet(str, i2, result, temp, pos);
                            break;
                        }
                    case 1:
                        if (ch != 'Q') {
                            state = 0;
                            break;
                        } else {
                            state = 1;
                            break;
                        }
                    case 2:
                        if (ch == '\\') {
                            state = 3;
                            break;
                        }
                        break;
                    case 3:
                        if (ch == 'E') {
                        }
                        state = 2;
                        break;
                }
            } else {
                return result.toString();
            }
            i = i2 + 1;
        }
    }

    public static String fix(String regex) {
        return STANDARD.transform(regex);
    }

    public static Pattern compile(String regex) {
        return Pattern.compile(STANDARD.transform(regex));
    }

    public static Pattern compile(String regex, int options) {
        return Pattern.compile(STANDARD.transform(regex), options);
    }

    public String compileBnf(String bnfLines) {
        return compileBnf((List<String>) Arrays.asList(bnfLines.split("\\r\\n?|\\n")));
    }

    public String compileBnf(List<String> lines) {
        Map<String, String> variables = getVariables(lines);
        Set<String> unused = new LinkedHashSet<>(variables.keySet());
        for (int i = 0; i < 2; i++) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String variable = entry.getKey();
                String definition = entry.getValue();
                for (Map.Entry<String, String> entry2 : variables.entrySet()) {
                    String variable2 = entry2.getKey();
                    String definition2 = entry2.getValue();
                    if (!variable.equals(variable2)) {
                        String altered2 = definition2.replace(variable, definition);
                        if (!altered2.equals(definition2)) {
                            unused.remove(variable);
                            variables.put(variable2, altered2);
                        }
                    }
                }
            }
        }
        if (unused.size() == 1) {
            return variables.get(unused.iterator().next());
        }
        throw new IllegalArgumentException("Not a single root: " + unused);
    }

    public String getBnfCommentString() {
        return this.bnfCommentString;
    }

    public void setBnfCommentString(String bnfCommentString2) {
        this.bnfCommentString = bnfCommentString2;
    }

    public String getBnfVariableInfix() {
        return this.bnfVariableInfix;
    }

    public void setBnfVariableInfix(String bnfVariableInfix2) {
        this.bnfVariableInfix = bnfVariableInfix2;
    }

    public String getBnfLineSeparator() {
        return this.bnfLineSeparator;
    }

    public void setBnfLineSeparator(String bnfLineSeparator2) {
        this.bnfLineSeparator = bnfLineSeparator2;
    }

    public static List<String> appendLines(List<String> result, String file, String encoding) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            return appendLines(result, is, encoding);
        } finally {
            is.close();
        }
    }

    public static List<String> appendLines(List<String> result, InputStream inputStream, String encoding) throws UnsupportedEncodingException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, encoding == null ? "UTF-8" : encoding));
        while (true) {
            String line = in.readLine();
            if (line == null) {
                return result;
            }
            result.add(line);
        }
    }

    public UnicodeRegex cloneAsThawed() {
        try {
            return (UnicodeRegex) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException();
        }
    }

    public UnicodeRegex freeze() {
        return this;
    }

    public boolean isFrozen() {
        return true;
    }

    private int processSet(String regex, int i, StringBuilder result, UnicodeSet temp, ParsePosition pos) {
        try {
            pos.setIndex(i);
            UnicodeSet x = temp.clear().applyPattern(regex, pos, this.symbolTable, 0);
            x.complement().complement();
            result.append(x.toPattern(false));
            return pos.getIndex() - 1;
        } catch (Exception e) {
            throw ((IllegalArgumentException) new IllegalArgumentException("Error in " + regex).initCause(e));
        }
    }

    private Map<String, String> getVariables(List<String> lines) {
        Map<String, String> variables = new TreeMap<>(this.LongestFirst);
        String variable = null;
        StringBuffer definition = new StringBuffer();
        int count = 0;
        Iterator<String> it = lines.iterator();
        while (it.hasNext()) {
            String line = it.next();
            count++;
            if (line.length() != 0) {
                if (line.charAt(0) == 65279) {
                    line = line.substring(1);
                }
                if (this.bnfCommentString != null) {
                    int hashPos = line.indexOf(this.bnfCommentString);
                    if (hashPos >= 0) {
                        line = line.substring(0, hashPos);
                    }
                }
                String trimline = line.trim();
                if (trimline.length() == 0) {
                    continue;
                } else {
                    String linePart = line;
                    if (linePart.trim().length() == 0) {
                        continue;
                    } else {
                        boolean terminated = trimline.endsWith(";");
                        if (terminated) {
                            linePart = linePart.substring(0, linePart.lastIndexOf(59));
                        }
                        int equalsPos = linePart.indexOf(this.bnfVariableInfix);
                        if (equalsPos >= 0) {
                            if (variable == null) {
                                variable = linePart.substring(0, equalsPos).trim();
                                if (!variables.containsKey(variable)) {
                                    definition.append(linePart.substring(equalsPos + 1).trim());
                                } else {
                                    throw new IllegalArgumentException("Duplicate variable definition in " + line);
                                }
                            } else {
                                throw new IllegalArgumentException("Missing ';' before " + count + ") " + line);
                            }
                        } else if (variable != null) {
                            definition.append(this.bnfLineSeparator);
                            definition.append(linePart);
                        } else {
                            throw new IllegalArgumentException("Missing '=' at " + count + ") " + line);
                        }
                        if (terminated) {
                            variables.put(variable, definition.toString());
                            variable = null;
                            definition.setLength(0);
                        }
                    }
                }
            }
        }
        if (variable == null) {
            return variables;
        }
        throw new IllegalArgumentException("Missing ';' at end");
    }
}
