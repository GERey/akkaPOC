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


/*
The object of this test is to verify that we can read from cassandra using the CassandraActor
 */


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datastax.driver.core.ResultSet;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.serialization.Serialization;
import akka.serialization.SerializationExtension;
import akka.serialization.Serializer;
import akka.testkit.JavaTestKit;
import cassandra.SessionQueryContainer;
import cassandra.SimpleClient;
import scala.concurrent.duration.Duration;

import static org.junit.Assert.assertEquals;


public class akkaCassandraReadTest {

    public static SimpleClient cassandraClient;
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        cassandraClient = new SimpleClient();
        cassandraClient.init();
        system = ActorSystem.create("system" );

    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem( system );
        cassandraClient.close();
    }

    @Test
    public void testCassandraRead(){


//        final SessionQueryContainer sessionQueryContainer = new SessionQueryContainer();
//        sessionQueryContainer.setCassandraKeyspaceSession( cassandraClient.getSession() );
//        sessionQueryContainer.setCassandraQuery( "SELECT * FROM simplex.playlists " +
//                "WHERE id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d;" );


        final String cassandraQueryString = "SELECT * FROM simplex.playlists " +
                "WHERE id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d;";

        new JavaTestKit( system ) {{
            final Props cassandraActorProps = Props.create( CassandraActor.class );
            final ActorRef cassandraActorRef = system.actorOf( cassandraActorProps );

            new Within(duration( "2 seconds" )){

                protected void run() {
                    cassandraActorRef.tell( cassandraQueryString,getRef() );



                    ResultSet returnedResultSets = ( ResultSet ) receiveOne( Duration.create("2 seconds" ) );
                    assertEquals(1,returnedResultSets.all().size() ) ;

                }
            };
        }};

    }

    @Test
    public void serializationTest(){

        // Get the Serialization Extension
        Serialization serialization = SerializationExtension.get(system );


        // Have something to serialize
//        final SessionQueryContainer sessionQueryContainer = new SessionQueryContainer();
//        sessionQueryContainer.setCassandraKeyspaceSession( cassandraClient.getSession() );
//        sessionQueryContainer.setCassandraQuery( "SELECT * FROM simplex.playlists " +
//                "WHERE id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d;" );


        final String cassandraQueryString = "SELECT * FROM simplex.playlists " +
                "WHERE id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d;";


        // Find the Serializer for it
        Serializer serializer = serialization.findSerializerFor(cassandraQueryString );

        // Turn it into bytes
        byte[] bytes = serializer.toBinary(cassandraQueryString);

        // Turn it back into an object,
        // the nulls are for the class manifest and for the classloader
        String back = (String) serializer.fromBinary(bytes);

        // Voilá!
        assertEquals(cassandraQueryString, back);
    }


}
