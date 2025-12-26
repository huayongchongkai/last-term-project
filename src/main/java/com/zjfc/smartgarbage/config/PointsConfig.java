package com.zjfc.smartgarbage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "smart-garbage")
public class PointsConfig {

    private Points points;
    private List<LevelRule> levels;

    @Data
    public static class Points {
        private Integer perCorrectDelivery;
        private Integer perShare;
        private Integer perFeedback;
        private Integer dailyMax;
    }

    @Data
    public static class LevelRule {
        private String name;
        private Integer minPoints;
        private Integer maxPoints;
    }

    public Integer getDailyMax() {
        return points.getDailyMax();
    }

    public List<LevelRule> getLevels() {
        return levels;
    }
}