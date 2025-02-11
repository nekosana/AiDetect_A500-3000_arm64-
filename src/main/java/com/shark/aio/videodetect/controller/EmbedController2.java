package com.shark.aio.videodetect.controller;

import com.shark.aio.videodetect.mapper.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

@Controller
@RequestMapping("/embed2")
public class EmbedController2 {

    @Autowired
    private VideoRepository videoRepository;

    @GetMapping
    @ResponseBody
    public void getFlaskPage(HttpServletRequest request, HttpServletResponse response) {
        // 获取完整的请求 URL
//        StringBuffer requestURL = request.getRequestURL();
//        String queryString = request.getQueryString();
//
//        if (queryString == null) {
//            System.out.println(requestURL.toString());vid
//        } else {
//            System.out.println(requestURL.append('?').append(queryString).toString());
//        }


        RestTemplate restTemplate = new RestTemplate();
        String flaskUrl = "http://localhost:5555/video_feed2";

        RequestCallback requestCallback = request1 -> request1.getHeaders().set(HttpHeaders.ACCEPT, "multipart/x-mixed-replace; boundary=frame");

        ResponseExtractor<Void> responseExtractor = flaskResponse -> {
            response.setContentType("multipart/x-mixed-replace; boundary=frame");
            try (InputStream inputStream = flaskResponse.getBody();
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }
            }
            return null;
        };

        restTemplate.execute(flaskUrl, HttpMethod.GET, requestCallback, responseExtractor);
    }
}
