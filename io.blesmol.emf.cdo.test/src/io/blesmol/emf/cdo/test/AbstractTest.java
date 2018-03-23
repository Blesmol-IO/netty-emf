package io.blesmol.emf.cdo.test;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import io.blesmol.testutil.ServiceHelper;

public abstract class AbstractTest {

	protected final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
	protected CdoOsgiTestUtils testUtils = new CdoOsgiTestUtils();
	protected ServiceHelper serviceHelper = new ServiceHelper();

	public void after() {
		serviceHelper.clear();
	}

}
