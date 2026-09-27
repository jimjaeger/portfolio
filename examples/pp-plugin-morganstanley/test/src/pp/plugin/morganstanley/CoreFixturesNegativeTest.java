package pp.plugin.morganstanley;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assume.assumeNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.pdf.PDFInputFile;
import name.abuchen.portfolio.model.Client;

/**
 * Plugin check: the contributed extractor must not recognize any document of
 * the built-in importers. The first extractor with a match wins, so an
 * extractor that matches too generously (in particular if preferred by the
 * user) would take over documents of other banks.
 * <p>
 * The folder with the test fixtures of the core importers is passed with the
 * system property <code>pp.core.fixtures</code>.
 */
@SuppressWarnings("nls")
public class CoreFixturesNegativeTest
{
    @Test
    public void testDoesNotRecognizeDocumentsOfBuiltInImporters() throws IOException
    {
        var folder = System.getProperty("pp.core.fixtures");
        assumeNotNull(folder);

        var provider = new MorganStanleyExtractorProvider();
        List<String> recognized = new ArrayList<>();
        var count = 0;

        try (Stream<Path> files = Files.walk(Path.of(folder)))
        {
            for (var file : files.filter(p -> p.toString().endsWith(".txt")).toList())
            {
                count++;
                var text = read(file);

                for (Extractor extractor : provider.create(new Client()))
                {
                    var items = extractor.extract(PDFInputFile.createTestCase(file.getFileName().toString(), text),
                                    new ArrayList<>());
                    if (!items.isEmpty())
                        recognized.add(Path.of(folder).relativize(file).toString());
                }
            }
        }

        assertThat("no core fixtures found in " + folder, count, greaterThan(0));
        assertThat("documents of other importers recognized", recognized, empty());
    }

    private static String read(Path file)
    {
        try
        {
            // like PDFInputFile.loadSingleTestCase: malformed input is
            // replaced (some core fixtures are not valid UTF-8)
            return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
