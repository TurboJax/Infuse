package com.catadmirer.infuseSMP.platform;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.platform.services.EffectRegistry;

import java.util.ServiceLoader;

public class Services {
    public static final EffectRegistry PLATFORM = load(EffectRegistry.class);

    // This code is used to load a service for the current environment. Your implementation of the service must be defined
    // manually by including a text file in META-INF/services named with the fully qualified class name of the service.
    // Inside the file you should write the fully qualified class name of the implementation to load for the platform. For
    // example our file on Forge points to ForgePlatformHelper while Fabric points to FabricPlatformHelper.
    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Infuse.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
