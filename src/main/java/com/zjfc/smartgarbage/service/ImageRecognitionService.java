package com.zjfc.smartgarbage.service;

import com.zjfc.smartgarbage.model.dto.ImageRecognitionResult;
import org.springframework.web.multipart.MultipartFile;

public interface ImageRecognitionService {
    /**
     * 通过上传的图片文件识别垃圾类型
     */
    ImageRecognitionResult recognizeByFile(MultipartFile file);
}