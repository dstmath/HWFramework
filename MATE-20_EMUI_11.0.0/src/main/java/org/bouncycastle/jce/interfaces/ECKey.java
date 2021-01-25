package org.bouncycastle.jce.interfaces;

import org.bouncycastle.jce.spec.ECParameterSpec;

public interface ECKey {
    ECParameterSpec getParameters();
}
