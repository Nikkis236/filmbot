package com.tg.filmbot;

import com.tg.filmbot.bot.Bot;
import com.tg.filmbot.dao.connection.ConnectionPoolException;
import com.tg.filmbot.dao.connection.PoolProvider;
import com.tg.filmbot.service.MessageReceiver;
import com.tg.filmbot.service.MessageSender;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.telegram.telegrambots.ApiContextInitializer;
import org.apache.log4j.Logger;

@SpringBootApplication
public class FilmbotApplication {

	private static final Logger log = Logger.getLogger(FilmbotApplication.class);
	private static final int PRIORITY_FOR_SENDER = 1;
	private static final int PRIORITY_FOR_RECEIVER = 3;

	public static void main(String[] args) throws ConnectionPoolException {
		ApiContextInitializer.init();
		PoolProvider.getConnectionPool().init("db");
		SpringApplication.run(FilmbotApplication.class, args);
		Bot filmBot = new Bot("film_bot", "2094811461:AAFtuxRwfKMyaLEg5C3fNXiwVnAh4HeNPUI");


		MessageReceiver messageReciever = new MessageReceiver(filmBot);
		MessageSender messageSender = new MessageSender(filmBot);

		filmBot.botConnect();

		Thread receiver = new Thread(messageReciever);
		receiver.setDaemon(true);
		receiver.setName("MsgReciever");
		receiver.setPriority(PRIORITY_FOR_RECEIVER);
		receiver.start();

		Thread sender = new Thread(messageSender);
		sender.setDaemon(true);
		sender.setName("MsgSender");
		sender.setPriority(PRIORITY_FOR_SENDER);
		sender.start();


	}
}
