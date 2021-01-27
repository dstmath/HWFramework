package com.android.server.autofill;

import android.os.IInterface;
import android.service.autofill.augmented.IAugmentedAutofillService;
import com.android.internal.infra.AbstractRemoteService;

/* renamed from: com.android.server.autofill.-$$Lambda$RemoteAugmentedAutofillService$e7zSmzv77rBdYV5oCl-Y8EJj9dY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemoteAugmentedAutofillService$e7zSmzv77rBdYV5oClY8EJj9dY implements AbstractRemoteService.AsyncRequest {
    public static final /* synthetic */ $$Lambda$RemoteAugmentedAutofillService$e7zSmzv77rBdYV5oClY8EJj9dY INSTANCE = new $$Lambda$RemoteAugmentedAutofillService$e7zSmzv77rBdYV5oClY8EJj9dY();

    private /* synthetic */ $$Lambda$RemoteAugmentedAutofillService$e7zSmzv77rBdYV5oClY8EJj9dY() {
    }

    public final void run(IInterface iInterface) {
        ((IAugmentedAutofillService) iInterface).onDestroyAllFillWindowsRequest();
    }
}
