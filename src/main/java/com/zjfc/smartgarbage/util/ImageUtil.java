package com.zjfc.smartgarbage.util;

import java.util.Base64;

/**
 * 图片处理工具类
 */
public class ImageUtil {
    
    /**
     * 将图片字节数组转换为Base64编码
     */
    public static String convertToBase64(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * 获取图片格式
     */
    public static String getImageFormat(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "JPEG";
        } else if (filename.endsWith(".png")) {
            return "PNG";
        } else if (filename.endsWith(".gif")) {
            return "GIF";
        } else {
            return "JPG";
        }
    }
}