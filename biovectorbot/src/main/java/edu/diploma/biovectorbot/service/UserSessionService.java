package edu.diploma.biovectorbot.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import edu.diploma.biovectorbot.dto.Task;
import edu.diploma.biovectorbot.user.UserState;

@Service
public class UserSessionService {
	private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
	private final Map<Long, Boolean> scenarios = new ConcurrentHashMap<>();
	private final Map<Long, Task> userTasks = new ConcurrentHashMap<>();
    
    public void setUserState(Long chatId, UserState state) {
        userStates.put(chatId, state);
    }
    
    public UserState getUserState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.DEFAULT);
    }
    
    public boolean isFirstSectionScenario(Long chatId) {
        return scenarios.getOrDefault(chatId, false);
    }
    
    public void setScenario(Long chatId, boolean isFirstSection) {
        scenarios.put(chatId, isFirstSection);
    }
    
    public void setUserTask(Long chatId, Task task) {
        userTasks.put(chatId, task);
    }
    
    public Task getUserTask(Long chatId) {
        return userTasks.get(chatId);
    }
    
    public void clearUserState(Long chatId) {
        userStates.remove(chatId);
        scenarios.remove(chatId);
        userTasks.remove(chatId);
    }
}
