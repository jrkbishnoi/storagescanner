package test.gspann.storagescanner.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import test.gspann.storagescanner.R;

/**
 * Created by noah on 18/4/18.
 */

public class ItemViewHolder extends RecyclerView.ViewHolder{
    public TextView tvName;
    public TextView tvValue;

    public ItemViewHolder(View view) {
        super(view);
        tvName = (TextView) view.findViewById(R.id.tvName);
        tvValue = (TextView) view.findViewById(R.id.tvValue);
    }
}
