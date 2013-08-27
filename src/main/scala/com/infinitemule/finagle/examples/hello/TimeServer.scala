package com.infinitemule.finagle.examples.hello


import com.twitter.finagle.Service
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.http.Http
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{
  HttpRequest, 
  HttpResponse
}
import org.jboss.netty.handler.codec.http.{
  DefaultHttpResponse,
  HttpResponseStatus,
  HttpVersion  
}
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8
import java.util.Date
import java.net.InetSocketAddress
import java.text.SimpleDateFormat


object TimeServer extends App {

  val server: Server = ServerBuilder()
      .codec(Http())
      .bindTo(new InetSocketAddress(8765))
      .name("timeServer")
      .build(new TimeService())
}


class TimeService extends Service[HttpRequest, HttpResponse] {
  
  def apply(req: HttpRequest): Future[HttpResponse] = { 
    
    Future.value(response(HttpResponseStatus.OK, dateString()))
  }
       
  private def response(status:  HttpResponseStatus, 
                       content: String) = {
    
    val resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, 
                                       status)    
    
    resp.setContent(copiedBuffer(content, UTF_8))    
    resp
  }
  
  
  private def dateString() = 
    new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z").format(new Date())

}