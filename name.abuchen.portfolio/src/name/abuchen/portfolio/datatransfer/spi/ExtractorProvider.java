package name.abuchen.portfolio.datatransfer.spi;

import java.util.List;

import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.model.Client;

/**
 * Contributes additional extractors to the PDF import. Bundles register an
 * implementation as OSGi service, for example with Declarative Services:
 *
 * <pre>
 * &#64;Component(service = ExtractorProvider.class)
 * public class MyBankProvider implements ExtractorProvider
 * {
 *     &#64;Override
 *     public List&lt;Extractor&gt; create(Client client)
 *     {
 *         return List.of(new MyBankPDFExtractor(client));
 *     }
 * }
 * </pre>
 * <p>
 * The provider is implemented by the contributing bundle (consumer type): a new
 * abstract method is an incompatible change and requires a new major version
 * of this package.
 */
public interface ExtractorProvider
{
    /**
     * Creates the extractors for the given client. Called once per import run.
     */
    List<Extractor> create(Client client);
}
