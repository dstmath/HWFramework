package com.android.server.biometrics.face;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.face.Face;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.server.biometrics.BiometricUtils;
import java.util.List;

public class FaceUtils implements BiometricUtils {
    private static FaceUtils sInstance;
    private static final Object sInstanceLock = new Object();
    @GuardedBy({"this"})
    private final SparseArray<FaceUserState> mUsers = new SparseArray<>();

    public static FaceUtils getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new FaceUtils();
            }
        }
        return sInstance;
    }

    private FaceUtils() {
    }

    @Override // com.android.server.biometrics.BiometricUtils
    public List<Face> getBiometricsForUser(Context ctx, int userId) {
        return getStateForUser(ctx, userId).getBiometrics();
    }

    @Override // com.android.server.biometrics.BiometricUtils
    public void addBiometricForUser(Context ctx, int userId, BiometricAuthenticator.Identifier identifier) {
        getStateForUser(ctx, userId).addBiometric(identifier);
    }

    @Override // com.android.server.biometrics.BiometricUtils
    public void removeBiometricForUser(Context ctx, int userId, int faceId) {
        getStateForUser(ctx, userId).removeBiometric(faceId);
    }

    @Override // com.android.server.biometrics.BiometricUtils
    public void renameBiometricForUser(Context ctx, int userId, int faceId, CharSequence name) {
        if (!TextUtils.isEmpty(name)) {
            getStateForUser(ctx, userId).renameBiometric(faceId, name);
        }
    }

    @Override // com.android.server.biometrics.BiometricUtils
    public CharSequence getUniqueName(Context context, int userId) {
        return getStateForUser(context, userId).getUniqueName();
    }

    private FaceUserState getStateForUser(Context ctx, int userId) {
        FaceUserState state;
        synchronized (this) {
            state = this.mUsers.get(userId);
            if (state == null) {
                state = new FaceUserState(ctx, userId);
                this.mUsers.put(userId, state);
            }
        }
        return state;
    }

    @Override // com.android.server.biometrics.BiometricUtils
    public boolean isDualFp() {
        return false;
    }

    @Override // com.android.server.biometrics.BiometricUtils
    public void setDualFp(boolean isDualFp) {
    }
}
