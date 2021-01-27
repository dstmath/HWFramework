package com.android.framework.protobuf;

interface MutabilityOracle {
    public static final MutabilityOracle IMMUTABLE = new MutabilityOracle() {
        /* class com.android.framework.protobuf.MutabilityOracle.AnonymousClass1 */

        @Override // com.android.framework.protobuf.MutabilityOracle
        public void ensureMutable() {
            throw new UnsupportedOperationException();
        }
    };

    void ensureMutable();
}
