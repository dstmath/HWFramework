package ohos.abilityshell.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.android.internal.R;
import java.util.List;
import ohos.abilityshell.AbilityShellData;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.ApplicationInfo;
import ohos.hiviewdfx.HiLogLabel;

public class AbilityResolver extends AlertDialog {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "AbilityResolver");
    private List<AbilityShellData> mAbilityList;
    private DialogInterface mDialogInterface = this;
    private String[] mItemNames;
    private ListView mListView;
    private IResolveResult mListener;

    public interface IResolveResult {
        void onResolveResult(AbilityShellData abilityShellData);
    }

    public AbilityResolver(Context context, List<AbilityShellData> list, IResolveResult iResolveResult) {
        super(context);
        if (iResolveResult == null) {
            AppLog.e(LABEL, "input listener is null", new Object[0]);
            return;
        }
        this.mListener = iResolveResult;
        if (list == null || list.isEmpty()) {
            AppLog.e(LABEL, "input list is null or empty", new Object[0]);
            return;
        }
        initItemNames(list);
        this.mAbilityList = list;
        Context context2 = getContext();
        super.setCancelable(true);
        super.setTitle(context2.getString(17041528));
        createListView(context);
        super.setView(this.mListView);
        super.setButton(-2, context2.getString(17039360), new DialogInterface.OnClickListener() {
            /* class ohos.abilityshell.utils.AbilityResolver.AnonymousClass1 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                AbilityResolver.this.mListener.onResolveResult(null);
                dialogInterface.dismiss();
            }
        });
    }

    public AbilityResolver(Context context) {
        super(context);
    }

    @Override // android.app.Dialog, android.view.Window.Callback
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (!z) {
            super.onBackPressed();
        }
    }

    private void initItemNames(List<AbilityShellData> list) {
        this.mItemNames = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ApplicationInfo applicationInfo = list.get(i).getAbilityInfo().getApplicationInfo();
            String str = (applicationInfo == null || applicationInfo.getLabel().isEmpty()) ? "" : " : " + applicationInfo.getLabel();
            if (list.get(i).getDeviceName().isEmpty()) {
                this.mItemNames[i] = str + list.get(i).getAbilityInfo().getLabel();
            } else {
                this.mItemNames[i] = list.get(i).getDeviceName() + str + " : " + list.get(i).getAbilityInfo().getLabel();
            }
        }
    }

    private void createListView(Context context) {
        LayoutInflater from = LayoutInflater.from(context);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(null, R.styleable.AlertDialog, 16842845, 0);
        int resourceId = obtainStyledAttributes.getResourceId(15, 17367284);
        CheckedItemAdapter checkedItemAdapter = new CheckedItemAdapter(context, obtainStyledAttributes.getResourceId(14, 17367057), 16908308, this.mItemNames);
        View inflate = from.inflate(resourceId, (ViewGroup) null);
        if (inflate instanceof ListView) {
            this.mListView = (ListView) inflate;
            this.mListView.setAdapter((ListAdapter) checkedItemAdapter);
            this.mListView.setSelector(17170441);
            this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                /* class ohos.abilityshell.utils.AbilityResolver.AnonymousClass2 */

                @Override // android.widget.AdapterView.OnItemClickListener
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                    AbilityResolver.this.mListener.onResolveResult((AbilityShellData) AbilityResolver.this.mAbilityList.get(i));
                    AbilityResolver.this.mDialogInterface.dismiss();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public long getItemId(int i) {
            return (long) i;
        }

        @Override // android.widget.Adapter, android.widget.BaseAdapter
        public boolean hasStableIds() {
            return true;
        }

        public CheckedItemAdapter(Context context, int i, int i2, CharSequence[] charSequenceArr) {
            super(context, i, i2, charSequenceArr);
        }
    }
}
