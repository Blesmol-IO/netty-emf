package io.blesmol.emf.api;

public interface EmfApi {

	// Note: keep these in sync with any component property type method names!
	String NS_URI = "emf.uri";
	String SCHEME = "emf.resource.factory.scheme";
	String EXTENSION = "emf.resource.factory.extension";
	String CONTENT_TYPE = "emf.resource.factory.contenttype";

	//
	// SERVICE CONSUMER ANNOTATIONS
	//

	/**
	 * @see org.eclipse.emf.ecore.EPackage
	 */
	@interface EPackage {
		String emf_uri();
	}

	/**
	 * @see org.eclipse.emf.ecore.EPackage.Descriptor
	 */
	@interface EPackage_Descriptor {
		String emf_uri();
	}

	/**
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory
	 */
	@interface Resource_Factory {
		
		String PID = "org.eclipse.emf.ecore.resource.Resource.Factory";

		String[] emf_resource_factory_scheme();

		String[] emf_resource_factory_extension();

		String[] emf_resource_factory_contenttype();
		
		@interface Reference {
			String RESOURCE_FACTORY_REGISTRY = "emf.resourcefactory.resourcefactoryregistry";
		}
	}

	/**
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor
	 */
	@interface Resource_Factory_Descriptor {
		
		String PID = "org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor";

		String[] emf_resource_factory_scheme();

		String[] emf_resource_factory_extension();

		String[] emf_resource_factory_contenttype();
		
		@interface Reference {
			String RESOURCE_FACTORY_REGISTRY = "emf.resourcefactorydescriptor.resourcefactoryregistry";
		}
	}

	//
	// PROVIDER COMPONENT PROPERTY TYPES
	//

	/**
	 * Component property type for {@link org.eclipse.emf.ecore.resource.Resource}
	 * providers
	 */
	@interface Resource {
		String PID = "org.eclipse.emf.ecore.resource.Resource.pid";

		String emf_uri();

		@interface Reference {
			String RESOURCE_SET = "emf.resoure.resourceset";
		}
	}

	/**
	 * Component property type for
	 * {@link org.eclipse.emf.ecore.resource.ResourceSet} providers
	 */
	@interface ResourceSet {
		String PID = "org.eclipse.emf.ecore.resource.ResourceSet.pid";

		/**
		 * Recommended reference names to be used by ResourceSet providers
		 */
		@interface Reference {
			String URI_CONVERTER = "emf.resourceset.uriconverter";
			String EPACKAGE_REGISTRY = "emf.resourceset.epackageregistry";
			String RESOURCE_FACTORY_REGISTRY = "emf.resourceset.resourcefactoryregistry";
		}
	}

	/**
	 * Component property type for
	 * {@link org.eclipse.emf.ecore.resource.URIConverter} providers
	 */
	@interface URIConverter {
		String PID = "org.eclipse.emf.ecore.resource.URIConverter.pid";
	}

	/**
	 * Component property type for {@link org.eclipse.emf.ecore.EPackage.Registry}
	 * providers
	 */
	@interface EPackage_Registry {
		String PID = "org.eclipse.emf.ecore.EPackage.Registry.pid";

		/**
		 * Recommended reference names to be used by EPackage.Registry providers
		 */
		@interface Reference {
			String EPACKAGES = "emf.epackage.registry.epackages";
			String EPACKAGE_DESCRIPTORS = "emf.epackage.registry.epackagedescriptors";
		}
	}

	/**
	 * Component property type for
	 * {@link org.eclipse.emf.ecore.resource.Resource.Factory.Registry} providers
	 */
	@interface Resource_Factory_Registry {
		String PID = "org.eclipse.emf.ecore.resource.Resource.Factory.Registry.pid";

		/**
		 * Recommended reference names to be used by Resource.Factory.Registry providers
		 */
		@interface Reference {
			String RESOURCE_FACTORIES = "emf.resource.factory.registry.resourcefactories";
			String RESOURCE_FACTORY_DESCRIPTORS = "emf.resource.factory.registry.resourcefactorydescriptors";
			String URI_CONVERTER = "emf.resource.factory.registry.uriconverter";
		}
	}
}
