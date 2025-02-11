package com.shark.aio.alarm.contactPart.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
/*
pom.xml
<dependency>
  <groupId>com.aliyun</groupId>
  <artifactId>aliyun-java-sdk-core</artifactId>
  <version>4.6.0</version>
</dependency>
*/

public class SendSms {

    public static String accessKeyId = Optional.ofNullable(System.getenv("ACCESS_KEY_ID")).orElse("LTAI5tRN7HMWtuhe1PWQjinK");

    public static String accessKeySecret = Optional.ofNullable(System.getenv("ACCESS_KEY_SECRET")).orElse("xNBj4L9HjlYCXyIQp9B1PHZiwAHp4G");

    public static String codeSign = Optional.ofNullable(System.getenv("CODE_SIGN")).orElse("南京和协集成电路科技");

    public static String alarmSign = Optional.ofNullable(System.getenv("ALARM_SIGN")).orElse("栾百翔的博客");

    public static String codeMessageTemplateCode = Optional.ofNullable(System.getenv("CODE_TEMPLATE")).orElse("SMS_302615635");

    public static String alarmMessageTemplateCode = Optional.ofNullable(System.getenv("ALARM_TEMPLATE")).orElse("SMS_302615635");


    public static void SendMessageCode(String code, String phone) throws ClientException {

        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        SendSmsRequest request = new SendSmsRequest();
        request.setSignName(codeSign);
        request.setTemplateCode(codeMessageTemplateCode);
        request.setPhoneNumbers(phone);
        request.setTemplateParam("{\"code\":\""+code+"\"}");

        try {
            SendSmsResponse response = client.getAcsResponse(request);
            String jsonStr = new Gson().toJson(response);
            System.out.println(jsonStr);

        } catch (ServerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送预警短信
     * @param param 插入短信内的信息，为JSON对象，键值对形式，每个键应该与在阿里云短信服务中申请的模板相同
     * @param phone 手机号
     */
    public static void SendAlarmMessage(JSONObject param, String phone) {
        //目前测试短信的模板没有变量，param没有作用，这样写只是为了以后短信模板申请下来后不用改代码，所以在写发送报警短信的时候，把需要插在短信里的变量写进来
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        SendSmsRequest request = new SendSmsRequest();
        request.setSignName(alarmSign);
        request.setTemplateCode(alarmMessageTemplateCode);
        request.setPhoneNumbers(phone);
        request.setTemplateParam(param.toJSONString());

        try {
            SendSmsResponse response = client.getAcsResponse(request);
            String jsonStr = new Gson().toJson(response);
            System.out.println(jsonStr);

        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
    }

}
