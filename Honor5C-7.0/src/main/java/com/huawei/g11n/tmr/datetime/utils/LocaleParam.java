package com.huawei.g11n.tmr.datetime.utils;

import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_ar;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_az;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_be;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_bg;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_bn;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_bo;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_bs;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_ca;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_cs;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_da;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_de;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_el;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_en;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_es;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_es_MX;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_et;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_eu;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_fa;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_fi;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_fil;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_fr;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_gl;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_he;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_hi;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_hr;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_hu;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_id;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_it;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_ja;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_jv;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_ka;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_kk;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_km;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_ko;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_lo;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_lt;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_lv;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_mk;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_ms;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_my;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_nb;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_ne;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_nl;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_pl;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_pt;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_ro;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_ru;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_si;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_sk;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_sl;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_sr;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_sv;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_th;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_tr;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_uk;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_ur;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_uz;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_vi;
import com.huawei.g11n.tmr.datetime.data.LocaleParamGet_zh_hans;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocaleParam {
    public static final String SAVE_OR = "\\u0604";
    public static final String WITHNOT_B = "\\u0605";
    private static List<String> support;
    private HashMap<String, String> defaultParam;
    private String locale;
    private HashMap<String, String> param;
    private HashMap<String, String> param_b;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.g11n.tmr.datetime.utils.LocaleParam.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.g11n.tmr.datetime.utils.LocaleParam.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.g11n.tmr.datetime.utils.LocaleParam.<clinit>():void");
    }

    public LocaleParam(String str) {
        this.param = null;
        this.locale = null;
        this.param_b = new HashMap<String, String>() {
            {
                put("en", "\\b");
                put("zh_hans", "");
                put("ko", "");
                put("ja", "");
                put("hi", "");
                put("he", "");
                put("km", "");
                put("bo", "");
                put("si", "");
                put("my", "");
                put("ne", "");
                put("bn", "");
                put("be", "");
                put("lo", "");
                put("kk", "");
            }
        };
        this.defaultParam = new HashMap<String, String>() {
            {
                put("param_tmark", ":");
            }
        };
        if (isSupport(str)) {
            this.locale = str;
            if (this.locale.equals("en")) {
                this.param = new LocaleParamGet_en().date;
            } else if (this.locale.equals("de")) {
                this.param = new LocaleParamGet_de().date;
            } else if (this.locale.equals("fr")) {
                this.param = new LocaleParamGet_fr().date;
            } else if (this.locale.equals("it")) {
                this.param = new LocaleParamGet_it().date;
            } else if (this.locale.equals("pt")) {
                this.param = new LocaleParamGet_pt().date;
            } else if (this.locale.equals("es")) {
                this.param = new LocaleParamGet_es().date;
            } else if (this.locale.equals("es_MX")) {
                this.param = new LocaleParamGet_es_MX().date;
            } else if (this.locale.equals("ca")) {
                this.param = new LocaleParamGet_ca().date;
            } else if (this.locale.equals("ru")) {
                this.param = new LocaleParamGet_ru().date;
            } else if (this.locale.equals("uk")) {
                this.param = new LocaleParamGet_uk().date;
            } else if (this.locale.equals("pl")) {
                this.param = new LocaleParamGet_pl().date;
            } else if (this.locale.equals("cs")) {
                this.param = new LocaleParamGet_cs().date;
            } else if (this.locale.equals("el")) {
                this.param = new LocaleParamGet_el().date;
            } else if (this.locale.equals("hu")) {
                this.param = new LocaleParamGet_hu().date;
            } else if (this.locale.equals("hr")) {
                this.param = new LocaleParamGet_hr().date;
            } else if (this.locale.equals("ro")) {
                this.param = new LocaleParamGet_ro().date;
            } else if (this.locale.equals("da")) {
                this.param = new LocaleParamGet_da().date;
            } else if (this.locale.equals("fi")) {
                this.param = new LocaleParamGet_fi().date;
            } else if (this.locale.equals("nl")) {
                this.param = new LocaleParamGet_nl().date;
            } else if (this.locale.equals("nb")) {
                this.param = new LocaleParamGet_nb().date;
            } else if (this.locale.equals("sv")) {
                this.param = new LocaleParamGet_sv().date;
            } else if (this.locale.equals("sk")) {
                this.param = new LocaleParamGet_sk().date;
            } else if (this.locale.equals("tr")) {
                this.param = new LocaleParamGet_tr().date;
            } else if (this.locale.equals("ar")) {
                this.param = new LocaleParamGet_ar().date;
            } else if (this.locale.equals("he")) {
                this.param = new LocaleParamGet_he().date;
            } else if (this.locale.equals("th")) {
                this.param = new LocaleParamGet_th().date;
            } else if (this.locale.equals("ja")) {
                this.param = new LocaleParamGet_ja().date;
            } else if (this.locale.equals("ko")) {
                this.param = new LocaleParamGet_ko().date;
            } else if (this.locale.equals("ms")) {
                this.param = new LocaleParamGet_ms().date;
            } else if (this.locale.equals("hi")) {
                this.param = new LocaleParamGet_hi().date;
            } else if (this.locale.equals("id")) {
                this.param = new LocaleParamGet_id().date;
            } else if (this.locale.equals("vi")) {
                this.param = new LocaleParamGet_vi().date;
            } else if (this.locale.equals("zh_hans")) {
                this.param = new LocaleParamGet_zh_hans().date;
            } else if (this.locale.equals("az")) {
                this.param = new LocaleParamGet_az().date;
            } else if (this.locale.equals("et")) {
                this.param = new LocaleParamGet_et().date;
            } else if (this.locale.equals("eu")) {
                this.param = new LocaleParamGet_eu().date;
            } else if (this.locale.equals("bg")) {
                this.param = new LocaleParamGet_bg().date;
            } else if (this.locale.equals("bs")) {
                this.param = new LocaleParamGet_bs().date;
            } else if (this.locale.equals("fa")) {
                this.param = new LocaleParamGet_fa().date;
            } else if (this.locale.equals("bo")) {
                this.param = new LocaleParamGet_bo().date;
            } else if (this.locale.equals("km")) {
                this.param = new LocaleParamGet_km().date;
            } else if (this.locale.equals("ka")) {
                this.param = new LocaleParamGet_ka().date;
            } else if (this.locale.equals("gl")) {
                this.param = new LocaleParamGet_gl().date;
            } else if (this.locale.equals("lv")) {
                this.param = new LocaleParamGet_lv().date;
            } else if (this.locale.equals("lt")) {
                this.param = new LocaleParamGet_lt().date;
            } else if (this.locale.equals("mk")) {
                this.param = new LocaleParamGet_mk().date;
            } else if (this.locale.equals("si")) {
                this.param = new LocaleParamGet_si().date;
            } else if (this.locale.equals("sr")) {
                this.param = new LocaleParamGet_sr().date;
            } else if (this.locale.equals("sl")) {
                this.param = new LocaleParamGet_sl().date;
            } else if (this.locale.equals("ur")) {
                this.param = new LocaleParamGet_ur().date;
            } else if (this.locale.equals("uz")) {
                this.param = new LocaleParamGet_uz().date;
            } else if (this.locale.equals("my")) {
                this.param = new LocaleParamGet_my().date;
            } else if (this.locale.equals("be")) {
                this.param = new LocaleParamGet_be().date;
            } else if (this.locale.equals("kk")) {
                this.param = new LocaleParamGet_kk().date;
            } else if (this.locale.equals("bn")) {
                this.param = new LocaleParamGet_bn().date;
            } else if (this.locale.equals("lo")) {
                this.param = new LocaleParamGet_lo().date;
            } else if (this.locale.equals("fil")) {
                this.param = new LocaleParamGet_fil().date;
            } else if (this.locale.equals("ne")) {
                this.param = new LocaleParamGet_ne().date;
            } else if (this.locale.equals("jv")) {
                this.param = new LocaleParamGet_jv().date;
            }
        }
        if (this.param == null) {
            this.param = new LocaleParamGet_en().date;
        }
    }

    public static boolean isSupport(String str) {
        return support.contains(str);
    }

    public String get(String str) {
        if (this.param == null) {
            return null;
        }
        String str2 = this.param.get(str) == null ? "" : (String) this.param.get(str);
        if (str2.trim().isEmpty() && this.defaultParam.containsKey(str)) {
            str2 = (String) this.defaultParam.get(str);
        }
        if (!(str2 == null || str2.trim().isEmpty())) {
            String[] split = str2.split("\\|");
            StringBuffer stringBuffer = new StringBuffer();
            Map map = this.param_b;
            if (map.containsKey(this.locale)) {
                str2 = (String) map.get(this.locale);
            } else {
                str2 = "\\b";
            }
            for (String str3 : split) {
                if (!str3.trim().isEmpty()) {
                    if (str3.equals(SAVE_OR)) {
                        stringBuffer.append("|");
                    } else if (str3.equals(WITHNOT_B)) {
                        str2 = "";
                    } else {
                        if (!str3.startsWith("\\b")) {
                            stringBuffer.append(str2);
                        }
                        stringBuffer.append(str3);
                        if (!(str3.endsWith("\\b") || str3.endsWith("."))) {
                            stringBuffer.append(str2);
                        }
                        stringBuffer.append("|");
                    }
                }
            }
            str2 = stringBuffer.toString();
            if (str2.endsWith("|")) {
                str2 = str2.substring(0, str2.length() - 1);
            }
        }
        return str2;
    }

    public String getWithoutB(String str) {
        if (this.param == null) {
            return null;
        }
        String str2;
        if (this.param.get(str) == null) {
            str2 = "";
        } else {
            str2 = (String) this.param.get(str);
        }
        if (str2.indexOf(WITHNOT_B) != -1) {
            String[] split = str2.split("\\|");
            StringBuffer stringBuffer = new StringBuffer();
            for (String str3 : split) {
                if (!(str3.trim().isEmpty() || str3.equals(SAVE_OR) || str3.equals(WITHNOT_B))) {
                    stringBuffer.append(str3).append("|");
                }
            }
            str2 = stringBuffer.toString();
            if (str2.endsWith("|")) {
                str2 = str2.substring(0, str2.length() - 1);
            }
        }
        return str2;
    }

    public static boolean isRelDates(String str, String str2) {
        if (str.trim().isEmpty()) {
            return true;
        }
        if (str.trim().equals(",")) {
            if (str2.equalsIgnoreCase("zh_hans") || str2.equalsIgnoreCase("ja") || str2.equalsIgnoreCase("ko")) {
                return false;
            }
            return true;
        } else if (str.trim().equals("\u060c") && str2.equalsIgnoreCase("ur")) {
            return true;
        } else {
            return false;
        }
    }
}
