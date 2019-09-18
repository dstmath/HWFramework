package com.android.server.locksettings.recoverablekeystore.serialization;

class KeyChainSnapshotSchema {
    static final String CERTIFICATE_FACTORY_TYPE = "X.509";
    static final String CERT_PATH_ENCODING = "PkiPath";
    static final String NAMESPACE = null;
    static final String OUTPUT_ENCODING = "UTF-8";
    static final String TAG_ALGORITHM = "algorithm";
    static final String TAG_ALIAS = "alias";
    static final String TAG_APPLICATION_KEY = "applicationKey";
    static final String TAG_APPLICATION_KEYS = "applicationKeysList";
    static final String TAG_BACKEND_PUBLIC_KEY = "backendPublicKey";
    static final String TAG_COUNTER_ID = "counterId";
    static final String TAG_KEY_CHAIN_PROTECTION_PARAMS = "keyChainProtectionParams";
    static final String TAG_KEY_CHAIN_PROTECTION_PARAMS_LIST = "keyChainProtectionParamsList";
    static final String TAG_KEY_CHAIN_SNAPSHOT = "keyChainSnapshot";
    static final String TAG_KEY_DERIVATION_PARAMS = "keyDerivationParams";
    static final String TAG_KEY_MATERIAL = "keyMaterial";
    static final String TAG_LOCK_SCREEN_UI_TYPE = "lockScreenUiType";
    static final String TAG_MAX_ATTEMPTS = "maxAttempts";
    static final String TAG_MEMORY_DIFFICULTY = "memoryDifficulty";
    static final String TAG_RECOVERY_KEY_MATERIAL = "recoveryKeyMaterial";
    static final String TAG_SALT = "salt";
    static final String TAG_SERVER_PARAMS = "serverParams";
    static final String TAG_SNAPSHOT_VERSION = "snapshotVersion";
    static final String TAG_TRUSTED_HARDWARE_CERT_PATH = "thmCertPath";
    static final String TAG_USER_SECRET_TYPE = "userSecretType";

    private KeyChainSnapshotSchema() {
    }
}
