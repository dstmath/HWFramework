package org.apache.xpath.compiler;

import javax.xml.transform.TransformerException;
import org.apache.xml.utils.ObjectVector;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;

public class OpMap {
    static final int BLOCKTOKENQUEUESIZE = 500;
    public static final int MAPINDEX_LENGTH = 1;
    static final int MAXTOKENQUEUESIZE = 500;
    protected String m_currentPattern;
    OpMapVector m_opMap;
    ObjectVector m_tokenQueue;

    public OpMap() {
        this.m_tokenQueue = new ObjectVector(MAXTOKENQUEUESIZE, MAXTOKENQUEUESIZE);
        this.m_opMap = null;
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

    public Object getToken(int pos) {
        return this.m_tokenQueue.elementAt(pos);
    }

    public int getTokenQueueSize() {
        return this.m_tokenQueue.size();
    }

    public OpMapVector getOpMap() {
        return this.m_opMap;
    }

    void shrink() {
        int n = this.m_opMap.elementAt(MAPINDEX_LENGTH);
        this.m_opMap.setToSize(n + 4);
        this.m_opMap.setElementAt(0, n);
        this.m_opMap.setElementAt(0, n + MAPINDEX_LENGTH);
        this.m_opMap.setElementAt(0, n + 2);
        n = this.m_tokenQueue.size();
        this.m_tokenQueue.setToSize(n + 4);
        this.m_tokenQueue.setElementAt(null, n);
        this.m_tokenQueue.setElementAt(null, n + MAPINDEX_LENGTH);
        this.m_tokenQueue.setElementAt(null, n + 2);
    }

    public int getOp(int opPos) {
        return this.m_opMap.elementAt(opPos);
    }

    public void setOp(int opPos, int value) {
        this.m_opMap.setElementAt(value, opPos);
    }

    public int getNextOpPos(int opPos) {
        return this.m_opMap.elementAt(opPos + MAPINDEX_LENGTH) + opPos;
    }

    public int getNextStepPos(int opPos) {
        int stepType = getOp(opPos);
        if (stepType >= 37 && stepType <= 53) {
            return getNextOpPos(opPos);
        }
        if (stepType < 22 || stepType > 25) {
            String str = XPATHErrorResources.ER_UNKNOWN_STEP;
            Object[] objArr = new Object[MAPINDEX_LENGTH];
            objArr[0] = String.valueOf(stepType);
            throw new RuntimeException(XPATHMessages.createXPATHMessage(str, objArr));
        }
        int newOpPos = getNextOpPos(opPos);
        while (29 == getOp(newOpPos)) {
            newOpPos = getNextOpPos(newOpPos);
        }
        stepType = getOp(newOpPos);
        if (stepType < 37 || stepType > 53) {
            return -1;
        }
        return newOpPos;
    }

    public static int getNextOpPos(int[] opMap, int opPos) {
        return opMap[opPos + MAPINDEX_LENGTH] + opPos;
    }

    public int getFirstPredicateOpPos(int opPos) throws TransformerException {
        int stepType = this.m_opMap.elementAt(opPos);
        if (stepType >= 37 && stepType <= 53) {
            return this.m_opMap.elementAt(opPos + 2) + opPos;
        }
        if (stepType >= 22 && stepType <= 25) {
            return this.m_opMap.elementAt(opPos + MAPINDEX_LENGTH) + opPos;
        }
        if (-2 == stepType) {
            return -2;
        }
        String str = XPATHErrorResources.ER_UNKNOWN_OPCODE;
        Object[] objArr = new Object[MAPINDEX_LENGTH];
        objArr[0] = String.valueOf(stepType);
        error(str, objArr);
        return -1;
    }

    public void error(String msg, Object[] args) throws TransformerException {
        throw new TransformerException(XPATHMessages.createXPATHMessage(msg, args));
    }

    public static int getFirstChildPos(int opPos) {
        return opPos + 2;
    }

    public int getArgLength(int opPos) {
        return this.m_opMap.elementAt(opPos + MAPINDEX_LENGTH);
    }

    public int getArgLengthOfStep(int opPos) {
        return this.m_opMap.elementAt((opPos + MAPINDEX_LENGTH) + MAPINDEX_LENGTH) - 3;
    }

    public static int getFirstChildPosOfStep(int opPos) {
        return opPos + 3;
    }

    public int getStepTestType(int opPosOfStep) {
        return this.m_opMap.elementAt(opPosOfStep + 3);
    }

    public String getStepNS(int opPosOfStep) {
        if (getArgLengthOfStep(opPosOfStep) != 3) {
            return null;
        }
        int index = this.m_opMap.elementAt(opPosOfStep + 4);
        if (index >= 0) {
            return (String) this.m_tokenQueue.elementAt(index);
        }
        if (-3 == index) {
            return PsuedoNames.PSEUDONAME_OTHER;
        }
        return null;
    }

    public String getStepLocalName(int opPosOfStep) {
        int index;
        switch (getArgLengthOfStep(opPosOfStep)) {
            case FunctionTable.FUNC_CURRENT /*0*/:
                index = -2;
                break;
            case MAPINDEX_LENGTH /*1*/:
                index = -3;
                break;
            case OpCodes.OP_OR /*2*/:
                index = this.m_opMap.elementAt(opPosOfStep + 4);
                break;
            case OpCodes.OP_AND /*3*/:
                index = this.m_opMap.elementAt(opPosOfStep + 5);
                break;
            default:
                index = -2;
                break;
        }
        if (index >= 0) {
            return this.m_tokenQueue.elementAt(index).toString();
        }
        if (-3 == index) {
            return PsuedoNames.PSEUDONAME_OTHER;
        }
        return null;
    }
}
