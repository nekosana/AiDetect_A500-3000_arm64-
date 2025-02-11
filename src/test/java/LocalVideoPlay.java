/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/16 0016 10:29
 */

import com.aliyun.apache.hc.core5.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/16 0016 10:29
 */
@Slf4j
public class LocalVideoPlay {
    @Test
    public void playVideo(String path, HttpServletRequest request, HttpServletResponse response) {
        //获取视频文件流
        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            fileInputStream = new FileInputStream(new File(path));
            byte[] cache = new byte[1024];
            response.setHeader(HttpHeaders.CONTENT_TYPE, "video/mp4");
            response.setHeader(HttpHeaders.CONTENT_LENGTH, fileInputStream.available() + "");
            int flag;
            while ((flag = fileInputStream.read(cache)) != -1) {
                outputStream.write(cache, 0, flag);
            }
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            log.error("文件传输错误", e);
            throw new RuntimeException("文件传输错误");
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("流释放错误", e);
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.error("文件流释放错误", e);
                }
            }

        }
    }
}
