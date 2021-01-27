package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.Translet;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xpath.internal.XPath;

public abstract class AnyNodeCounter extends NodeCounter {
    public AnyNodeCounter(Translet translet, DOM dom, DTMAxisIterator dTMAxisIterator) {
        super(translet, dom, dTMAxisIterator);
    }

    public AnyNodeCounter(Translet translet, DOM dom, DTMAxisIterator dTMAxisIterator, boolean z) {
        super(translet, dom, dTMAxisIterator, z);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter
    public NodeCounter setStartNode(int i) {
        this._node = i;
        this._nodeType = this._document.getExpandedTypeID(i);
        return this;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter
    public String getCounter() {
        if (this._value == -2.147483648E9d) {
            int i = this._node;
            int document = this._document.getDocument();
            int i2 = 0;
            while (i >= document && !matchesFrom(i)) {
                if (matchesCount(i)) {
                    i2++;
                }
                i--;
            }
            return formatNumbers(i2);
        } else if (this._value == XPath.MATCH_SCORE_QNAME) {
            return "0";
        } else {
            if (Double.isNaN(this._value)) {
                return "NaN";
            }
            if (this._value < XPath.MATCH_SCORE_QNAME && Double.isInfinite(this._value)) {
                return "-Infinity";
            }
            if (Double.isInfinite(this._value)) {
                return Constants.ATTRVAL_INFINITY;
            }
            return formatNumbers((int) this._value);
        }
    }

    public static NodeCounter getDefaultNodeCounter(Translet translet, DOM dom, DTMAxisIterator dTMAxisIterator) {
        return new DefaultAnyNodeCounter(translet, dom, dTMAxisIterator);
    }

    static class DefaultAnyNodeCounter extends AnyNodeCounter {
        public DefaultAnyNodeCounter(Translet translet, DOM dom, DTMAxisIterator dTMAxisIterator) {
            super(translet, dom, dTMAxisIterator);
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.AnyNodeCounter, ohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter
        public String getCounter() {
            int i;
            if (this._value == -2.147483648E9d) {
                i = 0;
                int expandedTypeID = this._document.getExpandedTypeID(this._node);
                int document = this._document.getDocument();
                for (int i2 = this._node; i2 >= 0; i2--) {
                    if (expandedTypeID == this._document.getExpandedTypeID(i2)) {
                        i++;
                    }
                    if (i2 == document) {
                        break;
                    }
                }
            } else if (this._value == XPath.MATCH_SCORE_QNAME) {
                return "0";
            } else {
                if (Double.isNaN(this._value)) {
                    return "NaN";
                }
                if (this._value < XPath.MATCH_SCORE_QNAME && Double.isInfinite(this._value)) {
                    return "-Infinity";
                }
                if (Double.isInfinite(this._value)) {
                    return Constants.ATTRVAL_INFINITY;
                }
                i = (int) this._value;
            }
            return formatNumbers(i);
        }
    }
}
