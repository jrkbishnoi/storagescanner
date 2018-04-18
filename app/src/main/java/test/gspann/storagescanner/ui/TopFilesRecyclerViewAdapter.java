package test.gspann.storagescanner.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import test.gspann.storagescanner.R;
import test.gspann.storagescanner.Util;
import test.gspann.storagescanner.results.UserFile;

/**
 * Created by noah on 18/4/18.
 */

public class TopFilesRecyclerViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    List<UserFile> files;

    public TopFilesRecyclerViewAdapter(List<UserFile> files) {
        this.files = files;
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
        UserFile file = files.get(position);
        holder.tvName.setText(file.getName());
        holder.tvValue.setText(Util.getFileSizeString(file.getSize()));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }
}
