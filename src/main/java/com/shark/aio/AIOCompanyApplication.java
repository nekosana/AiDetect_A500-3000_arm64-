package com.shark.aio;

import com.shark.aio.alarm.GradedAlarm.JedisConnectionFactory;
import com.shark.aio.alarm.contactPart.util.MD5Util;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@MapperScan(basePackages = {"com.shark.aio.users.mapper", "com.shark.aio.project.mapper", "com.shark.aio.base.mapper","com.shark.aio.serial.mapper"})
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(JedisConnectionFactory.class)
public class AIOCompanyApplication {


    public static void main(String[] args){

        SpringApplication.run(AIOCompanyApplication.class, args);


//        File file = new File("C:\\Users\\dell\\Desktop\\yao.jpg");
//        for (int i=0;i<10;i++){
//            Thread thread = new Thread(){
//                @Override
//                public void run() {
//                    while (true){
//                        FaceController.callFaceAI(file);
//                        LicenseController.callLicenseAI(file);
//                        try {
//                            sleep(500);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//            };
//            thread.start();
//        }




    }
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
        return tomcatServletWebServerFactory -> tomcatServletWebServerFactory.addContextCustomizers((TomcatContextCustomizer) context -> {
            context.setCookieProcessor(new LegacyCookieProcessor());
        });
    }

}
