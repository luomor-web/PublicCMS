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
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import com.publiccms.common.annotation.Csrf;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.logic.component.config.SimpleAiConfigComponent;
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

    @RequestMapping("chat")
    @Csrf
    public ResponseEntity<ResponseBodyEmitter> chat(@RequestAttribute SysSite site, @SessionAttribute SysUser admin,
            @ModelAttribute SimpleAiMessageParameters simpleAiMessageParameters, String skipWord, HttpServletRequest request,
            ModelMap model) {
        HttpRequest httpRequest = simpleAiConfigComponent.getHttpRequest(site.getId(), simpleAiMessageParameters.getMessages());
        if (null != request) {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter(0L);
            HttpResponse.BodySubscriber<String> subscriber = HttpResponse.BodySubscribers
                    .fromSubscriber(new ResultSender(emitter), ResultSender::getEnd);
            client.sendAsync(httpRequest, response -> subscriber).thenAccept(HttpResponse::body).thenAccept(log::info);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(emitter);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    class ResultSender implements Flow.Subscriber<List<ByteBuffer>> {
        private ResponseBodyEmitter emitter;

        public ResultSender(ResponseBodyEmitter emitter) {
            this.emitter = emitter;
        }

        public String getEnd() {
            return "end";
        }

        @Override
        public void onSubscribe(Subscription subscription) {
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
            String result = response.trim().replaceAll("data: ", "").replaceAll("\n", "");
            if (CommonUtils.notEmpty(result) && !"[]".equals(result) && !"[DONE]".equalsIgnoreCase(result)) {
                try {
                    emitter.send(result, MediaType.APPLICATION_JSON);
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            emitter.completeWithError(throwable);
        }

        @Override
        public void onComplete() {
            emitter.complete();
        }

    }
}
