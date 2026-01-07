package com.example.booklibrary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;


import java.util.List;

public class BookViewModel extends AndroidViewModel {

    private final BookRepository repository;

    // Внешний источник для фрагмента (один на все случаи)
    private final MediatorLiveData<List<Book>> booksLiveData = new MediatorLiveData<>();

    // Текущий источник (зависит от фильтра/сортировки)
    private LiveData<List<Book>> currentSource;

    private LiveData<List<Book>> allBooksSortedByTitle;
    private LiveData<List<Book>> allBooksSortedByDate;

    private final MutableLiveData<String> currentFilter = new MutableLiveData<>("");
    private final MutableLiveData<String> currentSort = new MutableLiveData<>("title");

    public BookViewModel(@NonNull Application application) {
        super(application);
        repository = new BookRepository(application);

        allBooksSortedByTitle = repository.getAllBooksSortedByTitle();
        allBooksSortedByDate = repository.getAllBooksSortedByDate();

        // по умолчанию сортировка по названию
        switchSource(allBooksSortedByTitle);
    }

    // --- CRUD ---
    public void insert(Book book) { repository.insert(book); }
    public void update(Book book) { repository.update(book); }
    public void delete(Book book) { repository.delete(book); }

    // --- Данные для UI ---
    public LiveData<List<Book>> getAllBooks() {
        return booksLiveData;
    }

    public LiveData<Book> getBookById(int id) {
        return repository.getBookById(id);
    }

    // --- Фильтры / поиск ---
    public void filterByStatus(String status) {
        currentFilter.setValue(status);
        LiveData<List<Book>> source = repository.getBooksByStatus(status);
        switchSource(source);
    }

    public void filterByAuthor(String author) {
        currentFilter.setValue("author:" + author);
        LiveData<List<Book>> source = repository.getBooksByAuthorLike(author);
        switchSource(source);
    }

    public void filterByGenre(String genre) {
        currentFilter.setValue("genre:" + genre);
        LiveData<List<Book>> source = repository.getBooksByGenreLike(genre);
        switchSource(source);
    }

    public void search(String query) {
        currentFilter.setValue("search:" + query);
        LiveData<List<Book>> source = repository.searchBooks(query);
        switchSource(source);
    }

    public void clearFilter() {
        currentFilter.setValue("");
        if ("date".equals(currentSort.getValue())) {
            switchSource(allBooksSortedByDate);
        } else {
            switchSource(allBooksSortedByTitle);
        }
    }

    // --- Сортировка ---
    public void sortByTitle() {
        currentSort.setValue("title");
        allBooksSortedByTitle = repository.getAllBooksSortedByTitle();
        if (!hasFilter()) {
            switchSource(allBooksSortedByTitle);
        } else {
            applyCurrentFilter();
        }
    }

    public void sortByDate() {
        currentSort.setValue("date");
        allBooksSortedByDate = repository.getAllBooksSortedByDate();
        if (!hasFilter()) {
            switchSource(allBooksSortedByDate);
        } else {
            applyCurrentFilter();
        }
    }

    private boolean hasFilter() {
        String f = currentFilter.getValue();
        return f != null && !f.isEmpty();
    }

    private void applyCurrentFilter() {
        String filter = currentFilter.getValue();
        if (filter == null || filter.isEmpty()) return;

        if (filter.startsWith("author:")) {
            filterByAuthor(filter.substring("author:".length()));
        } else if (filter.startsWith("genre:")) {
            filterByGenre(filter.substring("genre:".length()));
        } else if (filter.startsWith("search:")) {
            search(filter.substring("search:".length()));
        } else {
            filterByStatus(filter);
        }
    }

    // --- Переключение источников ---
    private void switchSource(LiveData<List<Book>> newSource) {
        if (currentSource != null) {
            booksLiveData.removeSource(currentSource);
        }
        currentSource = newSource;
        booksLiveData.addSource(currentSource, booksLiveData::setValue);
    }

    // --- Статистика ---
    public LiveData<Integer> getReadBooksCountForYear(String year) {
        return repository.getReadBooksCountForYear(year);
    }

    public LiveData<Integer> getTotalReadBooks() {
        return repository.getTotalReadBooks();
    }

    public LiveData<Integer> getTotalReadingBooks() {
        return repository.getTotalReadingBooks();
    }

    public LiveData<Integer> getTotalPlannedBooks() {
        return repository.getTotalPlannedBooks();
    }

    public LiveData<Float> getAverageRating() {
        return repository.getAverageRating();
    }

    public LiveData<Integer> getTotalBooks() {
        return repository.getTotalBooks();
    }

    public LiveData<List<Book>> getRecentReadBooks(int limit) {
        return repository.getRecentReadBooks(limit);
    }

    @Override
    protected void onCleared() {
        repository.shutdown();
    }
}
