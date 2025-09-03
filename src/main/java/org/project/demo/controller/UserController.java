// UserController.java
package org.project.demo.controller;

import org.project.demo.model.User;
import org.project.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> listUsers() {
        return userRepository.findAll();
    }
}
