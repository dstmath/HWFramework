package java.security.interfaces;

import java.security.spec.ECParameterSpec;

public interface ECKey {
    ECParameterSpec getParams();
}
