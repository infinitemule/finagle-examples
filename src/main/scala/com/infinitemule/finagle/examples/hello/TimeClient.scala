/*
 * Finagle Examples
 */
package com.infinitemule.finagle.examples.hello

import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.{
  Http,
  RequestBuilder
}
import com.twitter.finagle.Service
import com.twitter.util.Future
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.handler.codec.http.{
  HttpRequest, 
  HttpResponse
}
import org.jboss.netty.handler.codec.http.{
  DefaultHttpRequest,
  HttpVersion  
}
import org.jboss.netty.util.CharsetUtil.UTF_8

import java.net.InetSocketAddress

/**
 * Simply makes a request to the time server
 */
object TimeClient extends App {
  
  val client = new TimeClient(8765)
    
  client.requestTime()
  
  // Since the call is asynchronous and returns
  // immediately, we'll introduce a small delay so that
  // that the program doesn't terminate before writing to
  // the console
  Thread.sleep(2000)
  
  // This isn't mentioned in the documentation, but if you don't call
  // close on a client, it causes this demo to hang.
  client.close()
}


/**
 * TimeClient will build the request for us, handle
 * the response asynchronously, and display the 
 * time to the console
 */
class TimeClient(port: Int) {
  
  private val client: Service[HttpRequest, HttpResponse] = 
    ClientBuilder()
      .codec(Http())
      .hosts(new InetSocketAddress(port))
      .hostConnectionLimit(1)
      .build()

  private val req = RequestBuilder().url("http://localhost:" + port).buildGet    
  
  def requestTime() = {
       
    client(req)
      .onSuccess { response => 
        println("Time: " + response.getContent().toString(UTF_8)) 
      }
      .onFailure { ex =>       
        println("Error requesting time: " + ex.getMessage())
      }
      
  }
      
  
  def close() = client.close()
  
}