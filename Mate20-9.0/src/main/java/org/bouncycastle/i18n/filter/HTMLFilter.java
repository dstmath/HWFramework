package org.bouncycastle.i18n.filter;

import org.bouncycastle.asn1.eac.EACTags;

public class HTMLFilter implements Filter {
    public String doFilter(String str) {
        int i;
        String str2;
        StringBuffer stringBuffer = new StringBuffer(str);
        int i2 = 0;
        while (i2 < stringBuffer.length()) {
            char charAt = stringBuffer.charAt(i2);
            if (charAt == '+') {
                i = i2 + 1;
                str2 = "&#43";
            } else if (charAt == '-') {
                i = i2 + 1;
                str2 = "&#45";
            } else if (charAt != '>') {
                switch (charAt) {
                    case '\"':
                        i = i2 + 1;
                        str2 = "&#34";
                        break;
                    case '#':
                        i = i2 + 1;
                        str2 = "&#35";
                        break;
                    default:
                        switch (charAt) {
                            case EACTags.APPLICATION_EFFECTIVE_DATE:
                                i = i2 + 1;
                                str2 = "&#37";
                                break;
                            case EACTags.CARD_EFFECTIVE_DATE:
                                i = i2 + 1;
                                str2 = "&#38";
                                break;
                            case EACTags.INTERCHANGE_CONTROL:
                                i = i2 + 1;
                                str2 = "&#39";
                                break;
                            case '(':
                                i = i2 + 1;
                                str2 = "&#40";
                                break;
                            case EACTags.INTERCHANGE_PROFILE:
                                i = i2 + 1;
                                str2 = "&#41";
                                break;
                            default:
                                switch (charAt) {
                                    case ';':
                                        i = i2 + 1;
                                        str2 = "&#59";
                                        break;
                                    case '<':
                                        i = i2 + 1;
                                        str2 = "&#60";
                                        break;
                                    default:
                                        i2 -= 3;
                                        continue;
                                        continue;
                                        continue;
                                }
                        }
                }
            } else {
                i = i2 + 1;
                str2 = "&#62";
            }
            stringBuffer.replace(i2, i, str2);
            i2 += 4;
        }
        return stringBuffer.toString();
    }

    public String doFilterUrl(String str) {
        return doFilter(str);
    }
}
