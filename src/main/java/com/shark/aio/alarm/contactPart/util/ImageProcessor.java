package com.shark.aio.alarm.contactPart.util;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ImageProcessor {
    static int i = 0;

    static {
          System.load(System.getProperty("user.dir") + "/libopencv_java454d.so");
    }


    private Mat background = null;

    private File[] getLatestFiles(File directory, int n) {
        File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            return Arrays.copyOfRange(files, 0, Math.min(files.length, n));
        }
        return new File[]{}; // 如果没有文件，返回空数组
    }

    public double processImages(File parentDir) {
        //File parentDir1 = FileUtils.getFile("/home/thg/Shark-NJU/AIO-main2/data/images/input/lbx");
        File[] files = getLatestFiles(parentDir, 2);
        BufferedImage img1;
        try {
            img1 = ImageIO.read(files[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedImage img2;
        try {
            img2 = ImageIO.read(files[1]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Mat frame_lwpCV1 = bufferedImageToMat(img1);
        Mat frame_lwpCV2 = bufferedImageToMat(img2);

        Mat gray_lwpCV1 = new Mat();
        Mat gray_lwpCV2 = new Mat();
        long startTime = System.currentTimeMillis();
        Imgproc.cvtColor(frame_lwpCV1, gray_lwpCV1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray_lwpCV1, gray_lwpCV1, new Size(11, 11), 0);


        background = gray_lwpCV1;
        // 如果没有第二个图像，只处理第一个


        Imgproc.cvtColor(frame_lwpCV2, gray_lwpCV2, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray_lwpCV2, gray_lwpCV2, new Size(11, 11), 0);

        Mat diff = new Mat();
        Core.absdiff(background, gray_lwpCV2, diff);
        Imgproc.threshold(diff, diff, 50, 255, Imgproc.THRESH_BINARY);
        Imgproc.dilate(diff, diff, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(diff, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        long endTime = System.currentTimeMillis();
        i++;
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        // Save the difference image or any other result image

        for (MatOfPoint contour : contours) {
            //System.out.println("-------------------------"+Imgproc.contourArea(contour));
            //这里调整人的阈值，根据不同摄像头的视角和位置来改
            if (Imgproc.contourArea(contour) < 2000) {
                continue;
            } else {
                return 1;
            }
            // 处理大于阈值的轮廓
        }
        return 0;
    }

    private Mat bufferedImageToMat(BufferedImage bi) {
        // 首先，检查BufferedImage的类型
        // OpenCV需要数据类型为TYPE_3BYTE_BGR的BufferedImage，这是因为OpenCV默认使用BGR格式
        BufferedImage convertedImg = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        convertedImg.getGraphics().drawImage(bi, 0, 0, null);

        // 获取数据字节
        byte[] pixels = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();

        // 创建Mat对象
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);

        return mat;
    }
}
