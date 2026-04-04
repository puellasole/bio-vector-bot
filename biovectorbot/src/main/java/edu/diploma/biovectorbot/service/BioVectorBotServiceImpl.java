package edu.diploma.biovectorbot.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.diploma.biovectorbot.client.BioVectorBotClient;
import edu.diploma.biovectorbot.dto.Task;
import edu.diploma.biovectorbot.entities.TaskEntity;
import edu.diploma.biovectorbot.repository.BioVectorBotTaskRepository;

@Service
public class BioVectorBotServiceImpl implements BioVectorBotService {
	
	@Autowired
	BioVectorBotTaskRepository bioVectorBotTaskRepository;
	@Autowired
	PromptBuilder promptBuilder;
	@Autowired
	BioVectorBotClient client;

	Task task;

	@Override
	public String getAwards(Long chatId) {
		return null;
	}

	@Override
	public String getFinalFeedback(String studentAnswer) throws IOException {
		String prompt = promptBuilder.buildPrompt(this.task, studentAnswer);
		String response = client.getChatResponse(prompt);
		return response;
	}
	
	@Override
	public String getTaskQuestion(String taskNumber) {
		task = getTask(taskNumber);
		return task.getTaskQuestion();
	}

	public Task getTask(String taskNumber) {
		TaskEntity taskEntity = bioVectorBotTaskRepository.findByNumber(Integer.parseInt(taskNumber));
		Task task = convertEntityToDTO(taskEntity);
		return task;
	}
	
	public Task getTask() {
		return this.task;
	}
	
	public Task convertEntityToDTO(TaskEntity taskEntity) {
		return new Task(taskEntity.getNumber(),
				taskEntity.getTaskQuestion(),
				taskEntity.getAnswer(),
				taskEntity.getKeys(),
				taskEntity.getPicturePath());
	}

}
