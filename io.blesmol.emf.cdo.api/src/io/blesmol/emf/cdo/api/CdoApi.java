package io.blesmol.emf.cdo.api;

public interface CdoApi {

	@interface CdoServer {
		String PID = "io.blesmol.emf.cdo.api.CdoServer.pid";

		String repoName();

		boolean auditing() default true;

		boolean branching() default true;

		boolean withRanges() default false;

		@interface Reference {
			String DATA_SOURCE = "dataSource";
			String DB_ADAPTER = "dbAdapter";
			String MANAGED_CONTAINER = "container";
			String ACCEPTOR = "acceptor";
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
			String CONNECTOR = "connector";
		}
	}
}
