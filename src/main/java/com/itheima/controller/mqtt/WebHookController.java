package com.itheima.controller.mqtt;

import com.itheima.mqtt.enums.QosEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mqtt")
public class WebHookController {

    private static final Logger log = LoggerFactory.getLogger(WebHookController.class);

    private Map<String, Boolean> clientStatusMap = new HashMap<>();

    @PostMapping("/webhook")
    public void hook(@RequestBody Map<String, Object> params) {
        log.info("emqx 触发 webhook,请求体数据={}", params);
        String action = (String) params.get("action");
        String clientid = (String) params.get("clientid");
        if (action.equals("client_connected")) {
            //客户端成功接入
            clientStatusMap.put(clientid, true);
            //自动订阅autosub主题
            autoSub(clientid, "autosub/#", QosEnum.QoS2, true);
        }

        if (action.equals("client_disconnected")) {
            //客户端断开连接
            clientStatusMap.put(clientid, false);
            //自动取消订阅autosub主题
            autoSub(clientid, "autosub/#", QosEnum.QoS2, false);
        }
    }

    @GetMapping("/getall")
    public Map getAllStatus() {
        return clientStatusMap;
    }


    /**
     * 自动订阅或取消订阅
     * * @param clientId
     * * @param topicfilter
     * * @param qos * @param sub
     */
    private void autoSub(String clientId, String topicfilter, QosEnum qos, boolean sub) {
        //设置请求信息，后续可直接使用restTemplate发送请求
        RestTemplate restTemplate = new RestTemplateBuilder()
                .basicAuthentication("admin", "public")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE)
                .build();
        //装配参数
        HashMap<Object, Object> param = new HashMap<>();
        param.put("clientid", clientId);
        param.put("qos", qos.value());
        param.put("topic", topicfilter);
        log.info("请求emq的相关参数:{}", param);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<>(param, headers);
        //自动订阅
        if (sub) {
            new Thread(() -> {
                ResponseEntity<String> responseEntity =
                        restTemplate.postForEntity("http://121.41.6.44:8081/api/v4/mqtt/subscribe", entity, String.class);
                log.info("自动订阅的结果：{}", responseEntity.getBody());
            }).start();
            return;
        }
        //自动取消订阅
        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity("http://121.41.6.44:8081/api/v4/mqtt/unsubscribe", entity, String.class);
        log.info("自动取消订阅的结果:{}", responseEntity.getBody());
    }

}
