package com.atguigu.gmall.pms.feign;

        import com.atguigu.gmall.sms.api.GmallSmsApi;
        import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author XieHaidong
 * @create 2021-01-23 10:58
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
