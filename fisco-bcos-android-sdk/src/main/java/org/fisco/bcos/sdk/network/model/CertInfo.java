/*
 * Copyright 2014-2020  [fisco-dev]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package org.fisco.bcos.sdk.network.model;

public class CertInfo {

    private String cerFileName;
    private String p12FileName;
    private String password;
    private Boolean clientAuth;

    public CertInfo(String certPath) {
        this.cerFileName = certPath;
        this.clientAuth = false;
    }

    public CertInfo(String certPath, String p12Path, String password) {
        this.cerFileName = certPath;
        this.p12FileName = p12Path;
        this.password = password;
        this.clientAuth = true;
    }

    public String getCerFileName() {
        return cerFileName;
    }

    public String getP12FileName() {
        return p12FileName;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getClientAuth() {
        return clientAuth;
    }
}
