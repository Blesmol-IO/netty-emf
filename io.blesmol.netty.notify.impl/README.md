
## Todos

### Bytecode Modify DefaultChannelPipeline

The notifying pipeline is currently useless. Two problems:

* Most interesting methods in `DefaultChannelPipeline` are final
* `AbstractChannel` returns `DefaultChannelPipeline` via `newPipeline()`

So most, if not all, channel implementations require the use of the default channel pipeline implementation. However, that implementation isn't highly extensible.

A potential next step is to modify the bytecode of the class (also for OSGi), which either removes the final modifiers or directly inserts the notification code into the default channel pipeline implementation. This would also incur a dive into OSGi weaving.
