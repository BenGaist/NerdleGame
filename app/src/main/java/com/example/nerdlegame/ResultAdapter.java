package com.example.nerdlegame;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying the list of game results in a RecyclerView.
 * Binds {@link Result} objects to view holders.
 */
public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
    private final List<Result> results;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    /**
     * Constructor for the ResultAdapter.
     * @param results The list of Result objects to display.
     */
    public ResultAdapter(List<Result> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Result result = results.get(position);
        holder.tvUsername.setText(result.username);
        holder.tvEquation.setText(result.equation);
        holder.tvTime.setText("Time: " + result.timeFormatted);

        String dateStr = dateFormat.format(new Date(result.timestamp));
        holder.tvDate.setText(dateStr);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    /**
     * ViewHolder class for caching view references.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEquation, tvTime, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEquation = itemView.findViewById(R.id.tvEquation);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
