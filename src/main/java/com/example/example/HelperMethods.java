package com.example.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class HelperMethods{

    private MeterRegistry meterRegistry;
    public RestTemplate restTemplate;

    @Autowired
    public HelperMethods(MeterRegistry meterRegistry, RestTemplate restTemplate){
        this.meterRegistry = meterRegistry;
        this.restTemplate = restTemplate;
    }

    public String getProductPrice(String store, String id) throws JsonProcessingException {
        String url = String.format("http://localhost:8081/%s",store);
        String str = restTemplate.getForObject(url, String.class);
        Map<String, String> response = new ObjectMapper().readValue(str, HashMap.class);

        return response.get(id);
    }

    public String getProductPriceSlow(String store, String id){
        System.out.println("Here");
        String url = String.format("http://localhost:8081/%s",store);
        String str = restTemplate.getForObject(url, String.class);
        str.substring(1,str.length()-1);
        String[] products = str.split(",");
        for(int i = 0; i < products.length; i++){
            System.out.println(products[i].split(":")[1].substring(1,products[i].split(":")[1].length()-1));
            if(products[i].split(":")[0].substring(1,products[i].split(":")[0].length()-1).equals(id)){
                return products[i].split(":")[1].substring(1,products[i].split(":")[1].length()-1);
            }
        }
        return "0";
    }

    @ResponseBody
    public Map<String, String> getPricesForProduct(String id, String[] stores) throws JsonProcessingException {
        HashMap<String, String> response = new HashMap<>();
        response.put("ID",id);
        for (String store: stores){
            response.put(store,getProductPrice(store,id));
        }

        return response;
    }

    public Map<String, String> getPricesForProductSlow(String id, String[] stores) throws JsonProcessingException {
        HashMap<String, String> response = new HashMap<>();
        response.put("ID",id);
        for (String store: stores){
            response.put(store,getProductPriceSlow(store,id));
        }

        return response;
    }
}
