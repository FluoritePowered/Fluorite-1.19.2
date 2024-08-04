package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.remapper.generated.RemappingURLClassLoader;
import org.eclipse.aether.repository.RemoteRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

@Mixin(targets = "org.bukkit.plugin.java.LibraryLoader", remap = false)
public class LibraryLoaderMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/eclipse/aether/RepositorySystem;newResolutionRepositories(Lorg/eclipse/aether/RepositorySystemSession;Ljava/util/List;)Ljava/util/List;"), index = 1)
    private List<RemoteRepository> fluorite$mirror(List<RemoteRepository> list) {
        return Collections.singletonList((new RemoteRepository.Builder("Aliyun", "default", "https://maven.aliyun.com/repository/public/")).build());
    }

    @Redirect(method = "createLoader", at = @At(value = "NEW", target = "java/net/URLClassLoader"))
    private URLClassLoader arclight$useRemapped(URL[] urls, ClassLoader loader) {
        return new RemappingURLClassLoader(urls, loader);
    }
}
