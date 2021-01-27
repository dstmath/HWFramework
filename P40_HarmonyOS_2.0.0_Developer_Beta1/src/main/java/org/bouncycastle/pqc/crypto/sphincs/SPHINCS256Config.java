package org.bouncycastle.pqc.crypto.sphincs;

class SPHINCS256Config {
    static final int CRYPTO_BYTES = 41000;
    static final int CRYPTO_PUBLICKEYBYTES = 1056;
    static final int CRYPTO_SECRETKEYBYTES = 1088;
    static final int HASH_BYTES = 32;
    static final int MESSAGE_HASH_SEED_BYTES = 32;
    static final int MSGHASH_BYTES = 64;
    static final int N_LEVELS = 12;
    static final int SEED_BYTES = 32;
    static final int SK_RAND_SEED_BYTES = 32;
    static final int SUBTREE_HEIGHT = 5;
    static final int TOTALTREE_HEIGHT = 60;

    SPHINCS256Config() {
    }
}
