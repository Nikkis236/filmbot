package com.tg.filmbot.handler;

import com.tg.filmbot.bot.Bot;
import com.tg.filmbot.command.Command;
import com.tg.filmbot.command.ParsedCommand;
import com.tg.filmbot.dao.BookMarkDAO;
import com.tg.filmbot.dao.DAOProvider;
import com.tg.filmbot.entity.Bookmark;
import com.tg.filmbot.repository.BookmarkRepo;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.people.PersonCast;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class MovieHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(MovieHandler.class);

    private static final String PREV = "/popular_next";
    private static final String NEXT = "/popular_prev";
    private static final String BOOKMARK = "/movie_bookmark";

    private static int popularMoviePage = 1;
    private static final String END_LINE = "\n";

    private DAOProvider provider = DAOProvider.getInstance();

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
                    getMessagePopular(chatId);
                    break;
                case PREV:
                    popularMoviePage--;
                    getMessagePopular(chatId);
                    break;
                case BOOKMARK:
                    bot.sendQueue.add(addToBookmark(chatId, info));
            }
        } else {
            Command command = parsedCommand.getCommand();

            switch (command) {
                case POPULAR:
                    bot.sendQueue.add(getMessagePopular(chatId));
                    break;
                case MOVIE:
                    bot.sendQueue.add(getMessageMovie(chatId, parsedCommand));
                    break;
            }
        }
        return "";
    }

    private SendMessage addToBookmark(String chatId, String movieId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);

        BookMarkDAO bookmarkDAO = provider.getBookmarkDAO();
        bookmarkDAO.save(new Bookmark(chatId, movieId));

        return sendMessage;
    }


    private SendMessage getMessagePopular(String chatID) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.enableMarkdown(true);

        StringBuilder text = new StringBuilder().append("Популярное сейчас:").append(END_LINE);

        TmdbMovies movies = new TmdbApi("2ca681c09cdd54b6787ed999243219d9").getMovies();
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


    private SendMessage getMessageMovie(String chatID, ParsedCommand parsedCommand) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.enableMarkdown(true);


        TmdbMovies movies = new TmdbApi("2ca681c09cdd54b6787ed999243219d9").getMovies();
        MovieDb movie = movies.getMovie(Integer.parseInt(parsedCommand.getText()), "ru");

        StringBuilder text = new StringBuilder();
        text.append(movie.getTitle()).append(END_LINE)
                .append("Рейтинг: ").append(movie.getVoteAverage()).append(END_LINE)
                .append("Дата выхода: ").append(movie.getReleaseDate()).append(END_LINE)
                .append("Бюджет ").append(movie.getBudget()).append(END_LINE)
                .append("Сборы ").append(movie.getRevenue()).append(END_LINE)
                .append(movie.getOverview()).append(END_LINE)
                .append("Жанры: ");

        for (Genre genre : movie.getGenres()) {
            text.append(genre.getName())
                    .append(" [/genre_").append(genre.getId()).append("]((/genre_").append(genre.getId()).append(")) ");
        }

        if (movie.getCredits() != null) {
            text.append(END_LINE).append("Актёры: ");
            for (PersonCast actor : movie.getCast()) {
                text.append(actor.getName())
                        .append(" [/person").append(actor.getId()).append("](/person").append(actor.getId()).append(")) ");
            }
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Добавить в закладки").setCallbackData(BOOKMARK + "&" + parsedCommand.getText()));
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);

        return sendMessage.setText(text.toString());
    }


}
