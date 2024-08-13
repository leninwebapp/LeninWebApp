package com.mycompany.webserverlenin;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        return "error"; // Ensure error.html exists in src/main/resources/templates
    }

    public String getErrorPath() {
        return "/error";
    }
}
