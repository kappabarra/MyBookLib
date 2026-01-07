package com.example.booklibrary;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookRepository {

    private final BookDao bookDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BookRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        bookDao = db.bookDao();
    }

    // Запись в фоне
    public void insert(Book book) {
        executor.execute(() -> bookDao.insert(book));
    }

    public void update(Book book) {
        executor.execute(() -> bookDao.update(book));
    }

    public void delete(Book book) {
        executor.execute(() -> bookDao.delete(book));
    }

    // Чтение: просто отдаём LiveData из DAO

    public LiveData<List<Book>> getAllBooksSortedByTitle() {
        return bookDao.getAllBooksSortedByTitle();
    }

    public LiveData<List<Book>> getAllBooksSortedByDate() {
        return bookDao.getAllBooksSortedByDate();
    }

    public LiveData<List<Book>> getBooksByStatus(String status) {
        return bookDao.getBooksByStatus(status);
    }

    public LiveData<List<Book>> getBooksByAuthorLike(String author) {
        String q = "%" + author + "%";
        return bookDao.getBooksByAuthorLike(q);
    }

    public LiveData<List<Book>> getBooksByGenreLike(String genre) {
        String q = "%" + genre + "%";
        return bookDao.getBooksByGenreLike(q);
    }

    public LiveData<List<Book>> searchBooks(String query) {
        String wildcardQuery = "%" + query + "%";
        return bookDao.searchBooks(wildcardQuery);
    }

    public LiveData<Book> getBookById(int id) {
        return bookDao.getBookById(id);
    }

    // Статистика

    public LiveData<Integer> getReadBooksCountForYear(String year) {
        return bookDao.getReadBooksCountForYear(year);
    }

    public LiveData<Integer> getTotalReadBooks() {
        return bookDao.getTotalReadBooks();
    }

    public LiveData<Integer> getTotalReadingBooks() {
        return bookDao.getTotalReadingBooks();
    }

    public LiveData<Integer> getTotalPlannedBooks() {
        return bookDao.getTotalPlannedBooks();
    }

    public LiveData<Float> getAverageRating() {
        return bookDao.getAverageRating();
    }

    public LiveData<Integer> getTotalBooks() {
        return bookDao.getTotalBooks();
    }

    public LiveData<List<Book>> getRecentReadBooks(int limit) {
        return bookDao.getRecentReadBooks(limit);
    }

    public void shutdown() {
        executor.shutdown();
    }

}
