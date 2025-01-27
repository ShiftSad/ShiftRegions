package codes.shiftmc.regions;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

// I mostly know what I'm doing
@SuppressWarnings("UnstableApiUsage")
public class ShiftRegionsLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder pluginClasspathBuilder) {
        var resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());
        resolver.addRepository(new RemoteRepository.Builder("xenondevs", "default", "https://repo.xenondevs.xyz/releases/").build());
        resolver.addDependency(new Dependency(new DefaultArtifact("io.vertx:vertx-sql-client:4.5.11"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("io.projectreactor:reactor-core:3.7.2"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("io.vertx:vertx-mysql-client:4.5.12"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.github.ben-manes.caffeine:caffeine:3.2.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:invui:pom:2.0.0-alpha.7"), null));
        pluginClasspathBuilder.addLibrary(resolver);
    }
}
