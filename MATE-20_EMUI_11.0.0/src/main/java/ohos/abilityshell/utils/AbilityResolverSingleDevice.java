package ohos.abilityshell.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.R;
import java.util.List;
import ohos.abilityshell.AbilityShellData;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;

public class AbilityResolverSingleDevice extends AlertDialog {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "AbilityResolverSingleDevice");
    private List<AbilityShellData> mAbilityList;
    private DialogInterface mDialogInterface = this;
    private ListView mListView;
    private IResolveResult mListener;
    private Context mThemeContext;

    public interface IResolveResult {
        void onResolveResult(AbilityShellData abilityShellData);
    }

    public AbilityResolverSingleDevice(Context context, List<AbilityShellData> list, IResolveResult iResolveResult) {
        super(context, 33947691);
        if (iResolveResult == null) {
            AppLog.e(LABEL, "input listener is null", new Object[0]);
            return;
        }
        this.mListener = iResolveResult;
        if (list == null || list.isEmpty()) {
            AppLog.e(LABEL, "input list is null or empty", new Object[0]);
            return;
        }
        this.mAbilityList = list;
        initDisplayResolveInfo(this.mAbilityList);
        this.mThemeContext = getContext();
        super.setCancelable(true);
        super.setTitle(this.mThemeContext.getString(17041505));
        createListViewForCurDevice(context);
        super.setView(this.mListView);
        super.setButton(-2, this.mThemeContext.getString(17039360), new DialogInterface.OnClickListener() {
            /* class ohos.abilityshell.utils.AbilityResolverSingleDevice.AnonymousClass1 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                AbilityResolverSingleDevice.this.mListener.onResolveResult(null);
                dialogInterface.dismiss();
            }
        });
    }

    public void initDisplayResolveInfo(List<AbilityShellData> list) {
        for (AbilityShellData abilityShellData : list) {
            abilityShellData.setDisplayResolveInfo(new DisplayResolveInfo());
            if (abilityShellData.getDisplayResolveInfo() == null) {
                AppLog.e(LABEL, "getDisplayResolveInfo is null", new Object[0]);
                return;
            } else {
                abilityShellData.getDisplayResolveInfo().setResolveBundleName(abilityShellData.getAbilityInfo().getBundleName());
                abilityShellData.getDisplayResolveInfo().setLabel(abilityShellData.getAbilityInfo().getLabel());
            }
        }
    }

    public AbilityResolverSingleDevice(Context context) {
        super(context, 33947691);
    }

    @Override // android.app.Dialog, android.view.Window.Callback
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (!z) {
            super.onBackPressed();
        }
    }

    private void createListViewForCurDevice(Context context) {
        LayoutInflater from = LayoutInflater.from(context);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(null, R.styleable.AlertDialog, 16842845, 0);
        int resourceId = obtainStyledAttributes.getResourceId(15, 17367284);
        obtainStyledAttributes.getResourceId(14, 17367263);
        View inflate = from.inflate(resourceId, (ViewGroup) null);
        if (inflate instanceof ListView) {
            this.mListView = (ListView) inflate;
            this.mListView.setAdapter((ListAdapter) new ResolveListAdapter(context, this.mAbilityList));
            this.mListView.setSelector(17170441);
            this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                /* class ohos.abilityshell.utils.AbilityResolverSingleDevice.AnonymousClass2 */

                @Override // android.widget.AdapterView.OnItemClickListener
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                    AbilityResolverSingleDevice.this.mListener.onResolveResult((AbilityShellData) AbilityResolverSingleDevice.this.mAbilityList.get(i));
                    AbilityResolverSingleDevice.this.mDialogInterface.dismiss();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public class LoadIconTask extends AsyncTask<AbilityShellData, Void, AbilityShellData> {
        BaseAdapter mTaskInternalAdapter;

        LoadIconTask(BaseAdapter baseAdapter) {
            this.mTaskInternalAdapter = baseAdapter;
        }

        /* access modifiers changed from: protected */
        public AbilityShellData doInBackground(AbilityShellData... abilityShellDataArr) {
            AbilityShellData abilityShellData = abilityShellDataArr[0];
            Drawable drawable = null;
            if (AbilityResolverSingleDevice.this.mThemeContext == null) {
                return null;
            }
            try {
                drawable = AbilityResolverSingleDevice.this.mThemeContext.getPackageManager().getApplicationIcon(abilityShellData.getDisplayResolveInfo().getResolveBundleName());
            } catch (PackageManager.NameNotFoundException e) {
                AppLog.w("AbilityResolverSingleDevice load icon failed, error: %{public}s", e.getMessage());
            }
            abilityShellData.getDisplayResolveInfo().setDisplayIcon(drawable);
            return abilityShellData;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(AbilityShellData abilityShellData) {
            this.mTaskInternalAdapter.notifyDataSetChanged();
        }
    }

    /* access modifiers changed from: private */
    public class ResolveListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<AbilityShellData> mList;

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return (long) i;
        }

        ResolveListAdapter(Context context, List<AbilityShellData> list) {
            this.mList = list;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.mList.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            return this.mList.get(i);
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                ViewHolder viewHolder = new ViewHolder();
                View inflate = this.mInflater.inflate(17367263, (ViewGroup) null);
                if (inflate.findViewById(16908294) instanceof ImageView) {
                    viewHolder.imageView = (ImageView) inflate.findViewById(16908294);
                }
                if (inflate.findViewById(16908308) instanceof TextView) {
                    viewHolder.title = (TextView) inflate.findViewById(16908308);
                }
                inflate.setTag(viewHolder);
                view = inflate;
            } else if (view.getTag() instanceof ViewHolder) {
                ViewHolder viewHolder2 = (ViewHolder) view.getTag();
            }
            bindView(view, this.mList.get(i));
            return view;
        }

        private void bindView(View view, AbilityShellData abilityShellData) {
            if (view.getTag() instanceof ViewHolder) {
                ViewHolder viewHolder = (ViewHolder) view.getTag();
                viewHolder.title.setText(abilityShellData.getDisplayResolveInfo().getLabel());
                if (!abilityShellData.getDisplayResolveInfo().hasDisplayIcon()) {
                    new LoadIconTask(this).execute(abilityShellData);
                }
                viewHolder.imageView.setImageDrawable(abilityShellData.getDisplayResolveInfo().getDisplayIcon());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class ViewHolder {
        TextView content;
        ImageView imageView;
        TextView title;

        ViewHolder() {
        }
    }
}
