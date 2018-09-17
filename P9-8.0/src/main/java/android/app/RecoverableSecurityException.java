package android.app;

import android.R;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import com.android.internal.util.Preconditions;

public final class RecoverableSecurityException extends SecurityException implements Parcelable {
    public static final Creator<RecoverableSecurityException> CREATOR = new Creator<RecoverableSecurityException>() {
        public RecoverableSecurityException createFromParcel(Parcel source) {
            return new RecoverableSecurityException(source);
        }

        public RecoverableSecurityException[] newArray(int size) {
            return new RecoverableSecurityException[size];
        }
    };
    private static final String TAG = "RecoverableSecurityException";
    private final RemoteAction mUserAction;
    private final CharSequence mUserMessage;

    public static class LocalDialog extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            RecoverableSecurityException e = (RecoverableSecurityException) getArguments().getParcelable(RecoverableSecurityException.TAG);
            return new Builder(getActivity()).setMessage(e.mUserMessage).setPositiveButton(e.mUserAction.getTitle(), new -$Lambda$CTFS8-1U0ymoTt8ccmb1hIh486s(e)).setNegativeButton((int) R.string.cancel, null).create();
        }

        static /* synthetic */ void lambda$-android_app_RecoverableSecurityException$LocalDialog_8830(RecoverableSecurityException e, DialogInterface dialog, int which) {
            try {
                e.mUserAction.getActionIntent().send();
            } catch (CanceledException e2) {
                Log.e(RecoverableSecurityException.TAG, "onCreateDialog()");
            }
        }
    }

    public RecoverableSecurityException(Parcel in) {
        this(new SecurityException(in.readString()), in.readCharSequence(), (RemoteAction) RemoteAction.CREATOR.createFromParcel(in));
    }

    public RecoverableSecurityException(Throwable cause, CharSequence userMessage, RemoteAction userAction) {
        super(cause.getMessage());
        this.mUserMessage = (CharSequence) Preconditions.checkNotNull(userMessage);
        this.mUserAction = (RemoteAction) Preconditions.checkNotNull(userAction);
    }

    @Deprecated
    public RecoverableSecurityException(Throwable cause, CharSequence userMessage, CharSequence userActionTitle, PendingIntent userAction) {
        this(cause, userMessage, new RemoteAction(Icon.createWithResource("android", 17302600), userActionTitle, userActionTitle, userAction));
    }

    public CharSequence getUserMessage() {
        return this.mUserMessage;
    }

    public RemoteAction getUserAction() {
        return this.mUserAction;
    }

    @Deprecated
    public void showAsNotification(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(NotificationManager.class);
        String channelId = "RecoverableSecurityException_" + this.mUserAction.getActionIntent().getCreatorUid();
        nm.createNotificationChannel(new NotificationChannel(channelId, TAG, 3));
        showAsNotification(context, channelId);
    }

    public void showAsNotification(Context context, String channelId) {
        ((NotificationManager) context.getSystemService(NotificationManager.class)).notify(TAG, this.mUserAction.getActionIntent().getCreatorUid(), new Notification.Builder(context, channelId).setSmallIcon(17302598).setContentTitle(this.mUserAction.getTitle()).setContentText(this.mUserMessage).setContentIntent(this.mUserAction.getActionIntent()).setCategory(Notification.CATEGORY_ERROR).build());
    }

    public void showAsDialog(Activity activity) {
        Fragment dialog = new LocalDialog();
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
        ft.add(dialog, tag);
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
