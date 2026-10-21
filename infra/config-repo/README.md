# Local Config Repository

The centralized-config image creates an empty `/config-repo` directory for
the local file-backed Spring Cloud Config example. Add per-application
configuration files here and rebuild the application image when demonstrating
configuration refresh and versioned configuration. The directory is
intentionally empty so the config-server example can be built without a
remote Git repository.
