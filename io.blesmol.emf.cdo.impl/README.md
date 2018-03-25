
## Architecture

CDO has a concept of canonical and connection-aware URIs, where the author believes canonical is legacy in a way and connection-aware is more current. CDO view providers assume canonical URI representation.

The CDONet4JViewProvider uses connection-aware URIs for its processing. Looking at the class, a good extension point is `getContainer`. A container can provider credentials, so different clients may have differently configured containers.

However, this provider also requires the transport to be hardcoded, since there seems to be a disconnect between the CDOView and the transport used to get that view.

The view provider has access to the view via `getView(URI, ResourceSet)`. A hack would be to use the internal CDO view `.properties()` method, albeit using different values. This ties the implementation to the internal view.

A CDO view inherits `CDOCommonView.getViewID()`. So a more supported way would be to cache this ID against the transport. (I can't think of a reason why a transport would switch over but the view wouldn't; meaning two different transports should have two different view IDs; need to test.)
