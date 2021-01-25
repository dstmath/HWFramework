package ohos.global.icu.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import ohos.global.icu.text.CharsetRecog_2022;
import ohos.global.icu.text.CharsetRecog_Unicode;
import ohos.global.icu.text.CharsetRecog_mbcs;
import ohos.global.icu.text.CharsetRecog_sbcs;

public class CharsetDetector {
    private static final List<CSRecognizerInfo> ALL_CS_RECOGNIZERS;
    private static final int kBufSize = 8000;
    short[] fByteStats = new short[256];
    boolean fC1Bytes = false;
    String fDeclaredEncoding;
    private boolean[] fEnabledRecognizers;
    byte[] fInputBytes = new byte[kBufSize];
    int fInputLen;
    InputStream fInputStream;
    byte[] fRawInput;
    int fRawLength;
    private boolean fStripTags = false;

    public CharsetDetector setDeclaredEncoding(String str) {
        this.fDeclaredEncoding = str;
        return this;
    }

    public CharsetDetector setText(byte[] bArr) {
        this.fRawInput = bArr;
        this.fRawLength = bArr.length;
        return this;
    }

    public CharsetDetector setText(InputStream inputStream) throws IOException {
        this.fInputStream = inputStream;
        InputStream inputStream2 = this.fInputStream;
        int i = kBufSize;
        inputStream2.mark(kBufSize);
        this.fRawInput = new byte[kBufSize];
        this.fRawLength = 0;
        while (i > 0) {
            int read = this.fInputStream.read(this.fRawInput, this.fRawLength, i);
            if (read <= 0) {
                break;
            }
            this.fRawLength += read;
            i -= read;
        }
        this.fInputStream.reset();
        return this;
    }

    public CharsetMatch detect() {
        CharsetMatch[] detectAll = detectAll();
        if (detectAll == null || detectAll.length == 0) {
            return null;
        }
        return detectAll[0];
    }

    public CharsetMatch[] detectAll() {
        CharsetMatch match;
        ArrayList arrayList = new ArrayList();
        MungeInput();
        for (int i = 0; i < ALL_CS_RECOGNIZERS.size(); i++) {
            CSRecognizerInfo cSRecognizerInfo = ALL_CS_RECOGNIZERS.get(i);
            boolean[] zArr = this.fEnabledRecognizers;
            if ((zArr != null ? zArr[i] : cSRecognizerInfo.isDefaultEnabled) && (match = cSRecognizerInfo.recognizer.match(this)) != null) {
                arrayList.add(match);
            }
        }
        Collections.sort(arrayList);
        Collections.reverse(arrayList);
        return (CharsetMatch[]) arrayList.toArray(new CharsetMatch[arrayList.size()]);
    }

    public Reader getReader(InputStream inputStream, String str) {
        this.fDeclaredEncoding = str;
        try {
            setText(inputStream);
            CharsetMatch detect = detect();
            if (detect == null) {
                return null;
            }
            return detect.getReader();
        } catch (IOException unused) {
            return null;
        }
    }

    public String getString(byte[] bArr, String str) {
        this.fDeclaredEncoding = str;
        try {
            setText(bArr);
            CharsetMatch detect = detect();
            if (detect == null) {
                return null;
            }
            return detect.getString(-1);
        } catch (IOException unused) {
            return null;
        }
    }

    public static String[] getAllDetectableCharsets() {
        String[] strArr = new String[ALL_CS_RECOGNIZERS.size()];
        for (int i = 0; i < strArr.length; i++) {
            strArr[i] = ALL_CS_RECOGNIZERS.get(i).recognizer.getName();
        }
        return strArr;
    }

    public boolean inputFilterEnabled() {
        return this.fStripTags;
    }

    public boolean enableInputFilter(boolean z) {
        boolean z2 = this.fStripTags;
        this.fStripTags = z;
        return z2;
    }

    private void MungeInput() {
        int i;
        int i2;
        if (this.fStripTags) {
            int i3 = 0;
            i2 = 0;
            i = 0;
            boolean z = false;
            for (int i4 = 0; i4 < this.fRawLength && i3 < this.fInputBytes.length; i4++) {
                byte b = this.fRawInput[i4];
                if (b == 60) {
                    if (z) {
                        i++;
                    }
                    i2++;
                    z = true;
                }
                if (!z) {
                    this.fInputBytes[i3] = b;
                    i3++;
                }
                if (b == 62) {
                    z = false;
                }
            }
            this.fInputLen = i3;
        } else {
            i2 = 0;
            i = 0;
        }
        if (i2 < 5 || i2 / 5 < i || (this.fInputLen < 100 && this.fRawLength > 600)) {
            int i5 = this.fRawLength;
            if (i5 > kBufSize) {
                i5 = kBufSize;
            }
            int i6 = 0;
            while (i6 < i5) {
                this.fInputBytes[i6] = this.fRawInput[i6];
                i6++;
            }
            this.fInputLen = i6;
        }
        Arrays.fill(this.fByteStats, (short) 0);
        for (int i7 = 0; i7 < this.fInputLen; i7++) {
            int i8 = this.fInputBytes[i7] & 255;
            short[] sArr = this.fByteStats;
            sArr[i8] = (short) (sArr[i8] + 1);
        }
        this.fC1Bytes = false;
        for (int i9 = 128; i9 <= 159; i9++) {
            if (this.fByteStats[i9] != 0) {
                this.fC1Bytes = true;
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class CSRecognizerInfo {
        boolean isDefaultEnabled;
        CharsetRecognizer recognizer;

        CSRecognizerInfo(CharsetRecognizer charsetRecognizer, boolean z) {
            this.recognizer = charsetRecognizer;
            this.isDefaultEnabled = z;
        }
    }

    static {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_UTF8(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_Unicode.CharsetRecog_UTF_16_BE(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_Unicode.CharsetRecog_UTF_16_LE(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_Unicode.CharsetRecog_UTF_32_BE(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_Unicode.CharsetRecog_UTF_32_LE(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_mbcs.CharsetRecog_sjis(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_2022.CharsetRecog_2022JP(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_2022.CharsetRecog_2022CN(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_2022.CharsetRecog_2022KR(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_mbcs.CharsetRecog_gb_18030(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_mbcs.CharsetRecog_euc.CharsetRecog_euc_jp(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_mbcs.CharsetRecog_euc.CharsetRecog_euc_kr(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_mbcs.CharsetRecog_big5(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_1(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_2(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_5_ru(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_6_ar(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_7_el(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_8_I_he(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_8_he(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_windows_1251(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_windows_1256(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_KOI8_R(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_8859_9_tr(), true));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_IBM424_he_rtl(), false));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_IBM424_he_ltr(), false));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_IBM420_ar_rtl(), false));
        arrayList.add(new CSRecognizerInfo(new CharsetRecog_sbcs.CharsetRecog_IBM420_ar_ltr(), false));
        ALL_CS_RECOGNIZERS = Collections.unmodifiableList(arrayList);
    }

    @Deprecated
    public String[] getDetectableCharsets() {
        ArrayList arrayList = new ArrayList(ALL_CS_RECOGNIZERS.size());
        for (int i = 0; i < ALL_CS_RECOGNIZERS.size(); i++) {
            CSRecognizerInfo cSRecognizerInfo = ALL_CS_RECOGNIZERS.get(i);
            boolean[] zArr = this.fEnabledRecognizers;
            if (zArr == null ? cSRecognizerInfo.isDefaultEnabled : zArr[i]) {
                arrayList.add(cSRecognizerInfo.recognizer.getName());
            }
        }
        return (String[]) arrayList.toArray(new String[arrayList.size()]);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0028, code lost:
        r2 = false;
     */
    @Deprecated
    public CharsetDetector setDetectableCharset(String str, boolean z) {
        boolean z2;
        int i = 0;
        while (true) {
            if (i >= ALL_CS_RECOGNIZERS.size()) {
                i = -1;
                break;
            }
            CSRecognizerInfo cSRecognizerInfo = ALL_CS_RECOGNIZERS.get(i);
            if (!cSRecognizerInfo.recognizer.getName().equals(str)) {
                i++;
            } else if (cSRecognizerInfo.isDefaultEnabled == z) {
                z2 = true;
            }
        }
        if (i >= 0) {
            if (this.fEnabledRecognizers == null && !z2) {
                this.fEnabledRecognizers = new boolean[ALL_CS_RECOGNIZERS.size()];
                for (int i2 = 0; i2 < ALL_CS_RECOGNIZERS.size(); i2++) {
                    this.fEnabledRecognizers[i2] = ALL_CS_RECOGNIZERS.get(i2).isDefaultEnabled;
                }
            }
            boolean[] zArr = this.fEnabledRecognizers;
            if (zArr != null) {
                zArr[i] = z;
            }
            return this;
        }
        throw new IllegalArgumentException("Invalid encoding: \"" + str + "\"");
    }
}
