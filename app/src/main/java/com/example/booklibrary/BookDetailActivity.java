package com.example.booklibrary;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class BookDetailActivity extends BaseActivity {

    private BookViewModel viewModel;
    private EditText editTitle, editAuthor, editYear, editNotes;
    private AutoCompleteTextView editGenre;  // вместо EditText

    private RadioGroup radioGroupStatus;
    private RatingBar ratingBar;
    private Button buttonSave, buttonDelete;
    private Book currentBook;

    private static final int MAX_TITLE_LEN = 50;
    private static final int MAX_AUTHOR_LEN = 50;
    private static final int MAX_NOTES_LEN = 200;


    private Button buttonRead;
    private boolean isNewBook = true;

    public static void start(Context context, int bookId) {
        Intent intent = new Intent(context, BookDetailActivity.class);
        intent.putExtra("book_id", bookId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        setupViews();
        setupGenreDropdown();
        setupLoadFb2Button();
        viewModel = new ViewModelProvider(this).get(BookViewModel.class);

        int bookId = getIntent().getIntExtra("book_id", -1);
        if (bookId != -1) {
            loadBook(bookId);
            isNewBook = false;
        } else {
            currentBook = new Book();
            isNewBook = true;
            if (buttonDelete != null) buttonDelete.setVisibility(View.GONE);
        }

        setupListeners();
    }

    private void setupViews() {
        editTitle = findViewById(R.id.editTitle);
        editAuthor = findViewById(R.id.editAuthor);
        editGenre = findViewById(R.id.editGenre);
        editYear = findViewById(R.id.editYear);
        editNotes = findViewById(R.id.editNotes);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        ratingBar = findViewById(R.id.ratingBar);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonRead = findViewById(R.id.buttonRead);
    }

    private void setupLoadFb2Button() {
        findViewById(R.id.buttonLoadFb2).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            String[] mimeTypes = new String[]{"application/octet-stream", "text/xml"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(intent, 100);
        });
    }

    private void setupGenreDropdown() {
        String[] genresRu = getResources().getStringArray(R.array.book_genres_ru);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, genresRu);
        editGenre.setAdapter(adapter);
        editGenre.setKeyListener(null);  // Запрет ввода текста
        editGenre.setCursorVisible(false);
        editGenre.setFocusable(false);
        editGenre.setClickable(true);

        editGenre.setOnClickListener(v -> editGenre.showDropDown());
        editGenre.setOnItemClickListener((parent, view, position, id) -> {
            editGenre.dismissDropDown();
        });
    }


    private void loadBook(int bookId) {
        viewModel.getBookById(bookId).observe(this, book -> {
            if (book != null) {
                currentBook = book;
                populateFields();
            }
        });
    }

    private void populateFields() {
        if (currentBook == null) return;

        editTitle.setText(currentBook.getTitle());
        editAuthor.setText(currentBook.getAuthor());

        // Выбор жанра из списка по точному совпадению или первый элемент
        String genre = currentBook.getGenre();
        String[] genresRu = getResources().getStringArray(R.array.book_genres_ru);
        if (genre != null && !genre.isEmpty()) {
            // Поиск точного совпадения
            for (int i = 0; i < genresRu.length; i++) {
                if (genresRu[i].equals(genre)) {
                    editGenre.setText(genresRu[i], false);
                    editGenre.setTag(i);  // Сохраняем индекс для валидации
                    break;
                }
            }
        } else {
            // Если жанр пустой - первый элемент по умолчанию
            editGenre.setText(genresRu[0], false);
            editGenre.setTag(0);
        }

        editYear.setText(String.valueOf(currentBook.getYear()));
        editNotes.setText(currentBook.getNotes());
        ratingBar.setRating(currentBook.getRating());
        ratingBar.setIsIndicator(false);  // Разрешить изменение

        if (currentBook.getStatus() != null) {
            switch (currentBook.getStatus()) {
                case "planned":
                    radioGroupStatus.check(R.id.radioPlanned);
                    break;
                case "reading":
                    radioGroupStatus.check(R.id.radioReading);
                    break;
                case "read":
                    radioGroupStatus.check(R.id.radioRead);
                    break;
            }
        }
        updateRatingVisibility();

        if (currentBook != null && currentBook.getContent() != null
                && !currentBook.getContent().trim().isEmpty()) {
            buttonRead.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        radioGroupStatus.setOnCheckedChangeListener((group, checkedId) -> updateRatingVisibility());
        buttonSave.setOnClickListener(v -> saveBook());
        buttonDelete.setOnClickListener(v -> deleteBook());
        buttonRead.setOnClickListener(v -> {
            if (currentBook != null) {
                ReaderActivity.start(this, currentBook.getId());
            }
        });
    }

    private void updateRatingVisibility() {
        int checkedId = radioGroupStatus.getCheckedRadioButtonId();
        ratingBar.setVisibility(checkedId == R.id.radioRead ? View.VISIBLE : View.GONE);
    }


    private void saveBook() {
        if (editTitle.getText().toString().trim().isEmpty()) {
            editTitle.setError("Введите название");
            return;
        }

        String title = editTitle.getText().toString().trim();
        String author = editAuthor.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();

        if (title.isEmpty()) {
            editTitle.setError("Введите название");
            return;
        }

        if (title.length() > MAX_TITLE_LEN) {
            editTitle.setError("Макс. " + MAX_TITLE_LEN + " символов");
            return;
        }

        if (author.length() > MAX_AUTHOR_LEN) {
            editAuthor.setError("Макс. " + MAX_AUTHOR_LEN + " символов");
            return;
        }

        if (notes.length() > MAX_NOTES_LEN) {
            editNotes.setError("Макс. " + MAX_NOTES_LEN + " символов");
            return;
        }

        currentBook.setTitle(editTitle.getText().toString().trim());
        currentBook.setAuthor(editAuthor.getText().toString().trim());
        String selectedGenre = editGenre.getText().toString().trim();
        if (selectedGenre.isEmpty()) {
            editGenre.setError("Выберите жанр");
            return;
        }
        currentBook.setGenre(selectedGenre);

        try {
            currentBook.setYear(Integer.parseInt(editYear.getText().toString().trim()));
        } catch (Exception e) {
            currentBook.setYear(0);
        }

        currentBook.setNotes(editNotes.getText().toString().trim());
        currentBook.setRating(ratingBar.getRating());

        int checkedId = radioGroupStatus.getCheckedRadioButtonId();
        if (checkedId == R.id.radioPlanned) currentBook.setStatus("planned");
        else if (checkedId == R.id.radioReading) currentBook.setStatus("reading");
        else if (checkedId == R.id.radioRead) {
            currentBook.setStatus("read");
            if (currentBook.getDateRead() == null) {
                currentBook.setDateRead(new java.util.Date());
            }
        }

        if (isNewBook) {
            currentBook.setDateAdded(new java.util.Date());
            viewModel.insert(currentBook);
        } else {
            viewModel.update(currentBook);
        }
        finish();
    }

    private void deleteBook() {
        if (currentBook != null) {
            viewModel.delete(currentBook);
            finish();
        }
    }

    // --- обработка FB2 выбора файла ---

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            Fb2Parser parser = new Fb2Parser(this);
            ParsedBook parsed = parser.parseBook(uri);

            if (parsed != null) {
                if (currentBook == null) {
                    currentBook = new Book();
                    isNewBook = true;
                }

                if (parsed.title != null) currentBook.setTitle(parsed.title);
                if (parsed.author != null) currentBook.setAuthor(parsed.author);
                String mappedGenre = mapParsedGenreToKnown(parsed.genre);
                if (mappedGenre != null) {
                    currentBook.setGenre(mappedGenre);
                }


                int year = 0;
                if (parsed.dateString != null && parsed.dateString.length() >= 4) {
                    try {
                        year = Integer.parseInt(parsed.dateString.substring(0, 4));
                    } catch (NumberFormatException ignored) { }
                }
                currentBook.setYear(year);

                currentBook.setContent(parsed.fullText);

                populateFields();
                Toast.makeText(this, "Книга загружена из FB2", Toast.LENGTH_SHORT).show();

                // сразу сохраняем и закрываем карточку
                saveBookAndClose();
            } else {
                Toast.makeText(this, "Не удалось распознать FB2 файл", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String mapParsedGenreToKnown(String parsedGenre) {
        if (parsedGenre == null) return null;

        String normalized = parsedGenre.trim().toLowerCase();

        String[] genresEn = getResources().getStringArray(R.array.book_genres_en);
        String[] genresRu = getResources().getStringArray(R.array.book_genres_ru);

        for (int i = 0; i < genresEn.length; i++) {
            String en = genresEn[i].toLowerCase();
            String ru = genresRu[i].toLowerCase();

            if (normalized.contains(en) || en.contains(normalized)
                    || normalized.contains(ru) || ru.contains(normalized)) {
                return genresRu[i];  // возвращаем русский вариант для UI
            }
        }

        return "Другое";
    }



    private void saveBookAndClose() {
        // та же логика, что и в saveBook(), но в конце finish()

        if (editTitle.getText().toString().trim().isEmpty()) {
            editTitle.setError("Заполните название");
            return;
        }

        if (currentBook == null) {
            currentBook = new Book();
        }

        currentBook.setTitle(editTitle.getText().toString().trim());
        currentBook.setAuthor(editAuthor.getText().toString().trim());
        currentBook.setGenre(editGenre.getText().toString().trim());

        String yearStr = editYear.getText().toString().trim();
        int year = 0;
        if (!yearStr.isEmpty()) {
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException ignored) { }
        }
        currentBook.setYear(year);

        currentBook.setNotes(editNotes.getText().toString().trim());

        int checkedId = radioGroupStatus.getCheckedRadioButtonId();
        if (checkedId == R.id.radioPlanned) {
            currentBook.setStatus(Book.STATUS_PLANNED);
        } else if (checkedId == R.id.radioReading) {
            currentBook.setStatus(Book.STATUS_READING);
        } else if (checkedId == R.id.radioRead) {
            currentBook.setStatus(Book.STATUS_READ);
        }

        currentBook.setRating(ratingBar.getRating());

        if (isNewBook) {
            viewModel.insert(currentBook);
        } else {
            viewModel.update(currentBook);
        }

        // закрываем карточку и возвращаемся к списку
        setResult(RESULT_OK);
        finish();
    }

}
