# WPManager

An automated manager for WordPress Multisite instances.

## Brief Introduction

...

## Requirements

Execution Requirements:

* Java 1.8 or higher
* PHP 5.6 or higher
* Bash 4.0 or higher

Practical Usage Requirements:

* Filesystem-level access to the WordPress installations to be managed

## Design Goals

#### Minimal Commandline Configuration

Instead of producing a rich command-line interface, the project is attempting to move nearly all
configuration to the file system. This avoids option overload and should make it easier to support
various levels of complexity in the configuration.
