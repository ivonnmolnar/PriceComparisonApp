package com.example.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@CacheConfig(cacheNames = {"customer"})
public class GetController {

    private final RestTemplate restTemplate;
    private final String[] stores = new String[]{"tesco","sainsburys","morrisons","lidl","aldi"};
    private final HelperMethods helper;

    @Autowired
    public GetController(MeterRegistry registry, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.helper = new HelperMethods(registry,this.restTemplate);
    }

    @GetMapping("/helloworld")
    @ResponseBody
    public ResponseEntity<String> getHelloWorld(@RequestParam(name="response", required=false, defaultValue="200") int response,
                                                @RequestParam(name="latency", required=false, defaultValue="0") int latency ) throws InterruptedException {
        TimeUnit.SECONDS.sleep(latency);
        if (response == 500) {
            return new ResponseEntity<>("Hello World", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        else if (response == 400) {
            return new ResponseEntity<>("Hello World", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Hello World", HttpStatus.OK);
    }

    @GetMapping("/getTesco")
    @ResponseBody
    public Object getTescoFrom3rdPartyAPI() throws JsonProcessingException {
        String url = "http://localhost:8081/tesco";
        String str = restTemplate.getForObject(url, String.class);
        HashMap response = new ObjectMapper().readValue(str, HashMap.class);

        return response.get("99998");
    }

    @GetMapping("/product")
    @ResponseBody
    public Map<String,String> getProduct(@RequestParam(name="id", defaultValue="0") String id,
                                             @RequestParam(name="slow", required=false, defaultValue="false") boolean isSlow,
                                         @RequestParam(name="thirdPartyLatency", required=false, defaultValue="false") boolean isThirdPartyLatency) throws JsonProcessingException {
        if(isSlow){
            return helper.getPricesForProductSlow(id,stores);
        }
        else if(isThirdPartyLatency){
            restTemplate.getForObject("http://localhost:8081/slowStore", String.class);
            return helper.getPricesForProduct(id,stores);
        }

        return helper.getPricesForProduct(id,stores);
    }

    @GetMapping("/cachedProduct")
    @ResponseBody
    @Cacheable
    public Map<String,String> getCachedProduct(@RequestParam(name = "id", defaultValue = "0") String id) throws JsonProcessingException {

        return helper.getPricesForProduct(id,stores);
    }
}
