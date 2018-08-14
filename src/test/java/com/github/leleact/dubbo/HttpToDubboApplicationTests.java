package com.github.leleact.dubbo;

import com.alibaba.dubbo.config.annotation.Reference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.leleact.dubbo.dubbo.api.Api;
import com.github.leleact.dubbo.request.CommonRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HttpToDubboApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Reference
    private Api api;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    public void httpTest() throws Exception {

        CommonRequest request = new CommonRequest();
        request.setName("xx");
        request.setAge(1);
        request.setAddress("yyy");

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(request);
        String response = mvc.perform(
                post("/com.github.leleact.dubbo.dubbo.api.Api/call").contentType(
                        MediaType.APPLICATION_JSON_UTF8_VALUE).content(content)).andExpect(
                status().is2xxSuccessful()).andReturn().getResponse().getContentAsString();
        Assert.assertEquals("\"ok-ok-ok\"", response);
    }


    @Test
    public void dubboTest() {
        CommonRequest request = new CommonRequest();
        request.setName("xx");
        request.setAge(1);
        request.setAddress("yyy");
        String response = api.call(request);
        Assert.assertEquals("ok-ok-ok", response);
    }
}
