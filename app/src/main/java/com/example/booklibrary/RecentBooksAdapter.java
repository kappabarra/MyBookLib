package com.example.booklibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecentBooksAdapter extends RecyclerView.Adapter<RecentBooksAdapter.BookViewHolder> {
    private List<Book> books = new ArrayList<>();

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        private TextView textBookTitle, textBookAuthor, textBookYear, textRatingValue;
        private RatingBar ratingBar;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            textBookTitle = itemView.findViewById(R.id.textBookTitle);
            textBookAuthor = itemView.findViewById(R.id.textBookAuthor);
            textBookYear = itemView.findViewById(R.id.textBookYear);
            textRatingValue = itemView.findViewById(R.id.textRatingValue);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }

        public void bind(Book book) {
            textBookTitle.setText(book.getTitle());
            textBookAuthor.setText(book.getAuthor());
            textBookYear.setText(String.valueOf(book.getYear()));

            if (book.getRating() > 0) {
                ratingBar.setRating(book.getRating());
                textRatingValue.setText(String.format("%.1f", book.getRating()));
                ratingBar.setVisibility(View.VISIBLE);
                textRatingValue.setVisibility(View.VISIBLE);
            } else {
                ratingBar.setVisibility(View.GONE);
                textRatingValue.setVisibility(View.GONE);
            }
        }
    }
}