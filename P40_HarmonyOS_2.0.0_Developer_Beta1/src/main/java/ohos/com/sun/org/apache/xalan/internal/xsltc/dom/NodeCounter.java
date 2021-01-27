package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.util.Vector;
import ohos.agp.window.service.WindowManager;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.Translet;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.global.icu.impl.locale.LanguageTag;

public abstract class NodeCounter {
    public static final int END = -1;
    private static final String[] Hundreds = {"", "c", "cc", "ccc", "cd", "d", "dc", "dcc", "dccc", "cm"};
    private static final String[] Ones = {"", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix"};
    private static final String[] Tens = {"", LanguageTag.PRIVATEUSE, "xx", "xxx", "xl", "l", "lx", "lxx", "lxxx", "xc"};
    private static final String[] Thousands = {"", "m", "mm", "mmm"};
    public final DOM _document;
    protected String _format;
    private Vector _formatToks = new Vector();
    protected String _groupSep;
    protected int _groupSize;
    protected boolean _hasFrom;
    public final DTMAxisIterator _iterator;
    protected String _lang;
    protected String _letterValue;
    private int _nFormats = 0;
    private int _nSepars = 0;
    protected int _node = -1;
    protected int _nodeType = -1;
    private boolean _separFirst = true;
    private boolean _separLast = false;
    private Vector _separToks = new Vector();
    private StringBuilder _tempBuffer = new StringBuilder();
    public final Translet _translet;
    protected double _value = -2.147483648E9d;

    public abstract String getCounter();

    public boolean matchesFrom(int i) {
        return false;
    }

    public abstract NodeCounter setStartNode(int i);

    protected NodeCounter(Translet translet, DOM dom, DTMAxisIterator dTMAxisIterator) {
        this._translet = translet;
        this._document = dom;
        this._iterator = dTMAxisIterator;
    }

    protected NodeCounter(Translet translet, DOM dom, DTMAxisIterator dTMAxisIterator, boolean z) {
        this._translet = translet;
        this._document = dom;
        this._iterator = dTMAxisIterator;
        this._hasFrom = z;
    }

    public NodeCounter setValue(double d) {
        this._value = d;
        return this;
    }

    /* access modifiers changed from: protected */
    public void setFormatting(String str, String str2, String str3, String str4, String str5) {
        this._lang = str2;
        this._groupSep = str4;
        this._letterValue = str3;
        this._groupSize = parseStringToAnInt(str5);
        setTokens(str);
    }

    private int parseStringToAnInt(String str) {
        int length;
        int i;
        int i2;
        int i3;
        int i4;
        if (str == null || (length = str.length()) <= 0) {
            return 0;
        }
        if (str.charAt(0) == '-') {
            i2 = Integer.MIN_VALUE;
            i = 1;
        } else {
            i2 = WindowManager.LayoutConfig.INPUT_STATE_ALWAYS_HIDDEN;
            i = 0;
        }
        int i5 = i2 / 10;
        if (i < length) {
            int i6 = i + 1;
            int digit = Character.digit(str.charAt(i), 10);
            if (digit < 0) {
                return 0;
            }
            i3 = -digit;
            i = i6;
        } else {
            i3 = 0;
        }
        while (i < length) {
            int i7 = i + 1;
            int digit2 = Character.digit(str.charAt(i), 10);
            if (digit2 < 0 || i3 < i5 || (i4 = i3 * 10) < i2 + digit2) {
                return 0;
            }
            i3 = i4 - digit2;
            i = i7;
        }
        if (i == 0) {
            return -i3;
        }
        if (i > 1) {
            return i3;
        }
        return 0;
    }

    private final void setTokens(String str) {
        String str2 = this._format;
        if (str2 == null || !str.equals(str2)) {
            this._format = str;
            int length = this._format.length();
            this._separFirst = true;
            this._separLast = false;
            this._nSepars = 0;
            this._nFormats = 0;
            this._separToks.clear();
            this._formatToks.clear();
            boolean z = true;
            int i = 0;
            while (i < length) {
                char charAt = str.charAt(i);
                int i2 = i;
                while (Character.isLetterOrDigit(charAt) && (i2 = i2 + 1) != length) {
                    charAt = str.charAt(i2);
                }
                if (i2 > i) {
                    if (z) {
                        this._separToks.addElement(".");
                        this._separFirst = false;
                        z = false;
                    }
                    this._formatToks.addElement(str.substring(i, i2));
                }
                if (i2 == length) {
                    break;
                }
                char charAt2 = str.charAt(i2);
                boolean z2 = z;
                int i3 = i2;
                while (!Character.isLetterOrDigit(charAt2) && (i3 = i3 + 1) != length) {
                    charAt2 = str.charAt(i3);
                    z2 = false;
                }
                i = i3;
                if (i > i2) {
                    this._separToks.addElement(str.substring(i2, i));
                }
                z = z2;
            }
            this._nSepars = this._separToks.size();
            this._nFormats = this._formatToks.size();
            if (this._nSepars > this._nFormats) {
                this._separLast = true;
            }
            if (this._separFirst) {
                this._nSepars--;
            }
            if (this._separLast) {
                this._nSepars--;
            }
            if (this._nSepars == 0) {
                this._separToks.insertElementAt(".", 1);
                this._nSepars++;
            }
            if (this._separFirst) {
                this._nSepars++;
            }
        }
    }

    public NodeCounter setDefaultFormatting() {
        setFormatting("1", "en", Constants.ATTRVAL_ALPHABETIC, null, null);
        return this;
    }

    public String getCounter(String str, String str2, String str3, String str4, String str5) {
        setFormatting(str, str2, str3, str4, str5);
        return getCounter();
    }

    public boolean matchesCount(int i) {
        return this._nodeType == this._document.getExpandedTypeID(i);
    }

    /* access modifiers changed from: protected */
    public String formatNumbers(int i) {
        return formatNumbers(new int[]{i});
    }

    /* access modifiers changed from: protected */
    public String formatNumbers(int[] iArr) {
        boolean z = true;
        for (int i : iArr) {
            if (i != Integer.MIN_VALUE) {
                z = false;
            }
        }
        if (z) {
            return "";
        }
        this._tempBuffer.setLength(0);
        StringBuilder sb = this._tempBuffer;
        if (this._separFirst) {
            sb.append((String) this._separToks.elementAt(0));
        }
        int i2 = 0;
        boolean z2 = true;
        int i3 = 1;
        for (int i4 : iArr) {
            if (i4 != Integer.MIN_VALUE) {
                if (!z2) {
                    sb.append((String) this._separToks.elementAt(i3));
                    i3++;
                }
                int i5 = i2 + 1;
                formatValue(i4, (String) this._formatToks.elementAt(i2), sb);
                if (i5 == this._nFormats) {
                    i5--;
                }
                if (i3 >= this._nSepars) {
                    i3--;
                }
                z2 = false;
                i2 = i5;
            }
        }
        if (this._separLast) {
            sb.append((String) this._separToks.lastElement());
        }
        return sb.toString();
    }

    private void formatValue(int i, String str, StringBuilder sb) {
        char charAt = str.charAt(0);
        if (Character.isDigit(charAt)) {
            char numericValue = (char) (charAt - Character.getNumericValue(charAt));
            StringBuilder sb2 = this._groupSize > 0 ? new StringBuilder() : sb;
            String str2 = "";
            while (i > 0) {
                str2 = ((char) ((i % 10) + numericValue)) + str2;
                i /= 10;
            }
            for (int i2 = 0; i2 < str.length() - str2.length(); i2++) {
                sb2.append(numericValue);
            }
            sb2.append(str2);
            if (this._groupSize > 0) {
                for (int i3 = 0; i3 < sb2.length(); i3++) {
                    if (i3 != 0 && (sb2.length() - i3) % this._groupSize == 0) {
                        sb.append(this._groupSep);
                    }
                    sb.append(sb2.charAt(i3));
                }
            }
        } else if (charAt == 'i' && !this._letterValue.equals(Constants.ATTRVAL_ALPHABETIC)) {
            sb.append(romanValue(i));
        } else if (charAt != 'I' || this._letterValue.equals(Constants.ATTRVAL_ALPHABETIC)) {
            int i4 = 969;
            if (charAt < 945 || charAt > 969) {
                i4 = charAt;
                while (true) {
                    int i5 = i4 + 1;
                    if (!Character.isLetterOrDigit((char) i5)) {
                        break;
                    }
                    i4 = i5;
                }
            }
            sb.append(alphaValue(i, charAt, i4));
        } else {
            sb.append(romanValue(i).toUpperCase());
        }
    }

    private String alphaValue(int i, int i2, int i3) {
        if (i <= 0) {
            return "" + i;
        }
        int i4 = (i3 - i2) + 1;
        int i5 = i - 1;
        char c = (char) ((i5 % i4) + i2);
        if (i > i4) {
            return alphaValue(i5 / i4, i2, i3) + c;
        }
        return "" + c;
    }

    private String romanValue(int i) {
        if (i <= 0 || i > 4000) {
            return "" + i;
        }
        return Thousands[i / 1000] + Hundreds[(i / 100) % 10] + Tens[(i / 10) % 10] + Ones[i % 10];
    }
}
