package io.izzel.arclight.common.mixin.forge;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.google.common.base.Joiner;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.Logging;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ForgeConfigSpec.class)
public abstract class ForgeConfigSpecMixin {
    // @formatter:off
    @Shadow private Config childConfig;
    @Shadow public abstract boolean isCorrect(CommentedConfig config);
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private static Joiner DOT_JOINER;
    @Shadow public abstract int correct(CommentedConfig config, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener);
    @Shadow public abstract void afterReload();
    // @formatter:on

    /**
     * @author FluoritePowered
     * @reason warn -> debug
     */
    @Overwrite(remap = false)
    public void setConfig(CommentedConfig config) {
        this.childConfig = config;
        if (config != null && !this.isCorrect(config)) {
            String configName = config instanceof FileConfig ? ((FileConfig)config).getNioPath().toString() : config.toString();
            LOGGER.warn(Logging.CORE, "Configuration file {} is not correct. Correcting", configName);
            this.correct(config, (action, path, incorrectValue, correctedValue) -> {
                LOGGER.warn(Logging.CORE, "Incorrect key {} was corrected from {} to its default, {}. {}", DOT_JOINER.join(path), incorrectValue, correctedValue, incorrectValue == correctedValue ? "This seems to be an error." : "");
            }, (action, path, incorrectValue, correctedValue) -> {
                LOGGER.debug(Logging.CORE, "The comment on key {} does not match the spec. This may create a backup.", DOT_JOINER.join(path));
            });
            if (config instanceof FileConfig) {
                ((FileConfig)config).save();
            }
        }
        this.afterReload();
    }
}
