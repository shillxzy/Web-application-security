package chnu.edu.booksecurity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hello")
public class HelloController {

    @GetMapping("/admin")
    public String helloAdmin() {
        return "Hello, admin!";
    }

    @GetMapping("/user")
    public String helloUser() {
        return "Hello, user!";
    }

    @GetMapping("/man")
    public String helloMan() {
        return "Hello, man!";
    }

    @GetMapping("/unknown")
    public String helloUnknown() {
        return "Hello, unknown!";
    }
}
