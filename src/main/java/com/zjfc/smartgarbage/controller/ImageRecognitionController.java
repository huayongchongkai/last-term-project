package com.zjfc.smartgarbage.controller;

import com.zjfc.smartgarbage.model.dto.ImageRecognitionResult;
import com.zjfc.smartgarbage.model.vo.ApiResponse;
import com.zjfc.smartgarbage.service.ImageRecognitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/image")
@CrossOrigin(origins = "*")
public class ImageRecognitionController {
    
    @Autowired
    private ImageRecognitionService imageRecognitionService;
    
    /**
     * 上传图片识别垃圾类型
     * POST /api/image/recognize
     */
    @PostMapping("/recognize")
    public ApiResponse<ImageRecognitionResult> recognizeImage(@RequestParam("file") MultipartFile file) {
        try {
            log.info("收到图片识别请求: 文件名={}, 大小={}KB", 
                file.getOriginalFilename(), file.getSize() / 1024);
            
            // 验证文件
            if (file.isEmpty()) {
                return ApiResponse.error("请选择图片文件");
            }
            
            if (!file.getContentType().startsWith("image/")) {
                return ApiResponse.error("请上传图片文件（支持JPG、PNG格式）");
            }
            
            if (file.getSize() > 10 * 1024 * 1024) {
                return ApiResponse.error("图片大小不能超过10MB");
            }
            
            // 调用识别服务
            ImageRecognitionResult result = imageRecognitionService.recognizeByFile(file);
            
            log.info("图片识别完成: {}", result.getSuggestion());
            
            return ApiResponse.success("识别成功", result);
            
        } catch (Exception e) {
            log.error("图片识别失败", e);
            return ApiResponse.error("识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 服务健康检查
     * GET /api/image/health
     */
    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("服务正常", "垃圾分类图像识别API运行正常");
    }
}