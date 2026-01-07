package com.example.booklibrary;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.text.InputFilter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends BaseActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private BookViewModel viewModel;
    private FloatingActionButton fabAddBook;
    private static final int MAX_FILTER_LEN = 50;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(this).get(BookViewModel.class);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        fabAddBook = findViewById(R.id.fabAddBook);

        setupViewPager();
        setupFab();
        applyFabInsets();
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Книги");
            } else {
                tab.setText("Статистика");
            }
        }).attach();
    }

    private void setupFab() {
        fabAddBook.setOnClickListener(v ->
                startActivity(new Intent(this, BookDetailActivity.class))
        );
    }

    // Поднимаем FAB над системной навигацией (жестовая/кнопки)
    private void applyFabInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(fabAddBook, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int extraBottom = getResources().getDimensionPixelSize(R.dimen.fab_margin_bottom);
            // поднимаем FAB вверх на высоту навигационной панели + отступ
            v.setTranslationY(-systemBars.bottom - extraBottom);
            return insets;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();

        // сортировка
        if (id == R.id.menuSortTitle) {
            viewModel.sortByTitle();
            return true;
        }
        if (id == R.id.menuSortDate) {
            viewModel.sortByDate();
            return true;
        }

        // фильтры статуса
        if (id == R.id.menuFilterPlanned) {
            viewPager.setCurrentItem(0, true);
            viewModel.filterByStatus(Book.STATUS_PLANNED);
            return true;
        }
        if (id == R.id.menuFilterReading) {
            viewPager.setCurrentItem(0, true);
            viewModel.filterByStatus(Book.STATUS_READING);
            return true;
        }
        if (id == R.id.menuFilterRead) {
            viewPager.setCurrentItem(0, true);
            viewModel.filterByStatus(Book.STATUS_READ);
            return true;
        }

        if (id == R.id.menuFilterAuthor) {
            showAuthorFilterDialog();
            return true;
        }
        if (id == R.id.menuFilterGenre) {
            showGenreFilterDialog();
            return true;
        }

        if (id == R.id.menuClearFilter) {
            viewModel.clearFilter();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showYearGoalDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Количество книг в год");
        input.setText(String.valueOf(GoalPrefs.getYearGoal(this)));

        new AlertDialog.Builder(this)
                .setTitle("Цель на год")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!text.isEmpty()) {
                        try {
                            int goal = Integer.parseInt(text);
                            GoalPrefs.setYearGoal(this, goal);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showAuthorFilterDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_filter_author, null);
        EditText editAuthorFilter = dialogView.findViewById(R.id.editAuthorFilter);

        // Ограничение ввода
        editAuthorFilter.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_FILTER_LEN)
        });

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Отмена", (d, w) -> d.dismiss())
                        .create();

        dialog.setOnShowListener(d -> {
            android.widget.Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setTextColor(getResources().getColor(R.color.ocean_blue));

            btn.setOnClickListener(v -> {
                String author = editAuthorFilter.getText().toString().trim();

                if (author.isEmpty()) {
                    editAuthorFilter.setError("Введите автора");
                    return;
                }
                if (author.length() > MAX_FILTER_LEN) {
                    editAuthorFilter.setError("Макс. " + MAX_FILTER_LEN + " символов");
                    return;
                }

                viewModel.filterByAuthor(author);
                dialog.dismiss();
            });
        });

        dialog.show();
    }



    private void showGenreFilterDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_filter_genre, null);
        AutoCompleteTextView editGenreFilter =
                dialogView.findViewById(R.id.editGenreFilter);

        // Подключаем те же жанры, что и в BookDetailActivity
        String[] genres = getResources().getStringArray(R.array.book_genres_ru);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genres
        );

        editGenreFilter.setAdapter(adapter);
        editGenreFilter.setOnClickListener(v -> editGenreFilter.showDropDown());

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Отмена", (d, w) -> d.dismiss())
                        .create();

        dialog.setOnShowListener(d -> {
            android.widget.Button btn =
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            btn.setTextColor(getResources().getColor(R.color.ocean_blue));

            btn.setOnClickListener(v -> {
                String genre = editGenreFilter.getText().toString().trim();
                if (genre.isEmpty()) {
                    editGenreFilter.setError("Выберите жанр");
                    return;
                }
                viewModel.filterByGenre(genre);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

}
