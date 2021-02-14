package com.example.practice.web;

import com.example.practice.domain.posts.Posts;
import com.example.practice.domain.posts.PostsRepository;
import com.example.practice.web.dto.PostsSaveRequestDto;
import com.example.practice.web.dto.PostsUpdateRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @After
    public void tearDown() throws Exception {
        postsRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void savePosts() throws Exception {
        final String title = "title";
        final String content = "content";
        PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder()
                .title(title)
                .content(content)
                .author("author")
                .build();

        final String url = "http://localhost:" + port + "/api/v1/posts";

        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        final List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void updatePosts() throws Exception {
        final Posts savedPosts = postsRepository.save(Posts.builder()
            .title("title")
            .content("content")
            .author("author")
            .build());

        final Long updateId = savedPosts.getId();
        final String expectedTitle = "title2";
        final String expectedContent = "content2";

        final PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder()
                                            .title(expectedTitle)
                                            .content(expectedContent)
                                            .build();

        final String url = "http://localhost:" + port + "/api/v1/posts/" + updateId;

        mvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());


        final List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
        assertThat(all.get(0).getContent()).isEqualTo(expectedContent);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void deletePosts() throws Exception {
        final PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder()
                                            .title("title")
                                            .content("content")
                                            .author("author")
                                            .build();

        String url = "http://localhost:" + port + "/api/v1/posts";

        MvcResult mvcResult = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();


        Long savedId = Long.parseLong(mvcResult.getResponse().getContentAsString());

        url = "http://localhost:" + port + "/api/v1/posts/" + savedId;

        mvcResult = mvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        Long deletedId = Long.parseLong(mvcResult.getResponse().getContentAsString());

        assertThat(deletedId).isEqualTo(savedId);
    }
}
