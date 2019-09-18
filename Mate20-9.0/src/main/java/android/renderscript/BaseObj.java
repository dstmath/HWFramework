package android.renderscript;

import dalvik.system.CloseGuard;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BaseObj {
    final CloseGuard guard = CloseGuard.get();
    private boolean mDestroyed;
    private long mID;
    private String mName;
    RenderScript mRS;

    BaseObj(long id, RenderScript rs) {
        rs.validate();
        this.mRS = rs;
        this.mID = id;
        this.mDestroyed = false;
    }

    /* access modifiers changed from: package-private */
    public void setID(long id) {
        if (this.mID == 0) {
            this.mID = id;
            return;
        }
        throw new RSRuntimeException("Internal Error, reset of object ID.");
    }

    /* access modifiers changed from: package-private */
    public long getID(RenderScript rs) {
        this.mRS.validate();
        if (this.mDestroyed) {
            throw new RSInvalidStateException("using a destroyed object.");
        } else if (this.mID == 0) {
            throw new RSRuntimeException("Internal error: Object id 0.");
        } else if (rs == null || rs == this.mRS) {
            return this.mID;
        } else {
            throw new RSInvalidStateException("using object with mismatched context.");
        }
    }

    /* access modifiers changed from: package-private */
    public void checkValid() {
        if (this.mID == 0) {
            throw new RSIllegalArgumentException("Invalid object.");
        }
    }

    public void setName(String name) {
        if (name == null) {
            throw new RSIllegalArgumentException("setName requires a string of non-zero length.");
        } else if (name.length() < 1) {
            throw new RSIllegalArgumentException("setName does not accept a zero length string.");
        } else if (this.mName == null) {
            try {
                this.mRS.nAssignName(this.mID, name.getBytes("UTF-8"));
                this.mName = name;
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RSIllegalArgumentException("setName object already has a name.");
        }
    }

    public String getName() {
        return this.mName;
    }

    private void helpDestroy() {
        boolean shouldDestroy = false;
        synchronized (this) {
            if (!this.mDestroyed) {
                shouldDestroy = true;
                this.mDestroyed = true;
            }
        }
        if (shouldDestroy) {
            this.guard.close();
            ReentrantReadWriteLock.ReadLock rlock = this.mRS.mRWLock.readLock();
            rlock.lock();
            if (this.mRS.isAlive() && this.mID != 0) {
                this.mRS.nObjDestroy(this.mID);
            }
            rlock.unlock();
            this.mRS = null;
            this.mID = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.guard != null) {
                this.guard.warnIfOpen();
            }
            helpDestroy();
        } finally {
            super.finalize();
        }
    }

    public void destroy() {
        if (!this.mDestroyed) {
            helpDestroy();
            return;
        }
        throw new RSInvalidStateException("Object already destroyed.");
    }

    /* access modifiers changed from: package-private */
    public void updateFromNative() {
        this.mRS.validate();
        this.mName = this.mRS.nGetName(getID(this.mRS));
    }

    public int hashCode() {
        return (int) ((this.mID & 268435455) ^ (this.mID >> 32));
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (this.mID != ((BaseObj) obj).mID) {
            z = false;
        }
        return z;
    }
}
