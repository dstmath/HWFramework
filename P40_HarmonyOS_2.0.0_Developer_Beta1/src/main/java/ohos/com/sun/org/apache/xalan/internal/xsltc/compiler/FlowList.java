package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Iterator;
import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;

public final class FlowList {
    private Vector _elements;

    public FlowList() {
        this._elements = null;
    }

    public FlowList(InstructionHandle instructionHandle) {
        this._elements = new Vector();
        this._elements.addElement(instructionHandle);
    }

    public FlowList(FlowList flowList) {
        this._elements = flowList._elements;
    }

    public FlowList add(InstructionHandle instructionHandle) {
        if (this._elements == null) {
            this._elements = new Vector();
        }
        this._elements.addElement(instructionHandle);
        return this;
    }

    public FlowList append(FlowList flowList) {
        if (this._elements == null) {
            this._elements = flowList._elements;
        } else {
            Vector vector = flowList._elements;
            if (vector != null) {
                int size = vector.size();
                for (int i = 0; i < size; i++) {
                    this._elements.addElement(vector.elementAt(i));
                }
            }
        }
        return this;
    }

    public void backPatch(InstructionHandle instructionHandle) {
        Vector vector = this._elements;
        if (vector != null) {
            int size = vector.size();
            for (int i = 0; i < size; i++) {
                ((BranchHandle) this._elements.elementAt(i)).setTarget(instructionHandle);
            }
            this._elements.clear();
        }
    }

    public FlowList copyAndRedirect(InstructionList instructionList, InstructionList instructionList2) {
        FlowList flowList = new FlowList();
        Vector vector = this._elements;
        if (vector == null) {
            return flowList;
        }
        int size = vector.size();
        Iterator it = instructionList.iterator();
        Iterator it2 = instructionList2.iterator();
        while (it.hasNext()) {
            InstructionHandle instructionHandle = (InstructionHandle) it.next();
            InstructionHandle instructionHandle2 = (InstructionHandle) it2.next();
            for (int i = 0; i < size; i++) {
                if (this._elements.elementAt(i) == instructionHandle) {
                    flowList.add(instructionHandle2);
                }
            }
        }
        return flowList;
    }
}
