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


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datastax.driver.core.ResultSet;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import cassandra.SessionQueryContainer;
import cassandra.SimpleClient;
import scala.concurrent.duration.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;


public class RemoteActorTests {

    public static SimpleClient cassandraClient;
    //static ActorSystem system;

    @BeforeClass
    public static void setup() {
        cassandraClient = new SimpleClient();
        cassandraClient.init();
        //system = ActorSystem.create("system" );

    }

    @AfterClass
    public static void teardown() {
       // JavaTestKit.shutdownActorSystem( system );
        cassandraClient.close();
    }

    @Test
    public void testRemoteCassandraWorkerSystem(){

        final ActorSystem cassandraWorkerSystem = startCassandraRemoteWorkerSystem();
        final ActorSystem creatorSystem = startCreatorSystem();

        final SessionQueryContainer sessionQueryContainer = new SessionQueryContainer();
        sessionQueryContainer.setCassandraKeyspaceSession( cassandraClient.getSession() );
        sessionQueryContainer.setCassandraQuery( "SELECT * FROM simplex.playlists " +
                "WHERE id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d;" );




        new JavaTestKit( creatorSystem) {{
            final ActorRef creationActor = creatorSystem.actorOf( Props.create( RemoteCreatorActor.class ) ,"creationActor" );


            new Within(duration( "2 seconds" )){

                protected void run() {
                    creationActor.tell( sessionQueryContainer,getRef() );


                    ResultSet returnedResultSets = ( ResultSet ) receiveOne( Duration.create("2 seconds" ) );
                    assertNotSame( null,returnedResultSets );
                    assertEquals(1,returnedResultSets.all().size() ) ;

                }
            };
        }};

    }

    public static ActorSystem startCassandraRemoteWorkerSystem(){
        return ActorSystem.create("CassandraRemoteWorkerSystem", ConfigFactory.load("cassandra"));
    }

    public static ActorSystem startCreatorSystem(){
        return ActorSystem.create( "RemoteCreationSystem",ConfigFactory.load("application") );
    }


}
