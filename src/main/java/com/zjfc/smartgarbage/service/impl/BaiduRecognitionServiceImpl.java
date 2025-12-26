package com.zjfc.smartgarbage.service.impl;

import com.zjfc.smartgarbage.service.ImageRecognitionService;
import com.zjfc.smartgarbage.model.dto.ImageRecognitionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@Primary
public class BaiduRecognitionServiceImpl implements ImageRecognitionService {

    @Value("${baidu.ai.app-id}")
    private String APP_ID;

    @Value("${baidu.ai.api-key}")
    private String API_KEY;

    @Value("${baidu.ai.secret-key}")
    private String SECRET_KEY;

    @Value("${baidu.ai.recognition-url:https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general}")
    private String RECOGNITION_URL;

    private String accessToken;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 垃圾分类知识库
    private static final Map<String, String> GARBAGE_KNOWLEDGE_BASE = new HashMap<>();

    static {
        // 可回收物 - 扩展词汇
        GARBAGE_KNOWLEDGE_BASE.put("塑料瓶", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("矿泉水瓶", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("饮料瓶", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("啤酒瓶", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("易拉罐", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("玻璃瓶", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("纸箱", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("报纸", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("书本", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("纸袋", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("塑料袋", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("塑料玩具", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("塑料容器", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("PET瓶", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("饮料罐", "可回收物");
        GARBAGE_KNOWLEDGE_BASE.put("啤酒罐", "可回收物");

        // 有害垃圾 - 扩展词汇
        GARBAGE_KNOWLEDGE_BASE.put("电池", "有害垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("干电池", "有害垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("充电电池", "有害垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("纽扣电池", "有害垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("过期药品", "有害垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("药瓶", "有害垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("灯泡", "有害垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("灯管", "有害垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("油漆桶", "有害垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("杀虫剂", "有害垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("化妆品", "有害垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("废旧电池", "有害垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("废电池", "有害垃圾");

        // 厨余垃圾
        GARBAGE_KNOWLEDGE_BASE.put("剩饭", "厨余垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("剩菜", "厨余垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("果皮", "厨余垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("果核", "厨余垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("菜叶", "厨余垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("蛋壳", "厨余垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("骨头", "厨余垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("茶叶", "厨余垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("咖啡渣", "厨余垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("香蕉皮", "厨余垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("苹果核", "厨余垃圾");

        // 其他垃圾
        GARBAGE_KNOWLEDGE_BASE.put("卫生纸", "其他垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("餐巾纸", "其他垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("尿不湿", "其他垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("陶瓷碗", "其他垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("陶瓷盘", "其他垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("烟蒂", "其他垃圾");
        GARBAGE_KNOWLEDGE_BASE.put("一次性餐具", "其他垃圾");
    }

    public BaiduRecognitionServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        try {
            this.accessToken = getAccessToken();
            System.out.println("百度AI AccessToken 获取成功: " + (accessToken != null ? "成功" : "失败"));
        } catch (Exception e) {
            System.err.println("百度AI AccessToken 获取失败: " + e.getMessage());
        }
    }

    private String getAccessToken() {
        try {
            String authUrl = "https://aip.baidubce.com/oauth/2.0/token";
            String params = "grant_type=client_credentials&client_id=" + API_KEY + "&client_secret=" + SECRET_KEY;

            String url = authUrl + "?" + params;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.get("access_token").asText();
            }
        } catch (Exception e) {
            System.err.println("获取AccessToken失败: " + e.getMessage());
        }
        return null;
    }

    @Override
    public ImageRecognitionResult recognizeByFile(MultipartFile file) {
        ImageRecognitionResult result = new ImageRecognitionResult();
        result.setRecognitionTime(new Date());

        if (accessToken == null) {
            result.setConfidence(0.0);
            result.setSuggestion("百度AI服务未初始化，请检查API配置");
            result.setItems(new ArrayList<>());
            return result;
        }

        try {
            // 将图片转换为Base64
            String imageBase64 = Base64.getEncoder().encodeToString(file.getBytes());

            // 构建请求参数
            String requestBody = "image=" + java.net.URLEncoder.encode(imageBase64, "UTF-8")
                    + "&baike_num=5";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json");

            String apiUrl = RECOGNITION_URL + "?access_token=" + accessToken;

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseBaiduResponseToResult(response.getBody());
            } else {
                result.setConfidence(0.0);
                result.setSuggestion("百度AI服务响应异常: " + response.getStatusCode());
                result.setItems(new ArrayList<>());
            }

        } catch (Exception e) {
            result.setConfidence(0.0);
            result.setSuggestion("识别失败: " + e.getMessage());
            result.setItems(new ArrayList<>());
        }

        return result;
    }

    private ImageRecognitionResult parseBaiduResponseToResult(String responseBody) {
        ImageRecognitionResult result = new ImageRecognitionResult();
        result.setRecognitionTime(new Date());

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            if (rootNode.has("error_code")) {
                result.setConfidence(0.0);
                result.setSuggestion("识别失败: " + rootNode.get("error_msg").asText());
                result.setItems(new ArrayList<>());
                return result;
            }

            JsonNode resultArray = rootNode.get("result");
            if (resultArray == null || !resultArray.isArray() || resultArray.size() == 0) {
                result.setConfidence(0.0);
                result.setSuggestion("未识别到有效物体");
                result.setItems(new ArrayList<>());
                return result;
            }

            List<ImageRecognitionResult.GarbageItem> items = new ArrayList<>();
            double totalConfidence = 0.0;

            for (JsonNode itemNode : resultArray) {
                String keyword = itemNode.get("keyword").asText();
                double score = itemNode.get("score").asDouble();

                // 垃圾分类
                Map<String, String> classification = classifyGarbage(keyword);

                // 创建垃圾项
                ImageRecognitionResult.GarbageItem item = new ImageRecognitionResult.GarbageItem();
                item.setName(keyword);
                item.setScore(score);
                item.setCategory(classification.get("type"));

                // 根据垃圾类型设置处理方法
                item.setDisposalMethod(getDisposalMethod(classification.get("type"), keyword));

                items.add(item);
                totalConfidence += score;
            }

            // 按置信度排序
            items.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

            // 计算平均置信度
            double avgConfidence = totalConfidence / items.size();
            result.setConfidence(avgConfidence);
            result.setItems(items);

            // 生成投放建议
            if (!items.isEmpty()) {
                result.setSuggestion(generateConciseReminder(items));

                // 设置积分（根据分类结果）
                int points = calculatePoints(items);
                result.setPoints(points);
            } else {
                result.setSuggestion("未识别到垃圾，请重新上传图片");
                result.setPoints(0);
            }

        } catch (Exception e) {
            result.setConfidence(0.0);
            result.setSuggestion("解析响应失败: " + e.getMessage());
            result.setItems(new ArrayList<>());
            result.setPoints(0);
        }

        return result;
    }

    private Map<String, String> classifyGarbage(String keyword) {
        Map<String, String> result = new HashMap<>();

        // 1. 精确匹配
        if (GARBAGE_KNOWLEDGE_BASE.containsKey(keyword)) {
            result.put("isGarbage", "true");
            result.put("type", GARBAGE_KNOWLEDGE_BASE.get(keyword));
            result.put("method", "精确匹配");
            return result;
        }

        // 2. 模糊匹配（检查是否包含关键字）
        for (Map.Entry<String, String> entry : GARBAGE_KNOWLEDGE_BASE.entrySet()) {
            if (keyword.contains(entry.getKey())) {
                result.put("isGarbage", "true");
                result.put("type", entry.getValue());
                result.put("method", "模糊匹配");
                return result;
            }
        }

        // 3. 反向模糊匹配
        for (Map.Entry<String, String> entry : GARBAGE_KNOWLEDGE_BASE.entrySet()) {
            if (entry.getKey().contains(keyword)) {
                result.put("isGarbage", "true");
                result.put("type", entry.getValue());
                result.put("method", "反向匹配");
                return result;
            }
        }

        // 4. 规则匹配
        String ruleType = matchByRules(keyword);
        if (!"未知".equals(ruleType)) {
            result.put("isGarbage", "true");
            result.put("type", ruleType);
            result.put("method", "规则匹配");
            return result;
        }

        // 5. 未知类型
        result.put("isGarbage", "false");
        result.put("type", "未知");
        result.put("method", "未匹配");
        return result;
    }

    private String matchByRules(String keyword) {
        // 可回收物规则 - 扩展
        if (keyword.matches(".*(瓶|罐|桶|盒|箱|纸|塑料|玻璃|金属|铁|铝|铜|钢|布|饮料|矿泉水|纯净水|可乐|雪碧|啤酒).*")) {
            return "可回收物";
        }

        // 有害垃圾规则 - 扩展
        if (keyword.matches(".*(电池|药|漆|灯|汞|化学|毒|有害|危险|污染|废弃|旧|废).*")) {
            return "有害垃圾";
        }

        // 厨余垃圾规则 - 扩展
        if (keyword.matches(".*(果|菜|饭|食|肉|骨|皮|核|壳|叶|渣|屑|剩|餐|香蕉|苹果|橘子|橙子|西瓜|葡萄).*")) {
            return "厨余垃圾";
        }

        // 其他垃圾规则 - 扩展
        if (keyword.matches(".*(卫生纸|餐巾|陶瓷|尿布|烟|灰|土|尘|渣|袋|膜|包装|纸巾|湿巾).*")) {
            return "其他垃圾";
        }

        return "未知";
    }

    private String getDisposalMethod(String category, String itemName) {
        if (category == null || "未知".equals(category)) {
            if (isPotentiallyRecyclable(itemName)) {
                return "建议清洗干净后投入可回收物垃圾桶";
            } else if (isPotentiallyHazardous(itemName)) {
                return "建议单独收集，投入有害垃圾桶";
            } else {
                return "建议投入其他垃圾桶";
            }
        }

        switch (category) {
            case "可回收物":
                return "清洗干净后投入蓝色可回收物垃圾桶";
            case "有害垃圾":
                return "单独收集，投入红色有害垃圾桶，避免破损";
            case "厨余垃圾":
                return "沥干水分，去除包装后投入绿色厨余垃圾桶";
            case "其他垃圾":
                return "投入灰色其他垃圾桶";
            default:
                return "投入其他垃圾桶";
        }
    }

    /**
     * 生成简洁的核心提醒
     */
    private String generateConciseReminder(List<ImageRecognitionResult.GarbageItem> items) {
        boolean hasHazardous = items.stream()
                .anyMatch(item -> "有害垃圾".equals(item.getCategory()));

        int totalItems = items.size();
        long identifiedCategories = items.stream()
                .map(ImageRecognitionResult.GarbageItem::getCategory)
                .filter(cat -> cat != null && !"未知".equals(cat))
                .distinct()
                .count();

        StringBuilder reminder = new StringBuilder();

        if (hasHazardous) {
            reminder.append("⚠️ 检测到有害垃圾，请务必单独处理！");
        } else if (totalItems > 0) {
            reminder.append("识别到 ").append(totalItems).append(" 个物品，涉及 ")
                    .append(identifiedCategories).append(" 个分类，请分类投放。");
        } else {
            reminder.append("未识别到明确物品，请重新拍摄。");
        }

        return reminder.toString();
    }

    private boolean isPotentiallyRecyclable(String itemName) {
        return itemName.matches(".*(塑料|玻璃|金属|纸|布|瓶|罐|箱|盒|桶|饮料|水).*");
    }

    private boolean isPotentiallyHazardous(String itemName) {
        return itemName.matches(".*(电池|药|化学|漆|灯|汞|毒|剂).*");
    }

    /**
     * 生成清晰美观的投放建议
     */
    private String generateSuggestion(List<ImageRecognitionResult.GarbageItem> items) {
        if (items.isEmpty()) {
            return "未识别到垃圾物品，请重新上传清晰的图片";
        }

        StringBuilder suggestion = new StringBuilder();

        // 按类别分组
        Map<String, List<ImageRecognitionResult.GarbageItem>> categoryMap = new HashMap<>();
        for (ImageRecognitionResult.GarbageItem item : items) {
            String category = item.getCategory();
            if (category != null && !category.isEmpty()) {
                categoryMap.computeIfAbsent(category, k -> new ArrayList<>())
                        .add(item);
            }
        }

        suggestion.append("🎯 智能识别结果\n");
        suggestion.append("====================\n\n");

        // 统计信息
        suggestion.append("📊 统计信息\n");
        suggestion.append("• 识别物品总数: ").append(items.size()).append(" 个\n");
        suggestion.append("• 涉及分类类别: ").append(categoryMap.size()).append(" 类\n\n");

        // 按类别显示物品
        suggestion.append("🗂️ 物品分类详情\n");
        suggestion.append("----------------\n");

        // 定义类别显示顺序
        String[] categoryOrder = { "可回收物", "厨余垃圾", "有害垃圾", "其他垃圾", "未知" };

        for (String category : categoryOrder) {
            if (categoryMap.containsKey(category)) {
                List<ImageRecognitionResult.GarbageItem> categoryItems = categoryMap.get(category);

                suggestion.append("\n【").append(category).append("】\n");

                // 显示该类别下的物品（最多显示5个）
                int displayCount = Math.min(categoryItems.size(), 5);
                for (int i = 0; i < displayCount; i++) {
                    ImageRecognitionResult.GarbageItem item = categoryItems.get(i);
                    String confidence = String.format("%.1f", item.getScore() * 100);
                    suggestion.append("  ✓ ").append(item.getName())
                            .append(" (置信度: ").append(confidence).append("%)\n");
                }

                if (categoryItems.size() > 5) {
                    suggestion.append("  ... 等").append(categoryItems.size()).append("个物品\n");
                }

                // 显示该类别的处理方法
                if (!"未知".equals(category)) {
                    suggestion.append("  📝 处理方法: ").append(getDisposalMethod(category, "")).append("\n");
                }
            }
        }

        // 投放建议
        suggestion.append("\n💡 投放建议\n");
        suggestion.append("----------------\n");

        if (categoryMap.containsKey("有害垃圾")) {
            suggestion.append("⚠️ 重要提醒：检测到有害垃圾，请务必单独处理！\n");
            suggestion.append("建议：\n");
            suggestion.append("1. 先将有害垃圾投入红色垃圾桶\n");
            suggestion.append("2. 再将其他垃圾分别投放\n");
        } else if (categoryMap.size() == 1) {
            String category = categoryMap.keySet().iterator().next();
            if ("未知".equals(category)) {
                suggestion.append("⚠️ 识别结果不确定\n");
                suggestion.append("建议：如无法确定物品类型，可投入灰色其他垃圾桶\n");
            } else {
                suggestion.append("✅ 所有物品都属于").append(category).append("\n");
                suggestion.append("建议：全部投入").append(getCategoryColor(category)).append("垃圾桶\n");
            }
        } else {
            suggestion.append("✅ 检测到多种类型垃圾\n");
            suggestion.append("建议：请按以下顺序分类投放：\n");

            for (String category : categoryOrder) {
                if (categoryMap.containsKey(category) && !"未知".equals(category)) {
                    suggestion.append("1. ").append(category).append(" → ").append(getCategoryColor(category))
                            .append("垃圾桶\n");
                }
            }

            if (categoryMap.containsKey("未知")) {
                suggestion.append("2. 不确定物品 → 灰色垃圾桶\n");
            }
        }

        // 积分信息
        int points = calculatePoints(items);
        if (points > 0) {
            suggestion.append("\n🏆 积分奖励\n");
            suggestion.append("----------------\n");
            suggestion.append("🎉 本次正确分类可获得 ").append(points).append(" 积分！\n");
            suggestion.append("继续保持环保好习惯，累积更多积分吧！\n");
        }

        return suggestion.toString();
    }

    /**
     * 计算积分
     */
    private int calculatePoints(List<ImageRecognitionResult.GarbageItem> items) {
        int points = 0;

        for (ImageRecognitionResult.GarbageItem item : items) {
            String category = item.getCategory();
            if (category != null && !"未知".equals(category)) {
                // 根据置信度给予积分
                double score = item.getScore();
                int basePoints = getBasePoints(category);
                int itemPoints = (int) (basePoints * score);
                points += Math.max(1, itemPoints); // 至少1分
            }
        }

        return points;
    }

    /**
     * 获取各类别的基础积分
     */
    private int getBasePoints(String category) {
        switch (category) {
            case "可回收物":
                return 10;
            case "有害垃圾":
                return 15;
            case "厨余垃圾":
                return 5;
            case "其他垃圾":
                return 0;
            default:
                return 0;
        }
    }

    /**
     * 获取分类对应的垃圾桶颜色
     */
    private String getCategoryColor(String category) {
        switch (category) {
            case "可回收物":
                return "蓝色";
            case "有害垃圾":
                return "红色";
            case "厨余垃圾":
                return "绿色";
            case "其他垃圾":
                return "灰色";
            default:
                return "灰色";
        }
    }

    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "BaiduAI Image Recognition");
        status.put("initialized", accessToken != null);
        status.put("apiKeyConfigured", API_KEY != null && !API_KEY.isEmpty());
        status.put("secretKeyConfigured", SECRET_KEY != null && !SECRET_KEY.isEmpty());
        status.put("knowledgeBaseSize", GARBAGE_KNOWLEDGE_BASE.size());
        status.put("supportedCategories", Arrays.asList("可回收物", "有害垃圾", "厨余垃圾", "其他垃圾"));
        return status;
    }
}