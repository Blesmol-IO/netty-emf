package io.blesmol.emf.cdo.provider;

import org.eclipse.emf.cdo.eresource.impl.CDOResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import io.blesmol.emf.api.EmfApi;

// TODO: auto-generate
@Component(configurationPid = EmfApi.Resource_Factory.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, service = Resource.Factory.class, immediate = true)
public class CdoResourceFactoryProvider extends CDOResourceFactoryImpl {

}
