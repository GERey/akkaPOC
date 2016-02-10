import akka.CassandraActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import cassandra.SessionQueryContainer;
import cassandra.SimpleClient;


public class Main {

    public static SimpleClient cassandraClient;
//
    public static void main(String[] args) {
        cassandraClient = new SimpleClient();
         cassandraClient.init();

        SessionQueryContainer sessionQueryContainer = new SessionQueryContainer();
        sessionQueryContainer.setCassandraKeyspaceSession( cassandraClient.getSession() );
        sessionQueryContainer.setCassandraQuery( "SELECT * FROM simplex.playlists " +
                "WHERE id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d;" );


        ActorSystem system = ActorSystem.create("system");
        ActorRef a = system.actorOf(Props.create( CassandraActor.class ), "cassandraActor" );
        //sets the parent to be A but not entirely sure what it should be. I'm thinking it should be the system
        // parent but don't know how to retrieve that without doing the path manipulation in the actor context
        //which would seem like a drag considering how small this poc is.
        a.tell( sessionQueryContainer,a);


        system.actorOf(Props.create(Terminator.class, a), "terminator");
    }

    public static class Terminator extends UntypedActor {

        private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
        private final ActorRef ref;

        public Terminator(ActorRef ref) {
            this.ref = ref;
            getContext().watch(ref);
        }

        @Override
        public void onReceive(Object msg) {
            if (msg instanceof Terminated) {
                log.info("{} has terminated, shutting down system", ref.path());
                getContext().system().terminate();
            } else {
                unhandled(msg);
            }
        }

    }
}