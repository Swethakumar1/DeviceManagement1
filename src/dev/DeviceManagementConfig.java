package dev;

import org.glassfish.jersey.server.ResourceConfig;

public class DeviceManagementConfig extends ResourceConfig{

	public DeviceManagementConfig() {
        register(new DeviceManagementBinder());
        packages(true, "dev");
    }
}
