
## TODOs / Changes

### `setTarget` / `unsetTarget`:

These could be used, at least `setTarget` to create a new abstract bootstrap. It makes the state a bit more complicated. As for `unsetTarget` only see this being useful if the model contained some `isAdapted` element, which doesn't seem useful currently. For the persisted models, we don't want to remove the model from a resource because the adapter has been unset. But the model should reflect when the server has been started / stopped.
