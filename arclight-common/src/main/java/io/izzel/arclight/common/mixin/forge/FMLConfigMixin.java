package io.izzel.arclight.common.mixin.forge;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.fml.loading.FMLConfig;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Path;
import java.util.Objects;

import static net.minecraftforge.fml.loading.LogMarkers.CORE;

@Mixin(FMLConfig.class)
public class FMLConfigMixin {
    // @formatter:off
    @Shadow private CommentedFileConfig configData;
    @Shadow private static ConfigSpec configSpec;
    @Shadow @Final private static Logger LOGGER;
    // @formatter:on

    /**
     * @author FluoritePowered
     * @reason warn -> debug
     */
    @Overwrite(remap = false)
    private void loadFrom(final Path configFile) {
        configData = CommentedFileConfig.builder(configFile).sync()
                .onFileNotFound(FileNotFoundAction.copyData(Objects.requireNonNull(getClass().getResourceAsStream("/META-INF/defaultfmlconfig.toml"))))
                .autosave().autoreload()
                .writingMode(WritingMode.REPLACE)
                .build();
        try {
            configData.load();
        } catch (ParsingException e) {
            throw new RuntimeException("Failed to load FML config from " + configFile.toString(), e);
        }
        if (!configSpec.isCorrect(configData)) {
            LOGGER.warn(CORE, "Configuration file {} is not correct. Correcting", configFile);
            configSpec.correct(configData, (action, path, incorrectValue, correctedValue) ->
                    LOGGER.warn(CORE, "Incorrect key {} was corrected from {} to {}", path, incorrectValue, correctedValue));
        }
        configData.save();
    }

}
