package com.publiccms.common.api;

import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletResponse;

import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.trade.TradePayment;
import com.publiccms.entities.trade.TradeRefund;

public interface PaymentGateway extends Container<String> {
    String getAccountType();

    default Supplier<String> keyFunction() {
        return () -> getAccountType();
    }

    boolean enabled(short siteId);

    boolean pay(SysSite site, TradePayment payment, String paymentType, String callbackUrl, HttpServletResponse response);

    boolean confirmPay(short siteId, TradePayment payment);

    boolean refund(short siteId, TradePayment payment, TradeRefund refund);
}
