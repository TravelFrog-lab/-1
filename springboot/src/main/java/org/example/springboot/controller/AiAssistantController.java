package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.service.AiAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
}

