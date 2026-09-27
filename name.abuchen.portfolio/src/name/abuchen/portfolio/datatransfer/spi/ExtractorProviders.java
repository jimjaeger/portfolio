package name.abuchen.portfolio.datatransfer.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import name.abuchen.portfolio.PortfolioLog;
import name.abuchen.portfolio.model.Client;

/**
 * Collects the extractors of all registered {@link ExtractorProvider}
 * services. The registry is queried on every call, so bundles started or
 * stopped at runtime take effect with the next import.
 */
public final class ExtractorProviders
{
    private ExtractorProviders()
    {
    }

    /**
     * Returns the contributed extractors, or an empty list if running outside
     * of an OSGi framework (e.g. plain unit tests).
     */
    public static List<ContributedExtractor> collect(Client client)
    {
        var bundle = FrameworkUtil.getBundle(ExtractorProviders.class);
        if (bundle == null)
            return List.of();

        var context = bundle.getBundleContext();
        if (context == null)
            return List.of();

        return collect(context, client);
    }

    /* package */ static List<ContributedExtractor> collect(BundleContext context, Client client)
    {
        Collection<ServiceReference<ExtractorProvider>> references;
        try
        {
            references = context.getServiceReferences(ExtractorProvider.class, null);
        }
        catch (InvalidSyntaxException e)
        {
            // cannot happen without filter
            throw new IllegalStateException(e);
        }

        List<ContributedExtractor> answer = new ArrayList<>();

        for (var reference : references)
        {
            var origin = reference.getBundle();
            var provider = context.getService(reference);
            if (origin == null || provider == null)
                continue; // unregistered in the meantime

            try
            {
                for (var extractor : provider.create(client))
                {
                    answer.add(new ContributedExtractor(extractor, origin.getSymbolicName(),
                                    origin.getVersion().toString()));
                }
            }
            catch (RuntimeException | LinkageError e)
            {
                // a faulty contribution (including missing classes of an
                // incompatible plugin) must not break the import
                PortfolioLog.error(e);
            }
            finally
            {
                context.ungetService(reference);
            }
        }

        return answer;
    }
}
