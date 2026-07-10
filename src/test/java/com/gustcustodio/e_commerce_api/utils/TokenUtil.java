package com.gustcustodio.e_commerce_api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustcustodio.e_commerce_api.dtos.LoginRequestDTO;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class TokenUtil {

    private final ObjectMapper objectMapper;

    public TokenUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String obtainAccessToken(MockMvc mockMvc, String username, String password) throws Exception {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(username, password);

        ResultActions resultActions =
                mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        String result = resultActions.andReturn().getResponse().getContentAsString();

        JacksonJsonParser jacksonJsonParser = new JacksonJsonParser();
        return jacksonJsonParser.parseMap(result).get("token").toString();
    }

}
