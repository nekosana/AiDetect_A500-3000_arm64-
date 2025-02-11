package com.shark.aio.operation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AIOFile {
    private String fileName;
    private String fileSize;
    private String fileSrc;
    private Date createdTime;
    private static final DecimalFormat decimalFormat = new DecimalFormat("####.00");

    public AIOFile(String fileName, String fileSrc, File file) throws IOException {
        this.fileName = fileName;
        this.fileSrc = fileSrc;
        long size = file.length();
        if (size<1024){
            this.fileSize=size+"B";
        }else if(size>=1024&&size<1024*1024){
            this.fileSize=decimalFormat.format(size/1024.0)+"KB";
        }else if(size>=1024*1024&&size<1024*1024*1024){
            this.fileSize=decimalFormat.format(size/(1024*1024.0))+"MB";
        }else{
            this.fileSize=decimalFormat.format(size/(1024.0*1024*1024))+"GB";
        }
        this.createdTime = new Date(Files.readAttributes(Paths.get(file.getAbsolutePath()), BasicFileAttributes.class).creationTime().toMillis());
    }
}
