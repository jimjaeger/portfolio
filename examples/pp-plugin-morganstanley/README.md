# Example importer plugin: Morgan Stanley

Example for the pluggable importer proposal (branch `pluggable-importer`, forum topic
"Vorschlag: Pluggable Importer für Portfolio Performance"). It packages the Morgan Stanley
PDF importer from PR #6081 as a plugin that Portfolio Performance loads at runtime.

| JAR | Content |
|---|---|
| `pp.plugin.morganstanley.extractor_0.1.0.jar` | **Fragment** of `name.abuchen.portfolio` with the `MorganStanleyPDFExtractor`. A fragment is needed because the PDF parser DSL (`PDFParser`, `DocumentType`, `Block`, ...) is package-private. |
| `pp.plugin.morganstanley_0.1.0.jar` | Bundle with the `ExtractorProvider` service (Declarative Services, see `OSGI-INF`). Imports `name.abuchen.portfolio.datatransfer.spi;version="[1.0,2)"`. |

Two JARs are only necessary because Declarative Services does not process fragments. With a
public DSL API the extractor and the provider would live in one bundle.

## Build

```sh
sh build.sh
```

Requires JDK 21 and the compiled core bundle (`../../name.abuchen.portfolio/target/classes`),
e.g. after `mvn -f portfolio-app/pom.xml verify -Plocal-dev`.

## Test

```sh
sh test.sh    # after build.sh
```

The tests are plain JUnit 4 tests and run **without OSGi**:

| Test | Checks |
|---|---|
| `MorganStanleyPDFExtractorTest` | the extractor with anonymized fixtures, same style as the core tests (`ExtractorMatchers`, `AssertImportActions`) |
| `MorganStanleyExtractorProviderTest` | contract of the provider: one extractor with stable id and label |
| `CoreFixturesNegativeTest` | the extractor recognizes **none** of the test documents of the built-in importers (2684 fixtures of 134 banks). Important because the first extractor with a match wins. |

The test helpers of Portfolio Performance (`ExtractorMatchers`, `ExtractorTestUtilities`,
`AssertImportActions`) are not published. `test.sh` therefore compiles them from the sources of
this repository - a workaround until they are available in `name.abuchen.portfolio.junit`.

## Install

1. Copy both JARs into `<workspace>/plugins`. The folder is shown on the preference page **Importers**.
2. On the preference page **Importers** enable "Load importer plugins ..." and restart Portfolio Performance.
3. On the same page activate `pp.plugin.morganstanley`. This takes effect immediately.
4. Import a Morgan Stanley PDF. The import wizard shows the importer as
   "Morgan Stanley Smith Barney LLC [Plugin: pp.plugin.morganstanley]".

## Limitations

- The fragment is bound to Portfolio Performance 0.87.x (`Fragment-Host` version range) because it uses internal classes.
- The test helpers are compiled from the Portfolio Performance sources (see above).
