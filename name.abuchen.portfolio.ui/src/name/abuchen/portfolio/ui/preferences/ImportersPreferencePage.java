package name.abuchen.portfolio.ui.preferences;

import java.text.MessageFormat;
import java.util.Comparator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.osgi.framework.BundleException;

import name.abuchen.portfolio.PortfolioLog;
import name.abuchen.portfolio.datatransfer.spi.ContributedExtractor;
import name.abuchen.portfolio.datatransfer.spi.ExtractorProviders;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.ui.UIConstants;
import name.abuchen.portfolio.ui.addons.ImporterPluginsAddon;

/**
 * Configures the importer plugins: loading from the plugins folder, starting
 * and stopping of plugins (immediately), and disabling or preferring the
 * contributed importers.
 */
public class ImportersPreferencePage extends FieldEditorPreferencePage
{
    public ImportersPreferencePage()
    {
        super(GRID);
        setTitle(Messages.PrefTitleImporters);
    }

    @Override
    public void createFieldEditors()
    {
        addField(new DescriptionFieldEditor(Messages.PrefMsgImporterPluginsWarning, getFieldEditorParent()));

        addField(new BooleanFieldEditor(UIConstants.Preferences.IMPORTER_PLUGINS_ENABLED,
                        MessageFormat.format(Messages.PrefLabelLoadImporterPlugins,
                                        ImporterPreferences.getPluginsFolder().getAbsolutePath()),
                        getFieldEditorParent()));

        ImporterPluginsAddon.resolveInstalledPlugins();

        var plugins = ImporterPluginsAddon.getInstalledPlugins().stream()
                        .sorted(Comparator.comparing(b -> b.getSymbolicName())) //
                        .map(b -> new String[] { b.getSymbolicName() + " " + b.getVersion() //$NON-NLS-1$
                                        + (ImporterPluginsAddon.isFragment(b) ? " (fragment)" : "") //$NON-NLS-1$ //$NON-NLS-2$
                                        + (ImporterPluginsAddon.isIncompatible(b) ? " (incompatible)" : ""), //$NON-NLS-1$ //$NON-NLS-2$
                                        b.getSymbolicName() })
                        .toArray(String[][]::new);

        if (plugins.length == 0)
        {
            addField(new DescriptionFieldEditor(Messages.PrefMsgNoImporterPlugins, getFieldEditorParent()));
            return;
        }

        addField(new CheckboxGroupFieldEditor(UIConstants.Preferences.IMPORTER_PLUGINS_ACTIVE,
                        Messages.PrefLabelImporterPluginsActive, plugins, getFieldEditorParent()));

        var extractors = ExtractorProviders.collect(new Client()).stream()
                        .sorted(Comparator.comparing(ContributedExtractor::getLabel)) //
                        .map(e -> new String[] { e.getLabel(), e.getId() }) //
                        .toArray(String[][]::new);

        if (extractors.length > 0)
        {
            addField(new CheckboxGroupFieldEditor(UIConstants.Preferences.IMPORTER_PREFERRED,
                            Messages.PrefLabelPreferredImporters, extractors, getFieldEditorParent()));
            addField(new CheckboxGroupFieldEditor(UIConstants.Preferences.IMPORTER_DISABLED,
                            Messages.PrefLabelDisabledImporters, extractors, getFieldEditorParent()));
        }
    }

    @Override
    public boolean performOk()
    {
        var result = super.performOk();

        // start or stop the plugins immediately
        var active = ImporterPreferences.getActivePlugins();
        for (var bundle : ImporterPluginsAddon.getInstalledPlugins())
        {
            try
            {
                ImporterPluginsAddon.setActive(bundle, active.contains(bundle.getSymbolicName()));
            }
            catch (BundleException e)
            {
                PortfolioLog.error(e);
            }
        }

        return result;
    }
}
