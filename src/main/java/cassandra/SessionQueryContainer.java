package cassandra;/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.datastax.driver.core.Session;


public class SessionQueryContainer {


    public Session cassandraKeyspaceSession;
    public String cassandraQuery;

    public String getCassandraQuery() {
        return cassandraQuery;
    }


    public void setCassandraQuery( final String cassandraQuery ) {
        this.cassandraQuery = cassandraQuery;
    }


    public Session getCassandraKeyspaceSession() {
        return cassandraKeyspaceSession;
    }


    public void setCassandraKeyspaceSession( final Session cassandraKeyspaceSession ) {
        this.cassandraKeyspaceSession = cassandraKeyspaceSession;
    }

}
