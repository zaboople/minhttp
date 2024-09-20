# Minimal HTTP Server

## Why

This is mostly a Java wrapper for the Jetty web server, which of course does most of the heavy lifting, but I created a mapping system for pointing URL's at functions and is lambda-able.

The secondary point of this was to demonstrate a Java web server that boots up quickly (under half a second) and uses a rather small amount of memory (around 30MB). I get tired of people blaming Java for their badly behaved frameworks of choice and the overbearing, overweight behavior of such, i.e.: No, it's not Java. It's your bloated open-source frameworks that cause all the problems. We'll continue using such things for the time being because work is work and that's how we work, but we might do better in the future if we start recognizing *why* we have these issues instead of shifting blame.

This still uses Maven, which is inexcusably slow (Gradle is worse) but one thing at a time...

## How to Run