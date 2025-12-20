package com.zjfc.smartgarbage.model.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 图像识别结果
 */
@Data
public class ImageRecognitionResult {
    private List<GarbageItem> items; // 识别到的垃圾项
    private Double confidence; // 总体置信度
    private Date recognitionTime; // 识别时间
    private String suggestion; // 投放建议
    private Integer points; // 添加积分字段

    /**
     * 垃圾项详情
     */
    @Data
    public static class GarbageItem {
        private String name; // 垃圾名称
        private String category; // 垃圾类别：可回收、有害、厨余、其他
        private Double score; // 置信度分数
        private String disposalMethod; // 处理方法
    }
}