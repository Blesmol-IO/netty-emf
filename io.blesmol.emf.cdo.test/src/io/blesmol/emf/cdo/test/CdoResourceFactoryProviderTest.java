package io.blesmol.emf.cdo.test;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;

import io.blesmol.emf.api.EmfApi;

@RunWith(MockitoJUnitRunner.class)
public class CdoResourceFactoryProviderTest extends AbstractTest {

	@After
	@Override
	public void after() {
		super.after();
	}

	@Test
	public void cdoShouldBeRegisteredInEmfResourceFactoryRegistry() throws Exception {
		Map<String, Object> properties = new HashMap<>();
		properties.put(EmfApi.SCHEME, new String[]{"cdo.net4j.jvm"});

		Configuration resourceFactoryConfig = configureForResourceFactory(properties);
		String resourceFactoryFilter = String.format("(%s=%s)", Constants.SERVICE_PID, resourceFactoryConfig.getPid());
		Factory.Registry resourceFactoryRegistry = serviceHelper.getService(context, Factory.Registry.class,
				Optional.of(resourceFactoryFilter), 1000);
		assertNotNull(resourceFactoryRegistry);

		Object cdoFactory = resourceFactoryRegistry.getProtocolToFactoryMap().get("cdo.net4j.jvm");
		assertNotNull(cdoFactory);

	}

}
