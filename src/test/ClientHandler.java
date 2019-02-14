package test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.google.protobuf.SystemMsgPB;


public class ClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		//super.channelActive(ctx);
		
		for (int i = 0; i < 10; i++) {
			ctx.write(createVo(i));
		}
		ctx.flush();
	}
	
	private static SystemMsgPB.SystemMsg createVo(int i)
	{
		SystemMsgPB.SystemMsg.Builder vo = SystemMsgPB.SystemMsg.newBuilder();
		vo.setMsgCode(i);
		return vo.build();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		// TODO Auto-generated method stub
		super.channelRead(ctx, msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
}
