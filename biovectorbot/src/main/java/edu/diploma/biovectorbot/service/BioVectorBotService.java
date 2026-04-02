package edu.diploma.biovectorbot.service;

public interface BioVectorBotService {
	
	String getAwards(Long chatId);
	String getFinalFeedback(Long chatId, String studentAnswer); //task???
	String getTask(Long chatId, String taskNumber); //text of task
	
}
