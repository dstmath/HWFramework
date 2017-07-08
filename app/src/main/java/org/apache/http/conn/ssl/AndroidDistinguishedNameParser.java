package org.apache.http.conn.ssl;

import android.telephony.PhoneNumberUtils;
import android.text.format.DateFormat;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.RILConstants;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.security.auth.x500.X500Principal;

@Deprecated
final class AndroidDistinguishedNameParser {
    private int beg;
    private char[] chars;
    private int cur;
    private final String dn;
    private int end;
    private final int length;
    private int pos;

    public AndroidDistinguishedNameParser(X500Principal principal) {
        this.dn = principal.getName("RFC2253");
        this.length = this.dn.length();
    }

    private String nextAT() {
        while (this.pos < this.length && this.chars[this.pos] == ' ') {
            this.pos++;
        }
        if (this.pos == this.length) {
            return null;
        }
        this.beg = this.pos;
        this.pos++;
        while (this.pos < this.length && this.chars[this.pos] != '=' && this.chars[this.pos] != ' ') {
            this.pos++;
        }
        if (this.pos >= this.length) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        this.end = this.pos;
        if (this.chars[this.pos] == ' ') {
            while (this.pos < this.length && this.chars[this.pos] != '=' && this.chars[this.pos] == ' ') {
                this.pos++;
            }
            if (this.chars[this.pos] != '=' || this.pos == this.length) {
                throw new IllegalStateException("Unexpected end of DN: " + this.dn);
            }
        }
        this.pos++;
        while (this.pos < this.length && this.chars[this.pos] == ' ') {
            this.pos++;
        }
        if (this.end - this.beg > 4 && this.chars[this.beg + 3] == '.' && ((this.chars[this.beg] == 'O' || this.chars[this.beg] == 'o') && ((this.chars[this.beg + 1] == 'I' || this.chars[this.beg + 1] == 'i') && (this.chars[this.beg + 2] == 'D' || this.chars[this.beg + 2] == DateFormat.DATE)))) {
            this.beg += 4;
        }
        return new String(this.chars, this.beg, this.end - this.beg);
    }

    private String quotedAV() {
        this.pos++;
        this.beg = this.pos;
        this.end = this.beg;
        while (this.pos != this.length) {
            if (this.chars[this.pos] == '\"') {
                this.pos++;
                while (this.pos < this.length && this.chars[this.pos] == ' ') {
                    this.pos++;
                }
                return new String(this.chars, this.beg, this.end - this.beg);
            }
            if (this.chars[this.pos] == '\\') {
                this.chars[this.end] = getEscaped();
            } else {
                this.chars[this.end] = this.chars[this.pos];
            }
            this.pos++;
            this.end++;
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    private String hexAV() {
        if (this.pos + 4 >= this.length) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        int hexLen;
        this.beg = this.pos;
        this.pos++;
        while (this.pos != this.length && this.chars[this.pos] != '+' && this.chars[this.pos] != PhoneNumberUtils.PAUSE && this.chars[this.pos] != PhoneNumberUtils.WAIT) {
            if (this.chars[this.pos] == ' ') {
                this.end = this.pos;
                this.pos++;
                while (this.pos < this.length && this.chars[this.pos] == ' ') {
                    this.pos++;
                }
                hexLen = this.end - this.beg;
                if (hexLen >= 5 || (hexLen & 1) == 0) {
                    throw new IllegalStateException("Unexpected end of DN: " + this.dn);
                }
                byte[] encoded = new byte[(hexLen / 2)];
                int p = this.beg + 1;
                for (int i = 0; i < encoded.length; i++) {
                    encoded[i] = (byte) getByte(p);
                    p += 2;
                }
                return new String(this.chars, this.beg, hexLen);
            }
            if (this.chars[this.pos] >= DateFormat.CAPITAL_AM_PM && this.chars[this.pos] <= 'F') {
                char[] cArr = this.chars;
                int i2 = this.pos;
                cArr[i2] = (char) (cArr[i2] + 32);
            }
            this.pos++;
        }
        this.end = this.pos;
        hexLen = this.end - this.beg;
        if (hexLen >= 5) {
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    private String escapedAV() {
        this.beg = this.pos;
        this.end = this.pos;
        while (this.pos < this.length) {
            char[] cArr;
            int i;
            switch (this.chars[this.pos]) {
                case IndexSearchConstants.INDEX_BUILD_FLAG_INTERNAL_FILE /*32*/:
                    this.cur = this.end;
                    this.pos++;
                    cArr = this.chars;
                    i = this.end;
                    this.end = i + 1;
                    cArr[i] = ' ';
                    while (this.pos < this.length && this.chars[this.pos] == ' ') {
                        cArr = this.chars;
                        i = this.end;
                        this.end = i + 1;
                        cArr[i] = ' ';
                        this.pos++;
                    }
                    if (!(this.pos == this.length || this.chars[this.pos] == PhoneNumberUtils.PAUSE || this.chars[this.pos] == '+')) {
                        if (this.chars[this.pos] != PhoneNumberUtils.WAIT) {
                            break;
                        }
                    }
                    return new String(this.chars, this.beg, this.cur - this.beg);
                case StatisticalConstant.TYPE_SINGLEHAND_ENTER_2S_EXIT /*43*/:
                case RILConstants.RIL_REQUEST_CHANGE_BARRING_PASSWORD /*44*/:
                case RILConstants.RIL_REQUEST_OEM_HOOK_RAW /*59*/:
                    return new String(this.chars, this.beg, this.end - this.beg);
                case RILConstants.RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG /*92*/:
                    cArr = this.chars;
                    i = this.end;
                    this.end = i + 1;
                    cArr[i] = getEscaped();
                    this.pos++;
                    break;
                default:
                    cArr = this.chars;
                    i = this.end;
                    this.end = i + 1;
                    cArr[i] = this.chars[this.pos];
                    this.pos++;
                    break;
            }
        }
        return new String(this.chars, this.beg, this.end - this.beg);
    }

    private char getEscaped() {
        this.pos++;
        if (this.pos == this.length) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        switch (this.chars[this.pos]) {
            case IndexSearchConstants.INDEX_BUILD_FLAG_INTERNAL_FILE /*32*/:
            case StatisticalConstant.TYPE_MULTIWINDOW_FRAME_SIZE_CHANGED /*34*/:
            case PerfHub.PERF_TAG_AVL_B_CPU_FREQ_LIST /*35*/:
            case PerfHub.PERF_TAG_AVL_DDR_FREQ_LIST /*37*/:
            case StatisticalConstant.TYPE_SINGLEHAND_EXIT /*42*/:
            case StatisticalConstant.TYPE_SINGLEHAND_ENTER_2S_EXIT /*43*/:
            case RILConstants.RIL_REQUEST_CHANGE_BARRING_PASSWORD /*44*/:
            case RILConstants.RIL_REQUEST_OEM_HOOK_RAW /*59*/:
            case StatisticalConstant.TYPE_MEDIA_FOUNCTION_STATICS /*60*/:
            case StatisticalConstant.TYPE_WIFI_END /*61*/:
            case RILConstants.RIL_REQUEST_SET_SUPP_SVC_NOTIFICATION /*62*/:
            case RILConstants.RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG /*92*/:
            case RILConstants.RIL_REQUEST_CDMA_SUBSCRIPTION /*95*/:
                return this.chars[this.pos];
            default:
                return getUTF8();
        }
    }

    private char getUTF8() {
        int res = getByte(this.pos);
        this.pos++;
        if (res < LogPower.START_CHG_ROTATION) {
            return (char) res;
        }
        if (res < MetricsEvent.ACTION_LS_NOTE || res > MetricsEvent.FINGERPRINT_FIND_SENSOR_SETUP) {
            return '?';
        }
        int count;
        if (res <= MetricsEvent.DOZING) {
            count = 1;
            res &= 31;
        } else if (res <= MetricsEvent.ACTION_ASSIST_LONG_PRESS) {
            count = 2;
            res &= 15;
        } else {
            count = 3;
            res &= 7;
        }
        for (int i = 0; i < count; i++) {
            this.pos++;
            if (this.pos == this.length || this.chars[this.pos] != '\\') {
                return '?';
            }
            this.pos++;
            int b = getByte(this.pos);
            this.pos++;
            if ((b & MetricsEvent.ACTION_LS_NOTE) != LogPower.START_CHG_ROTATION) {
                return '?';
            }
            res = (res << 6) + (b & 63);
        }
        return (char) res;
    }

    private int getByte(int position) {
        if (position + 1 >= this.length) {
            throw new IllegalStateException("Malformed DN: " + this.dn);
        }
        int b1 = this.chars[position];
        if (b1 >= 48 && b1 <= 57) {
            b1 -= 48;
        } else if (b1 >= 97 && b1 <= LogPower.WEBVIEW_RESUMED) {
            b1 -= 87;
        } else if (b1 < 65 || b1 > 70) {
            throw new IllegalStateException("Malformed DN: " + this.dn);
        } else {
            b1 -= 55;
        }
        int b2 = this.chars[position + 1];
        if (b2 >= 48 && b2 <= 57) {
            b2 -= 48;
        } else if (b2 >= 97 && b2 <= LogPower.WEBVIEW_RESUMED) {
            b2 -= 87;
        } else if (b2 < 65 || b2 > 70) {
            throw new IllegalStateException("Malformed DN: " + this.dn);
        } else {
            b2 -= 55;
        }
        return (b1 << 4) + b2;
    }

    public String findMostSpecific(String attributeType) {
        this.pos = 0;
        this.beg = 0;
        this.end = 0;
        this.cur = 0;
        this.chars = this.dn.toCharArray();
        String attType = nextAT();
        if (attType == null) {
            return null;
        }
        do {
            String attValue = "";
            if (this.pos == this.length) {
                return null;
            }
            switch (this.chars[this.pos]) {
                case StatisticalConstant.TYPE_MULTIWINDOW_FRAME_SIZE_CHANGED /*34*/:
                    attValue = quotedAV();
                    break;
                case PerfHub.PERF_TAG_AVL_B_CPU_FREQ_LIST /*35*/:
                    attValue = hexAV();
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_ENTER_2S_EXIT /*43*/:
                case RILConstants.RIL_REQUEST_CHANGE_BARRING_PASSWORD /*44*/:
                case RILConstants.RIL_REQUEST_OEM_HOOK_RAW /*59*/:
                    break;
                default:
                    attValue = escapedAV();
                    break;
            }
            if (attributeType.equalsIgnoreCase(attType)) {
                return attValue;
            }
            if (this.pos >= this.length) {
                return null;
            }
            if (this.chars[this.pos] == PhoneNumberUtils.PAUSE || this.chars[this.pos] == PhoneNumberUtils.WAIT || this.chars[this.pos] == '+') {
                this.pos++;
                attType = nextAT();
            } else {
                throw new IllegalStateException("Malformed DN: " + this.dn);
            }
        } while (attType != null);
        throw new IllegalStateException("Malformed DN: " + this.dn);
    }

    public List<String> getAllMostSpecificFirst(String attributeType) {
        this.pos = 0;
        this.beg = 0;
        this.end = 0;
        this.cur = 0;
        this.chars = this.dn.toCharArray();
        List<String> result = Collections.emptyList();
        String attType = nextAT();
        if (attType == null) {
            return result;
        }
        while (this.pos < this.length) {
            String attValue = "";
            switch (this.chars[this.pos]) {
                case StatisticalConstant.TYPE_MULTIWINDOW_FRAME_SIZE_CHANGED /*34*/:
                    attValue = quotedAV();
                    break;
                case PerfHub.PERF_TAG_AVL_B_CPU_FREQ_LIST /*35*/:
                    attValue = hexAV();
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_ENTER_2S_EXIT /*43*/:
                case RILConstants.RIL_REQUEST_CHANGE_BARRING_PASSWORD /*44*/:
                case RILConstants.RIL_REQUEST_OEM_HOOK_RAW /*59*/:
                    break;
                default:
                    attValue = escapedAV();
                    break;
            }
            if (attributeType.equalsIgnoreCase(attType)) {
                if (result.isEmpty()) {
                    result = new ArrayList();
                }
                result.add(attValue);
            }
            if (this.pos >= this.length) {
                return result;
            }
            if (this.chars[this.pos] == PhoneNumberUtils.PAUSE || this.chars[this.pos] == PhoneNumberUtils.WAIT || this.chars[this.pos] == '+') {
                this.pos++;
                attType = nextAT();
                if (attType == null) {
                    throw new IllegalStateException("Malformed DN: " + this.dn);
                }
            }
            throw new IllegalStateException("Malformed DN: " + this.dn);
        }
        return result;
    }
}
