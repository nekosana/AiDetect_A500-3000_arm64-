package com.shark.aio.alarm.contactPart.util;

import cn.hutool.json.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HttpClient工具类，用于发送HTTP请求
 */
public class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    // 默认超时时间（毫秒）
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;
    private static final int SOCKET_TIMEOUT = 5000;

    // 单例模式的CloseableHttpClient实例
    private static final CloseableHttpClient httpClient;

    static {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();

        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * 发送GET方式请求
     *
     * @param url      请求URL
     * @param paramMap 请求参数（可为null）
     * @return 响应内容，如果请求失败或响应状态码不为200，则返回空字符串
     */
    public static String doGet(String url, Map<String, String> paramMap) {
        String result = "";
        try {
            URIBuilder builder = new URIBuilder(url);
            if (paramMap != null) {
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    builder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            URI uri = builder.build();

            HttpGet httpGet = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                } else {
                    logger.warn("GET请求失败，URL：{}，状态码：{}", url, statusCode);
                }
            }
        } catch (Exception e) {
            logger.error("执行GET请求时发生异常，URL：{}", url, e);
        }
        return result;
    }

    /**
     * 发送POST方式请求，参数为键值对形式
     *
     * @param url      请求URL
     * @param paramMap 请求参数（可为null）
     * @return 响应内容，如果请求失败或响应状态码不为200，则返回空字符串
     */
    public static String doPost(String url, Map<String, String> paramMap) {
        String result = "";
        HttpPost httpPost = new HttpPost(url);

        if (paramMap != null) {
            List<NameValuePair> paramList = new ArrayList<>();
            for (Map.Entry<String, String> param : paramMap.entrySet()) {
                paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }
            try {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8);
                httpPost.setEntity(entity);
            } catch (Exception e) {
                logger.error("设置POST请求参数时发生异常，URL：{}", url, e);
                return result;
            }
        }

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            } else {
                logger.warn("POST请求失败，URL：{}，状态码：{}", url, statusCode);
            }
        } catch (Exception e) {
            logger.error("执行POST请求时发生异常，URL：{}", url, e);
        }

        return result;
    }

    /**
     * 发送POST方式请求，参数为JSON格式
     *
     * @param url      请求URL
     * @param paramMap 请求参数（可为null）
     * @return 响应内容，如果请求失败或响应状态码不为200，则返回空字符串
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) {
        String result = "";
        HttpPost httpPost = new HttpPost(url);

        if (paramMap != null) {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, String> param : paramMap.entrySet()) {
                jsonObject.put(param.getKey(), param.getValue());
            }
            StringEntity entity = new StringEntity(jsonObject.toString(), StandardCharsets.UTF_8);
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
        }

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            } else {
                logger.warn("POST-JSON请求失败，URL：{}，状态码：{}", url, statusCode);
            }
        } catch (Exception e) {
            logger.error("执行POST-JSON请求时发生异常，URL：{}", url, e);
        }

        return result;
    }

    /**
     * 构建请求配置
     *
     * @return RequestConfig实例
     */
    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT) // 设置连接超时时间
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT) // 设置从连接池获取连接的超时时间
                .setSocketTimeout(SOCKET_TIMEOUT) // 设置请求获取数据的超时时间
                .build();
    }
}

//package com.shark.aio.util;
//
//import cn.hutool.json.JSONObject;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.client.utils.URIBuilder;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.util.EntityUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.net.URI;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * HttpClient工具类，用于发送HTTP请求
// */
//public class HttpClientUtil {
//
//    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
//
//    // 默认超时时间（毫秒）
//    private static final int CONNECT_TIMEOUT = 5000;
//    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;
//    private static final int SOCKET_TIMEOUT = 5000;
//
//    // 单例模式的CloseableHttpClient实例
//    private static final CloseableHttpClient httpClient;
//
//    static {
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setConnectTimeout(CONNECT_TIMEOUT)
//                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
//                .setSocketTimeout(SOCKET_TIMEOUT)
//                .build();
//
//        httpClient = HttpClients.custom()
//                .setDefaultRequestConfig(requestConfig)
//                .build();
//    }
//
//    /**
//     * 发送GET方式请求
//     *
//     * @param url      请求URL
//     * @param paramMap 请求参数（可为null）
//     * @return 响应内容，如果请求失败或响应状态码不为200，则返回空字符串
//     */
//    public static String doGet(String url, Map<String, String> paramMap) {
//        String result = "";
//        try {
//            URIBuilder builder = new URIBuilder(url);
//            if (paramMap != null) {
//                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
//                    builder.addParameter(entry.getKey(), entry.getValue());
//                }
//            }
//            URI uri = builder.build();
//
//            HttpGet httpGet = new HttpGet(uri);
//            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
//                int statusCode = response.getStatusLine().getStatusCode();
//                if (statusCode == 200) {
//                    result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
//                } else {
//                    logger.warn("GET请求失败，URL：{}，状态码：{}", url, statusCode);
//                }
//            }
//        } catch (Exception e) {
//            logger.error("执行GET请求时发生异常，URL：{}", url, e);
//        }
//        return result;
//    }
//
//    /**
//     * 发送POST方式请求，参数为键值对形式
//     *
//     * @param url      请求URL
//     * @param paramMap 请求参数（可为null）
//     * @return 响应内容，如果请求失败或响应状态码不为200，则返回空字符串
//     */
//    public static String doPost(String url, Map<String, String> paramMap) {
//        String result = "";
//        HttpPost httpPost = new HttpPost(url);
//
//        if (paramMap != null) {
//            List<NameValuePair> paramList = new ArrayList<>();
//            for (Map.Entry<String, String> param : paramMap.entrySet()) {
//                paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
//            }
//            try {
//                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8);
//                httpPost.setEntity(entity);
//            } catch (Exception e) {
//                logger.error("设置POST请求参数时发生异常，URL：{}", url, e);
//                return result;
//            }
//        }
//
//        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
//            int statusCode = response.getStatusLine().getStatusCode();
//            if (statusCode == 200) {
//                result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
//            } else {
//                logger.warn("POST请求失败，URL：{}，状态码：{}", url, statusCode);
//            }
//        } catch (Exception e) {
//            logger.error("执行POST请求时发生异常，URL：{}", url, e);
//        }
//
//        return result;
//    }
//
//    /**
//     * 发送POST方式请求，参数为JSON格式
//     *
//     * @param url      请求URL
//     * @param paramMap 请求参数（可为null）
//     * @return 响应内容，如果请求失败或响应状态码不为200，则返回空字符串
//     */
//    public static String doPost4Json(String url, Map<String, String> paramMap) {
//        String result = "";
//        HttpPost httpPost = new HttpPost(url);
//
//        if (paramMap != null) {
//            JSONObject jsonObject = new JSONObject();
//            for (Map.Entry<String, String> param : paramMap.entrySet()) {
//                jsonObject.put(param.getKey(), param.getValue());
//            }
//            StringEntity entity = new StringEntity(jsonObject.toString(), StandardCharsets.UTF_8);
//            entity.setContentType("application/json");
//            httpPost.setEntity(entity);
//        }
//
//        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
//            int statusCode = response.getStatusLine().getStatusCode();
//            if (statusCode == 200) {
//                result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
//            } else {
//                logger.warn("POST-JSON请求失败，URL：{}，状态码：{}", url, statusCode);
//            }
//        } catch (Exception e) {
//            logger.error("执行POST-JSON请求时发生异常，URL：{}", url, e);
//        }
//
//        return result;
//    }
//
//    /**
//     * 构建请求配置
//     *
//     * @return RequestConfig实例
//     */
//    private static RequestConfig builderRequestConfig() {
//        return RequestConfig.custom()
//                .setConnectTimeout(CONNECT_TIMEOUT) // 设置连接超时时间
//                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT) // 设置从连接池获取连接的超时时间
//                .setSocketTimeout(SOCKET_TIMEOUT) // 设置请求获取数据的超时时间
//                .build();
//    }
//}
