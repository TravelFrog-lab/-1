package org.example.springboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 助手服务，负责调用 DeepSeek 等大模型 API。
 */
@Service
public class AiAssistantService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiAssistantService.class);

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用 DeepSeek，返回回答文本。
     * 说明：未配置 API Key 时，返回友好提示，方便本地演示。
     */
    public String ask(String question) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            LOGGER.warn("DeepSeek API key 未配置，返回本地提示信息");
            return "当前环境未配置 DeepSeek API Key，暂时使用演示模式。\n\n" +
                    "示例回答：您好，关于「" + question + "」，您可以先在系统相关功能中查看，例如物业缴费、报修服务、停车服务等；" +
                    "如有紧急问题，请直接联系物业前台处理。";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey.trim());

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            // 构造对话消息：系统提示 + 用户问题
            Map<String, Object> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content",
                    "你是一个小区物业 AI 助手，\n" +
                    "仅回答与小区物业管理相关的问题，例如：物业费缴纳方式、停车费标准、报修流程、" +
                    "小区活动、物业办公时间等。\n" +
                    "回答要简洁、分点说明，必要时给出操作步骤，不要输出与物业无关的内容。");

            Map<String, Object> user = new HashMap<>();
            user.put("role", "user");
            user.put("content", question);

            body.put("messages", List.of(sys, user));
            body.put("temperature", 0.3);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                LOGGER.warn("调用 DeepSeek 失败，status={}, body={}", resp.getStatusCode(), resp.getBody());
                return "AI 服务暂时不可用，请稍后再试，或直接联系物业工作人员。";
            }

            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode msg = choices.get(0).get("message");
                if (msg != null && msg.get("content") != null) {
                    return msg.get("content").asText();
                }
            }
            LOGGER.warn("解析 DeepSeek 响应失败，body={}", resp.getBody());
            return "AI 服务返回了空结果，请稍后再试。";
        } catch (Exception e) {
            LOGGER.error("调用 DeepSeek 接口异常", e);
            return "调用 AI 服务时出现异常，请稍后再试，或联系管理员检查 DeepSeek 配置。";
        }
    }
}

