package com.example.practice.web.dto;

import com.example.practice.domain.posts.Posts;
import lombok.Getter;

import javax.persistence.SequenceGenerator;

@Getter
public class PostsResponseDto {
    private Long id;
    private String title;
    private String content;
    private String author;

    public PostsResponseDto(Posts entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.author = entity.getAuthor();
    }
}
