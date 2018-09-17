package android.renderscript;

import android.renderscript.Type.CubemapFace;

public class AllocationAdapter extends Allocation {
    Type mWindow;

    AllocationAdapter(long id, RenderScript rs, Allocation alloc, Type t) {
        super(id, rs, alloc.mType, alloc.mUsage);
        this.mAdaptedAllocation = alloc;
        this.mWindow = t;
    }

    void initLOD(int lod) {
        if (lod < 0) {
            throw new RSIllegalArgumentException("Attempting to set negative lod (" + lod + ").");
        }
        int tx = this.mAdaptedAllocation.mType.getX();
        int ty = this.mAdaptedAllocation.mType.getY();
        int tz = this.mAdaptedAllocation.mType.getZ();
        for (int ct = 0; ct < lod; ct++) {
            if (tx == 1 && ty == 1 && tz == 1) {
                throw new RSIllegalArgumentException("Attempting to set lod (" + lod + ") out of range.");
            }
            if (tx > 1) {
                tx >>= 1;
            }
            if (ty > 1) {
                ty >>= 1;
            }
            if (tz > 1) {
                tz >>= 1;
            }
        }
        this.mCurrentDimX = tx;
        this.mCurrentDimY = ty;
        this.mCurrentDimZ = tz;
        this.mCurrentCount = this.mCurrentDimX;
        if (this.mCurrentDimY > 1) {
            this.mCurrentCount *= this.mCurrentDimY;
        }
        if (this.mCurrentDimZ > 1) {
            this.mCurrentCount *= this.mCurrentDimZ;
        }
        this.mSelectedY = 0;
        this.mSelectedZ = 0;
    }

    private void updateOffsets() {
        int a1 = 0;
        int a2 = 0;
        int a3 = 0;
        int a4 = 0;
        if (this.mSelectedArray != null) {
            if (this.mSelectedArray.length > 0) {
                a1 = this.mSelectedArray[0];
            }
            if (this.mSelectedArray.length > 1) {
                a2 = this.mSelectedArray[2];
            }
            if (this.mSelectedArray.length > 2) {
                a3 = this.mSelectedArray[2];
            }
            if (this.mSelectedArray.length > 3) {
                a4 = this.mSelectedArray[3];
            }
        }
        this.mRS.nAllocationAdapterOffset(getID(this.mRS), this.mSelectedX, this.mSelectedY, this.mSelectedZ, this.mSelectedLOD, this.mSelectedFace.mID, a1, a2, a3, a4);
    }

    public void setLOD(int lod) {
        if (!this.mAdaptedAllocation.getType().hasMipmaps()) {
            throw new RSInvalidStateException("Cannot set LOD when the allocation type does not include mipmaps.");
        } else if (this.mWindow.hasMipmaps()) {
            throw new RSInvalidStateException("Cannot set LOD when the adapter includes mipmaps.");
        } else {
            initLOD(lod);
            this.mSelectedLOD = lod;
            updateOffsets();
        }
    }

    public void setFace(CubemapFace cf) {
        if (!this.mAdaptedAllocation.getType().hasFaces()) {
            throw new RSInvalidStateException("Cannot set Face when the allocation type does not include faces.");
        } else if (this.mWindow.hasFaces()) {
            throw new RSInvalidStateException("Cannot set face when the adapter includes faces.");
        } else if (cf == null) {
            throw new RSIllegalArgumentException("Cannot set null face.");
        } else {
            this.mSelectedFace = cf;
            updateOffsets();
        }
    }

    public void setX(int x) {
        if (this.mAdaptedAllocation.getType().getX() <= x) {
            throw new RSInvalidStateException("Cannot set X greater than dimension of allocation.");
        } else if (this.mWindow.getX() == this.mAdaptedAllocation.getType().getX()) {
            throw new RSInvalidStateException("Cannot set X when the adapter includes X.");
        } else if (this.mWindow.getX() + x >= this.mAdaptedAllocation.getType().getX()) {
            throw new RSInvalidStateException("Cannot set (X + window) which would be larger than dimension of allocation.");
        } else {
            this.mSelectedX = x;
            updateOffsets();
        }
    }

    public void setY(int y) {
        if (this.mAdaptedAllocation.getType().getY() == 0) {
            throw new RSInvalidStateException("Cannot set Y when the allocation type does not include Y dim.");
        } else if (this.mAdaptedAllocation.getType().getY() <= y) {
            throw new RSInvalidStateException("Cannot set Y greater than dimension of allocation.");
        } else if (this.mWindow.getY() == this.mAdaptedAllocation.getType().getY()) {
            throw new RSInvalidStateException("Cannot set Y when the adapter includes Y.");
        } else if (this.mWindow.getY() + y >= this.mAdaptedAllocation.getType().getY()) {
            throw new RSInvalidStateException("Cannot set (Y + window) which would be larger than dimension of allocation.");
        } else {
            this.mSelectedY = y;
            updateOffsets();
        }
    }

    public void setZ(int z) {
        if (this.mAdaptedAllocation.getType().getZ() == 0) {
            throw new RSInvalidStateException("Cannot set Z when the allocation type does not include Z dim.");
        } else if (this.mAdaptedAllocation.getType().getZ() <= z) {
            throw new RSInvalidStateException("Cannot set Z greater than dimension of allocation.");
        } else if (this.mWindow.getZ() == this.mAdaptedAllocation.getType().getZ()) {
            throw new RSInvalidStateException("Cannot set Z when the adapter includes Z.");
        } else if (this.mWindow.getZ() + z >= this.mAdaptedAllocation.getType().getZ()) {
            throw new RSInvalidStateException("Cannot set (Z + window) which would be larger than dimension of allocation.");
        } else {
            this.mSelectedZ = z;
            updateOffsets();
        }
    }

    public void setArray(int arrayNum, int arrayVal) {
        if (this.mAdaptedAllocation.getType().getArray(arrayNum) == 0) {
            throw new RSInvalidStateException("Cannot set arrayNum when the allocation type does not include arrayNum dim.");
        } else if (this.mAdaptedAllocation.getType().getArray(arrayNum) <= arrayVal) {
            throw new RSInvalidStateException("Cannot set arrayNum greater than dimension of allocation.");
        } else if (this.mWindow.getArray(arrayNum) == this.mAdaptedAllocation.getType().getArray(arrayNum)) {
            throw new RSInvalidStateException("Cannot set arrayNum when the adapter includes arrayNum.");
        } else if (this.mWindow.getArray(arrayNum) + arrayVal >= this.mAdaptedAllocation.getType().getArray(arrayNum)) {
            throw new RSInvalidStateException("Cannot set (arrayNum + window) which would be larger than dimension of allocation.");
        } else {
            this.mSelectedArray[arrayNum] = arrayVal;
            updateOffsets();
        }
    }

    public static AllocationAdapter create1D(RenderScript rs, Allocation a) {
        rs.validate();
        return createTyped(rs, a, Type.createX(rs, a.getElement(), a.getType().getX()));
    }

    public static AllocationAdapter create2D(RenderScript rs, Allocation a) {
        rs.validate();
        return createTyped(rs, a, Type.createXY(rs, a.getElement(), a.getType().getX(), a.getType().getY()));
    }

    public static AllocationAdapter createTyped(RenderScript rs, Allocation a, Type t) {
        rs.validate();
        if (a.mAdaptedAllocation != null) {
            throw new RSInvalidStateException("Adapters cannot be nested.");
        } else if (!a.getType().getElement().equals(t.getElement())) {
            throw new RSInvalidStateException("Element must match Allocation type.");
        } else if (t.hasFaces() || t.hasMipmaps()) {
            throw new RSInvalidStateException("Adapters do not support window types with Mipmaps or Faces.");
        } else {
            Type at = a.getType();
            if (t.getX() > at.getX() || t.getY() > at.getY() || t.getZ() > at.getZ() || t.getArrayCount() > at.getArrayCount()) {
                throw new RSInvalidStateException("Type cannot have dimension larger than the source allocation.");
            }
            if (t.getArrayCount() > 0) {
                for (int i = 0; i < t.getArray(i); i++) {
                    if (t.getArray(i) > at.getArray(i)) {
                        throw new RSInvalidStateException("Type cannot have dimension larger than the source allocation.");
                    }
                }
            }
            long id = rs.nAllocationAdapterCreate(a.getID(rs), t.getID(rs));
            if (id != 0) {
                return new AllocationAdapter(id, rs, a, t);
            }
            throw new RSRuntimeException("AllocationAdapter creation failed.");
        }
    }

    public synchronized void resize(int dimX) {
        throw new RSInvalidStateException("Resize not allowed for Adapters.");
    }
}
