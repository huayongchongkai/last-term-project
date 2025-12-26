package com.zjfc.smartgarbage.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRecordVO {
    private String exchangeId;
    private String itemName;
    private Integer quantity;
    private Integer pointsUsed;
    private String status;
    private LocalDateTime exchangeTime;
    private String remark;
}