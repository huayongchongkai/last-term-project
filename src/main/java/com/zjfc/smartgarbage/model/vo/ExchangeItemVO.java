package com.zjfc.smartgarbage.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeItemVO {

    private String itemId;
    private String name;
    private String description;
    private Integer pointsRequired;
    private Integer stock;
    private String imageUrl;
    private String category; // COUPON, PHYSICAL, SERVICE
}