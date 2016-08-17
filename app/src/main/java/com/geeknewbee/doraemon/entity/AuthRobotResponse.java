package com.geeknewbee.doraemon.entity;


public class AuthRobotResponse {

    private String token;
    /**
     * username : 164525cb1cba408f9c8cf7acfc6f34ae
     * password : 1b10e57c6dd14159b946ab2fbc4be247
     */

    private HxUserInfo hx_user;

    public String getToken() {
        return token;
    }

    public HxUserInfo getHx_user() {
        return hx_user;
    }


    public static class HxUserInfo {

        /**
         * username : 164525cb1cba408f9c8cf7acfc6f34ae
         * password : 1b10e57c6dd14159b946ab2fbc4be247
         */

        private String username;
        private String password;

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
}

