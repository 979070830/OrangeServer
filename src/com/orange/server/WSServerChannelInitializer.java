package com.orange.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.ssl.SslContext;

import com.google.protobuf.SystemMsgPB;

public class WSServerChannelInitializer extends ChannelInitializer<SocketChannel>{

	private final int maxContentLength = 65536;//64 * 1024;
	
	private SslContext sslCtx;
	
	public WSServerChannelInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		
		ChannelPipeline p = ch.pipeline();
		
		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc()));
		}
		
		p.addLast(new HttpServerCodec());
		p.addLast(new HttpObjectAggregator(maxContentLength));
		
		p.addLast(new ProtobufDecoder(SystemMsgPB.SystemMsg.getDefaultInstance()));
		p.addLast(new ProtobufEncoder());
		
		p.addLast(new WSChannelInboundHandlerAdapter());
		//p.addLast(new WebSocketServerProtocolHandler("/ws"));
		
	}

}
