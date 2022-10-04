package com.bradley.bradleyadapter.controller;

import com.bradley.bradleyadapter.model.AuthError;
import com.bradley.bradleyadapter.model.Cashnet;
import com.bradley.bradleyadapter.model.authenticationRequest;
import com.bradley.bradleyadapter.model.authenticationResponse;
import com.bradley.bradleyadapter.services.MyUserDetailsService;
import com.bradley.bradleyadapter.util.jwtutil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@RestController
public class bradleyController {
    @Value("${access.url}")
    private String BASE_URL;
    private final RestTemplate restTemplate;
    Logger logger = LoggerFactory.getLogger(bradleyController.class);
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private MyUserDetailsService userDetailsService;
    @Autowired
    private jwtutil jwtTokenUtil;
    //from properties file
    @Value("${access.username}")
    private String USER_NAME1;
    //from properties file
    @Value("${access.password}")
    private String PASSWORD1;

    public bradleyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
   // @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
   @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody authenticationRequest authRequest) throws Exception {
      /*
       try {
           authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
           );
       }catch (BadCredentialsException e){
           throw new Exception("Incorrect username and password", e);
       }
      */
     if(authRequest.getUsername().equals(USER_NAME1) && authRequest.getPassword().equals(PASSWORD1)) {

         final UserDetails userDetails = userDetailsService       //we give the username the user put in the request body and
                 .loadUserByUsername(authRequest.getUsername());      //send it to the loadUserByUsername method of MyUserServicesClass
         //to get the User object with Username, password and Authorities
         final String access_token = jwtTokenUtil.generateToken(userDetails);  //pass the userDetail object to the jwtutil class's generateToken
         //method which calls the creatToken method of the same class to generate
         //the token
         //  final String refresh_token = jwtTokenUtil.generateRefreshToken(userDetails);
         return ResponseEntity.ok(new authenticationResponse(access_token)); //ResponseEntity represents an HTTP response, including headers, body, and status. While @ResponseBody puts the return value into the body of the response, ResponseEntity also allows us to add headers and status code.
     }else{
        AuthError authError= new AuthError();
         //return authError.setAuthenticationError("Wrong User and Password Combination");
         return ResponseEntity.ok(new AuthError());
     }

     }
    @PostMapping({"/test"})
    public String test(){
        return "Test Successful";
    }
    @PostMapping({"/connection_test"})
    public String testConnection() throws Exception {

        String requestUrl = BASE_URL+"/connection_test";
        if (requestUrl == null) {
            throw new Exception("No request url is specified");
        }
        logger.info(requestUrl);
        //requestUrl = requestUrl;
        //System.out.println(requestUrl);

        // return restTemplate.getForObject(requestUrl,String.class);
        return restTemplate.postForObject(requestUrl,null,String.class);
    }
    @RequestMapping(path = "/pull_test", method = RequestMethod.POST,produces = { "application/xml" })
    public //@ResponseBody
    String getData(@RequestParam String cust_code2) {

        String requestUrl = BASE_URL+"/pull_test?cust_code2="+cust_code2;

        logger.info("requestUrl_note {}",requestUrl.toString());
        logger.info("cust_code2 {}",cust_code2.toString());


        return restTemplate.postForObject(requestUrl,null,String.class);

    }
    @PostMapping(value ="push_test_without_validation"
            //,consumes = {"application/xml"}
            //,produces = {"application/json"}
            ,produces="text/html"
    )

    public @ResponseBody
    String getStudentBalance2(@RequestParam Integer cust_code,
                              @RequestParam String description,
                              @RequestParam String term_code,
                              @RequestParam String billno,
                              @RequestParam String busdate,
                              @RequestParam Double amount,
                              @RequestParam String batchno,
                              @RequestParam String cctype,
                              @RequestParam String paymenttype,
                              @RequestParam String paymentcode
    ) throws Exception {

        String requestUrl  = BASE_URL + "/push_test";
        HttpHeaders httpHeaders = new HttpHeaders();
        // httpHeaders.set("Authorization", "Basic " + encodedCredentials);
        //httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> bodyParamMap = new HashMap<>();

        bodyParamMap.put("cust_code", String.valueOf(cust_code));
        bodyParamMap.put("description", description);
        bodyParamMap.put("term_code", term_code);
        bodyParamMap.put("billno", billno);
        bodyParamMap.put("busdate", busdate);
        bodyParamMap.put("amount", String.valueOf(amount));
        bodyParamMap.put("batchno", batchno);
        bodyParamMap.put("cctype", cctype);
        bodyParamMap.put("paymenttype", paymenttype);
        bodyParamMap.put("paymentcode", paymentcode);

        //  ObjectMapper mapper = new ObjectMapper();
        //JsonNode jsonNode = mapper.valueToTree(bodyParamMap);

        ObjectMapper Obj = new ObjectMapper();
        String jsonStr ="";
        try {
            // Converting the Java object into a JSON string
            jsonStr = Obj.writeValueAsString(bodyParamMap);
            // Displaying Java object into a JSON string
            //System.out.println(jsonStr);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        logger.info(jsonStr);

        HttpEntity<String> httpEntity = new HttpEntity<String>(jsonStr,httpHeaders);
        //HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(bodyParamMap, httpHeaders);
        // logger.info(jsonStr);
        //logger.info(httpEntity.toString());
        // transactTransactionalImport;
        ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.POST, httpEntity,String.class);

        //logger.info(jsonNode.toString());
        //return restTemplate.postForEntity(requestUrl, jsonStr,Cashnet.class).getBody();
        // return jsonStr;
        //ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, httpEntity, String.class);
        // check response

        if (response.getStatusCode() == HttpStatus.OK) {
            //System.out.println(response.getBody());
            return  "<cashnet>result=0&respmessage=Successfully Posted</cashnet>";

        } else {
            //System.out.println(response.getStatusCode());
            return "<cashnet>result=100&respmessage=Failed Posted</cashnet>";

        }
    }
    @PostMapping(value ="push_test"
            //,consumes = {"application/xml"}
            //,produces = {"application/json"}
            ,produces="text/html"
    )

    public @ResponseBody
    String pushTestValidation(@RequestParam Integer cust_code,
                              @RequestParam String description,
                              @RequestParam String term_code,
                              @RequestParam String billno,
                              @RequestParam String busdate,
                              @RequestParam Double amount,
                              @RequestParam String batchno,
                              @RequestParam String cctype,
                              @RequestParam String paymenttype,
                              @RequestParam String paymentcode
    ) throws Exception {

        String requestUrl  = BASE_URL + "/push_test";
        String validationUrl= BASE_URL + "/term_code_validation_test";
        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> bodyParamMap = new HashMap<>();

        bodyParamMap.put("cust_code", String.valueOf(cust_code));
        bodyParamMap.put("description", description);
        bodyParamMap.put("term_code", term_code);
        bodyParamMap.put("billno", billno);
        bodyParamMap.put("busdate", busdate);
        bodyParamMap.put("amount", String.valueOf(amount));
        bodyParamMap.put("batchno", batchno);
        bodyParamMap.put("cctype", cctype);
        bodyParamMap.put("paymenttype", paymenttype);
        bodyParamMap.put("paymentcode", paymentcode);

        ObjectMapper Obj = new ObjectMapper();
        String jsonStr ="";
        try {
            // Converting the Java object into a JSON string
            jsonStr = Obj.writeValueAsString(bodyParamMap);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        logger.info(jsonStr);

        HttpEntity<String> httpEntity = new HttpEntity<String>(jsonStr,httpHeaders);

        String validationReference = restTemplate.getForObject(validationUrl,String.class);
        String validationReferenceFormated=validationReference.replace("[","").replace("]","").replace("\"","") ;

        String[] validationReferenceArray = validationReferenceFormated.split(",");
        List<String> validationReferenceList = new ArrayList<>(Arrays.asList(validationReferenceArray));

        if(validationReferenceList.contains(term_code)){
            ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.POST, httpEntity,String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                //System.out.println(response.getBody());
                return  "<cashnet>result=0&respmessage=Successfully Posted</cashnet>";

            } else {
                //System.out.println(response.getStatusCode());
                return "<cashnet>result=100&respmessage=Failed Posted</cashnet>";

            }
        }else{
            return "<cashnet>result=100&respmessage=Failed Posted Invalid Term Code "+term_code+"</cashnet>";
        }

    }

    //BELOW ARE ENDPOINTS FOR TEST, Bradley wants to have prod and test enviroment to go side by side so instead of
    //setting a profile in the application.properties which would only enable us to run a single profile at a time
    //we've setup a separate endpoint for each prod and test
    @RequestMapping(path = "/pull", method = RequestMethod.POST,produces = { "application/xml" })
    public //@ResponseBody
    String getData_prod(@RequestParam String cust_code2) {

        String requestUrl = BASE_URL+"/pull?cust_code2="+cust_code2;

        logger.info("requestUrl_note {}",requestUrl.toString());
        logger.info("cust_code2 {}",cust_code2.toString());


        return restTemplate.postForObject(requestUrl,null,String.class);

    }
    @PostMapping(value ="push"
            //,consumes = {"application/xml"}
            //,produces = {"application/json"}
            ,produces="text/html"
    )

    public @ResponseBody
    String getStudentBalance_prod(@RequestParam Integer cust_code,
                              @RequestParam String description,
                              @RequestParam String term_code,
                              @RequestParam String billno,
                              @RequestParam String busdate,
                              @RequestParam Double amount,
                              @RequestParam String batchno,
                              @RequestParam String cctype,
                              @RequestParam String paymenttype,
                              @RequestParam String paymentcode
    ) throws Exception {

        String requestUrl  = BASE_URL + "/push";
        HttpHeaders httpHeaders = new HttpHeaders();
        // httpHeaders.set("Authorization", "Basic " + encodedCredentials);
        //httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> bodyParamMap = new HashMap<>();

        bodyParamMap.put("cust_code", String.valueOf(cust_code));
        bodyParamMap.put("description", description);
        bodyParamMap.put("term_code", term_code);
        bodyParamMap.put("billno", billno);
        bodyParamMap.put("busdate", busdate);
        bodyParamMap.put("amount", String.valueOf(amount));
        bodyParamMap.put("batchno", batchno);
        bodyParamMap.put("cctype", cctype);
        bodyParamMap.put("paymenttype", paymenttype);
        bodyParamMap.put("paymentcode", paymentcode);

        //  ObjectMapper mapper = new ObjectMapper();
        //JsonNode jsonNode = mapper.valueToTree(bodyParamMap);

        ObjectMapper Obj = new ObjectMapper();
        String jsonStr ="";
        try {
            // Converting the Java object into a JSON string
            jsonStr = Obj.writeValueAsString(bodyParamMap);
            // Displaying Java object into a JSON string
            //System.out.println(jsonStr);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        logger.info(jsonStr);

        HttpEntity<String> httpEntity = new HttpEntity<String>(jsonStr,httpHeaders);
        //HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(bodyParamMap, httpHeaders);
        // logger.info(jsonStr);
        //logger.info(httpEntity.toString());
        // transactTransactionalImport;
        ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.POST, httpEntity,String.class);

        //logger.info(jsonNode.toString());
        //return restTemplate.postForEntity(requestUrl, jsonStr,Cashnet.class).getBody();
        // return jsonStr;
        //ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, httpEntity, String.class);
        // check response

        if (response.getStatusCode() == HttpStatus.OK) {
            //System.out.println(response.getBody());
            return  "<cashnet>result=0&respmessage=Successfully Posted</cashnet>";

        } else {
            //System.out.println(response.getStatusCode());
            return "<cashnet>result=100&respmessage=Failed Posted</cashnet>";

        }
    }
}
