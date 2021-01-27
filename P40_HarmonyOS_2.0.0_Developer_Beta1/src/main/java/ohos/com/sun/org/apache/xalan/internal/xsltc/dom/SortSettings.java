package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.text.Collator;
import java.util.Locale;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;

final class SortSettings {
    private String[] _caseOrders;
    private Collator[] _collators;
    private Locale[] _locales;
    private int[] _sortOrders;
    private AbstractTranslet _translet;
    private int[] _types;

    SortSettings(AbstractTranslet abstractTranslet, int[] iArr, int[] iArr2, Locale[] localeArr, Collator[] collatorArr, String[] strArr) {
        this._translet = abstractTranslet;
        this._sortOrders = iArr;
        this._types = iArr2;
        this._locales = localeArr;
        this._collators = collatorArr;
        this._caseOrders = strArr;
    }

    /* access modifiers changed from: package-private */
    public AbstractTranslet getTranslet() {
        return this._translet;
    }

    /* access modifiers changed from: package-private */
    public int[] getSortOrders() {
        return this._sortOrders;
    }

    /* access modifiers changed from: package-private */
    public int[] getTypes() {
        return this._types;
    }

    /* access modifiers changed from: package-private */
    public Locale[] getLocales() {
        return this._locales;
    }

    /* access modifiers changed from: package-private */
    public Collator[] getCollators() {
        return this._collators;
    }

    /* access modifiers changed from: package-private */
    public String[] getCaseOrders() {
        return this._caseOrders;
    }
}
