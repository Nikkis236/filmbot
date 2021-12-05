package com.tg.filmbot.handler;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.credits.MediaCreditCast;
import com.omertron.themoviedbapi.model.media.MediaCreditList;
import com.tg.filmbot.bot.Bot;
import com.tg.filmbot.command.Command;
import com.tg.filmbot.command.ParsedCommand;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbTV;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.tv.TvSeries;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class TVSeriesHandler extends AbstractHandler{

    private static final Logger log = Logger.getLogger(TVSeriesHandler.class);
    private static final String END_LINE = "\n";

    public TVSeriesHandler(Bot bot) {
        super(bot);
    }

    @Override
    public String operate(String chatId, ParsedCommand parsedCommand, Update update) {
        Command command = parsedCommand.getCommand();

        switch (command) {
            case POPULARTVSERIES:
                bot.sendQueue.add(getMessagePopular(chatId));
                break;
            case TOPTVSERIES:
                bot.sendQueue.add(getMessageTop(chatId));
                break;
            case SERIES:
                bot.sendQueue.add(getSeries(chatId,parsedCommand.getText()));
                break;
            case TVSERIES:
                bot.sendQueue.add(getSeriesKeyboard(chatId));
                break;

        }
        return "";
    }

    private Object getMessageTop(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);

        TmdbTV tvSeries = new TmdbApi(API_KEY).getTvSeries();

        StringBuilder text = new StringBuilder();
        text.append("Популяные сериалы: ");
        for (TvSeries person : tvSeries.getPopular("ru",1).getResults()) {
            text.append("-")
                    .append(person.getName())
                    .append(" [/series_").append(person.getId()).append("](/series").append(person.getId()).append("))")
                    .append(END_LINE);

        }

        sendMessage.setReplyMarkup(this.getKeyboard());
        return sendMessage.setText(text.toString());
    }

    private Object getMessagePopular(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);

        TmdbTV tvSeries = new TmdbApi(API_KEY).getTvSeries();

        StringBuilder text = new StringBuilder();
        text.append("Лучшие сериалы: ");
        for (TvSeries person : tvSeries.getTopRated("ru",1).getResults()) {
            text.append("-")
                    .append(person.getName())
                    .append(" [/series_").append(person.getId()).append("](/series").append(person.getId()).append("))")
                    .append(END_LINE);

        }

        sendMessage.setReplyMarkup(this.getKeyboard());
        return sendMessage.setText(text.toString());
    }

    private Object getSeries(String chatId, String text) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);


        TmdbTV tvSeries = new TmdbApi(API_KEY).getTvSeries();
        TvSeries series = tvSeries.getSeries(Integer.parseInt(text), "ru");

        StringBuilder builder = new StringBuilder();
        builder.append(series.getName()).append(END_LINE)
                .append("⭐ ").append(series.getVoteAverage()).append(END_LINE)
                .append("\uD83D\uDCC5 ").append(series.getFirstAirDate()).append(END_LINE)
                .append("Количество сезонов: ").append(series.getNumberOfSeasons()).append(END_LINE)
                .append("Количество эпизодов: ").append(series.getNumberOfEpisodes()).append(END_LINE)
                .append(series.getOverview()).append(END_LINE)
                .append("\uD83C\uDFAD ");

        for (Genre genre : series.getGenres()) {
            builder.append(genre.getName())
                    .append(" [/genre_").append(genre.getId()).append("]((/genre_").append(genre.getId()).append(")) ");
        }

        try {
            TheMovieDbApi api = new TheMovieDbApi(API_KEY);
            MediaCreditList movieCredits = api.getTVCredits(Integer.parseInt(text), "ru");
            builder.append(END_LINE).append("Актёры: ");
            List<MediaCreditCast> creditCasts = movieCredits.getCast().size() > 10
                    ? movieCredits.getCast().subList(0, 10) : movieCredits.getCast();
            for (MediaCreditCast actor : creditCasts) {
                builder.append(actor.getName())
                        .append(" [/person_").append(actor.getId()).append("](/person_").append(actor.getId()).append(")) ");
            }
        } catch (MovieDbException e) {
            log.error(e);
        }

        return sendMessage.setText(builder.toString());
    }


    private Object getSeriesKeyboard(String chatId) {
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

        popularFilmsKey.add(new KeyboardButton("/populartvseries"));
        popularFilmsKey.add(new KeyboardButton("/toptvseries"));
        returnKey.add(new KeyboardButton("/return"));

        keyboard.add(popularFilmsKey);
        keyboard.add(returnKey);
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }
}
