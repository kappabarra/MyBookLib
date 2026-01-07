package com.example.booklibrary;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookDao {

    // CRUD
    @Insert
    long insert(Book book);

    @Update
    int update(Book book);

    @Delete
    int delete(Book book);

    // Списки (LiveData)
    @Query("SELECT * FROM books ORDER BY title ASC")
    LiveData<List<Book>> getAllBooksSortedByTitle();

    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    LiveData<List<Book>> getAllBooksSortedByDate();

    @Query("SELECT * FROM books WHERE status = :status ORDER BY title ASC")
    LiveData<List<Book>> getBooksByStatus(String status);

    // Поиск по нескольким полям
    @Query("SELECT * FROM books WHERE author LIKE :query OR title LIKE :query OR genre LIKE :query ORDER BY title ASC")
    LiveData<List<Book>> searchBooks(String query);

    // Одна книга
    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    LiveData<Book> getBookById(int id);

    // Статистика (LiveData)
    @Query("SELECT COUNT(*) FROM books WHERE status = 'read' AND strftime('%Y', dateRead / 1000, 'unixepoch') = :year")
    LiveData<Integer> getReadBooksCountForYear(String year);

    @Query("SELECT COUNT(*) FROM books WHERE status = 'read'")
    LiveData<Integer> getTotalReadBooks();

    @Query("SELECT COUNT(*) FROM books WHERE status = 'reading'")
    LiveData<Integer> getTotalReadingBooks();

    @Query("SELECT COUNT(*) FROM books WHERE status = 'planned'")
    LiveData<Integer> getTotalPlannedBooks();

    @Query("SELECT AVG(rating) FROM books WHERE status = 'read' AND rating > 0")
    LiveData<Float> getAverageRating();

    @Query("SELECT COUNT(*) FROM books")
    LiveData<Integer> getTotalBooks();

    @Query("SELECT * FROM books WHERE status = 'read' ORDER BY dateRead DESC LIMIT :limit")
    LiveData<List<Book>> getRecentReadBooks(int limit);

    @Query("SELECT * FROM books WHERE author LIKE '%' || :author || '%' ORDER BY title")
    LiveData<List<Book>> getBooksByAuthorLike(String author);

    @Query("SELECT * FROM books WHERE genre LIKE '%' || :genre || '%' ORDER BY title")
    LiveData<List<Book>> getBooksByGenreLike(String genre);
}
