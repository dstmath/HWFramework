package org.bouncycastle.i18n.filter;

import org.bouncycastle.asn1.eac.EACTags;

public class HTMLFilter implements Filter {
    @Override // org.bouncycastle.i18n.filter.Filter
    public String doFilter(String str) {
        String str2;
        int i;
        StringBuffer stringBuffer = new StringBuffer(str);
        int i2 = 0;
        while (i2 < stringBuffer.length()) {
            char charAt = stringBuffer.charAt(i2);
            if (charAt == '\"') {
                i = i2 + 1;
                str2 = "&#34";
            } else if (charAt == '#') {
                i = i2 + 1;
                str2 = "&#35";
            } else if (charAt == '+') {
                i = i2 + 1;
                str2 = "&#43";
            } else if (charAt == '-') {
                i = i2 + 1;
                str2 = "&#45";
            } else if (charAt == '>') {
                i = i2 + 1;
                str2 = "&#62";
            } else if (charAt == ';') {
                i = i2 + 1;
                str2 = "&#59";
            } else if (charAt != '<') {
                switch (charAt) {
                    case EACTags.APPLICATION_EFFECTIVE_DATE /* 37 */:
                        i = i2 + 1;
                        str2 = "&#37";
                        break;
                    case EACTags.CARD_EFFECTIVE_DATE /* 38 */:
                        i = i2 + 1;
                        str2 = "&#38";
                        break;
                    case EACTags.INTERCHANGE_CONTROL /* 39 */:
                        i = i2 + 1;
                        str2 = "&#39";
                        break;
                    case '(':
                        i = i2 + 1;
                        str2 = "&#40";
                        break;
                    case EACTags.INTERCHANGE_PROFILE /* 41 */:
                        i = i2 + 1;
                        str2 = "&#41";
                        break;
                    default:
                        i2 -= 3;
                        continue;
                        i2 += 4;
                }
            } else {
                i = i2 + 1;
                str2 = "&#60";
            }
            stringBuffer.replace(i2, i, str2);
            i2 += 4;
        }
        return stringBuffer.toString();
    }

    @Override // org.bouncycastle.i18n.filter.Filter
    public String doFilterUrl(String str) {
        return doFilter(str);
    }
}
