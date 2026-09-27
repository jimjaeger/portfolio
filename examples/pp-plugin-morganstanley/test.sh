#!/bin/sh
# Runs the unit tests of the plugin without OSGi (plain JUnit 4).
#
#   - extractor tests with the anonymized fixtures (same style as the PP core tests)
#   - provider contract test
#   - negative test: the extractor must not recognize any fixture of the built-in importers
#
# Prerequisites: sh build.sh; PP core bundle compiled (../../name.abuchen.portfolio/target/classes).
# The test helpers (ExtractorMatchers, ExtractorTestUtilities, AssertImportActions) are compiled from
# the PP sources because PP does not publish them yet (RFC question).
set -e
cd "$(dirname "$0")"

case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
    *) SEP=":" ;;
esac

PP="../.."
CORE="$PP/name.abuchen.portfolio/target/classes"
PPTESTS="$PP/name.abuchen.portfolio.tests/src"
FIXTURES="$PPTESTS/name/abuchen/portfolio/datatransfer/pdf"
P2="$HOME/.m2/repository/p2/osgi/bundle"

# newest jar of a p2 bundle in the local Maven repository (no source jars);
# javac/java on Windows need Windows paths
bundle() {
    jar=$(find "$P2/$1" -name "*.jar" ! -name "*source*" | sort | tail -1)
    if command -v cygpath >/dev/null 2>&1; then cygpath -w "$jar"; else echo "$jar"; fi
}

LIBS="$(bundle org.junit)${SEP}$(bundle org.hamcrest)"
LIBS="$LIBS${SEP}$(bundle org.eclipse.osgi)${SEP}$(bundle org.eclipse.equinox.common)"

[ -d target/fragment ] || { echo "run build.sh first" >&2; exit 1; }

rm -rf target/test-helpers target/test-classes
mkdir -p target/test-helpers target/test-classes

javac --release 21 -encoding UTF-8 -nowarn -cp "$CORE${SEP}$LIBS" -d target/test-helpers \
    "$PPTESTS/name/abuchen/portfolio/datatransfer/ExtractorMatchers.java" \
    "$PPTESTS/name/abuchen/portfolio/datatransfer/ExtractorTestUtilities.java" \
    "$PPTESTS/name/abuchen/portfolio/datatransfer/actions/AssertImportActions.java"

CP="$CORE${SEP}target/fragment${SEP}target/provider${SEP}target/test-helpers${SEP}$LIBS"

javac --release 21 -encoding UTF-8 -nowarn -cp "$CP" -d target/test-classes $(find test/src -name '*.java')

# fixtures next to the test classes (loaded with PDFInputFile.loadTestCase)
(cd test/src && find . -name '*.txt' -exec cp --parents {} ../../target/test-classes/ \;)

java -Dpp.core.fixtures="$FIXTURES" -cp "target/test-classes${SEP}$CP" org.junit.runner.JUnitCore \
    name.abuchen.portfolio.datatransfer.pdf.morganstanley.MorganStanleyPDFExtractorTest \
    pp.plugin.morganstanley.MorganStanleyExtractorProviderTest \
    pp.plugin.morganstanley.CoreFixturesNegativeTest
