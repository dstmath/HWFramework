package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.Translet;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xpath.internal.XPath;

public abstract class SingleNodeCounter extends NodeCounter {
    private static final int[] EmptyArray = new int[0];
    DTMAxisIterator _countSiblings = null;

    public SingleNodeCounter(Translet translet, DOM dom, DTMAxisIterator dTMAxisIterator) {
        super(translet, dom, dTMAxisIterator);
    }

    public SingleNodeCounter(Translet translet, DOM dom, DTMAxisIterator dTMAxisIterator, boolean z) {
        super(translet, dom, dTMAxisIterator, z);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter
    public NodeCounter setStartNode(int i) {
        this._node = i;
        this._nodeType = this._document.getExpandedTypeID(i);
        this._countSiblings = this._document.getAxisIterator(12);
        return this;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter
    public String getCounter() {
        int i;
        if (this._value == -2.147483648E9d) {
            int i2 = this._node;
            int i3 = 0;
            boolean matchesCount = matchesCount(i2);
            if (!matchesCount) {
                while (true) {
                    i2 = this._document.getParent(i2);
                    if (i2 > -1 && !matchesCount(i2)) {
                        if (matchesFrom(i2)) {
                            i2 = -1;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            if (i2 != -1) {
                if (!matchesCount && this._hasFrom) {
                    i = i2;
                    do {
                        i = this._document.getParent(i);
                        if (i <= -1) {
                            break;
                        }
                    } while (!matchesFrom(i));
                } else {
                    i = i2;
                }
                if (i != -1) {
                    this._countSiblings.setStartNode(i2);
                    do {
                        if (matchesCount(i2)) {
                            i3++;
                        }
                        i2 = this._countSiblings.next();
                    } while (i2 != -1);
                    return formatNumbers(i3);
                }
            }
            return formatNumbers(EmptyArray);
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
        return new DefaultSingleNodeCounter(translet, dom, dTMAxisIterator);
    }

    static class DefaultSingleNodeCounter extends SingleNodeCounter {
        public DefaultSingleNodeCounter(Translet translet, DOM dom, DTMAxisIterator dTMAxisIterator) {
            super(translet, dom, dTMAxisIterator);
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SingleNodeCounter, ohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter
        public NodeCounter setStartNode(int i) {
            this._node = i;
            this._nodeType = this._document.getExpandedTypeID(i);
            this._countSiblings = this._document.getTypedAxisIterator(12, this._document.getExpandedTypeID(i));
            return this;
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SingleNodeCounter, ohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter
        public String getCounter() {
            int i;
            if (this._value == -2.147483648E9d) {
                this._countSiblings.setStartNode(this._node);
                i = 1;
                while (this._countSiblings.next() != -1) {
                    i++;
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
