package test.gspann.storagescanner.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import test.gspann.storagescanner.R;
import test.gspann.storagescanner.results.ExtensionFrequency;

/**
 * Created by noah on 18/4/18.
 */

public class FrequentExtensionRecyclerViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    List<ExtensionFrequency> extenstions;

    public FrequentExtensionRecyclerViewAdapter(List<ExtensionFrequency> extensions) {
        this.extenstions = extensions;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ExtensionFrequency extensionFrequency = extenstions.get(position);
        holder.tvName.setText(extensionFrequency.getExtension());
        holder.tvValue.setText(String.valueOf(extensionFrequency.getFrequency()));
    }

    @Override
    public int getItemCount() {
        return extenstions.size();
    }
}
