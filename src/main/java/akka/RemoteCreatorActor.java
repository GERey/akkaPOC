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


import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.AddressFromURIString;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.remote.routing.RemoteRouterConfig;
import akka.routing.ConsistentHashingPool;
import akka.routing.ConsistentHashingRouter;


public class RemoteCreatorActor extends UntypedActor {

    static ActorRef router;




    @Override
    public void preStart() {
        final ConsistentHashingRouter.ConsistentHashMapper hashMapper = new ConsistentHashingRouter.ConsistentHashMapper() {
            public Object hashKey( Object message ) {
                if ( message instanceof String) {
                    return message;
                }
                else {
                    return null;
                }
            }
        };

        final Address[] addresses =  {
                AddressFromURIString.parse( ConfigFactory.load( "application" ).getObject( "akka" ).toConfig()
                                                         .getObject( "actor" ).toConfig().getObject( "deployment" ).toConfig()
                                                         .getObject( "/creationActor/\"*\"" ).toConfig().getString( "remote" ) )
        };

        final RemoteRouterConfig remoteRouterConfig = new RemoteRouterConfig( new ConsistentHashingPool( 5 ).withHashMapper( hashMapper ),addresses); //new ConsistentHashingPool( 5 ).withHashMapper( hashMapper ), addresses);

        router = getContext().actorOf( remoteRouterConfig.props( Props.create( CassandraActor.class ) ) );
        System.out.println("Configured router");
    }

    @Override
    public void onReceive( final Object message ) {



        System.out.println("Recieved router message");

        if(message instanceof String){

             router.forward( message,getContext() );

        }
        else{
            unhandled( message );
        }
    }
}
