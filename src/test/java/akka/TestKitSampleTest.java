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
import akka.testkit.JavaTestKit;
import scala.concurrent.duration.Duration;

public class TestKitSampleTest {

    public static class Cache extends UntypedActor {
        ActorRef target = null;

        public void onReceive(Object msg) {

            if (msg.equals("hello")) {
                getSender().tell("world", getSelf());
                if (target != null) target.forward(msg, getContext());

            } else if (msg instanceof ActorRef) {
                target = (ActorRef) msg;
                getSender().tell("done", getSelf());
            }
        }
    }

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testIt() {
    /*
     * Wrap the whole test procedure within a testkit constructor
     * if you want to receive actor replies or use Within(), etc.
     */
        new JavaTestKit(system) {{
            final Props props = Props.create(Cache.class);
            final ActorRef subject = system.actorOf(props);

            // can also use JavaTestKit “from the outside”
            final JavaTestKit probe = new JavaTestKit(system);
            // “inject” the probe by passing it to the test subject
            // like a real resource would be passed in production
            subject.tell(probe.getRef(), getRef());
            // await the correct response
            expectMsgEquals(duration("1 second"), "done");

            // the run() method needs to finish within 3 seconds
//            new Within(duration("3 seconds")) {
//                protected void run() {
//
//                    subject.tell("hello", getRef());
//
//                    // This is a demo: would normally use expectMsgEquals().
//                    // Wait time is bounded by 3-second deadline above.
//                    new AwaitCond() {
//                        protected boolean cond() {
//                            return probe.msgAvailable();
//                        }
//                    };
//
//                    // response must have been enqueued to us before probe
//                    expectMsgEquals(Duration.Zero(), "world");
//                    // check that the probe we injected earlier got the msg
//                    probe.expectMsgEquals(Duration.Zero(), "hello");
//                    Assert.assertEquals(getRef(), probe.getLastSender());
//
//                    // Will wait for the rest of the 3 seconds
//                    expectNoMsg();
//                }
//            };
        }};
    }

}
