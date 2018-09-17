package com.huawei.g11n.tmr.datetime.parse;

import java.util.HashMap;

public class ParseRules {
    private HashMap<Integer, String> rules = new HashMap<Integer, String>() {
        {
            put(Integer.valueOf(901), "([regex_y].*?)?([param_MMMM]|[param_MMM]).*?[regex_d]");
            put(Integer.valueOf(902), "([param_MMMM]|[param_MMM]).*?[regex_d](.+?[regex_y])?");
            put(Integer.valueOf(903), "[regex_d].*?([param_MMMM]|[param_MMM])(.*?[regex_y])?");
            put(Integer.valueOf(904), "(\\d+).+?(\\d+).+?(\\d+)");
            put(Integer.valueOf(905), "(\\d+).+?(\\d+)");
            put(Integer.valueOf(906), ".*?(\\d{1,2}).*?");
            put(Integer.valueOf(907), "(\\bam|\\bpm|\\bmm)?\\s*(\\d{1,2})[\\s.:]+(\\d{2})([\\s.:]+(\\d{2}))?\\s*(\\bam|\\bpm|\\bmm)?[\\s(]*([regex_zzzz])?[\\s)]*");
            put(Integer.valueOf(908), "(\\bam|\\bpm|\\bmm)?\\s*(\\d{1,2}).\\s*(\\d{2})?(.\\s*(\\d{2}))?.?\\s*\\(?([regex_zzzz])?");
            put(Integer.valueOf(909), "(\\bam|\\bpm|\\bmm)?\\s*(\\d{1,2})\\s*[^0-9+-]+\\s*(\\d{2})(\\s*[^0-9+-]+\\s*(\\d{2}))?[^0-9+-]+([regex_zzzz])?");
            put(Integer.valueOf(910), "(([param_EEEE]|[param_E])\\s*)?(([regex_zzzz])\\s*)?(\\bam|\\bpm|\\bmm)?\\s*(\\d{1,2}[param_digit])(時|时|点|點|гадзін|\\s)+(\\d{1,2}[param_timesuf][param_digit])?[\\s分]*((\\d{1,2}[param_digit])[\\s秒]+)?");
            put(Integer.valueOf(911), "(2[0-4]|[0-1]?[0-9])[:.]([0-5][0-9]|60)([:.]([0-5][0-9]|60))?");
            put(Integer.valueOf(912), "(\\b(am|pm|mm)\\b)?\\s*(\\d{1,2})");
            put(Integer.valueOf(913), "(([regex_zzzz])\\s*)?(\\bam|\\bpm|\\bmm)\\s*(\\d{1,2})[\\s.:]+(\\d{2})([\\s.:]+(\\d{2}))?");
            put(Integer.valueOf(915), "(\\d{1,2}).+?(\\d{1,2}).*?\\b([param_MMMM]|[param_MMM])(.*?((20)?[0-9]{2}))?");
            put(Integer.valueOf(916), "\\b([param_MMMM]|[param_MMM]).*?(\\d{1,2}).+?(\\d{1,2}).+?((20)?[0-9]{2})");
            put(Integer.valueOf(917), "(\\d{1,2}).+?\\b([param_MMMM]|[param_MMM]).+?(\\d{1,2}).+?\\b([param_MMMM]|[param_MMM])(.+?((20)?[0-9]{2}))?");
            put(Integer.valueOf(920), "((?!<\\d)(\\d{2,4})\\s*[年|년]\\s*)?(\\d{1,2})[^0-9]+(\\d{1,2})([^0-9]+(\\d{1,2}))?[^0-9]+(\\d{1,2})");
            put(Integer.valueOf(921), "(((20)?[0-9]{2}).*?)?([param_MMMM]|[param_MMM])[^0-9]*?(\\d{1,2}).+?(\\d{1,2})");
            put(Integer.valueOf(924), "(([regex_y])(/|-|(\\.[ ]{0,3})))?([regex_m])(\\5|(/|-|(\\.[ ]{0,3})))([regex_d])\\.?\\s*[-–~]\\s*([regex_d])\\.?(?!\\.?\\d)");
            put(Integer.valueOf(925), "(([regex_y])[^0-9]*\\s+)?([param_MMMM]|[param_MMM])(ren)?\\s+[regex_d]a?");
            put(Integer.valueOf(926), "(([regex_y])[^0-9]+)([regex_d])[^0-9]+([param_MMMM]|[param_MMM])");
        }
    };

    public HashMap<Integer, String> getRules() {
        return this.rules;
    }
}
