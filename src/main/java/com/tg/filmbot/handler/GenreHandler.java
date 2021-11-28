package com.tg.filmbot.handler;

import com.tg.filmbot.bot.Bot;
import com.tg.filmbot.command.Command;
import com.tg.filmbot.command.ParsedCommand;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbGenre;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

public class GenreHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(SystemHandler.class);
    private final String END_LINE = "\n";

    public GenreHandler(Bot bot) {
        super(bot);
    }

    @Override
    public String operate(String chatId, ParsedCommand parsedCommand, Update update) {
        Command command = parsedCommand.getCommand();

        switch (command) {
            case GENRES:
                bot.sendQueue.add(getMessageGenres(chatId));
                break;
            case GENRE:
                bot.sendQueue.add(getMessageGenre(chatId, parsedCommand));
                break;
        }
        return "";
    }

    private SendMessage getMessageGenres(String chatID) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.enableMarkdown(true);


        TmdbGenre tmdbGenre = new TmdbApi("2ca681c09cdd54b6787ed999243219d9").getGenre();

        StringBuilder text = new StringBuilder();
        text.append("Список жанров: ");
        for (Genre genre : tmdbGenre.getGenreList("ru")) {
            text.append("-")
                    .append(genre.getName())
                    .append(" [/genre_").append(genre.getId()).append("](/genre_").append(genre.getId()).append("))")
                    .append(END_LINE);

        }

        return sendMessage.setText(text.toString());
    }


    private SendMessage getMessageGenre(String chatID, ParsedCommand parsedCommand) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.enableMarkdown(true);


        TmdbGenre tmdbGenre = new TmdbApi("2ca681c09cdd54b6787ed999243219d9").getGenre();
        MovieResultsPage moviesPage = tmdbGenre.getGenreMovies(Integer.parseInt(parsedCommand.getText()), "ru", 1, true);

        StringBuilder text = new StringBuilder();
        for (MovieDb movie : moviesPage.getResults()) {
            text.append("-")
                    .append(movie.getTitle())
                    .append(" (")
                    .append(movie.getReleaseDate()).append(", ")
                    .append(movie.getVoteAverage()).append(", ")
                    .append(" [/movie_").append(movie.getId()).append("](/movie_").append(movie.getId()).append("))")
                    .append(END_LINE);
        }

        return sendMessage.setText(text.toString());
    }
}
