package name.abuchen.portfolio.datatransfer.spi;

import java.util.List;
import java.util.Objects;

import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.SecurityCache;

/**
 * Extractor contributed by another bundle. The label shows the origin in the
 * import wizard. It intentionally does not contain the version of the bundle
 * because the wizard stores user preferences by label.
 */
public final class ContributedExtractor implements Extractor
{
    private final Extractor delegate;
    private final String bundleName;
    private final String bundleVersion;

    public ContributedExtractor(Extractor delegate, String bundleName, String bundleVersion)
    {
        this.delegate = Objects.requireNonNull(delegate);
        this.bundleName = Objects.requireNonNull(bundleName);
        this.bundleVersion = Objects.requireNonNull(bundleVersion);
    }

    public Extractor getDelegate()
    {
        return delegate;
    }

    public String getBundleName()
    {
        return bundleName;
    }

    public String getBundleVersion()
    {
        return bundleVersion;
    }

    @Override
    public String getId()
    {
        return delegate.getId();
    }

    @Override
    public String getLabel()
    {
        return delegate.getLabel() + " [Plugin: " + bundleName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public List<Item> extract(SecurityCache securityCache, InputFile file, List<Exception> errors)
    {
        return delegate.extract(securityCache, file, errors);
    }

    @Override
    public void postProcessing(List<Item> result)
    {
        delegate.postProcessing(result);
    }
}
