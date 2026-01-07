package com.example.booklibrary;

import android.content.Context;
import android.net.Uri;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class Fb2Parser {

    private final Context context;

    public Fb2Parser(Context context) {
        this.context = context;
    }

    /**
     * Парсит FB2-файл и возвращает ParsedBook:
     *  - title      — название (<book-title>)
     *  - author     — автор (first-name + middle-name + last-name)
     *  - genre      — жанр (<genre>)
     *  - fullText   — текст (параграфы <p> из <body>)
     *  - dateString — строка из <date> (для вычисления года)
     */
    public ParsedBook parseBook(Uri uri) {
        ParsedBook result = new ParsedBook();
        StringBuilder fullText = new StringBuilder();

        try (InputStream inputStream =
                     context.getContentResolver().openInputStream(uri)) {

            if (inputStream == null) {
                Toast.makeText(context, "Не удаётся открыть файл", Toast.LENGTH_SHORT).show();
                return null;
            }

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, "UTF-8");

            int eventType = parser.getEventType();
            boolean inBody = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String name = parser.getName();

                    switch (name) {
                        // --- метаданные ---
                        case "book-title":
                            result.title = readSimpleText(parser);
                            break;

                        case "author":
                            result.author = parseAuthor(parser);
                            break;

                        case "genre":
                            result.genre = readSimpleText(parser);
                            break;

                        case "date":
                            result.dateString = readSimpleText(parser);
                            break;

                        case "body":
                            inBody = true;
                            break;

                        // --- основной текст ---
                        case "p":
                            if (inBody) {
                                String paragraph = readSimpleText(parser);
                                if (!paragraph.isEmpty()) {
                                    fullText.append(paragraph).append("\n\n");
                                }
                            }
                            break;
                    }

                } else if (eventType == XmlPullParser.END_TAG) {
                    if ("body".equals(parser.getName())) {
                        inBody = false;
                    }
                }

                eventType = parser.next();
            }

            result.fullText = fullText.toString().trim();

            // Проверки
            if (result.title == null || result.title.trim().isEmpty()) {
                Toast.makeText(context, "Не удалось найти название книги в FB2", Toast.LENGTH_SHORT).show();
                return null;
            }

            if (result.fullText.isEmpty()) {
                Toast.makeText(context, "Текст книги пустой или повреждён", Toast.LENGTH_SHORT).show();
                // можно всё равно вернуть метаданные, если хочешь:
                // return result;
                return null;
            }

            return result;

        } catch (IOException | XmlPullParserException e) {
            Toast.makeText(context, "Ошибка парсинга FB2: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // Читает <tag>текст</tag>, когда курсор стоит на START_TAG
    private String readSimpleText(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        String result = "";
        int eventType = parser.next();
        if (eventType == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag(); // перейти к END_TAG
        }
        return result != null ? result.trim() : "";
    }

    // Парсит <author>...</author>
    private String parseAuthor(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        StringBuilder author = new StringBuilder();
        int startDepth = parser.getDepth();
        int eventType = parser.next();

        while (!(eventType == XmlPullParser.END_TAG
                && parser.getDepth() == startDepth
                && "author".equals(parser.getName()))) {

            if (eventType == XmlPullParser.START_TAG) {
                String name = parser.getName();
                switch (name) {
                    case "first-name":
                        author.append(readSimpleText(parser)).append(" ");
                        break;
                    case "middle-name":
                        author.append(readSimpleText(parser)).append(" ");
                        break;
                    case "last-name":
                        author.append(readSimpleText(parser));
                        break;
                    default:
                        skipTag(parser);
                        break;
                }
            }

            eventType = parser.next();
        }

        return author.toString().trim();
    }

    // Пропускает текущий тег целиком вместе с вложенными (курсор на START_TAG)
    private void skipTag(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        int depth = 1;
        while (depth != 0) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                depth++;
            } else if (eventType == XmlPullParser.END_TAG) {
                depth--;
            }
        }
    }
}
