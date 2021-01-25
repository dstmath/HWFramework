package ohos.com.sun.org.apache.xerces.internal.impl.validation;

import java.util.Vector;

public class ValidationManager {
    protected boolean fCachedDTD = false;
    protected boolean fGrammarFound = false;
    protected final Vector fVSs = new Vector();

    public final void addValidationState(ValidationState validationState) {
        this.fVSs.addElement(validationState);
    }

    public final void setEntityState(EntityState entityState) {
        for (int size = this.fVSs.size() - 1; size >= 0; size--) {
            ((ValidationState) this.fVSs.elementAt(size)).setEntityState(entityState);
        }
    }

    public final void setGrammarFound(boolean z) {
        this.fGrammarFound = z;
    }

    public final boolean isGrammarFound() {
        return this.fGrammarFound;
    }

    public final void setCachedDTD(boolean z) {
        this.fCachedDTD = z;
    }

    public final boolean isCachedDTD() {
        return this.fCachedDTD;
    }

    public final void reset() {
        this.fVSs.removeAllElements();
        this.fGrammarFound = false;
        this.fCachedDTD = false;
    }
}
