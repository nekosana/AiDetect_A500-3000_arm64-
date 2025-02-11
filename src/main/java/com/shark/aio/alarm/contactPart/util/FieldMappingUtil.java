package com.shark.aio.alarm.contactPart.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 字段映射工具类，将原始字段名映射为中文名称
 */
public class FieldMappingUtil {

    private static final Map<String, String> fieldMap = new HashMap<>();

    static {
        //间隔按小时算，周期按天算
        fieldMap.put("spanTimeY", "跨度时间Y");
        fieldMap.put("range", "范围");
        fieldMap.put("accuracy", "准确度");
        fieldMap.put("absorbanceTwo", "吸光度二");
        fieldMap.put("valueStatus", "数值状态");
        fieldMap.put("zeroTimeY", "零点时间Y");
        fieldMap.put("calibrationMethod", "校准方法");
        fieldMap.put("warning", "警告");
        fieldMap.put("spanValue", "跨度值");
        fieldMap.put("value", "数值");
        fieldMap.put("dissolveTime", "溶解时间");
        fieldMap.put("zeroTImeM", "零点时间M");
        fieldMap.put("concentrationTwo", "浓度二");
        fieldMap.put("absorbance", "吸光度");
        fieldMap.put("spanTImeM", "跨度时间M");
        fieldMap.put("CF", "CF");
        fieldMap.put("zeroValue", "零点值");
        fieldMap.put("zeroTimeF", "零点时间F");
        fieldMap.put("zeroTimeH", "零点时间H");
        fieldMap.put("spanTimeD", "跨度时间D");
        fieldMap.put("deviation", "偏差");
        fieldMap.put("zeroTimeS", "零点时间S");
        fieldMap.put("spanTimeF", "跨度时间F");
        fieldMap.put("slope", "斜率");
        fieldMap.put("absorbanceOne", "吸光度一");
        fieldMap.put("zeroStatus", "零点状态");
        fieldMap.put("intercept", "截距");
        fieldMap.put("spanTimeS", "跨度时间S");
        fieldMap.put("calibrationInterval", "校准间隔");
        fieldMap.put("concentrationOne", "浓度一");
        fieldMap.put("zeroTimeD", "零点时间D");
        fieldMap.put("spanStatus", "跨度状态");
        fieldMap.put("dissolveTemperature", "溶解温度");
        fieldMap.put("status", "状态");
        fieldMap.put("cleaningCycle", "清洗周期");
        fieldMap.put("triggerSelfCalibration", "触发自校准");
        fieldMap.put("calibrationStartTime", "校准开始时间");
        fieldMap.put("standardSolutionMeasurementLightIntensity", "标准溶液测量光强度");
        fieldMap.put("calibrationCycle", "校准周期");
        fieldMap.put("referenceLightIntensityOfStandardSolution", "标准溶液参考光强度");
        fieldMap.put("enterService", "进入服务");
        fieldMap.put("interval", "间隔");
        fieldMap.put("triggerCleaning", "触发清洗");
        fieldMap.put("cleaningStartTime", "清洗开始时间");
        fieldMap.put("measuringLightIntensity", "测量光强度");
        fieldMap.put("measurementResult", "测量结果");
        fieldMap.put("referenceLightIntensity", "参考光强度");
        fieldMap.put("temperature", "温度");
        fieldMap.put("zeroScaleMeasurementLightIntensity", "零点标定光强度");
        fieldMap.put("triggerMeasurement", "触发测量");
        fieldMap.put("correctionOffset", "校正偏移");
        fieldMap.put("correctionSlope", "校正斜率");
        fieldMap.put("instrumentStatus", "仪器状态");
        fieldMap.put("zeroReferenceLightIntensity", "零点参考光强度");
        fieldMap.put("measuringAbsorbance", "测量吸光度");
        fieldMap.put("lastCalibrationTime", "最后校准时间");
        fieldMap.put("lastCleaningTime", "最后清洗时间");
        fieldMap.put("systemtime", "系统时间");
        fieldMap.put("measurementMode", "测量方法");
        fieldMap.put("time", "时间");

    }

    /**
     * 获取中文字段名
     *
     * @param originalField 原始字段名
     * @return 中文字段名，如果没有映射则返回原始字段名
     */
    public static String getChineseFieldName(String originalField) {
        return fieldMap.getOrDefault(originalField, originalField);
    }
}