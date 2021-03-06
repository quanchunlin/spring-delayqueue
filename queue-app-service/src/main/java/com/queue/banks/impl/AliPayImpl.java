package com.queue.banks.impl;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.queue.banks.OrderQueryFacade;
import com.queue.enums.OrderStatusEnum;
import com.queue.orderpolling.vo.OrderQueryResult;
import com.queue.utils.HttpUtils;

@Service("ALIPAY_IMPL")
public class AliPayImpl implements OrderQueryFacade {
	private static final Logger logger = LoggerFactory.getLogger(AliPayImpl.class);
	
	@Override
	public OrderQueryResult orderQuery(String orderTransactionNo) {
		logger.info("银行渠道(ALIPAY_IMPL) 银行渠道交易流水号: {}", orderTransactionNo);
		
		String bankPayUrl = "http://127.0.0.1:8080/queue-web-gateway/bankQuery";//支付宝, 微信, 各种银行等
		String bankResult = HttpUtils.get(bankPayUrl +"?orderTransactionNo=" + String.valueOf(orderTransactionNo));
		logger.info("银行渠道(ALIPAY_IMPL) 返回结果: {}", bankResult);
		
		OrderQueryResult orderQueryResult = new OrderQueryResult();
		orderQueryResult.setOrderStatusEnum(OrderStatusEnum.UNKNOWN);//默认是 未知结果，继续轮询查询订单
		
		JSON jsonData = (JSON)JSONObject.parse(bankResult);
		Map result = JSONObject.toJavaObject(jsonData, Map.class);
		logger.info("银行渠道(ALIPAY_IMPL) 解析返回结果: {}", result);
		
        if (result != null) {
        	
			switch((String)result.get("status")) {
				case "SUCCESS":
					orderQueryResult.setOrderStatusEnum(OrderStatusEnum.SUCCESS);
					break;
				case "FAILED":
					orderQueryResult.setOrderStatusEnum(OrderStatusEnum.FAILED);
					break;
				case "NOTPAY":
					orderQueryResult.setOrderStatusEnum(OrderStatusEnum.UNKNOWN);
					break;
			}
        }

		return orderQueryResult;
	}
}