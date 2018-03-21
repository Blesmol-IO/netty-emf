package io.blesmol.emf.cdo.api;

public interface CdoApi {

	@interface CdoServer {
		String PID = "io.blesmol.emf.cdo.api.CdoServer.pid";

		String blesmol_cdoserver_reponame();

		boolean blesmol_cdoserver_auditing() default true;

		boolean blesmol_cdoserver_branching() default true;

		boolean blesmol_cdoserver_withranges() default false;

		@interface Reference {
			String DATA_SOURCE = "blesmol.emf.cdoserver.datasource";
			String DB_ADAPTER = "blesmol.emf.cdoserver.dbadapter";
			String MANAGED_CONTAINER = "blesmol.emf.cdoserver.managedcontainer";
			String ACCEPTOR = "blesmol.emf.cdoserver.acceptor";
		}
	}

	@interface CdoViewProvider {
		String PID = "io.blesmol.emf.cdo.api.CdoViewProvider.pid";

		String blesmol_cdoviewprovider_regex() default "cdo:.*";

		/**
		 * @see org.eclipse.emf.cdo.view.CDOViewProvider.DEFAULT_PRIORITY
		 */
		int blesmol_cdoviewprovider_priority() default 500;

		@interface Reference {
			String CONNECTOR = "emf.cdo.viewprovider.connector";
		}
	}

	@interface IConnector {
		String PID = "org.eclipse.net4j.connector.IConnector.pid";

		String emf_cdo_connector_description();

		String emf_cdo_connector_productgroup() default "org.eclipse.net4j.connectors";

		// jvm, tcp, ssl
		String emf_cdo_connector_type() default "jvm";

		@interface Reference {
			String MANAGED_CONTAINER = "emf.cdo.iconnector.managedcontainer";
		}
	}

	@interface IManagedContainer {
		String PID = "org.eclipse.net4j.util.container.IManagedContainer.pid";

		// jvm, tcp, ssl
		String emf_cdo_managedcontainer_type() default "jvm";
	}

	@interface IAcceptor {
		String PID = "org.eclipse.net4j.acceptor.IAcceptor.pid";

		String emf_cdo_acceptor_description();

		// jvm, tcp, ssl
		String emf_cdo_acceptor_type() default "jvm";

		@interface Reference {
			String MANAGED_CONTAINER = "emf.cdo.acceptor.managedcontainer";
		}
	}
	
	@interface H2Adapter {
		String PID = "org.eclipse.net4j.db.h2.H2Adapter.pid";
	}
}
