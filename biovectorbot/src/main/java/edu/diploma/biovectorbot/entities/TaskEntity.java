package edu.diploma.biovectorbot.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class TaskEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "number", nullable = false)
    private Integer number;
    
    @Column(name = "task_question", nullable = false, columnDefinition = "TEXT")
    private String taskQuestion;
    
    @Column(name = "answer", nullable = false)
    private String answer;
    
    @Column(name = "task_keys", columnDefinition = "TEXT")
    private String keys;
    
    @Column(name = "check_type")
    private String checkType;

	@Column(name = "picture_path")
    private String picturePath;
    
    public TaskEntity() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
	
	public String getCheckType() {
		return checkType;
	}

	public void setCheckType(String checkType) {
		this.checkType = checkType;
	}

	public String getPicturePath() {
		return picturePath;
	}

	public void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
	}
    
    

}
