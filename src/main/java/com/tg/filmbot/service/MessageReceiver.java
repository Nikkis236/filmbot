package com.tg.filmbot.service;

import com.tg.filmbot.bot.Bot;
import com.tg.filmbot.command.Command;
import com.tg.filmbot.command.ParsedCommand;
import com.tg.filmbot.command.Parser;
import com.tg.filmbot.handler.*;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;


import static java.util.Optional.ofNullable;

@Component
public class MessageReceiver implements Runnable {
    private static final Logger log = Logger.getLogger(MessageReceiver.class);
    private final int WAIT_FOR_NEW_MESSAGE_DELAY = 1000;
    private final Bot bot;
    private final Parser parser;

    public MessageReceiver(Bot bot) {
        this.bot = bot;
        parser = new Parser(bot.getBotName());
    }

    @Override
    public void run() {
        log.info("[STARTED] MsgReciever.  Bot class: " + bot);
        while (true) {
            for (Object object = bot.receiveQueue.poll(); object != null; object = bot.receiveQueue.poll()) {
                log.debug("New object for analyze in queue " + object);
                analyze(object);
            }
            try {
                Thread.sleep(WAIT_FOR_NEW_MESSAGE_DELAY);
            } catch (InterruptedException e) {
                log.error("Catch interrupt. Exit", e);
                return;
            }
        }
    }

    private void analyze(Object object) {
        if (object instanceof Update) {
            Update update = (Update) object;
            log.debug("Update recieved: " + update);
            analyzeForUpdateType(update);
        } else log.warn("Cant operate type of object: " + object.toString());
    }

    private void analyzeForUpdateType(Update update) {
        final Long chatId = ofNullable(update.getMessage())
                .map(Message::getChatId)
                .orElse(
                        ofNullable(update.getCallbackQuery())
                                .map(CallbackQuery::getMessage)
                                .map(Message::getChatId)
                                .orElse(0L));
        final String inputText = ofNullable(update.getMessage())
                .map(Message::getText)
                .orElse("");

        ParsedCommand parsedCommand;
        if (update.hasCallbackQuery()) {
            parsedCommand = parser.getParsedCommand(update.getCallbackQuery().getData());
        } else {
            parsedCommand = parser.getParsedCommand(inputText);
        }

        AbstractHandler handlerForCommand = getHandlerForCommand(parsedCommand.getCommand());

        String operationResult = handlerForCommand.operate(String.valueOf(chatId), parsedCommand, update);

        if (!"".equals(operationResult)) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(operationResult);
            bot.sendQueue.add(message);
        }
    }

    private AbstractHandler getHandlerForCommand(Command command) {
        if (command == null) {
            log.warn("Null command accepted. This is not good scenario.");
            return new DefaultHandler(bot);
        }
        switch (command) {
            case START:
            case HELP:
                SystemHandler systemHandler = new SystemHandler(bot);
                log.info("Handler for command[" + command + "] is: " + systemHandler);
                return systemHandler;
            case GENRES:
            case GENRE:
                return new GenreHandler(bot);
            case POPULAR:
            case MOVIE:
                return new MovieHandler(bot);
            case TOPPERSONS:
                return new PersonHandler(bot);
            default:
                log.info("Handler for command[" + command + "] not Set. Return DefaultHandler");
                return new DefaultHandler(bot);
        }
    }
}