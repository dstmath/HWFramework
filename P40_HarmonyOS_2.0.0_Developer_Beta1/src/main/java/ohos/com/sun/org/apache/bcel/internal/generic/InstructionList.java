package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public class InstructionList implements Serializable {
    private int[] byte_positions;
    private InstructionHandle end = null;
    private int length = 0;
    private ArrayList observers;
    private InstructionHandle start = null;

    public InstructionList() {
    }

    public InstructionList(Instruction instruction) {
        append(instruction);
    }

    public InstructionList(BranchInstruction branchInstruction) {
        append(branchInstruction);
    }

    public InstructionList(CompoundInstruction compoundInstruction) {
        append(compoundInstruction.getInstructionList());
    }

    public boolean isEmpty() {
        return this.start == null;
    }

    public static InstructionHandle findHandle(InstructionHandle[] instructionHandleArr, int[] iArr, int i, int i2) {
        int i3 = i - 1;
        int i4 = 0;
        do {
            int i5 = (i4 + i3) / 2;
            int i6 = iArr[i5];
            if (i6 == i2) {
                return instructionHandleArr[i5];
            }
            if (i2 < i6) {
                i3 = i5 - 1;
                continue;
            } else {
                i4 = i5 + 1;
                continue;
            }
        } while (i4 <= i3);
        return null;
    }

    public InstructionHandle findHandle(int i) {
        return findHandle(getInstructionHandles(), this.byte_positions, this.length, i);
    }

    public InstructionList(byte[] bArr) {
        InstructionHandle instructionHandle;
        ByteSequence byteSequence = new ByteSequence(bArr);
        InstructionHandle[] instructionHandleArr = new InstructionHandle[bArr.length];
        int[] iArr = new int[bArr.length];
        int i = 0;
        while (byteSequence.available() > 0) {
            try {
                int index = byteSequence.getIndex();
                iArr[i] = index;
                Instruction readInstruction = Instruction.readInstruction(byteSequence);
                if (readInstruction instanceof BranchInstruction) {
                    instructionHandle = append((BranchInstruction) readInstruction);
                } else {
                    instructionHandle = append(readInstruction);
                }
                instructionHandle.setPosition(index);
                instructionHandleArr[i] = instructionHandle;
                i++;
            } catch (IOException e) {
                throw new ClassGenException(e.toString());
            }
        }
        this.byte_positions = new int[i];
        System.arraycopy(iArr, 0, this.byte_positions, 0, i);
        for (int i2 = 0; i2 < i; i2++) {
            if (instructionHandleArr[i2] instanceof BranchHandle) {
                BranchInstruction branchInstruction = (BranchInstruction) instructionHandleArr[i2].instruction;
                InstructionHandle findHandle = findHandle(instructionHandleArr, iArr, i, branchInstruction.position + branchInstruction.getIndex());
                if (findHandle != null) {
                    branchInstruction.setTarget(findHandle);
                    if (branchInstruction instanceof Select) {
                        Select select = (Select) branchInstruction;
                        int[] indices = select.getIndices();
                        for (int i3 = 0; i3 < indices.length; i3++) {
                            InstructionHandle findHandle2 = findHandle(instructionHandleArr, iArr, i, branchInstruction.position + indices[i3]);
                            if (findHandle2 != null) {
                                select.setTarget(i3, findHandle2);
                            } else {
                                throw new ClassGenException("Couldn't find target for switch: " + branchInstruction);
                            }
                        }
                        continue;
                    } else {
                        continue;
                    }
                } else {
                    throw new ClassGenException("Couldn't find target for branch: " + branchInstruction);
                }
            }
        }
    }

    public InstructionHandle append(InstructionHandle instructionHandle, InstructionList instructionList) {
        if (instructionList == null) {
            throw new ClassGenException("Appending null InstructionList");
        } else if (instructionList.isEmpty()) {
            return instructionHandle;
        } else {
            InstructionHandle instructionHandle2 = instructionHandle.next;
            InstructionHandle instructionHandle3 = instructionList.start;
            instructionHandle.next = instructionHandle3;
            instructionHandle3.prev = instructionHandle;
            InstructionHandle instructionHandle4 = instructionList.end;
            instructionHandle4.next = instructionHandle2;
            if (instructionHandle2 != null) {
                instructionHandle2.prev = instructionHandle4;
            } else {
                this.end = instructionHandle4;
            }
            this.length += instructionList.length;
            instructionList.clear();
            return instructionHandle3;
        }
    }

    public InstructionHandle append(Instruction instruction, InstructionList instructionList) {
        InstructionHandle findInstruction2 = findInstruction2(instruction);
        if (findInstruction2 != null) {
            return append(findInstruction2, instructionList);
        }
        throw new ClassGenException("Instruction " + instruction + " is not contained in this list.");
    }

    public InstructionHandle append(InstructionList instructionList) {
        if (instructionList == null) {
            throw new ClassGenException("Appending null InstructionList");
        } else if (instructionList.isEmpty()) {
            return null;
        } else {
            if (!isEmpty()) {
                return append(this.end, instructionList);
            }
            this.start = instructionList.start;
            this.end = instructionList.end;
            this.length = instructionList.length;
            instructionList.clear();
            return this.start;
        }
    }

    private void append(InstructionHandle instructionHandle) {
        if (isEmpty()) {
            this.end = instructionHandle;
            this.start = instructionHandle;
            instructionHandle.prev = null;
            instructionHandle.next = null;
        } else {
            InstructionHandle instructionHandle2 = this.end;
            instructionHandle2.next = instructionHandle;
            instructionHandle.prev = instructionHandle2;
            instructionHandle.next = null;
            this.end = instructionHandle;
        }
        this.length++;
    }

    public InstructionHandle append(Instruction instruction) {
        InstructionHandle instructionHandle = InstructionHandle.getInstructionHandle(instruction);
        append(instructionHandle);
        return instructionHandle;
    }

    public BranchHandle append(BranchInstruction branchInstruction) {
        BranchHandle branchHandle = BranchHandle.getBranchHandle(branchInstruction);
        append(branchHandle);
        return branchHandle;
    }

    public InstructionHandle append(Instruction instruction, Instruction instruction2) {
        return append(instruction, new InstructionList(instruction2));
    }

    public InstructionHandle append(Instruction instruction, CompoundInstruction compoundInstruction) {
        return append(instruction, compoundInstruction.getInstructionList());
    }

    public InstructionHandle append(CompoundInstruction compoundInstruction) {
        return append(compoundInstruction.getInstructionList());
    }

    public InstructionHandle append(InstructionHandle instructionHandle, CompoundInstruction compoundInstruction) {
        return append(instructionHandle, compoundInstruction.getInstructionList());
    }

    public InstructionHandle append(InstructionHandle instructionHandle, Instruction instruction) {
        return append(instructionHandle, new InstructionList(instruction));
    }

    public BranchHandle append(InstructionHandle instructionHandle, BranchInstruction branchInstruction) {
        BranchHandle branchHandle = BranchHandle.getBranchHandle(branchInstruction);
        InstructionList instructionList = new InstructionList();
        instructionList.append(branchHandle);
        append(instructionHandle, instructionList);
        return branchHandle;
    }

    public InstructionHandle insert(InstructionHandle instructionHandle, InstructionList instructionList) {
        if (instructionList == null) {
            throw new ClassGenException("Inserting null InstructionList");
        } else if (instructionList.isEmpty()) {
            return instructionHandle;
        } else {
            InstructionHandle instructionHandle2 = instructionHandle.prev;
            InstructionHandle instructionHandle3 = instructionList.start;
            InstructionHandle instructionHandle4 = instructionList.end;
            instructionHandle.prev = instructionHandle4;
            instructionHandle4.next = instructionHandle;
            instructionHandle3.prev = instructionHandle2;
            if (instructionHandle2 != null) {
                instructionHandle2.next = instructionHandle3;
            } else {
                this.start = instructionHandle3;
            }
            this.length += instructionList.length;
            instructionList.clear();
            return instructionHandle3;
        }
    }

    public InstructionHandle insert(InstructionList instructionList) {
        if (!isEmpty()) {
            return insert(this.start, instructionList);
        }
        append(instructionList);
        return this.start;
    }

    private void insert(InstructionHandle instructionHandle) {
        if (isEmpty()) {
            this.end = instructionHandle;
            this.start = instructionHandle;
            instructionHandle.prev = null;
            instructionHandle.next = null;
        } else {
            InstructionHandle instructionHandle2 = this.start;
            instructionHandle2.prev = instructionHandle;
            instructionHandle.next = instructionHandle2;
            instructionHandle.prev = null;
            this.start = instructionHandle;
        }
        this.length++;
    }

    public InstructionHandle insert(Instruction instruction, InstructionList instructionList) {
        InstructionHandle findInstruction1 = findInstruction1(instruction);
        if (findInstruction1 != null) {
            return insert(findInstruction1, instructionList);
        }
        throw new ClassGenException("Instruction " + instruction + " is not contained in this list.");
    }

    public InstructionHandle insert(Instruction instruction) {
        InstructionHandle instructionHandle = InstructionHandle.getInstructionHandle(instruction);
        insert(instructionHandle);
        return instructionHandle;
    }

    public BranchHandle insert(BranchInstruction branchInstruction) {
        BranchHandle branchHandle = BranchHandle.getBranchHandle(branchInstruction);
        insert(branchHandle);
        return branchHandle;
    }

    public InstructionHandle insert(Instruction instruction, Instruction instruction2) {
        return insert(instruction, new InstructionList(instruction2));
    }

    public InstructionHandle insert(Instruction instruction, CompoundInstruction compoundInstruction) {
        return insert(instruction, compoundInstruction.getInstructionList());
    }

    public InstructionHandle insert(CompoundInstruction compoundInstruction) {
        return insert(compoundInstruction.getInstructionList());
    }

    public InstructionHandle insert(InstructionHandle instructionHandle, Instruction instruction) {
        return insert(instructionHandle, new InstructionList(instruction));
    }

    public InstructionHandle insert(InstructionHandle instructionHandle, CompoundInstruction compoundInstruction) {
        return insert(instructionHandle, compoundInstruction.getInstructionList());
    }

    public BranchHandle insert(InstructionHandle instructionHandle, BranchInstruction branchInstruction) {
        BranchHandle branchHandle = BranchHandle.getBranchHandle(branchInstruction);
        InstructionList instructionList = new InstructionList();
        instructionList.append(branchHandle);
        insert(instructionHandle, instructionList);
        return branchHandle;
    }

    public void move(InstructionHandle instructionHandle, InstructionHandle instructionHandle2, InstructionHandle instructionHandle3) {
        if (instructionHandle == null || instructionHandle2 == null) {
            throw new ClassGenException("Invalid null handle: From " + instructionHandle + " to " + instructionHandle2);
        } else if (instructionHandle3 == instructionHandle || instructionHandle3 == instructionHandle2) {
            throw new ClassGenException("Invalid range: From " + instructionHandle + " to " + instructionHandle2 + " contains target " + instructionHandle3);
        } else {
            for (InstructionHandle instructionHandle4 = instructionHandle; instructionHandle4 != instructionHandle2.next; instructionHandle4 = instructionHandle4.next) {
                if (instructionHandle4 == null) {
                    throw new ClassGenException("Invalid range: From " + instructionHandle + " to " + instructionHandle2);
                } else if (instructionHandle4 == instructionHandle3) {
                    throw new ClassGenException("Invalid range: From " + instructionHandle + " to " + instructionHandle2 + " contains target " + instructionHandle3);
                }
            }
            InstructionHandle instructionHandle5 = instructionHandle.prev;
            InstructionHandle instructionHandle6 = instructionHandle2.next;
            if (instructionHandle5 != null) {
                instructionHandle5.next = instructionHandle6;
            } else {
                this.start = instructionHandle6;
            }
            if (instructionHandle6 != null) {
                instructionHandle6.prev = instructionHandle5;
            } else {
                this.end = instructionHandle5;
            }
            instructionHandle2.next = null;
            instructionHandle.prev = null;
            if (instructionHandle3 == null) {
                instructionHandle2.next = this.start;
                this.start = instructionHandle;
                return;
            }
            InstructionHandle instructionHandle7 = instructionHandle3.next;
            instructionHandle3.next = instructionHandle;
            instructionHandle.prev = instructionHandle3;
            instructionHandle2.next = instructionHandle7;
            if (instructionHandle7 != null) {
                instructionHandle7.prev = instructionHandle2;
            }
        }
    }

    public void move(InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        move(instructionHandle, instructionHandle, instructionHandle2);
    }

    private void remove(InstructionHandle instructionHandle, InstructionHandle instructionHandle2) throws TargetLostException {
        InstructionHandle instructionHandle3;
        InstructionHandle instructionHandle4;
        if (instructionHandle == null && instructionHandle2 == null) {
            instructionHandle3 = this.start;
            this.end = null;
            this.start = null;
            instructionHandle4 = instructionHandle3;
        } else {
            if (instructionHandle == null) {
                instructionHandle4 = this.start;
                this.start = instructionHandle2;
            } else {
                instructionHandle4 = instructionHandle.next;
                instructionHandle.next = instructionHandle2;
            }
            if (instructionHandle2 == null) {
                InstructionHandle instructionHandle5 = this.end;
                this.end = instructionHandle;
                instructionHandle3 = instructionHandle5;
            } else {
                InstructionHandle instructionHandle6 = instructionHandle2.prev;
                instructionHandle2.prev = instructionHandle;
                instructionHandle3 = instructionHandle6;
            }
        }
        instructionHandle4.prev = null;
        instructionHandle3.next = null;
        ArrayList arrayList = new ArrayList();
        for (InstructionHandle instructionHandle7 = instructionHandle4; instructionHandle7 != null; instructionHandle7 = instructionHandle7.next) {
            instructionHandle7.getInstruction().dispose();
        }
        StringBuffer stringBuffer = new StringBuffer("{ ");
        while (instructionHandle4 != null) {
            InstructionHandle instructionHandle8 = instructionHandle4.next;
            this.length--;
            if (instructionHandle4.hasTargeters()) {
                arrayList.add(instructionHandle4);
                stringBuffer.append(instructionHandle4.toString(true) + " ");
                instructionHandle4.prev = null;
                instructionHandle4.next = null;
            } else {
                instructionHandle4.dispose();
            }
            instructionHandle4 = instructionHandle8;
        }
        stringBuffer.append("}");
        if (!arrayList.isEmpty()) {
            InstructionHandle[] instructionHandleArr = new InstructionHandle[arrayList.size()];
            arrayList.toArray(instructionHandleArr);
            throw new TargetLostException(instructionHandleArr, stringBuffer.toString());
        }
    }

    public void delete(InstructionHandle instructionHandle) throws TargetLostException {
        remove(instructionHandle.prev, instructionHandle.next);
    }

    public void delete(Instruction instruction) throws TargetLostException {
        InstructionHandle findInstruction1 = findInstruction1(instruction);
        if (findInstruction1 != null) {
            delete(findInstruction1);
            return;
        }
        throw new ClassGenException("Instruction " + instruction + " is not contained in this list.");
    }

    public void delete(InstructionHandle instructionHandle, InstructionHandle instructionHandle2) throws TargetLostException {
        remove(instructionHandle.prev, instructionHandle2.next);
    }

    public void delete(Instruction instruction, Instruction instruction2) throws TargetLostException {
        InstructionHandle findInstruction1 = findInstruction1(instruction);
        if (findInstruction1 != null) {
            InstructionHandle findInstruction2 = findInstruction2(instruction2);
            if (findInstruction2 != null) {
                delete(findInstruction1, findInstruction2);
                return;
            }
            throw new ClassGenException("Instruction " + instruction2 + " is not contained in this list.");
        }
        throw new ClassGenException("Instruction " + instruction + " is not contained in this list.");
    }

    private InstructionHandle findInstruction1(Instruction instruction) {
        for (InstructionHandle instructionHandle = this.start; instructionHandle != null; instructionHandle = instructionHandle.next) {
            if (instructionHandle.instruction == instruction) {
                return instructionHandle;
            }
        }
        return null;
    }

    private InstructionHandle findInstruction2(Instruction instruction) {
        for (InstructionHandle instructionHandle = this.end; instructionHandle != null; instructionHandle = instructionHandle.prev) {
            if (instructionHandle.instruction == instruction) {
                return instructionHandle;
            }
        }
        return null;
    }

    public boolean contains(InstructionHandle instructionHandle) {
        if (instructionHandle == null) {
            return false;
        }
        for (InstructionHandle instructionHandle2 = this.start; instructionHandle2 != null; instructionHandle2 = instructionHandle2.next) {
            if (instructionHandle2 == instructionHandle) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(Instruction instruction) {
        return findInstruction1(instruction) != null;
    }

    public void setPositions() {
        setPositions(false);
    }

    public void setPositions(boolean z) {
        InstructionHandle[] targets;
        int[] iArr = new int[this.length];
        if (z) {
            for (InstructionHandle instructionHandle = this.start; instructionHandle != null; instructionHandle = instructionHandle.next) {
                Instruction instruction = instructionHandle.instruction;
                if (instruction instanceof BranchInstruction) {
                    Instruction instruction2 = ((BranchInstruction) instruction).getTarget().instruction;
                    if (contains(instruction2)) {
                        if (instruction instanceof Select) {
                            Instruction instruction3 = instruction2;
                            for (InstructionHandle instructionHandle2 : ((Select) instruction).getTargets()) {
                                instruction3 = instructionHandle2.instruction;
                                if (!contains(instruction3)) {
                                    throw new ClassGenException("Branch target of " + Constants.OPCODE_NAMES[instruction.opcode] + ":" + instruction3 + " not in instruction list");
                                }
                            }
                            instruction2 = instruction3;
                        }
                        if (!(instructionHandle instanceof BranchHandle)) {
                            throw new ClassGenException("Branch instruction " + Constants.OPCODE_NAMES[instruction.opcode] + ":" + instruction2 + " not contained in BranchHandle.");
                        }
                    } else {
                        throw new ClassGenException("Branch target of " + Constants.OPCODE_NAMES[instruction.opcode] + ":" + instruction2 + " not in instruction list");
                    }
                }
            }
        }
        InstructionHandle instructionHandle3 = this.start;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        while (instructionHandle3 != null) {
            Instruction instruction4 = instructionHandle3.instruction;
            instructionHandle3.setPosition(i);
            int i4 = i2 + 1;
            iArr[i2] = i;
            switch (instruction4.getOpcode()) {
                case 167:
                case 168:
                    i3 += 2;
                    break;
                case 170:
                case 171:
                    i3 += 3;
                    break;
            }
            i += instruction4.getLength();
            instructionHandle3 = instructionHandle3.next;
            i2 = i4;
        }
        int i5 = 0;
        for (InstructionHandle instructionHandle4 = this.start; instructionHandle4 != null; instructionHandle4 = instructionHandle4.next) {
            i5 += instructionHandle4.updatePosition(i5, i3);
        }
        InstructionHandle instructionHandle5 = this.start;
        int i6 = 0;
        int i7 = 0;
        while (instructionHandle5 != null) {
            Instruction instruction5 = instructionHandle5.instruction;
            instructionHandle5.setPosition(i7);
            iArr[i6] = i7;
            i7 += instruction5.getLength();
            instructionHandle5 = instructionHandle5.next;
            i6++;
        }
        this.byte_positions = new int[i6];
        System.arraycopy(iArr, 0, this.byte_positions, 0, i6);
    }

    public byte[] getByteCode() {
        setPositions();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            for (InstructionHandle instructionHandle = this.start; instructionHandle != null; instructionHandle = instructionHandle.next) {
                instructionHandle.instruction.dump(dataOutputStream);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            System.err.println(e);
            return null;
        }
    }

    public Instruction[] getInstructions() {
        ByteSequence byteSequence = new ByteSequence(getByteCode());
        ArrayList arrayList = new ArrayList();
        while (byteSequence.available() > 0) {
            try {
                arrayList.add(Instruction.readInstruction(byteSequence));
            } catch (IOException e) {
                throw new ClassGenException(e.toString());
            }
        }
        Instruction[] instructionArr = new Instruction[arrayList.size()];
        arrayList.toArray(instructionArr);
        return instructionArr;
    }

    @Override // java.lang.Object
    public String toString() {
        return toString(true);
    }

    public String toString(boolean z) {
        StringBuffer stringBuffer = new StringBuffer();
        for (InstructionHandle instructionHandle = this.start; instructionHandle != null; instructionHandle = instructionHandle.next) {
            stringBuffer.append(instructionHandle.toString(z) + "\n");
        }
        return stringBuffer.toString();
    }

    public Iterator iterator() {
        return new Iterator() {
            /* class ohos.com.sun.org.apache.bcel.internal.generic.InstructionList.AnonymousClass1 */
            private InstructionHandle ih = InstructionList.this.start;

            @Override // java.util.Iterator
            public Object next() {
                InstructionHandle instructionHandle = this.ih;
                this.ih = instructionHandle.next;
                return instructionHandle;
            }

            @Override // java.util.Iterator
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.ih != null;
            }
        };
    }

    public InstructionHandle[] getInstructionHandles() {
        InstructionHandle[] instructionHandleArr = new InstructionHandle[this.length];
        InstructionHandle instructionHandle = this.start;
        for (int i = 0; i < this.length; i++) {
            instructionHandleArr[i] = instructionHandle;
            instructionHandle = instructionHandle.next;
        }
        return instructionHandleArr;
    }

    public int[] getInstructionPositions() {
        return this.byte_positions;
    }

    public InstructionList copy() {
        HashMap hashMap = new HashMap();
        InstructionList instructionList = new InstructionList();
        for (InstructionHandle instructionHandle = this.start; instructionHandle != null; instructionHandle = instructionHandle.next) {
            Instruction copy = instructionHandle.instruction.copy();
            if (copy instanceof BranchInstruction) {
                hashMap.put(instructionHandle, instructionList.append((BranchInstruction) copy));
            } else {
                hashMap.put(instructionHandle, instructionList.append(copy));
            }
        }
        InstructionHandle instructionHandle2 = this.start;
        InstructionHandle instructionHandle3 = instructionList.start;
        while (instructionHandle2 != null) {
            Instruction instruction = instructionHandle2.instruction;
            Instruction instruction2 = instructionHandle3.instruction;
            if (instruction instanceof BranchInstruction) {
                BranchInstruction branchInstruction = (BranchInstruction) instruction;
                BranchInstruction branchInstruction2 = (BranchInstruction) instruction2;
                branchInstruction2.setTarget((InstructionHandle) hashMap.get(branchInstruction.getTarget()));
                if (branchInstruction instanceof Select) {
                    InstructionHandle[] targets = ((Select) branchInstruction).getTargets();
                    InstructionHandle[] targets2 = ((Select) branchInstruction2).getTargets();
                    for (int i = 0; i < targets.length; i++) {
                        targets2[i] = (InstructionHandle) hashMap.get(targets[i]);
                    }
                }
            }
            instructionHandle2 = instructionHandle2.next;
            instructionHandle3 = instructionHandle3.next;
        }
        return instructionList;
    }

    public void replaceConstantPool(ConstantPoolGen constantPoolGen, ConstantPoolGen constantPoolGen2) {
        for (InstructionHandle instructionHandle = this.start; instructionHandle != null; instructionHandle = instructionHandle.next) {
            Instruction instruction = instructionHandle.instruction;
            if (instruction instanceof CPInstruction) {
                CPInstruction cPInstruction = (CPInstruction) instruction;
                cPInstruction.setIndex(constantPoolGen2.addConstant(constantPoolGen.getConstant(cPInstruction.getIndex()), constantPoolGen));
            }
        }
    }

    private void clear() {
        this.end = null;
        this.start = null;
        this.length = 0;
    }

    public void dispose() {
        for (InstructionHandle instructionHandle = this.end; instructionHandle != null; instructionHandle = instructionHandle.prev) {
            instructionHandle.dispose();
        }
        clear();
    }

    public InstructionHandle getStart() {
        return this.start;
    }

    public InstructionHandle getEnd() {
        return this.end;
    }

    public int getLength() {
        return this.length;
    }

    public int size() {
        return this.length;
    }

    public void redirectBranches(InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        for (InstructionHandle instructionHandle3 = this.start; instructionHandle3 != null; instructionHandle3 = instructionHandle3.next) {
            Instruction instruction = instructionHandle3.getInstruction();
            if (instruction instanceof BranchInstruction) {
                BranchInstruction branchInstruction = (BranchInstruction) instruction;
                if (branchInstruction.getTarget() == instructionHandle) {
                    branchInstruction.setTarget(instructionHandle2);
                }
                if (branchInstruction instanceof Select) {
                    Select select = (Select) branchInstruction;
                    InstructionHandle[] targets = select.getTargets();
                    for (int i = 0; i < targets.length; i++) {
                        if (targets[i] == instructionHandle) {
                            select.setTarget(i, instructionHandle2);
                        }
                    }
                }
            }
        }
    }

    public void redirectLocalVariables(LocalVariableGen[] localVariableGenArr, InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        for (int i = 0; i < localVariableGenArr.length; i++) {
            InstructionHandle start2 = localVariableGenArr[i].getStart();
            InstructionHandle end2 = localVariableGenArr[i].getEnd();
            if (start2 == instructionHandle) {
                localVariableGenArr[i].setStart(instructionHandle2);
            }
            if (end2 == instructionHandle) {
                localVariableGenArr[i].setEnd(instructionHandle2);
            }
        }
    }

    public void redirectExceptionHandlers(CodeExceptionGen[] codeExceptionGenArr, InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        for (int i = 0; i < codeExceptionGenArr.length; i++) {
            if (codeExceptionGenArr[i].getStartPC() == instructionHandle) {
                codeExceptionGenArr[i].setStartPC(instructionHandle2);
            }
            if (codeExceptionGenArr[i].getEndPC() == instructionHandle) {
                codeExceptionGenArr[i].setEndPC(instructionHandle2);
            }
            if (codeExceptionGenArr[i].getHandlerPC() == instructionHandle) {
                codeExceptionGenArr[i].setHandlerPC(instructionHandle2);
            }
        }
    }

    public void addObserver(InstructionListObserver instructionListObserver) {
        if (this.observers == null) {
            this.observers = new ArrayList();
        }
        this.observers.add(instructionListObserver);
    }

    public void removeObserver(InstructionListObserver instructionListObserver) {
        ArrayList arrayList = this.observers;
        if (arrayList != null) {
            arrayList.remove(instructionListObserver);
        }
    }

    public void update() {
        ArrayList arrayList = this.observers;
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ((InstructionListObserver) it.next()).notify(this);
            }
        }
    }
}
