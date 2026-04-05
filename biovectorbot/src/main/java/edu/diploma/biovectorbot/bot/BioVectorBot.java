package edu.diploma.biovectorbot.bot;

import java.io.File;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import edu.diploma.biovectorbot.service.BioVectorBotService;
import edu.diploma.biovectorbot.service.UserSessionService;
import edu.diploma.biovectorbot.user.UserState;

@Component
public class BioVectorBot extends TelegramLongPollingBot{
	
	private static Logger log = LoggerFactory.getLogger(BioVectorBot.class);
	
	private static final String START = "/start";
	private static final String STATS = "/stats";
	private static final String SIXTH = "/6";
	private static final String ELEVENTH = "/11";
	private static final String TWENTYFIRST = "/21";
	private static final String TWENTYSECOND = "/22";
	private static final String FIRSTSECTION = "/first";
	private static final String SECONDSECTION = "/second";
	private static final String HELP = "/help";
	private static final String QUIT = "/quit";
	
	//TESTING
	private static final String TEST = "/test";

	@Autowired
	private BioVectorBotService service;
	
	@Autowired
    private UserSessionService userSessionService;

	public BioVectorBot(@Value("${bot.token}") String botToken) {
		super(botToken);
	}

	@Override
	public void onUpdateReceived(Update update) {
		
		if(!update.hasMessage() || !update.getMessage().hasText()) {
			return;
		}
		var message = update.getMessage().getText();
		var chatId = update.getMessage().getChatId();
		var userState = userSessionService.getUserState(chatId);
		
		if (userState == UserState.WAITING_FOR_TASK_QUESTION) {
			String taskNumber = message.substring(1);
        	processTaskExtraction(chatId, taskNumber);
            return;
        }
        		
        if (userState == UserState.WAITING_FOR_DEEPSEEK_REPLY) {
        	processDeepSeekResponse(chatId, message);
            return;
        }
        
        if (userState == UserState.WAITING_FOR_AWARDS) {
        	processAwardsInfo(chatId);
            return;
        }
        
		switch(message) {
			case START -> {
				String userName = update.getMessage().getChat().getFirstName();
				startCommand(chatId, userName);
			}
			case SIXTH -> {
				sixthCommand(chatId, message);
			}
			case TWENTYSECOND -> {
				twentySecondCommand(chatId, message);
			}
			case STATS -> {
				statsCommand(chatId);
			}
			case TEST -> {
				testCommand(chatId);
			}
			case QUIT -> {
				quitCommand(chatId);
			}
			case HELP -> helpCommand(chatId);
			default -> unknownCommand(chatId);
		}
	}
	
	private void twentySecondCommand(Long chatId, String message) {
		String taskNumber = message.substring(1);
		userSessionService.setUserState(chatId, UserState.WAITING_FOR_TASK_QUESTION);
		var text = """
				Отлично! Давай разберём %s задание.
				Выбери номер задания:
				/2201 /2202 /2203 /2204 /2205
				""";
		var formattedText = String.format(text, taskNumber);
		sendMessage(chatId, formattedText);
	}

	private void quitCommand(Long chatId) {
		userSessionService.setUserState(chatId, UserState.DEFAULT);
		var text = """
				Состояние сброшено. Теперь ты можешь выбрать другую задачу.
				Чтобы вернуться к списку задач нажми /start.
				""";
		var formattedText = String.format(text);
		sendMessage(chatId, formattedText);
	}

	private void testCommand(Long chatId) {
		try {
			InputStream imageStream = getClass().getResourceAsStream("/images/6001.png");
	        
	        if (imageStream == null) {
	            System.out.println("Ошибка: файл /images/6001.png не найден в resources");
	            return;
	        }
	        
	        SendPhoto photo = new SendPhoto(chatId.toString(), new InputFile(imageStream, "6001.png"));
	        photo.setCaption("Вот ваша картинка!");
	        
	        execute(photo);
	        System.out.println("Картинка отправлена!");
        } catch (TelegramApiException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
	}
	
	private void sendPictureCommand(Long chatId, String taskNumber) {
		var picturaName = "%s.png";
		var picturePath = "/images/%s.png";
		var formattedText = String.format(picturePath, taskNumber);
		var formattedTextName = String.format(picturaName, taskNumber);
		try {
			InputStream imageStream = getClass().getResourceAsStream(formattedText);
	        
	        if (imageStream == null) {
	            System.out.println("Ошибка: файл " + formattedText + "не найден в resources");
	            return;
	        }
	        
	        SendPhoto photo = new SendPhoto(chatId.toString(), new InputFile(imageStream, formattedTextName));
	        photo.setCaption("Вот ваша картинка!");
	        
	        execute(photo);
	        System.out.println("Картинка отправлена!");
        } catch (TelegramApiException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
	}

	private void processAwardsInfo(Long chatId) {
		try {
	        String result = service.getAwards(chatId);
	        sendMessage(chatId, result);
	    } catch (Exception e) {
	        sendMessage(chatId, "❌ Ошибка при обработке информации по наградам: " + e.getMessage());
	    } finally {
	        userSessionService.clearUserState(chatId);
	    }
	}

	private void processTaskExtraction(Long chatId, String taskNumber) {
		try {
	        String result = service.getTaskQuestion(taskNumber);
	        sendPictureCommand(chatId, taskNumber);
	        sendMessage(chatId, result);
	        userSessionService.setUserState(chatId, UserState.WAITING_FOR_DEEPSEEK_REPLY);
	    } catch (Exception e) {
	        sendMessage(chatId, "❌ Ошибка при обработке оправки задачи: " + e.getMessage());
	        userSessionService.clearUserState(chatId);
	    } /*finally {
	        userSessionService.clearUserState(chatId);
	        
	    }*/
		
	}

	private void processDeepSeekResponse(Long chatId, String studentAnswer) {
		try {
	        String result = service.getFinalFeedback(studentAnswer);
	        sendMessage(chatId, result);
	    } catch (Exception e) {
	    	 e.printStackTrace();
	        sendMessage(chatId, "❌ Ошибка при обработке запроса: " + e.getMessage());
	    } finally {
	        userSessionService.clearUserState(chatId);
	    }
	}

	private void statsCommand(Long chatId) {
		userSessionService.setUserState(chatId, UserState.WAITING_FOR_AWARDS);
		var text = """
				Сейчас отправлю информацию по твоим наградам :)
				""";
		var formattedText = String.format(text);
		sendMessage(chatId, formattedText);
		
	}

	private void unknownCommand(Long chatId) {
		var text = """ 
				Я не знаю эту команду :(
				""";
		sendMessage(chatId, text);
	}

	private void sixthCommand(Long chatId, String message) {
		String taskNumber = message.substring(1);
		userSessionService.setUserState(chatId, UserState.WAITING_FOR_TASK_QUESTION);
		var text = """
				Отлично! Давай разберём %s задание.
				Выбери номер задания:
				/6001 /6002 /6003 /6004 /6005
				""";
		var formattedText = String.format(text, taskNumber);
		sendMessage(chatId, formattedText);
	}

	private void startCommand(Long chatId, String userName) {
		var text = """
				Привет, %s!
				Я твой персональный репетитор по биологии.
				Выбери какие задания будем решать сегодня: 
				
				ПЕРВАЯ ЧАСТЬ:
				/6 - [название]
				/11 - [название]
				/18
				/19
				
				ВТОРАЯ ЧАСТЬ:
				/21 - [название]
				/22 - [название]
				/23 - [название]
				/24 - [название]
				
				Дополнительные команды:
				/help - Справочная информация
				/stats - Твоя персональная статистика по наградам
				/quit - Сброс (если передумал(а) решать задачу)
				""";
		var formattedText = String.format(text, userName);
		sendMessage(chatId, formattedText);
		
	}
	
	private void sendMessage(Long chatId, String text) {
		var chatIdStr = String.valueOf(chatId);
		var sendMessage = new SendMessage(chatIdStr, text);
		sendMessage.setParseMode("Markdown");
		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			log.error("Failed sending message");
		}
		
	}
	
	public void helpCommand(Long chatId) {
		String text = """
		        📖 Помощь по командам:
		        
		        /start - Начать работу (выбор заданий)
		        /stats - Твоя персональная статистика по наградам
		        /quit - Сброс (не забывай нажимать, если передумаешь решать задачу)
		        /help - Показать эту справку
		        
		        💡 Желаю успехов в учёбе!
		        """;
		sendMessage(chatId, text);
	}

	@Override
	public String getBotUsername() {
		return "bio_vector_bot";
	}

}
