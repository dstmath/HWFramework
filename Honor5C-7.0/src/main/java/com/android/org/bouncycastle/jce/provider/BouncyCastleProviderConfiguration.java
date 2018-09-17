package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import com.android.org.bouncycastle.jce.spec.ECParameterSpec;
import java.security.Permission;
import javax.crypto.spec.DHParameterSpec;

class BouncyCastleProviderConfiguration implements ProviderConfiguration {
    private static Permission BC_DH_LOCAL_PERMISSION;
    private static Permission BC_DH_PERMISSION;
    private static Permission BC_EC_LOCAL_PERMISSION;
    private static Permission BC_EC_PERMISSION;
    private volatile Object dhDefaultParams;
    private ThreadLocal dhThreadSpec;
    private volatile ECParameterSpec ecImplicitCaParams;
    private ThreadLocal ecThreadSpec;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.jce.provider.BouncyCastleProviderConfiguration.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.jce.provider.BouncyCastleProviderConfiguration.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.BouncyCastleProviderConfiguration.<clinit>():void");
    }

    BouncyCastleProviderConfiguration() {
        this.ecThreadSpec = new ThreadLocal();
        this.dhThreadSpec = new ThreadLocal();
    }

    void setParameter(String parameterName, Object parameter) {
        SecurityManager securityManager = System.getSecurityManager();
        if (parameterName.equals(ConfigurableProvider.THREAD_LOCAL_EC_IMPLICITLY_CA)) {
            ECParameterSpec curveSpec;
            if (securityManager != null) {
                securityManager.checkPermission(BC_EC_LOCAL_PERMISSION);
            }
            if ((parameter instanceof ECParameterSpec) || parameter == null) {
                curveSpec = (ECParameterSpec) parameter;
            } else {
                curveSpec = EC5Util.convertSpec((java.security.spec.ECParameterSpec) parameter, false);
            }
            if (curveSpec == null) {
                this.ecThreadSpec.remove();
            } else {
                this.ecThreadSpec.set(curveSpec);
            }
        } else if (parameterName.equals(ConfigurableProvider.EC_IMPLICITLY_CA)) {
            if (securityManager != null) {
                securityManager.checkPermission(BC_EC_PERMISSION);
            }
            if ((parameter instanceof ECParameterSpec) || parameter == null) {
                this.ecImplicitCaParams = (ECParameterSpec) parameter;
            } else {
                this.ecImplicitCaParams = EC5Util.convertSpec((java.security.spec.ECParameterSpec) parameter, false);
            }
        } else if (parameterName.equals(ConfigurableProvider.THREAD_LOCAL_DH_DEFAULT_PARAMS)) {
            if (securityManager != null) {
                securityManager.checkPermission(BC_DH_LOCAL_PERMISSION);
            }
            if ((parameter instanceof DHParameterSpec) || (parameter instanceof DHParameterSpec[]) || parameter == null) {
                Object dhSpec = parameter;
                if (parameter == null) {
                    this.dhThreadSpec.remove();
                    return;
                } else {
                    this.dhThreadSpec.set(parameter);
                    return;
                }
            }
            throw new IllegalArgumentException("not a valid DHParameterSpec");
        } else if (parameterName.equals(ConfigurableProvider.DH_DEFAULT_PARAMS)) {
            if (securityManager != null) {
                securityManager.checkPermission(BC_DH_PERMISSION);
            }
            if ((parameter instanceof DHParameterSpec) || (parameter instanceof DHParameterSpec[]) || parameter == null) {
                this.dhDefaultParams = parameter;
                return;
            }
            throw new IllegalArgumentException("not a valid DHParameterSpec or DHParameterSpec[]");
        }
    }

    public ECParameterSpec getEcImplicitlyCa() {
        ECParameterSpec spec = (ECParameterSpec) this.ecThreadSpec.get();
        if (spec != null) {
            return spec;
        }
        return this.ecImplicitCaParams;
    }

    public DHParameterSpec getDHDefaultParameters(int keySize) {
        DHParameterSpec params = this.dhThreadSpec.get();
        if (params == null) {
            params = this.dhDefaultParams;
        }
        if (params instanceof DHParameterSpec) {
            DHParameterSpec spec = params;
            if (spec.getP().bitLength() == keySize) {
                return spec;
            }
        } else if (params instanceof DHParameterSpec[]) {
            DHParameterSpec[] specs = (DHParameterSpec[]) params;
            for (int i = 0; i != specs.length; i++) {
                if (specs[i].getP().bitLength() == keySize) {
                    return specs[i];
                }
            }
        }
        return null;
    }
}
