package pp.plugin.morganstanley;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import name.abuchen.portfolio.model.Client;

/**
 * Contract of the provider: it creates the extractor with a stable id.
 */
@SuppressWarnings("nls")
public class MorganStanleyExtractorProviderTest
{
    @Test
    public void testCreatesMorganStanleyExtractor()
    {
        var extractors = new MorganStanleyExtractorProvider().create(new Client());

        assertThat(extractors, hasSize(1));
        assertThat(extractors.get(0).getId(), is("name.abuchen.portfolio.datatransfer.pdf.MorganStanleyPDFExtractor"));
        assertThat(extractors.get(0).getLabel(), is("Morgan Stanley Smith Barney LLC"));
    }
}
