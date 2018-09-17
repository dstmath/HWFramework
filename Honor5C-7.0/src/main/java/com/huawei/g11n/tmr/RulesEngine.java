package com.huawei.g11n.tmr;

import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RulesEngine {
    private String locale;
    private HashMap<Integer, Pattern> patterns;
    private HashMap<Integer, String> regexs;

    public RulesEngine(String str, HashMap<Integer, String> hashMap, HashMap<String, String> hashMap2, LocaleParam localeParam, LocaleParam localeParam2, boolean z) {
        this.locale = str;
        init(str, hashMap, hashMap2, localeParam, localeParam2, z);
    }

    public RulesEngine(String str, HashMap<Integer, String> hashMap, HashMap<String, String> hashMap2, LocaleParam localeParam, LocaleParam localeParam2) {
        this.locale = str;
        init(str, hashMap, hashMap2, localeParam, localeParam2, true);
    }

    public Pattern getPatterns(Integer num) {
        if (this.patterns == null || !this.patterns.containsKey(num)) {
            return null;
        }
        return (Pattern) this.patterns.get(num);
    }

    private void init(String str, HashMap<Integer, String> hashMap, HashMap<String, String> hashMap2, LocaleParam localeParam, LocaleParam localeParam2, boolean z) {
        this.patterns = new HashMap();
        this.regexs = new HashMap();
        Pattern compile = Pattern.compile("\\[(param_\\w+)\\]");
        Pattern compile2 = Pattern.compile("\\[regex_(\\w+)\\]");
        for (Entry entry : hashMap.entrySet()) {
            String str2;
            String str3;
            Integer num = (Integer) entry.getKey();
            CharSequence charSequence = (String) entry.getValue();
            if (!(hashMap2 == null || hashMap2.isEmpty())) {
                Matcher matcher = compile2.matcher(charSequence);
                str2 = charSequence;
                while (matcher.find()) {
                    str2 = str2.replace(matcher.group(), (CharSequence) hashMap2.get(matcher.group(1)));
                }
                Object obj = str2;
            }
            if (localeParam == null && localeParam2 == null) {
                str3 = charSequence;
                obj = 1;
            } else {
                Matcher matcher2 = compile.matcher(charSequence);
                str3 = charSequence;
                while (matcher2.find()) {
                    String group = matcher2.group(1);
                    charSequence = (localeParam == null || localeParam.get(group) == null) ? "" : localeParam.get(group);
                    str2 = (localeParam2 == null || localeParam2.get(group) == null) ? "" : localeParam2.get(group);
                    if (charSequence.isEmpty() && !str2.isEmpty()) {
                        obj = str2;
                    } else if (!(charSequence.trim().isEmpty() || str2.trim().isEmpty() || charSequence.endsWith("]") || charSequence.endsWith("]\\b") || !isConactBkParam(num))) {
                        charSequence = charSequence.concat("|").concat(str2);
                    }
                    if (charSequence.trim().isEmpty()) {
                        obj = null;
                        break;
                    }
                    str3 = str3.replace("[".concat(group).concat("]"), charSequence);
                }
                int i = 1;
            }
            if (!(str3 == null || str3.trim().equals("") || r1 == null)) {
                if (z) {
                    this.patterns.put(num, Pattern.compile(str3, 2));
                } else {
                    this.regexs.put(num, str3);
                }
            }
        }
    }

    private boolean isConactBkParam(Integer num) {
        if (num.intValue() != 20009 && num.intValue() != 20010 && num.intValue() != 20011) {
            return true;
        }
        if (this.locale.equals("zh_hans") || this.locale.equals("en")) {
            return true;
        }
        return false;
    }

    public HashMap<Integer, String> getRegexs() {
        return this.regexs;
    }

    public List<Match> match(String str) {
        List<Match> arrayList = new ArrayList();
        List<Integer> arrayList2 = new ArrayList();
        arrayList2.addAll(this.patterns.keySet());
        Collections.sort(arrayList2);
        for (Integer num : arrayList2) {
            Matcher matcher = ((Pattern) this.patterns.get(num)).matcher(str);
            while (matcher.find()) {
                arrayList.add(new Match(matcher.start(), matcher.end(), String.valueOf(num)));
            }
        }
        return arrayList;
    }

    public Pattern getPattenById(Integer num) {
        if (this.patterns != null && this.patterns.containsKey(num)) {
            return (Pattern) this.patterns.get(num);
        }
        return null;
    }

    public String getLocale() {
        return this.locale;
    }
}
