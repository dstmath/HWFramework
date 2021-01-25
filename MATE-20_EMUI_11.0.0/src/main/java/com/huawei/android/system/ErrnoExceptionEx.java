package com.huawei.android.system;

import android.system.ErrnoException;
import com.huawei.annotation.HwSystemApi;
import java.io.IOException;

@HwSystemApi
public class ErrnoExceptionEx {
    public static IOException rethrowAsIOException(ErrnoException exception) throws IOException {
        throw exception.rethrowAsIOException();
    }
}
