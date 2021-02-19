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

package org.fisco.bcos.sdk.client.protocol.response;

import android.util.Base64;
import java.io.IOException;
import java.util.zip.DataFormatException;
import org.fisco.bcos.sdk.client.exceptions.ClientException;
import org.fisco.bcos.sdk.log.Logger;
import org.fisco.bcos.sdk.log.LoggerFactory;
import org.fisco.bcos.sdk.model.DataCompress;
import org.fisco.bcos.sdk.model.JsonRpcResponse;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;

public class BcosTransactionReceiptsDecoder extends JsonRpcResponse<String> {
    private static Logger logger = LoggerFactory.getLogger(BcosTransactionReceiptsDecoder.class);

    public BcosTransactionReceiptsInfo.TransactionReceiptsInfo decodeTransactionReceiptsInfo()
            throws ClientException {
        try {
            String encodedData = getResult();
            // decode the data with base64
            byte[] compressedData = android.util.Base64.decode(encodedData, Base64.NO_WRAP);
            // uncompress the compressed data
            byte[] jsonData = DataCompress.uncompress(compressedData);
            return ObjectMapperFactory.getObjectMapper()
                    .readValue(jsonData, BcosTransactionReceiptsInfo.TransactionReceiptsInfo.class);
        } catch (IOException | DataFormatException e) {
            throw new ClientException("decode receipts failed, error info:" + e.getMessage(), e);
        }
    }
}
