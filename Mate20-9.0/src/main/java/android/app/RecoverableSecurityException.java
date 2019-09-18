package android.app;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.util.Preconditions;

public final class RecoverableSecurityException extends SecurityException implements Parcelable {
    public static final Parcelable.Creator<RecoverableSecurityException> CREATOR = new Parcelable.Creator<RecoverableSecurityException>() {
        public RecoverableSecurityException createFromParcel(Parcel source) {
            return new RecoverableSecurityException(source);
        }

        public RecoverableSecurityException[] newArray(int size) {
            return new RecoverableSecurityException[size];
        }
    };
    private static final String TAG = "RecoverableSecurityException";
    /* access modifiers changed from: private */
    public final RemoteAction mUserAction;
    /* access modifiers changed from: private */
    public final CharSequence mUserMessage;

    public static class LocalDialog extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            RecoverableSecurityException e = (RecoverableSecurityException) getArguments().getParcelable(RecoverableSecurityException.TAG);
            return new AlertDialog.Builder(getActivity()).setMessage(e.mUserMessage).setPositiveButton(e.mUserAction.getTitle(), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public final void onClick(DialogInterface dialogInterface, int i) {
                    RecoverableSecurityException.LocalDialog.lambda$onCreateDialog$0(RecoverableSecurityException.this, dialogInterface, i);
                }
            }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
        }

        static /* synthetic */ void lambda$onCreateDialog$0(RecoverableSecurityException e, DialogInterface dialog, int which) {
            try {
                e.mUserAction.getActionIntent().send();
            } catch (PendingIntent.CanceledException e2) {
                Log.e(RecoverableSecurityException.TAG, "onCreateDialog()");
            }
        }
    }

    public RecoverableSecurityException(Parcel in) {
        this(new SecurityException(in.readString()), in.readCharSequence(), RemoteAction.CREATOR.createFromParcel(in));
    }

    public RecoverableSecurityException(Throwable cause, CharSequence userMessage, RemoteAction userAction) {
        super(cause.getMessage());
        this.mUserMessage = (CharSequence) Preconditions.checkNotNull(userMessage);
        this.mUserAction = (RemoteAction) Preconditions.checkNotNull(userAction);
    }

    @Deprecated
    public RecoverableSecurityException(Throwable cause, CharSequence userMessage, CharSequence userActionTitle, PendingIntent userAction) {
        this(cause, userMessage, new RemoteAction(Icon.createWithResource("android", (int) R.drawable.ic_restart), userActionTitle, userActionTitle, userAction));
    }

    public CharSequence getUserMessage() {
        return this.mUserMessage;
    }

    public RemoteAction getUserAction() {
        return this.mUserAction;
    }

    @Deprecated
    public void showAsNotification(Context context) {
        String channelId = "RecoverableSecurityException_" + this.mUserAction.getActionIntent().getCreatorUid();
        ((NotificationManager) context.getSystemService(NotificationManager.class)).createNotificationChannel(new NotificationChannel(channelId, TAG, 3));
        showAsNotification(context, channelId);
    }

    public void showAsNotification(Context context, String channelId) {
        ((NotificationManager) context.getSystemService(NotificationManager.class)).notify(TAG, this.mUserAction.getActionIntent().getCreatorUid(), new Notification.Builder(context, channelId).setSmallIcon((int) R.drawable.ic_print_error).setContentTitle(this.mUserAction.getTitle()).setContentText(this.mUserMessage).setContentIntent(this.mUserAction.getActionIntent()).setCategory(Notification.CATEGORY_ERROR).build());
    }

    public void showAsDialog(Activity activity) {
        LocalDialog dialog = new LocalDialog();
        Bundle args = new Bundle();
        args.putParcelable(TAG, this);
        dialog.setArguments(args);
        String tag = "RecoverableSecurityException_" + this.mUserAction.getActionIntent().getCreatorUid();
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment old = fm.findFragmentByTag(tag);
        if (old != null) {
            ft.remove(old);
        }
        ft.add((Fragment) dialog, tag);
        ft.commitAllowingStateLoss();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getMessage());
        dest.writeCharSequence(this.mUserMessage);
        this.mUserAction.writeToParcel(dest, flags);
    }
}
