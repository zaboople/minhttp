#!/bin/bash
cd $(dirname "$0")
cd .. || exit 1
mvn --batch-mode clean install || exit 1
cd $(dirname "$0")
mvn --batch-mode clean package || exit 1
