package jcifs.dcerpc;

import jcifs.dcerpc.ndr.NdrBuffer;

public interface DcerpcSecurityProvider {
    void unwrap(NdrBuffer ndrBuffer) throws DcerpcException;

    void wrap(NdrBuffer ndrBuffer) throws DcerpcException;
}
