package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.service.AiAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI助手", description = "AI 助手问答接口")
public class AiAssistantController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @PostMapping("/ask")
    @Operation(summary = "提交问题给 AI 助手并返回回答")
    public Result<Map<String, String>> ask(@RequestBody Map<String, String> body) {
        String question = body != null ? body.get("question") : null;
        if (question == null || question.trim().isEmpty()) {
            return Result.error("-1", "问题不能为空");
        }
        String answer = aiAssistantService.ask(question.trim());
        Map<String, String> data = new HashMap<>();
        data.put("answer", answer);
        return Result.success(data);
    }

    /**
     * 统一智能客服 / 报修解析：type=faq | repair_parse
     */
    @PostMapping("/chat")
    @Operation(summary = "智能客服对话或报修信息解析")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        if (body == null) {
            return Result.error("-1", "请求体不能为空");
        }
        String type = body.get("type") != null ? String.valueOf(body.get("type")) : "";
        String message = body.get("message") != null ? String.valueOf(body.get("message")).trim() : "";
        if (message.isEmpty()) {
            return Result.error("-1", "message 不能为空");
        }
        if ("faq".equalsIgnoreCase(type)) {
            String role = body.get("role") != null ? String.valueOf(body.get("role")) : "OWNER";
            List<Map<String, Object>> history = null;
            Object h = body.get("history");
            if (h instanceof List<?> list) {
                history = new ArrayList<>();
                for (Object o : list) {
                    if (o instanceof Map<?, ?> m) {
                        Map<String, Object> one = new LinkedHashMap<>();
                        m.forEach((k, v) -> one.put(String.valueOf(k), v));
                        history.add(one);
                    }
                }
            }
            Map<String, Object> data = aiAssistantService.chatFaq(message, role, history);
            return Result.success(data);
        }
        if ("repair_parse".equalsIgnoreCase(type)) {
            Map<String, Object> data = aiAssistantService.parseRepairDescription(message);
            return Result.success(data);
        }
        return Result.error("-1", "不支持的 type，请使用 faq 或 repair_parse");
    }
}

