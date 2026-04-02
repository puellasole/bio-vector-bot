package edu.diploma.biovectorbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import edu.diploma.biovectorbot.bot.BioVectorBot;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

@Configuration
public class BioVectorBotConfiguration {

	private HttpLoggingInterceptor log = new HttpLoggingInterceptor().setLevel(Level.BASIC);
	
	@Bean
	public OkHttpClient okHttpClient() {
		return new OkHttpClient.Builder()
				.addInterceptor(log)
				.build();
	}
	
	@Bean
	public TelegramBotsApi telegramBotsApi(BioVectorBot bot) throws TelegramApiException{
		var api = new TelegramBotsApi(DefaultBotSession.class);
		api.registerBot(bot);
		return api;
	}
}
