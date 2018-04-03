package io.blesmol.netty.notify.impl;

import java.net.InetAddress;
import java.net.SocketAddress;

import io.netty.bootstrap.ChannelFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.util.AttributeKey;

@SuppressWarnings("deprecation")
public class DelegatingServerBootstrapImpl extends ServerBootstrap {

	protected ServerBootstrap delegatingBootstrap;

	protected void activate() {
		assert delegatingBootstrap != null;
	}

	protected void deactivate() {
		delegatingBootstrap.config().group().shutdownGracefully();
		delegatingBootstrap.config().childGroup().shutdownGracefully();
	}

	@Override
	public ServerBootstrap group(EventLoopGroup group) {
		return delegatingBootstrap.group(group);
	}

	@Override
	public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup) {
		return delegatingBootstrap.group(parentGroup, childGroup);
	}

	@Override
	public <T> ServerBootstrap childOption(ChannelOption<T> childOption, T value) {
		return delegatingBootstrap.childOption(childOption, value);
	}

	@Override
	public <T> ServerBootstrap childAttr(AttributeKey<T> childKey, T value) {
		return delegatingBootstrap.childAttr(childKey, value);
	}

	@Override
	public ServerBootstrap childHandler(ChannelHandler childHandler) {
		return delegatingBootstrap.childHandler(childHandler);
	}

	@Override
	public ServerBootstrap validate() {
		return delegatingBootstrap.validate();
	}

	@Override
	public ServerBootstrap clone() {
		return delegatingBootstrap.clone();
	}

	@Override
	public EventLoopGroup childGroup() {
		return delegatingBootstrap.childGroup();
	}

	@Override
	public ServerBootstrap channel(Class<? extends ServerChannel> channelClass) {
		return delegatingBootstrap.channel(channelClass);
	}

	@Override
	public ServerBootstrap channelFactory(ChannelFactory<? extends ServerChannel> channelFactory) {
		return delegatingBootstrap.channelFactory(channelFactory);
	}

	@Override
	public ServerBootstrap channelFactory(io.netty.channel.ChannelFactory<? extends ServerChannel> channelFactory) {
		return delegatingBootstrap.channelFactory(channelFactory);
	}

	@Override
	public ServerBootstrap localAddress(SocketAddress localAddress) {
		return delegatingBootstrap.localAddress(localAddress);
	}

	@Override
	public ServerBootstrap localAddress(int inetPort) {
		return delegatingBootstrap.localAddress(inetPort);
	}

	@Override
	public ServerBootstrap localAddress(String inetHost, int inetPort) {
		return delegatingBootstrap.localAddress(inetHost, inetPort);
	}

	@Override
	public ServerBootstrap localAddress(InetAddress inetHost, int inetPort) {
		return delegatingBootstrap.localAddress(inetHost, inetPort);
	}

	@Override
	public <T> ServerBootstrap option(ChannelOption<T> option, T value) {
		return delegatingBootstrap.option(option, value);
	}

	@Override
	public <T> ServerBootstrap attr(AttributeKey<T> key, T value) {
		return delegatingBootstrap.attr(key, value);
	}

	@Override
	public ChannelFuture register() {
		return delegatingBootstrap.register();
	}

	@Override
	public ChannelFuture bind() {
		return delegatingBootstrap.bind();
	}

	@Override
	public ChannelFuture bind(int inetPort) {
		return delegatingBootstrap.bind(inetPort);
	}

	@Override
	public ChannelFuture bind(String inetHost, int inetPort) {
		return delegatingBootstrap.bind(inetHost, inetPort);
	}

	@Override
	public ChannelFuture bind(InetAddress inetHost, int inetPort) {
		return delegatingBootstrap.bind(inetHost, inetPort);
	}

	@Override
	public ChannelFuture bind(SocketAddress localAddress) {
		return delegatingBootstrap.bind(localAddress);
	}

	@Override
	public ServerBootstrap handler(ChannelHandler handler) {
		return delegatingBootstrap.handler(handler);
	}

	@Override
	public String toString() {
		return delegatingBootstrap.toString();
	}

}
