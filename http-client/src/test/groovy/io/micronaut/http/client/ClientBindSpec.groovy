package io.micronaut.http.client

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = 'spec.name', value = 'ClientBindSpec')
@MicronautTest
class ClientBindSpec extends Specification {

    @Inject BindClient bindClient

    void "test binding to a query value"() {
        expect:
        bindClient.queryValue("xx", Optional.of("yy")) == '[x:xx, y:yy]'
        bindClient.queryValue("xx", Optional.empty()) == '[x:xx]'
    }

    void "test binding to a path value"() {
        expect:
        bindClient.pathValue("xx", Optional.of("yy")) == '/xx/yy'
        bindClient.pathValue("xx", Optional.empty()) == '/xx'
    }

    @Requires(property = 'spec.name', value = 'ClientBindSpec')
    @Client("/bind")
    static interface BindClient {

        @Get("/queryValue")
        String queryValue(@QueryValue String x, @QueryValue Optional<String> y)

        @Get("/pathValue{/x}{/y}")
        String pathValue(String x, Optional<String> y)

    }

    @Requires(property = 'spec.name', value = 'ClientBindSpec')
    @Controller("/bind")
    static class BindController {

        @Get("/queryValue{?params*}")
        String queryValue(Map<String, String> params) {
            params.toString()
        }

        @Get("/pathValue{+path}")
        String pathValue(String path) {
            path
        }
    }
}
