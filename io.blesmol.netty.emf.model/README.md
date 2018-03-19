# Netty EMF Models

Netty-related EMF Xcore models 

## Todos

### Resolving error annotations in Xcore files

Speculation: since Xcore projects aren't PDE projects, their `plugin.xml` files aren't processed the same way. Because of this, EMF / Xtext registries aren't populated the same way. The Xtext / Xcore UI could be somehow utilizing these registries when resolving entries.

Conversely, move to Tycho / PDE build...
