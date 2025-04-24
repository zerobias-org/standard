#! /bin/sh

set -x 

if [ $# -lt 4 ]; then
    echo "Usage: $0 <standard_type> <vendor> <suite> <version>"
    exit 1
fi

BASE_DIR=$(dirname $0)
TYPE=$1
VENDOR=$2
SUITE=$3
VERSION=$4
CODE="$2\_$3\_$4"
FOLDER_PATH="$BASE_DIR/../package/$TYPE/$VENDOR/$SUITE/$VERSION"

if [ ! -d "$FOLDER_PATH" ]; then
  echo "Creating folder $FOLDER_PATH."
  mkdir -p $FOLDER_PATH
fi

PACKAGE_TYPE=$TYPE
if [ $TYPE != "framework" ] && [ $TYPE != "benchmark" ]; then
  PACKAGE_TYPE=standard
fi

cp -r $BASE_DIR/../templates/* $FOLDER_PATH
cp  $BASE_DIR/../.npmrc $FOLDER_PATH

sed -i "s/{type}/$PACKAGE_TYPE/g" $FOLDER_PATH/package.json
sed -i "s/{vendor}/$VENDOR/g" $FOLDER_PATH/package.json
sed -i "s/{suite}/$SUITE/g" $FOLDER_PATH/package.json
sed -i "s/{version}/$VERSION/g" $FOLDER_PATH/package.json

UUID=$(uuidgen)
sed -i "s/{id}/$UUID/g" $FOLDER_PATH/index.yml
sed -i "s/{type}/$TYPE/g" $FOLDER_PATH/index.yml
sed -i "s/{code}/$CODE/g" $FOLDER_PATH/index.yml
