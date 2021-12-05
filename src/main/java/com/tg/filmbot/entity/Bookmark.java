package com.tg.filmbot.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "user_bookmark")
public class Bookmark {

    @Id
    private Long id;

    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "movie_id")
    private String movieId;


    public Bookmark(String chatId, String movieId) {
        this.chatId = chatId;
        this.movieId = movieId;
    }

    public Bookmark() {
    }

}
