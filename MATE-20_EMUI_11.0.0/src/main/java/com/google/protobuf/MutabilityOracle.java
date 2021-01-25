package com.google.protobuf;

interface MutabilityOracle {
    public static final MutabilityOracle IMMUTABLE = new MutabilityOracle() {
        /* class com.google.protobuf.MutabilityOracle.AnonymousClass1 */

        @Override // com.google.protobuf.MutabilityOracle
        public void ensureMutable() {
            throw new UnsupportedOperationException();
        }
    };

    void ensureMutable();
}
