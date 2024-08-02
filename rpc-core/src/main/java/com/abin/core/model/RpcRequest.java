package com.abin.core.model;

import com.abin.core.constant.RpcConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest implements Serializable {

    private String serviceName;

    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] args;
}
