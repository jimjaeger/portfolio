#!/bin/sh
# Builds the two plugin bundles against the compiled classes of the PP core bundle.
#
#   fragment -> target/pp.plugin.morganstanley.extractor_0.1.0.jar  (extractor, uses the package-private PDF DSL)
#   provider -> target/pp.plugin.morganstanley_0.1.0.jar            (ExtractorProvider service via Declarative Services)
#
# Prerequisite: the core bundle has been built, e.g. with the Maven build of this repository
# (../../name.abuchen.portfolio/target/classes).
# Install: copy both JARs into <workspace>/plugins and enable "Load importer plugins" in the preferences.
set -e
cd "$(dirname "$0")"

CORE="../../name.abuchen.portfolio/target/classes"
if [ ! -d "$CORE" ]; then
    echo "core classes not found: $CORE (build Portfolio Performance first)" >&2
    exit 1
fi

# javac on Windows (also from Git Bash) expects ';' as class path separator
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
    *) SEP=":" ;;
esac

rm -rf target
mkdir -p target/fragment target/provider

javac --release 21 -encoding UTF-8 -nowarn -cp "$CORE" -d target/fragment \
    $(find fragment/src -name '*.java')
jar --create --file target/pp.plugin.morganstanley.extractor_0.1.0.jar \
    --manifest fragment/META-INF/MANIFEST.MF -C target/fragment .

javac --release 21 -encoding UTF-8 -nowarn -cp "$CORE${SEP}target/fragment" -d target/provider \
    $(find provider/src -name '*.java')
cp -r provider/OSGI-INF target/provider/
jar --create --file target/pp.plugin.morganstanley_0.1.0.jar \
    --manifest provider/META-INF/MANIFEST.MF -C target/provider .

ls -l target/*.jar
