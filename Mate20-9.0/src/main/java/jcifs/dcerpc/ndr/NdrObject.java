package jcifs.dcerpc.ndr;

public abstract class NdrObject {
    public abstract void decode(NdrBuffer ndrBuffer) throws NdrException;

    public abstract void encode(NdrBuffer ndrBuffer) throws NdrException;
}
