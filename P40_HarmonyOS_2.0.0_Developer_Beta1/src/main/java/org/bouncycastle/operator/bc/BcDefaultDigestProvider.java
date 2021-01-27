package org.bouncycastle.operator.bc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.digests.GOST3411Digest;
import org.bouncycastle.crypto.digests.GOST3411_2012_256Digest;
import org.bouncycastle.crypto.digests.GOST3411_2012_512Digest;
import org.bouncycastle.crypto.digests.MD2Digest;
import org.bouncycastle.crypto.digests.MD4Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.RIPEMD128Digest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.RIPEMD256Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA224Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.operator.OperatorCreationException;

public class BcDefaultDigestProvider implements BcDigestProvider {
    public static final BcDigestProvider INSTANCE = new BcDefaultDigestProvider();
    private static final Map lookup = createTable();

    private BcDefaultDigestProvider() {
    }

    private static Map createTable() {
        HashMap hashMap = new HashMap();
        hashMap.put(OIWObjectIdentifiers.idSHA1, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass1 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA1Digest();
            }
        });
        hashMap.put(NISTObjectIdentifiers.id_sha224, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass2 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA224Digest();
            }
        });
        hashMap.put(NISTObjectIdentifiers.id_sha256, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass3 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA256Digest();
            }
        });
        hashMap.put(NISTObjectIdentifiers.id_sha384, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass4 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA384Digest();
            }
        });
        hashMap.put(NISTObjectIdentifiers.id_sha512, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass5 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA512Digest();
            }
        });
        hashMap.put(NISTObjectIdentifiers.id_sha3_224, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass6 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA3Digest(224);
            }
        });
        hashMap.put(NISTObjectIdentifiers.id_sha3_256, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass7 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA3Digest(256);
            }
        });
        hashMap.put(NISTObjectIdentifiers.id_sha3_384, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass8 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA3Digest(384);
            }
        });
        hashMap.put(NISTObjectIdentifiers.id_sha3_512, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass9 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA3Digest(512);
            }
        });
        hashMap.put(PKCSObjectIdentifiers.md5, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass10 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new MD5Digest();
            }
        });
        hashMap.put(PKCSObjectIdentifiers.md4, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass11 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new MD4Digest();
            }
        });
        hashMap.put(PKCSObjectIdentifiers.md2, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass12 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new MD2Digest();
            }
        });
        hashMap.put(CryptoProObjectIdentifiers.gostR3411, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass13 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new GOST3411Digest();
            }
        });
        hashMap.put(RosstandartObjectIdentifiers.id_tc26_gost_3411_12_256, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass14 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new GOST3411_2012_256Digest();
            }
        });
        hashMap.put(RosstandartObjectIdentifiers.id_tc26_gost_3411_12_512, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass15 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new GOST3411_2012_512Digest();
            }
        });
        hashMap.put(TeleTrusTObjectIdentifiers.ripemd128, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass16 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new RIPEMD128Digest();
            }
        });
        hashMap.put(TeleTrusTObjectIdentifiers.ripemd160, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass17 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new RIPEMD160Digest();
            }
        });
        hashMap.put(TeleTrusTObjectIdentifiers.ripemd256, new BcDigestProvider() {
            /* class org.bouncycastle.operator.bc.BcDefaultDigestProvider.AnonymousClass18 */

            @Override // org.bouncycastle.operator.bc.BcDigestProvider
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new RIPEMD256Digest();
            }
        });
        return Collections.unmodifiableMap(hashMap);
    }

    @Override // org.bouncycastle.operator.bc.BcDigestProvider
    public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
        BcDigestProvider bcDigestProvider = (BcDigestProvider) lookup.get(algorithmIdentifier.getAlgorithm());
        if (bcDigestProvider != null) {
            return bcDigestProvider.get(algorithmIdentifier);
        }
        throw new OperatorCreationException("cannot recognise digest");
    }
}
