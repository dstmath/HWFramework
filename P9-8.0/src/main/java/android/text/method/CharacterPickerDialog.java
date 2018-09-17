package android.text.method;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import com.android.internal.R;

public class CharacterPickerDialog extends Dialog implements OnItemClickListener, OnClickListener {
    private Button mCancelButton;
    private LayoutInflater mInflater;
    private boolean mInsert;
    private String mOptions;
    private Editable mText;
    private View mView;

    private class OptionsAdapter extends BaseAdapter {
        public OptionsAdapter(Context context) {
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Button b = (Button) CharacterPickerDialog.this.mInflater.inflate((int) R.layout.character_picker_button, null);
            b.setText(String.valueOf(CharacterPickerDialog.this.mOptions.charAt(position)));
            b.setOnClickListener(CharacterPickerDialog.this);
            return b;
        }

        public final int getCount() {
            return CharacterPickerDialog.this.mOptions.length();
        }

        public final Object getItem(int position) {
            return String.valueOf(CharacterPickerDialog.this.mOptions.charAt(position));
        }

        public final long getItemId(int position) {
            return (long) position;
        }
    }

    public CharacterPickerDialog(Context context, View view, Editable text, String options, boolean insert) {
        super(context, R.style.Theme_Panel);
        this.mView = view;
        this.mText = text;
        this.mOptions = options;
        this.mInsert = insert;
        this.mInflater = LayoutInflater.from(context);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutParams params = getWindow().getAttributes();
        params.token = this.mView.getApplicationWindowToken();
        params.type = 1003;
        params.flags |= 1;
        setContentView(R.layout.character_picker);
        GridView grid = (GridView) findViewById(R.id.characterPicker);
        grid.setAdapter(new OptionsAdapter(getContext()));
        grid.setOnItemClickListener(this);
        this.mCancelButton = (Button) findViewById(R.id.cancel);
        this.mCancelButton.setOnClickListener(this);
    }

    public void onItemClick(AdapterView parent, View view, int position, long id) {
        replaceCharacterAndClose(String.valueOf(this.mOptions.charAt(position)));
    }

    private void replaceCharacterAndClose(CharSequence replace) {
        int selEnd = Selection.getSelectionEnd(this.mText);
        if (this.mInsert || selEnd == 0) {
            this.mText.insert(selEnd, replace);
        } else {
            this.mText.replace(selEnd - 1, selEnd, replace);
        }
        dismiss();
    }

    public void onClick(View v) {
        if (v == this.mCancelButton) {
            dismiss();
        } else if (v instanceof Button) {
            replaceCharacterAndClose(((Button) v).getText());
        }
    }
}
