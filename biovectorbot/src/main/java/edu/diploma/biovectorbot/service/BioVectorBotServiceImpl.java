package edu.diploma.biovectorbot.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.diploma.biovectorbot.client.BioVectorBotClient;
import edu.diploma.biovectorbot.dto.Task;
import edu.diploma.biovectorbot.entities.TaskEntity;
import edu.diploma.biovectorbot.entities.UserEntity;
import edu.diploma.biovectorbot.repository.BioVectorBotTaskRepository;
import edu.diploma.biovectorbot.repository.BioVectorBotUserRepository;

@Service
public class BioVectorBotServiceImpl implements BioVectorBotService {
	
	@Autowired
	BioVectorBotTaskRepository bioVectorBotTaskRepository;
	@Autowired
	BioVectorBotUserRepository bioVectorBotUserRepository;
	@Autowired
	PromptBuilder promptBuilder;
	@Autowired
	BioVectorBotClient client;
	@Autowired
	UserSessionService userSessionService;

	@Override
	public String getAwards(Long chatId) {
		UserEntity user = bioVectorBotUserRepository.findById(chatId).orElse(null);
	    if (user == null) {
	        return "У вас пока нет наград. Начните решать задания первой части!";
	    }
	    
	    int xp = user.getXpCnt();
	    
	    return String.format("""
	            🏆 **Твои награды** 🏆
	            
	            ⭐ **XP очки:** %d
	            📊 **Статус:** %s
	            """, 
	            xp,
	            getStatusByXp(xp));
	}
	
	private String getStatusByXp(int xp) {
	    if (xp < 10) return "🌱 Начинающий";
	    if (xp < 30) return "📚 Ученик";
	    if (xp < 60) return "🎓 Знаток";
	    return "🏅 Мастер биологии";
	}

	@Override
	public String getFinalFeedback(Long chatId, String studentAnswer, boolean isFirstSectionScenario) throws IOException {
		String response;
		if(isFirstSectionScenario) {
			String prompt = promptBuilder.buildFirstSectionPrompt(userSessionService.getUserTask(chatId), studentAnswer);
			response = client.getChatResponse(prompt);
		} else {
			String prompt = promptBuilder.buildPrompt(userSessionService.getUserTask(chatId), studentAnswer);
			response = client.getChatResponse(prompt);
		}
		return response;
	}

	@Override
	public Task getTask(String taskNumber) {
		TaskEntity taskEntity = bioVectorBotTaskRepository.findByNumber(Integer.parseInt(taskNumber));
		Task task = convertEntityToDTO(taskEntity);
		return task;
	}
	
	public Task convertEntityToDTO(TaskEntity taskEntity) {
		return new Task(taskEntity.getNumber(),
				taskEntity.getTaskQuestion(),
				taskEntity.getAnswer(),
				taskEntity.getKeys(),
				taskEntity.getPicturePath());
	}

	@Override
	public void updateXpCountForUser(Long chatId, int xpToAdd) {
		UserEntity user = bioVectorBotUserRepository.findById(chatId).orElse(null);
	    
	    if (user == null) {
	        user = new UserEntity(chatId, xpToAdd);
	    } else {
	        user.setXpCnt(user.getXpCnt() + xpToAdd);
	    }
	    
	    bioVectorBotUserRepository.save(user);
	}

	@Override
	public boolean checkAnswer(Long chatId, String studentAnswer) {
		Task task = userSessionService.getUserTask(chatId);
	    if (task == null) {
	        throw new IllegalStateException("У пользователя " + chatId + " нет активного задания");
	    }
	    return task.getAnswer().equals(studentAnswer);
	}

}
