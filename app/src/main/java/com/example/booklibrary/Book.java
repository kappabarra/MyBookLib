package com.example.booklibrary;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "books")
public class Book {

    public static final String STATUS_PLANNED = "planned";
    public static final String STATUS_READING = "reading";
    public static final String STATUS_READ = "read";

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String author;
    private String genre;
    private int year;
    private String status;
    private String notes;
    private float rating;
    private Date dateAdded;
    private Date dateRead;
    private String content;

    // Старое (постраничное) — оставляем для совместимости
    private int lastPage;
    private int totalPages = 0;

    // Новое — прогресс прокрутки (0..100)
    private int scrollPercent = 0;

    private int readerPosition = 0;   // индекс первого видимого блока
    private int readerOffset = 0;     // смещение в px внутри блока (сколько прокрутили вниз)
    private int readerPercent = 0;    // 0..100 (чтобы быстро показывать прогресс в списке)

    public Book() {
        this.dateAdded = new Date();
        this.status = STATUS_PLANNED;
        this.rating = 0.0f;
        this.lastPage = 0;
        this.totalPages = 0;
        this.scrollPercent = 0;
    }

    @Ignore
    public Book(String title, String author, String genre, int year) {
        this();
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre != null ? genre : ""; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public Date getDateAdded() { return dateAdded; }
    public void setDateAdded(Date dateAdded) { this.dateAdded = dateAdded; }

    public Date getDateRead() { return dateRead; }
    public void setDateRead(Date dateRead) { this.dateRead = dateRead; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getLastPage() { return lastPage; }
    public void setLastPage(int lastPage) { this.lastPage = lastPage; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getScrollPercent() { return scrollPercent; }
    public void setScrollPercent(int scrollPercent) {
        if (scrollPercent < 0) scrollPercent = 0;
        if (scrollPercent > 100) scrollPercent = 100;
        this.scrollPercent = scrollPercent;
    }

    // Прогресс в процентах (0..100) — теперь учитывает scrollPercent
    public float getProgress() {
        // Если читаем — показываем прогресс читалки
        if (STATUS_READING.equals(status)) {
            return (float) readerPercent;
        }

        if (totalPages == 0 && lastPage == 0) return 0f;
        return (float) (lastPage + 1) / (float) totalPages * 100f;
    }

    public boolean isRead() {
        return STATUS_READ.equals(status);
    }

    public boolean isReading() {
        return STATUS_READING.equals(status);
    }

    public boolean isPlanned() {
        return STATUS_PLANNED.equals(status);
    }

    public int getReaderPosition() { return readerPosition; }
    public void setReaderPosition(int readerPosition) {
        this.readerPosition = Math.max(0, readerPosition);
    }

    public int getReaderOffset() { return readerOffset; }
    public void setReaderOffset(int readerOffset) {
        this.readerOffset = Math.max(0, readerOffset);
    }

    public int getReaderPercent() { return readerPercent; }
    public void setReaderPercent(int readerPercent) {
        if (readerPercent < 0) readerPercent = 0;
        if (readerPercent > 100) readerPercent = 100;
        this.readerPercent = readerPercent;
    }

}
