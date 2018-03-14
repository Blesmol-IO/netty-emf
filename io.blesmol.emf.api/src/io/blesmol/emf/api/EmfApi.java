package io.blesmol.emf.api;

public interface EmfApi {

	// Note: keep these in sync with the annotation type method names!
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
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory 
	 */
	@interface EPackage_Descriptor {
		String emf_uri();
	}

	/**
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory
	 */
	@interface Resource_Factory {
		String emf_resource_factory_scheme();

		String emf_resource_factory_extension();
		
		String emf_resource_factory_contenttype();
	}
	
	/**
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor 
	 */
	@interface Resource_Factory_Descriptor {
		
		String emf_resource_factory_scheme();

		String emf_resource_factory_extension();
		
		String emf_resource_factory_contenttype();
	}

	
	//
	// PROVIDER COMPONENT PROPERTY TYPES
	//
	
	/**
	 * Component property type for ResourceSet providers
	 * 
	 * @see org.eclipse.emf.ecore.resource.ResourceSet
	 * 
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
	 * Component property type for URIConverter providers
	 * 
	 * @see org.eclipse.emf.ecore.resource.ResourceSet
	 * 
	 */
	@interface URIConverter {
		String PID = "org.eclipse.emf.ecore.resource.URIConverter.pid";
	}

	/**
	 * Component property type for EPackage.Registry providers
	 * 
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * 
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
	 * Component property type for Resource.Factory.Registry providers
	 * 
	 * @see org.eclipse.emf.ecore.resource.Resource.Factory.Registry
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
