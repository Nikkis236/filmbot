package com.tg.filmbot.handler;

import com.tg.filmbot.bot.Bot;
import com.tg.filmbot.command.Command;
import com.tg.filmbot.command.ParsedCommand;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbPeople;
import info.movito.themoviedbapi.model.people.Person;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

public class PersonHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(SystemHandler.class);
    private final String END_LINE = "\n";

    public PersonHandler(Bot bot) {
        super(bot);
    }

    @Override
    public String operate(String chatId, ParsedCommand parsedCommand, Update update) {
        Command command = parsedCommand.getCommand();

        switch (command) {
            case TOPPERSONS:
                bot.sendQueue.add(getMessagePersons(chatId));
                break;
        }
        return "";
    }

    private SendMessage getMessagePersons(String chatID) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.enableMarkdown(true);

        TmdbPeople tmdbPeople = new TmdbApi("2ca681c09cdd54b6787ed999243219d9").getPeople();

        StringBuilder text = new StringBuilder();
        text.append("Известные люди: ");
        for (Person person : tmdbPeople.getPersonPopular(1).getResults()) {
            text.append("-")
                    .append(person.getName())
                    .append(" [/person_").append(person.getId()).append("](/person_").append(person.getId()).append("))")
                    .append(END_LINE);

        }

        return sendMessage.setText(text.toString());
    }


}
