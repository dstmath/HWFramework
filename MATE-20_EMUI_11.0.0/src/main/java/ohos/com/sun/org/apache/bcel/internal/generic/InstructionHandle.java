package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import ohos.com.sun.org.apache.bcel.internal.classfile.Utility;
import ohos.global.icu.text.PluralRules;

public class InstructionHandle implements Serializable {
    private static InstructionHandle ih_list;
    private HashMap attributes;
    protected int i_position = -1;
    Instruction instruction;
    InstructionHandle next;
    InstructionHandle prev;
    private HashSet targeters;

    public final InstructionHandle getNext() {
        return this.next;
    }

    public final InstructionHandle getPrev() {
        return this.prev;
    }

    public final Instruction getInstruction() {
        return this.instruction;
    }

    public void setInstruction(Instruction instruction2) {
        if (instruction2 == null) {
            throw new ClassGenException("Assigning null to handle");
        } else if (getClass() == BranchHandle.class || !(instruction2 instanceof BranchInstruction)) {
            Instruction instruction3 = this.instruction;
            if (instruction3 != null) {
                instruction3.dispose();
            }
            this.instruction = instruction2;
        } else {
            throw new ClassGenException("Assigning branch instruction " + instruction2 + " to plain handle");
        }
    }

    public Instruction swapInstruction(Instruction instruction2) {
        Instruction instruction3 = this.instruction;
        this.instruction = instruction2;
        return instruction3;
    }

    protected InstructionHandle(Instruction instruction2) {
        setInstruction(instruction2);
    }

    static final InstructionHandle getInstructionHandle(Instruction instruction2) {
        InstructionHandle instructionHandle = ih_list;
        if (instructionHandle == null) {
            return new InstructionHandle(instruction2);
        }
        ih_list = instructionHandle.next;
        instructionHandle.setInstruction(instruction2);
        return instructionHandle;
    }

    /* access modifiers changed from: protected */
    public int updatePosition(int i, int i2) {
        this.i_position += i;
        return 0;
    }

    public int getPosition() {
        return this.i_position;
    }

    /* access modifiers changed from: package-private */
    public void setPosition(int i) {
        this.i_position = i;
    }

    /* access modifiers changed from: protected */
    public void addHandle() {
        this.next = ih_list;
        ih_list = this;
    }

    /* access modifiers changed from: package-private */
    public void dispose() {
        this.prev = null;
        this.next = null;
        this.instruction.dispose();
        this.instruction = null;
        this.i_position = -1;
        this.attributes = null;
        removeAllTargeters();
        addHandle();
    }

    public void removeAllTargeters() {
        HashSet hashSet = this.targeters;
        if (hashSet != null) {
            hashSet.clear();
        }
    }

    public void removeTargeter(InstructionTargeter instructionTargeter) {
        this.targeters.remove(instructionTargeter);
    }

    public void addTargeter(InstructionTargeter instructionTargeter) {
        if (this.targeters == null) {
            this.targeters = new HashSet();
        }
        this.targeters.add(instructionTargeter);
    }

    public boolean hasTargeters() {
        HashSet hashSet = this.targeters;
        return hashSet != null && hashSet.size() > 0;
    }

    public InstructionTargeter[] getTargeters() {
        if (!hasTargeters()) {
            return null;
        }
        InstructionTargeter[] instructionTargeterArr = new InstructionTargeter[this.targeters.size()];
        this.targeters.toArray(instructionTargeterArr);
        return instructionTargeterArr;
    }

    public String toString(boolean z) {
        return Utility.format(this.i_position, 4, false, ' ') + PluralRules.KEYWORD_RULE_SEPARATOR + this.instruction.toString(z);
    }

    @Override // java.lang.Object
    public String toString() {
        return toString(true);
    }

    public void addAttribute(Object obj, Object obj2) {
        if (this.attributes == null) {
            this.attributes = new HashMap(3);
        }
        this.attributes.put(obj, obj2);
    }

    public void removeAttribute(Object obj) {
        HashMap hashMap = this.attributes;
        if (hashMap != null) {
            hashMap.remove(obj);
        }
    }

    public Object getAttribute(Object obj) {
        HashMap hashMap = this.attributes;
        if (hashMap != null) {
            return hashMap.get(obj);
        }
        return null;
    }

    public Collection getAttributes() {
        return this.attributes.values();
    }

    public void accept(Visitor visitor) {
        this.instruction.accept(visitor);
    }
}
