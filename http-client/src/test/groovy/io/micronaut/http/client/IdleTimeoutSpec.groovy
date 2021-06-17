package io.micronaut.http.client;

import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.runtime.server.EmbeddedServer
import io.netty.channel.Channel
import io.netty.channel.pool.AbstractChannelPoolMap
import spock.lang.AutoCleanup
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.lang.reflect.Field

@Retry
class IdleTimeoutSpec extends Specification {

  @Shared
  @AutoCleanup
  EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

  def "should close connection according to connection-pool-idle-timeout"() {
    setup:
    ApplicationContext clientContext = ApplicationContext.run(
      'my.port':embeddedServer.getPort(),
      'micronaut.http.client.connection-pool-idle-timeout':'800ms',
      'micronaut.http.client.pool.enabled':true
    )
    ReactorHttpClient httpClient = clientContext.createBean(ReactorHttpClient, embeddedServer.getURL())

    when:"make first request"
    httpClient.retrieve(HttpRequest.GET('/idleTimeout/'),String).blockFirst()
    Deque<Channel> deque = getQueuedChannels(httpClient)
    Channel ch1 = deque.first

    then:"ensure that connection is open as connection-pool-idle-timeout is not reached"
    deque.size() == 1
    ch1.isOpen()
    new PollingConditions(timeout: 1).eventually {
      !ch1.isOpen()
    }

    when:"make another request"
    httpClient.retrieve(HttpRequest.GET('/idleTimeout'),String).blockFirst()
    Channel ch2 = deque.first

    then:"ensure channel 2 is open and channel 2 != channel 1"
    deque.size() == 1
    ch1 != ch2
    ch2.isOpen()
    new PollingConditions(timeout: 1).eventually {
      !ch2.isOpen()
    }

    cleanup:
    httpClient.close()
    clientContext.close()
  }

  def "shouldn't close connection if connection-pool-idle-timeout is not passed"() {
    setup:
    ApplicationContext clientContext = ApplicationContext.run(
      'my.port':embeddedServer.getPort(),
      'micronaut.http.client.pool.enabled':true
    )
    ReactorHttpClient httpClient = clientContext.createBean(ReactorHttpClient, embeddedServer.getURL())

    when:"make first request"
    httpClient.retrieve(HttpRequest.GET('/idleTimeout/'),String).blockFirst()
    Deque<Channel> deque = getQueuedChannels(httpClient)
    Channel ch1 = deque.first

    then:"ensure that connection is open as connection-pool-idle-timeout is not reached"
    deque.size() == 1
    new PollingConditions().eventually {
      deque.first.isOpen()
    }

    when:"make another request"
    httpClient.retrieve(HttpRequest.GET('/idleTimeout'),String).blockFirst()
    Channel ch2 = deque.first

    then:"ensure channel is still open"
    ch1 == ch2
    deque.size() == 1
    new PollingConditions().eventually {
      deque.first.isOpen()
    }

    cleanup:
    httpClient.close()
    clientContext.close()
  }

  Deque getQueuedChannels(ReactorHttpClient client) {
    AbstractChannelPoolMap poolMap = client.poolMap
    Field mapField = AbstractChannelPoolMap.getDeclaredField("map")
    mapField.setAccessible(true)
    Map innerMap = mapField.get(poolMap)
    return innerMap.values().first().deque
  }

  @Controller('/idleTimeout')
  static class GetController {

    @Get(value = "/", produces = MediaType.TEXT_PLAIN)
    String get() {
      return "success"
    }
  }

}
