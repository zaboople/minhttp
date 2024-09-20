#!/bin/bash
cd $(dirname "$0")
mvn --batch-mode clean package || exit 1

# Running out of temp dir so I can do compiles while still running:
mkdir -p temp
cp target/hello*.jar ./temp/ || exit 1
java -Xmx15m -jar "$(cygpath -w ./temp/hello-1.0.0.jar)" "$@"
