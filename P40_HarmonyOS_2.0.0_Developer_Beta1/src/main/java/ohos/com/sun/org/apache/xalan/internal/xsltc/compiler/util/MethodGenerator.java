package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import ohos.com.sun.org.apache.bcel.internal.classfile.Field;
import ohos.com.sun.org.apache.bcel.internal.classfile.Method;
import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.DLOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.DSTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.FLOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.FSTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.ICONST;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.IfInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.IndexedInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionConstants;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionTargeter;
import ohos.com.sun.org.apache.bcel.internal.generic.LLOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.LSTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.MethodGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUTFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.RET;
import ohos.com.sun.org.apache.bcel.internal.generic.Select;
import ohos.com.sun.org.apache.bcel.internal.generic.TargetLostException;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.XSLTC;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

public class MethodGenerator extends MethodGen implements Constants {
    private static final int DOM_INDEX = 1;
    private static final String END_ELEMENT_SIG = "(Ljava/lang/String;)V";
    private static final int HANDLER_INDEX = 3;
    protected static final int INVALID_INDEX = -1;
    private static final int ITERATOR_INDEX = 2;
    private static final int MAX_BRANCH_TARGET_OFFSET = 32767;
    private static final int MAX_METHOD_SIZE = 65535;
    private static final int MINIMUM_OUTLINEABLE_CHUNK_SIZE = 1000;
    private static final int MIN_BRANCH_TARGET_OFFSET = -32768;
    private static final String START_ELEMENT_SIG = "(Ljava/lang/String;)V";
    private static final int TARGET_METHOD_SIZE = 60000;
    private boolean _allocatorInit = false;
    private final Instruction _aloadDom = new ALOAD(1);
    private final Instruction _aloadHandler = new ALOAD(3);
    private final Instruction _aloadIterator = new ALOAD(2);
    private final Instruction _astoreDom = new ASTORE(1);
    private final Instruction _astoreHandler = new ASTORE(3);
    private final Instruction _astoreIterator = new ASTORE(2);
    private final Instruction _attribute;
    private final Instruction _endDocument;
    private final Instruction _endElement;
    private Instruction _iloadCurrent;
    private Instruction _istoreCurrent;
    private LocalVariableRegistry _localVariableRegistry;
    private InstructionList _mapTypeSub;
    private final Instruction _namespace;
    private final Instruction _nextNode;
    private Map<Pattern, InstructionList> _preCompiled = new HashMap();
    private final Instruction _reset;
    private final Instruction _setStartNode;
    private SlotAllocator _slotAllocator;
    private final Instruction _startDocument;
    private final Instruction _startElement;
    private final Instruction _uniqueAttribute;
    private int m_openChunks = 0;
    private int m_totalChunks = 0;

    public MethodGenerator(int i, Type type, Type[] typeArr, String[] strArr, String str, String str2, InstructionList instructionList, ConstantPoolGen constantPoolGen) {
        super(i, type, typeArr, strArr, str, str2, instructionList, constantPoolGen);
        this._startElement = new INVOKEINTERFACE(constantPoolGen.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "startElement", "(Ljava/lang/String;)V"), 2);
        this._endElement = new INVOKEINTERFACE(constantPoolGen.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "endElement", "(Ljava/lang/String;)V"), 2);
        this._attribute = new INVOKEINTERFACE(constantPoolGen.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", Constants.ADD_ATTRIBUTE, "(Ljava/lang/String;Ljava/lang/String;)V"), 3);
        this._uniqueAttribute = new INVOKEINTERFACE(constantPoolGen.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "addUniqueAttribute", "(Ljava/lang/String;Ljava/lang/String;I)V"), 4);
        this._namespace = new INVOKEINTERFACE(constantPoolGen.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "namespaceAfterStartElement", "(Ljava/lang/String;Ljava/lang/String;)V"), 3);
        this._startDocument = new INVOKEINTERFACE(constantPoolGen.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "startDocument", "()V"), 1);
        this._endDocument = new INVOKEINTERFACE(constantPoolGen.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "endDocument", "()V"), 1);
        this._setStartNode = new INVOKEINTERFACE(constantPoolGen.addInterfaceMethodref(Constants.NODE_ITERATOR, Constants.SET_START_NODE, "(I)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), 2);
        this._reset = new INVOKEINTERFACE(constantPoolGen.addInterfaceMethodref(Constants.NODE_ITERATOR, Constants.RESET, "()Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), 1);
        this._nextNode = new INVOKEINTERFACE(constantPoolGen.addInterfaceMethodref(Constants.NODE_ITERATOR, Constants.NEXT, "()I"), 1);
        this._slotAllocator = new SlotAllocator();
        this._slotAllocator.initialize(getLocalVariableRegistry().getLocals(false));
        this._allocatorInit = true;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.MethodGen
    public LocalVariableGen addLocalVariable(String str, Type type, InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        if (this._allocatorInit) {
            return addLocalVariable2(str, type, instructionHandle);
        }
        LocalVariableGen addLocalVariable = super.addLocalVariable(str, type, instructionHandle, instructionHandle2);
        getLocalVariableRegistry().registerLocalVariable(addLocalVariable);
        return addLocalVariable;
    }

    public LocalVariableGen addLocalVariable2(String str, Type type, InstructionHandle instructionHandle) {
        LocalVariableGen addLocalVariable = super.addLocalVariable(str, type, this._slotAllocator.allocateSlot(type), instructionHandle, null);
        getLocalVariableRegistry().registerLocalVariable(addLocalVariable);
        return addLocalVariable;
    }

    private LocalVariableRegistry getLocalVariableRegistry() {
        if (this._localVariableRegistry == null) {
            this._localVariableRegistry = new LocalVariableRegistry();
        }
        return this._localVariableRegistry;
    }

    /* access modifiers changed from: protected */
    public class LocalVariableRegistry {
        protected HashMap _nameToLVGMap = new HashMap();
        protected ArrayList _variables = new ArrayList();

        protected LocalVariableRegistry() {
        }

        /* access modifiers changed from: protected */
        public void registerLocalVariable(LocalVariableGen localVariableGen) {
            int index = localVariableGen.getIndex();
            int size = this._variables.size();
            if (index >= size) {
                while (size < index) {
                    this._variables.add(null);
                    size++;
                }
                this._variables.add(localVariableGen);
            } else {
                Object obj = this._variables.get(index);
                if (obj == null) {
                    this._variables.set(index, localVariableGen);
                } else if (obj instanceof LocalVariableGen) {
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(obj);
                    arrayList.add(localVariableGen);
                    this._variables.set(index, arrayList);
                } else {
                    ((ArrayList) obj).add(localVariableGen);
                }
            }
            registerByName(localVariableGen);
        }

        /* access modifiers changed from: protected */
        public LocalVariableGen lookupRegisteredLocalVariable(int i, int i2) {
            ArrayList arrayList = this._variables;
            Object obj = arrayList != null ? arrayList.get(i) : null;
            if (obj != null) {
                if (obj instanceof LocalVariableGen) {
                    LocalVariableGen localVariableGen = (LocalVariableGen) obj;
                    if (MethodGenerator.this.offsetInLocalVariableGenRange(localVariableGen, i2)) {
                        return localVariableGen;
                    }
                } else {
                    ArrayList arrayList2 = (ArrayList) obj;
                    int size = arrayList2.size();
                    for (int i3 = 0; i3 < size; i3++) {
                        LocalVariableGen localVariableGen2 = (LocalVariableGen) arrayList2.get(i3);
                        if (MethodGenerator.this.offsetInLocalVariableGenRange(localVariableGen2, i2)) {
                            return localVariableGen2;
                        }
                    }
                }
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void registerByName(LocalVariableGen localVariableGen) {
            ArrayList arrayList;
            Object obj = this._nameToLVGMap.get(localVariableGen.getName());
            if (obj == null) {
                this._nameToLVGMap.put(localVariableGen.getName(), localVariableGen);
                return;
            }
            if (obj instanceof ArrayList) {
                arrayList = (ArrayList) obj;
                arrayList.add(localVariableGen);
            } else {
                ArrayList arrayList2 = new ArrayList();
                arrayList2.add(obj);
                arrayList2.add(localVariableGen);
                arrayList = arrayList2;
            }
            this._nameToLVGMap.put(localVariableGen.getName(), arrayList);
        }

        /* access modifiers changed from: protected */
        public void removeByNameTracking(LocalVariableGen localVariableGen) {
            Object obj = this._nameToLVGMap.get(localVariableGen.getName());
            if (obj instanceof ArrayList) {
                ArrayList arrayList = (ArrayList) obj;
                for (int i = 0; i < arrayList.size(); i++) {
                    if (arrayList.get(i) == localVariableGen) {
                        arrayList.remove(i);
                        return;
                    }
                }
                return;
            }
            this._nameToLVGMap.remove(localVariableGen);
        }

        /* access modifiers changed from: protected */
        public LocalVariableGen lookUpByName(String str) {
            Object obj = this._nameToLVGMap.get(str);
            if (!(obj instanceof ArrayList)) {
                return (LocalVariableGen) obj;
            }
            ArrayList arrayList = (ArrayList) obj;
            LocalVariableGen localVariableGen = null;
            for (int i = 0; i < arrayList.size(); i++) {
                localVariableGen = (LocalVariableGen) arrayList.get(i);
                if (localVariableGen.getName() == str) {
                    return localVariableGen;
                }
            }
            return localVariableGen;
        }

        /* access modifiers changed from: protected */
        public LocalVariableGen[] getLocals(boolean z) {
            ArrayList arrayList = new ArrayList();
            if (z) {
                int size = arrayList.size();
                for (int i = 0; i < size; i++) {
                    Object obj = this._variables.get(i);
                    if (obj != null) {
                        if (obj instanceof ArrayList) {
                            ArrayList arrayList2 = (ArrayList) obj;
                            for (int i2 = 0; i2 < arrayList2.size(); i2++) {
                                arrayList.add(arrayList2.get(i));
                            }
                        } else {
                            arrayList.add(obj);
                        }
                    }
                }
            } else {
                for (Map.Entry entry : this._nameToLVGMap.entrySet()) {
                    Object value = entry.getValue();
                    if (value != null) {
                        if (value instanceof ArrayList) {
                            ArrayList arrayList3 = (ArrayList) value;
                            for (int i3 = 0; i3 < arrayList3.size(); i3++) {
                                arrayList.add(arrayList3.get(i3));
                            }
                        } else {
                            arrayList.add(value);
                        }
                    }
                }
            }
            LocalVariableGen[] localVariableGenArr = new LocalVariableGen[arrayList.size()];
            arrayList.toArray(localVariableGenArr);
            return localVariableGenArr;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean offsetInLocalVariableGenRange(LocalVariableGen localVariableGen, int i) {
        InstructionHandle start = localVariableGen.getStart();
        InstructionHandle end = localVariableGen.getEnd();
        if (start == null) {
            start = getInstructionList().getStart();
        }
        if (end == null) {
            end = getInstructionList().getEnd();
        }
        return start.getPosition() <= i && end.getPosition() + end.getInstruction().getLength() >= i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.MethodGen
    public void removeLocalVariable(LocalVariableGen localVariableGen) {
        this._slotAllocator.releaseSlot(localVariableGen);
        getLocalVariableRegistry().removeByNameTracking(localVariableGen);
        super.removeLocalVariable(localVariableGen);
    }

    public Instruction loadDOM() {
        return this._aloadDom;
    }

    public Instruction storeDOM() {
        return this._astoreDom;
    }

    public Instruction storeHandler() {
        return this._astoreHandler;
    }

    public Instruction loadHandler() {
        return this._aloadHandler;
    }

    public Instruction storeIterator() {
        return this._astoreIterator;
    }

    public Instruction loadIterator() {
        return this._aloadIterator;
    }

    public final Instruction setStartNode() {
        return this._setStartNode;
    }

    public final Instruction reset() {
        return this._reset;
    }

    public final Instruction nextNode() {
        return this._nextNode;
    }

    public final Instruction startElement() {
        return this._startElement;
    }

    public final Instruction endElement() {
        return this._endElement;
    }

    public final Instruction startDocument() {
        return this._startDocument;
    }

    public final Instruction endDocument() {
        return this._endDocument;
    }

    public final Instruction attribute() {
        return this._attribute;
    }

    public final Instruction uniqueAttribute() {
        return this._uniqueAttribute;
    }

    public final Instruction namespace() {
        return this._namespace;
    }

    public Instruction loadCurrentNode() {
        if (this._iloadCurrent == null) {
            int localIndex = getLocalIndex(Keywords.FUNC_CURRENT_STRING);
            if (localIndex > 0) {
                this._iloadCurrent = new ILOAD(localIndex);
            } else {
                this._iloadCurrent = new ICONST(0);
            }
        }
        return this._iloadCurrent;
    }

    public Instruction storeCurrentNode() {
        Instruction instruction = this._istoreCurrent;
        if (instruction != null) {
            return instruction;
        }
        ISTORE istore = new ISTORE(getLocalIndex(Keywords.FUNC_CURRENT_STRING));
        this._istoreCurrent = istore;
        return istore;
    }

    public Instruction loadContextNode() {
        return loadCurrentNode();
    }

    public Instruction storeContextNode() {
        return storeCurrentNode();
    }

    public int getLocalIndex(String str) {
        return getLocalVariable(str).getIndex();
    }

    public LocalVariableGen getLocalVariable(String str) {
        return getLocalVariableRegistry().lookUpByName(str);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.MethodGen
    public void setMaxLocals() {
        int maxLocals = super.getMaxLocals();
        LocalVariableGen[] localVariables = super.getLocalVariables();
        if (localVariables != null && localVariables.length > maxLocals) {
            maxLocals = localVariables.length;
        }
        if (maxLocals < 5) {
            maxLocals = 5;
        }
        super.setMaxLocals(maxLocals);
    }

    public void addInstructionList(Pattern pattern, InstructionList instructionList) {
        this._preCompiled.put(pattern, instructionList);
    }

    public InstructionList getInstructionList(Pattern pattern) {
        return this._preCompiled.get(pattern);
    }

    /* access modifiers changed from: private */
    public class Chunk implements Comparable {
        private InstructionHandle m_end;
        private int m_size;
        private InstructionHandle m_start;

        Chunk(InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
            this.m_start = instructionHandle;
            this.m_end = instructionHandle2;
            this.m_size = instructionHandle2.getPosition() - instructionHandle.getPosition();
        }

        /* access modifiers changed from: package-private */
        public boolean isAdjacentTo(Chunk chunk) {
            return getChunkEnd().getNext() == chunk.getChunkStart();
        }

        /* access modifiers changed from: package-private */
        public InstructionHandle getChunkStart() {
            return this.m_start;
        }

        /* access modifiers changed from: package-private */
        public InstructionHandle getChunkEnd() {
            return this.m_end;
        }

        /* access modifiers changed from: package-private */
        public int getChunkSize() {
            return this.m_size;
        }

        @Override // java.lang.Comparable
        public int compareTo(Object obj) {
            return getChunkSize() - ((Chunk) obj).getChunkSize();
        }
    }

    private ArrayList getCandidateChunks(ClassGenerator classGenerator, int i) {
        InstructionHandle instructionHandle;
        int size;
        Iterator it = getInstructionList().iterator();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        Stack stack = new Stack();
        if (this.m_openChunks == 0) {
            ArrayList arrayList3 = arrayList2;
            boolean z = true;
            boolean z2 = false;
            do {
                ArrayList arrayList4 = null;
                instructionHandle = it.hasNext() ? (InstructionHandle) it.next() : null;
                Instruction instruction = instructionHandle != null ? instructionHandle.getInstruction() : null;
                if (z) {
                    arrayList3.add(instructionHandle);
                    z2 = true;
                    z = false;
                }
                if (instruction instanceof OutlineableChunkStart) {
                    if (z2) {
                        stack.push(arrayList3);
                        arrayList3 = new ArrayList();
                    }
                    arrayList3.add(instructionHandle);
                    z2 = true;
                    continue;
                } else if (instructionHandle == null || (instruction instanceof OutlineableChunkEnd)) {
                    if (!z2) {
                        arrayList4 = arrayList3;
                        arrayList3 = (ArrayList) stack.pop();
                    }
                    if ((instructionHandle != null ? instructionHandle.getPosition() : i) - ((InstructionHandle) arrayList3.get(arrayList3.size() - 1)).getPosition() <= 60000) {
                        arrayList3.add(instructionHandle);
                    } else {
                        if (!z2 && (size = arrayList4.size() / 2) > 0) {
                            Chunk[] chunkArr = new Chunk[size];
                            for (int i2 = 0; i2 < size; i2++) {
                                int i3 = i2 * 2;
                                chunkArr[i2] = new Chunk((InstructionHandle) arrayList4.get(i3), (InstructionHandle) arrayList4.get(i3 + 1));
                            }
                            ArrayList mergeAdjacentChunks = mergeAdjacentChunks(chunkArr);
                            for (int i4 = 0; i4 < mergeAdjacentChunks.size(); i4++) {
                                Chunk chunk = (Chunk) mergeAdjacentChunks.get(i4);
                                int chunkSize = chunk.getChunkSize();
                                if (chunkSize >= 1000 && chunkSize <= 60000) {
                                    arrayList.add(chunk);
                                }
                            }
                        }
                        arrayList3.remove(arrayList3.size() - 1);
                    }
                    z2 = (arrayList3.size() & 1) == 1;
                    continue;
                }
            } while (instructionHandle != null);
            return arrayList;
        }
        throw new InternalError(new ErrorMsg(ErrorMsg.OUTLINE_ERR_UNBALANCED_MARKERS).toString());
    }

    private ArrayList mergeAdjacentChunks(Chunk[] chunkArr) {
        int[] iArr = new int[chunkArr.length];
        int[] iArr2 = new int[chunkArr.length];
        boolean[] zArr = new boolean[chunkArr.length];
        ArrayList arrayList = new ArrayList();
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        for (int i4 = 1; i4 < chunkArr.length; i4++) {
            if (!chunkArr[i4 - 1].isAdjacentTo(chunkArr[i4])) {
                int i5 = i4 - i;
                if (i2 < i5) {
                    i2 = i5;
                }
                if (i5 > 1) {
                    iArr2[i3] = i5;
                    iArr[i3] = i;
                    i3++;
                }
                i = i4;
            }
        }
        if (chunkArr.length - i > 1) {
            int length = chunkArr.length - i;
            if (i2 < length) {
                i2 = length;
            }
            iArr2[i3] = chunkArr.length - i;
            iArr[i3] = i;
            i3++;
        }
        while (i2 > 1) {
            int i6 = 0;
            while (i6 < i3) {
                int i7 = iArr[i6];
                int i8 = (iArr2[i6] + i7) - 1;
                int i9 = i3;
                boolean z = false;
                while (true) {
                    int i10 = (i7 + i2) - 1;
                    if (i10 > i8 || z) {
                        break;
                    }
                    int i11 = 0;
                    for (int i12 = i7; i12 <= i10; i12++) {
                        i11 += chunkArr[i12].getChunkSize();
                    }
                    if (i11 <= 60000) {
                        for (int i13 = i7; i13 <= i10; i13++) {
                            zArr[i13] = true;
                        }
                        arrayList.add(new Chunk(chunkArr[i7].getChunkStart(), chunkArr[i10].getChunkEnd()));
                        iArr2[i6] = iArr[i6] - i7;
                        int i14 = i8 - i10;
                        if (i14 >= 2) {
                            iArr[i9] = i10 + 1;
                            iArr2[i9] = i14;
                            i9++;
                        }
                        z = true;
                    }
                    i7++;
                }
                i6++;
                i3 = i9;
            }
            i2--;
        }
        for (int i15 = 0; i15 < chunkArr.length; i15++) {
            if (!zArr[i15]) {
                arrayList.add(chunkArr[i15]);
            }
        }
        return arrayList;
    }

    public Method[] outlineChunks(ClassGenerator classGenerator, int i) {
        ArrayList arrayList = new ArrayList();
        String name = getName();
        if (name.equals(ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME)) {
            name = "$lt$init$gt$";
        } else if (name.equals(ohos.com.sun.org.apache.bcel.internal.Constants.STATIC_INITIALIZER_NAME)) {
            name = "$lt$clinit$gt$";
        }
        int i2 = 0;
        while (true) {
            ArrayList candidateChunks = getCandidateChunks(classGenerator, i);
            Collections.sort(candidateChunks);
            int size = candidateChunks.size() - 1;
            int i3 = i2;
            boolean z = false;
            while (size >= 0 && i > 60000) {
                Chunk chunk = (Chunk) candidateChunks.get(size);
                arrayList.add(outline(chunk.getChunkStart(), chunk.getChunkEnd(), name + "$outline$" + i3, classGenerator));
                i3++;
                InstructionList instructionList = getInstructionList();
                InstructionHandle end = instructionList.getEnd();
                instructionList.setPositions();
                i = end.getPosition() + end.getInstruction().getLength();
                size--;
                z = true;
            }
            if (!z || i <= 60000) {
                break;
            }
            i2 = i3;
        }
        if (i <= 65535) {
            Method[] methodArr = new Method[(arrayList.size() + 1)];
            arrayList.toArray(methodArr);
            methodArr[arrayList.size()] = getThisMethod();
            return methodArr;
        }
        throw new InternalError(new ErrorMsg(ErrorMsg.OUTLINE_ERR_METHOD_TOO_BIG).toString());
    }

    /* JADX WARNING: Removed duplicated region for block: B:105:0x03f3  */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x03f8 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x02d2  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x02dc  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x02e4  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x02f5  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x03c8  */
    private Method outline(InstructionHandle instructionHandle, InstructionHandle instructionHandle2, String str, ClassGenerator classGenerator) {
        InstructionHandle instructionHandle3;
        String[] exceptions;
        InstructionHandle instructionHandle4;
        HashMap hashMap;
        InstructionList instructionList;
        HashMap hashMap2;
        InstructionHandle instructionHandle5;
        MethodGenerator methodGenerator;
        Object obj;
        int i;
        HashMap hashMap3;
        MethodGenerator methodGenerator2;
        InstructionList instructionList2;
        InstructionList instructionList3;
        InstructionList instructionList4;
        InstructionHandle instructionHandle6;
        HashMap hashMap4;
        InstructionHandle instructionHandle7;
        InstructionHandle instructionHandle8;
        InstructionHandle instructionHandle9;
        HashMap hashMap5;
        InstructionHandle instructionHandle10;
        int i2;
        MethodGenerator methodGenerator3 = this;
        if (getExceptionHandlers().length == 0) {
            int position = instructionHandle.getPosition();
            int position2 = instructionHandle2.getPosition() + instructionHandle2.getInstruction().getLength();
            ConstantPoolGen constantPool = getConstantPool();
            InstructionList instructionList5 = new InstructionList();
            XSLTC xsltc = classGenerator.getParser().getXSLTC();
            String helperClassName = xsltc.getHelperClassName();
            Type[] typeArr = {new ObjectType(helperClassName).toJCType()};
            String[] strArr = {"copyLocals"};
            int i3 = 18;
            boolean z = (getAccessFlags() & 8) != 0;
            if (z) {
                i3 = 26;
            }
            MethodGenerator methodGenerator4 = new MethodGenerator(i3, Type.VOID, typeArr, strArr, str, getClassName(), instructionList5, constantPool);
            InstructionList instructionList6 = instructionList5;
            AnonymousClass1 r10 = new ClassGenerator(helperClassName, Constants.OBJECT_CLASS, helperClassName + ".java", 49, null, classGenerator.getStylesheet()) {
                /* class ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator.AnonymousClass1 */

                @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator
                public boolean isExternal() {
                    return true;
                }
            };
            ConstantPoolGen constantPool2 = r10.getConstantPool();
            r10.addEmptyConstructor(1);
            InstructionHandle next = instructionHandle2.getNext();
            InstructionList instructionList7 = new InstructionList();
            InstructionList instructionList8 = new InstructionList();
            InstructionList instructionList9 = new InstructionList();
            InstructionList instructionList10 = new InstructionList();
            InstructionHandle append = instructionList7.append(new NEW(constantPool.addClass(helperClassName)));
            instructionList7.append(InstructionConstants.DUP);
            instructionList7.append(InstructionConstants.DUP);
            InstructionHandle instructionHandle11 = append;
            instructionList7.append(new INVOKESPECIAL(constantPool.addMethodref(helperClassName, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "()V")));
            if (z) {
                instructionHandle3 = instructionList8.append(new INVOKESTATIC(constantPool.addMethodref(classGenerator.getClassName(), str, methodGenerator4.getSignature())));
            } else {
                instructionList8.append(InstructionConstants.THIS);
                instructionList8.append(InstructionConstants.SWAP);
                instructionHandle3 = instructionList8.append(new INVOKEVIRTUAL(constantPool.addMethodref(classGenerator.getClassName(), str, methodGenerator4.getSignature())));
            }
            HashMap hashMap6 = new HashMap();
            HashMap hashMap7 = new HashMap();
            HashMap hashMap8 = new HashMap();
            HashMap hashMap9 = new HashMap();
            InstructionHandle instructionHandle12 = instructionHandle3;
            InstructionHandle instructionHandle13 = instructionHandle;
            HashMap hashMap10 = hashMap8;
            boolean z2 = false;
            int i4 = 0;
            InstructionHandle instructionHandle14 = null;
            InstructionHandle instructionHandle15 = null;
            while (instructionHandle13 != next) {
                Instruction instruction = instructionHandle13.getInstruction();
                if (instruction instanceof MarkerInstruction) {
                    if (instructionHandle13.hasTargeters()) {
                        if (instruction instanceof OutlineableChunkEnd) {
                            hashMap6.put(instructionHandle13, instructionHandle14);
                        } else if (!z2) {
                            instructionHandle6 = instructionHandle13;
                            instructionHandle15 = instructionHandle6;
                            hashMap3 = hashMap7;
                            instructionList2 = instructionList6;
                            methodGenerator2 = methodGenerator4;
                            z2 = true;
                            hashMap4 = hashMap10;
                            instructionList3 = instructionList10;
                            instructionList4 = instructionList8;
                        }
                    }
                    instructionHandle6 = instructionHandle13;
                    hashMap3 = hashMap7;
                    instructionList2 = instructionList6;
                    methodGenerator2 = methodGenerator4;
                    hashMap4 = hashMap10;
                    instructionList3 = instructionList10;
                    instructionList4 = instructionList8;
                } else {
                    Instruction copy = instruction.copy();
                    if (copy instanceof BranchInstruction) {
                        instructionHandle7 = instructionList6.append((BranchInstruction) copy);
                    } else {
                        instructionHandle7 = instructionList6.append(copy);
                    }
                    if ((copy instanceof LocalVariableInstruction) || (copy instanceof RET)) {
                        int index = ((IndexedInstruction) copy).getIndex();
                        instructionList2 = instructionList6;
                        LocalVariableGen lookupRegisteredLocalVariable = getLocalVariableRegistry().lookupRegisteredLocalVariable(index, instructionHandle13.getPosition());
                        LocalVariableGen localVariableGen = (LocalVariableGen) hashMap7.get(lookupRegisteredLocalVariable);
                        if (hashMap7.get(lookupRegisteredLocalVariable) == null) {
                            boolean offsetInLocalVariableGenRange = methodGenerator3.offsetInLocalVariableGenRange(lookupRegisteredLocalVariable, position != 0 ? position - 1 : 0);
                            hashMap5 = hashMap6;
                            boolean offsetInLocalVariableGenRange2 = methodGenerator3.offsetInLocalVariableGenRange(lookupRegisteredLocalVariable, position2 + 1);
                            if (offsetInLocalVariableGenRange || offsetInLocalVariableGenRange2) {
                                String name = lookupRegisteredLocalVariable.getName();
                                instructionHandle9 = instructionHandle7;
                                Type type = lookupRegisteredLocalVariable.getType();
                                instructionHandle8 = instructionHandle13;
                                LocalVariableGen addLocalVariable = methodGenerator4.addLocalVariable(name, type, null, null);
                                int index2 = addLocalVariable.getIndex();
                                methodGenerator2 = methodGenerator4;
                                String signature = type.getSignature();
                                hashMap7.put(lookupRegisteredLocalVariable, addLocalVariable);
                                int i5 = i4 + 1;
                                hashMap3 = hashMap7;
                                String str2 = "field" + i5;
                                r10.addField(new Field(1, constantPool2.addUtf8(str2), constantPool2.addUtf8(signature), null, constantPool2.getConstantPool()));
                                int addFieldref = constantPool.addFieldref(helperClassName, str2, signature);
                                if (offsetInLocalVariableGenRange) {
                                    instructionList7.append(InstructionConstants.DUP);
                                    InstructionHandle append2 = instructionList7.append(loadLocal(index, type));
                                    instructionList7.append(new PUTFIELD(addFieldref));
                                    if (!offsetInLocalVariableGenRange2) {
                                        hashMap9.put(lookupRegisteredLocalVariable, append2);
                                    }
                                    instructionList9.append(InstructionConstants.ALOAD_1);
                                    instructionList9.append(new GETFIELD(addFieldref));
                                    i2 = index2;
                                    instructionList9.append(storeLocal(i2, type));
                                } else {
                                    i2 = index2;
                                }
                                if (offsetInLocalVariableGenRange2) {
                                    instructionList3 = instructionList10;
                                    instructionList3.append(InstructionConstants.ALOAD_1);
                                    instructionList3.append(loadLocal(i2, type));
                                    instructionList3.append(new PUTFIELD(addFieldref));
                                    instructionList4 = instructionList8;
                                    instructionList4.append(InstructionConstants.DUP);
                                    instructionList4.append(new GETFIELD(addFieldref));
                                    InstructionHandle append3 = instructionList4.append(storeLocal(index, type));
                                    hashMap4 = hashMap10;
                                    if (!offsetInLocalVariableGenRange) {
                                        hashMap4.put(lookupRegisteredLocalVariable, append3);
                                    }
                                } else {
                                    hashMap4 = hashMap10;
                                    instructionList3 = instructionList10;
                                    instructionList4 = instructionList8;
                                }
                                i4 = i5;
                                if (instructionHandle8.hasTargeters()) {
                                    hashMap6 = hashMap5;
                                    instructionHandle10 = instructionHandle9;
                                    instructionHandle6 = instructionHandle8;
                                    hashMap6.put(instructionHandle6, instructionHandle10);
                                } else {
                                    hashMap6 = hashMap5;
                                    instructionHandle10 = instructionHandle9;
                                    instructionHandle6 = instructionHandle8;
                                }
                                if (z2) {
                                    InstructionHandle instructionHandle16 = instructionHandle15;
                                    do {
                                        hashMap6.put(instructionHandle16, instructionHandle10);
                                        instructionHandle16 = instructionHandle16.getNext();
                                    } while (instructionHandle16 != instructionHandle6);
                                    instructionHandle14 = instructionHandle10;
                                    instructionHandle15 = instructionHandle16;
                                    z2 = false;
                                } else {
                                    instructionHandle14 = instructionHandle10;
                                }
                            } else {
                                instructionHandle8 = instructionHandle13;
                                instructionHandle9 = instructionHandle7;
                                hashMap3 = hashMap7;
                                methodGenerator2 = methodGenerator4;
                                hashMap4 = hashMap10;
                                instructionList3 = instructionList10;
                                instructionList4 = instructionList8;
                                if (instructionHandle8.hasTargeters()) {
                                }
                                if (z2) {
                                }
                            }
                        } else {
                            hashMap5 = hashMap6;
                            instructionHandle8 = instructionHandle13;
                            instructionHandle9 = instructionHandle7;
                            hashMap3 = hashMap7;
                        }
                    } else {
                        hashMap5 = hashMap6;
                        instructionHandle8 = instructionHandle13;
                        instructionHandle9 = instructionHandle7;
                        hashMap3 = hashMap7;
                        instructionList2 = instructionList6;
                    }
                    methodGenerator2 = methodGenerator4;
                    hashMap4 = hashMap10;
                    instructionList3 = instructionList10;
                    instructionList4 = instructionList8;
                    if (instructionHandle8.hasTargeters()) {
                    }
                    if (z2) {
                    }
                }
                methodGenerator3 = this;
                hashMap10 = hashMap4;
                instructionHandle13 = instructionHandle6.getNext();
                instructionList8 = instructionList4;
                instructionList10 = instructionList3;
                next = next;
                instructionList6 = instructionList2;
                methodGenerator4 = methodGenerator2;
                hashMap7 = hashMap3;
            }
            HashMap hashMap11 = hashMap7;
            MethodGenerator methodGenerator5 = methodGenerator4;
            InstructionList instructionList11 = instructionList10;
            InstructionHandle start = instructionList6.getStart();
            InstructionHandle instructionHandle17 = instructionHandle;
            while (start != null) {
                Instruction instruction2 = instructionHandle17.getInstruction();
                Instruction instruction3 = start.getInstruction();
                if (instruction2 instanceof BranchInstruction) {
                    BranchInstruction branchInstruction = (BranchInstruction) instruction3;
                    BranchInstruction branchInstruction2 = (BranchInstruction) instruction2;
                    branchInstruction.setTarget((InstructionHandle) hashMap6.get(branchInstruction2.getTarget()));
                    if (branchInstruction2 instanceof Select) {
                        InstructionHandle[] targets = ((Select) branchInstruction2).getTargets();
                        InstructionHandle[] targets2 = ((Select) branchInstruction).getTargets();
                        for (int i6 = 0; i6 < targets.length; i6++) {
                            targets2[i6] = (InstructionHandle) hashMap6.get(targets[i6]);
                        }
                    }
                } else if ((instruction2 instanceof LocalVariableInstruction) || (instruction2 instanceof RET)) {
                    IndexedInstruction indexedInstruction = (IndexedInstruction) instruction3;
                    LocalVariableGen lookupRegisteredLocalVariable2 = getLocalVariableRegistry().lookupRegisteredLocalVariable(indexedInstruction.getIndex(), instructionHandle17.getPosition());
                    hashMap2 = hashMap11;
                    LocalVariableGen localVariableGen2 = (LocalVariableGen) hashMap2.get(lookupRegisteredLocalVariable2);
                    if (localVariableGen2 == null) {
                        hashMap = hashMap6;
                        instructionList = instructionList11;
                        methodGenerator = methodGenerator5;
                        LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable(lookupRegisteredLocalVariable2.getName(), lookupRegisteredLocalVariable2.getType(), null, null);
                        i = addLocalVariable2.getIndex();
                        hashMap2.put(lookupRegisteredLocalVariable2, addLocalVariable2);
                        hashMap10.put(lookupRegisteredLocalVariable2, instructionHandle12);
                        hashMap9.put(lookupRegisteredLocalVariable2, instructionHandle12);
                        instructionHandle5 = instructionHandle12;
                    } else {
                        hashMap = hashMap6;
                        instructionList = instructionList11;
                        instructionHandle5 = instructionHandle12;
                        methodGenerator = methodGenerator5;
                        i = localVariableGen2.getIndex();
                    }
                    indexedInstruction.setIndex(i);
                    if (instructionHandle17.hasTargeters()) {
                        InstructionTargeter[] targeters = instructionHandle17.getTargeters();
                        for (InstructionTargeter instructionTargeter : targeters) {
                            if ((instructionTargeter instanceof LocalVariableGen) && ((LocalVariableGen) instructionTargeter).getEnd() == instructionHandle17 && (obj = hashMap2.get(instructionTargeter)) != null) {
                                methodGenerator.removeLocalVariable((LocalVariableGen) obj);
                            }
                        }
                    }
                    if (instruction2 instanceof MarkerInstruction) {
                        start = start.getNext();
                    }
                    instructionHandle17 = instructionHandle17.getNext();
                    methodGenerator5 = methodGenerator;
                    instructionHandle12 = instructionHandle5;
                    hashMap11 = hashMap2;
                    instructionList11 = instructionList;
                    hashMap6 = hashMap;
                }
                hashMap = hashMap6;
                instructionList = instructionList11;
                instructionHandle5 = instructionHandle12;
                methodGenerator = methodGenerator5;
                hashMap2 = hashMap11;
                if (instructionHandle17.hasTargeters()) {
                }
                if (instruction2 instanceof MarkerInstruction) {
                }
                instructionHandle17 = instructionHandle17.getNext();
                methodGenerator5 = methodGenerator;
                instructionHandle12 = instructionHandle5;
                hashMap11 = hashMap2;
                instructionList11 = instructionList;
                hashMap6 = hashMap;
            }
            instructionList8.append(InstructionConstants.POP);
            for (Map.Entry entry : hashMap10.entrySet()) {
                ((LocalVariableGen) entry.getKey()).setStart((InstructionHandle) entry.getValue());
            }
            for (Map.Entry entry2 : hashMap9.entrySet()) {
                ((LocalVariableGen) entry2.getKey()).setEnd((InstructionHandle) entry2.getValue());
            }
            xsltc.dumpClass(r10.getJavaClass());
            InstructionList instructionList12 = getInstructionList();
            instructionList12.insert(instructionHandle, instructionList7);
            instructionList12.insert(instructionHandle, instructionList8);
            instructionList6.insert(instructionList9);
            instructionList6.append(instructionList11);
            instructionList6.append(InstructionConstants.RETURN);
            try {
                instructionList12.delete(instructionHandle, instructionHandle2);
            } catch (TargetLostException e) {
                InstructionHandle[] targets3 = e.getTargets();
                for (InstructionHandle instructionHandle18 : targets3) {
                    InstructionTargeter[] targeters2 = instructionHandle18.getTargeters();
                    int i7 = 0;
                    while (i7 < targeters2.length) {
                        if (targeters2[i7] instanceof LocalVariableGen) {
                            LocalVariableGen localVariableGen3 = (LocalVariableGen) targeters2[i7];
                            if (localVariableGen3.getStart() == instructionHandle18) {
                                localVariableGen3.setStart(instructionHandle12);
                            }
                            if (localVariableGen3.getEnd() == instructionHandle18) {
                                localVariableGen3.setEnd(instructionHandle12);
                            }
                            instructionHandle4 = instructionHandle11;
                        } else {
                            instructionHandle4 = instructionHandle11;
                            targeters2[i7].updateTarget(instructionHandle18, instructionHandle4);
                        }
                        i7++;
                        instructionHandle11 = instructionHandle4;
                    }
                }
            }
            for (String str3 : getExceptions()) {
                methodGenerator5.addException(str3);
            }
            return methodGenerator5.getThisMethod();
        }
        throw new InternalError(new ErrorMsg(ErrorMsg.OUTLINE_ERR_TRY_CATCH).toString());
    }

    private static Instruction loadLocal(int i, Type type) {
        if (type == Type.BOOLEAN) {
            return new ILOAD(i);
        }
        if (type == Type.INT) {
            return new ILOAD(i);
        }
        if (type == Type.SHORT) {
            return new ILOAD(i);
        }
        if (type == Type.LONG) {
            return new LLOAD(i);
        }
        if (type == Type.BYTE) {
            return new ILOAD(i);
        }
        if (type == Type.CHAR) {
            return new ILOAD(i);
        }
        if (type == Type.FLOAT) {
            return new FLOAD(i);
        }
        if (type == Type.DOUBLE) {
            return new DLOAD(i);
        }
        return new ALOAD(i);
    }

    private static Instruction storeLocal(int i, Type type) {
        if (type == Type.BOOLEAN) {
            return new ISTORE(i);
        }
        if (type == Type.INT) {
            return new ISTORE(i);
        }
        if (type == Type.SHORT) {
            return new ISTORE(i);
        }
        if (type == Type.LONG) {
            return new LSTORE(i);
        }
        if (type == Type.BYTE) {
            return new ISTORE(i);
        }
        if (type == Type.CHAR) {
            return new ISTORE(i);
        }
        if (type == Type.FLOAT) {
            return new FSTORE(i);
        }
        if (type == Type.DOUBLE) {
            return new DSTORE(i);
        }
        return new ASTORE(i);
    }

    public void markChunkStart() {
        getInstructionList().append(OutlineableChunkStart.OUTLINEABLECHUNKSTART);
        this.m_totalChunks++;
        this.m_openChunks++;
    }

    public void markChunkEnd() {
        getInstructionList().append(OutlineableChunkEnd.OUTLINEABLECHUNKEND);
        this.m_openChunks--;
        if (this.m_openChunks < 0) {
            throw new InternalError(new ErrorMsg(ErrorMsg.OUTLINE_ERR_UNBALANCED_MARKERS).toString());
        }
    }

    /* access modifiers changed from: package-private */
    public Method[] getGeneratedMethods(ClassGenerator classGenerator) {
        InstructionList instructionList = getInstructionList();
        InstructionHandle end = instructionList.getEnd();
        instructionList.setPositions();
        int position = end.getPosition() + end.getInstruction().getLength();
        if (position > MAX_BRANCH_TARGET_OFFSET && widenConditionalBranchTargetOffsets()) {
            instructionList.setPositions();
            InstructionHandle end2 = instructionList.getEnd();
            position = end2.getPosition() + end2.getInstruction().getLength();
        }
        return position > 65535 ? outlineChunks(classGenerator, position) : new Method[]{getThisMethod()};
    }

    /* access modifiers changed from: protected */
    public Method getThisMethod() {
        stripAttributes(true);
        setMaxLocals();
        setMaxStack();
        removeNOPs();
        return getMethod();
    }

    /* access modifiers changed from: package-private */
    public boolean widenConditionalBranchTargetOffsets() {
        InstructionList instructionList = getInstructionList();
        int i = 0;
        for (InstructionHandle start = instructionList.getStart(); start != null; start = start.getNext()) {
            short opcode = start.getInstruction().getOpcode();
            if (opcode == 170 || opcode == 171) {
                i += 3;
            } else {
                if (!(opcode == 198 || opcode == 199)) {
                    switch (opcode) {
                        case 167:
                        case 168:
                            i += 2;
                            break;
                    }
                }
                i += 5;
            }
        }
        BranchHandle start2 = instructionList.getStart();
        boolean z = false;
        while (start2 != null) {
            Instruction instruction = start2.getInstruction();
            if (instruction instanceof IfInstruction) {
                IfInstruction ifInstruction = (IfInstruction) instruction;
                BranchHandle branchHandle = (BranchHandle) start2;
                InstructionHandle target = ifInstruction.getTarget();
                int position = target.getPosition() - branchHandle.getPosition();
                if (position - i < MIN_BRANCH_TARGET_OFFSET || position + i > MAX_BRANCH_TARGET_OFFSET) {
                    InstructionHandle next = branchHandle.getNext();
                    BranchHandle append = instructionList.append((InstructionHandle) branchHandle, (BranchInstruction) ifInstruction.negate());
                    BranchHandle append2 = instructionList.append((InstructionHandle) append, (BranchInstruction) new GOTO(target));
                    if (next == null) {
                        next = instructionList.append(append2, NOP);
                    }
                    append.updateTarget(target, next);
                    if (branchHandle.hasTargeters()) {
                        InstructionTargeter[] targeters = branchHandle.getTargeters();
                        for (InstructionTargeter instructionTargeter : targeters) {
                            if (instructionTargeter instanceof LocalVariableGen) {
                                LocalVariableGen localVariableGen = (LocalVariableGen) instructionTargeter;
                                if (localVariableGen.getStart() == branchHandle) {
                                    localVariableGen.setStart(append);
                                } else if (localVariableGen.getEnd() == branchHandle) {
                                    localVariableGen.setEnd(append2);
                                }
                            } else {
                                instructionTargeter.updateTarget(branchHandle, append);
                            }
                        }
                    }
                    try {
                        instructionList.delete(branchHandle);
                        z = true;
                        start2 = append2;
                    } catch (TargetLostException e) {
                        throw new InternalError(new ErrorMsg(ErrorMsg.OUTLINE_ERR_DELETED_TARGET, e.getMessage()).toString());
                    }
                }
            }
            start2 = start2.getNext();
        }
        return z;
    }
}
