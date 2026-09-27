package pp.plugin.morganstanley;

import java.util.List;

import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.pdf.MorganStanleyPDFExtractor;
import name.abuchen.portfolio.datatransfer.spi.ExtractorProvider;
import name.abuchen.portfolio.model.Client;

/**
 * Contributes the Morgan Stanley importer. Registered as OSGi service via
 * Declarative Services (see OSGI-INF).
 * <p>
 * The extractor itself lives in a fragment of the core bundle because the PDF
 * parser DSL is package-private. With a public DSL API the extractor could
 * live in this bundle.
 */
public class MorganStanleyExtractorProvider implements ExtractorProvider
{
    @Override
    public List<Extractor> create(Client client)
    {
        return List.of(new MorganStanleyPDFExtractor(client));
    }
}
