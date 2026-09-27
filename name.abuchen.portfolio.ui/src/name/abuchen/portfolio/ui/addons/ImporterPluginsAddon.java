package name.abuchen.portfolio.ui.addons;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.FrameworkWiring;

import name.abuchen.portfolio.PortfolioLog;
import name.abuchen.portfolio.ui.preferences.ImporterPreferences;

/**
 * Installs importer plugins (OSGi bundles) from the plugins folder of the
 * workspace. Plugins are only started if the user activated them. Fragments
 * attach to their host with the next start of the application.
 */
public class ImporterPluginsAddon
{
    @PostConstruct
    public void installPlugins()
    {
        if (!ImporterPreferences.isPluginsEnabled())
            return;

        var context = FrameworkUtil.getBundle(ImporterPluginsAddon.class).getBundleContext();
        var folder = ImporterPreferences.getPluginsFolder();
        var jars = folder.listFiles((dir, name) -> name.endsWith(".jar")); //$NON-NLS-1$
        var files = jars != null ? Arrays.asList(jars) : List.<File>of();

        uninstallRemoved(context, folder, files);

        List<Bundle> installed = new ArrayList<>();

        for (var jar : files)
        {
            try
            {
                installed.add(install(context, jar));
            }
            catch (BundleException | RuntimeException e)
            {
                // a broken plugin must not break the application
                PortfolioLog.error(e);
            }
        }

        // resolve all plugins (fragments attach to their host if possible)
        // so that incompatible plugins can be detected before starting them
        resolve(context, installed);

        var active = ImporterPreferences.getActivePlugins();
        for (var bundle : installed)
        {
            try
            {
                setActive(bundle, active.contains(bundle.getSymbolicName()));
            }
            catch (BundleException | RuntimeException e)
            {
                PortfolioLog.error(e);
            }
        }
    }

    /**
     * Resolves the given bundles. Bundles that remain unresolved are
     * incompatible, e.g. because the required API version is missing.
     */
    public static void resolve(BundleContext context, Collection<Bundle> bundles)
    {
        if (bundles.isEmpty())
            return;

        var wiring = context.getBundle(Constants.SYSTEM_BUNDLE_LOCATION).adapt(FrameworkWiring.class);
        if (!wiring.resolveBundles(bundles))
        {
            bundles.stream().filter(ImporterPluginsAddon::isIncompatible).forEach(b -> PortfolioLog
                            .warning("Importer plugin cannot be resolved: " + b.getSymbolicName() + " " + b.getVersion())); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Resolves the installed plugins, e.g. before showing their state.
     */
    public static void resolveInstalledPlugins()
    {
        var context = FrameworkUtil.getBundle(ImporterPluginsAddon.class).getBundleContext();
        resolve(context, getInstalledPlugins());
    }

    /**
     * Returns the bundles installed from the plugins folder.
     */
    public static List<Bundle> getInstalledPlugins()
    {
        var context = FrameworkUtil.getBundle(ImporterPluginsAddon.class).getBundleContext();
        var prefix = ImporterPreferences.getPluginsFolder().toURI().toString();
        return Arrays.stream(context.getBundles()).filter(b -> b.getLocation().startsWith(prefix)).toList();
    }

    public static boolean isFragment(Bundle bundle)
    {
        return bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null;
    }

    /**
     * Starts or stops the bundle. Takes effect immediately: the contributed
     * extractors (OSGi services) are registered or unregistered.
     */
    public static void setActive(Bundle bundle, boolean active) throws BundleException
    {
        if (isFragment(bundle))
            return;

        if (active && bundle.getState() != Bundle.ACTIVE)
            bundle.start(Bundle.START_TRANSIENT);
        else if (!active && bundle.getState() == Bundle.ACTIVE)
            bundle.stop(Bundle.STOP_TRANSIENT);
    }

    /**
     * Returns true if the bundle cannot be resolved, e.g. because it requires
     * a version of the importer API that is not available. Only meaningful
     * after {@link #resolve(BundleContext, Collection)}.
     */
    public static boolean isIncompatible(Bundle bundle)
    {
        return bundle.getState() == Bundle.INSTALLED;
    }

    private static Bundle install(BundleContext context, File jar) throws BundleException
    {
        var location = jar.toURI().toString();
        var existing = context.getBundle(location);

        if (existing == null)
        {
            var bundle = context.installBundle(location);
            PortfolioLog.info("Installed importer plugin " + bundle.getSymbolicName() + " " + bundle.getVersion()); //$NON-NLS-1$ //$NON-NLS-2$
            return bundle;
        }

        if (existing.getLastModified() < jar.lastModified())
        {
            existing.update();
            PortfolioLog.info("Updated importer plugin " + existing.getSymbolicName() + " " + existing.getVersion()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return existing;
    }

    private static void uninstallRemoved(BundleContext context, File folder, List<File> files)
    {
        var prefix = folder.toURI().toString();
        Set<String> locations = Set.copyOf(files.stream().map(f -> f.toURI().toString()).toList());

        for (var bundle : context.getBundles())
        {
            if (!bundle.getLocation().startsWith(prefix) || locations.contains(bundle.getLocation()))
                continue;

            try
            {
                bundle.uninstall();
                PortfolioLog.info("Uninstalled importer plugin " + bundle.getSymbolicName()); //$NON-NLS-1$
            }
            catch (BundleException e)
            {
                PortfolioLog.error(e);
            }
        }
    }
}
