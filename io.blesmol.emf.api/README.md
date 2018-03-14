
# io.blesmol.emf.api

## EmfApi

Consists of an API implemented via annotation types.

### Services Consumed by Providers

Given the following:

```java
	/**
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory
	 */
	@interface Resource_Factory {
		String emf_resource_factory_scheme();

		String emf_resource_factory_extension();
		
		String emf_resource_factory_contenttype();
	}
```

Using the OSGi `_` &rarr; `.` convention, the above annotation type `Resource_Factory` conveys there are three properties consumers of the service `org.eclipse.emf.ecore.resource.Resource.Factory` expect:

* A property name `emf.resource.factory.scheme` with the type returned by `emf_resource_factory_scheme()`: `String`.
* A property name  `emf.resource.factory.extension` of type `String`
* A property name  `emf.resource.factory.contenttype` of type `String`

For convenience, these property names are also specified as constants:

```java
	String SCHEME = "emf.resource.factory.scheme";
	String EXTENSION = "emf.resource.factory.extension";
	String CONTENT_TYPE = "emf.resource.factory.contenttype";
```

### Provider Implementing the API

The API conveys the following for Declarative Service providers:

* Service configuration property names and return type and to use by providers, see above section.
* Reference names via nested annotation types matching `@interface Reference` expected to be used by a provider via its `@Reference(name = ...)` annotation. When utilized, bundles creating a service factory configuration can also use these names as targets, which would be included in the properties provided to Configuration Admin.
* Service factory PID used by the provider via its `@Component(configurationPid=...)` annotation.

Any provider-focused annotation type that has one or more methods is called a "component property type" (OSGi R6 Compendium §112.8.2). Providers are encouraged to use these component property types via their `@Activate`, `@Modified`, and/or `@Deactivate` methods, like so:

```java
	@Activate
	void activate(EmfApi.URIConverter config) {
	  // ...
	}
```

 