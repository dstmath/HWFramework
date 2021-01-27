package ohos.com.sun.org.apache.xpath.internal.compiler;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.ObjectVector;
import ohos.javax.xml.transform.TransformerException;

public class OpMap {
    static final int BLOCKTOKENQUEUESIZE = 500;
    public static final int MAPINDEX_LENGTH = 1;
    static final int MAXTOKENQUEUESIZE = 500;
    protected String m_currentPattern;
    OpMapVector m_opMap = null;
    ObjectVector m_tokenQueue = new ObjectVector(500, 500);

    public static int getFirstChildPos(int i) {
        return i + 2;
    }

    public static int getFirstChildPosOfStep(int i) {
        return i + 3;
    }

    public String toString() {
        return this.m_currentPattern;
    }

    public String getPatternString() {
        return this.m_currentPattern;
    }

    public ObjectVector getTokenQueue() {
        return this.m_tokenQueue;
    }

    public Object getToken(int i) {
        return this.m_tokenQueue.elementAt(i);
    }

    public int getTokenQueueSize() {
        return this.m_tokenQueue.size();
    }

    public OpMapVector getOpMap() {
        return this.m_opMap;
    }

    /* access modifiers changed from: package-private */
    public void shrink() {
        int elementAt = this.m_opMap.elementAt(1);
        this.m_opMap.setToSize(elementAt + 4);
        this.m_opMap.setElementAt(0, elementAt);
        this.m_opMap.setElementAt(0, elementAt + 1);
        this.m_opMap.setElementAt(0, elementAt + 2);
        int size = this.m_tokenQueue.size();
        this.m_tokenQueue.setToSize(size + 4);
        this.m_tokenQueue.setElementAt(null, size);
        this.m_tokenQueue.setElementAt(null, size + 1);
        this.m_tokenQueue.setElementAt(null, size + 2);
    }

    public int getOp(int i) {
        return this.m_opMap.elementAt(i);
    }

    public void setOp(int i, int i2) {
        this.m_opMap.setElementAt(i2, i);
    }

    public int getNextOpPos(int i) {
        return i + this.m_opMap.elementAt(i + 1);
    }

    public int getNextStepPos(int i) {
        int op = getOp(i);
        if (op >= 37 && op <= 53) {
            return getNextOpPos(i);
        }
        if (op < 22 || op > 25) {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_UNKNOWN_STEP", new Object[]{String.valueOf(op)}));
        }
        int nextOpPos = getNextOpPos(i);
        while (29 == getOp(nextOpPos)) {
            nextOpPos = getNextOpPos(nextOpPos);
        }
        int op2 = getOp(nextOpPos);
        if (op2 < 37 || op2 > 53) {
            return -1;
        }
        return nextOpPos;
    }

    public static int getNextOpPos(int[] iArr, int i) {
        return i + iArr[i + 1];
    }

    public int getFirstPredicateOpPos(int i) throws TransformerException {
        int elementAt;
        int elementAt2 = this.m_opMap.elementAt(i);
        if (elementAt2 >= 37 && elementAt2 <= 53) {
            elementAt = this.m_opMap.elementAt(i + 2);
        } else if (elementAt2 >= 22 && elementAt2 <= 25) {
            elementAt = this.m_opMap.elementAt(i + 1);
        } else if (-2 == elementAt2) {
            return -2;
        } else {
            error("ER_UNKNOWN_OPCODE", new Object[]{String.valueOf(elementAt2)});
            return -1;
        }
        return i + elementAt;
    }

    public void error(String str, Object[] objArr) throws TransformerException {
        throw new TransformerException(XSLMessages.createXPATHMessage(str, objArr));
    }

    public int getArgLength(int i) {
        return this.m_opMap.elementAt(i + 1);
    }

    public int getArgLengthOfStep(int i) {
        return this.m_opMap.elementAt((i + 1) + 1) - 3;
    }

    public int getStepTestType(int i) {
        return this.m_opMap.elementAt(i + 3);
    }

    public String getStepNS(int i) {
        if (getArgLengthOfStep(i) == 3) {
            int elementAt = this.m_opMap.elementAt(i + 4);
            if (elementAt >= 0) {
                return (String) this.m_tokenQueue.elementAt(elementAt);
            }
            if (-3 == elementAt) {
                return "*";
            }
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0033  */
    public String getStepLocalName(int i) {
        int i2;
        int argLengthOfStep = getArgLengthOfStep(i);
        if (argLengthOfStep != 0) {
            if (argLengthOfStep == 1) {
                i2 = -3;
            } else if (argLengthOfStep == 2) {
                i2 = this.m_opMap.elementAt(i + 4);
            } else if (argLengthOfStep == 3) {
                i2 = this.m_opMap.elementAt(i + 5);
            }
            if (i2 < 0) {
                return this.m_tokenQueue.elementAt(i2).toString();
            }
            if (-3 == i2) {
                return "*";
            }
            return null;
        }
        i2 = -2;
        if (i2 < 0) {
        }
    }
}
