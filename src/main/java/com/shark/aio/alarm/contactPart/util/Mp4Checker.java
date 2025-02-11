package com.shark.aio.alarm.contactPart.util;/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/18 0018 20:16
 */

import java.io.*;
import java.nio.ByteBuffer;

/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/18 0018 20:16
 */
public  class Mp4Checker {
    public  static boolean check(String filePath) {
        long total = 0;
        try (InputStream is = new FileInputStream(filePath)) {
            long realSize = new File(filePath).length();

            boolean readLarge = false;
            long size;
            byte[] buff = new byte[8];
            while (is.read(buff,0,buff.length) != -1) {
                ByteBuffer wrap = ByteBuffer.wrap(buff);
                if (readLarge) {
                    size = wrap.getLong();
                }else {
                    size = Integer.toUnsignedLong(wrap.getInt());
                }

                if (size == 0) {
                    break;
                }
                if (size == 1) {
                    //读取largeSize
                    readLarge = true;
                }else {
                    total += size;
                    long skip;
                    if (readLarge) {
                        skip = size - 16;//跳过size + type + largeSize
                    }else {
                        skip = size - 8;
                    }
                    if (skip > 0) {
                        is.skip(skip);
                    }
                    readLarge = false;
                }
            }
            return realSize==total?true:false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
