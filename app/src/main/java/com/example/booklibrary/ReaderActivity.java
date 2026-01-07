package com.example.booklibrary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReaderActivity extends AppCompatActivity {

    private static final String EXTRA_BOOK_ID = "bookid";

    private BookViewModel viewModel;
    private Book currentBook;

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    private ReaderBlocksAdapter adapter;

    private final ExecutorService parserExecutor = Executors.newSingleThreadExecutor();

    private String baseTitle;

    public static void start(Context context, int bookId) {
        Intent intent = new Intent(context, ReaderActivity.class);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        initViews();
        setupToolbar();
        setupRecycler();
        setupViewModel();

        int bookId = getIntent().getIntExtra(EXTRA_BOOK_ID, -1);
        if (bookId != -1) {
            loadBook(bookId);
        } else {
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewReader);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecycler() {
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ReaderBlocksAdapter();
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(null); // чуть меньше дерганий на больших текстах
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(BookViewModel.class);
    }

    private void loadBook(int bookId) {
        viewModel.getBookById(bookId).observe(this, book -> {
            if (book == null) {
                finish();
                return;
            }

            currentBook = book;

            baseTitle = (book.getTitle() == null || book.getTitle().trim().isEmpty())
                    ? "Читалка"
                    : book.getTitle().trim();

            toolbar.setTitle(baseTitle);

            String content = book.getContent();
            if (content == null || content.trim().isEmpty()) {
                adapter.setBlocks(new ArrayList<>());
                toolbar.setTitle(baseTitle + " (нет текста)");
                return;
            }

            // Парсим текст в блоки на фоне (чтобы не фризило UI)
            parserExecutor.execute(() -> {
                List<String> blocks = splitToBlocks(content);

                runOnUiThread(() -> {
                    adapter.setBlocks(blocks);
                    restoreReadingPosition();
                    updateToolbarPercent();
                });
            });
        });
    }

    private List<String> splitToBlocks(String text) {
        String normalized = text.replace("\r\n", "\n").replace("\r", "\n").trim();
        String[] parts = normalized.split("\\n{2,}"); // блоки разделяем пустыми строками

        List<String> blocks = new ArrayList<>(parts.length);
        for (String p : parts) {
            String block = p.trim();
            if (!block.isEmpty()) blocks.add(block);
        }

        // fallback: если вдруг нет пустых строк, режем по одной строке
        if (blocks.isEmpty()) {
            String[] lines = normalized.split("\\n");
            for (String line : lines) {
                String b = line.trim();
                if (!b.isEmpty()) blocks.add(b);
            }
        }

        return blocks;
    }

    private void restoreReadingPosition() {
        if (currentBook == null) return;

        final int pos = Math.max(0, Math.min(currentBook.getReaderPosition(), adapter.getBlocksCount() - 1));
        final int offset = Math.max(0, currentBook.getReaderOffset());

        recyclerView.post(() -> {
            if (adapter.getBlocksCount() <= 0) return;
            layoutManager.scrollToPositionWithOffset(pos, -offset);
        });
    }

    private void saveReadingPosition() {
        if (currentBook == null) return;
        if (layoutManager == null) return;
        if (adapter == null || adapter.getBlocksCount() == 0) return;

        int pos = layoutManager.findFirstVisibleItemPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        int offset = 0;
        RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(pos);
        if (vh != null && vh.itemView != null) {
            offset = Math.max(0, -vh.itemView.getTop());
        }

        currentBook.setReaderPosition(pos);
        currentBook.setReaderOffset(offset);

        // Процент для списка (грубо + поправка на offset внутри блока)
        int percent = computePercent(pos, offset, vh);
        currentBook.setReaderPercent(percent);

        viewModel.update(currentBook);
    }

    private int computePercent(int pos, int offset, RecyclerView.ViewHolder vh) {
        int n = adapter.getBlocksCount();
        if (n <= 1) return 0;

        float inside = 0f;
        if (vh != null && vh.itemView != null) {
            int h = vh.itemView.getHeight();
            if (h > 0) inside = Math.min(1f, offset / (float) h);
        }

        float progress = (pos + inside) / (float) (n - 1);
        int percent = Math.round(progress * 100f);

        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        return percent;
    }

    private void updateToolbarPercent() {
        if (currentBook == null) return;
        toolbar.setTitle(baseTitle + " (" + currentBook.getReaderPercent() + "%)");
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveReadingPosition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        parserExecutor.shutdown();
    }
}
