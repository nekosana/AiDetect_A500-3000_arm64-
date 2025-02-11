package com.shark.aio.alarm.contactPart.util;

import java.lang.reflect.Field;

public class ObjectUtil {

    /**
     * 判断一个实例对象的成员变量是否全部为null
     * @param obj 实例对象
     * @return 若全为null返回true，否则返回false
     */
    public static boolean isEmpty(Object obj){
        if(obj==null) return true;
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f: fields){
            f.setAccessible(true);
            Object field = null;
            try {
                field  = f.get(obj);
            }catch (IllegalAccessException e){
                e.printStackTrace();
            }
            if (field!=null){
                return false;
            }
        }
        return true;
    }

    public static boolean isEmptyString(String str){
        return str==null||str.equals("");
    }
}
