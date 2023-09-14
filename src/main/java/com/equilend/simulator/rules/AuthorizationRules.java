package com.equilend.simulator.rules;

import java.util.Map;

public class AuthorizationRules implements Rules{

    public class Authorization{
        
        private String clientId;
        private String clientSecret;
        private String grantType;
        private String username;
        private String password;
        
        public Authorization(String client_id, String client_secret, String grant_type, String username,
                String password) {
            this.clientId = client_id;
            this.clientSecret = client_secret;
            this.grantType = grant_type;
            this.username = username;
            this.password = password;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getGrantType() {
            return grantType;
        }

        public void setGrantType(String grantType) {
            this.grantType = grantType;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }

    private Authorization borrower;
    private Authorization lender;

    public AuthorizationRules(Map<String, Map<String, String>> rulesMap){
        lender = new Authorization("", "", "", "", "");
        borrower = new Authorization("", "", "", "", "");
    }

    public Authorization getBorrower() {
        return borrower;
    }

    public void setBorrower(Authorization borrower) {
        this.borrower = borrower;
    }

    public Authorization getLender() {
        return lender;
    }

    public void setLender(Authorization lender) {
        this.lender = lender;
    }
    
}