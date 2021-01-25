package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.text.Collator;
import java.util.Locale;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.Translet;
import ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;

public class NodeSortRecordFactory {
    private static int DESCENDING = 10;
    private static int NUMBER = 6;
    private Class _class;
    private final String _className;
    protected Collator _collator;
    private final DOM _dom;
    private SortSettings _sortSettings;

    private final void setLang(String[] strArr) {
    }

    public NodeSortRecordFactory(DOM dom, String str, Translet translet, String[] strArr, String[] strArr2) throws TransletException {
        this(dom, str, translet, strArr, strArr2, null, null);
    }

    /* JADX WARN: Type inference failed for: r9v1, types: [java.lang.Throwable, ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public NodeSortRecordFactory(DOM dom, String str, Translet translet, String[] strArr, String[] strArr2, String[] strArr3, String[] strArr4) throws TransletException {
        try {
            this._dom = dom;
            this._className = str;
            this._class = translet.getAuxiliaryClass(str);
            if (this._class == null) {
                this._class = ObjectFactory.findProviderClass(str, true);
            }
            int length = strArr.length;
            int[] iArr = new int[length];
            int[] iArr2 = new int[length];
            for (int i = 0; i < length; i++) {
                if (strArr[i].length() == DESCENDING) {
                    iArr[i] = 1;
                }
                if (strArr2[i].length() == NUMBER) {
                    iArr2[i] = 1;
                }
            }
            String[] strArr5 = null;
            if (strArr3 == null || strArr4 == null) {
                int length2 = strArr.length;
                String[] strArr6 = new String[length2];
                for (int i2 = 0; i2 < length2; i2++) {
                    strArr6[i2] = "";
                }
                strArr5 = strArr6;
            }
            strArr3 = strArr3 == null ? strArr5 : strArr3;
            String[] strArr7 = strArr4 == null ? strArr5 : strArr4;
            int length3 = strArr3.length;
            Locale[] localeArr = new Locale[length3];
            Collator[] collatorArr = new Collator[length3];
            for (int i3 = 0; i3 < length3; i3++) {
                localeArr[i3] = LocaleUtility.langToLocale(strArr3[i3]);
                collatorArr[i3] = Collator.getInstance(localeArr[i3]);
            }
            this._sortSettings = new SortSettings((AbstractTranslet) translet, iArr, iArr2, localeArr, collatorArr, strArr7);
        } catch (ClassNotFoundException e) {
            throw new TransletException(e);
        }
    }

    public NodeSortRecord makeNodeSortRecord(int i, int i2) throws ExceptionInInitializerError, LinkageError, IllegalAccessException, InstantiationException, SecurityException, TransletException {
        NodeSortRecord nodeSortRecord = (NodeSortRecord) this._class.newInstance();
        nodeSortRecord.initialize(i, i2, this._dom, this._sortSettings);
        return nodeSortRecord;
    }

    public String getClassName() {
        return this._className;
    }
}
