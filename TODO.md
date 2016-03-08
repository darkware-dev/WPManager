# Foundation Features

## Email support

WPManager should have a facility for sending emails.

## Blog Post support

WPManager should have the ability to create new blog posts.

## Log monitoring support

WPManager should have the ability to monitor logs (possibly via FIFO or UDP)

## REST API

WPManager should provide a REST API for exposing various methods using Spark:
http://sparkjava.com/documentation.html

# Runtime Features

## Plugin List Support

WPManager should support a list of plugins which are routinely checked to ensure that they
are installed. Updates may still be done separately, but this may be rolled into a single
plugin agent.

## Theme List Support

Much like plugins, WPManager should maintain a list of themes that are kept installed.

## User Auditing

WPManager should include some user auditing procedures.

+ Check for users that belong to no blog, and optionally remove them
+ Check for users with prohibited names
+ Check for new superadmins (and notify)
+ Check for new admins (and notify)

# Security

## Hash Library

WPManager should scan various important directories, calculating hashes for files and 
sending alerts when hashes don't match. This needs to include purging and regenerating
hashes when plugins, themes, and core files are updated.
