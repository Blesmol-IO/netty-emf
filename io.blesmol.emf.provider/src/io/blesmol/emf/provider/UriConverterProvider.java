package io.blesmol.emf.provider;

import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import io.blesmol.emf.api.EmfApi;

@Component(configurationPid = EmfApi.URIConverter.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = URIConverter.class)
public class UriConverterProvider extends ExtensibleURIConverterImpl {

}
