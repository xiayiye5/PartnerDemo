package com.union_test.toutiao.utils;

import android.os.Environment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShowSeqUtils {
    private FileReader fr;
    private BufferedReader br;
    private FileWriter fw;
    private BufferedWriter bw;


    public int loadShowSeq(){
        int show_seq = 1;
        //获取当前日期字符串
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String data = sdf.format(new Date());
        //读取本地缓存show_seq的文件
        try {
            String rootPath =  Environment.getExternalStorageDirectory().getPath();
            File file = new File(rootPath + "/Android/data/com.snssdk.api/cache/adloadSeqTemp.txt");
            if (!file.exists()) {
                file.createNewFile();
                return show_seq;
            }
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] temp = line.split(",");
                if (temp[0].equals(data)){
                    //日期相同返回字段
                    show_seq = Integer.parseInt(temp[1]);
                }
            }
            return show_seq;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(fr!=null){
                    fr.close();
                }
                if(br!=null){
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return show_seq;
    }

    public void writeToFile(int show_seq){
        //获取当前日期字符串
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String data = sdf.format(new Date());
        String content = data+","+show_seq;
        //读取本地缓存show_seq的文件
        try {
            String rootPath =  Environment.getExternalStorageDirectory().getPath();
            File file = new File(rootPath + "/Android/data/com.snssdk.api/cache/");
            if (!file.exists()) {
                file.mkdir();
            }
            String filename = file.getAbsolutePath()+"/adloadSeqTemp.txt";
            file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }
            fw = new FileWriter(file, false);
            fw.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(fw!=null){
                    fw.flush();
                    fw.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
