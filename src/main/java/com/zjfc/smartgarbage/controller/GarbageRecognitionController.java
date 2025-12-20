package com.zjfc.smartgarbage.controller;

import com.zjfc.smartgarbage.model.dto.ImageRecognitionResult;
import com.zjfc.smartgarbage.service.ImageRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/garbage")
@CrossOrigin(origins = "*") // 允许跨域
public class GarbageRecognitionController {

    @Autowired
    private ImageRecognitionService imageRecognitionService;

    @PostMapping("/recognize")
    public ResponseEntity<ImageRecognitionResult> recognizeGarbage(
            @RequestParam("file") MultipartFile file) {

        System.out.println("接收到图片识别请求: " + file.getOriginalFilename());
        System.out.println("文件大小: " + file.getSize() + " bytes");
        System.out.println("文件类型: " + file.getContentType());

        try {
            ImageRecognitionResult result = imageRecognitionService.recognizeByFile(file);

            System.out.println("识别结果: ");
            System.out.println("置信度: " + result.getConfidence());
            System.out.println("建议: " + result.getSuggestion());
            System.out.println("物品数量: " + (result.getItems() != null ? result.getItems().size() : 0));

            if (result.getItems() != null) {
                result.getItems().forEach(item -> {
                    System.out.println("物品: " + item.getName() + ", 分类: " + item.getCategory() +
                            ", 分数: " + item.getScore());
                });
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("识别失败: " + e.getMessage());
            e.printStackTrace();

            ImageRecognitionResult errorResult = new ImageRecognitionResult();
            errorResult.setSuggestion("识别失败: " + e.getMessage());
            errorResult.setConfidence(0.0);

            return ResponseEntity.status(500).body(errorResult);
        }
    }
}