/*
 * Finagle Examples - ZipCode
 */
package com.infinitemule.finagle.examples.zipcode

import com.twitter.finagle.builder.{ClientBuilder, 								                                     
                                    Server, 
                                    ServerBuilder}

import com.twitter.finagle.http.{Http,
                                 RequestBuilder}

import com.twitter.finagle.Service
import com.twitter.util.Future

import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer

import org.jboss.netty.handler.codec.http.{DefaultHttpRequest,
										   DefaultHttpResponse,
									       HttpMethod,
										   HttpRequest, 
  									       HttpResponse,
  									       HttpResponseStatus,
  									       HttpVersion}

import org.jboss.netty.util.CharsetUtil.UTF_8

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit._

import com.twitter.util.Duration


/**
 * 
 */
object ZipCodeServer extends App {
  
  val server: Server = ServerBuilder()
      .codec(Http())
      .bindTo(new InetSocketAddress(8765))
      .name("zipCodeServer")
      .build(new ZipCodeService())
}

class ZipCodeService extends Service[HttpRequest, HttpResponse] {

  val ziptasticHost = "ziptasticapi.com"
  
  private val ziptasticClient: Service[HttpRequest, HttpResponse] = 
    ClientBuilder()
      .codec(Http())
      .hosts(new InetSocketAddress(ziptasticHost, 80))
      .hostConnectionLimit(1)
      .tcpConnectTimeout(Duration(3, SECONDS))
      .build()
        

  def apply(req: HttpRequest): Future[HttpResponse] = {     
    
    val zipCode = req.getUri().split("/").last
    
    callZiptastic(zipCode)
      .onSuccess { resp =>
        Future.value(responseOk(resp.getContent().toString(UTF_8)))
      }
      .onFailure { ex =>         
        Future.value(responseError(ex.getMessage()))
      }
  }
    
  
  private def callZiptastic(zipCode: String): Future[HttpResponse] = {
    
    ziptasticClient(ziptasticRequest(zipCode))
      .onSuccess { resp =>        
        Future.value(resp)
      }
      .onFailure { ex =>
        Future.exception(ex)
      }      
  }
  
  
  private def ziptasticRequest(zipCode: String) = {
    //val req = new DefaultHttpRequest(
    //  HttpVersion.HTTP_1_1, 
    //  HttpMethod.GET, 
    //  "/" + zipCode)
    
    RequestBuilder()
      .url("http://%s/%s".format(ziptasticHost, zipCode))      
      .buildGet
  }
  
  
  private def responseOk(content: String) = {
    response(HttpResponseStatus.OK, content)
  }
  
  private def responseError(error: String) = {
    response(HttpResponseStatus.INTERNAL_SERVER_ERROR, error)
  }
  
  private def response(status:  HttpResponseStatus, 
                       content: String) = {
    
    val resp = 
      new DefaultHttpResponse(HttpVersion.HTTP_1_1, status)    
    
    resp.setContent(copiedBuffer(content, UTF_8)) 
    resp
        
  }

}