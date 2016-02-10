package akka;/*
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


import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import akka.actor.UntypedActor;
import cassandra.SessionQueryContainer;
import cassandra.SimpleClient;


public class CassandraActor extends UntypedActor {
    //SimpleClient cassandraClient = new SimpleClient();

    @Override
    public void onReceive(Object msg) {

        if (msg instanceof SessionQueryContainer ){
            SessionQueryContainer sessionQueryContainer = (SessionQueryContainer) msg;

            //sessionQueryContainer.getCassandraKeyspaceSession().execute( sessionQueryContainer.getCassandraQuery() );

            ResultSet results = sessionQueryContainer.getCassandraKeyspaceSession().execute(sessionQueryContainer.getCassandraQuery());

            getSender().tell( results,getSelf() );

            //prints out the results in the actor.
//            System.out.println(String.format("%-30s\t%-20s\t%-20s\n%s", "title", "album", "artist",
//                    "-------------------------------+-----------------------+--------------------"));
//            for (Row row : results) {
//                System.out.println(String.format("%-30s\t%-20s\t%-20s", row.getString("title"),
//                        row.getString("album"),  row.getString("artist")));
//            }
//            System.out.println();

            getContext().stop(getSelf());
        }
        else
            unhandled(msg);
    }

}



