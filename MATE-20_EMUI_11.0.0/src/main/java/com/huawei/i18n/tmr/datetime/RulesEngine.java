package com.huawei.i18n.tmr.datetime;

import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.datetime.data.LocaleParam;
import com.huawei.i18n.tmr.datetime.utils.RulesSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RulesEngine {
    private String locale;
    private HashMap<Integer, Pattern> patterns;
    private HashMap<Integer, String> regexps;

    RulesEngine(String locale2, RulesSet rulesSet, boolean isPat) {
        this.locale = locale2;
        init(rulesSet, isPat);
    }

    RulesEngine(String locale2, RulesSet rulesSet) {
        this(locale2, rulesSet, true);
    }

    /* access modifiers changed from: package-private */
    public Optional<Pattern> getPatterns(Integer key) {
        HashMap<Integer, Pattern> hashMap = this.patterns;
        if (hashMap == null) {
            return Optional.empty();
        }
        if (hashMap.containsKey(key)) {
            return Optional.ofNullable(this.patterns.get(key));
        }
        return Optional.empty();
    }

    private void init(RulesSet rulesSet, boolean isPattern) {
        this.patterns = new HashMap<>();
        this.regexps = new HashMap<>();
        Pattern patternParam = Pattern.compile("\\[(param_\\w+)\\]");
        for (Map.Entry<Integer, String> entry : rulesSet.getRulesMap().entrySet()) {
            Integer rulesKey = entry.getKey();
            String rulesValue = initOptRules(rulesSet.getParam(), rulesSet.getParamBackup(), initSubRules(rulesSet.getSubRules(), entry.getValue()));
            boolean isValid = true;
            if (rulesSet.getParam() != null || rulesSet.getParamBackup() != null) {
                Matcher match = patternParam.matcher(rulesValue);
                while (true) {
                    if (!match.find()) {
                        break;
                    }
                    String value = match.group(1);
                    String paramValue = (rulesSet.getParam() == null || rulesSet.getParam().get(value) == null) ? StorageManagerExt.INVALID_KEY_DESC : rulesSet.getParam().get(value);
                    String backupValue = (rulesSet.getParamBackup() == null || rulesSet.getParamBackup().get(value) == null) ? StorageManagerExt.INVALID_KEY_DESC : rulesSet.getParamBackup().get(value);
                    if (paramValue.isEmpty() && !backupValue.isEmpty()) {
                        paramValue = backupValue;
                    } else if (!paramValue.trim().isEmpty() && !backupValue.trim().isEmpty() && !paramValue.endsWith("]") && !paramValue.endsWith("]\\b") && isAddBackupParam(rulesKey)) {
                        paramValue = paramValue.concat("|").concat(backupValue);
                    }
                    if (paramValue.trim().isEmpty()) {
                        isValid = false;
                        break;
                    }
                    rulesValue = rulesValue.replace("[".concat(value).concat("]"), paramValue);
                }
            }
            if (rulesValue != null && !StorageManagerExt.INVALID_KEY_DESC.equals(rulesValue.trim()) && isValid) {
                if (isPattern) {
                    this.patterns.put(rulesKey, Pattern.compile(rulesValue, 2));
                } else {
                    this.regexps.put(rulesKey, rulesValue);
                }
            }
        }
    }

    private String initOptRules(LocaleParam param, LocaleParam paramBackup, String rule) {
        int rindex;
        String rulesValue = rule;
        Pattern optPattern = Pattern.compile("\\[paramopt_(\\w+)\\]");
        if (!(param == null && paramBackup == null)) {
            Matcher optMatcher = optPattern.matcher(rulesValue);
            while (optMatcher.find()) {
                String value = "param_" + optMatcher.group(1);
                int findex = optMatcher.start();
                int fend = optMatcher.end();
                String paramValue = (param == null || param.get(value) == null) ? StorageManagerExt.INVALID_KEY_DESC : param.get(value);
                String backupValue = (paramBackup == null || paramBackup.get(value) == null) ? StorageManagerExt.INVALID_KEY_DESC : paramBackup.get(value);
                if (paramValue.isEmpty() && !backupValue.isEmpty()) {
                    paramValue = backupValue;
                } else if (!paramValue.trim().isEmpty() && !backupValue.trim().isEmpty()) {
                    paramValue = paramValue.concat("|").concat(backupValue);
                }
                if (paramValue.isEmpty() && backupValue.isEmpty()) {
                    if ("|".equals(rulesValue.substring(findex - 1, findex))) {
                        rindex = findex - 1;
                    } else {
                        rindex = findex;
                    }
                    rulesValue = rulesValue.replace(rulesValue.substring(rindex, fend), StorageManagerExt.INVALID_KEY_DESC);
                }
                rulesValue = rulesValue.replace("[".concat("paramopt_" + optMatcher.group(1)).concat("]"), paramValue);
            }
        }
        return rulesValue;
    }

    private String initSubRules(HashMap<String, String> subRules, String rule) {
        String rulesValue = rule;
        Pattern patternRegex = Pattern.compile("\\[regex_(\\w+)\\]");
        if (subRules != null && !subRules.isEmpty()) {
            Matcher subMatcher = patternRegex.matcher(rulesValue);
            while (subMatcher.find()) {
                rulesValue = rulesValue.replace(subMatcher.group(), subRules.get(subMatcher.group(1)));
            }
        }
        return rulesValue;
    }

    private boolean isAddBackupParam(Integer integer) {
        if ((integer.intValue() == 20009 || integer.intValue() == 20010 || integer.intValue() == 20011) && !"zh_hans".equals(this.locale) && !"en".equals(this.locale)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public HashMap<Integer, String> getRegexps() {
        return this.regexps;
    }

    /* access modifiers changed from: package-private */
    public List<Match> match(String msg) {
        List<Match> matches = new ArrayList<>();
        List<Integer> keys = new ArrayList<>(this.patterns.keySet());
        Collections.sort(keys);
        for (Integer name : keys) {
            Matcher match = this.patterns.get(name).matcher(msg);
            while (match.find()) {
                matches.add(new Match(match.start(), match.end(), String.valueOf(name)));
            }
        }
        return matches;
    }

    public Optional<Pattern> getPattenById(Integer id) {
        HashMap<Integer, Pattern> hashMap = this.patterns;
        if (hashMap == null || !hashMap.containsKey(id)) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.patterns.get(id));
    }

    public String getLocale() {
        return this.locale;
    }
}
