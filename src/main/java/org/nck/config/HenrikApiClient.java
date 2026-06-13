package org.nck.config;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
@Component
@RequiredArgsConstructor
public class HenrikApiClient {

    private final RestTemplate restTemplate;

    @Value("${henrik.api.key}")
    private String apiKey;

    @Value("${henrik.api.base-url}")
    private String baseUrl;

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        return headers;
    }

    // 계정 조회
    public JsonNode getAccount(String name, String tag) {
        String url = baseUrl + "/valorant/v1/account/" + name + "/" + tag;
        HttpEntity<Void> entity = new HttpEntity<>(getHeaders());
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class
        );
        return response.getBody().get("data");
    }

    // 매치 히스토리 조회 (커스텀 게임 포함)
    public JsonNode getMatchHistory(String region, String name, String tag) {
        String url = baseUrl + "/valorant/v3/matches/" + region + "/" + name + "/" + tag + "?mode=custom&size=20";
        HttpEntity<Void> entity = new HttpEntity<>(getHeaders());
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class
        );

        JsonNode data = response.getBody().get("data");

        if (data == null || !data.isArray()) return data;

        com.fasterxml.jackson.databind.node.ArrayNode result =
                com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode();

        for (JsonNode match : data) {
            String queue = match.path("metadata").path("queue").asText();
            if ("Standard".equals(queue)) {
                result.add(match);
            }
        }

        return result;
    }

    // MMR (티어) 조회
    public JsonNode getMmr(String region, String name, String tag) {
        String url = baseUrl + "/valorant/v2/mmr/" + region + "/" + name + "/" + tag;
        HttpEntity<Void> entity = new HttpEntity<>(getHeaders());
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class
        );
        return response.getBody().get("data");
    }
}
