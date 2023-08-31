package tech.powerjob.remote.akka;

import tech.powerjob.remote.framework.base.ServerType;

/**
 * AkkaConstant
 *
 * @author tjq
 * @since 2022/12/31
 */
public class AkkaConstant {

    public static final String AKKA_CONFIG = "powerjob.akka.conf";

    public static final String WORKER_ACTOR_SYSTEM_NAME = "oms";
    public static final String SERVER_ACTOR_SYSTEM_NAME = "oms-server";

    /**
     * Get actorSystem name
     * @param serverType current server type, powerjob-server is server, powerjob-worker is worker
     * @return actorSystemName
     */
    public static String fetchActorSystemName(ServerType serverType) {


        return serverType == ServerType.SERVER ? SERVER_ACTOR_SYSTEM_NAME : WORKER_ACTOR_SYSTEM_NAME;
    }

}
