package name.abuchen.portfolio.ui.preferences;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Platform;

import name.abuchen.portfolio.datatransfer.spi.ExtractorSelection;
import name.abuchen.portfolio.ui.PortfolioPlugin;
import name.abuchen.portfolio.ui.UIConstants;

/**
 * Access to the importer preferences (plugins folder, disabled and preferred
 * extractors).
 */
public final class ImporterPreferences
{
    private ImporterPreferences()
    {
    }

    /**
     * Returns the folder from which importer plugins are installed:
     * <code>&lt;workspace&gt;/plugins</code>.
     */
    public static File getPluginsFolder()
    {
        return new File(Platform.getInstanceLocation().getURL().getFile(), "plugins"); //$NON-NLS-1$
    }

    public static boolean isPluginsEnabled()
    {
        return PortfolioPlugin.getDefault().getPreferenceStore()
                        .getBoolean(UIConstants.Preferences.IMPORTER_PLUGINS_ENABLED);
    }

    public static Set<String> getActivePlugins()
    {
        return read(UIConstants.Preferences.IMPORTER_PLUGINS_ACTIVE);
    }

    public static ExtractorSelection getSelection()
    {
        return new ExtractorSelection(read(UIConstants.Preferences.IMPORTER_DISABLED),
                        read(UIConstants.Preferences.IMPORTER_PREFERRED));
    }

    private static Set<String> read(String key)
    {
        var serialized = PortfolioPlugin.getDefault().getPreferenceStore().getString(key);
        if (serialized == null || serialized.isBlank())
            return Set.of();
        return Arrays.stream(serialized.split(",")).filter(s -> !s.isBlank()).collect(Collectors.toSet()); //$NON-NLS-1$
    }
}
