package io.github.bartoszpop.camel;

import java.util.Iterator;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.ZipFileDataFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootTest
class Camel19670Test {

  @Autowired
  private ProducerTemplate producerTemplate;

  @Test
  void someRoute_conditionMet() {
    // Act
    var response = producerTemplate.requestBody("direct:some", (Object) null);
  }

  @Configuration
  @EnableAutoConfiguration
  static class Config {

    @Bean
    public RouteBuilder sampleRoute() {
      return new EndpointRouteBuilder() {
        @Override
        public void configure() {
          onException(Exception.class)
              .useOriginalMessage();

          from(direct("some"))
              .pollEnrich("file:src/test/resources?include=.*\\.zip&noop=true")
              .unmarshal(multiEntryZipFormat())
              .split(bodyAs(Iterator.class))
              .streaming()
              .log("${body}");
        }
      };
    }

    private ZipFileDataFormat multiEntryZipFormat() {
      var zipFormat = new ZipFileDataFormat();
      zipFormat.setUsingIterator("true");
      return zipFormat;
    }
  }
}
