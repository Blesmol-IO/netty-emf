package io.blesmol.emf.cdo.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.net4j.CDONet4jSession;
import org.eclipse.emf.cdo.net4j.CDONet4jViewProvider;
import org.eclipse.emf.cdo.util.CDOURIData;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.cdo.view.CDOViewProviderRegistry;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.channel.IChannel;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.util.container.IManagedContainer;

import io.blesmol.emf.cdo.api.CdoViewProvider;

/**
 * 
 * Overrides getView(CDOView, String) and getResourceURI(CDOView, String)
 * because they use the private super member <code>transport</code>. Rather,
 * obtain this value via the map of view IDs to transports or the URI directly.
 */
public class CdoViewProviderImpl extends CDONet4jViewProvider implements CdoViewProvider {

	protected static final Pattern transportPattern = Pattern.compile("(?:cdo\\.net4j\\.)(jvm|tcp|ssl)");

	private static final String DUMMY_REGEX = "";
	private static final int DUMMY_PRIORITY = 0;
	
	/*
	 * Regex and priorty are assumed to be set by subclass or test, so use dummy
	 * values here.
	 */
	public CdoViewProviderImpl() {
		super(DUMMY_REGEX, DUMMY_PRIORITY);
	}

	protected final Map<Integer, String> viewToTransport = new ConcurrentHashMap<>();

	/**
	 * A prepared, active container
	 */
	protected IManagedContainer container;

	/**
	 * Expected values must be set prior to calling activate
	 */
	protected void activate() {
		assert container != null;
		assert !getRegex().equals(DUMMY_REGEX);
		assert getPriority() != DUMMY_PRIORITY;
		// Register to static instance. CDO impl code also calls this directly :-/
		CDOViewProviderRegistry.INSTANCE.addViewProvider(this);
	}

	// We do not deactivate the container since other consumers might be using it.
	protected void deactivate() {
		CDOViewProviderRegistry.INSTANCE.removeViewProvider(this);
	}

	protected String getTransport(String scheme) {
		final Matcher matcher = transportPattern.matcher(scheme);
		matcher.matches();
		return matcher.group(1);
	}

	protected String getTransport(CDOView view) {
		final String transport = viewToTransport.get(view.getViewID());
		assert transport != null;
		return transport;
	}

	protected IConnector getConnector(String authority, String transport) {
		IManagedContainer container = getContainer();
		String description = getConnectorDescription(authority);
		return Net4jUtil.getConnector(container, transport, description);
	}

	@Override
	protected IManagedContainer getContainer() {
		return container;
	}

	// FIXME: Copy-pasta of super class, CDO copyright applicable to this code
	@Override
	public URI getResourceURI(CDOView view, String path) {

		final String transport = viewToTransport.get(view.getSession().getSessionID());
		assert transport != null;

		StringBuilder builder = new StringBuilder();
		builder.append("cdo.net4j.");
		builder.append(transport);
		builder.append("://");

		CDONet4jSession session = (CDONet4jSession) view.getSession();

		IChannel channel = session.options().getNet4jProtocol().getChannel();
		if (channel == null) {
			return null;
		}
		IConnector connector = (IConnector) channel.getMultiplexer();

		// append
		String repositoryName = session.getRepositoryInfo().getName();
		String authority = getURIAuthority(connector);
		builder.append(authority);

		builder.append("/");
		builder.append(repositoryName);

		if (path != null) {
			if (!path.startsWith("/")) {
				builder.append("/");
			}

			builder.append(path);
		}

		int params = 0;

		String branchPath = view.getBranch().getPathName();
		if (!CDOBranch.MAIN_BRANCH_NAME.equalsIgnoreCase(branchPath)) {
			builder.append(params++ == 0 ? "?" : "&");
			builder.append(CDOURIData.BRANCH_PARAMETER);
			builder.append("=");
			builder.append(branchPath);
		}

		long timeStamp = view.getTimeStamp();
		if (timeStamp != CDOBranchPoint.UNSPECIFIED_DATE) {
			builder.append(params++ == 0 ? "?" : "&");
			builder.append(CDOURIData.TIME_PARAMETER);
			builder.append("=");
			builder.append(new SimpleDateFormat().format(new Date(timeStamp)));
		}

		if (!view.isReadOnly()) {
			builder.append(params++ == 0 ? "?" : "&");
			builder.append(CDOURIData.TRANSACTIONAL_PARAMETER);
			builder.append("=true");
		}

		return URI.createURI(builder.toString());
	}

	// FIXME: Copy-pasta of super class, CDO copyright applicable to this code
	@Override
	public CDOView getView(URI uri, ResourceSet resourceSet) {
		final CDOURIData data = new CDOURIData(uri);
		final String transport = getTransport(data.getScheme());

		IConnector connector = getConnector(data.getAuthority(), transport);
		CDONet4jSession session = getNet4jSession(connector, data.getUserName(), data.getPassWord(),
				data.getRepositoryName());

		viewToTransport.putIfAbsent(session.getSessionID(), transport);

		String viewID = data.getViewID();
		if (viewID != null) {
			if (data.isTransactional()) {
				return session.openTransaction(viewID, resourceSet);
			}
			return session.openView(viewID, resourceSet);
		}

		String branchPath = data.getBranchPath().toPortableString();
		CDOBranch branch = session.getBranchManager().getBranch(branchPath);
		long timeStamp = data.getTimeStamp();

		if (data.isTransactional()) {
			return session.openTransaction(branch, resourceSet);
		} else {
			return session.openView(branch, timeStamp, resourceSet);
		}

	}

}
