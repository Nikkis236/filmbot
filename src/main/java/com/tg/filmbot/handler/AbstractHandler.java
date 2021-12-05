package com.tg.filmbot.handler;


import com.tg.filmbot.bot.Bot;
import com.tg.filmbot.command.ParsedCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public abstract class AbstractHandler {
    Bot bot;

    AbstractHandler(Bot bot) {
        this.bot = bot;
    }

    public abstract String operate(String chatId, ParsedCommand parsedCommand, Update update);

    public ReplyKeyboardMarkup getKeyboard(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow popularFilmsKey = new KeyboardRow();
        KeyboardRow helpKey = new KeyboardRow();
        KeyboardRow personsKey = new KeyboardRow();
        KeyboardRow bookmarks = new KeyboardRow();

        popularFilmsKey.add(new KeyboardButton("/popular"));
        popularFilmsKey.add(new KeyboardButton("/topmovies"));
        popularFilmsKey.add(new KeyboardButton("/genres"));
        personsKey.add(new KeyboardButton("/toppersons"));
        bookmarks.add(new KeyboardButton("/bookmarks"));

        keyboard.add(popularFilmsKey);
        keyboard.add(personsKey);
        keyboard.add(bookmarks);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }
}