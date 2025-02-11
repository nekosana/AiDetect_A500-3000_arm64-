package com.shark.aio.alarm.contactPart.util;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {

	/**
	 * 判断文件夹是否存在，不存在就创建
	 * @param dirPath
	 * @return
	 */
	public static void isDirExist(String dirPath) {
		File localPath = new File(dirPath);
		if (!localPath.exists()) { // 获得文件目录，判断目录是否存在，不存在就新建一个
			localPath.mkdirs();
			log.info("FileUtil, 创建文件夹：{}", localPath);
		}
	}
}
