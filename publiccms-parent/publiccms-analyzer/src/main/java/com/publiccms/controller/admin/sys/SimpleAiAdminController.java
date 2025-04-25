package com.publiccms.controller.admin.sys;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import com.publiccms.common.annotation.Csrf;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.RequestUtils;
import com.publiccms.entities.log.LogOperate;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.logic.component.config.SimpleAiConfigComponent;
import com.publiccms.logic.service.log.LogLoginService;
import com.publiccms.logic.service.log.LogOperateService;
import com.publiccms.views.pojo.model.SimpleAiMessageParameters;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * SimpleAiAdminController
 * 
 */
@Controller
@RequestMapping("simpleAi")
public class SimpleAiAdminController {
    protected final Log log = LogFactory.getLog(getClass());
    @Resource
    private SimpleAiConfigComponent simpleAiConfigComponent;
    private static HttpClient client = HttpClient.newHttpClient();
    @Resource
    protected LogOperateService logOperateService;

    @PostMapping("doChat")
    @Csrf
    public ResponseEntity<ResponseBodyEmitter> chat(@RequestAttribute SysSite site, @SessionAttribute SysUser admin,
            @ModelAttribute SimpleAiMessageParameters simpleAiMessageParameters, HttpServletRequest request) {
        HttpRequest httpRequest = simpleAiConfigComponent.getChatRequest(site.getId(), simpleAiMessageParameters.getMessages());
        if (null != httpRequest) {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter(0L);
            HttpResponse.BodySubscriber<String> subscriber = HttpResponse.BodySubscribers
                    .fromSubscriber(new ResultSender(emitter), ResultSender::getResult);
            String ip = RequestUtils.getIpAddress(request);
            client.sendAsync(httpRequest, response -> subscriber).thenApply(HttpResponse::body).thenAccept(result -> {
                logOperateService
                        .save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(), LogLoginService.CHANNEL_WEB_MANAGER,
                                "ai.chat", ip, CommonUtils.getDate(),
                                CommonUtils.joinString(
                                        simpleAiMessageParameters.getMessages()
                                                .get(simpleAiMessageParameters.getMessages().size() - 1).getContent(),
                                        ":", result)));
            });
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(emitter);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    class ResultSender implements Flow.Subscriber<List<ByteBuffer>> {
        private ResponseBodyEmitter emitter;
        private Subscription subscription;
        private StringBuilder result = new StringBuilder();

        public ResultSender(ResponseBodyEmitter emitter) {
            this.emitter = emitter;
        }

        public String getResult() {
            return result.toString();
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(List<ByteBuffer> item) {
            byte[] data = new byte[item.stream().mapToInt(ByteBuffer::remaining).sum()];
            int offset = 0;
            for (ByteBuffer buffer : item) {
                int remain = buffer.remaining();
                buffer.get(data, offset, remain);
                offset += remain;
            }
            String response = new String(data);
            String temp = response.replaceAll("data: ", "").replaceAll("[DONE]", "");
            if (CommonUtils.notEmpty(temp)) {
                try {
                    result.append(temp);
                    emitter.send(temp, MediaType.TEXT_PLAIN);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                    subscription.cancel();
                    log.error(e.getMessage());
                    return ;
                }
            }
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            emitter.completeWithError(throwable);
            subscription.cancel();
        }

        @Override
        public void onComplete() {
            emitter.complete();
        }

    }
}
