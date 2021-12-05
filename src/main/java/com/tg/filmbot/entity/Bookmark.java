package com.tg.filmbot.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Bookmark {

    private Long id;
    private String chatId;
    private String movieId;


    public Bookmark(String chatId, String movieId) {
        this.chatId = chatId;
        this.movieId = movieId;
    }

    public Bookmark() {
    }

}
