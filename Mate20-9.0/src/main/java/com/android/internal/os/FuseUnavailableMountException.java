package com.android.internal.os;

public class FuseUnavailableMountException extends Exception {
    public FuseUnavailableMountException(int mountId) {
        super("AppFuse mount point " + mountId + " is unavailable");
    }
}
