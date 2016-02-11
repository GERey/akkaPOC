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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datastax.driver.core.ResultSet;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import cassandra.SessionQueryContainer;
import cassandra.SimpleClient;
import scala.concurrent.duration.Duration;
import scala.math.Ordering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class RemoteActorTests {

    public static SimpleClient cassandraClient;
    //static ActorSystem system;

//    @BeforeClass
//    public static void setup() {
//        cassandraClient = new SimpleClient();
//        cassandraClient.init();
//
//    }
//
//    @AfterClass
//    public static void teardown() {
//        cassandraClient.close();
//    }

    @Test
    public void testRemoteCassandraWorkerSystem(){


        final ActorSystem creatorSystem = startCreatorSystem();
        final ActorSystem cassandraWorkerSystem = startCassandraRemoteWorkerSystem();


        new JavaTestKit(creatorSystem) {{
            final ActorRef creationActor = creatorSystem.actorOf( Props.create( RemoteCreatorActor.class ) ,"creationActor" );


            new Within(duration( "3 seconds" )){

                protected void run() {

                    creationActor.tell( "Blop blop", getRef() );


                    Object returnedResultSets =  receiveOne( Duration.create("3 seconds" ) );
                    if(returnedResultSets == null){
                        fail("should have returned some text");
                    }
                    assertEquals("returned Correctly", returnedResultSets ) ;


                }
            };
        }};

    }


    @Test
    public void testThreadPool(){


        final ActorSystem creatorSystem = startCreatorSystem();
        final ActorSystem cassandraWorkerSystem = startCassandraRemoteWorkerSystem();


        new JavaTestKit(creatorSystem) {{
            final ActorRef creationActor = creatorSystem.actorOf( Props.create( RemoteCreatorActor.class ) ,"creationActor" );


            new Within(duration( "3 seconds" )){

                protected void run() {

                    //Send the string in 5 times to see if the actor system will round robin through the remotes

                    for(int i = 0; i<5;i++) {
                        creationActor.tell( "test string", getRef() );
                    }

                    Object[] returnedMessages = receiveN( 5, duration( "3 seconds" ));
                    assertEquals( 5,returnedMessages.length );
                    String[] stringArrayOfActorNames = new String[5];
                    List<String> actorList = new ArrayList<>(  );


                    for(int i= 0;i<5;i++){
                        actorList.add( returnedMessages[i].toString());
                    }


                    //below verifies that 5 differently named threads are created and that they can be accounted for kinda?
                    for(int i = 0; i<5;i++){
                        String threadSubname = "c"+(i+1);

                        for(String actorName : actorList){
                            if(actorName.contains( threadSubname )){
                                actorList.remove( actorName );
                                break;
                            }
                        }
                    }
                    assertEquals( 0,actorList.size() );

                }
            };
        }};

    }

    @Test
    public void testConfigurationLoading(){
        String workerString =  ConfigFactory.load( "application" ).getObject( "akka" ).toConfig().getObject( "actor" ).toConfig().getObject( "deployment" ).toConfig().getObject( "/creationActor/\"*\"" ).toConfig().getString( "remote" );
        assertEquals("akka.tcp://CassandraRemoteWorkerSystem@127.0.0.1:2552",workerString);
    }

    public static ActorSystem startCassandraRemoteWorkerSystem(){
        return ActorSystem.create("CassandraRemoteWorkerSystem", ConfigFactory.load("cassandra"));
    }

    public static ActorSystem startCreatorSystem(){
        return ActorSystem.create( "RemoteCreationSystem",ConfigFactory.load("application") );
    }


}
