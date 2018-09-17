package android.renderscript;

import android.renderscript.Script.FieldID;
import android.renderscript.Script.InvokeID;
import android.renderscript.Script.KernelID;
import android.telecom.Logging.Session;
import android.util.Log;
import android.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class ScriptGroup extends BaseObj {
    private static final String TAG = "ScriptGroup";
    private List<Closure> mClosures;
    IO[] mInputs;
    private List<Input> mInputs2;
    private String mName;
    IO[] mOutputs;
    private Future[] mOutputs2;

    public static final class Binding {
        private final FieldID mField;
        private final Object mValue;

        public Binding(FieldID field, Object value) {
            this.mField = field;
            this.mValue = value;
        }

        FieldID getField() {
            return this.mField;
        }

        Object getValue() {
            return this.mValue;
        }
    }

    public static final class Builder2 {
        private static final String TAG = "ScriptGroup.Builder2";
        List<Closure> mClosures = new ArrayList();
        List<Input> mInputs = new ArrayList();
        RenderScript mRS;

        public Builder2(RenderScript rs) {
            this.mRS = rs;
        }

        private Closure addKernelInternal(KernelID k, Type returnType, Object[] args, Map<FieldID, Object> globalBindings) {
            Closure c = new Closure(this.mRS, k, returnType, args, globalBindings);
            this.mClosures.add(c);
            return c;
        }

        private Closure addInvokeInternal(InvokeID invoke, Object[] args, Map<FieldID, Object> globalBindings) {
            Closure c = new Closure(this.mRS, invoke, args, globalBindings);
            this.mClosures.add(c);
            return c;
        }

        public Input addInput() {
            Input unbound = new Input();
            this.mInputs.add(unbound);
            return unbound;
        }

        public Closure addKernel(KernelID k, Type returnType, Object... argsAndBindings) {
            ArrayList<Object> args = new ArrayList();
            Map<FieldID, Object> bindingMap = new HashMap();
            if (seperateArgsAndBindings(argsAndBindings, args, bindingMap)) {
                return addKernelInternal(k, returnType, args.toArray(), bindingMap);
            }
            return null;
        }

        public Closure addInvoke(InvokeID invoke, Object... argsAndBindings) {
            ArrayList<Object> args = new ArrayList();
            Map<FieldID, Object> bindingMap = new HashMap();
            if (seperateArgsAndBindings(argsAndBindings, args, bindingMap)) {
                return addInvokeInternal(invoke, args.toArray(), bindingMap);
            }
            return null;
        }

        public ScriptGroup create(String name, Future... outputs) {
            if (name == null || name.isEmpty() || name.length() > 100 || (name.equals(name.replaceAll("[^a-zA-Z0-9-]", Session.SESSION_SEPARATION_CHAR_CHILD)) ^ 1) != 0) {
                throw new RSIllegalArgumentException("invalid script group name");
            }
            ScriptGroup ret = new ScriptGroup(this.mRS, name, this.mClosures, this.mInputs, outputs);
            this.mClosures = new ArrayList();
            this.mInputs = new ArrayList();
            return ret;
        }

        private boolean seperateArgsAndBindings(Object[] argsAndBindings, ArrayList<Object> args, Map<FieldID, Object> bindingMap) {
            int i = 0;
            while (i < argsAndBindings.length && !(argsAndBindings[i] instanceof Binding)) {
                args.add(argsAndBindings[i]);
                i++;
            }
            while (i < argsAndBindings.length) {
                if (!(argsAndBindings[i] instanceof Binding)) {
                    return false;
                }
                Binding b = argsAndBindings[i];
                bindingMap.put(b.getField(), b.getValue());
                i++;
            }
            return true;
        }
    }

    public static final class Builder {
        private int mKernelCount;
        private ArrayList<ConnectLine> mLines = new ArrayList();
        private ArrayList<Node> mNodes = new ArrayList();
        private RenderScript mRS;

        public Builder(RenderScript rs) {
            this.mRS = rs;
        }

        private void validateCycle(Node target, Node original) {
            for (int ct = 0; ct < target.mOutputs.size(); ct++) {
                Node tn;
                ConnectLine cl = (ConnectLine) target.mOutputs.get(ct);
                if (cl.mToK != null) {
                    tn = findNode(cl.mToK.mScript);
                    if (tn.equals(original)) {
                        throw new RSInvalidStateException("Loops in group not allowed.");
                    }
                    validateCycle(tn, original);
                }
                if (cl.mToF != null) {
                    tn = findNode(cl.mToF.mScript);
                    if (tn.equals(original)) {
                        throw new RSInvalidStateException("Loops in group not allowed.");
                    }
                    validateCycle(tn, original);
                }
            }
        }

        private void mergeDAGs(int valueUsed, int valueKilled) {
            for (int ct = 0; ct < this.mNodes.size(); ct++) {
                if (((Node) this.mNodes.get(ct)).dagNumber == valueKilled) {
                    ((Node) this.mNodes.get(ct)).dagNumber = valueUsed;
                }
            }
        }

        private void validateDAGRecurse(Node n, int dagNumber) {
            if (n.dagNumber == 0 || n.dagNumber == dagNumber) {
                n.dagNumber = dagNumber;
                for (int ct = 0; ct < n.mOutputs.size(); ct++) {
                    ConnectLine cl = (ConnectLine) n.mOutputs.get(ct);
                    if (cl.mToK != null) {
                        validateDAGRecurse(findNode(cl.mToK.mScript), dagNumber);
                    }
                    if (cl.mToF != null) {
                        validateDAGRecurse(findNode(cl.mToF.mScript), dagNumber);
                    }
                }
                return;
            }
            mergeDAGs(n.dagNumber, dagNumber);
        }

        private void validateDAG() {
            int ct;
            for (ct = 0; ct < this.mNodes.size(); ct++) {
                Node n = (Node) this.mNodes.get(ct);
                if (n.mInputs.size() == 0) {
                    if (n.mOutputs.size() != 0 || this.mNodes.size() <= 1) {
                        validateDAGRecurse(n, ct + 1);
                    } else {
                        throw new RSInvalidStateException("Groups cannot contain unconnected scripts");
                    }
                }
            }
            int dagNumber = ((Node) this.mNodes.get(0)).dagNumber;
            for (ct = 0; ct < this.mNodes.size(); ct++) {
                if (((Node) this.mNodes.get(ct)).dagNumber != dagNumber) {
                    throw new RSInvalidStateException("Multiple DAGs in group not allowed.");
                }
            }
        }

        private Node findNode(Script s) {
            for (int ct = 0; ct < this.mNodes.size(); ct++) {
                if (s == ((Node) this.mNodes.get(ct)).mScript) {
                    return (Node) this.mNodes.get(ct);
                }
            }
            return null;
        }

        private Node findNode(KernelID k) {
            for (int ct = 0; ct < this.mNodes.size(); ct++) {
                Node n = (Node) this.mNodes.get(ct);
                for (int ct2 = 0; ct2 < n.mKernels.size(); ct2++) {
                    if (k == n.mKernels.get(ct2)) {
                        return n;
                    }
                }
            }
            return null;
        }

        public Builder addKernel(KernelID k) {
            if (this.mLines.size() != 0) {
                throw new RSInvalidStateException("Kernels may not be added once connections exist.");
            } else if (findNode(k) != null) {
                return this;
            } else {
                this.mKernelCount++;
                Node n = findNode(k.mScript);
                if (n == null) {
                    n = new Node(k.mScript);
                    this.mNodes.add(n);
                }
                n.mKernels.add(k);
                return this;
            }
        }

        public Builder addConnection(Type t, KernelID from, FieldID to) {
            Node nf = findNode(from);
            if (nf == null) {
                throw new RSInvalidStateException("From script not found.");
            }
            Node nt = findNode(to.mScript);
            if (nt == null) {
                throw new RSInvalidStateException("To script not found.");
            }
            ConnectLine cl = new ConnectLine(t, from, to);
            this.mLines.add(new ConnectLine(t, from, to));
            nf.mOutputs.add(cl);
            nt.mInputs.add(cl);
            validateCycle(nf, nf);
            return this;
        }

        public Builder addConnection(Type t, KernelID from, KernelID to) {
            Node nf = findNode(from);
            if (nf == null) {
                throw new RSInvalidStateException("From script not found.");
            }
            Node nt = findNode(to);
            if (nt == null) {
                throw new RSInvalidStateException("To script not found.");
            }
            ConnectLine cl = new ConnectLine(t, from, to);
            this.mLines.add(new ConnectLine(t, from, to));
            nf.mOutputs.add(cl);
            nt.mInputs.add(cl);
            validateCycle(nf, nf);
            return this;
        }

        public ScriptGroup create() {
            if (this.mNodes.size() == 0) {
                throw new RSInvalidStateException("Empty script groups are not allowed");
            }
            int ct;
            for (ct = 0; ct < this.mNodes.size(); ct++) {
                ((Node) this.mNodes.get(ct)).dagNumber = 0;
            }
            validateDAG();
            ArrayList<IO> inputs = new ArrayList();
            ArrayList<IO> outputs = new ArrayList();
            long[] kernels = new long[this.mKernelCount];
            int idx = 0;
            for (ct = 0; ct < this.mNodes.size(); ct++) {
                Node n = (Node) this.mNodes.get(ct);
                int ct2 = 0;
                while (ct2 < n.mKernels.size()) {
                    int ct3;
                    KernelID kid = (KernelID) n.mKernels.get(ct2);
                    int idx2 = idx + 1;
                    kernels[idx] = kid.getID(this.mRS);
                    boolean hasInput = false;
                    boolean hasOutput = false;
                    for (ct3 = 0; ct3 < n.mInputs.size(); ct3++) {
                        if (((ConnectLine) n.mInputs.get(ct3)).mToK == kid) {
                            hasInput = true;
                        }
                    }
                    for (ct3 = 0; ct3 < n.mOutputs.size(); ct3++) {
                        if (((ConnectLine) n.mOutputs.get(ct3)).mFrom == kid) {
                            hasOutput = true;
                        }
                    }
                    if (!hasInput) {
                        inputs.add(new IO(kid));
                    }
                    if (!hasOutput) {
                        outputs.add(new IO(kid));
                    }
                    ct2++;
                    idx = idx2;
                }
            }
            if (idx != this.mKernelCount) {
                throw new RSRuntimeException("Count mismatch, should not happen.");
            }
            long[] src = new long[this.mLines.size()];
            long[] dstk = new long[this.mLines.size()];
            long[] dstf = new long[this.mLines.size()];
            long[] types = new long[this.mLines.size()];
            for (ct = 0; ct < this.mLines.size(); ct++) {
                ConnectLine cl = (ConnectLine) this.mLines.get(ct);
                src[ct] = cl.mFrom.getID(this.mRS);
                if (cl.mToK != null) {
                    dstk[ct] = cl.mToK.getID(this.mRS);
                }
                if (cl.mToF != null) {
                    dstf[ct] = cl.mToF.getID(this.mRS);
                }
                types[ct] = cl.mAllocationType.getID(this.mRS);
            }
            long id = this.mRS.nScriptGroupCreate(kernels, src, dstk, dstf, types);
            if (id == 0) {
                throw new RSRuntimeException("Object creation error, should not happen.");
            }
            ScriptGroup scriptGroup = new ScriptGroup(id, this.mRS);
            scriptGroup.mOutputs = new IO[outputs.size()];
            for (ct = 0; ct < outputs.size(); ct++) {
                scriptGroup.mOutputs[ct] = (IO) outputs.get(ct);
            }
            scriptGroup.mInputs = new IO[inputs.size()];
            for (ct = 0; ct < inputs.size(); ct++) {
                scriptGroup.mInputs[ct] = (IO) inputs.get(ct);
            }
            return scriptGroup;
        }
    }

    public static final class Closure extends BaseObj {
        private static final String TAG = "Closure";
        private Object[] mArgs;
        private Map<FieldID, Object> mBindings;
        private FieldPacker mFP;
        private Map<FieldID, Future> mGlobalFuture;
        private Future mReturnFuture;
        private Allocation mReturnValue;

        private static final class ValueAndSize {
            public int size;
            public long value;

            public ValueAndSize(RenderScript rs, Object obj) {
                if (obj instanceof Allocation) {
                    this.value = ((Allocation) obj).getID(rs);
                    this.size = -1;
                } else if (obj instanceof Boolean) {
                    this.value = (long) (((Boolean) obj).booleanValue() ? 1 : 0);
                    this.size = 4;
                } else if (obj instanceof Integer) {
                    this.value = ((Integer) obj).longValue();
                    this.size = 4;
                } else if (obj instanceof Long) {
                    this.value = ((Long) obj).longValue();
                    this.size = 8;
                } else if (obj instanceof Float) {
                    this.value = (long) Float.floatToRawIntBits(((Float) obj).floatValue());
                    this.size = 4;
                } else if (obj instanceof Double) {
                    this.value = Double.doubleToRawLongBits(((Double) obj).doubleValue());
                    this.size = 8;
                }
            }
        }

        Closure(long id, RenderScript rs) {
            super(id, rs);
        }

        Closure(RenderScript rs, KernelID kernelID, Type returnType, Object[] args, Map<FieldID, Object> globals) {
            super(0, rs);
            this.mArgs = args;
            this.mReturnValue = Allocation.createTyped(rs, returnType);
            this.mBindings = globals;
            this.mGlobalFuture = new HashMap();
            int numValues = args.length + globals.size();
            long[] fieldIDs = new long[numValues];
            long[] values = new long[numValues];
            int[] sizes = new int[numValues];
            long[] depClosures = new long[numValues];
            long[] depFieldIDs = new long[numValues];
            int i = 0;
            while (i < args.length) {
                fieldIDs[i] = 0;
                retrieveValueAndDependenceInfo(rs, i, null, args[i], values, sizes, depClosures, depFieldIDs);
                i++;
            }
            for (Entry<FieldID, Object> entry : globals.entrySet()) {
                Object obj = entry.getValue();
                FieldID fieldID = (FieldID) entry.getKey();
                fieldIDs[i] = fieldID.getID(rs);
                retrieveValueAndDependenceInfo(rs, i, fieldID, obj, values, sizes, depClosures, depFieldIDs);
                i++;
            }
            setID(rs.nClosureCreate(kernelID.getID(rs), this.mReturnValue.getID(rs), fieldIDs, values, sizes, depClosures, depFieldIDs));
            this.guard.open("destroy");
        }

        Closure(RenderScript rs, InvokeID invokeID, Object[] args, Map<FieldID, Object> globals) {
            super(0, rs);
            this.mFP = FieldPacker.createFromArray(args);
            this.mArgs = args;
            this.mBindings = globals;
            this.mGlobalFuture = new HashMap();
            int numValues = globals.size();
            long[] fieldIDs = new long[numValues];
            long[] values = new long[numValues];
            int[] sizes = new int[numValues];
            long[] depClosures = new long[numValues];
            long[] depFieldIDs = new long[numValues];
            int i = 0;
            for (Entry<FieldID, Object> entry : globals.entrySet()) {
                Object obj = entry.getValue();
                FieldID fieldID = (FieldID) entry.getKey();
                fieldIDs[i] = fieldID.getID(rs);
                retrieveValueAndDependenceInfo(rs, i, fieldID, obj, values, sizes, depClosures, depFieldIDs);
                i++;
            }
            setID(rs.nInvokeClosureCreate(invokeID.getID(rs), this.mFP.getData(), fieldIDs, values, sizes));
            this.guard.open("destroy");
        }

        public void destroy() {
            super.destroy();
            if (this.mReturnValue != null) {
                this.mReturnValue.destroy();
            }
        }

        protected void finalize() throws Throwable {
            this.mReturnValue = null;
            super.finalize();
        }

        private void retrieveValueAndDependenceInfo(RenderScript rs, int index, FieldID fid, Object obj, long[] values, int[] sizes, long[] depClosures, long[] depFieldIDs) {
            Input obj2;
            if (obj2 instanceof Future) {
                Future f = (Future) obj2;
                obj2 = f.getValue();
                depClosures[index] = f.getClosure().getID(rs);
                FieldID fieldID = f.getFieldID();
                depFieldIDs[index] = fieldID != null ? fieldID.getID(rs) : 0;
            } else {
                depClosures[index] = 0;
                depFieldIDs[index] = 0;
            }
            if (obj2 instanceof Input) {
                Input unbound = obj2;
                if (index < this.mArgs.length) {
                    unbound.addReference(this, index);
                } else {
                    unbound.addReference(this, fid);
                }
                values[index] = 0;
                sizes[index] = 0;
                return;
            }
            ValueAndSize vs = new ValueAndSize(rs, obj2);
            values[index] = vs.value;
            sizes[index] = vs.size;
        }

        public Future getReturn() {
            if (this.mReturnFuture == null) {
                this.mReturnFuture = new Future(this, null, this.mReturnValue);
            }
            return this.mReturnFuture;
        }

        public Future getGlobal(FieldID field) {
            Future f = (Future) this.mGlobalFuture.get(field);
            if (f != null) {
                return f;
            }
            Object obj = this.mBindings.get(field);
            if (obj instanceof Future) {
                obj = ((Future) obj).getValue();
            }
            f = new Future(this, field, obj);
            this.mGlobalFuture.put(field, f);
            return f;
        }

        void setArg(int index, Object obj) {
            if (obj instanceof Future) {
                obj = ((Future) obj).getValue();
            }
            this.mArgs[index] = obj;
            ValueAndSize vs = new ValueAndSize(this.mRS, obj);
            this.mRS.nClosureSetArg(getID(this.mRS), index, vs.value, vs.size);
        }

        void setGlobal(FieldID fieldID, Object obj) {
            if (obj instanceof Future) {
                obj = ((Future) obj).getValue();
            }
            this.mBindings.put(fieldID, obj);
            ValueAndSize vs = new ValueAndSize(this.mRS, obj);
            this.mRS.nClosureSetGlobal(getID(this.mRS), fieldID.getID(this.mRS), vs.value, vs.size);
        }
    }

    static class ConnectLine {
        Type mAllocationType;
        KernelID mFrom;
        FieldID mToF;
        KernelID mToK;

        ConnectLine(Type t, KernelID from, KernelID to) {
            this.mFrom = from;
            this.mToK = to;
            this.mAllocationType = t;
        }

        ConnectLine(Type t, KernelID from, FieldID to) {
            this.mFrom = from;
            this.mToF = to;
            this.mAllocationType = t;
        }
    }

    public static final class Future {
        Closure mClosure;
        FieldID mFieldID;
        Object mValue;

        Future(Closure closure, FieldID fieldID, Object value) {
            this.mClosure = closure;
            this.mFieldID = fieldID;
            this.mValue = value;
        }

        Closure getClosure() {
            return this.mClosure;
        }

        FieldID getFieldID() {
            return this.mFieldID;
        }

        Object getValue() {
            return this.mValue;
        }
    }

    static class IO {
        Allocation mAllocation;
        KernelID mKID;

        IO(KernelID s) {
            this.mKID = s;
        }
    }

    public static final class Input {
        List<Pair<Closure, Integer>> mArgIndex = new ArrayList();
        List<Pair<Closure, FieldID>> mFieldID = new ArrayList();
        Object mValue;

        Input() {
        }

        void addReference(Closure closure, int index) {
            this.mArgIndex.add(Pair.create(closure, Integer.valueOf(index)));
        }

        void addReference(Closure closure, FieldID fieldID) {
            this.mFieldID.add(Pair.create(closure, fieldID));
        }

        void set(Object value) {
            this.mValue = value;
            for (Pair<Closure, Integer> p : this.mArgIndex) {
                p.first.setArg(((Integer) p.second).intValue(), value);
            }
            for (Pair<Closure, FieldID> p2 : this.mFieldID) {
                ((Closure) p2.first).setGlobal(p2.second, value);
            }
        }

        Object get() {
            return this.mValue;
        }
    }

    static class Node {
        int dagNumber;
        ArrayList<ConnectLine> mInputs = new ArrayList();
        ArrayList<KernelID> mKernels = new ArrayList();
        Node mNext;
        ArrayList<ConnectLine> mOutputs = new ArrayList();
        Script mScript;

        Node(Script s) {
            this.mScript = s;
        }
    }

    ScriptGroup(long id, RenderScript rs) {
        super(id, rs);
        this.guard.open("destroy");
    }

    ScriptGroup(RenderScript rs, String name, List<Closure> closures, List<Input> inputs, Future[] outputs) {
        super(0, rs);
        this.mName = name;
        this.mClosures = closures;
        this.mInputs2 = inputs;
        this.mOutputs2 = outputs;
        long[] closureIDs = new long[closures.size()];
        for (int i = 0; i < closureIDs.length; i++) {
            closureIDs[i] = ((Closure) closures.get(i)).getID(rs);
        }
        setID(rs.nScriptGroup2Create(name, RenderScript.getCachePath(), closureIDs));
        this.guard.open("destroy");
    }

    public Object[] execute(Object... inputs) {
        if (inputs.length < this.mInputs2.size()) {
            Log.e(TAG, toString() + " receives " + inputs.length + " inputs, " + "less than expected " + this.mInputs2.size());
            return null;
        }
        int i;
        if (inputs.length > this.mInputs2.size()) {
            Log.i(TAG, toString() + " receives " + inputs.length + " inputs, " + "more than expected " + this.mInputs2.size());
        }
        for (i = 0; i < this.mInputs2.size(); i++) {
            Object obj = inputs[i];
            if ((obj instanceof Future) || (obj instanceof Input)) {
                Log.e(TAG, toString() + ": input " + i + " is a future or unbound value");
                return null;
            }
            ((Input) this.mInputs2.get(i)).set(obj);
        }
        this.mRS.nScriptGroup2Execute(getID(this.mRS));
        Object[] outputObjs = new Object[this.mOutputs2.length];
        Future[] futureArr = this.mOutputs2;
        int i2 = 0;
        int length = futureArr.length;
        int i3 = 0;
        while (i2 < length) {
            Object output = futureArr[i2].getValue();
            if (output instanceof Input) {
                output = ((Input) output).get();
            }
            i = i3 + 1;
            outputObjs[i3] = output;
            i2++;
            i3 = i;
        }
        return outputObjs;
    }

    public void setInput(KernelID s, Allocation a) {
        for (int ct = 0; ct < this.mInputs.length; ct++) {
            if (this.mInputs[ct].mKID == s) {
                this.mInputs[ct].mAllocation = a;
                this.mRS.nScriptGroupSetInput(getID(this.mRS), s.getID(this.mRS), this.mRS.safeID(a));
                return;
            }
        }
        throw new RSIllegalArgumentException("Script not found");
    }

    public void setOutput(KernelID s, Allocation a) {
        for (int ct = 0; ct < this.mOutputs.length; ct++) {
            if (this.mOutputs[ct].mKID == s) {
                this.mOutputs[ct].mAllocation = a;
                this.mRS.nScriptGroupSetOutput(getID(this.mRS), s.getID(this.mRS), this.mRS.safeID(a));
                return;
            }
        }
        throw new RSIllegalArgumentException("Script not found");
    }

    public void execute() {
        this.mRS.nScriptGroupExecute(getID(this.mRS));
    }

    public void destroy() {
        super.destroy();
        if (this.mClosures != null) {
            for (Closure c : this.mClosures) {
                c.destroy();
            }
        }
    }
}
