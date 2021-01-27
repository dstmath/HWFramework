package com.huawei.i18n.tmr.datetime.utils;

import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.datetime.utils.digit.LocaleDigit;
import com.huawei.i18n.tmr.datetime.utils.digit.LocaleDigitBn;
import com.huawei.i18n.tmr.datetime.utils.digit.LocaleDigitFa;
import com.huawei.i18n.tmr.datetime.utils.digit.LocaleDigitNe;
import com.huawei.i18n.tmr.datetime.utils.digit.LocaleDigitZh;
import java.util.Locale;

public class StringConvert {
    private String convertQanChar(String instr) {
        StringBuffer retsb = new StringBuffer(StorageManagerExt.INVALID_KEY_DESC);
        for (int i = 0; i < instr.length(); i++) {
            String tempstr = instr.substring(i, i + 1);
            int index = "　：／．＼∕，.！（）？﹡；：【】－＋＝｛｝１２３４５６７８９０ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ".indexOf(tempstr);
            if (index == -1) {
                retsb.append(tempstr);
            } else {
                retsb.append(" :/.\\/,.!()?*;:[]-+={}1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(index, index + 1));
            }
        }
        return retsb.toString();
    }

    private String replaceZh(String content) {
        return content.replaceAll("礼拜", "星期").replaceAll("星期天", "星期日").replaceAll("週", "周").replaceAll("周天", "周日").replaceAll("後", "后").replaceAll("個", "个").replaceAll("兩", "两").replaceAll("鍾", "钟");
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public String convertString(String content, String locale) {
        char c;
        switch (locale.hashCode()) {
            case -325339409:
                if (locale.equals("zh_hans")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 3122:
                if (locale.equals("as")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 3139:
                if (locale.equals("be")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 3148:
                if (locale.equals("bn")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 3241:
                if (locale.equals("en")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 3259:
                if (locale.equals("fa")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 3424:
                if (locale.equals("kk")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 3464:
                if (locale.equals("lt")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 3493:
                if (locale.equals("mr")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 3511:
                if (locale.equals("ne")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 3651:
                if (locale.equals("ru")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 107861:
                if (locale.equals("mai")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
                return replaceZh(convertQanChar(content));
            case 2:
                return convertDigit(content, "fa");
            case 3:
                return convertDigit(content, "ne");
            case 4:
                return convertDigit(content, "bn");
            case 5:
                return convertDigit(content, "as");
            case 6:
                return convertDigit(content, "mr");
            case 7:
                return convertDigit(content, "mai");
            case '\b':
            case '\t':
            case '\n':
            case 11:
                return content.toLowerCase(new Locale(locale));
            default:
                return content;
        }
    }

    public String convertDigit(String content, String locale) {
        LocaleDigit result = getLocaleDigit(locale);
        if (result == null) {
            return content;
        }
        return result.convert(content);
    }

    public boolean isDigit(String content, String locale) {
        LocaleDigit result = getLocaleDigit(locale);
        if (result == null) {
            return false;
        }
        return result.isDigit(content);
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0093  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x009a  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:55:? A[RETURN, SYNTHETIC] */
    private LocaleDigit getLocaleDigit(String locale) {
        char c;
        int hashCode = locale.hashCode();
        if (hashCode != -325339409) {
            if (hashCode != 3122) {
                if (hashCode != 3148) {
                    if (hashCode != 3241) {
                        if (hashCode != 3259) {
                            if (hashCode != 3383) {
                                if (hashCode != 3493) {
                                    if (hashCode != 3511) {
                                        if (hashCode == 107861 && locale.equals("mai")) {
                                            c = 6;
                                            switch (c) {
                                                case 0:
                                                case 1:
                                                case 2:
                                                    return new LocaleDigitZh();
                                                case 3:
                                                    return new LocaleDigitFa();
                                                case 4:
                                                case 5:
                                                case 6:
                                                    return new LocaleDigitNe();
                                                case 7:
                                                case '\b':
                                                    return new LocaleDigitBn();
                                                default:
                                                    return null;
                                            }
                                        }
                                    } else if (locale.equals("ne")) {
                                        c = 4;
                                        switch (c) {
                                        }
                                    }
                                } else if (locale.equals("mr")) {
                                    c = 5;
                                    switch (c) {
                                    }
                                }
                            } else if (locale.equals("ja")) {
                                c = 1;
                                switch (c) {
                                }
                            }
                        } else if (locale.equals("fa")) {
                            c = 3;
                            switch (c) {
                            }
                        }
                    } else if (locale.equals("en")) {
                        c = 2;
                        switch (c) {
                        }
                    }
                } else if (locale.equals("bn")) {
                    c = 7;
                    switch (c) {
                    }
                }
            } else if (locale.equals("as")) {
                c = '\b';
                switch (c) {
                }
            }
        } else if (locale.equals("zh_hans")) {
            c = 0;
            switch (c) {
            }
        }
        c = 65535;
        switch (c) {
        }
    }
}
