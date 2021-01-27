package android.service.autofill;

import android.os.Parcelable;
import android.view.ViewGroup;

public abstract class InternalOnClickAction implements OnClickAction, Parcelable {
    public abstract void onClick(ViewGroup viewGroup);
}
