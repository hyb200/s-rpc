package com.abin.core.model;

import cn.hutool.core.util.StrUtil;
import com.abin.core.constant.RpcConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMetaInfo {

    private String serviceName;

    @Builder.Default
    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;

    private String host;

    private String port;

    @Builder.Default
    private String group = "default";

    public String getServiceKey() {
        //  todo 扩展服务分组
        return String.format("%s/%s/%s", serviceName, serviceVersion, group);
    }

    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), host, port);
    }

    public String getServiceAddress() {
        if (!StrUtil.contains(host, "http")) {
            return String.format("http://%s:%s", host, port);
        }
        return String.format("%s:%s", host, port);
    }
}
