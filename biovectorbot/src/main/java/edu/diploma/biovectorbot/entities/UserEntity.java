package edu.diploma.biovectorbot.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user")
public class UserEntity {
	@Id
    @Column(name = "chatId")
    private Long chatId;
    
    @Column(name = "xp_cnt", columnDefinition = "int default 0")
    private Integer xpCnt = 0;
    
    public UserEntity() {}
    
    public UserEntity(Long chatId) {
        this.chatId = chatId;
        this.xpCnt = 0;
    }
    
    public UserEntity(Long chatId, Integer xpCnt) {
        this.chatId = chatId;
        this.xpCnt = xpCnt;
    }
    
    public Long getChatId() {
        return chatId;
    }
    
    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
    
    public Integer getXpCnt() {
        return xpCnt;
    }
    
    public void setXpCnt(Integer xpCnt) {
        this.xpCnt = xpCnt;
    }
}
