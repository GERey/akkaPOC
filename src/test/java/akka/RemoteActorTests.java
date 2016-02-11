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
import com.typesafe.config.ConfigValue;

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
import static org.junit.Assert.fail;


public class RemoteActorTests {

    public static SimpleClient cassandraClient;
    //static ActorSystem system;

    @BeforeClass
    public static void setup() {
        cassandraClient = new SimpleClient();
        cassandraClient.init();

    }

    @AfterClass
    public static void teardown() {
        cassandraClient.close();
    }

    @Test
    public void testRemoteCassandraWorkerSystem(){


        final ActorSystem creatorSystem = startCreatorSystem();
        final ActorSystem cassandraWorkerSystem = startCassandraRemoteWorkerSystem();


        new JavaTestKit(creatorSystem) {{
            final ActorRef creationActor = creatorSystem.actorOf( Props.create( RemoteCreatorActor.class ) ,"creationActor" );


            new Within(duration( "30 seconds" )){

                protected void run() {
                    creationActor.tell( "Blop blop",getRef() );


                    Object returnedResultSets =  receiveOne( Duration.create("32 seconds" ) );
                    if(returnedResultSets == null){
                        fail("should have returned some text");
                    }
                    assertEquals("returned Correctly", returnedResultSets ) ;



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
