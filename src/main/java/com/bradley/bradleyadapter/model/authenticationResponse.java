 package com.bradley.bradleyadapter.model;

public class authenticationResponse {
    private final String access_token;
     //private final String refresh_token;

 public authenticationResponse(String access_token) {
  this.access_token = access_token;
  //this.refresh_token = refresh_token;
 }

 public String getAccess_token() {
  return access_token;
 }


}
