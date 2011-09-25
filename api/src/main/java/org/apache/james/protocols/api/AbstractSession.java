/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.protocols.api;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;


import org.apache.james.protocols.api.FutureResponse.ResponseListener;
import org.apache.james.protocols.api.ProtocolTransport;
import org.apache.james.protocols.api.Response;
import org.slf4j.Logger;

/**
 * Abstract implementation of TLSSupportedSession which use Netty
 * 
 * 
 */
public abstract class AbstractSession implements ProtocolSession {
    /** the Session state */
    final static String SESSION_STATE_MAP = "SESSION_STATE_MAP";

    protected InetSocketAddress socketAddress;
    private Logger logger;
    private SessionLog pLog = null;
    
    protected String user;

    private String id;
    protected ProtocolTransport transport;

    private Map<String, Object> connectionState;

    
    public AbstractSession(Logger logger, ProtocolTransport transport) {
        this.transport = transport;
        this.socketAddress = transport.getRemoteAddress();
        this.logger = logger;
        this.id = transport.getId();
        this.connectionState = new HashMap<String, Object>();
    }

    /**
     * @see org.apache.james.api.protocol.TLSSupportedSession#getRemoteHost()
     */
    public String getRemoteHost() {
        return socketAddress.getHostName();
    }

    /**
     * @see org.apache.james.api.protocol.TLSSupportedSession#getRemoteIPAddress()
     */
    public String getRemoteIPAddress() {
        return socketAddress.getAddress().getHostAddress();
    }

    /**
     * @see org.apache.james.api.protocol.TLSSupportedSession#getUser()
     */
    public String getUser() {
        return user;
    }

    /**
     * @see org.apache.james.api.protocol.TLSSupportedSession#setUser(java.lang.String)
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Return underlying {@link ProtocolTransport}
     * 
     * @return session
     */
    public ProtocolTransport getProtocolTransport() {
        return transport;
    }

    /**
     * @see org.apache.james.api.protocol.TLSSupportedSession#isStartTLSSupported()
     */
    public boolean isStartTLSSupported() {
        return transport.isStartTLSSupported();
    }

    /**
     * @see org.apache.james.api.protocol.TLSSupportedSession#isTLSStarted()
     */
    public boolean isTLSStarted() {
        return transport.isTLSStarted();
    }

    /**
     * @see org.apache.james.api.protocol.ProtocolSession#getLogger()
     */
    public Logger getLogger() {
        if (pLog == null) {
            pLog = new SessionLog(getSessionID(), logger);
        }
        return pLog;
    }
    

    /*
     * (non-Javadoc)
     * @see org.apache.james.api.protocol.ProtocolSession#writeResponse(org.apache.james.api.protocol.Response)
     */
    public void writeResponse(final Response response) {
        if (response instanceof FutureResponse) {
            ((FutureResponse) response).addListener(new ResponseListener() {

                public void onResponse(Response response) {
                    transport.writeResponse(response, AbstractSession.this);
                }
            });
        } else {
            transport.writeResponse(response, this);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.james.protocols.api.ProtocolSession#getSessionID()
     */
    public String getSessionID() {
        return id;
    }
    
    
    /**
     * @see org.apache.james.protocols.smtp.SMTPSession#getConnectionState()
     */
    public Map<String, Object> getConnectionState() {
        return connectionState;
    }

    /**
     * @see org.apache.james.protocols.smtp.SMTPSession#getState()
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getState() {
        Map<String, Object> res = (Map<String, Object>) getConnectionState().get(SESSION_STATE_MAP);
        if (res == null) {
            res = new HashMap<String, Object>();
            getConnectionState().put(SESSION_STATE_MAP, res);
        }
        return res;
    }

}
