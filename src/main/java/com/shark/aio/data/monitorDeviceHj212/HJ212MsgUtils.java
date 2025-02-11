package com.shark.aio.data.monitorDeviceHj212;

import com.alibaba.fastjson.JSONObject;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author DaHuaJia
 * @Description HJ212消息数据处理
 * HJ212数据示例：##0746ST=31;CN=2061;PW=123456;MN=7568770259402;Flag=0;CP=&&DataTime=20221008100000;B02-Min=1.6960,B02-Avg=3.0586,B02-Max=3.7704,B02-Cou=11010.8437;S01-Min=17.7469,S01-Avg=19.4636,S01-Max=19.6944;S02-Min=3.2459,S02-Avg=5.6705,S02-Max=6.9578;S03-Min=30.0434,S03-Avg=30.2675,S03-Max=30.4503;S08-Min=-0.4643,S08-Avg=-0.3541,S08-Max=0.0000;S05-Min=6.1814,S05-Avg=6.8655,S05-Max=7.0097;a24088-Min=2.4957,a24088-Avg=3.2176,a24088-Max=4.7744,a24088-Cou=0.0354;25-Min=6.4292,25-Avg=12.1457,25-Max=20.0606,25-Cou=0.1336;a05002-Min=2.7715,a05002-Avg=7.8561,a05002-Max=14.7934,a05002-Cou=0.0863;17-Min=0.0000,17-Avg=0.0000,17-Max=0.0000,17-Cou=0.0000;18-Min=0.0000,18-Avg=0.0000,18-Max=0.0000,18-Cou=0.0000;16-Min=0.0000,16-Avg=0.0000,16-Max=0.0000,16-Cou=0.0000&&7BC0
 * ##0169ST=32;CN=3020;PW=123456;MN=010000A8900C0;Flag=5;CP=&&DataTime=20100301145000;PolId=w01018;i12001-Info=%d;i12002-Info=%d;i12003-Info=%d;i22001-Info=%d;i32001-Info=%d&&cd41\r\n
 * @Date 2022-10-09 11:09:16
 */
@Slf4j
public class HJ212MsgUtils {

//    private static JedisPool pool = null;
//
//    /*** 获取jedis连接池* */
//    public static JedisPool getPool() {
//        if (pool == null) {
//            //创建jedis连接池配置
//            JedisPoolConfig config = new JedisPoolConfig();
//            // 最大连接数
//            config.setMaxTotal(100);
//            // 最大空闲连接
//            config.setMaxIdle(5);
//            //创建redis连接池
//            pool = new JedisPool(config,"127.0.0.1",6379);
//        }
//        return pool;
//    }

    /**
     * 传入HJ212数据，返回简单json数据，没有对污染源进行分类汇总。
     *
     * @param msg hj212
     * @return json
     */
    public static JSONObject dealMsg1(Jedis jedis, String msg) {

        if (msg != null && !msg.contains("SIZE") && !msg.contains("++") && !msg.contains("AT")) {
            JSONObject data = new JSONObject();
            try {
                // 拆分消息
                //##0746ST=31;CN=2061;PW=123456;MN=7568770259402;Flag=0;CP=
                //&&DataTime=20221008100000;B02-Min=1.6960,B02-Avg=3.0586,B02-Max=3.7704,B02-Cou=11010.8437;S01-Min=17.7469,S01-Avg=19.4636,S01-Max=19.6944;S02-Min=3.2459,S02-Avg=5.6705,S02-Max=6.9578;S03-Min=30.0434,S03-Avg=30.2675,S03-Max=30.4503;S08-Min=-0.4643,S08-Avg=-0.3541,S08-Max=0.0000;S05-Min=6.1814,S05-Avg=6.8655,S05-Max=7.0097;a24088-Min=2.4957,a24088-Avg=3.2176,a24088-Max=4.7744,a24088-Cou=0.0354;25-Min=6.4292,25-Avg=12.1457,25-Max=20.0606,25-Cou=0.1336;a05002-Min=2.7715,a05002-Avg=7.8561,a05002-Max=14.7934,a05002-Cou=0.0863;17-Min=0.0000,17-Avg=0.0000,17-Max=0.0000,17-Cou=0.0000;18-Min=0.0000,18-Avg=0.0000,18-Max=0.0000,18-Cou=0.0000;16-Min=0.0000,16-Avg=0.0000,16-Max=0.0000,16-Cou=0.0000
                //&&7BC0"));
                String[] subMsg = msg.split("&&");

                // 清洗消息头基本数据
                String headStr = subMsg[0].substring(2).replace(";CP=", "").replace("=", "\":\"")
                        .replace(",", "\",\"").replace(";", "\",\"");
                data.put("SIZE", headStr.substring(0, 4));
                data.putAll(JSONObject.parseObject("{\"" + headStr.substring(4) + "\"}"));

                boolean flag = false;
                String a = null;
                if (!subMsg[1].contains("DataTime")) {
                    flag = true;
                    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    a = timeFormat.format(new Date());
                }
                // 清洗消息头基本数据
                //分号或者逗号一分
                String[] monitors = subMsg[1].split(";|,");
                //monitors例子
                //DataTime":"20221008100000
                //B02-Min":"1.6960
                JSONObject cp = new JSONObject();

                for (String obj : monitors) {
                    String paramStr = obj.replace("=", "\":\"").replace(",", "\",\"")
                            .replace(";", "\",\"");
                    // 如果是时间信息，则直接放到外层
                    if (paramStr.contains("DataTime") || flag) {
                        // 数据包里有DataTime,进行转换，没有则走的上面定义当前时间
                        if (!flag) {
                            //QN转换成日期格式
                            String dataTime = paramStr.substring(paramStr.indexOf(":") + 2);
                            String dataTime2 = dataTime.substring(0, 14);
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                            SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = formatter.parse(dataTime2);
                            a = timeFormat.format(date);
                            timeFormat.format(new Date());
                        }

                        data.put("DataTime", a);
                    } else if (paramStr.contains("PoIId")) {
                        data.putAll(JSONObject.parseObject("{\"" + paramStr + "\"}"));
                    } else {
                        if (paramStr.contains("Flag") || paramStr.contains("RS")) {
                            continue;
                        } else {
                            String[] chineseField = setChineseField(jedis, paramStr);
                            cp.put(chineseField[0], chineseField[1]);
                        }

                    }
                }
                //如果数据字段为空，不记录
                if (cp.isEmpty()) {
//                    cp.put("data", "无数据");
                    return null;
                }

                data.put("CP", cp);
                // 保存消息尾数据，主要是CRC校验和包结束符
                data.put("End", subMsg[2].substring(0, 4));
                return data;
            } catch (Exception e) {
                log.error("HJ212数据转JSON错误。报错信息：{}，消息内容：{}", e.getMessage(), msg);
                e.printStackTrace();
                return null;
            }

        }
        return null;
    }

    /**
     * 将数据中的
     */
    private static String[] setChineseField(Jedis jedis, String data) {

        String key = data.substring(0, data.indexOf("\""));
        data = data.replaceAll(key + "\":\"", "").replaceAll("\":\"", "");

        try{
            String code = null;
            Boolean exit = false;
            if (key.contains("-")) {
                code = key.substring(0, key.indexOf("-"));
                exit = jedis.exists(code);
                if (exit) {
                    key = key.replace(code, jedis.get(code));
                }
            } else {
                code = key;
                exit = jedis.exists(code);
                if (exit == true) {
                    key = jedis.get(key);
                }
            }


        } catch (Exception e) {
            log.error("redis加入失败！");
        }

        if (key.contains("f21011")) {

            if (key.contains("Ua")) {
                key = "A相电压";
            } else if (key.contains("Ub")) {
                key = "B相电压";
            } else if (key.contains("Uc")) {
                key = "C相电压";
            } else if (key.contains("Ia")) {
                key = "A相电流";
            } else if (key.contains("Ib")) {
                key = "B相电流";
            } else if (key.contains("Ic")) {
                key = "C相电流";
            } else if (key.contains("Pt")) {
                key = "总有功功率";
            } else if (key.contains("Qt")) {
                key = "总无功功率";
            } else if (key.contains("cos")) {
                key = "功率因数";
            } else if (key.contains("Ept")) {
                key = "总有功电量";
            }
        }


        int OC = 0;
        switch (key) {
            case "i13006-Info":
                key = "校零时间";
                break;
            case "il3001-Info":
                key = "测量量程";
                break;
            case  "il3002-Info":
                key = "测量精度";
                break;
            case  "il3007-Info":
                key = "截距";
                break;
            case  "il3008-Info":
                key = "斜率";
                break;
            case  "il3004-Info":
                key = "消解温度";
                break;
            case  "il3005-Info":
                key = "消解时长";
                break;

            case "i33101-Info":
                key = "监测站房温度";
                break;
            case "i33103-Info":
                key = "监测站房电压";
                break;
            case "i13003-Info":
                key = "测量间隔";
                break;

            // 校零参数
            case "xxxxx1-Info":
                key = "校零的标样浓度";
                break;
            case "xxxxx2-Info":
                key = "校零参数中间测量值";
                break;
            case "xxxxx3-Info":
                key = "校零结果标识";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "无";
                        break;
                    case 1:
                        data = "成功";
                        break;
                    case 2:
                        data = "失败";
                        break;
                }
                break;

            //跨度校准参数
            case "xxxxx4-Info":
                key = "输出跨度校准时间";
                break;
            case "xxxxx5-Info":
                key = "跨度校准的标样浓度";
                break;
            case "xxxxx6-Info":
                key = "跨度校准中间测量值";
                break;
            case "xxxxx7-Info":
                key = "跨度校准结果标识";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "无";
                        break;
                    case 1:
                        data = "成功";
                        break;
                    case 2:
                        data = "失败";
                        break;
                }
                break;

            //校满参数
            case "xxxxx8-Info":
                key = "输出校满时间";
                break;
            case "xxxxx9-Info":
                key = "校满的标样浓度";
                break;
            case "xxxxx10-Info":
                key = "校满中间测量值";
                break;
            case "xxxxx11-Info":
                key = "校满结果标识";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "无";
                        break;
                    case 1:
                        data = "成功";
                        break;
                    case 2:
                        data = "失败";
                        break;
                }
                break;

            //标样核查参数
            case "xxxxx12-Info":
                key = "输出标样校验(核查)的标液浓度";
                break;
            case "xxxxx13-Info":
                key = "浓度指示值";
                break;
            case "xxxxx14-Info":
                key = "标样校验(核查)的时间";
                break;
            case "xxxxx15-Info":
                key = "中间测量值";
                break;
            case "xxxxx16-Info":
                key = "标样核查的结果标识";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "无";
                        break;
                    case 1:
                        data = "成功";
                        break;
                    case 2:
                        data = "失败";
                        break;
                }
                break;

            case "xxxxx17-Info":
                key = "自动校准时间间隔";
                break;

            case "xxxxx18-Info":
                key = "标液浓度";
                break;

            case "xxxxx19-Info":
                key = "修正因子";
                break;

            case "xxxxx20-Info":
                key = "修正值";
                break;

            case "xxxxx21-Info":
                key = "自动校验(核查)时间间隔";
                break;

            case "xxxxx22-Info":
                key = "燃烧温度";
                break;

            case "xxxxx23-Info":
                key = "加热温度";
                break;

            case "i12001-Info":
                key = "仪表与板卡通讯状态";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "运行";
                        break;
                    case 1:
                        data = "维护";
                        break;
                    case 2:
                        data = "故障";
                        break;
                }
                break;
            case "i22001-Info":
//                key = "仪表与板卡通讯状态";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "运行";
                        break;
                    case 1:
                        data = "维护";
                        break;
                    case 2:
                        data = "故障";
                        break;
                }
                break;

            case "il2001-Info":
                key = "工作状态";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "运行";
                        break;
                    case 1:
                        data = "维护";
                        break;
                    case 2:
                        data = "故障";
                        break;
                    case 3:
                        data = "校准";
                        break;
                    case 5:
                        data = "反吹";
                        break;
                    case 6:
                        data = "电源故障";
                        break;
                    case 7:
                        data = "测量";
                        break;
                    case 8:
                        data = "标定";
                        break;
                    case 9:
                        data = "待机";
                        break;
                    case 10:
                        data = "运维";
                        break;
                }
                break;

            case "xxxxxx1-Info":
                key = "报警信息";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "无报警";
                        break;
                    case 1:
                        data = "系统故障";
                        break;
                    case 2:
                        data = "电源故障";
                        break;
                    case 3:
                        data = "缺试剂";
                        break;
                    case 4:
                        data = "缺蒸馏水";
                        break;
                    case 5:
                        data = "加热故障";
                        break;
                    case 6:
                        data = "排残液故障";
                        break;
                    case 7:
                        data = "测量值超量程异常";
                        break;
                    case 8:
                        data = "其他故障";
                        break;
                    case 9:
                        data = "采集水样、试剂超时";
                        break;
                    case 10:
                        data = "其他报警";
                        break;
                }
                break;

            case "xxxxxx2-Info":
                key = "校准方式";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "人工校准";
                        break;
                    case 1:
                        data = "自动校准";
                        break;
                }
                break;

            case "xxxxxx3-Info":
                key = "比色法光源";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "单光谱仪";
                        break;
                    case 1:
                        data = "多光谱仪";
                        break;
                }
                break;

            case "xxxxxx4-Info":
                key = "校验(核查)方式";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "人工校验";
                        break;
                    case 1:
                        data = "自动校验";
                        break;
                }
                break;

            case "i42002-Info":
                key = "仪表与板卡通讯状态";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "正常";
                        break;
                    case 1:
                        data = "异常";
                        break;
                }
                break;
            case "i42001-Info":
                key = "自定义板卡工作状态";
                OC = Integer.parseInt(data);
                switch (OC) {
                    case 0:
                        data = "运行";
                        break;
                    case 1:
                        data = "停机";
                        break;
                    case 2:
                        data = "故障";
                        break;
                    case 3:
                        data = "维护";
                        break;
                }
                break;
        }


        String[] result = new String[2];
        result[0] = key;
        result[1] = data;

        return result;
    }

    /**
     * 传入HJ212数据，返回复杂json数据，对污染源进行分类汇总，不同污染源用不同的json key。
     *
     * @param msg hj212
     * @return json
     */
    public static JSONObject dealMsg2(String msg) {
        JSONObject data = new JSONObject();
        try {
            // 拆分消息
            String[] subMsg = msg.split("&&");

            // 清洗消息头基本数据
            String headStr = subMsg[0].substring(2).replace(";CP=", "").replace("=", "\":\"")
                    .replace(",", "\",\"").replace(";", "\",\"");
            data.put("SIZE", headStr.substring(0, 4));
            data.putAll(JSONObject.parseObject("{\"" + headStr.substring(4) + "\"}"));

            // 清洗数据体基本数据
            String[] monitors = subMsg[1].split(";");
            JSONObject cp = new JSONObject();
            for (String obj : monitors) {
                String paramStr = obj.replace("=", "\":\"").replace(",", "\",\"")
                        .replace(";", "\",\"");
                // 如果是时间信息，则直接放到外层
                if (paramStr.contains("DataTime")) {
                    data.putAll(JSONObject.parseObject("{\"" + paramStr + "\"}"));
                } else if (paramStr.contains("PoIId")) {
                    data.putAll(JSONObject.parseObject("{\"" + paramStr + "\"}"));
                } else {
                    String[] ele = getPollutionSource(paramStr);
                    cp.put(ele[0], JSONObject.parseObject("{\"" + ele[1] + "\"}"));
                }
            }
            data.put("CP", cp);

            // 保存消息尾数据，主要是CRC校验和包结束符
            data.put("End", subMsg[2]);

        } catch (Exception e) {
            log.error("HJ212数据转JSON错误。报错信息：{}，消息内容：{}", e.getMessage(), msg);
            e.printStackTrace();
        }
        return data;
    }


    /**
     * 解析污染源数据，获取污染源编号
     */
    private static String[] getPollutionSource(String data) {
        String key = data.substring(0, data.indexOf("-"));
//        System.out.println("key" + key);
        data = data.replaceAll(key + "-", "");

        String[] result = new String[2];
        result[0] = key;
        result[1] = data;
        return result;
    }

    /**
     * 校验CRC
     *
     * @param data212
     * @return
     */
    public static String checkData(String data212) {
        try {
            if (!(data212.contains("CP") && data212.contains("\r\n") && data212.contains("#"))) {
//                System.out.println("数据包不完整"+data212);
                return "error";
            }
            ;
            String input = data212.substring(data212.indexOf("QN"), data212.indexOf("\r") - 4);

            //长度校验
            String length = Integer.toString(input.length());
            String lengthCheck = null;
            if (length.length() == 3) {
                lengthCheck = "0" + length;
            } else if (length.length() == 2) {
                lengthCheck = "00" + length;
            } else if (length.length() == 1) {
                lengthCheck = "000" + length;
            }
            if (!data212.substring(2, 6).equals(lengthCheck)) {
                System.out.println("CRC校验失败");
                return "error2";
            }
            ;

            int CRC = 0xFFFF;
            int num = 0xA001;
            int inum = 0;
            byte[] sb = input.getBytes();
            for (int j = 0; j < sb.length; j++) {
                inum = sb[j];
                CRC = (CRC >> 8) & 0x00FF;
                CRC ^= inum;
                for (int k = 0; k < 8; k++) {
                    int flag = CRC % 2;
                    CRC = CRC >> 1;

                    if (flag == 1) {
                        CRC = CRC ^ num;
                    }
                }
            }
            String CRCString = Integer.toHexString(CRC);
            String CRCCheck = null;
            String CRCData = data212.substring(data212.indexOf("\r") - 4, data212.indexOf("\r"));
            if (CRCString.length() == 3) {
                CRCCheck = "0" + CRCString;
            } else if (CRCString.length() == 2) {
                CRCCheck = "00" + CRCString;
            } else if (CRCString.length() == 1) {
                CRCCheck = "000" + CRCString;
            } else {
                CRCCheck = CRCString;
            }
            //转成大写toUpperCase
            CRCCheck = CRCCheck.toUpperCase();

//            System.out.println(CRCCheck);
//            System.out.println(CRCData);
            if (!CRCCheck.equals(CRCData)) {
                return "error3";
            }
            return CRCCheck;
        } catch (Exception e) {
            log.error("CRC校验失败！", e);
            return "error4";
        }

    }


    public static void main(String[] args) throws IOException, WriteException, BiffException {
//        System.out.println(dealMsg1("##0101QN=20160801085857223;ST=32;CN=1062;PW=100000;MN=010000A8900016F000169DC0;Flag=5\n" +
//                ";CP=&&RtdInterval=30&&1C80\r\n"));
//        System.out.println(dealMsg1("##0746ST=31;CN=2061;PW=123456;MN=7568770259402;Flag=0;CP=&&DataTime=20221008100000;B02-Min=1.6960,B02-Avg=3.0586,B02-Max=3.7704,B02-Cou=11010.8437;S01-Min=17.7469,S01-Avg=19.4636,S01-Max=19.6944;S02-Min=3.2459,S02-Avg=5.6705,S02-Max=6.9578;S03-Min=30.0434,S03-Avg=30.2675,S03-Max=30.4503;S08-Min=-0.4643,S08-Avg=-0.3541,S08-Max=0.0000;S05-Min=6.1814,S05-Avg=6.8655,S05-Max=7.0097;a24088-Min=2.4957,a24088-Avg=3.2176,a24088-Max=4.7744,a24088-Cou=0.0354;25-Min=6.4292,25-Avg=12.1457,25-Max=20.0606,25-Cou=0.1336;a05002-Min=2.7715,a05002-Avg=7.8561,a05002-Max=14.7934,a05002-Cou=0.0863;17-Min=0.0000,17-Avg=0.0000,17-Max=0.0000,17-Cou=0.0000;18-Min=0.0000,18-Avg=0.0000,18-Max=0.0000,18-Cou=0.0000;16-Min=0.0000,16-Avg=0.0000,16-Max=0.0000,16-Cou=0.0000&&7BC0"));
//
//        System.out.println("=============" + (new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS")).format(new Date()) + "=============");
//        System.out.println(checkData("##0746ST=31;CN=2061;PW=123456;MN=7568770259402;Flag=0;CP=&&DataTime=20221008100000;B02-Min=1.6960,B02-Avg=3.0586,B02-Max=3.7704,B02-Cou=11010.8437;S01-Min=17.7469,S01-Avg=19.4636,S01-Max=19.6944;S02-Min=3.2459,S02-Avg=5.6705,S02-Max=6.9578;S03-Min=30.0434,S03-Avg=30.2675,S03-Max=30.4503;S08-Min=-0.4643,S08-Avg=-0.3541,S08-Max=0.0000;S05-Min=6.1814,S05-Avg=6.8655,S05-Max=7.0097;a24088-Min=2.4957,a24088-Avg=3.2176,a24088-Max=4.7744,a24088-Cou=0.0354;25-Min=6.4292,25-Avg=12.1457,25-Max=20.0606,25-Cou=0.1336;a05002-Min=2.7715,a05002-Avg=7.8561,a05002-Max=14.7934,a05002-Cou=0.0863;17-Min=0.0000,17-Avg=0.0000,17-Max=0.0000,17-Cou=0.0000;18-Min=0.0000,18-Avg=0.0000,18-Max=0.0000,18-Cou=0.0000;16-Min=0.0000,16-Avg=0.0000,16-Max=0.0000,16-Cou=0.0000&&7BC0"));
//
//        System.out.println(checkData("##0255QN=20230706182702378;ST=80;CN=2011;PW=123456;MN=010000A8900016F000169DC1;Flag=5;CP=&&DataTime=20230706182700;f21011-Ua=222.0,f21011-Ub=0.1,f21011-Uc=0.1,f21011-Ia=0.21,f21011-Ib=0.02,f21011-Ic=0.02,f21011-Pt=18,f21011-Qt=13,f21011-cos=0.811,f21011-Ept=0&&9841\r\n"));

//        String a = "##0255QN=20230706182702378;ST=80;CN=2011;PW=123456;MN=010000A8900016F000169DC1;Flag=5;CP=&&DataTime=20230706182700;f21011-Ua=222.0,f21011-Ub=0.1,f21011-Uc=0.1,f21011-Ia=0.21,f21011-Ib=0.02,f21011-Ic=0.02,f21011-Pt=18,f21011-Qt=13,f21011-cos=0.811,f21011-Ept=0&&9841\\r\\n";
//        System.out.println(dealMsg1(a));
//        System.out.println(dealMsg2(a));


//        FileInputStream fin = new FileInputStream("C:\\Users\\lbx\\Desktop\\11111.txt");
//        InputStreamReader reader = new InputStreamReader(fin, "utf-8");
//        BufferedReader buffReader = new BufferedReader(reader);
//        String strTmp = "";
//        ArrayList<JSONObject> data = new ArrayList<>();
//        ArrayList<String> keyset = new ArrayList<>();
//        List<String> keyset_notime = new ArrayList<>();
//        JSONObject result = new JSONObject();
//       int a = 0;
//        long b = System.currentTimeMillis();
//        while ((strTmp = buffReader.readLine()) != null) {
//            a++;
//        }
//        long c = System.currentTimeMillis();
//        long d= c-b;
//        System.out.println("文件读取消耗的时间：" + d);
//        System.out.println(a);
//        buffReader.close();
//
//        b = System.currentTimeMillis();
////        alarmMapping.getAllAlarmRecordsMedium();
//        c = System.currentTimeMillis();
//        d= c-b;
//        System.out.println("数据库读取消耗的时间："+ d);
//        System.out.println(a);
//
//
//        System.out.println("文件读取消耗的时间：9 毫秒" );
//        System.out.println("文件数据量（行）：12840" );
//
//        System.out.println("数据库读取消耗的时间：37 毫秒" );
//        System.out.println("数据库数据量（条）：12840" );
//        System.out.println("2024-03-13 21:28:17 查询污染源监测2024-03-11成功!");


//        File monitor = new File(Constants.CONDITIONPATH);
////        if (!monitor.exists()) monitor.mkdirs();
//        File[] monitorfiles = monitor.listFiles();
//        if (monitorfiles.length == 0) {
//
//        } else {
//            String[] fileList = new String[monitorfiles.length];
//            for (int i = 0; i < monitorfiles.length; i++) {
////                fileList[i] = monitorfiles[i].getName();
//
//                File time = new File(Constants.CONDITIONPATH + monitorfiles[i].getName());
//                File[] timefiles = time.listFiles();
//                if (timefiles == null) {
//
//                } else {
//                    String[] timefileList = new String[timefiles.length];
//                    for (int j = 0; j < timefiles.length; j++) {
//                        timefileList[j] = timefiles[j].getName();
//
//                        File time = new File(Constants.CONDITIONPATH + monitorfiles[i].getName() + "/" + timefiles[j].getName());
//                    }
//
//                }
//
//
//            }
//
//        }







        /**
         * 将原始数据的某个参数摘出来
         */
//        String[] titleA = {"时间", "监测值"};
//        //创建Excel文件，B库CD表文件
//        File fileA = new File("C:\\Users\\lbx\\Desktop\\非甲烷总烃-Max.xls");
//        if (fileA.exists()) {
//            //如果文件存在就删除
//            fileA.delete();
//        }
//
//        fileA.createNewFile();
//        //创建工作簿
//        WritableWorkbook workbookA = Workbook.createWorkbook(fileA);
//        //创建sheet
//        WritableSheet sheetA = workbookA.createSheet("sheet1", 0);
//        Label labelA = null;
//        //设置列名
////        for (int i = 0; i < titleA.length; i++) {
////            labelA = new Label(i, 0, titleA[i]);
////            sheetA.addCell(labelA);
////        }
//        long i = 0;
//
//        int k = 0;
//
//
//        File time = new File(Constants.POLLUTIONPATH + "厂房");
//        File[] timefiles = time.listFiles();
//        if (timefiles == null) {
//
//        } else {
//
//            for (int j = 0; j < timefiles.length; j++) {
////                timefileList[j] = timefiles[j].getName();
//
//                String path = Constants.POLLUTIONPATH + "厂房" + "\\" + timefiles[j].getName() + "\\" + "污染源监测.txt";
//
//
//                FileInputStream fin = new FileInputStream(path);
//                InputStreamReader reader = new InputStreamReader(fin, "utf-8");
//                BufferedReader buffReader = new BufferedReader(reader);
//                String strTmp = "";
//
//                while ((strTmp = buffReader.readLine()) != null) {
////                    ConditionController.sample(buffReader, length);
//                    JSONObject jsonObject = JSON.parseObject(strTmp);
//                    if (jsonObject != null) {
//                        JSONObject dataObj = jsonObject.getJSONObject("CP");
//                        if (dataObj.containsKey("非甲烷总烃-Max")) {
//                            try {
//                                //获取数据源
//
//                                labelA = new Label(k, Math.toIntExact(i), jsonObject.getString("DataTime"));
//                                sheetA.addCell(labelA);
//                                labelA = new Label(k + 1, Math.toIntExact(i), dataObj.getString("非甲烷总烃-Max"));
//                                sheetA.addCell(labelA);
//                                i++;
//                                if (i == 50000) {
//                                    i = 0;
//                                    k += 2;
//                                }
////
//
//                            } catch (Exception e) {
//                            }
//
//                        }
//
//                    }
//                }
//                System.out.println("完成写入" + timefiles[j].getName());
////        System.out.println(out);
//                buffReader.close();
//
//
//            }
//            workbookA.write();    //写入数据
//            workbookA.close();  //关闭连接
//        }






        /**
         * 小时平均数据
         */
        /**
         * 文件输出位置
         */
        File file = new File("C:\\Users\\lbx\\Desktop\\非甲烷总烃-Max-小时平均数据5.xls");
        if (file.exists()) {
            //如果文件存在就删除
            file.delete();
        }
        file.createNewFile();

        //创建工作簿
        WritableWorkbook workbookA = Workbook.createWorkbook(file);
        //创建sheet
        WritableSheet sheetA = workbookA.createSheet("sheet1", 0);
        Label labelA = null;

        /**
         * 取数据的地方
         */
        File fileA = new File("C:\\Users\\lbx\\Desktop\\非甲烷总烃-Max.xls");


        // 解析路径的file文件
        Workbook workbook = Workbook.getWorkbook(fileA);
        // 获取第一张工作表
        Sheet sheet = workbook.getSheet(0);
        // 循环获取每一行数据 因为默认第一行为标题行，我们可以从 1 开始循环，如果需要读取标题行，从 0 开始
        // sheet.getRows() 获取总行数
        double nums = 2.668;
        //一小时有几个数据
        int k = 1;
        //新的excel第几行
        int j = 0;
        //读取excel第几列
        int q = 0;

        System.out.println(sheet.getCell(q, 0).getContents().substring(0,14));

        for (int i = 0; i < 23323 - 1; i++) {
            // 获取第一列的第 i 行信息 sheet.getCell(列，行)，下标从0开始
            String time = sheet.getCell(q, i).getContents();
            String time2 = sheet.getCell(q, i + 1).getContents();
            String value = sheet.getCell(q + 1, i).getContents();
            String value2 = sheet.getCell(q + 1, i + 1).getContents();

            if (time.charAt(11) == time2.charAt(11) && time.charAt(12) == time2.charAt(12)) {

                nums += Double.parseDouble(value2);
                k++;

            } else {

                labelA = new Label(q, Math.toIntExact(j), time.substring(0,14) + "00:00");
                sheetA.addCell(labelA);
                labelA = new Label(q + 1, Math.toIntExact(j),String.format("%.3f", nums/k));
                sheetA.addCell(labelA);

                j++;
                nums = Double.parseDouble(value2);
                k = 1;
            }
            // 获取第二列的第 i 行信息

            // 获取第三列的第 i 行信息
//            String sex = sheet.getCell(2, i).getContents();
//            // 获取第四列的第 i 行信息
//            String grade = sheet.getCell(3, i).getContents();
            // 存入本地或者是存入对象等根据给人需求自己定就行,创建对象存储，然后加入集合中
            // ......
        }
        workbookA.write();    //写入数据
        workbookA.close();  //关闭连接
    }

}
