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


import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import cassandra.SessionQueryContainer;


public class RemoteCreatorActor extends UntypedActor{


    @Override
    public void onReceive( final Object message ) {
        if(message instanceof SessionQueryContainer){
            ActorRef cassandraWorker = getContext().actorOf( Props.create( CassandraActor.class ) );
            cassandraWorker.tell( message,getSelf() );
        }
        else{
            unhandled( message );
        }
    }
}
