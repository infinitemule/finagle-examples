/*
 * Finagle Examples
 */
package com.infinitemule.finagle.examples.hello


import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.http.Http
import com.twitter.finagle.Service

import com.twitter.util.Future

import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.handler.codec.http.{
  HttpRequest, 
  HttpResponse
}
import org.jboss.netty.handler.codec.http.{
  DefaultHttpResponse,
  HttpResponseStatus,
  HttpVersion  
}
import org.jboss.netty.util.CharsetUtil.UTF_8

import java.net.InetSocketAddress
import java.text.SimpleDateFormat
import java.util.Date

/**
 * TimeServer will take our service, bind it
 * to a port and serve until it's terminated.
 */
object TimeServer extends App {

  val server: Server = ServerBuilder()
      .codec(Http())
      .bindTo(new InetSocketAddress(8765))
      .name("timeServer")
      .build(new TimeService())
}


/**
 * TimeService will handle all our requests by
 * responding with a timestamp of the current system time.
 */
class TimeService extends Service[HttpRequest, HttpResponse] {
  
  /**
   * Creates a time stamp and sends it back to the client.
   */
  def apply(req: HttpRequest): Future[HttpResponse] = {     
    Future.value(response(HttpResponseStatus.OK, dateString()))
  }
  
  
  /**
   * Creates the DefaultHttpResponse using the specified
   * status and content.  Since the response needs
   * a ChannelBuffer for the content, we must
   * convert our String using the copiedBuffer method.
   */
  private def response(status:  HttpResponseStatus, 
                       content: String) = {
    
    val resp = 
      new DefaultHttpResponse(HttpVersion.HTTP_1_1, status)    
    
    resp.setContent(copiedBuffer(content, UTF_8)) 
    resp
  }
  
  
  /**
   * Creates a timestamp of the current system date
   */
  private def dateString() = 
    new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z").format(new Date())

}