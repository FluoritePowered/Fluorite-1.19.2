package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class OptimizationSpec {

    @Setting("cache-plugin-class")
    private boolean cachePluginClass;

    @Setting("goal-selector-update-interval")
    private int goalSelectorInterval;

    @Setting("enable-teleport-command-permission-check")
    private boolean enableTeleportCommandPermissionCheck;

    public boolean enableTeleportCommandPermissionCheck() {
        return enableTeleportCommandPermissionCheck;
    }

    public boolean isCachePluginClass() {
        return cachePluginClass;
    }

    public int getGoalSelectorInterval() {
        return goalSelectorInterval;
    }
}
