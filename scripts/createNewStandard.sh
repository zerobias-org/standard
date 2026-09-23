#!/bin/sh
# Scaffold a new standard package:
#   package/<standard_type>/<vendor>/<suite>/<version>/
#
# <standard_type> is the index.yml standardType (law, technical, guidance, ...)
# and doubles as the category folder. It is dropped from the package identity:
#   npm name          @zerobias-org/standard-<vendor>-<suite>-<version>
#   zerobias.package  <vendor>.<suite>.<version>.standard   (dots in <version> → _)
#   code              <vendor>_<suite>_<version>            (same normalization)
# which is exactly what build.gradle.kts's validator derives from the path.
set -eu

if [ $# -lt 4 ]; then
  echo "Usage: $0 <standard_type> <vendor> <suite> <version>"
  exit 1
fi

BASE_DIR=$(cd "$(dirname "$0")/.." && pwd)
TYPE=$1
VENDOR=$2
SUITE=$3
VERSION=$4
PKG_VERSION=$(printf '%s' "$VERSION" | tr '.' '_')
CODE="${VENDOR}_${SUITE}_${PKG_VERSION}"
FOLDER_PATH="$BASE_DIR/package/$TYPE/$VENDOR/$SUITE/$VERSION"

if [ -e "$FOLDER_PATH" ]; then
  echo "$FOLDER_PATH already exists."
  exit 1
fi
mkdir -p "$FOLDER_PATH"

# `cp -R dir/.` copies dotfiles (.npmrc) too, unlike `dir/*`.
cp -R "$BASE_DIR/templates/." "$FOLDER_PATH"

# Portable in-place substitution (BSD and GNU sed disagree on `-i`).
fill() {
  file=$1; shift
  tmp="$file.tmp"
  sed "$@" "$file" > "$tmp" && mv "$tmp" "$file"
}

fill "$FOLDER_PATH/package.json" \
  -e "s/{type}/$TYPE/g" -e "s/{vendor}/$VENDOR/g" -e "s/{suite}/$SUITE/g" \
  -e "s/\.{version}\.standard/.$PKG_VERSION.standard/" -e "s/{version}/$VERSION/g"

UUID=$(uuidgen | tr '[:upper:]' '[:lower:]')
fill "$FOLDER_PATH/index.yml" \
  -e "s/{id}/$UUID/g" -e "s/{type}/$TYPE/g" -e "s/{code}/$CODE/g"

echo "Created $FOLDER_PATH"
echo "Next: fill the remaining {placeholders} in index.yml and package.json, add elements/, then run"
echo "  ./gradlew :$TYPE:$VENDOR:$SUITE:$VERSION:gate"
