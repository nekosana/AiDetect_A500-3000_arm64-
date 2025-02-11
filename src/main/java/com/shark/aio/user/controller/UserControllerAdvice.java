package com.shark.aio.user.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

//@ControllerAdvice
public class UserControllerAdvice {
    @ModelAttribute("iconPath")
    public String iconPath(){
        return null;
    }
}
