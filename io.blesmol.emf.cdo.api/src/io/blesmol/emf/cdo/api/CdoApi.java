package io.blesmol.emf.cdo.api;

public interface CdoApi {

	@interface CdoServer {
		String PID = "io.blesmol.emf.cdo.api.CdoServer.pid";

		String repoName();

		boolean auditing() default true;

		boolean branching() default true;

		boolean withRanges() default false;

		@interface Reference {
			String DATA_SOURCE = "blesmol.emf.cdoserver.datasource";
			String DB_ADAPTER = "blesmol.emf.cdoserver.dbadapter";
			String MANAGED_CONTAINER = "blesmol.emf.cdoserver.managedcontainer";
			String ACCEPTOR = "blesmol.emf.cdoserver.acceptor";
		}
	}

	@interface CdoViewProvider {
		String PID = "io.blesmol.emf.cdo.api.CdoViewProvider.pid";

		String regex() default "cdo:.*";

		/**
		 * @see org.eclipse.emf.cdo.view.CDOViewProvider.DEFAULT_PRIORITY
		 */
		int priority() default 500;

		@interface Reference {
			String CONNECTOR = "emf.cdo.viewprovider.connector";
		}
	}

	@interface IConnector {
		String PID = "org.eclipse.net4j.connector.IConnector.pid";

		String description();

		String productGroup() default "org.eclipse.net4j.connectors";

		// jvm, tcp, ssl
		String type() default "jvm";

		@interface Reference {
			String MANAGED_CONTAINER = "emf.cdo.iconnector.managedcontainer";
		}
	}

	@interface IManagedContainer {
		String PID = "org.eclipse.net4j.util.container.IManagedContainer.pid";

		// jvm, tcp, ssl
		String type() default "jvm";
	}

	@interface IAcceptor {
		String PID = "org.eclipse.net4j.acceptor.IAcceptor.pid";

		String description();

		// jvm, tcp, ssl
		String type() default "jvm";

		@interface Reference {
			String MANAGED_CONTAINER = "emf.cdo.iacceptor.managedcontainer";
		}
	}
	
	@interface H2Adapter {
		String PID = "org.eclipse.net4j.db.h2.H2Adapter.pid";
	}
}
