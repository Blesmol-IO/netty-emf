package io.blesmol.emf.cdo.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Test that a CDO view provider can 
 */
@RunWith(MockitoJUnitRunner.class)
public class CdoRoundTripITest {

	private final BundleContext context = FrameworkUtil.getBundle(CdoRoundTripITest.class).getBundleContext();
	
	@Before
	public void before() {
		// TODO add test setup here
	}

	@After
	public void after() {
		// TODO add test clear up here
	}

	/*
	 * Create delegated container
	 * Create delegated acceptor
	 */
	@Test
	public void testExample() {
		// TODO implement a test here
	}

}