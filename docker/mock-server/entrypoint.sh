#!/usr/bin/env bash

set -euo pipefail

rm -fr tmp/pids/*

bundle exec rails server -b '0.0.0.0' -p 3000