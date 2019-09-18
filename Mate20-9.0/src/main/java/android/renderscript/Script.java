package android.renderscript;

import android.util.SparseArray;
import java.io.UnsupportedEncodingException;

public class Script extends BaseObj {
    private final SparseArray<FieldID> mFIDs = new SparseArray<>();
    private final SparseArray<InvokeID> mIIDs = new SparseArray<>();
    long[] mInIdsBuffer = new long[1];
    private final SparseArray<KernelID> mKIDs = new SparseArray<>();

    public static class Builder {
        RenderScript mRS;

        Builder(RenderScript rs) {
            this.mRS = rs;
        }
    }

    public static class FieldBase {
        protected Allocation mAllocation;
        protected Element mElement;

        /* access modifiers changed from: protected */
        public void init(RenderScript rs, int dimx) {
            this.mAllocation = Allocation.createSized(rs, this.mElement, dimx, 1);
        }

        /* access modifiers changed from: protected */
        public void init(RenderScript rs, int dimx, int usages) {
            this.mAllocation = Allocation.createSized(rs, this.mElement, dimx, 1 | usages);
        }

        protected FieldBase() {
        }

        public Element getElement() {
            return this.mElement;
        }

        public Type getType() {
            return this.mAllocation.getType();
        }

        public Allocation getAllocation() {
            return this.mAllocation;
        }

        public void updateAllocation() {
        }
    }

    public static final class FieldID extends BaseObj {
        Script mScript;
        int mSlot;

        FieldID(long id, RenderScript rs, Script s, int slot) {
            super(id, rs);
            this.mScript = s;
            this.mSlot = slot;
        }
    }

    public static final class InvokeID extends BaseObj {
        Script mScript;
        int mSlot;

        InvokeID(long id, RenderScript rs, Script s, int slot) {
            super(id, rs);
            this.mScript = s;
            this.mSlot = slot;
        }
    }

    public static final class KernelID extends BaseObj {
        Script mScript;
        int mSig;
        int mSlot;

        KernelID(long id, RenderScript rs, Script s, int slot, int sig) {
            super(id, rs);
            this.mScript = s;
            this.mSlot = slot;
            this.mSig = sig;
        }
    }

    public static final class LaunchOptions {
        private int strategy;
        /* access modifiers changed from: private */
        public int xend = 0;
        /* access modifiers changed from: private */
        public int xstart = 0;
        /* access modifiers changed from: private */
        public int yend = 0;
        /* access modifiers changed from: private */
        public int ystart = 0;
        /* access modifiers changed from: private */
        public int zend = 0;
        /* access modifiers changed from: private */
        public int zstart = 0;

        public LaunchOptions setX(int xstartArg, int xendArg) {
            if (xstartArg < 0 || xendArg <= xstartArg) {
                throw new RSIllegalArgumentException("Invalid dimensions");
            }
            this.xstart = xstartArg;
            this.xend = xendArg;
            return this;
        }

        public LaunchOptions setY(int ystartArg, int yendArg) {
            if (ystartArg < 0 || yendArg <= ystartArg) {
                throw new RSIllegalArgumentException("Invalid dimensions");
            }
            this.ystart = ystartArg;
            this.yend = yendArg;
            return this;
        }

        public LaunchOptions setZ(int zstartArg, int zendArg) {
            if (zstartArg < 0 || zendArg <= zstartArg) {
                throw new RSIllegalArgumentException("Invalid dimensions");
            }
            this.zstart = zstartArg;
            this.zend = zendArg;
            return this;
        }

        public int getXStart() {
            return this.xstart;
        }

        public int getXEnd() {
            return this.xend;
        }

        public int getYStart() {
            return this.ystart;
        }

        public int getYEnd() {
            return this.yend;
        }

        public int getZStart() {
            return this.zstart;
        }

        public int getZEnd() {
            return this.zend;
        }
    }

    /* access modifiers changed from: protected */
    public KernelID createKernelID(int slot, int sig, Element ein, Element eout) {
        KernelID k = this.mKIDs.get(slot);
        if (k != null) {
            return k;
        }
        long id = this.mRS.nScriptKernelIDCreate(getID(this.mRS), slot, sig);
        if (id != 0) {
            KernelID kernelID = new KernelID(id, this.mRS, this, slot, sig);
            KernelID k2 = kernelID;
            this.mKIDs.put(slot, k2);
            return k2;
        }
        throw new RSDriverException("Failed to create KernelID");
    }

    /* access modifiers changed from: protected */
    public InvokeID createInvokeID(int slot) {
        InvokeID i = this.mIIDs.get(slot);
        if (i != null) {
            return i;
        }
        long id = this.mRS.nScriptInvokeIDCreate(getID(this.mRS), slot);
        if (id != 0) {
            InvokeID invokeID = new InvokeID(id, this.mRS, this, slot);
            InvokeID i2 = invokeID;
            this.mIIDs.put(slot, i2);
            return i2;
        }
        throw new RSDriverException("Failed to create KernelID");
    }

    /* access modifiers changed from: protected */
    public FieldID createFieldID(int slot, Element e) {
        FieldID f = this.mFIDs.get(slot);
        if (f != null) {
            return f;
        }
        long id = this.mRS.nScriptFieldIDCreate(getID(this.mRS), slot);
        if (id != 0) {
            FieldID fieldID = new FieldID(id, this.mRS, this, slot);
            FieldID f2 = fieldID;
            this.mFIDs.put(slot, f2);
            return f2;
        }
        throw new RSDriverException("Failed to create FieldID");
    }

    /* access modifiers changed from: protected */
    public void invoke(int slot) {
        this.mRS.nScriptInvoke(getID(this.mRS), slot);
    }

    /* access modifiers changed from: protected */
    public void invoke(int slot, FieldPacker v) {
        if (v != null) {
            this.mRS.nScriptInvokeV(getID(this.mRS), slot, v.getData());
        } else {
            this.mRS.nScriptInvoke(getID(this.mRS), slot);
        }
    }

    /* access modifiers changed from: protected */
    public void forEach(int slot, Allocation ain, Allocation aout, FieldPacker v) {
        forEach(slot, ain, aout, v, (LaunchOptions) null);
    }

    /* access modifiers changed from: protected */
    public void forEach(int slot, Allocation ain, Allocation aout, FieldPacker v, LaunchOptions sc) {
        Allocation allocation = ain;
        Allocation allocation2 = aout;
        this.mRS.validate();
        this.mRS.validateObject(allocation);
        this.mRS.validateObject(allocation2);
        if (allocation == null && allocation2 == null && sc == null) {
            throw new RSIllegalArgumentException("At least one of input allocation, output allocation, or LaunchOptions is required to be non-null.");
        }
        long[] in_ids = null;
        if (allocation != null) {
            in_ids = this.mInIdsBuffer;
            in_ids[0] = allocation.getID(this.mRS);
        }
        long out_id = 0;
        if (allocation2 != null) {
            out_id = allocation2.getID(this.mRS);
        }
        byte[] params = null;
        if (v != null) {
            params = v.getData();
        }
        byte[] params2 = params;
        int[] limits = null;
        if (sc != null) {
            limits = new int[]{sc.xstart, sc.xend, sc.ystart, sc.yend, sc.zstart, sc.zend};
        }
        this.mRS.nScriptForEach(getID(this.mRS), slot, in_ids, out_id, params2, limits);
    }

    /* access modifiers changed from: protected */
    public void forEach(int slot, Allocation[] ains, Allocation aout, FieldPacker v) {
        forEach(slot, ains, aout, v, (LaunchOptions) null);
    }

    /* access modifiers changed from: protected */
    public void forEach(int slot, Allocation[] ains, Allocation aout, FieldPacker v, LaunchOptions sc) {
        long[] in_ids;
        Allocation[] allocationArr = ains;
        Allocation allocation = aout;
        this.mRS.validate();
        if (allocationArr != null) {
            for (Allocation ain : allocationArr) {
                this.mRS.validateObject(ain);
            }
        }
        this.mRS.validateObject(allocation);
        if (allocationArr == null && allocation == null) {
            throw new RSIllegalArgumentException("At least one of ain or aout is required to be non-null.");
        }
        if (allocationArr != null) {
            in_ids = new long[allocationArr.length];
            for (int index = 0; index < allocationArr.length; index++) {
                in_ids[index] = allocationArr[index].getID(this.mRS);
            }
        } else {
            in_ids = null;
        }
        long[] in_ids2 = in_ids;
        long out_id = 0;
        if (allocation != null) {
            out_id = allocation.getID(this.mRS);
        }
        long out_id2 = out_id;
        byte[] params = null;
        if (v != null) {
            params = v.getData();
        }
        int[] limits = null;
        if (sc != null) {
            limits = new int[]{sc.xstart, sc.xend, sc.ystart, sc.yend, sc.zstart, sc.zend};
        }
        this.mRS.nScriptForEach(getID(this.mRS), slot, in_ids2, out_id2, params, limits);
    }

    /* access modifiers changed from: protected */
    public void reduce(int slot, Allocation[] ains, Allocation aout, LaunchOptions sc) {
        Allocation[] allocationArr = ains;
        Allocation allocation = aout;
        this.mRS.validate();
        if (allocationArr == null || allocationArr.length < 1) {
            throw new RSIllegalArgumentException("At least one input is required.");
        } else if (allocation != null) {
            for (Allocation ain : allocationArr) {
                this.mRS.validateObject(ain);
            }
            long[] in_ids = new long[allocationArr.length];
            for (int index = 0; index < allocationArr.length; index++) {
                in_ids[index] = allocationArr[index].getID(this.mRS);
            }
            long out_id = allocation.getID(this.mRS);
            int[] limits = null;
            if (sc != null) {
                limits = new int[]{sc.xstart, sc.xend, sc.ystart, sc.yend, sc.zstart, sc.zend};
            }
            this.mRS.nScriptReduce(getID(this.mRS), slot, in_ids, out_id, limits);
        } else {
            throw new RSIllegalArgumentException("aout is required to be non-null.");
        }
    }

    Script(long id, RenderScript rs) {
        super(id, rs);
        this.guard.open("destroy");
    }

    public void bindAllocation(Allocation va, int slot) {
        Allocation allocation = va;
        this.mRS.validate();
        this.mRS.validateObject(allocation);
        if (allocation != null) {
            if (this.mRS.getApplicationContext().getApplicationInfo().targetSdkVersion >= 20) {
                Type t = allocation.mType;
                if (t.hasMipmaps() || t.hasFaces() || t.getY() != 0 || t.getZ() != 0) {
                    throw new RSIllegalArgumentException("API 20+ only allows simple 1D allocations to be used with bind.");
                }
            }
            this.mRS.nScriptBindAllocation(getID(this.mRS), allocation.getID(this.mRS), slot);
            return;
        }
        this.mRS.nScriptBindAllocation(getID(this.mRS), 0, slot);
    }

    public void setVar(int index, float v) {
        this.mRS.nScriptSetVarF(getID(this.mRS), index, v);
    }

    public float getVarF(int index) {
        return this.mRS.nScriptGetVarF(getID(this.mRS), index);
    }

    public void setVar(int index, double v) {
        this.mRS.nScriptSetVarD(getID(this.mRS), index, v);
    }

    public double getVarD(int index) {
        return this.mRS.nScriptGetVarD(getID(this.mRS), index);
    }

    public void setVar(int index, int v) {
        this.mRS.nScriptSetVarI(getID(this.mRS), index, v);
    }

    public int getVarI(int index) {
        return this.mRS.nScriptGetVarI(getID(this.mRS), index);
    }

    public void setVar(int index, long v) {
        this.mRS.nScriptSetVarJ(getID(this.mRS), index, v);
    }

    public long getVarJ(int index) {
        return this.mRS.nScriptGetVarJ(getID(this.mRS), index);
    }

    public void setVar(int index, boolean v) {
        this.mRS.nScriptSetVarI(getID(this.mRS), index, v);
    }

    public boolean getVarB(int index) {
        return this.mRS.nScriptGetVarI(getID(this.mRS), index) > 0;
    }

    public void setVar(int index, BaseObj o) {
        this.mRS.validate();
        this.mRS.validateObject(o);
        this.mRS.nScriptSetVarObj(getID(this.mRS), index, o == null ? 0 : o.getID(this.mRS));
    }

    public void setVar(int index, FieldPacker v) {
        this.mRS.nScriptSetVarV(getID(this.mRS), index, v.getData());
    }

    public void setVar(int index, FieldPacker v, Element e, int[] dims) {
        this.mRS.nScriptSetVarVE(getID(this.mRS), index, v.getData(), e.getID(this.mRS), dims);
    }

    public void getVarV(int index, FieldPacker v) {
        this.mRS.nScriptGetVarV(getID(this.mRS), index, v.getData());
    }

    public void setTimeZone(String timeZone) {
        this.mRS.validate();
        try {
            this.mRS.nScriptSetTimeZone(getID(this.mRS), timeZone.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
