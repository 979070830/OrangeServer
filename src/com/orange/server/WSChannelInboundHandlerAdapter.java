package com.orange.server;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AsciiString;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.SystemMsgPB;
import com.orange.core.ISFSEventParam;
import com.orange.core.SFSEvent;
import com.orange.core.SFSEventParam;
import com.orange.core.SFSEventType;
import com.orange.entities.User;
import com.orange.entities.Zone;

public class WSChannelInboundHandlerAdapter extends SimpleChannelInboundHandler<Object> {
	
	private WebSocketServerHandshaker handshaker;
	private OrangeServerEngine sfs;

	public WSChannelInboundHandlerAdapter() {
		super();
		this.sfs = OrangeServerEngine.getInstance();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);

		//添加到全局
		//GlobalChannel.group.add(ctx.channel());
		
		
		
		System.out.println("客户端与服务端连接开启：" + ctx.channel().remoteAddress().toString());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		
		//从全局中移除
		//GlobalChannel.group.remove(ctx.channel());
		
		
		User user = this.sfs.getSessionManager().getUser(ctx);
		if(user != null) user.getZone().getUserManager().removeUser(user);
		//user.getZone().getUserManager().getOwnerRoom().removeUser(user);
		//user.getZone().removeUser(user);
		
		System.out.println("客户端与服务端连接关闭：" + ctx.channel().remoteAddress().toString());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object frame) throws Exception {
		if (frame instanceof FullHttpRequest) {//如果是HTTP请求，进行HTTP操作
			processHttpRequest(ctx, (FullHttpRequest) frame);
		}
		else if (frame instanceof BinaryWebSocketFrame) {//如果是Websocket请求，则进行websocket操作
			processBinaryWebSocketRequest(ctx, (BinaryWebSocketFrame) frame);
		}
		else if(frame instanceof TextWebSocketFrame)
		{
			processTextWebSocketRequest(ctx, (TextWebSocketFrame) frame);
		}
		else if (frame instanceof CloseWebSocketFrame) // 判断是否关闭链路的指令
		{
			System.out.println("CloseWebSocketFrame");
			handshaker.close(ctx.channel(), ((CloseWebSocketFrame) frame).retain());
		}
		else if (frame instanceof PingWebSocketFrame)// 判断是否ping消息
		{
			System.out.println("PingWebSocketFrame");
			ctx.channel().write(new PongWebSocketFrame(((PingWebSocketFrame) frame).content().retain()));
		}
	}

	protected void processHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
		if (!HttpMethod.GET.equals(request.getMethod()) || !"websocket".equalsIgnoreCase(request.headers().get("Upgrade"))) {//如果不是WS握手的HTTP请求，将进行处理

			//			DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
			//			ctx.channel().write(resp);
			//			ctx.channel().close();

			handleHttpRequest(ctx, request);
		}
		else
		{
			String uri=request.uri();
			System.out.println("ws url:"+uri);
			
			String version = request.headers().get(Names.SEC_WEBSOCKET_VERSION);
			
			WebSocketHandshakerFactory wsShakerFactory = new WebSocketHandshakerFactory("ws://" + request.headers().get(HttpHeaders.Names.HOST), null, false);
			handshaker = wsShakerFactory.newHandshaker(request);
			if (handshaker == null) {
				WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
			} else {
				handshaker.handshake(ctx.channel(), request);
			}
			
			
			//加入区
			String zoneName = request.uri().substring(1);
			Zone zone = OrangeServerEngine.getInstance().getZoneManager().getZoneByName(zoneName);
			System.out.println(zoneName+zone);
			
			User user = new User(ctx);
			zone.getUserManager().addUser(user);//加入主区
			//zone.getUserManager().getOwnerRoom().addUser(user);//加入主房间
			
		      Map<ISFSEventParam, Object> evtParams = new HashMap();
		      evtParams.put(SFSEventParam.ZONE, zone);
		      evtParams.put(SFSEventParam.USER, user);
		      
		      this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.USER_JOIN_ZONE, evtParams));
				
				

		}
	}

	private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };
	private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
	private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
	private static final AsciiString CONNECTION = AsciiString.cached("Connection");
	private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");
	//处理HTTP的代码
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
		HttpMethod method=req.method();
		String uri=req.uri();

		System.out.println(uri);

		if(uri == "/favicon.ico")
		{
			DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
			ctx.channel().write(resp);
			ctx.channel().close();
			return;
		}
		else
		{
			boolean keepAlive = HttpUtil.isKeepAlive(req);
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(CONTENT));
			response.headers().set(CONTENT_TYPE, "text/plain");
			response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

			if (!keepAlive) {
				ctx.write(response).addListener(ChannelFutureListener.CLOSE);
			} else {
				response.headers().set(CONNECTION, KEEP_ALIVE);
				ctx.write(response);
			}
			ctx.flush();
			ctx.close();

			if(method==HttpMethod.GET&&"/login".equals(uri))
			{
				//....处理 
			}else if(method==HttpMethod.POST&&"/register".equals(uri)){
				//...处理
			}
		}

	}
	
	private static Message parseFrom(Class<?> messageClass,SystemMsgPB.SystemMsgByteArray systemMsgByteArray) throws InvalidProtocolBufferException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Method method = messageClass.getMethod("parser");
		com.google.protobuf.Parser<Message> parser = (com.google.protobuf.Parser<Message>) method.invoke(null,null);
		return parser.parseFrom(systemMsgByteArray.getBytes());
	}

	protected void processBinaryWebSocketRequest(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		ByteBuf copy = frame.content().copy();

		byte[] data = new byte[copy.capacity()];
		copy.readBytes(data);
 
		try {
			SystemMsgPB.SystemMsg resultVO = decode(data);
			
			System.out.println("SystemMsg:"+resultVO);
			System.out.println(SystemMsgPB.SystemMsg.class.getPackage().getName());
			Class msgClass = AllMessageClassUtil.getClass(resultVO.getClassName());//TestMsgPB.TestMsg.class
			
			SystemMsgPB.SystemMsgByteArray msgByteArray;
			Message message;
			
			if(resultVO.getIsMsgArray())
			{
				List<Message> msgs = new ArrayList<Message>();
			    
				int len = resultVO.getMsgBytesCount();
				for (int i = 0; i < len; i++) {
					
					msgByteArray = resultVO.getMsgBytes(i);
					message = parseFrom(msgClass,msgByteArray);
					msgs.add(message);
				}
				
				System.out.println("接受到(SFSArray):"+msgs);
			}
			else
			{
				msgByteArray = resultVO.getMsgBytes(0);
				message = parseFrom(msgClass,msgByteArray);
				
				System.out.println("接受到(SFSObject):"+message);
			}
			
			System.out.println("code:"+resultVO.getMsgCode());
//			if(resultVO.getIsMsgArray())
//			{
//				System.out.println(msgs.get(0));
//			}
//			else
//			{
//				System.out.println(msgs.get(0));
//			}
				
			
				
				
//				System.out.println("code:"+resultVO.getMsgCode());
//				System.out.println(msgs);
//				
//				TestMsgPB.TestMsg test = TestMsgPB.TestMsg.parseFrom(resultVO.getMsgBytes(0));
//				System.out.println(resultVO);
//				System.out.println(test);
//				System.out.println("结束");
			
			
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		StringBuilder a = new StringBuilder();
//		for (int i = 0; i < data.length; i++) {
//			int v = data[i] & 0xFF;
//			a.append(Integer.toHexString(v));
//		}
//		
//		
//		
//		TestReq.TestVO vo1 = createVO();
//		System.out.println("之前"+vo1.toString());
//		try {
//			
//			TestReq.TestVO vo2 = decode(encode(vo1));
//			System.out.println("之后"+vo2.toString());
//			
//			System.out.println("是否相等:"+vo1.equals(vo2));
//			
//		} catch (InvalidProtocolBufferException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
	}
	
	
	
	private static byte[] encode(SystemMsgPB.SystemMsg vo)
	{
		return vo.toByteArray();
	}
	
	private static SystemMsgPB.SystemMsg decode(byte[] body) throws InvalidProtocolBufferException
	{
		return SystemMsgPB.SystemMsg.parseFrom(body);
	}
	
	private SystemMsgPB.SystemMsg createVO()
	{
		SystemMsgPB.SystemMsg.Builder builder = SystemMsgPB.SystemMsg.newBuilder();
		
		SystemMsgPB.SystemMsg vo = builder.build();
		return vo;
	}

	private void processTextWebSocketRequest(ChannelHandlerContext ctx, TextWebSocketFrame frame) 
	{
		// 返回应答消息
		String request = ((TextWebSocketFrame) frame).text();
		System.out.println("服务端收到：" + request);

		TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString() + ctx.channel().id() + "：" + request);
		// 群发
		//GlobalChannel.group.writeAndFlush(tws);
		// 返回【谁发的发给谁】
		// ctx.channel().writeAndFlush(tws);
	}


//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg){
//		try {
//			super.channelRead(ctx, msg);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try
//		{
//			TestVO vo = (TestVO) msg;
//			System.out.println(vo);
//			
//		}
//		catch(Exception e)
//		{
//			System.out.println(e);
//		}
//
//	}
	
//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg) {
//
//	System.out.println("ctx:"+ctx);
//		System.out.println("channelRead:"+msg);
//		//静默丢弃接收到的数据
//		//((ByteBuf) msg).release();
//
//		//    	ByteBuf in = (ByteBuf) msg;
//		//        try {
//		//            while (in.isReadable()) {
//		//                System.out.print((char) in.readByte());
//		//                System.out.flush();
//		//            }
//		//        } finally {
//		//            ReferenceCountUtil.release(msg);
//		//        }
//
////		ByteBuf in = (ByteBuf) msg;
////		String rect = in.toString(io.netty.util.CharsetUtil.US_ASCII);
////		System.out.println("接收:"+rect);
//		
//		String sss = msg.getClass().getName();
//		System.out.println("sss="+sss);
//		if(sss == "io.netty.handler.codec.http.HttpObjectAggregator$AggregatedFullHttpRequest")
//		{
//			try {
//				super.channelRead(ctx, msg);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		else
//		{
////			ctx.flush();
////	    	String send = "返回:";//+rect;
////	    	//final ByteBuf time = ctx.alloc().buffer(4); // (2)
////	    	final ByteBuf time = Unpooled.buffer(4);
////	    	//time.writeInt(11111);
////	        //time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
////	        time.writeChar("s".toCharArray()[0]);
////	        time.writeChar("s".toCharArray()[0]);
////	        time.writeChar("s".toCharArray()[0]);
////	        time.writeChar("s".toCharArray()[0]);
////	    	//ctx.write(msg);
////	        //ctx.flush();
////	    	ctx.writeAndFlush(time);
//		}
//	}
//
//	@Override
//	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//		super.channelReadComplete(ctx);
//	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);

		//引发异常时关闭连接
		cause.printStackTrace();
		ctx.close();
	}
}
