package name.abuchen.portfolio.datatransfer.spi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.SecurityCache;
import name.abuchen.portfolio.datatransfer.pdf.PDFImportAssistant;
import name.abuchen.portfolio.model.Client;

/**
 * Runs inside the OSGi test runtime: registers an {@link ExtractorProvider}
 * service and checks that the PDF import picks it up.
 */
@SuppressWarnings("nls")
public class ExtractorProvidersTest
{
    private static final String ID = "test.plugin.extractor";

    private ServiceRegistration<ExtractorProvider> registration;

    @Before
    public void registerProvider()
    {
        var bundle = FrameworkUtil.getBundle(ExtractorProvidersTest.class);
        assertNotNull("test must run in an OSGi framework", bundle);

        ExtractorProvider provider = client -> List.of(new Extractor()
        {
            @Override
            public String getId()
            {
                return ID;
            }

            @Override
            public String getLabel()
            {
                return "Test Plugin";
            }

            @Override
            public List<Item> extract(SecurityCache securityCache, InputFile file, List<Exception> errors)
            {
                return List.of();
            }
        });

        registration = bundle.getBundleContext().registerService(ExtractorProvider.class, provider, null);
    }

    @After
    public void unregisterProvider()
    {
        if (registration != null)
            registration.unregister();
    }

    @Test
    public void testContributedExtractorIsEvaluatedAfterBuiltInExtractors()
    {
        var extractors = new PDFImportAssistant(new Client(), List.of()).getExtractors();

        var last = extractors.get(extractors.size() - 1);
        assertThat(last, instanceOf(ContributedExtractor.class));
        assertThat(last.getId(), is(ID));
        assertThat(last.getLabel(), endsWith("[Plugin: " + FrameworkUtil.getBundle(getClass()).getSymbolicName() + "]"));
    }

    @Test
    public void testPreferredContributedExtractorIsEvaluatedFirst()
    {
        var selection = new ExtractorSelection(Set.of(), Set.of(ID));

        var extractors = new PDFImportAssistant(new Client(), List.of(), selection).getExtractors();

        assertThat(extractors.get(0).getId(), is(ID));
    }

    @Test
    public void testUnregisteredProviderIsGoneWithNextImport()
    {
        var before = new PDFImportAssistant(new Client(), List.of()).getExtractors().size();

        registration.unregister();
        registration = null;

        var after = new PDFImportAssistant(new Client(), List.of()).getExtractors().size();
        assertThat(after, is(before - 1));
    }
}
