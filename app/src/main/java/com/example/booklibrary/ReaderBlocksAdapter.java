package com.example.booklibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReaderBlocksAdapter extends RecyclerView.Adapter<ReaderBlocksAdapter.BlockVH> {

    private final List<String> blocks = new ArrayList<>();

    public void setBlocks(List<String> newBlocks) {
        blocks.clear();
        if (newBlocks != null) blocks.addAll(newBlocks);
        notifyDataSetChanged();
    }

    public int getBlocksCount() {
        return blocks.size();
    }

    @NonNull
    @Override
    public BlockVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reader_block, parent, false);
        return new BlockVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockVH holder, int position) {
        holder.textBlock.setText(blocks.get(position));
    }

    @Override
    public int getItemCount() {
        return blocks.size();
    }

    static class BlockVH extends RecyclerView.ViewHolder {
        TextView textBlock;

        BlockVH(@NonNull View itemView) {
            super(itemView);
            textBlock = itemView.findViewById(R.id.textBlock);
        }
    }
}
