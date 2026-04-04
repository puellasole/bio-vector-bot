package edu.diploma.biovectorbot.dto;

public class Task {

	private Integer number;
    private String taskQuestion;
    private String answer;
    private String keys;
    private String picturePath;

	public Task(Integer number, String taskQuestion, String answer, String keys, String picturePath) {
		super();
		this.number = number;
		this.taskQuestion = taskQuestion;
		this.answer = answer;
		this.keys = keys;
		this.picturePath = picturePath;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getTaskQuestion() {
		return taskQuestion;
	}

	public void setTaskQuestion(String taskQuestion) {
		this.taskQuestion = taskQuestion;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getKeys() {
		return keys;
	}

	public void setKeys(String keys) {
		this.keys = keys;
	}

	public String getPicturePath() {
		return picturePath;
	}

	public void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
	}
	
       
}
