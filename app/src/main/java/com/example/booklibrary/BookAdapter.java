package com.example.booklibrary;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> books;
    private OnBookClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public interface OnBookClickListener {
        void onBookClick(Book book);
        void onBookLongClick(Book book);
    }

    public BookAdapter(OnBookClickListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle, textAuthor, textStatus, textProgress, textGenre, textDateRead;
        private RatingBar ratingBar;
        private ProgressBar progressBar;
        private MaterialCardView cardView;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.textTitle);
            textAuthor = itemView.findViewById(R.id.textAuthor);
            textStatus = itemView.findViewById(R.id.textStatus);
            textGenre = itemView.findViewById(R.id.textGenre);
            textDateRead = itemView.findViewById(R.id.textDateRead);
            progressBar = itemView.findViewById(R.id.progressBar);
            textProgress = itemView.findViewById(R.id.textProgress);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            cardView = itemView.findViewById(R.id.card_book);  // MaterialCardView

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBookClick(books.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBookLongClick(books.get(position));
                    return true;
                }
                return false;
            });
        }

        public void bind(Book book) {
            Context context = itemView.getContext();

            // название и автор
            textTitle.setText(book.getTitle());
            textAuthor.setText(book.getAuthor());
            textGenre.setText(book.getGenre());

            // статус + цвет карточки
            String statusText = "";
            int statusColor = ContextCompat.getColor(context, R.color.light_gray);
            int cardColor = ContextCompat.getColor(context, R.color.paper_white);

            if (book.getStatus() != null) {
                switch (book.getStatus()) {
                    case Book.STATUS_PLANNED:
                        statusText = "В планах";
                        statusColor = ContextCompat.getColor(context, android.R.color.darker_gray);
                        cardColor = ContextCompat.getColor(context, R.color.card_planned);
                        break;
                    case Book.STATUS_READING:
                        statusText = "Читаю";
                        statusColor = ContextCompat.getColor(context, android.R.color.darker_gray);
                        cardColor = ContextCompat.getColor(context, R.color.card_reading);
                        break;
                    case Book.STATUS_READ:
                        statusText = "Прочитано";
                        statusColor = ContextCompat.getColor(context, android.R.color.darker_gray);
                        cardColor = ContextCompat.getColor(context, R.color.card_read);
                        break;
                }
            }


            textStatus.setText(statusText);
            textStatus.setTextColor(statusColor);

            textStatus.setTypeface(null, Typeface.BOLD);

            if (cardView != null) {
                cardView.setCardBackgroundColor(cardColor);
            }

            // рейтинг только для прочитанных
            if (book.isRead() && ratingBar != null) {
                ratingBar.setRating(book.getRating());
                ratingBar.setVisibility(View.VISIBLE);
            } else if (ratingBar != null) {
                ratingBar.setVisibility(View.GONE);
            }

            // ПРОГРЕСС только для "Читаю"
            if (Book.STATUS_READING.equals(book.getStatus())) {
                float progress = book.getProgress();
                if (progress > 0 && progressBar != null && textProgress != null) {
                    progressBar.setProgress((int) progress);
                    textProgress.setText(String.format(Locale.getDefault(), "%.0f%%", progress));
                    progressBar.setVisibility(View.VISIBLE);
                    textProgress.setVisibility(View.VISIBLE);
                } else {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (textProgress != null) textProgress.setVisibility(View.GONE);
                }
            } else {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (textProgress != null) textProgress.setVisibility(View.GONE);
            }

            // ДАТА ПРОЧТЕНИЯ только для прочитанных
            if (book.isRead() && book.getDateRead() != null && textDateRead != null) {
                textDateRead.setText(dateFormat.format(book.getDateRead()));
                textDateRead.setVisibility(View.VISIBLE);
            } else if (textDateRead != null) {
                textDateRead.setVisibility(View.GONE);
            }
        }
    }
}
