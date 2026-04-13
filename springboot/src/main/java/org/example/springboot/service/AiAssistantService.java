package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.springboot.entity.RepairType;
import org.example.springboot.mapper.RepairTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * AI 助手：DeepSeek 调用、FAQ 跳转解析、报修字段解析。
 */
@Service
public class AiAssistantService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiAssistantService.class);

    private static final String DEFAULT_FAIL = "抱歉，我暂时无法回答，请稍后再试或联系人工客服。";

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private RepairTypeMapper repairTypeMapper;

    /**
     * 旧版单轮问答（保留兼容）。
     */
    public String ask(String question) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            LOGGER.warn("DeepSeek API key 未配置，返回本地提示信息");
            return "当前环境未配置 DeepSeek API Key，暂时使用演示模式。\n\n" +
                    "示例回答：您好，关于「" + question + "」，您可以先在系统相关功能中查看，例如物业缴费、报修服务、家政与投诉建议等；" +
                    "如有紧急问题，请直接联系物业前台处理。";
        }
        try {
            Map<String, Object> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content",
                    "你是一个小区物业 AI 助手，\n" +
                            "仅回答与小区物业管理相关的问题，例如：物业费缴纳方式、报修流程、" +
                            "小区活动、物业办公时间等。\n" +
                            "回答要简洁、分点说明，必要时给出操作步骤，不要输出与物业无关的内容。");
            Map<String, Object> user = new HashMap<>();
            user.put("role", "user");
            user.put("content", question);
            return callDeepSeek(List.of(sys, user));
        } catch (Exception e) {
            LOGGER.error("调用 DeepSeek 接口异常", e);
            return "调用 AI 服务时出现异常，请稍后再试，或联系管理员检查 DeepSeek 配置。";
        }
    }

    /**
     * FAQ：多轮 + 可选跳转；返回结构供 /api/ai/chat 使用。
     */
    public Map<String, Object> chatFaq(String message, String role, List<Map<String, Object>> history) {
        Map<String, Object> out = new LinkedHashMap<>();
        String r = normalizeRole(role);
        if (message == null || message.trim().isEmpty()) {
            out.put("type", "text");
            out.put("text", "请输入您的问题。");
            return out;
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            out.put("type", "text");
            out.put("text", "当前环境未配置 DeepSeek API Key，无法使用智能客服。请使用左侧菜单进入「物业缴费」「报修服务」等功能。");
            return out;
        }
        try {
            List<Map<String, Object>> messages = buildFaqMessages(message.trim(), r, history);
            String raw = callDeepSeekMessages(messages);
            JsonNode gotoJson = extractGotoJson(raw);
            if (gotoJson != null) {
                String url = gotoJson.has("url") ? gotoJson.get("url").asText("") : "";
                String btn = gotoJson.has("text") ? gotoJson.get("text").asText("前往") : "前往";
                String normalized = normalizePath(url);
                if (normalized != null && isUrlAllowed(normalized, r)) {
                    String textOnly = stripTrailingJson(raw).trim();
                    if (textOnly.isEmpty()) {
                        textOnly = "您可以点击下方按钮进入对应页面。";
                    }
                    out.put("type", "link");
                    out.put("text", textOnly);
                    out.put("linkUrl", normalized);
                    out.put("linkText", btn);
                    return out;
                }
                String textOnly = stripTrailingJson(raw).trim();
                if (textOnly.isEmpty()) {
                    textOnly = raw.trim();
                }
                out.put("type", "text");
                out.put("text", textOnly);
                return out;
            }
            out.put("type", "text");
            out.put("text", raw.trim());
            return out;
        } catch (Exception e) {
            LOGGER.error("chatFaq 异常", e);
            out.put("type", "text");
            out.put("text", DEFAULT_FAIL);
            return out;
        }
    }

    /**
     * 报修描述解析。
     */
    public Map<String, Object> parseRepairDescription(String message) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (message == null || message.trim().isEmpty()) {
            out.put("success", false);
            out.put("reason", "parse_error");
            return out;
        }
        List<RepairType> types = repairTypeMapper.selectList(
                new LambdaQueryWrapper<RepairType>().orderByAsc(RepairType::getId));
        if (types == null || types.isEmpty()) {
            out.put("success", false);
            out.put("reason", "parse_error");
            return out;
        }
        String trimmed = message.trim();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            LOGGER.info("DeepSeek 未配置，报修解析使用本地关键词规则");
            return fallbackParseRepair(trimmed, types);
        }
        String prompt = buildRepairParsePrompt(trimmed);
        try {
            Map<String, Object> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content", "你是一个严谨的报修信息提取助手，只输出 JSON，不要输出任何其他文字。");
            Map<String, Object> user = new HashMap<>();
            user.put("role", "user");
            user.put("content", prompt);
            String raw = callDeepSeekMessages(List.of(sys, user));
            String jsonStr = extractJsonBlock(raw);
            JsonNode node = objectMapper.readTree(jsonStr);
            if (node.has("error") && "非报修描述".equals(node.get("error").asText())) {
                if (looksLikeFacilityRepair(trimmed)) {
                    LOGGER.info("模型误判非报修，已用语义关键词回退本地规则: {}", trimmed);
                    return fallbackParseRepair(trimmed, types);
                }
                out.put("success", false);
                out.put("reason", "not_repair");
                return out;
            }
            String category = node.has("type") ? node.get("type").asText("其他") : "其他";

            List<String> allowedCat = Arrays.asList("水电", "家电", "房屋结构", "公共设施", "其他");
            if (!allowedCat.contains(category)) {
                category = "其他";
            }
            Long repairTypeId = resolveRepairTypeId(category, types);
            if (repairTypeId == null) {
                repairTypeId = types.get(types.size() - 1).getId();
            }
            out.put("success", true);
            out.put("category", category);
            out.put("repairTypeId", repairTypeId);
            out.put("source", "ai");
            return out;
        } catch (Exception e) {
            LOGGER.warn("parseRepairDescription 大模型解析失败，回退本地规则: {}", e.getMessage());
            return fallbackParseRepair(trimmed, types);
        }
    }

    private String buildRepairParsePrompt(String message) {
        return "你是一个报修单自动填写助手。业主账号已绑定房屋，无需提取位置信息。你的任务是从用户描述的故障中判断故障类型，并以严格的 JSON 格式返回，不要有任何其他文字。\n\n"
                + "字段说明：\n"
                + "- type：故障类型，必须从以下列表中选择一个：[\"水电\", \"家电\", \"房屋结构\", \"公共设施\", \"其他\"]\n\n"
                + "典型归类（务必遵守）：\n"
                + "- 门锁、钥匙丢失/打不开门、换锁、门卡失灵、入户门故障 → 房屋结构\n"
                + "- 水管漏水、电路跳闸、灯具开关 → 水电\n"
                + "- 空调冰箱等家用电器 → 家电\n"
                + "- 电梯楼道公共设施 → 公共设施\n\n"
                + "重要规则：\n"
                + "- 「钥匙丢了开不了门」「门锁坏了」「进不了家门」等属于房屋/门锁类报修，必须返回 JSON 且 type 一般为「房屋结构」，禁止误判为「非报修描述」。\n"
                + "- 仅当内容完全是投诉服务态度、与房屋设施无关时，才返回：{\"error\":\"非报修描述\"}（例如：只写「我要投诉保安态度差」且无任何设施故障）。\n"
                + "- 若用户描述中包含对物业的抱怨但同时有设施问题，只提取故障事实，按上表归类。\n\n"
                + "用户描述：" + message;
    }

    /**
     * 未配置大模型时：用关键词推断故障大类并映射到 repair_type，保证本地演示可用。
     */
    private Map<String, Object> fallbackParseRepair(String message, List<RepairType> types) {
        Map<String, Object> out = new LinkedHashMap<>();
        String category = classifyCategoryFallback(message);
        Long repairTypeId = resolveRepairTypeId(category, types);
        if (repairTypeId == null) {
            repairTypeId = types.get(types.size() - 1).getId();
        }
        out.put("success", true);
        out.put("category", category);
        out.put("repairTypeId", repairTypeId);
        out.put("source", "fallback");
        return out;
    }

    private static String classifyCategoryFallback(String message) {
        if (message == null || message.isEmpty()) {
            return "其他";
        }
        if (containsAny(message, "电梯", "楼道灯", "楼道", "公共区域", "公共设施")) {
            return "公共设施";
        }
        if (containsAny(message, "空调", "冰箱", "洗衣机", "电视", "热水器")) {
            return "家电";
        }
        // 门锁、钥匙、无法入户等优先归为房屋结构（与「门」字是否出现无关）
        if (containsAny(message, "钥匙", "门锁", "换锁", "开锁", "开不了门", "进不了门", "门打不开", "锁芯", "锁坏了")
                || containsAny(message, "门", "锁", "墙", "窗", "天花", "地面开裂")) {
            return "房屋结构";
        }
        if (containsAny(message, "水", "漏", "龙头", "水管", "排水", "电", "灯", "电路", "跳闸", "插座", "开关")) {
            return "水电";
        }
        return "其他";
    }

    /**
     * 模型误报「非报修」时，用语义关键词二次判断是否为设施类报修。
     */
    private static boolean looksLikeFacilityRepair(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        return containsAny(message,
                "钥匙", "门锁", "换锁", "开锁", "开不了门", "进不了门", "门打不开", "锁芯", "锁坏了",
                "漏水", "水管", "龙头", "跳闸", "电路", "灯不亮", "插座",
                "门", "窗", "墙", "电梯", "空调", "冰箱", "洗衣机");
    }

    private static boolean containsAny(String message, String... needles) {
        for (String n : needles) {
            if (message.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private Long resolveRepairTypeId(String category, List<RepairType> types) {
        for (RepairType t : types) {
            String n = t.getName() == null ? "" : t.getName();
            switch (category) {
                case "水电":
                    if (n.contains("水") || n.contains("管") || n.contains("电") || n.contains("路")) {
                        return t.getId();
                    }
                    break;
                case "家电":
                    if (n.contains("电") || n.contains("路") || n.contains("锁")) {
                        return t.getId();
                    }
                    break;
                case "房屋结构":
                    if (n.contains("门") || n.contains("锁") || n.contains("墙") || n.contains("窗")) {
                        return t.getId();
                    }
                    break;
                case "公共设施":
                    if (n.contains("公共") || n.contains("电梯") || n.contains("楼道")) {
                        return t.getId();
                    }
                    break;
                default:
                    break;
            }
        }
        switch (category) {
            case "水电":
                return findFirstNameContaining(types, "水", "电", "管");
            case "家电":
                return findFirstNameContaining(types, "电", "路");
            case "房屋结构":
                return findFirstNameContaining(types, "门", "锁");
            case "公共设施":
                return types.isEmpty() ? null : types.get(0).getId();
            case "其他":
            default:
                return types.isEmpty() ? null : types.get(types.size() - 1).getId();
        }
    }

    private Long findFirstNameContaining(List<RepairType> types, String... subs) {
        for (RepairType t : types) {
            String n = t.getName() == null ? "" : t.getName();
            for (String s : subs) {
                if (n.contains(s)) {
                    return t.getId();
                }
            }
        }
        return null;
    }

    private String extractJsonBlock(String raw) {
        if (raw == null) {
            return "{}";
        }
        String s = raw.trim();
        if (s.startsWith("```")) {
            int start = s.indexOf('{');
            int end = s.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return s.substring(start, end + 1);
            }
        }
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1);
        }
        return s;
    }

    private List<Map<String, Object>> buildFaqMessages(String message, String role, List<Map<String, Object>> history) {
        String urlBlock = buildUrlBlockForRole(role);
        String system = "你是一个严谨的物业智能助手。\n"
                + "任务：\n"
                + "1) 友好、简洁地回答用户关于小区物业的问题。\n"
                + "2) 仅当用户明确表达要去某个功能页面、或要求「带我去…页」时，在你回答的末尾附加一段 JSON（除此之外不要附加 JSON）。\n"
                + "JSON 格式严格为：{\"action\":\"goto\",\"url\":\"路径\",\"text\":\"按钮文字\"}\n"
                + "路径必须从下列列表中选择（根据用户角色）；不要使用列表外的路径。\n"
                + urlBlock + "\n"
                + "规则：\n"
                + "- 若只需知识问答、不要求跳转，只输出纯文本，不要附加 JSON。\n"
                + "- 若需要跳转，先写清楚说明文字，最后一行再输出上述 JSON，JSON 之后不要有任何字符。\n"
                + "用户角色：" + role + "\n";
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> sys = new HashMap<>();
        sys.put("role", "system");
        sys.put("content", system);
        messages.add(sys);
        if (history != null) {
            int from = Math.max(0, history.size() - 8);
            for (int i = from; i < history.size(); i++) {
                Map<String, Object> h = history.get(i);
                if (h == null) {
                    continue;
                }
                String rr = h.get("role") != null ? String.valueOf(h.get("role")) : null;
                String cc = h.get("content") != null ? String.valueOf(h.get("content")) : null;
                if (rr == null || cc == null || cc.trim().isEmpty()) {
                    continue;
                }
                if (!"user".equals(rr) && !"assistant".equals(rr)) {
                    continue;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("role", rr);
                m.put("content", cc);
                messages.add(m);
            }
        }
        Map<String, Object> u = new HashMap<>();
        u.put("role", "user");
        u.put("content", message);
        messages.add(u);
        return messages;
    }

    private String buildUrlBlockForRole(String role) {
        StringBuilder sb = new StringBuilder();
        switch (role) {
            case "ADMIN":
                sb.append("业主端：缴费 /property-fee 或 /payment；报修 /repair；公告 /notice 或 /home；投诉 /complaint；个人中心 /profile（含车牌等信息）；联系物业 /contact；消息 /notification；家政 /housekeeping；活动 /community-activity；投票 /vote。\n");
                sb.append("管理端：数据看板 /showView；业主管理 /ownerManager；维修工单 /repairRecordManager；数据统计可用 /showView。\n");
                break;
            case "MAINTENANCE":
                sb.append("业主端：/property-fee、/repair、/complaint、/profile、/home、/notification、/housekeeping、/community-activity、/vote。\n");
                sb.append("维修端：工作台相关页面 /repairRecordManager、数据看板 /showView。\n");
                break;
            case "KEEPER":
                sb.append("/property-fee、/repair、/complaint、/profile、/home、/notification、/housekeeping、/community-activity、/vote。\n");
                break;
            case "OWNER":
            default:
                sb.append("缴费 /property-fee 或 /payment；报修 /repair；公告首页 /notice 或 /home；投诉建议 /complaint；业主信息 /profile（含车牌等）；联系物业 /contact；消息通知 /notification；家政服务 /housekeeping；小区活动 /community-activity；投票 /vote；AI助手页 /ai-assistant。\n");
                break;
        }
        return sb.toString();
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "OWNER";
        }
        String u = role.trim().toUpperCase();
        if ("OWNER".equals(u) || "ADMIN".equals(u) || "MAINTENANCE".equals(u) || "KEEPER".equals(u)) {
            return u;
        }
        return "OWNER";
    }

    /**
     * 将别名转为站内实际路径，再参与白名单校验。
     */
    private String normalizePath(String url) {
        if (url == null) {
            return null;
        }
        String p = url.trim();
        if (p.isEmpty()) {
            return null;
        }
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        if (p.contains("..") || p.contains("//")) {
            return null;
        }
        return switch (p) {
            case "/payment" -> "/property-fee";
            case "/parking" -> "/profile";
            case "/notice" -> "/home";
            case "/contact" -> "/complaint";
            case "/ownerManage" -> "/ownerManager";
            case "/repairDispatch" -> "/repairRecordManager";
            case "/statistics" -> "/showView";
            default -> p;
        };
    }

    private boolean isUrlAllowed(String path, String role) {
        Set<String> set = allowedPathsForRole(role);
        if (set.contains(path)) {
            return true;
        }
        // 动态路由：/news/123
        if (path.startsWith("/news/") && set.contains("/news")) {
            return true;
        }
        if (path.startsWith("/vote/") && set.contains("/vote")) {
            return true;
        }
        return false;
    }

    private Set<String> allowedPathsForRole(String role) {
        Set<String> common = new HashSet<>(Arrays.asList(
                "/property-fee", "/repair", "/complaint", "/profile", "/home",
                "/notification", "/housekeeping", "/community-activity", "/vote",
                "/ai-assistant", "/alipay-payment", "/news"
        ));
        switch (role) {
            case "ADMIN":
                common.addAll(Arrays.asList("/showView", "/ownerManager", "/repairRecordManager"));
                return common;
            case "MAINTENANCE":
                common.addAll(Arrays.asList("/showView", "/repairRecordManager"));
                return common;
            case "KEEPER":
            case "OWNER":
            default:
                return common;
        }
    }

    private JsonNode extractGotoJson(String text) {
        if (text == null) {
            return null;
        }
        int i = text.lastIndexOf('{');
        while (i >= 0) {
            String sub = text.substring(i);
            for (int len = sub.length(); len > 0; len--) {
                try {
                    JsonNode n = objectMapper.readTree(sub.substring(0, len));
                    if (n.has("action") && "goto".equals(n.get("action").asText())) {
                        return n;
                    }
                    break;
                } catch (Exception ignored) {
                    // try shorter
                }
            }
            i = text.lastIndexOf('{', i - 1);
        }
        return null;
    }

    private String stripTrailingJson(String text) {
        if (text == null) {
            return "";
        }
        int i = text.lastIndexOf('{');
        while (i >= 0) {
            String sub = text.substring(i);
            for (int len = sub.length(); len > 0; len--) {
                try {
                    JsonNode n = objectMapper.readTree(sub.substring(0, len));
                    if (n.has("action") && "goto".equals(n.get("action").asText())) {
                        return text.substring(0, i).trim();
                    }
                    break;
                } catch (Exception ignored) {
                }
            }
            i = text.lastIndexOf('{', i - 1);
        }
        return text;
    }

    private String callDeepSeek(List<Map<String, Object>> messages) throws Exception {
        return callDeepSeekMessages(messages);
    }

    private String callDeepSeekMessages(List<Map<String, Object>> messages) throws Exception {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("no api key");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.3);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            LOGGER.warn("调用 DeepSeek 失败，status={}, body={}", resp.getStatusCode(), resp.getBody());
            throw new IllegalStateException("bad response");
        }
        JsonNode root = objectMapper.readTree(resp.getBody());
        JsonNode choices = root.get("choices");
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode msg = choices.get(0).get("message");
            if (msg != null && msg.get("content") != null) {
                return msg.get("content").asText();
            }
        }
        throw new IllegalStateException("empty choices");
    }
}
