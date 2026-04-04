package edu.diploma.biovectorbot.service;

import java.io.IOException;

public interface BioVectorBotService {
	
	String getAwards(Long chatId);
	String getFinalFeedback(String studentAnswer) throws IOException; //task???
	String getTaskQuestion(String taskNumber); //text of task 
	//why chatid???
	
}
