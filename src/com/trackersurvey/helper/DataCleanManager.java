package com.trackersurvey.helper;

/** 
 * �� �� ��:  DataCleanManager.java
 * ��    ��:  ��Ҫ�����������/�⻺�棬������ݿ⣬���sharedPreference�����files������Զ���Ŀ¼  
 */
import java.io.File;
import java.math.BigDecimal;

import android.content.Context;
import android.os.Environment;
/** * ��Ӧ��������������� */
public class DataCleanManager {
    /** * �����Ӧ���ڲ�����(/data/data/com.xxx.xxx/cache) * * @param context */
    public static void cleanInternalCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir());
    }
    /** * �����Ӧ���������ݿ�(/data/data/com.xxx.xxx/databases) * * @param context */
    public static void cleanDatabases(Context context) {
        deleteFilesByDirectory(new File("/data/data/"
                + context.getPackageName() + "/databases"));
    }
    /**
     * * �����Ӧ��SharedPreference(/data/data/com.xxx.xxx/shared_prefs) * * @param
     * context
     */
    public static void cleanSharedPreference(Context context) {
        deleteFilesByDirectory(new File("/data/data/"
                + context.getPackageName() + "/shared_prefs"));
    }
    /** * �����������Ӧ�����ݿ� * * @param context * @param dbName */
    public static void cleanDatabaseByName(Context context, String dbName) {
        context.deleteDatabase(dbName);
    }
    /** * ���/data/data/com.xxx.xxx/files�µ����� * * @param context */
    public static void cleanFiles(Context context) {
        deleteFilesByDirectory(context.getFilesDir());
    }
    /**
     * * ����ⲿcache�µ�����(/mnt/sdcard/android/data/com.xxx.xxx/cache) * * @param
     * context
     */
    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }
    /** * ����Զ���·���µ��ļ���ʹ����С�ģ��벻Ҫ��ɾ������ֻ֧��Ŀ¼�µ��ļ�ɾ�� * * @param filePath */
    public static void cleanCustomCache(String filePath) {
        deleteFilesByDirectory(new File(filePath));
    }
    /** * �����Ӧ�����е����� * * @param context * @param filepath */
    public static void cleanApplicationData(Context context, String... filepath) {
        cleanInternalCache(context);
        cleanExternalCache(context);
        cleanDatabases(context);
        //cleanSharedPreference(context);
        cleanFiles(context);
        for (String filePath : filepath) {
            cleanCustomCache(filePath);
        }
    }
    /** * ɾ������ ����ֻ��ɾ��ĳ���ļ����µ��ļ�����������directory�Ǹ��ļ������������� * * @param directory */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }
    
    public static long getFolderSize(File file) throws Exception {  
        long size = 0;  
        try {  
            File[] fileList = file.listFiles();  
            for (int i = 0; i < fileList.length; i++) {  
                // ������滹���ļ�  
                if (fileList[i].isDirectory()) {  
                    size = size + getFolderSize(fileList[i]);  
                } else {  
                    size = size + fileList[i].length();  
                }  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return size;  
    }  
    /** 
     * ��ʽ����λ 
     *  
     * @param size 
     * @return 
     */  
    public static String getFormatSize(double size) {  
        double kiloByte = size / 1024;  
        if (kiloByte < 1) {  
            return size + "Byte";  
        }  
  
        double megaByte = kiloByte / 1024;  
        if (megaByte < 1) {  
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));  
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)  
                    .toPlainString() + "KB";  
        }  
  
        double gigaByte = megaByte / 1024;  
        if (gigaByte < 1) {  
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));  
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)  
                    .toPlainString() + "MB";  
        }  
  
        double teraBytes = gigaByte / 1024;  
        if (teraBytes < 1) {  
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));  
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)  
                    .toPlainString() + "GB";  
        }  
        BigDecimal result4 = new BigDecimal(teraBytes);  
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()  
                + "TB";  
    }  
      
      
    public static String getCacheSize(File file) throws Exception {  
        return getFormatSize(getFolderSize(file));  
    }  
}
