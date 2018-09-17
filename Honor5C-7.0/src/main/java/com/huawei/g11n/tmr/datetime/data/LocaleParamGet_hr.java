package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_hr {
    public HashMap<String, String> date;

    public LocaleParamGet_hr() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "ujutro|prijepodne");
                put("param_pm", "podne|popodne|no\u0107u|n");
                put("param_MMM", "sij|velj|o\u017eu|tra|svi|lip|srp|kol|ruj|lis|stu|pro");
                put("param_MMMM", "sije\u010dnja|velja\u010de|o\u017eujka|travnja|svibnja|lipnja|srpnja|kolovoza|rujna|listopada|studenoga|prosinca");
                put("param_E", "ned|pon|uto|sri|\u010det|pet|sub");
                put("param_E2", "ned|pon|uto|sri|\u010det|pet|sub");
                put("param_EEEE", "nedjelja|ponedjeljak|utorak|srijeda|\u010detvrtak|petak|subota");
                put("param_days", "danas|sutra|prekosutra");
                put("param_thisweek", "ova\\s+nedjelja|ovaj\\s+ponedjeljak|ovaj\\s+utorak|ova\\s+srijeda|ovaj\\s+\u010detvrtak|ovaj\\s+petak|ova\\s+subota");
                put("param_nextweek", "sljede\u0107a\\s+nedjelja|sljede\u0107i\\s+ponedjeljak|sljede\u0107i\\s+utorak|sljede\u0107a\\s+srijeda|sljede\u0107i\\s+\u010detvrtak|sljede\u0107i\\s+petak|sljede\u0107a\\s+subota");
            }
        };
    }
}
