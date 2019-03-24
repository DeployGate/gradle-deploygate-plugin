#!/usr/bin/env bash

set -eu

# Usage:
# export BUNDLE_TOOL_VERSION=... # use a default version unless specified
# bundle_universal_apk.bash <aab_file> <apk_file_to_be_saved> [additional options for bundletool]
#
# Example:
# bundle_universal_apk.bash \
#   "app/build/outputs/bundle/flavor1Debug/app.aab" \
#   app.apk \
#   --ks="~/.android/debug.keystore" \
#   --ks-key-alias="androiddebugkey" \
#   --ks-pass="pass:android" \
#   --key-pass="pass:android"
#


mktempdir() {
  local -r name="$(basename $(mktemp -u))"
  mkdir -p "$name" >/dev/null
  echo "$PWD/$name"
}

readonly temp_dir=$(mktempdir)

trap "rm -fr ${temp_dir}" ERR 1 2 3 15

: "${BUNDLE_TOOL_VERSION:=0.8.0}"

readonly bundle_file_path="$1"
readonly save_to="$2"

shift 2

download_bundletool() {
    curl -sSL# \
      -o "$temp_dir/bundletool.jar" \
      "https://github.com/google/bundletool/releases/download/$BUNDLE_TOOL_VERSION/bundletool-all-$BUNDLE_TOOL_VERSION.jar"
}

build_apks() {
    java -jar "$temp_dir/bundletool.jar" \
      build-apks \
      --bundle="$bundle_file_path" \
      --output="$temp_dir/bundle.apks" \
      --overwrite \
      --mode=universal \
      "$@"
}

download_bundletool
build_apks "$@"

unzip "$temp_dir/bundle.apks" -d "$temp_dir"

readonly apk_file_path=$(find "$temp_dir" -name "*.apk")

mkdir -p "$(dirname "$save_to")"

cp "$apk_file_path" "$save_to"

rm -fr "${temp_dir}"
