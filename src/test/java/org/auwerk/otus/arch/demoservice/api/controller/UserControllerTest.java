package org.auwerk.otus.arch.demoservice.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.auwerk.otus.arch.demoservice.api.dto.User;
import org.auwerk.otus.arch.demoservice.entity.UserEntity;
import org.auwerk.otus.arch.demoservice.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void resetDb() {
        userRepository.deleteAll();
    }

    @Test
    void createUser() throws Exception {
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(buildUser("user")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.id").exists());

        assertEquals(1, userRepository.count());
    }

    @Test
    void createUserDuplicate() throws Exception {
        UserEntity userEntity = UserEntity.fromDto(buildUser("user"));
        userRepository.save(userEntity);

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(buildUser("user"))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateUser() throws Exception {
        User user = buildUser("user");
        UserEntity userEntity = UserEntity.fromDto(user);
        userRepository.save(userEntity);

        user.setFirstName("John");
        user.setLastName("Doe");

        mockMvc.perform(put("/user/" + userEntity.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(user)))
                .andExpect(status().isOk());

        Optional<UserEntity> foundUser = userRepository.findById(userEntity.getId());
        assertEquals("John", foundUser.get().getFirstName());
        assertEquals("Doe", foundUser.get().getLastName());
    }

    @Test
    void deleteUser() throws Exception {
        UserEntity userEntity = UserEntity.fromDto(buildUser("user"));
        userRepository.save(userEntity);

        mockMvc.perform(delete("/user/" + userEntity.getId()))
                .andExpect(status().isNoContent());

        assertEquals(0, userRepository.count());
    }

    @Test
    void deleteNonExistingUser() throws Exception {
        mockMvc.perform(delete("/user/" + 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUsers() throws Exception {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            users.add(buildUser("user-" + i));
        }
        users.forEach(user -> {
            UserEntity userEntity = UserEntity.fromDto(user);
            userRepository.save(userEntity);
            user.setId(userEntity.getId());
        });

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", Matchers.hasSize(users.size())));
    }

    @Test
    void getUserById() throws Exception {
        UserEntity userEntity = UserEntity.fromDto(buildUser("user"));
        userRepository.save(userEntity);

        mockMvc.perform(get("/user/{id}", userEntity.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void getNonExistingUser() throws Exception {
        mockMvc.perform(get("/user/{id}", 123L))
                .andExpect(status().isNotFound());
    }

    private static User buildUser(String userName) {
        return new User(null, userName, userName, userName, userName + "@user.ru", "999999");
    }
}
