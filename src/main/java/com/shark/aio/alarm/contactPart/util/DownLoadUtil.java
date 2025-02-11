package com.shark.aio.alarm.contactPart.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
@Slf4j
public class DownLoadUtil {
	  /**
     * 下载压缩包
     * @param file
     * @param response
     * @return
     */
    public static HttpServletResponse downloadZip(File file,HttpServletResponse response) {
        InputStream fis = null;
        OutputStream toClient = null;
        try {
            // 以流的形式下载文件。
             fis = new BufferedInputStream(new FileInputStream(file.getPath()));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            // 清空response
            response.reset();
             toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            String fileName = getFileName(file.getPath());
            //如果输出的是中文名的文件，在此处就要用URLEncoder.encode方法进行处理
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            toClient.write(buffer);
            toClient.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }finally{
            try {
                File f = new File(file.getPath());
                f.delete();
               if(fis!=null){
                   fis.close();
               }
               if(toClient!=null){
                   toClient.close();
               }
     
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return response;
    }
    /**
	 * 拿到文件url的文件名
	 * 
	 * @param path
	 * @return
	 */
	private static String getFileName(String path) {
		String[] split = path.split("/");
		return split[split.length - 1];
	}
}
