package name.abuchen.portfolio.datatransfer.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import name.abuchen.portfolio.datatransfer.Extractor;

/**
 * User selection of the extractors: disabled extractors are skipped, preferred
 * (contributed) extractors are evaluated before the built-in extractors.
 * Extractors are identified by {@link Extractor#getId()}.
 */
public record ExtractorSelection(Set<String> disabled, Set<String> preferred)
{
    public static final ExtractorSelection DEFAULT = new ExtractorSelection(Set.of(), Set.of());

    public ExtractorSelection
    {
        disabled = Set.copyOf(disabled);
        preferred = Set.copyOf(preferred);
    }

    public boolean isDisabled(Extractor extractor)
    {
        return disabled.contains(extractor.getId());
    }

    public boolean isPreferred(Extractor extractor)
    {
        return preferred.contains(extractor.getId());
    }

    /**
     * Returns the extractors in the order they are evaluated: preferred
     * contributed extractors, then the built-in extractors, then the remaining
     * contributed extractors. Disabled extractors are removed.
     */
    public List<Extractor> arrange(List<? extends Extractor> builtIn, List<? extends Extractor> contributed)
    {
        List<Extractor> answer = new ArrayList<>();
        contributed.stream().filter(this::isPreferred).forEach(answer::add);
        answer.addAll(builtIn);
        contributed.stream().filter(e -> !isPreferred(e)).forEach(answer::add);
        answer.removeIf(this::isDisabled);
        return answer;
    }
}
