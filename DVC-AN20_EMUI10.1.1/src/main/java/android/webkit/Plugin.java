package android.webkit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

@Deprecated
public class Plugin {
    private String mDescription;
    private String mFileName;
    private PreferencesClickHandler mHandler = new DefaultClickHandler();
    private String mName;
    private String mPath;

    public interface PreferencesClickHandler {
        void handleClickEvent(Context context);
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
        PreferencesClickHandler preferencesClickHandler = this.mHandler;
        if (preferencesClickHandler != null) {
            preferencesClickHandler.handleClickEvent(context);
        }
    }

    @Deprecated
    private class DefaultClickHandler implements PreferencesClickHandler, DialogInterface.OnClickListener {
        private AlertDialog mDialog;

        private DefaultClickHandler() {
        }

        @Override // android.webkit.Plugin.PreferencesClickHandler
        @Deprecated
        public void handleClickEvent(Context context) {
            if (this.mDialog == null) {
                this.mDialog = new AlertDialog.Builder(context).setTitle(Plugin.this.mName).setMessage(Plugin.this.mDescription).setPositiveButton(17039370, this).setCancelable(false).show();
            }
        }

        @Override // android.content.DialogInterface.OnClickListener
        @Deprecated
        public void onClick(DialogInterface dialog, int which) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
    }
}
