package io.blesmol.testutil;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

// FIXME: move to generic io.blesmol.test project
public class ServiceHelper {

	private final Queue<ServiceTracker<?, ?>> serviceTrackers = new ConcurrentLinkedQueue<>();
	private final Queue<ServiceRegistration<?>> serviceRegistrations = new ConcurrentLinkedQueue<>();
	private final Queue<Configuration> configurations = new ConcurrentLinkedQueue<>();
	private volatile ConfigurationAdmin configAdmin;

	public <T> ServiceTracker<T, T> getTracker(BundleContext context, Class<T> clazz, Optional<String> filter)
			throws Exception {
		ServiceTracker<T, T> st;
		if (filter.isPresent()) {
			st = new ServiceTracker<>(context, context.createFilter(filter.get()), null);
		} else {
			st = new ServiceTracker<>(context, clazz, null);
		}
		st.open();
		serviceTrackers.add(st);
		return st;
	}

	public <T> T getService(BundleContext context, Class<T> clazz, Optional<String> filter, long timeout)
			throws Exception {
		return getTracker(context, clazz, filter).waitForService(timeout);
	}

	public void clear() {
		serviceTrackers.stream().forEach(ServiceTracker::close);
		serviceTrackers.clear();

		serviceRegistrations.stream().forEach(ServiceRegistration::unregister);
		serviceRegistrations.clear();

		configurations.stream().forEach(t -> {
			try {
				t.delete();
			} catch (IOException e) {
				System.err.println(e);
			}
		});
	}

	public <S> void registerService(BundleContext context, Class<S> serviceClass, S service,
			Map<String, Object> properties) {
		serviceRegistrations.add(context.registerService(serviceClass, service, new Hashtable<>(properties)));
	}

	public Configuration createFactoryConfiguration(BundleContext context, Optional<String> filter, String pid,
			Map<String, Object> properties) throws Exception {
		ConfigurationAdmin configAdmin = this.configAdmin;
		if (configAdmin == null) {
			this.configAdmin = getService(context, ConfigurationAdmin.class, filter, 100);
			configAdmin = this.configAdmin;
		}
		final Configuration result = configAdmin.createFactoryConfiguration(pid, "?");
		configurations.add(result);
		result.update(new Hashtable<>(properties));
		return result;

	}
}
