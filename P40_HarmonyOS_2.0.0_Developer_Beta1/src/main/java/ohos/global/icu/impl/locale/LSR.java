package ohos.global.icu.impl.locale;

import java.util.Objects;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;

public final class LSR {
    public static final boolean DEBUG_OUTPUT = false;
    public static final int REGION_INDEX_LIMIT = 1677;
    public final String language;
    public final String region;
    final int regionIndex;
    public final String script;

    public LSR(String str, String str2, String str3) {
        this.language = str;
        this.script = str2;
        this.region = str3;
        this.regionIndex = indexForRegion(str3);
    }

    public static final int indexForRegion(String str) {
        int charAt;
        int charAt2;
        int charAt3;
        int charAt4;
        if (str.length() == 2) {
            int charAt5 = str.charAt(0) - 'A';
            if (charAt5 < 0 || 25 < charAt5 || str.charAt(1) - 'A' < 0 || 25 < charAt4) {
                return 0;
            }
            return (charAt5 * 26) + charAt4 + 1001;
        } else if (str.length() != 3 || str.charAt(0) - '0' < 0 || 9 < charAt || str.charAt(1) - '0' < 0 || 9 < charAt2 || str.charAt(2) - '0' < 0 || 9 < charAt3) {
            return 0;
        } else {
            return (((charAt * 10) + charAt2) * 10) + charAt3 + 1;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.language);
        if (!this.script.isEmpty()) {
            sb.append(LocaleUtility.IETF_SEPARATOR);
            sb.append(this.script);
        }
        if (!this.region.isEmpty()) {
            sb.append(LocaleUtility.IETF_SEPARATOR);
            sb.append(this.region);
        }
        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (this != obj) {
            if (obj != null && obj.getClass() == getClass()) {
                LSR lsr = (LSR) obj;
                if (!this.language.equals(lsr.language) || !this.script.equals(lsr.script) || !this.region.equals(lsr.region)) {
                }
            }
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.language, this.script, this.region);
    }
}
