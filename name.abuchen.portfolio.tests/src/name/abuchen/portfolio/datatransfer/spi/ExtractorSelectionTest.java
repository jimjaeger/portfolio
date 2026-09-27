package name.abuchen.portfolio.datatransfer.spi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.SecurityCache;

@SuppressWarnings("nls")
public class ExtractorSelectionTest
{
    private static Extractor extractor(String id)
    {
        return new Extractor()
        {
            @Override
            public String getId()
            {
                return id;
            }

            @Override
            public String getLabel()
            {
                return id;
            }

            @Override
            public List<Item> extract(SecurityCache securityCache, InputFile file, List<Exception> errors)
            {
                return List.of();
            }
        };
    }

    private static List<String> ids(List<Extractor> extractors)
    {
        return extractors.stream().map(Extractor::getId).toList();
    }

    private final List<Extractor> builtIn = List.of(extractor("stable1"), extractor("stable2"));
    private final List<Extractor> contributed = List.of(extractor("plugin1"), extractor("plugin2"));

    @Test
    public void testDefaultEvaluatesBuiltInBeforeContributed()
    {
        var arranged = ExtractorSelection.DEFAULT.arrange(builtIn, contributed);

        assertThat(ids(arranged), contains("stable1", "stable2", "plugin1", "plugin2"));
    }

    @Test
    public void testPreferredContributedExtractorIsEvaluatedFirst()
    {
        var selection = new ExtractorSelection(Set.of(), Set.of("plugin2"));

        var arranged = selection.arrange(builtIn, contributed);

        assertThat(ids(arranged), contains("plugin2", "stable1", "stable2", "plugin1"));
    }

    @Test
    public void testDisabledExtractorsAreRemoved()
    {
        var selection = new ExtractorSelection(Set.of("stable1", "plugin2"), Set.of("plugin2"));

        var arranged = selection.arrange(builtIn, contributed);

        assertThat(ids(arranged), contains("stable2", "plugin1"));
    }
}
