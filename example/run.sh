#!/bin/bash
cd $(dirname "$0")
mvn --batch-mode clean package || exit 1
cp target/hello*.jar ~/temp/ || exit 1
java -Xmx15m -jar "$(cygpath -w ~/temp/hello-1.0.0.jar)" "$@"
