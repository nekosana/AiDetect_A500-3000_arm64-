package com.shark.aio.operation.controller;

import com.github.pagehelper.PageInfo;
import com.shark.aio.operation.entity.AnnouncementEntity;
import com.shark.aio.operation.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @RequestMapping({"/announcement","/announcement/{pageSize}/{pageNum}"})
    public String toAnnouncementPage(HttpServletRequest request,
                                     @PathVariable(required = false) Integer pageSize,
                                     @PathVariable(required = false) Integer pageNum,
                                     String feature){
        PageInfo<AnnouncementEntity> announcementEntityPageInfo = announcementService.selectAnnouncementByPage(pageSize, pageNum, feature);
        request.setAttribute("allAnnouncement",announcementEntityPageInfo);
        return "announcement";
    }

    @GetMapping("/announcement/context")
    public String toContextPage(HttpServletRequest request, int id){
        AnnouncementEntity announcement = announcementService.selectAnnouncementById(id);
        request.setAttribute("announcement",announcement);
        return "announcementContext";
    }

}
