package edu.diploma.biovectorbot.bot;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import edu.diploma.biovectorbot.dto.Task;
import edu.diploma.biovectorbot.service.BioVectorBotService;
import edu.diploma.biovectorbot.service.UserSessionService;
import edu.diploma.biovectorbot.user.UserState;
import edu.diploma.biovectorbot.utils.AwardConstants;

@Component
public class BioVectorBot extends TelegramLongPollingBot{
	
	private static Logger log = LoggerFactory.getLogger(BioVectorBot.class);
	
	private static final String START = "/start";
	private static final String STATS = "/stats";
	private static final String FIRSTSECTION = "/first";
	private static final String SECONDSECTION = "/second";
	private static final String HELP = "/help";
	private static final String QUIT = "/quit";

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
		
		if (QUIT.equals(message)) {
	        quitCommand(chatId);
	        return;
	    }
		
		if (HELP.equals(message)) {
			helpCommand(chatId);
	        return;
	    }
		
		if (userState == UserState.WAITING_FOR_TASK_QUESTION) {
			String taskNumber = message.substring(1);
        	processTaskExtraction(chatId, taskNumber);
            return;
        }
        		
        if (userState == UserState.WAITING_FOR_DEEPSEEK_REPLY) {
        	boolean isFirstSectionScenario = userSessionService.isFirstSectionScenario(chatId);
        	if(isFirstSectionScenario) {
        		if(checkAnswer(chatId,message)) {
        			userSessionService.clearUserState(chatId);
        			return;
        		} else {
        			processDeepSeekResponse(chatId, message, isFirstSectionScenario);
        		}
        	} else {
        		processDeepSeekResponse(chatId, message, isFirstSectionScenario);
        	}
            return;
        }
        
		switch(message) {
			case START -> {
				String userName = update.getMessage().getChat().getFirstName();
				startCommand(chatId, userName);
			}
			case FIRSTSECTION -> {
				firstSectionCommand(chatId);
			}
			case SECONDSECTION -> {
				secondSectionCommand(chatId);
			}
			case STATS -> {
				statsCommand(chatId);
			}
			default -> unknownCommand(chatId);
		}
	}
	
	private boolean checkAnswer(Long chatId, String studentAnswer) {
		try {
			boolean isAnswerRight = service.checkAnswer(chatId, studentAnswer);
	        if(isAnswerRight) {
	        	service.updateXpCountForUser(chatId, AwardConstants.FIRST_SECTION_CORRECT_XP);
	        	String message = String.format("""
	                    🎉 Поздравляю! Ответ верный ✅ 
	                    Ты получаешь %d XP 📈 
	                    /stats - Твоя статистика наград
	                    """, AwardConstants.FIRST_SECTION_CORRECT_XP);
	        	sendMessage(chatId, message);
	        	return true;
	        }
		} catch(IllegalStateException e) {
			sendMessage(chatId, "❌ Ошибка: задание не найдено. Пожалуйста, выберите задачу заново.");
	        userSessionService.clearUserState(chatId);
		}
		
		return false;
	}

	private void secondSectionCommand(Long chatId) {
		userSessionService.setScenario(chatId, false);
		userSessionService.setUserState(chatId, UserState.WAITING_FOR_TASK_QUESTION);
		var text = """
				🎯 Отлично! Давай разберём задания второй части.
				
				Выбери номер задания:
				🧪 Тип 22. Методология эксперимента
				/2201 /2202 /2203 /2204 /2205
				📊 Тип 23. Выводы по результатам эксперимента
				/2301 /2302 /2303 /2304 /2305
				📄 Тип 24. Анализ текстовой и графической информации
				/2401 /2402 /2403 /2404 /2405
				🧍 Тип 25. Человек и многообразие организмов
				/2501 /2502 /2503 /2504 /2505
				🍂 Тип 26. Общебиологические закономерности
				/2601 /2502 /2503 /2504 /2505
				🔬 Тип 27. Задача по цитологии
				/2701 /2502 /2503 /2504 /2505
				🧬 Тип 28. Задача по генетике
				/2801 /2502 /2503 /2504 /2505
				""";
		sendMessage(chatId, text);
	}

	private void firstSectionCommand(Long chatId) {
		userSessionService.setScenario(chatId, true);
		userSessionService.setUserState(chatId, UserState.WAITING_FOR_TASK_QUESTION);
		var text = """
				🎯 Отлично! Давай разберём задания первой части.
				
				Выбери номер задания:
				🦠 Тип 6. Клетка, организм (установление соответствия)
				/6001 /6002 /6003 /6004 /6005
				🧫 Тип 7. Клетка, организм (множественный выбор)
				/7001 /7002 /7003 /7004 /7005
				⚙️ Тип 8. Клетка, организм (установление последовательности)
				/8001 /8002 /8003 /8004 /8005
				🌍 Тип 10. Многообразие организмов (установление соответствия)
				/1001 /1002 /1003 /1004 /1005
				🌿 Тип 11. Многообразие организмов (множественный выбор)
				/1101 /1102 /1103 /1104 /1105
				🧠 Тип 14. Организм человека
				/1401 /1402 /1403 /1404 /1405
				🩺 Тип 16. Организм человека и гигиена
				/1601 /1602 /1603 /1604 /1605
				🐚 Тип 20. Общебиологические закономерности
				/2001 /2002 /2003 /2004 /2005
				""";		
		sendMessage(chatId, text);
	}

	private void quitCommand(Long chatId) {
		userSessionService.clearUserState(chatId);
		var text = """
				🔄 Состояние сброшено. Теперь ты можешь выбрать другую задачу.
				Чтобы вернуться к списку задач нажми /start.
				""";
		sendMessage(chatId, text);
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
			Task task = service.getTask(taskNumber);  
            userSessionService.setUserTask(chatId, task);
            
	        String result = task.getTaskQuestion();
	        sendPictureCommand(chatId, taskNumber);
	        sendMessage(chatId, result);
	        userSessionService.setUserState(chatId, UserState.WAITING_FOR_DEEPSEEK_REPLY);
	    } catch (Exception e) {
	        sendMessage(chatId, "❌ Ошибка при обработке отправки задачи: " + e.getMessage());
	        userSessionService.clearUserState(chatId);
	    } 
		
	}

	private void processDeepSeekResponse(Long chatId, String studentAnswer, boolean isFirstSectionScenario) {
		try {
			String result = service.getFinalFeedback(chatId, studentAnswer, isFirstSectionScenario);
			
			if (!isFirstSectionScenario) {
	            int score = extractScoreFromDeepSeekResponse(result);
	            int xpEarned = score * AwardConstants.SECOND_SECTION_XP_MULTIPLIER;
	            service.updateXpCountForUser(chatId, xpEarned);
	            
	            result += String.format("\n\n✨ Ты получаешь %d XP за это задание", xpEarned);
	        }
			
	        sendMessage(chatId, result);
	    } catch (Exception e) {
	    	 e.printStackTrace();
	        sendMessage(chatId, "❌ Ошибка при обработке запроса: " + e.getMessage());
	    } finally {
	        userSessionService.clearUserState(chatId);
	    }
	}
	
	private int extractScoreFromDeepSeekResponse(String response) {
	    Pattern pattern = Pattern.compile("(?:ИТОГО|ФИНАЛЬНЫЙ РЕЗУЛЬТАТ)\\s*:\\s*(\\d+)");
	    Matcher matcher = pattern.matcher(response);
	    if (matcher.find()) {
	        return Integer.parseInt(matcher.group(1));
	    }
	    return 0; 
	}

	private void statsCommand(Long chatId) {
		userSessionService.setUserState(chatId, UserState.WAITING_FOR_AWARDS);
		var text = """
				Сейчас отправлю информацию по твоим наградам 🏆🙂
				""";
		sendMessage(chatId, text);
		processAwardsInfo(chatId);
	}

	private void unknownCommand(Long chatId) {
		var text = """ 
				Я не знаю эту команду ☹️
				""";
		sendMessage(chatId, text);
	}

	private void startCommand(Long chatId, String userName) {
		var text = """
				Привет, %s! ✨
				Я твой персональный репетитор по биологии. 
				
				📚 Выбери задания какой части будем решать сегодня: 
				
				/first - Первая часть
				/second - Вторая часть
				
				Дополнительные команды:
				/help - Справочная информация и правила пользования
				/stats - Твоя персональная статистика по наградам
				/quit - Сброс (если передумал(а) решать задачу)
				
				💡 Желаю успехов в учёбе!
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
				📋 Справка по использованию бота:
				
				🧠 Как работать:
		        1. /start — выбрать первую или вторую часть
		        2. Выбрать номер задания 
		        3. Написать ответ на задание
		        4. Получить разбор и XP
		        
		        🔄 Команда /quit:
		        Сбрасывает текущее задание.
		        Нужна, если передумал(а) решать или хочешь выбрать другое задание.
				
				📌 Важно: после выбора задания бот ждёт только твой ответ.
				
		        📖 Помощь по командам:
		        
		        /start - Начать работу (выбор заданий)
		        /stats - Твоя персональная статистика по наградам
		        /quit - Сброс (не забывай нажимать, если передумаешь решать задачу)
		        /help - Показать эту справку
		        """;
		sendMessage(chatId, text);
	}

	@Override
	public String getBotUsername() {
		return "bio_vector_bot";
	}

}
