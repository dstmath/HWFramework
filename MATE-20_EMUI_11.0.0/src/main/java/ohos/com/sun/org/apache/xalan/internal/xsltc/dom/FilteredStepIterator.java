package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;

public final class FilteredStepIterator extends StepIterator {
    private Filter _filter;

    public FilteredStepIterator(DTMAxisIterator dTMAxisIterator, DTMAxisIterator dTMAxisIterator2, Filter filter) {
        super(dTMAxisIterator, dTMAxisIterator2);
        this._filter = filter;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.StepIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int next() {
        int next;
        do {
            next = super.next();
            if (next == -1) {
                return next;
            }
        } while (!this._filter.test(next));
        return returnNode(next);
    }
}
