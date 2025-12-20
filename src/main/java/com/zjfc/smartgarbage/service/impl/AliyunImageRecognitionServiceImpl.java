package com.zjfc.smartgarbage.service.impl;

import com.zjfc.smartgarbage.model.dto.ImageRecognitionResult;
import com.zjfc.smartgarbage.service.ImageRecognitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Service
public class AliyunImageRecognitionServiceImpl implements ImageRecognitionService {
    
    @Override
    public ImageRecognitionResult recognizeByFile(MultipartFile file) {
        try {
            log.info("开始识别图片: {}", file.getOriginalFilename());
            
            // 模拟识别逻辑（先测试流程，后续接入真实阿里云API）
            return createMockResult(file.getOriginalFilename());
            
        } catch (Exception e) {
            log.error("图像识别失败", e);
            throw new RuntimeException("识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建模拟识别结果（用于测试）
     */
    private ImageRecognitionResult createMockResult(String filename) {
        ImageRecognitionResult result = new ImageRecognitionResult();
        result.setRecognitionTime(new Date());
        
        // 根据文件名关键词模拟不同垃圾类型
        ImageRecognitionResult.GarbageItem item = detectGarbageType(filename);
        
        List<ImageRecognitionResult.GarbageItem> items = new ArrayList<>();
        items.add(item);
        
        result.setItems(items);
        result.setConfidence(item.getScore());
        result.setSuggestion("建议：请投入" + item.getCategory());
        
        log.info("模拟识别结果: {} -> {}", filename, item.getCategory());
        
        return result;
    }
    
    /**
     * 根据文件名检测垃圾类型（模拟逻辑）
     */
    private ImageRecognitionResult.GarbageItem detectGarbageType(String filename) {
        String lowerName = filename.toLowerCase();
        ImageRecognitionResult.GarbageItem item = new ImageRecognitionResult.GarbageItem();
        
        // 可回收垃圾
        if (lowerName.contains("plastic") || lowerName.contains("bottle") || 
            lowerName.contains("can") || lowerName.contains("paper") || 
            lowerName.contains("glass")) {
            item.setName("可回收物品");
            item.setCategory("可回收垃圾");
            item.setScore(0.85 + Math.random() * 0.1);
            item.setDisposalMethod("清洗干净后投入蓝色可回收垃圾桶");
        }
        // 有害垃圾
        else if (lowerName.contains("battery") || lowerName.contains("medicine") || 
                 lowerName.contains("chemical") || lowerName.contains("lamp")) {
            item.setName("有害物品");
            item.setCategory("有害垃圾");
            item.setScore(0.88 + Math.random() * 0.1);
            item.setDisposalMethod("轻拿轻放，投入红色有害垃圾桶");
        }
        // 厨余垃圾
        else if (lowerName.contains("food") || lowerName.contains("fruit") || 
                 lowerName.contains("vegetable") || lowerName.contains("meal")) {
            item.setName("厨余垃圾");
            item.setCategory("厨余垃圾");
            item.setScore(0.9 + Math.random() * 0.08);
            item.setDisposalMethod("沥干水分后投入绿色厨余垃圾桶");
        }
        // 其他垃圾
        else {
            item.setName("其他垃圾");
            item.setCategory("其他垃圾");
            item.setScore(0.8 + Math.random() * 0.15);
            item.setDisposalMethod("投入灰色其他垃圾桶");
        }
        
        return item;
    }
}