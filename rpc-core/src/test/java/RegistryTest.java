import com.abin.core.config.RegistryConfig;
import com.abin.core.model.ServiceMetaInfo;
import com.abin.core.registry.EtcdRegistry;
import com.abin.core.registry.Registry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class RegistryTest {

    final Registry registry = new EtcdRegistry();

    @Before
    public void init() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("http://localhost:2379");
        registry.init(registryConfig);
    }

    @Test
    public void register() {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setHost("localhost");
        serviceMetaInfo.setPort(String.valueOf(12));
        registry.register(serviceMetaInfo);
        ServiceMetaInfo serviceMetaInfo1 = new ServiceMetaInfo();
        serviceMetaInfo1.setServiceName("myService");
        serviceMetaInfo1.setHost("localhost");
        serviceMetaInfo1.setPort(String.valueOf(23));
        registry.register(serviceMetaInfo1);
        ServiceMetaInfo serviceMetaInfo2 = new ServiceMetaInfo();
        serviceMetaInfo2.setServiceName("myService");
        serviceMetaInfo2.setHost("localhost");
        serviceMetaInfo2.setPort(String.valueOf(34));
        registry.register(serviceMetaInfo2);
    }

    @Test
    public void logout() {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setHost("localhost");
        serviceMetaInfo.setPort(String.valueOf(12));
        registry.logout(serviceMetaInfo);
    }

    @Test
    public void serviceDiscovery() {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        String serviceKey = serviceMetaInfo.getServiceKey();
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceKey);
        Assert.assertNotNull(serviceMetaInfoList);
        System.out.println(serviceMetaInfoList);
    }

}

