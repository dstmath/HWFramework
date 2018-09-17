package android.webkit;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.android.internal.R;

@Deprecated
public class Plugin {
    private String mDescription;
    private String mFileName;
    private PreferencesClickHandler mHandler = new DefaultClickHandler(this, null);
    private String mName;
    private String mPath;

    public interface PreferencesClickHandler {
        void handleClickEvent(Context context);
    }

    @Deprecated
    private class DefaultClickHandler implements PreferencesClickHandler, OnClickListener {
        private AlertDialog mDialog;

        /* synthetic */ DefaultClickHandler(Plugin this$0, DefaultClickHandler -this1) {
            this();
        }

        private DefaultClickHandler() {
        }

        @Deprecated
        public void handleClickEvent(Context context) {
            if (this.mDialog == null) {
                this.mDialog = new Builder(context).setTitle(Plugin.this.mName).setMessage(Plugin.this.mDescription).setPositiveButton(R.string.ok, this).setCancelable(false).show();
            }
        }

        @Deprecated
        public void onClick(DialogInterface dialog, int which) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
    }

    @Deprecated
    public Plugin(String name, String path, String fileName, String description) {
        this.mName = name;
        this.mPath = path;
        this.mFileName = fileName;
        this.mDescription = description;
    }

    @Deprecated
    public String toString() {
        return this.mName;
    }

    @Deprecated
    public String getName() {
        return this.mName;
    }

    @Deprecated
    public String getPath() {
        return this.mPath;
    }

    @Deprecated
    public String getFileName() {
        return this.mFileName;
    }

    @Deprecated
    public String getDescription() {
        return this.mDescription;
    }

    @Deprecated
    public void setName(String name) {
        this.mName = name;
    }

    @Deprecated
    public void setPath(String path) {
        this.mPath = path;
    }

    @Deprecated
    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    @Deprecated
    public void setDescription(String description) {
        this.mDescription = description;
    }

    @Deprecated
    public void setClickHandler(PreferencesClickHandler handler) {
        this.mHandler = handler;
    }

    @Deprecated
    public void dispatchClickEvent(Context context) {
        if (this.mHandler != null) {
            this.mHandler.handleClickEvent(context);
        }
    }
}
