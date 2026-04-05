package edu.diploma.biovectorbot.service;

import java.io.IOException;

import edu.diploma.biovectorbot.dto.Task;

public interface BioVectorBotService {
	
	Task getTask(String taskNumber);
	String getAwards(Long chatId);
	String getFinalFeedback(Long chatId, String studentAnswer, boolean isFirstSectionScenario) throws IOException; //task???

	void updateXpCountForUser(Long chatId, int xpToAdd);
	boolean checkAnswer(Long chatId, String studentAnswer);
	
}
