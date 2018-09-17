package com.huawei.android.pushselfshow.richpush.favorites;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.utils.a.d;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class a extends BaseAdapter {
    private Context a;
    private boolean b = true;
    private boolean c = false;
    private List d = new ArrayList();

    private static class a {
        ImageView a;
        TextView b;
        TextView c;
        CheckBox d;

        private a() {
        }
    }

    public a(Context context) {
        this.a = context;
    }

    /* renamed from: a */
    public e getItem(int i) {
        return (e) this.d.get(i);
    }

    public List a() {
        return this.d;
    }

    public void a(int i, e eVar) {
        try {
            if (this.d.size() >= i) {
                this.d.set(i, eVar);
            }
        } catch (Exception e) {
            c.d("PushSelfShowLog", e.toString());
        }
    }

    public void a(boolean z) {
        this.b = z;
        notifyDataSetChanged();
    }

    public void a(boolean z, Set set) {
        this.c = z;
        int i = 0;
        for (e eVar : this.d) {
            if (set != null && set.contains(Integer.valueOf(i))) {
                eVar.a(!z);
            } else {
                eVar.a(z);
            }
            int i2 = i + 1;
            a(i, eVar);
            i = i2;
        }
        notifyDataSetChanged();
    }

    public void b() {
        this.d = d.a(this.a, null);
    }

    public int getCount() {
        return this.d.size();
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        a aVar;
        Exception e;
        Exception exception;
        if (view != null) {
            try {
                aVar = (a) view.getTag();
            } catch (Exception e2) {
                e = e2;
                c.b("PushSelfShowLog", e.toString());
                exception = e;
                return view;
            }
        }
        a aVar2 = new a();
        try {
            view = ((LayoutInflater) this.a.getSystemService("layout_inflater")).inflate(com.huawei.android.pushselfshow.utils.d.c(this.a, "hwpush_collection_item"), null);
            aVar2.a = (ImageView) view.findViewById(com.huawei.android.pushselfshow.utils.d.e(this.a, "hwpush_favicon"));
            aVar2.b = (TextView) view.findViewById(com.huawei.android.pushselfshow.utils.d.e(this.a, "hwpush_selfshowmsg_title"));
            aVar2.c = (TextView) view.findViewById(com.huawei.android.pushselfshow.utils.d.e(this.a, "hwpush_selfshowmsg_content"));
            aVar2.d = (CheckBox) view.findViewById(com.huawei.android.pushselfshow.utils.d.e(this.a, "hwpush_delCheck"));
            view.setTag(aVar2);
            aVar = aVar2;
        } catch (Exception e3) {
            aVar = aVar2;
            e = e3;
            c.b("PushSelfShowLog", e.toString());
            exception = e;
            return view;
        }
        Bitmap d = ((e) this.d.get(i)).d();
        if (d == null) {
            d = BitmapFactory.decodeResource(this.a.getResources(), com.huawei.android.pushselfshow.utils.d.g(this.a, "hwpush_main_icon"));
        }
        aVar.a.setBackgroundDrawable(new BitmapDrawable(this.a.getResources(), d));
        CharSequence p = ((e) this.d.get(i)).b().p();
        if (p != null && p.length() > 0) {
            aVar.b.setText(p);
        }
        CharSequence n = ((e) this.d.get(i)).b().n();
        if (n != null) {
            if (n.length() > 0) {
                aVar.c.setText(n);
            }
        }
        if (this.b) {
            aVar.d.setVisibility(4);
        } else {
            aVar.d.setVisibility(0);
            if (this.c || ((e) this.d.get(i)).a()) {
                aVar.d.setChecked(true);
            } else {
                aVar.d.setChecked(false);
            }
        }
        return view;
    }
}
