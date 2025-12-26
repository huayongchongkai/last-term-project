package com.zjfc.smartgarbage.service;

import com.zjfc.smartgarbage.model.dto.ExchangeRequest;

public interface PointsService {

    void addPoints(String type, Integer points, String remark);

    void deductPoints(Integer points, String remark);

    Object exchangeItem(ExchangeRequest request);

    Object getExchangeItems();

    Object getExchangeRecords(int page, int size);

    Object getPointsLeaderboard(int top);
}