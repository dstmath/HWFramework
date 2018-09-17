package jcifs.dcerpc.ndr;

import java.io.IOException;

public class NdrException extends IOException {
    public static final String INVALID_CONFORMANCE = "invalid array conformance";
    public static final String NO_NULL_REF = "ref pointer cannot be null";

    public NdrException(String msg) {
        super(msg);
    }
}
