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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.ConsistentHashingPool;
import akka.routing.ConsistentHashingRouter;
import akka.routing.RoundRobinPool;
import akka.serialization.Serialization;
import akka.serialization.SerializationExtension;
import akka.serialization.Serializer;
import akka.testkit.JavaTestKit;
import cassandra.SimpleClient;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.typesafe.config.ConfigFactory;


public class ConsistentHashingTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("system" );

    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem( system );
    }


    public static class CacheActor extends UntypedActor {
        Map<String, String> cache = new HashMap<String, String>();


        @Override
        public void onReceive( Object msg ) {
            System.out.println("reached the actor");

            if (msg instanceof Entry) {
                System.out.println("ENTRY LEVEL SON");

                Entry entry = (Entry) msg;
                cache.put(entry.key, entry.value);
            } else if (msg instanceof Get) {
                System.out.println("GET GOT SON");

                Get get = (Get) msg;
                Object value = cache.get(get.key);
                getSender().tell(value == null ? NOT_FOUND : value,
                        getContext().self());
            } else if (msg instanceof Evict) {
                System.out.println("EVICTED SON");

                Evict evict = (Evict) msg;
                cache.remove(evict.key);
            } else {
                System.out.println("failed to recognized son!");

                unhandled(msg);
            }
        }
    }

    public static final class Evict implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String key;
        public Evict(String key) {
            this.key = key;
        }
    }

    public static final class Get implements Serializable, ConsistentHashingRouter.ConsistentHashable {
        private static final long serialVersionUID = 1L;
        public final String key;
        public Get(String key) {
            this.key = key;
        }
        public Object consistentHashKey() {
            return key;
        }
    }

    public static final class Entry implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String key;
        public final String value;
        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public static final String NOT_FOUND = "NOT_FOUND";


    @Test
    public void consistentHashingTest() {



        final ConsistentHashingRouter.ConsistentHashMapper hashMapper = new ConsistentHashingRouter.ConsistentHashMapper() {
            public Object hashKey( Object message ) {
                if ( message instanceof Evict ) {
                    return ( ( Evict ) message ).key;
                }
                else {
                    return null;
                }
            }
        };

        new JavaTestKit( system ) {
            {

                final ActorRef cache = system.actorOf( new ConsistentHashingPool(10).withHashMapper(hashMapper).props(
                        Props.create(CacheActor.class)),"cache");

                cache.tell(
                        new ConsistentHashingRouter.ConsistentHashableEnvelope( new Entry( "hello", "HELLO" ), "hello" ), getRef());

                cache.tell( new ConsistentHashingRouter.ConsistentHashableEnvelope( new Entry( "hi", "HI" ), "hi" ),
                        getRef() );

                cache.tell( new Get( "hello" ), getRef() );
                expectMsgEquals( "HELLO" );

                cache.tell( new Get( "hi" ), getRef() );
                expectMsgEquals( "HI" );

                cache.tell( new Evict( "hi" ), getRef() );
                cache.tell( new Get( "hi" ), getRef() );
                expectMsgEquals( NOT_FOUND );
            }
        };
    }
}
