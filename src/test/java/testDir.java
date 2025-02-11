/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/18 0018 10:23
 */

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/18 0018 10:23
 */
public class testDir{

    public class MyMatch implements FilenameFilter {
        private Pattern p;
        public MyMatch(String regex){
            p = Pattern.compile(regex);
        }
        @Override
        public boolean accept(File dir, String name) {
            return p.matcher(name).matches();
        }

    }

    @Test
    public void getDir(){
//        String regex = "\\d{4}-\\d{2}-\\d{2}-\\d{2}";
        String regex = "2024-12-18-10";
        MyMatch myMatch = new MyMatch(regex);
        File file = new File("D:\\项目\\AIO\\localvideo");
        File[] files = file.listFiles();
        for (File fil :
                files) {
            System.out.println("路径： " + fil.getPath());
            System.out.println(myMatch.accept(fil,regex));
        }

    }
    @Test
    public void testCoopy() throws Exception{
        String inputFile = "D:\\项目\\AIO\\localvideo\\测试摄像头2-2024-12-18-10-45-21.mp4";
        String outputFile="D:\\项目\\AIO\\waringVideo\\测试摄像头2-2024-12-18-10-45-21.mp4";
        BufferedOneArrayOne(inputFile,outputFile);
    }
    public static void BufferedOneArrayOne(String inputFile,String outputFile) throws
            IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
        byte[] bytes = new byte[2048];
        int len;
        while ((len = bis.read(bytes)) != -1) {
            bos.write(bytes,0,len);
        }
        bis.close();
        bos.close();
    }


}






