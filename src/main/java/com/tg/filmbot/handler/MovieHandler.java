package com.tg.filmbot.handler;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.credits.MediaCreditCast;
import com.omertron.themoviedbapi.model.media.MediaCreditList;
import com.tg.filmbot.bot.Bot;
import com.tg.filmbot.command.Command;
import com.tg.filmbot.command.ParsedCommand;
import com.tg.filmbot.dao.BookMarkDAO;
import com.tg.filmbot.dao.DAOProvider;
import com.tg.filmbot.entity.Bookmark;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class MovieHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(MovieHandler.class);

    private static final String PREV = "/popular_prev";
    private static final String NEXT = "/popular_next";
    private static final String BOOKMARK = "/movie_bookmark";
    private static final String SIMILAR = "/movie_similar";
    private static final String BOOKMARK_REMOVE = " /movie_bookmarkRemove";
    private static final String END_LINE = "\n";
    private static int popularMoviePage = 1;
    private final DAOProvider provider = DAOProvider.getInstance();

    public MovieHandler(Bot bot) {
        super(bot);
    }

    @Override
    public String operate(String chatId, ParsedCommand parsedCommand, Update update) {
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            String command;
            String info = "";
            if (data.contains("&")) {
                command = data.substring(0, data.indexOf("&"));
                info = data.substring(data.indexOf("&") + 1);
            } else {
                command = data;
            }
            switch (command) {
                case NEXT:
                    popularMoviePage++;
                    bot.sendQueue.add(getMessagePopular(chatId));
                    break;
                case PREV:
                    popularMoviePage--;
                    bot.sendQueue.add(getMessagePopular(chatId));
                    break;
                case BOOKMARK:
                    bot.sendQueue.add(addToBookmark(chatId, info));
                    break;
                case BOOKMARK_REMOVE:
                    bot.sendQueue.add(removeFromBookmark(chatId, info));
                    break;
                case SIMILAR:
                    bot.sendQueue.add(getMessageSimilarMovies(chatId, info));
            }
        } else {
            Command command = parsedCommand.getCommand();

            switch (command) {
                case POPULAR:
                    bot.sendQueue.add(getMessagePopular(chatId));
                    break;
                case TOPMOVIES:
                    bot.sendQueue.add(getMessageTop(chatId));
                    break;
                case MOVIE:
                    bot.sendQueue.add(getMessageMovie(chatId, parsedCommand));
                    break;
                case BOOKMARKS:
                    bot.sendQueue.add(getBookmarksMovie(chatId));
                    break;
                case MOVIES:
                    bot.sendQueue.add(getMovieKeyboard(chatId));
                    break;
            }
        }
        return "";
    }

    private Object getMovieKeyboard(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(getKeyboard());

        return  sendMessage.setText("Что дальше?");
    }

    @Override
    public ReplyKeyboardMarkup getKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow popularFilmsKey = new KeyboardRow();
        KeyboardRow returnKey = new KeyboardRow();

        popularFilmsKey.add(new KeyboardButton("/popular"));
        popularFilmsKey.add(new KeyboardButton("/topmovies"));
        returnKey.add(new KeyboardButton("/return"));

        keyboard.add(popularFilmsKey);
        keyboard.add(returnKey);
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    private Object removeFromBookmark(String chatId, String movieId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);

        BookMarkDAO bookmarkDAO = provider.getBookmarkDAO();
        if(bookmarkDAO.isInBookmarks(chatId,movieId)) {
            bookmarkDAO.deleteBookmark(chatId, movieId);
            return sendMessage.setText("Успешно удалено!");
        } else {
            return sendMessage.setText("Выберите что-нибудь другое...");
        }
    }

    private SendMessage addToBookmark(String chatId, String movieId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);

        BookMarkDAO bookmarkDAO = provider.getBookmarkDAO();
        if(bookmarkDAO.isInBookmarks(chatId,movieId)){
            return sendMessage.setText("Уже присутствует в ваших закладках. Выберите что нибудь другое!");
        } else {
            bookmarkDAO.save(new Bookmark(chatId, movieId));
            return sendMessage.setText("Успешно добавлено в ваши закладки!");
        }
    }


    private Object getMessageSimilarMovies(String chatId, String info) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);

        StringBuilder text = new StringBuilder().append("Похожие фильмы :").append(END_LINE);

        TmdbMovies movies = new TmdbApi(API_KEY).getMovies();
        MovieResultsPage moviePage = movies
                .getSimilarMovies(Integer.parseInt(info), "ru", 1);
        for (MovieDb movie : moviePage.getResults()) {
            text.append("-")
                    .append(movie.getTitle())
                    .append(" (")
                    .append(movie.getReleaseDate()).append(", ")
                    .append(movie.getVoteAverage()).append(", ")
                    .append(" [/movie_").append(movie.getId()).append("](/movie_").append(movie.getId()).append("))")
                    .append(END_LINE);
        }

        sendMessage.setReplyMarkup(getKeyboard());
        return sendMessage.setText(text.toString());
    }

    private Object getBookmarksMovie(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);


        BookMarkDAO bookmarkDAO = provider.getBookmarkDAO();
        List<String> allMovieByChat = bookmarkDAO.getAllMovieByChat(chatId);
        if (allMovieByChat.isEmpty()) {
            return sendMessage.setText("Ваши закладки пока поусты. Добавьте фильмы :)-");
        } else {
            StringBuilder text = new StringBuilder().append("Ваши закладки:").append(END_LINE);

            TmdbMovies movies = new TmdbApi(API_KEY).getMovies();
            for (String movieId : allMovieByChat) {
                MovieDb moviePage = movies.getMovie(Integer.parseInt(movieId), "ru");
                text.append("-")
                        .append(moviePage.getTitle())
                        .append(" (")
                        .append(moviePage.getReleaseDate()).append(", ")
                        .append(moviePage.getVoteAverage()).append(", ")
                        .append(" [/movie_").append(moviePage.getId()).append("](/movie_").append(moviePage.getId()).append("))")
                        .append(END_LINE);

            }
            sendMessage.setReplyMarkup(getKeyboard());
            return sendMessage.setText(text.toString());
        }
    }

    private SendMessage getMessagePopular(String chatID) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.enableMarkdown(true);

        StringBuilder text = new StringBuilder().append("Популярное сейчас:").append(END_LINE);

        TmdbMovies movies = new TmdbApi(API_KEY).getMovies();
        MovieResultsPage moviePage = movies.getPopularMovies("ru", popularMoviePage);
        for (MovieDb movie : moviePage.getResults()) {
            text.append("-")
                    .append(movie.getTitle())
                    .append(" (")
                    .append(movie.getReleaseDate()).append(", ")
                    .append(movie.getVoteAverage()).append(", ")
                    .append(" [/movie_").append(movie.getId()).append("](/movie_").append(movie.getId()).append("))")
                    .append(END_LINE);
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        if (popularMoviePage != 1) {
            rowInline.add(new InlineKeyboardButton().setText("<< Назад").setCallbackData(PREV));
        }
        rowInline.add(new InlineKeyboardButton().setText("Далее >>").setCallbackData(NEXT));

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);

        return sendMessage.setText(text.toString());
    }

    private SendMessage getMessageTop(String chatID) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.enableMarkdown(true);

        StringBuilder text = new StringBuilder().append("Лучшие фильмы:").append(END_LINE);

        TmdbMovies movies = new TmdbApi(API_KEY).getMovies();
        MovieResultsPage moviePage = movies.getTopRatedMovies("ru", 1);
        for (MovieDb movie : moviePage.getResults()) {
            text.append("-")
                    .append(movie.getTitle())
                    .append(" (")
                    .append(movie.getReleaseDate()).append(", ")
                    .append(movie.getVoteAverage()).append(", ")
                    .append(" [/movie_").append(movie.getId()).append("](/movie_").append(movie.getId()).append("))")
                    .append(END_LINE);
        }

        sendMessage.setReplyMarkup(getKeyboard());
        return sendMessage.setText(text.toString());
    }



    private SendMessage getMessageMovie(String chatID, ParsedCommand parsedCommand) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.enableMarkdown(true);


        TmdbMovies movies = new TmdbApi(API_KEY).getMovies();
        MovieDb movie = movies.getMovie(Integer.parseInt(parsedCommand.getText()), "ru");

        StringBuilder text = new StringBuilder();
        text.append(movie.getTitle()).append(END_LINE)
                .append("⭐ ").append(movie.getVoteAverage()).append(END_LINE)
                .append("\uD83D\uDCC5 ").append(movie.getReleaseDate()).append(END_LINE)
                .append("Бюджет: ").append(movie.getBudget()).append(END_LINE)
                .append("Сборы: ").append(movie.getRevenue()).append(END_LINE)
                .append(movie.getOverview()).append(END_LINE)
                .append("\uD83C\uDFAD ");

        for (Genre genre : movie.getGenres()) {
            text.append(genre.getName())
                    .append(" [/genre_").append(genre.getId()).append("]((/genre_").append(genre.getId()).append(")) ");
        }

        try {
            TheMovieDbApi api = new TheMovieDbApi(API_KEY);
            MediaCreditList movieCredits = api.getMovieCredits(Integer.parseInt(parsedCommand.getText()));
            text.append(END_LINE).append("Актёры: ");
            List<MediaCreditCast> creditCasts = movieCredits.getCast().size() > 10 ?
                    movieCredits.getCast().subList(0, 10) : movieCredits.getCast();
            for (MediaCreditCast actor : creditCasts) {
                text.append(actor.getName())
                        .append(" [/person_").append(actor.getId()).append("](/person_").append(actor.getId()).append(")) ");
            }
        } catch (MovieDbException e) {
            log.error(e);
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        BookMarkDAO bookmarkDAO = provider.getBookmarkDAO();
        if (bookmarkDAO.isInBookmarks(chatID, String.valueOf(movie.getId()))) {
            rowInline.add(new InlineKeyboardButton().setText("Удалить из закладок ❌")
                    .setCallbackData(BOOKMARK_REMOVE + "&" + parsedCommand.getText()));
        } else {
            rowInline.add(new InlineKeyboardButton().setText("Добавить в закладки \uD83D\uDCD5")
                    .setCallbackData(BOOKMARK + "&" + parsedCommand.getText()));
        }

        rowInline.add(new InlineKeyboardButton().setText("Найти похожие фильмы \uD83D\uDD0D")
                .setCallbackData(SIMILAR + "&" + parsedCommand.getText()));
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);

        return sendMessage.setText(text.toString());
    }


}
